package com.crmbank.erp.crm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.dto.CallMstDto;
import com.crmbank.erp.crm.dto.TotalCallLogDto;
import com.crmbank.erp.crm.mapper.inbound.InboundMapper;
import com.crmbank.erp.crm.service.InboundService;
import com.crmbank.erp.crm.service.GeminiAiService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/crm/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;
    private final InboundMapper inboundMapper; // 💡 주입 추가
    private final GeminiAiService geminiAiService;

    /**
     * 💡 녹취 파일 스트리밍
     */
    @GetMapping("/play-recording")
    public ResponseEntity<Resource> playRecording(@RequestParam String file) {
        String path = "/var/spool/asterisk/monitor/" + file;
        File audioFile = new File(path);
        if (!audioFile.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file + "\"")
                .body(new FileSystemResource(audioFile));
    }

    /**
     * 💡 상담 저장 및 자동 STT/요약 + 통합 이력 연동
     */
    @PostMapping("/save")
    public String save(@RequestBody SaveRequest request, HttpSession session) {
        String cmpycd = String.valueOf(session.getAttribute("cmpycd"));
        String userid = String.valueOf(session.getAttribute("userid"));
        
        String interactionId = "IN_" + UUID.randomUUID().toString().substring(0, 8);
        
        CallMstDto dto = request.getDto();
        dto.setCmpycd(cmpycd);
        dto.setConsultid(userid);
        dto.setUpdemp(userid);
        dto.setInteraction_id(interactionId);
        dto.setEnd_time(LocalDateTime.now());

        String lastFile = null;
        if (request.getRecordings() != null && !request.getRecordings().isEmpty()) {
            lastFile = request.getRecordings().get(request.getRecordings().size() - 1);
            String fullPath = "/var/spool/asterisk/monitor/" + lastFile;
            
            log.info("🎙 [AI Inbound] 분석 시작: {}", fullPath);
            Map<String, String> aiResult = geminiAiService.analyzeAudio(fullPath);
            
            dto.setAns_ment(aiResult.get("stt"));
            dto.setAi_summary(aiResult.get("summary"));
            dto.setRec_file(lastFile);
        }

        String svcymd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return inboundService.saveCallMst(dto, request.getRecordings(), svcymd);
    }

    /**
     * 💡 인바운드 상담현황 목록 조회
     */
    @GetMapping("/status-list")
    public List<Map<String, Object>> getStatusList(
            @RequestParam String fromdt,
            @RequestParam String todt,
            @RequestParam(required = false) String custnm,
            HttpSession session) {
        String cmpycd = (String) session.getAttribute("cmpycd");
        if (cmpycd == null) cmpycd = "HAIONNET";
        return inboundService.getStatusList(cmpycd, fromdt, todt, custnm);
    }

    /**
     * 💡 [IVR 연동] 콜백 접수 기록
     */
    @PostMapping("/log-callback")
    public void logCallback(@RequestBody Map<String, Object> params) {
        log.info("📞 [IVR] 콜백 접수 요청: {}", params);
        
        String interactionId = (String) params.get("interaction_id"); // 💡 변수 추출
        String cmpycd = (String) params.get("cmpycd");
        String keyword = (String) params.get("keyword");

        // 💡 total_interaction_log 테이블에 media_type='callback'으로 저장
        inboundMapper.insertTotalInteractionLog(
                TotalCallLogDto.builder()
                        .uniqueid(interactionId)
                        .cmpycd(cmpycd != null ? cmpycd : "HAIONNET")
                        .direction("in")
                        .media_type("callback") // ✅ 필드명 보정됨
                        .dst_no(keyword)
                        .start_time(LocalDateTime.now())
                        .build()
        );
        log.info("✅ [IVR] 콜백 저장 완료: {}", interactionId);
    }

    @GetMapping("/customer-detail")
    public Map<String, Object> getCustomerDetail(@RequestParam String custcd, HttpSession session) {
        String cmpycd = String.valueOf(session.getAttribute("cmpycd"));
        return inboundService.getCustomerByCustCd(cmpycd, custcd);
    }

    @GetMapping("/item-list")
    public List<Map<String, Object>> getItemList(@RequestParam String custcd, HttpSession session) {
        String cmpycd = String.valueOf(session.getAttribute("cmpycd"));
        return inboundService.getItemList(cmpycd, custcd);
    }

    @GetMapping("/call-history")
    public List<Map<String, Object>> getCallHistory(@RequestParam String custcd, HttpSession session) {
        String cmpycd = String.valueOf(session.getAttribute("cmpycd"));
        return inboundService.getCallHistory(cmpycd, custcd);
    }

    @Data public static class SaveRequest { private CallMstDto dto; private List<String> recordings; }
}
