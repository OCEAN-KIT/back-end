package com.ocean.piuda.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모니터링 - 해조 이식 적합성
 */
@Getter
@RequiredArgsConstructor
public enum Suitability {
    SUITABLE("적합"),
    UNSUITABLE("부적합");

    private final String description;
}