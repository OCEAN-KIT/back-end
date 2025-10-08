package com.ocean.piuda.security.jwt.controller;

import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.security.jwt.util.TokenCookieFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ocean.piuda.security.jwt.dto.request.SignUpRequestDto;
import com.ocean.piuda.security.jwt.dto.request.UserUpdateRequestDto;
import com.ocean.piuda.security.jwt.dto.request.UsernameLoginRequestDto;
import com.ocean.piuda.security.jwt.dto.response.SignUpResponseDto;
import com.ocean.piuda.security.jwt.dto.response.TokenResponseDto;
import com.ocean.piuda.security.jwt.service.AuthService;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;

@Tag(name = "Auth API", description = "유저 및 회원가입 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenCookieFactory tokenCookieFactory;


    /** 자체 회원가입 유저의 로그인 (공개) */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "자체 회원가입 유저의 아이디/비밀번호 로그인")
    public ApiData<TokenResponseDto> usernameLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 요청 바디", required = true
            )
            @RequestBody UsernameLoginRequestDto request,
            HttpServletResponse response
    ) {
        TokenResponseDto dto = authService.usernameLogin(request); // 서비스는 토큰만 생성/검증
        ResponseCookie cookie = tokenCookieFactory.buildAccessTokenCookie(dto.getAccess());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiData.ok(dto);
    }

    /** 자체 회원 가입 (1차, 공개) */
    @Operation(summary = "자체 회원가입 1단계", description = "자체 로그인 기반 회원가입 1단계 수행")
    @PostMapping("/sign-up")
    public ApiData<SignUpResponseDto> signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 요청 바디", required = true
            )
            @RequestBody SignUpRequestDto requestDto
    ) {
        SignUpResponseDto responseDto = authService.signup(requestDto);
        return ApiData.created(responseDto);
    }

    /** ROLE_USER 최종(2차) 회원가입 (보호) */
    @PostMapping("/complete-sign-up/user")
    @Operation(summary = "회원가입 2단계(일반 사용자)", description = "일반 사용자에 대한 최종 회원가입 완료")
    @SecurityRequirement(name = "bearerAuth")
    public ApiData<Void> completeUserSignup(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 2단계 요청 바디", required = true
            )
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        authService.completeUserSignup(principal.getUser().getId(), requestDto);
        return ApiData.created(null);
    }


}
