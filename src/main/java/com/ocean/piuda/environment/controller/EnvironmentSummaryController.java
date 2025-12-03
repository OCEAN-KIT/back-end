package com.ocean.piuda.environment.controller;

import com.ocean.piuda.environment.dto.EnvironmentSummaryRequest;
import com.ocean.piuda.environment.dto.EnvironmentSummaryResponse;
import com.ocean.piuda.environment.service.EnvironmentSummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 해양 환경 요약 API 컨트롤러
 */
@RestController
@RequestMapping("/api/environment")
@RequiredArgsConstructor
public class EnvironmentSummaryController {

    private final EnvironmentSummaryService environmentSummaryService;

    /**
     * 해양 환경 요약 조회
     * 
     * GET /api/environment/summary?lat={lat}&lon={lon}
     * GET /api/environment/summary?pointId={pointId}
     * 
     * @param lat 위도 (선택, pointId가 있으면 무시)
     * @param lon 경도 (선택, pointId가 있으면 무시)
     * @param pointId 다이빙 포인트 ID (선택, 있으면 lat/lon 무시)
     * @return 환경 요약 정보
     */
    @GetMapping("/summary")
    public ResponseEntity<EnvironmentSummaryResponse> getEnvironmentSummary(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Long pointId
    ) {
        EnvironmentSummaryRequest request = EnvironmentSummaryRequest.builder()
                .lat(lat)
                .lon(lon)
                .pointId(pointId)
                .build();

        EnvironmentSummaryResponse response = environmentSummaryService.getEnvironmentSummary(request);
        return ResponseEntity.ok(response);
    }
}

