package com.ocean.piuda.dashboard.dto;


import lombok.Builder;

import java.time.LocalDate;
import java.util.List;


@Builder
public record TimeSeriesChartDto(
        List<LocalDate> labels,   // x축 (예: ["2025-01", "2025-02"...])
        List<Double> values,    // y축 수치
        String unit,            // 단위
        String targetSpecies,   // 대상 종
        Long targetSpeciesId,     // 대상 종의 ID
        String period           // 기간
) { }