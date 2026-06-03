package com.crmbank.erp.hapl.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HaplMapper {
    /**
     * 배부기준관리 조회 및 처리 (HAPL_010U_STR)
     */
    List<Map<String, Object>> HAPL_010U_STR(Map<String, Object> params);

    /**
     * 계정별 배부기준관리 조회 및 처리 (HAPL_020U_STR)
     */
    List<Map<String, Object>> HAPL_020U_STR(Map<String, Object> params);

    /**
     * 배부적수관리 조회 및 처리 (HAPL_030U_STR)
     */
    List<Map<String, Object>> HAPL_030U_STR(Map<String, Object> params);

    /**
     * 부서집계수식 조회 및 처리 (HAPL_040U_STR)
     */
    List<Map<String, Object>> HAPL_040U_STR(Map<String, Object> params);

    /**
     * 품목배부율관리 조회 및 처리 (HAPL_050U_STR)
     */
    List<Map<String, Object>> HAPL_050U_STR(Map<String, Object> params);

    /**
     * 배부작업 처리 (HAPL_100U_STR)
     */
    List<Map<String, Object>> HAPL_100U_STR(Map<String, Object> params);

    /**
     * 손익계산서 조회 (HAPL_110S_STR)
     */
    List<Map<String, Object>> HAPL_110S_STR(Map<String, Object> params);

    /**
     * 손익계산서 초기화 정보 조회 (최근 작업년월)
     */
    String HAPL_110S_INIT(Map<String, Object> params);

    /**
     * 배부현황 조회 (HAPL_120S_STR)
     */
    List<Map<String, Object>> HAPL_120S_STR(Map<String, Object> params);

    /**
     * 부서별 손익계산서 조회 (HAPL_140S_STR)
     */
    List<Map<String, Object>> HAPL_140S_STR(Map<String, Object> params);

    /**
     * 거래처별 손익현황 조회 (HAPL_210S_STR)
     */
    List<Map<String, Object>> HAPL_210S_STR(Map<String, Object> params);

    /**
     * 품목별 손익현황 조회 (HAPL_220S_STR)
     */
    List<Map<String, Object>> HAPL_220S_STR(Map<String, Object> params);
}
