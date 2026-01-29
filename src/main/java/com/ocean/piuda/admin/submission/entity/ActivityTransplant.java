package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.HealthStatus;
import com.ocean.piuda.admin.common.enums.TransplantLocationType;
import com.ocean.piuda.admin.common.enums.TransplantMethodType;
import com.ocean.piuda.admin.common.enums.TransplantSpeciesType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity_transplant")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ActivityTransplant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Enumerated(EnumType.STRING)
    @Column(name = "species_type", nullable = false)
    private TransplantSpeciesType speciesType;  // 대상 종류

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private TransplantLocationType locationType;  // 이식 장소

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false)
    private TransplantMethodType methodType;  // 이식 방식

    @Column(nullable = false, length = 200)
    private String scale;  // 이식 규모

    @Column(nullable = false, length = 1)
    private String zone;  // 구역 (A/B/C/D)

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false)
    private HealthStatus healthStatus;  // 건강상태

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
