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
    
    @JsonProperty("CUST_EMAIL")
    private String custEmail;
    
    @JsonProperty("CUST_NM")
    private String custNm;
    
    @JsonProperty("RSLT_CD")
    private String rsltCd;
    
    @JsonProperty("MEMO")
    private String memo;
    
    @JsonProperty("RESV_DTIME")
    private String resvDtime; 
    
    @JsonProperty("RESV_MEMO")
    private String resvMemo;  
    
    @JsonProperty("SURVEYS")
    private List<Map<String, String>> surveys; 
    
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
