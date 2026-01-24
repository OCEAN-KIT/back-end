package com.ocean.piuda.dashboard.entity;

import com.ocean.piuda.dashboard.enums.HabitatType;
import com.ocean.piuda.dashboard.enums.ProjectStatus;
import com.ocean.piuda.dashboard.enums.RestorationRegion;
import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.global.util.GeometryUtils;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.locationtech.jts.geom.Point;

import java.awt.*;
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

    // 작업 영역 이름
    @Column(nullable = false)
    private String name; // 예: "작업 영역 1"

    @Enumerated(EnumType.STRING)
    private RestorationRegion restorationRegion;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private HabitatType habitat;

    private Double depth; // 수심 (m)
    private Double areaSize; // 면적 (m^2)

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectStatus status;


    // --- 하위 연관 관계 ---

    @OneToMany(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransplantLog> transplants = new ArrayList<>();

    @OneToMany(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GrowthLog> growthLogs = new ArrayList<>();

    @OneToMany(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WaterLog> waterLogs = new ArrayList<>();

//    @OneToOne(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
//    private BiodiversitySummary biodiversity;

    @OneToMany(mappedBy = "projectArea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MediaLog> mediaLogs = new ArrayList<>();


    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;


    /**
     * lat, lon -> GIS location set 편의 메서드
     */
    public void setLocation(Double lat, Double lon) {
        if (lat != null && lon != null) this.location = GeometryUtils.createPoint(lon, lat);
        else this.location = null;
    }


    /**
     * GIS locatoin -> lat , lon get 편의 메서드
     */
    public Double getLat() {
        return this.location != null ? this.location.getY() : null;
    }

    public Double getLon() {
        return this.location != null ? this.location.getX() : null;
    }

    // --- 연관관계 편의 메서드 ---
    public void addTransplant(TransplantLog log) { this.transplants.add(log); log.setProjectArea(this); }
    public void addGrowth(GrowthLog log) { this.growthLogs.add(log); log.setProjectArea(this); }
    public void addWater(WaterLog log) { this.waterLogs.add(log); log.setProjectArea(this); }
//    public void setBiodiversity(BiodiversitySummary bio) { this.biodiversity = bio; bio.setProjectArea(this); }
    public void addMedia(MediaLog log) { this.mediaLogs.add(log); log.setProjectArea(this); }
}