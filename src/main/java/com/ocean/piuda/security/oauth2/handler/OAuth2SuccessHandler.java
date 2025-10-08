package com.ocean.piuda.security.oauth2.handler;

import com.ocean.piuda.security.jwt.util.TokenCookieFactory;
import com.ocean.piuda.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.jwt.service.AuthService;
import com.ocean.piuda.security.jwt.util.JwtTokenProvider;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${oauth2.url.base}")
    private String BASE_URL;

    @Value("${oauth2.url.path.signup}")
    private String SIGNUP_PATH;

    @Value("${oauth2.url.path.auth}")
    private String AUTH_PATH;


    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCookieFactory tokenCookieFactory;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        User user = principalDetails.getUser();

        // 액세스 토큰 발급
        String accessToken = authService.issueTokensFor(user.getId());

        /**
         * access token 을 쿠키로 전달
         */
        ResponseCookie cookie = tokenCookieFactory.buildAccessTokenCookie(accessToken);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 리다이렉트 URL 생성
        String redirectUrl = getRedirectUrlByRole(user);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

    }



    private String getRedirectUrlByRole(User user) {
        Role role = user.getRole();
        Long userId = user.getId();


        /**
         * 최종 회원가입 이전의 사용자에 대한 리다이렉트
         * 여기까지 왔는데 회원가입 되지 않았다의 의미 : provider 상에 등록 완료됬고, 우리 db에 저장까지 됬는데 추가적인 부가정보를 입력하지 않아 우리 애플리케이션 상에서 최종 회원가입 완료는 안된 상태
         */
        if (role == Role.NOT_REGISTERED) {
            return UriComponentsBuilder
                    .fromUriString(BASE_URL + SIGNUP_PATH) // fromUriString -> 상대경로 포함 가능
                    .queryParam("id", userId)
                    .build()
                    .toUriString();
        }

        /**
         * 최종 회원가입된 사용자에 대한 리다이렉트
         */
        return UriComponentsBuilder
                .fromHttpUrl(BASE_URL + AUTH_PATH) // fromHttpUrl() -> 절대 경로만 허용
                .queryParam("id", userId)
                .build()
                .toUriString();
    }
}