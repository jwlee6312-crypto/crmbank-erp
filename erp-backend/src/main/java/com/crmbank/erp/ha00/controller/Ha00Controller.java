package com.crmbank.erp.ha00.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.ha00.mapper.Ha00Mapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/ha00")
@RequiredArgsConstructor
public class Ha00Controller {

    private final Ha00Mapper ha00Mapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            params.putIfAbsent("CMPYCD", user.getCMPYCD());
        }
        // 파라미터 기본값 보정
        params.putIfAbsent("GBNCD", "");
        params.putIfAbsent("SEARCH", "");
        params.putIfAbsent("CODE", "");
    }

    @GetMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeGet(
            @PathVariable String procedure, @RequestParam Map<String, Object> params, HttpSession session) {
        return execute(procedure, new HashMap<>(params), session);
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executePost(
            @PathVariable String procedure, @RequestBody Map<String, Object> params, HttpSession session) {
        return execute(procedure, params, session);
    }

    private ResponseEntity<List<Map<String, Object>>> execute(String procedure, Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("📋 [HA00] SSMS 실행용: {}", buildSsmsLog(procedure, params));
        if ("HA00_00P_STR".equalsIgnoreCase(procedure)) {
            return ResponseEntity.ok(ha00Mapper.HA00_00P_STR(params));
        } else if ("HA00_010S_STR".equalsIgnoreCase(procedure)) {
            return ResponseEntity.ok(ha00Mapper.HA00_010S_STR(params));
        }
        return ResponseEntity.notFound().build();
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        String[] keys = {"GUBUN", "CMPYCD", "GBNCD", "SEARCH", "ETC"};
        String values = java.util.Arrays.stream(keys)
                .map(key -> {
                    Object val = params.get(key);
                    return val == null ? "''" : "'" + val.toString().trim() + "'";
                })
                .collect(Collectors.joining(", "));
        return String.format("EXEC %s %s", procedure.toUpperCase(), values);
    }
}
