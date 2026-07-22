package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.service.ManualService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ManualController {

    private final ManualService manualService;

    @Value("${MANUAL_PATH:}")
    private String basePath;

    /**
     * 기존 이미지 기반 매뉴얼 조회 (호환성 유지)
     */
    @GetMapping("/manual/{fileName}")
    public ResponseEntity<Resource> getManual(@PathVariable String fileName) {
        // 확장자 목록
        String[] extensions = {".jpg", ".jpeg", ".png", ".pdf"};
        Path foundPath = null;

        for (String ext : extensions) {
            Path p = Paths.get(basePath, fileName + ext);
            if (Files.exists(p)) {
                foundPath = p;
                fileName = fileName + ext;
                break;
            }
        }

        if (foundPath == null) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(foundPath.toFile());
        String lower = fileName.toLowerCase();
        MediaType mediaType = lower.endsWith(".pdf") ? MediaType.APPLICATION_PDF : 
                          (lower.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG);

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
            .body(resource);
    }

    /**
     * DB 기반 매뉴얼 조회
     */
    @GetMapping("/api/manual/db/{progid}")
    public ResponseEntity<?> getManualDb(@PathVariable String progid) {
        return ResponseEntity.ok(manualService.getManual(progid));
    }

    /**
     * DB 기반 매뉴얼 저장
     */
    @PostMapping("/api/manual/db/save")
    public ResponseEntity<?> saveManualDb(@RequestBody Map<String, Object> params) {
        try {
            log.info("매뉴얼 저장 시도: {}", params.get("progid"));
            manualService.saveManual(params);
            return ResponseEntity.ok("저장되었습니다.");
        } catch (Exception e) {
            log.error("매뉴얼 저장 실패: ", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
