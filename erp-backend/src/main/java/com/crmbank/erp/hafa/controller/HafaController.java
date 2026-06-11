package com.crmbank.erp.hafa.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hafa.mapper.HafaMapper;
import com.crmbank.erp.hafa.service.HafaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hafa")
@RequiredArgsConstructor
public class HafaController {

    private final HafaMapper hafaMapper;
    private final HafaService hafaService;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
            if (!params.containsKey("usernm")) params.put("usernm", user.getUsernm());
        }
    }

    @PostMapping("/HAFA_010U_STR")
    public List<Map<String, Object>> executeHafa010U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_010U_STR(params);
    }

    @PostMapping("/HAFA_020S_STR")
    public List<Map<String, Object>> getHafa020S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_020S_STR(params);
    }

    @PostMapping("/HAFA_040S_STR")
    public List<Map<String, Object>> getHafa040S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_040S_STR(params);
    }

    @PostMapping("/HAFA_050U_STR")
    public List<Map<String, Object>> executeHafa050U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_050U_STR(params);
    }

    @PostMapping("/HAFA_090U_STR")
    public List<Map<String, Object>> executeHafa090U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_090U_STR(params);
    }

    @PostMapping("/HAFA_120S_STR")
    public List<Map<String, Object>> getHafa120S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_120S_STR(params);
    }

    @PostMapping("/HAFA_130S_STR")
    public List<Map<String, Object>> getHafa130S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_130S_STR(params);
    }

    @PostMapping("/HA00_150S_STR")
    public List<Map<String, Object>> getHafa150Header(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HA00_150S_STR(params);
    }

    @PostMapping("/HAFA_140S_STR")
    public List<Map<String, Object>> getHafa140S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_140S_STR(params);
    }

    @PostMapping("/HAFA_150U_STR")
    public List<Map<String, Object>> getHafa150U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_150U_STR(params);
    }

    /**
     * 🚀 감가상각전표 발행 처리 (Service 복합 로직 -> ApiResponse 유지)
     */
    @PostMapping("/HAFA_150U_SAVE")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveHafa150U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        try {
            Map<String, Object> result = hafaService.saveDepreciationSlip(params);
            return ResponseEntity.ok(ApiResponse.success(result, "성공"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HAFA_900U_STR")
    public List<Map<String, Object>> executeHafa900U(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        return hafaMapper.HAFA_900U_STR(params);
    }
}
