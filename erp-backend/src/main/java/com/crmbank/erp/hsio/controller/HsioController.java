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

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsio")
@RequiredArgsConstructor
public class HsioController {

    private final HsioMapper hsioMapper;
    private final HsioService hsioService;

    @PostMapping("/hsio_010u_save")
    public ResponseEntity<ApiResponse<?>> saveRequest(@RequestBody Hsio010uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveRequest(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/hsio_050u_save")
    public ResponseEntity<ApiResponse<?>> saveOrder(@RequestBody Hsio050uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/hsio_500u_save")
    public ResponseEntity<ApiResponse<?>> savePurchase(@RequestBody Hsio500uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String userId = (user != null) ? user.getUserid() : "system";
        String cmpycd = (user != null) ? user.getCmpycd() : "";
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.savePurchase(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/{procedure}")
    public List<Map<String, Object>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        
        switch (procedure.toLowerCase()) {
            case "hsio_010u_str": return hsioMapper.HSIO_010U_STR(params);
            case "hsio_011u_str": return hsioMapper.HSIO_011U_STR(params);
            case "hsio_020u_str": return hsioMapper.HSIO_020U_STR(params);
            case "hsio_021u_str": return hsioMapper.HSIO_021U_STR(params);
            case "hsio_050u_str": return hsioMapper.HSIO_050U_STR(params);
            case "hsio_051u_str": return hsioMapper.HSIO_051U_STR(params);
            case "hsio_052u_str": return hsioMapper.HSIO_052U_STR(params);
            case "hsio_060u_str": return hsioMapper.HSIO_060U_STR(params);
            case "hsio_061u_str": return hsioMapper.HSIO_061U_STR(params);
            case "hsio_070u_str": return hsioMapper.HSIO_070U_STR(params);
            case "hsio_080s_str": return hsioMapper.HSIO_080S_STR(params);
            case "hsio_082s_str": return hsioMapper.HSIO_082S_STR(params);
            case "hsio_085s_str": return hsioMapper.HSIO_085S_STR(params);
            case "hsio_110u_str": return hsioMapper.HSIO_110U_STR(params);
            case "hsio_120u_str": return hsioMapper.HSIO_120U_STR(params);
            case "hsio_130u_str": return hsioMapper.HSIO_130U_STR(params);
            case "hsio_131u_str": return hsioMapper.HSIO_131U_STR(params);
            case "hsio_140u_str": return hsioMapper.HSIO_140U_STR(params);
            case "hsio_141u_str": return hsioMapper.HSIO_141U_STR(params);
            case "hsio_160u_str": return hsioMapper.HSIO_160U_STR(params);
            case "hsio_170u_str": return hsioMapper.HSIO_170U_STR(params);
            case "hsio_171u_str": return hsioMapper.HSIO_171U_STR(params);
            case "hsio_180u_str": return hsioMapper.HSIO_180U_STR(params);
            case "hsio_181u_str": return hsioMapper.HSIO_181U_STR(params);
            case "hsio_190u_str": return hsioMapper.HSIO_190U_STR(params);
            case "hsio_191u_str": return hsioMapper.HSIO_191U_STR(params);
            case "hsio_200s_str": return hsioMapper.HSIO_200S_STR(params);
            case "hsio_210s_str": return hsioMapper.HSIO_210S_STR(params);
            case "hsio_215s_str": return hsioMapper.HSIO_215S_STR(params);
            case "hsio_220s_str": return hsioMapper.HSIO_220S_STR(params);
            case "hsio_250u_str": return hsioMapper.HSIO_250U_STR(params);
            case "hsio_251u_str": return hsioMapper.HSIO_251U_STR(params);
            case "hsio_300u_str": return hsioMapper.HSIO_300U_STR(params);
            case "hsio_301u_str": return hsioMapper.HSIO_301U_STR(params);
            case "hsio_325u_str": return hsioMapper.HSIO_325U_STR(params);
            case "hsio_410s_str": return hsioMapper.HSIO_410S_STR(params);
            case "hsio_470s_str": return hsioMapper.HSIO_470S_STR(params);
            case "hsio_490u_str": return hsioMapper.HSIO_490U_STR(params);
            case "hsio_491u_str": return hsioMapper.HSIO_491U_STR(params);
            case "hsio_500u_str": return hsioMapper.HSIO_500U_STR(params);
            case "hsio_501u_str": return hsioMapper.HSIO_501U_STR(params);
            case "hsio_510u_str": return hsioMapper.HSIO_510U_STR(params);
            case "hsio_511u_str": return hsioMapper.HSIO_511U_STR(params);
            case "hsio_520u_str": return hsioMapper.HSIO_520U_STR(params);
            case "hsio_521u_str": return hsioMapper.HSIO_521U_STR(params);
            case "hsio_530u_str": return hsioMapper.HSIO_530U_STR(params);
            case "hsio_531u_str": return hsioMapper.HSIO_531U_STR(params);
            case "hsio_540u_str": return hsioMapper.HSIO_540U_STR(params);
            case "hsio_541u_str": return hsioMapper.HSIO_541U_STR(params);
            case "hsio_550u_str": return hsioMapper.HSIO_550U_STR(params);
            case "hsio_551u_str": return hsioMapper.HSIO_551U_STR(params);
            case "hsio_560u_str": return hsioMapper.HSIO_560U_STR(params);
            case "hsio_570u_str": return hsioMapper.HSIO_570U_STR(params);
            case "hsio_571u_str": return hsioMapper.HSIO_571U_STR(params);
            case "hsio_580u_str": return hsioMapper.HSIO_580U_STR(params);
            case "hsio_581u_str": return hsioMapper.HSIO_581U_STR(params);
            case "hsio_600s_str": return hsioMapper.HSIO_600S_STR(params);
            case "hsio_610s_str": return hsioMapper.HSIO_610S_STR(params);
            case "hsio_620s_str": return hsioMapper.HSIO_620S_STR(params);
            case "hsio_640s_str": return hsioMapper.HSIO_640S_STR(params);
            case "hsio_650s_str": return hsioMapper.HSIO_650S_STR(params);
            case "hsio_660s_str": return hsioMapper.HSIO_660S_STR(params);
            case "hsio_680s_str": return hsioMapper.HSIO_680S_STR(params);
            case "hsio_690s_str": return hsioMapper.HSIO_690S_STR(params);
            case "hsio_720u_str": return hsioMapper.HSIO_720U_STR(params);
            case "hsio_721u_str": return hsioMapper.HSIO_721U_STR(params);
            case "hsio_730u_str": return hsioMapper.HSIO_730U_STR(params);
            case "hsio_731u_str": return hsioMapper.HSIO_731U_STR(params);
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
