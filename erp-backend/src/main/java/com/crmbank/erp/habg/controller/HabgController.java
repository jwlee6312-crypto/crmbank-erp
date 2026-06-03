package com.crmbank.erp.habg.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.habg.mapper.HabgMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/habg")
@RequiredArgsConstructor
public class HabgController {

    private final HabgMapper habgMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
        }
    }

    /**
     * 예산신청 조회 및 처리
     */
    @PostMapping("/HABG_010U_STR")
    public List<Map<String, Object>> getHabg010U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_010U_STR(params);
    }

    /**
     * 예산신청서 조회
     */
    @PostMapping("/HABG_020S_STR")
    public List<Map<String, Object>> getHabg020S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_020S_STR(params);
    }

    /**
     * 예산조정 조회 및 처리
     */
    @PostMapping("/HABG_030U_STR")
    public List<Map<String, Object>> executeHabg030U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_030U_STR(params);
    }

    /**
     * 예산배정 조회 및 처리
     */
    @PostMapping("/HABG_050U_STR")
    public List<Map<String, Object>> executeHabg050U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_050U_STR(params);
    }

    /**
     * 예산일괄배정 처리
     */
    @PostMapping("/HABG_060U_STR")
    public List<Map<String, Object>> executeHabg060U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_060U_STR(params);
    }

    /**
     * 예산배정서 조회
     */
    @PostMapping("/HABG_070S_STR")
    public List<Map<String, Object>> getHabg070S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_070S_STR(params);
    }

    /**
     * 추가/조정신청 조회 및 처리
     */
    @PostMapping("/HABG_110U_STR")
    public List<Map<String, Object>> executeHabg110U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_110U_STR(params);
    }

    /**
     * 추가/조정신청 처리
     */
    @PostMapping("/HABG_120U_STR")
    public List<Map<String, Object>> executeHabg120U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_120U_STR(params);
    }

    /**
     * 예산현황 조회
     */
    @PostMapping("/HABG_210S_STR")
    public List<Map<String, Object>> getHabg210S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_210S_STR(params);
    }

    /**
     * 예산상세현황 조회
     */
    @PostMapping("/HABG_220S_STR")
    public List<Map<String, Object>> getHabg220S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_220S_STR(params);
    }

    /**
     * 예산실적서 조회
     */
    @PostMapping("/HABG_230S_STR")
    public List<Map<String, Object>> getHabg230S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.HABG_230S_STR(params);
    }

    /**
     * 예산구분 조회
     */
    @PostMapping("/GET_BGTYPE")
    public Map<String, Object> getBgType(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return habgMapper.GET_BGTYPE(params);
    }
}
