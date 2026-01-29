package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.CurrentState;
import com.ocean.piuda.admin.common.enums.MarineCondition;
import com.ocean.piuda.admin.common.enums.Weather;
import com.ocean.piuda.admin.submission.entity.BasicEnv;

import java.time.LocalDate;
import java.time.LocalTime;

public record BasicEnvResponse(
        LocalDate recordDate,
        // 새로운 필드 (우선 사용)
        Double avgDepthM,  // 평균 수심
        Double maxDepthM,  // 최대 수심
        Double waterTempC,  // 수온
        MarineCondition visibilityStatus,  // 시야 상태
        MarineCondition waveStatus,  // 파도 상태
        MarineCondition surgeStatus,  // 서지 상태
        MarineCondition currentStatus,  // 조류 상태
        // 하위 호환성을 위한 기존 필드 (deprecated)
        @Deprecated LocalTime startTime,
        @Deprecated LocalTime endTime,
        @Deprecated Float visibilityM,
        @Deprecated Float depthM,
        @Deprecated CurrentState currentState,
        @Deprecated Weather weather
) {
    public static BasicEnvResponse from(BasicEnv basicEnv) {
        if (basicEnv == null) return null;
        return new BasicEnvResponse(
                basicEnv.getRecordDate(),
                // 새로운 필드
                basicEnv.getAvgDepthM(),
                basicEnv.getMaxDepthM(),
                basicEnv.getWaterTempC(),
                basicEnv.getVisibilityStatus(),
                basicEnv.getWaveStatus(),
                basicEnv.getSurgeStatus(),
                basicEnv.getCurrentStatus(),
                // 하위 호환성 필드
                basicEnv.getStartTime(),
                basicEnv.getEndTime(),
                basicEnv.getVisibilityM(),
                basicEnv.getDepthM(),
                basicEnv.getCurrentState(),
                basicEnv.getWeather()
        );
    }
}
