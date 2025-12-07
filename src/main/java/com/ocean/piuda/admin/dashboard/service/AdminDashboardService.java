package com.ocean.piuda.admin.dashboard.service;

import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.dashboard.dto.AdminDashboardSummaryResponse;
import com.ocean.piuda.admin.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final SubmissionRepository submissionRepository;

    /**
     * 대시보드 통계 조회
     */
    public AdminDashboardSummaryResponse getDashboardSummary() {
        long total = submissionRepository.count();
        long pending = submissionRepository.countByStatus(SubmissionStatus.PENDING);
        long approved = submissionRepository.countByStatus(SubmissionStatus.APPROVED);
        long rejected = submissionRepository.countByStatus(SubmissionStatus.REJECTED);
        long deleted = submissionRepository.countByStatus(SubmissionStatus.DELETED);

        return AdminDashboardSummaryResponse.of(total, pending, approved, rejected, deleted);
    }
}
