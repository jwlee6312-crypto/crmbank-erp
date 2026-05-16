package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 통합 통화 행위 및 실패 이력 (TOTAL_CALL_LOG) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalCallLogDto {

    private String cmpycd;           // 회사코드
    private String uniqueid;         // Asterisk 고유 ID
    private String linkedid;         // 연결 ID (전화돌려주기 등)
    private String call_type;        // 통화유형 (010:인바운드, 020:캠페인...)
    private String media_type;       // 매체유형 (call, callback, chat...)
    private String direction;        // 방향 (IN, OUT, INT)
    private String src_no;           // 발신번호
    private String dst_no;           // 수신번호
    private String userid;           // 상담원ID
    private String exten;            // 내선번호
    private LocalDateTime start_time; // 시작시간
    private LocalDateTime answer_time;// 응답시간 (NULL이면 미응답)
    private LocalDateTime end_time;   // 종료시간
    private Integer wait_sec;        // 대기시간
    private Integer talk_sec;        // 통화시간
    private String result_cd;        // 결과코드 (100:성공, 200:포기, 300:실패)
    private Integer hangup_cause;    // Asterisk 종료 코드
    private String fail_reason;      // 상세 실패 사유
    private String redial_yn;        // 재시도 여부
    private Integer redial_count;    // 재시도 횟수
    private String rec_file;         // 녹취파일명
    private String call_memo;        // 통화 메모
}
