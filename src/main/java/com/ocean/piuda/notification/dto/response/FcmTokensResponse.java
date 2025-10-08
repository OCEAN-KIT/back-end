package com.ocean.piuda.notification.dto.response;

import com.ocean.piuda.notification.entity.FcmToken;
import lombok.Builder;

import java.util.List;

/**
 * 여러 FCM 토큰 한 번에 반환
 */
@Builder
public record FcmTokensResponse(
        List<FcmTokenResponse> tokens
) {
    public static FcmTokensResponse fromEntities(List<FcmToken> entities) {
        return FcmTokensResponse.builder()
                .tokens(entities.stream().map(FcmTokenResponse::from).toList())
                .build();
    }

}