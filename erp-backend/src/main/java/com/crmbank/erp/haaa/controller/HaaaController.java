package com.crmbank.erp.haaa.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.haaa.mapper.HaaaMapper;
import com.crmbank.erp.comm.service.CommService;

import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/haaa")
@RequiredArgsConstructor
public class HaaaController {

    private final HaaaMapper haaaMapper;
    private final CommService commService;
    private final SqlSession sqlSession;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data, HttpServletRequest request) {
        try {
            UserSession user = commService.login(data.get("cmpycd"), data.get("userid"), data.get("PASSWD"), request.getRemoteAddr());
            request.getSession(true).setAttribute("user_session", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(401).build();
    }

    @GetMapping("/top-menus")
    public ResponseEntity<List<Map<String, Object>>> getTopMenus() {
        return ResponseEntity.ok(commService.getTopMenus());
    }

    @GetMapping("/left-menus")
    public ResponseEntity<List<Map<String, Object>>> getLeftMenus(@RequestParam("upmucd") String upmucd) {
        return ResponseEntity.ok(commService.getLeftMenus(upmucd));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{procedure}")
    public ResponseEntity<?> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session,
            HttpServletRequest request) {
        
        String proc = procedure.toUpperCase();

        // 🚀 특수 엔드포인트 수동 매핑 (Java 호출 인지 및 통제 강화)
        switch (proc) {
            case "LOGIN": 
                Map<String, String> loginData = new HashMap<>();
                params.forEach((k, v) -> loginData.put(k, String.valueOf(v)));
                return login(loginData, request);
            case "LOGOUT": return logout(session);
            case "SESSION": return getSession(session);
            case "TOP-MENUS": return getTopMenus();
            case "LEFT-MENUS": return getLeftMenus(String.valueOf(params.getOrDefault("upmucd", "")));
        }

        if (session.getAttribute("user_session") == null) {
            return ResponseEntity.status(401).build();
        }

        injectSession(params, session);
        String actkind = String.valueOf(params.getOrDefault("actkind", "")).toUpperCase();

        try {
            fillMissingParameters(proc, params);
            log.info("📋 [haaa] 실행 요청: {}", proc);
            
            List<Map<String, Object>> rawResult;
            if (proc.endsWith("U_STR") && (actkind.startsWith("A") || actkind.startsWith("U"))) {
                rawResult = executeJdbcQuery(proc, params);
            } else {
                rawResult = switch (proc) {
                    case "HAAA_010U_STR" -> haaaMapper.HAAA_010U_STR(params);
                    case "HAAA_800U_STR" -> haaaMapper.HAAA_800U_STR(params);
                    case "HAAA_810U_STR" -> haaaMapper.HAAA_810U_STR(params);
                    default -> null;
                };
            }

            if (rawResult == null) return ResponseEntity.notFound().build();

            List<Map<String, Object>> finalResult = rawResult.isEmpty() ? 
                    List.of(Map.of("res", "OK")) : rawResult;

            return ResponseEntity.ok(convertToLowerCaseKeys(finalResult));

        } catch (Exception e) {
            log.error("❌ [haaa] executeProcedure Error ({}): {}", proc, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private List<Map<String, Object>> executeJdbcQuery(String proc, Map<String, Object> params) {
        String positionalSql = buildPositionalSql(proc, params);
        log.info("📋 [ASP 스타일 실행] SQL: {}", positionalSql);

        return jdbcTemplate.query(positionalSql, (rs, rowNum) -> {
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
    }

    private List<Map<String, Object>> convertToLowerCaseKeys(List<Map<String, Object>> list) {
        List<Map<String, Object>> newList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Map<String, Object> newMap = new LinkedHashMap<>();
            map.forEach((k, v) -> newMap.put(k.toLowerCase(), v));
            newList.add(newMap);
        }
        return newList;
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
            params.putIfAbsent("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HaaaMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    params.putIfAbsent(prop.trim(), "");
                }
            }
        } catch (Exception e) { log.warn("🛠 누락 파라미터 보정 중 알림 ({}): {}", proc, e.getMessage()); }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HaaaMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return "EXEC " + proc;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);

            List<String> values = new ArrayList<>();
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                Object val = params.get(pm.getProperty().trim());
                String valStr = (val == null) ? "''" : "'" + val.toString().replace("'", "''").trim() + "'";
                values.add(valStr);
            }
            return String.format("EXEC %s %s", proc, String.join(", ", values));
        } catch (Exception e) { return "EXEC " + proc; }
    }
}
