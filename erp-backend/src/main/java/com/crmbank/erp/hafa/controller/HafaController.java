package com.crmbank.erp.hafa.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hafa.mapper.HafaMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hafa")
@RequiredArgsConstructor
public class HafaController {

    private final HafaMapper hafaMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
        }
    }

    /**
     * 자산이력관리 조회 및 처리
     */
    @PostMapping("/HAFA_010U_STR")
    public List<Map<String, Object>> executeHafa010U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_010U_STR(params);
    }

    /**
     * 자산이력조회
     */
    @PostMapping("/HAFA_020S_STR")
    public List<Map<String, Object>> getHafa020S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_020S_STR(params);
    }

    /**
     * 고정자산대장 조회
     */
    @PostMapping("/HAFA_040S_STR")
    public List<Map<String, Object>> getHafa040S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_040S_STR(params);
    }

    /**
     * 고정자산관리 조회 및 처리
     */
    @PostMapping("/HAFA_050U_STR")
    public List<Map<String, Object>> executeHafa050U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_050U_STR(params);
    }

    /**
     * 감가상각계산 처리
     */
    @PostMapping("/HAFA_090U_STR")
    public List<Map<String, Object>> executeHafa090U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_090U_STR(params);
    }

    /**
     * 감가상각명세서 조회
     */
    @PostMapping("/HAFA_120S_STR")
    public List<Map<String, Object>> getHafa120S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_120S_STR(params);
    }

    /**
     * 감가상각집계표 조회
     */
    @PostMapping("/HAFA_130S_STR")
    public List<Map<String, Object>> getHafa130S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_130S_STR(params);
    }

    /**
     * 월별 감가상각명세서 헤더 조회
     */
    @PostMapping("/HA00_150S_STR")
    public List<Map<String, Object>> getHafa150Header(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HA00_150S_STR(params);
    }

    /**
     * 월별 감가상각명세서 조회
     */
    @PostMapping("/HAFA_140S_STR")
    public List<Map<String, Object>> getHafa140S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_140S_STR(params);
    }

    /**
     * 감가상각전표 대상 조회
     */
    @PostMapping("/HAFA_150U_STR")
    public List<Map<String, Object>> getHafa150U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_150U_STR(params);
    }

    /**
     * 감가상각계정과목 설정
     */
    @PostMapping("/HAFA_900U_STR")
    public List<Map<String, Object>> executeHafa900U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_900U_STR(params);
    }

    /**
     * 자산 선택 도움말
     */
    @PostMapping("/HELP_ASETCD_LTD_STR")
    public List<Map<String, Object>> getHelpAsetcd(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HELP_ASETCD_LTD_STR(params);
    }

    /**
     * 상각율 선택 도움말
     */
    @PostMapping("/HELP_ASETRATE_LTD_STR")
    public List<Map<String, Object>> getHelpAsetrate(@RequestBody Map<String, Object> params) {
        return hafaMapper.HELP_ASETRATE_LTD_STR(params);
    }
}
