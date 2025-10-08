package com.ocean.piuda.payment.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.payment.enums.PaymentMethod;
import com.ocean.piuda.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Order order;

    @Column(nullable = false, unique = true, length = 80)
    private String merchantUid;   // PG와 매칭되는 고유 식별자

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Long totalCouponUsedPrice;
    private Long totalPrice;

    @Column(length = 80, unique = true)
    private String idempotencyKey;

    private Integer version;
}