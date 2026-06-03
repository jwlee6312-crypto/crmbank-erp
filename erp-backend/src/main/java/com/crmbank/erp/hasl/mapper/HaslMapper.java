package com.crmbank.erp.hasl.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HaslMapper {
    List<Map<String, Object>> HASL_010U_STR(Map<String, Object> params);
    List<Map<String, Object>> HASL_011U_STR(Map<String, Object> params);
    List<Map<String, Object>> HASL_020U_STR(Map<String, Object> params);
    List<Map<String, Object>> HASL_110U_STR(Map<String, Object> params);
    List<Map<String, Object>> HASL_111U_STR(Map<String, Object> params);
    List<Map<String, Object>> HASL_030S_STR(Map<String, Object> params);
    List<Map<String, Object>> HASL_120S_STR(Map<String, Object> params);
    List<Map<String, Object>> HASL_130S_STR(Map<String, Object> params);
}
