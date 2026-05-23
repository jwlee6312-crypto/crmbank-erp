package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 인바운드상담녹취파일 (CALL_MONITOR_TBL) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallMonitorDto {

    private String CMPYCD;      // 회사코드 (PK)
    private String SVCNO;       // 접수번호 (PK)
    private String ROW;         // 녹취SERIAL (PK)
    private String MONITERNM;   // 녹취파일
    private LocalDateTime ADDTIME; // 등록일시
    private LocalDateTime UPDTIME; // 수정일시
    private String UPDEMP;      // 수정자ID
}
