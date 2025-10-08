//package com.ocean.piuda.store.controller;
//
//import com.ocean.piuda.global.api.dto.ApiData;
//import com.ocean.piuda.global.api.dto.PageResponse;
//import com.ocean.piuda.store.dto.request.StorePageRequest;
//import com.ocean.piuda.store.dto.request.StoreSearchRequest;
//import com.ocean.piuda.store.dto.response.*;
//import com.ocean.piuda.store.service.StoreQueryService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//
//@Tag(name = "가게", description = "가게 정보 조회 및 구독 관리 API")
//@RestController
//@RequestMapping("/api/stores")
//@RequiredArgsConstructor
//public class StoreController {
//
//    private final StoreQueryService storeQueryService;
//
//
//    @Operation(
//        summary = "가게 목록 조회",
//        description = "가게 목록을 페이징하여 조회합니다.\n\n" +
//                     "**카테고리 (category)**: 한식, 중식, 일식, 양식, 호텔, 모텔, 펜션, 리조트, 패션, 가전, 생활용품, 뷰티, 국내선, 국제선, 특가\n\n" +
//                     "**정렬 (sort)**: LATEST(최신순), POPULAR(인기순), RATING(별점높은순), REVIEW_COUNT(리뷰많은순)"
//    )
//    @GetMapping
//    public ApiData<PageResponse<StoreListResponse>> getStores(
//            @ModelAttribute @Valid StorePageRequest pageRequest
//    ) {
//        PageResponse<StoreListResponse> stores = storeQueryService.getStores(pageRequest);
//        return ApiData.ok(stores);
//    }
//
//    @Operation(summary = "가게 상세 조회", description = "특정 가게의 상세 정보를 조회합니다.")
//    @GetMapping("/{storeId}")
//    public ApiData<StoreResponse> getStore(@PathVariable Long storeId) {
//        StoreResponse store = storeQueryService.getStore(storeId);
//        return ApiData.ok(store);
//    }
//
//
//    @Operation(summary = "가게 메뉴판 조회", description = "특정 가게의 메뉴판을 카테고리별로 분류하여 조회합니다.")
//    @GetMapping("/{storeId}/menu-board")
//    public ApiData<MenuBoardResponse> getMenuBoard(@PathVariable Long storeId) {
//        MenuBoardResponse menuBoard = storeQueryService.getMenuBoardOfStore(storeId);
//        return ApiData.ok(menuBoard);
//    }
//
//    @Operation(
//        summary = "맞춤 추천 가게 목록",
//        description = "로그인한 사용자가 최근 주문한 가게를 추천합니다."
//    )
//    @GetMapping("/recommendations")
//    public ApiData<StoreSummaryListResponse> getPersonalizedRecommendations(
//    ) {
//        List<StoreSummaryResponse> stores = storeQueryService.getPersonalizedRecommendations(5);
//        return ApiData.ok(StoreSummaryListResponse.of(stores));
//    }
//
//    @Operation(
//        summary = "실시간 트렌드 가게 목록",
//        description = "전체 사용자들의 최근 주문 기준으로 상위 인기 가게를 조회합니다.\n\n" +
//                     "최근 20개 주문을 분석하여 가장 많이 주문된 가게들을 실시간으로 집계합니다."
//    )
//    @GetMapping("/trending")
//    public ApiData<StoreSummaryListResponse> getTrendingStores(
//    ) {
//        List<StoreSummaryResponse> stores = storeQueryService.getTrendingStores(5);
//        return ApiData.ok(StoreSummaryListResponse.of(stores));
//    }
//
//    @Operation(
//        summary = "할인율 높은 가게 목록",
//        description = "단일 메뉴 기준으로 할인율이 가장 높은 상위 가게를 조회합니다.\n\n" +
//                     "상위 20개 할인 메뉴의 가게들을 분석하여 할인율이 높은 순으로 정렬합니다."
//    )
//    @GetMapping("/discounts")
//    public ApiData<StoreSummaryListResponse> getDiscountStores(
//    ) {
//        List<StoreSummaryResponse> stores = storeQueryService.getDiscountStores(5);
//        return ApiData.ok(StoreSummaryListResponse.of(stores));
//    }
//
//    @Operation(
//        summary = "가게 통합 검색",
//        description = "메뉴 이름과 가게 이름으로 통합 검색합니다.\n\n" +
//                     "**페이징**: page, size 파라미터로 페이징을 지원합니다."
//    )
//    @GetMapping("/search")
//    public ApiData<PageResponse<StoreListResponse>> searchStores(
//            @RequestParam String keyword,
//            @ModelAttribute @Valid StoreSearchRequest pageRequest
//    ) {
//        PageResponse<StoreListResponse> stores = storeQueryService.searchStores(keyword, pageRequest);
//        return ApiData.ok(stores);
//    }
//
//    //  Todo(later). 식권 관련
//}
