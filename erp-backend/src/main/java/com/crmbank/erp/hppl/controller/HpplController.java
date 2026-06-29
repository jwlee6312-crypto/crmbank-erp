package com.crmbank.erp.hppl.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hppl.mapper.HpplMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hppl")
@RequiredArgsConstructor
public class HpplController {

    private final HpplMapper hpplMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.put("cmpycd", user.getCmpycd());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        injectSession(params, session);
        log.info("🚀 [HPPL] {} 호출: {}", procedure, params);

        switch (procedure.toUpperCase()) {
            case "HPPL_100U_STR": return ResponseEntity.ok(hpplMapper.HPPL_100U_STR(params));
            case "HPPL_110S_STR": return ResponseEntity.ok(hpplMapper.HPPL_110S_STR(params));
            case "HPPL_120U_STR": return ResponseEntity.ok(hpplMapper.HPPL_120U_STR(params));
            case "HPPL_150U_STR": return ResponseEntity.ok(hpplMapper.HPPL_150U_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }
}
