package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.mapper.CommMapper;
import com.crmbank.erp.comm.service.CommService;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.ha00.mapper.Ha00Mapper;
import com.crmbank.erp.hp00.mapper.Hp00Mapper;
import com.crmbank.erp.hs00.mapper.Hs00Mapper;

import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/comm")
@RequiredArgsConstructor
public class CommController {

    private final CommService commService;
    private final CommMapper commMapper;
    private final Ha00Mapper ha00Mapper;
    private final Hp00Mapper hp00Mapper;
    private final Hs00Mapper hs00Mapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            params.put("CMPYCD", user.getCMPYCD());
            params.put("USERID", user.getUSERID());
            params.put("UPDEMP", user.getUSERID());
        }
    }

    private Map<String, Object> normalizeParams(Map<String, Object> params) {
        Map<String, Object> upperParams = new HashMap<>();
        params.forEach((k, v) -> upperParams.put(k.toUpperCase(), v));
        return upperParams;
    }

    @GetMapping("/codes/{cdType}")
    public ResponseEntity<List<Map<String, Object>>> getCodes(@PathVariable String cdType) {
        return ResponseEntity.ok(commMapper.GET_CODE_LIST(cdType));
    }

    @GetMapping("/HS00_000S_STR")
    public ResponseEntity<List<Map<String, Object>>> executeHS00_000S_STR(@RequestParam Map<String, Object> params, HttpSession session) {
        Map<String, Object> p = normalizeParams(params);
        injectSession(p, session);
        p.putIfAbsent("GBNCD", "");
        p.putIfAbsent("CODE", "");
        List<Map<String, Object>> result = hs00Mapper.HS00_000S_STR(p);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/HP00_000S_STR")
    public ResponseEntity<List<Map<String, Object>>> executeHP00_000S_STR(@RequestParam Map<String, Object> params, HttpSession session) {
        Map<String, Object> p = normalizeParams(params);
        injectSession(p, session);
        p.putIfAbsent("GBNCD", "");
        p.putIfAbsent("CODE", "");
        List<Map<String, Object>> result = hp00Mapper.HP00_000S_STR(p);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/HA00_00P_STR")
    public ResponseEntity<List<Map<String, Object>>> executeHA00_00P_STR(@RequestParam Map<String, Object> params, HttpSession session) {
        Map<String, Object> p = normalizeParams(params);
        injectSession(p, session);
        p.putIfAbsent("SELGBN", "");
        p.putIfAbsent("SEARCH", "");
        p.putIfAbsent("ETC", "");
        List<Map<String, Object>> result = ha00Mapper.HA00_00P_STR(p);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data, HttpServletRequest request) {
        try {
            UserSession user = commService.login(data.get("CMPYCD"), data.get("USERID"), data.get("PASSWD"), request.getRemoteAddr());
            HttpSession session = request.getSession(true);
            
            session.setAttribute("USER_SESSION", user);
            session.setAttribute("cmpycd", user.getCMPYCD());
            session.setAttribute("userid", user.getUSERID());
            session.setAttribute("usernm", user.getUSERNM());
            session.setAttribute("email", user.getEMAIL());
            session.setAttribute("deptcd", user.getDEPTCD());
            session.setAttribute("deptnm", user.getDEPTNM());
            session.setAttribute("inner_no", user.getINNER_NO());
            session.setAttribute("extension", user.getINNER_NO());

            log.info("🔐 [로그인 성공] ID: {}", user.getUSERID());
            return ResponseEntity.ok(user);
        } catch (Exception e) { 
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) { 
        session.invalidate(); 
        return ResponseEntity.ok("로그아웃"); 
    }

    /**
     * 💡 무한 루프 방지를 위해 세션이 없을 경우 401 대신 빈 응답 반환
     */
    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user == null) {
            // 💡 401을 반환하면 프론트엔드에서 무한 루프가 발생할 수 있으므로 빈 응답 반환
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/top-menus")
    public ResponseEntity<List<Map<String, Object>>> getTopMenus() { return ResponseEntity.ok(commService.getTopMenus()); }

    @GetMapping("/left-menus")
    public ResponseEntity<List<Map<String, Object>>> getLeftMenus(@RequestParam("UPMUCD") String upmucd) { return ResponseEntity.ok(commService.getLeftMenus(upmucd)); }
}
