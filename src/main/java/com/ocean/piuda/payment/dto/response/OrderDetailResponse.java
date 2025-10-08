package com.ocean.piuda.payment.dto.response;

import com.ocean.piuda.payment.enums.OrderStatus;

import java.util.List;

public record OrderDetailResponse(
    Long id,
    Long storeId,
    Long userId,
    OrderStatus status,
    Long totalOriginalPrice,
    Long totalDiscountedPrice,
    Long totalPrice,
    List<OrderItemResponse> items,
    String merchantUid
) {}