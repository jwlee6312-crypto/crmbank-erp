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
        String proc = procedure.toLowerCase();
        
        if (proc.equals("hafn_610u_save") || proc.equals("hafn_620u_save") || proc.equals("hafn_630u_save")) {
            return saveSlip110(params, session);
        }

        log.info("🚀 [hafn] {} 호출: {}", proc, params);
        
        switch (proc) {
            case "hafn_010s_str": return ResponseEntity.ok(hafnMapper.hafn_010s_str(params));
            case "hafn_110s_str": return ResponseEntity.ok(hafnMapper.hafn_110s_str(params));
            case "hafn_120s_str": return ResponseEntity.ok(hafnMapper.hafn_120s_str(params));
            case "hafn_210s_str": return ResponseEntity.ok(hafnMapper.hafn_210s_str(params));
            case "hafn_310s_str": return ResponseEntity.ok(hafnMapper.hafn_310s_str(params));
            case "hafn_410s_str": return ResponseEntity.ok(hafnMapper.hafn_410s_str(params));
            case "hafn_420s_str": return ResponseEntity.ok(hafnMapper.hafn_420s_str(params));
            case "hafn_430s_str": return ResponseEntity.ok(hafnMapper.hafn_430s_str(params));
            case "hafn_510s_str": return ResponseEntity.ok(hafnMapper.hafn_510s_str(params));
            case "hafn_520s_str": return ResponseEntity.ok(hafnMapper.hafn_520s_str(params));
            case "hafn_610u_str": return ResponseEntity.ok(hafnMapper.hafn_610u_str(params));
            case "hafn_620u_str": return ResponseEntity.ok(hafnMapper.hafn_620u_str(params));
            case "hafn_630u_str": return ResponseEntity.ok(hafnMapper.hafn_630u_str(params));
            case "hafn_670s_str": return ResponseEntity.ok(hafnMapper.hafn_670s_str(params));
            case "hafn_690s_str": return ResponseEntity.ok(hafnMapper.hafn_690s_str(params));
            default:
                log.warn("⚠️ 미등록 hafn 프로시저: {}", proc);
                return ResponseEntity.notFound().build();
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
                    detail.put("slipymd", slipymd);
                    detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", actkind);

                    List<Map<String, Object>> detres = haslMapper.hasl_111u_str(detail);
                    if (!detres.isEmpty()) {
                        Map<String, Object> resmap = detres.get(0);
                        if ("y".equalsIgnoreCase(String.valueOf(resmap.get("ret_yn")))) {
                            throw new RuntimeException(String.valueOf(resmap.get("ret_msg")));
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("slipno", slipno);
            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
