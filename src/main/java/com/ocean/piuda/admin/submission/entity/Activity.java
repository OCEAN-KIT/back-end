package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.HealthGrade;
import com.ocean.piuda.admin.submission.entity.embeded.NaturalReproduction;
import com.ocean.piuda.admin.submission.entity.embeded.Survival;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "collection_amount")
    @Builder.Default
    private Float collectionAmount = 0f;

    @Column(name = "duration_hours")
    @Builder.Default
    private Float durationHours = 0f;


    @Enumerated(EnumType.STRING)
    @Column(name = "health_grade")
    private HealthGrade healthGrade;

    @Column(name = "growth_cm")
    @Builder.Default
    private Float growthCm = 0f;

    @Embedded
    private NaturalReproduction naturalReproduction;

    @Embedded
    private Survival survival;


    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}