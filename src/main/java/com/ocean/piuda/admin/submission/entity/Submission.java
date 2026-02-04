package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.StructureType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "submission")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Submission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "site_name", nullable = false, length = 200)
    private String siteName;

    @Enumerated(EnumType.STRING)
    @Column(name = "structure_type", nullable = false)
    @Builder.Default
    private StructureType structureType = StructureType.OTHER;

    @Column(name = "custom_structure_type", length = 200)
    private String customStructureType;  // 구조물 유형 커스텀 텍스트 (OTHER일 때 사용)

    @Column(name = "record_date", nullable = false)
    @Builder.Default
    private LocalDate recordDate = LocalDate.now();

    @Column(name = "diving_round", nullable = false)
    @Min(1)
    @Max(5)
    private Integer divingRound;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();  // 생성 시 제출 시점 기록

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(name = "author_email", length = 200)
    private String authorEmail;

    @Column(name = "attachment_count")
    @Builder.Default
    private Integer attachmentCount = 0;

    @Column(name = "work_description", columnDefinition = "TEXT")
    private String workDescription;  // 작업 내용 (기존 feedbackText)

    @Column(name = "admin_memo", columnDefinition = "TEXT")
    private String adminMemo;  // 관리자 검수 메모

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "participant_names", columnDefinition = "TEXT")
    private String participantNames;


    // 관계 매핑
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private BasicEnv basicEnv;

    // 작업 유형별 Activity (조건부 1:1)
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityTransplant activityTransplant;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityGrazerRemoval activityGrazerRemoval;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivitySubstrateImprovement activitySubstrateImprovement;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityMonitoring activityMonitoring;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityMarineCleanup activityMarineCleanup;

    // 하위 호환성을 위한 기존 Activity (deprecated)
    @Deprecated
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Activity activity;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private RejectReason rejectReason;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();



    // 비즈니스 메서드
    public void updateStatus(SubmissionStatus status) {
        this.status = status;
    }

    /**
     * 승인 (SUBMITTED -> APPROVED)
     */
    public void approve() {
        if (this.status != SubmissionStatus.SUBMITTED) {
            throw new IllegalStateException("SUBMITTED 상태만 승인 가능합니다. 현재 상태: " + this.status);
        }
        this.status = SubmissionStatus.APPROVED;
    }

    /**
     * 반려 (SUBMITTED -> REJECTED)
     */
    public void reject() {
        if (this.status != SubmissionStatus.SUBMITTED) {
            throw new IllegalStateException("SUBMITTED 상태만 반려 가능합니다. 현재 상태: " + this.status);
        }
        this.status = SubmissionStatus.REJECTED;
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.updateSubmission(this);
        this.attachmentCount = attachments.size();
    }

    public void addAuditLog(AuditLog auditLog) {
        auditLogs.add(auditLog);
        auditLog.updateSubmission(this);
    }

    public void setBasicEnv(BasicEnv basicEnv) {
        this.basicEnv = basicEnv;
        if (basicEnv != null) {
            basicEnv.updateSubmission(this);
        }
    }

    public void updateParticipantNames(String participantNames) {
        this.participantNames = participantNames;
    }


    public void setActivity(Activity activity) {
        this.activity = activity;
        if (activity != null) {
            activity.updateSubmission(this);
        }
    }

    public void setRejectReason(RejectReason rejectReason) {
        this.rejectReason = rejectReason;
        if (rejectReason != null) {
            rejectReason.updateSubmission(this);
        }
    }

    public void setActivityTransplant(ActivityTransplant activityTransplant) {
        this.activityTransplant = activityTransplant;
        if (activityTransplant != null) {
            activityTransplant.updateSubmission(this);
        }
    }

    public void setActivityGrazerRemoval(ActivityGrazerRemoval activityGrazerRemoval) {
        this.activityGrazerRemoval = activityGrazerRemoval;
        if (activityGrazerRemoval != null) {
            activityGrazerRemoval.updateSubmission(this);
        }
    }

    public void setActivitySubstrateImprovement(ActivitySubstrateImprovement activitySubstrateImprovement) {
        this.activitySubstrateImprovement = activitySubstrateImprovement;
        if (activitySubstrateImprovement != null) {
            activitySubstrateImprovement.updateSubmission(this);
        }
    }

    public void setActivityMonitoring(ActivityMonitoring activityMonitoring) {
        this.activityMonitoring = activityMonitoring;
        if (activityMonitoring != null) {
            activityMonitoring.updateSubmission(this);
        }
    }

    public void setActivityMarineCleanup(ActivityMarineCleanup activityMarineCleanup) {
        this.activityMarineCleanup = activityMarineCleanup;
        if (activityMarineCleanup != null) {
            activityMarineCleanup.updateSubmission(this);
        }
    }
}
