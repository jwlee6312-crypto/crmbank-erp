package com.crmbank.erp.haba.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HabaMapper {
    List<Map<String, Object>> HABA_030U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_060U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_100U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_900U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_910U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_920U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_935U_STR(Map<String, Object> params);
}
