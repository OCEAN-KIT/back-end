package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.MediaCategory;

import java.time.LocalDate;

public record UpdateMediaLogRequest(
        LocalDate recordDate,
        String mediaUrl,
        String caption,
        MediaCategory category
) {}

