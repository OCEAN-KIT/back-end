package com.ocean.piuda.store.dto.response;

import com.ocean.piuda.store.dto.StoreWithStatsDto;
import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.store.entity.embeded.Address;
import com.ocean.piuda.store.enums.StoreCategory;

public record StoreListResponse(
        Long id,
        String name,
        StoreCategory category,
        String description,
        Address address,
        String bannerImageUrl,
        Boolean isOpen,
        Double averageRating,
        Long reviewCount,
        Long orderCount
) {
    public static StoreListResponse of(Store store) {
        return new StoreListResponse(
                store.getId(),
                store.getName(),
                store.getCategory(),
                store.getDescription(),
                store.getAddress(),
                store.getThumbnailImageUrl(),
                store.getIsOpen(),
                0.0, // 평점 정보 없음
                0L, // 리뷰 개수 정보 없음
                0L // 주문 개수 정보 없음
        );
    }


    public static StoreResponse of(StoreWithStatsDto storeWithStats) {
        return StoreResponse.of(
                storeWithStats.store(),
                storeWithStats.averageRating(),
                storeWithStats.reviewCount()
        );
    }

    public static StoreListResponse of(Store store, Double averageRating, Long reviewCount, Long orderCount) {
        return new StoreListResponse(
                store.getId(),
                store.getName(),
                store.getCategory(),
                store.getDescription(),
                store.getAddress(),
                store.getThumbnailImageUrl(),
                store.getIsOpen(),
                averageRating,
                reviewCount,
                orderCount
        );
    }
}
