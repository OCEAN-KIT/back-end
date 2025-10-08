package com.ocean.piuda.security.jwt.config;

import com.ocean.piuda.security.jwt.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.jwt.filter.JwtAuthenticationFilter;
import com.ocean.piuda.security.jwt.handler.CustomAccessDeniedHandler;
import com.ocean.piuda.security.jwt.handler.CustomAuthenticationEntryPoint;
import com.ocean.piuda.security.jwt.service.CustomUserDetailsService;
import com.ocean.piuda.security.oauth2.handler.OAuth2FailureHandler;
import com.ocean.piuda.security.oauth2.handler.OAuth2SuccessHandler;
import com.ocean.piuda.security.oauth2.service.CustomOAuth2UserService;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;




    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                        .requestMatchers("/api/auth/login", "/api/auth/sign-up").permitAll()
                        .requestMatchers("/api/auth/complete-sign-up/**").hasAuthority(Role.NOT_REGISTERED.getKey())

        // 그 외 비로그인 사용자도 허용
                        .anyRequest().permitAll()
                )

                // oauth2 설정
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(c -> c.userService(customOAuth2UserService)) // 사용자 정보를 어디서 load 할지 설정
                        .successHandler(oAuth2SuccessHandler) // 로그인 성공 시 핸들러
                        .failureHandler(oAuth2FailureHandler) // 로그인 실패 시 핸들러
                )

                // jwt 설정
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService), UsernamePasswordAuthenticationFilter.class);  // JWT 인증 필터 추가;


        return http.build();
    }


}
