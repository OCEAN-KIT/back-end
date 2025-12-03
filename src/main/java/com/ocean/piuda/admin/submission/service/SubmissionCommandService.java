package com.ocean.piuda.admin.submission.service;

import com.ocean.piuda.admin.submission.dto.response.*;
import com.ocean.piuda.admin.common.enums.AuditAction;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.submission.dto.request.*;
import com.ocean.piuda.admin.submission.dto.response.SubmissionDetailResponse;
import com.ocean.piuda.admin.submission.entity.Activity;
import com.ocean.piuda.admin.submission.entity.Attachment;
import com.ocean.piuda.admin.submission.entity.AuditLog;
import com.ocean.piuda.admin.submission.entity.BasicEnv;
import com.ocean.piuda.admin.submission.entity.Participants;
import com.ocean.piuda.admin.submission.entity.RejectReason;
import com.ocean.piuda.admin.submission.entity.Submission;
import com.ocean.piuda.admin.submission.entity.embeded.NaturalReproduction;
import com.ocean.piuda.admin.submission.entity.embeded.Survival;
import com.ocean.piuda.admin.submission.repository.AuditLogRepository;
import com.ocean.piuda.admin.submission.repository.SubmissionRepository;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 제출 데이터 생성
     */
    public SubmissionDetailResponse createSubmission(CreateSubmissionRequest request) {
        // 기본값 설정
        LocalDateTime submittedAt = request.getSubmittedAt() != null
                ? request.getSubmittedAt()
                : LocalDateTime.now();

        // Submission 생성
        Submission submission = Submission.builder()
                .siteName(request.getSiteName())
                .activityType(request.getActivityType())
                .submittedAt(submittedAt)
                .status(SubmissionStatus.PENDING)
                .authorName(request.getAuthorName())
                .authorEmail(request.getAuthorEmail())
                .feedbackText(request.getFeedbackText())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .attachmentCount(0)
                .build();

        // BasicEnv 생성
        if (request.getBasicEnv() != null) {
            CreateSubmissionRequest.BasicEnvDto envDto = request.getBasicEnv();
            BasicEnv basicEnv = BasicEnv.builder()
                    .recordDate(envDto.getRecordDate())
                    .startTime(envDto.getStartTime())
                    .endTime(envDto.getEndTime())
                    .waterTempC(envDto.getWaterTempC())
                    .visibilityM(envDto.getVisibilityM())
                    .depthM(envDto.getDepthM())
                    .currentState(envDto.getCurrentState())
                    .weather(envDto.getWeather())
                    .build();
            submission.setBasicEnv(basicEnv);
        }

        // Participants 생성
        if (request.getParticipants() != null) {
            CreateSubmissionRequest.ParticipantsDto participantsDto = request.getParticipants();
            Participants participants = Participants.builder()
                    .leaderName(participantsDto.getLeaderName())
                    .participantCount(participantsDto.getParticipantCount() != null
                            ? participantsDto.getParticipantCount()
                            : 1)
                    .role(participantsDto.getRole() != null
                            ? participantsDto.getRole()
                            : com.ocean.piuda.admin.common.enums.ParticipantRole.CITIZEN_DIVER)
                    .build();
            submission.setParticipants(participants);
        }

        // Activity 생성 및 신규 필드 매핑
        CreateSubmissionRequest.ActivityDto activityDto = request.getActivity();

        // NaturalReproduction VO 빌드
        NaturalReproduction naturalReproduction = null;
        if (activityDto.getNaturalReproduction() != null) {
            naturalReproduction = NaturalReproduction.builder()
                    .radiusM(activityDto.getNaturalReproduction().getRadiusM() != null ? activityDto.getNaturalReproduction().getRadiusM() : 0f)
                    .numerator(activityDto.getNaturalReproduction().getNumerator() != null ? activityDto.getNaturalReproduction().getNumerator() : 0f)
                    .denominator(activityDto.getNaturalReproduction().getDenominator() != null ? activityDto.getNaturalReproduction().getDenominator() : 0f)
                    .build();
        }

        // Survival VO 빌드
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
                .collectionAmount(activityDto.getCollectionAmount() != null
                        ? activityDto.getCollectionAmount()
                        : 0f)
                .durationHours(activityDto.getDurationHours() != null
                        ? activityDto.getDurationHours()
                        : 0f)
                // 추가된 필드 매핑
                .healthGrade(activityDto.getHealthGrade())
                .growthCm(activityDto.getGrowthCm() != null ? activityDto.getGrowthCm() : 0f)
                .naturalReproduction(naturalReproduction)
                .survival(survival)
                .build();

        submission.setActivity(activity);

        // Attachments 생성
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (CreateSubmissionRequest.AttachmentDto attachmentDto : request.getAttachments()) {
                Attachment attachment = Attachment.builder()
                        .fileName(attachmentDto.getFileName())
                        .fileUrl(attachmentDto.getFileUrl())
                        .mimeType(attachmentDto.getMimeType())
                        .fileSize(attachmentDto.getFileSize())
                        .uploadedAt(submittedAt)
                        .build();
                submission.addAttachment(attachment);
            }
        }

        // 저장
        Submission saved = submissionRepository.save(submission);

        // AuditLog 생성 (제출됨)
        createAuditLog(saved, AuditAction.SUBMITTED, null);

        return SubmissionDetailResponse.from(saved);
    }

    /**
     * 단건 승인
     */
    public SubmissionDetailResponse approveSubmission(Long submissionId) {
        Submission submission = submissionQueryService.getSubmissionById(submissionId);

        if (submission.getStatus() == SubmissionStatus.APPROVED) {
            throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_APPROVED);
        }
        if (submission.getStatus() == SubmissionStatus.DELETED) {
            throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_DELETED);
        }

        submission.updateStatus(SubmissionStatus.APPROVED);
        createAuditLog(submission, AuditAction.APPROVED, null);

        return SubmissionDetailResponse.from(submission);
    }

    /**
     * 단건 반려
     */
    public SubmissionDetailResponse rejectSubmission(Long submissionId, SingleRejectRequest request) {
        Submission submission = submissionQueryService.getSubmissionById(submissionId);

        if (submission.getStatus() == SubmissionStatus.REJECTED) {
            throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_REJECTED);
        }
        if (submission.getStatus() == SubmissionStatus.DELETED) {
            throw new BusinessException(ExceptionType.SUBMISSION_ALREADY_DELETED);
        }

        if (request.reason() == null || request.reason().message() == null || request.reason().message().isBlank()) {
            throw new BusinessException(ExceptionType.REJECT_REASON_REQUIRED);
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