package com.ocean.piuda.garmin.dto.response;


public record WatchPairResponse(
        String deviceId,   // 워치에서 보여준 암호화된 deviceId (또는 short ID를 서버에서 풀어쓴 값)
        Long userId
) {}