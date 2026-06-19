package com.ocean.piuda.dashboard.repository.projection;

import java.time.LocalDate;

/**
 * 지도 마커 쿼리용 Projection
 * - 네이티브 쿼리 SELECT 컬럼 alias 와 이름을 맞춰야 함
 */
public interface AreaMarkerProjection {
    Long getId();
    String getName();

    Double getLat();
    Double getLon();

    LocalDate getStartDate();
    Double getDepth();
    Double getAreaSize();

    String getHabitat(); // DB에 저장된 Enum String (예: "ROCKY_REEF")
    String getLevel();  // DB에 저장된 Enum String (예: "TRANSPLANT_COMPLETED")
}
