package com.ocean.piuda.dashboard.service;

import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.dto.response.AreaMarkerResponse;
import com.ocean.piuda.dashboard.dto.response.AreaStatResponse;
import com.ocean.piuda.dashboard.entity.ProjectArea;
import com.ocean.piuda.dashboard.enums.HabitatType;
import com.ocean.piuda.dashboard.enums.ProjectStatus;
import com.ocean.piuda.dashboard.repository.ProjectAreaRepository;
import com.ocean.piuda.dashboard.repository.projection.AreaMarkerProjection;
import com.ocean.piuda.dashboard.repository.projection.AreaStatProjection;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {

    private final ProjectAreaRepository projectAreaRepository;

    // 1. 상세 조회
    public AreaDetailResponse getAreaDetail(Long id) {
        ProjectArea area = projectAreaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        return AreaDetailResponse.fromEntity(area);
    }

    /**
     * @deprecated
     *   - 여러 작업 영역의 '상세 데이터'를 반경 기준으로 한 번에 조회하기 위한 메서드입니다.
     *   - 현재 서비스 플로우에서는 사용하지 않으며,잠재적 재활용을 위해 보존합니다.
     */
    @Deprecated
    public List<AreaDetailResponse> getNearbyAreas(Double lat, Double lon, Double radiusKm) {
        double radiusMeters = (radiusKm != null ? radiusKm : 5.0) * 1000;
        return projectAreaRepository.findNearbyAreas(lat, lon, radiusMeters).stream()
                .map(AreaDetailResponse::fromEntity)
                .toList();
    }

    /**
     * @deprecated
     *   - 뷰포트 기준으로 여러 작업 영역의 '상세 데이터'를 모두 내려주는 메서드입니다.
     *   - 현재 서비스 플로우에서는 사용하지 않으며,잠재적 재활용을 위해 보존합니다.
     */
    @Deprecated
    public List<AreaDetailResponse> getAreasInBBox(Double minLat, Double minLon, Double maxLat, Double maxLon) {
        return projectAreaRepository.findWithinBBox(minLat, minLon, maxLat, maxLon).stream()
                .map(AreaDetailResponse::fromEntity)
                .toList();
    }

    /**
     * @deprecated
     *   - "가장 가까운 작업 영역 목록의 상세 데이터"를 조회하기 위한 메서드입니다.
     *   - 현재 서비스 플로우에서는 사용하지 않으며,잠재적 재활용을 위해 보존합니다.
     */
    @Deprecated
    public List<AreaDetailResponse> getNearestAreas(Double lat, Double lon, Integer limit) {
        int limitCount = (limit != null && limit > 0) ? limit : 3;
        return projectAreaRepository.findNearestAreas(lat, lon, limitCount).stream()
                .map(AreaDetailResponse::fromEntity)
                .toList();
    }

    // 1-4. 통계 (Projection -> DTO)
    public AreaStatResponse getNearbyStats(Double lat, Double lon, Double radiusKm) {
        double radiusMeters = (radiusKm != null ? radiusKm : 10.0) * 1000;

        AreaStatProjection proj = projectAreaRepository.getNearbyStatistics(lat, lon, radiusMeters);

        return AreaStatResponse.builder()
                .totalCount(proj.getTotalCount() != null ? proj.getTotalCount() : 0L)
                .totalAreaSize(proj.getTotalAreaSize() != null ? proj.getTotalAreaSize() : 0.0)
                .avgDepth(proj.getAvgDepth() != null ? proj.getAvgDepth() : 0.0)
                .build();
    }

    // 2. 지도 마커용 경량 조회

    /**
     * 뷰포트(BBox) 내 마커 목록
     * - 지도 뷰포트에 보이는 마커들만 요약 정보로 반환합니다.
     * - 상세 데이터는 /areas/{id}로 별도 조회하는 패턴을 사용합니다.
     */
    public List<AreaMarkerResponse> getMarkersInBBox(
            Double minLat, Double minLon,
            Double maxLat, Double maxLon
    ) {
        List<AreaMarkerProjection> rows =
                projectAreaRepository.findMarkersWithinBBox(minLat, minLon, maxLat, maxLon);

        return rows.stream()
                .map(this::toMarkerResponse)
                .toList();
    }

    /**
     * 반경 기반 마커 목록
     * - 중심 좌표 + 반경 기준으로 주변 마커를 조회합니다.
     * - 상세 데이터가 필요할 때는 getAreaDetail(id)와 조합해서 사용하세요.
     */
    public List<AreaMarkerResponse> getNearbyMarkers(
            Double lat, Double lon,
            Double radiusKm
    ) {
        double radiusMeters = (radiusKm != null ? radiusKm : 5.0) * 1000;

        List<AreaMarkerProjection> rows =
                projectAreaRepository.findMarkersNearby(lat, lon, radiusMeters);

        return rows.stream()
                .map(this::toMarkerResponse)
                .toList();
    }

    /**
     * KNN 기반 가장 가까운 N개 마커 목록
     * - 주어진 좌표에서 가장 가까운 작업 영역들을 거리순으로 반환합니다.
     * - 지도에서 "내 주변" 리스트/마커를 보여줄 때 유용합니다.
     */
    public List<AreaMarkerResponse> getNearestMarkers(
            Double lat, Double lon,
            Integer limit
    ) {
        int limitCount = (limit != null && limit > 0) ? limit : 3;

        List<AreaMarkerProjection> rows =
                projectAreaRepository.findNearestMarkers(lat, lon, limitCount);

        return rows.stream()
                .map(this::toMarkerResponse)
                .toList();
    }

    // 내부 변환 로직
    private AreaMarkerResponse toMarkerResponse(AreaMarkerProjection p) {
        String habitatDesc = null;
        if (p.getHabitat() != null) {
            try {
                habitatDesc = HabitatType.valueOf(p.getHabitat()).getDescription();
            } catch (IllegalArgumentException e) {
                // enum 매칭 실패 시, 원본 값을 그대로 사용
                habitatDesc = p.getHabitat();
            }
        }

        String stageDesc = null;
        if (p.getStatus() != null) {
            try {
                stageDesc = ProjectStatus.valueOf(p.getStatus()).getDescription();
            } catch (IllegalArgumentException e) {
                stageDesc = p.getStatus();
            }
        }

        return AreaMarkerResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .lat(p.getLat())
                .lon(p.getLon())
                .startDate(p.getStartDate())
                .depth(p.getDepth())
                .areaSize(p.getAreaSize())
                .habitat(habitatDesc)
                .stage(stageDesc)
                .build();
    }


}
