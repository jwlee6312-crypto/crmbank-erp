package com.crmbank.erp.hsip.service;

import com.crmbank.erp.hsip.mapper.HsipMapper;
import com.crmbank.erp.hsip.dto.Hsip100uRequest;
import com.crmbank.erp.hsip.dto.Hsip101u;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsipService {

    private final HsipMapper hsipMapper;

    /**
     * 🚀 수입발주 통합 저장 (Master + Details)
     * HSOD100U와 동일한 패턴: 채번된 fileno를 수신하여 상세에 동기화
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveImportOrder(Hsip100uRequest request, String userId) throws Exception {
        // 1. 마스터 저장 (A0: 추가 또는 U0: 수정)
        // actkind가 null이거나 없으면 A0로 처리하는 등의 로직은 컨트롤러나 프론트에서 넘어온다고 가정
        List<Map<String, Object>> res = hsipMapper.HSIP_100U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        // 채번된 번호 확인 (프로시저에서 FILENO 컬럼으로 반환한다고 가정)
        String fileno = mstRow.get("fileno") != null ? String.valueOf(mstRow.get("fileno")).trim() : "";
        
        // 💡 업무 에러 체크 ('000000' 이면 fileno에 담긴 에러메시지로 즉시 중단)
        if ("000000".equals(fileno)) {
            String errorMsg = mstRow.get("error_msg") != null ? String.valueOf(mstRow.get("error_msg")) : "Business Error";
            throw new Exception(errorMsg);
        }

        // 2. 상세 내역 저장
        if (request.getDtl() != null) {
            // 상세 저장 전 기존 내역 삭제 (D0: 삭제) - 주문등록 패턴에 따라 선택적용 가능하나
            // 보통 상세는 전체 삭제 후 재입력 또는 개별 처리함. 여기서는 개별 처리 패턴 유지.
            for (Hsip101u dtl : request.getDtl()) {
                dtl.setFileno(fileno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setUpdemp(userId);
                
                // 신규 채번 유도 (prowno가 '0'이거나 없으면 신규)
                if (dtl.getProwno() == null || "0".equals(dtl.getProwno())) dtl.setProwno("");

                List<Map<String, Object>> dtlRes = hsipMapper.HSIP_101U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    String dStatus = dtlRow.get("fileno") != null ? String.valueOf(dtlRow.get("fileno")).trim() : "";
                    if ("000000".equals(dStatus)) {
                        throw new Exception("Detail Save Error: " + fileno);
                    }
                }
            }
        }
        return mstRow;
    }
}
