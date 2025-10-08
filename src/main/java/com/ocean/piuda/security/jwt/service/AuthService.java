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
     * @param request
     * @return
     */
    public TokenResponseDto usernameLogin(UsernameLoginRequestDto request) {

        /**
         * 유효한 username, pw 을 통한 로그인인지 확인
         * access token 신규 발급
         */
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        // db 에 이미 암호화된 형태로 저장되있기에 request 에 담긴 비밀번호를 암호화 하여 비교 수행
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ExceptionType.PASSWORD_NOT_MATCHED);
        }

        /**
         * 최종 회원 가입까지 완료된 사용자가 아닌 경우 "임시" access token 발급
         * 최종 회원 가입까지 완료된 사용자인 경우 "정식" access token 발급
         */
        return TokenResponseDto.builder()
                .access(issueTokensFor(user.getId()))
                .build();
    }




    /**
     * 특정 유저에 대한 임시/정식 access token 발급
     * @return
     */
    public String issueTokensFor(Long userId) {
        return jwtTokenProvider.generateAccessToken(userId);
    }





    public SignUpResponseDto signup(SignUpRequestDto request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ExceptionType.DUPLICATED_USERNAME);
        }

        // 비밀번호 암호화 및 저장
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        User user = request.toEntity();
        user = userRepository.save(user);

        return SignUpResponseDto.fromEntity(user);
    }

    public SignUpResponseDto completeUserSignup(Long userId, UserUpdateRequestDto request) {
        User user = findAndUpdateUserForCompleteSignup(userId,request);
        user.updateRole(Role.USER);// USER 권한으로 승격
        return SignUpResponseDto.fromEntity(user);
    }



    private User findAndUpdateUserForCompleteSignup(Long userId, UserUpdateRequestDto request){
        User user = userRepository
                .findById(userId)
                .orElseThrow(()->new BusinessException(ExceptionType.USER_NOT_FOUND));

        if (user.getRole() != Role.NOT_REGISTERED) {
            throw new BusinessException(ExceptionType.ALREADY_REGISTERED_USER);
        }

        // 최종 회원 가입을 위한 추가 작업 정의
        user.update(request);
        return user;


    }





}
