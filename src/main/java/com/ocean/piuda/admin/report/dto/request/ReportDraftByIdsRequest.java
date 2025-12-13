package com.ocean.piuda.admin.report.dto.request;

import com.ocean.piuda.admin.report.enums.ReportDraftType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ReportDraftByIdsRequest(
        @NotEmpty(message = "ids는 1개 이상이어야 합니다")
        List<Long> ids,

        @NotNull(message = "reportType은 필수입니다")
        ReportDraftType reportType,

        String prompt
) {}
