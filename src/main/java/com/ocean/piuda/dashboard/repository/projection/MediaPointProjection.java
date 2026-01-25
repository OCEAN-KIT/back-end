package com.ocean.piuda.dashboard.repository.projection;

import com.ocean.piuda.dashboard.enums.MediaCategory;
import java.time.LocalDate;

public interface MediaPointProjection {
    String getMediaUrl();
    LocalDate getRecordDate();
    String getCaption();
    MediaCategory getCategory();
}