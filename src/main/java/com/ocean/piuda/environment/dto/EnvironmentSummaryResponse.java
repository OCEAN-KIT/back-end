package com.ocean.piuda.environment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 환경 요약 응답 DTO
 */
@Builder
public record EnvironmentSummaryResponse(
        Location location,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        ZonedDateTime timestamp,
        Water water,
        Wave wave,
        Tide tide,
        Meta meta
) {
    @Builder
    public record Location(
            Double lat,
            Double lon,
            NearestStations nearestStations
    ) {}

    @Builder
    public record NearestStations(
            StationInfo nifs,
            StationInfo kma,
            StationInfo khoa
    ) {}

    @Builder
    public record StationInfo(
            String id,
            String name,
            Double distanceKm
    ) {}

    @Builder
    public record Water(
            Double midLayerTemp,
            Double surfaceTemp,
            Double salinity,
            Double dissolvedOxygen
    ) {}

    @Builder
    public record Wave(
            Double significantWaveHeight,
            Double windDirectionDeg,
            Double windSpeedMs
    ) {}

    @Builder
    public record Tide(
            Double tideLevelCm,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
            ZonedDateTime tideObservedAt
    ) {}

    @Builder
    public record Meta(
            List<String> rawSources,
            String note
    ) {}
}

