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

@Slf4j
@RestController
@RequestMapping("/api/hsip")
@RequiredArgsConstructor
public class HsipController {

    private final HsipMapper hsipMapper;
    private final HsipService hsipService;

    /**
     * 🚀 수입발주 통합 저장 (Service 레이어 사용 -> ApiResponse 유지)
     */
    @PostMapping("/HSIP_100U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveImportOrder(@RequestBody Hsip100uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsipService.saveImportOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 단순 프로시저 호출 (Mapper 직접 사용 -> List 반환)
     */
    @PostMapping("/{procedure}")
    public List<Map<String, Object>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        log.info("📋 [HSIP] {} 호출: {}", procedure.toUpperCase(), params);
        
        switch (procedure.toUpperCase()) {
            case "HSIP_100U_STR": return hsipMapper.HSIP_100U_STR(params);
            case "HSIP_101U_STR": return hsipMapper.HSIP_101U_STR(params);
            case "HSIP_110U_STR": return hsipMapper.HSIP_110U_STR(params);
            case "HSIP_111U_STR": return hsipMapper.HSIP_111U_STR(params);
            case "HSIP_120U_STR": return hsipMapper.HSIP_120U_STR(params);
            case "HSIP_121U_STR": return hsipMapper.HSIP_121U_STR(params);
            case "HSIP_130U_STR": return hsipMapper.HSIP_130U_STR(params);
            case "HSIP_131U_STR": return hsipMapper.HSIP_131U_STR(params);
            case "HSIP_140U_STR": return hsipMapper.HSIP_140U_STR(params);
            case "HSIP_145U_STR": return hsipMapper.HSIP_145U_STR(params);
            case "HSIP_150U_STR": return hsipMapper.HSIP_150U_STR(params);
            case "HSIP_155U_STR": return hsipMapper.HSIP_155U_STR(params);
            case "HSIP_160U_STR": return hsipMapper.HSIP_160U_STR(params);
            case "HSIP_161S_STR": return hsipMapper.HSIP_161S_STR(params);
            case "HSIP_180U_STR": return hsipMapper.HSIP_180U_STR(params);
            case "HSIP_200S_STR": return hsipMapper.HSIP_200S_STR(params);
            case "HSIP_210S_STR": return hsipMapper.HSIP_210S_STR(params);
            default:
                return null;
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
}
