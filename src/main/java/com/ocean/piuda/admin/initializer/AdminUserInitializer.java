package com.ocean.piuda.admin.initializer;

import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod") // 프로덕션 환경에서는 실행되지 않음
@Order(0) // SubmissionDataInitializer보다 먼저 실행
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "admin@admin.com";
    private static final String ADMIN_PASSWORD = "password";

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 Admin 계정이 있으면 생성하지 않음
        if (userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            log.info("Admin 계정이 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("Admin 계정 초기화 시작...");

        // Admin 계정 생성
        User adminUser = User.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .email(ADMIN_USERNAME)
                .nickname("관리자")
                .build();

        userRepository.save(adminUser);
        log.info("Admin 계정 생성 완료 - Username: {}, Role: {}", ADMIN_USERNAME, Role.ADMIN);
    }
}
