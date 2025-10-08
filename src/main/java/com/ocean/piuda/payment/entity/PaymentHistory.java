package com.ocean.piuda.payment.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.payment.enums.PaymentEvent;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PaymentHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private PaymentEvent event;

    private Long amountDelta;
}