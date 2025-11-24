package com.ocean.piuda.activity.entity;


import com.ocean.piuda.activity.enums.GarminActivityType;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "activity_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GarminActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 사용자. 워치 단독 업로드면 null 허용 */
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GarminActivityType garminActivityType;

    @Column(length = 20)
    private String gridId;

    private Long teamId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer totalCount;

    // 위치 정보 (GPS)
    private Double startLat;
    private Double startLon;
    private Double endLat;
    private Double endLon;

    /** DB: jsonb, Java: List<List<Object>> */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<List<Object>> workLogs;
}
