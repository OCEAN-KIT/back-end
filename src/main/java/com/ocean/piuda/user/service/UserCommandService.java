package com.ocean.piuda.user.service;

import com.ocean.piuda.security.jwt.dto.request.UserUpdateRequestDto;
import com.ocean.piuda.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class UserCommandService {
    private final UserQueryService userQueryService;

    public void patch( Long userId, UserUpdateRequestDto req) {
        User user = userQueryService.getUserById(userId);
        user.update(req);
    }











}