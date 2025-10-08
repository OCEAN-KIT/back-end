package com.ocean.piuda.user.dto.response;


import com.ocean.piuda.user.entity.User;
import lombok.Builder;

import java.util.List;


@Builder
public record DetailedUserResponse(
        Long id,
        String nickname,
        String email,
        String phone
// TODO : 다른도메인에서 얻은 값들

) {

    public static DetailedUserResponse fromEntity(
            User u
        // 추가한 다른 도메인 필드들에 대한 파라미터들
    ){
        return DetailedUserResponse.builder()
                .id(u.getId())
                .nickname(u.getNickname())
                .email(u.getEmail())
                .phone(u.getPhone())
            // 그 파라미터들 대한 빌더 패턴
                .build();
    }
}
