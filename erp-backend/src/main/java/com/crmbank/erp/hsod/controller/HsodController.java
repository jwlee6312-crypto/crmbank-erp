package com.crmbank.erp.hsod.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsod.dto.Hsod100uRequest;
import com.crmbank.erp.hsod.mapper.HsodMapper;
import com.crmbank.erp.hsod.service.HsodService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsod")
@RequiredArgsConstructor
public class HsodController {

    private final HsodMapper hsodMapper;
    private final HsodService hsodService;

    /**
     * 🚀 주문등록 통합 저장 (Service 레이어 사용 -> ApiResponse 유지)
     */
    @PostMapping("/HSOD_100U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOrder(@RequestBody Hsod100uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsodService.saveOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 주문 저장 오류: {}", e.getMessage());
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
        
        switch (procedure.toUpperCase()) {
            case "HSOD_100U_STR": return hsodMapper.HSOD_100U_STR(params);
            case "HSOD_101U_STR": return hsodMapper.HSOD_101U_STR(params);
            case "HSOD_110S_STR": return hsodMapper.HSOD_110S_STR(params);
            case "HSOD_120U_STR": return hsodMapper.HSOD_120U_STR(params);
            case "HSOD_210U_STR": return hsodMapper.HSOD_210U_STR(params);
            default:
                return null;
        }
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            params.put("updemp", user.getUserid());
        }
    }
}
