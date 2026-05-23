package com.crmbank.erp.crm.service;

import com.crmbank.erp.crm.dto.CampRsltDtlDto;
import com.crmbank.erp.crm.dto.CampRsltMstDto;
import com.crmbank.erp.crm.dto.ConsultSaveRequest;
import com.crmbank.erp.crm.mapper.outbound.OutboundMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
            if (request.getStartTime() != null) {
                String st = request.getStartTime();
                if (st.length() == 10) st += " 00:00:00";
                startDtime = LocalDateTime.parse(st, formatter);
            }
            if (request.getEndTime() != null) {
                String et = request.getEndTime();
                if (et.length() == 10) et += " 23:59:59";
                endDtime = LocalDateTime.parse(et, formatter);
            }
        } catch (Exception e) {
            log.warn("Time parsing failed: {}", e.getMessage());
        }

        if (startDtime == null) startDtime = LocalDateTime.now().minusMinutes(5);
        if (endDtime == null) endDtime = LocalDateTime.now();

        String cmpycd = (request.getCmpycd() != null && !request.getCmpycd().isEmpty()) ? request.getCmpycd() : "haionnet";
        String mediaType = (request.getMediaType() != null && !request.getMediaType().isEmpty()) ? request.getMediaType() : "chat";

        CampRsltMstDto mstDto = CampRsltMstDto.builder()
                .CMPYCD(cmpycd)
                .CALL_SEQ(request.getCallSeq())
                .CAMP_NO(request.getCampNo())
                .RSLT_CD(request.getRsltCd())
                .REMARK(request.getMemo())
                .USERID(request.getUserid())
                .LINE_NUM(request.getLineNum())   // 내선번호
                .CALL_TELNO(request.getCallTelno()) // 고객연락처
                .INTERACTION_ID(interactionId)
                .MEDIA_TYPE(mediaType)
                .CHAT_LOG(chatLog)
                .AI_SUMMARY(aiSummary)
                .REC_FILE(request.getRecFile())
                .START_DTIME(startDtime)
                .END_DTIME(endDtime)
                .UPDEMP(request.getUserid())
                .ADDTIME(LocalDateTime.now())
                .UPDTIME(LocalDateTime.now())
                .build();
        
        outboundMapper.insertCampaignRsltMst(mstDto);

        if (request.getSurveys() != null) {
            for (Map<String, Object> survey : request.getSurveys()) {
                String survNo = (String) survey.get("surv_no");
                String ansNo = survey.get("ans_no") != null ? (String) survey.get("ans_no") : "001";

                // 💡 프론트에서 이미 점수를 가져왔으므로 DB 조회 없이 전달받은 점수 사용
                Object pointObj = survey.get("point");
                BigDecimal point = BigDecimal.ZERO;
                if (pointObj != null) {
                    try {
                        point = new BigDecimal(pointObj.toString());
                    } catch (Exception e) {
                        log.warn("Point parsing failed for value: {}", pointObj);
                    }
                }

                CampRsltDtlDto dtlDto = CampRsltDtlDto.builder()
                        .CMPYCD(cmpycd)
                        .RSLT_NO(mstDto.getRSLT_NO())
                        .SURV_NO(survNo)
                        .ANS_NO(ansNo)
                        .POINT(point) // 💡 전달받은 점수 바로 등록
                        .REMARK((String) survey.get("essay"))
                        .UPDEMP(request.getUserid())
                        .ADDTIME(LocalDateTime.now())
                        .UPDTIME(LocalDateTime.now())
                        .build();
                outboundMapper.insertCampaignRsltDtl(dtlDto);
            }
        }

        Map<String, Object> statusParam = new HashMap<>();
        statusParam.put("CMPYCD", cmpycd);
        statusParam.put("CALL_SEQ", request.getCallSeq());
        statusParam.put("STATUS", "030");
        statusParam.put("RSLT_CD", request.getRsltCd());
        statusParam.put("RESV_DTIME", request.getResvDtime());
        statusParam.put("RESV_Memo", request.getResvMemo());
        outboundMapper.updateCallListStatus(statusParam);
    }
}
