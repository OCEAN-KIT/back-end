package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.MarineStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateWaterLogRequest(
        @NotNull LocalDate recordDate,
        @NotNull Double temperature,
        @NotNull Double dissolvedOxygen,
        @NotNull Double nutrient,
        @NotNull MarineStatus visibility,
        @NotNull MarineStatus current,
        @NotNull MarineStatus surge,
        @NotNull MarineStatus wave
) {}
