package com.ocean.piuda.store.enums;

import com.ocean.piuda.global.enums.SortEnum;
import org.springframework.data.domain.Sort;

public enum StoreSortType implements SortEnum {
    LATEST,      // 최신순
    POPULAR,     // 주문많은순
    RATING,      // 별점높은순
    REVIEW_COUNT; // 리뷰많은순

    @Override
    public Sort toSort() {
        return switch (this) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case POPULAR -> Sort.by(Sort.Direction.DESC, "orderCount");
            case RATING -> Sort.by(Sort.Direction.DESC, "averageRating");
            case REVIEW_COUNT -> Sort.by(Sort.Direction.DESC, "reviewCount");
        };
    }
}
