package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * ARS 콜백 요청 (ARS_CALLBACK) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArsCallbackDto {
    private Integer id;             // 고유번호
    private String cmpycd;          // 회사코드
    private String call_id;         // Asterisk Unique ID
    private String caller_num;      // 고객 발신 번호
    private String callback_num;    // 콜백 받을 번호
    private LocalDateTime request_time; // 요청일시
    private String status;          // 상태 (010:대기, 020:처리중, 030:완료)
    private String agent_id;        // 처리 상담원 ID
    private LocalDateTime process_time; // 처리일시
}
