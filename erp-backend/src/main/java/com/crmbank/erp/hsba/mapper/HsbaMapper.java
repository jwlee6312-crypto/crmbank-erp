package com.crmbank.erp.hsba.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HsbaMapper {
    List<Map<String, Object>> HSBA_701U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_711U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_721U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_800U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_810U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_820U_STR(Map<String, Object> params);

    List<Map<String, Object>> HSBA_065U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_090U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_140U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_730U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_750U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSBA_740U_STR(Map<String, Object> params);

}
