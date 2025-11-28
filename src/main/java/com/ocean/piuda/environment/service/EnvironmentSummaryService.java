package com.ocean.piuda.environment.service;

import com.ocean.piuda.environment.dto.EnvironmentSummaryRequest;
import com.ocean.piuda.environment.dto.EnvironmentSummaryResponse;

/**
 * 해양 환경 요약 서비스 인터페이스
 */
public interface EnvironmentSummaryService {

    /**
     * 해양 환경 요약 조회
     *
     * @param request 요청 파라미터 (lat/lon 또는 pointId)
     * @return 환경 요약 정보
     */
    EnvironmentSummaryResponse getEnvironmentSummary(EnvironmentSummaryRequest request);
}

