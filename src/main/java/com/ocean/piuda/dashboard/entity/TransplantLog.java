package com.ocean.piuda.dashboard.entity;

import com.ocean.piuda.dashboard.enums.TransplantMethod;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transplant_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TransplantLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    @Setter
    private ProjectArea projectArea;

    @Enumerated(EnumType.STRING)
    private TransplantMethod method;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id")
    private Species species;

    private Integer count;      // 개체수
    private Double areaSize;    // 이식 면적 (m2)
}