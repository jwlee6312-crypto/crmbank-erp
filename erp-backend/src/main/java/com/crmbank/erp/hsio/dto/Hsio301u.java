package com.crmbank.erp.hsio.dto;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 매입등록 마스터 DTO (HSIO_500U_STR)
 */
@Data
public class Hsio301u {
    private String actkind;
    private String cmpycd;
    private String custcd;
    private String iogbn;
    private String ioym;
    private String iono;
    private String imym;
    private String imno;
    private String imrowno;
    private String imymd;
    private String maeamt;
    private String imamt;
    private String deptcd;
    private String userid;
}
