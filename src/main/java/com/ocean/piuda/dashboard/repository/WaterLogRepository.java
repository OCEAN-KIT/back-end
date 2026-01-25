package com.ocean.piuda.dashboard.repository;

import com.ocean.piuda.dashboard.entity.WaterLog;
import com.ocean.piuda.dashboard.repository.projection.EnvironmentSummaryProjection;
import com.ocean.piuda.dashboard.repository.projection.TemperaturePointProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;


@Repository
public interface WaterLogRepository extends JpaRepository<WaterLog, Long> {

    Optional<WaterLog> findByIdAndProjectAreaId(Long id, Long projectAreaId);


    /**
     * 수온 차트: 전체 기간에서 날짜와 온도만 (엔티티 로딩 X)
     */
    @Query("SELECT w.recordDate as recordDate, w.temperature as temperature " +
            "FROM WaterLog w WHERE w.projectArea.id = :areaId ORDER BY w.recordDate ASC")
    List<TemperaturePointProjection> findTemperatureHistory(@Param("areaId") Long areaId);

    /**
     * 환경 요약: 가장 최근 1건의 상태만 조회
     */
    @Query(value = "SELECT * FROM water_logs WHERE area_id = :areaId ORDER BY record_date DESC LIMIT 1", nativeQuery = true)
    Optional<WaterLog> findTopByAreaIdOrderByRecordDateDesc(@Param("areaId") Long areaId);

    @Query(value = """
        SELECT
          (SELECT w.visibility
             FROM water_logs w
            WHERE w.area_id = :areaId
              AND w.record_date > (SELECT MAX(record_date) FROM water_logs WHERE area_id = :areaId) - INTERVAL '3 months'
            GROUP BY w.visibility
            ORDER BY COUNT(*) DESC, w.visibility
            LIMIT 1
          ) AS visibility,

          (SELECT w."current"
             FROM water_logs w
            WHERE w.area_id = :areaId
              AND w.record_date > (SELECT MAX(record_date) FROM water_logs WHERE area_id = :areaId) - INTERVAL '3 months'
            GROUP BY w."current"
            ORDER BY COUNT(*) DESC, w."current"
            LIMIT 1
          ) AS current,

          (SELECT w.surge
             FROM water_logs w
            WHERE w.area_id = :areaId
              AND w.record_date > (SELECT MAX(record_date) FROM water_logs WHERE area_id = :areaId) - INTERVAL '3 months'
            GROUP BY w.surge
            ORDER BY COUNT(*) DESC, w.surge
            LIMIT 1
          ) AS surge,

          (SELECT w.wave
             FROM water_logs w
            WHERE w.area_id = :areaId
              AND w.record_date > (SELECT MAX(record_date) FROM water_logs WHERE area_id = :areaId) - INTERVAL '3 months'
            GROUP BY w.wave
            ORDER BY COUNT(*) DESC, w.wave
            LIMIT 1
          ) AS wave
        """, nativeQuery = true)
    EnvironmentSummaryProjection findEnvironmentSummaryModeLast3Months(@Param("areaId") Long areaId);

    Page<WaterLog> findAllByProjectAreaIdAndRecordDateBetween(
            Long projectAreaId, LocalDate from, LocalDate to, Pageable pageable
    );

}
