package com.ocean.piuda.admin.submission.entity;

import com.ocean.piuda.admin.common.enums.ParticipantRole;
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

    @Column(name = "participant_count")
    @Builder.Default
    private Integer participantCount = 1;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ParticipantRole role = ParticipantRole.CITIZEN_DIVER;

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
