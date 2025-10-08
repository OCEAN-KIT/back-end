package com.ocean.piuda.payment.dto.response;

public record OrderItemResponse(
    Long id,
    Long menuId,
    String menuNameSnapshot,
    Long menuBasePriceSnapshot,
    Long menuDiscountedPriceSnapshot,
    Integer quantity
) {}
