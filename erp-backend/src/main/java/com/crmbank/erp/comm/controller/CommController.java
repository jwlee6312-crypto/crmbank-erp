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

    /**
     * 💡 [소문자 표준화] 세션 정보를 파라미터 맵에 소문자 키로 주입
     */
    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.put("cmpycd", user.getCmpycd());
            params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    /**
     * 💡 [소문자 표준화] 모든 파라미터 키를 소문자로 정규화
     */
    private Map<String, Object> normalizeParams(Map<String, Object> params) {
        Map<String, Object> lowerParams = new HashMap<>();
        params.forEach((k, v) -> lowerParams.put(k.toLowerCase(), v));
        return lowerParams;
    }

    @GetMapping("/codes/{cdType}")
    public ResponseEntity<List<Map<String, Object>>> getCodes(@PathVariable String cdType) {
        return ResponseEntity.ok(commMapper.GET_CODE_LIST(cdType));
    }

    @GetMapping("/HS00_000S_STR")
    public ResponseEntity<List<Map<String, Object>>> executeHS00_000S_STR(@RequestParam Map<String, Object> params, HttpSession session) {
        Map<String, Object> p = normalizeParams(params);
        injectSession(p, session);
        p.putIfAbsent("gbncd", "");
        p.putIfAbsent("code", "");
        List<Map<String, Object>> result = hs00Mapper.HS00_000S_STR(p);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/HP00_000S_STR")
    public ResponseEntity<List<Map<String, Object>>> executeHP00_000S_STR(@RequestParam Map<String, Object> params, HttpSession session) {
        Map<String, Object> p = normalizeParams(params);
        injectSession(p, session);
        p.putIfAbsent("gbncd", "");
        p.putIfAbsent("code", "");
        List<Map<String, Object>> result = hp00Mapper.HP00_000S_STR(p);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/HA00_00P_STR")
    public ResponseEntity<List<Map<String, Object>>> executeHA00_00P_STR(@RequestParam Map<String, Object> params, HttpSession session) {
        Map<String, Object> p = normalizeParams(params);
        injectSession(p, session);
        p.putIfAbsent("selgbn", "");
        p.putIfAbsent("search", "");
        p.putIfAbsent("etc", "");
        List<Map<String, Object>> result = ha00Mapper.HA00_00P_STR(p);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data, HttpServletRequest request) {
        try {
            // 💡 [소문자 표준화] 프론트엔드에서 보낸 소문자 키로 접근
            UserSession user = commService.login(data.get("cmpycd"), data.get("userid"), data.get("passwd"), request.getRemoteAddr());
            HttpSession session = request.getSession(true);
            
            session.setAttribute("user_session", user);
            // 내부 세션 속성도 소문자로 관리
            session.setAttribute("cmpycd", user.getCmpycd());
            session.setAttribute("userid", user.getUserid());
            session.setAttribute("usernm", user.getUsernm());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("deptcd", user.getDeptcd());
            session.setAttribute("deptnm", user.getDeptnm());
            session.setAttribute("inner_no", user.getInner_no());

            log.info("🔐 [로그인 성공] ID: {}", user.getUserid());
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

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/top-menus")
    public ResponseEntity<List<Map<String, Object>>> getTopMenus() { return ResponseEntity.ok(commService.getTopMenus()); }

    @GetMapping("/left-menus")
    public ResponseEntity<List<Map<String, Object>>> getLeftMenus(@RequestParam("upmucd") String upmucd) { return ResponseEntity.ok(commService.getLeftMenus(upmucd)); }
}
