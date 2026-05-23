package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 업무 이관 및 피드백 관리 (CTI_ESCALATION_TBL) DTO - 대문자 원칙 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CtiEscalationDto {
    private Integer ESCALATION_NO;   // 이관 번호 (PK, AI)
    private String SVCNO;            // 상담 접수번호
    private String linkedid;         // Asterisk LinkedID (Asterisk 관련 필드 - 소문자 유지)
    private LocalDateTime REG_DATE;  // 이관 요청일
    private String SENDER_ID;        // 이관 요청자 ID
    private String DEPTCD;           // 이관 대상 부서코드
    private String SUMMARY;          // 상담 요약
    private String ESC_MEMO;         // 이관 요청 메모
    private String STATUS;           // 상태 (100: REQUEST, 200: PROGRESS, 300: DONE)
    private String FEEDBACK_MEMO;    // 피드백 내용
    private LocalDateTime FEEDBACK_DATE; // 피드백 완료일
    private String FEEDBACK_USER;    // 피드백 작성자
    
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자
}
