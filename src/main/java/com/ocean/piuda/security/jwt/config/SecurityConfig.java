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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/sign-up").permitAll()
                        .requestMatchers("/api/v1/activities").permitAll()
                        .requestMatchers("/api/auth/complete-sign-up/**").hasAuthority(Role.NOT_REGISTERED.getKey())
                        .requestMatchers("/api/admin/**").hasAuthority(Role.ADMIN.getKey())
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
}