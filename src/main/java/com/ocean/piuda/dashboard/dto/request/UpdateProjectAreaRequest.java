package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.AreaAttachmentStatus;
import com.ocean.piuda.dashboard.enums.HabitatType;
import com.ocean.piuda.dashboard.enums.ProjectLevel;
import com.ocean.piuda.dashboard.enums.RestorationRegion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateProjectAreaRequest(
        String name,
        RestorationRegion restorationRegion,
        LocalDate startDate,
        LocalDate endDate,
        HabitatType habitat,
        Double depth,
        Double areaSize,
        ProjectLevel level,
        AreaAttachmentStatus attachmentStatus,
        Double lat,
        Double lon
) {}
