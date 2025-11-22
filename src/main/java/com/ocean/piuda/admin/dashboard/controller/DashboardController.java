package com.ocean.piuda.admin.dashboard.controller;

import com.ocean.piuda.admin.dashboard.dto.DashboardSummaryResponse;
import com.ocean.piuda.admin.dashboard.service.DashboardService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(
        name = "Admin Dashboard",
        description = "관리자 대시보드 통계 API입니다. 전체 제출 현황 통계를 조회할 수 있습니다."
)
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 통계 조회
     */
    @GetMapping("/summary")
    @Operation(summary = "대시보드 통계 조회", description = "전체 제출 현황 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
        return ApiData.ok(summary);
    }
}
