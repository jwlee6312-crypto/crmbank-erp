package com.crmbank.erp.comm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommMapper {
    Map<String, Object> GET_COMPANY_INFO(Map<String, Object> param);
    Map<String, Object> GET_USER_INFO(Map<String, Object> param);
    Map<String, Object> GET_MENU_CONFIG(Map<String, Object> param);
    List<Map<String, Object>> GET_TOP_MENU_LIST();
    List<Map<String, Object>> GET_LEFT_MENU_LIST(@Param("upmucd") String upmucd);

    void INSERT_LOGIN_HISTORY(Map<String, Object> param);
    void DELETE_USER_CONN(Map<String, Object> param);
}
