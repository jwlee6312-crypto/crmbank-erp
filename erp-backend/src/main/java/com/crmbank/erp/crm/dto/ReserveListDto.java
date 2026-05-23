package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 상담예약 (RESERVE_LIST) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveListDto {

    private String CMPYCD;           // 회사코드
    private Integer RESV_NO;         // 예약번호
    private String RESV_GB;          // 예약구분
    private Integer CALL_SEQ;        // 콜번호
    private String USERID;           // 사용자ID
    private String RESV_DT;          // 예약일자
    private String RESV_H;           // 예약시
    private String RESV_M;           // 예약분
    private String PHONE_TYPE;       // 전화유형
    private String TELNO;            // 전화번호
    private String RESV_MEMO;        // 예약메모
    private LocalDateTime ALARM_DT;  // 알람일시
    private String FLAG;             // 플래그
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자
}
