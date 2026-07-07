package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.service.CommService;
import com.crmbank.erp.comm.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/comm")
@RequiredArgsConstructor
public class CommController {

    private final CommService commService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data, HttpServletRequest request) {
        try {
            UserSession user = commService.login(data.get("cmpycd"), data.get("userid"), data.get("passwd"), request.getRemoteAddr());
            HttpSession session = request.getSession(true);
            
            session.setAttribute("user_session", user);
            // 내부 세션 속성 관리
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
    public ResponseEntity<List<Map<String, Object>>> getTopMenus() { 
        return ResponseEntity.ok(commService.getTopMenus()); 
    }

    @GetMapping("/left-menus")
    public ResponseEntity<List<Map<String, Object>>> getLeftMenus(@RequestParam("upmucd") String upmucd) { 
        return ResponseEntity.ok(commService.getLeftMenus(upmucd)); 
    }

    @PostMapping("/getProgramList")
    public ResponseEntity<List<Map<String, Object>>> getProgramList(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(commService.getProgramList(data));
    }
}
