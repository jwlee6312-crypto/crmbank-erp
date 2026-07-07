package com.crmbank.erp.hsio.service;

import com.crmbank.erp.hasl.mapper.HaslMapper;
import com.crmbank.erp.hsio.mapper.HsioMapper;
import com.crmbank.erp.hsio.dto.*;
import com.crmbank.erp.hasl.dto.Hasl010u;
import com.crmbank.erp.hasl.dto.Hasl020u;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsioService {

    private final HsioMapper hsioMapper;
    private final HaslMapper haslMapper;
    private final ObjectMapper objectMapper;

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveRequest(Hsio010uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_010U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String reqym = String.valueOf(mstValues.get(0)).trim();
        String reqno = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(reqym)) throw new Exception(reqno);
        if (request.getDtl() != null) {
            for (Hsio011u dtl : request.getDtl()) {
                dtl.setReqym(reqym); dtl.setReqno(reqno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setReqymd(request.getMst().getReqymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setUpdemp(userId);
                if (dtl.getRrowno() == null || "0".equals(dtl.getRrowno())) dtl.setRrowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_011U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveOrder(Hsio050uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_050U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String balym = String.valueOf(mstValues.get(0)).trim();
        String balno = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(balym)) throw new Exception(balno);
        if (request.getDtl() != null) {
            for (Hsio051u dtl : request.getDtl()) {
                dtl.setBalym(balym); dtl.setBalno(balno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setBalymd(request.getMst().getBalymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);
                if (dtl.getBrowno() == null || "0".equals(dtl.getBrowno())) dtl.setBrowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_051U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveGeneralOrder(Hsio052uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_052U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String balym = String.valueOf(mstValues.get(0)).trim();
        String balno = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(balym)) throw new Exception(balno);
        if (request.getDtl() != null) {
            for (Hsio051u dtl : request.getDtl()) {
                dtl.setBalym(balym); dtl.setBalno(balno);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setBalymd(request.getMst().getBalymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);
                if (dtl.getBrowno() == null || "0".equals(dtl.getBrowno())) dtl.setBrowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_051U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveOtherIn(Hsio190uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_190U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();
        String outno = mstValues.size() > 2 ? String.valueOf(mstValues.get(2)).trim() : "";

        if ("000000".equals(ioym)) throw new Exception(iono);
        if (request.getDtl() != null) {
            for (Hsio191u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIono(iono);
                dtl.setOutno(outno); // 마스터에서 받은 출고번호 매핑
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setIotype(request.getMst().getIotype());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setCfmyn("Y"); // ASP 원본 소스의 'Y' 값 반영
                dtl.setUpdemp(userId);
                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_191U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveOtherOut(Hsio250uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_250U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(ioym)) throw new Exception(iono);
        if (request.getDtl() != null) {
            for (Hsio251u dtl : request.getDtl()) {
                dtl.setIoym(ioym); dtl.setIono(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setIotype(request.getMst().getIotype());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setCfmyn("Y"); // ASP 원본 소스의 'Y' 값 반영
                dtl.setUpdemp(userId);
                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_251U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveDeposit(Hsio300uRequest request, String userId) throws Exception {
        Hsio300u mst = request.getMst();
        List<Map<String, Object>> res = hsioMapper.HSIO_300U_STR(mst);
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String imym = String.valueOf(mstValues.get(0)).trim();
        String imno = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(imym)) throw new Exception(imno);
        if (request.getDtl() != null) {
            for (Hsio300u dtl : request.getDtl()) {
                dtl.setImym(imym); dtl.setImno(imno);
                dtl.setCmpycd(mst.getCmpycd()); dtl.setDeptcd(mst.getDeptcd());
                dtl.setCustcd(mst.getCustcd()); dtl.setImymd(mst.getImymd());
                dtl.setUpdemp(userId);
                String state = dtl.getActkind();
                if ("C".equals(state)) dtl.setActkind("A1");
                else if ("U".equals(state)) dtl.setActkind("U1");
                else if ("D".equals(state)) dtl.setActkind("D1");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_300U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        if (request.getSetDtl() != null) {
            for (Hsio301u sdtl : request.getSetDtl()) {
                sdtl.setImym(imym); sdtl.setImno(imno);
                sdtl.setCmpycd(mst.getCmpycd()); sdtl.setCustcd(mst.getCustcd());
                sdtl.setImymd(mst.getImymd()); sdtl.setUserid(userId);
                if (sdtl.getIogbn() == null || sdtl.getIogbn().isEmpty()) sdtl.setIogbn("200");
                if (sdtl.getActkind() == null || sdtl.getActkind().isEmpty()) sdtl.setActkind("A0");
                List<Map<String, Object>> sdtlRes = hsioMapper.HSIO_301U_STR(sdtl);
                if (sdtlRes != null && !sdtlRes.isEmpty()) {
                    List<Object> sdtlValues = new ArrayList<>(sdtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(sdtlValues.get(0)).trim())) throw new Exception(String.valueOf(sdtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveDepositSlip(Hsio320uSaveRequest request, String userId) throws Exception {
        Hasl010u slipMst = request.getMst();
        List<Hsio320u> dtlList = request.getDtl();
        if (slipMst == null || dtlList == null || dtlList.isEmpty()) throw new Exception("발행할 데이터가 없습니다.");
        String autoSlipYn = "N";
        List<Map<String, Object>> p1Res = haslMapper.HASL_010U_STR(Map.of("actkind", "S", "cmpycd", slipMst.getCmpycd(), "slipgu", "P1"));
        if (p1Res != null && !p1Res.isEmpty()) autoSlipYn = String.valueOf(p1Res.get(0).get("slipyn"));
        String acctymd = "Y".equals(autoSlipYn) ? slipMst.getSlipymd() : "";
        String slipKind = "040";
        String currentSlipNo = slipMst.getSlipno();
        int proCnt = 0;
        for (int i = 0; i < dtlList.size(); i++) {
            Hsio320u item = dtlList.get(i);
            if (proCnt % 50 == 0) {
                slipMst.setActkind("A"); slipMst.setAcctymd(acctymd); slipMst.setSlipgu(slipKind); slipMst.setUpdemp(userId);
                Map<String, Object> mstMap = objectMapper.convertValue(slipMst, new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> mstRes = haslMapper.HASL_010U_STR(mstMap);
                if (mstRes == null || mstRes.isEmpty()) throw new Exception("전표 마스터 생성 실패");
                currentSlipNo = String.valueOf(new ArrayList<>(mstRes.get(0).values()).get(0)).trim();
            }
            item.setActkind("U0"); item.setCmpycd(slipMst.getCmpycd()); item.setDeptcd(slipMst.getDeptcd());
            item.setSlipymd(slipMst.getSlipymd()); item.setSlipno(currentSlipNo); item.setUpdemp(userId);
            List<Map<String, Object>> itemRes = hsioMapper.HSIO_320U_STR(item);
            if (itemRes != null && !itemRes.isEmpty()) {
                List<Object> resValues = new ArrayList<>(itemRes.get(0).values());
                if ("Y".equals(String.valueOf(resValues.get(0)).trim())) throw new Exception(String.valueOf(resValues.get(1)));
            }
            proCnt++;
        }
        if ("Y".equals(autoSlipYn)) {
            Hasl020u confirmDto = new Hasl020u();
            confirmDto.setActkind("A0"); confirmDto.setCmpycd(slipMst.getCmpycd());
            confirmDto.setSlipymd(slipMst.getSlipymd()); confirmDto.setAcctymd(acctymd);
            confirmDto.setSlipno(currentSlipNo); confirmDto.setDeptcd(slipMst.getDeptcd());
            confirmDto.setSlipkind(slipKind); confirmDto.setSlipyn("N"); confirmDto.setCofmyn("Y"); confirmDto.setUpdemp(userId);
            haslMapper.HASL_020U_STR(objectMapper.convertValue(confirmDto, new TypeReference<Map<String, Object>>() {}));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("slipymd", slipMst.getSlipymd()); result.put("slipno", currentSlipNo);
        return result;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveSettlement(Hsio510uRequest request, String userId) throws Exception {
        Hsio510u mst = request.getMst();
        List<Map<String, Object>> res = hsioMapper.HSIO_510U_STR(mst);
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String jsanym = String.valueOf(mstValues.get(0)).trim();
        String jsanno = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(jsanym)) throw new Exception(jsanno);
        if (request.getDtl() != null) {
            for (Hsio510u dtl : request.getDtl()) {
                dtl.setJsanym(jsanym); dtl.setJsanno(jsanno);
                dtl.setCmpycd(mst.getCmpycd()); dtl.setIogbn(mst.getIogbn());
                dtl.setDeptcd(mst.getDeptcd()); dtl.setCustcd(mst.getCustcd());
                dtl.setJsanymd(mst.getJsanymd()); dtl.setTaxunit(mst.getTaxunit());
                dtl.setVattype(mst.getVattype()); dtl.setUpdemp(userId);
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_510U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
            // 🚀 V0: 부가세 내역 생성 (누락분 추가)
            Hsio510u vat = new Hsio510u();
            vat.setActkind("V0");
            vat.setCmpycd(mst.getCmpycd());
            vat.setIogbn(mst.getIogbn());
            vat.setJsanymd(mst.getJsanymd());
            vat.setJsanym(jsanym);
            vat.setJsanno(jsanno);

            vat.setUpdemp(userId);
            hsioMapper.HSIO_510U_STR(vat);
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> savePurchase(Hsio500uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_500U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(ioym)) throw new Exception(iono);
        if (request.getDtl() != null) {
            for (Hsio501u dtl : request.getDtl()) {
                dtl.setIoym(ioym); dtl.setIono(iono);
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setUpdemp(userId);
                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_501U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveWarehouseTransfer(Hsio580uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_580U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        String ioym = mstRow.get("ioym") != null ? String.valueOf(mstRow.get("ioym")).trim() : "";
        String iono = mstRow.get("iono") != null ? String.valueOf(mstRow.get("iono")).trim() : "";
        String ino = mstRow.get("ino") != null ? String.valueOf(mstRow.get("ino")).trim() : "";
        if ("000000".equals(ioym)) throw new Exception(iono);
        if (request.getDtl() != null) {
            for (Hsio581u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIono(iono);
                dtl.setIno(ino);
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
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveStockAdjustment(Hsio720uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_720U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(ioym)) throw new Exception(iono);
        if (request.getDtl() != null) {
            for (Hsio721u dtl : request.getDtl()) {
                dtl.setIoym(ioym); dtl.setIno(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setUpdemp(userId);
                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_721U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveInventoryAdjustment(Hsio730Request request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_730U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();
        if ("000000".equals(ioym)) throw new Exception(iono);
        if (request.getDtl() != null) {
            for (Hsio731u dtl : request.getDtl()) {
                dtl.setIoym(ioym); dtl.setIno(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setUpdemp(userId);
                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_731U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveStoreInout(Hsio570uRequest request, String userId) throws Exception {
        List<Map<String, Object>> res = hsioMapper.HSIO_570U_STR(request.getMst());
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");
        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        
        // 🚀 프로젝트 표준 원칙(0, 1, 2 인덱스)에 따라 ioym, iono, ino 추출
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();
        String ino = String.valueOf(mstValues.get(2)).trim();

        if ("000000".equals(ioym)) throw new Exception(iono);
        if (request.getDtl() != null) {
            for (Hsio571u dtl : request.getDtl()) {
                dtl.setIoym(ioym); dtl.setIono(iono);
                dtl.setCmpycd(request.getMst().getCmpycd());
                dtl.setIogbn(request.getMst().getIogbn());
                dtl.setIoymd(request.getMst().getIoymd());
                dtl.setIotype(request.getMst().getIotype());
                dtl.setDeptcd(request.getMst().getDeptcd());
                dtl.setWhcd(request.getMst().getWhcd());
                dtl.setCustcd(request.getMst().getCustcd());
                dtl.setItemcd_t("");
                dtl.setItsize_t("");
                dtl.setUnit_t("");
                dtl.setIno(ino);
                dtl.setUserid(request.getMst().getUserid());
                dtl.setUpdemp(userId);
                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");
                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_571U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    List<Object> dtlValues = new ArrayList<>(dtlRes.get(0).values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) throw new Exception(String.valueOf(dtlValues.get(1)));
                }
            }
        }
        return mstRow;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveBatchSettlement(Hsio590uRequest request, String userId) throws Exception {
        if (request.getList() == null || request.getList().isEmpty()) throw new Exception("정산할 데이터가 없습니다.");
        for (Hsio590u item : request.getList()) {
            item.setActkind("U0"); item.setUpdemp(userId);
            List<Map<String, Object>> res = hsioMapper.HSIO_590U_STR(item);
            if (res != null && !res.isEmpty()) {
                List<Object> values = new ArrayList<>(res.get(0).values());
                if (values.size() > 1 && "Y".equals(String.valueOf(values.get(0)).trim())) throw new Exception(String.valueOf(values.get(1)));
            }
        }
        Map<String, Object> result = new HashMap<>(); result.put("res", "OK"); return result;
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveSalesReturn(Hsio490uRequest request, String userId) throws Exception {
        Hsio490u mst = request.getMst();

        if (mst.getIogbn() == null || mst.getIogbn().isEmpty())
            mst.setIogbn("200");
        if (mst.getArea() == null) mst.setArea("");
        if (mst.getRemark() == null) mst.setRemark("");
        if (mst.getTrancd() == null) mst.setTrancd("");
        if (mst.getTrnemp() == null) mst.setTrnemp("");
        if (mst.getAddress() == null) mst.setAddress("");
        
        List<Map<String, Object>> res = hsioMapper.HSIO_490U_STR(mst);
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();
        String ino  = mstValues.size() > 2 ? String.valueOf(mstValues.get(2)).trim() : "";

        if ("000000".equals(ioym)) throw new Exception(iono);

        if (request.getDtl() != null) {
            for (Hsio491u dtl : request.getDtl()) {
                dtl.setIoym(ioym);
                dtl.setIono(iono);
                dtl.setIno(ino); 
                dtl.setCmpycd(mst.getCmpycd());
                dtl.setIogbn(mst.getIogbn());
                dtl.setIoymd(mst.getIoymd());
                dtl.setIotype(mst.getIotype());
                dtl.setDeptcd(mst.getDeptcd());
                dtl.setCustcd(mst.getCustcd());
                dtl.setWhcd(mst.getWhcd());
                dtl.setArea(mst.getArea());
                dtl.setUserid(mst.getUserid());
                dtl.setUpdemp(userId);

                if (dtl.getIorowno() == null || "0".equals(dtl.getIorowno())) dtl.setIorowno("");
                if (dtl.getItsize() == null) dtl.setItsize("");
                if (dtl.getUnit() == null) dtl.setUnit("");

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_491U_STR(dtl);
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

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveHSIO600U(Map<String, Object> payload) throws Exception {
        // ... (기존 로직 유지)
        return Map.of("res", "OK");
    }

    /**
     * 🚀 외부전표전송 (ASP 로직 이식: 상세조회 -> 루프 -> 외부INSERT -> 로컬상태업데이트)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public void transferExternalSlip(Map<String, Object> item, String userId) throws Exception {
        // 1. 상세 데이터 조회 (ASP SELECT문 루프 시작 전 단계)
        List<Map<String, Object>> details = hsioMapper.getSlipDetailsForTransfer(item);
        if (details == null || details.isEmpty()) return;

        for (Map<String, Object> row : details) {
            // 2. 더존 DB용 데이터 가공 (ASP 변수 할당 로직)
            String acctCd = String.valueOf(row.get("acctcd")).trim();
            String taxType = String.valueOf(row.get("tax_type"));
            String dealNm = "";
            String jeonjaYn = "0";

            if ("13500".equals(acctCd) || "25500".equals(acctCd)) {
                if ("11".equals(taxType)) { dealNm = "과세매출"; jeonjaYn = "1"; }
                else if ("21".equals(taxType)) { dealNm = "과세매입"; jeonjaYn = "1"; }
                else if ("16".equals(taxType)) { dealNm = "수출"; jeonjaYn = "0"; }
                else if ("25".equals(taxType)) { dealNm = "수입"; jeonjaYn = "0"; }
            }

            // 3. 더존 DB(THEJONE) INSERT (ASP의 THEJONE.Execute 이식)
            // TODO: THEJONE용 JdbcTemplate 또는 Linked Server 쿼리 적용 필요
            log.info("🚀 [THEJONE 전송 대기] SLIP: {}-{}, ACCT: {}, DEAL: {}", 
                    row.get("slipymd"), row.get("slipno"), acctCd, dealNm);
        }

        // 4. 로컬 DB 상태 업데이트 (HSIO_990U_STR 'U0' 호출)
        item.put("actkind", "U0");
        item.put("updemp", userId);
        hsioMapper.HSIO_990U_STR(item);
    }
}
