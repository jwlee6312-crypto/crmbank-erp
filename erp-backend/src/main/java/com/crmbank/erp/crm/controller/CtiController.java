package com.crmbank.erp.crm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.service.AsteriskService;
import com.crmbank.erp.crm.service.CtiOutboundService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/crm/cti")
@RequiredArgsConstructor
public class CtiController {

    private final AsteriskService asteriskService;
    private final CtiOutboundService ctiOutboundService;

    @GetMapping("/answer")
    public Map<String, Object> answerCall(@RequestParam String exten) {
        log.info("📥 [API] 전화받기 요청 -> 내선: {}", exten);
        asteriskService.answerCall(exten);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/hangup")
    public Map<String, Object> hangupCall(@RequestParam String exten, @RequestParam(required = false) String channel) {
        log.info("📥 [API] 전화끊기 요청 -> 내선: {}", exten);
        asteriskService.hangupCall(exten, channel);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/transfer")
    public Map<String, Object> transferCall(@RequestParam String exten, @RequestParam String target) {
        log.info("📥 [API] 돌려주기 요청 -> {} to {}", exten, target);
        asteriskService.transferCall(exten, target);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> checkStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("online", asteriskService.checkAmiConnection());
        return result;
    }

    /**
     * 💡 실제 아웃바운드 발신 실행
     */
    @GetMapping("/make-call")
    public Map<String, Object> makeCall(@RequestParam String exten, @RequestParam String dest) {
        log.info("🚀 [API] 아웃바운드 발신 요청: {} -> {}", exten, dest);
        String recFile = ctiOutboundService.makeCall(exten, dest, "haion-outbound");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("recFile", recFile); // 💡 생성될 파일명을 응답에 포함
        return result;
    }

    /**
     * 💡 녹취 파일 직접 재생 API
     */
    @GetMapping("/play-recording")
    public ResponseEntity<Resource> playRecording(@RequestParam String file) {
        String path = "/var/spool/asterisk/monitor/" + file;
        File audioFile = new File(path);
        
        if (!audioFile.exists()) {
            log.warn("⚠️ 녹취 파일 없음: {}", path);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file + "\"")
                .body(new FileSystemResource(audioFile));
    }
}
