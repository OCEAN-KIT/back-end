package com.ocean.piuda.admin.submission.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkApproveRequest(
        @NotEmpty(message = "승인할 제출 ID 목록은 필수입니다.")
        List<Long> ids
) {
}
