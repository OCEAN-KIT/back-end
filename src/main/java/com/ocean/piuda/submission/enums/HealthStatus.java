package com.ocean.piuda.submission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 건강 상태 (이식 작업 전용)
 */
@Getter
@RequiredArgsConstructor
public enum HealthStatus {
    A("활착 양호, 생육 정상"),
    B("부분 스트레스"),
    C("쇠약/탈락 진행"),
    D("대부분 탈락");

    private final String description;
}
