package com.ocean.piuda.submission.service;

import com.ocean.piuda.submission.enums.AuditAction;
import com.ocean.piuda.submission.enums.SubmissionStatus;
import com.ocean.piuda.site.repository.SiteNameOptionRepository;
import com.ocean.piuda.submission.dto.request.BulkApproveRequest;
import com.ocean.piuda.submission.dto.request.BulkRejectRequest;
import com.ocean.piuda.submission.dto.request.RejectReasonDto;
import com.ocean.piuda.submission.dto.response.BulkApproveResponse;
import com.ocean.piuda.submission.dto.response.BulkRejectResponse;
import com.ocean.piuda.submission.entity.AuditLog;
import com.ocean.piuda.submission.entity.Submission;
import com.ocean.piuda.submission.repository.AuditLogRepository;
import com.ocean.piuda.submission.repository.SubmissionRepository;
import com.ocean.piuda.submission.service.SubmissionCommandService;
import com.ocean.piuda.submission.service.SubmissionQueryService;
import com.ocean.piuda.submission.validator.ActivityValidator;
import com.ocean.piuda.submission.validator.SubmissionStatusValidator;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SubmissionCommandServiceTest {

    private SubmissionRepository submissionRepository;
    private AuditLogRepository auditLogRepository;
    private SubmissionCommandService submissionCommandService;

    @BeforeEach
    void setUp() {
        submissionRepository = mock(SubmissionRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);

        SubmissionQueryService submissionQueryService = mock(SubmissionQueryService.class);
        ActivityValidator activityValidator = mock(ActivityValidator.class);
        TokenUserService tokenUserService = mock(TokenUserService.class);
        SiteNameOptionRepository siteNameOptionRepository = mock(SiteNameOptionRepository.class);

        submissionCommandService = new SubmissionCommandService(
                submissionRepository,
                auditLogRepository,
                submissionQueryService,
                activityValidator,
                new SubmissionStatusValidator(),
                tokenUserService,
                siteNameOptionRepository
        );
    }

    @Test
    void bulkReject_doesNotRejectApprovedSubmission() {
        Submission approvedSubmission = Submission.builder()
                .submissionId(1L)
                .status(SubmissionStatus.APPROVED)
                .build();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(approvedSubmission));

        BulkRejectResponse response = submissionCommandService.bulkReject(
                new BulkRejectRequest(
                        List.of(1L),
                        new RejectReasonDto(null, "승인된 데이터는 다시 반려할 수 없습니다.")
                )
        );

        assertThat(response.rejected()).isEmpty();
        assertThat(response.conflicts()).containsExactly(1L);
        assertThat(approvedSubmission.getStatus()).isEqualTo(SubmissionStatus.APPROVED);

        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void bulkReject_rejectsSubmittedSubmission() {
        Submission submittedSubmission = Submission.builder()
                .submissionId(1L)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submittedSubmission));

        BulkRejectResponse response = submissionCommandService.bulkReject(
                new BulkRejectRequest(
                        List.of(1L),
                        new RejectReasonDto("PHOTO_INSUFFICIENT", "사진이 부족합니다.")
                )
        );

        assertThat(response.rejected()).containsExactly(1L);
        assertThat(response.conflicts()).isEmpty();
        assertThat(submittedSubmission.getStatus()).isEqualTo(SubmissionStatus.REJECTED);
        assertThat(submittedSubmission.getRejectReason()).isNotNull();
        assertThat(submittedSubmission.getRejectReason().getMessage()).isEqualTo("사진이 부족합니다.");

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog auditLog = auditLogCaptor.getValue();
        assertThat(auditLog.getAction()).isEqualTo(AuditAction.REJECTED);
        assertThat(auditLog.getComment()).isEqualTo("사진이 부족합니다.");
    }

    @Test
    void bulkApprove_doesNotApproveRejectedSubmission() {
        Submission rejectedSubmission = Submission.builder()
                .submissionId(1L)
                .status(SubmissionStatus.REJECTED)
                .build();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(rejectedSubmission));

        BulkApproveResponse response = submissionCommandService.bulkApprove(
                new BulkApproveRequest(List.of(1L))
        );

        assertThat(response.approved()).isEmpty();
        assertThat(response.skipped()).containsExactly(1L);
        assertThat(rejectedSubmission.getStatus()).isEqualTo(SubmissionStatus.REJECTED);

        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void bulkApprove_approvesSubmittedSubmission() {
        Submission submittedSubmission = Submission.builder()
                .submissionId(1L)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submittedSubmission));

        BulkApproveResponse response = submissionCommandService.bulkApprove(
                new BulkApproveRequest(List.of(1L))
        );

        assertThat(response.approved()).containsExactly(1L);
        assertThat(response.skipped()).isEmpty();
        assertThat(submittedSubmission.getStatus()).isEqualTo(SubmissionStatus.APPROVED);

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog auditLog = auditLogCaptor.getValue();
        assertThat(auditLog.getAction()).isEqualTo(AuditAction.APPROVED);
    }
}