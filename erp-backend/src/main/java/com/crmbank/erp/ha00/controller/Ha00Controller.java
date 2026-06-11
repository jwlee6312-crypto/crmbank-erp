package com.crmbank.erp.ha00.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.ha00.mapper.Ha00Mapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * 💡 공통 코드 조회 (/api/ha00/codes/WH 등)
     */
    @GetMapping("/codes/{cdType}")
    public ResponseEntity<List<Map<String, Object>>> getCodes(@PathVariable String cdType) {
        log.info("🔎 [HA00] 공통 코드 조회: {}", cdType);
        return ResponseEntity.ok(ha00Mapper.GET_CODE_LIST(cdType));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executePost(
            @PathVariable String procedure, @RequestBody Map<String, Object> params, HttpSession session) {
        
        // 🚀 1. 세션 유실 체크 (유실 시 401 반환 -> 프론트엔드 자동 로그아웃 처리)
        if (session.getAttribute("user_session") == null) {
            log.warn("🚨 [ha00] 세션 만료 - 접근 거부: {}", procedure);
            return ResponseEntity.status(401).build();
        }

        injectSession(params, session);
        
        // 🚀 2. 명명된 매개변수 방식으로 로그 생성 (@key = value)
        log.info("📋 [HA00] SSMS 실행용: {}", buildSsmsLog(procedure, params));
        
        try {
            String proc = procedure.toUpperCase();
            if ("HA00_00P_STR".equals(proc)) {
                return ResponseEntity.ok(ha00Mapper.HA00_00P_STR(params));
            } else if ("HA00_010S_STR".equals(proc)) {
                return ResponseEntity.ok(ha00Mapper.HA00_010S_STR(params));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ [HA00] 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeGet(
            @PathVariable String procedure, @RequestParam Map<String, Object> params, HttpSession session) {
        return executePost(procedure, new HashMap<>(params), session);
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.putIfAbsent("userid", user.getUserid());
        }
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        String values = params.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object val = entry.getValue();
                    String valStr = (val == null) ? "''" : (val instanceof Number ? val.toString() : "'" + val.toString().trim() + "'");
                    return "@" + key + " = " + valStr;
                })
                .collect(Collectors.joining(", "));
        return String.format("EXEC %s %s", procedure.toUpperCase(), values);
    }
}
