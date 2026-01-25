package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.SpeciesAttachmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateGrowthLogRequest(
        @NotNull Long speciesId,
        @NotNull Boolean isRepresentative,
        @NotNull LocalDate recordDate,
        @NotNull Double attachmentRate,
        @NotNull Double survivalRate,
        @NotNull Double growthLength,
        @NotNull SpeciesAttachmentStatus status
) {}
