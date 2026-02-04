package com.ocean.piuda.admin.submission.validator;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.submission.dto.request.CreateSubmissionRequest;
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
                // 기존 유형들은 기존 Activity로 처리
                break;
        }
    }

    private void validateTransplant(CreateSubmissionRequest.TransplantActivityDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("이식 작업 정보는 필수입니다");
        }

        Set<ConstraintViolation<CreateSubmissionRequest.TransplantActivityDto>> violations = 
            validator.validate(dto);
        
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                "이식 작업 정보 검증 실패: " + violations.iterator().next().getMessage()
            );
        }

        // 추가 검증
        if (dto.getSpeciesType() == null) {
            throw new IllegalArgumentException("대상 종류는 필수입니다");
        }
        if (dto.getLocationType() == null) {
            throw new IllegalArgumentException("이식 장소는 필수입니다");
        }
        if (dto.getMethodType() == null) {
            throw new IllegalArgumentException("이식 방식은 필수입니다");
        }
        if (dto.getScale() == null || dto.getScale().isBlank()) {
            throw new IllegalArgumentException("이식 규모는 필수입니다");
        }
        if (dto.getHealthStatus() == null) {
            throw new IllegalArgumentException("건강상태는 필수입니다");
        }
    }

    private void validateGrazerRemoval(CreateSubmissionRequest.GrazerRemovalActivityDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("조식동물 작업 정보는 필수입니다");
        }

        Set<ConstraintViolation<CreateSubmissionRequest.GrazerRemovalActivityDto>> violations = 
            validator.validate(dto);
        
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                "조식동물 작업 정보 검증 실패: " + violations.iterator().next().getMessage()
            );
        }

        if (dto.getTargetSpecies() == null || dto.getTargetSpecies().isEmpty()) {
            throw new IllegalArgumentException("대상 생물은 최소 1개 이상 선택해야 합니다");
        }
        if (dto.getDensityBeforeWork() == null) {
            throw new IllegalArgumentException("작업 전 밀도는 필수입니다");
        }
        if (dto.getWorkScope() == null) {
            throw new IllegalArgumentException("작업 범위는 필수입니다");
        }
        if (dto.getCollectionAmount() == null || dto.getCollectionAmount().isBlank()) {
            throw new IllegalArgumentException("수거량은 필수입니다");
        }
    }

    private void validateSubstrateImprovement(CreateSubmissionRequest.SubstrateImprovementActivityDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("부착기질 개선 작업 정보는 필수입니다");
        }

        if (dto.getTargetType() == null) {
            throw new IllegalArgumentException("작업 대상은 필수입니다");
        }
        if (dto.getWorkScope() == null || dto.getWorkScope().isBlank()) {
            throw new IllegalArgumentException("작업 범위는 필수입니다");
        }
        if (dto.getSubstrateState() == null || dto.getSubstrateState().isBlank()) {
            throw new IllegalArgumentException("작업 후 기질 상태는 필수입니다");
        }
    }

    private void validateMonitoring(CreateSubmissionRequest.MonitoringActivityDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("모니터링 작업 정보는 필수입니다");
        }

        Set<ConstraintViolation<CreateSubmissionRequest.MonitoringActivityDto>> violations =
            validator.validate(dto);

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                "모니터링 작업 정보 검증 실패: " + violations.iterator().next().getMessage()
            );
        }

        // 추가 검증
        if (dto.getTerrain() == null) {
            throw new IllegalArgumentException("지형 구성은 필수입니다");
        }
        if (dto.getBarrenExtent() == null) {
            throw new IllegalArgumentException("갯녹음 정도는 필수입니다");
        }
        if (dto.getGrazerDistribution() == null) {
            throw new IllegalArgumentException("조식동물 분포는 필수입니다");
        }
        if (dto.getRockFeatures() == null || dto.getRockFeatures().isEmpty()) {
            throw new IllegalArgumentException("암반 특성은 최소 1개 이상 선택해야 합니다");
        }
        if (dto.getSuitability() == null) {
            throw new IllegalArgumentException("해조 이식 적합성은 필수입니다");
        }
    }

    private void validateMarineCleanup(CreateSubmissionRequest.MarineCleanupActivityDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("해양정화 작업 정보는 필수입니다");
        }

        if (dto.getWasteTypes() == null || dto.getWasteTypes().isEmpty()) {
            throw new IllegalArgumentException("폐기물 유형은 최소 1개 이상 선택해야 합니다");
        }
        if (dto.getMethod() == null) {
            throw new IllegalArgumentException("인양 방식은 필수입니다");
        }
        if (dto.getCollectionAmount() == null || dto.getCollectionAmount().isBlank()) {
            throw new IllegalArgumentException("수거량은 필수입니다");
        }
    }
}
