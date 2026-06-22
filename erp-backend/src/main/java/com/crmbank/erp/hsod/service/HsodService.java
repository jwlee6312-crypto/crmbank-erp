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
        // 🚀 [Position-First] 알리아스 무관하게 첫 번째/두 번째 컬럼값 추출
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ordym = String.valueOf(mstValues.get(0)).trim();
        String ordno = String.valueOf(mstValues.get(1)).trim();

        // Business Error Check for Master (첫 번째 컬럼이 '000000'이면 에러)
        if ("000000".equals(ordym)) {
            throw new Exception(ordno);
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
