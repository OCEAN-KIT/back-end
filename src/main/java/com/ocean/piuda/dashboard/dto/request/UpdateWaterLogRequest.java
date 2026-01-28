package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.MarineStatus;

import java.time.LocalDate;

public record UpdateWaterLogRequest(
        LocalDate recordDate,
        Double temperature,
        Double dissolvedOxygen,
        Double nutrient,
        MarineStatus visibility,
        MarineStatus current,
        MarineStatus surge,
        MarineStatus wave
) {}

