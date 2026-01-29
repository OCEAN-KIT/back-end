package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.SubstrateTargetType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity_substrate_improvement")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ActivitySubstrateImprovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private SubstrateTargetType targetType;  // 작업 대상

    @Column(name = "work_scope", nullable = false, length = 200)
    private String workScope;  // 작업 범위

    @Column(name = "substrate_state", nullable = false, columnDefinition = "TEXT")
    private String substrateState;  // 작업 후 기질 상태

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
