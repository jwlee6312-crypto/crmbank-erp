package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 통합 통화 행위 및 실패 이력 (TOTAL_CALL_LOG) DTO - 대문자 원칙 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalCallLogDto {

    private String CMPYCD;           // 회사코드
    private String uniqueid;         // Asterisk 고유 ID (Asterisk 유지)
    private String linkedid;         // 연결 ID (전화돌려주기 등) (Asterisk 유지)
    private String KEYWORD;          // 검색 키워드
    private String CALL_TYPE;        // 통화유형
    private String MEDIA_TYPE;       // 매체유형
    private String DIRECTION;        // 방향
    private String SRC_NO;           // 발신번호
    private String DST_NO;           // 수신번호
    private String USERID;           // 상담원ID
    private String EXTEN;            // 내선번호
    private LocalDateTime START_TIME; // 시작시간
    private LocalDateTime ANSWER_TIME;// 응답시간
    private LocalDateTime END_TIME;   // 종료시간
    private Integer WAIT_SEC;        // 대기시간
    private Integer TALK_SEC;        // 통화시간
    private String RESULT_CD;        // 결과코드
    private Integer HANGUP_CAUSE;    // Asterisk 종료 코드
    private String FAIL_REASON;      // 상세 실패 사유
    private String REDIAL_YN;        // 재시도 여부
    private Integer REDIAL_COUNT;    // 재시도 횟수
    private String REC_FILE;         // 녹취파일명
    private String CALL_MEMO;        // 통화 메모

    // 💡 콜백 관련 필드 추가 (대문자 원칙)
    private String CALLBACK_YN;      // 콜백여부 (y/n)
    private String CALLBACK_NO;      // 고객이 남긴 콜백 연락처
    private Integer CALLBACK_RETRY_CNT; // 콜백 시도 횟수
    private LocalDateTime CALLBACK_REQ_TIME; // 콜백 요청 시각
}
