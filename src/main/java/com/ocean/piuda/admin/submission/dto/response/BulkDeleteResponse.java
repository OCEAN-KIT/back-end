package com.ocean.piuda.admin.submission.dto.response;

import java.util.List;

public record BulkDeleteResponse(
        List<Long> deleted,
        List<Long> failed
) {}