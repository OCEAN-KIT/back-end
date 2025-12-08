package com.ocean.piuda.dashboard.dto.response;

import com.ocean.piuda.dashboard.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Builder
@Getter
public class AreaDetailResponse {

    private Long id;
    private BasicInfo basic;

    private List<TransplantDto> transplants;
    private List<GrowthLogDto> growthLogs;
    private List<WaterLogDto> waterLogs;
    private BiodiversityDto biodiversity;
    private List<MediaDto> mediaLogs;

    @Builder @Getter
    public static class BasicInfo {
        private String name;
        private LocalDate startDate;
        private String habitat;
        private Double depth;
        private Double areaSize;
        private String stage;
        private Double lat;
        private Double lon;
    }

    // ... (TransplantDto, GrowthLogDto, WaterLogDto, BiodiversityDto, MediaDto 등 내부 클래스 기존과 동일)
    @Builder @Getter public static class TransplantDto { String species; Integer count; Double area; }
    @Builder @Getter public static class GrowthLogDto { LocalDate date; Double attachmentRate; Double survivalRate; Double growthLength; }
    @Builder @Getter public static class WaterLogDto { LocalDate date; Double temperature; Double dissolvedOxygen; Double nutrient; }
    @Builder @Getter public static class BiodiversityDto { Stat before; Stat after; @Builder @Getter public static class Stat { Integer fishCount; Integer invertCount; Double shannonIndex; } }
    @Builder @Getter public static class MediaDto { LocalDate date; String url; String caption; }

    public static AreaDetailResponse fromEntity(ProjectArea entity) {
        return AreaDetailResponse.builder()
                .id(entity.getId())
                .basic(BasicInfo.builder()
                        .name(entity.getName())
                        .startDate(entity.getStartDate())
                        .habitat(entity.getHabitat() != null ? entity.getHabitat().getDescription() : null)
                        .depth(entity.getDepth())
                        .areaSize(entity.getAreaSize())
                        .stage(entity.getStatus() != null ? entity.getStatus().getDescription() : null)
                        // [수정] Entity의 캡슐화된 메서드 사용
                        .lat(entity.getLat())
                        .lon(entity.getLon())
                        .build())

                .transplants(entity.getTransplants().stream()
                        .map(t -> TransplantDto.builder().species(t.getSpeciesName()).count(t.getCount()).area(t.getAreaSize()).build()).toList())
                .growthLogs(entity.getGrowthLogs().stream()
                        .sorted(Comparator.comparing(GrowthLog::getRecordDate))
                        .map(g -> GrowthLogDto.builder().date(g.getRecordDate()).attachmentRate(g.getAttachmentRate()).survivalRate(g.getSurvivalRate()).growthLength(g.getGrowthLength()).build()).toList())
                .waterLogs(entity.getWaterLogs().stream()
                        .sorted(Comparator.comparing(WaterLog::getRecordDate))
                        .map(w -> WaterLogDto.builder().date(w.getRecordDate()).temperature(w.getTemperature()).dissolvedOxygen(w.getDissolvedOxygen()).nutrient(w.getNutrient()).build()).toList())
                .biodiversity(entity.getBiodiversity() != null ? BiodiversityDto.builder()
                        .before(BiodiversityDto.Stat.builder().fishCount(entity.getBiodiversity().getFishCountBefore()).invertCount(entity.getBiodiversity().getInvertCountBefore()).shannonIndex(entity.getBiodiversity().getShannonIndexBefore()).build())
                        .after(BiodiversityDto.Stat.builder().fishCount(entity.getBiodiversity().getFishCountAfter()).invertCount(entity.getBiodiversity().getInvertCountAfter()).shannonIndex(entity.getBiodiversity().getShannonIndexAfter()).build())
                        .build() : null)
                .mediaLogs(entity.getMediaLogs().stream()
                        .sorted(Comparator.comparing(MediaLog::getRecordDate))
                        .map(m -> MediaDto.builder().date(m.getRecordDate()).url(m.getMediaUrl()).caption(m.getCaption()).build()).toList())
                .build();
    }
}