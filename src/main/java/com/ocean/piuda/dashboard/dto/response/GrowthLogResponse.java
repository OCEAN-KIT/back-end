package com.ocean.piuda.dashboard.dto.response;

import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.dashboard.entity.GrowthLog;
import com.ocean.piuda.dashboard.enums.SpeciesAttachmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GrowthLogResponse {
    private Long id;
    private LocalDate recordDate;

    private Long speciesId;
    private String speciesName;

    private Double growthLength;

    private SpeciesAttachmentStatus status;
    private String statusName;

    public static GrowthLogResponse from(GrowthLog g) {
        Species s = g.getSpecies();
        return GrowthLogResponse.builder()
                .id(g.getId())
                .recordDate(g.getRecordDate())
                .speciesId(s != null ? s.getId() : null)
                .speciesName(s != null ? s.getName() : null)
                .growthLength(g.getGrowthLength())
                .status(g.getStatus())
                .statusName(g.getStatus() != null ? g.getStatus().getName() : null)
                .build();
    }
}
