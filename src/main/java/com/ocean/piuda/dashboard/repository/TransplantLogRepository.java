package com.ocean.piuda.dashboard.repository;

import com.ocean.piuda.dashboard.entity.TransplantLog;
import com.ocean.piuda.dashboard.entity.WaterLog;
import com.ocean.piuda.dashboard.repository.projection.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TransplantLogRepository extends JpaRepository<TransplantLog, Long> {

    Optional<TransplantLog> findByIdAndProjectAreaId(Long id, Long projectAreaId);


    /**
     * 이식 방식별 최신 착생 상태
     */
    @Query(value = """
    SELECT DISTINCT ON (t.method)
        t.method as methodName,
        t.attachment_status as statusName
    FROM transplant_logs t
    WHERE t.area_id = :areaId
   ORDER BY t.method, t.record_date DESC NULLS LAST, t.id DESC
   """, nativeQuery = true)
    List<MethodAttachmentStatusProjection> findLatestAttachmentStatusPerMethod(@Param("areaId") Long areaId);


    /**
     * 방식별 분포: DB에서 집계 (전체 기간)
     */
    @Query("SELECT t.method as methodName, COUNT(t) as count FROM TransplantLog t " +
            "WHERE t.projectArea.id = :areaId GROUP BY t.method")
    List<MethodDistributionProjection> findMethodDistribution(@Param("areaId") Long areaId);

    /**
     * 누적 통계: DB에서 집계 (전체 기간)
     */
    @Query("""
       SELECT COUNT(t) as totalCount,
              MAX(t.recordDate) as lastDate,
              COALESCE(SUM(t.areaSize), 0) as totalArea
       FROM TransplantLog t
       WHERE t.projectArea.id = :areaId""")

    AccumulatedStatsProjection getAccumulatedStats(@Param("areaId") Long areaId);

    /**
     * 작업 히스토리: 최근 3개월 월별 집계 (DB에서 연-월 그룹화)
     */
    @Query(value = """
        SELECT (DATE_TRUNC('month', record_date))::date as month, COUNT(*) as count
        FROM transplant_logs
        WHERE area_id = :areaId
        AND record_date > (SELECT MAX(record_date) FROM transplant_logs WHERE area_id = :areaId) - INTERVAL '3 months'
        GROUP BY month ORDER BY month ASC
        """, nativeQuery = true)
    List<WorkHistoryPointProjection> findWorkHistory(@Param("areaId") Long areaId);


    @Query("""
            SELECT t.species.name as speciesName,
                   t.method as methodName,
                   SUM(t.count) as totalCount
            FROM TransplantLog t
            WHERE t.projectArea.id = :areaId
            GROUP BY t.species.name, t.method
    """)
    List<TransplantItemProjection> findTransplantItems(@Param("areaId") Long areaId);

    @EntityGraph(attributePaths = {"species"})
    Page<TransplantLog> findAllByProjectAreaIdAndRecordDateBetween(
            Long projectAreaId, LocalDate from, LocalDate to, Pageable pageable
    );

}
