package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.DensityLevel;
import com.ocean.piuda.admin.common.enums.GrazerSpeciesType;
import com.ocean.piuda.admin.common.enums.WorkScope;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_grazer_removal")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ActivityGrazerRemoval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "grazer_removal_target_species",
        joinColumns = @JoinColumn(name = "activity_id")
    )
    @Column(name = "species")
    @Builder.Default
    private List<GrazerSpeciesType> targetSpecies = new ArrayList<>();  // 대상 생물 (복수)

    @Enumerated(EnumType.STRING)
    @Column(name = "density_before_work", nullable = false)
    private DensityLevel densityBeforeWork;  // 작업 전 밀도

    @Enumerated(EnumType.STRING)
    @Column(name = "work_scope", nullable = false)
    private WorkScope workScope;  // 작업 범위

    @Column(columnDefinition = "TEXT")
    private String note;  // 보충 설명

    @Column(name = "collection_amount", nullable = false, length = 200)
    private String collectionAmount;  // 수거량

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
