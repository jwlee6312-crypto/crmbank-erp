package com.crmbank.erp.hsip.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsip.dto.Hsip100uRequest;
import com.crmbank.erp.hsip.dto.Hsip120uSaveRequest;
import com.crmbank.erp.hsip.mapper.HsipMapper;
import com.crmbank.erp.hsip.service.HsipService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/hsip")
@RequiredArgsConstructor
public class HsipController {

    private final HsipMapper hsipMapper;
    private final HsipService hsipService;
    private final SqlSession sqlSession;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/HSIP_120U_SAVE")
    public ResponseEntity<?> saveCustomsStock(@RequestBody Hsip120uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            String userId = user.getUserid();
            String cmpycd = user.getCmpycd();
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsipService.saveCustomsStock(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsip] Customs Save Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/HSIP_100U_SAVE")
    public ResponseEntity<?> saveImportOrder(@RequestBody Hsip100uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();

        try {
            String userId = user.getUserid();
            String cmpycd = user.getCmpycd();

            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsipService.saveImportOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsip] Save Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{procedure}")
    public ResponseEntity<?> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        if (session.getAttribute("user_session") == null) {
            return ResponseEntity.status(401).build();
        }

        String proc = procedure.toUpperCase();
        try {
            injectSession(params, session);
            fillMissingParameters(proc, params);

            String actkind = String.valueOf(params.getOrDefault("actkind", "")).toUpperCase();
            if (proc.length() >= 9 && proc.charAt(8) == 'U' && (actkind.startsWith("A") || actkind.startsWith("U"))) {
                String validationMsg = validateParameters(HsipMapper.class, proc, params);
                if (validationMsg != null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "status", "VALIDATION_ERROR",
                            "message", "🛠 [PROGRAM VALID ALARM]\n" + validationMsg
                    ));
                }
            }

            log.info("📋 [hsip] 실행 요청: {}", proc);

            List<Map<String, Object>> result;
            if (proc.endsWith("U_STR") && (actkind.startsWith("A") || actkind.startsWith("U"))) {
                String positionalSql = buildPositionalSql(proc, params);
                log.info("📋 [ASP 스타일 실행] SQL: {}", positionalSql);

                result = jdbcTemplate.query(positionalSql, (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    List<Object> values = new ArrayList<>();
                    int colCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= colCount; i++) {
                        Object val = rs.getObject(i);
                        String colName = rs.getMetaData().getColumnLabel(i); 
                        if (colName == null || colName.isEmpty()) colName = "col_" + (i-1);
                        row.put(colName.toLowerCase(), val == null ? "" : val);
                        values.add(val == null ? "" : val);
                    }
                    row.put("returnKeyValue", values); 
                    return row;
                });
                log.info("🎯 [무결성 직접 수신 성공] 데이터: {}", result);
            } else {
                switch (proc) {
                    case "HSIP_100U_STR": result = hsipMapper.HSIP_100U_STR(params); break;
                    case "HSIP_101U_STR": result = hsipMapper.HSIP_101U_STR(params); break;
                    case "HSIP_110U_STR": result = hsipMapper.HSIP_110U_STR(params); break;
                    case "HSIP_111U_STR": result = hsipMapper.HSIP_111U_STR(params); break;
                    case "HSIP_112U_STR": result = hsipMapper.HSIP_112U_STR(params); break;
                    case "HSIP_120U_STR": result = hsipMapper.HSIP_120U_STR(params); break;
                    case "HSIP_121U_STR": result = hsipMapper.HSIP_121U_STR(params); break;
                    case "HSIP_122U_STR": result = hsipMapper.HSIP_122U_STR(params); break;
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
                        return ResponseEntity.notFound().build();
                }
            }

            if (result == null || result.isEmpty()) {
                result = List.of(Map.of("res", "OK"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [hsip] executeProcedure Error ({}): {}", proc, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (params.get("cmpycd") == null || params.get("cmpycd").toString().trim().isEmpty()) {
                params.put("cmpycd", user.getCmpycd());
            }
            if (params.get("userid") == null || params.get("userid").toString().trim().isEmpty()) {
                params.put("userid", user.getUserid());
            }
            params.put("updemp", user.getUserid());
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HsipMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);

            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    String cleanProp = prop.trim();
                    // 🚀 기존: 무조건 "" 채움 -> 수정: 파라미터가 없거나 null인 경우만 null로 유지 (MyBatis가 처리하도록)
                    if (!params.containsKey(cleanProp)) {
                        params.put(cleanProp, null);
                    }
                    if (!cleanProp.equals(prop)) params.put(prop, params.get(cleanProp));
                }
            }
        } catch (Exception e) { log.warn("🛠 누락 파라미터 보정 중 알림 ({}): {}", proc, e.getMessage()); }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HsipMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return "EXEC " + proc;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);
            List<String> values = new ArrayList<>();
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                Object val = params.get(pm.getProperty().trim());
                // 🚀 빈 문자열("")이나 null이 들어오면 SQL 상에서 NULL로 처리하여 numeric 변환 오류 방지
                if (val == null || (val instanceof String && ((String) val).trim().isEmpty())) {
                    values.add("NULL");
                } else {
                    values.add("'" + val.toString().replace("'", "''").trim() + "'");
                }
            }
            return String.format("EXEC %s %s", proc, String.join(", ", values));
        } catch (Exception e) { return "EXEC " + proc; }
    }

    private String validateParameters(Class<?> mapperClass, String proc, Map<String, Object> vueParams) {
        try {
            String statementId = mapperClass.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return null;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(vueParams);
            List<ParameterMapping> xmlMappings = boundSql.getParameterMappings();
            Set<String> xmlKeys = new LinkedHashSet<>();
            for (ParameterMapping pm : xmlMappings) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) xmlKeys.add(prop);
            }
            if (vueParams.keySet().size() < xmlKeys.size()) {
                return String.format("📍 [PARAM SHORTAGE] XML:%d > VUE:%d\n📋 [REQUIRED]: %s", xmlKeys.size(), vueParams.keySet().size(), xmlKeys);
            }
            return null;
        } catch (Exception e) { return "VALIDATION ERROR: " + e.getMessage(); }
    }
}
