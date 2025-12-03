package com.ocean.piuda.mission.dto;

import com.ocean.piuda.bio.enums.BioGroup;
import com.ocean.piuda.mission.enums.MissionStatus;
import lombok.Builder;

@Builder
public record MissionSearchCondition(
        MissionStatus status,
        BioGroup targetBioGroup,
        String regionName
) {}

