package com.crmbank.erp.hsip.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsip.mapper.HsipMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/hsip")
@RequiredArgsConstructor
public class HsipController {

    private final HsipMapper hsipMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            if (!params.containsKey("CMPYCD")) params.put("CMPYCD", user.getCMPYCD());
            if (!params.containsKey("USERID")) params.put("USERID", user.getUSERID());
            params.put("UPDEMP", user.getUSERID());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        
        // 📋 SSMS 실행용 로그 생성
        log.info("📋 SSMS 실행용: {}", buildSsmsLog(procedure, params));
        
        switch (procedure.toUpperCase()) {
            case "HSIP_100U_STR": return ResponseEntity.ok(hsipMapper.HSIP_100U_STR(params));
            case "HSIP_101U_STR": return ResponseEntity.ok(hsipMapper.HSIP_101U_STR(params));
            case "HSIP_110U_STR": return ResponseEntity.ok(hsipMapper.HSIP_110U_STR(params));
            case "HSIP_111U_STR": return ResponseEntity.ok(hsipMapper.HSIP_111U_STR(params));
            case "HSIP_120U_STR": return ResponseEntity.ok(hsipMapper.HSIP_120U_STR(params));
            case "HSIP_121U_STR": return ResponseEntity.ok(hsipMapper.HSIP_121U_STR(params));
            case "HSIP_130U_STR": return ResponseEntity.ok(hsipMapper.HSIP_130U_STR(params));
            case "HSIP_131U_STR": return ResponseEntity.ok(hsipMapper.HSIP_131U_STR(params));
            case "HSIP_140U_STR": return ResponseEntity.ok(hsipMapper.HSIP_140U_STR(params));
            case "HSIP_145U_STR": return ResponseEntity.ok(hsipMapper.HSIP_145U_STR(params));
            case "HSIP_150U_STR": return ResponseEntity.ok(hsipMapper.HSIP_150U_STR(params));
            case "HSIP_155U_STR": return ResponseEntity.ok(hsipMapper.HSIP_155U_STR(params));
            case "HSIP_160U_STR": return ResponseEntity.ok(hsipMapper.HSIP_160U_STR(params));
            case "HSIP_161S_STR": return ResponseEntity.ok(hsipMapper.HSIP_161S_STR(params));
            case "HSIP_180U_STR": return ResponseEntity.ok(hsipMapper.HSIP_180U_STR(params));
            case "HSIP_200S_STR": return ResponseEntity.ok(hsipMapper.HSIP_200S_STR(params));
            case "HSIP_210S_STR": return ResponseEntity.ok(hsipMapper.HSIP_210S_STR(params));
            default:
                log.warn("⚠️ 미등록 HSIP 프로시저: {}", procedure);
                return ResponseEntity.notFound().build();
        }
    }

    private String buildSsmsLog(String procedure, Map<String, Object> params) {
        String[] keys;
        String proc = procedure.toUpperCase();
        
        switch (proc) {
            case "HSIP_100U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "DEPTCD", "OFFERNO", "SUPERGBN", "IMPTGBN", "ISSYMD", "CUSTCD", "CURRCD", "FRGNRATE", "NACD", "SHIPPORT", "PRICOND", "PRITEXT", "ARVPORT", "PAYCOND", "PAYTERM", "LCAMT", "WONAMT", "FRGNAMT", "XTAMT", "BIGO", "INYMD", "APVYN", "APVYMD", "APVEMP", "UPDEMP"};
                break;
            case "HSIP_101U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "PROWNO", "ITEMCD", "ITSIZE", "UNIT", "QTY", "AMT", "UPDEMP"};
                break;
            case "HSIP_110U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "SHIPSEQ", "SHIPYMD", "ARVYMD", "STORSEAT", "BLNO", "FRGNRATE", "UPDEMP"};
                break;
            case "HSIP_111U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "SHIPSEQ", "SROWNO", "ITEMCD", "ITSIZE", "UNIT", "QTY", "AMT", "WONAMT", "UPDEMP"};
                break;
            case "HSIP_120U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "SHIPSEQ", "PASSSEQ", "PASSYMD", "TAXORG", "PASSNO", "WHCD", "DEPTCD", "IOYM", "IONO", "UPDEMP"};
                break;
            case "HSIP_121U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "SHIPSEQ", "PASSSEQ", "PROWNO", "ITEMCD", "ITSIZE", "UNIT", "GQTY", "QTY", "AMT", "IOYM", "IONO", "IOROWNO", "UPDEMP"};
                break;
            case "HSIP_130U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "DOCNO", "DEPTCD", "PUBYMD", "BIGO", "UPDEMP"};
                break;
            case "HSIP_131U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "DOCNO", "ROWNO", "COSTCD", "DEPTCD", "SHIPSEQ", "PASSSEQ", "PUBYMD", "COSTAMT", "CURRCD", "FRGNRATE", "FRGNAMT", "PAYCUST", "MGTNO", "BIGO", "SLIPYMD", "SLIPNO", "SROWNO", "UPDEMP"};
                break;
            case "HSIP_140U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "DEPTCD", "IOYMDFR", "IOYMDTO", "FILENO", "DOCNO", "CROWNO", "SLIPYMD", "SLIPNO", "UPDEMP"};
                break;
            case "HSIP_145U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "COSTCD", "IOYMDFR", "IOYMDTO", "DEPTCD", "FILENO", "DOCNO", "CROWNO", "JSANYMD", "SPYAMT", "VATAMT", "CUSTCD", "TAXUNIT", "VATTYPE", "SLIPYMD", "SLIPNO", "SLIPKIND", "HDEPTCD", "BUSINESS", "UPDEMP"};
                break;
            case "HSIP_150U_STR":
            case "HSIP_155U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "IOYMDFR", "IOYMDTO", "DEPTCD", "SLIPYMD", "SLIPNO", "UPDEMP"};
                break;
            case "HSIP_160U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "DEPTCD", "FRYMD", "TOYMD", "DIVYN", "FILENO", "SHIPSEQ", "PASSSEQ", "UPDEMP"};
                break;
            case "HSIP_161S_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "SHIPSEQ", "PASSSEQ", "DIVYN", "ITEMCD"};
                break;
            case "HSIP_180U_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "SHIPSEQ", "PASSSEQ", "IOYM", "IONO", "IOROWNO", "IOAMT", "UPDEMP"};
                break;
            case "HSIP_200S_STR":
                keys = new String[]{"ACTKIND", "CMPYCD", "FILENO", "DEPTCD"};
                break;
            case "HSIP_210S_STR":
                keys = new String[]{"CMPYCD", "YY", "DEPTCD"};
                break;
            default:
                keys = params.keySet().toArray(new String[0]);
        }

        String values = java.util.Arrays.stream(keys)
                .map(key -> {
                    Object val = params.get(key);
                    return val == null ? "''" : "'" + val.toString().trim() + "'";
                })
                .collect(Collectors.joining(", "));

        return String.format("EXEC %s %s", proc, values);
    }
}
