package com.ocean.piuda.admin.report.dto.request;

import com.ocean.piuda.admin.report.enums.ReportDraftType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ReportDraftByPeriodRequest(
        @NotNull(message = "dateFrom는 필수입니다")
        LocalDate dateFrom,

        @NotNull(message = "dateTo는 필수입니다")
        LocalDate dateTo,

        @NotNull(message = "reportType은 필수입니다")
        ReportDraftType reportType,

        String prompt
) {}
