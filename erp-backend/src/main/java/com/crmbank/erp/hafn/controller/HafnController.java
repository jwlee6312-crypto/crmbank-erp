package com.crmbank.erp.hafn.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hafn.mapper.HafnMapper;
import com.crmbank.erp.hasl.mapper.HaslMapper;
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
@RequestMapping("/api/hafn")
@RequiredArgsConstructor
public class HafnController {

    private final HafnMapper hafnMapper;
    private final HaslMapper haslMapper;
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
        
        // 🚀 복합 저장 로직 (ApiResponse 반환)
        if (proc.equals("HAFN_610U_SAVE") || proc.equals("HAFN_620U_SAVE") || proc.equals("HAFN_630U_SAVE")) {
            return saveSlip110(params, session);
        }

        try {
            fillMissingParameters(proc, params);

            log.info("🚀 [hafn] 실행 요청: {}", proc);
            
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
                    case "HAFN_010S_STR": result = hafnMapper.HAFN_010S_STR(params); break;
                    case "HAFN_110S_STR": result = hafnMapper.HAFN_110S_STR(params); break;
                    case "HAFN_120S_STR": result = hafnMapper.HAFN_120S_STR(params); break;
                    case "HAFN_210S_STR": result = hafnMapper.HAFN_210S_STR(params); break;
                    case "HAFN_310S_STR": result = hafnMapper.HAFN_310S_STR(params); break;
                    case "HAFN_410S_STR": result = hafnMapper.HAFN_410S_STR(params); break;
                    case "HAFN_420S_STR": result = hafnMapper.HAFN_420S_STR(params); break;
                    case "HAFN_430S_STR": result = hafnMapper.HAFN_430S_STR(params); break;
                    case "HAFN_510S_STR": result = hafnMapper.HAFN_510S_STR(params); break;
                    case "HAFN_520S_STR": result = hafnMapper.HAFN_520S_STR(params); break;
                    case "HAFN_610U_STR": result = hafnMapper.HAFN_610U_STR(params); break;
                    case "HAFN_620U_STR": result = hafnMapper.HAFN_620U_STR(params); break;
                    case "HAFN_630U_STR": result = hafnMapper.HAFN_630U_STR(params); break;
                    case "HAFN_670S_STR": result = hafnMapper.HAFN_670S_STR(params); break;
                    case "HAFN_680S_STR": result = hafnMapper.HAFN_680S_STR(params); break;
                    case "HAFN_690S_STR": result = hafnMapper.HAFN_690S_STR(params); break;
                    default:
                        return ResponseEntity.notFound().build();
                }
            }

            if (result == null || result.isEmpty()) {
                result = List.of(Map.of("res", "OK"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [hafn] executeProcedure Error ({}): {}", proc, e.getMessage());
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
            String statementId = HafnMapper.class.getName() + "." + proc;
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
            String statementId = HafnMapper.class.getName() + "." + proc;
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

    @Transactional
    public ResponseEntity<ApiResponse<?>> saveSlip110(Map<String, Object> payload, HttpSession session) {
        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("master");
            List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("details");
            String actkind = (String) payload.get("actkind");

            injectSession(master, session);
            master.put("actkind", actkind);

            String sql = buildPositionalSql("HASL_110U_STR", master);
            List<Map<String, Object>> masterresult = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.put(rs.getMetaData().getColumnLabel(i).toLowerCase(), rs.getObject(i));
                }
                return row;
            });

            if (masterresult.isEmpty()) throw new RuntimeException("마스터 저장 실패");
            
            List<Object> mstValues = new ArrayList<>(masterresult.get(0).values());
            String status = String.valueOf(mstValues.get(0)).trim();
            if ("000000".equals(status)) throw new RuntimeException(String.valueOf(mstValues.get(1)));

            String slipno = String.valueOf(mstValues.get(0));
            String slipymd = String.valueOf(master.get("slipymd"));

            if (details != null) {
                for (Map<String, Object> detail : details) {
                    injectSession(detail, session);
                    detail.put("slipymd", slipymd);
                    detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", actkind);

                    List<Map<String, Object>> detres = haslMapper.HASL_111U_STR(detail);
                    if (!detres.isEmpty()) {
                        List<Object> dtlValues = new ArrayList<>(detres.get(0).values());
                        if ("Y".equalsIgnoreCase(String.valueOf(dtlValues.get(0)))) {
                            throw new RuntimeException(String.valueOf(dtlValues.get(1)));
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("slipno", slipno);
            return ResponseEntity.ok(ApiResponse.success(response, "성공적으로 저장되었습니다."));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }
}
