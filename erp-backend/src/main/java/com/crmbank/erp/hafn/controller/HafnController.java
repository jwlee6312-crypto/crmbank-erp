package com.crmbank.erp.hafn.controller;

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
    public ResponseEntity<Object> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        
        if (procedure.equalsIgnoreCase("HAFN_610U_SAVE")) {
            return saveSlip110(params, session); // 미지급금 지불
        }
        if (procedure.equalsIgnoreCase("HAFN_620U_SAVE")) {
            return saveSlip110(params, session); // 카드 지불
        }
        if (procedure.equalsIgnoreCase("HAFN_630U_SAVE")) {
            return saveSlip110(params, session); // 선급비용 상계
        }

        log.info("🚀 [HAFN] {} 호출: {}", procedure, params);
        
        switch (procedure.toUpperCase()) {
            case "HAFN_010S_STR": return ResponseEntity.ok(hafnMapper.HAFN_010S_STR(params));
            case "HAFN_110S_STR": return ResponseEntity.ok(hafnMapper.HAFN_110S_STR(params));
            case "HAFN_120S_STR": return ResponseEntity.ok(hafnMapper.HAFN_120S_STR(params));
            case "HAFN_210S_STR": return ResponseEntity.ok(hafnMapper.HAFN_210S_STR(params));
            case "HAFN_310S_STR": return ResponseEntity.ok(hafnMapper.HAFN_310S_STR(params));
            case "HAFN_410S_STR": return ResponseEntity.ok(hafnMapper.HAFN_410S_STR(params));
            case "HAFN_420S_STR": return ResponseEntity.ok(hafnMapper.HAFN_420S_STR(params));
            case "HAFN_430S_STR": return ResponseEntity.ok(hafnMapper.HAFN_430S_STR(params));
            case "HAFN_510S_STR": return ResponseEntity.ok(hafnMapper.HAFN_510S_STR(params));
            case "HAFN_520S_STR": return ResponseEntity.ok(hafnMapper.HAFN_520S_STR(params));
            case "HAFN_610U_STR": return ResponseEntity.ok(hafnMapper.HAFN_610U_STR(params));
            case "HAFN_620U_STR": return ResponseEntity.ok(hafnMapper.HAFN_620U_STR(params));
            case "HAFN_630U_STR": return ResponseEntity.ok(hafnMapper.HAFN_630U_STR(params));
            case "HAFN_670S_STR": return ResponseEntity.ok(hafnMapper.HAFN_670S_STR(params));
            case "HAFN_690S_STR": return ResponseEntity.ok(hafnMapper.HAFN_690S_STR(params));
            default:
                log.warn("⚠️ 미등록 HAFN 프로시저: {}", procedure);
                return ResponseEntity.notFound().build();
        }
    }

    /**
     * 경리전표(110U) 형식의 통합 저장 로직
     */
    @Transactional
    public ResponseEntity<Object> saveSlip110(Map<String, Object> payload, HttpSession session) {
        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("MASTER");
            List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("DETAILS");
            String actKind = (String) payload.get("ACTKIND");

            injectSession(master, session);
            master.put("ACTKIND", actKind);

            // 1. 전표 마스터 저장 (HASL_110U_STR)
            List<Map<String, Object>> masterResult = haslMapper.HASL_110U_STR(master);
            if (masterResult.isEmpty()) throw new RuntimeException("마스터 저장 실패");
            
            String slipNo = String.valueOf(masterResult.get(0).get("SLIPNO"));
            String slipYmd = String.valueOf(master.get("SLIPYMD"));

            // 2. 전표 상세 저장 (HASL_111U_STR)
            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("SLIPYMD", slipYmd);
                    detail.put("SLIPNO", slipNo);
                    detail.put("ACCTYMD", master.get("ACCTYMD"));
                    detail.put("ACTKIND", actKind);

                    List<Map<String, Object>> detRes = haslMapper.HASL_111U_STR(detail);
                    if (!detRes.isEmpty()) {
                        Map<String, Object> resMap = detRes.get(0);
                        if ("Y".equals(resMap.get("RET_YN"))) {
                            throw new RuntimeException(String.valueOf(resMap.get("RET_MSG")));
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("SLIPNO", slipNo);
            response.put("STATUS", "SUCCESS");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ HAFN 전표 저장 오류: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("STATUS", "ERROR");
            error.put("MESSAGE", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
