package com.crmbank.erp.hsio.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsio.dto.*;
import com.crmbank.erp.hsio.mapper.HsioMapper;
import com.crmbank.erp.hsio.service.HsioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsio")
@RequiredArgsConstructor
public class HsioController {

    private final HsioMapper hsioMapper;
    private final HsioService hsioService;

    /**
     * 🚀 요청등록 통합 저장 (HSIO_010U_SAVE)
     */
    @PostMapping("/HSIO_010U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveRequest(@RequestBody Hsio010uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 요청 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getReqym() : "NULL");
            Map<String, Object> result = hsioService.saveRequest(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 요청 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 발주등록 통합 저장 (HSIO_050U_SAVE)
     */
    @PostMapping("/HSIO_050U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOrder(@RequestBody Hsio050uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 발주 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getBalym() : "NULL");
            Map<String, Object> result = hsioService.saveOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 발주 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 일반발주 통합 저장 (HSIO_052U_SAVE)
     */
    @PostMapping("/HSIO_052U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveGeneralOrder(@RequestBody Hsio052uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 일반발주 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getBalym() : "NULL");
            Map<String, Object> result = hsioService.saveGeneralOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 일반발주 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 기타입고 통합 저장 (HSIO_190U_SAVE)
     */
    @PostMapping("/HSIO_190U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOtherIn(@RequestBody Hsio190uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 기타입고 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getIoym() : "NULL");
            Map<String, Object> result = hsioService.saveOtherIn(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 기타입고 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 기타출고 통합 저장 (HSIO_250U_SAVE)
     */
    @PostMapping("/HSIO_250U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOtherOut(@RequestBody Hsio250uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 기타출고 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getIoym() : "NULL");
            Map<String, Object> result = hsioService.saveOtherOut(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 기타출고 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 매입등록 통합 저장 (HSIO_500U_SAVE)
     */
    @PostMapping("/HSIO_500U_SAVE")
    public ResponseEntity<ApiResponse<?>> savePurchase(@RequestBody Hsio500uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 매입 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getIoym() : "NULL");
            Map<String, Object> result = hsioService.savePurchase(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 매입 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 전시장 입출고 통합 저장 (HSIO_570U_SAVE)
     */
    @PostMapping("/HSIO_570U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveStoreInout(@RequestBody Hsio570uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 전시장 입출고 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getIoym() : "NULL");
            Map<String, Object> result = hsioService.saveStoreInout(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 전시장 입출고 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 타창고이동 통합 저장 (HSIO_580U_SAVE)
     */
    @PostMapping("/HSIO_580U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveWarehouseTransfer(@RequestBody Hsio580uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 타창고이동 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getIoym() : "NULL");
            Map<String, Object> result = hsioService.saveWarehouseTransfer(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 타창고이동 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 재고실사 통합 저장 (HSIO_720U_SAVE)
     */
    @PostMapping("/HSIO_720U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveStockAdjustment(@RequestBody Hsio720uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 재고실사 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getIoym() : "NULL");
            Map<String, Object> result = hsioService.saveStockAdjustment(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 재고실사 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 재고조정 통합 저장 (HSIO_730U_SAVE)
     */
    @PostMapping("/HSIO_730U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveInventoryAdjustment(@RequestBody Hsio730Request request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            log.info("💾 재고조정 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getIoym() : "NULL");
            Map<String, Object> result = hsioService.saveInventoryAdjustment(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 재고조정 저장 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    /**
     * 🚀 입금입력 통합 저장 (HSIO_300U_SAVE)
     */
    @PostMapping("/HSIO_300U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveDeposit(@RequestBody Hsio300uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";

        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
                // 마스터 액션코드 설정 (번호가 없으면 신규 A0, 있으면 수정 U0)
                if (request.getMst().getImno() == null || request.getMst().getImno().isEmpty()) {
                    request.getMst().setActkind("A0");
                } else {
                    request.getMst().setActkind("U0");
                }
            }
            log.info("💾 입금 통합 저장 시작: {}-{}", cmpycd, request.getMst() != null ? request.getMst().getImym() : "NULL");
            Map<String, Object> result = hsioService.saveDeposit(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ 입금 저장 오류: {}", e.getMessage());
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
                case "HSIO_500U_STR": result = hsioMapper.HSIO_500U_STR(params); break;
                case "HSIO_501U_STR": result = hsioMapper.HSIO_501U_STR(params); break;
                case "HSIO_010U_STR": result = hsioMapper.HSIO_010U_STR(params); break;
                case "HSIO_011U_STR": result = hsioMapper.HSIO_011U_STR(params); break;
                case "HSIO_050U_STR": result = hsioMapper.HSIO_050U_STR(params); break;
                case "HSIO_051U_STR": result = hsioMapper.HSIO_051U_STR(params); break;
                case "HSIO_052U_STR": result = hsioMapper.HSIO_052U_STR(params); break;
                case "HSIO_060U_STR": result = hsioMapper.HSIO_060U_STR(params); break;
                case "HSIO_190U_STR": result = hsioMapper.HSIO_190U_STR(params); break;
                case "HSIO_191U_STR": result = hsioMapper.HSIO_191U_STR(params); break;
                case "HSIO_250U_STR": result = hsioMapper.HSIO_250U_STR(params); break;
                case "HSIO_251U_STR": result = hsioMapper.HSIO_251U_STR(params); break;
                case "HSIO_550U_STR": result = hsioMapper.HSIO_550U_STR(params); break;
                case "HSIO_570U_STR": result = hsioMapper.HSIO_570U_STR(params); break;
                case "HSIO_571U_STR": result = hsioMapper.HSIO_571U_STR(params); break;
                case "HSIO_580U_STR": result = hsioMapper.HSIO_580U_STR(params); break;
                case "HSIO_581U_STR": result = hsioMapper.HSIO_581U_STR(params); break;
                case "HSIO_720U_STR": result = hsioMapper.HSIO_720U_STR(params); break;
                case "HSIO_721U_STR": result = hsioMapper.HSIO_721U_STR(params); break;
                case "HSIO_730U_STR": result = hsioMapper.HSIO_730U_STR(params); break;
                case "HSIO_731U_STR": result = hsioMapper.HSIO_731U_STR(params); break;
                case "HSIO_300U_STR": result = hsioMapper.HSIO_300U_STR(params); break;
                case "HSIO_301U_STR": result = hsioMapper.HSIO_301U_STR(params); break;
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
