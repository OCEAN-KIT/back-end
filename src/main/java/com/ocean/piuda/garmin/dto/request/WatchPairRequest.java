package com.ocean.piuda.garmin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WatchPairRequest(
        @NotBlank
        String deviceId   // 워치에서 보여준 암호화된 deviceId (또는 short ID를 서버에서 풀어쓴 값)
) {}
