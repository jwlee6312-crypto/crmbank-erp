package com.crmbank.erp.hsip.service;

import com.crmbank.erp.hsio.dto.Hsio100u;
import com.crmbank.erp.hsio.dto.Hsio101u;
import com.crmbank.erp.hsio.mapper.HsioMapper;
import com.crmbank.erp.hsip.dto.Hsip120u;
import com.crmbank.erp.hsip.dto.Hsip121u;
import com.crmbank.erp.hsip.dto.Hsip120uSaveRequest;
import com.crmbank.erp.hsip.mapper.HsipMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsipService {

    private final HsipMapper hsipMapper;
    private final HsioMapper hsioMapper;

    /**
     * 🚀 통관 및 입고 통합 저장 (Service 레이어에서 Transaction 보장)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveCustomsStock(Hsip120uSaveRequest request, String userId) throws Exception {
        Hsip120u mst = request.getMst();
        List<Hsip121u> dtlList = request.getDtl();

        if (mst == null) throw new Exception("마스터 데이터가 누락되었습니다.");

        // 헬퍼 함수: 공백 제거
        java.util.function.Function<String, String> clean = (val) -> val == null ? "" : val.trim();

        // 1. 입고 마스터 판별 및 저장 (HSIO_100U_STR)
        String mstIono = clean.apply(mst.getIono());
        String mstActKind = mstIono.isEmpty() ? "A" : "U";
        
        // HSIO 전용 DTO 구성 및 매핑
        Hsio100u hsioMst = new Hsio100u();
        hsioMst.setActkind(mstActKind);
        hsioMst.setCmpycd(clean.apply(mst.getCmpycd()));
        hsioMst.setIogbn("100");
        hsioMst.setIoym(clean.apply(mst.getIoym()));
        hsioMst.setIono(mstIono);
        hsioMst.setIoymd(clean.apply(mst.getPassymd()));
        hsioMst.setIotype("100");
        hsioMst.setCustcd(clean.apply(mst.getCustcd()));
        hsioMst.setDeptcd(clean.apply(mst.getDeptcd()));
        hsioMst.setWhcd(clean.apply(mst.getWhcd()));
        hsioMst.setCfmyn("Y");
        hsioMst.setUpdemp(clean.apply(userId));
        hsioMst.setRemark(clean.apply(mst.getCustnm()) + " 수입 통관 입고");
        hsioMst.setAddress("");
        hsioMst.setGubun("");
        hsioMst.setTotsum(String.valueOf(BigDecimal.ZERO));

        log.info("[HSIP120U] Step 1: Stock Master Save - actkind: {}, iono: {}", mstActKind, mstIono);
        List<Map<String, Object>> resM = hsioMapper.HSIO_100U_STR(hsioMst);
        if (resM == null || resM.isEmpty()) throw new Exception("입고 마스터 저장 결과가 없습니다.");

        Map<String, Object> mstResult = resM.get(0);
        List<Object> mstValues = new ArrayList<>(mstResult.values());
        String keyIoym = String.valueOf(mstValues.get(0)).trim();
        String keyIono = String.valueOf(mstValues.get(1)).trim();

        if ("000000".equals(keyIoym)) {
            throw new Exception("입고 마스터 저장 실패: " + keyIono);
        }

        if (keyIono.isEmpty()) throw new Exception("입고번호(iono)를 확인할 수 없습니다.");

        // 2. 통관 마스터 저장 (HSIP_120U_STR) - A0, U0 형태
        mst.setActkind(mstActKind + "0");
        mst.setIono(keyIono);
        mst.setIoym(keyIoym);
        mst.setUpdemp(userId);
        
        log.info("[HSIP120U] Step 2: Customs Master Save - actkind: {}, iono: {}", mst.getActkind(), keyIono);
        List<Map<String, Object>> resPassM = hsipMapper.HSIP_120U_STR(mst);
        if (resPassM != null && !resPassM.isEmpty()) {
            List<Object> pValues = new ArrayList<>(resPassM.get(0).values());
            String pRes = String.valueOf(pValues.get(0)).trim();
            if ("000000".equals(pRes)) {
                throw new Exception("통관 마스터 저장 실패: " + String.valueOf(pValues.get(1)));
            }
        }

        // 3. 품목별 상세 저장 루프
        if (dtlList != null) {
            log.info("[HSIP120U] Step 3: Saving {} Details", dtlList.size());
            for (Hsip121u item : dtlList) {
                String iorowno = clean.apply(item.getIorowno());
                
                BigDecimal iqty = item.getIqty() != null ? item.getIqty() : BigDecimal.ZERO;
                BigDecimal gqty = item.getGqty() != null ? item.getGqty() : BigDecimal.ZERO;
                BigDecimal amt = item.getAmt() != null ? item.getAmt() : BigDecimal.ZERO;
                BigDecimal totalQty = iqty.add(gqty);

                // 🔍 HSIO_101U_STR actkind 결정: 기존 행이 있고 수량이 0이면 'D' (삭제)
                String itemActKind_101 = iorowno.isEmpty() ? "A" : (totalQty.compareTo(BigDecimal.ZERO) == 0 ? "D" : "U");
                // 🔍 HSIP_121U_STR actkind 결정: 기존 행이면 'U0', 신규면 'A0'
                String itemActKind_121 = iorowno.isEmpty() ? "A0" : "U0";

                // a. 입고 상세 저장 (HSIO_101U_STR)
                Hsio101u hsioDtl = new Hsio101u();
                hsioDtl.setActkind(itemActKind_101);
                hsioDtl.setCmpycd(clean.apply(mst.getCmpycd()));
                hsioDtl.setIogbn("100");
                hsioDtl.setIoym(clean.apply(keyIoym));
                hsioDtl.setIono(clean.apply(keyIono));
                hsioDtl.setIorowno(iorowno);
                hsioDtl.setIoymd(clean.apply(mst.getPassymd()));
                hsioDtl.setCustcd(clean.apply(mst.getCustcd()));
                hsioDtl.setIotype("100");
                hsioDtl.setDeptcd(clean.apply(mst.getDeptcd()));
                hsioDtl.setWhcd(clean.apply(mst.getWhcd()));
                hsioDtl.setItemcd(clean.apply(item.getItemcd()));
                hsioDtl.setItsize(clean.apply(item.getItsize()));
                hsioDtl.setUnit(clean.apply(item.getUnit()));
                hsioDtl.setPkunit(clean.apply(item.getUnit()));
                hsioDtl.setIoqty(iqty);
                hsioDtl.setIoamt(item.getAmt() != null ? item.getAmt() : BigDecimal.ZERO);
                hsioDtl.setIovat(BigDecimal.ZERO);
                hsioDtl.setCfmyn("Y");
                hsioDtl.setScustcd("");
                hsioDtl.setScustnm("");
                hsioDtl.setUpdemp(clean.apply(userId));

                List<Map<String, Object>> resD = hsioMapper.HSIO_101U_STR(hsioDtl);
                if (resD == null || resD.isEmpty()) throw new Exception("입고 상세 저장 결과가 없습니다.");
                
                List<Object> dValues = new ArrayList<>(resD.get(0).values());
                String dRes = String.valueOf(dValues.get(0)).trim();
                if ("000000".equals(dRes)) {
                    throw new Exception("품목 상세 저장 실패: " + String.valueOf(dValues.get(1)));
                }

                String savedIorowno = String.valueOf(dValues.get(2)).trim();
                if (savedIorowno.isEmpty()) savedIorowno = iorowno;

                // b. 통관 상세 저장 (HSIP_121U_STR)
                item.setActkind(itemActKind_121);
                item.setCmpycd(clean.apply(mst.getCmpycd()));
                item.setFileno(clean.apply(mst.getFileno()));
                item.setShipseq(clean.apply(mst.getShipseq()));
                item.setPassseq(clean.apply(mst.getPassseq()));
                item.setProwno(clean.apply(item.getProwno())); // 🚀 전달받은 prowno(srowno) 유지
                item.setItemcd(clean.apply(item.getItemcd()));
                item.setItsize(clean.apply(item.getItsize()));
                item.setUnit(clean.apply(item.getUnit()));
                item.setIoym(clean.apply(keyIoym));
                item.setIono(clean.apply(keyIono));
                item.setIorowno(savedIorowno);
                item.setQty(iqty);
                item.setGqty(gqty);
                item.setAmt(amt);
                item.setUpdemp(clean.apply(userId));

                hsipMapper.HSIP_121U_STR(item);
            }
        }
        return mstResult;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveImportOrder(com.crmbank.erp.hsip.dto.Hsip100uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsipMapper.HSIP_100U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String fileno = String.valueOf(mstValues.get(0)).trim();
        
        if ("000000".equals(fileno)) {
            throw new Exception(String.valueOf(mstValues.get(1)));
        }

        if (request.getDtl() != null) {
            for (com.crmbank.erp.hsip.dto.Hsip101u dtl : request.getDtl()) {
                dtl.setFileno(fileno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setUpdemp(userId);
                if (dtl.getProwno() == null || "0".equals(dtl.getProwno())) dtl.setProwno("");
                
                List<Map<String, Object>> dtlRes = hsipMapper.HSIP_101U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
                    }
                }
            }
        }
        return mstRow;
    }
}
