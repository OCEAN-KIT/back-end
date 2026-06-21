package com.ocean.piuda.submission.dto.response;

import java.util.List;

public record BulkRejectResponse(
        List<Long> rejected,
        List<Long> conflicts
) {}