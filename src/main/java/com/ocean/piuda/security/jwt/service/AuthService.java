package com.ocean.piuda.security.jwt.service;

import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import com.ocean.piuda.security.jwt.dto.request.SignUpRequestDto;
import com.ocean.piuda.security.jwt.dto.request.UserUpdateRequestDto;
import com.ocean.piuda.security.jwt.dto.request.UsernameLoginRequestDto;
import com.ocean.piuda.security.jwt.dto.response.SignUpResponseDto;
import com.ocean.piuda.security.jwt.dto.response.TokenResponseDto;
import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.jwt.util.JwtTokenProvider;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * username, pw 를 통한 자체 로그인 로직
     */
    public TokenResponseDto usernameLogin(UsernameLoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        // DB 에 이미 암호화된 형태로 저장된 비밀번호와 요청 비밀번호를 비교
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ExceptionType.PASSWORD_NOT_MATCHED);
        }

        return TokenResponseDto.builder()
                .access(issueTokensFor(user.getId()))
                .build();
    }

    /**
     * 특정 유저에 대한 access token 발급
     */
    public String issueTokensFor(Long userId) {
        return jwtTokenProvider.generateAccessToken(userId);
    }

    /**
     * 자체 회원가입 1단계.
     *
     * 최초 가입 시에는 NOT_REGISTERED 상태로 저장합니다.
     * 추가 정보 입력 후 completeUserSignup 단계에서 USER 권한으로 전환합니다.
     */
    public SignUpResponseDto signup(SignUpRequestDto request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ExceptionType.DUPLICATED_USERNAME);
        }

        request.setPassword(passwordEncoder.encode(request.getPassword()));

        User user = request.toEntity();
        user = userRepository.save(user);

        return SignUpResponseDto.fromEntity(user);
    }

    /**
     * 일반 사용자 최종 회원가입 완료.
     *
     * public 회원가입 플로우에서는 ADMIN 권한을 부여하지 않습니다.
     * 관리자 계정은 운영자가 별도 DB 삽입 또는 관리자 전용 절차로 생성해야 합니다.
     */
    public SignUpResponseDto completeUserSignup(Long userId, UserUpdateRequestDto request) {
        User user = findAndUpdateUserForCompleteSignup(userId, request);
        user.updateRole(Role.USER);
        return SignUpResponseDto.fromEntity(user);
    }

    private User findAndUpdateUserForCompleteSignup(Long userId, UserUpdateRequestDto request) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        if (user.getRole() != Role.NOT_REGISTERED) {
            throw new BusinessException(ExceptionType.ALREADY_REGISTERED_USER);
        }

        user.update(request);
        return user;
    }
}