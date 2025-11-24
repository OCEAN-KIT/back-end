package com.ocean.piuda.activity.dto.request;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityIngestRequest(
        Summary summary,
        // JSON의 "work_logs"를 그대로 받음
        @JsonProperty("work_logs") List<List<Object>> workLogs
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(
            String activityType,  // "URCHIN_REMOVAL" | "SEAWEED_TRANSPLANT"
            String gridId,
            Long teamId,
            Long startTime, // epoch seconds
            Long endTime,   // epoch seconds
            Integer totalCount,
            Double startLat,
            Double startLon,
            Double endLat,
            Double endLon
    ) {}
}
