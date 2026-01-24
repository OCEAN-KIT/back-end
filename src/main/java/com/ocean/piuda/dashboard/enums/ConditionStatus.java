package com.ocean.piuda.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ConditionStatus {

    @Getter
    @RequiredArgsConstructor
    public enum Environment {
        GOOD("좋음"), NORMAL("보통"), POOR("나쁨");
        private final String name;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Ecology {
        STABLE("안정"), DECREASED("일부 감소"), UNSTABLE("불안정");
        private final String name;
    }
}