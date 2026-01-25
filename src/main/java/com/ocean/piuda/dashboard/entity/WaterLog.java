package com.ocean.piuda.dashboard.entity;

import com.ocean.piuda.dashboard.enums.MarineStatus;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "water_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WaterLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    @Setter
    private ProjectArea projectArea;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    private Double temperature;     // 수온

    @Column(nullable = false)
    private Double dissolvedOxygen; // DO

    @Column(nullable = false)
    private Double nutrient;        // 영양염류


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarineStatus visibility; // 시야

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarineStatus current;    // 조류

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarineStatus surge;     // 서지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarineStatus wave;      // 파도

    public void update(
            LocalDate recordDate,
            Double temperature,
            Double dissolvedOxygen,
            Double nutrient,
            MarineStatus visibility,
            MarineStatus current,
            MarineStatus surge,
            MarineStatus wave
    ) {
        if (recordDate != null) this.recordDate = recordDate;
        if (temperature != null) this.temperature = temperature;
        if (dissolvedOxygen != null) this.dissolvedOxygen = dissolvedOxygen;
        if (nutrient != null) this.nutrient = nutrient;
        if (visibility != null) this.visibility = visibility;
        if (current != null) this.current = current;
        if (surge != null) this.surge = surge;
        if (wave != null) this.wave = wave;
    }

}
