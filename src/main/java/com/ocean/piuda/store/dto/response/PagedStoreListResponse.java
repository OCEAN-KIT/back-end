package com.ocean.piuda.store.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedStoreListResponse(
        List<StoreListResponse> content,
        int page,        // 1부터 시작
        int size,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    public static PagedStoreListResponse of(Page<StoreListResponse> page) {
        return new PagedStoreListResponse(
                page.getContent(),
                page.getNumber() + 1, // 0-based를 1-based로 변환
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
