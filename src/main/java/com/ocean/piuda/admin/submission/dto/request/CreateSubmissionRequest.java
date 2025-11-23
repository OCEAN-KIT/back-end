package com.ocean.piuda.admin.submission.dto.request;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.CurrentState;
import com.ocean.piuda.admin.common.enums.ParticipantRole;
import com.ocean.piuda.admin.common.enums.Weather;
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
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubmissionRequest {

    @NotBlank(message = "현장명은 필수입니다")
    private String siteName;

    @NotNull(message = "활동유형은 필수입니다")
    private ActivityType activityType;

    private LocalDateTime submittedAt; // null이면 현재 시간 사용

    @NotBlank(message = "작성자명은 필수입니다")
    private String authorName;

    private String authorEmail;

    private String feedbackText;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Valid
    private BasicEnvDto basicEnv;

    @Valid
    private ParticipantsDto participants;

    @Valid
    @NotNull(message = "활동 정보는 필수입니다")
    private ActivityDto activity;

    @Valid
    private List<AttachmentDto> attachments;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicEnvDto {
        private LocalDate recordDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Float waterTempC;
        private Float visibilityM;
        private Float depthM;
        private CurrentState currentState;
        private Weather weather;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantsDto {
        private String leaderName;
        private Integer participantCount;
        private ParticipantRole role;
    }

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
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentDto {
        private String fileName;
        private String fileUrl;
        private String mimeType;
        private Integer fileSize;
    }
}

