package com.ocean.piuda.admin.dashboard.service;

import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.dashboard.dto.DashboardSummaryResponse;
import com.ocean.piuda.admin.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SubmissionRepository submissionRepository;

    /**
     * 대시보드 통계 조회
     */
    public DashboardSummaryResponse getDashboardSummary() {
        long total = submissionRepository.count();
        long pending = submissionRepository.countByStatus(SubmissionStatus.PENDING);
        long approved = submissionRepository.countByStatus(SubmissionStatus.APPROVED);
        long rejected = submissionRepository.countByStatus(SubmissionStatus.REJECTED);
        long deleted = submissionRepository.countByStatus(SubmissionStatus.DELETED);

        return DashboardSummaryResponse.of(total, pending, approved, rejected, deleted);
    }
}
