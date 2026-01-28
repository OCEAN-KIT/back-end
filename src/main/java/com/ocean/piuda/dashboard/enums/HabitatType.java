package com.ocean.piuda.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HabitatType {
    ROCKY("암반"),
    MIXED("혼합"),
    OTHER("기타");

    private final String name;
}