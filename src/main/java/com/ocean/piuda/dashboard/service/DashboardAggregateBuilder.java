package com.ocean.piuda.dashboard.service;

import com.ocean.piuda.dashboard.dto.TimeSeriesChartDto;
import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.entity.*;
import com.ocean.piuda.dashboard.enums.*;
import com.ocean.piuda.dashboard.repository.projection.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DashboardAggregateBuilder {

    /**
     * @param area 기본 정보를 담은 엔티티
     * @param tempHistory 수온 차트용 프로젝션 (전체 기간)
     * @param envSummary 최신 3개월 환경 요약
     * @param distributions 이식 방식 분포 (전체 집계 프로젝션)
     * @param history 최근 3개월 작업 횟수 (집계 프로젝션)
     * @param repGrowthLogs 대표 개체 성장 로그 (필터링된 엔티티 리스트)
     */
    public AreaDetailResponse build(
            ProjectArea area,
            List<TemperaturePointProjection> tempHistory,
            EnvironmentSummaryProjection envSummary,
            List<MethodDistributionProjection> distributions,
            List<WorkHistoryPointProjection> history,
            List<GrowthLog> repGrowthLogs,
            List<MethodAttachmentStatusProjection> methodStatuses,
            List<TransplantItemProjection> speciesItems,
            LocalDate lastWorkDate,
            Double totalTransplantArea,
            List<MediaPointProjection> mediaHistory
    ) {
        return AreaDetailResponse.builder()
                .id(area.getId())
                .overview(buildOverview(area))
                .status(buildStatus(area, distributions, history,speciesItems,lastWorkDate,totalTransplantArea))
                .ecology(buildEcology(area, repGrowthLogs,methodStatuses))
                .environment(buildEnvironment(envSummary, tempHistory))
                .photos(buildPhotos(mediaHistory))
                .build();
    }

    private AreaDetailResponse.OverviewTab buildOverview(ProjectArea area) {
        return AreaDetailResponse.OverviewTab.builder()
                .name(area.getName())
                .areaId(area.getId())
                .restorationRegion(area.getRestorationRegion().getName())
                .startDate(area.getStartDate())
                .endDate(area.getEndDate())
                .currentStatus(AreaDetailResponse.StatusInfo.builder()
                        .name(area.getLevel().getName())
                        .description(area.getLevel().getDescription())
                        .build())
                .areaSize(area.getAreaSize())
                .avgDepth(area.getDepth())
                .habitatType(area.getHabitat().getName())
                .lat(area.getLat())
                .lon(area.getLon())
                .attachmentStatus(area.getAttachmentStatus() != null ?
                        area.getAttachmentStatus().getName() : null)
                .build();
    }

    private AreaDetailResponse.StatusTab buildStatus(
            ProjectArea area,
            List<MethodDistributionProjection> distributions,
            List<WorkHistoryPointProjection> history,
            List<TransplantItemProjection> speciesItems,
            LocalDate lastWorkDate,
            Double totalTransplantArea
    ) {
        //  방식별 분포
        Map<String, Double> distributionMap = new HashMap<>();
        long totalCount = distributions.stream().mapToLong(MethodDistributionProjection::getCount).sum();

        distributions.forEach(d -> {
            double ratio = totalCount == 0 ? 0 : (double) d.getCount() / totalCount * 100;
            distributionMap.put(d.getMethodName().getName(), Math.round(ratio * 10.0) / 10.0);
        });

        // 종별 리스트 생성

        List<AreaDetailResponse.TransplantItem> speciesList = speciesItems.stream()
                .map(p -> {

                    TransplantMethod method = p.getMethodName();

                    return AreaDetailResponse.TransplantItem.builder()
                        .speciesName(p.getSpeciesName())
                        .method(method.getName())
                        .methodDesc(method.getDescription())
                            .quantity(p.getTotalCount())
                            .unit(method.getUnit())
                            .build();
                }
                )
                .toList();


        return AreaDetailResponse.StatusTab.builder()
                .speciesList(speciesList)
                .methodDistribution(distributionMap)
                .accumulated(AreaDetailResponse.AccumulatedStats.builder()
                        .totalAreaSize(totalTransplantArea)
                        .totalWorkCount((int) totalCount)
                        .lastWorkDate(lastWorkDate)
                        .build())
                .workHistoryChart(TimeSeriesChartDto.builder()
                        .labels(history.stream().map(WorkHistoryPointProjection::getMonth).toList())
                        .values(history.stream().map(h -> h.getCount().doubleValue()).toList())
                        .unit("회")
                        .build())
                .build();
    }

    private AreaDetailResponse.EcologyTab buildEcology(ProjectArea area, List<GrowthLog> repLogs, List<MethodAttachmentStatusProjection> methodStatuses) {

        // DB에서 가져온 종별 최신 상태를 DTO로 매핑
        List<AreaDetailResponse.AttachmentStatus> attachmentStatuses = methodStatuses.stream()
                .map(p -> {
                    TransplantMethod method = TransplantMethod.valueOf(p.getMethodName());
                    String statusKorean = SpeciesAttachmentStatus.valueOf(p.getStatusName()).getName();
                    return AreaDetailResponse.AttachmentStatus.builder()
                            .method(method.getName())
                            .status(statusKorean)
                            .build();
                })
                .toList();

        String targetSpeciesName = "미지정";
        Long targetSpeciesId = null;
        if (area.getRepresentativeSpecies() != null) {
            targetSpeciesName = area.getRepresentativeSpecies().getName();
            targetSpeciesId = area.getRepresentativeSpecies().getId();
        }
        return AreaDetailResponse.EcologyTab.builder()
                .attachmentStatuses(attachmentStatuses)
                .survivalStatus(area.getAttachmentStatus() != null ? area.getAttachmentStatus().getName() : "안정")
                .representativeGrowthChart(TimeSeriesChartDto.builder()
                        .labels(repLogs.stream().map(GrowthLog::getRecordDate).toList())
                        .values(repLogs.stream().map(GrowthLog::getGrowthLength).toList())
                        .targetSpecies(targetSpeciesName)
                        .targetSpeciesId(targetSpeciesId)
                        .unit("mm/월")
                        .build())
                .build();
    }

    private AreaDetailResponse.EnvironmentTab buildEnvironment( EnvironmentSummaryProjection summary, List<TemperaturePointProjection> tempHistory) {
        return AreaDetailResponse.EnvironmentTab.builder()
                .last3MonthsSummary(AreaDetailResponse.EnvironmentSummary.builder()
                        .visibility(toMarineStatusName(summary != null ? summary.getVisibility() : null))
                        .current(toMarineStatusName(summary != null ? summary.getCurrent() : null))
                        .surge(toMarineStatusName(summary != null ? summary.getSurge() : null))
                        .wave(toMarineStatusName(summary != null ? summary.getWave() : null))
                        .build())

                .temperatureChart(TimeSeriesChartDto.builder()
                        .labels(tempHistory.stream().map(TemperaturePointProjection::getRecordDate).toList())
                        .values(tempHistory.stream().map(TemperaturePointProjection::getTemperature).toList())
                        .unit("℃")
                        .build())
                .build();
    }
    private String toMarineStatusName(String enumName) {
        if (enumName == null) return "데이터 없음";
        return MarineStatus.valueOf(enumName).getName(); // GOOD/NORMAL/POOR -> 좋음/보통/나쁨
    }

    private AreaDetailResponse.PhotoTab buildPhotos(List<MediaPointProjection> mediaHistory) {
        // 1. 복원 전/후 사진 추출 (단건)
        String before = mediaHistory.stream()
                .filter(m -> m.getCategory() == MediaCategory.BEFORE)
                .map(MediaPointProjection::getMediaUrl).findFirst().orElse(null);

        String after = mediaHistory.stream()
                .filter(m -> m.getCategory() == MediaCategory.AFTER)
                .map(MediaPointProjection::getMediaUrl).findFirst().orElse(null);

        // 2. 타임라인 리스트 생성
        List<AreaDetailResponse.TimelinePhoto> timeline = mediaHistory.stream()
                .filter(m -> m.getCategory() == MediaCategory.TIMELINE)
                .map(m -> AreaDetailResponse.TimelinePhoto.builder()
                        .url(m.getMediaUrl())
                        .label(m.getRecordDate().format(DateTimeFormatter.ofPattern("yyyy.MM")))
                        .caption(m.getCaption())
                        .build())
                .toList();

        return AreaDetailResponse.PhotoTab.builder()
                .beforeUrl(before)
                .afterUrl(after)
                .timeline(timeline)
                .build();    }
}