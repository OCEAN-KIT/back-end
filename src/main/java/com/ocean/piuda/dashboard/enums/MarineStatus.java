package com.ocean.piuda.dashboard.enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 기록 당시 해양 기상
 */
@Getter
@RequiredArgsConstructor
public enum MarineStatus {
    GOOD("좋음"), NORMAL("보통"), POOR("나쁨");
    private final String name;
}
