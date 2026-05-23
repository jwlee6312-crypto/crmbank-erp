package com.crmbank.erp.hsba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsba.mapper.HsbaMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsba")
@RequiredArgsConstructor
public class HsbaController {

    private final HsbaMapper hsbaMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            if (params.get("CMPYCD") == null) params.put("CMPYCD", user.getCMPYCD());
            if (params.get("USERID") == null) params.put("USERID", user.getUSERID());
            params.put("UPDEMP", user.getUSERID());
            params.put("USER_ID", user.getUSERID());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        injectSession(params, session);

        // 💡 [표준] HSBA_010U_STR: XML 29개 파라미터 완벽 대응
        if ("HSBA_010U_STR".equalsIgnoreCase(procedure)) {
            String[] stringKeys = {
                "ACTKIND", "CMPYCD", "ASTKIND", "ITEMCD", "ITSIZE", "UNIT", "ITEMNM", "ITEMENM",
                "INUNIT", "OUTUNIT", "SETYN", "AGRPCD", "BGRPCD", "VATYN", "SOTAXYN",
                "UDOGYN", "BARCODE", "HSCODE", "REMARK", "USEYN", "IN_CUSTCD", "MAKER"
            };
            for (String key : stringKeys) if (params.get(key) == null) params.put(key, "");
            String[] numberKeys = {"INQTY", "OUTQTY", "IMPRICE", "OMPRICE", "STOCK", "QTYPNT"};
            for (String key : numberKeys) if (params.get(key) == null) params.put(key, 0);
        }

        // 💡 [표준] HSBA_170U_STR: XML 18개 파라미터 완벽 대응
        if ("HSBA_170U_STR".equalsIgnoreCase(procedure)) {
            String[] stringKeys = {
                "ACTKIND", "CMPYCD", "CUSTCD", "CUSTNM", "ROWNO", "DAMKIND", "DAMGBN",
                "DAMYEO", "DAMMGT", "FRYMD", "TOYMD", "SOUNM", "BIGO", "USEYN"
            };
            for (String key : stringKeys) if (params.get(key) == null) params.put(key, "");
            String[] numberKeys = {"DAMSEL", "SAMHAN", "RCVDD"};
            for (String key : numberKeys) if (params.get(key) == null) params.put(key, 0);
        }

        // 💡 [표준] HSBA_190U_STR: XML 11개 파라미터 완벽 대응
        if ("HSBA_190U_STR".equalsIgnoreCase(procedure)) {
            String[] keys = {"ACTKIND", "CMPYCD", "CUSTCD", "CUSTNM", "ROWNO", "DEPTNM", "DAMDANG", "TELNO", "EMAIL", "USEYN", "UPDEMP"};
            for (String key : keys) if (params.get(key) == null) params.put(key, "");
        }

        // 💡 [표준] HSBA_130U_STR: XML 13개 파라미터 완벽 대응
        if ("HSBA_130U_STR".equalsIgnoreCase(procedure)) {
            String[] keys = {"ACTKIND", "CMPYCD", "CUSTCD", "CUSTNM", "TRANCD", "AREA", "POSTNO", "ADDRESS", "DAMDANG", "TELNO", "EMAIL", "USEYN", "UPDEMP"};
            for (String key : keys) if (params.get(key) == null) params.put(key, "");
        }

        // 💡 [표준] HSBA_070U_STR: XML 39개 파라미터 규격 완벽 대응
        if ("HSBA_070U_STR".equalsIgnoreCase(procedure)) {
            String[] stringKeys = {
                "ACTKIND", "CMPYCD", "CUSTCD", "CUSTNO", "CUSTNM", "CUSTGBN", "BOSSNM",
                "JUMINNO", "LEGALNO", "CUSTKIND", "CUSTTYPE", "TELNO", "FAXNO", "POSTNO",
                "ADDRESS", "IOGBN", "INPRCGBN", "OUTPRCGBN", "AGRPCD", "BGRPCD",
                "CGRPCD", "AREA", "CDAMDANG", "CTELNO", "CEMAIL", "REMARK", "STATUS",
                "BANKNM", "GUJOA", "FNDYMD", "STDYMD", "CLSYMD", "SINGRD", "ELCYN",
                "USEYN", "GIGBCD"
            };
            for (String key : stringKeys) if (params.get(key) == null) params.put(key, "");
            String[] numberKeys = {"HDAMT", "RCVDD"};
            for (String key : numberKeys) if (params.get(key) == null) params.put(key, 0);
        }

        // 💡 [표준] HSBA_280U_STR 파라미터 보정
        if ("HSBA_280U_STR".equalsIgnoreCase(procedure)) {
            String[] keys = {"ACTKIND", "CMPYCD", "IOGBN", "CUSTCD", "NACD", "SHIPPORT", "ARVPORT", "CURRCD", "PAYCOND", "PRICOND", "USEYN"};
            for (String key : keys) if (params.get(key) == null) params.put(key, "");
        }

        // 💡 [표준] HSBA_060U_STR: XML 10개 파라미터 완벽 대응
        if ("HSBA_060U_STR".equalsIgnoreCase(procedure)) {
            String[] keys = {"ACTKIND", "CMPYCD", "IOGBN", "ITEMCD", "PRCGBN", "STDYN", "REMARK", "USEYN"};
            for (String key : keys) if (params.get(key) == null) params.put(key, "");
            if (params.get("PRICE") == null) params.put("PRICE", 0);
            if (params.get("USERID") == null) params.put("USERID", params.get("UPDEMP"));
        }

        log.info("🚀 [HSBA] {} 호출: {}", procedure, params);

        switch (procedure.toUpperCase()) {
            case "HSBA_010U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_010U_STR(params));
            case "HSBA_020U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_020U_STR(params));
            case "HSBA_030U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_030U_STR(params));
            case "HSBA_040U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_040U_STR(params));
            case "HSBA_060U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_060U_STR(params));
            case "HSBA_070U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_070U_STR(params));
            case "HSBA_280U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_280U_STR(params));
            case "HSBA_065U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_065U_STR(params));
            case "HSBA_090U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_090U_STR(params));
            case "HSBA_130U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_130U_STR(params));
            case "HSBA_140U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_140U_STR(params));
            case "HSBA_170U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_170U_STR(params));
            case "HSBA_190U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_190U_STR(params));
            case "HSBA_700U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_700U_STR(params));
            case "HSBA_710U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_710U_STR(params));
            case "HSBA_701U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_701U_STR(params));
            case "HSBA_711U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_711U_STR(params));
            case "HSBA_720U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_720U_STR(params));
            case "HSBA_721U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_721U_STR(params));
            case "HSBA_730U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_730U_STR(params));
            case "HSBA_740U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_740U_STR(params));
            case "HSBA_750U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_750U_STR(params));
            case "HSBA_800U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_800U_STR(params));
            case "HSBA_810U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_810U_STR(params));
            case "HSBA_820U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_820U_STR(params));
            case "HSBA_830U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_830U_STR(params));
            case "HSBA_900U_STR": return ResponseEntity.ok(hsbaMapper.HSBA_900U_STR(params));

            default:
                return ResponseEntity.notFound().build();
        }
    }
}
