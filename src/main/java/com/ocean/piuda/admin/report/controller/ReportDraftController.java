package com.ocean.piuda.admin.report.controller;

import com.ocean.piuda.admin.report.dto.request.ReportDraftByIdsRequest;
import com.ocean.piuda.admin.report.dto.request.ReportDraftByPeriodRequest;
import com.ocean.piuda.admin.report.dto.response.ReportDraftResponse;
import com.ocean.piuda.admin.report.service.ReportDraftService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports/drafts")
@RequiredArgsConstructor
@Tag(name = "Admin Report Draft", description = "선택한 해양 활동 제출물로 리포트 초안을 Gemini로 생성합니다.")
public class ReportDraftController {

    private final ReportDraftService reportDraftService;

    @PostMapping("/by-ids")
    @Operation(
            summary = "리포트 초안 생성 (IDs 기반)",
            description = "선택한 submission ids(기본: APPROVED)를 기반으로 내부/대외홍보용 리포트 초안을 생성합니다."
    )
    public ApiData<ReportDraftResponse> byIds(@RequestBody @Valid ReportDraftByIdsRequest request) {
        return ApiData.ok(reportDraftService.generateByIds(request));
    }

    @PostMapping("/by-period")
    @Operation(
            summary = "리포트 초안 생성 (기간 기반)",
            description = "dateFrom~dateTo 기간(기본: APPROVED)을 기반으로 내부/대외홍보용 리포트 초안을 생성합니다."
    )
    public ApiData<ReportDraftResponse> byPeriod(@RequestBody @Valid ReportDraftByPeriodRequest request) {
        return ApiData.ok(reportDraftService.generateByPeriod(request));
    }
}
