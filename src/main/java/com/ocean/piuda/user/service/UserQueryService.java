package com.ocean.piuda.user.service;

import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ocean.piuda.user.dto.request.UserSearchRequest;
import com.ocean.piuda.user.dto.response.DetailedUserResponse;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;


@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserAggregateBuilder aggregateBuilder;


    /**
     * 기본적인 조회
     */

    public User getUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));
    }






    // TODO: 데모 버전에선 간단하게 mysql 쿼리로 검색하지만 추후 elasticsearch 확장

    /**
     * 1) nickname 전용 검색
     */
    public Page<DetailedUserResponse> searchByNickname(UserSearchRequest req) {
        String q = normalize(req.qOrEmpty());
        var pageable = PageRequest.of(req.pageOrDefault(), req.sizeOrDefault());
        Page<User> page = userRepository.searchByNicknameFulltext(q, pageable);
        return page.map(aggregateBuilder::build);
    }



    /**
     * 2) username 전용 검색
     */
    public Page<DetailedUserResponse> searchByUsername(UserSearchRequest req) {
        String q = normalize(req.qOrEmpty());
        var pageable = PageRequest.of(req.pageOrDefault(), req.sizeOrDefault());
        Page<User> page = userRepository.searchByUsernameFulltext(q, pageable);
        return page.map(aggregateBuilder::build);
    }


    /**
     * 3) nickname + username 동시 검색
     */
    public Page<DetailedUserResponse> searchByNicknameOrUsername(UserSearchRequest req) {
        String q = normalize(req.qOrEmpty());
        var pageable = PageRequest.of(req.pageOrDefault(), req.sizeOrDefault());
        Page<User> page = userRepository.searchByNicknameOrUsernameFulltext(q, pageable);
        return page.map(aggregateBuilder::build);
    }



    /**
     * 불리언 모드 특수문자로 인한 파싱 오류를 줄이기 위한 간단 정규화
     */
    private String normalize(String q) {
        if (q == null) return "";
        // 불리언 모드에서 의미 있는 특수기호를 제거/공백화
        return q.replaceAll("[+\\-><()~\"@]+", " ").trim();
    }








}