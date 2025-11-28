package com.ocean.piuda.environment.repository;

import com.ocean.piuda.environment.domain.MarineStation;
import com.ocean.piuda.environment.domain.StationSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 해양 관측소 Repository
 */
public interface MarineStationRepository extends JpaRepository<MarineStation, Long> {

    /**
     * 외부 소스와 외부 ID로 관측소 조회
     */
    Optional<MarineStation> findByExternalSourceAndExternalStationId(
            StationSource source, String externalStationId);

    /**
     * 활성화된 관측소만 조회
     */
    List<MarineStation> findByExternalSourceAndIsActiveTrue(StationSource source);

    /**
     * 외부 소스로 관측소 삭제
     */
    void deleteByExternalSource(StationSource source);
}

