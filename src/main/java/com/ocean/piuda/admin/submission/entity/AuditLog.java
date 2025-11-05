package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.AuditAction;
import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(length = 500)
    private String comment;

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
