package com.ocean.piuda.dashboard.repository.projection;

import com.ocean.piuda.dashboard.enums.TransplantMethod;

public interface TransplantItemProjection {
    String getSpeciesName();
    TransplantMethod getMethodName(); // Enum name
    Long getTotalCount();
}