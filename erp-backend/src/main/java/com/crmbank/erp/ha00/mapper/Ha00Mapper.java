package com.crmbank.erp.ha00.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface Ha00Mapper {
    /**
     * HA00_00P_STR: 공통 팝업/도움창 데이터 조회
     */
    List<Map<String, Object>> HA00_00P_STR(Map<String, Object> params);
    /**
     * HA00_010S_STR: 공통 코드 조회
     */
    List<Map<String, Object>> HA00_010S_STR(Map<String, Object> params);
}
