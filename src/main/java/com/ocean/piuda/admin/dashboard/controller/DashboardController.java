package com.ocean.piuda.admin.dashboard.controller;

import com.ocean.piuda.admin.dashboard.dto.DashboardSummaryResponse;
import com.ocean.piuda.admin.dashboard.service.DashboardService;
import com.ocean.piuda.global.api.dto.ApiData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 통계 조회
     */
    @GetMapping("/summary")
    public ApiData<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
        return ApiData.ok(summary);
    }
}
