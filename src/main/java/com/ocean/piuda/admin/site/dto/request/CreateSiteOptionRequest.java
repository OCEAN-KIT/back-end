package com.ocean.piuda.admin.site.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSiteOptionRequest(
        @NotBlank(message = "현장 명칭은 필수입니다.")
        String name
) {}