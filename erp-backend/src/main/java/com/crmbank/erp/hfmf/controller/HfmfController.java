package com.crmbank.erp.hfmf.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hfmf.mapper.HfmfMapper;
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
@RequestMapping("/api/hfmf")
@RequiredArgsConstructor
public class HfmfController {

    private final HfmfMapper hfmfMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
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
        log.info("🚀 [HFMF] {} 호출: {}", procedure.toUpperCase(), params);
        
        switch (procedure.toUpperCase()) {
            case "FMF1020U_STR": return ResponseEntity.ok(hfmfMapper.FMF1020U_STR(params));
            case "FMF1040U_STR": return ResponseEntity.ok(hfmfMapper.FMF1040U_STR(params));
            case "FMF1050U_STR": return ResponseEntity.ok(hfmfMapper.FMF1050U_STR(params));
            case "FMF2010U_STR": return ResponseEntity.ok(hfmfMapper.FMF2010U_STR(params));
            case "FMF2020U_STR": return ResponseEntity.ok(hfmfMapper.FMF2020U_STR(params));
            case "FMF2060R_STR": return ResponseEntity.ok(hfmfMapper.FMF2060R_STR(params)); // 💡 추가됨
            case "FMF2070U_STR": return ResponseEntity.ok(hfmfMapper.FMF2070U_STR(params));
            case "FMF2110U_STR": return ResponseEntity.ok(hfmfMapper.FMF2110U_STR(params));
            case "FMF2120R_STR": return ResponseEntity.ok(hfmfMapper.FMF2120R_STR(params));
            case "FMF2140R_STR": return ResponseEntity.ok(hfmfMapper.FMF2140R_STR(params));
            case "FMF2150R_STR": return ResponseEntity.ok(hfmfMapper.FMF2150R_STR(params));
            case "FMF2160R_STR": return ResponseEntity.ok(hfmfMapper.FMF2160R_STR(params));
            case "FMF2180R_STR": return ResponseEntity.ok(hfmfMapper.FMF2180R_STR(params));
            case "FMF2190R_STR": return ResponseEntity.ok(hfmfMapper.FMF2190R_STR(params));
            case "FMF2210R_STR": return ResponseEntity.ok(hfmfMapper.FMF2210R_STR(params));
            case "FMF3010U_STR": return ResponseEntity.ok(hfmfMapper.FMF3010U_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        String proc = procedure.toUpperCase();
        String[] keys;
        
        if (proc.equals("FMF1040U_STR")) {
            keys = new String[]{"CMPYCD", "ACTKIND", "YM", "COSTCD", "ACCT", "COSTAMT", "USERID"};
        } else if (proc.equals("FMF1050U_STR")) {
            keys = new String[]{"CMPYCD", "ACTKIND", "YM", "COSTCD", "LINECD", "DIVSTD", "RATE", "REMARK", "USERID"};
        } else if (proc.equals("FMF2010U_STR")) {
            keys = new String[]{"CMPYCD", "ACTKIND", "YM", "COLGBN", "USERID"};
        } else if (proc.equals("FMF2020U_STR")) {
            keys = new String[]{"CMPYCD", "ACTKIND", "YM", "CHASU", "USERID"};
        } else if (proc.equals("FMF2060R_STR")) {
            keys = new String[]{"CMPYCD", "ACTKIND", "YM", "COSTCD"};
        } else if (proc.equals("FMF2070U_STR")) {
            keys = new String[]{"CMPYCD", "ACTKIND", "YM", "COSTCD", "ACCT", "LINECD", "ADSTAMT", "USERID"};
        } else if (proc.equals("FMF2110U_STR")) {
            keys = new String[]{"CMPYCD", "ACTKIND", "YM", "JOBORD", "USERID", "MSGYN", "MSGTEXT"};
        } else if (proc.equals("FMF3010U_STR")) {
            keys = new String[]{"CMPYCD", "ACTKIND", "YM", "JAGBN", "USERID"};
        } else {
            keys = params.keySet().toArray(new String[0]);
        }

        String values = java.util.Arrays.stream(keys)
                .map(key -> {
                    Object val = params.get(key);
                    if (val == null) val = params.get(key.toUpperCase());
                    return val == null ? "NULL" : "'" + val.toString().trim() + "'";
                })
                .collect(Collectors.joining(", "));

        return String.format("EXEC %s %s", proc, values);
    }
}
