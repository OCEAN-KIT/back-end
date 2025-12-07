package com.ocean.piuda.dashboard.controller;

import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.service.DashboardQueryService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "대시보드 작업 영역 및 통계 데이터 조회")
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    @GetMapping("/areas/{id}")
    @Operation(summary = "작업 영역 상세 조회", description = "ID 기반으로 작업 영역의 상세 데이터(성장률, 수질 등)를 조회합니다.")
    public ApiData<AreaDetailResponse> getAreaDetail(@PathVariable Long id) {
        return ApiData.ok(dashboardQueryService.getAreaDetail(id));
    }
}