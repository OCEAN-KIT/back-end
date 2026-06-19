package com.ocean.piuda.admin.submission.validator;

import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Submission 상태 전이 검증
 */
@Component
public class SubmissionStatusValidator {

    private static final Map<SubmissionStatus, Set<SubmissionStatus>> ALLOWED_TRANSITIONS = Map.of(
        SubmissionStatus.SUBMITTED, Set.of(SubmissionStatus.APPROVED, SubmissionStatus.REJECTED),
        SubmissionStatus.APPROVED, Set.of(),  // 최종 상태
        SubmissionStatus.REJECTED, Set.of()   // 최종 상태
    );

    /**
     * 상태 전이 검증
     * @param from 현재 상태
     * @param to 전이할 상태
     * @throws IllegalStateException 전이가 불가능한 경우
     */
    public void validateTransition(SubmissionStatus from, SubmissionStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("상태는 null일 수 없습니다");
        }

        Set<SubmissionStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null) {
            throw new IllegalStateException("알 수 없는 상태: " + from);
        }

        if (!allowed.contains(to)) {
            throw new IllegalStateException(
                String.format("상태 전이 불가: %s → %s", from, to)
            );
        }
    }


    /**
     * 승인 가능 여부 확인
     */
    public boolean canApprove(SubmissionStatus status) {
        return status == SubmissionStatus.SUBMITTED;
    }

    /**
     * 반려 가능 여부 확인
     */
    public boolean canReject(SubmissionStatus status) {
        return status == SubmissionStatus.SUBMITTED;
    }


}
