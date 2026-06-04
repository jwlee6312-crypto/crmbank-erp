package com.crmbank.erp.hasl.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HaslMapper {
    List<Map<String, Object>> hasl_010u_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_011u_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_020u_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_110u_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_111u_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_030s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_120s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_130s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_040s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_050u_master(Map<String, Object> params);
    List<Map<String, Object>> hasl_050u_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_510s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_520s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_530s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_540s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_550s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_560s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_610s_str(Map<String, Object> params);
    List<Map<String, Object>> get_slip_list(Map<String, Object> params);
    List<Map<String, Object>> hasl_630s_str(Map<String, Object> params);
    List<Map<String, Object>> hasl_710s_str(Map<String, Object> params);
}
