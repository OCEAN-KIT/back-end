package com.ocean.piuda.store.service;

import com.ocean.piuda.security.jwt.service.TokenUserService;
import com.ocean.piuda.store.dto.request.StoreCreateRequest;
import com.ocean.piuda.store.dto.request.StoreUpdateRequest;
import com.ocean.piuda.store.dto.response.StoreResponse;
import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.store.repository.StoreRepository;
import com.ocean.piuda.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreCommandService {

    private final StoreRepository storeRepository;
    private final StoreQueryService storeQueryService;
    private final TokenUserService tokenUserService;


    public StoreResponse createStore(StoreCreateRequest request) {
        User currentUser = tokenUserService.getCurrentUser();
        System.out.println(currentUser);
        Store store = Store.builder()
                .owner(currentUser)
                .name(request.name())
                .category(request.category())
                .description(request.description())
                .address(request.address())
                .phoneNumber(request.phoneNumber())
                .openingHours(request.openingHours())
                .bannerImageUrl(request.bannerImageUrl())
                .bizRegNo(request.bizRegNo())
                .build();

        Store savedStore = storeRepository.save(store);
        return StoreResponse.of(savedStore);
    }


    public StoreResponse updateStore(Long storeId, StoreUpdateRequest request) {
        Store store = storeQueryService.getStoreById(storeId);

        Store updatedStore = store.toBuilder()
                .name(request.name() != null ? request.name() : store.getName())
                .category(request.category() != null ? request.category() : store.getCategory())
                .description(request.description() != null ? request.description() : store.getDescription())
                .address(request.address() != null ? request.address() : store.getAddress())
                .phoneNumber(request.phoneNumber() != null ? request.phoneNumber() : store.getPhoneNumber())
                .openingHours(request.openingHours() != null ? request.openingHours() : store.getOpeningHours())
                .bannerImageUrl(request.bannerImageUrl() != null ? request.bannerImageUrl() : store.getBannerImageUrl())
                .bizRegNo(request.bizRegNo() != null ? request.bizRegNo() : store.getBizRegNo())
                .build();

        Store savedStore = storeRepository.save(updatedStore);
        return StoreResponse.of(savedStore);
    }



    public void deleteStore(Long storeId) {
        Store store = storeQueryService.getStoreById(storeId);
        storeRepository.delete(store);
    }


    public void toggleStoreOpenStatus(Long storeId) {
        Store store = storeQueryService.getStoreById(storeId);
        store.changeOpenStatus(!store.getIsOpen());
        storeRepository.save(store);
    }
}
