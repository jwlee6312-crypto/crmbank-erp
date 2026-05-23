package com.crmbank.erp.crm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 상담 통합 저장 요청 DTO (프론트엔드 대문자 키값 완벽 호환)
 */
@Data
public class ConsultSaveRequest {
    @JsonProperty("CMPYCD")
    private String cmpycd;

    @JsonProperty("CAMP_NO")
    private String campNo;
    
    @JsonProperty("CALL_SEQ")
    private int callSeq;
    
    @JsonProperty("USERID")
    private String userid;

    @JsonProperty("LINE_NUM")
    private String lineNum;    // 내선번호
    
    @JsonProperty("CUST_EMAIL")
    private String custEmail;
    
    @JsonProperty("CUST_NM")
    private String custNm;

    @JsonProperty("CALL_TELNO")
    private String callTelno;  // 고객연락처
    
    @JsonProperty("RSLT_CD")
    private String rsltCd;
    
    @JsonProperty("MEMO")
    private String memo;
    
    @JsonProperty("RESV_DTIME")
    private String resvDtime; 
    
    @JsonProperty("RESV_Memo")
    private String resvMemo;  
    
    @JsonProperty("SURVEYS")
    private List<Map<String, Object>> surveys; // 💡 Object 타입으로 변경하여 점수(point) 수신 허용
    
    @JsonProperty("CHAT_HISTORY")
    private String chatHistory;
    
    @JsonProperty("REC_FILE")
    private String recFile;

    @JsonProperty("START_TIME")
    private String startTime;
    
    @JsonProperty("END_TIME")
    private String endTime;
    
    @JsonProperty("MEDIA_TYPE")
    private String mediaType;
}
