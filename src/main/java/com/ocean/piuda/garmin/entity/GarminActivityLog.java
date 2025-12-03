package com.ocean.piuda.garmin.entity;

import com.ocean.piuda.garmin.enums.GarminActivityType;
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

    /** 한 번의 세션 요청 단위 식별자 */
    @Column(name = "session_id", length = 64)
    private String sessionId;

    /** 로그인 사용자(웹/앱 연동 시). 워치 단독이면 null 가능 */
    @Column(name = "user_id")
    private Long userId;

    /** 워치 기기 고유 ID (String, 암호화/해시된 값) */
    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GarminActivityType garminActivityType;

    @Column(length = 20)
    private String gridId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer totalCount;

    // 위치 정보 (GPS) - 세션 단위 정보를 각 활동 로그에 복사해서 저장
    private Double startLat;
    private Double startLon;
    private Double endLat;
    private Double endLon;

    /** DB: jsonb, Java: List<List<Object>> */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<List<Object>> workLogs;
}
