package com.crmbank.erp.crm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.dto.CallMstDto;
import com.crmbank.erp.crm.dto.TotalCallLogDto;
import com.crmbank.erp.comm.dto.UserSession;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/crm/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;
    private final GeminiAiService geminiAiService;

    /**
     * 📞 ARS 콜백 요청 로그 기록
     */
    @PostMapping("/log-callback")
    public ResponseEntity<Map<String, Object>> logCallback(@RequestBody Map<String, Object> params) {
        try {
            log.info("📞 [ARS CALLBACK] 요청 수신: {}", params);
            
            TotalCallLogDto logDto = TotalCallLogDto.builder()
                    .uniqueid(String.valueOf(params.get("interaction_id")))
                    .keyword(String.valueOf(params.get("keyword")))
                    .media_type(String.valueOf(params.get("media_type")))
                    .cmpycd(String.valueOf(params.get("cmpycd")))
                    .direction("in")
                    .start_time(LocalDateTime.now())
                    .callback_yn("Y")
                    .callback_no(String.valueOf(params.get("keyword")))
                    .callback_req_time(LocalDateTime.now())
                    .callback_retry_cnt(0)
                    .build();

            inboundService.insertTotalInteractionLog(logDto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("❌ 콜백 로그 저장 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 💡 상담 통합 저장 및 자동 STT/요약
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestBody SaveRequest request, HttpSession session) {
        try {
            // 💡 [소문자 표준화] session key: "user_session"
            UserSession user = (UserSession) session.getAttribute("user_session");
            String cmpycd = user != null ? user.getCmpycd() : "haionnet";
            String userid = user != null ? user.getUserid() : "system";
            String deptcd = user != null ? user.getDeptcd() : "";
            
            CallMstDto dto = request.getDto();
            dto.setCmpycd(cmpycd);
            dto.setConsultid(userid);
            dto.setUpdemp(userid);
            dto.setDeptcd(deptcd);
            dto.setHappycall_yn("N");
            
            if (dto.getInteraction_id() == null || dto.getInteraction_id().isEmpty()) {
                dto.setInteraction_id("IN_" + UUID.randomUUID().toString().substring(0, 8));
            }
            
            dto.setEnd_time(LocalDateTime.now());

            if (request.getRecordings() != null && !request.getRecordings().isEmpty()) {
                String lastFile = request.getRecordings().get(request.getRecordings().size() - 1);
                String fullPath = "/var/spool/asterisk/monitor/" + lastFile;
                Map<String, String> aiResult = geminiAiService.analyzeAudio(fullPath);
                dto.setAns_ment(aiResult.get("stt"));
                dto.setAi_summary(aiResult.get("summary"));
                dto.setRec_file(lastFile);
            }

            String svcymd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String svcno = inboundService.saveCallMst(dto, request.getRecordings(), svcymd, deptcd);

            return ResponseEntity.ok(Map.of("success", true, "svcno", svcno));
        } catch (Exception e) {
            log.error("상담 저장 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/ai-summarize")
    public Map<String, String> aiSummarize(@RequestBody Map<String, String> params) {
        String trbMent = params.get("trb_ment");
        String ansMent = params.get("ans_ment");
        String chatLog = "문의내용: " + trbMent + "\n답변내용: " + ansMent;
        String summary = geminiAiService.summarizeText(chatLog);

        Map<String, String> result = new HashMap<>();
        result.put("summary", summary);
        result.put("deptcd", ""); 
        return result;
    }

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

    @GetMapping("/status-list")
    public List<Map<String, Object>> getStatusList(
            @RequestParam String fromdt,
            @RequestParam String todt,
            @RequestParam(required = false) String custnm,
            HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "haionnet";
        return inboundService.getStatusList(cmpycd, fromdt, todt, custnm);
    }

    @GetMapping("/customer-detail")
    public Map<String, Object> getCustomerDetail(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "haionnet";
        return inboundService.getCustomerByCustCd(cmpycd, custcd);
    }

    @GetMapping("/item-list")
    public List<Map<String, Object>> getItemList(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "haionnet";
        return inboundService.getItemList(cmpycd, custcd);
    }

    @GetMapping("/call-history")
    public List<Map<String, Object>> getCallHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "haionnet";
        return inboundService.getCallHistory(cmpycd, custcd);
    }

    @GetMapping("/service-history")
    public List<Map<String, Object>> getServiceHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "haionnet";
        return inboundService.getServiceHistory(cmpycd, custcd);
    }

    @GetMapping("/settle-history")
    public List<Map<String, Object>> getSettleHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "haionnet";
        return inboundService.getSettleHistory(cmpycd, custcd);
    }

    @Data public static class SaveRequest { private CallMstDto dto; private List<String> recordings; }
}
