package com.ocean.piuda.notification.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SendResultResponse(
        int successCount,
        int failureCount,
        List<String> messageIds
) {}
