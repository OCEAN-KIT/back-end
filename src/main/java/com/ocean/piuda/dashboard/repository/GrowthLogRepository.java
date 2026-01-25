package com.ocean.piuda.dashboard.repository;

import com.ocean.piuda.dashboard.entity.GrowthLog;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface GrowthLogRepository extends JpaRepository<GrowthLog, Long> {
    Optional<GrowthLog> findByIdAndProjectAreaId(Long id, Long projectAreaId);


    /**
     * 특정 작업 영역(ProjectArea)의 대표 개체 성장 로그만 조회
     * - isRepresentative: true 인 데이터만 필터링
     * - recordDate: 시간순(오름차순) 정렬
     */
    List<GrowthLog> findAllByProjectAreaIdAndIsRepresentativeTrueOrderByRecordDateAsc(Long projectAreaId);

    @EntityGraph(attributePaths = {"species"})
    Page<GrowthLog> findAllByProjectAreaIdAndRecordDateBetween(
            Long projectAreaId, LocalDate from, LocalDate to, Pageable pageable
    );

}
