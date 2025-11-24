package com.ocean.piuda.mission.dto;

import com.ocean.piuda.mission.domain.BioGroup;
import com.ocean.piuda.mission.domain.MissionStatus;

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

