package com.ocean.piuda.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이식 방식별 착생 상태
 */
@Getter
@RequiredArgsConstructor
public enum SpeciesAttachmentStatus {
    GOOD("양호"), NORMAL("보통"), POOR("미흡");
    private final String name;
}
