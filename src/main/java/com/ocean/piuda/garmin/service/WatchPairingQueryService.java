package com.ocean.piuda.garmin.service;


import com.ocean.piuda.garmin.dto.response.WatchPairResponse;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchPairingQueryService {

    private final UserRepository userRepository;

    /**
     * 현재 유저에 페어링된 워치 ID 조회
     * - 페어링 안 되어 있으면 null 반환
     */
    public WatchPairResponse getByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        return new WatchPairResponse(user.getWatchDeviceId(), userId);   // 페어링 없으면 null
    }
}
