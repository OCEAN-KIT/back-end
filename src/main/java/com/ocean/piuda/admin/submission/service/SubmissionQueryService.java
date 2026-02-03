package com.ocean.piuda.admin.submission.service;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.submission.dto.response.AuditLogResponse;
import com.ocean.piuda.admin.submission.dto.response.SubmissionDetailResponse;
import com.ocean.piuda.admin.submission.dto.response.SubmissionListResponse;
import com.ocean.piuda.admin.submission.entity.Submission;
import com.ocean.piuda.admin.submission.repository.AuditLogRepository;
import com.ocean.piuda.admin.submission.repository.SubmissionRepository;
import com.ocean.piuda.global.api.dto.PageResponse;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
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

    /**
     * 제출 목록 조회 (필터링, 검색, 정렬 포함)
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
     * 제출 상세 조회
     */
    public SubmissionDetailResponse getSubmissionDetail(Long submissionId) {
        // attachments는 fetch join으로 조회 (auditLogs는 제외하여 MultipleBagFetchException 방지)
        Submission submission = submissionRepository.findByIdWithDetails(submissionId)
                .orElseThrow(() -> new BusinessException(ExceptionType.SUBMISSION_NOT_FOUND));
        
        // auditLogs는 별도 Repository로 조회하여 초기화
        List<com.ocean.piuda.admin.submission.entity.AuditLog> auditLogs = 
                auditLogRepository.findBySubmissionSubmissionIdOrderByCreatedAtDesc(submissionId);
        
        // submission의 auditLogs 컬렉션 초기화
        submission.getAuditLogs().clear();
        submission.getAuditLogs().addAll(auditLogs);

        return SubmissionDetailResponse.from(submission);
    }



    /**
     * 검수 로그 조회
     */
    public List<AuditLogResponse> getSubmissionLogs(Long submissionId) {
        return auditLogRepository.findBySubmissionSubmissionIdOrderByCreatedAtDesc(submissionId)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    /**
     * Submission 엔티티 조회 (내부용)
     */
    public Submission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(ExceptionType.SUBMISSION_NOT_FOUND));
    }
}
