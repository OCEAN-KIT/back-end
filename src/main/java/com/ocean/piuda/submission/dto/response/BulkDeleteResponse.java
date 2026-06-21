package com.ocean.piuda.submission.dto.response;

import java.util.List;

public record BulkDeleteResponse(
        List<Long> deleted,
        List<Long> failed
) {}