package com.ocean.piuda.dashboard.repository.projection;

import java.time.LocalDate;

public interface TemperaturePointProjection {
    LocalDate getRecordDate();
    Double getTemperature();
}
