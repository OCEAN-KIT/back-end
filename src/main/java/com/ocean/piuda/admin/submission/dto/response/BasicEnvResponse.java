package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.CurrentState;
import com.ocean.piuda.admin.common.enums.Weather;
import com.ocean.piuda.admin.submission.entity.BasicEnv;

import java.time.LocalDate;
import java.time.LocalTime;

public record BasicEnvResponse(
        LocalDate recordDate,
        LocalTime startTime,
        LocalTime endTime,
        Float waterTempC,
        Float visibilityM,
        Float depthM,
        CurrentState currentState,
        Weather weather
) {
    public static BasicEnvResponse from(BasicEnv basicEnv) {
        if (basicEnv == null) return null;
        return new BasicEnvResponse(
                basicEnv.getRecordDate(),
                basicEnv.getStartTime(),
                basicEnv.getEndTime(),
                basicEnv.getWaterTempC(),
                basicEnv.getVisibilityM(),
                basicEnv.getDepthM(),
                basicEnv.getCurrentState(),
                basicEnv.getWeather()
        );
    }
}
