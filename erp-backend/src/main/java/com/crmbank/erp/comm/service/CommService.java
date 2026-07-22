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
     * Map에서 대소문자 무시하고 값 가져오기 (소문자 표준화 대응)
     */
    private String getVal(Map<String, Object> map, String key, String defaultVal) {
        if (map == null) return defaultVal;
        // 소문자 키 우선 검색
        Object v = map.get(key.toLowerCase());
        if (v == null) v = map.get(key.toUpperCase());
        return (v != null) ? String.valueOf(v).trim() : defaultVal;
    }

    @Transactional("erpTransactionManager")
    public UserSession login(String cmpycd, String userid, String passwd, String ip) throws Exception {
        Map<String, Object> param = new HashMap<>();
        // MyBatis XML Mapper와 맞추기 위해 소문자로 전달
        param.put("cmpycd", cmpycd.trim());
        param.put("userid", userid.trim());

        log.info("🔐 [DB 조회 시작] cmpycd: {}, userid: {}", cmpycd, userid);

        // 1. 회사 정보 조회
        Map<String, Object> companyInfo;
        try {
            companyInfo = commMapper.GET_COMPANY_INFO(param);
        } catch (Exception e) {
            log.error("🔥 [DB 에러 - 회사정보] : {}", e.getMessage());
            throw new Exception("데이터베이스 연결 오류가 발생했습니다. (회사조회)");
        }
        
        if (companyInfo == null) throw new Exception("등록되지 않은 회사아이디 입니다.");
        
        // 2. 사용자 정보 조회 (HA00_010S_STR)
        Map<String, Object> userInfo;
        try {
            userInfo = commMapper.GET_USER_INFO(param);
        } catch (Exception e) {
            log.error("🔥 [DB 에러 - 사용자정보] : {}", e.getMessage());
            throw new Exception("데이터베이스 연결 오류가 발생했습니다. (사용자조회)");
        }
        
        log.info("🔍 [사용자 정보 조회 결과] keys: {}", userInfo.keySet());
        log.info("🔍 [사진 경로 확인] photo_path: {}", userInfo.get("photo_path"));

        // 3. 비밀번호 체크
        String dbPw = getVal(userInfo, "pw", "");
        if (!passwd.trim().equals(dbPw)) throw new Exception("입력하신 비밀번호가 틀립니다.");

        // 4. 세션 객체 생성 및 데이터 매핑 (소문자 표준 적용)
        UserSession session = new UserSession();
        session.setCmpycd(getVal(companyInfo, "cmpycd", cmpycd));
        session.setCmpynm(getVal(companyInfo, "cmpynm", ""));
        session.setDomain(getVal(companyInfo, "domain", ""));
        
        session.setUserid(getVal(userInfo, "userid", userid));
        session.setUsernm(getVal(userInfo, "usernm", ""));
        session.setInner_no(getVal(userInfo, "inner_no", ""));
        
        session.setDeptcd(getVal(userInfo, "deptcd", ""));
        session.setDeptnm(getVal(userInfo, "deptnm", ""));
        session.setEmpno(getVal(userInfo, "empno", ""));
        session.setSalsyn(getVal(userInfo, "salsyn", ""));
        session.setUsergrp(getVal(userInfo, "usergrp", ""));
        session.setEmail(getVal(userInfo, "email", ""));
        session.setPhoto_path(getVal(userInfo, "photo_path", "")); // 🚀 사진 경로 매핑

        log.info("🔓 [로그인 성공] ID: {}, 이름: {}, 내선: {}", session.getUserid(), session.getUsernm(), session.getInner_no());

        // 5. 로그인 이력 기록
        param.put("iogbn", "I");
        param.put("ip", ip);
        commMapper.INSERT_LOGIN_HISTORY(param);

        return session;
    }

    public List<Map<String, Object>> getTopMenus() {
        return commMapper.GET_TOP_MENU_LIST();
    }

    public List<Map<String, Object>> getLeftMenus(String upmucd) {
        return commMapper.GET_LEFT_MENU_LIST(upmucd);
    }

    public List<Map<String, Object>> getProgramList(Map<String, Object> param) {
        return commMapper.GET_PROGRAM_LIST(param);
    }
}
