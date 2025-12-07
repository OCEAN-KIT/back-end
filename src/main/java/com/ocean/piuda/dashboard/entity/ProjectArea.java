package com.ocean.piuda.dashboard.entity;

import com.ocean.piuda.dashboard.enums.HabitatType;
import com.ocean.piuda.dashboard.enums.ProjectStatus;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_areas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class ProjectArea extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long id;

    @Column(nullable = false)
    private String name; // 예: "작업 영역 1"

    private String description; // 상세 설명

    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HabitatType habitat; // String -> HabitatType

    private Double depth; // 수심 (m)

    private Double areaSize; // 면적 (m2) - 숫자형으로 관리 권장

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectStatus status;

    // 좌표 (GeoSpatial 타입 도입 전에는 Double 사용)
    private Double lat;
    private Double lon;

    // --- 하위 연관 관계 (Cascade 설정) ---

    @OneToMany(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransplantLog> transplants = new ArrayList<>();

    @OneToMany(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GrowthLog> growthLogs = new ArrayList<>();

    @OneToMany(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WaterLog> waterLogs = new ArrayList<>();

    @OneToOne(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    private BiodiversitySummary biodiversity;

    @OneToMany(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MediaLog> mediaLogs = new ArrayList<>();

    // --- 연관관계 편의 메서드 ---
    public void addTransplant(TransplantLog log) { this.transplants.add(log); log.setProjectArea(this); }
    public void addGrowth(GrowthLog log) { this.growthLogs.add(log); log.setProjectArea(this); }
    public void addWater(WaterLog log) { this.waterLogs.add(log); log.setProjectArea(this); }
    public void setBiodiversity(BiodiversitySummary bio) { this.biodiversity = bio; bio.setProjectArea(this); }
    public void addMedia(MediaLog log) { this.mediaLogs.add(log); log.setProjectArea(this); }
}