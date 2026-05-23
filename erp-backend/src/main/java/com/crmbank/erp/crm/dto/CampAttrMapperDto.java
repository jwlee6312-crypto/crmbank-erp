package com.crmbank.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 캠페인 속성 매핑 정의 (CAMP_ATTR_MAPPER) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampAttrMapperDto {
    private String CMPYCD;      // 회사코드
    private String SURV_GB;     // 설문유형
    private String SURV_GB_NM;  // 설문유형명 (조회용)
    private String ATTR_NM;     // 엑셀 헤더명 (한글)
    private String ATTR_KEY;    // 표준 영문 키
    private String DATA_TYPE;   // 데이터타입
    private String STATS_YN;    // 통계 활용 여부
    private String USEYN;       // 사용여부
    private LocalDateTime ADDTIME;
    private LocalDateTime UPDTIME;
    private String UPDEMP;      // 수정자
}
