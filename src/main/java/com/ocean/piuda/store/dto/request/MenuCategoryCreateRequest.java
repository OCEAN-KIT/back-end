package com.ocean.piuda.store.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MenuCategoryCreateRequest(
        @NotBlank(message = "카테고리 제목은 필수입니다.")
        String title,

        String description
) {
}
