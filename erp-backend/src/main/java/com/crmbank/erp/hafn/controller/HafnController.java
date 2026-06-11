package com.crmbank.erp.hafn.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hafn.mapper.HafnMapper;
import com.crmbank.erp.hasl.mapper.HaslMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/hafn")
@RequiredArgsConstructor
public class HafnController {

    private final HafnMapper hafnMapper;
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
    public Object executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        String proc = procedure.toUpperCase();
        
        // 🚀 복합 저장 로직 (ApiResponse 반환)
        if (proc.equals("HAFN_610U_SAVE") || proc.equals("HAFN_620U_SAVE") || proc.equals("HAFN_630U_SAVE")) {
            return saveSlip110(params, session);
        }

        log.info("🚀 [hafn] {} 호출: {}", proc, params);
        
        // 🚀 단순 조회 로직 (List 반환)
        switch (proc) {
            case "HAFN_010S_STR": return hafnMapper.HAFN_010S_STR(params);
            case "HAFN_110S_STR": return hafnMapper.HAFN_110S_STR(params);
            case "HAFN_120S_STR": return hafnMapper.HAFN_120S_STR(params);
            case "HAFN_210S_STR": return hafnMapper.HAFN_210S_STR(params);
            case "HAFN_310S_STR": return hafnMapper.HAFN_310S_STR(params);
            case "HAFN_410S_STR": return hafnMapper.HAFN_410S_STR(params);
            case "HAFN_420S_STR": return hafnMapper.HAFN_420S_STR(params);
            case "HAFN_430S_STR": return hafnMapper.HAFN_430S_STR(params);
            case "HAFN_510S_STR": return hafnMapper.HAFN_510S_STR(params);
            case "HAFN_520S_STR": return hafnMapper.HAFN_520S_STR(params);
            case "HAFN_610U_STR": return hafnMapper.HAFN_610U_STR(params);
            case "HAFN_620U_STR": return hafnMapper.HAFN_620U_STR(params);
            case "HAFN_630U_STR": return hafnMapper.HAFN_630U_STR(params);
            case "HAFN_670S_STR": return hafnMapper.HAFN_670S_STR(params);
            case "HAFN_680S_STR": return hafnMapper.HAFN_680S_STR(params);
            case "HAFN_690S_STR": return hafnMapper.HAFN_690S_STR(params);
            default:
                return null;
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
            
            String slipno = String.valueOf(masterresult.get(0).get("SLIPNO") != null ? masterresult.get(0).get("SLIPNO") : masterresult.get(0).get("slipno"));
            String slipymd = String.valueOf(master.get("slipymd"));

            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("slipymd", slipymd);
                    detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", actkind);

                    List<Map<String, Object>> detres = haslMapper.HASL_111U_STR(detail);
                    if (!detres.isEmpty()) {
                        Map<String, Object> resmap = detres.get(0);
                        if ("Y".equalsIgnoreCase(String.valueOf(resmap.get("RET_YN") != null ? resmap.get("RET_YN") : resmap.get("ret_yn")))) {
                            throw new RuntimeException(String.valueOf(resmap.get("RET_MSG") != null ? resmap.get("RET_MSG") : resmap.get("ret_msg")));
                        }
                    }
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
