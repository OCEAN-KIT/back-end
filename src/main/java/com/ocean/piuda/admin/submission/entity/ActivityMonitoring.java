package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_monitoring")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ActivityMonitoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    // 적지조사
    @Column(name = "entry_coordinate", length = 200)
    private String entryCoordinate;  // 입수좌표 (lat/lon 또는 text)

    @Column(name = "exit_coordinate", length = 200)
    private String exitCoordinate;  // 출수좌표

    @Column(length = 100)
    private String direction;  // 진행방위

    @Enumerated(EnumType.STRING)
    @Column(name = "terrain")
    private TerrainType terrain;  // 지형 구성

    @Enumerated(EnumType.STRING)
    @Column(name = "barren_extent")
    private BarrenExtent barrenExtent;  // 갯녹음 정도

    @Enumerated(EnumType.STRING)
    @Column(name = "grazer_distribution")
    private DensityLevel grazerDistribution;  // 조식동물 분포

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "monitoring_rock_features",
        joinColumns = @JoinColumn(name = "activity_id")
    )
    @Column(name = "feature")
    @Builder.Default
    private List<RockFeature> rockFeatures = new ArrayList<>();  // 암반 특성 (복수)

    @Enumerated(EnumType.STRING)
    @Column(name = "suitability")
    private Suitability suitability;  // 해조 이식 적합성

    // 해조류 상태
    @Column(name = "seaweed_id_number", length = 100)
    private String seaweedIdNumber;  // 식별번호

    @Enumerated(EnumType.STRING)
    @Column(name = "seaweed_health_status")
    private com.ocean.piuda.admin.common.enums.SeaweedHealthStatus seaweedHealthStatus;  // 생육상태 (양호/쇠약/탈락)

    @Column(name = "precision_measurement")
    private Boolean precisionMeasurement;  // 정밀측정 여부

    @Column(name = "leaf_length", length = 100)
    private String leafLength;  // 엽장

    @Column(name = "max_leaf_width", length = 100)
    private String maxLeafWidth;  // 최대엽폭

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }

}
