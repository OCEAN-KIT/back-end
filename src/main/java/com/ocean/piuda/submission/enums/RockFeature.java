package com.ocean.piuda.submission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모니터링 - 암반 특성
 */
@Getter
@RequiredArgsConstructor
public enum RockFeature {
    SMOOTH("매끈"),
    CRACKED("균열"),
    CALCAREOUS_ALGAE("석회조류 우점"),
    MIXED("혼합"),
    SEAWEED_VEGETATION("해조류 식생");

    private final String description;
}