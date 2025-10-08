package com.ocean.piuda.security.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.global.api.exception.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 조차 안한 상태에서 접근 튕길때의 처리 담당
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {


        ApiData<?> apiData = ApiData.error(ExceptionType.ACCESS_DENIED);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(apiData));

    }
}