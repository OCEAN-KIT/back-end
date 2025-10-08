package com.ocean.piuda.payment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotNull Long storeId,
        @Valid @NotNull List<Item> items,

        List<Long> participantUserIds // 공동 결제가 아닐 경우 nullable

) {
    public record Item(
            @NotNull Long menuId,
            @NotNull @Min(1) Integer quantity
    ) {}
}