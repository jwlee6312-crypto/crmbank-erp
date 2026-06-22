package com.crmbank.erp.haba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.haba.mapper.HabaMapper;
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
@RequestMapping("/api/haba")
@RequiredArgsConstructor
public class HabaController {

    private final HabaMapper habaMapper;
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

        String proc = procedure.toUpperCase();
        try {
            injectSession(params, session);
            fillMissingParameters(proc, params);

            String actkind = String.valueOf(params.getOrDefault("actkind", "")).toUpperCase();
            if (proc.length() >= 9 && proc.charAt(8) == 'U' && (actkind.startsWith("A") || actkind.startsWith("U"))) {
                String validationMsg = validateParameters(HabaMapper.class, proc, params);
                if (validationMsg != null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "VALIDATION_ERROR",
                        "message", "🛠 [PROGRAM VALID ALARM]\n" + validationMsg
                    ));
                }
            }

            log.info("📋 [haba] 실행 요청: {}", proc);

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
                    case "HABA_010U_STR": result = habaMapper.HABA_010U_STR(params); break;
                    case "HABA_020U_STR": result = habaMapper.HABA_020U_STR(params); break;
                    case "HABA_021U_STR": result = habaMapper.HABA_021U_STR(params); break;
                    case "HABA_022U_STR": result = habaMapper.HABA_022U_STR(params); break;
                    case "HABA_030U_STR": result = habaMapper.HABA_030U_STR(params); break;
                    case "HABA_040U_STR": result = habaMapper.HABA_040U_STR(params); break;
                    case "HABA_050U_STR": result = habaMapper.HABA_050U_STR(params); break;
                    case "HABA_060U_STR": result = habaMapper.HABA_060U_STR(params); break;
                    case "HABA_070U_STR": result = habaMapper.HABA_070U_STR(params); break;
                    case "HABA_071U_STR": result = habaMapper.HABA_071U_STR(params); break;
                    case "HABA_080U_STR": result = habaMapper.HABA_080U_STR(params); break;
                    case "HABA_090U_STR": result = habaMapper.HABA_090U_STR(params); break;
                    case "HABA_100U_STR": result = habaMapper.HABA_100U_STR(params); break;
                    case "HABA_110U_STR": result = habaMapper.HABA_110U_STR(params); break;
                    case "HABA_120U_STR": result = habaMapper.HABA_120U_STR(params); break;
                    case "HABA_130U_STR": result = habaMapper.HABA_130U_STR(params); break;
                    case "HABA_140U_STR": result = habaMapper.HABA_140U_STR(params); break;
                    case "HABA_150U_STR": result = habaMapper.HABA_150U_STR(params); break;
                    case "HABA_160U_STR": result = habaMapper.HABA_160U_STR(params); break;
                    case "HABA_170U_STR": result = habaMapper.HABA_170U_STR(params); break;
                    case "HABA_180U_STR": result = habaMapper.HABA_180U_STR(params); break;
                    case "HABA_190S_STR": result = habaMapper.HABA_190S_STR(params); break;
                    case "HABA_210U_STR": result = habaMapper.HABA_210U_STR(params); break;
                    case "HABA_220U_STR": result = habaMapper.HABA_220U_STR(params); break;
                    case "HABA_230U_STR": result = habaMapper.HABA_230U_STR(params); break;
                    case "HABA_240U_STR": result = habaMapper.HABA_240U_STR(params); break;
                    case "HABA_250U_STR": result = habaMapper.HABA_250U_STR(params); break;
                    case "HABA_260U_STR": result = habaMapper.HABA_260U_STR(params); break;
                    case "HABA_510U_STR": result = habaMapper.HABA_510U_STR(params); break;
                    case "HABA_900U_STR": result = habaMapper.HABA_900U_STR(params); break;
                    case "HABA_910U_STR": result = habaMapper.HABA_910U_STR(params); break;
                    case "HABA_920U_STR": result = habaMapper.HABA_920U_STR(params); break;
                    case "HABA_935U_STR": result = habaMapper.HABA_935U_STR(params); break;
                    default:
                        return ResponseEntity.notFound().build();
                }
            }

            if (result == null || result.isEmpty()) {
                result = List.of(Map.of("res", "OK"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [haba] executeProcedure Error ({}): {}", proc, e.getMessage());
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
            String statementId = HabaMapper.class.getName() + "." + proc;
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
            String statementId = HabaMapper.class.getName() + "." + proc;
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
