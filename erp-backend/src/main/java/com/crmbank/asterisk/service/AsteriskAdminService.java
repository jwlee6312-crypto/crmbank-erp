package com.crmbank.asterisk.service;

import lombok.RequiredArgsConstructor;
import com.crmbank.asterisk.mapper.AsteriskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AsteriskAdminService {

    private final AsteriskMapper asteriskMapper;

    // 1. PJSIP 내선번호 저장
    @Transactional
    public void savePjsip(List<Map<String, Object>> list) {
        for (Map<String, Object> data : list) {
            asteriskMapper.insertPjsipAuth(data);
            asteriskMapper.insertPjsipAor(data);
            asteriskMapper.insertPjsipEndpoint(data);
        }
    }

    // 2. 수신그룹(Queue) 저장
    @Transactional
    public void saveQueues(List<Map<String, Object>> list) {
        for (Map<String, Object> data : list) {
            asteriskMapper.upsertQueue(data);
        }
    }

    // 3. 수신그룹 멤버 저장
    @Transactional
    public void saveQueueMembers(String queueName, List<Map<String, Object>> members) {
        asteriskMapper.deleteQueueMembers(queueName);
        for (Map<String, Object> member : members) {
            member.put("queue_name", queueName);
            asteriskMapper.insertQueueMember(member);
        }
    }

    // 4. 다이얼플랜 저장
    @Transactional
    public void saveExtensions(List<Map<String, Object>> list) {
        for (Map<String, Object> data : list) {
            asteriskMapper.deleteExtensions(data);
            asteriskMapper.insertExtension(data);
        }
    }

    /**
     * 💡 [테스트 최적화] 3자리 내선번호(101, 102) 환경을 위한 표준 IVR 시나리오
     * 내선 간 통화, IVR 진입(700), 콜백(4) 연동을 포함합니다.
     */
    public List<Map<String, Object>> getStandardIvrTemplate() {
        List<Map<String, Object>> template = new ArrayList<>();

        // [0] from-internal: 내선 전화기(101, 102)가 속한 기본 컨텍스트
        addExten(template, "from-internal", "700", 1, "NoOp", "### [TEST] Dialed IVR Entry ###");
        addExten(template, "from-internal", "700", 2, "Goto", "incoming-main,s,1");
        addExten(template, "from-internal", "_1XX", 1, "NoOp", "### [TEST] Internal Call to ${EXTEN} ###");
        addExten(template, "from-internal", "_1XX", 2, "Dial", "PJSIP/${EXTEN},20,tT"); // tT: 호전환 허용
        addExten(template, "from-internal", "_1XX", 3, "Hangup", "");

        // [1] incoming-main: 외부(또는 700번) 진입부
        addExten(template, "incoming-main", "s", 1, "Answer", "");
        addExten(template, "incoming-main", "s", 2, "Set", "REC_FILE=${STRFTIME(${EPOCH},,%Y%m%d-%H%M%S)}-${CALLERID(num)}-${UNIQUEID}.wav");
        addExten(template, "incoming-main", "s", 3, "MixMonitor", "${REC_FILE}");
        addExten(template, "incoming-main", "s", 4, "GotoIfTime", "09:00-18:00,mon-fri,*,*?ivr-main,s,1");
        addExten(template, "incoming-main", "s", 5, "Goto", "after-hours,s,1");

        // [2] ivr-main: 업무 시간 ARS 메뉴
        addExten(template, "ivr-main", "s", 1, "Background", "custom/01_welcome_and_recording");
        addExten(template, "ivr-main", "s", 2, "Background", "custom/02_select_menu");
        addExten(template, "ivr-main", "s", 3, "WaitExten", "5");

        // 1번: 영업팀 (테스트용 내선 101, 102 동시 호출)
        addExten(template, "ivr-main", "1", 1, "NoOp", "### [TEST] Dialing Sales Team (101 & 102) ###");
        addExten(template, "ivr-main", "1", 2, "Playback", "custom/03_connect_wait_moment");
        addExten(template, "ivr-main", "1", 3, "Dial", "PJSIP/101&PJSIP/102,30,tT");
        addExten(template, "ivr-main", "1", 4, "GotoIf", "$[\"${DIALSTATUS}\" = \"BUSY\"]?busy-handling,s,1");
        addExten(template, "ivr-main", "1", 5, "GotoIf", "$[\"${DIALSTATUS}\" = \"NOANSWER\"]?busy-handling,s,1");

        // 2번/3번: 부서별 대기열(Queue) 연결
        addExten(template, "ivr-main", "2", 1, "Queue", "tech_queue");
        addExten(template, "ivr-main", "3", 1, "Queue", "admin_queue");

        // 4번: 콜백 서비스 (윈도우 백엔드 연동)
        addExten(template, "ivr-main", "4", 1, "NoOp", "### [TEST] Callback Requested ###");
        addExten(template, "ivr-main", "4", 2, "Playback", "custom/05_callback_confirm");
        // 💡 127.0.0.1: WSL에서 윈도우 호스트 접근용
        addExten(template, "ivr-main", "4", 3, "System", "curl -X POST http://127.0.0.1:8080/api/crm/inbound/log-callback -H \"Content-Type: application/json\" -d '{\"interaction_id\":\"CB_${UNIQUEID}\", \"keyword\":\"${CALLERID(num)}\", \"media_type\":\"callback\", \"cmpycd\":\"HAIONNET\"}'");
        addExten(template, "ivr-main", "4", 4, "Playback", "custom/06_thank_you_bye");
        addExten(template, "ivr-main", "4", 5, "Hangup", "");

        // 5번: 정보 안내
        addExten(template, "ivr-main", "5", 1, "Playback", "custom/07_company_info");
        addExten(template, "ivr-main", "5", 2, "Goto", "ivr-main,s,1");

        // 내선번호 직접 연결 (3자리: 101, 102 등)
        addExten(template, "ivr-main", "_1XX", 1, "NoOp", "### [TEST] IVR Direct Dial to ${EXTEN} ###");
        addExten(template, "ivr-main", "_1XX", 2, "Dial", "PJSIP/${EXTEN},20,tT");
        addExten(template, "ivr-main", "_1XX", 3, "Playback", "vm-nobodyavail");
        addExten(template, "ivr-main", "_1XX", 4, "Hangup", "");

        // 예외 처리
        addExten(template, "ivr-main", "i", 1, "Playback", "pbx-invalid");
        addExten(template, "ivr-main", "i", 2, "Goto", "ivr-main,s,1");
        addExten(template, "ivr-main", "t", 1, "Goto", "ivr-main,s,1");

        // [3] busy-handling: 상담원 통화 중 안내
        addExten(template, "busy-handling", "s", 1, "Playback", "custom/04_calling_busy");
        addExten(template, "busy-handling", "s", 2, "Wait", "1");
        addExten(template, "busy-handling", "s", 3, "Goto", "ivr-main,s,1");

        // [4] after-hours: 야간 및 휴일
        addExten(template, "after-hours", "s", 1, "Playback", "custom/08_night_greeting");
        addExten(template, "after-hours", "9", 1, "Playback", "custom/09_transfer_to_emergency");
        addExten(template, "after-hours", "9", 2, "Dial", "PJSIP/01032043901@PJSIP/my-trunk,30");

        return template;
    }

    private void addExten(List<Map<String, Object>> list, String ctx, String ext, int pri, String app, String arg) {
        Map<String, Object> row = new HashMap<>();
        row.put("context", ctx);
        row.put("exten", ext);
        row.put("priority", pri);
        row.put("app", app);
        row.put("appdata", arg);
        list.add(row);
    }
}
