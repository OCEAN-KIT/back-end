package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.SpeciesAttachmentStatus;

import java.time.LocalDate;

public record UpdateGrowthLogRequest(
        Long speciesId,
        Boolean isRepresentative,
        LocalDate recordDate,
        Double attachmentRate,
        Double survivalRate,
        Double growthLength,
        SpeciesAttachmentStatus status
) {}
