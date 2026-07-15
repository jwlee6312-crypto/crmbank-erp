package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/comm/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @GetMapping("/display/{fileName:.+}")
    public ResponseEntity<Resource> displayFile(@PathVariable String fileName) {
        try {
            File file = fileStorageService.getDownloadFile(fileName);
            Resource resource = new UrlResource(file.toURI());
            
            if (resource.exists() || resource.isReadable()) {
                String contentType = "image/jpeg";
                if (fileName.toLowerCase().endsWith(".png")) contentType = "image/png";
                else if (fileName.toLowerCase().endsWith(".gif")) contentType = "image/gif";
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> uploadProfile(@RequestParam("file") MultipartFile file, @RequestParam("userid") String userid) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 누락되었습니다.");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 파일명 중복 방지
            String fileName = "profile_" + userid + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            
            fileStorageService.saveFileWithName(file, fileName);

            log.info("📸 [프로필 업로드 성공] ID: {}, Path: {}", userid, fileName);
            return ResponseEntity.ok(Map.of("filePath", fileName));
        } catch (Exception e) {
            log.error("❌ [프로필 업로드 에러]: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
