package com.ocean.piuda.admin.submission.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkRejectRequest(
        @NotEmpty(message = "반려할 제출 ID 목록은 필수입니다.")
        List<Long> ids,
        
        @NotNull(message = "반려 사유는 필수입니다.")
        @Valid
        RejectReasonDto reason
) {
}
