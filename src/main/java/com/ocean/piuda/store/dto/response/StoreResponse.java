package com.ocean.piuda.store.dto.response;

import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.store.entity.embeded.Address;
import com.ocean.piuda.store.entity.embeded.OpeningHours;
import com.ocean.piuda.store.enums.StoreCategory;


public record StoreResponse(
        Long id,
        String name,
        StoreCategory category,
        String description,
        Address address,
        String phoneNumber,
        OpeningHours openingHours,
        String bannerImageUrl,
        Boolean isOpen,
        Long ownerId,
        Double averageRating,
        Long totalReviewCount
) {
    public static StoreResponse of(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getCategory(),
                store.getDescription(),
                store.getAddress(),
                store.getPhoneNumber(),
                store.getOpeningHours(),
                store.getBannerImageUrl(),
                store.getIsOpen(),
                store.getOwner().getId(),
                0.0, // 평점 정보 없음
                0L  // 리뷰 수 정보 없음
        );
    }



    public static StoreResponse of(Store store, Double averageRating, Long totalReviewCount) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getCategory(),
                store.getDescription(),
                store.getAddress(),
                store.getPhoneNumber(),
                store.getOpeningHours(),
                store.getBannerImageUrl(),
                store.getIsOpen(),
                store.getOwner().getId(),
                averageRating,
                totalReviewCount
        );
    }
}
