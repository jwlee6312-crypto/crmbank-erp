package com.crmbank.erp.hs00.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hs00.mapper.Hs00Mapper;
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
@RequestMapping("/api/hs00")
@RequiredArgsConstructor
public class Hs00Controller {

    private final Hs00Mapper hs00Mapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            params.putIfAbsent("CMPYCD", user.getCMPYCD());
        }
        // 💡 파라미터 NULL 방지 로직 추가 (빈 값 주입)
        params.putIfAbsent("GUBUN", "");
        params.putIfAbsent("GBNCD", "");
        params.putIfAbsent("CODE", "");
        params.putIfAbsent("CODENM", "");
        params.putIfAbsent("ETCVAL", "");
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
        // 📋 6개 파라미터 전체 로그 출력
        log.info("📋 [HS00] SSMS 실행용: {}", buildSsmsLog(procedure, params));
        
        if ("HS00_000S_STR".equalsIgnoreCase(procedure)) {
            return ResponseEntity.ok(hs00Mapper.HS00_000S_STR(params));
        }
        return ResponseEntity.notFound().build();
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        // 💡 XML 규격에 맞춘 6개 키 정의
        String[] keys = {"GUBUN", "CMPYCD", "GBNCD", "CODE", "CODENM", "ETCVAL"};
        String values = java.util.Arrays.stream(keys)
                .map(key -> {
                    Object val = params.get(key);
                    return val == null ? "''" : "'" + val.toString().trim() + "'";
                })
                .collect(Collectors.joining(", "));
        return String.format("EXEC %s %s", procedure.toUpperCase(), values);
    }
}
