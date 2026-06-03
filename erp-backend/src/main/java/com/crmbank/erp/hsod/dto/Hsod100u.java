package com.crmbank.erp.hsod.dto;

import lombok.Data;

/**
 * 주문 마스터 DTO
 * 완전 소문자 표준화 적용 (JsonProperty 제거 및 postno 추가)
 */
@Data
public class Hsod100u {
    private String actkind;
    private String cmpycd;
    private String ymdfr;
    private String ymdto;
    private String ordym;
    private String ordno;
    private String ordkind;
    private String deptcd;
    private String deptnm;
    private String custcd;
    private String custnm;
    private String whcd;
    private String area;
    private String ordymd;
    private String ordemp;
    private String paycndt;
    private String outymd;
    private String trancd;
    private String postno;
    private String address;
    private String remark;
    private String totsum;
    private String sts;
    private String updemp;
    private String tranamt;
}
