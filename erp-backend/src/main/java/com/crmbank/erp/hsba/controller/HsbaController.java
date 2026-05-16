package com.crmbank.erp.hsba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsba.mapper.HsbaMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsba")
@RequiredArgsConstructor
public class HsbaController {

    private final HsbaMapper hsbaMapper;

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

            case "HSBA_065U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_065U_STR(params));
            case "HSBA_090U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_090U_STR(params));
            case "HSBA_140U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_140U_STR(params));
            case "HSBA_701U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_701U_STR(params));
            case "HSBA_711U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_711U_STR(params));
            case "HSBA_721U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_721U_STR(params));
            case "HSBA_730U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_730U_STR(params));
            case "HSBA_740U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_740U_STR(params));
            case "HSBA_750U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_750U_STR(params));
            case "HSBA_800U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_800U_STR(params));
            case "HSBA_810U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_810U_STR(params));
            case "HSBA_820U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_820U_STR(params));

            default:
                return ResponseEntity.notFound().build();
        }
    }
}
