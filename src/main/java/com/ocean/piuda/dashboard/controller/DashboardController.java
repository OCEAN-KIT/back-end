package com.ocean.piuda.dashboard.controller;

import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.dto.response.AreaStatResponse;
import com.ocean.piuda.dashboard.service.DashboardQueryService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "대시보드 작업 영역(PostGIS) 및 통계 데이터 조회")
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    @GetMapping("/areas/{id}")
    @Operation(summary = "작업 영역 상세 조회", description = "ID 기반으로 작업 영역의 상세 데이터(성장률, 수질 등)를 조회합니다.")
    public ApiData<AreaDetailResponse> getAreaDetail(@PathVariable Long id) {
        return ApiData.ok(dashboardQueryService.getAreaDetail(id));
    }

    @GetMapping("/areas/nearby")
    @Operation(summary = "반경 기반 검색", description = "중심 좌표와 반경(km)으로 작업 영역을 검색합니다.")
    public ApiData<List<AreaDetailResponse>> getNearby(
            @Parameter(description = "위도") @RequestParam Double lat,
            @Parameter(description = "경도") @RequestParam Double lon,
            @Parameter(description = "반경(km), 기본값 5.0") @RequestParam(defaultValue = "5.0") Double radius) {
        return ApiData.ok(dashboardQueryService.getNearbyAreas(lat, lon, radius));
    }

    @GetMapping("/areas/bbox")
    @Operation(summary = "뷰포트(BBox) 검색", description = "지도 화면 영역(Min/Max LatLon) 내의 데이터만 조회합니다.")
    public ApiData<List<AreaDetailResponse>> getBBox(
            @RequestParam Double minLat, @RequestParam Double minLon,
            @RequestParam Double maxLat, @RequestParam Double maxLon) {
        return ApiData.ok(dashboardQueryService.getAreasInBBox(minLat, minLon, maxLat, maxLon));
    }

    @GetMapping("/areas/nearest")
    @Operation(summary = "가장 가까운 영역 찾기 (KNN)", description = "내 위치에서 가장 가까운 N개의 작업 영역을 찾습니다.")
    public ApiData<List<AreaDetailResponse>> getNearest(
            @RequestParam Double lat, @RequestParam Double lon,
            @RequestParam(defaultValue = "3") Integer limit) {
        return ApiData.ok(dashboardQueryService.getNearestAreas(lat, lon, limit));
    }

    @GetMapping("/stats/nearby")
    @Operation(summary = "반경 내 통계 요약", description = "반경 내 프로젝트 수, 총 면적, 평균 수심 등을 집계합니다.")
    public ApiData<AreaStatResponse> getStats(
            @RequestParam Double lat, @RequestParam Double lon,
            @RequestParam(defaultValue = "10.0") Double radius) {
        return ApiData.ok(dashboardQueryService.getNearbyStats(lat, lon, radius));
    }
}