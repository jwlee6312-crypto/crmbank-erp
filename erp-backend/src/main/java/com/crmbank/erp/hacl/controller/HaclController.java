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

    @PostMapping("/HACL_010S_STR")
    public List<Map<String, Object>> searchHacl010S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_010S_STR(params);
    }

    @PostMapping("/HACL_020S_STR")
    public List<Map<String, Object>> searchHacl020S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_020S_STR(params);
    }

    @PostMapping("/HACL_030S_STR")
    public List<Map<String, Object>> searchHacl030S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_030S_STR(params);
    }

    @PostMapping("/HACL_040S_STR")
    public List<Map<String, Object>> searchHacl040S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_040S_STR(params);
    }

    @PostMapping("/HACL_050S_STR")
    public List<Map<String, Object>> searchHacl050S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_050S_STR(params);
    }

    @PostMapping("/HACL_060S_STR")
    public List<Map<String, Object>> searchHacl060S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_060S_STR(params);
    }

    @PostMapping("/HACL_070S_STR")
    public List<Map<String, Object>> searchHacl070S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_070S_STR(params);
    }

    @PostMapping("/HACL_080S_STR")
    public List<Map<String, Object>> searchHacl080S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_080S_STR(params);
    }

    @PostMapping("/HACL_800U_STR")
    public List<Map<String, Object>> executeHacl800U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_800U_STR(params);
    }

    @PostMapping("/HACL_900U_STR")
    public List<Map<String, Object>> executeHacl900U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haclMapper.HACL_900U_STR(params);
    }
}
