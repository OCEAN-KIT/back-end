package com.ocean.piuda.activity.util;


import com.ocean.piuda.activity.dto.request.ActivityIngestRequest;
import com.ocean.piuda.activity.entity.GarminActivityLog;
import com.ocean.piuda.activity.enums.GarminActivityType;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ActivityMapper {

    public GarminActivityLog toEntity(ActivityIngestRequest request, Long userId) {
        if (request == null || request.summary() == null) {
            throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
        }
        var s = request.summary();

        GarminActivityType type;
        try {
            type = GarminActivityType.valueOf(s.activityType());
        } catch (Exception e) {
            throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
        }

        return GarminActivityLog.builder()
                .userId(userId)
                .garminActivityType(type)
                .gridId(s.gridId())
                .teamId(s.teamId())
                .startTime(toLocalDateTime(s.startTime()))
                .endTime(toLocalDateTime(s.endTime()))
                .totalCount(s.totalCount())
                .startLat(s.startLat())
                .startLon(s.startLon())
                .endLat(s.endLat())
                .endLon(s.endLon())
                .workLogs(request.workLogs())   // ← 여기!
                .build();
    }

    private LocalDateTime toLocalDateTime(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.of("Asia/Seoul"));
    }
}
