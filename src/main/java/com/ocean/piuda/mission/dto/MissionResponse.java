package com.ocean.piuda.mission.dto;

import com.ocean.piuda.bio.enums.BioGroup;
import com.ocean.piuda.mission.domain.Mission;
import com.ocean.piuda.mission.enums.MissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MissionResponse(
        Long id,
        String title,
        BioGroup targetBioGroup,
        Long ownerId,
        Long pointId,
        String description,
        String regionName,
        LocalDate startDate,
        LocalDate endDate,
        MissionStatus status,
        String coverMediaUrl,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static MissionResponse from(Mission mission) {
        return new MissionResponse(
                mission.getId(),
                mission.getTitle(),
                mission.getTargetBioGroup(),
                mission.getOwnerId(),
                mission.getPointId(),
                mission.getDescription(),
                mission.getRegionName(),
                mission.getStartDate(),
                mission.getEndDate(),
                mission.getStatus(),
                mission.getCoverMediaUrl(),
                mission.getCreatedAt(),
                mission.getModifiedAt()
        );
    }
}

