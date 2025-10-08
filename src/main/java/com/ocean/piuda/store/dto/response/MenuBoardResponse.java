package com.ocean.piuda.store.dto.response;


import com.ocean.piuda.store.entity.Menu;
import com.ocean.piuda.store.entity.MenuCategory;

import java.math.BigDecimal;
import java.util.List;

public record MenuBoardResponse(
        List<MenuCategoryResponse> categories
) {

    public record MenuCategoryResponse(
            String title,
            String description,
            List<MenuResponse> menus
    ) {
        public static MenuCategoryResponse of(MenuCategory category, List<Menu> menus) {
            return new MenuCategoryResponse(
                    category.getTitle(),
                    category.getDescription(),
                    menus.stream().map(MenuResponse::of).toList()
            );
        }

        public static MenuCategoryResponse ofUncategorized(List<Menu> menus) {
            return new MenuCategoryResponse(
                    "미분류",
                    "",
                    menus.stream().map(MenuResponse::of).toList()
            );
        }
    }

    public record MenuResponse(
            Long id,
            String name,
            String description,
            BigDecimal basePrice,
            BigDecimal discountedPrice,
            String imageUrl,
            boolean isActive
    ) {
        public static MenuResponse of(Menu menu) {
            return new MenuResponse(
                    menu.getId(),
                    menu.getName(),
                    menu.getDescription(),
                    menu.getBasePrice(),
                    menu.getDiscountedPrice(),
                    menu.getImageUrl(),
                    menu.isActive()
            );
        }
    }
}
