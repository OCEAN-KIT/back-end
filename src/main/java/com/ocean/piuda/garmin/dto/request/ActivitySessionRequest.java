package com.ocean.piuda.garmin.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ocean.piuda.garmin.entity.GarminActivityLog;
import com.ocean.piuda.garmin.enums.GarminActivityType;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivitySessionRequest(
        Summary summary,
        List<ActivityItem> activities
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(
            @JsonProperty("userId")
            String deviceId,   // 워치에서 온 암호화된 deviceId
            Long startTime,
            Long endTime,
            Double startLat,
            Double startLon,
            Double endLat,
            Double endLon
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ActivityItem(
            String activityType, // "URCHIN_REMOVAL" | "SEAWEED_TRANSPLANT" | "OTHER_ACTIVITY"
            String gridId,
            Integer totalCount,
            @JsonProperty("work_logs")
            List<List<Object>> workLogs  // JSON의 "work_logs" [[time, depth, temp, ...], ...]
    ) {}

    /**
     * 서비스 레이어에서 생성된 sessionId, 최종 userId를 받아
     * 요청을 엔티티 리스트로 변환.
     */
    public List<GarminActivityLog> toEntities(Long userId, String sessionId) {
        // activities == null만 막고 있고, [](빈 리스트)는 허용한다.
        if (summary == null || activities == null) {
            throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
        }

        Summary s = summary;

        LocalDateTime startDt = toLocalDateTime(s.startTime());
        LocalDateTime endDt   = toLocalDateTime(s.endTime());
        String deviceId       = s.deviceId(); // 워치가 보낸 암호화된 deviceId

        List<GarminActivityLog> logs = new ArrayList<>();

        for (ActivityItem item : activities) {
            GarminActivityType type;
            try {
                type = GarminActivityType.valueOf(item.activityType());
            } catch (Exception e) {
                type = GarminActivityType.OTHER_ACTIVITY;
            }

            GarminActivityLog log = GarminActivityLog.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .deviceId(deviceId)
                    .garminActivityType(type)
                    .gridId(item.gridId())
                    .totalCount(item.totalCount())
                    .workLogs(item.workLogs())
                    .startTime(startDt)
                    .endTime(endDt)
                    .startLat(s.startLat())
                    .startLon(s.startLon())
                    .endLat(s.endLat())
                    .endLon(s.endLon())
                    .build();

            logs.add(log);
        }

        return logs;
    }

    private static LocalDateTime toLocalDateTime(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.of("Asia/Seoul"));
    }
}
