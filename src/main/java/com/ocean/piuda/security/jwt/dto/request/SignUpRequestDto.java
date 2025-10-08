package com.ocean.piuda.security.jwt.dto.request;


import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.user.entity.User;
import lombok.*;

/**
 * 자체 회원가입을 위한 dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDto {
    private String username;
    private String password;

    public User toEntity() {
        return User.builder()
                .username(username)
                .password(password)
                .role(Role.NOT_REGISTERED) // OAuth2 회원가입과 마찬가지로, 처음 기본 정보만으로는 완전한 회원가입 처리 시키진 않음
                .build();
    }
}
