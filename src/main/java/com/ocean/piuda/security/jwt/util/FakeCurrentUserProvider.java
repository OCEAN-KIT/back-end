package com.ocean.piuda.security.jwt.util;

import com.ocean.piuda.security.jwt.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class FakeCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        // TODO: 실제 구현 시 SecurityContext에서 가져오도록 수정
        return 1L; // 테스트용 mock user id
    }

    @Override
    public Role getCurrentUserRole() {
        // TODO: 실제 구현 시 SecurityContext에서 가져오도록 수정
        return Role.ADMIN; // 테스트용 mock role (ADMIN 또는 USER로 변경 가능)
    }
}

