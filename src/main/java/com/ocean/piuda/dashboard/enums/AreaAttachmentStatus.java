package com.ocean.piuda.dashboard.enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 영역 전체 범위의 착생 상태
 */
@Getter
@RequiredArgsConstructor
public enum AreaAttachmentStatus {
    STABLE("안정"), DECREASED("일부 감소"), UNSTABLE("불안정");
    private final String name;
}
