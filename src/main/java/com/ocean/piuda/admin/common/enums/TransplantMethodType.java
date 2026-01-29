package com.ocean.piuda.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이식 방식
 */
@Getter
@RequiredArgsConstructor
public enum TransplantMethodType {
    ROPE("로프"),
    LINE_SET("연승"),
    SEED("종자"),
    DIRECT("직접이식"),
    MODULE("이식용 모듈"),
    OTHER("기타");

    private final String description;
}
