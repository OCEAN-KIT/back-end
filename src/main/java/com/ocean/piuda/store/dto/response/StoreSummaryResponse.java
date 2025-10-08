package com.ocean.piuda.store.dto.response;


import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.store.enums.StoreCategory;

public record StoreSummaryResponse(
        Long id,
        String name,
        StoreCategory category,
        String bannerImageUrl
) {

    public static StoreSummaryResponse of(Store store) {
        return new StoreSummaryResponse(
                store.getId(),
                store.getName(),
                store.getCategory(),
                store.getThumbnailImageUrl()
        );
    }
}
