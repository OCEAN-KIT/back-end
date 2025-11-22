package com.ocean.piuda.mission.dto;

import com.ocean.piuda.mission.domain.BioGroup;
import com.ocean.piuda.mission.domain.MissionStatus;
import lombok.Builder;

@Builder
public record MissionSearchCondition(
        MissionStatus status,
        BioGroup targetBioGroup,
        String regionName
) {}

