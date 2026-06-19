package com.ocean.piuda.dashboard.repository;

import com.ocean.piuda.dashboard.entity.MediaLog;
import com.ocean.piuda.dashboard.repository.projection.MediaPointProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Repository
public interface MediaLogRepository extends JpaRepository<MediaLog, Long> {

    Optional<MediaLog> findByIdAndProjectAreaId(Long id, Long projectAreaId);


    /**
     * 특정 영역의 모든 사진 데이터를 날짜순으로 조회
     */
    List<MediaPointProjection> findAllByProjectAreaIdOrderByRecordDateAsc(Long areaId);

    Page<MediaLog> findAllByProjectAreaIdAndRecordDateBetween(
            Long projectAreaId, LocalDate from, LocalDate to, Pageable pageable
    );

}