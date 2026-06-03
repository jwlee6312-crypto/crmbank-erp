package com.crmbank.erp.hacl.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HaclMapper {
    /**
     * 월 마감작업 처리 및 오류 전표 조회 (HACL_800U_STR)
     */
    List<Map<String, Object>> HACL_800U_STR(Map<String, Object> params);

    /**
     * 차기이월 작업 처리 (HACL_900U_STR)
     */
    List<Map<String, Object>> HACL_900U_STR(Map<String, Object> params);
}
