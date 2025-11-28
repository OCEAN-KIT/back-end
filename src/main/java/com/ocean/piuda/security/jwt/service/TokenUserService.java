package com.ocean.piuda.security.jwt.service;


import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.security.jwt.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;
import com.ocean.piuda.user.entity.User;

/**
 * 시큐리티 컨텍스트에서 현재 인증된 사용자 정보를 조회하는 서비스
 */
@Service
@RequiredArgsConstructor
public class TokenUserService {

    /**
     * 현재 인증된 사용자의 User 엔티티를 반환합니다.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ExceptionType.UNAUTHORIZED_USER);
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof PrincipalDetails principalDetails)) {
            throw new BusinessException(ExceptionType.UNAUTHORIZED_USER);
        }

        return principalDetails.getUser();
    }



    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }



    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     */
    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }



    /**
     * 현재 사용자가 인증되어 있는지 확인합니다.
     *
     * @return 인증된 경우 true, 그렇지 않으면 false
     */
    public boolean isAuthenticated() {
        try {
            getCurrentUser();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
