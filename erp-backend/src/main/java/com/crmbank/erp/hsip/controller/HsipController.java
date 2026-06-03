package com.crmbank.erp.hsip.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsip.dto.Hsip100uRequest;
import com.crmbank.erp.hsip.mapper.HsipMapper;
import com.crmbank.erp.hsip.service.HsipService;
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
    private final HsipService hsipService;

    /**
     * 🚀 수입발주 통합 저장 (HSOD100U와 동일 패턴)
     */
    @PostMapping("/HSIP_100U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveImportOrder(@RequestBody Hsip100uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            // 세션 정보 주입
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            
            log.info("💾 수입발주 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getFileno() : "NULL");
            Map<String, Object> result = hsipService.saveImportOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 수입발주 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<ApiResponse<?>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        
        // 📋 SSMS 실행용 로그 생성
        log.info("📋 SSMS 실행용: {}", buildSsmsLog(procedure, params));
        
        try {
            Object result;
            switch (procedure.toUpperCase()) {
                case "HSIP_100U_STR": result = hsipMapper.HSIP_100U_STR(params); break;
                case "HSIP_101U_STR": result = hsipMapper.HSIP_101U_STR(params); break;
                case "HSIP_110U_STR": result = hsipMapper.HSIP_110U_STR(params); break;
                case "HSIP_111U_STR": result = hsipMapper.HSIP_111U_STR(params); break;
                case "HSIP_120U_STR": result = hsipMapper.HSIP_120U_STR(params); break;
                case "HSIP_121U_STR": result = hsipMapper.HSIP_121U_STR(params); break;
                case "HSIP_130U_STR": result = hsipMapper.HSIP_130U_STR(params); break;
                case "HSIP_131U_STR": result = hsipMapper.HSIP_131U_STR(params); break;
                case "HSIP_140U_STR": result = hsipMapper.HSIP_140U_STR(params); break;
                case "HSIP_145U_STR": result = hsipMapper.HSIP_145U_STR(params); break;
                case "HSIP_150U_STR": result = hsipMapper.HSIP_150U_STR(params); break;
                case "HSIP_155U_STR": result = hsipMapper.HSIP_155U_STR(params); break;
                case "HSIP_160U_STR": result = hsipMapper.HSIP_160U_STR(params); break;
                case "HSIP_161S_STR": result = hsipMapper.HSIP_161S_STR(params); break;
                case "HSIP_180U_STR": result = hsipMapper.HSIP_180U_STR(params); break;
                case "HSIP_200S_STR": result = hsipMapper.HSIP_200S_STR(params); break;
                case "HSIP_210S_STR": result = hsipMapper.HSIP_210S_STR(params); break;
                default:
                    return ResponseEntity.status(404).body(ApiResponse.notFound("해당 서비스를 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(ApiResponse.success(result, "조회 성공"));
        } catch (Exception e) {
            log.error("❌ 프로시저 실행 오류 ({}): {}", procedure, e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
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
            // ... (기존 로그 생성 로직 유지)
            default:
                keys = params.keySet().toArray(new String[0]);
        }

        String values = java.util.Arrays.stream(keys)
                .filter(params::containsKey)
                .map(key -> {
                    Object val = params.get(key);
                    return val == null ? "''" : "'" + val.toString().trim() + "'";
                })
                .collect(Collectors.joining(", "));

        return String.format("EXEC %s %s", proc, values);
    }
}
