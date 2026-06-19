package com.ocean.piuda.dashboard.enums;

import com.ocean.piuda.global.enums.SortEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public enum AreaSort implements SortEnum {

    ID_DESC(Sort.by(Sort.Direction.DESC, "id")),
    ID_ASC(Sort.by(Sort.Direction.ASC, "id")),

    START_DATE_DESC(Sort.by(Sort.Direction.DESC, "startDate").and(Sort.by(Sort.Direction.DESC, "id"))),
    START_DATE_ASC(Sort.by(Sort.Direction.ASC, "startDate").and(Sort.by(Sort.Direction.ASC, "id"))),

    NAME_ASC(Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id"))),
    NAME_DESC(Sort.by(Sort.Direction.DESC, "name").and(Sort.by(Sort.Direction.DESC, "id")));

    private final Sort sort;

    @Override
    public Sort toSort() {
        return sort;
    }
}
