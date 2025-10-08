package com.ocean.piuda.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MenuCreateRequest(
        @NotBlank(message = "메뉴 이름은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "기본 가격은 필수입니다.")
        @Positive(message = "가격은 0보다 커야 합니다.")
        BigDecimal basePrice,

        String imageUrl,

        Long menuCategoryId
) {
}
