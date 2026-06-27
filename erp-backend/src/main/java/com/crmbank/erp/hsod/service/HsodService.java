package com.crmbank.erp.hsod.service;

import com.crmbank.erp.hsod.mapper.HsodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import com.crmbank.erp.hsod.dto.Hsod100uRequest;
import com.crmbank.erp.hsod.dto.Hsod101u;

@Service
@RequiredArgsConstructor
public class HsodService {

    private final HsodMapper hsodMapper;

    /**
     * 🚀 Integrated Order Save
     * Stored procedure must return 'ordym' and 'ordno'.
     * Error code '000000' in 'ordym' indicates a business error.
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveOrder(Hsod100uRequest request, String userId) throws Exception {
        // 1. Master Save
        List<Map<String, Object>> res = hsodMapper.HSOD_100U_STR(request.getMst());
        if (res == null || res.isEmpty()) {
            throw new Exception("Master Save: No response from database.");
        }

        Map<String, Object> mstRow = res.get(0);

        // 🚀 순수하게 알리아스(Key)로만 수신
        String ordym = mstRow.get("ordym") != null ? String.valueOf(mstRow.get("ordym")).trim() : "";
        String ordno = mstRow.get("ordno") != null ? String.valueOf(mstRow.get("ordno")).trim() : "";

        // 비즈니스 에러 체크 (000000)
        if ("000000".equals(ordym)) {
            throw new Exception(ordno.isEmpty() ? "마스터 저장 오류" : ordno);
        }

        // 채번 누락 체크
        if (ordym.isEmpty() || ordno.isEmpty()) {
            throw new Exception("정산 채번 실패: 알리아스(ordym, ordno)를 확인할 수 없습니다.");
        }

        // 2. Detail Save
        if (request.getDtl() != null) {
            for (Hsod101u dtl : request.getDtl()) {
                dtl.setOrdym(ordym);
                dtl.setOrdno(ordno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setOrdymd(request.getMst().getOrdymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);
                
                if (dtl.getOrowno() == null || "0".equals(dtl.getOrowno())) {
                    dtl.setOrowno("");
                }

                List<Map<String, Object>> dtlRes = hsodMapper.HSOD_101U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    String dStatus = String.valueOf(dtlValues.get(0)).trim();
                    String dMessage = String.valueOf(dtlValues.get(1)).trim();
                    
                    if ("000000".equals(dStatus)) {
                        throw new Error(dMessage); // Business error from DB
                    }
                }
            }
        }
        return mstRow;
    }
}
