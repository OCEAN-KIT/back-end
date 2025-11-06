package com.ocean.piuda.admin.export.controller;

import com.ocean.piuda.admin.export.dto.request.ExportRequest;
import com.ocean.piuda.admin.export.dto.response.ExportJobResponse;
import com.ocean.piuda.admin.export.service.ExportService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Admin Export",
        description = "제출 데이터 내보내기 API입니다. 승인된 데이터를 CSV 형식으로 내보내고 이력을 조회할 수 있습니다."
)
public class ExportController {

    private final ExportService exportService;
    private final TokenUserService tokenUserService;

    @PostMapping("/download")
    @Operation(summary = "데이터 내보내기 (CSV 다운로드)", description = "승인된 제출 데이터를 CSV 파일로 내보냅니다. 파일이 바로 다운로드됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV 파일 다운로드 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류 (format 누락 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
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
    @Operation(summary = "내보내기 이력 조회", description = "내보내기 작업 이력을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<List<ExportJobResponse>> getExportHistory() {
        List<ExportJobResponse> history = exportService.getExportHistory();
        return ApiData.ok(history);
    }
}
