package com.ocean.piuda.mission.service;

import com.ocean.piuda.common.security.CurrentUserProvider;
import com.ocean.piuda.mission.domain.Mission;
import com.ocean.piuda.mission.dto.*;
import com.ocean.piuda.mission.exception.MissionAccessDeniedException;
import com.ocean.piuda.mission.exception.MissionNotFoundException;
import com.ocean.piuda.mission.repository.MissionRepository;
import com.ocean.piuda.security.jwt.enums.Role;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionServiceImpl implements MissionService {

    private final MissionRepository missionRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public MissionResponse createMission(MissionCreateRequest request) {
        // 권한 체크: ADMIN만 가능
        ensureAdmin();

        Long ownerId = currentUserProvider.getCurrentUserId();
        Mission mission = request.toEntity(ownerId);
        mission = missionRepository.save(mission);
        return MissionResponse.from(mission);
    }

    @Override
    public MissionResponse getMission(Long id) {
        Mission mission = findMission(id);
        return MissionResponse.from(mission);
    }

    @Override
    public Page<MissionResponse> getMissions(MissionSearchCondition condition, Pageable pageable) {
        Specification<Mission> spec = buildSpecification(condition);
        return missionRepository.findAll(spec, pageable)
                .map(MissionResponse::from);
    }

    @Override
    @Transactional
    public MissionResponse updateMission(Long id, MissionUpdateRequest request) {
        Mission mission = findMission(id);

        // 권한 체크: ADMIN 또는 owner만 가능
        ensureOwnerOrAdmin(mission.getOwnerId());

        Mission.MissionUpdater updater = Mission.MissionUpdater.builder()
                .title(request.title())
                .targetBioGroup(request.targetBioGroup())
                .pointId(request.pointId())
                .description(request.description())
                .regionName(request.regionName())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status())
                .coverMediaUrl(request.coverMediaUrl())
                .build();

        mission.update(updater);
        return MissionResponse.from(mission);
    }

    @Override
    @Transactional
    public void deleteMission(Long id) {
        Mission mission = findMission(id);

        // 권한 체크: ADMIN 또는 owner만 가능
        ensureOwnerOrAdmin(mission.getOwnerId());

        missionRepository.delete(mission);
    }

    private Mission findMission(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException(id));
    }

    private void ensureAdmin() {
        Role currentRole = currentUserProvider.getCurrentUserRole();
        if (currentRole != Role.ADMIN) {
            throw new MissionAccessDeniedException("ADMIN 권한이 필요합니다.");
        }
    }

    private void ensureOwnerOrAdmin(Long ownerId) {
        Role currentRole = currentUserProvider.getCurrentUserRole();
        Long currentUserId = currentUserProvider.getCurrentUserId();

        if (currentRole != Role.ADMIN && !ownerId.equals(currentUserId)) {
            throw new MissionAccessDeniedException("미션 소유자 또는 ADMIN 권한이 필요합니다.");
        }
    }

    private Specification<Mission> buildSpecification(MissionSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (condition != null) {
                if (condition.status() != null) {
                    predicates.add(cb.equal(root.get("status"), condition.status()));
                }
                if (condition.targetBioGroup() != null) {
                    predicates.add(cb.equal(root.get("targetBioGroup"), condition.targetBioGroup()));
                }
                if (condition.regionName() != null && !condition.regionName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("regionName")),
                            "%" + condition.regionName().toLowerCase() + "%"
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

