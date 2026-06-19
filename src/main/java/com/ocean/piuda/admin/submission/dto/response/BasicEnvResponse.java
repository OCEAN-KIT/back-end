package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.MarineCondition;
import com.ocean.piuda.admin.submission.entity.BasicEnv;

import java.time.LocalDate;

public record BasicEnvResponse(
        LocalDate recordDate,
        // 새로운 필드 (우선 사용)
        Double avgDepthM,  // 평균 수심
        Double maxDepthM,  // 최대 수심
        Double waterTempC,  // 수온
        MarineCondition visibilityStatus,  // 시야 상태
        MarineCondition waveStatus,  // 파도 상태
        MarineCondition surgeStatus,  // 서지 상태
        MarineCondition currentStatus  // 조류 상태
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
                basicEnv.getCurrentStatus());
    }
}
