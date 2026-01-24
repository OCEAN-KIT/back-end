package com.ocean.piuda.dashboard.entity;

import com.ocean.piuda.dashboard.enums.ConditionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "water_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WaterLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    @Setter
    private ProjectArea projectArea;

    private LocalDate recordDate;

    private Double temperature;     // 수온
    private Double dissolvedOxygen; // DO
    private Double nutrient;        // 영양염류


    @Enumerated(EnumType.STRING)
    private ConditionStatus.Environment visibility; // 시야
    @Enumerated(EnumType.STRING)
    private ConditionStatus.Environment current;    // 조류
    @Enumerated(EnumType.STRING)
    private ConditionStatus.Environment surge;     // 서지
    @Enumerated(EnumType.STRING)
    private ConditionStatus.Environment wave;      // 파도
}