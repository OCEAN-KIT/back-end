package com.ocean.piuda.notification.dto.request;

import java.util.List;

public record SendNotificationRequest(
        List<Long> userIds,
        List<String> tokens,
        String title,
        String body,
        String url
) { }

