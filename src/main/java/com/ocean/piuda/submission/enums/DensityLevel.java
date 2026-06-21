package com.ocean.piuda.submission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 밀도/분포 레벨
 */
@Getter
@RequiredArgsConstructor
public enum DensityLevel {
    LOW("낮음/적음"),
    MID("중간/보통"),
    HIGH("높음/많음"),
    NONE("없음"),
    ONGOING("진행중"),
    SEVERE("심각");

    private final String description;
}
