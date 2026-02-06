package com.ocean.piuda.security.jwt.filter;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ocean.piuda.security.jwt.service.CustomUserDetailsService;
import com.ocean.piuda.security.jwt.util.JwtTokenProvider;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;

import java.io.IOException;


@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = JwtTokenProvider.extractToken(request.getHeader("Authorization"));  // JWT 토큰 추출

        log.debug("[JwtAuthenticationFilter] token: {}", token);

        // 쿠키에서 ACCESS_TOKEN 찾기 (oauth2 리다이렉트 시 쿠키에 access token 저장)
        if (token == null) {
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("ACCESS_TOKEN".equals(c.getName())) {
                        token = c.getValue();
                        break;
                    }
                }
            }
        }

        // 임시/정식 access 토큰이 존재하고 유효한 경우
        if (token != null && jwtTokenProvider.isValidToken(token)) {
            Long id =jwtTokenProvider.extractId(token);  // JWT에서 사용자명 추출

            log.debug("[JwtAuthenticationFilter] userId from token: {}", id);

            // UserDetailsService 를 통해 사용자 정보 로드
            PrincipalDetails userDetails = (PrincipalDetails) customUserDetailsService.loadUserById(id);

            // 인증 객체 생성 및 SecurityContext에 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }


}
