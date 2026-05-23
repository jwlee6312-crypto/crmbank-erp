package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * AS결과 (AS_MST_TBL) DTO - 대문자 원칙 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsMstDto {

    private String CMPYCD;           // 회사코드
    private String SVCNO;            // 접수번호
    private String ACCEPT_USERID;    // 접수자
    private String ACCEPTYMD;        // 접수일
    private String CUSTCD;           // 거래처
    private String IONO;             // 출고번호
    private String ITEMCD;           // 품목코드
    private String FIXED_USERID;     // 수리기사
    private String FIXEDYMD;         // 수리일자
    private String FIXED_MENT;       // 수리결과
    private String COMPLETEYMD;      // 완료일
    private String EXPIREYMD;        // AS만료일
    private String CHARGEYN;         // 유상여부
    private Long FIXAMT;             // 수리비용
    private Long FIXVAT;             // 부가세
    private LocalDateTime ADDTIME;   // 등록일시
    private LocalDateTime UPDTIME;   // 수정일시
    private String UPDEMP;           // 수정자
}
