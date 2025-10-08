package com.ocean.piuda.store.dto.request;

import com.ocean.piuda.store.entity.embeded.Address;
import com.ocean.piuda.store.entity.embeded.OpeningHours;
import com.ocean.piuda.store.enums.StoreCategory;

public record StoreUpdateRequest(
        String name,
        StoreCategory category,
        String description,
        Address address,
        String phoneNumber,
        OpeningHours openingHours,
        String bannerImageUrl,
        String thumbnailImageUrl,
        String bizRegNo
) {
}
