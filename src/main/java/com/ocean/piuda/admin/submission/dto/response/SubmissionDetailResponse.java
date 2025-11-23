package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.submission.entity.Submission;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record SubmissionDetailResponse(
        Long submissionId,
        String siteName,
        ActivityType activityType,
        LocalDateTime submittedAt,
        SubmissionStatus status,
        String authorName,
        String authorEmail,
        Integer attachmentCount,
        String feedbackText,
        BigDecimal latitude,
        BigDecimal longitude,
        BasicEnvResponse basicEnv,
        ParticipantsResponse participants,
        ActivityResponse activity,
        List<AttachmentResponse> attachments,
        String rejectReason,
        List<AuditLogResponse> auditLogs,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static SubmissionDetailResponse from(Submission submission) {
        List<AttachmentResponse> attachments = null;
        if (submission.getAttachments() != null) {
            attachments = submission.getAttachments().stream()
                    .map(AttachmentResponse::from)
                    .collect(Collectors.toList());
        }

        List<AuditLogResponse> auditLogs = null;
        if (submission.getAuditLogs() != null) {
            auditLogs = submission.getAuditLogs().stream()
                    .map(AuditLogResponse::from)
                    .collect(Collectors.toList());
        }

        String rejectReason = null;
        if (submission.getRejectReason() != null) {
            rejectReason = submission.getRejectReason().getMessage();
        }

        return new SubmissionDetailResponse(
                submission.getSubmissionId(),
                submission.getSiteName(),
                submission.getActivityType(),
                submission.getSubmittedAt(),
                submission.getStatus(),
                submission.getAuthorName(),
                submission.getAuthorEmail(),
                submission.getAttachmentCount(),
                submission.getFeedbackText(),
                submission.getLatitude(),
                submission.getLongitude(),
                BasicEnvResponse.from(submission.getBasicEnv()),
                ParticipantsResponse.from(submission.getParticipants()),
                ActivityResponse.from(submission.getActivity()),
                attachments,
                rejectReason,
                auditLogs,
                submission.getCreatedAt(),
                submission.getModifiedAt()
        );
    }
}
