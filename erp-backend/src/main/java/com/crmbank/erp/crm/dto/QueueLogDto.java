package com.crmbank.erp.crm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Asterisk 큐 로그 (QUEUE_LOG) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueLogDto {
    private Long id;          // 고유번호
    private String time;      // 발생시각
    private String callid;    // 콜 고유ID
    private String queuename; // 큐 이름
    private String agent;     // 상담원
    private String event;     // 이벤트명 (ENTERQUEUE, CONNECT, ABANDON 등)
    private String data1;     // 부가데이터1
    private String data2;     // 부가데이터2
    private String data3;     // 부가데이터3
    private String data4;     // 부가데이터4
    private String data5;     // 부가데이터5
}
