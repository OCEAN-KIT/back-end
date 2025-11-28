package com.ocean.piuda.environment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Builder;

/**
 * 환경 요약 조회 요청 DTO
 */
@Builder
public record EnvironmentSummaryRequest(
        /**
         * 위도 (-90 ~ 90)
         */
        @DecimalMin(value = "-90.0", message = "위도는 -90 ~ 90 사이여야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 -90 ~ 90 사이여야 합니다")
        Double lat,

        /**
         * 경도 (-180 ~ 180)
         */
        @DecimalMin(value = "-180.0", message = "경도는 -180 ~ 180 사이여야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 -180 ~ 180 사이여야 합니다")
        Double lon,

        /**
         * 다이빙 포인트 ID (pointId가 있으면 lat/lon 무시)
         */
        Long pointId
) {
    /**
     * 좌표 기반 요청인지 확인
     */
    public boolean isCoordinateBased() {
        return pointId == null && lat != null && lon != null;
    }

    /**
     * 포인트 ID 기반 요청인지 확인
     */
    public boolean isPointIdBased() {
        return pointId != null;
    }
}

