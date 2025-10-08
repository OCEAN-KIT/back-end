package com.ocean.piuda.payment.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.store.entity.Menu;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private Menu menu;

    private String menuNameSnapshot;
    private Long menuBasePriceSnapshot;
    private Long menuDiscountedPriceSnapshot;

    private Integer quantity;
}