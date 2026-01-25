package com.ocean.piuda.dashboard.dto.response;

import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.dashboard.entity.TransplantLog;
import com.ocean.piuda.dashboard.enums.SpeciesAttachmentStatus;
import com.ocean.piuda.dashboard.enums.TransplantMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TransplantLogResponse {
    private Long id;
    private LocalDate recordDate;

    private TransplantMethod method;
    private String methodName;
    private String methodDesc;
    private String unit;

    private Long speciesId;
    private String speciesName;

    private Integer count;
    private Double areaSize;

    private SpeciesAttachmentStatus attachmentStatus;
    private String attachmentStatusName;

    public static TransplantLogResponse from(TransplantLog t) {
        Species s = t.getSpecies();
        TransplantMethod m = t.getMethod();

        return TransplantLogResponse.builder()
                .id(t.getId())
                .recordDate(t.getRecordDate())
                .method(m)
                .methodName(m != null ? m.getName() : null)
                .methodDesc(m != null ? m.getDescription() : null)
                .unit(m != null ? m.getUnit() : null)
                .speciesId(s != null ? s.getId() : null)
                .speciesName(s != null ? s.getName() : null)
                .count(t.getCount())
                .areaSize(t.getAreaSize())
                .attachmentStatus(t.getAttachmentStatus())
                .attachmentStatusName(t.getAttachmentStatus() != null ? t.getAttachmentStatus().getName() : null)
                .build();
    }
}
