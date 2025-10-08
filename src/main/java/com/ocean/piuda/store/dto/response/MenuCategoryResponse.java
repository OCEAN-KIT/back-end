package com.ocean.piuda.store.dto.response;


import com.ocean.piuda.store.entity.MenuCategory;

public record MenuCategoryResponse(
        Long id,
        String title,
        String description
) {
    public static MenuCategoryResponse of(MenuCategory menuCategory) {
        return new MenuCategoryResponse(
                menuCategory.getId(),
                menuCategory.getTitle(),
                menuCategory.getDescription()
        );
    }
}
