package com.ocean.piuda.dashboard.service;

import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.dto.response.AreaStatResponse;
import com.ocean.piuda.dashboard.entity.ProjectArea;
import com.ocean.piuda.dashboard.repository.ProjectAreaRepository;
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

    public AreaDetailResponse getAreaDetail(Long id) {
        ProjectArea area = projectAreaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        return AreaDetailResponse.fromEntity(area);
    }

    // 1. 반경 검색
    public List<AreaDetailResponse> getNearbyAreas(Double lat, Double lon, Double radiusKm) {
        double radiusMeters = (radiusKm != null ? radiusKm : 5.0) * 1000;
        return projectAreaRepository.findNearbyAreas(lat, lon, radiusMeters).stream()
                .map(AreaDetailResponse::fromEntity)
                .toList();
    }

    // 2. 뷰포트 검색
    public List<AreaDetailResponse> getAreasInBBox(Double minLat, Double minLon, Double maxLat, Double maxLon) {
        return projectAreaRepository.findWithinBBox(minLat, minLon, maxLat, maxLon).stream()
                .map(AreaDetailResponse::fromEntity)
                .toList();
    }

    // 3. 가장 가까운 N개
    public List<AreaDetailResponse> getNearestAreas(Double lat, Double lon, Integer limit) {
        int limitCount = (limit != null && limit > 0) ? limit : 3;
        return projectAreaRepository.findNearestAreas(lat, lon, limitCount).stream()
                .map(AreaDetailResponse::fromEntity)
                .toList();
    }

    // 4. 통계 (Projection -> DTO)
    public AreaStatResponse getNearbyStats(Double lat, Double lon, Double radiusKm) {
        double radiusMeters = (radiusKm != null ? radiusKm : 10.0) * 1000;

        AreaStatProjection proj = projectAreaRepository.getNearbyStatistics(lat, lon, radiusMeters);

        return AreaStatResponse.builder()
                .totalCount(proj.getTotalCount() != null ? proj.getTotalCount() : 0L)
                .totalAreaSize(proj.getTotalAreaSize() != null ? proj.getTotalAreaSize() : 0.0)
                .avgDepth(proj.getAvgDepth() != null ? proj.getAvgDepth() : 0.0)
                .build();
    }
}