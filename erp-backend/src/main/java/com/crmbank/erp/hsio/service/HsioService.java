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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsioService {

    private final HsioMapper hsioMapper;
    private final HaslMapper haslMapper;
    private final ObjectMapper objectMapper;

    /**
     * 🚀 요청등록 통합 저장 (HSIO_010U_SAVE)
     */
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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String balym = String.valueOf(mstValues.get(0)).trim();
        String balno = String.valueOf(mstValues.get(1)).trim();

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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String balym = String.valueOf(mstValues.get(0)).trim();
        String balno = String.valueOf(mstValues.get(1)).trim();

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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();

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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();

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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 입금입력 및 건별 상계 통합 저장 (HSIO_300U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveDeposit(Hsio300uRequest request, String userId) throws Exception {
        // 1. 마스터 저장 (A0, U0, D0)
        Hsio300u mst = request.getMst();
        List<Map<String, Object>> res = hsioMapper.HSIO_300U_STR(mst);
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String imym = String.valueOf(mstValues.get(0)).trim();
        String imno = String.valueOf(mstValues.get(1)).trim();

        if ("000000".equals(imym)) throw new Exception(imno);

        // 2. 상세 내역 저장 (입금 수단: A1, U1, D1 / A2)
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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
                    }
                }
            }
        }

        // 3. 매출 건별 상계 저장 (HSIO305U 전용: HSIO_301U_STR)
        if (request.getSetDtl() != null) {
            for (Hsio301u sdtl : request.getSetDtl()) {
                sdtl.setImym(imym);
                sdtl.setImno(imno);
                sdtl.setCmpycd(mst.getCmpycd());
                sdtl.setCustcd(mst.getCustcd());
                sdtl.setImymd(mst.getImymd());
                sdtl.setUserid(userId);
                
                // 기본값 보정 (필요시)
                if (sdtl.getIogbn() == null || sdtl.getIogbn().isEmpty()) sdtl.setIogbn("200");
                if (sdtl.getActkind() == null || sdtl.getActkind().isEmpty()) sdtl.setActkind("A0");

                List<Map<String, Object>> sdtlRes = hsioMapper.HSIO_301U_STR(sdtl);
                if (sdtlRes != null && !sdtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = sdtlRes.get(0);
                    List<Object> sdtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(sdtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(sdtlValues.get(1)));
                    }
                }
            }
        }

        return mstRow;
    }

    /**
     * 🚀 입금전표 통합 저장 (HSIO_320U_SAVE)
     * ASP HSIO_320U_IF_AUD.asp 로직 완벽 재현
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveDepositSlip(Hsio320uSaveRequest request, String userId) throws Exception {
        Hasl010u slipMst = request.getMst();
        List<Hsio320u> dtlList = request.getDtl();

        if (slipMst == null || dtlList == null || dtlList.isEmpty()) {
            throw new Exception("발행할 데이터가 없습니다.");
        }

        // 1. 자동전표 여부 확인 (HA00_010S_STR P1)
        String autoSlipYn = "N";
        List<Map<String, Object>> p1Res = haslMapper.HASL_010U_STR(Map.of("actkind", "S", "cmpycd", slipMst.getCmpycd(), "slipgu", "P1"));
        if (p1Res != null && !p1Res.isEmpty()) {
            autoSlipYn = String.valueOf(p1Res.get(0).get("slipyn"));
        }

        String acctymd = "Y".equals(autoSlipYn) ? slipMst.getSlipymd() : "";
        String slipKind = "040"; // 입금전표 고정코드
        
        // 2. 전표 발행 루프 (50건 단위 분할 처리 포함)
        String currentSlipNo = slipMst.getSlipno();
        int proCnt = 0;

        for (int i = 0; i < dtlList.size(); i++) {
            Hsio320u item = dtlList.get(i);
            
            // 50건마다 또는 첫 시작 시 전표 마스터 생성 (채번)
            if (proCnt % 50 == 0) {
                slipMst.setActkind("A");
                slipMst.setAcctymd(acctymd);
                slipMst.setSlipgu(slipKind);
                slipMst.setUpdemp(userId);
                
                Map<String, Object> mstMap = objectMapper.convertValue(slipMst, new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> mstRes = haslMapper.HASL_010U_STR(mstMap);
                if (mstRes == null || mstRes.isEmpty()) throw new Exception("전표 마스터 생성 실패");
                
                // 생성된 전표번호 획득
                currentSlipNo = String.valueOf(new ArrayList<>(mstRes.get(0).values()).get(0)).trim();
            }

            // 3. 입금 내역에 전표번호 매핑 (HSIO_320U_STR U0)
            item.setActkind("U0");
            item.setCmpycd(slipMst.getCmpycd());
            item.setDeptcd(slipMst.getDeptcd());
            item.setSlipymd(slipMst.getSlipymd());
            item.setSlipno(currentSlipNo);
            item.setUpdemp(userId);

            List<Map<String, Object>> itemRes = hsioMapper.HSIO_320U_STR(item);
            if (itemRes != null && !itemRes.isEmpty()) {
                List<Object> resValues = new ArrayList<>(itemRes.get(0).values());
                if ("Y".equals(String.valueOf(resValues.get(0)).trim())) {
                    throw new Exception(String.valueOf(resValues.get(1))); // errMsg
                }
            }
            proCnt++;
        }

        // 4. 자동전표 확정 처리 (HASL_020U_STR)
        if ("Y".equals(autoSlipYn)) {
            Hasl020u confirmDto = new Hasl020u();
            confirmDto.setActkind("A0");
            confirmDto.setCmpycd(slipMst.getCmpycd());
            confirmDto.setSlipymd(slipMst.getSlipymd());
            confirmDto.setAcctymd(acctymd);
            confirmDto.setSlipno(currentSlipNo);
            confirmDto.setDeptcd(slipMst.getDeptcd());
            confirmDto.setSlipkind(slipKind);
            confirmDto.setSlipyn("N");
            confirmDto.setCofmyn("Y");
            confirmDto.setUpdemp(userId);

            Map<String, Object> confMap = objectMapper.convertValue(confirmDto, new TypeReference<Map<String, Object>>() {});
            haslMapper.HASL_020U_STR(confMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("slipymd", slipMst.getSlipymd());
        result.put("slipno", currentSlipNo);
        return result;
    }

    /**
     * 🚀 매출정산 통합 저장 (HSIO_510U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveSettlement(Hsio510uRequest request, String userId) throws Exception {
        // 1. 마스터 저장 (A0)
        Hsio510u mst = request.getMst();
        List<Map<String, Object>> res = hsioMapper.HSIO_510U_STR(mst);
        if (res == null || res.isEmpty()) throw new Exception("No response from Master procedure.");

        Map<String, Object> mstRow = res.get(0);
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String jsanym = String.valueOf(mstValues.get(0)).trim();
        String jsanno = String.valueOf(mstValues.get(1)).trim();

        if ("000000".equals(jsanym)) throw new Exception(jsanno);

        // 2. 상세 내역 저장 (U0)
        if (request.getDtl() != null) {
            for (Hsio510u dtl : request.getDtl()) {
                dtl.setJsanym(jsanym);
                dtl.setJsanno(jsanno);
                dtl.setCmpycd(mst.getCmpycd());
                dtl.setIogbn(mst.getIogbn());
                dtl.setDeptcd(mst.getDeptcd());
                dtl.setCustcd(mst.getCustcd());
                dtl.setJsanymd(mst.getJsanymd());
                dtl.setTaxunit(mst.getTaxunit());
                dtl.setVattype(mst.getVattype());
                dtl.setUpdemp(userId);

                List<Map<String, Object>> dtlRes = hsioMapper.HSIO_510U_STR(dtl);
                if (dtlRes != null && !dtlRes.isEmpty()) {
                    Map<String, Object> dtlRow = dtlRes.get(0);
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();

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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();

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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();

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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
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
        List<Object> mstValues = new ArrayList<>(mstRow.values());
        String ioym = String.valueOf(mstValues.get(0)).trim();
        String iono = String.valueOf(mstValues.get(1)).trim();

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
                    List<Object> dtlValues = new ArrayList<>(dtlRow.values());
                    if ("000000".equals(String.valueOf(dtlValues.get(0)).trim())) {
                        throw new Exception(String.valueOf(dtlValues.get(1)));
                    }
                }
            }
        }
        return mstRow;
    }

    /**
     * 🚀 일괄매출정산 통합 저장 (HSIO_590U_SAVE)
     */
    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> saveBatchSettlement(Hsio590uRequest request, String userId) throws Exception {
        if (request.getList() == null || request.getList().isEmpty()) {
            throw new Exception("정산할 데이터가 없습니다.");
        }

        for (Hsio590u item : request.getList()) {
            item.setActkind("U0"); 
            item.setUpdemp(userId);
            
            List<Map<String, Object>> res = hsioMapper.HSIO_590U_STR(item);
            if (res != null && !res.isEmpty()) {
                Map<String, Object> row = res.get(0);
                List<Object> values = new ArrayList<>(row.values());
                if (values.size() > 1 && "Y".equals(String.valueOf(values.get(0)).trim())) {
                    throw new Exception(String.valueOf(values.get(1)));
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("res", "OK");
        return result;
    }
}
