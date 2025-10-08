package com.ocean.piuda.store.dto.response;


import com.ocean.piuda.store.entity.Menu;

import java.math.BigDecimal;

public record MenuResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        BigDecimal discountedPrice,
        String imageUrl,
        boolean isActive,
        Long menuCategoryId,
        String menuCategoryTitle
) {
    public static MenuResponse of(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getBasePrice(),
                menu.getDiscountedPrice(),
                menu.getImageUrl(),
                menu.isActive(),
                menu.getMenuCategory() != null ? menu.getMenuCategory().getId() : null,
                menu.getMenuCategory() != null ? menu.getMenuCategory().getTitle() : null
        );
    }
}
