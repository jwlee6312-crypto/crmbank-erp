package com.crmbank.erp.hppl.service;

import com.crmbank.erp.hppl.dto.PdOrderDto;
import com.crmbank.erp.hppl.mapper.PdOrderMapper;
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
public class PdOrderService {

    private final PdOrderMapper pdOrderMapper;

    public List<PdOrderDto> getPdOrderTargetList(Map<String, Object> params) {
        return pdOrderMapper.getPdOrderTargetList(params);
    }

    public List<PdOrderDto> getPdOrderList(Map<String, Object> params) {
        return pdOrderMapper.getPdOrderList(params);
    }

    /**
     * 작업지시 일괄 저장 (일반 자바 프로그램 방식)
     * _status 값에 따라 분기 처리
     */
    @Transactional
    public void savePdOrderList(List<PdOrderDto> list, String cmpycd, String userId) {
        for (PdOrderDto dto : list) {
            dto.setCmpycd(cmpycd);
            dto.setUpdemp(userId);

            String status = dto.get_status();
            if (status == null || status.isEmpty()) continue;

            try {
                if ("입력".equals(status)) {
                    // 1. 신규 LOT 번호 채번
                    Map<String, Object> lotParams = new HashMap<>();
                    lotParams.put("cmpycd", cmpycd);
                    lotParams.put("linecd", dto.getLinecd());
                    lotParams.put("itemcd", dto.getItemcd());
                    lotParams.put("ordymd", dto.getOrdymd());
                    
                    String newLotNo = pdOrderMapper.selectNewLotNo(lotParams);
                    dto.setLotno(newLotLotMapping(newLotNo, dto.getOrdymd()));

                    // 2. 마스터 및 상세 저장
                    pdOrderMapper.insertPdLotNo(dto);
                    pdOrderMapper.insertPdOrder(dto);
                    
                    // 3. 생산계획 테이블의 지시수량 업데이트 (기존 로직 유지)
                    updatePlanOrdQty(dto, dto.getOrdqty());
                    
                    log.info("✅ 작업지시 신규 등록: {} - LOT: {}", dto.getItemcd(), dto.getLotno());
                } 
                else if ("수정".equals(status)) {
                    // 기존 수량 차이만큼 계획 테이블 보정 후 업데이트
                    // (상세한 수량 보정 로직은 필요 시 추가 구현 가능)
                    pdOrderMapper.updatePdLotNo(dto);
                    pdOrderMapper.updatePdOrder(dto);
                    log.info("✅ 작업지시 수정 완료: {}", dto.getLotno());
                } 
                else if ("삭제".equals(status)) {
                    // 계획 테이블 지시수량 원복 후 삭제
                    updatePlanOrdQty(dto, dto.getOrdqty().negate());
                    pdOrderMapper.deletePdLotNo(dto);
                    pdOrderMapper.deletePdOrder(dto);
                    log.info("✅ 작업지시 삭제: {}", dto.getLotno());
                }
            } catch (Exception e) {
                log.error("❌ 작업지시 처리 오류: {}", e.getMessage());
                throw new RuntimeException("작업지시 저장 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }

    private void updatePlanOrdQty(PdOrderDto dto, java.math.BigDecimal qty) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", dto.getCmpycd());
        params.put("yymmdd", dto.getYymmdd());
        params.put("ser", dto.getSer());
        params.put("itemcd", dto.getItemcd());
        params.put("qty", qty);
        pdOrderMapper.updatePlanOrdQty(params);
    }

    // LOT 번호가 NULL로 올 경우를 대비한 가공 로직
    private String newLotLotMapping(String lotNo, String ymd) {
        if (lotNo == null || lotNo.trim().isEmpty()) {
            return "L" + ymd + "0001";
        }
        return lotNo;
    }
}
