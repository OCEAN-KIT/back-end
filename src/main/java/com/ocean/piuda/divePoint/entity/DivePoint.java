package com.ocean.piuda.divePoint.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 오션캠퍼스 지정 다이빙 포인트로,
 * 미션과 게시물 에서 참조한다
 */
@Entity
@Table(name = "dive_points", indexes = {
    @Index(name = "idx_dive_point_lat_lon", columnList = "lat, lon")
})
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DivePoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 포인트 이름
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
     * 지역명 (예: "포항 월포")
     */
    @Column(length = 200)
    private String regionName;

    /**
     * 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;
}

