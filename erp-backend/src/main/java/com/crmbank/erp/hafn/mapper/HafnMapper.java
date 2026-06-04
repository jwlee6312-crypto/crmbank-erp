package com.crmbank.erp.hafn.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HafnMapper {
    List<Map<String, Object>> hafn_010s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_110s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_120s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_210s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_310s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_410s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_420s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_430s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_510s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_520s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_610u_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_620u_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_630u_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_670s_str(Map<String, Object> params);
    List<Map<String, Object>> hafn_690s_str(Map<String, Object> params);
}
