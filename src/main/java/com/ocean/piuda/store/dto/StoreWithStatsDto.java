package com.ocean.piuda.store.dto;


import com.ocean.piuda.store.entity.Store;

public record StoreWithStatsDto(
        Store store,
        Double averageRating,
        Long reviewCount,
        Long orderCount,
        Boolean isSubscribed
) {

    public StoreWithStatsDto {
        if (averageRating == null) {
            averageRating = 0.0;
        } else averageRating = averageRating = Math.round(averageRating * 10.0) / 10.0;
        if (reviewCount == null) {
            reviewCount = 0L;
        }
        if (orderCount == null) {
            orderCount = 0L;
        }
        if (isSubscribed == null) {
            isSubscribed = false;
        }
    }
}
