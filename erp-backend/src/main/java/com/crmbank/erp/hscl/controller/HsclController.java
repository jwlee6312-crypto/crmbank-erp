package com.crmbank.erp.hscl.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hscl.mapper.HsclMapper;
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
@RequestMapping("/api/hscl")
@RequiredArgsConstructor
public class HsclController {

    private final HsclMapper hsclMapper;
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

        // 🚀 특별 처리: 복합 저장 로직 (프론트엔드 요구사항 대응)
        if (proc.equals("HSCL_110U_SAVE")) return saveHscl110(params, session);
        if (proc.equals("HSCL_115U_SAVE")) return saveHscl115(params, session);

        try {
            fillMissingParameters(proc, params);
            String actkind = String.valueOf(params.getOrDefault("actkind", "")).toUpperCase();
            
            List<Map<String, Object>> result;
            // 💡 ASP 스타일 실행 (U_STR로 끝나고 actkind가 A/U인 경우)
            if (proc.endsWith("U_STR") && (actkind.startsWith("A") || actkind.startsWith("U") || actkind.startsWith("D"))) {
                String positionalSql = buildPositionalSql(proc, params);
                log.info("📋 [ASP 스타일 실행] SQL: {}", positionalSql);

                result = jdbcTemplate.query(positionalSql, (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int colCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= colCount; i++) {
                        String colName = rs.getMetaData().getColumnLabel(i);
                        if (colName == null || colName.isEmpty()) colName = "col_" + (i - 1);
                        row.put(colName.toLowerCase(), rs.getObject(i) == null ? "" : rs.getObject(i));
                    }
                    return row;
                });
            } else {
                switch (proc) {
                    case "HSCL_100U_STR": result = hsclMapper.HSCL_100U_STR(params); break;
                    case "HSCL_110U_STR": result = hsclMapper.HSCL_110U_STR(params); break;
                    case "HSCL_115U_STR": result = hsclMapper.HSCL_115U_STR(params); break;
                    case "HSCL_200S_STR": result = hsclMapper.HSCL_200S_STR(params); break;
                    case "HSCL_210S_STR": result = hsclMapper.HSCL_210S_STR(params); break;
                    case "HSCL_220S_STR": result = hsclMapper.HSCL_220S_STR(params); break;
                    case "HSCL_270S_STR": result = hsclMapper.HSCL_270S_STR(params); break;
                    case "HSCL_290S_STR": result = hsclMapper.HSCL_290S_STR(params); break;
                    case "HSCL_310S_STR": result = hsclMapper.HSCL_310S_STR(params); break;
                    case "HSCL_520S_STR": result = hsclMapper.HSCL_520S_STR(params); break;
                    default: return ResponseEntity.notFound().build();
                }
            }

            if (result == null || result.isEmpty()) {
                result = List.of(Map.of("res", "OK", "result", "OK"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [hscl] Error ({}): {}", proc, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage(), "message", e.getMessage()));
        }
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (params.get("cmpycd") == null || params.get("cmpycd").toString().isEmpty()) params.put("cmpycd", user.getCmpycd());
            params.put("updemp", user.getUserid());
            params.put("userid", user.getUserid());
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HsclMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    if (!params.containsKey(prop) || params.get(prop) == null) params.put(prop, "");
                }
            }
        } catch (Exception e) {}
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HsclMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return "EXEC " + proc;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);
            List<String> values = new ArrayList<>();
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                Object val = params.get(pm.getProperty().trim());
                values.add(val == null ? "''" : "'" + val.toString().replace("'", "''").trim() + "'");
            }
            return String.format("EXEC %s %s", proc, String.join(", ", values));
        } catch (Exception e) { return "EXEC " + proc; }
    }

    // 🚀 타계정전표발행 (HSCL110U) 통합 저장 로직
    private ResponseEntity<?> saveHscl110(Map<String, Object> params, HttpSession session) {
        try {
            // actkind: 'A' (발행), 'D' (삭제)
            String sql = buildPositionalSql("HSCL_110U_STR", params);
            List<Map<String, Object>> res = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("res", rs.getString(1));
                row.put("msg", rs.getString(2));
                return row;
            });
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    // 🚀 외부 타계정전표발행 (HSCL115U) 일괄 저장 로직
    private ResponseEntity<?> saveHscl115(Map<String, Object> params, HttpSession session) {
        try {
            List<Map<String, Object>> list = (List<Map<String, Object>>) params.get("list");
            String actkind = (String) params.get("actkind");
            
            for (Map<String, Object> item : list) {
                item.put("actkind", actkind);
                injectSession(item, session);
                String sql = buildPositionalSql("HSCL_115U_STR", item);
                jdbcTemplate.execute(sql);
            }
            return ResponseEntity.ok(List.of(Map.of("res", "OK", "result", "OK")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
}
