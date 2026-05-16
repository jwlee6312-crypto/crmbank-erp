package com.crmbank.erp.hsio.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HsioMapper {
    // 구매/입고 관련 (기존 Puch)
    List<Map<String, Object>> HSIO_010U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_011U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_020U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_021U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_060U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_061U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_070U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_080S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_082S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_085S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_110U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_120U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_130U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_131U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_140U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_141U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_160U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_170U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_171U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_180U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_181U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_190U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_191U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_200S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_210S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_215S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_220S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_250U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_251U_STR(Map<String, Object> params);


    // 출고/매출/수금 관련 (기존 Sale)
    List<Map<String, Object>> HSIO_300U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_325U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_470S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_490U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_491U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_500U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_501U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_510U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_511U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_520U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_521U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_530U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_531U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_540U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_541U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_550U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_551U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_560U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_570U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_571U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_580U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_581U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_600S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_610S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_620S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_640S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_650S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_660S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_680S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_690S_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_720U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_721U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_730U_STR(Map<String, Object> params);
    List<Map<String, Object>> HSIO_731U_STR(Map<String, Object> params);


}
