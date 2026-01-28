package com.ocean.piuda.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectLevel {
    OBSERVATION("관측", "초기 상태 기록"),
    SETTLEMENT("정착", "이식 단위 활착 확인"),
    GROWTH("성장", "해조류 군집 확대 관찰"),
    MANAGEMENT("관리", "지속 관찰 및 유지");

    private final String name;
    private final String description;
}