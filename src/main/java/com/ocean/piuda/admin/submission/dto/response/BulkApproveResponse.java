package com.ocean.piuda.admin.submission.dto.response;

import java.util.List;

public record BulkApproveResponse(
        List<Long> approved,
        List<Long> skipped
) {}