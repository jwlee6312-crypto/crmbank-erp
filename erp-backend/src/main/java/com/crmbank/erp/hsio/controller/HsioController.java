package com.crmbank.erp.hsio.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsio.dto.*;
import com.crmbank.erp.hsio.mapper.HsioMapper;
import com.crmbank.erp.hsio.service.HsioService;
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

import java.lang.reflect.Method;
import java.util.*;

/**
 * [HSIO] 영업수불 통합 컨트롤러 (사용자 정의 최종 표준형 - 전수 복구 완료)
 */
@Slf4j
@RestController
@RequestMapping("/api/hsio")
@RequiredArgsConstructor
public class HsioController {

    private final HsioMapper hsioMapper;
    private final HsioService hsioService;
    private final SqlSession sqlSession;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/HSIO_010U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveRequest(@RequestBody Hsio010uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(user.getCmpycd());
                request.getMst().setUpdemp(user.getUserid());
            }
            Map<String, Object> result = hsioService.saveRequest(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_050U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOrder(@RequestBody Hsio050uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(user.getCmpycd());
                request.getMst().setUpdemp(user.getUserid());
            }
            Map<String, Object> result = hsioService.saveOrder(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{procedure}")
    public ResponseEntity<?> executeProcedure(@PathVariable String procedure, @RequestBody Map<String, Object> params, HttpSession session) {
        String proc = procedure.toUpperCase();
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();

        try {
            // 💡 표준 파라미터 강제 세팅 (updemp 포함)
            params.put("cmpycd", user.getCmpycd());
            params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());

            fillMissingParameters(proc, params);
            String actkind = String.valueOf(params.getOrDefault("actkind", "")).toUpperCase().trim();

            log.info("📋 [HSIO] 실행 요청: {}", proc);

            // 🚀 특수 로직: 외부전표전송 (약속된 기능 유지)
            if ("HSIO_990U_STR".equals(proc) && "U0".equals(actkind)) {
                hsioService.transferExternalSlip(params, user.getUserid());
                return ResponseEntity.ok(List.of(Map.of("res", "OK")));
            }

            List<Map<String, Object>> resultList;

            // 💡 표준: 쓰기 액션(A, U, D, DR) 및 집계(C)인 경우 무결성 수신을 위해 직접 실행 및 로깅
            if (proc.endsWith("U_STR") && (actkind.startsWith("A") || actkind.startsWith("U") || actkind.startsWith("D") || actkind.equals("DR") || actkind.equals("C"))) {
                resultList = executeDirectSql(proc, params);
            } else {
                // 💡 [전수 복구] HsioMapper의 모든 메소드를 Switch-Case로 명시적 호출
                switch (proc) {
                    case "HSIO_010U_STR": resultList = hsioMapper.HSIO_010U_STR(params); break;
                    case "HSIO_011U_STR": resultList = hsioMapper.HSIO_011U_STR(params); break;
                    case "HSIO_020U_STR": resultList = hsioMapper.HSIO_020U_STR(params); break;
                    case "HSIO_021U_STR": resultList = hsioMapper.HSIO_021U_STR(params); break;
                    case "HSIO_050U_STR": resultList = hsioMapper.HSIO_050U_STR(params); break;
                    case "HSIO_051U_STR": resultList = hsioMapper.HSIO_051U_STR(params); break;
                    case "HSIO_052U_STR": resultList = hsioMapper.HSIO_052U_STR(params); break;
                    case "HSIO_060U_STR": resultList = hsioMapper.HSIO_060U_STR(params); break;
                    case "HSIO_061U_STR": resultList = hsioMapper.HSIO_061U_STR(params); break;
                    case "HSIO_070U_STR": resultList = hsioMapper.HSIO_070U_STR(params); break;
                    case "HSIO_080S_STR": resultList = hsioMapper.HSIO_080S_STR(params); break;
                    case "HSIO_082S_STR": resultList = hsioMapper.HSIO_082S_STR(params); break;
                    case "HSIO_085S_STR": resultList = hsioMapper.HSIO_085S_STR(params); break;
                    case "HSIO_100U_STR": resultList = hsioMapper.HSIO_100U_STR(params); break;
                    case "HSIO_101U_STR": resultList = hsioMapper.HSIO_101U_STR(params); break;
                    case "HSIO_110U_STR": resultList = hsioMapper.HSIO_110U_STR(params); break;
                    case "HSIO_120U_STR": resultList = hsioMapper.HSIO_120U_STR(params); break;
                    case "HSIO_130U_STR": resultList = hsioMapper.HSIO_130U_STR(params); break;
                    case "HSIO_131U_STR": resultList = hsioMapper.HSIO_131U_STR(params); break;
                    case "HSIO_140U_STR": resultList = hsioMapper.HSIO_140U_STR(params); break;
                    case "HSIO_141U_STR": resultList = hsioMapper.HSIO_141U_STR(params); break;
                    case "HSIO_160U_STR": resultList = hsioMapper.HSIO_160U_STR(params); break;
                    case "HSIO_170U_STR": resultList = hsioMapper.HSIO_170U_STR(params); break;
                    case "HSIO_171U_STR": resultList = hsioMapper.HSIO_171U_STR(params); break;
                    case "HSIO_180U_STR": resultList = hsioMapper.HSIO_180U_STR(params); break;
                    case "HSIO_181U_STR": resultList = hsioMapper.HSIO_181U_STR(params); break;
                    case "HSIO_190U_STR": resultList = hsioMapper.HSIO_190U_STR(params); break;
                    case "HSIO_191U_STR": resultList = hsioMapper.HSIO_191U_STR(params); break;
                    case "HSIO_200S_STR": resultList = hsioMapper.HSIO_200S_STR(params); break;
                    case "HSIO_210S_STR": resultList = hsioMapper.HSIO_210S_STR(params); break;
                    case "HSIO_215S_STR": resultList = hsioMapper.HSIO_215S_STR(params); break;
                    case "HSIO_220S_STR": resultList = hsioMapper.HSIO_220S_STR(params); break;
                    case "HSIO_250U_STR": resultList = hsioMapper.HSIO_250U_STR(params); break;
                    case "HSIO_251U_STR": resultList = hsioMapper.HSIO_251U_STR(params); break;
                    case "HSIO_300U_STR": resultList = hsioMapper.HSIO_300U_STR(params); break;
                    case "HSIO_301U_STR": resultList = hsioMapper.HSIO_301U_STR(params); break;
                    case "HSIO_320U_STR": resultList = hsioMapper.HSIO_320U_STR(params); break;
                    case "HSIO_325U_STR": resultList = hsioMapper.HSIO_325U_STR(params); break;
                    case "HSIO_410S_STR": resultList = hsioMapper.HSIO_410S_STR(params); break;
                    case "HSIO_470S_STR": resultList = hsioMapper.HSIO_470S_STR(params); break;
                    case "HSIO_490U_STR": resultList = hsioMapper.HSIO_490U_STR(params); break;
                    case "HSIO_491U_STR": resultList = hsioMapper.HSIO_491U_STR(params); break;
                    case "HSIO_500U_STR": resultList = hsioMapper.HSIO_500U_STR(params); break;
                    case "HSIO_501U_STR": resultList = hsioMapper.HSIO_501U_STR(params); break;
                    case "HSIO_510U_STR": resultList = hsioMapper.HSIO_510U_STR(params); break;
                    case "HSIO_511U_STR": resultList = hsioMapper.HSIO_511U_STR(params); break;
                    case "HSIO_520U_STR": resultList = hsioMapper.HSIO_520U_STR(params); break;
                    case "HSIO_521U_STR": resultList = hsioMapper.HSIO_521U_STR(params); break;
                    case "HSIO_530U_STR": resultList = hsioMapper.HSIO_530U_STR(params); break;
                    case "HSIO_531U_STR": resultList = hsioMapper.HSIO_531U_STR(params); break;
                    case "HSIO_540U_STR": resultList = hsioMapper.HSIO_540U_STR(params); break;
                    case "HSIO_541U_STR": resultList = hsioMapper.HSIO_541U_STR(params); break;
                    case "HSIO_550U_STR": resultList = hsioMapper.HSIO_550U_STR(params); break;
                    case "HSIO_551U_STR": resultList = hsioMapper.HSIO_551U_STR(params); break;
                    case "HSIO_560U_STR": resultList = hsioMapper.HSIO_560U_STR(params); break;
                    case "HSIO_570U_STR": resultList = hsioMapper.HSIO_570U_STR(params); break;
                    case "HSIO_571U_STR": resultList = hsioMapper.HSIO_571U_STR(params); break;
                    case "HSIO_580U_STR": resultList = hsioMapper.HSIO_580U_STR(params); break;
                    case "HSIO_581U_STR": resultList = hsioMapper.HSIO_581U_STR(params); break;
                    case "HSIO_590U_STR": resultList = hsioMapper.HSIO_590U_STR(params); break;
                    case "HSIO_600U_STR": resultList = hsioMapper.HSIO_600U_STR(params); break;
                    case "HSIO_600S_STR": resultList = hsioMapper.HSIO_600S_STR(params); break;
                    case "HSIO_610S_STR": resultList = hsioMapper.HSIO_610S_STR(params); break;
                    case "HSIO_620S_STR": resultList = hsioMapper.HSIO_620S_STR(params); break;
                    case "HSIO_640S_STR": resultList = hsioMapper.HSIO_640S_STR(params); break;
                    case "HSIO_650S_STR": resultList = hsioMapper.HSIO_650S_STR(params); break;
                    case "HSIO_660S_STR": resultList = hsioMapper.HSIO_660S_STR(params); break;
                    case "HSIO_680S_STR": resultList = hsioMapper.HSIO_680S_STR(params); break;
                    case "HSIO_690S_STR": resultList = hsioMapper.HSIO_690S_STR(params); break;
                    case "HSIO_720U_STR": resultList = hsioMapper.HSIO_720U_STR(params); break;
                    case "HSIO_721U_STR": resultList = hsioMapper.HSIO_721U_STR(params); break;
                    case "HSIO_730U_STR": resultList = hsioMapper.HSIO_730U_STR(params); break;
                    case "HSIO_731U_STR": resultList = hsioMapper.HSIO_731U_STR(params); break;
                    default:
                        resultList = invokeMapper(proc, params);
                        if (resultList == null) resultList = executeDirectSql(proc, params);
                        break;
                }
            }

            if (resultList == null || resultList.isEmpty()) {
                resultList = List.of(Map.of("res", "OK"));
            }
            return ResponseEntity.ok(convertToLowerCaseKeys(resultList));

        } catch (Exception e) {
            log.error("❌ [HSIO] {} Error: {}", proc, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private List<Map<String, Object>> invokeMapper(String proc, Map<String, Object> params) {
        try {
            Method method = HsioMapper.class.getMethod(proc, Map.class);
            return (List<Map<String, Object>>) method.invoke(hsioMapper, params);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> executeDirectSql(String proc, Map<String, Object> params) {
        String sql = buildPositionalSql(proc, params);
        log.info("==>  Direct Executing: {}", sql);
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                int colCount = rs.getMetaData().getColumnCount();
                for (int k = 1; k <= colCount; k++) {
                    String label = rs.getMetaData().getColumnLabel(k).toLowerCase();
                    row.put(label, rs.getObject(k) == null ? "" : rs.getObject(k));
                }
                row.put("returnkeyvalue", new ArrayList<>()); // 💡 시스템 표준 명칭 (소문자)
                return row;
            });
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("No ResultSet") || e.getMessage().contains("did not return a result set"))) {
                return new ArrayList<>();
            }
            throw e;
        }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HsioMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return "EXEC " + proc;
            BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(statementId).getBoundSql(params);
            List<String> values = new ArrayList<>();
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty().trim();
                Object val = params.get(prop);
                // 💡 명칭 혼용 대응 (updemp, etc 등)
                if (val == null) {
                    if ("updemp".equalsIgnoreCase(prop)) val = params.get("userid");
                    else if ("userid".equalsIgnoreCase(prop)) val = params.get("updemp");
                    else if ("etc".equalsIgnoreCase(prop)) val = params.get("deptcd");
                    else if ("deptcd".equalsIgnoreCase(prop)) val = params.get("etc");
                }
                values.add(val == null ? "NULL" : "'" + val.toString().replace("'", "''").trim() + "'");
            }
            return String.format("EXEC %s %s", proc, String.join(", ", values));
        } catch (Exception e) { return "EXEC " + proc; }
    }

    private List<Map<String, Object>> convertToLowerCaseKeys(List<Map<String, Object>> list) {
        if (list == null) return new ArrayList<>();
        List<Map<String, Object>> newList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Map<String, Object> newMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                newMap.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            newList.add(newMap);
        }
        return newList;
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HsioMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    String cleanProp = prop.trim();
                    if (!params.containsKey(cleanProp)) params.put(cleanProp, null);
                    if (!cleanProp.equals(prop)) params.put(prop, params.get(cleanProp));
                }
            }
        } catch (Exception e) { }
    }
}
