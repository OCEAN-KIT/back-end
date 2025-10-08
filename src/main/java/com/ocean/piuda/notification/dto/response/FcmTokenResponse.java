package com.ocean.piuda.notification.dto.response;


import com.ocean.piuda.notification.entity.FcmToken;
import lombok.Builder;

/**
 * 하나의 FCM 토큰 반환
 */
@Builder
public record FcmTokenResponse(
        Long id, String token
){
    public static FcmTokenResponse from(FcmToken e) {
        return FcmTokenResponse.builder()
                .id(e.getId())
                .token(e.getToken())
                .build();
    }

}
