package com.ocean.piuda.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이식 방식
 */
@Getter
@RequiredArgsConstructor
public enum TransplantMethodType {
    ROPE_LINE("로프 연승"),
    SEED_DIRECT("종자 직접 이식"),
    MODULE("이식용 모듈"),
    OTHER("기타");
    private final String description;
}
