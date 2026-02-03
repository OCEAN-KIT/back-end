package com.ocean.piuda.dashboard.entity;

import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.dashboard.enums.SpeciesAttachmentStatus;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "growth_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GrowthLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    @Setter
    private ProjectArea projectArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;


    @Column(nullable = false)
    private LocalDate recordDate; // 기록 날짜 (YYYY-MM-DD)

    @Column(nullable = false)
    private Double attachmentRate; // 착생률 (%)
    @Column(nullable = false)
    private Double survivalRate;   // 생존률 (%)
    @Column(nullable = false)
    private Double growthLength;   // 성장 길이 (mm)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpeciesAttachmentStatus status;

    public void update(
            Species species,
            LocalDate recordDate,
            Double attachmentRate,
            Double survivalRate,
            Double growthLength,
            SpeciesAttachmentStatus status
    ) {
        if (species != null) this.species = species;
        if (recordDate != null) this.recordDate = recordDate;
        if (attachmentRate != null) this.attachmentRate = attachmentRate;
        if (survivalRate != null) this.survivalRate = survivalRate;
        if (growthLength != null) this.growthLength = growthLength;
        if (status != null) this.status = status;
    }

}