package com.crmbank.erp.hp00.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hp00.mapper.Hp00Mapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/hp00")
@RequiredArgsConstructor
public class Hp00Controller {

    private final Hp00Mapper hp00Mapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            params.putIfAbsent("CMPYCD", user.getCMPYCD());
        }
    }

    @GetMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedureGet(
            @PathVariable String procedure,
            @RequestParam Map<String, Object> params,
            HttpSession session) {
        Map<String, Object> p = new HashMap<>(params);
        return execute(procedure, p, session);
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedurePost(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        return execute(procedure, params, session);
    }

    private ResponseEntity<List<Map<String, Object>>> execute(String procedure, Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("📋 [HP00] 호출: {}", procedure);

        if ("HP00_000S_STR".equalsIgnoreCase(procedure)) {
            return ResponseEntity.ok(hp00Mapper.HP00_000S_STR(params));
        }
        return ResponseEntity.notFound().build();
    }
}
