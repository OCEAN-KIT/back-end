package com.ocean.piuda.mission.controller;

import com.ocean.piuda.bio.enums.BioGroup;
import com.ocean.piuda.mission.enums.MissionStatus;
import com.ocean.piuda.mission.dto.*;
import com.ocean.piuda.mission.service.MissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Mission 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    /**
     * 미션 생성 (ADMIN만 가능)
     * POST /api/missions
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MissionResponse createMission(@Valid @RequestBody MissionCreateRequest request) {
        return missionService.createMission(request);
    }

    /**
     * 미션 단건 조회 (모든 사용자 접근 가능)
     * GET /api/missions/{id}
     */
    @GetMapping("/{id}")
    public MissionResponse getMission(@PathVariable Long id) {
        return missionService.getMission(id);
    }

    /**
     * 미션 목록 조회 (필터링 지원, 모든 사용자 접근 가능)
     * GET /api/missions?status=ACTIVE&targetBioGroup=FISH&regionName=포항&page=0&size=20
     */
    @GetMapping
    public Page<MissionResponse> getMissions(
            @RequestParam(required = false) MissionStatus status,
            @RequestParam(required = false) BioGroup targetBioGroup,
            @RequestParam(required = false) String regionName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MissionSearchCondition condition = MissionSearchCondition.builder()
                .status(status)
                .targetBioGroup(targetBioGroup)
                .regionName(regionName)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        return missionService.getMissions(condition, pageable);
    }

    /**
     * 미션 수정 (ADMIN 또는 owner만 가능)
     * PATCH /api/missions/{id}
     */
    @PatchMapping("/{id}")
    public MissionResponse updateMission(
            @PathVariable Long id,
            @Valid @RequestBody MissionUpdateRequest request
    ) {
        return missionService.updateMission(id, request);
    }

    /**
     * 미션 삭제 (ADMIN 또는 owner만 가능)
     * DELETE /api/missions/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
    }
}

