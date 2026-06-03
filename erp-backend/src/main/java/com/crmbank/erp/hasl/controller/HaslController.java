package com.crmbank.erp.hasl.controller;

import com.crmbank.erp.comm.dto.UserSession;
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

    /**
     * 일반 프로시저 실행 (조회 등)
     */
    @PostMapping("/{procedure}")
    public ResponseEntity<Object> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        
        // SAVE 로직은 별도 메소드에서 처리
        if (procedure.equalsIgnoreCase("HASL_010U_SAVE")) {
            return saveSlip010(params, session);
        }
        if (procedure.equalsIgnoreCase("HASL_110U_SAVE")) {
            return saveSlip110(params, session);
        }

        log.info("🚀 [HASL] {} 호출: {}", procedure, params);
        
        switch (procedure.toUpperCase()) {
            case "HASL_010U_STR": return ResponseEntity.ok(haslMapper.HASL_010U_STR(params));
            case "HASL_011U_STR": return ResponseEntity.ok(haslMapper.HASL_011U_STR(params));
            case "HASL_020U_STR": return ResponseEntity.ok(haslMapper.HASL_020U_STR(params));
            case "HASL_110U_STR": return ResponseEntity.ok(haslMapper.HASL_110U_STR(params));
            case "HASL_111U_STR": return ResponseEntity.ok(haslMapper.HASL_111U_STR(params));
            case "HASL_030S_STR": return ResponseEntity.ok(haslMapper.HASL_030S_STR(params));
            case "HASL_120S_STR": return ResponseEntity.ok(haslMapper.HASL_120S_STR(params));
            case "HASL_130S_STR": return ResponseEntity.ok(haslMapper.HASL_130S_STR(params));
            default:
                log.warn("⚠️ 미등록 HASL 프로시저: {}", procedure);
                return ResponseEntity.notFound().build();
        }
    }

    /**
     * 현업전표등록(010U) 통합 저장 로직
     */
    @Transactional
    public ResponseEntity<Object> saveSlip010(Map<String, Object> payload, HttpSession session) {
        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("MASTER");
            List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("DETAILS");
            String actKind = (String) payload.get("actkind");

            injectSession(master, session);

            // 1. 전표 마스터 저장
            master.put("actkind", actKind);
            List<Map<String, Object>> masterResult = haslMapper.HASL_010U_STR(master);
            
            if (masterResult.isEmpty()) throw new RuntimeException("마스터 저장 실패");
            
            String slipNo = String.valueOf(masterResult.get(0).get("slipno"));
            String slipYmd = String.valueOf(master.get("slipymd"));

            // 2. 전표 상세 저장
            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("slipymd", slipYmd);
                    detail.put("slipno", slipNo);
                    detail.put("acctymd", master.get("acctymd"));
                    
                    if ("D".equals(actKind)) {
                        detail.put("actkind", "D");
                    } else {
                        String rowKind = (String) detail.getOrDefault("upkind", "A");
                        detail.put("actkind", rowKind);
                    }

                    List<Map<String, Object>> detRes = haslMapper.HASL_011U_STR(detail);
                    if (!detRes.isEmpty()) {
                        Map<String, Object> resMap = detRes.get(0);
                        if ("Y".equals(resMap.get("ret_yn"))) {
                            throw new RuntimeException(String.valueOf(resMap.get("ret_msg")));
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("slipno", slipNo);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 010U 저장 오류: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 경리전표입력(110U) 통합 저장 로직
     */
    @Transactional
    public ResponseEntity<Object> saveSlip110(Map<String, Object> payload, HttpSession session) {
        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("MASTER");
            List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("DETAILS");
            String actKind = (String) payload.get("actkind");

            injectSession(master, session);

            // 1. 전표 마스터 저장
            master.put("actkind", actKind);
            List<Map<String, Object>> masterResult = haslMapper.HASL_110U_STR(master);
            
            if (masterResult.isEmpty()) throw new RuntimeException("마스터 저장 실패");
            
            String slipno = String.valueOf(masterResult.get(0).get("slipno"));
            String slipymd = String.valueOf(master.get("slipymd"));

            // 2. 전표 상세 저장
            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("slipymd", slipymd);
                    detail.put("slipno", slipno);
                    detail.put("ACCTYMD", master.get("ACCTYMD"));
                    
                    if ("D".equals(actKind)) {
                        detail.put("actkind", "D");
                    } else {
                        String rowKind = (String) detail.getOrDefault("upkind", "A");
                        detail.put("actkind", rowKind);
                    }

                    List<Map<String, Object>> detRes = haslMapper.HASL_111U_STR(detail);
                    if (!detRes.isEmpty()) {
                        Map<String, Object> resMap = detRes.get(0);
                        if ("Y".equals(resMap.get("ret_yn"))) {
                            throw new RuntimeException(String.valueOf(resMap.get("ret_msg")));
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("slipno", slipno);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 110U 저장 오류: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
