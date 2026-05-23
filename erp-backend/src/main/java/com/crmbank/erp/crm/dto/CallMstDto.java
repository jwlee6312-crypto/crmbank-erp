package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 인바운드상담 (CALL_MST_TBL) DTO - 대문자 원칙 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallMstDto {
    private String CMPYCD;           // 회사코드 (PK)
    private String SVCNO;            // 접수번호 (PK)
    private String DEPTCD;           // 부서코드
    private String CUSTCD;           // 거래처코드
    private String SVCYMD;           // 접수일자
    private String IONO;             // 출고번호
    private String ITEMCD;           // 가입상품코드
    private String TRB_MENT;         // 문의내용
    private String ANS_MENT;         // 답변내용
    private String AI_SUMMARY;       // AI 요약 내용
    private LocalDateTime START_TIME; // 상담시작일시
    private LocalDateTime END_TIME;   // 상담종료일시
    private String CONSULTID;        // 상담원ID
    private String REC_FILE;         // 녹취파일명
    private String ESCALATION_YN;    // 이관여부
    private String ESCALATION_NO;    // 이관번호
    private String ESC_MEMO;         // 이관내용
    private String HAPPYCALL_YN;     // 해피콜여부
    private String INTERACTION_ID;   // 통합 상담 고유 ID
    private String LINKEDID;         // Asterisk 고유 ID
    private String CALL_TELNO;       // 상담시 수신번호
    private String CALL_USERNM;      // 상담시 담당자명
    private String CALL_EMAIL;       // 상담시 이메일
    private LocalDateTime ADDTIME;
    private LocalDateTime UPDTIME;
    private String UPDEMP;
}
