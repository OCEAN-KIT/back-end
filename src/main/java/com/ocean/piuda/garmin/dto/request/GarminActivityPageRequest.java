package com.ocean.piuda.garmin.dto.request;

import com.ocean.piuda.garmin.enums.GarminActivitySort;
import com.ocean.piuda.global.api.dto.PageRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GarminActivityPageRequest extends PageRequest<GarminActivitySort> {

    public GarminActivityPageRequest() {
        // 기본 정렬값 지정 (예: 최신 순)
        setSort(GarminActivitySort.START_TIME_DESC);
    }

    @Override
    @NotNull
    public GarminActivitySort getSort() {
        return super.getSort();
    }
}
