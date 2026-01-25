package com.ocean.piuda.dashboard.entity;

import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.dashboard.enums.SpeciesAttachmentStatus;
import com.ocean.piuda.dashboard.enums.TransplantMethod;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "transplant_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TransplantLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    @Setter
    private ProjectArea projectArea;

    @Column(nullable = false)
    private LocalDate recordDate;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransplantMethod method;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @Column(nullable = false)
    private Integer count;      // 개체

    @Column(nullable = false)
    private Double areaSize;    // 이식 면적 (m2)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpeciesAttachmentStatus attachmentStatus; // 전문가 입력: GOOD/NORMAL/POOR

    public void update(
            LocalDate recordDate,
            TransplantMethod method,
            Species species,
            Integer count,
            Double areaSize,
            SpeciesAttachmentStatus attachmentStatus
    ) {
        if (recordDate != null) this.recordDate = recordDate;
        if (method != null) this.method = method;
        if (species != null) this.species = species;
        if (count != null) this.count = count;
        if (areaSize != null) this.areaSize = areaSize;
        if (attachmentStatus != null) this.attachmentStatus = attachmentStatus;
    }

}