package com.crmbank.erp.comm.service;

import com.crmbank.erp.comm.mapper.CommMapper;
import com.crmbank.erp.comm.dto.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommService {

    private final CommMapper commMapper;

    /**
     * Map에서 대소문자 무시하고 값 가져오기
     */
    private String getVal(Map<String, Object> map, String key, String defaultVal) {
        if (map == null) return defaultVal;
        Object v = map.get(key.toUpperCase());
        if (v == null) v = map.get(key.toLowerCase());
        return (v != null) ? String.valueOf(v).trim() : defaultVal;
    }

    @Transactional("erpTransactionManager")
    public UserSession login(String cmpycd, String userid, String passwd, String ip) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("CMPYCD", cmpycd.trim());
        param.put("USERID", userid.trim());

        // 1. 회사 정보 조회
        Map<String, Object> companyInfo = commMapper.GET_COMPANY_INFO(param);
        if (companyInfo == null) throw new Exception("등록되지 않은 회사아이디 입니다.");
        
        // 2. 사용자 정보 조회 (HA00_010S_STR)
        Map<String, Object> userInfo = commMapper.GET_USER_INFO(param);
        if (userInfo == null) throw new Exception("등록되지 않은 사용자아이디 입니다.");

        // 3. 비밀번호 체크 (DB 컬럼명 'PW' 또는 'pw')
        String dbPw = getVal(userInfo, "PW", "");
        if (!passwd.trim().equals(dbPw)) throw new Exception("입력하신 비밀번호가 틀립니다.");

        // 4. 세션 객체 생성 및 데이터 매핑 (강력한 필드 추출)
        UserSession session = new UserSession();
        session.setCMPYCD(getVal(companyInfo, "CMPYCD", cmpycd));
        session.setCMPYNM(getVal(companyInfo, "CMPYNM", ""));
        session.setDOMAIN(getVal(companyInfo, "DOMAIN", ""));
        
        // ⭐ 사용자 핵심 정보 (USERID, USERNM, INNER_NO)
        session.setUSERID(getVal(userInfo, "USERID", userid));
        session.setUSERNM(getVal(userInfo, "USERNM", ""));
        session.setINNER_NO(getVal(userInfo, "INNER_NO", ""));
        
        session.setDEPTCD(getVal(userInfo, "DEPTCD", ""));
        session.setDEPTNM(getVal(userInfo, "DEPTNM", ""));
        session.setEMPNO(getVal(userInfo, "EMPNO", ""));
        session.setSALSYN(getVal(userInfo, "SALSYN", ""));
        session.setUSERGRP(getVal(userInfo, "USERGRP", ""));
        session.setEMAIL(getVal(userInfo, "EMAIL", ""));



        log.info("🔓 [로그인 성공] ID: {}, 이름: {}, 내선: {}", session.getUSERID(), session.getUSERNM(), session.getINNER_NO());

        // 5. 로그인 이력 기록
        param.put("IOGBN", "I");
        param.put("IP", ip);
        commMapper.INSERT_LOGIN_HISTORY(param);

        return session;
    }

    public List<Map<String, Object>> getTopMenus() {
        return commMapper.GET_TOP_MENU_LIST();
    }

    public List<Map<String, Object>> getLeftMenus(String upmucd) {
        return commMapper.GET_LEFT_MENU_LIST(upmucd);
    }
}
