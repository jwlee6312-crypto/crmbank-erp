package com.crmbank.erp.hsio.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsio.mapper.HsioMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsio")
@RequiredArgsConstructor
public class HsioController {

    private final HsioMapper hsioMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("USER_SESSION");
        if (user != null) {
            if (!params.containsKey("CMPYCD")) params.put("CMPYCD", user.getCMPYCD());
            if (!params.containsKey("USERID")) params.put("USERID", user.getUSERID());
            params.put("UPDEMP", user.getUSERID());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        log.info("🚀 [HSIO] {} 호출: {}", procedure, params);
        
        switch (procedure.toUpperCase()) {
            // 구매/입고/정산
            case "HSIO_010U_STR": return ResponseEntity.ok(hsioMapper.HSIO_010U_STR(params));
            case "HSIO_011U_STR": return ResponseEntity.ok(hsioMapper.HSIO_011U_STR(params));
            case "HSIO_020U_STR": return ResponseEntity.ok(hsioMapper.HSIO_020U_STR(params));
            case "HSIO_021U_STR": return ResponseEntity.ok(hsioMapper.HSIO_021U_STR(params));
            case "HSIO_060U_STR": return ResponseEntity.ok(hsioMapper.HSIO_060U_STR(params));
            case "HSIO_061U_STR": return ResponseEntity.ok(hsioMapper.HSIO_061U_STR(params));
            case "HSIO_070U_STR": return ResponseEntity.ok(hsioMapper.HSIO_070U_STR(params));
            case "HSIO_080S_STR": return ResponseEntity.ok(hsioMapper.HSIO_080S_STR(params));
            case "HSIO_082S_STR": return ResponseEntity.ok(hsioMapper.HSIO_082S_STR(params));
            case "HSIO_085S_STR": return ResponseEntity.ok(hsioMapper.HSIO_085S_STR(params));
            case "HSIO_110U_STR": return ResponseEntity.ok(hsioMapper.HSIO_110U_STR(params));
            case "HSIO_120U_STR": return ResponseEntity.ok(hsioMapper.HSIO_120U_STR(params));
            case "HSIO_130U_STR": return ResponseEntity.ok(hsioMapper.HSIO_130U_STR(params));
            case "HSIO_131U_STR": return ResponseEntity.ok(hsioMapper.HSIO_131U_STR(params));
            case "HSIO_140U_STR": return ResponseEntity.ok(hsioMapper.HSIO_140U_STR(params));
            case "HSIO_141U_STR": return ResponseEntity.ok(hsioMapper.HSIO_141U_STR(params));

            case "HSIO_160U_STR": return ResponseEntity.ok(hsioMapper.HSIO_160U_STR(params));
            case "HSIO_170U_STR": return ResponseEntity.ok(hsioMapper.HSIO_170U_STR(params));
            case "HSIO_171U_STR": return ResponseEntity.ok(hsioMapper.HSIO_171U_STR(params));
            case "HSIO_180U_STR": return ResponseEntity.ok(hsioMapper.HSIO_180U_STR(params));
            case "HSIO_181U_STR": return ResponseEntity.ok(hsioMapper.HSIO_181U_STR(params));
            case "HSIO_190U_STR": return ResponseEntity.ok(hsioMapper.HSIO_190U_STR(params));
            case "HSIO_191U_STR": return ResponseEntity.ok(hsioMapper.HSIO_191U_STR(params));
            case "HSIO_200S_STR": return ResponseEntity.ok(hsioMapper.HSIO_200S_STR(params));
            case "HSIO_210S_STR": return ResponseEntity.ok(hsioMapper.HSIO_210S_STR(params));
            case "HSIO_215S_STR": return ResponseEntity.ok(hsioMapper.HSIO_215S_STR(params));
            case "HSIO_220S_STR": return ResponseEntity.ok(hsioMapper.HSIO_220S_STR(params));
            case "HSIO_250U_STR": return ResponseEntity.ok(hsioMapper.HSIO_250U_STR(params));
            case "HSIO_251U_STR": return ResponseEntity.ok(hsioMapper.HSIO_251U_STR(params));
            case "HSIO_690S_STR": return ResponseEntity.ok(hsioMapper.HSIO_690S_STR(params));

            // 출고/매출/입금
            case "HSIO_300U_STR": return ResponseEntity.ok(hsioMapper.HSIO_300U_STR(params));
            case "HSIO_325U_STR": return ResponseEntity.ok(hsioMapper.HSIO_325U_STR(params));
            case "HSIO_470S_STR": return ResponseEntity.ok(hsioMapper.HSIO_470S_STR(params));
            case "HSIO_490U_STR": return ResponseEntity.ok(hsioMapper.HSIO_490U_STR(params));
            case "HSIO_491U_STR": return ResponseEntity.ok(hsioMapper.HSIO_491U_STR(params));
            case "HSIO_500U_STR": return ResponseEntity.ok(hsioMapper.HSIO_500U_STR(params));
            case "HSIO_501U_STR": return ResponseEntity.ok(hsioMapper.HSIO_501U_STR(params));
            case "HSIO_510U_STR": return ResponseEntity.ok(hsioMapper.HSIO_510U_STR(params));
            case "HSIO_511U_STR": return ResponseEntity.ok(hsioMapper.HSIO_511U_STR(params));
            case "HSIO_520U_STR": return ResponseEntity.ok(hsioMapper.HSIO_520U_STR(params));
            case "HSIO_521U_STR": return ResponseEntity.ok(hsioMapper.HSIO_521U_STR(params));
            case "HSIO_530U_STR": return ResponseEntity.ok(hsioMapper.HSIO_530U_STR(params));
            case "HSIO_531U_STR": return ResponseEntity.ok(hsioMapper.HSIO_531U_STR(params));
            case "HSIO_540U_STR": return ResponseEntity.ok(hsioMapper.HSIO_540U_STR(params));
            case "HSIO_541U_STR": return ResponseEntity.ok(hsioMapper.HSIO_541U_STR(params));
            case "HSIO_550U_STR": return ResponseEntity.ok(hsioMapper.HSIO_550U_STR(params));
            case "HSIO_551U_STR": return ResponseEntity.ok(hsioMapper.HSIO_551U_STR(params));
            case "HSIO_560U_STR": return ResponseEntity.ok(hsioMapper.HSIO_560U_STR(params));
            case "HSIO_570U_STR": return ResponseEntity.ok(hsioMapper.HSIO_570U_STR(params));
            case "HSIO_571U_STR": return ResponseEntity.ok(hsioMapper.HSIO_571U_STR(params));
            case "HSIO_580U_STR": return ResponseEntity.ok(hsioMapper.HSIO_580U_STR(params));
            case "HSIO_581U_STR": return ResponseEntity.ok(hsioMapper.HSIO_581U_STR(params));
            case "HSIO_600S_STR": return ResponseEntity.ok(hsioMapper.HSIO_600S_STR(params));
            case "HSIO_610S_STR": return ResponseEntity.ok(hsioMapper.HSIO_610S_STR(params));
            case "HSIO_620S_STR": return ResponseEntity.ok(hsioMapper.HSIO_620S_STR(params));
            case "HSIO_640S_STR": return ResponseEntity.ok(hsioMapper.HSIO_640S_STR(params));
            case "HSIO_650S_STR": return ResponseEntity.ok(hsioMapper.HSIO_650S_STR(params));
            case "HSIO_660S_STR": return ResponseEntity.ok(hsioMapper.HSIO_660S_STR(params));
            case "HSIO_680S_STR": return ResponseEntity.ok(hsioMapper.HSIO_680S_STR(params));
            case "HSIO_720U_STR": return ResponseEntity.ok(hsioMapper.HSIO_720U_STR(params));
            case "HSIO_721U_STR": return ResponseEntity.ok(hsioMapper.HSIO_721U_STR(params));
            case "HSIO_730U_STR": return ResponseEntity.ok(hsioMapper.HSIO_730U_STR(params));
            case "HSIO_731U_STR": return ResponseEntity.ok(hsioMapper.HSIO_731U_STR(params));

            default:
                log.warn("⚠️ 미등록 HSIO 프로시저: {}", procedure);
                return ResponseEntity.notFound().build();
        }
    }
}
