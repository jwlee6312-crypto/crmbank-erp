package com.crmbank.erp.hpba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hpba.mapper.HpbaMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hpba")
@RequiredArgsConstructor
public class HpbaController {

    private final HpbaMapper hpbaMapper;

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
        log.info("🚀 [HPBA] {} 호출: {}", procedure, params);
        
        switch (procedure.toUpperCase()) {
            case "HPBA_100U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_100U_STR(params));
            case "HPBA_130U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_130U_STR(params));
            case "HPBA_200U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_200U_STR(params));
            case "HPBA_210U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_210U_STR(params));
            case "HPBA_800U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_800U_STR(params));
            case "HPBA_810U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_810U_STR(params));
            case "HPBA_820U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_820U_STR(params));
            case "HPBA_830U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_830U_STR(params));
            case "HPBA_840U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_840U_STR(params));
            case "HPBA_900U_STR": return ResponseEntity.ok(hpbaMapper.HPBA_900U_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }
}
