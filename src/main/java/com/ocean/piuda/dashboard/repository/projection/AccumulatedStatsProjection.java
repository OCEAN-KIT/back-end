package com.ocean.piuda.dashboard.repository.projection;

import java.time.LocalDate;

public interface AccumulatedStatsProjection {
    Long getTotalCount();
    LocalDate getLastDate();
    Double getTotalArea();
}
