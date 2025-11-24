package com.ocean.piuda.activity.controller;

import com.ocean.piuda.activity.dto.request.ActivityIngestRequest;
import com.ocean.piuda.activity.dto.response.ActivityIngestResponse;
import com.ocean.piuda.activity.dto.response.ActivitySummaryResponse;
import com.ocean.piuda.activity.service.GarminActivityService;
import com.ocean.piuda.activity.util.WatchApiKeyValidator;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final GarminActivityService activityService;
    private final WatchApiKeyValidator apiKeyValidator;

    @PostMapping
    @Operation(summary = "워치 활동 로그 수신", description = "Garmin 워치 앱에서 보낸 활동 데이터를 저장합니다.")
    public ApiData<ActivityIngestResponse> ingestActivity(
            @RequestHeader(value = "X-WATCH-API-KEY", required = false) String watchApiKey,
            @RequestBody ActivityIngestRequest request,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        apiKeyValidator.validate(watchApiKey);

        Long userId = (principal != null && principal.getUser() != null)
                ? principal.getUser().getId()
                : null;

        Long id = activityService.ingest(userId, request);
        return ApiData.created(new ActivityIngestResponse(id, "STORED"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "단건 조회", description = "저장된 활동 로그 요약을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiData<ActivitySummaryResponse> getOne(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        return ApiData.ok(activityService.getOne(id));
    }
}
