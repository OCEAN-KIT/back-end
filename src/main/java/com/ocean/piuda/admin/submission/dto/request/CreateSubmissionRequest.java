package com.ocean.piuda.admin.submission.dto.request;

import com.ocean.piuda.admin.common.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubmissionRequest {

    @NotBlank(message = "현장명은 필수입니다")
    private String siteName;

    // 구조물 유형 (선택사항, null이면 OTHER로 기본값 설정)
    private StructureType structureType;
    
    // 구조물 유형 커스텀 텍스트 (structureType이 OTHER일 때 사용 가능)
    private String customStructureType;

    private LocalDate recordDate;  // null이면 현재 날짜

    @NotNull(message = "다이빙 회차는 필수입니다")
    private Integer divingRound;  // 1~5

    @NotNull(message = "작업 유형은 필수입니다")
    private ActivityType activityType;

    @NotBlank(message = "작성자명은 필수입니다")
    private String authorName;

    private String authorEmail;

    private String workDescription;  // 작업 내용

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Valid
    @NotNull(message = "기본 환경 정보는 필수입니다")
    private BasicEnvDto basicEnv;

    @Valid
    private ParticipantsDto participants;


    /**
     * TRANSPLANT (이식) 작업 시 사용되는 필드
     * activityType이 TRANSPLANT일 때만 필수
     */
    private TransplantActivityDto transplantActivity;

    /**
     * GRAZER_REMOVAL (조식동물 작업) 작업 시 사용되는 필드
     * activityType이 GRAZER_REMOVAL일 때만 필수
     * 
     * @Valid 제거: ActivityValidator에서만 검증 (다른 activity DTO와 충돌 방지)
     */
    private GrazerRemovalActivityDto grazerRemovalActivity;

    /**
     * SUBSTRATE_IMPROVEMENT (부착기질 개선) 작업 시 사용되는 필드
     * activityType이 SUBSTRATE_IMPROVEMENT일 때만 필수
     * 
     * @Valid 제거: ActivityValidator에서만 검증 (다른 activity DTO와 충돌 방지)
     */
    private SubstrateImprovementActivityDto substrateImprovementActivity;

    /**
     * MONITORING (모니터링) 작업 시 사용되는 필드
     * activityType이 MONITORING일 때만 필수
     * 
     * @Valid 제거: ActivityValidator에서만 검증 (다른 activity DTO와 충돌 방지)
     */
    private MonitoringActivityDto monitoringActivity;

    /**
     * MARINE_CLEANUP (해양정화) 작업 시 사용되는 필드
     * activityType이 MARINE_CLEANUP일 때만 필수
     * 
     * @Valid 제거: ActivityValidator에서만 검증 (다른 activity DTO와 충돌 방지)
     */
    private MarineCleanupActivityDto marineCleanupActivity;

    @Valid
    private List<AttachmentDto> attachments;

    // 하위 호환성을 위한 기존 필드 (deprecated)
    @Deprecated
    private LocalDateTime submittedAt;

    @Deprecated
    @Valid
    private ActivityDto activity;

    // === 내부 DTO 클래스들 ===

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicEnvDto {
        @NotNull(message = "기록 날짜는 필수입니다")
        private LocalDate recordDate;

        @NotNull(message = "평균 수심은 필수입니다")
        private Double avgDepthM;

        @NotNull(message = "최대 수심은 필수입니다")
        private Double maxDepthM;

        @NotNull(message = "수온은 필수입니다")
        private Double waterTempC;

        @NotNull(message = "시야 상태는 필수입니다")
        private MarineCondition visibilityStatus;

        @NotNull(message = "파도 상태는 필수입니다")
        private MarineCondition waveStatus;

        @NotNull(message = "서지 상태는 필수입니다")
        private MarineCondition surgeStatus;

        @NotNull(message = "조류 상태는 필수입니다")
        private MarineCondition currentStatus;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantsDto {
        private String participantNames;  // comma-separated 또는 JSON 배열
    }

    /**
     * TRANSPLANT (이식) 작업 전용 DTO
     * activityType이 TRANSPLANT일 때만 사용
     * 
     * 필드:
     * - speciesType: 대상 종류 (감태/다시마/곰피/모자반/대황/기타)
     * - locationType: 이식 장소 (어초/암반/기타)
     * - methodType: 이식 방식
     * - scale: 이식 규모 (텍스트)
     * - healthStatus: 건강상태
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransplantActivityDto {
        @NotNull(message = "대상 종류는 필수입니다")
        private TransplantSpeciesType speciesType;

        @NotNull(message = "이식 장소는 필수입니다")
        private TransplantLocationType locationType;

        @NotNull(message = "이식 방식은 필수입니다")
        private TransplantMethodType methodType;

        @NotBlank(message = "이식 규모는 필수입니다")
        private String scale;

        @NotNull(message = "건강상태는 필수입니다")
        private HealthStatus healthStatus;
    }

    /**
     * GRAZER_REMOVAL (조식동물 작업) 전용 DTO
     * activityType이 GRAZER_REMOVAL일 때만 사용
     * 
     * 필드:
     * - targetSpecies: 대상 생물 (복수 선택: 성게/소라/전복/불가사리/기타)
     * - densityBeforeWork: 작업 전 밀도 (LOW/MID/HIGH)
     * - workScope: 작업 범위 (LOCAL/ZONE/WIDE)
     * - note: 보충 설명 (선택)
     * - collectionAmount: 수거량 (텍스트)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrazerRemovalActivityDto {
        @NotNull(message = "대상 생물은 필수입니다")
        private List<GrazerSpeciesType> targetSpecies;

        @NotNull(message = "작업 전 밀도는 필수입니다")
        private DensityLevel densityBeforeWork;

        @NotNull(message = "작업 범위는 필수입니다")
        private WorkScope workScope;

        private String note;  // 보충 설명

        @NotBlank(message = "수거량은 필수입니다")
        private String collectionAmount;
    }

    /**
     * SUBSTRATE_IMPROVEMENT (부착기질 개선) 전용 DTO
     * activityType이 SUBSTRATE_IMPROVEMENT일 때만 사용
     * 
     * 필드:
     * - targetType: 작업 대상 (암반/어초/구조물/기타)
     * - workScope: 작업 범위 (텍스트)
     * - substrateState: 작업 후 기질 상태 (텍스트)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubstrateImprovementActivityDto {
        @NotNull(message = "작업 대상은 필수입니다")
        private SubstrateTargetType targetType;

        @NotBlank(message = "작업 범위는 필수입니다")
        private String workScope;

        @NotBlank(message = "작업 후 기질 상태는 필수입니다")
        private String substrateState;
    }

    /**
     * MONITORING (모니터링) 전용 DTO
     * activityType이 MONITORING일 때만 사용
     * 
     * 필드:
     * - entryCoordinate: 입수 좌표 (텍스트 또는 lat/lon 구조)
     * - exitCoordinate: 출수 좌표 (텍스트 또는 lat/lon 구조)
     * - direction: 진행방위 (텍스트)
     * - terrain: 지형 구성 (enum: ROCK/SAND/MIXED/OTHER)
     * - barrenExtent: 갯녹음 정도 (enum: NONE/ONGOING/SEVERE)
     * - grazerDistribution: 조식동물 분포 (enum: LOW/MID/HIGH)
     * - rockFeatures: 암반 특성 (복수 enum: SMOOTH/CRACKED/CALCAREOUS_ALGAE/MIXED/SEAWEED_VEGETATION)
     * - suitability: 해조 이식 적합성 (enum: SUITABLE/UNSUITABLE)
     * - seaweedIdNumber: 해조류 식별번호 (텍스트)
     * - seaweedHealthStatus: 해조류 생육상태 (enum: A/B/C/D)
     * - precisionMeasurement: 정밀측정 여부 (boolean)
     * - leafLength: 엽장 (텍스트 또는 숫자)
     * - maxLeafWidth: 최대엽폭 (텍스트 또는 숫자)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonitoringActivityDto {
        // 적지조사
        private String entryCoordinate;
        private String exitCoordinate;
        private String direction;
        
        // 지형 구성
        @NotNull(message = "지형 구성은 필수입니다")
        private TerrainType terrain;
        
        // 갯녹음 정도
        @NotNull(message = "갯녹음 정도는 필수입니다")
        private BarrenExtent barrenExtent;
        
        // 조식동물 분포
        @NotNull(message = "조식동물 분포는 필수입니다")
        private DensityLevel grazerDistribution;
        
        // 암반 특성 (복수 선택)
        @NotNull(message = "암반 특성은 최소 1개 이상 선택해야 합니다")
        private List<RockFeature> rockFeatures;
        
        // 해조 이식 적합성
        @NotNull(message = "해조 이식 적합성은 필수입니다")
        private Suitability suitability;
        
        // 해조류 상태
        private String seaweedIdNumber;  // 식별번호
        private SeaweedHealthStatus seaweedHealthStatus;  // 생육상태 (양호/쇠약/탈락)
        
        // 정밀측정 여부
        private Boolean precisionMeasurement;
        private String leafLength;  // 엽장
        private String maxLeafWidth;  // 최대엽폭
    }

    /**
     * MARINE_CLEANUP (해양정화) 전용 DTO
     * activityType이 MARINE_CLEANUP일 때만 사용
     * 
     * 필드:
     * - wasteTypes: 폐기물 유형 (복수 선택: 그물/통발/기타어구/낚시도구/플라스틱/기타)
     * - method: 인양 방식 (HAND/BAG/CRANE)
     * - collectionAmount: 수거량 (텍스트)
     * - uncollectedScale: 미수거 폐기물 규모 (SMALL/MEDIUM/LARGE, 선택)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarineCleanupActivityDto {
        @NotNull(message = "폐기물 유형은 필수입니다")
        private List<WasteType> wasteTypes;

        @NotNull(message = "인양 방식은 필수입니다")
        private CleanupMethodType method;

        @NotBlank(message = "수거량은 필수입니다")
        private String collectionAmount;

        private UncollectedScale uncollectedScale;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentDto {
        private String fileName;
        private String fileUrl;
        private String presignedUrl;  // presigned URL
        private String mimeType;
        private Integer fileSize;
    }

    // 하위 호환성을 위한 기존 DTO (deprecated)
    @Deprecated
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDto {
        @NotNull(message = "활동유형은 필수입니다")
        private ActivityType type;
        private String details;
        private Float collectionAmount;
        private Float durationHours;
        private HealthStatus healthStatus;
        private Float growthCm;
        private NaturalReproductionDto naturalReproduction;
        private SurvivalDto survival;
    }

    @Deprecated
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NaturalReproductionDto {
        private Float radiusM;
        private Float numerator;
        private Float denominator;
    }

    @Deprecated
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurvivalDto {
        private Float dieCount;
        private Float totalCount;
    }
}
