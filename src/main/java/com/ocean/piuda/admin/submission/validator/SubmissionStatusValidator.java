package com.ocean.piuda.admin.submission.validator;

import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Submission 상태 전이 정책을 한 곳에서 관리합니다.
 *
 * 현재 정책:
 * - SUBMITTED -> APPROVED
 * - SUBMITTED -> REJECTED
 *
 * 그 외 상태 전이는 허용하지 않습니다.
 * 즉, APPROVED / REJECTED / DELETED 는 최종 상태로 취급합니다.
 */
@Component
public class SubmissionStatusValidator {

    private static final Map<SubmissionStatus, Set<SubmissionStatus>> ALLOWED_TRANSITIONS = Map.of(
            SubmissionStatus.SUBMITTED, Set.of(SubmissionStatus.APPROVED, SubmissionStatus.REJECTED),
            SubmissionStatus.APPROVED, Set.of(),
            SubmissionStatus.REJECTED, Set.of(),
            SubmissionStatus.DELETED, Set.of()
    );

    /**
     * 상태 전이 가능 여부를 반환합니다.
     */
    public boolean canTransition(SubmissionStatus from, SubmissionStatus to) {
        if (from == null || to == null) {
            return false;
        }

        return ALLOWED_TRANSITIONS
                .getOrDefault(from, Set.of())
                .contains(to);
    }

    /**
     * 상태 전이 정책을 검증합니다.
     *
     * @throws IllegalArgumentException 상태값이 null 인 경우
     * @throws IllegalStateException 허용되지 않는 상태 전이인 경우
     */
    public void validateTransition(SubmissionStatus from, SubmissionStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("상태는 null일 수 없습니다");
        }

        if (!canTransition(from, to)) {
            throw new IllegalStateException(
                    String.format("상태 전이 불가: %s -> %s", from, to)
            );
        }
    }

    /**
     * 승인 가능 여부.
     */
    public boolean canApprove(SubmissionStatus status) {
        return canTransition(status, SubmissionStatus.APPROVED);
    }

    /**
     * 반려 가능 여부.
     */
    public boolean canReject(SubmissionStatus status) {
        return canTransition(status, SubmissionStatus.REJECTED);
    }
}