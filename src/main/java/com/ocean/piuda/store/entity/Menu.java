package com.ocean.piuda.store.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;


@SuperBuilder(toBuilder = true)
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(precision = 15, scale = 0)
    private BigDecimal basePrice;

    @Embedded
    private Discount discount;

    private String imageUrl;

    @Builder.Default
    private boolean isActive = true;

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private MenuCategory menuCategory;

    @JoinColumn(name = "store_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Store store;

    public BigDecimal getDiscountedPrice() {
        if (this.discount == null) return basePrice;

        return this.discount.apply(basePrice);
    }


}
