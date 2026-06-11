package com.crmbank.erp.habg.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.habg.mapper.HabgMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/habg")
@RequiredArgsConstructor
public class HabgController {

    private final HabgMapper habgMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    @PostMapping("/{procedure}")
    public List<Map<String, Object>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        injectSession(params, session);

        String proc = procedure.toUpperCase();
        log.info("📋 SSMS 실행용: {}", buildSsmsLog(proc, params));

        switch (proc) {
            case "HABG_010U_STR": return habgMapper.HABG_010U_STR(params);
            case "HABG_020S_STR": return habgMapper.HABG_020S_STR(params);
            case "HABG_030U_STR": return habgMapper.HABG_030U_STR(params);
            case "HABG_050U_STR": return habgMapper.HABG_050U_STR(params);
            case "HABG_060U_STR": return habgMapper.HABG_060U_STR(params);
            case "HABG_070S_STR": return habgMapper.HABG_070S_STR(params);
            case "HABG_110U_STR": return habgMapper.HABG_110U_STR(params);
            case "HABG_120U_STR": return habgMapper.HABG_120U_STR(params);
            case "HABG_210S_STR": return habgMapper.HABG_210S_STR(params);
            case "HABG_220S_STR": return habgMapper.HABG_220S_STR(params);
            case "HABG_230S_STR": return habgMapper.HABG_230S_STR(params);
            default:
                log.warn("❌ [habg] Unregistered procedure: {}", proc);
                return null;
        }
    }

    private String buildSsmsLog(String proc, Map<String, Object> params) {
        String[] keys = params.keySet().toArray(new String[0]);
        String values = java.util.Arrays.stream(keys)
                .map(key -> {
                    Object val = params.get(key);
                    return val == null ? "''" : "'" + val.toString().trim() + "'";
                })
                .collect(Collectors.joining(", "));

        return String.format("EXEC %s %s", proc, values);
    }
}
