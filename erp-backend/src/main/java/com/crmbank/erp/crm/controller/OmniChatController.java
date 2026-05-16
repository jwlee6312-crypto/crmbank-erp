package com.crmbank.erp.crm.controller;

import com.crmbank.erp.crm.dto.ConsultSaveRequest;
import com.crmbank.erp.crm.service.ConsultSaveService;
import com.crmbank.erp.crm.service.OmniChatwootService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/omni/chat")
@RequiredArgsConstructor
public class OmniChatController {

    private final OmniChatwootService omniChatwootService;
    private final ConsultSaveService consultSaveService; 

    @PostMapping("/inquiry")
    public Map<String, Object> sendInquiry(@RequestBody ChatRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            omniChatwootService.sendInquiry(request.getEmail(), request.getName(), request.getContent());
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    @PostMapping("/reply")
    public Map<String, Object> replyToCustomer(@RequestBody ChatRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            omniChatwootService.replyToCustomer(request.getEmail(), request.getContent());
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    @GetMapping("/messages")
    public List<Map> getMessages(@RequestParam String email) {
        return omniChatwootService.getMessages(email);
    }

    @PostMapping("/save-consolidated")
    public Map<String, Object> saveConsolidated(@RequestBody ConsultSaveRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            consultSaveService.saveOmniConsultation(request);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    @Data
    public static class ChatRequest {
        private String email;
        private String name;
        private String content;
    }
}
