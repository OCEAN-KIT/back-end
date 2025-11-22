package com.ocean.piuda.mission.domain;

import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "missions")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Mission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BioGroup targetBioGroup;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private Long pointId;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String regionName;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MissionStatus status = MissionStatus.PLANNED;

    private String coverMediaUrl;

    public void update(MissionUpdater updater) {
        if (updater.getTitle() != null) {
            this.title = updater.getTitle();
        }
        if (updater.getTargetBioGroup() != null) {
            this.targetBioGroup = updater.getTargetBioGroup();
        }
        if (updater.getPointId() != null) {
            this.pointId = updater.getPointId();
        }
        if (updater.getDescription() != null) {
            this.description = updater.getDescription();
        }
        if (updater.getRegionName() != null) {
            this.regionName = updater.getRegionName();
        }
        if (updater.getStartDate() != null) {
            this.startDate = updater.getStartDate();
        }
        if (updater.getEndDate() != null) {
            this.endDate = updater.getEndDate();
        }
        if (updater.getStatus() != null) {
            this.status = updater.getStatus();
        }
        if (updater.getCoverMediaUrl() != null) {
            this.coverMediaUrl = updater.getCoverMediaUrl();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MissionUpdater {
        private final String title;
        private final BioGroup targetBioGroup;
        private final Long pointId;
        private final String description;
        private final String regionName;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final MissionStatus status;
        private final String coverMediaUrl;
    }
}

