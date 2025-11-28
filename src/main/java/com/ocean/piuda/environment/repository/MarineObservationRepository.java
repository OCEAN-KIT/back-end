package com.ocean.piuda.environment.repository;

import com.ocean.piuda.environment.domain.MarineObservation;
import com.ocean.piuda.environment.domain.MarineStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * 해양 관측값 Repository
 */
public interface MarineObservationRepository extends JpaRepository<MarineObservation, Long> {

    /**
     * 관측소와 관측 시각으로 최신 관측값 조회
     */
    Optional<MarineObservation> findTopByStationOrderByObservedAtDesc(MarineStation station);

    /**
     * 관측소와 시간 범위로 관측값 조회
     */
    Optional<MarineObservation> findTopByStationAndObservedAtBetweenOrderByObservedAtDesc(
            MarineStation station, ZonedDateTime start, ZonedDateTime end);
}

