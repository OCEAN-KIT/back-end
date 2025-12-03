package com.ocean.piuda.garmin.service;


import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WatchPairingCommandService {

    private final UserRepository userRepository;

    /**
     * 한 유저당 최대 1 워치.
     * - 동일 유저가 다시 같은 워치를 등록하면 no-op (idempotent)
     * - 같은 유저가 다른 워치를 등록하면 기존 워치에서 교체 (upsert)
     * - 이미 다른 유저에 묶인 워치는 거절.
     */
    public void pair(Long userId, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        // 이 deviceId를 이미 쓰고 있는 "다른 유저"가 있는지 확인
        userRepository.findByWatchDeviceId(deviceId)
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    // 다른 계정에 이미 묶인 워치 → 훔쳐오지 못하게 차단
                    throw new BusinessException(ExceptionType.WATCH_ALREADY_PAIRED);
                });

        user.pairWatch(deviceId);
    }

    public void unpair(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));
        user.unpairWatch();
    }
}
