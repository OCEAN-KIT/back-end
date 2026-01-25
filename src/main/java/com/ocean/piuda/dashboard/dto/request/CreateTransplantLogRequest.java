package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.SpeciesAttachmentStatus;
import com.ocean.piuda.dashboard.enums.TransplantMethod;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateTransplantLogRequest(
        @NotNull LocalDate recordDate,
        @NotNull TransplantMethod method,
        @NotNull Long speciesId,
        @NotNull Integer count,
        @NotNull Double areaSize,
        @NotNull SpeciesAttachmentStatus attachmentStatus
) {}
