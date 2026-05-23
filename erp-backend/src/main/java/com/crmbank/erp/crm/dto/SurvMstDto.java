package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 질문 마스터 (SURV_MST) DTO - 대문자 원칙 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurvMstDto {

    private String CMPYCD;           // 회사코드
    private String SURV_NO;          // 질문번호
    private String QUESTION;         // 질문내용
    private String ANS_TP;           // 답변유형
    private Integer ANS_CNT;         // 답변개수
    private Double WEIGHT;           // 가중치
    private String DSPORD;           // 표시순서
    private String USEYN;            // 사용여부
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자
}
