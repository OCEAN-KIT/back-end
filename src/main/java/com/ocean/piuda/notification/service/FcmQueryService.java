package com.ocean.piuda.notification.service;

import com.ocean.piuda.notification.entity.FcmToken;
import com.ocean.piuda.notification.repository.FcmTokenRepository;
import com.ocean.piuda.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmQueryService {

    private final FcmTokenRepository fcmTokenRepository;

    public List<FcmToken> getByUser(User user) {
        return fcmTokenRepository.findAllByUser(user);
    }
}
