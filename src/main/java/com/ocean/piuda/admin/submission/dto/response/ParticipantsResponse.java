package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.ParticipantRole;
import com.ocean.piuda.admin.submission.entity.Participants;

public record ParticipantsResponse(
        String leaderName,
        Integer participantCount,
        ParticipantRole role
) {
    public static ParticipantsResponse from(Participants participants) {
        if (participants == null) return null;
        return new ParticipantsResponse(
                participants.getLeaderName(),
                participants.getParticipantCount(),
                participants.getRole()
        );
    }
}
