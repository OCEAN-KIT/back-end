package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.LogSort;
import com.ocean.piuda.global.api.dto.PageRequest;
import jakarta.validation.constraints.NotNull;

public class LogPageRequest extends PageRequest<LogSort> {

    public LogPageRequest() {
        super();
        this.setSort(LogSort.RECORD_DATE_DESC);
    }

    @Override
    public void setSort(LogSort sort) {
        super.setSort(sort == null ? LogSort.RECORD_DATE_DESC : sort);
    }
}
