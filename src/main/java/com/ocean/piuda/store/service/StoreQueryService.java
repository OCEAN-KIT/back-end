package com.ocean.piuda.store.service;

import com.ocean.piuda.global.api.dto.PageResponse;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.payment.entity.Order;
import com.ocean.piuda.payment.repository.OrderRepository;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import com.ocean.piuda.store.dto.request.StorePageRequest;
import com.ocean.piuda.store.dto.response.MenuBoardResponse;
import com.ocean.piuda.store.dto.response.StoreListResponse;
import com.ocean.piuda.store.dto.response.StoreResponse;
import com.ocean.piuda.store.dto.response.StoreSummaryResponse;
import com.ocean.piuda.store.dto.StoreWithStatsDto;
import com.ocean.piuda.store.entity.Menu;
import com.ocean.piuda.store.entity.MenuCategory;
import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.store.enums.StoreSortType;
import com.ocean.piuda.store.repository.MenuRepository;
import com.ocean.piuda.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class StoreQueryService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final TokenUserService tokenUserService;

    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
    }

    /**
     * store 존재 + userId = null(비로그인) → DTO 반환, isSubscribed = false
     * store 존재 + userId != null(로그인) → DTO 반환, isSubscribed = (구독 여부)
     * store 없음 → DTO 없음 → NotFoundException 발생
     */
    public StoreResponse getStore(Long storeId) {
        Long currentUserId = tokenUserService.getCurrentUserIdOrNull();

        StoreWithStatsDto dto = storeRepository.findByIdWithStats(storeId, currentUserId);
        if (dto == null) throw new BusinessException(ExceptionType.RESOURCE_NOT_FOUND);

        return StoreResponse.of(
                dto.store(),
                dto.averageRating(),
                dto.reviewCount()
        );
    }

    public PageResponse<StoreListResponse> getStores(StorePageRequest pageRequest) {
        Pageable pageable = pageRequest.toPageableWithoutSort();
        Long currentUserId = tokenUserService.getCurrentUserIdOrNull();
        StoreSortType sortType = pageRequest.getSort();

        Page<StoreWithStatsDto> page = getStoresWithSort(pageRequest, currentUserId, sortType, pageable);

        Page<StoreListResponse> mapped = page.map(dto ->
                StoreListResponse.of(
                        dto.store(),
                        dto.averageRating(),
                        dto.reviewCount(),
                        dto.orderCount()
                )
        );
        return PageResponse.of(mapped);
    }

    private Page<StoreWithStatsDto> getStoresWithSort(StorePageRequest pageRequest, Long userId, StoreSortType sortType, Pageable pageable) {
        return switch (sortType) {
            case LATEST -> storeRepository.findAllByCategoryWithStats(userId, pageRequest.getCategory(), pageable);
            case POPULAR ->
                    storeRepository.findAllByCategoryAndPopularityWithStats(userId, pageRequest.getCategory(), pageable);
            case RATING ->
                    storeRepository.findAllByCategoryAndRatingWithStats(userId, pageRequest.getCategory(), pageable);
            case REVIEW_COUNT ->
                    storeRepository.findAllByCategoryAndReviewCountWithStats(userId, pageRequest.getCategory(), pageable);
        };
    }

    /**
     * 맞춤 추천 가게 목록 조회
     * 로그인한 사용자가 최근 주문한 가게 추천
     */
    public List<StoreSummaryResponse> getPersonalizedRecommendations(int limit) {
        Long currentUserId = tokenUserService.getCurrentUserIdOrNull();
        if (currentUserId == null) {
            return List.of(); // 비로그인 시 빈 목록 반환
        }

        List<Order> recentOrders = orderRepository.findRecentOrdersByUserId(currentUserId, PageRequest.of(0, 20));
        List<Store> stores = recentOrders.stream()
                .map(Order::getStore)
                .distinct()
                .limit(limit)
                .toList();


        return stores.stream()
                .map(store ->  StoreSummaryResponse.of(store))
                .toList();
    }

    /**
     * 실시간 트렌드 가게 목록 조회
     * 전체 최근 주문 기준으로 상위 가게들
     */
    public List<StoreSummaryResponse> getTrendingStores(int limit) {
        Pageable pageable = PageRequest.of(0, 20);
        List<Order> recentOrders = orderRepository.findRecentOrders(pageable);

        List<Store> stores = recentOrders.stream()
                .map(Order::getStore)
                .distinct()
                .limit(limit)
                .toList();



        return stores.stream()
                .map(store ->  StoreSummaryResponse.of(store))
                .toList();
    }

    /**
     * 할인율 높은 가게 목록 조회
     * 단일 메뉴 기준으로 할인율이 가장 높은 상위 가게들
     */
    public List<StoreSummaryResponse> getDiscountStores(int limit) {
        Pageable pageable = PageRequest.of(0, 20);
        List<Menu> topDiscountMenus = menuRepository.findTopDiscountMenus(pageable);

        List<Store> selectedStores = topDiscountMenus.stream()
                .map(Menu::getStore)
                .distinct()
                .limit(limit)
                .toList();


        return selectedStores.stream()
                .map(store ->  StoreSummaryResponse.of(store))
                .toList();
    }

    public MenuBoardResponse getMenuBoardOfStore(Long storeId) {
        List<Menu> menus = menuRepository.findAllByStoreIdWithCategory(storeId);
        Map<MenuCategory, List<Menu>> classifiedMenus = menus.stream()
                .collect(Collectors.groupingBy(Menu::getMenuCategory));

        List<MenuBoardResponse.MenuCategoryResponse> categories = classifiedMenus.entrySet().stream()
                .map(entry -> {
                    MenuCategory category = entry.getKey();
                    List<Menu> categoryMenus = entry.getValue();

                    if (category == null) {
                        return MenuBoardResponse.MenuCategoryResponse.ofUncategorized(categoryMenus);
                    } else {
                        return MenuBoardResponse.MenuCategoryResponse.of(category, categoryMenus);
                    }
                })
                .toList();

        return new MenuBoardResponse(categories);
    }

    public PageResponse<StoreListResponse> searchStores(String keyword, StorePageRequest pageRequest) {
        Pageable pageable = pageRequest.toPageableWithoutSort();
        Long currentUserId = tokenUserService.getCurrentUserIdOrNull();

        Page<StoreWithStatsDto> page = storeRepository.searchStoresByKeyword(keyword, currentUserId, pageable);

        Page<StoreListResponse> mapped = page.map(dto ->
                StoreListResponse.of(
                        dto.store(),
                        dto.averageRating(),
                        dto.reviewCount(),
                        dto.orderCount()
                )
        );
        return PageResponse.of(mapped);
    }
}
