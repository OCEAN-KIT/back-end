package com.ocean.piuda.security.jwt.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class TokenCookieFactory {
    private final JwtTokenProvider jwtTokenProvider;
    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";

    /**
     * HttpOnly=true
     * https 배포 하여 secure=true 설정 활성화,
     * secure=true 함에 따라 SameSite는 none 으로
     */
    public ResponseCookie buildAccessTokenCookie(String accessToken) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .sameSite("None")    // 크로스사이트 전송 허용
                .secure(true)        // sameSite 가 None이면 필수
                .path("/")
                .maxAge(computeMaxAgeSeconds(accessToken))
                .build();
    }

    private long computeMaxAgeSeconds(String accessToken) {
        try {
            LocalDateTime exp = jwtTokenProvider.extractExpiration(accessToken);
            long seconds = Duration.between(LocalDateTime.now(ZoneId.systemDefault()), exp).getSeconds();
            return Math.max(seconds, 1L); // 음수/0 방지
        } catch (Exception e) {
            return Duration.ofHours(1).getSeconds();  // 보수적으로 1시간로 설정
        }
    }

}
