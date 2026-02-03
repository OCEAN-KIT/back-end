package com.ocean.piuda.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 해조류 생육 상태 (모니터링 전용)
 */
@Getter
@RequiredArgsConstructor
public enum SeaweedHealthStatus {
    GOOD("양호"),
    WEAK("쇠약"),
    DROPPED("탈락");

    private final String description;
}
