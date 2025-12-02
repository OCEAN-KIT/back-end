package com.ocean.piuda.admin.submission.dto.response;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.HealthGrade;
import com.ocean.piuda.admin.submission.entity.Activity;
import com.ocean.piuda.admin.submission.entity.embeded.NaturalReproduction;
import com.ocean.piuda.admin.submission.entity.embeded.Survival;

public record ActivityResponse(
        ActivityType type,
        String details,
        Float collectionAmount,
        Float durationHours,

        // --- 추가된 필드 ---
        HealthGrade healthGrade,
        Float growthCm,
        NaturalReproductionResponse naturalReproduction,
        SurvivalResponse survival
) {
    public static ActivityResponse from(Activity activity) {
        if (activity == null) return null;
        return new ActivityResponse(
                activity.getType(),
                activity.getDetails(),
                activity.getCollectionAmount(),
                activity.getDurationHours(),

                // 매핑 로직
                activity.getHealthGrade(),
                activity.getGrowthCm(),
                NaturalReproductionResponse.from(activity.getNaturalReproduction()),
                SurvivalResponse.from(activity.getSurvival())
        );
    }

    // --- 내부 레코드 정의 ---
    public record NaturalReproductionResponse(
            Float radiusM,
            Float numerator,
            Float denominator
    ) {
        public static NaturalReproductionResponse from(NaturalReproduction vo) {
            if (vo == null) return null;
            return new NaturalReproductionResponse(vo.getRadiusM(), vo.getNumerator(), vo.getDenominator());
        }
    }

    public record SurvivalResponse(
            Float dieCount,
            Float totalCount
    ) {
        public static SurvivalResponse from(Survival vo) {
            if (vo == null) return null;
            return new SurvivalResponse(vo.getDieCount(), vo.getTotalCount());
        }
    }
}