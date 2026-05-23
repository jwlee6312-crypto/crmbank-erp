package com.crmbank.erp.crm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 상세 (CAMPAIGN_DTL) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampDtlDto {

    private String CMPYCD;   // 회사코드
    private String CAMP_NO;  // 캠페인번호
    private String TYPE;     // 타입
    private Long CNT;        // 건수
}
