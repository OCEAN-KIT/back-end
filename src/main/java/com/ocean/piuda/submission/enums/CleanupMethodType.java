package com.ocean.piuda.submission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 해양정화 인양 방식
 */
@Getter
@RequiredArgsConstructor
public enum CleanupMethodType {
    HAND("수작업"),
    BAG("인양백"),
    CRANE("크레인");

    private final String description;
}
