package com.crmbank.erp.hppl.service;

import com.crmbank.erp.hppl.dto.PdPlanDto;
import com.crmbank.erp.hppl.mapper.PdPlanMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdPlanService {

    private final PdPlanMapper pdPlanMapper;

    public List<PdPlanDto> getPdPlanTargetList(Map<String, Object> params) {
        return pdPlanMapper.getPdPlanTargetList(params);
    }

    public List<PdPlanDto> getPdPlanList(Map<String, Object> params) {
        return pdPlanMapper.getPdPlanList(params);
    }

    public List<PdPlanDto> getPdPlanMonthlyList(Map<String, Object> params) {
        return pdPlanMapper.getPdPlanMonthlyList(params);
    }

    public List<Map<String, Object>> getPdPlanMonthlySummary(Map<String, Object> params) {
        return pdPlanMapper.getPdPlanMonthlySummary(params);
    }

    /**
     * 양산/외주 요청 자료 조회 (전체 내역)
     */
    public List<PdPlanDto> getPdRequestList(Map<String, Object> params) {
        return pdPlanMapper.getPdRequestList(params);
    }

    /**
     * 양산/외주 요청 자료 일괄 저장
     */
    @Transactional
    public void savePdRequestList(List<PdPlanDto> list, String cmpycd, String userId) {
        for (PdPlanDto dto : list) {
            dto.setCmpycd(cmpycd);
            
            // 🛡️ 문자열 잘림 방지 (Defensive check)
            if (userId != null && userId.length() > 20) userId = userId.substring(0, 20);
            dto.setUpdemp(userId);
            
            if (dto.getBigo() != null && dto.getBigo().length() > 1000) {
                dto.setBigo(dto.getBigo().substring(0, 1000));
            }
            
            String status = dto.get_status();
            if (status == null || status.isEmpty()) continue;

            // 날짜 가공
            if (dto.getOrdymd() != null) dto.setOrdymd(dto.getOrdymd().replace("-", ""));
            if (dto.getNapgiymd() != null) dto.setNapgiymd(dto.getNapgiymd().replace("-", ""));

            if ("입력".equals(status)) {
                pdPlanMapper.insertPdRequest(dto);
            } else if ("수정".equals(status)) {
                pdPlanMapper.updatePdRequest(dto);
            } else if ("삭제".equals(status)) {
                pdPlanMapper.deletePdRequest(dto);
            }
        }
    }

    /**
     * 주간생산계획 일괄 저장 (일반 자바 프로그램 방식)
     * _status 값에 따라 Insert, Update, Delete를 분기 처리
     */
    @Transactional
    public void savePdPlanList(List<PdPlanDto> list, String cmpycd, String userId) {
        // 🛡️ User ID 길이 제한
        String safeUserId = (userId != null && userId.length() > 20) ? userId.substring(0, 20) : userId;

        for (PdPlanDto dto : list) {
            dto.setCmpycd(cmpycd);
            dto.setUpdemp(safeUserId);

            // _status 또는 state 필드 확인 (하이브리드 지원)
            String status = dto.get_status();
            if (status == null || status.isEmpty()) {
                String state = dto.getState();
                if ("C".equals(state)) status = "입력";
                else if ("U".equals(state)) status = "수정";
                else if ("D".equals(state)) status = "삭제";
            }

            if (status == null || status.isEmpty()) continue;

            try {
                // 날짜 가공 (하이픈 제거)
                if (dto.getYymmdd() != null) dto.setYymmdd(dto.getYymmdd().replace("-", ""));
                if (dto.getNapgiymd() != null) dto.setNapgiymd(dto.getNapgiymd().replace("-", ""));

                if ("입력".equals(status)) {
                    pdPlanMapper.insertPdPlan(dto);
                    log.info("✅ 생산계획 등록 완료 (공정 자동 전계): {} - {}", dto.getYymmdd(), dto.getItemcd());
                } 
                else if ("수정".equals(status)) {
                    pdPlanMapper.updatePdPlan(dto);
                    log.info("✅ 생산계획 수정 완료: {} - {}", dto.getYymmdd(), dto.getSer());
                } 
                else if ("삭제".equals(status)) {
                    Map<String, Object> delParams = new HashMap<>();
                    delParams.put("cmpycd", cmpycd);
                    delParams.put("yymmdd", dto.getYymmdd());
                    delParams.put("ser", dto.getSer());
                    pdPlanMapper.deletePdPlan(delParams);
                    log.info("✅ 생산계획 삭제: {} - {}", dto.getYymmdd(), dto.getSer());
                }
            } catch (Exception e) {
                log.error("❌ 생산계획 처리 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("생산계획 저장 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }

    @Transactional
    public void deletePdPlan(Map<String, Object> params) {
        pdPlanMapper.deletePdPlan(params);
    }

    /**
     * 월간 생산계획 일자 이동 (자바 방식: 삭제 후 재등록)
     */
    @Transactional
    public void movePdPlanDate(Map<String, Object> params) {
        // 1. 기존 계획 조회 (oldyymmdd, oldser 사용)
        PdPlanDto oldPlan = pdPlanMapper.selectPdPlan(params);
        if (oldPlan == null) throw new RuntimeException("이동할 계획 데이터를 찾을 수 없습니다.");
        
        // 🚀 [보안] 서버측에서도 작업지시 여부 재검증
        if (oldPlan.getOrdqty() != null && oldPlan.getOrdqty().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new RuntimeException("이미 작업지시가 진행된 계획은 일자를 이동할 수 없습니다.");
        }

        // 2. 기존 계획 삭제 (cmpycd, yymmdd, ser 명시적 매핑)
        Map<String, Object> delParams = new HashMap<>();
        delParams.put("cmpycd", params.get("cmpycd"));
        delParams.put("yymmdd", params.get("oldyymmdd"));
        delParams.put("ser", params.get("oldser"));
        pdPlanMapper.deletePdPlan(delParams);

        // 3. 신규 순번 채번 및 재등록
        Map<String, Object> serParams = new HashMap<>();
        serParams.put("cmpycd", oldPlan.getCmpycd());
        serParams.put("yymmdd", params.get("newyymmdd").toString());
        String newSer = pdPlanMapper.selectNextSer(serParams);

        oldPlan.setYymmdd(params.get("newyymmdd").toString());
        oldPlan.setSer(newSer);
        
        String updemp = params.get("updemp").toString();
        if (updemp.length() > 20) updemp = updemp.substring(0, 20);
        oldPlan.setUpdemp(updemp);
        
        // 🚀 [해결] 자동 전계(insertPdPlan)가 아닌 단건 저장(insertPdPlanSingle) 호출
        pdPlanMapper.insertPdPlanSingle(oldPlan);
        log.info("🚚 계획 일자 이동 완료: {} -> {}", params.get("oldyymmdd"), params.get("newyymmdd"));
    }
}
