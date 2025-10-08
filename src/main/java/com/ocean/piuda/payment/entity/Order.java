package com.ocean.piuda.payment.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.payment.enums.OrderStatus;
import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder(toBuilder = true)
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Store store;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Long totalOriginalPrice;   // 메뉴 총합(정가 기준)
    private Long totalDiscountedPrice; // 총 할인 금액
    private Long totalPrice;           // 최종 결제 금액


    // 공동 결제/주문 참여자들 (주문 당사자 제외)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderParticipant> participants = new ArrayList<>();



    public void addParticipant(OrderParticipant p) {
        participants.add(p);
        p.updateOrder(this);
    }

}
