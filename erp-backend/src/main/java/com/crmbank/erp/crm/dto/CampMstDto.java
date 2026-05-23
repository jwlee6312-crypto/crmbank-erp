package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 마스터 (CAMPAIGN_MST) DTO
 * 💡 캠페인구분(CAMP_GB)을 설문유형(SURV_GB)으로 통합 관리함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampMstDto {

    private String CMPYCD;           // 회사코드
    private String CAMP_NO;          // 캠페인번호
    private String CAMP_NM;          // 캠페인명
    private String SURV_GB;          // 설문유형 겸 캠페인구분 (통합 필드)
    private Long TOT_DB_CNT;         // 총DB수
    private Long SUCC_DB_CNT;        // 성공DB수
    private String USEYN;            // 사용여부
    private String CALLBACK_YN;      // 콜백여부
    private String REMARK;           // 비고
    private String ACTDATE;          // 실행일자 (YYYYMMDD)
    private String STATUS;           // 실행상태 (010:대기, 020:진행, 030:완료)
    private String START_MENT;       // 시작 인사말
    private String END_MENT;         // 종료 인사말
    private String CONNECT_URL;      // 연결URL
    private String SMS_MENT;         // SMS멘트
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자/상담원ID
}
