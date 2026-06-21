package com.ocean.piuda.submission.service;

import com.ocean.piuda.submission.enums.ActivityType;
import com.ocean.piuda.submission.enums.AuditAction;
import com.ocean.piuda.submission.enums.SubmissionStatus;
import com.ocean.piuda.site.entity.SiteNameOption;
import com.ocean.piuda.site.repository.SiteNameOptionRepository;
import com.ocean.piuda.submission.dto.request.*;
import com.ocean.piuda.submission.dto.response.BulkApproveResponse;
import com.ocean.piuda.submission.dto.response.BulkDeleteResponse;
import com.ocean.piuda.submission.dto.response.BulkRejectResponse;
import com.ocean.piuda.submission.dto.response.SubmissionDetailResponse;
import com.ocean.piuda.submission.entity.*;
import com.ocean.piuda.submission.repository.AuditLogRepository;
import com.ocean.piuda.submission.repository.SubmissionRepository;
import com.ocean.piuda.submission.validator.ActivityValidator;
import com.ocean.piuda.submission.validator.SubmissionStatusValidator;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import com.ocean.piuda.user.entity.User;
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
    private final TokenUserService tokenUserService;
    private final SiteNameOptionRepository siteNameOptionRepository;

    /**
     * 기록 제출.
     * 제출된 데이터는 SUBMITTED 상태로 저장됩니다.
     */
    public SubmissionDetailResponse submitSubmission(CreateSubmissionRequest request) {
        return createSubmissionInternal(request, SubmissionStatus.SUBMITTED, LocalDateTime.now());
    }

    private SubmissionDetailResponse createSubmissionInternal(
            CreateSubmissionRequest request,
            SubmissionStatus status,
            LocalDateTime submittedAt
    ) {
        activityValidator.validate(request.getActivityType(), request);

        LocalDate recordDate = request.getRecordDate() != null
                ? request.getRecordDate()
                : LocalDate.now();

        String finalSiteName;
        SiteNameOption siteOption = null;

        if (request.getSiteNameOptionId() != null) {
            siteOption = siteNameOptionRepository.findById(request.getSiteNameOptionId())
                    .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
            finalSiteName = siteOption.getName();
        } else {
            if (request.getSiteName() == null || request.getSiteName().isBlank()) {
                throw new BusinessException(ExceptionType.INVALID_INPUT_VALUE);
            }
            finalSiteName = request.getSiteName();
        }

        User currentUser = tokenUserService.getCurrentUser();

        Submission submission = Submission.builder()
                .siteName(finalSiteName)
                .siteNameOption(siteOption)
                .recordDate(recordDate)
                .divingRound(request.getDivingRound())
                .activityType(request.getActivityType())
                .status(status)
                .submittedAt(submittedAt)
                .user(currentUser)
                .authorName(currentUser.getNickname())
                .authorEmail(currentUser.getEmail())
                .workDescription(request.getWorkDescription())
                .attachmentCount(0)
                .build();

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

        if (request.getParticipants() != null) {
            submission.updateParticipantNames(request.getParticipants().getParticipantNames());
        }

        createActivityByType(submission, request);
        addAttachments(submission, request.getAttachments());

        Submission saved = submissionRepository.save(submission);

        if (status == SubmissionStatus.SUBMITTED) {
            createAuditLog(saved, AuditAction.SUBMITTED, null);
        }

        return SubmissionDetailResponse.from(saved);
    }

    private void createActivityByType(Submission submission, CreateSubmissionRequest request) {
        ActivityType activityType = request.getActivityType();

        switch (activityType) {
            case TRANSPLANT:
                createTransplantActivity(submission, request.getTransplantActivity());
                break;
            case GRAZER_REMOVAL:
                createGrazerRemovalActivity(submission, request.getGrazerRemovalActivity());
                break;
            case SUBSTRATE_IMPROVEMENT:
                createSubstrateImprovementActivity(submission, request.getSubstrateImprovementActivity());
                break;
            case MONITORING:
                createMonitoringActivity(submission, request.getMonitoringActivity());
                break;
            case MARINE_CLEANUP:
                createMarineCleanupActivity(submission, request.getMarineCleanupActivity());
                break;
            default:
                break;
        }
    }

    private void createTransplantActivity(
            Submission submission,
            CreateSubmissionRequest.TransplantActivityDto dto
    ) {
        if (dto == null) {
            return;
        }

        ActivityTransplant activity = ActivityTransplant.builder()
                .submission(submission)
                .speciesType(dto.getSpeciesType())
                .locationType(dto.getLocationType())
                .methodType(dto.getMethodType())
                .scale(dto.getScale())
                .healthStatus(dto.getHealthStatus())
                .build();

        submission.setActivityTransplant(activity);
    }

    private void createGrazerRemovalActivity(
            Submission submission,
            CreateSubmissionRequest.GrazerRemovalActivityDto dto
    ) {
        if (dto == null) {
            return;
        }

        ActivityGrazerRemoval activity = ActivityGrazerRemoval.builder()
                .submission(submission)
                .targetSpecies(dto.getTargetSpecies())
                .densityBeforeWork(dto.getDensityBeforeWork())
                .workScope(dto.getWorkScope())
                .note(dto.getNote())
                .collectionAmount(dto.getCollectionAmount())
                .build();

        submission.setActivityGrazerRemoval(activity);
    }

    private void createSubstrateImprovementActivity(
            Submission submission,
            CreateSubmissionRequest.SubstrateImprovementActivityDto dto
    ) {
        if (dto == null) {
            return;
        }

        ActivitySubstrateImprovement activity = ActivitySubstrateImprovement.builder()
                .submission(submission)
                .targetType(dto.getTargetType())
                .workScope(dto.getWorkScope())
                .substrateState(dto.getSubstrateState())
                .build();

        submission.setActivitySubstrateImprovement(activity);
    }

    private void createMonitoringActivity(
            Submission submission,
            CreateSubmissionRequest.MonitoringActivityDto dto
    ) {
        if (dto == null) {
            return;
        }

        ActivityMonitoring activity = ActivityMonitoring.builder()
                .submission(submission)
                .entryCoordinate(dto.getEntryCoordinate())
                .exitCoordinate(dto.getExitCoordinate())
                .direction(dto.getDirection())
                .terrain(dto.getTerrain())
                .barrenExtent(dto.getBarrenExtent())
                .grazerDistribution(dto.getGrazerDistribution())
                .rockFeatures(dto.getRockFeatures() != null ? dto.getRockFeatures() : new ArrayList<>())
                .suitability(dto.getSuitability())
                .seaweedIdNumber(dto.getSeaweedIdNumber())
                .seaweedHealthStatus(dto.getSeaweedHealthStatus())
                .leafLength(dto.getLeafLength())
                .maxLeafWidth(dto.getMaxLeafWidth())
                .build();

        submission.setActivityMonitoring(activity);
    }

    private void createMarineCleanupActivity(
            Submission submission,
            CreateSubmissionRequest.MarineCleanupActivityDto dto
    ) {
        if (dto == null) {
            return;
        }

        ActivityMarineCleanup activity = ActivityMarineCleanup.builder()
                .submission(submission)
                .wasteTypes(dto.getWasteTypes())
                .method(dto.getMethod())
                .collectionAmount(dto.getCollectionAmount())
                .uncollectedScale(dto.getUncollectedScale())
                .build();

        submission.setActivityMarineCleanup(activity);
    }

    private void addAttachments(
            Submission submission,
            List<CreateSubmissionRequest.AttachmentDto> attachmentDtos
    ) {
        if (attachmentDtos == null || attachmentDtos.isEmpty()) {
            return;
        }

        for (CreateSubmissionRequest.AttachmentDto attachmentDto : attachmentDtos) {
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

    /**
     * 단건 승인.
     */
    public SubmissionDetailResponse approveSubmission(Long submissionId) {
        Submission submission = submissionQueryService.getSubmissionById(submissionId);

        validateCanApprove(submission);

        submission.approve();
        createAuditLog(submission, AuditAction.APPROVED, null);

        return SubmissionDetailResponse.from(submission);
    }

    /**
     * 단건 반려.
     */
    public SubmissionDetailResponse rejectSubmission(Long submissionId, SingleRejectRequest request) {
        validateRejectReason(request.reason());

        Submission submission = submissionQueryService.getSubmissionById(submissionId);

        validateCanReject(submission);

        submission.reject();
        setRejectReason(submission, request.reason());
        createAuditLog(submission, AuditAction.REJECTED, request.reason().message());

        return SubmissionDetailResponse.from(submission);
    }

    /**
     * 단건 삭제.
     *
     * 현재 삭제는 상태 전이가 아니라 실제 삭제로 처리합니다.
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
     * 일괄 승인.
     *
     * 단건 승인과 동일하게 SUBMITTED 상태만 APPROVED 로 변경합니다.
     * APPROVED / REJECTED / DELETED / 존재하지 않는 ID 는 skipped 로 분류합니다.
     */
    public BulkApproveResponse bulkApprove(BulkApproveRequest request) {
        List<Long> approved = new ArrayList<>();
        List<Long> skipped = new ArrayList<>();

        for (Long id : request.ids()) {
            if (id == null) {
                skipped.add(null);
                continue;
            }

            Submission submission = submissionRepository.findById(id).orElse(null);

            if (submission == null || !statusValidator.canApprove(submission.getStatus())) {
                skipped.add(id);
                continue;
            }

            submission.approve();
            createAuditLog(submission, AuditAction.APPROVED, null);
            approved.add(id);
        }

        return new BulkApproveResponse(approved, skipped);
    }

    /**
     * 일괄 반려.
     *
     * 단건 반려와 동일하게 SUBMITTED 상태만 REJECTED 로 변경합니다.
     * APPROVED / REJECTED / DELETED / 존재하지 않는 ID 는 conflicts 로 분류합니다.
     */
    public BulkRejectResponse bulkReject(BulkRejectRequest request) {
        validateRejectReason(request.reason());

        List<Long> rejected = new ArrayList<>();
        List<Long> conflicts = new ArrayList<>();

        for (Long id : request.ids()) {
            if (id == null) {
                conflicts.add(null);
                continue;
            }

            Submission submission = submissionRepository.findById(id).orElse(null);

            if (submission == null || !statusValidator.canReject(submission.getStatus())) {
                conflicts.add(id);
                continue;
            }

            submission.reject();
            setRejectReason(submission, request.reason());
            createAuditLog(submission, AuditAction.REJECTED, request.reason().message());

            rejected.add(id);
        }

        return new BulkRejectResponse(rejected, conflicts);
    }

    /**
     * 일괄 삭제.
     */
    public BulkDeleteResponse bulkDelete(BulkDeleteRequest request) {
        List<Long> deleted = new ArrayList<>();
        List<Long> failed = new ArrayList<>();

        for (Long id : request.ids()) {
            if (id == null) {
                failed.add(null);
                continue;
            }

            Submission submission = submissionRepository.findById(id).orElse(null);

            if (submission == null || submission.getStatus() == SubmissionStatus.DELETED) {
                failed.add(id);
                continue;
            }

            createAuditLog(submission, AuditAction.DELETED, request.reason());
            submissionRepository.delete(submission);
            deleted.add(id);
        }

        return new BulkDeleteResponse(deleted, failed);
    }

    private void validateCanApprove(Submission submission) {
        if (statusValidator.canApprove(submission.getStatus())) {
            return;
        }

        if (submission.getStatus() == SubmissionStatus.APPROVED) {
            throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_APPROVED);
        }

        throw new BusinessException(ExceptionType.SUBMISSION_INVALID_STATUS);
    }

    private void validateCanReject(Submission submission) {
        if (statusValidator.canReject(submission.getStatus())) {
            return;
        }

        if (submission.getStatus() == SubmissionStatus.REJECTED) {
            throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_REJECTED);
        }

        throw new BusinessException(ExceptionType.SUBMISSION_INVALID_STATUS);
    }

    private void validateRejectReason(RejectReasonDto reason) {
        if (reason == null || reason.message() == null || reason.message().isBlank()) {
            throw new BusinessException(ExceptionType.REJECT_REASON_REQUIRED);
        }
    }

    private void setRejectReason(Submission submission, RejectReasonDto reason) {
        RejectReason rejectReason = RejectReason.builder()
                .submission(submission)
                .templateCode(reason.templateCode())
                .message(reason.message())
                .rejectedBy(getCurrentUsername())
                .rejectedAt(LocalDateTime.now())
                .build();

        submission.setRejectReason(rejectReason);
    }

    private void createAuditLog(Submission submission, AuditAction action, String comment) {
        AuditLog auditLog = AuditLog.builder()
                .submission(submission)
                .action(action)
                .performedBy(getCurrentUsername())
                .comment(comment)
                .build();

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