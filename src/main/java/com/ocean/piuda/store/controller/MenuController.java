//package com.ocean.piuda.store.controller;
//
//import com.ocean.piuda.global.api.dto.ApiData;
//import com.ocean.piuda.store.dto.request.MenuCategoryCreateRequest;
//import com.ocean.piuda.store.dto.request.MenuCategoryUpdateRequest;
//import com.ocean.piuda.store.dto.request.MenuCreateRequest;
//import com.ocean.piuda.store.dto.request.MenuUpdateRequest;
//import com.ocean.piuda.store.dto.response.MenuCategoryResponse;
//import com.ocean.piuda.store.dto.response.MenuResponse;
//import com.ocean.piuda.store.service.MenuService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//
//@Tag(name = "메뉴", description = "메뉴 및 메뉴 카테고리 관리 API")
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//public class MenuController {
//
//    private final MenuService menuService;
//
//    @Operation(summary = "메뉴 생성", description = "특정 가게에 새로운 메뉴를 생성합니다.")
//    @PostMapping("/stores/{storeId}/menus")
//    public ApiData<MenuResponse> createMenu(
//            @PathVariable Long storeId,
//            @Valid @RequestBody MenuCreateRequest request
//    ) {
//        MenuResponse menu = menuService.createMenu(storeId, request);
//        return ApiData.created(menu);
//    }
//
//    @Operation(summary = "메뉴 수정", description = "기존 메뉴의 정보를 수정합니다.")
//    @PutMapping("/menus/{menuId}")
//    public ApiData<MenuResponse> updateMenu(
//            @PathVariable Long menuId,
//            @Valid @RequestBody MenuUpdateRequest request
//    ) {
//        MenuResponse menu = menuService.updateMenu(menuId, request);
//        return ApiData.ok(menu);
//    }
//
//    @Operation(summary = "메뉴 삭제", description = "기존 메뉴를 삭제합니다.")
//    @DeleteMapping("/menus/{menuId}")
//    public ApiData<Void> deleteMenu(@PathVariable Long menuId) {
//        menuService.deleteMenu(menuId);
//        return ApiData.noContent();
//    }
//
//    @Operation(summary = "메뉴 상세 조회", description = "특정 메뉴의 상세 정보를 조회합니다.")
//    @GetMapping("/menus/{menuId}")
//    public ApiData<MenuResponse> getMenu(@PathVariable Long menuId) {
//        MenuResponse menu = menuService.getMenu(menuId);
//        return ApiData.ok(menu);
//    }
//
//    @Operation(summary = "가게 메뉴 목록 조회", description = "특정 가게의 모든 메뉴 목록을 조회합니다.")
//    @GetMapping("/stores/{storeId}/menus")
//    public ApiData<List<MenuResponse>> getMenusByStore(@PathVariable Long storeId) {
//        List<MenuResponse> menus = menuService.getMenusByStore(storeId);
//        return ApiData.ok(menus);
//    }
//
//    @Operation(summary = "메뉴 카테고리 생성", description = "특정 가게에 새로운 메뉴 카테고리를 생성합니다.")
//    @PostMapping("/stores/{storeId}/menu-categories")
//    public ApiData<MenuCategoryResponse> createMenuCategory(
//            @PathVariable Long storeId,
//            @Valid @RequestBody MenuCategoryCreateRequest request
//    ) {
//        MenuCategoryResponse category = menuService.createMenuCategory(storeId, request);
//        return ApiData.created(category);
//    }
//
//    @Operation(summary = "메뉴 카테고리 수정", description = "기존 메뉴 카테고리의 정보를 수정합니다.")
//    @PutMapping("/menu-categories/{categoryId}")
//    public ApiData<MenuCategoryResponse> updateMenuCategory(
//            @PathVariable Long categoryId,
//            @Valid @RequestBody MenuCategoryUpdateRequest request
//    ) {
//        MenuCategoryResponse category = menuService.updateMenuCategory(categoryId, request);
//        return ApiData.ok(category);
//    }
//
//    @Operation(summary = "메뉴 카테고리 삭제", description = "기존 메뉴 카테고리를 삭제합니다.")
//    @DeleteMapping("/menu-categories/{categoryId}")
//    public ApiData<Void> deleteMenuCategory(@PathVariable Long categoryId) {
//        menuService.deleteMenuCategory(categoryId);
//        return ApiData.noContent();
//    }
//
//    @Operation(summary = "가게 메뉴 카테고리 목록 조회", description = "특정 가게의 모든 메뉴 카테고리 목록을 조회합니다.")
//    @GetMapping("/stores/{storeId}/menu-categories")
//    public ApiData<List<MenuCategoryResponse>> getMenuCategoriesByStore(@PathVariable Long storeId) {
//        List<MenuCategoryResponse> categories = menuService.getMenuCategoriesByStore(storeId);
//        return ApiData.ok(categories);
//    }
//}
