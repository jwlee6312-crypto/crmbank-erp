package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 인바운드상담 (CALL_MST_TBL) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallMstDto {

    private String cmpycd;           // 회사코드 (PK)
    private String svcno;            // 접수번호 (PK)
    private String deptcd;           // 부서코드
    private String custcd;           // 거래처
    private String svcymd;           // 접수일자
    private String iono;             // 출고번호
    private String itemcd;           // 가입상품
    private String trb_ment;         // 문의내용
    private String ans_ment;         // 답변내용
    private String ai_summary;       // ai가 요약한 상담 핵심 내용
    private LocalDateTime start_time; // 상담시작일시
    private LocalDateTime end_time;   // 상담종료일시
    private String consultid;        // 상담원id
    private String rec_file;         // 녹취/로그파일경로
    private String escalation_yn;    // 상담이관여부
    private String escalation_no;    // 상담이관번호
    private String esc_memo;         // 이관내용
    private String happycall_yn;     // 해피콜여부
    private String interaction_id;   // 💡 추가: 통합 상담 고유 id 연동
    private LocalDateTime addtime;   // addtime
    private LocalDateTime updtime;   // updtime
    private String updemp;           // updemp
    
    // 💡 내부 관리를 위한 추가 필드
    private String linkedid;         // Asterisk LinkedID (이관 테이블 연동용)
}
