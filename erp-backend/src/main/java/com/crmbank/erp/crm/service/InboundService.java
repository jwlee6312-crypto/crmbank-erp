package com.crmbank.erp.crm.service;

import lombok.RequiredArgsConstructor;
import com.crmbank.erp.crm.dto.CallMonitorDto;
import com.crmbank.erp.crm.dto.CallMstDto;
import com.crmbank.erp.crm.dto.CtiEscalationDto;
import com.crmbank.erp.crm.mapper.inbound.InboundMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InboundService {

    private final InboundMapper inboundMapper;

    private List<Map<String, Object>> toLowerCase(List<Map<String, Object>> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue, (v1, v2) -> v1)))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getCustomerByCustCd(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        params.put("custcd", custcd);
        return inboundMapper.findCustomerByCustCd(params);
    }

    @Transactional
    public String saveCallMst(CallMstDto dto, List<String> recordings, String svcymd) {
        dto.setCmpycd(dto.getCmpycd() != null ? dto.getCmpycd() : "haionnet");
        
        boolean isNew = false;
        if (dto.getSvcno() == null || dto.getSvcno().isEmpty()) {
            isNew = true;
            if (dto.getStart_time() != null) {
                svcymd = dto.getStart_time().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            dto.setSvcymd(svcymd);
            Map<String, Object> params = new HashMap<>();
            params.put("cmpycd", dto.getCmpycd());
            params.put("svcymd", svcymd);
            dto.setSvcno(inboundMapper.generateSvcNo(params));
        }

        if ("Y".equals(dto.getEscalation_yn()) && dto.getEsc_memo() != null && !dto.getEsc_memo().trim().isEmpty()) {
            CtiEscalationDto escDto = CtiEscalationDto.builder()
                    .svcno(dto.getSvcno())
                    .linkedid(dto.getLinkedid())
                    .sender_id(dto.getConsultid())
                    .deptcd(dto.getDeptcd())
                    .summary(dto.getAi_summary())
                    .esc_memo(dto.getEsc_memo())
                    .build();
            inboundMapper.insertEscalation(escDto);
        }

        if (isNew) inboundMapper.insertCallMst(dto);
        else inboundMapper.updateCallMst(dto);

        if (recordings != null && !recordings.isEmpty()) {
            Map<String, Object> delParams = new HashMap<>();
            delParams.put("cmpycd", dto.getCmpycd());
            delParams.put("svcno", dto.getSvcno());
            inboundMapper.deleteCallMonitor(delParams);

            for (int i = 0; i < recordings.size(); i++) {
                CallMonitorDto monitorDto = CallMonitorDto.builder()
                        .cmpycd(dto.getCmpycd())
                        .svcno(dto.getSvcno())
                        .row(String.valueOf(i + 1))
                        .moniternm(recordings.get(i))
                        .updemp(dto.getConsultid())
                        .build();
                inboundMapper.insertCallMonitor(monitorDto);
            }
        }
        return dto.getSvcno();
    }

    /**
     * 인바운드 상담현황 목록 조회
     */
    public List<Map<String, Object>> getStatusList(String cmpycd, String fromdt, String todt, String custnm) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        params.put("fromdt", fromdt);
        params.put("todt", todt);
        params.put("custnm", custnm);
        return toLowerCase(inboundMapper.selectStatusList(params));
    }

    @Transactional
    public void deleteCallMst(String cmpycd, String svcno) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        params.put("svcno", svcno);
        inboundMapper.deleteCallMonitor(params);
        inboundMapper.deleteCallMst(params);
    }

    public CallMstDto getCallMst(String cmpycd, String svcno) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        params.put("svcno", svcno);
        return inboundMapper.selectCallMst(params);
    }

    public List<Map<String, Object>> getItemList(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd != null ? cmpycd : "haionnet");
        params.put("custcd", custcd);
        return toLowerCase(inboundMapper.selectItemList(params));
    }

    public List<Map<String, Object>> getSettleHistory(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd != null ? cmpycd : "haionnet");
        params.put("custcd", custcd);
        return toLowerCase(inboundMapper.selectSettleHistory(params));
    }

    public List<Map<String, Object>> getCallHistory(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd != null ? cmpycd : "haionnet");
        params.put("custcd", custcd);
        return toLowerCase(inboundMapper.selectCallHistory(params));
    }

    public List<Map<String, Object>> getServiceHistory(String cmpycd, String custcd) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd != null ? cmpycd : "haionnet");
        params.put("custcd", custcd);
        return toLowerCase(inboundMapper.selectServiceHistory(params));
    }
}
