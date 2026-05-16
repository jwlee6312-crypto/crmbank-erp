package com.crmbank.erp.haaa.controller;

import com.crmbank.erp.comm.service.CommService;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.haaa.mapper.HaaaMapper;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/haaa")
@RequiredArgsConstructor
public class HaaaController {

    private final HaaaMapper haaaMapper;
    private final CommService commService;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            if (!params.containsKey("CMPYCD")) params.put("CMPYCD", user.getCMPYCD());
            if (!params.containsKey("USERID")) params.put("USERID", user.getUSERID());
            params.put("UPDEMP", user.getUSERID());
        }
    }

    // --- 로그인 및 세션 관리 ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data, HttpServletRequest request) {
        try {
            UserSession user = commService.login(data.get("CMPYCD"), data.get("USERID"), data.get("PASSWD"), request.getRemoteAddr());
            request.getSession(true).setAttribute("USER_SESSION", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(401).build();
    }

    // --- 메뉴 관리 ---
    @GetMapping("/top-menus")
    public ResponseEntity<List<Map<String, Object>>> getTopMenus() {
        return ResponseEntity.ok(commService.getTopMenus());
    }

    @GetMapping("/left-menus")
    public ResponseEntity<List<Map<String, Object>>> getLeftMenus(@RequestParam("UPMUCD") String upmucd) {
        return ResponseEntity.ok(commService.getLeftMenus(upmucd));
    }

    // --- HAAA 프로시저 실행 ---
    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        
        // 📋 SSMS 실행용 로그 생성
        log.info("📋 SSMS 실행용: {}", buildSsmsLog(procedure, params));
        
        switch (procedure.toUpperCase()) {
            case "HAAA_010U_STR": return ResponseEntity.ok(haaaMapper.HAAA_010U_STR(params));
            case "HAAA_800U_STR": return ResponseEntity.ok(haaaMapper.HAAA_800U_STR(params));
            case "HAAA_810U_STR": return ResponseEntity.ok(haaaMapper.HAAA_810U_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        String[] keys;
        String proc = procedure.toUpperCase();
        if (proc.equals("HAAA_010U_STR")) {
            keys = new String[]{"ACTKIND", "CDTYPE", "CODECD", "CODENM", "DSPORD", "REMARK", "USEYN", "USERID"};
        } else if (proc.equals("HAAA_800U_STR")) {
            keys = new String[]{"ACTKIND", "PGMID", "PGMNM", "UPMUCD", "GRPCD", "GRPNM", "DSPORD", "USEYN", "USERID"};
        } else if (proc.equals("HAAA_810U_STR")) {
            keys = new String[]{"ACTKIND", "UPMUCD", "GRPCD", "GRPNM", "DSPORD", "USEYN", "USERID"};
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
