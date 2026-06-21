package com.ocean.piuda.submission.validator;

import com.ocean.piuda.submission.enums.ActivityType;
import com.ocean.piuda.submission.dto.request.CreateSubmissionRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 작업 유형별 Activity 검증
 */
@Component
@RequiredArgsConstructor
public class ActivityValidator {

    private final Validator validator;

    /**
     * 작업 유형별 필수값 검증
     */
    public void validate(ActivityType activityType, CreateSubmissionRequest request) {
        switch (activityType) {
            case TRANSPLANT:
                validateTransplant(request.getTransplantActivity());
                break;
            case GRAZER_REMOVAL:
                validateGrazerRemoval(request.getGrazerRemovalActivity());
                break;
            case SUBSTRATE_IMPROVEMENT:
                validateSubstrateImprovement(request.getSubstrateImprovementActivity());
                break;
            case MONITORING:
                validateMonitoring(request.getMonitoringActivity());
                break;
            case MARINE_CLEANUP:
                validateMarineCleanup(request.getMarineCleanupActivity());
                break;
            default:
                // OTHER 등 기타 유형은 별도 DTO 검증 없음 (필요시 추가)
                break;
        }
    }

    private void validateTransplant(CreateSubmissionRequest.TransplantActivityDto dto) {
        if (dto == null) throw new IllegalArgumentException("이식 작업 정보는 필수입니다");
        validateAnnotations(dto, "이식 작업");
    }

    private void validateGrazerRemoval(CreateSubmissionRequest.GrazerRemovalActivityDto dto) {
        if (dto == null) throw new IllegalArgumentException("조식동물 작업 정보는 필수입니다");
        validateAnnotations(dto, "조식동물 작업");
        if (dto.getTargetSpecies() == null || dto.getTargetSpecies().isEmpty()) {
            throw new IllegalArgumentException("대상 생물은 최소 1개 이상 선택해야 합니다");
        }
    }

    private void validateSubstrateImprovement(CreateSubmissionRequest.SubstrateImprovementActivityDto dto) {
        if (dto == null) throw new IllegalArgumentException("부착기질 개선 작업 정보는 필수입니다");
        validateAnnotations(dto, "부착기질 개선 작업");
    }

    private void validateMonitoring(CreateSubmissionRequest.MonitoringActivityDto dto) {
        if (dto == null) throw new IllegalArgumentException("모니터링 작업 정보는 필수입니다");

        validateAnnotations(dto, "모니터링 작업");

        // 암반 특성 리스트 검증
        if (dto.getRockFeatures() == null || dto.getRockFeatures().isEmpty()) {
            throw new IllegalArgumentException("암반 특성은 최소 1개 이상 선택해야 합니다");
        }

    }

    private void validateMarineCleanup(CreateSubmissionRequest.MarineCleanupActivityDto dto) {
        if (dto == null) throw new IllegalArgumentException("해양정화 작업 정보는 필수입니다");

        validateAnnotations(dto, "해양정화 작업");

        if (dto.getWasteTypes() == null || dto.getWasteTypes().isEmpty()) {
            throw new IllegalArgumentException("폐기물 유형은 최소 1개 이상 선택해야 합니다");
        }
    }

    /**
     * DTO 내부의 어노테이션(@NotNull 등)을 수동으로 트리거하여 검증
     */
    private <T> void validateAnnotations(T dto, String targetName) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            // 첫 번째 에러 메시지만 반환
            throw new IllegalArgumentException(
                    targetName + " 정보 검증 실패: " + violations.iterator().next().getMessage()
            );
        }
    }
}
