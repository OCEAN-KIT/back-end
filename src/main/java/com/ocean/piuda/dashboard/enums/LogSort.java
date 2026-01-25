package com.ocean.piuda.dashboard.enums;

import com.ocean.piuda.global.enums.SortEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public enum LogSort implements SortEnum {

    RECORD_DATE_DESC(Sort.by(Sort.Direction.DESC, "recordDate").and(Sort.by(Sort.Direction.DESC, "id"))),
    RECORD_DATE_ASC(Sort.by(Sort.Direction.ASC, "recordDate").and(Sort.by(Sort.Direction.ASC, "id"))),
    ID_DESC(Sort.by(Sort.Direction.DESC, "id")),
    ID_ASC(Sort.by(Sort.Direction.ASC, "id"));

    private final Sort sort;

    @Override
    public Sort toSort() {
        return sort;
    }
}
