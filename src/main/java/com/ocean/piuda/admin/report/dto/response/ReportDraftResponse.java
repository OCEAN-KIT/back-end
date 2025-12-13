package com.ocean.piuda.admin.report.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ocean.piuda.ai.gemini.dto.response.GeminiMeta;
import com.ocean.piuda.admin.report.enums.ReportDraftType;
import lombok.Builder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ReportDraftResponse(
        List<Long> submissionIds,
        List<Long> missingIds,

        ReportDraftType reportType,

        String internalDraft,
        String externalNewsletter,
        String externalInstagram,
        String externalPublication,

        GeminiMeta meta
) {}
