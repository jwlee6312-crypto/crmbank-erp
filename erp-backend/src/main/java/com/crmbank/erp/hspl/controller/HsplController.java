package com.crmbank.erp.hspl.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hspl.mapper.HsplMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hspl")
@RequiredArgsConstructor
public class HsplController {

    private final HsplMapper hsplMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            params.put("CMPYCD", user.getCMPYCD());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        injectSession(params, session);
        log.info("🚀 [HsPL] {} 호출: {}", procedure, params);

        switch (procedure.toUpperCase()) {
            case "HSPL_100U_STR": return ResponseEntity.ok(hsplMapper.HSPL_100U_STR(params));
            case "HSPL_110U_STR": return ResponseEntity.ok(hsplMapper.HSPL_110U_STR(params));
            case "HSPL_200S_STR": return ResponseEntity.ok(hsplMapper.HSPL_200S_STR(params));
            case "HSPL_210S_STR": return ResponseEntity.ok(hsplMapper.HSPL_210S_STR(params));
            case "HSPL_220S_STR": return ResponseEntity.ok(hsplMapper.HSPL_220S_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }
}

