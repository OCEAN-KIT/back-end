package com.ocean.piuda.payment.dto.response;

import com.ocean.piuda.payment.entity.Payment;
import com.ocean.piuda.payment.enums.PaymentStatus;


public record PaymentResponse(
        Long id,
        String merchantUid,
        PaymentStatus status,
        Long totalPrice
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchantUid(),
                payment.getStatus(),
                payment.getTotalPrice()
        );
    }
}
