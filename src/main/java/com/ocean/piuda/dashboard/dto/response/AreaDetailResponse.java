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

    // Row-Oriented Lists (Standard REST)
    private List<TransplantDto> transplants;
    private List<GrowthLogDto> growthLogs;
    private List<WaterLogDto> waterLogs;
    private BiodiversityDto biodiversity;
    private List<MediaDto> mediaLogs;

    // --- Inner DTOs ---

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

    @Builder @Getter
    public static class TransplantDto {
        private String species;
        private Integer count;
        private Double area;
    }

    @Builder @Getter
    public static class GrowthLogDto {
        private LocalDate date;
        private Double attachmentRate;
        private Double survivalRate;
        private Double growthLength;
    }

    @Builder @Getter
    public static class WaterLogDto {
        private LocalDate date;
        private Double temperature;
        private Double dissolvedOxygen;
        private Double nutrient;
    }

    @Builder @Getter
    public static class BiodiversityDto {
        private Stat before;
        private Stat after;

        @Builder @Getter
        public static class Stat {
            private Integer fishCount;
            private Integer invertCount;
            private Double shannonIndex;
        }
    }

    @Builder @Getter
    public static class MediaDto {
        private LocalDate date;
        private String url;
        private String caption;
    }

    // --- Converter ---
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
                        .lat(entity.getLat())
                        .lon(entity.getLon())
                        .build())

                .transplants(entity.getTransplants().stream()
                        .map(t -> TransplantDto.builder()
                                .species(t.getSpeciesName())
                                .count(t.getCount())
                                .area(t.getAreaSize())
                                .build())
                        .toList())

                .growthLogs(entity.getGrowthLogs().stream()
                        .sorted(Comparator.comparing(GrowthLog::getRecordDate))
                        .map(g -> GrowthLogDto.builder()
                                .date(g.getRecordDate())
                                .attachmentRate(g.getAttachmentRate())
                                .survivalRate(g.getSurvivalRate())
                                .growthLength(g.getGrowthLength())
                                .build())
                        .toList())

                .waterLogs(entity.getWaterLogs().stream()
                        .sorted(Comparator.comparing(WaterLog::getRecordDate))
                        .map(w -> WaterLogDto.builder()
                                .date(w.getRecordDate())
                                .temperature(w.getTemperature())
                                .dissolvedOxygen(w.getDissolvedOxygen())
                                .nutrient(w.getNutrient())
                                .build())
                        .toList())

                .biodiversity(entity.getBiodiversity() != null ? BiodiversityDto.builder()
                        .before(BiodiversityDto.Stat.builder()
                                .fishCount(entity.getBiodiversity().getFishCountBefore())
                                .invertCount(entity.getBiodiversity().getInvertCountBefore())
                                .shannonIndex(entity.getBiodiversity().getShannonIndexBefore())
                                .build())
                        .after(BiodiversityDto.Stat.builder()
                                .fishCount(entity.getBiodiversity().getFishCountAfter())
                                .invertCount(entity.getBiodiversity().getInvertCountAfter())
                                .shannonIndex(entity.getBiodiversity().getShannonIndexAfter())
                                .build())
                        .build() : null)

                .mediaLogs(entity.getMediaLogs().stream()
                        .sorted(Comparator.comparing(MediaLog::getRecordDate))
                        .map(m -> MediaDto.builder()
                                .date(m.getRecordDate())
                                .url(m.getMediaUrl())
                                .caption(m.getCaption())
                                .build())
                        .toList())
                .build();
    }
}