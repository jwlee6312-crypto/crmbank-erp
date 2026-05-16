package com.crmbank.asterisk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.asterisk.mapper.AsteriskMapper;
import com.crmbank.asterisk.service.AsteriskAdminService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/crm/asterisk")
@RequiredArgsConstructor
public class AsteriskAdminController {

    private final AsteriskMapper asteriskMapper;
    private final AsteriskAdminService asteriskAdminService;

    /**
     * 💡 표준 IVR 시나리오 템플릿 조회 (화면에서 복사/적용용)
     */
    @GetMapping("/extension/template")
    public List<Map<String, Object>> getIvrTemplate() {
        log.info("📋 [IVR 템플릿 요청]");
        return convertListKeysToLowerCase(asteriskAdminService.getStandardIvrTemplate());
    }

    /**
     * 🎙️ ARS 음원 파일 (.wav) 업로드
     * 경로: /var/lib/asterisk/sounds/custom/
     */
    @PostMapping("/sound/upload")
    public Map<String, Object> uploadSound(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        // 💡 WSL 환경의 Asterisk 표준 경로
        String uploadDir = "/var/lib/asterisk/sounds/custom/";
        
        try {
            Path directory = Paths.get(uploadDir);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path targetPath = directory.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("✅ [ARS 음원 업로드 성공] 파일명: {}", file.getOriginalFilename());
            result.put("success", true);
            result.put("fileName", file.getOriginalFilename());
        } catch (IOException e) {
            log.error("❌ [ARS 음원 업로드 실패] ", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 📂 서버에 저장된 음원 파일 목록 조회
     */
    @GetMapping("/sound/list")
    public List<String> listSounds() {
        String dirPath = "/var/lib/asterisk/sounds/custom/";
        try (Stream<Path> stream = Files.list(Paths.get(dirPath))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.toLowerCase().endsWith(".wav"))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("❌ [음원 목록 조회 실패] ", e);
            return new ArrayList<>();
        }
    }

    // 1. 내선번호 (PJSIP) 조회/저장
    @GetMapping("/pjsip/search")
    public List<Map<String, Object>> searchPjsip(@RequestParam Map<String, Object> params) {
        return convertListKeysToLowerCase(asteriskMapper.selectPjsipList(params));
    }
    
    @PostMapping("/pjsip/save")
    public void savePjsip(@RequestBody List<Map<String, Object>> list) {
        asteriskAdminService.savePjsip(list);
    }

    // 2. 수신그룹 (Queue) 조회/저장
    @GetMapping("/queue/search")
    public List<Map<String, Object>> searchQueues(@RequestParam Map<String, Object> params) {
        return convertListKeysToLowerCase(asteriskMapper.selectQueueList(params));
    }

    //selectQueueMemberList
    @GetMapping("/queue/member/search")
    public List<Map<String, Object>> searchQueueMembers(@RequestParam String queue_name) {
        log.info("📋 [queue_name] queue_name: {}", queue_name);
        return convertListKeysToLowerCase(asteriskMapper.selectQueueMemberList(queue_name));
    }

    @PostMapping("/queue/save")
    public void saveQueue(@RequestBody List<Map<String, Object>> list) {
        asteriskAdminService.saveQueues(list);
    }

    // 3. 다이얼플랜 (Extensions) 조회/저장
    @GetMapping("/extension/search")
    public List<Map<String, Object>> searchExtensions(@RequestParam Map<String, Object> params) {
        return convertListKeysToLowerCase(asteriskMapper.selectExtensions(params));
    }

    @PostMapping("/extension/save")
    public void saveExtensions(@RequestBody List<Map<String, Object>> list) {
        asteriskAdminService.saveExtensions(list);
    }

    private List<Map<String, Object>> convertListKeysToLowerCase(List<Map<String, Object>> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(map -> {
            Map<String, Object> lowerMap = new HashMap<>();
            map.forEach((k, v) -> lowerMap.put(k.toLowerCase(), v));
            return lowerMap;
        }).collect(Collectors.toList());
    }
}
