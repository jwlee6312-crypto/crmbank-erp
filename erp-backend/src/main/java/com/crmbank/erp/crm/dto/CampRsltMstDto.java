package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 전화상담결과 마스터 (CAMPAIGN_RSLT_MST) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampRsltMstDto {

    private Integer RSLT_NO;         // 결과번호
    private String CMPYCD;           // 회사코드
    private Integer CALL_SEQ;        // 전화목록 고유번호
    private String CAMP_NO;          // 캠페인번호
    private LocalDateTime START_DTIME; // 통화시작
    private LocalDateTime END_DTIME;   // 통화종료
    private String PHONE_TYPE;       // 전화유형
    private String CALL_TELNO;       // 콜번호
    private String RSLT_CD;          // 통화결과
    private String REMARK;           // 특이사항
    private String USERID;           // 사용자
    private String LINE_NUM;         // 내선번호
    private String INTERACTION_ID;   // 통합 상담 고유 ID 연동
    private String MEDIA_TYPE;       // 상담 매체 (CALL, CHAT, SMS)
    private String CHAT_LOG;         // 실시간 채팅/SMS 대화 전체 내역
    private String AI_SUMMARY;       // AI가 요약한 상담 핵심 내용
    private String REC_FILE;         // 💡 추가: 녹취파일명
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자
}
