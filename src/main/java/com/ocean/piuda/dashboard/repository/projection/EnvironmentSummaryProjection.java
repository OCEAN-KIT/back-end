package com.ocean.piuda.dashboard.repository.projection;

public interface EnvironmentSummaryProjection {
    String getVisibility();
    String getCurrent();
    String getSurge();
    String getWave();
}
