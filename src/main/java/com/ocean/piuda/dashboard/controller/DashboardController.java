package com.ocean.piuda.dashboard.controller;

import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.dto.response.AreaMarkerResponse;
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

    // 1. 상세 데이터 API
    @GetMapping("/areas/{id}")
    @Operation(summary = "작업 영역 상세 조회", description = "ID 기반으로 작업 영역의 상세 데이터(성장률, 수질 등)를 조회합니다.")
    public ApiData<AreaDetailResponse> getAreaDetail(@PathVariable Long id) {
        return ApiData.ok(dashboardQueryService.getAreaDetail(id));
    }

    /**
     * @deprecated
     *   - 실제 화면 구현에서는 마커 클릭 시 ID 기반 단건 상세 조회만 사용하고 있어, 반경 기준 상세 리스트는 필요하지 않아 미사용 상태입니다.
     *   - 설계 의도: 특정 좌표/반경 기준으로 여러 작업 영역의 '상세 데이터'를 한 번에 조회하는 분석/리포트 용도.
     *   - 향후 관리자용 리포트/분석 화면 등에서 재활용 가능성을 고려해 일단 보존합니다.
     */
    @Deprecated
    @GetMapping("/areas/nearby")
    @Operation(
            summary = "[Deprecated] 반경 기반 작업 영역 상세 검색",
            description = """
                설계 의도: 중심 좌표와 반경(km) 기준으로 여러 작업 영역의 '상세 데이터'를 한 번에 조회
                                
                실제 구현에서는 영역별 상세는 ID 기반 단건 조회로 충분하여, 
                상세 데이터는 마커 클릭 시 /areas/{id}로 개별 조회하면 충분하다고 판단되어 미사용 상태입니다.
                향후 다른 분석/관리 도메인에서 재활용 가능성을 고려해 보존합니다.                
                """,
            deprecated = true
    )
    public ApiData<List<AreaDetailResponse>> getNearby(
            @Parameter(description = "위도") @RequestParam Double lat,
            @Parameter(description = "경도") @RequestParam Double lon,
            @Parameter(description = "반경(km), 기본값 5.0") @RequestParam(defaultValue = "5.0") Double radius) {
        return ApiData.ok(dashboardQueryService.getNearbyAreas(lat, lon, radius));
    }

    /**
     * @deprecated
     *   - 실제 구현에서는 뷰포트 기준으론 마커용 경량 데이터만 필요하고, 상세 데이터는 마커 클릭 시 /areas/{id}로 개별 조회하면 충분하다고 판단되어 미사용 상태입니다.
     *   - 설계 의도 : 지도 뷰포트(BBox)에 포함된 모든 작업 영역의 '상세 데이터'를 한 번에 조회.
     *   - 향후 다른 분석/관리 도메인에서 재활용 가능성을 고려해 보존합니다.
     */
    @Deprecated
    @GetMapping("/areas/bbox")
    @Operation(
            summary = "[Deprecated] 뷰포트(BBox) 내 작업 영역 상세 검색",
            description = """
                설계 의도 : 지도 뷰포트(BBox)에 포함된 모든 작업 영역의 '상세 데이터'를 한 번에 조회.
                
                실제 구현에서는 뷰포트 기준으론 마커용 경량 데이터만 필요하고, 
                상세 데이터는 마커 클릭 시 /areas/{id}로 개별 조회하면 충분하다고 판단되어 미사용 상태입니다.
                향후 다른 분석/관리 도메인에서 재활용 가능성을 고려해 보존합니다.                
                """,
            deprecated = true
    )
    public ApiData<List<AreaDetailResponse>> getBBox(
            @RequestParam Double minLat, @RequestParam Double minLon,
            @RequestParam Double maxLat, @RequestParam Double maxLon) {
        return ApiData.ok(dashboardQueryService.getAreasInBBox(minLat, minLon, maxLat, maxLon));
    }

    /**
     * @deprecated
     *   - 실제 UX에서는 리스트/지도에는 요약 정보만 보여주고, 상세는 클릭 시 단건 조회하는 방식으로 충분해 이 API는 사용하지 않게 되었습니다.
     *   - 초기 설계 의도: "내 위치에서 가장 가까운 N개 작업 영역의 상세 데이터"를 한 번에 조회하는 기능.
     *   - 다만 KNN 기반 상세 리스트가 필요한 별도 화면(예: 관리자 분석 페이지 등)이 생길 수 있어 보존합니다.
     */
    @Deprecated
    @GetMapping("/areas/nearest")
    @Operation(
            summary = "[Deprecated] 가장 가까운 작업 영역 상세 검색",
            description = """
                설계 의도: "내 위치에서 가장 가까운 N개 작업 영역의 상세 데이터"를 한 번에 조회하는 기능.
               
                실제 UX에서는 상세는 클릭 시 단건 조회하는 방식으로 충분해 이 API는 사용하지 않습니다.
                잠재적 재활용(예: 관리자/분석 화면)을 위해 보존합니다.
                """,
            deprecated = true
    )
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

    // 2. 지도 마커 전용 API

    @GetMapping("/markers/bbox")
    @Operation(
            summary = "지도 뷰포트 내 마커 목록",
            description = """
                현재 지도 화면(BBox)에 포함된 작업 영역의 '마커용 요약 정보'를 조회합니다.
                - 상세 데이터(시계열, 수질, 생물다양성 등)는 포함하지 않습니다.
                - 마커 클릭 시에는 /api/dashboard/areas/{id} API로 상세 정보를 조회하는 패턴을 권장합니다.
                """
    )
    public ApiData<List<AreaMarkerResponse>> getMarkersInBBox(
            @Parameter(description = "최소 위도 (south)") @RequestParam Double minLat,
            @Parameter(description = "최소 경도 (west)") @RequestParam Double minLon,
            @Parameter(description = "최대 위도 (north)") @RequestParam Double maxLat,
            @Parameter(description = "최대 경도 (east)") @RequestParam Double maxLon
    ) {
        return ApiData.ok(
                dashboardQueryService.getMarkersInBBox(minLat, minLon, maxLat, maxLon)
        );
    }

    @GetMapping("/markers/nearby")
    @Operation(
            summary = "반경 내 마커 목록",
            description = """
                중심 좌표와 반경(km)으로 주변 작업 영역의 '마커용 요약 정보'를 조회합니다.
                - 지도/리스트 표시용으로 설계된 경량 응답입니다.
                - 상세 데이터가 필요하면 /api/dashboard/areas/{id} API를 함께 사용하세요.
                """
    )
    public ApiData<List<AreaMarkerResponse>> getNearbyMarkers(
            @Parameter(description = "위도") @RequestParam Double lat,
            @Parameter(description = "경도") @RequestParam Double lon,
            @Parameter(description = "반경(km), 기본값 5.0") @RequestParam(defaultValue = "5.0") Double radius
    ) {
        return ApiData.ok(
                dashboardQueryService.getNearbyMarkers(lat, lon, radius)
        );
    }

    @GetMapping("/markers/nearest")
    @Operation(
            summary = "가장 가까운 마커 목록 (KNN)",
            description = """
                내 위치에서 가장 가까운 N개의 작업 영역을 KNN(Nearest Neighbor) 순으로 조회합니다.
                - 응답은 지도/리스트 표시용 '마커 요약 데이터'만 포함합니다.
                - 마커 클릭 시 /api/dashboard/areas/{id}로 상세 정보를 조회하는 패턴을 권장합니다.
                """
    )
    public ApiData<List<AreaMarkerResponse>> getNearestMarkers(
            @Parameter(description = "위도") @RequestParam Double lat,
            @Parameter(description = "경도") @RequestParam Double lon,
            @Parameter(description = "가져올 마커 개수, 기본값 3") @RequestParam(defaultValue = "3") Integer limit
    ) {
        return ApiData.ok(
                dashboardQueryService.getNearestMarkers(lat, lon, limit)
        );
    }

}
