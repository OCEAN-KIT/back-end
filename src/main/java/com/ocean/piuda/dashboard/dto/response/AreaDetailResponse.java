package com.ocean.piuda.dashboard.dto.response;

import com.ocean.piuda.dashboard.dto.TimeSeriesChartDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class AreaDetailResponse {
    private Long id;
    private OverviewTab overview;
    private StatusTab status;
    private EcologyTab ecology;
    private EnvironmentTab environment;
    private PhotoTab photos;

    /**
     * 개요탭
     */
    @Builder
    @Getter
    public static class OverviewTab {
        private String name;
        private Long areaId;
        private String restorationRegion; // "포항" / "울진"
        private LocalDate startDate;
        private LocalDate endDate;
        private StatusInfo currentStatus;  // 단계 이름 + 설명 객체
        private Double areaSize;
        private Double avgDepth;
        private String habitatType;       // "암반" / "혼합" 등

        private Double lat;
        private Double lon;
        private String attachmentStatus; // "안정", "일부 감소" 등
    }

    /**
     * 개요탭
     */
    @Builder
    @Getter
    public static class StatusInfo {
        private String name;        // 예: "관측"
        private String description; // 예: "초기 상태 기록"
    }

    /**
     * 현황 탭
     */
    @Builder
    @Getter
    public static class StatusTab {
        private List<TransplantItem> speciesList;
        private Map<String, Double> methodDistribution; // 이식 방식별 비율 (%)
        private AccumulatedStats accumulated;           // 누적 집계
        private TimeSeriesChartDto workHistoryChart;    // 최근 3개월 작업 횟수 그래프
    }

    @Builder
    @Getter
    public static class TransplantItem {
        private String speciesName;
        private String method;      // 예: "로프"
        private String methodDesc;  // 예: "종묘줄을 로프에 고정하여 이식"
        private Long quantity;   // 수량
        private String unit;     // 수랑의 단위 ("줄", "m", "기", "지점")
    }

    @Builder
    @Getter
    public static class AccumulatedStats {
        private Double totalAreaSize;
        private Integer totalWorkCount;
        private LocalDate lastWorkDate;
    }

    /**
     * 생태 반응 탭
     */
    @Builder
    @Getter
    public static class EcologyTab {
        private List<AttachmentStatus> attachmentStatuses;
        private String areaAttachmentStatus; // "안정", "일부 감소", "불안정"
        private TimeSeriesChartDto representativeGrowthChart; // 성장 추이 그래프
    }

    @Builder
    @Getter
    public static class AttachmentStatus {
        private String method; // 이식 방식 명칭
        private String status; // "양호", "보통", "미흡"
    }

    /**
     * 환경 탭
     */
    @Builder
    @Getter
    public static class EnvironmentTab {
        private EnvironmentSummary last3MonthsSummary;
        private TimeSeriesChartDto temperatureChart; // 수온 시계열
    }

    @Builder
    @Getter
    public static class EnvironmentSummary {
        private String visibility; // 시야 (좋음/보통/나쁨)
        private String current;    // 조류
        private String surge;      // 서지
        private String wave;       // 파도
    }

    /**
     * 사진 탭
     */
    @Builder
    @Getter
    public static class PhotoTab {
        private String beforeUrl;
        private String afterUrl;
        private List<TimelinePhoto> timeline;
    }

    @Builder
    @Getter
    public static class TimelinePhoto {
        private String url;
        private String label; // "2025.07" 또는 "3분기"
        private String caption;
    }


}