package com.ocean.piuda.user.service;

import com.ocean.piuda.user.dto.response.DetailedUserResponse;
import com.ocean.piuda.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 애플리케이션에서 user 와 관련된 종합적인 정보
 * 한번에 반환하기 위한 builder.
 * 해당 유저의 축약된 전반적인 정보 반환이 목적.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserAggregateBuilder {


    public DetailedUserResponse build(User user) {
        // TODO: 다른 도메인 리파지토리 활용해 필요한 정보 얻기
        return DetailedUserResponse.fromEntity(
                user
                // 여기에 얻은 정보들 대입
        );
    }
}
