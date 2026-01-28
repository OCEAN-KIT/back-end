package com.ocean.piuda.dashboard.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaCategory {
    BEFORE("복원 전"),
    AFTER("복원 후"),
    TIMELINE("타임라인");

    private final String name;
}