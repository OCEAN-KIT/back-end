package com.ocean.piuda.environment.repository;

import com.ocean.piuda.environment.domain.DivePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 다이빙 포인트 Repository
 */
public interface DivePointRepository extends JpaRepository<DivePoint, Long> {
    Optional<DivePoint> findById(Long id);
}

