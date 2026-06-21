package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.site.entity.SiteNameOption;
import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 스냅샷: 실제 화면에 표시될 이름
     * 직접 입력한 현장명 또는 선택된 현장 옵션의 이름을 저장합니다.
     */
    @Column(name = "site_name", nullable = false, length = 200)
    private String siteName;

    /**
     * 현장 명칭 마스터 데이터 연동.
     * 사용자가 직접 입력한 경우 null 입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_option_id")
    private SiteNameOption siteNameOption;

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

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    /**
     * 실제 유저 데이터.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 유저의 탈퇴/정보 변경 이후에도 기록 당시 정보를 보존하기 위한 스냅샷 필드.
     */
    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(name = "author_email", length = 200)
    private String authorEmail;

    @Column(name = "attachment_count", nullable = false)
    @Builder.Default
    private Integer attachmentCount = 0;

    @Column(name = "work_description", columnDefinition = "TEXT")
    private String workDescription;

    @Column(name = "admin_memo", columnDefinition = "TEXT")
    private String adminMemo;

    @Column(name = "participant_names", columnDefinition = "TEXT")
    private String participantNames;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private BasicEnv basicEnv;

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

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private RejectReason rejectReason;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();

    /**
     * 승인.
     *
     * 현재 정책상 SUBMITTED 상태만 APPROVED 로 전이될 수 있습니다.
     * APPROVED / REJECTED / DELETED 상태는 최종 상태로 취급합니다.
     */
    public void approve() {
        ensureSubmitted("승인");
        this.status = SubmissionStatus.APPROVED;
    }

    /**
     * 반려.
     *
     * 현재 정책상 SUBMITTED 상태만 REJECTED 로 전이될 수 있습니다.
     * 승인 후 재반려, 반려 후 재승인은 허용하지 않습니다.
     */
    public void reject() {
        ensureSubmitted("반려");
        this.status = SubmissionStatus.REJECTED;
    }

    private void ensureSubmitted(String actionName) {
        if (this.status != SubmissionStatus.SUBMITTED) {
            throw new IllegalStateException(
                    String.format("SUBMITTED 상태만 %s 가능합니다. 현재 상태: %s", actionName, this.status)
            );
        }
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