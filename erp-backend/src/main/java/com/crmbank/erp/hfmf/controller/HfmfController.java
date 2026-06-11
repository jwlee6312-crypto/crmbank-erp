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

        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.putIfAbsent("userid", user.getUserid());
            params.put("updemp", user.getUserid());
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
            case "FMF2060R_STR": return ResponseEntity.ok(hfmfMapper.FMF2060R_STR(params)); 
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
}
