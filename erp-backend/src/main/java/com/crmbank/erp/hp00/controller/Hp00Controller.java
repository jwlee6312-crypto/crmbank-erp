package com.crmbank.erp.hp00.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
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
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
        }
    }

    @GetMapping("/{procedure}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> executeProcedureGet(
            @PathVariable String procedure,
            @RequestParam Map<String, Object> params,
            HttpSession session) {
        return execute(procedure, new HashMap<>(params), session);
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> executeProcedurePost(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        return execute(procedure, params, session);
    }

    private ResponseEntity<ApiResponse<List<Map<String, Object>>>> execute(String procedure, Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("📋 [HP00] EXEC: {}", procedure);

        try {
            List<Map<String, Object>> result;
            if ("HP00_000S_STR".equalsIgnoreCase(procedure)) {
                result = hp00Mapper.HP00_000S_STR(params);
            } else {
                return ResponseEntity.status(404).body(ApiResponse.notFound("서비스를 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(ApiResponse.success(result, "조회 성공"));
        } catch (Exception e) {
            log.error("❌ [HP00] 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }
}
