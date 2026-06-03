package com.crmbank.erp.hapl.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hapl.mapper.HaplMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hapl")
@RequiredArgsConstructor
public class HaplController {

    private final HaplMapper haplMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
        }
    }

    /**
     * 배부기준관리 조회 및 처리
     */
    @PostMapping("/HAPL_010U_STR")
    public List<Map<String, Object>> executeHapl010U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haplMapper.HAPL_010U_STR(params);
    }

    /**
     * 계정별 배부기준관리 조회 및 처리
     */
    @PostMapping("/HAPL_020U_STR")
    public List<Map<String, Object>> executeHapl020U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haplMapper.HAPL_020U_STR(params);
    }

    /**
     * 배부적수관리 조회 및 처리
     */
    @PostMapping("/HAPL_030U_STR")
    public List<Map<String, Object>> executeHapl030U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haplMapper.HAPL_030U_STR(params);
    }

    /**
     * 배부적수관리 일괄 저장
     */
    @Transactional
    @PostMapping("/HAPL_030U_SAVE_BATCH")
    public void saveHapl030UBatch(@RequestBody Map<String, Object> params, HttpSession session) {
        String cmpycd = (String) params.get("cmpycd");
        String stdym = (String) params.get("stdym");
        String divcd = (String) params.get("divcd");
        String deptcd = (String) params.get("deptcd");
        String userid = (String) params.get("userid");
        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("ITEMS");

        if (items != null) {
            for (Map<String, Object> item : items) {
                item.put("actkind", "A0");
                item.put("cmpycd", cmpycd);
                item.put("stdym", stdym);
                item.put("divcd", divcd);
                item.put("deptcd", deptcd);
                item.put("userid", userid);
                haplMapper.HAPL_030U_STR(item);
            }
        }
    }

    /**
     * 부서집계수식 조회 및 처리
     */
    @PostMapping("/HAPL_040U_STR")
    public List<Map<String, Object>> executeHapl040U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haplMapper.HAPL_040U_STR(params);
    }

    /**
     * 부서집계수식 일괄 저장
     */
    @Transactional
    @PostMapping("/HAPL_040U_SAVE_BATCH")
    public void saveHapl040UBatch(@RequestBody Map<String, Object> params, HttpSession session) {
        String cmpycd = (String) params.get("cmpycd");
        String stdym = (String) params.get("stdym");
        String deptcd = (String) params.get("deptcd");
        String userid = (String) params.get("userid");
        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("ITEMS");

        if (items != null) {
            for (Map<String, Object> item : items) {
                item.put("actkind", "A0");
                item.put("cmpycd", cmpycd);
                item.put("stdym", stdym);
                item.put("deptcd", deptcd);
                item.put("userid", userid);
                haplMapper.HAPL_040U_STR(item);
            }
        }
    }

    /**
     * 배부작업 처리
     */
    @PostMapping("/HAPL_100U_STR")
    public List<Map<String, Object>> executeHapl100U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haplMapper.HAPL_100U_STR(params);
    }

    /**
     * 손익계산서 조회
     */
    @PostMapping("/HAPL_110S_STR")
    public List<Map<String, Object>> executeHapl110S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haplMapper.HAPL_110S_STR(params);
    }

    /**
     * 손익계산서 초기화 정보 조회
     */
    @PostMapping("/HAPL_110S_INIT")
    public String getHapl110SInit(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haplMapper.HAPL_110S_INIT(params);
    }

    /**
     * 배부현황 조회
     */
    @PostMapping("/HAPL_120S_STR")
    public List<Map<String, Object>> executeHapl120S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return haplMapper.HAPL_120S_STR(params);
    }
}
