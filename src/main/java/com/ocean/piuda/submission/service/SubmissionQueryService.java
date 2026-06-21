package com.ocean.piuda.submission.service;

import com.ocean.piuda.submission.enums.ActivityType;
import com.ocean.piuda.submission.enums.SubmissionStatus;
import com.ocean.piuda.submission.dto.response.AuditLogResponse;
import com.ocean.piuda.submission.dto.response.SubmissionDetailResponse;
import com.ocean.piuda.submission.dto.response.SubmissionListResponse;
import com.ocean.piuda.submission.entity.AuditLog;
import com.ocean.piuda.submission.entity.Submission;
import com.ocean.piuda.submission.repository.AuditLogRepository;
import com.ocean.piuda.submission.repository.SubmissionRepository;
import com.ocean.piuda.global.api.dto.PageResponse;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class SubmissionQueryService {

    private final SubmissionRepository submissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final TokenUserService tokenUserService;

    /**
     * Admin 제출 목록 조회.
     *
     * 전체 제출물을 대상으로 필터링/검색/정렬합니다.
     * 일반 record 사용자의 제출 목록 조회에는 사용하지 않습니다.
     */
    public PageResponse<SubmissionListResponse> getSubmissions(
            String keyword,
            SubmissionStatus status,
            ActivityType activityType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Page<Submission> page = submissionRepository.findWithFilters(
                keyword, status, activityType, startDate, endDate, pageable
        );

        Page<SubmissionListResponse> mapped = page.map(SubmissionListResponse::from);
        return PageResponse.of(mapped);
    }

    /**
     * Record 내 제출 목록 조회.
     *
     * 현재 로그인한 사용자의 제출물만 조회합니다.
     */
    public PageResponse<SubmissionListResponse> getMySubmissions(Pageable pageable) {
        Long currentUserId = tokenUserService.getCurrentUserId();

        Page<Submission> page = submissionRepository.findAllByUserIdWithBasicEnv(
                currentUserId,
                pageable
        );

        Page<SubmissionListResponse> mapped = page.map(SubmissionListResponse::from);
        return PageResponse.of(mapped);
    }

    /**
     * Admin 제출 상세 조회.
     *
     * 제출 ID만으로 전체 제출물에서 조회합니다.
     */
    public SubmissionDetailResponse getSubmissionDetail(Long submissionId) {
        Submission submission = submissionRepository.findByIdWithDetails(submissionId)
                .orElseThrow(() -> new BusinessException(ExceptionType.SUBMISSION_NOT_FOUND));

        attachAuditLogs(submission);

        return SubmissionDetailResponse.from(submission);
    }

    /**
     * Record 내 제출 상세 조회.
     *
     * 현재 로그인한 사용자의 제출물일 때만 조회합니다.
     * 다른 사용자의 제출 ID를 직접 입력해도 조회되지 않습니다.
     */
    public SubmissionDetailResponse getMySubmissionDetail(Long submissionId) {
        Long currentUserId = tokenUserService.getCurrentUserId();

        Submission submission = submissionRepository.findByIdAndUserIdWithDetails(
                        submissionId,
                        currentUserId
                )
                .orElseThrow(() -> new BusinessException(ExceptionType.SUBMISSION_NOT_FOUND));

        attachAuditLogs(submission);

        return SubmissionDetailResponse.from(submission);
    }

    /**
     * 검수 로그 조회.
     *
     * Admin 화면용입니다.
     */
    public List<AuditLogResponse> getSubmissionLogs(Long submissionId) {
        return auditLogRepository.findBySubmissionSubmissionIdOrderByCreatedAtDesc(submissionId)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    /**
     * Submission 엔티티 조회.
     *
     * CommandService 내부 승인/반려/삭제용입니다.
     */
    public Submission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(ExceptionType.SUBMISSION_NOT_FOUND));
    }

    private void attachAuditLogs(Submission submission) {
        List<AuditLog> auditLogs = auditLogRepository
                .findBySubmissionSubmissionIdOrderByCreatedAtDesc(submission.getSubmissionId());

        submission.getAuditLogs().clear();
        submission.getAuditLogs().addAll(auditLogs);
    }
}