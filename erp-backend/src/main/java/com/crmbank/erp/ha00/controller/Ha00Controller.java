package com.crmbank.erp.ha00.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
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

@Slf4j
@RestController
@RequestMapping("/api/ha00")
@RequiredArgsConstructor
public class Ha00Controller {

    private final Ha00Mapper ha00Mapper;

    @PostMapping("/{procedure}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> executePost(
            @PathVariable String procedure, @RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("📋 [HA00] EXEC: {}", procedure);
        
        try {
            List<Map<String, Object>> result;
            if ("HA00_00P_STR".equalsIgnoreCase(procedure)) {
                result = ha00Mapper.HA00_00P_STR(params);
            } else if ("HA00_010S_STR".equalsIgnoreCase(procedure)) {
                result = ha00Mapper.HA00_010S_STR(params);
            } else {
                return ResponseEntity.status(404).body(ApiResponse.notFound("기초 정보 서비스를 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(ApiResponse.success(result, "조회 성공"));
        } catch (Exception e) {
            log.error("❌ [HA00] 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @GetMapping("/{procedure}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> executeGet(
            @PathVariable String procedure, @RequestParam Map<String, Object> params, HttpSession session) {
        return executePost(procedure, new HashMap<>(params), session);
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        // 💡 [소문자 표준화] session key: "user_session"
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.putIfAbsent("userid", user.getUserid());
        }
        params.putIfAbsent("gubun", "");
        params.putIfAbsent("gbncd", "");
        params.putIfAbsent("code", "");
        params.putIfAbsent("search", "");
        params.putIfAbsent("remark", "");
    }
}
