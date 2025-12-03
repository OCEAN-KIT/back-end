package com.ocean.piuda.garmin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ocean.piuda.garmin.entity.GarminActivityLog;
import com.ocean.piuda.garmin.enums.GarminActivityType;

import java.util.List;
import java.util.stream.Collectors;

import static com.ocean.piuda.garmin.util.GarminUtils.*;

public record ActivitySessionResponse(
        Summary summary,
        List<ActivityItem> activities
) {
    public static ActivitySessionResponse fromEntities(List<GarminActivityLog> logs) {

        if (logs == null || logs.isEmpty()) {
            return new ActivitySessionResponse(null, List.of());
        }

        GarminActivityLog first = logs.get(0);

        // 세션 전체 work_logs(flatten) 모으기
        List<List<Object>> allRows = logs.stream()
                .filter(log -> log.getWorkLogs() != null)
                .flatMap(log -> log.getWorkLogs().stream())
                .toList();

        // 평균 수심/수온 계산 (없으면 null)
        Double avgDepth = computeAverageDepth(allRows);
        Double avgTemperature = computeAverageTemperature(allRows);

        Summary summary = Summary.from(first, avgDepth, avgTemperature);
        List<ActivityItem> items = logs.stream()
                .map(ActivityItem::fromEntity)
                .collect(Collectors.toList());

        return new ActivitySessionResponse(summary, items);
    }

    public record Summary(
            String sessionId,    // 세션 ID
            String userId,       // deviceId (워치 암호화 ID)
            String startTime,    // ISO 문자열 (예: 2025-11-27T21:01:23)
            String endTime,      // ISO 문자열
            Double startLat,
            Double startLon,
            Double endLat,
            Double endLon,
            Double avgDepth,         // work_logs 기반 평균 수심
            Double avgTemperature    // work_logs 기반 평균 수온
    ) {
        public static Summary from(GarminActivityLog log,
                                   Double avgDepth,
                                   Double avgTemperature) {

            return new Summary(
                    log.getSessionId(),
                    log.getDeviceId(),
                    toIsoString(log.getStartTime()),
                    toIsoString(log.getEndTime()),
                    log.getStartLat(),
                    log.getStartLon(),
                    log.getEndLat(),
                    log.getEndLon(),
                    avgDepth,
                    avgTemperature
            );
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
                    mapWorkLogsEpochToIso(log.getWorkLogs())
            );
        }
    }

}
