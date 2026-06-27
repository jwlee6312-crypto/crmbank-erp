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

import java.util.*;
import java.util.stream.Collectors;

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
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveRequest(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveRequest Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_050U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOrder(@RequestBody Hsio050uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveOrder Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_052U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveGeneralOrder(@RequestBody Hsio052uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveGeneralOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveGeneralOrder Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_500U_SAVE")
    public ResponseEntity<ApiResponse<?>> savePurchase(@RequestBody Hsio500uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.savePurchase(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] savePurchase Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_190U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOtherIn(@RequestBody Hsio190uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveOtherIn(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveOtherIn Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_250U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOtherOut(@RequestBody Hsio250uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveOtherOut(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveOtherOut Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_300U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveDeposit(@RequestBody Hsio300uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveDeposit(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveDeposit Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_320U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveDepositSlip(@RequestBody Hsio320uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveDepositSlip(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 전표가 발행되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveDepositSlip Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_510U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveSettlement(@RequestBody Hsio510uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveSettlement(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 정산 처리되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveSettlement Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_590U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveBatchSettlement(@RequestBody Hsio590uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.saveBatchSettlement(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 일괄 정산 처리되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveBatchSettlement Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_580U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveWarehouseTransfer(@RequestBody Hsio580uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveWarehouseTransfer(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveWarehouseTransfer Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_720U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveStockAdjustment(@RequestBody Hsio720uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveStockAdjustment(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveStockAdjustment Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_730U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveInventoryAdjustment(@RequestBody Hsio730Request request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveInventoryAdjustment(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveInventoryAdjustment Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_570U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveStoreInout(@RequestBody Hsio570uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveStoreInout(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveStoreInout Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_490U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveSalesReturn(@RequestBody Hsio490uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveSalesReturn(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveSalesReturn Error: {}, Payload: {}", e.getMessage(), request);
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
                String validationMsg = validateParameters(HsioMapper.class, proc, params);
                if (validationMsg != null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "VALIDATION_ERROR",
                        "message", "🛠 [PROGRAM VALID ALARM]\n" + validationMsg
                    ));
                }
            }

            log.info("📋 [hsio] 실행 요청: {}", proc);
            
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
                        
                        // 🚀 [지시사항] 프로시저별 리턴 필드명(Alias) 강제 지정
                        if (colName == null || colName.isEmpty() || colName.toLowerCase().startsWith("col")) {
                            if (proc.equals("HSIO_010U_STR")) {
                                if (i == 1) colName = "reqym";
                                else if (i == 2) colName = "reqno";
                            }
                            if (proc.equals("HSIO_050U_STR")  || proc.equals("HSIO_052U_STR") ) {
                                if (i == 1) colName = "balym";
                                else if (i == 2) colName = "balno";
                            }
                            if (proc.equals("HSIO_300U_STR")  || proc.equals("HSIO_320U_STR") ) {
                                if (i == 1) colName = "imym";
                                else if (i == 2) colName = "imno";
                            }
                            if (proc.equals("HSIO_325U_STR") || proc.equals("HSIO_140U_STR")) {
                                if (i == 1) colName = "slipymd";
                                else if (i == 2) colName = "slipno";
                            }
                            if (proc.equals("HSIO_510U_STR") || proc.equals("HSIO_110U_STR") || proc.equals("HSIO_120U_STR")) {
                                if (i == 1) colName = "jsanym";
                                else if (i == 2) colName = "jsanno";
                            }
                            if (proc.equals("HSIO_530U_STR") || proc.equals("HSIO_531U_STR")) {
                                if (i == 1) colName = "slipymd";
                                else if (i == 2) colName = "slipno";
                            }
                            if (proc.equals("HSIO_570U_STR") || proc.equals("HSIO_490U_STR") ) {
                                if (i == 1) colName = "ioym";
                                else if (i == 2) colName = "iono";
                                else if (i == 3) colName = "ino";
                            }
                            if (proc.equals("HSIO_730U_STR")  ) {
                                if (i == 1) colName = "ioym";
                                else if (i == 2) colName = "iono";
                                else if (i == 3) colName = "ono";
                            }
                            if (proc.equals("HSIO_720U_STR")  ) {
                                if (i == 1) colName = "ioym";
                                else if (i == 2) colName = "ino";
                                else if (i == 3) colName = "ono";
                            }
                            if (proc.equals("HSIO_580U_STR")) {
                                if (i == 1) colName = "ioym";
                                else if (i == 2) colName = "iono";
                                else if (i == 3) colName = "ino";
                            }
                            if (proc.equals("HSIO_540U_STR") || proc.equals("HSIO_541U_STR") || proc.equals("HSIO_590U_STR")) {
                                if (i == 1) colName = "result";
                                else if (i == 2) colName = "msg";
                            }

                            if (proc.equals("HSIO_500U_STR") || proc.equals("HSIO_550U_STR") ||
                                proc.equals("HSIO_190U_STR") || proc.equals("HSIO_250U_STR") ||
                                proc.equals("HSIO_100U_STR")) {
                                if (i == 1) colName = "ioym";
                                else if (i == 2) colName = "iono";
                            }
                        }
                        if (colName == null || colName.isEmpty()) colName = "col_" + (i-1);
                        row.put(colName.toLowerCase(), val == null ? "" : val);
                        values.add(val == null ? "" : val);
                    }
                    row.put("returnKeyValue", values); 
                    return row;
                });
                log.info("🎯 [succ] data: {}", result);
            } else {
                switch (proc) {
                    case "HSIO_010U_STR": result = hsioMapper.HSIO_010U_STR(params); break;
                    case "HSIO_011U_STR": result = hsioMapper.HSIO_011U_STR(params); break;
                    case "HSIO_020U_STR": result = hsioMapper.HSIO_020U_STR(params); break;
                    case "HSIO_021U_STR": result = hsioMapper.HSIO_021U_STR(params); break;
                    case "HSIO_050U_STR": result = hsioMapper.HSIO_050U_STR(params); break;
                    case "HSIO_051U_STR": result = hsioMapper.HSIO_051U_STR(params); break;
                    case "HSIO_052U_STR": result = hsioMapper.HSIO_052U_STR(params); break;
                    case "HSIO_060U_STR": result = hsioMapper.HSIO_060U_STR(params); break;
                    case "HSIO_061U_STR": result = hsioMapper.HSIO_061U_STR(params); break;
                    case "HSIO_070U_STR": result = hsioMapper.HSIO_070U_STR(params); break;
                    case "HSIO_080S_STR": result = hsioMapper.HSIO_080S_STR(params); break;
                    case "HSIO_082S_STR": result = hsioMapper.HSIO_082S_STR(params); break;
                    case "HSIO_085S_STR": result = hsioMapper.HSIO_085S_STR(params); break;
                    case "HSIO_100U_STR": result = hsioMapper.HSIO_100U_STR(params); break;
                    case "HSIO_101U_STR": result = hsioMapper.HSIO_101U_STR(params); break;
                    case "HSIO_110U_STR": result = hsioMapper.HSIO_110U_STR(params); break;
                    case "HSIO_120U_STR": result = hsioMapper.HSIO_120U_STR(params); break;
                    case "HSIO_130U_STR": result = hsioMapper.HSIO_130U_STR(params); break;
                    case "HSIO_131U_STR": result = hsioMapper.HSIO_131U_STR(params); break;
                    case "HSIO_140U_STR": result = hsioMapper.HSIO_140U_STR(params); break;
                    case "HSIO_141U_STR": result = hsioMapper.HSIO_141U_STR(params); break;
                    case "HSIO_160U_STR": result = hsioMapper.HSIO_160U_STR(params); break;
                    case "HSIO_170U_STR": result = hsioMapper.HSIO_170U_STR(params); break;
                    case "HSIO_171U_STR": result = hsioMapper.HSIO_171U_STR(params); break;
                    case "HSIO_180U_STR": result = hsioMapper.HSIO_180U_STR(params); break;
                    case "HSIO_181U_STR": result = hsioMapper.HSIO_181U_STR(params); break;
                    case "HSIO_190U_STR": result = hsioMapper.HSIO_190U_STR(params); break;
                    case "HSIO_191U_STR": result = hsioMapper.HSIO_191U_STR(params); break;
                    case "HSIO_200S_STR": result = hsioMapper.HSIO_200S_STR(params); break;
                    case "HSIO_210S_STR": result = hsioMapper.HSIO_210S_STR(params); break;
                    case "HSIO_215S_STR": result = hsioMapper.HSIO_215S_STR(params); break;
                    case "HSIO_220S_STR": result = hsioMapper.HSIO_220S_STR(params); break;
                    case "HSIO_250U_STR": result = hsioMapper.HSIO_250U_STR(params); break;
                    case "HSIO_251U_STR": result = hsioMapper.HSIO_251U_STR(params); break;
                    case "HSIO_300U_STR": result = hsioMapper.HSIO_300U_STR(params); break;
                    case "HSIO_301U_STR": result = hsioMapper.HSIO_301U_STR(params); break;
                    case "HSIO_320U_STR": result = hsioMapper.HSIO_320U_STR(params); break;
                    case "HSIO_325U_STR": result = hsioMapper.HSIO_325U_STR(params); break;
                    case "HSIO_410S_STR": result = hsioMapper.HSIO_410S_STR(params); break;
                    case "HSIO_470S_STR": result = hsioMapper.HSIO_470S_STR(params); break;
                    case "HSIO_490U_STR": result = hsioMapper.HSIO_490U_STR(params); break;
                    case "HSIO_491U_STR": result = hsioMapper.HSIO_491U_STR(params); break;
                    case "HSIO_500U_STR": result = hsioMapper.HSIO_500U_STR(params); break;
                    case "HSIO_501U_STR": result = hsioMapper.HSIO_501U_STR(params); break;
                    case "HSIO_510U_STR": result = hsioMapper.HSIO_510U_STR(params); break;
                    case "HSIO_511U_STR": result = hsioMapper.HSIO_511U_STR(params); break;
                    case "HSIO_520U_STR": result = hsioMapper.HSIO_520U_STR(params); break;
                    case "HSIO_521U_STR": result = hsioMapper.HSIO_521U_STR(params); break;
                    case "HSIO_530U_STR": result = hsioMapper.HSIO_530U_STR(params); break;
                    case "HSIO_531U_STR": result = hsioMapper.HSIO_531U_STR(params); break;
                    case "HSIO_540U_STR": result = hsioMapper.HSIO_540U_STR(params); break;
                    case "HSIO_541U_STR": result = hsioMapper.HSIO_541U_STR(params); break;
                    case "HSIO_550U_STR": result = hsioMapper.HSIO_550U_STR(params); break;
                    case "HSIO_551U_STR": result = hsioMapper.HSIO_551U_STR(params); break;
                    case "HSIO_560U_STR": result = hsioMapper.HSIO_560U_STR(params); break;
                    case "HSIO_570U_STR": result = hsioMapper.HSIO_570U_STR(params); break;
                    case "HSIO_571U_STR": result = hsioMapper.HSIO_571U_STR(params); break;
                    case "HSIO_580U_STR": result = hsioMapper.HSIO_580U_STR(params); break;
                    case "HSIO_581U_STR": result = hsioMapper.HSIO_581U_STR(params); break;
                    case "HSIO_590U_STR": result = hsioMapper.HSIO_590U_STR(params); break;
                    case "HSIO_600S_STR": result = hsioMapper.HSIO_600S_STR(params); break;
                    case "HSIO_610S_STR": result = hsioMapper.HSIO_610S_STR(params); break;
                    case "HSIO_620S_STR": result = hsioMapper.HSIO_620S_STR(params); break;
                    case "HSIO_640S_STR": result = hsioMapper.HSIO_640S_STR(params); break;
                    case "HSIO_650S_STR": result = hsioMapper.HSIO_650S_STR(params); break;
                    case "HSIO_660S_STR": result = hsioMapper.HSIO_660S_STR(params); break;
                    case "HSIO_680S_STR": result = hsioMapper.HSIO_680S_STR(params); break;
                    case "HSIO_690S_STR": result = hsioMapper.HSIO_690S_STR(params); break;
                    case "HSIO_720U_STR": result = hsioMapper.HSIO_720U_STR(params); break;
                    case "HSIO_721U_STR": result = hsioMapper.HSIO_721U_STR(params); break;
                    case "HSIO_730U_STR": result = hsioMapper.HSIO_730U_STR(params); break;
                    case "HSIO_731U_STR": result = hsioMapper.HSIO_731U_STR(params); break;
                    default:
                        return ResponseEntity.notFound().build();
                }
            }

            if (result == null || result.isEmpty()) {
                // 🚀 조회성(Select, List, Print 등) 요청인 경우 빈 배열 반환, 그 외(저장/삭제 등)는 성공 메시지 반환
                if (actkind.startsWith("S") || actkind.startsWith("L") || actkind.startsWith("P") || actkind.isEmpty()) {
                    result = new ArrayList<>();
                } else {
                    result = List.of(Map.of("res", "OK"));
                }
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [hsio] executeProcedure Error ({}): {}, Payload: {}", proc, e.getMessage(), params);
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
            String statementId = HsioMapper.class.getName() + "." + proc;
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
        } catch (Exception e) { log.warn("🛠 missing parameter alarm ({}): {}", proc, e.getMessage()); }
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
            Set<String> vueKeys = vueParams.keySet();
            if (vueKeys.size() < xmlKeys.size()) {
                return String.format("📍 [PARAM SHORTAGE] XML:%d > VUE:%d\n📋 [REQUIRED]: %s", xmlKeys.size(), vueKeys.size(), xmlKeys);
            }
            return null;
        } catch (Exception e) { return "VALIDATION ERROR: " + e.getMessage(); }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HsioMapper.class.getName() + "." + proc;
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
