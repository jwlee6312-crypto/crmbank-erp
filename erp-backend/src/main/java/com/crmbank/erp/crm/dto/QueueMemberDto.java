package com.crmbank.erp.crm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Asterisk 큐 멤버 (QUEUE_MEMBERS) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueMemberDto {

    private Integer uniqueid;       // 고유번호 (PK)
    private String queue_name;      // 큐 이름
    private String interface_name;  // 인터페이스 (예: PJSIP/1001) - DB의 'interface' 컬럼 대응
    private String membername;      // 멤버 이름
    private String state_interface; // 상태 감시 인터페이스
    private Integer penalty;        // 페널티 (우선순위)
    private Integer paused;         // 일시정지 여부 (0: 활성, 1: 중지)
    private Integer wrapuptime;     // 통화 후 정리 시간
}
