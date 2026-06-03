package com.crmbank.erp.hsba.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsba.mapper.HsbaMapper;
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
@RequestMapping("/api/hsba")
@RequiredArgsConstructor
public class HsbaController {

    private final HsbaMapper hsbaMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.put("cmpycd", user.getCmpycd());
            params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        // 💡 모든 파라미터 키를 소문자로 정규화
        Map<String, Object> p = new HashMap<>();
        params.forEach((k, v) -> p.put(k.toLowerCase(), v));

        injectSession(p, session);

        // 프로시저별 파라미터 보정
        if ("HSBA_750U_STR".equalsIgnoreCase(procedure)) {
            if (p.get("sch_text") != null) p.put("costnm", p.get("sch_text"));
            if (p.get("sch_useyn") != null) p.put("useyn", p.get("sch_useyn"));
            p.putIfAbsent("actkind", "S0");
            p.putIfAbsent("costcd", "");
            p.putIfAbsent("costgbn", "");
            p.putIfAbsent("divcd", "");
            p.putIfAbsent("bigo", "");
        }

        if ("HSBA_090U_STR".equalsIgnoreCase(procedure) || "HSBA_070U_STR".equalsIgnoreCase(procedure)) {
            p.putIfAbsent("actkind", "S0");
            if (p.get("custnm") == null && p.get("sch_text") != null) p.put("custnm", p.get("sch_text"));
        }

        log.info("🚀 [HSBA] {} 호출 (정규화): {}", procedure, p);

        List<Map<String, Object>> result;
        switch (procedure.toUpperCase()) {
            case "HSBA_010U_STR": result = hsbaMapper.HSBA_010U_STR(p); break;
            case "HSBA_020U_STR": result = hsbaMapper.HSBA_020U_STR(p); break;
            case "HSBA_030U_STR": result = hsbaMapper.HSBA_030U_STR(p); break;
            case "HSBA_040U_STR": result = hsbaMapper.HSBA_040U_STR(p); break;
            case "HSBA_060U_STR": result = hsbaMapper.HSBA_060U_STR(p); break;
            case "HSBA_070U_STR": result = hsbaMapper.HSBA_070U_STR(p); break;
            case "HSBA_280U_STR": result = hsbaMapper.HSBA_280U_STR(p); break;
            case "HSBA_065U_STR": result = hsbaMapper.HSBA_065U_STR(p); break;
            case "HSBA_090U_STR": result = hsbaMapper.HSBA_090U_STR(p); break;
            case "HSBA_130U_STR": result = hsbaMapper.HSBA_130U_STR(p); break;
            case "HSBA_140U_STR": result = hsbaMapper.HSBA_140U_STR(p); break;
            case "HSBA_170U_STR": result = hsbaMapper.HSBA_170U_STR(p); break;
            case "HSBA_190U_STR": result = hsbaMapper.HSBA_190U_STR(p); break;
            case "HSBA_700U_STR": result = hsbaMapper.HSBA_700U_STR(p); break;
            case "HSBA_710U_STR": result = hsbaMapper.HSBA_710U_STR(p); break;
            case "HSBA_701U_STR": result = hsbaMapper.HSBA_701U_STR(p); break;
            case "HSBA_711U_STR": result = hsbaMapper.HSBA_711U_STR(p); break;
            case "HSBA_720U_STR": result = hsbaMapper.HSBA_720U_STR(p); break;
            case "HSBA_721U_STR": result = hsbaMapper.HSBA_721U_STR(p); break;
            case "HSBA_730U_STR": result = hsbaMapper.HSBA_730U_STR(p); break;
            case "HSBA_740U_STR": result = hsbaMapper.HSBA_740U_STR(p); break;
            case "HSBA_750U_STR": result = hsbaMapper.HSBA_750U_STR(p); break;
            case "HSBA_800U_STR": result = hsbaMapper.HSBA_800U_STR(p); break;
            case "HSBA_810U_STR": result = hsbaMapper.HSBA_810U_STR(p); break;
            case "HSBA_820U_STR": result = hsbaMapper.HSBA_820U_STR(p); break;
            case "HSBA_830U_STR": result = hsbaMapper.HSBA_830U_STR(p); break;
            case "HSBA_900U_STR": result = hsbaMapper.HSBA_900U_STR(p); break;
            default: return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(result, "조회 성공"));
    }
}
