package com.crmbank.erp.hpio.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hpio.mapper.HpioMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hpio")
@RequiredArgsConstructor
public class HpioController {

    private final HpioMapper hpioMapper;

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (!params.containsKey("cmpycd")) params.put("cmpycd", user.getCmpycd());
            if (!params.containsKey("userid")) params.put("userid", user.getUserid());
            params.put("updemp", user.getUserid());
        }
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {
        
        injectSession(params, session);
        log.info("🚀 [HPIO] {} 호출: {}", procedure, params);
        
        switch (procedure.toUpperCase()) {
            case "HPIO_200U_STR": return ResponseEntity.ok(hpioMapper.HPIO_200U_STR(params));
            case "HPIO_210U_STR": return ResponseEntity.ok(hpioMapper.HPIO_210U_STR(params));
            case "HPIO_230S_STR": return ResponseEntity.ok(hpioMapper.HPIO_230S_STR(params));
            case "HPIO_290U_STR": return ResponseEntity.ok(hpioMapper.HPIO_290U_STR(params));
            case "HPIO_291U_STR": return ResponseEntity.ok(hpioMapper.HPIO_291U_STR(params));
            case "HPIO_300U_STR": return ResponseEntity.ok(hpioMapper.HPIO_300U_STR(params));
            case "HPIO_301U_STR": return ResponseEntity.ok(hpioMapper.HPIO_301U_STR(params));
            case "HPIO_340U_STR": return ResponseEntity.ok(hpioMapper.HPIO_340U_STR(params));
            case "HPIO_341U_STR": return ResponseEntity.ok(hpioMapper.HPIO_341U_STR(params));
            case "HPIO_350U_STR": return ResponseEntity.ok(hpioMapper.HPIO_350U_STR(params));
            case "HPIO_351U_STR": return ResponseEntity.ok(hpioMapper.HPIO_351U_STR(params));
            case "HPIO_360S_STR": return ResponseEntity.ok(hpioMapper.HPIO_360S_STR(params));
            case "HPIO_370S_STR": return ResponseEntity.ok(hpioMapper.HPIO_370S_STR(params));
            case "HPIO_380S_STR": return ResponseEntity.ok(hpioMapper.HPIO_380S_STR(params));
            case "HPIO_390S_STR": return ResponseEntity.ok(hpioMapper.HPIO_390S_STR(params));
            case "HPIO_400U_STR": return ResponseEntity.ok(hpioMapper.HPIO_400U_STR(params));
            case "HPIO_410U_STR": return ResponseEntity.ok(hpioMapper.HPIO_410U_STR(params));
            case "HPIO_420S_STR": return ResponseEntity.ok(hpioMapper.HPIO_420S_STR(params));
            case "HPIO_430S_STR": return ResponseEntity.ok(hpioMapper.HPIO_430S_STR(params));
            case "HPIO_500U_STR": return ResponseEntity.ok(hpioMapper.HPIO_500U_STR(params));
            case "HPIO_501U_STR": return ResponseEntity.ok(hpioMapper.HPIO_501U_STR(params));
            case "HPIO_510U_STR": return ResponseEntity.ok(hpioMapper.HPIO_510U_STR(params));
            case "HPIO_511U_STR": return ResponseEntity.ok(hpioMapper.HPIO_511U_STR(params));
            case "HPIO_520U_STR": return ResponseEntity.ok(hpioMapper.HPIO_520U_STR(params));
            case "HPIO_521U_STR": return ResponseEntity.ok(hpioMapper.HPIO_521U_STR(params));
            case "HPIO_640S_STR": return ResponseEntity.ok(hpioMapper.HPIO_640S_STR(params));
            case "HPIO_650S_STR": return ResponseEntity.ok(hpioMapper.HPIO_650S_STR(params));
            case "HPIO_660S_STR": return ResponseEntity.ok(hpioMapper.HPIO_660S_STR(params));
            case "HPIO_710S_STR": return ResponseEntity.ok(hpioMapper.HPIO_710S_STR(params));
            case "HPIO_720S_STR": return ResponseEntity.ok(hpioMapper.HPIO_720S_STR(params));
            case "HPIO_850S_STR": return ResponseEntity.ok(hpioMapper.HPIO_850S_STR(params));
            case "HPIO_870U_STR": return ResponseEntity.ok(hpioMapper.HPIO_870U_STR(params));
            default:
                return ResponseEntity.notFound().build();
        }
    }
}
