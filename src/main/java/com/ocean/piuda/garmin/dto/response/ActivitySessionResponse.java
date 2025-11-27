package com.ocean.piuda.garmin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ocean.piuda.garmin.entity.GarminActivityLog;
import com.ocean.piuda.garmin.enums.GarminActivityType;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public record ActivitySessionResponse(
        Summary summary,
        List<ActivityItem> activities
) {
    public static ActivitySessionResponse fromEntities(List<GarminActivityLog> logs) {

        if (logs == null || logs.isEmpty()) {
            return new ActivitySessionResponse(null, List.of());
        }

        GarminActivityLog first = logs.get(0);

        Summary summary = Summary.from(first);
        List<ActivityItem> items = logs.stream()
                .map(ActivityItem::fromEntity)
                .collect(Collectors.toList());

        return new ActivitySessionResponse(summary, items);
    }

    public record Summary(
            String sessionId,    // 세션 ID
            String userId,       // deviceId (워치 암호화 ID)
            Long startTime,      // epoch seconds
            Long endTime,        // epoch seconds
            Double startLat,
            Double startLon,
            Double endLat,
            Double endLon
    ) {
        public static Summary from(GarminActivityLog log) {


            return new Summary(
                    log.getSessionId(),
                    log.getDeviceId(),
                    toEpoch(log.getStartTime()),
                    toEpoch(log.getEndTime()),
                    log.getStartLat(),
                    log.getStartLon(),
                    log.getEndLat(),
                    log.getEndLon()
            );
        }

        private static Long toEpoch(java.time.LocalDateTime dt) {
            if (dt == null) return null;
            return dt.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
        }
    }

    public record ActivityItem(
            Long id,
            GarminActivityType activityType,
            String gridId,
            Integer totalCount,

            @JsonProperty("work_logs")
            List<List<Object>> workLogs
    ) {
        public static ActivityItem fromEntity(GarminActivityLog log) {
            return new ActivityItem(
                    log.getId(),
                    log.getGarminActivityType(),
                    log.getGridId(),
                    log.getTotalCount(),
                    log.getWorkLogs()
            );
        }
    }
}
