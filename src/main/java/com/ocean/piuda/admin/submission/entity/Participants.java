package com.ocean.piuda.admin.submission.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "participants")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Participants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "leader_name", length = 100)
    private String leaderName;

    @Column(name = "participant_names", columnDefinition = "TEXT")
    private String participantNames;  // comma-separated 또는 JSON 배열

    // 하위 호환성을 위한 기존 필드들 (deprecated)
    @Deprecated
    @Column(name = "participant_count")
    @Builder.Default
    private Integer participantCount = 1;

    @Deprecated
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.ocean.piuda.admin.common.enums.ParticipantRole role = 
        com.ocean.piuda.admin.common.enums.ParticipantRole.CITIZEN_DIVER;

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
