package com.ocean.piuda.admin.submission.service;

import com.ocean.piuda.admin.submission.dto.response.*;
import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.AuditAction;
import com.ocean.piuda.admin.common.enums.StructureType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.submission.dto.request.*;
import com.ocean.piuda.admin.submission.dto.response.SubmissionDetailResponse;
import com.ocean.piuda.admin.submission.entity.*;
import com.ocean.piuda.admin.submission.entity.embeded.NaturalReproduction;
import com.ocean.piuda.admin.submission.entity.embeded.Survival;
import com.ocean.piuda.admin.submission.repository.AuditLogRepository;
import com.ocean.piuda.admin.submission.repository.SubmissionRepository;
import com.ocean.piuda.admin.submission.validator.ActivityValidator;
import com.ocean.piuda.admin.submission.validator.SubmissionStatusValidator;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class SubmissionCommandService {

    private final SubmissionRepository submissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final SubmissionQueryService submissionQueryService;
    private final ActivityValidator activityValidator;
    private final SubmissionStatusValidator statusValidator;

    /**
     * 임시저장 (DRAFT 상태로 저장)
     */
    public SubmissionDetailResponse saveDraft(CreateSubmissionRequest request) {
        return createSubmissionInternal(request, SubmissionStatus.DRAFT, null);
    }

    /**
     * 바로 제출 (SUBMITTED 상태로 저장)
     */
    public SubmissionDetailResponse submitSubmission(CreateSubmissionRequest request) {
        return createSubmissionInternal(request, SubmissionStatus.SUBMITTED, LocalDateTime.now());
    }

    /**
     * 제출 데이터 생성 내부 메서드
     */
    private SubmissionDetailResponse createSubmissionInternal(
            CreateSubmissionRequest request,
            SubmissionStatus status,
            LocalDateTime submittedAt
    ) {
        // 작업 유형별 검증
        activityValidator.validate(request.getActivityType(), request);

        // 기본값 설정
        LocalDate recordDate = request.getRecordDate() != null 
                ? request.getRecordDate() 
                : LocalDate.now();

        // 구조물 유형 처리 (null이면 OTHER로 기본값, 커스텀 텍스트도 설정)
        StructureType structureType = request.getStructureType() != null 
                ? request.getStructureType() 
                : StructureType.OTHER;
        
        // Submission 생성
        Submission submission = Submission.builder()
                .siteName(request.getSiteName())
                .structureType(structureType)
                .customStructureType(request.getCustomStructureType())
                .recordDate(recordDate)
                .divingRound(request.getDivingRound())
                .activityType(request.getActivityType())
                .status(status)
                .submittedAt(submittedAt)
                .authorName(request.getAuthorName())
                .authorEmail(request.getAuthorEmail())
                .workDescription(request.getWorkDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .attachmentCount(0)
                .build();

        // BasicEnv 생성
        if (request.getBasicEnv() != null) {
            CreateSubmissionRequest.BasicEnvDto envDto = request.getBasicEnv();
            BasicEnv basicEnv = BasicEnv.builder()
                    .recordDate(envDto.getRecordDate() != null ? envDto.getRecordDate() : recordDate)
                    .avgDepthM(envDto.getAvgDepthM())
                    .maxDepthM(envDto.getMaxDepthM())
                    .waterTempC(envDto.getWaterTempC())
                    .visibilityStatus(envDto.getVisibilityStatus())
                    .waveStatus(envDto.getWaveStatus())
                    .surgeStatus(envDto.getSurgeStatus())
                    .currentStatus(envDto.getCurrentStatus())
                    .build();
            submission.setBasicEnv(basicEnv);
        }

        // Participants 생성
        if (request.getParticipants() != null) {
            CreateSubmissionRequest.ParticipantsDto participantsDto = request.getParticipants();
            Participants participants = Participants.builder()
                    .leaderName(participantsDto.getLeaderName())
                    .participantNames(participantsDto.getParticipantNames())
                    .build();
            submission.setParticipants(participants);
        }

        // 작업 유형별 Activity 생성
        createActivityByType(submission, request);

        // Attachments 생성
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (CreateSubmissionRequest.AttachmentDto attachmentDto : request.getAttachments()) {
                Attachment attachment = Attachment.builder()
                        .fileName(attachmentDto.getFileName())
                        .fileUrl(attachmentDto.getFileUrl())
                        .mimeType(attachmentDto.getMimeType())
                        .fileSize(attachmentDto.getFileSize())
                        .uploadedAt(LocalDateTime.now())
                        .build();
                submission.addAttachment(attachment);
            }
        }

        // 저장
        Submission saved = submissionRepository.save(submission);

        // AuditLog 생성
        if (status == SubmissionStatus.SUBMITTED) {
            createAuditLog(saved, AuditAction.SUBMITTED, null);  // 제출 시에만 로그 생성
        }
        // 임시저장(DRAFT)은 AuditLog 생성하지 않음 (제출 시점에 생성)

        return SubmissionDetailResponse.from(saved);
    }

    /**
     * 작업 유형별 Activity 생성 (조건부 처리)
     * 
     * activityType에 따라 해당하는 Activity 엔티티만 생성됨:
     * - TRANSPLANT → ActivityTransplant (이식 작업)
     * - GRAZER_REMOVAL → ActivityGrazerRemoval (조식동물 작업)
     * - SUBSTRATE_IMPROVEMENT → ActivitySubstrateImprovement (부착기질 개선)
     * - MONITORING → ActivityMonitoring (모니터링)
     * - MARINE_CLEANUP → ActivityMarineCleanup (해양정화)
     * 
     * 각 작업 유형에 맞는 DTO만 사용되며, 나머지는 무시됨
     */
    private void createActivityByType(Submission submission, CreateSubmissionRequest request) {
        ActivityType activityType = request.getActivityType();

        switch (activityType) {
            case TRANSPLANT:
                // 이식 작업: transplantActivity DTO 사용
                if (request.getTransplantActivity() != null) {
                    CreateSubmissionRequest.TransplantActivityDto dto = request.getTransplantActivity();
                    ActivityTransplant activity = ActivityTransplant.builder()
                            .submission(submission)
                            .speciesType(dto.getSpeciesType())
                            .locationType(dto.getLocationType())
                            .methodType(dto.getMethodType())
                            .scale(dto.getScale())
                            .zone(dto.getZone())
                            .healthStatus(dto.getHealthStatus())
                            .build();
                    activity.updateSubmission(submission);
                    submission.setActivityTransplant(activity);
                }
                break;

            case GRAZER_REMOVAL:
                // 조식동물 작업: grazerRemovalActivity DTO 사용
                if (request.getGrazerRemovalActivity() != null) {
                    CreateSubmissionRequest.GrazerRemovalActivityDto dto = request.getGrazerRemovalActivity();
                    ActivityGrazerRemoval activity = ActivityGrazerRemoval.builder()
                            .submission(submission)
                            .targetSpecies(dto.getTargetSpecies())
                            .densityBeforeWork(dto.getDensityBeforeWork())
                            .workScope(dto.getWorkScope())
                            .note(dto.getNote())
                            .collectionAmount(dto.getCollectionAmount())
                            .build();
                    activity.updateSubmission(submission);
                    submission.setActivityGrazerRemoval(activity);
                }
                break;

            case SUBSTRATE_IMPROVEMENT:
                // 부착기질 개선: substrateImprovementActivity DTO 사용
                if (request.getSubstrateImprovementActivity() != null) {
                    CreateSubmissionRequest.SubstrateImprovementActivityDto dto = request.getSubstrateImprovementActivity();
                    ActivitySubstrateImprovement activity = ActivitySubstrateImprovement.builder()
                            .submission(submission)
                            .targetType(dto.getTargetType())
                            .workScope(dto.getWorkScope())
                            .substrateState(dto.getSubstrateState())
                            .build();
                    activity.updateSubmission(submission);
                    submission.setActivitySubstrateImprovement(activity);
                }
                break;

            case MONITORING:
                // 모니터링: monitoringActivity DTO 사용
                if (request.getMonitoringActivity() != null) {
                    CreateSubmissionRequest.MonitoringActivityDto dto = request.getMonitoringActivity();
                    ActivityMonitoring activity = ActivityMonitoring.builder()
                            .submission(submission)
                            // 적지조사
                            .entryCoordinate(dto.getEntryCoordinate())
                            .exitCoordinate(dto.getExitCoordinate())
                            .direction(dto.getDirection())
                            // 지형 구성
                            .terrain(dto.getTerrain())
                            // 갯녹음 정도
                            .barrenExtent(dto.getBarrenExtent())
                            // 조식동물 분포
                            .grazerDistribution(dto.getGrazerDistribution())
                            // 암반 특성 (복수)
                            .rockFeatures(dto.getRockFeatures() != null ? dto.getRockFeatures() : new ArrayList<>())
                            // 해조 이식 적합성
                            .suitability(dto.getSuitability())
                            // 해조류 상태
                            .seaweedIdNumber(dto.getSeaweedIdNumber())
                            .seaweedHealthStatus(dto.getSeaweedHealthStatus())
                            // 정밀측정
                            .precisionMeasurement(dto.getPrecisionMeasurement())
                            .leafLength(dto.getLeafLength())
                            .maxLeafWidth(dto.getMaxLeafWidth())
                            .build();
                    activity.updateSubmission(submission);
                    submission.setActivityMonitoring(activity);
                }
                break;

            case MARINE_CLEANUP:
                // 해양정화: marineCleanupActivity DTO 사용
                if (request.getMarineCleanupActivity() != null) {
                    CreateSubmissionRequest.MarineCleanupActivityDto dto = request.getMarineCleanupActivity();
                    ActivityMarineCleanup activity = ActivityMarineCleanup.builder()
                            .submission(submission)
                            .wasteTypes(dto.getWasteTypes())
                            .method(dto.getMethod())
                            .collectionAmount(dto.getCollectionAmount())
                            .uncollectedScale(dto.getUncollectedScale())
                            .build();
                    activity.updateSubmission(submission);
                    submission.setActivityMarineCleanup(activity);
                }
                break;

            // 하위 호환성을 위한 기존 Activity 처리
            default:
                if (request.getActivity() != null) {
                    createLegacyActivity(submission, request.getActivity());
                }
                break;
        }
    }

    /**
     * 기존 Activity 생성 (하위 호환성)
     */
    @Deprecated
    private void createLegacyActivity(Submission submission, CreateSubmissionRequest.ActivityDto activityDto) {
        NaturalReproduction naturalReproduction = null;
        if (activityDto.getNaturalReproduction() != null) {
            naturalReproduction = NaturalReproduction.builder()
                    .radiusM(activityDto.getNaturalReproduction().getRadiusM() != null ? activityDto.getNaturalReproduction().getRadiusM() : 0f)
                    .numerator(activityDto.getNaturalReproduction().getNumerator() != null ? activityDto.getNaturalReproduction().getNumerator() : 0f)
                    .denominator(activityDto.getNaturalReproduction().getDenominator() != null ? activityDto.getNaturalReproduction().getDenominator() : 0f)
                    .build();
        }

        Survival survival = null;
        if (activityDto.getSurvival() != null) {
            survival = Survival.builder()
                    .dieCount(activityDto.getSurvival().getDieCount() != null ? activityDto.getSurvival().getDieCount() : 0f)
                    .totalCount(activityDto.getSurvival().getTotalCount() != null ? activityDto.getSurvival().getTotalCount() : 0f)
                    .build();
        }

        Activity activity = Activity.builder()
                .type(activityDto.getType())
                .details(activityDto.getDetails())
                .collectionAmount(activityDto.getCollectionAmount() != null ? activityDto.getCollectionAmount() : 0f)
                .durationHours(activityDto.getDurationHours() != null ? activityDto.getDurationHours() : 0f)
                .healthGrade(activityDto.getHealthGrade())
                .growthCm(activityDto.getGrowthCm() != null ? activityDto.getGrowthCm() : 0f)
                .naturalReproduction(naturalReproduction)
                .survival(survival)
                .build();
        submission.setActivity(activity);
    }

    /**
     * 임시저장된 기록 제출 (DRAFT -> SUBMITTED)
     */
    public SubmissionDetailResponse submitDraftSubmission(Long submissionId) {
        Submission submission = submissionQueryService.getSubmissionById(submissionId);
        
        if (!statusValidator.canSubmit(submission.getStatus())) {
            if (submission.getStatus() == SubmissionStatus.SUBMITTED) {
                throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_SUBMITTED);
            }
            throw new BusinessException(ExceptionType.SUBMISSION_INVALID_STATUS);
        }

        submission.submit();
        createAuditLog(submission, AuditAction.SUBMITTED, null);

        return SubmissionDetailResponse.from(submission);
    }

    /**
     * 단건 승인
     */
    public SubmissionDetailResponse approveSubmission(Long submissionId) {
        Submission submission = submissionQueryService.getSubmissionById(submissionId);

        if (!statusValidator.canApprove(submission.getStatus())) {
            if (submission.getStatus() == SubmissionStatus.APPROVED) {
                throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_APPROVED);
            }
            throw new BusinessException(ExceptionType.SUBMISSION_INVALID_STATUS);
        }

        submission.approve();
        createAuditLog(submission, AuditAction.APPROVED, null);

        return SubmissionDetailResponse.from(submission);
    }

    /**
     * 단건 반려
     */
    public SubmissionDetailResponse rejectSubmission(Long submissionId, SingleRejectRequest request) {
        Submission submission = submissionQueryService.getSubmissionById(submissionId);

        if (!statusValidator.canReject(submission.getStatus())) {
            if (submission.getStatus() == SubmissionStatus.REJECTED) {
                throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_REJECTED);
            }
            throw new BusinessException(ExceptionType.SUBMISSION_INVALID_STATUS);
        }

        if (request.reason() == null || request.reason().message() == null || request.reason().message().isBlank()) {
            throw new BusinessException(ExceptionType.REJECT_REASON_REQUIRED);
        }

        submission.reject();

        RejectReason rejectReason = RejectReason.builder()
                .submission(submission)
                .templateCode(request.reason().templateCode())
                .message(request.reason().message())
                .rejectedBy(getCurrentUsername())
                .rejectedAt(LocalDateTime.now())
                .build();
        submission.setRejectReason(rejectReason);

        createAuditLog(submission, AuditAction.REJECTED, request.reason().message());

        return SubmissionDetailResponse.from(submission);
    }

    /**
     * 단건 삭제
     */
    public void deleteSubmission(Long submissionId, SingleDeleteRequest request) {
        Submission submission = submissionQueryService.getSubmissionById(submissionId);

        if (submission.getStatus() == SubmissionStatus.DELETED) {
            throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_DELETED);
        }

        createAuditLog(submission, AuditAction.DELETED, request.reason());
        submissionRepository.delete(submission);
    }

    /**
     * 일괄 승인
     */
    public BulkApproveResponse bulkApprove(BulkApproveRequest request) {
        List<Long> approved = new ArrayList<>();
        List<Long> skipped = new ArrayList<>();

        for (Long id : request.ids()) {
            try {
                Submission submission = submissionRepository.findById(id).orElse(null);
                if (submission == null || submission.getStatus() == SubmissionStatus.DELETED) {
                    skipped.add(id);
                    continue;
                }
                if (submission.getStatus() == SubmissionStatus.APPROVED) {
                    skipped.add(id);
                    continue;
                }

                submission.updateStatus(SubmissionStatus.APPROVED);
                createAuditLog(submission, AuditAction.APPROVED, null);
                approved.add(id);
            } catch (Exception e) {
                skipped.add(id);
            }
        }

        return new BulkApproveResponse(approved, skipped);
    }

    /**
     * 일괄 반려
     */
    public BulkRejectResponse bulkReject(BulkRejectRequest request) {
        if (request.reason() == null || request.reason().message() == null || request.reason().message().isBlank()) {
            throw new BusinessException(ExceptionType.REJECT_REASON_REQUIRED);
        }

        List<Long> rejected = new ArrayList<>();
        List<Long> conflicts = new ArrayList<>();

        for (Long id : request.ids()) {
            try {
                Submission submission = submissionRepository.findById(id).orElse(null);
                if (submission == null || submission.getStatus() == SubmissionStatus.DELETED) {
                    conflicts.add(id);
                    continue;
                }
                if (submission.getStatus() == SubmissionStatus.REJECTED) {
                    conflicts.add(id);
                    continue;
                }

                submission.updateStatus(SubmissionStatus.REJECTED);

                RejectReason rejectReason = RejectReason.builder()
                        .submission(submission)
                        .templateCode(request.reason().templateCode())
                        .message(request.reason().message())
                        .rejectedBy(getCurrentUsername())
                        .rejectedAt(LocalDateTime.now())
                        .build();
                submission.setRejectReason(rejectReason);

                createAuditLog(submission, AuditAction.REJECTED, request.reason().message());
                rejected.add(id);
            } catch (Exception e) {
                conflicts.add(id);
            }
        }

        return new BulkRejectResponse(rejected, conflicts);
    }

    /**
     * 일괄 삭제
     */
    public BulkDeleteResponse bulkDelete(BulkDeleteRequest request) {
        List<Long> deleted = new ArrayList<>();
        List<Long> failed = new ArrayList<>();

        for (Long id : request.ids()) {
            try {
                Submission submission = submissionRepository.findById(id).orElse(null);
                if (submission == null || submission.getStatus() == SubmissionStatus.DELETED) {
                    failed.add(id);
                    continue;
                }

                createAuditLog(submission, AuditAction.DELETED, request.reason());
                submissionRepository.delete(submission);
                deleted.add(id);
            } catch (Exception e) {
                failed.add(id);
            }
        }

        return new BulkDeleteResponse(deleted, failed);
    }

    private void createAuditLog(Submission submission, AuditAction action, String comment) {
        AuditLog auditLog = AuditLog.builder()
                .submission(submission)
                .action(action)
                .performedBy(getCurrentUsername())
                .comment(comment)
                .build();
        auditLog.updateSubmission(submission);
        auditLogRepository.save(auditLog);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }

}