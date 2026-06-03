package com.crmbank.erp.hsio.service;

import com.crmbank.erp.hsio.mapper.HsioMapper;
import com.crmbank.erp.hsio.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsioService {

    private final HsioMapper hsioMapper;

    /**
     * 🚀 요청등록 통합 저장 (HSIO_010U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveRequest(Hsio010uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_010U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String reqym = mstRow.get("reqym") != null ? String.valueOf(mstRow.get("reqym")).trim() : "";
        String reqno = mstRow.get("reqno") != null ? String.valueOf(mstRow.get("reqno")).trim() : "";

        if ("000000".equals(reqym)) throw new Exception(reqno);

        if (request.getDtl() != null) {
            for (Hsio011u dtl : request.getDtl()) {
                dtl.setReqym(reqym);
                dtl.setReqno(reqno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setReqymd(request.getMst().getReqymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setUpdemp(userId);
                
                if (dtl.getRrowno() == null || "0".equals(dtl.getRrowno())) dtl.setRrowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_011U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("reqym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("reqno")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 발주등록 통합 저장 (HSIO_050U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveOrder(Hsio050uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_050U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String balym = mstRow.get("balym") != null ? String.valueOf(mstRow.get("balym")).trim() : "";
        String balno = mstRow.get("balno") != null ? String.valueOf(mstRow.get("balno")).trim() : "";

        if ("000000".equals(balym)) throw new Exception(balno);

        if (request.getDtl() != null) {
            for (Hsio051u dtl : request.getDtl()) {
                dtl.setBalym(balym);
                dtl.setBalno(balno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setBalymd(request.getMst().getBalymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);
                
                if (dtl.getBrowno() == null || "0".equals(dtl.getBrowno())) dtl.setBrowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_051U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("balym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("balno")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 일반발주 통합 저장 (HSIO_0520U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveGeneralOrder(Hsio052uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_052U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String balym = mstRow.get("balym") != null ? String.valueOf(mstRow.get("balym")).trim() : "";
        String balno = mstRow.get("balno") != null ? String.valueOf(mstRow.get("balno")).trim() : "";

        if ("000000".equals(balym)) throw new Exception(balno);

        if (request.getDtl() != null) {
            for (Hsio051u dtl : request.getDtl()) {
                dtl.setBalym(balym);
                dtl.setBalno(balno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setBalymd(request.getMst().getBalymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);

                if (dtl.getBrowno() == null || "0".equals(dtl.getBrowno())) dtl.setBrowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_051U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("balym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("balno")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 기타입고 통합 저장 (HSIO_190U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveOtherIn(Hsio190uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_190U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String ioym = mstRow.get("ioym") != null ? String.valueOf(mstRow.get("ioym")).trim() : "";
        String iono = mstRow.get("iono") != null ? String.valueOf(mstRow.get("iono")).trim() : "";

        if ("000000".equals(ioym)) throw new Exception(iono);

        if (request.getDtl() != null) {
            for (Hsio191u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIono(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setIotype(request.getMst().getIotype());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);

                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_191U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("ioym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("iono")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 기타출고 통합 저장 (HSIO_250U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveOtherOut(Hsio250uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_250U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String ioym = mstRow.get("ioym") != null ? String.valueOf(mstRow.get("ioym")).trim() : "";
        String iono = mstRow.get("iono") != null ? String.valueOf(mstRow.get("iono")).trim() : "";

        if ("000000".equals(ioym)) throw new Exception(iono);

        if (request.getDtl() != null) {
            for (Hsio251u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIono(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setIotype(request.getMst().getIotype());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);

                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_251U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("ioym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("iono")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 입금입력 통합 저장 (HSIO_300U_SAVE)
     * 마스터(A0,U0,D0) + 상세(A1,U1,D1) 패턴. Hsio300u DTO를 마스터/상세 공용 사용.
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveDeposit(Hsio300uRequest request, String userId) throws Exception {
        // 1. 마스터 저장 (A0, U0, D0)
        Hsio300u mst = request.getMst();
        List<Map<String, Object>> res = hsioMapper.HSIO_300U_STR(mst);
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String imym = mstRow.get("imym") != null ? String.valueOf(mstRow.get("imym")).trim() : "";
        String imno = mstRow.get("imno") != null ? String.valueOf(mstRow.get("imno")).trim() : "";

        if ("000000".equals(imym)) throw new Exception(imno);

        // 2. 상세 내역 저장 (A1, U1, D1)
        if (request.getDtl() != null) {
            for (Hsio300u dtl : request.getDtl()) {
                dtl.setImym(imym);
                dtl.setImno(imno);
                dtl.setCmpycd(mst.getCmpycd());
                dtl.setDeptcd(mst.getDeptcd());
                dtl.setCustcd(mst.getCustcd());
                dtl.setImymd(mst.getImymd());
                dtl.setUpdemp(userId);

                // 상태에 따른 액션코드 매핑 (Vue에서 넘어온 'C','U','D'를 'A1','U1','D1'로)
                String state = dtl.getActkind();
                if ("C".equals(state)) dtl.setActkind("A1");
                else if ("U".equals(state)) dtl.setActkind("U1");
                else if ("D".equals(state)) dtl.setActkind("D1");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_300U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("imym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("imno")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 매입등록 통합 저장 (HSIO_500U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> savePurchase(Hsio500uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_500U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String ioym = mstRow.get("ioym") != null ? String.valueOf(mstRow.get("ioym")).trim() : "";
        String iono = mstRow.get("iono") != null ? String.valueOf(mstRow.get("iono")).trim() : "";

        if ("000000".equals(ioym)) throw new Exception(iono);

        if (request.getDtl() != null) {
            for (Hsio501u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIono(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);
                
                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_501U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("ioym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("iono")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 타창고이동 통합 저장 (HSIO_580U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveWarehouseTransfer(Hsio580uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_580U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String ioym = mstRow.get("ioym") != null ? String.valueOf(mstRow.get("ioym")).trim() : "";
        String iono = mstRow.get("iono") != null ? String.valueOf(mstRow.get("iono")).trim() : ""; // 출고번호
        String ino = mstRow.get("ino") != null ? String.valueOf(mstRow.get("ino")).trim() : "";   // 입고번호

        if ("000000".equals(ioym)) throw new Exception(iono);

        if (request.getDtl() != null) {
            for (Hsio581u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setOno(iono);
                if (!ino.isEmpty()) dtl.setIno(ino);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setOdeptcd(request.getMst().getOdeptcd());
                dtl.setOwhcd(request.getMst().getOwhcd());
                dtl.setIdeptcd(request.getMst().getIdeptcd());
                dtl.setIwhcd(request.getMst().getIwhcd());
                dtl.setUpdemp(userId);

                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_581U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("ioym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("iono")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 재고실사 통합 저장 (HSIO_720U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveStockAdjustment(Hsio720uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_720U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String ioym = mstRow.get("ioym") != null ? String.valueOf(mstRow.get("ioym")).trim() : "";
        String iono = mstRow.get("iono") != null ? String.valueOf(mstRow.get("iono")).trim() : "";

        if ("000000".equals(ioym)) throw new Exception(iono);

        if (request.getDtl() != null) {
            for (Hsio721u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIno(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setUpdemp(userId);

                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_721U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("ioym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("iono")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 재고조정 통합 저장 (HSIO_730U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveInventoryAdjustment(Hsio730Request request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_730U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String ioym = mstRow.get("ioym") != null ? String.valueOf(mstRow.get("ioym")).trim() : "";
        String iono = mstRow.get("iono") != null ? String.valueOf(mstRow.get("iono")).trim() : "";

        if ("000000".equals(ioym)) throw new Exception(iono);

        if (request.getDtl() != null) {
            for (Hsio731u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIno(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setUpdemp(userId);

                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_731U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("ioym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("iono")));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 전시장 입출고 통합 저장 (HSIO_570U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveStoreInout(Hsio570uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_570U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        String ioym = mstRow.get("ioym") != null ? String.valueOf(mstRow.get("ioym")).trim() : "";
        String iono = mstRow.get("iono") != null ? String.valueOf(mstRow.get("iono")).trim() : "";

        if ("000000".equals(ioym)) throw new Exception(iono);

        if (request.getDtl() != null) {
            for (Hsio571u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIono(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setIotype(request.getMst().getIotype());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUserid(request.getMst().getUserid());
                dtl.setUpdemp(userId);

                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_571U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    if ("000000".equals(String.valueOf(dtlRow.get("ioym")).trim())) {
                        throw new Exception(String.valueOf(dtlRow.get("iono")));
                    }
                }
            }
        }
        return mstRow;
    }
}
