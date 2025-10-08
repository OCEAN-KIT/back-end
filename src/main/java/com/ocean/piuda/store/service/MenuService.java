package com.ocean.piuda.store.service;

import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import com.ocean.piuda.store.dto.request.MenuCategoryCreateRequest;
import com.ocean.piuda.store.dto.request.MenuCategoryUpdateRequest;
import com.ocean.piuda.store.dto.request.MenuCreateRequest;
import com.ocean.piuda.store.dto.request.MenuUpdateRequest;
import com.ocean.piuda.store.dto.response.MenuCategoryResponse;
import com.ocean.piuda.store.dto.response.MenuResponse;
import com.ocean.piuda.store.entity.Menu;
import com.ocean.piuda.store.entity.MenuCategory;
import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.store.repository.MenuCategoryRepository;
import com.ocean.piuda.store.repository.MenuRepository;
import com.ocean.piuda.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreRepository storeRepository;
    private final TokenUserService tokenUserService;

    @Transactional
    public MenuResponse createMenu(Long storeId, MenuCreateRequest request) {
        Store store = getStoreById(storeId);

        MenuCategory menuCategory = null;
        if (request.menuCategoryId() != null) {
            menuCategory = getMenuCategoryById(request.menuCategoryId());
        }

        Menu menu = Menu.builder()
                .name(request.name())
                .description(request.description())
                .basePrice(request.basePrice())
                .imageUrl(request.imageUrl())
                .store(store)
                .menuCategory(menuCategory)
                .build();

        Menu savedMenu = menuRepository.save(menu);
        return MenuResponse.of(savedMenu);
    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, MenuUpdateRequest request) {
        Menu menu = getMenuById(menuId);

        MenuCategory menuCategory = null;
        if (request.menuCategoryId() != null) {
            menuCategory = getMenuCategoryById(request.menuCategoryId());
        }

        Menu updatedMenu = menu.toBuilder()
                .name(request.name() != null ? request.name() : menu.getName())
                .description(request.description() != null ? request.description() : menu.getDescription())
                .basePrice(request.basePrice() != null ? request.basePrice() : menu.getBasePrice())
                .imageUrl(request.imageUrl() != null ? request.imageUrl() : menu.getImageUrl())
                .isActive(request.isActive() != null ? request.isActive() : menu.isActive())
                .menuCategory(request.menuCategoryId() != null ? menuCategory : menu.getMenuCategory())
                .build();

        Menu savedMenu = menuRepository.save(updatedMenu);
        return MenuResponse.of(savedMenu);
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = getMenuById(menuId);

        menuRepository.delete(menu);
    }

    @Transactional(readOnly = true)
    public MenuResponse getMenu(Long menuId) {
        Menu menu = getMenuById(menuId);
        return MenuResponse.of(menu);
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenusByStore(Long storeId) {
        List<Menu> menus = menuRepository.findAllByStoreId(storeId);
        return menus.stream()
                .map(MenuResponse::of)
                .toList();
    }

    @Transactional
    public MenuCategoryResponse createMenuCategory(Long storeId, MenuCategoryCreateRequest request) {
        Store store = getStoreById(storeId);

        MenuCategory menuCategory = MenuCategory.builder()
                .title(request.title())
                .description(request.description())
                .store(store)
                .build();

        MenuCategory savedCategory = menuCategoryRepository.save(menuCategory);
        return MenuCategoryResponse.of(savedCategory);
    }

    @Transactional
    public MenuCategoryResponse updateMenuCategory(Long categoryId, MenuCategoryUpdateRequest request) {
        MenuCategory category = getMenuCategoryById(categoryId);

        MenuCategory updatedCategory = category.toBuilder()
                .title(request.title() != null ? request.title() : category.getTitle())
                .description(request.description() != null ? request.description() : category.getDescription())
                .build();

        MenuCategory savedCategory = menuCategoryRepository.save(updatedCategory);
        return MenuCategoryResponse.of(savedCategory);
    }

    @Transactional
    public void deleteMenuCategory(Long categoryId) {
        MenuCategory category = getMenuCategoryById(categoryId);

        menuCategoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getMenuCategoriesByStore(Long storeId) {
        List<MenuCategory> categories = menuCategoryRepository.findAllByStoreId(storeId);
        return categories.stream()
                .map(MenuCategoryResponse::of)
                .toList();
    }

    private Menu getMenuById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
    }

    private MenuCategory getMenuCategoryById(Long categoryId) {
        return menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
    }

    private Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
    }
}
