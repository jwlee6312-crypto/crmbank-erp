package com.crmbank.erp.hpio.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hpio.mapper.HpioMapper;
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

@Slf4j
@RestController
@RequestMapping("/api/hpio")
@RequiredArgsConstructor
public class HpioController {

    private final HpioMapper hpioMapper;
    private final com.crmbank.erp.hpio.service.HpioService hpioService;
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

        if ("HPIO_250U_SAVE".equals(proc)) {
            return ResponseEntity.ok(hpioService.saveHpio250U(params));
        }

        String actkind = String.valueOf(params.getOrDefault("actkind", "")).toUpperCase();

        try {
            fillMissingParameters(proc, params);
            log.info("🚀 [hpio] 실행 요청: {}", proc);
            
            List<Map<String, Object>> result;
            if (proc.endsWith("U_STR") && (actkind.startsWith("A") || actkind.startsWith("U"))) {
                String positionalSql = buildPositionalSql(proc, params);
                log.info("📋 [ASP 스타일 실행] SQL: {}", positionalSql);

                try {
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
                } catch (org.springframework.jdbc.UncategorizedSQLException ex) {
                    if (ex.getMessage() != null && ex.getMessage().contains("결과 집합을 반환하지 않았습니다")) {
                        result = List.of(Map.of("res", "OK"));
                    } else {
                        throw ex;
                    }
                }
            } else {
                switch (proc) {
                    case "HPIO_200U_STR": result = hpioMapper.HPIO_200U_STR(params); break;
                    case "HPIO_210U_STR": result = hpioMapper.HPIO_210U_STR(params); break;
                    case "HPIO_230S_STR": result = hpioMapper.HPIO_230S_STR(params); break;
                    case "HPIO_250U_STR": result = hpioMapper.HPIO_250U_STR(params); break;
                    case "HPIO_251U_STR": result = hpioMapper.HPIO_251U_STR(params); break;
                    case "HPIO_250U_POP": result = hpioMapper.HPIO_250U_POP(params); break;
                    case "HPIO_290U_STR": result = hpioMapper.HPIO_290U_STR(params); break;
                    case "HPIO_291U_STR": result = hpioMapper.HPIO_291U_STR(params); break;
                    case "HPIO_300U_STR": result = hpioMapper.HPIO_300U_STR(params); break;
                    case "HPIO_301U_STR": result = hpioMapper.HPIO_301U_STR(params); break;
                    case "HPIO_340U_STR": result = hpioMapper.HPIO_340U_STR(params); break;
                    case "HPIO_341U_STR": result = hpioMapper.HPIO_341U_STR(params); break;
                    case "HPIO_350U_STR": result = hpioMapper.HPIO_350U_STR(params); break;
                    case "HPIO_351U_STR": result = hpioMapper.HPIO_351U_STR(params); break;
                    case "HPIO_360S_STR": result = hpioMapper.HPIO_360S_STR(params); break;
                    case "HPIO_370S_STR": result = hpioMapper.HPIO_370S_STR(params); break;
                    case "HPIO_380S_STR": result = hpioMapper.HPIO_380S_STR(params); break;
                    case "HPIO_390S_STR": result = hpioMapper.HPIO_390S_STR(params); break;
                    case "HPIO_400U_STR": result = hpioMapper.HPIO_400U_STR(params); break;
                    case "HPIO_410U_STR": result = hpioMapper.HPIO_410U_STR(params); break;
                    case "HPIO_420S_STR": result = hpioMapper.HPIO_420S_STR(params); break;
                    case "HPIO_430S_STR": result = hpioMapper.HPIO_430S_STR(params); break;
                    case "HPIO_500U_STR": result = hpioMapper.HPIO_500U_STR(params); break;
                    case "HPIO_501U_STR": result = hpioMapper.HPIO_501U_STR(params); break;
                    case "HPIO_510U_STR": result = hpioMapper.HPIO_510U_STR(params); break;
                    case "HPIO_511U_STR": result = hpioMapper.HPIO_511U_STR(params); break;
                    case "HPIO_520U_STR": result = hpioMapper.HPIO_520U_STR(params); break;
                    case "HPIO_521U_STR": result = hpioMapper.HPIO_521U_STR(params); break;
                    case "HPIO_640S_STR": result = hpioMapper.HPIO_640S_STR(params); break;
                    case "HPIO_650S_STR": result = hpioMapper.HPIO_650S_STR(params); break;
                    case "HPIO_660S_STR": result = hpioMapper.HPIO_660S_STR(params); break;
                    case "HPIO_710S_STR": result = hpioMapper.HPIO_710S_STR(params); break;
                    case "HPIO_720S_STR": result = hpioMapper.HPIO_720S_STR(params); break;
                    case "HPIO_850S_STR": result = hpioMapper.HPIO_850S_STR(params); break;
                    case "HPIO_870U_STR": result = hpioMapper.HPIO_870U_STR(params); break;
                    case "HPIO_251S_STR": result = hpioMapper.HPIO_251S_STR(params); break;
                    case "HPIO_252S_STR": result = hpioMapper.HPIO_252S_STR(params); break;
                    case "HPIO_253U_STR": result = hpioMapper.HPIO_253U_STR(params); break;
                    default: return ResponseEntity.notFound().build();
                }
            }

            if (result == null || result.isEmpty()) {
                result = (actkind.startsWith("S") || actkind.startsWith("L") || actkind.isEmpty()) ? new ArrayList<>() : List.of(Map.of("res", "OK"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [hpio] executeProcedure Error ({}): {}", proc, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (params.get("cmpycd") == null || params.get("cmpycd").toString().trim().isEmpty()) params.put("cmpycd", user.getCmpycd());
            if (params.get("userid") == null || params.get("userid").toString().trim().isEmpty()) params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = "com.crmbank.erp.hpio.mapper.HpioMapper." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    String cleanProp = prop.trim();
                    if (!params.containsKey(cleanProp) || params.get(cleanProp) == null || params.get(cleanProp).toString().trim().isEmpty()) {
                        // 🚀 사용자 약속: 빈 문자열 할당만 수행 (숫자 주입 제거)
                        params.put(cleanProp, "");
                    }
                    if (!cleanProp.equals(prop)) params.put(prop, params.get(cleanProp));
                }
            }
        } catch (Exception e) {}
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = "com.crmbank.erp.hpio.mapper.HpioMapper." + proc;
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
}
