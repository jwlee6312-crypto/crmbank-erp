package com.crmbank.erp.hatx.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hatx.mapper.HatxMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hatx")
@RequiredArgsConstructor
public class HatxController {

    private final HatxMapper hatxMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
        }
    }

    /**
     * 매입부가세관리 조회 및 처리
     */
    @PostMapping("/HATX_010U_STR")
    public List<Map<String, Object>> executeHatx010U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_010U_STR(params);
    }

    /**
     * 매입부가세 품목 상세 처리
     */
    @PostMapping("/HATX_011U_STR")
    public List<Map<String, Object>> executeHatx011U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_011U_STR(params);
    }

    /**
     * 매입장 조회
     */
    @PostMapping("/HATX_030S_STR")
    public List<Map<String, Object>> executeHatx030S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_030S_STR(params);
    }

    /**
     * 매출장 조회
     */
    @PostMapping("/HATX_040S_STR")
    public List<Map<String, Object>> executeHatx040S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_040S_STR(params);
    }

    /**
     * 세금계산서합계표 조회
     */
    @PostMapping("/HATX_110S_STR")
    public List<Map<String, Object>> executeHatx110S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_110S_STR(params);
    }

    /**
     * 신용카드매출전표수취명세서 조회
     */
    @PostMapping("/HATX_130S_STR")
    public List<Map<String, Object>> executeHatx130S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_130S_STR(params);
    }

    /**
     * 신용카드매출전표등발행금액집계표 조회
     */
    @PostMapping("/HATX_140S_STR")
    public List<Map<String, Object>> executeHatx140S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_140S_STR(params);
    }

    /**
     * 수출실적명세서 조회
     */
    @PostMapping("/HATX_600S_STR")
    public List<Map<String, Object>> executeHatx600S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_600S_STR(params);
    }

    /**
     * 전표 마스터 처리
     */
    @PostMapping("/HATX_01AU_STR")
    public List<Map<String, Object>> executeHatx01AU(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_01AU_STR(params);
    }

    /**
     * 전표 상세 처리
     */
    @PostMapping("/HATX_01BU_STR")
    public List<Map<String, Object>> executeHatx01BU(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_01BU_STR(params);
    }

    /**
     * 매출부가세접수 처리
     */
    @PostMapping("/HATX_060U_STR")
    public List<Map<String, Object>> executeHatx060U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_060U_STR(params);
    }

    /**
     * 전자세금계산서발송 처리
     */
    @PostMapping("/HATX_080U_STR")
    public List<Map<String, Object>> executeHatx080U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hatxMapper.HATX_080U_STR(params);
    }
}
