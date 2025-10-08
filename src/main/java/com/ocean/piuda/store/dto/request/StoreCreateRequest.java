package com.ocean.piuda.store.dto.request;

import com.ocean.piuda.store.entity.embeded.Address;
import com.ocean.piuda.store.entity.embeded.OpeningHours;
import com.ocean.piuda.store.enums.StoreCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoreCreateRequest(
        @NotBlank(message = "가게 이름은 필수입니다.")
        String name,

        @NotNull(message = "가게 카테고리는 필수입니다.")
        StoreCategory category,

        String description,

        @NotBlank(message = "주소는 필수입니다.")
        Address address,

        String phoneNumber,

        OpeningHours openingHours,

        String bannerImageUrl,

        @NotBlank(message = "사업자등록번호는 필수입니다.")
        String bizRegNo
) {
}
