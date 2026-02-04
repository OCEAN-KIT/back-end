//package com.ocean.piuda.admin.report.controller;
//import org.springframework.http.ContentDisposition;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import java.nio.charset.StandardCharsets;
//
//import com.ocean.piuda.admin.report.dto.request.ReportDraftByIdsRequest;
//import com.ocean.piuda.admin.report.dto.request.ReportDraftByPeriodRequest;
//import com.ocean.piuda.admin.report.dto.response.ReportDraftResponse;
//import com.ocean.piuda.admin.report.service.ReportDraftService;
//import com.ocean.piuda.global.api.dto.ApiData;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/admin/reports/drafts")
//@RequiredArgsConstructor
//@Tag(name = "Admin Report Draft", description = "선택한 해양 활동 제출물로 리포트 초안을 Gemini로 생성합니다.")
//public class ReportDraftController {
//
//    private final ReportDraftService reportDraftService;
//
//    @PostMapping("/by-ids")
//    @Operation(
//            summary = "리포트 초안 생성 (IDs 기반)",
//            description = "선택한 submission ids(기본: APPROVED)를 기반으로 내부/대외홍보용 리포트 초안을 생성합니다."
//    )
//    public ApiData<ReportDraftResponse> byIds(@RequestBody @Valid ReportDraftByIdsRequest request) {
//        return ApiData.ok(reportDraftService.generateByIds(request));
//    }
//
//    @PostMapping("/by-period")
//    @Operation(
//            summary = "리포트 초안 생성 (기간 기반)",
//            description = "dateFrom~dateTo 기간(기본: APPROVED)을 기반으로 내부/대외홍보용 리포트 초안을 생성합니다."
//    )
//    public ApiData<ReportDraftResponse> byPeriod(@RequestBody @Valid ReportDraftByPeriodRequest request) {
//        return ApiData.ok(reportDraftService.generateByPeriod(request));
//    }
//
//    @PostMapping(
//            value = "/by-ids/pdf",
//            produces = { MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE }
//    )
//    @Operation(summary = "리포트 PDF 생성 (IDs 기반)", description = "리포트 초안을 PDF로 생성하여 다운로드합니다.")
//    public ResponseEntity<byte[]> byIdsPdf(@RequestBody @Valid ReportDraftByIdsRequest request) {
//        var pdf = reportDraftService.generatePdfByIds(request);
//
//        ContentDisposition cd = ContentDisposition.attachment()
//                .filename(pdf.fileName(), StandardCharsets.UTF_8)
//                .build();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDisposition(cd);
//        headers.setContentLength(pdf.bytes().length);
//        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
//        headers.add("Pragma", "no-cache");
//        headers.add("Expires", "0");
//
//        return ResponseEntity.ok().headers(headers).body(pdf.bytes());
//    }
//
//    @PostMapping(
//            value = "/by-period/pdf",
//            produces = { MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE }
//    )
//    @Operation(summary = "리포트 PDF 생성 (기간 기반)", description = "리포트 초안을 PDF로 생성하여 다운로드합니다.")
//    public ResponseEntity<byte[]> byPeriodPdf(@RequestBody @Valid ReportDraftByPeriodRequest request) {
//        var pdf = reportDraftService.generatePdfByPeriod(request);
//
//        ContentDisposition cd = ContentDisposition.attachment()
//                .filename(pdf.fileName(), StandardCharsets.UTF_8)
//                .build();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDisposition(cd);
//        headers.setContentLength(pdf.bytes().length);
//        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
//        headers.add("Pragma", "no-cache");
//        headers.add("Expires", "0");
//
//        return ResponseEntity.ok().headers(headers).body(pdf.bytes());
//    }
//
//}
