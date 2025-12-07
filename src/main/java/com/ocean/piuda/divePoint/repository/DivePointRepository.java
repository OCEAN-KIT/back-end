package com.ocean.piuda.divePoint.repository;

import com.ocean.piuda.divePoint.entity.DivePoint;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 다이빙 포인트 Repository
 */
public interface DivePointRepository extends JpaRepository<DivePoint, Long> {
}