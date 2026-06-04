package com.crmbank.erp.hasl.controller;

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

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<Object> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        String proc = procedure.trim().toLowerCase();

        if (proc.equals("hasl_010u_save")) return saveSlip010(params, session);
        if (proc.equals("hasl_110u_save")) return saveSlip110(params, session);

        if (proc.equals("hasl_040s_str")) {
            String rawkeyword = String.valueOf(params.getOrDefault("keyword", "")).trim();
            if (!rawkeyword.isEmpty() && !rawkeyword.equalsIgnoreCase("null")) {
                params.put("keywords", modernTokenize(rawkeyword));
            }
            return ResponseEntity.ok(haslMapper.hasl_040s_str(params));
        }

        log.info("🚀 [hasl] procedure: {}, params: {}", proc, params);
        
        switch (proc) {
            case "hasl_010u_str": return ResponseEntity.ok(haslMapper.hasl_010u_str(params));
            case "hasl_011u_str": return ResponseEntity.ok(haslMapper.hasl_011u_str(params));
            case "hasl_020u_str": return ResponseEntity.ok(haslMapper.hasl_020u_str(params));
            case "hasl_110u_str": return ResponseEntity.ok(haslMapper.hasl_110u_str(params));
            case "hasl_111u_str": return ResponseEntity.ok(haslMapper.hasl_111u_str(params));
            case "hasl_030s_str": return ResponseEntity.ok(haslMapper.hasl_030s_str(params));
            case "hasl_120s_str": return ResponseEntity.ok(haslMapper.hasl_120s_str(params));
            case "hasl_130s_str": return ResponseEntity.ok(haslMapper.hasl_130s_str(params));
            case "hasl_050u_master": return ResponseEntity.ok(haslMapper.hasl_050u_master(params));
            case "hasl_050u_str": return ResponseEntity.ok(haslMapper.hasl_050u_str(params));
            case "hasl_510s_str": return ResponseEntity.ok(haslMapper.hasl_510s_str(params));
            case "hasl_520s_str": return ResponseEntity.ok(haslMapper.hasl_520s_str(params));
            case "hasl_530s_str": return ResponseEntity.ok(haslMapper.hasl_530s_str(params));
            case "hasl_540s_str": return ResponseEntity.ok(haslMapper.hasl_540s_str(params));
            case "hasl_550s_str": return ResponseEntity.ok(haslMapper.hasl_550s_str(params));
            case "hasl_560s_str": return ResponseEntity.ok(haslMapper.hasl_560s_str(params));
            case "hasl_610s_str": return ResponseEntity.ok(haslMapper.hasl_610s_str(params));
            case "get_slip_list": return ResponseEntity.ok(haslMapper.get_slip_list(params));
            case "hasl_630s_str": return ResponseEntity.ok(haslMapper.hasl_630s_str(params));
            case "hasl_710s_str": return ResponseEntity.ok(haslMapper.hasl_710s_str(params));
            default:
                log.warn("❌ [hasl] Unregistered procedure: {}", proc);
                return ResponseEntity.notFound().build();
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
    public ResponseEntity<Object> saveSlip010(Map<String, Object> payload, HttpSession session) {
        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("master");
            List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("details");
            String actkind = (String) payload.get("actkind");
            injectSession(master, session);
            master.put("actkind", actkind);
            List<Map<String, Object>> masterresult = haslMapper.hasl_010u_str(master);
            if (masterresult.isEmpty()) throw new RuntimeException("마스터 저장 실패");
            String slipno = String.valueOf(masterresult.get(0).get("slipno"));
            String slipymd = String.valueOf(master.get("slipymd"));
            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("slipymd", slipymd); detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", "d".equals(actkind) ? "d" : detail.getOrDefault("upkind", "a"));
                    haslMapper.hasl_011u_str(detail);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("slipno", slipno); response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error"); error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Transactional
    public ResponseEntity<Object> saveSlip110(Map<String, Object> payload, HttpSession session) {
        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("master");
            List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("details");
            String actkind = (String) payload.get("actkind");
            injectSession(master, session);
            master.put("actkind", actkind);
            List<Map<String, Object>> masterresult = haslMapper.hasl_110u_str(master);
            if (masterresult.isEmpty()) throw new RuntimeException("마스터 저장 실패");
            String slipno = String.valueOf(masterresult.get(0).get("slipno"));
            String slipymd = String.valueOf(master.get("slipymd"));
            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("slipymd", slipymd); detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", "d".equals(actkind) ? "d" : detail.getOrDefault("upkind", "a"));
                    haslMapper.hasl_111u_str(detail);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("slipno", slipno); response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error"); error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
