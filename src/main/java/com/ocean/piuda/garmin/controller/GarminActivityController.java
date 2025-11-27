package com.ocean.piuda.garmin.controller;


import com.ocean.piuda.garmin.dto.request.ActivitySessionRequest;
import com.ocean.piuda.garmin.dto.request.GarminActivityPageRequest;
import com.ocean.piuda.garmin.dto.response.ActivitySessionResponse;
import com.ocean.piuda.garmin.service.GarminActivityCommandService;
import com.ocean.piuda.garmin.service.GarminActivityQueryService;
import com.ocean.piuda.garmin.util.WatchApiKeyValidator;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.global.api.dto.PageResponse;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class GarminActivityController {

    private final GarminActivityQueryService activityQueryService;
    private final GarminActivityCommandService activityCommandService;
    private final WatchApiKeyValidator apiKeyValidator;
    private final TokenUserService tokenUserService;

    @PostMapping
    @Operation(summary = "워치 활동 로그 수신", description = "활동 데이터를 저장하고, 저장된 전체 구조(Session)를 반환합니다.")
    public ApiData<ActivitySessionResponse> createActivityLog(
            @RequestHeader(value = "X-WATCH-API-KEY", required = false) String watchApiKey,
            @RequestBody ActivitySessionRequest request
    ) {
        apiKeyValidator.validate(watchApiKey);

        ActivitySessionResponse response = activityCommandService.createActivityLog(request);

        return ApiData.created(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "로그 ID 기준 단건 조회", description = "저장된 활동 로그 단건을 로그 ID 기준으로 조회합니다.")
    public ApiData<ActivitySessionResponse> searchByLogId(
            @PathVariable Long id
    ) {
        return ApiData.ok(activityQueryService.searchByLogId(id));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "세션 ID 기준 조회", description = "sessionId 기준으로 한 세션 전체를 조회합니다.")
    public ApiData<ActivitySessionResponse> searchBySessionId(
            @PathVariable String sessionId
    ) {
        return ApiData.ok(activityQueryService.searchBySessionId(sessionId));
    }

    @PostMapping("/my/sessions")
    @Operation(
            summary = "내 활동 세션 목록 조회",
            description = "로그인한 유저 기준으로 세션 단위 활동 로그를 페이지네이션하여 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiData<PageResponse<ActivitySessionResponse>> getMySessions(
            @RequestBody @Valid GarminActivityPageRequest req
    ) {
        Long userId = tokenUserService.getCurrentUser().getId();
        PageResponse<ActivitySessionResponse> res = activityQueryService.searchMySessions(userId, req);
        return ApiData.ok(res);
    }

    @PostMapping("/user/{userId}/sessions")
    @Operation(
            summary = "특정 유저의 활동 세션 목록 조회",
            description = "지정한 userId 기준으로 세션 단위 활동 로그를 페이지네이션하여 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiData<PageResponse<ActivitySessionResponse>> getUserSessions(
            @PathVariable Long userId,
            @RequestBody @Valid GarminActivityPageRequest req
    ) {
        PageResponse<ActivitySessionResponse> res = activityQueryService.searchMySessions(userId, req);
        return ApiData.ok(res);
    }




}

