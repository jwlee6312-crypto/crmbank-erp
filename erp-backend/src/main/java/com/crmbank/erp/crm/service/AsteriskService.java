package com.crmbank.erp.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.handler.CtiWebSocketHandler;
import com.crmbank.erp.crm.mapper.inbound.InboundMapper;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.*;
import org.asteriskjava.manager.event.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsteriskService implements ManagerEventListener {

    private final ManagerConnection managerConnection;
    private final InboundMapper inboundMapper; 
    private final CtiWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> EXTEN_CHANNEL_MAP = new ConcurrentHashMap<>(); 
    private final Map<String, String> EXTEN_CALLER_CHANNEL = new ConcurrentHashMap<>(); 
    private final Map<String, String> EXTEN_LINKED_ID = new ConcurrentHashMap<>(); 
    private final Map<String, Set<String>> SESSION_CHANNELS = new ConcurrentHashMap<>(); 
    private final Set<String> ANSWERING_LOCK = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Map<String, String> getEXTEN_CHANNEL_MAP() { return EXTEN_CHANNEL_MAP; }
    public Map<String, String> getEXTEN_LINKED_ID() { return EXTEN_LINKED_ID; }
    public Map<String, Set<String>> getSESSION_CHANNELS() { return SESSION_CHANNELS; }

    @Override
    public void onManagerEvent(ManagerEvent event) {
        if (event instanceof NewChannelEvent) {
            String channel = ((NewChannelEvent) event).getChannel();
            String uniqueId = ((NewChannelEvent) event).getUniqueId();
            String ext = extractNumberOnly(channel);
            if (isValidExt(ext)) EXTEN_CHANNEL_MAP.put(ext, channel);
            SESSION_CHANNELS.computeIfAbsent(uniqueId, k -> Collections.synchronizedSet(new HashSet<>())).add(channel);
        } else if (event instanceof AgentCalledEvent) {
            handleAgentCalled((AgentCalledEvent) event);
        } else if (event instanceof BridgeEnterEvent) {
            handleBridgeEnter((BridgeEnterEvent) event);
        } else if (event instanceof HangupEvent) {
            handleHangup((HangupEvent) event);
        }
    }

    private void handleAgentCalled(AgentCalledEvent e) {
        String agentExt = extractNumberOnly(e.getInterface());
        if (isValidExt(agentExt)) {
            EXTEN_CALLER_CHANNEL.put(agentExt, e.getChannel());
            EXTEN_LINKED_ID.put(agentExt, e.getLinkedId());
            if (e.getDestinationChannel() != null) EXTEN_CHANNEL_MAP.put(agentExt, e.getDestinationChannel());
            
            if (!ANSWERING_LOCK.contains(agentExt)) {
                sendInboundPopup(agentExt, e.getCallerIdNum(), e.getChannel(), e.getLinkedId());
            }
        }
    }

    private void handleBridgeEnter(BridgeEnterEvent e) {
        String exten = extractNumberOnly(e.getChannel());
        if (isValidExt(exten)) {
            ANSWERING_LOCK.remove(exten);
            sendCtiEvent(exten, "CALL_CONNECTED", e.getChannel(), null);
        }
    }

    private void handleHangup(HangupEvent e) {
        String exten = extractNumberOnly(e.getChannel());
        if (isValidExt(exten)) {
            if (ANSWERING_LOCK.contains(exten)) return;
            // 💡 [핵심] 종료 시 녹취 파일명(LinkedID)을 반드시 전송하여 상담원이 볼 수 있게 함
            String recFile = EXTEN_LINKED_ID.get(exten);
            sendCtiEvent(exten, "CALL_HANGUP", e.getChannel(), recFile);
            
            EXTEN_CHANNEL_MAP.remove(exten);
            EXTEN_CALLER_CHANNEL.remove(exten);
            EXTEN_LINKED_ID.remove(exten);
        }
    }

    public void answerCall(String exten) {
        String callerChannel = EXTEN_CALLER_CHANNEL.get(exten);
        String agentChannel = EXTEN_CHANNEL_MAP.get(exten);
        if (callerChannel == null) return;

        ANSWERING_LOCK.add(exten);
        sendCtiEvent(exten, "STOP_RINGTONE", null, null);

        new Thread(() -> {
            try {
                if (agentChannel != null && !agentChannel.equals("UNKNOWN")) {
                    managerConnection.sendAction(new HangupAction(agentChannel));
                    Thread.sleep(600);
                }
                managerConnection.sendAction(new SetVarAction(callerChannel, "AGENT_EXTEN", exten));
                managerConnection.sendAction(new RedirectAction(callerChannel, "cti-answer-force", "s", 1));
                Thread.sleep(4000);
                ANSWERING_LOCK.remove(exten);
            } catch (Exception ex) { ANSWERING_LOCK.remove(exten); }
        }).start();
    }

    public void hangupCall(String exten, String ch) {
        try {
            if (ch != null) managerConnection.sendAction(new HangupAction(ch));
            String myCh = EXTEN_CHANNEL_MAP.get(exten);
            if (myCh != null) managerConnection.sendAction(new HangupAction(myCh));
            String callerCh = EXTEN_CALLER_CHANNEL.get(exten);
            if (callerCh != null) managerConnection.sendAction(new HangupAction(callerCh));
        } catch (Exception ex) {}
    }

    public void transferCall(String exten, String target) {
        String myChannel = EXTEN_CHANNEL_MAP.get(exten);
        if (myChannel != null) {
            try { managerConnection.sendAction(new RedirectAction(myChannel, "from-internal", target, 1)); } catch (Exception e) {}
        }
    }

    public boolean checkAmiConnection() {
        return managerConnection != null && managerConnection.getState() == org.asteriskjava.manager.ManagerConnectionState.CONNECTED;
    }

    private void sendInboundPopup(String exten, String callerId, String channel, String linkedId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "INBOUND_CALL");
            data.put("callerid", callerId);
            data.put("exten", exten);
            data.put("linkedid", linkedId);
            Map<String, Object> params = new HashMap<>();
            params.put("cmpycd", "haionnet"); params.put("phone", callerId);
            Map<String, Object> customer = inboundMapper.findCustomerByPhoneMap(params);
            if (customer != null) customer.forEach((k, v) -> data.put(k.toLowerCase(), v));
            else data.put("custnm", "미등록 고객");
            webSocketHandler.sendMessage(exten, objectMapper.writeValueAsString(data));
        } catch (Exception e) {}
    }

    private void sendCtiEvent(String exten, String type, String channel, String recFile) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", type); data.put("exten", exten);
            if (channel != null) data.put("channel", channel);
            if (recFile != null) data.put("recordingFile", recFile + ".wav");
            webSocketHandler.sendMessage(exten, objectMapper.writeValueAsString(data));
        } catch (Exception e) {}
    }

    private boolean isValidExt(String s) { return s != null && s.matches("^\\d{3,4}$"); }
    private String extractNumberOnly(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("(\\d{3,4})").matcher(s);
        return m.find() ? m.group(1) : null;
    }

    @PostConstruct public void init() { new Thread(() -> { try { managerConnection.addEventListener(this); if (!checkAmiConnection()) managerConnection.login(); } catch (Exception e) {} }).start(); }
    @PreDestroy public void cleanup() { try { if (managerConnection != null) managerConnection.logoff(); } catch (Exception ex) {} }
}
