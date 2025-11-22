package com.ocean.piuda.admin.submission.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SingleRejectRequest(
        @NotNull(message = "반려 사유는 필수입니다.")
        @Valid
        RejectReasonDto reason
) {
}
