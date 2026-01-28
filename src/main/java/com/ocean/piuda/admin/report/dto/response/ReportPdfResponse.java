package com.ocean.piuda.admin.report.dto.response;

public record ReportPdfResponse(
        String fileName,
        byte[] bytes
) {}
