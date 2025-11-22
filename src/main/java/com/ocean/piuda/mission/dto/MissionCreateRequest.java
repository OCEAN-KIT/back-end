package com.ocean.piuda.mission.dto;

import com.ocean.piuda.mission.domain.BioGroup;
import com.ocean.piuda.mission.domain.Mission;
import com.ocean.piuda.mission.domain.MissionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MissionCreateRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @NotNull(message = "대상 생물 분류는 필수입니다")
        BioGroup targetBioGroup,

        @NotNull(message = "포인트 ID는 필수입니다")
        Long pointId,

        String description,

        String regionName,

        @NotNull(message = "시작일은 필수입니다")
        LocalDate startDate,

        LocalDate endDate,

        MissionStatus status,

        String coverMediaUrl
) {
    public Mission toEntity(Long ownerId) {
        return Mission.builder()
                .title(title)
                .targetBioGroup(targetBioGroup)
                .ownerId(ownerId)
                .pointId(pointId)
                .description(description)
                .regionName(regionName)
                .startDate(startDate)
                .endDate(endDate)
                .status(status != null ? status : MissionStatus.PLANNED)
                .coverMediaUrl(coverMediaUrl)
                .build();
    }
}

