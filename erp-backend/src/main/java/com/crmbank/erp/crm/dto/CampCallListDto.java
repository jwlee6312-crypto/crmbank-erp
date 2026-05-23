package com.crmbank.erp.crm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Map;

/**
 * 캠페인 전화 발신 대상 목록 (CAMPAIGN_CALL_LIST) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampCallListDto {
    private Integer CALL_SEQ;        // 전화목록 고유번호
    private String CMPYCD;           // 회사코드
    private String CAMP_NO;          // 캠페인번호
    private String SURV_GB;          // 설문유형
    private String CUST_NM;          // 고객명
    private String EMAIL;            // 메일주소
    private String TEL_NO;           // 연락처
    private String STATUS;           // 상담상태코드
    private String STATUS_NM;        // 상담상태명
    private String RSLT_CD;          // 상담결과코드
    private String RSLT_NM;          // 상담결과명
    private String USERID;           // 배정상담원
    private Map<String, Object> EXT_DATA; // 가변 속성 데이터 (JSON)
    private Integer READ_CNT;        // 조회횟수
    private String REDIAL_YN;        // 재통화필요여부
    private String RESV_DTIME;       // ✅ 약속시간 (추가)
    private String RESV_MEMO;        // ✅ 예약메모 (추가)
    private String UPDEMP;           // 수정자
}
