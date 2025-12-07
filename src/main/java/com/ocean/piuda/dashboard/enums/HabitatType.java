package com.ocean.piuda.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HabitatType {
    KELP_FOREST("켈프숲"),
    SEAGRASS_BED("잘피밭"),
    URCHIN_BARREN("성게 황폐지"),
    ROCKY_REEF("암반리프"),
    SOFT_BOTTOM("연질저서"),
    ARTIFICIAL_STRUCTURE("인공구조물"),
    OTHER("기타");

    private final String description;
}