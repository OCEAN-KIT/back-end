package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.MarineCondition;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "basic_env")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BasicEnv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "env_id")
    private Long envId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "avg_depth_m", nullable = false)
    private Double avgDepthM;  // 평균 수심

    @Column(name = "max_depth_m", nullable = false)
    private Double maxDepthM;  // 최대 수심

    @Column(name = "water_temp_c", nullable = false)
    private Double waterTempC;  // 수온

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_status", nullable = false)
    private MarineCondition visibilityStatus;  // 시야 (BAD/NORMAL/GOOD)

    @Enumerated(EnumType.STRING)
    @Column(name = "wave_status", nullable = false)
    private MarineCondition waveStatus;  // 파도

    @Enumerated(EnumType.STRING)
    @Column(name = "surge_status", nullable = false)
    private MarineCondition surgeStatus;  // 서지

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private MarineCondition currentStatus;  // 조류


    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
