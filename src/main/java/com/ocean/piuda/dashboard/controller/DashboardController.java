package com.ocean.piuda.dashboard.controller;

import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.dto.response.AreaMarkerResponse;
import com.ocean.piuda.dashboard.dto.response.AreaStatResponse;
import com.ocean.piuda.dashboard.dto.response.IdResponse;
import com.ocean.piuda.dashboard.service.DashboardCommandService;
import com.ocean.piuda.dashboard.service.DashboardQueryService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.ocean.piuda.dashboard.dto.request.*;
import jakarta.validation.Valid;
import com.ocean.piuda.dashboard.dto.request.LogPageRequest;
import com.ocean.piuda.dashboard.dto.response.*;
import com.ocean.piuda.global.api.dto.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import com.ocean.piuda.dashboard.dto.request.AreaPageRequest;
import com.ocean.piuda.dashboard.enums.HabitatType;
import com.ocean.piuda.dashboard.enums.ProjectLevel;
import com.ocean.piuda.dashboard.enums.RestorationRegion;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "대시보드 작업 영역(PostGIS) 및 통계 데이터 조회")
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;
    private final DashboardCommandService dashboardCommandService;

    /**
     * ProjectArea
     */
    @PostMapping("/areas")
    @Operation(summary = "작업 영역 생성")
    public ApiData<IdResponse> createArea(@RequestBody @Valid CreateProjectAreaRequest req) {
        return ApiData.ok(new IdResponse(dashboardCommandService.createArea(req)));
    }

    @PatchMapping("/areas/{id}")
    @Operation(summary = "작업 영역 부분 수정(PATCH)")
    public ApiData<IdResponse> patchArea(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProjectAreaRequest req
    ) {
        dashboardCommandService.updateArea(id, req);
        return ApiData.ok(new IdResponse(id));
    }


    @DeleteMapping("/areas/{id}")
    @Operation(summary = "작업 영역 삭제")
    public ApiData<IdResponse> deleteArea(@PathVariable Long id) {
        dashboardCommandService.deleteArea(id);
        return ApiData.ok(new IdResponse(id));
    }


    /**
     * ProjectArea Representative Species
     */
    @PatchMapping("/areas/{id}/representative-species")
    @Operation(summary = "작업 영역 대표종 설정", description = "성장 추이 차트에 표시될 대표 종을 설정합니다.")
    public ApiData<IdResponse> setRepresentativeSpecies(
            @PathVariable Long id,
            @RequestBody @Valid SetRepresentativeSpeciesRequest req
    ) {
        dashboardCommandService.setRepresentativeSpecies(id, req);
        return ApiData.ok(new IdResponse(id));
    }

    @GetMapping("/areas/{id}/species")
    @Operation(summary = "영역 내 이식된 종 목록 조회", description = "대표종 설정 팝업 등에서 사용할 '이 영역에 존재하는 종' 목록을 반환합니다.")
    public ApiData<List<AreaSpeciesResponse>> getAreaSpecies(@PathVariable Long id) {
        return ApiData.ok(dashboardQueryService.getAreaSpeciesCandidates(id));
    }

    @GetMapping("/areas/{id}/representative-species")
    @Operation(summary = "작업 영역 현재 대표종 조회", description = "현재 설정된 대표종 정보를 반환합니다. 설정되지 않은 경우 null을 반환합니다.")
    public ApiData<AreaSpeciesResponse> getRepresentativeSpecies(@PathVariable Long id) {
        return ApiData.ok(dashboardQueryService.getRepresentativeSpecies(id));
    }


    /**
     * TransplantLog
     */

    @GetMapping("/areas/{areaId}/transplants/{logId}")
    @Operation(summary = "이식 로그 단건 조회")
    public ApiData<TransplantLogResponse> getTransplantLog(
            @PathVariable Long areaId,
            @PathVariable Long logId
    ) {
        return ApiData.ok(dashboardQueryService.getTransplantLog(areaId, logId));
    }

    @GetMapping("/areas/{areaId}/transplants")
    @Operation(summary = "이식 로그 기간 기반 페이징 목록 조회")
    public ApiData<PageResponse<TransplantLogResponse>> getTransplantLogs(
            @PathVariable Long areaId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Valid LogPageRequest pageReq
    ) {
        return ApiData.ok(dashboardQueryService.getTransplantLogs(areaId, from, to, pageReq));
    }


    @PostMapping("/areas/{areaId}/transplants")
    @Operation(summary = "이식 로그 생성")
    public ApiData<IdResponse> createTransplant(@PathVariable Long areaId, @RequestBody @Valid CreateTransplantLogRequest req) {
        return ApiData.ok(new IdResponse(dashboardCommandService.createTransplant(areaId, req)));
    }

    @PatchMapping("/areas/{areaId}/transplants/{logId}")
    @Operation(summary = "이식 로그 부분 수정(PATCH)")
    public ApiData<IdResponse> patchTransplant(
            @PathVariable Long areaId,
            @PathVariable Long logId,
            @RequestBody @Valid UpdateTransplantLogRequest req
    ) {
        dashboardCommandService.updateTransplant(areaId, logId, req);
        return ApiData.ok(new IdResponse(logId));
    }


    @DeleteMapping("/areas/{areaId}/transplants/{logId}")
    @Operation(summary = "이식 로그 삭제")
    public ApiData<IdResponse> deleteTransplant(@PathVariable Long areaId, @PathVariable Long logId) {
        dashboardCommandService.deleteTransplant(areaId, logId);
        return ApiData.ok(new IdResponse(logId));
    }


    /**
     * GrowthLog
     */
    @GetMapping("/areas/{areaId}/growth-logs/{logId}")
    @Operation(summary = "성장 로그 단건 조회")
    public ApiData<GrowthLogResponse> getGrowthLog(
            @PathVariable Long areaId,
            @PathVariable Long logId
    ) {
        return ApiData.ok(dashboardQueryService.getGrowthLog(areaId, logId));
    }

    @GetMapping("/areas/{areaId}/growth-logs")
    @Operation(summary = "성장 로그 기간 기반 페이징 목록 조회")
    public ApiData<PageResponse<GrowthLogResponse>> getGrowthLogs(
            @PathVariable Long areaId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Valid LogPageRequest pageReq
    ) {
        return ApiData.ok(dashboardQueryService.getGrowthLogs(areaId, from, to, pageReq));
    }


    @PostMapping("/areas/{areaId}/growth-logs")
    @Operation(summary = "성장 로그 생성")
    public ApiData<IdResponse> createGrowth(@PathVariable Long areaId, @RequestBody @Valid CreateGrowthLogRequest req) {
        return ApiData.ok(new IdResponse(dashboardCommandService.createGrowth(areaId, req)));
    }

    @PatchMapping("/areas/{areaId}/growth-logs/{logId}")
    @Operation(summary = "성장 로그 부분 수정(PATCH)")
    public ApiData<IdResponse> patchGrowth(
            @PathVariable Long areaId,
            @PathVariable Long logId,
            @RequestBody @Valid UpdateGrowthLogRequest req
    ) {
        dashboardCommandService.updateGrowth(areaId, logId, req);
        return ApiData.ok(new IdResponse(logId));
    }


    @DeleteMapping("/areas/{areaId}/growth-logs/{logId}")
    @Operation(summary = "성장 로그 삭제")
    public ApiData<IdResponse> deleteGrowth(@PathVariable Long areaId, @PathVariable Long logId) {
        dashboardCommandService.deleteGrowth(areaId, logId);
        return ApiData.ok(new IdResponse(logId));
    }


    /**
     * WaterLog
     */
    @GetMapping("/areas/{areaId}/water-logs/{logId}")
    @Operation(summary = "환경 로그 단건 조회")
    public ApiData<WaterLogResponse> getWaterLog(
            @PathVariable Long areaId,
            @PathVariable Long logId
    ) {
        return ApiData.ok(dashboardQueryService.getWaterLog(areaId, logId));
    }

    @GetMapping("/areas/{areaId}/water-logs")
    @Operation(summary = "환경 로그 기간 기반 페이징 목록 조회")
    public ApiData<PageResponse<WaterLogResponse>> getWaterLogs(
            @PathVariable Long areaId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Valid LogPageRequest pageReq
    ) {
        return ApiData.ok(dashboardQueryService.getWaterLogs(areaId, from, to, pageReq));
    }


    @PostMapping("/areas/{areaId}/water-logs")
    @Operation(summary = "환경 로그 생성")
    public ApiData<IdResponse> createWater(@PathVariable Long areaId, @RequestBody @Valid CreateWaterLogRequest req) {
        return ApiData.ok(new IdResponse(dashboardCommandService.createWater(areaId, req)));
    }

    @PatchMapping("/areas/{areaId}/water-logs/{logId}")
    @Operation(summary = "환경 로그 부분 수정(PATCH)")
    public ApiData<IdResponse> patchWater(
            @PathVariable Long areaId,
            @PathVariable Long logId,
            @RequestBody @Valid UpdateWaterLogRequest req
    ) {
        dashboardCommandService.updateWater(areaId, logId, req);
        return ApiData.ok(new IdResponse(logId));
    }



    @DeleteMapping("/areas/{areaId}/water-logs/{logId}")
    @Operation(summary = "환경 로그 삭제")
    public ApiData<IdResponse> deleteWater(@PathVariable Long areaId, @PathVariable Long logId) {
        dashboardCommandService.deleteWater(areaId, logId);
        return ApiData.ok(new IdResponse(logId));
    }

    /**
     * MediaLog
     */
    @GetMapping("/areas/{areaId}/media-logs/{logId}")
    @Operation(summary = "미디어 로그 단건 조회")
    public ApiData<MediaLogResponse> getMediaLog(
            @PathVariable Long areaId,
            @PathVariable Long logId
    ) {
        return ApiData.ok(dashboardQueryService.getMediaLog(areaId, logId));
    }

    @GetMapping("/areas/{areaId}/media-logs")
    @Operation(summary = "미디어 로그 기간 기반 페이징 목록 조회")
    public ApiData<PageResponse<MediaLogResponse>> getMediaLogs(
            @PathVariable Long areaId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Valid LogPageRequest pageReq
    ) {
        return ApiData.ok(dashboardQueryService.getMediaLogs(areaId, from, to, pageReq));
    }


    @PostMapping("/areas/{areaId}/media-logs")
    @Operation(summary = "미디어 로그 생성")
    public ApiData<IdResponse> createMedia(@PathVariable Long areaId, @RequestBody @Valid CreateMediaLogRequest req) {
        return ApiData.ok(new IdResponse(dashboardCommandService.createMedia(areaId, req)));
    }

    @PatchMapping("/areas/{areaId}/media-logs/{logId}")
    @Operation(summary = "미디어 로그 부분 수정(PATCH)")
    public ApiData<IdResponse> patchMedia(
            @PathVariable Long areaId,
            @PathVariable Long logId,
            @RequestBody @Valid UpdateMediaLogRequest req
    ) {
        dashboardCommandService.updateMedia(areaId, logId, req);
        return ApiData.ok(new IdResponse(logId));
    }


    @DeleteMapping("/areas/{areaId}/media-logs/{logId}")
    @Operation(summary = "미디어 로그 삭제")
    public ApiData<IdResponse> deleteMedia(@PathVariable Long areaId, @PathVariable Long logId) {
        dashboardCommandService.deleteMedia(areaId, logId);
        return ApiData.ok(new IdResponse(logId));
    }


    @GetMapping("/areas")
    @Operation(
            summary = "작업 영역 목록 조회(페이징) + 검색/필터",
            description = """
            관리자용 작업영역 전체 목록 조회 API입니다.
            - region(포항/울진), level(프로젝트 단계), habitat, 기간(from~to), 키워드(name) 필터 지원
            - page/size/sort 페이징 지원
            """
    )
    public ApiData<PageResponse<ProjectAreaListItemResponse>> getAreas(
            @RequestParam(required = false) RestorationRegion region,
            @RequestParam(required = false) ProjectLevel level,
            @RequestParam(required = false) HabitatType habitat,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @Valid AreaPageRequest pageReq
    ) {
        return ApiData.ok(dashboardQueryService.getAreas(region, level, habitat, from, to, keyword, pageReq));
    }


    /**
     * 대시보드 조회
     */

    // 1. 상세 데이터 API
    @GetMapping("/areas/{id}")
    @Operation(summary = "작업 영역 상세 조회", description = "ID 기반으로 작업 영역의 상세 데이터(성장률, 수질 등)를 조회합니다.")
    public ApiData<AreaDetailResponse> getAreaDetail(@PathVariable Long id) {
        return ApiData.ok(dashboardQueryService.getAreaDetail(id));
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
