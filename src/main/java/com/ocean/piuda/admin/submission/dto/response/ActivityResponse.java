package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.submission.entity.Activity;

public record ActivityResponse(
        ActivityType type,
        String details,
        Float collectionAmount,
        Float durationHours
) {
    public static ActivityResponse from(Activity activity) {
        if (activity == null) return null;
        return new ActivityResponse(
                activity.getType(),
                activity.getDetails(),
                activity.getCollectionAmount(),
                activity.getDurationHours()
        );
    }
}
