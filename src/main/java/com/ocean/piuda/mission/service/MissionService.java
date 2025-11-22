package com.ocean.piuda.mission.service;

import com.ocean.piuda.mission.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MissionService {

    /**
     * 미션 생성 (ADMIN만 가능)
     */
    MissionResponse createMission(MissionCreateRequest request);

    /**
     * 미션 단건 조회 (모든 사용자 접근 가능)
     */
    MissionResponse getMission(Long id);

    /**
     * 미션 목록 조회 (필터링 지원, 모든 사용자 접근 가능)
     */
    Page<MissionResponse> getMissions(MissionSearchCondition condition, Pageable pageable);

    /**
     * 미션 수정 (ADMIN 또는 owner만 가능)
     */
    MissionResponse updateMission(Long id, MissionUpdateRequest request);

    /**
     * 미션 삭제 (ADMIN 또는 owner만 가능)
     */
    void deleteMission(Long id);
}

