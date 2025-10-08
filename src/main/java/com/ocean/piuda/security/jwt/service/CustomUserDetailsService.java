package com.ocean.piuda.security.jwt.service;




import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;


@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 사실상 얘만 현재 사용
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));
        return new PrincipalDetails(user);
    }

    // 필수 override
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));
        return new PrincipalDetails(user);
    }
}
