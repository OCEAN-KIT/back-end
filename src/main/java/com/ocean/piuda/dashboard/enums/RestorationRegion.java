package com.ocean.piuda.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestorationRegion {
    POHANG("포항"),
    ULJIN("울진");

    private final String name;
}