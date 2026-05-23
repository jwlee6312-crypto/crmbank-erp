package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 질문 설정 (CAMP_SURVEY) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampSurveyDto {

    private String CMPYCD;           // 회사코드
    private String CAMP_GB;          // 캠페인구분
    private String SURV_GB;          // 설문구분
    private String SURV_NO;          // 질문번호
    private String SORTCD;           // 정렬코드
    private String USEYN;            // 사용여부
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자
}
