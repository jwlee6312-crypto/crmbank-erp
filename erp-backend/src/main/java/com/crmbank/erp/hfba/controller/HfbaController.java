package com.crmbank.erp.hfba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hfba.mapper.HfbaMapper;
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
@RequestMapping("/api/hfba")
@RequiredArgsConstructor
public class HfbaController {

    private final HfbaMapper hfbaMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        // 💡 모든 파라미터 키를 대문자로 변환하여 MyBatis 매핑 오류 방지
        Map<String, Object> upperMap = new HashMap<>();
        params.forEach((k, v) -> upperMap.put(k.toUpperCase(), v));
        params.clear();
        params.putAll(upperMap);

        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            params.putIfAbsent("CMPYCD", user.getCMPYCD());
            params.putIfAbsent("USERID", user.getUSERID());
            params.put("UPDEMP", user.getUSERID());
        }

        // 💡 모든 파라미터를 문자열로 변환하되, 빈 값("")은 null로 처리하여 DB 숫자 변환 오류를 원천 차단
        params.replaceAll((key, value) -> {
            if (value == null) return null;
            String str = value.toString().trim();
            return str.isEmpty() ? null : str;
        });
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        injectSession(params, session);
        log.info("🚀 [HFBA] {} 호출: {}", procedure.toUpperCase(), params);
        
        switch (procedure.toUpperCase()) {
            case "FBA1010U_STR": return ResponseEntity.ok(hfbaMapper.FBA1010U_STR(params));
            case "FBA1040U_STR": return ResponseEntity.ok(hfbaMapper.FBA1040U_STR(params));
            case "FBA1060U_STR": return ResponseEntity.ok(hfbaMapper.FBA1060U_STR(params));
            case "FBA2010U_STR": return ResponseEntity.ok(hfbaMapper.FBA2010U_STR(params));
            case "FBA3010U_STR": return ResponseEntity.ok(hfbaMapper.FBA3010U_STR(params));
            case "SELECT_ACCT_LIST": return ResponseEntity.ok(hfbaMapper.selectAcctList(params));
            case "SELECT_DIVIDE_LIST": return ResponseEntity.ok(hfbaMapper.selectDivideList(params));
            case "SELECT_DIVIDE_JUKSU_LIST": return ResponseEntity.ok(hfbaMapper.selectDivideJuksuList(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }
}
