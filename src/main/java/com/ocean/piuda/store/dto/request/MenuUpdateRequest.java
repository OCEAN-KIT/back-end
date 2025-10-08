package com.ocean.piuda.store.dto.request;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MenuUpdateRequest(
        String name,

        String description,

        @Positive(message = "가격은 0보다 커야 합니다.")
        BigDecimal basePrice,

        String imageUrl,

        Boolean isActive,

        Long menuCategoryId
) {
}
