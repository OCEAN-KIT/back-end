package com.ocean.piuda.security.jwt.service;

import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.security.jwt.dto.request.UserUpdateRequestDto;
import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.jwt.util.JwtTokenProvider;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private JwtTokenProvider jwtTokenProvider;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        passwordEncoder = mock(PasswordEncoder.class);

        authService = new AuthService(
                userRepository,
                jwtTokenProvider,
                passwordEncoder
        );
    }

    @Test
    void completeUserSignup_assignsUserRole() {
        User user = User.builder()
                .username("user@test.com")
                .role(Role.NOT_REGISTERED)
                .build();

        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .nickname("일반 사용자")
                .email("user@test.com")
                .phone("010-0000-0000")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.completeUserSignup(1L, request);

        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getNickname()).isEqualTo("일반 사용자");
        assertThat(user.getEmail()).isEqualTo("user@test.com");

        verify(userRepository).findById(1L);
    }

    @Test
    void completeUserSignup_rejectsAlreadyRegisteredUser() {
        User user = User.builder()
                .username("admin@test.com")
                .role(Role.ADMIN)
                .build();

        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .nickname("관리자")
                .email("admin@test.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.completeUserSignup(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("exceptionType")
                .isEqualTo(ExceptionType.ALREADY_REGISTERED_USER);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).findById(1L);
    }
}