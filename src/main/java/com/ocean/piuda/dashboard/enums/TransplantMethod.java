package com.ocean.piuda.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransplantMethod {
    SEEDLING_STRING("종묘줄", "종묘줄(줄)"),
    ROPE("로프", "로프(m) : 종묘줄을 로프에 고정하여 이식"),
    ROCK_FIXATION("암반 고정", "종묘를 암반에 직접 부착하여 이식"),
    TRANSPLANT_MODULE("이식 모듈", "이식 모듈(기) : 제작된 모듈에 부착 후 수중에 고정"),
    DIRECT_FIXATION("직접 고정 지점", "직접 고정 지점(지점)");

    private final String name;
    private final String description;
}