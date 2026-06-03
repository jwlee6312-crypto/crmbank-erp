package com.crmbank.erp.hacl.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hacl.mapper.HaclMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hacl")
@RequiredArgsConstructor
public class HaclController {

    private final HaclMapper haclMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("CMPYCD")) params.put("CMPYCD", user.getCmpycd());
            if (!params.containsKey("USERID")) params.put("USERID", user.getUserid());
        }
    }

    /**
     * 월 마감작업 처리 및 오류 전표 조회
     */
    @PostMapping("/HACL_800U_STR")
    public List<Map<String, Object>> executeHacl800U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_800U_STR(params);
    }

    /**
     * 차기이월 작업 처리
     */
    @PostMapping("/HACL_900U_STR")
    public List<Map<String, Object>> executeHacl900U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_900U_STR(params);
    }
}
