package com.ocean.piuda.dashboard.service;
import com.ocean.piuda.dashboard.dto.request.AreaPageRequest;
import com.ocean.piuda.dashboard.enums.RestorationRegion;
import com.ocean.piuda.dashboard.enums.ProjectLevel;
import com.ocean.piuda.dashboard.enums.HabitatType;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.dto.response.AreaMarkerResponse;
import com.ocean.piuda.dashboard.dto.response.AreaStatResponse;
import com.ocean.piuda.dashboard.entity.*;
import com.ocean.piuda.dashboard.enums.HabitatType;
import com.ocean.piuda.dashboard.enums.ProjectLevel;
import com.ocean.piuda.dashboard.repository.*;
import com.ocean.piuda.dashboard.repository.projection.*;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import com.ocean.piuda.dashboard.dto.request.LogPageRequest;
import com.ocean.piuda.dashboard.dto.response.*;
import com.ocean.piuda.global.api.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {

    private final ProjectAreaRepository projectAreaRepository;
    private final DashboardAggregateBuilder aggregateBuilder;
    private final WaterLogRepository waterLogRepository;
    private final TransplantLogRepository transplantLogRepository;
    private final GrowthLogRepository growthLogRepository;
    private final MediaLogRepository mediaLogRepository;

    private static final LocalDate DEFAULT_FROM = LocalDate.of(1900, 1, 1);
    private static final LocalDate DEFAULT_TO   = LocalDate.of(3000, 12, 31);

    private LocalDate safeFrom(LocalDate from) { return from != null ? from : DEFAULT_FROM; }
    private LocalDate safeTo(LocalDate to) { return to != null ? to : DEFAULT_TO; }

    /**
     * ----------------------------
     * TransplantLog 조회
     * ----------------------------
     */
    public TransplantLogResponse getTransplantLog(Long areaId, Long logId) {
        var log = transplantLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        return TransplantLogResponse.from(log);
    }

    public PageResponse<TransplantLogResponse> getTransplantLogs(
            Long areaId, LocalDate from, LocalDate to, LogPageRequest req
    ) {
        var pageable = req.toPageable();
        Page<TransplantLog> page = transplantLogRepository.findAllByProjectAreaIdAndRecordDateBetween(
                areaId, safeFrom(from), safeTo(to), pageable
        );

        List<TransplantLogResponse> content = page.getContent().stream()
                .map(TransplantLogResponse::from)
                .toList();

        Page<TransplantLogResponse> mapped = new PageImpl<>(content, pageable, page.getTotalElements());
        return PageResponse.of(mapped);
    }

    /**
     * ----------------------------
     * GrowthLog 조회
     * ----------------------------
     */
    public GrowthLogResponse getGrowthLog(Long areaId, Long logId) {
        var log = growthLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        return GrowthLogResponse.from(log);
    }

    public PageResponse<GrowthLogResponse> getGrowthLogs(
            Long areaId, LocalDate from, LocalDate to, LogPageRequest req
    ) {
        var pageable = req.toPageable();
        Page<GrowthLog> page = growthLogRepository.findAllByProjectAreaIdAndRecordDateBetween(
                areaId, safeFrom(from), safeTo(to), pageable
        );

        List<GrowthLogResponse> content = page.getContent().stream()
                .map(GrowthLogResponse::from)
                .toList();

        Page<GrowthLogResponse> mapped = new PageImpl<>(content, pageable, page.getTotalElements());
        return PageResponse.of(mapped);
    }

    /**
     * ----------------------------
     * WaterLog 조회
     * ----------------------------
     */
    public WaterLogResponse getWaterLog(Long areaId, Long logId) {
        var log = waterLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        return WaterLogResponse.from(log);
    }

    public PageResponse<WaterLogResponse> getWaterLogs(
            Long areaId, LocalDate from, LocalDate to, LogPageRequest req
    ) {
        var pageable = req.toPageable();
        Page<WaterLog> page = waterLogRepository.findAllByProjectAreaIdAndRecordDateBetween(
                areaId, safeFrom(from), safeTo(to), pageable
        );

        List<WaterLogResponse> content = page.getContent().stream()
                .map(WaterLogResponse::from)
                .toList();

        Page<WaterLogResponse> mapped = new PageImpl<>(content, pageable, page.getTotalElements());
        return PageResponse.of(mapped);
    }

    /**
     * ----------------------------
     * MediaLog 조회
     * ----------------------------
     */
    public MediaLogResponse getMediaLog(Long areaId, Long logId) {
        var log = mediaLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        return MediaLogResponse.from(log);
    }

    public PageResponse<MediaLogResponse> getMediaLogs(
            Long areaId, LocalDate from, LocalDate to, LogPageRequest req
    ) {
        var pageable = req.toPageable();
        Page<MediaLog> page = mediaLogRepository.findAllByProjectAreaIdAndRecordDateBetween(
                areaId, safeFrom(from), safeTo(to), pageable
        );

        List<MediaLogResponse> content = page.getContent().stream()
                .map(MediaLogResponse::from)
                .toList();

        Page<MediaLogResponse> mapped = new PageImpl<>(content, pageable, page.getTotalElements());
        return PageResponse.of(mapped);
    }


    public PageResponse<ProjectAreaListItemResponse> getAreas(
            RestorationRegion region,
            ProjectLevel level,
            HabitatType habitat,
            LocalDate from,
            LocalDate to,
            String keyword,
            AreaPageRequest req
    ) {
        var pageable = req.toPageable();

        Specification<ProjectArea> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (region != null) {
                predicates.add(cb.equal(root.get("restorationRegion"), region));
            }
            if (level != null) {
                predicates.add(cb.equal(root.get("level"), level));
            }
            if (habitat != null) {
                predicates.add(cb.equal(root.get("habitat"), habitat));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), like));
            }

            //  기간 필터: "겹치는 프로젝트만" 포함
            // - from/to 둘 다 있을 때: startDate <= to AND (endDate IS NULL OR endDate >= from)
            // - from만: endDate IS NULL OR endDate >= from
            // - to만: startDate <= to
            if (from != null && to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), to));
                predicates.add(cb.or(
                        cb.isNull(root.get("endDate")),
                        cb.greaterThanOrEqualTo(root.get("endDate"), from)
                ));
            } else if (from != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("endDate")),
                        cb.greaterThanOrEqualTo(root.get("endDate"), from)
                ));
            } else if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProjectArea> page = projectAreaRepository.findAll(spec, pageable);

        var content = page.getContent().stream()
                .map(ProjectAreaListItemResponse::from)
                .toList();

        Page<ProjectAreaListItemResponse> mapped = new PageImpl<>(content, pageable, page.getTotalElements());
        return PageResponse.of(mapped);
    }


    /**
     * 1. 상세 조회 : 특정 작업 영역의 모든 상세 데이터(5개 탭)를 조회합니다.
     */
    public AreaDetailResponse getAreaDetail(Long id) {
        // 작업 영역 엔티티
        ProjectArea area = projectAreaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        // 환경 데이터 (DB 최적화 조회)
        // - 전체 수온 시계열 (프로젝션)
        List<TemperaturePointProjection> tempHistory = waterLogRepository.findTemperatureHistory(id);
        // - 최신 환경 요약 (단건 엔티티)
        EnvironmentSummaryProjection envSummary = waterLogRepository.findEnvironmentSummaryModeLast3Months(id);

        // 이식 데이터 (DB 집계 조회)
        // - 전체 방식별 분포 (집계 프로젝션)
        List<MethodDistributionProjection> distributions = transplantLogRepository.findMethodDistribution(id);
        // - 최근 3개월 월별 작업 횟수 (서브쿼리 집계 프로젝션)
        List<WorkHistoryPointProjection> history = transplantLogRepository.findWorkHistory(id);

        // 4. 생태 데이터 (조건부 필터링 조회)
        // - 대표 개체로 지정된 로그의 전체 이력만 조회
        List<GrowthLog> repGrowthLogs = growthLogRepository.findAllByProjectAreaIdAndIsRepresentativeTrueOrderByRecordDateAsc(id);
        List<TransplantItemProjection> speciesItems = transplantLogRepository.findTransplantItems(id);
        List<MethodAttachmentStatusProjection> methodStatuses = transplantLogRepository.findLatestAttachmentStatusPerMethod(id);

        // 마지막 작업일
        // lastDate : @Query 에서 부여된 alias
        AccumulatedStatsProjection stats = transplantLogRepository.getAccumulatedStats(id);

        LocalDate lastWorkDate = (stats != null && stats.getLastDate() != null)
                        ? stats.getLastDate()
                        : area.getStartDate();

        Double totalTransplantArea = (stats != null && stats.getTotalArea() != null)
                ? stats.getTotalArea()
                : 0.0;

        // 5. 미디어 데이터
        List<MediaPointProjection> mediaHistory = mediaLogRepository.findAllByProjectAreaIdOrderByRecordDateAsc(id);


        // 최종 조립
        return aggregateBuilder.build(area, tempHistory, envSummary, distributions, history, repGrowthLogs, methodStatuses, speciesItems, lastWorkDate, totalTransplantArea, mediaHistory);
    }


    /**
     *  반경 내 통계 정보 요약
     */
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
        String habitatName = p.getHabitat() != null ?
                HabitatType.valueOf(p.getHabitat()).getName() : "기타";

        String stageName = p.getLevel() != null ?
                ProjectLevel.valueOf(p.getLevel()).getName() : "미정";

        return AreaMarkerResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .lat(p.getLat())
                .lon(p.getLon())
                .startDate(p.getStartDate())
                .depth(p.getDepth())
                .areaSize(p.getAreaSize())
                .habitat(habitatName)
                .level(stageName)
                .build();
    }


}
