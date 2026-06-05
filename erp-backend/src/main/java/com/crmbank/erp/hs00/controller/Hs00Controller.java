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
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.putIfAbsent("userid", user.getUserid());
        }
        // 💡 HS00_000S_STR 명세(@iGUBUN, @iCMPYCD, @iGBNCD, @iCODE, @iCODENM, @iETCVAL) 6개 변수 보장
        params.putIfAbsent("gubun", "");
        params.putIfAbsent("cmpycd", "");
        params.putIfAbsent("gbncd", "");
        params.putIfAbsent("code", "");
        params.putIfAbsent("codenm", "");
        params.putIfAbsent("etcval", "");
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
        // 📋 SSMS 실행용 로그 출력 (6개 변수 규격)
        log.info("📋 [HS00] SSMS 실행용: {}", buildSsmsLog(procedure, params));
        
        try {
            List<Map<String, Object>> result;
            if ("HS00_000S_STR".equalsIgnoreCase(procedure)) {
                result = hs00Mapper.HS00_000S_STR(params);
            } else {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [HS00] 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        String[] keys = {"gubun", "cmpycd", "gbncd", "code", "codenm", "etcval"};
        String values = java.util.Arrays.stream(keys)
                .map(key -> {
                    Object val = params.get(key);
                    return val == null ? "''" : "'" + val.toString().trim() + "'";
                })
                .collect(Collectors.joining(", "));
        return String.format("EXEC %s %s", procedure.toUpperCase(), values);
    }
}
