package com.ocean.piuda.dashboard.repository.projection;

import com.ocean.piuda.dashboard.enums.TransplantMethod;

public interface MethodDistributionProjection {
    TransplantMethod getMethodName();
    Long getCount();
}
