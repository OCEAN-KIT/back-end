package com.ocean.piuda.dashboard.dto.request;

import com.ocean.piuda.dashboard.enums.AreaSort;
import com.ocean.piuda.global.api.dto.PageRequest;

public class AreaPageRequest extends PageRequest<AreaSort> {

    public AreaPageRequest() {
        super();
        this.setSort(AreaSort.ID_DESC);
    }

    @Override
    public void setSort(AreaSort sort) {
        super.setSort(sort == null ? AreaSort.ID_DESC : sort);
    }
}
