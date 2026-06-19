package com.ocean.piuda.dashboard.dto.response;

import com.ocean.piuda.dashboard.entity.ProjectArea;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProjectAreaListItemResponse {

    private Long id;
    private String name;

    private String restorationRegion; // "포항"/"울진"
    private LocalDate startDate;
    private LocalDate endDate;

    private String habitat;           // "암반"/"혼합"/...
    private Double depth;
    private Double areaSize;

    private String level;             // "관측"/"정착"/...
    private String attachmentStatus;  // "안정"/"일부 감소"/...

    private Double lat;
    private Double lon;

    public static ProjectAreaListItemResponse from(ProjectArea a) {
        return ProjectAreaListItemResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .restorationRegion(a.getRestorationRegion() != null ? a.getRestorationRegion().getName() : null)
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .habitat(a.getHabitat() != null ? a.getHabitat().getName() : null)
                .depth(a.getDepth())
                .areaSize(a.getAreaSize())
                .level(a.getLevel() != null ? a.getLevel().getName() : null)
                .attachmentStatus(a.getAttachmentStatus() != null ? a.getAttachmentStatus().getName() : null)
                .lat(a.getLat())
                .lon(a.getLon())
                .build();
    }
}
