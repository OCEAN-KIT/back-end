package com.ocean.piuda.mission.dto;

import com.ocean.piuda.bio.enums.BioGroup;
import com.ocean.piuda.mission.enums.MissionStatus;

import java.time.LocalDate;

public record MissionUpdateRequest(
        String title,
        BioGroup targetBioGroup,
        Long pointId,
        String description,
        String regionName,
        LocalDate startDate,
        LocalDate endDate,
        MissionStatus status,
        String coverMediaUrl
) {}

