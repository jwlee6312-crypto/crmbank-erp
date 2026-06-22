package com.crmbank.erp.hafa.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hafa.mapper.HafaMapper;
import com.crmbank.erp.hafa.service.HafaService;
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
@RequestMapping("/api/hafa")
@RequiredArgsConstructor
public class HafaController {

    private final HafaMapper hafaMapper;
    private final HafaService hafaService;
    private final SqlSession sqlSession;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{procedure}")
    public ResponseEntity<?> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        if (session.getAttribute("user_session") == null) {
            return ResponseEntity.status(401).build();
        }

        injectSession(params, session);
        String proc = procedure.toUpperCase();
        String actkind = String.valueOf(params.getOrDefault("actkind", "")).toUpperCase();

        try {
            fillMissingParameters(proc, params);
            log.info("📋 [hafa] 실행 요청: {}", proc);

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
                    case "HAFA_010U_STR": result = hafaMapper.HAFA_010U_STR(params); break;
                    case "HAFA_020S_STR": result = hafaMapper.HAFA_020S_STR(params); break;
                    case "HAFA_040S_STR": result = hafaMapper.HAFA_040S_STR(params); break;
                    case "HAFA_050U_STR": result = hafaMapper.HAFA_050U_STR(params); break;
                    case "HAFA_090U_STR": result = hafaMapper.HAFA_090U_STR(params); break;
                    case "HAFA_120S_STR": result = hafaMapper.HAFA_120S_STR(params); break;
                    case "HAFA_130S_STR": result = hafaMapper.HAFA_130S_STR(params); break;
                    case "HA00_150S_STR": result = hafaMapper.HA00_150S_STR(params); break;
                    case "HAFA_140S_STR": result = hafaMapper.HAFA_140S_STR(params); break;
                    case "HAFA_150U_STR": result = hafaMapper.HAFA_150U_STR(params); break;
                    case "HAFA_900U_STR": result = hafaMapper.HAFA_900U_STR(params); break;
                    default:
                        return ResponseEntity.notFound().build();
                }
            }

            if (result == null || result.isEmpty()) {
                result = List.of(Map.of("res", "OK"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [hafa] executeProcedure Error ({}): {}", proc, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/HAFA_150U_SAVE")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveHafa150U(@RequestBody Map<String, Object> params, HttpSession session) {
        if (session.getAttribute("user_session") == null) {
            return ResponseEntity.status(401).build();
        }
        injectSession(params, session);
        try {
            Map<String, Object> result = hafaService.saveDepreciationSlip(params);
            return ResponseEntity.ok(ApiResponse.success(result, "성공"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
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
            params.putIfAbsent("usernm", user.getUsernm());
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HafaMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);

            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    String cleanProp = prop.trim();
                    if (!params.containsKey(cleanProp) || params.get(cleanProp) == null || params.get(cleanProp).toString().trim().isEmpty()) {
                        params.put(cleanProp, "");
                    }
                    if (!cleanProp.equals(prop)) params.put(prop, params.get(cleanProp));
                }
            }
        } catch (Exception e) { log.warn("🛠 누락 파라미터 보정 중 알림 ({}): {}", proc, e.getMessage()); }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HafaMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return "EXEC " + proc;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);
            List<String> values = new ArrayList<>();
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                Object val = params.get(pm.getProperty().trim());
                if (val == null) values.add("''");
                else values.add("'" + val.toString().replace("'", "''").trim() + "'");
            }
            return String.format("EXEC %s %s", proc, String.join(", ", values));
        } catch (Exception e) { return "EXEC " + proc; }
    }

    private String validateParameters(Class<?> mapperClass, String proc, Map<String, Object> vueParams) {
        try {
            String statementId = mapperClass.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return null;
            return null;
        } catch (Exception e) { return "VALIDATION ERROR: " + e.getMessage(); }
    }

    private String buildSsmsLog(String proc, Map<String, Object> params) {
        String values = params.entrySet().stream()
                .map(entry -> "@" + entry.getKey() + " = " + (entry.getValue() == null ? "''" : "'" + entry.getValue().toString().trim() + "'"))
                .collect(Collectors.joining(", "));
        return String.format("EXEC %s %s", proc, values);
    }
}
