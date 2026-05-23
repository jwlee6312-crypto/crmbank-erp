package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 질문 상세/답변 (SURV_DTL) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurvDtlDto {

    private String CMPYCD;           // 회사코드
    private String SURV_NO;          // 질문번호
    private String ANS_NO;           // 답변번호
    private String ANS_CONT;         // 답변내용
    private Integer ANS_POINT;       // 답변점수
    private String USEYN;            // 사용여부
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자
    private String ESSAY_YN;         // 주관식여부
}
