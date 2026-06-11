package com.crmbank.erp.hasl.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hasl.mapper.HaslMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/hasl")
@RequiredArgsConstructor
public class HaslController {

    private final HaslMapper haslMapper;

    @PostMapping("/{procedure}")
    public Object executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        String proc = procedure.trim().toLowerCase();

        // 🚀 복합 저장 로직 (ApiResponse 반환)
        if (proc.equals("hasl_010u_save")) return saveSlip010(params, session);
        if (proc.equals("hasl_110u_save")) return saveSlip110(params, session);

        // 🚀 단순 조회 로직 (List 반환)
        if (proc.equals("hasl_040s_str")) {
            String rawkeyword = String.valueOf(params.getOrDefault("keyword", "")).trim();
            if (!rawkeyword.isEmpty() && !rawkeyword.equalsIgnoreCase("null")) {
                params.put("keywords", modernTokenize(rawkeyword));
            }
            return haslMapper.HASL_040S_STR(params);
        }

        log.info("🚀 [hasl] procedure: {}, params: {}", proc, params);
        
        switch (proc) {
            case "hasl_010u_str": return haslMapper.HASL_010U_STR(params);
            case "hasl_011u_str": return haslMapper.HASL_011U_STR(params);
            case "hasl_020u_str": return haslMapper.HASL_020U_STR(params);
            case "hasl_030s_str": return haslMapper.HASL_030S_STR(params);
            case "hasl_050u_master": return haslMapper.HASL_050U_MASTER(params);
            case "hasl_050u_str": return haslMapper.HASL_050U_STR(params);
            case "hasl_110u_str": return haslMapper.HASL_110U_STR(params);
            case "hasl_111u_str": return haslMapper.HASL_111U_STR(params);
            case "hasl_120s_str": return haslMapper.HASL_120S_STR(params);
            case "hasl_130s_str": return haslMapper.HASL_130S_STR(params);
            case "hasl_510s_str": return haslMapper.HASL_510S_STR(params);
            case "hasl_520s_str": return haslMapper.HASL_520S_STR(params);
            case "hasl_530s_str": return haslMapper.HASL_530S_STR(params);
            case "hasl_540s_str": return haslMapper.HASL_540S_STR(params);
            case "hasl_550s_str": return haslMapper.HASL_550S_STR(params);
            case "hasl_560s_str": return haslMapper.HASL_560S_STR(params);
            case "hasl_610s_str": return haslMapper.HASL_610S_STR(params);
            case "hasl_620s_str": return haslMapper.HASL_620S_STR(params);
            case "hasl_630s_str": return haslMapper.HASL_630S_STR(params);
            case "hasl_710s_str": return haslMapper.HASL_710S_STR(params);
            default:
                log.warn("❌ [hasl] Unregistered procedure: {}", proc);
                return null;
        }
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    private List<String> modernTokenize(String query) {
        if (query == null || query.trim().isEmpty()) return List.of();
        String cleaned = query.replaceAll("[\\.,\\?\\!\\(\\)\\[\\]]", " ")
                             .replaceAll("[0-9]", "")
                             .replaceAll("원", "")
                             .replaceAll("\\s+", " ").trim();
        String particleregex = "(은|는|이|가|을|를|과|와|로|으로|에서|에게|의|도|만|까지|부터|하고|했다|하며|하였다|했으며|하였습니다|하였으며|이다|입니다|습니까|니까|함|했고|있다|았다|었다|들과|들의|같이)$";
        Pattern pattern = Pattern.compile(particleregex);
        return Arrays.stream(cleaned.split(" "))
                .map(word -> {
                    String prev = ""; String curr = word;
                    while(!prev.equals(curr)) { prev = curr; curr = pattern.matcher(curr).replaceAll(""); }
                    return curr;
                })
                .filter(word -> word.length() >= 1)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseEntity<ApiResponse<?>> saveSlip010(Map<String, Object> payload, HttpSession session) {
        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("master");
            List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("details");
            String actkind = (String) payload.get("actkind");
            injectSession(master, session);
            master.put("actkind", actkind);
            List<Map<String, Object>> masterresult = haslMapper.HASL_010U_STR(master);
            if (masterresult.isEmpty()) throw new RuntimeException("마스터 저장 실패");
            String slipno = String.valueOf(masterresult.get(0).get("slipno"));
            String slipymd = String.valueOf(master.get("slipymd"));
            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("slipymd", slipymd); detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", "d".equals(actkind) ? "d" : detail.getOrDefault("upkind", "a"));
                    haslMapper.HASL_011U_STR(detail);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("slipno", slipno);
            return ResponseEntity.ok(ApiResponse.success(response, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<ApiResponse<?>> saveSlip110(Map<String, Object> payload, HttpSession session) {
        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("master");
            List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("details");
            String actkind = (String) payload.get("actkind");
            injectSession(master, session);
            master.put("actkind", actkind);
            List<Map<String, Object>> masterresult = haslMapper.HASL_110U_STR(master);
            if (masterresult.isEmpty()) throw new RuntimeException("마스터 저장 실패");
            String slipno = String.valueOf(masterresult.get(0).get("slipno"));
            String slipymd = String.valueOf(master.get("slipymd"));
            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("slipymd", slipymd); detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", "d".equals(actkind) ? "d" : detail.getOrDefault("upkind", "a"));
                    haslMapper.HASL_111U_STR(detail);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("slipno", slipno);
            return ResponseEntity.ok(ApiResponse.success(response, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }
}
