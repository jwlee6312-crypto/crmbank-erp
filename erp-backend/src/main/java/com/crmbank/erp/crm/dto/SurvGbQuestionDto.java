package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 설문유형별 질문지 매핑 (SURV_GB_QUESTION) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurvGbQuestionDto {

    private String CMPYCD;           // 회사코드
    private String SURV_GB;          // 설문유형 (CDTYPE='910')
    private String SURV_NO;          // 질문번호 (SURV_MST 외래키)
    private String SORTCD;           // 정렬순서
    private String USEYN;            // 사용여부 (Y/N)
    
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자ID
}
