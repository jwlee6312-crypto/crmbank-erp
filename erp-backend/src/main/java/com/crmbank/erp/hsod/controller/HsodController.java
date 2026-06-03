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

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsod")
@RequiredArgsConstructor
public class HsodController {

    private final HsodMapper hsodMapper;
    private final HsodService hsodService;

    /**
     * 🚀 주문등록 통합 저장 (ApiResponse 표준 체계)
     */
    @PostMapping("/HSOD_100U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOrder(@RequestBody Hsod100uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            // 세션의 회사코드와 사용자ID 강제 주입
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            
            log.info("💾 주문 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getOrdym() : "NULL");
            Map<String, Object> result = hsodService.saveOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 주문 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 개별 프로시저 호출 (Map 직접 전달 방식)
     */
    @PostMapping("/{procedure}")
    public ResponseEntity<ApiResponse<?>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        
        try {
            Object result;
            switch (procedure.toUpperCase()) {
                case "HSOD_100U_STR": result = hsodMapper.HSOD_100U_STR(params); break;
                case "HSOD_101U_STR": result = hsodMapper.HSOD_101U_STR(params); break;
                case "HSOD_110S_STR": result = hsodMapper.HSOD_110S_STR(params); break;
                case "HSOD_120U_STR": result = hsodMapper.HSOD_120U_STR(params); break;
                case "HSOD_210U_STR": result = hsodMapper.HSOD_210U_STR(params); break;
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
            params.put("updemp", user.getUserid());
        }
    }
}
