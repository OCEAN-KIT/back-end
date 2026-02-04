package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.*;
import com.ocean.piuda.admin.submission.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record SubmissionDetailResponse(
        Long submissionId,
        String siteName,
        Long siteNameOptionId,
        ActivityType activityType,
        LocalDateTime submittedAt,
        SubmissionStatus status,
        String authorName,
        String authorEmail,
        Integer attachmentCount,
        String feedbackText,  // API 하위 호환성을 위해 필드명 유지 (내부적으로는 workDescription 사용)
        BigDecimal latitude,
        BigDecimal longitude,
        BasicEnvResponse basicEnv,
        ParticipantsResponse participants,

        TransplantResponse transplantActivity,
        GrazerRemovalResponse grazerRemovalActivity,
        SubstrateImprovementResponse substrateImprovementActivity,
        MonitoringResponse monitoringActivity,
        MarineCleanupResponse marineCleanupActivity,

        List<AttachmentResponse> attachments,
        String rejectReason,
        List<AuditLogResponse> auditLogs,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static SubmissionDetailResponse from(Submission submission) {
        List<AttachmentResponse> attachments = null;
        if (submission.getAttachments() != null) {
            attachments = submission.getAttachments().stream()
                    .map(AttachmentResponse::from)
                    .collect(Collectors.toList());
        }

        List<AuditLogResponse> auditLogs = null;
        if (submission.getAuditLogs() != null) {
            auditLogs = submission.getAuditLogs().stream()
                    .map(AuditLogResponse::from)
                    .collect(Collectors.toList());
        }

        String rejectReason = null;
        if (submission.getRejectReason() != null) {
            rejectReason = submission.getRejectReason().getMessage();
        }

        return new SubmissionDetailResponse(
                submission.getSubmissionId(),
                submission.getSiteName(),
                submission.getSiteNameOption() != null ? submission.getSiteNameOption().getId() : null,
                submission.getActivityType(),
                submission.getSubmittedAt(),
                submission.getStatus(),
                submission.getAuthorName(),
                submission.getAuthorEmail(),
                submission.getAttachmentCount(),
                submission.getWorkDescription(),
                submission.getLatitude(),
                submission.getLongitude(),
                BasicEnvResponse.from(submission.getBasicEnv()),
                new ParticipantsResponse(submission.getParticipantNames()),

                // Activity Type별 매핑 (존재하는 것만 변환, 나머지는 null)
                TransplantResponse.from(submission.getActivityTransplant()),
                GrazerRemovalResponse.from(submission.getActivityGrazerRemoval()),
                SubstrateImprovementResponse.from(submission.getActivitySubstrateImprovement()),
                MonitoringResponse.from(submission.getActivityMonitoring()),
                MarineCleanupResponse.from(submission.getActivityMarineCleanup()),

                attachments,
                rejectReason,
                auditLogs,
                submission.getCreatedAt(),
                submission.getModifiedAt()
        );
    }


    //  Inner Record Classes (각 활동 유형별 응답 DTO)
    public record TransplantResponse(
            TransplantSpeciesType speciesType,
            TransplantLocationType locationType,
            TransplantMethodType methodType,
            String scale,
            HealthStatus healthStatus
    ) {
        public static TransplantResponse from(ActivityTransplant entity) {
            if (entity == null) return null;
            return new TransplantResponse(
                    entity.getSpeciesType(),
                    entity.getLocationType(),
                    entity.getMethodType(),
                    entity.getScale(),
                    entity.getHealthStatus()
            );
        }
    }

    public record GrazerRemovalResponse(
            List<GrazerSpeciesType> targetSpecies,
            DensityLevel densityBeforeWork,
            WorkScope workScope,
            String note,
            String collectionAmount
    ) {
        public static GrazerRemovalResponse from(ActivityGrazerRemoval entity) {
            if (entity == null) return null;
            return new GrazerRemovalResponse(
                    entity.getTargetSpecies(),
                    entity.getDensityBeforeWork(),
                    entity.getWorkScope(),
                    entity.getNote(),
                    entity.getCollectionAmount()
            );
        }
    }

    public record SubstrateImprovementResponse(
            SubstrateTargetType targetType,
            String workScope,
            String substrateState
    ) {
        public static SubstrateImprovementResponse from(ActivitySubstrateImprovement entity) {
            if (entity == null) return null;
            return new SubstrateImprovementResponse(
                    entity.getTargetType(),
                    entity.getWorkScope(),
                    entity.getSubstrateState()
            );
        }
    }

    public record MonitoringResponse(
            String entryCoordinate,
            String exitCoordinate,
            String direction,
            TerrainType terrain,
            BarrenExtent barrenExtent,
            DensityLevel grazerDistribution,
            List<RockFeature> rockFeatures,
            Suitability suitability,
            String seaweedIdNumber,
            SeaweedHealthStatus seaweedHealthStatus,
            String leafLength,
            String maxLeafWidth
    ) {
        public static MonitoringResponse from(ActivityMonitoring entity) {
            if (entity == null) return null;
            return new MonitoringResponse(
                    entity.getEntryCoordinate(),
                    entity.getExitCoordinate(),
                    entity.getDirection(),
                    entity.getTerrain(),
                    entity.getBarrenExtent(),
                    entity.getGrazerDistribution(),
                    entity.getRockFeatures(),
                    entity.getSuitability(),
                    entity.getSeaweedIdNumber(),
                    entity.getSeaweedHealthStatus(),
                    entity.getLeafLength(),
                    entity.getMaxLeafWidth()
            );
        }
    }

    public record MarineCleanupResponse(
            List<WasteType> wasteTypes,
            CleanupMethodType method,
            String collectionAmount,
            UncollectedScale uncollectedScale
    ) {
        public static MarineCleanupResponse from(ActivityMarineCleanup entity) {
            if (entity == null) return null;
            return new MarineCleanupResponse(
                    entity.getWasteTypes(),
                    entity.getMethod(),
                    entity.getCollectionAmount(),
                    entity.getUncollectedScale()
            );
        }
    }
}
