package com.crmbank.erp.crm.service;

import com.crmbank.erp.crm.dto.CampRsltDtlDto;
import com.crmbank.erp.crm.dto.CampRsltMstDto;
import com.crmbank.erp.crm.dto.ConsultSaveRequest;
import com.crmbank.erp.crm.mapper.outbound.OutboundMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultSaveService {

    private final OutboundMapper outboundMapper;
    private final GeminiAiService geminiAiService;

    @Transactional
    public void saveOmniConsultation(ConsultSaveRequest request) {
        String interactionId = (request.getMediaType() != null && "call".equalsIgnoreCase(request.getMediaType())) 
                ? "IN_" + UUID.randomUUID().toString().substring(0, 8)
                : "CHAT_" + UUID.randomUUID().toString().substring(0, 8);
        
        // 🤖 통합 AI 분석 및 요약 적용
        Map<String, String> aiResult = geminiAiService.analyze(request.getChatHistory(), request.getRecFile());
        String chatLog = aiResult.get("stt");
        String aiSummary = aiResult.get("summary");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDtime = null;
        LocalDateTime endDtime = null;
        try {
            if (request.getStartTime() != null) startDtime = LocalDateTime.parse(request.getStartTime(), formatter);
            if (request.getEndTime() != null) endDtime = LocalDateTime.parse(request.getEndTime(), formatter);
        } catch (Exception e) { }

        if (startDtime == null) startDtime = LocalDateTime.now().minusMinutes(5);
        if (endDtime == null) endDtime = LocalDateTime.now();

        String cmpycd = (request.getCmpycd() != null && !request.getCmpycd().isEmpty()) ? request.getCmpycd() : "haionnet";
        String mediaType = (request.getMediaType() != null && !request.getMediaType().isEmpty()) ? request.getMediaType() : "chat";

        CampRsltMstDto mstDto = CampRsltMstDto.builder()
                .cmpycd(cmpycd)
                .call_seq(request.getCallSeq()) 
                .camp_no(request.getCampNo())
                .rslt_cd(request.getRsltCd())
                .remark(request.getMemo())
                .userid(request.getUserid())
                .interaction_id(interactionId)
                .media_type(mediaType)
                .chat_log(chatLog)
                .ai_summary(aiSummary)
                .rec_file(request.getRecFile())
                .start_dtime(startDtime)
                .end_dtime(endDtime)
                .updemp(request.getUserid())
                .addtime(LocalDateTime.now())
                .updtime(LocalDateTime.now())
                .build();
        
        outboundMapper.insertCampaignRsltMst(mstDto);

        if (request.getSurveys() != null) {
            for (Map<String, String> survey : request.getSurveys()) {
                CampRsltDtlDto dtlDto = CampRsltDtlDto.builder()
                        .cmpycd(cmpycd)
                        .rslt_no(mstDto.getRslt_no())
                        .surv_no(survey.get("surv_no"))
                        .ans_no(survey.get("ans_no") != null ? survey.get("ans_no") : "001")
                        .remark(survey.get("essay"))
                        .updemp(request.getUserid())
                        .addtime(LocalDateTime.now())
                        .updtime(LocalDateTime.now())
                        .build();
                outboundMapper.insertCampaignRsltDtl(dtlDto);
            }
        }

        Map<String, Object> statusParam = new HashMap<>();
        statusParam.put("cmpycd", cmpycd);
        statusParam.put("call_seq", request.getCallSeq());
        statusParam.put("status", "030");
        statusParam.put("rslt_cd", request.getRsltCd());
        statusParam.put("resv_dtime", request.getResvDtime());
        statusParam.put("resv_memo", request.getResvMemo());
        outboundMapper.updateCallListStatus(statusParam);
    }
}
