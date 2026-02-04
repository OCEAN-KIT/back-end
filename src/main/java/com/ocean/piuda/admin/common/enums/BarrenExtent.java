package com.ocean.piuda.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모니터링 - 갯녹음 정도
 */
@Getter
@RequiredArgsConstructor
public enum BarrenExtent {
    NONE("없음"),
    ONGOING("진행중"),
    SEVERE("심각");

    private final String description;
}