package com.ocean.piuda.admin.submission.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkDeleteRequest(
        @NotEmpty(message = "삭제할 제출 ID 목록은 필수입니다.")
        List<Long> ids,
        
        String reason
) {
}
