package com.crmbank.erp.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 전화상담결과 상세 (CAMPAIGN_RSLT_DTL) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampRsltDtlDto {

    private String CMPYCD;           // 회사코드
    private Integer RSLT_NO;         // 상담결과번호
    private String SURV_NO;          // 질문번호
    private String ANS_NO;           // 답변번호
    private BigDecimal POINT;        // 점수
    private String REMARK;           // 비고
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자
}
