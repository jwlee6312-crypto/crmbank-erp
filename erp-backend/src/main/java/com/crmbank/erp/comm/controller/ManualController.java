//package net.haion.erp.controller.common;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@Slf4j
//@RestController
//@RequestMapping("/manual")
//public class ManualController {
//
//    @Value("${MANUAL_PATH}")
//    private String basePath;
//
//    @GetMapping("/{fileName}")
//    public ResponseEntity<Resource> getManual(@PathVariable String fileName) {
//        log.info("파일경로: {}", basePath);
//        log.info("파일명: {}", fileName);
//
//        Path path = Paths.get(basePath, fileName);
//        Resource resource = new FileSystemResource(path.toFile());
//
//        if (!resource.exists()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        // 파일 확장자 확인
//        String lower = fileName.toLowerCase();
//        MediaType mediaType;
//
//        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
//            mediaType = MediaType.IMAGE_JPEG;
//        } else if (lower.endsWith(".png")) {
//            mediaType = MediaType.IMAGE_PNG;
//        } else if (lower.endsWith(".pdf")) {
//            mediaType = MediaType.APPLICATION_PDF;
//        } else {
//            // 그 외 파일은 다운로드
//            mediaType = MediaType.APPLICATION_OCTET_STREAM;
//        }
//
//        return ResponseEntity.ok()
//            .contentType(mediaType)
//            .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
//            .body(resource);
//    }
//}
package com.crmbank.erp.comm.controller;

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

@Slf4j
@RestController
@RequestMapping("/manual")
public class ManualController {

    @Value("${MANUAL_PATH}")
    private String basePath;

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getManual(@PathVariable String fileName) {
        log.info("파일경로: {}", basePath);
        log.info("파일명: {}", fileName);

        // 확장자 목록
        String[] extensions = {".jpg", ".jpeg", ".png", ".pdf"};

        Path foundPath = null;

        // 하나씩 확장자를 붙여보며 존재하는 파일 탐색
        for (String ext : extensions) {
            Path p = Paths.get(basePath, fileName + ext);
            if (Files.exists(p)) {
                foundPath = p;
                fileName = fileName + ext; // 최종 파일명 갱신
                break;
            }
        }

        // 파일을 못 찾으면 404 반환
        if (foundPath == null) {
            log.warn("파일을 찾을 수 없습니다 : {}", fileName);
            return ResponseEntity.notFound().build();
        }

        log.info("파일: {}", foundPath);

        Resource resource = new FileSystemResource(foundPath.toFile());

        // MIME 타입 결정
        String lower = fileName.toLowerCase();
        MediaType mediaType;

        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (lower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
            .body(resource);
    }
}