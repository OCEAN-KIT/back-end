package com.ocean.piuda.environment.controller;

import com.ocean.piuda.environment.dto.EnvironmentSummaryRequest;
import com.ocean.piuda.environment.dto.EnvironmentSummaryResponse;
import com.ocean.piuda.environment.service.EnvironmentSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
     */
    @GetMapping("/summary")
    public Mono<ResponseEntity<EnvironmentSummaryResponse>> getEnvironmentSummary(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Long pointId
    ) {
        EnvironmentSummaryRequest request = EnvironmentSummaryRequest.builder()
                .lat(lat)
                .lon(lon)
                .pointId(pointId)
                .build();

        return environmentSummaryService.getEnvironmentSummary(request)
                .map(ResponseEntity::ok);
    }
}