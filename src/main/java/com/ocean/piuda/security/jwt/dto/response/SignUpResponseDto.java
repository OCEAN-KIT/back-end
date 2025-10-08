package com.ocean.piuda.security.jwt.dto.response;

import lombok.Builder;
import lombok.Getter;
import com.ocean.piuda.user.entity.User;

@Getter
@Builder
public class SignUpResponseDto {
    private Long id;
    private String username;

    public static SignUpResponseDto fromEntity(User user) {
        return SignUpResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
