package com.crmbank.erp.haba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.haba.mapper.HabaMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/haba")
@RequiredArgsConstructor
public class HabaController {

    private final HabaMapper habaMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            // cmpycd와 updemp 정보만 세션에서 보충
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.put("updemp", user.getUserid());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        log.info("==============" + params);

        // 🚀 세션 유실 체크 (유실 시 401 반환 -> axios 인터셉터가 감지하여 자동 로그아웃)
        if (session.getAttribute("user_session") == null) {
            log.warn("🚨 [haba] 세션 만료 - 접근 거부: {}", procedure);
            return ResponseEntity.status(401).build();
        }

        injectSession(params, session);
        log.info("==============" + params);
        String proc = procedure.toUpperCase();
        // 📋 로그 출력 (표준 @key 형식)
        log.info("📋 [haba] SSMS 실행용: {}", buildSsmsLog(proc, params));

        // 🚀 Vue에서 받은 파라미터 그대로 Mapper에 전달
        switch (proc) {
            case "HABA_YYYY_S": return ResponseEntity.ok(habaMapper.HABA_YYYY_S(params));
            case "HABA_010U_STR": return ResponseEntity.ok(habaMapper.HABA_010U_STR(params));
            case "HABA_020U_STR": return ResponseEntity.ok(habaMapper.HABA_020U_STR(params));
            case "HABA_021U_STR": return ResponseEntity.ok(habaMapper.HABA_021U_STR(params));
            case "HABA_022U_STR": return ResponseEntity.ok(habaMapper.HABA_022U_STR(params));
            case "HABA_030U_STR": return ResponseEntity.ok(habaMapper.HABA_030U_STR(params));
            case "HABA_040U_STR": return ResponseEntity.ok(habaMapper.HABA_040U_STR(params));
            case "HABA_050U_STR": return ResponseEntity.ok(habaMapper.HABA_050U_STR(params));
            case "HABA_060U_STR": return ResponseEntity.ok(habaMapper.HABA_060U_STR(params));
            case "HABA_070U_STR": return ResponseEntity.ok(habaMapper.HABA_070U_STR(params));
            case "HABA_080U_STR": return ResponseEntity.ok(habaMapper.HABA_080U_STR(params));
            case "HABA_090U_STR": return ResponseEntity.ok(habaMapper.HABA_090U_STR(params));
            case "HABA_100U_STR": return ResponseEntity.ok(habaMapper.HABA_100U_STR(params));
            case "HABA_110U_STR": return ResponseEntity.ok(habaMapper.HABA_110U_STR(params));
            case "HABA_120U_STR": return ResponseEntity.ok(habaMapper.HABA_120U_STR(params));
            case "HABA_130U_STR": return ResponseEntity.ok(habaMapper.HABA_130U_STR(params));
            case "HABA_140U_STR": return ResponseEntity.ok(habaMapper.HABA_140U_STR(params));
            case "HABA_150U_STR": return ResponseEntity.ok(habaMapper.HABA_150U_STR(params));
            case "HABA_160U_STR": return ResponseEntity.ok(habaMapper.HABA_160U_STR(params));
            case "HABA_170U_STR": return ResponseEntity.ok(habaMapper.HABA_170U_STR(params));
            case "HABA_180U_STR": return ResponseEntity.ok(habaMapper.HABA_180U_STR(params));
            case "HABA_190S_STR": return ResponseEntity.ok(habaMapper.HABA_190S_STR(params));
            case "HABA_210U_STR": return ResponseEntity.ok(habaMapper.HABA_210U_STR(params));
            case "HABA_220U_STR": return ResponseEntity.ok(habaMapper.HABA_220U_STR(params));
            case "HABA_230U_STR": return ResponseEntity.ok(habaMapper.HABA_230U_STR(params));
            case "HABA_240U_STR": return ResponseEntity.ok(habaMapper.HABA_240U_STR(params));
            case "HABA_250U_STR": return ResponseEntity.ok(habaMapper.HABA_250U_STR(params));
            case "HABA_260U_STR": return ResponseEntity.ok(habaMapper.HABA_260U_STR(params));
            case "HABA_510U_STR": return ResponseEntity.ok(habaMapper.HABA_510U_STR(params));
            case "HABA_900U_STR": return ResponseEntity.ok(habaMapper.HABA_900U_STR(params));
            case "HABA_910U_STR": return ResponseEntity.ok(habaMapper.HABA_910U_STR(params));
            case "HABA_920U_STR": return ResponseEntity.ok(habaMapper.HABA_920U_STR(params));
            case "HABA_935U_STR": return ResponseEntity.ok(habaMapper.HABA_935U_STR(params));
            default:
                log.warn("❌ [haba] Unregistered procedure: {}", proc);
                return ResponseEntity.notFound().build();
        }
    }

    private String buildSsmsLog(String proc, Map<String, Object> params) {
        String values = params.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object val = entry.getValue();
                    String valStr = (val == null) ? "''" : (val instanceof Number ? val.toString() : "'" + val.toString().trim() + "'");
                    return "@" + key + " = " + valStr;
                })
                .collect(Collectors.joining(", "));
        return String.format("EXEC %s %s", proc, values);
    }
}
