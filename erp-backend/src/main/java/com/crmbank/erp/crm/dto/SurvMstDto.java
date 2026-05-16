package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 질문 마스터 (SURV_MST) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurvMstDto {

    private String cmpycd;           // 회사코드
    private String surv_no;          // 질문번호
    private String question;         // 질문내용
    private String ans_tp;           // 답변유형
    private Integer ans_cnt;         // 답변개수
    private Double weight;           // 가중치
    private String dspord;           // 표시순서 (Integer -> String/Char(3) 수정)
    private String useyn;            // 사용여부
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자
}
