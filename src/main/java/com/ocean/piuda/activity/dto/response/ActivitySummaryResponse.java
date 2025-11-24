package com.ocean.piuda.activity.dto.response;


import com.ocean.piuda.activity.enums.GarminActivityType;

public record ActivitySummaryResponse(
        Long id,
        Long userId,
        GarminActivityType activityType,
        String gridId,
        Integer totalCount,
        Long startTime, // epoch seconds
        Long endTime,   // epoch seconds
        Double startLat,
        Double startLon,
        Double endLat,
        Double endLon
) {}
