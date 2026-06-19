package com.ocean.piuda.user.service;

import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.user.dto.request.UserSearchRequest;
import com.ocean.piuda.user.dto.response.DetailedUserResponse;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserAggregateBuilder aggregateBuilder;

    public User getUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));
    }

    public Page<DetailedUserResponse> searchByNickname(UserSearchRequest req) {
        String q = normalize(req.qOrEmpty());
        var pageable = PageRequest.of(req.pageOrDefault(), req.sizeOrDefault());
        Page<User> page = userRepository.searchByNicknameFulltext(q, pageable);
        return page.map(aggregateBuilder::build);
    }

    public Page<DetailedUserResponse> searchByUsername(UserSearchRequest req) {
        String q = normalize(req.qOrEmpty());
        var pageable = PageRequest.of(req.pageOrDefault(), req.sizeOrDefault());
        Page<User> page = userRepository.searchByUsernameFulltext(q, pageable);
        return page.map(aggregateBuilder::build);
    }

    public Page<DetailedUserResponse> searchByNicknameOrUsername(UserSearchRequest req) {
        String q = normalize(req.qOrEmpty());
        var pageable = PageRequest.of(req.pageOrDefault(), req.sizeOrDefault());
        Page<User> page = userRepository.searchByNicknameOrUsernameFulltext(q, pageable);
        return page.map(aggregateBuilder::build);
    }

    private String normalize(String q) {
        if (q == null) return "";
        return q.replaceAll("[+\\-><()~\"@]+", " ").trim();
    }
}
