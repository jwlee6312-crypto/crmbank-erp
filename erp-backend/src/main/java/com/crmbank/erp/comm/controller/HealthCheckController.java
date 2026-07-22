package com.crmbank.erp.comm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public String healthCheck(jakarta.servlet.http.HttpServletRequest request) {
        return "<h1>🚀 ERP Backend System is Running!</h1>" +
               "<p>Your IP: <b>" + request.getRemoteAddr() + "</b></p>" +
               "<p>네트워크 연결이 완벽하게 성공했습니다. 핸드폰에서 이 화면이 보인다면 관문 설정이 모두 끝난 것입니다.</p>";
    }

    @GetMapping("/api")
    public String apiCheck(jakarta.servlet.http.HttpServletRequest request) {
        return "<h1>✅ API Gateway is Working! (Path: /api)</h1>" +
               "<p>백엔드와 Nginx 사이의 연결이 완벽합니다.</p>";
    }
}
