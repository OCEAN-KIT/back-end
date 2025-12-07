package com.ocean.piuda.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "growth_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GrowthLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    @Setter
    private ProjectArea projectArea;

    private LocalDate recordDate; // 기록 날짜 (YYYY-MM-DD)

    private Double attachmentRate; // 착생률 (%)
    private Double survivalRate;   // 생존률 (%)
    private Double growthLength;   // 성장 길이 (mm)
}