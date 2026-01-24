/**
 * 개편된 5개의 탭 구조에 맞춰 불필요한 "생물 다양성 요약 엔티티"는 사용하지 않습니다.
 */
/*
package com.ocean.piuda.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "biodiversity_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BiodiversitySummary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    @Setter
    private ProjectArea projectArea;

    // 복원 전
    private Integer fishCountBefore;
    private Integer invertCountBefore;
    private Double shannonIndexBefore;

    // 복원 후
    private Integer fishCountAfter;
    private Integer invertCountAfter;
    private Double shannonIndexAfter;
}

 */