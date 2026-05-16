package com.crmbank.erp.comm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;

@Data
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("CMPYCD")
    private String CMPYCD;
    
    @JsonProperty("CMPYNM")
    private String CMPYNM;
    
    @JsonProperty("USERID")
    private String USERID;
    
    @JsonProperty("USERNM")
    private String USERNM;
    
    @JsonProperty("INNER_NO")
    private String INNER_NO;

    @JsonProperty("EMAIL")
    private String EMAIL;

    
    @JsonProperty("DEPTCD")
    private String DEPTCD;
    
    @JsonProperty("DEPTNM")
    private String DEPTNM;
    
    @JsonProperty("EMPNO")
    private String EMPNO;
    
    @JsonProperty("DOMAIN")
    private String DOMAIN;
    
    @JsonProperty("SALSYN")
    private String SALSYN;
    
    @JsonProperty("USERGRP")
    private String USERGRP;
    
    @JsonProperty("DATENAME")
    private String DATENAME;

    // 기존 호환성용 Getter/Setter
    public String getUSER_ID() { return this.USERID; }
    public void setUSER_ID(String id) { this.USERID = id; }
    public String getUSER_NAME() { return this.USERNM; }
    public void setUSER_NAME(String name) { this.USERNM = name; }

    private String LEFTLOGO;
    private String RIGHTIMG;
    private String ACCTMENU;
    private String PUCHMENU;
    private String SALSMENU;
    private String INSAMENU;
    private String MNGTMENU;
    private String ETC1MENU;
    private String ETC2MENU;
    private String ACCTCODE;
    private String PUCHCODE;
    private String SALSCODE;
    private String INSACODE;
    private String MNGTCODE;
    private String ETC1CODE;
    private String ETC2CODE;
    private String ETC3CODE;
    private String ETC4CODE;
    private String CAPYRIGHT;
}
