package com.ocean.piuda.submission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모니터링 - 지형 구성
 */
@Getter
@RequiredArgsConstructor
public enum TerrainType {
    ROCK("암반"),
    SAND("모래"),
    MIXED("혼합"),
    OTHER("기타");

    private final String description;
}