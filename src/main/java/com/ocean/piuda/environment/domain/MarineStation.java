package com.ocean.piuda.environment.domain;

import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 해양 관측소 엔티티
 * 외부 API에서 제공하는 관측소 정보를 저장
 */
@Entity
@Table(name = "marine_stations", indexes = {
    @Index(name = "idx_source_ext", columnList = "externalSource, externalStationId"),
    @Index(name = "idx_lat_lon", columnList = "lat, lon")
})
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MarineStation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 외부 API 소스 (NIFS_RISA, KMA_SEA_OBS, KHOA_SURVEY_TIDE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StationSource externalSource;

    /**
     * 외부 API에서 사용하는 관측소 ID
     */
    @Column(nullable = false, length = 100)
    private String externalStationId;

    /**
     * 관측소 이름
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 위도
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(9,6)")
    private Double lat;

    /**
     * 경도
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(9,6)")
    private Double lon;

    /**
     * 관측소 활성화 여부
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
}

