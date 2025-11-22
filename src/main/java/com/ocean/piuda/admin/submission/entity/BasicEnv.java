package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.CurrentState;
import com.ocean.piuda.admin.common.enums.Weather;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

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

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "water_temp_c")
    private Float waterTempC;

    @Column(name = "visibility_m")
    private Float visibilityM;

    @Column(name = "depth_m")
    private Float depthM;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_state")
    private CurrentState currentState;

    @Enumerated(EnumType.STRING)
    private Weather weather;

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
