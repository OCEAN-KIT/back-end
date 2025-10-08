package com.ocean.piuda.payment.dto.response;

import com.ocean.piuda.payment.enums.OrderStatus;

public record OrderResponse(
    Long id,
    Long storeId,
    Long userId,
    OrderStatus status,
    Long totalOriginalPrice,
    Long totalDiscountedPrice,
    Long totalPrice,
    String MerchantUid
) {}
