package com.crmbank.erp.haba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.haba.mapper.HabaMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.put("updemp", user.getUserid());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        injectSession(params, session);
        
        // 📋 SSMS 실행용 로그 생성 (사용자 수정 규격 반영)
        log.info("📋 SSMS 실행용: {}", buildSsmsLog(procedure, params));

        switch (procedure.toUpperCase()) {
            case "HABA_030U_STR": return ResponseEntity.ok(habaMapper.HABA_030U_STR(params));
            case "HABA_060U_STR": return ResponseEntity.ok(habaMapper.HABA_060U_STR(params));
            case "HABA_100U_STR": return ResponseEntity.ok(habaMapper.HABA_100U_STR(params));
            case "HABA_110U_STR": return ResponseEntity.ok(habaMapper.HABA_110U_STR(params));
            case "HABA_900U_STR": return ResponseEntity.ok(habaMapper.HABA_900U_STR(params));
            case "HABA_910U_STR": return ResponseEntity.ok(habaMapper.HABA_910U_STR(params));
            case "HABA_920U_STR": return ResponseEntity.ok(habaMapper.HABA_920U_STR(params));
            case "HABA_935U_STR": return ResponseEntity.ok(habaMapper.HABA_935U_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        String proc = procedure.toUpperCase();
        String[] keys;

        if (proc.equals("HABA_920U_STR")) {
            keys = new String[]{
                "ACTKIND", "WORD", "CMPYCD", "USERID", "USERNM", "DEPTCD", "DEPTNM",
                "EMPNO", "SALSYN", "PW", "TELNO", "HPNO", "EMAIL", 
                "SDEPT", "USEYN", "USERGRP", "UPDEMP", "PRICEGBN", "POSITIONOFF"
            };
        } else if (proc.equals("HABA_100U_STR")) {
            // 💡 사용자가 HabaMapper.xml에 수정한 21개 파라미터 순서와 100% 일치시킴
            keys = new String[]{
                    "ACTKIND", "CMPYCD", "CLSYMD", "GLINE1", "GLINE2", "GLINE3", "GLINE4", "GLINE5",
                    "BGTYPE", "STKGBN", "MNFYN", "STOKYN", "SLIPYN", "PRICEGBN", "CARDCUST", "LOGOIMG", "STAMPIMG", "UPDEMP",
                    "YEOSINYN", "IOCNFMYN", "OUTACCTYN"
            };
        } else if (proc.equals("HABA_910U_STR")) {
            keys = new String[]{"ACTKIND", "CMPYCD", "USERID", "USERNM", "DEPTCD", "DEPTNM", "PW", "TELNO", "HPNO",
                    "EMAIL", "USEYN", "USERID", "PRICEGBN"};
        } else {
            keys = params.keySet().toArray(new String[0]);
        }
        String values = java.util.Arrays.stream(keys)
                .map(key -> {
                    Object val = params.get(key);
                    return val == null ? "''" : "'" + val.toString().trim() + "'";
                })
                .collect(Collectors.joining(", "));

        return String.format("EXEC %s %s", proc, values);
    }
}
