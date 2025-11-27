package com.ocean.piuda.environment.domain;

import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

/**
 * 해양 관측값 엔티티
 * 모든 외부 API의 관측값을 통합하는 정규화된 엔티티
 */
@Entity
@Table(name = "marine_observations", indexes = {
    @Index(name = "idx_station_observed", columnList = "station_id, observedAt")
})
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MarineObservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 관측소
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private MarineStation station;

    /**
     * 관측 시각
     */
    @Column(nullable = false)
    private ZonedDateTime observedAt;

    /**
     * 수층 정보 (예: "surface", "mid", "bottom", "2" 등)
     */
    @Column(length = 50)
    private String layer;

    // 수온/염분/용존산소
    /**
     * 수온 (℃)
     */
    @Column(columnDefinition = "DOUBLE")
    private Double waterTemp;

    /**
     * 염분 (psu)
     */
    @Column(columnDefinition = "DOUBLE")
    private Double salinity;

    /**
     * 용존산소 (mg/L)
     */
    @Column(columnDefinition = "DOUBLE")
    private Double dissolvedOxygen;

    // 파고/풍향/풍속
    /**
     * 파고 (m)
     */
    @Column(columnDefinition = "DOUBLE")
    private Double waveHeight;

    /**
     * 풍향 (도)
     */
    @Column(columnDefinition = "DOUBLE")
    private Double windDirection;

    /**
     * 풍속 (m/s)
     */
    @Column(columnDefinition = "DOUBLE")
    private Double windSpeed;

    // 조위
    /**
     * 조위 (cm)
     */
    @Column(columnDefinition = "DOUBLE")
    private Double tideLevel;

    /**
     * 원본 API 응답 JSON (디버깅/감사용)
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawPayload;
}

