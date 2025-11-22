package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
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
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "submitted_at", nullable = false)
    private java.time.LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(name = "author_email", length = 200)
    private String authorEmail;

    @Column(name = "attachment_count")
    @Builder.Default
    private Integer attachmentCount = 0;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    // 관계 매핑
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private BasicEnv basicEnv;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Participants participants;

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

    public void setParticipants(Participants participants) {
        this.participants = participants;
        if (participants != null) {
            participants.updateSubmission(this);
        }
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
}
