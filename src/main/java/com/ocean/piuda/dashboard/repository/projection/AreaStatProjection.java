package com.ocean.piuda.dashboard.repository.projection;

public interface AreaStatProjection {
    Long getTotalCount();
    Double getTotalAreaSize();
    Double getAvgDepth();
}