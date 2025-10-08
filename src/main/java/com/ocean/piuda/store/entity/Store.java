package com.ocean.piuda.store.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.store.entity.embeded.Address;
import com.ocean.piuda.store.entity.embeded.OpeningHours;
import com.ocean.piuda.store.enums.StoreCategory;
import com.ocean.piuda.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Entity
@Getter
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User owner;

    private String name;

    @Enumerated(EnumType.STRING)
    private StoreCategory category;


    @Embedded
    private Address address;

    @Embedded
    private OpeningHours openingHours;



    private String description;
    private String phoneNumber;


    private String bannerImageUrl;
    private String thumbnailImageUrl;


    private String bizRegNo; //사업자등록번호

    // 가게 오픈시 사장님이 활성화
    @Builder.Default
    private Boolean isOpen = false;


    public void changeOpenStatus(Boolean isOpen) {
        this.isOpen = isOpen;
    }




}
