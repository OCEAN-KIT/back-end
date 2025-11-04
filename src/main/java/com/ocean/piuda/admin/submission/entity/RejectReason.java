package com.ocean.piuda.admin.submission.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "reject_reason")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RejectReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reason_id")
    private Long reasonId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "template_code", length = 100)
    private String templateCode;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "rejected_by", nullable = false, length = 100)
    private String rejectedBy;

    @Column(name = "rejected_at")
    @Builder.Default
    private LocalDateTime rejectedAt = LocalDateTime.now();

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
