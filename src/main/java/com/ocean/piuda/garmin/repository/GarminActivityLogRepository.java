package com.ocean.piuda.garmin.repository;

import com.ocean.piuda.garmin.entity.GarminActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface GarminActivityLogRepository extends JpaRepository<GarminActivityLog, Long> {

    // 같은 기기 + 같은 시작 시간 기준 탐색
    List<GarminActivityLog> findAllByDeviceIdAndStartTime(String deviceId, LocalDateTime startTime);

    // sessionId 기준으로 세션 전체 조회
    List<GarminActivityLog> findAllBySessionId(String sessionId);


    @Query("""
        SELECT l
        FROM GarminActivityLog l
        WHERE l.userId = :userId
          AND l.sessionId IS NOT NULL
          AND l.id = (
              SELECT MIN(l2.id)
              FROM GarminActivityLog l2
              WHERE l2.userId = :userId
                AND l2.sessionId = l.sessionId
          )
        """)
    Page<GarminActivityLog> findSessionHeadsByUserId(@Param("userId") Long userId,
                                                     Pageable pageable);


}
