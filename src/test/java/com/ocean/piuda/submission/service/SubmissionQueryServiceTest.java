package com.ocean.piuda.submission.service;

import com.ocean.piuda.submission.enums.ActivityType;
import com.ocean.piuda.submission.enums.SubmissionStatus;
import com.ocean.piuda.submission.dto.response.SubmissionListResponse;
import com.ocean.piuda.submission.entity.Submission;
import com.ocean.piuda.submission.repository.AuditLogRepository;
import com.ocean.piuda.submission.repository.SubmissionRepository;
import com.ocean.piuda.global.api.dto.PageResponse;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import com.ocean.piuda.submission.service.SubmissionQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SubmissionQueryServiceTest {

    private SubmissionRepository submissionRepository;
    private AuditLogRepository auditLogRepository;
    private TokenUserService tokenUserService;
    private SubmissionQueryService submissionQueryService;

    @BeforeEach
    void setUp() {
        submissionRepository = mock(SubmissionRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        tokenUserService = mock(TokenUserService.class);

        submissionQueryService = new SubmissionQueryService(
                submissionRepository,
                auditLogRepository,
                tokenUserService
        );
    }

    @Test
    void getMySubmissions_queriesOnlyCurrentUserSubmissions() {
        Long currentUserId = 7L;
        Pageable pageable = PageRequest.of(0, 20);

        Submission mySubmission = Submission.builder()
                .submissionId(10L)
                .siteName("십자형 어초")
                .activityType(ActivityType.TRANSPLANT)
                .submittedAt(LocalDateTime.of(2026, 6, 21, 10, 0))
                .status(SubmissionStatus.SUBMITTED)
                .authorName("일반 사용자")
                .authorEmail("user@test.com")
                .attachmentCount(1)
                .build();

        when(tokenUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(submissionRepository.findAllByUserIdWithBasicEnv(currentUserId, pageable))
                .thenReturn(new PageImpl<>(List.of(mySubmission), pageable, 1));

        PageResponse<SubmissionListResponse> response =
                submissionQueryService.getMySubmissions(pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).submissionId()).isEqualTo(10L);
        assertThat(response.content().get(0).siteName()).isEqualTo("십자형 어초");

        verify(tokenUserService).getCurrentUserId();
        verify(submissionRepository).findAllByUserIdWithBasicEnv(currentUserId, pageable);
        verify(submissionRepository, never()).findAll();
    }

    @Test
    void getMySubmissionDetail_queriesBySubmissionIdAndCurrentUserId() {
        Long currentUserId = 7L;
        Long submissionId = 10L;

        Submission mySubmission = Submission.builder()
                .submissionId(submissionId)
                .siteName("십자형 어초")
                .activityType(ActivityType.TRANSPLANT)
                .submittedAt(LocalDateTime.of(2026, 6, 21, 10, 0))
                .status(SubmissionStatus.SUBMITTED)
                .authorName("일반 사용자")
                .authorEmail("user@test.com")
                .attachmentCount(0)
                .build();

        when(tokenUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(submissionRepository.findByIdAndUserIdWithDetails(submissionId, currentUserId))
                .thenReturn(Optional.of(mySubmission));
        when(auditLogRepository.findBySubmissionSubmissionIdOrderByCreatedAtDesc(submissionId))
                .thenReturn(List.of());

        var response = submissionQueryService.getMySubmissionDetail(submissionId);

        assertThat(response.submissionId()).isEqualTo(submissionId);
        assertThat(response.siteName()).isEqualTo("십자형 어초");

        verify(tokenUserService).getCurrentUserId();
        verify(submissionRepository).findByIdAndUserIdWithDetails(submissionId, currentUserId);
        verify(submissionRepository, never()).findByIdWithDetails(anyLong());
    }

    @Test
    void getMySubmissionDetail_throwsWhenSubmissionDoesNotBelongToCurrentUser() {
        Long currentUserId = 7L;
        Long otherUsersSubmissionId = 99L;

        when(tokenUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(submissionRepository.findByIdAndUserIdWithDetails(otherUsersSubmissionId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionQueryService.getMySubmissionDetail(otherUsersSubmissionId))
                .isInstanceOf(BusinessException.class)
                .extracting("exceptionType")
                .isEqualTo(ExceptionType.SUBMISSION_NOT_FOUND);

        verify(tokenUserService).getCurrentUserId();
        verify(submissionRepository).findByIdAndUserIdWithDetails(otherUsersSubmissionId, currentUserId);
        verify(submissionRepository, never()).findByIdWithDetails(anyLong());
        verifyNoInteractions(auditLogRepository);
    }
}