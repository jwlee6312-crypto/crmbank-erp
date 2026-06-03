package com.crmbank.erp.hpcl.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hpcl.mapper.HpclMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hpcl")
@RequiredArgsConstructor
public class HpclController {

    private final HpclMapper hpclMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        log.info("🚀 [HPCL] {} 호출: {}", procedure, params);
        
        switch (procedure.toUpperCase()) {
            case "HPCL_100U_STR": return ResponseEntity.ok(hpclMapper.HPCL_100U_STR(params));
            case "HPCL_110U_STR": return ResponseEntity.ok(hpclMapper.HPCL_110U_STR(params));
            case "HPCL_200S_STR": return ResponseEntity.ok(hpclMapper.HPCL_200S_STR(params));
            case "HPCL_210S_STR": return ResponseEntity.ok(hpclMapper.HPCL_210S_STR(params));
            case "HPCL_220S_STR": return ResponseEntity.ok(hpclMapper.HPCL_220S_STR(params));
            case "HPCL_230S_STR": return ResponseEntity.ok(hpclMapper.HPCL_230S_STR(params));
            case "HPCL_240S_STR": return ResponseEntity.ok(hpclMapper.HPCL_240S_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }
}
