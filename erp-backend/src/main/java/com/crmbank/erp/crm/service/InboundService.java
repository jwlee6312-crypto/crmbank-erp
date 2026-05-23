package com.crmbank.erp.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.dto.*;
import com.crmbank.erp.crm.mapper.inbound.InboundMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InboundService {

    private final InboundMapper inboundMapper;

    /**
     * 💡 MyBatis 결과를 소문자 Key로 변환 (프론트엔드 호환성)
     */
    private List<Map<String, Object>> toLowerCase(List<Map<String, Object>> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (list == null) return result;

        for (Map<String, Object> map : list) {
            if (map == null) continue;
            Map<String, Object> lowerMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    lowerMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
            result.add(lowerMap);
        }
        return result;
    }

    public Map<String, Object> getCustomerByCustCd(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("CMPYCD", cmpycd);
        params.put("CUSTCD", custcd);
        return inboundMapper.findCustomerByCustCd(params);
    }

    /**
     * 💡 인바운드 상담 통합 저장 (MSSQL 대응 및 수동 입력 지원)
     * @param agentDeptCd 로그인한 상담원의 부서코드
     */
    @Transactional
    public String saveCallMst(CallMstDto dto, List<String> recordings, String svcymd, String agentDeptCd) {
        // 💡 대문자 필드 원칙에 따른 메서드 호출
        dto.setCMPYCD(dto.getCMPYCD() != null ? dto.getCMPYCD() : "HAIONNET");
        dto.setHAPPYCALL_YN("N"); 
        
        if (dto.getCALL_TELNO() != null) dto.setCALL_TELNO(dto.getCALL_TELNO().trim());
        if (dto.getCALL_USERNM() != null) dto.setCALL_USERNM(dto.getCALL_USERNM().trim());
        if (dto.getCALL_EMAIL() != null) dto.setCALL_EMAIL(dto.getCALL_EMAIL().trim());
        if (dto.getIONO() != null) dto.setIONO(dto.getIONO().trim());

        if (dto.getSTART_TIME() == null) dto.setSTART_TIME(LocalDateTime.now().minusMinutes(5));
        if (dto.getEND_TIME() == null) dto.setEND_TIME(LocalDateTime.now());

        String targetDeptCd = dto.getDEPTCD();
        dto.setDEPTCD(agentDeptCd);

        // 1. 접수번호 채번
        boolean isNew = false;
        if (dto.getSVCNO() == null || dto.getSVCNO().trim().isEmpty()) {
            isNew = true;
            dto.setSVCYMD(svcymd);
            Map<String, Object> params = new HashMap<>();
            params.put("CMPYCD", dto.getCMPYCD());
            params.put("SVCYMD", svcymd);
            dto.setSVCNO(inboundMapper.generateSvcNo(params));
        }

        // 2. 상담 마스터 저장
        if (isNew) {
            inboundMapper.insertCallMst(dto);
        } else {
            inboundMapper.updateCallMst(dto);
        }

        // 3. 타 부서 이관 처리
        if ("Y".equals(dto.getESCALATION_YN()) && dto.getESC_MEMO() != null && !dto.getESC_MEMO().trim().isEmpty()) {
            CtiEscalationDto escDto = CtiEscalationDto.builder()
                    .SVCNO(dto.getSVCNO())
                    .linkedid(dto.getLINKEDID())
                    .SENDER_ID(dto.getCONSULTID())
                    .DEPTCD(targetDeptCd)
                    .SUMMARY(dto.getAI_SUMMARY())
                    .ESC_MEMO(dto.getESC_MEMO())
                    .build();
            
            inboundMapper.insertEscalation(escDto);

            if (escDto.getESCALATION_NO() != null) {
                dto.setESCALATION_NO(escDto.getESCALATION_NO().toString());
                inboundMapper.updateCallMst(dto);
            }
        }

        // 4. 녹취 파일 정보 저장
        if (recordings != null && !recordings.isEmpty()) {
            Map<String, Object> delParams = new HashMap<>();
            delParams.put("CMPYCD", dto.getCMPYCD());
            delParams.put("SVCNO", dto.getSVCNO());
            inboundMapper.deleteCallMonitor(delParams);

            for (int i = 0; i < recordings.size(); i++) {
                inboundMapper.insertCallMonitor(CallMonitorDto.builder()
                        .CMPYCD(dto.getCMPYCD())
                        .SVCNO(dto.getSVCNO())
                        .ROW(String.format("%03d", i + 1))
                        .MONITERNM(recordings.get(i))
                        .UPDEMP(dto.getCONSULTID())
                        .build());
            }
        }

        // 5. 통합 인터랙션 로그
        String interactionId = dto.getINTERACTION_ID();
        if (interactionId == null || interactionId.isEmpty()) {
            interactionId = "MANUAL_" + UUID.randomUUID().toString().substring(0, 8);
            dto.setINTERACTION_ID(interactionId);
        }

        String srcNo = dto.getCALL_TELNO() != null && !dto.getCALL_TELNO().trim().isEmpty() ? dto.getCALL_TELNO() : dto.getCUSTCD();

        TotalCallLogDto logDto = TotalCallLogDto.builder()
                .uniqueid(interactionId)
                .CMPYCD(dto.getCMPYCD())
                .MEDIA_TYPE("CALL")
                .linkedid(dto.getLINKEDID())
                .DIRECTION("in")
                .SRC_NO(srcNo)
                .DST_NO(dto.getCONSULTID())
                .KEYWORD(srcNo)
                .START_TIME(dto.getSTART_TIME())
                .RESULT_CD("100")
                .REC_FILE(dto.getREC_FILE())
                .build();
        
        inboundMapper.insertTotalInteractionLog(logDto);

        return dto.getSVCNO();
    }

    /**
     * 💡 ARS 콜백 로그 저장
     */
    @Transactional
    public void insertTotalInteractionLog(TotalCallLogDto logDto) {
        inboundMapper.insertTotalInteractionLog(logDto);
    }

    public List<Map<String, Object>> getStatusList(String cmpycd, String fromdt, String todt, String custnm) {
        Map<String, Object> params = new HashMap<>();
        params.put("CMPYCD", cmpycd);
        params.put("FROMDT", fromdt);
        params.put("TODT", todt);
        params.put("custnm", custnm);
        return toLowerCase(inboundMapper.selectStatusList(params));
    }

    public List<Map<String, Object>> getItemList(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("CMPYCD", cmpycd);
        params.put("CUSTCD", custcd);
        return toLowerCase(inboundMapper.selectItemList(params));
    }

    public List<Map<String, Object>> getCallHistory(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("CMPYCD", cmpycd);
        params.put("CUSTCD", custcd);
        return toLowerCase(inboundMapper.selectCallHistory(params));
    }

    public List<Map<String, Object>> getServiceHistory(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("CMPYCD", cmpycd);
        params.put("CUSTCD", custcd);
        return toLowerCase(inboundMapper.selectServiceHistory(params));
    }

    public List<Map<String, Object>> getSettleHistory(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("CMPYCD", cmpycd);
        params.put("CUSTCD", custcd);
        return toLowerCase(inboundMapper.selectSettleHistory(params));
    }
}
