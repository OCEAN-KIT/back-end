package com.ocean.piuda.site.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSiteOptionRequest(
        @Size(min = 1, message = "현장 명칭은 최소 1글자 이상이어야 합니다.")
        String name,

        Boolean isActive
) {}