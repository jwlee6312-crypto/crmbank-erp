package com.crmbank.erp.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.dto.*;
import com.crmbank.erp.crm.mapper.outbound.OutboundMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundService {

    private final OutboundMapper outboundMapper;
    private final ObjectMapper objectMapper;
    private final GeminiAiService geminiAiService;

    // 1. 공통 코드 및 캠페인 조회
    public List<Map<String, Object>> getCrmCodeList(Map<String, Object> params) {
        return outboundMapper.selectCrmCodeList(params);
    }

    public List<Map<String, Object>> getCampList(Map<String, Object> params) {
        return outboundMapper.selectCampList(params);
    }

    // 2. 캠페인 마스터 관리
    @Transactional
    public void saveCampMst(CampMstDto dto) {
        if (dto.getCamp_no() == null || dto.getCamp_no().trim().isEmpty()) {
            Map<String, Object> p = new HashMap<>();
            p.put("CMPYCD", dto.getCmpycd());
            dto.setCamp_no(outboundMapper.generateCampNo(p));
            outboundMapper.insertCampMst(dto);
        } else {
            outboundMapper.updateCampMst(dto);
        }
    }

    @Transactional
    public void deleteCamp(Map<String, Object> params) {
        outboundMapper.deleteCampMst(params);
    }

    // 3. 질문 및 답변 관리
    public List<Map<String, Object>> getSurvMstList(Map<String, Object> params) {
        return outboundMapper.selectSurvMstList(params);
    }

    public List<Map<String, Object>> getCampSurvMstList(Map<String, Object> params) {
        return outboundMapper.selectCampSurvMstList(params);
    }

    public List<Map<String, Object>> getSurvDtlList(Map<String, Object> params) {
        return outboundMapper.selectSurvDtlList(params);
    }

    @Transactional
    public void saveSurveyTransaction(Map<String, Object> payload, String CMPYCD, String USERID) {
        Map<String, Object> mstMap = (Map<String, Object>) payload.get("MST");
        List<Map<String, Object>> dtlList = (List<Map<String, Object>>) payload.get("DTL");

        String SURV_NO = (String) mstMap.get("SURV_NO");
        SurvMstDto mstDto = objectMapper.convertValue(mstMap, SurvMstDto.class);
        mstDto.setCmpycd(CMPYCD);
        mstDto.setUpdemp(USERID);

        if (SURV_NO == null || SURV_NO.isEmpty()) {
            SURV_NO = outboundMapper.generateSurvNo(Map.of("CMPYCD", CMPYCD));
            mstDto.setSurv_no(SURV_NO);
            outboundMapper.insertSurvMst(mstDto);
        } else {
            outboundMapper.updateSurvMst(mstDto);
        }

        outboundMapper.deleteSurvDtlAll(Map.of("CMPYCD", CMPYCD, "SURV_NO", SURV_NO));
        if (dtlList != null) {
            for (Map<String, Object> dtl : dtlList) {
                SurvDtlDto dtlDto = objectMapper.convertValue(dtl, SurvDtlDto.class);
                dtlDto.setCmpycd(CMPYCD);
                dtlDto.setSurv_no(SURV_NO);
                dtlDto.setUpdemp(USERID);
                outboundMapper.insertSurvDtl(dtlDto);
            }
        }
    }

    @Transactional
    public void removeSurvey(Map<String, Object> params) {
        outboundMapper.deleteSurvDtlAll(params);
        outboundMapper.deleteSurvMst(params);
    }

    // 4. 설문 유형 매핑
    public List<Map<String, Object>> getMappedQuestions(Map<String, Object> params) {
        return outboundMapper.selectMappedQuestions(params);
    }

    @Transactional
    public void saveMapping(Map<String, Object> payload) {
        outboundMapper.deleteCampSurveyAll(payload);
        List<Map<String, Object>> questions = (List<Map<String, Object>>) payload.get("QUESTIONS");
        if (questions != null) {
            for (Map<String, Object> q : questions) {
                Map<String, Object> p = new HashMap<>(payload);
                p.put("SURV_NO", q.get("SURV_NO"));
                p.put("SORTCD", q.get("SORTCD"));
                p.put("USEYN", "Y");
                outboundMapper.insertCampSurvey(p);
            }
        }
    }

    // 5. 캠페인 대상자(콜 리스트) 관리
    public List<Map<String, Object>> getCampCallList(Map<String, Object> params) {
        return outboundMapper.selectCampCallList(params);
    }

    @Transactional
    public void saveCallListBatch(List<Map<String, Object>> list) {
        if (list != null && !list.isEmpty()) {
            outboundMapper.insertCampCallListBatch(list);
        }
    }

    @Transactional
    public void removeCampCallListBatch(Map<String, Object> params) {
        outboundMapper.deleteCampCallListBatch(params);
    }

    // 6. 속성 매핑 관리
    public List<CampAttrMapperDto> getAttrList(Map<String, Object> params) {
        return outboundMapper.selectAttrMapperList(params);
    }

    @Transactional
    public void saveAttrMapperBatch(Map<String, Object> params, List<Map<String, Object>> list) {
        outboundMapper.deleteAttrMapperAll(params);
        if (list != null) {
            for (Map<String, Object> item : list) {
                CampAttrMapperDto dto = objectMapper.convertValue(item, CampAttrMapperDto.class);
                dto.setCmpycd((String) params.get("CMPYCD"));
                dto.setSurv_gb((String) params.get("SURV_GB"));
                outboundMapper.insertAttrMapper(dto);
            }
        }
    }

    // 7. 상담 현황 및 이력
    public Map<String, Object> getCampStats(Map<String, Object> params) {
        Map<String, Object> stats = outboundMapper.selectCampCallStats(params);
        if (stats == null) stats = new HashMap<>();
        stats.put("DETAILS", outboundMapper.selectCampDetailStats(params));
        return stats;
    }

    public List<Map<String, Object>> getSurvFormList(Map<String, Object> params) {
        return outboundMapper.selectSurvFormList(params);
    }

    public List<Map<String, Object>> getCampaignHistory(Map<String, Object> params) {
        return outboundMapper.selectCampaignHistory(params);
    }

    /**
     * 💡 [최종] 캠페인 상담 통합 저장 (에러 보정 및 안정화)
     */
    @Transactional
    public void saveConsolidated(Map<String, Object> payload) {
        String CMPYCD = (String) payload.get("CMPYCD");
        String CAMP_NO = (String) payload.get("CAMP_NO");
        Object callSeqObj = payload.get("CALL_SEQ");
        Integer CALL_SEQ = callSeqObj instanceof Integer ? (Integer) callSeqObj : Integer.parseInt(String.valueOf(callSeqObj));
        String RSLT_CD = (String) payload.get("RSLT_CD");
        String REMARK = (String) (payload.get("MEMO") != null ? payload.get("MEMO") : payload.get("REMARK"));
        String USERID = (String) payload.get("USERID");
        String REC_FILE = (String) payload.get("REC_FILE");
        String CHAT_HISTORY = (String) payload.get("CHAT_HISTORY");
        String MEDIA_TYPE = (String) payload.get("MEDIA_TYPE");
        String LINE_NUM = (String) payload.get("LINE_NUM");

        // 시간 파싱 (String -> LocalDateTime)
        String startTimeStr = (String) payload.get("START_TIME");
        String endTimeStr = (String) payload.get("END_TIME");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        LocalDateTime startDtime = (startTimeStr != null && !startTimeStr.isEmpty()) ? LocalDateTime.parse(startTimeStr, formatter) : LocalDateTime.now();
        LocalDateTime endDtime = (endTimeStr != null && !endTimeStr.isEmpty()) ? LocalDateTime.parse(endTimeStr, formatter) : LocalDateTime.now();

        // 1. 고객 정보 조회 (키워드 추출용)
        Map<String, Object> q = new HashMap<>();
        q.put("CMPYCD", CMPYCD); q.put("CAMP_NO", CAMP_NO); q.put("CALL_SEQ", CALL_SEQ);
        List<Map<String, Object>> customers = outboundMapper.selectCampCallList(q);
        Map<String, Object> customer = (customers != null && !customers.isEmpty()) ? customers.get(0) : new HashMap<>();
        String CUST_TEL = (String) customer.get("TEL_NO");
        String CUST_EMAIL = (String) customer.get("EMAIL");

        String interactionId = "OUT_" + UUID.randomUUID().toString().substring(0, 8);

        // 2. 통합 AI 분석 및 요약 적용
        String fullPath = (REC_FILE != null && !REC_FILE.isEmpty()) ? "/var/spool/asterisk/monitor/" + REC_FILE : null;
        Map<String, String> aiResult = geminiAiService.analyze(CHAT_HISTORY, fullPath);
        
        String chatLog = aiResult.get("stt");
        String aiSummary = aiResult.get("summary");

        // 3. 상담 결과 마스터 저장 (DTO 필드명 매핑)
        CampRsltMstDto mstDto = CampRsltMstDto.builder()
                .cmpycd(CMPYCD).camp_no(CAMP_NO).call_seq(CALL_SEQ).rslt_cd(RSLT_CD)
                .interaction_id(interactionId).remark(REMARK).userid(USERID)
                .chat_log(chatLog).ai_summary(aiSummary).rec_file(REC_FILE)
                .start_dtime(startDtime).end_dtime(endDtime).updemp(USERID)
                .line_num(LINE_NUM)
                .build();
        outboundMapper.insertCampaignRsltMst(mstDto);

        // 4. 설문 답변 저장
        List<Map<String, Object>> surveys = (List<Map<String, Object>>) payload.get("SURVEYS");
        if (surveys != null) {
            for (Map<String, Object> s : surveys) {
                CampRsltDtlDto dtlDto = CampRsltDtlDto.builder()
                        .cmpycd(CMPYCD).rslt_no(mstDto.getRslt_no()).surv_no((String) s.get("SURV_NO"))
                        .ans_no((String) s.get("ANS_NO")).remark((String) s.get("REMARK"))
                        .updemp(USERID).build();
                outboundMapper.insertCampaignRsltDtl(dtlDto);
            }
        }

        // 5. 통합 히스토리 로그 저장
        Map<String, Object> interLog = new HashMap<>();
        interLog.put("INTERACTION_ID", interactionId);
        interLog.put("CMPYCD", CMPYCD);
        interLog.put("DIRECTION", "out");
        interLog.put("USERID", USERID);
        interLog.put("RESULT_CD", RSLT_CD);
        interLog.put("REC_FILE", REC_FILE);
        interLog.put("START_TIME", startDtime);
        interLog.put("END_TIME", endDtime);

        if ("chat".equalsIgnoreCase(MEDIA_TYPE)) {
            interLog.put("MEDIA_TYPE", "chat");
            interLog.put("KEYWORD", CUST_EMAIL);
            interLog.put("SRC_NO", "SYSTEM");
            interLog.put("DST_NO", CUST_EMAIL);
        } else {
            interLog.put("MEDIA_TYPE", "call");
            interLog.put("KEYWORD", CUST_TEL);
            interLog.put("SRC_NO", USERID);
            interLog.put("DST_NO", CUST_TEL);
        }
        outboundMapper.insertTotalInteractionLog(interLog);

        // 6. 발신 리스트 상태 업데이트
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("CMPYCD", CMPYCD);
        updateParams.put("CALL_SEQ", CALL_SEQ);
        updateParams.put("RSLT_CD", RSLT_CD);
        updateParams.put("STATUS", "030");
        outboundMapper.updateCallListStatus(updateParams);
    }
}
