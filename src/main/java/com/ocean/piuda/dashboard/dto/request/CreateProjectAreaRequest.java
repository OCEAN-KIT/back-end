package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.AreaAttachmentStatus;
import com.ocean.piuda.dashboard.enums.HabitatType;
import com.ocean.piuda.dashboard.enums.ProjectLevel;
import com.ocean.piuda.dashboard.enums.RestorationRegion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateProjectAreaRequest(
        @NotBlank String name,
        @NotNull RestorationRegion restorationRegion,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull HabitatType habitat,
        @NotNull Double depth,
        @NotNull Double areaSize,
        @NotNull ProjectLevel level,
        @NotNull AreaAttachmentStatus attachmentStatus,
        @NotNull Double lat,
        @NotNull Double lon
) {}
