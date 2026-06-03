package com.crmbank.erp.hfba.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hfba.mapper.HfbaMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/hfba")
@RequiredArgsConstructor
public class HfbaController {

    private final HfbaMapper hfbaMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        // 💡 [소문자 원칙 적용] 프론트엔드(axios.ts)에서 소문자로 통일하여 보내므로, 백엔드에서도 소문자 키를 유지합니다.
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.putIfAbsent("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }

        // 💡 모든 파라미터 값을 문자열로 변환하고 빈 값 처리 (DB 숫자 변환 오류 방지 및 검색 필터 호환성 유지)
        params.replaceAll((key, value) -> {
            if (value == null) return "";
            return value.toString().trim();
        });
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        injectSession(params, session);
        log.info("🚀 [HFBA] {} 호출: {}", procedure.toUpperCase(), params);
        
        List<Map<String, Object>> result;
        switch (procedure.toUpperCase()) {
            case "FBA1010U_STR": result = hfbaMapper.FBA1010U_STR(params); break;
            case "FBA1040U_STR": result = hfbaMapper.FBA1040U_STR(params); break;
            case "FBA1060U_STR": result = hfbaMapper.FBA1060U_STR(params); break;
            case "FBA2010U_STR": result = hfbaMapper.FBA2010U_STR(params); break;
            case "FBA3010U_STR": result = hfbaMapper.FBA3010U_STR(params); break;
            case "SELECT_ACCT_LIST": result = hfbaMapper.selectAcctList(params); break;
            case "SELECT_DIVIDE_LIST": result = hfbaMapper.selectDivideList(params); break;
            case "SELECT_DIVIDE_JUKSU_LIST": result = hfbaMapper.selectDivideJuksuList(params); break;
            default:
                return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(result, "조회 성공"));
    }
}
