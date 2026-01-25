package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.SpeciesAttachmentStatus;
import com.ocean.piuda.dashboard.enums.TransplantMethod;

import java.time.LocalDate;

public record UpdateTransplantLogRequest(
        LocalDate recordDate,
        TransplantMethod method,
        Long speciesId,
        Integer count,
        Double areaSize,
        SpeciesAttachmentStatus attachmentStatus
) {}
