//package com.ocean.piuda.store.controller;
//
//import com.ocean.piuda.global.api.dto.ApiData;
//import com.ocean.piuda.store.service.StoreCommandService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@Tag(name = "가게 관리", description = "가게 사장용 가게 관리 API")
//@RestController
//@RequestMapping("/api/stores")
//@RequiredArgsConstructor
//public class StoreOwnerController {
//
//    private final StoreCommandService storeCommandService;
//
////    @Operation(
////        summary = "가게 등록",
////        description = "새로운 가게를 등록합니다.\n\n" +
////                     "**카테고리 (category)**: 한식, 중식, 일식, 양식, 호텔, 모텔, 펜션, 리조트, 패션, 가전, 생활용품, 뷰티, 국내선, 국제선, 특가"
////    )
////    @PostMapping
////    public ApiData<StoreResponse> createStore(
////            @Valid @RequestBody StoreCreateRequest request
////    ) {
////        StoreResponse store = storeCommandService.createStore(request);
////        return ApiData.created(store);
////    }
////
////    @Operation(
////        summary = "가게 정보 수정",
////        description = "기존 가게의 정보를 수정합니다.\n\n" +
////                     "**카테고리 (category)**: 한식, 중식, 일식, 양식, 호텔, 모텔, 펜션, 리조트, 패션, 가전, 생활용품, 뷰티, 국내선, 국제선, 특가"
////    )
////    @PutMapping("/{storeId}")
////    public ApiData<StoreResponse> updateStore(
////            @PathVariable Long storeId,
////            @Valid @RequestBody StoreUpdateRequest request
////    ) {
////        StoreResponse store = storeCommandService.updateStore(storeId, request);
////        return ApiData.ok(store);
////    }
//
//    @Operation(summary = "가게 삭제", description = "기존 가게를 삭제합니다.")
//    @DeleteMapping("/{storeId}")
//    public ApiData<Void> deleteStore(@PathVariable Long storeId) {
//        storeCommandService.deleteStore(storeId);
//        return ApiData.noContent();
//    }
//
//    @Operation(summary = "가게 오픈/클로즈 상태 전환", description = "가게의 오픈/클로즈 상태를 전환합니다.")
//    @PatchMapping("/{storeId}/toggle-open")
//    public ApiData<Void> toggleStoreOpenStatus(@PathVariable Long storeId) {
//        storeCommandService.toggleStoreOpenStatus(storeId);
//        return ApiData.noContent();
//    }
//
//}
