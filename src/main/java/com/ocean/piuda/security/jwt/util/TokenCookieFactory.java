package com.ocean.piuda.security.jwt.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class TokenCookieFactory {
    private final JwtTokenProvider jwtTokenProvider;
    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    // 환경별 설정 (prod/dev 분리)
    @Value("${app.cookie.domain:}")        // 예: jungjiyu.com  (앞에 점 붙이지 말 것!)
    private String cookieDomain;

    @Value("${app.cookie.secure:true}")    // prod: true, dev(http): false
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:None}") // prod: None, dev: Lax
    private String cookieSameSite;

    public ResponseCookie buildAccessTokenCookie(String accessToken) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie
                .from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)  // PWA 이슈 회피: None + Secure
                .path("/")
                .maxAge(computeMaxAgeSeconds(accessToken));

        if (StringUtils.hasText(cookieDomain)) {
            // "jungjiyu.com" 으로 넣어야 함 (".jungjiyu.com" 금지)
            b = b.domain(cookieDomain);
        }
        return b.build();
    }

    public ResponseCookie deleteAccessTokenCookie() {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie
                .from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(0);

        if (StringUtils.hasText(cookieDomain)) {
            b = b.domain(cookieDomain);
        }
        return b.build();
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

    /**
     * 로그아웃용 쿠키 삭제 (maxAge=0)
     */
    public ResponseCookie buildLogoutCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .path("/")
                .maxAge(0)  // 즉시 삭제
                .build();
    }

}
