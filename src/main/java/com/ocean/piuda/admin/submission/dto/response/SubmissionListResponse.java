package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.submission.entity.Submission;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubmissionListResponse(
        Long submissionId,
        String siteName,
        ActivityType activityType,
        LocalDateTime submittedAt,
        SubmissionStatus status,
        String authorName,
        String authorEmail,
        Integer attachmentCount
) {
    public static SubmissionListResponse from(Submission submission) {
        return new SubmissionListResponse(
                submission.getSubmissionId(),
                submission.getSiteName(),
                submission.getActivityType(),
                submission.getSubmittedAt(),
                submission.getStatus(),
                submission.getAuthorName(),
                submission.getAuthorEmail(),
                submission.getAttachmentCount()
        );
    }
}
