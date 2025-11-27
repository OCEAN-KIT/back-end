package com.ocean.piuda.garmin.controller;

import com.ocean.piuda.garmin.dto.request.WatchPairRequest;
import com.ocean.piuda.garmin.dto.response.WatchPairResponse;
import com.ocean.piuda.garmin.service.WatchPairingCommandService;
import com.ocean.piuda.garmin.service.WatchPairingQueryService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/pair")
@RequiredArgsConstructor
@Tag(name = "Watch API", description = "시계 페어링/연결 해제")
public class WatchController {

    private final TokenUserService tokenUserService;
    private final WatchPairingCommandService watchPairingService;
    private final WatchPairingQueryService watchPairingQueryService;

    @PostMapping("/my/watch")
    @Operation(summary = "내 워치 등록(페어링)", description = "현재 로그인한 유저 계정에 워치를 연결합니다.")
    public ApiData<Boolean> pairMyWatch(@RequestBody @Valid WatchPairRequest req) {
        Long userId = tokenUserService.getCurrentUser().getId();
        watchPairingService.pair(userId, req.deviceId());
        return ApiData.ok(true);
    }

    @DeleteMapping("/my/watch")
    @Operation(summary = "내 워치 연결 해제", description = "현재 로그인한 유저 계정에서 워치를 해제합니다.")
    public ApiData<Boolean> unpairMyWatch() {
        Long userId = tokenUserService.getCurrentUser().getId();
        watchPairingService.unpair(userId);
        return ApiData.ok(true);
    }


    @GetMapping("/my/watch")
    @Operation(summary = "내 워치 ID 조회", description = "현재 로그인한 유저 계정에 연결된 워치 ID를 조회합니다.")
    public ApiData<WatchPairResponse> getMyWatch() {
        Long userId = tokenUserService.getCurrentUser().getId();
        WatchPairResponse res = watchPairingQueryService.getByUserId(userId);
        return ApiData.ok(res);
    }
}
