package com.ocean.piuda.dashboard.repository;

import com.ocean.piuda.dashboard.entity.ProjectArea;
import com.ocean.piuda.dashboard.repository.projection.AreaStatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectAreaRepository extends JpaRepository<ProjectArea, Long> {

    // 1. 반경 기반 주변 검색 (Nearby)
    @Query(value = """
        SELECT * FROM project_areas p
        WHERE ST_DWithin(
            CAST(p.location AS geography),
            CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography),
            :radiusInMeters
        )
        """, nativeQuery = true)
    List<ProjectArea> findNearbyAreas(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusInMeters") double radiusInMeters
    );

    // 2. 뷰포트 영역 조회 (BBox)
    @Query(value = """
        SELECT * FROM project_areas p
        WHERE p.location && ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
        """, nativeQuery = true)
    List<ProjectArea> findWithinBBox(
            @Param("minLat") double minLat, @Param("minLon") double minLon,
            @Param("maxLat") double maxLat, @Param("maxLon") double maxLon
    );

    // 3. 가장 가까운 N개 찾기 (Nearest - KNN)
    @Query(value = """
        SELECT * FROM project_areas p
        ORDER BY p.location <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)
        LIMIT :limitCount
        """, nativeQuery = true)
    List<ProjectArea> findNearestAreas(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("limitCount") int limitCount
    );

    // 4. 반경 내 통계 (Aggregation) -> Interface Projection 반환
    @Query(value = """
        SELECT 
            count(*)                      AS totalCount, 
            COALESCE(sum(p.area_size), 0) AS totalAreaSize, 
            COALESCE(avg(p.depth), 0)     AS avgDepth
        FROM project_areas p
        WHERE ST_DWithin(
            CAST(p.location AS geography),
            CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography),
            :radiusInMeters
        )
        """, nativeQuery = true)
    AreaStatProjection getNearbyStatistics(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusInMeters") double radiusInMeters
    );
}