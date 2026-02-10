package com.ocean.piuda.dashboard.repository;

import com.ocean.piuda.bio.entity.Species;
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
     * 작업 히스토리: 최근 데이터가 존재하는 3개의 월만 조회 (항상 3개 이하 보장)
     * - 로직 변경: 기간 계산(Interval) -> 개수 제한(Limit)
     * - 서브쿼리: 최신순으로 3개를 뽑고 (LIMIT 3)
     * - 메인쿼리: 차트 X축 순서를 위해 다시 날짜 오름차순(ASC) 정렬
     */
    @Query(value = """
        SELECT sub.month, sub.count 
        FROM (
            SELECT (DATE_TRUNC('month', record_date))::date as month, COUNT(*) as count
            FROM transplant_logs
            WHERE area_id = :areaId
            GROUP BY month
            ORDER BY month DESC
            LIMIT 3
        ) sub
        ORDER BY sub.month ASC
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

    /**
     * 특정 영역에 이식된 적이 있는 모든 종(Species) 목록 조회 (중복 제거)
     * - 드롭다운 후보군 제공용
     */
    @Query("SELECT DISTINCT t.species FROM TransplantLog t WHERE t.projectArea.id = :areaId")
    List<Species> findDistinctSpeciesByAreaId(@Param("areaId") Long areaId);

}
