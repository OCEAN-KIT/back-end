package com.ocean.piuda.security.jwt.config;

import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.jwt.filter.JwtAuthenticationFilter;
import com.ocean.piuda.security.jwt.handler.CustomAccessDeniedHandler;
import com.ocean.piuda.security.jwt.handler.CustomAuthenticationEntryPoint;
import com.ocean.piuda.security.jwt.service.CustomUserDetailsService;
import com.ocean.piuda.security.jwt.util.JwtTokenProvider;
import com.ocean.piuda.security.oauth2.handler.OAuth2FailureHandler;
import com.ocean.piuda.security.oauth2.handler.OAuth2SuccessHandler;
import com.ocean.piuda.security.oauth2.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectProvider<CustomOAuth2UserService> customOAuth2UserServiceProvider;
    private final ObjectProvider<OAuth2SuccessHandler> oAuth2SuccessHandlerProvider;
    private final ObjectProvider<OAuth2FailureHandler> oAuth2FailureHandlerProvider;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Value("${app.oauth2.enabled:false}")
    private boolean oauth2Enabled;

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();

        hierarchy.setHierarchy("""
                ROLE_ADMIN > ROLE_RESEARCHER
                ROLE_ADMIN > ROLE_DIVER
                ROLE_RESEARCHER > ROLE_USER
                ROLE_DIVER > ROLE_USER
                """);

        return hierarchy;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            RoleHierarchy roleHierarchy
    ) throws Exception {
        AuthorizationManager<RequestAuthorizationContext> registeredUser =
                hasAuthorityWithHierarchy(Role.USER.getKey(), roleHierarchy);

        AuthorizationManager<RequestAuthorizationContext> admin =
                hasAuthorityWithHierarchy(Role.ADMIN.getKey(), roleHierarchy);

        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        /*
                         * CORS preflight.
                         *
                         * OPTIONS 요청은 /api/record/**, /api/admin/** 같은 보호 path보다 먼저 허용해야
                         * 브라우저 preflight 단계에서 인증 요구로 막히지 않습니다.
                         */
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        /*
                         * 추가 정보 입력 전 사용자 전용 API.
                         *
                         * public fallback보다 먼저 제한해야 합니다.
                         */
                        .requestMatchers("/api/auth/complete-sign-up/**")
                        .hasAuthority(Role.NOT_REGISTERED.getKey())

                        /*
                         * Dashboard API.
                         *
                         * - GET 조회 API는 public fallback으로 허용합니다.
                         * - 생성/수정/삭제 계열은 ADMIN만 허용합니다.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/dashboard/**")
                        .access(admin)
                        .requestMatchers(HttpMethod.PUT, "/api/dashboard/**")
                        .access(admin)
                        .requestMatchers(HttpMethod.PATCH, "/api/dashboard/**")
                        .access(admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/dashboard/**")
                        .access(admin)

                        /*
                         * Record 앱 API.
                         *
                         * ROLE_USER 이상 접근 가능합니다.
                         * RoleHierarchy에 의해 ROLE_DIVER, ROLE_RESEARCHER, ROLE_ADMIN도 접근 가능합니다.
                         */
                        .requestMatchers("/api/record/**")
                        .access(registeredUser)

                        /*
                         * User API.
                         *
                         * 회원 정보 조회/수정 등 사용자 관련 API는 ROLE_USER 이상 접근 가능합니다.
                         */
                        .requestMatchers("/api/user/**")
                        .access(registeredUser)

                        /*
                         * Admin API.
                         *
                         * ROLE_ADMIN만 접근 가능합니다.
                         */
                        .requestMatchers("/api/admin/**")
                        .access(admin)

                        /*
                         * 기존 실증 public API 호환.
                         *
                         * 위에서 명시적으로 보호한 API 외에는 허용합니다.
                         */
                        .anyRequest().permitAll()
                );

        if (oauth2Enabled) {
            CustomOAuth2UserService customOAuth2UserService = customOAuth2UserServiceProvider.getObject();
            OAuth2SuccessHandler oAuth2SuccessHandler = oAuth2SuccessHandlerProvider.getObject();
            OAuth2FailureHandler oAuth2FailureHandler = oAuth2FailureHandlerProvider.getObject();

            http.oauth2Login(oauth -> oauth
                    .userInfoEndpoint(c -> c.userService(customOAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
            );
        }

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> hasAuthorityWithHierarchy(
            String authority,
            RoleHierarchy roleHierarchy
    ) {
        AuthorityAuthorizationManager<RequestAuthorizationContext> manager =
                AuthorityAuthorizationManager.hasAuthority(authority);

        manager.setRoleHierarchy(roleHierarchy);
        return manager;
    }
}