package com.ocean.piuda.dashboard.repository;

import com.ocean.piuda.dashboard.entity.ProjectArea;
import com.ocean.piuda.dashboard.repository.projection.AreaMarkerProjection;
import com.ocean.piuda.dashboard.repository.projection.AreaStatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

@Repository
public interface ProjectAreaRepository extends JpaRepository<ProjectArea, Long> , JpaSpecificationExecutor<ProjectArea> {

    // 1. 상세 엔티티 조회용 공간쿼리 (Deprecated / 현재 미사용)
    /**
     * @deprecated
     *   - 현재 서비스 플로우에서는 사용하지 않습니다.
     *   - 잠재적 재활용(예: 관리자/분석 화면)을 위해 보존합니다.
     */
    @Deprecated
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


    /**
     * @deprecated
     *   - 현재 서비스 플로우에서는 사용하지 않습니다.
     *   - 잠재적 재활용을 위해 보존합니다.
     */
    @Deprecated
    @Query(value = """
        SELECT * FROM project_areas p
        WHERE p.location && ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
        """, nativeQuery = true)
    List<ProjectArea> findWithinBBox(
            @Param("minLat") double minLat, @Param("minLon") double minLon,
            @Param("maxLat") double maxLat, @Param("maxLon") double maxLon
    );

    /**
     * @deprecated
     *   - 현재 서비스 플로우에서는 사용하지 않습니다.
     *   - 잠재적 재활용을 위해 보존합니다.
     */
    @Deprecated
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

    // 5. 지도 마커용 경량 쿼리들

    /**
     * 지도 뷰포트(BBox) 내 마커 목록
     * - 상세 데이터 없이, 마커/리스트 표시용 핵심 필드만 조회
     */
    @Query(value = """
        SELECT 
            p.area_id        AS id,
            p.name           AS name,
            ST_Y(p.location) AS lat,
            ST_X(p.location) AS lon,
            p.start_date     AS startDate,
            p.depth          AS depth,
            p.area_size      AS areaSize,
            p.habitat        AS habitat,
            p.level          AS level
        FROM project_areas p
        WHERE p.location && ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
        """, nativeQuery = true)
    List<AreaMarkerProjection> findMarkersWithinBBox(
            @Param("minLat") double minLat,
            @Param("minLon") double minLon,
            @Param("maxLat") double maxLat,
            @Param("maxLon") double maxLon
    );

    /**
     * 중심 좌표와 반경(미터) 기준 마커 목록
     * - 지도/리스트에서 주변 마커를 표시할 때 사용
     */
    @Query(value = """
        SELECT 
            p.area_id        AS id,
            p.name           AS name,
            ST_Y(p.location) AS lat,
            ST_X(p.location) AS lon,
            p.start_date     AS startDate,
            p.depth          AS depth,
            p.area_size      AS areaSize,
            p.habitat        AS habitat,
            p.level          AS level
        FROM project_areas p
        WHERE ST_DWithin(
            CAST(p.location AS geography),
            CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography),
            :radiusInMeters
        )
        """, nativeQuery = true)
    List<AreaMarkerProjection> findMarkersNearby(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusInMeters") double radiusInMeters
    );


    /**
     * KNN 기반 가장 가까운 N개 마커 목록
     * - 거리순(가까운 순)으로 정렬된 마커 요약 데이터를 반환합니다.
     */
    @Query(value = """
        SELECT 
            p.area_id        AS id,
            p.name           AS name,
            ST_Y(p.location) AS lat,
            ST_X(p.location) AS lon,
            p.start_date     AS startDate,
            p.depth          AS depth,
            p.area_size      AS areaSize,
            p.habitat        AS habitat,
            p.level          AS level
        FROM project_areas p
        ORDER BY p.location <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)
        LIMIT :limitCount
        """, nativeQuery = true)
    List<AreaMarkerProjection> findNearestMarkers(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("limitCount") int limitCount
    );
}
