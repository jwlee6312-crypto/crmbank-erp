package com.crmbank.erp.hsst.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsst.mapper.HsstMapper;
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
@RequestMapping("/api/hsst")
@RequiredArgsConstructor
public class HsstController {

    private final HsstMapper hsstMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        
        // 📋 SSMS 실행용 로그 생성
        log.info("📋 SSMS 실행용: {}", buildSsmsLog(procedure, params));
        
        switch (procedure.toUpperCase()) {
            case "HSST_100S_STR": return ResponseEntity.ok(hsstMapper.HSST_100S_STR(params));
            case "HSST_120S_STR": return ResponseEntity.ok(hsstMapper.HSST_120S_STR(params));
            case "HSST_130S_STR": return ResponseEntity.ok(hsstMapper.HSST_130S_STR(params));
            case "HSST_150S_STR": return ResponseEntity.ok(hsstMapper.HSST_150S_STR(params));
            case "HSST_180S_STR": return ResponseEntity.ok(hsstMapper.HSST_180S_STR(params));
            case "HSST_200S_STR": return ResponseEntity.ok(hsstMapper.HSST_200S_STR(params));
            case "HSST_210S_STR": return ResponseEntity.ok(hsstMapper.HSST_210S_STR(params));
            case "HSST_300S_STR": return ResponseEntity.ok(hsstMapper.HSST_300S_STR(params));
            case "HSST_320S_STR": return ResponseEntity.ok(hsstMapper.HSST_320S_STR(params));
            case "HSST_340S_STR": return ResponseEntity.ok(hsstMapper.HSST_340S_STR(params));
            case "HSST_360S_STR": return ResponseEntity.ok(hsstMapper.HSST_360S_STR(params));
            case "HSST_510S_STR": return ResponseEntity.ok(hsstMapper.HSST_510S_STR(params));
            case "HSST_520S_STR": return ResponseEntity.ok(hsstMapper.HSST_520S_STR(params));
            case "HSST_570S_STR": return ResponseEntity.ok(hsstMapper.HSST_570S_STR(params));
            case "HSST_600S_STR": return ResponseEntity.ok(hsstMapper.HSST_600S_STR(params));
            case "HSST_610S_STR": return ResponseEntity.ok(hsstMapper.HSST_610S_STR(params));

            default:
                return ResponseEntity.notFound().build();
        }
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        String[] keys;
        String proc = procedure.toUpperCase();
        
        switch (proc) {
            case "HSST_100S_STR":
                keys = new String[]{"cmpycd", "selgbn", "deptcd", "custfr", "custto", "ymdfr", "ymdto"};
                break;
            case "HSST_120S_STR":
                keys = new String[]{"cmpycd", "deptcd", "ymd"};
                break;
            case "HSST_200S_STR":
                keys = new String[]{"cmpycd", "ymd", "whcd", "astkind", "itemnm", "pageno", "maxcnt"};
                break;
            default:
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
