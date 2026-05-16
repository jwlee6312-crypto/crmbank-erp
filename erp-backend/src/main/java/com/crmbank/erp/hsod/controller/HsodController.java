package com.crmbank.erp.hsod.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsod.mapper.HsodMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsod")
@RequiredArgsConstructor
public class HsodController {

    private final HsodMapper hsodMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            if (!params.containsKey("CMPYCD")) params.put("CMPYCD", user.getCMPYCD());
            if (!params.containsKey("USERID")) params.put("USERID", user.getUSERID());
            params.put("UPDEMP", user.getUSERID());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        log.info("🚀 [HSOD] {} 호출: {}", procedure, params);
        
        switch (procedure.toUpperCase()) {
            case "HSOD_100U_STR": return ResponseEntity.ok(hsodMapper.HSOD_100U_STR(params));
            case "HSOD_101U_STR": return ResponseEntity.ok(hsodMapper.HSOD_101U_STR(params));
            case "HSOD_110S_STR": return ResponseEntity.ok(hsodMapper.HSOD_110S_STR(params));
            case "HSOD_120U_STR": return ResponseEntity.ok(hsodMapper.HSOD_120U_STR(params));
            case "HSOD_210U_STR": return ResponseEntity.ok(hsodMapper.HSOD_210U_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }
}
