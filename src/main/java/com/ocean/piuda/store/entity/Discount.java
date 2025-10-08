package com.ocean.piuda.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class Discount {

    @Column(precision = 15, scale = 0)
    private BigDecimal percentage; // 정률 할인만 사용

    @Column(precision = 15, scale = 0)
    private BigDecimal maxDiscountAmount;

    public static Discount of(BigDecimal percentage, BigDecimal maxDiscountAmount) {
        if (maxDiscountAmount == null) maxDiscountAmount = BigDecimal.valueOf(Integer.MAX_VALUE);

        return new Discount(percentage, maxDiscountAmount);
    }

    public BigDecimal apply(BigDecimal price) {
        BigDecimal discountValue = price.multiply(percentage)
                .divide(BigDecimal.valueOf(100), RoundingMode.DOWN)
                .min(maxDiscountAmount);

        return price.subtract(discountValue).max(BigDecimal.ZERO);
    }
}
