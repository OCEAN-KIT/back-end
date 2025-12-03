package com.ocean.piuda.garmin.enums;

import com.ocean.piuda.global.enums.SortEnum;
import org.springframework.data.domain.Sort;

public enum GarminActivitySort implements SortEnum {

    START_TIME_DESC,
    START_TIME_ASC;

    @Override
    public Sort toSort() {
        return switch (this) {
            case START_TIME_DESC -> Sort.by(Sort.Direction.DESC, "startTime", "id");
            case START_TIME_ASC  -> Sort.by(Sort.Direction.ASC, "startTime", "id");
        };
    }
}
