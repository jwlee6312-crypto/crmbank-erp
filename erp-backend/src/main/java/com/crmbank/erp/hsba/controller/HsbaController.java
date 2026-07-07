package com.crmbank.erp.hsba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsba.mapper.HsbaMapper;
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
@RequestMapping("/api/hsba")
@RequiredArgsConstructor
public class HsbaController {

    private final HsbaMapper hsbaMapper;
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

        Map<String, Object> p = new HashMap<>();
        params.forEach((k, v) -> p.put(k.toLowerCase(), v));

        injectSession(p, session);
        String proc = procedure.toUpperCase();

        if ("HSBA_750U_STR".equals(proc)) {
            if (p.get("sch_text") != null) p.put("costnm", p.get("sch_text"));
            if (p.get("sch_useyn") != null) p.put("useyn", p.get("sch_useyn"));
            p.putIfAbsent("actkind", "S0");
        }
        if ("HSBA_090U_STR".equals(proc) || "HSBA_070U_STR".equals(proc)) {
            p.putIfAbsent("actkind", "S0");
            if (p.get("custnm") == null && p.get("sch_text") != null) p.put("custnm", p.get("sch_text"));
        }

        fillMissingParameters(proc, p);
        String actkind = String.valueOf(p.getOrDefault("actkind", "")).toUpperCase();
        log.info("📋 [hsba] 실행 요청: {}", proc);

        List<Map<String, Object>> result;
        if (proc.endsWith("U_STR") && (actkind.startsWith("A") || actkind.startsWith("U"))) {
            String positionalSql = buildPositionalSql(proc, p);
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
                    row.put("returnkeyvalue", values);
                    return row;
                });
            } catch (Exception e) {
                log.warn("⚠️ 결과 집합 없음 또는 실행 오류 ({}): {}", proc, e.getMessage());
                result = new ArrayList<>(); // 결과가 없어도 성공으로 간주 (res: OK는 하단에서 처리)
            }
        } else {
            switch (proc) {
                case "HSBA_010U_STR": result = hsbaMapper.HSBA_010U_STR(p); break;
                case "HSBA_020U_STR": result = hsbaMapper.HSBA_020U_STR(p); break;
                case "HSBA_030U_STR": result = hsbaMapper.HSBA_030U_STR(p); break;
                case "HSBA_040U_STR": result = hsbaMapper.HSBA_040U_STR(p); break;
                case "HSBA_050U_STR": result = hsbaMapper.HSBA_050U_STR(p); break;
                case "HSBA_060U_STR": result = hsbaMapper.HSBA_060U_STR(p); break;
                case "HSBA_070U_STR": result = hsbaMapper.HSBA_070U_STR(p); break;
                case "HSBA_280U_STR": result = hsbaMapper.HSBA_280U_STR(p); break;
                case "HSBA_065U_STR": result = hsbaMapper.HSBA_065U_STR(p); break;
                case "HSBA_090U_STR": result = hsbaMapper.HSBA_090U_STR(p); break;
                case "HSBA_130U_STR": result = hsbaMapper.HSBA_130U_STR(p); break;
                case "HSBA_140U_STR": result = hsbaMapper.HSBA_140U_STR(p); break;
                case "HSBA_170U_STR": result = hsbaMapper.HSBA_170U_STR(p); break;
                case "HSBA_190U_STR": result = hsbaMapper.HSBA_190U_STR(p); break;
                case "HSBA_210U_STR": result = hsbaMapper.HSBA_210U_STR(p); break;
                case "HSBA_211U_STR": result = hsbaMapper.HSBA_211U_STR(p); break;
                case "HSBA_700U_STR": result = hsbaMapper.HSBA_700U_STR(p); break;
                case "HSBA_710U_STR": result = hsbaMapper.HSBA_710U_STR(p); break;
                case "HSBA_701U_STR": result = hsbaMapper.HSBA_701U_STR(p); break;
                case "HSBA_711U_STR": result = hsbaMapper.HSBA_711U_STR(p); break;
                case "HSBA_720U_STR": result = hsbaMapper.HSBA_720U_STR(p); break;
                case "HSBA_721U_STR": result = hsbaMapper.HSBA_721U_STR(p); break;
                case "HSBA_730U_STR": result = hsbaMapper.HSBA_730U_STR(p); break;
                case "HSBA_740U_STR": result = hsbaMapper.HSBA_740U_STR(p); break;
                case "HSBA_750U_STR": result = hsbaMapper.HSBA_750U_STR(p); break;
                case "HSBA_800U_STR": result = hsbaMapper.HSBA_800U_STR(p); break;
                case "HSBA_810U_STR": result = hsbaMapper.HSBA_810U_STR(p); break;
                case "HSBA_820U_STR": result = hsbaMapper.HSBA_820U_STR(p); break;
                case "HSBA_830U_STR": result = hsbaMapper.HSBA_830U_STR(p); break;
                case "HSBA_900U_STR": result = hsbaMapper.HSBA_900U_STR(p); break;
                default:
                    return ResponseEntity.notFound().build();
            }
        }

        if (result == null || result.isEmpty()) {
            result = List.of(Map.of("res", "OK"));
        }
        return ResponseEntity.ok(result);
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
            String statementId = HsbaMapper.class.getName() + "." + proc;
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
            String statementId = HsbaMapper.class.getName() + "." + proc;
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
