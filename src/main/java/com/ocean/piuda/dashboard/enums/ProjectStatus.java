package com.ocean.piuda.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectStatus {
    TRANSPLANT_COMPLETED("이식 완료"),
    GROWING("성장 중"),
    STABLE("안정화 구역"),
    MONITORING("모니터링 중");

    private final String description;
}