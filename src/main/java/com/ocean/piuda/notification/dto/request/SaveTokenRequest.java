package com.ocean.piuda.notification.dto.request;


import com.ocean.piuda.notification.enums.Platform;

public record SaveTokenRequest(
        String token,
        String deviceId,
        Platform platform
) {}
