package com.ocean.piuda.admin.export.controller;

import com.ocean.piuda.admin.export.dto.request.ExportRequest;
import com.ocean.piuda.admin.export.dto.response.ExportJobResponse;
import com.ocean.piuda.admin.export.service.ExportService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/exports")
@RequiredArgsConstructor
@Slf4j
public class ExportController {

    private final ExportService exportService;
    private final TokenUserService tokenUserService;

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadExport(
            @Valid @RequestBody ExportRequest request
    ) {
        byte[] csvBytes = exportService.generateExportFile(request);
        String fileName = exportService.generateFileName(request.getFormat());
        
        try {
            String requestedBy = tokenUserService.getCurrentUser().getEmail() != null 
                    ? tokenUserService.getCurrentUser().getEmail() 
                    : tokenUserService.getCurrentUser().getUsername();
            exportService.saveExportHistory(request, requestedBy);
        } catch (Exception e) {
            log.warn("Export 이력 저장 실패: {}", e.getMessage());
        }
        
        // Content-Type 설정 (CSV의 경우)
        MediaType contentType = request.getFormat() == com.ocean.piuda.admin.common.enums.ExportFormat.CSV 
                ? new MediaType("text", "csv", StandardCharsets.UTF_8)
                : MediaType.APPLICATION_OCTET_STREAM;
        
        // Content-Disposition 헤더 설정 (한글 파일명 지원)
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setContentDisposition(contentDisposition);
        headers.setContentLength(csvBytes.length);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    @GetMapping
    public ApiData<List<ExportJobResponse>> getExportHistory() {
        List<ExportJobResponse> history = exportService.getExportHistory();
        return ApiData.ok(history);
    }
}
