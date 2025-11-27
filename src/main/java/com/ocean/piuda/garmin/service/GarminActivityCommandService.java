package com.ocean.piuda.garmin.service;

import com.ocean.piuda.garmin.dto.request.ActivitySessionRequest;
import com.ocean.piuda.garmin.dto.response.ActivitySessionResponse;
import com.ocean.piuda.garmin.entity.GarminActivityLog;
import com.ocean.piuda.garmin.repository.GarminActivityLogRepository;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GarminActivityCommandService {

    private final GarminActivityLogRepository repository;
    private final UserRepository userRepository;

    /**
     * 워치 활동 수신
     * - 한 워치당 한 유저에만 페어링
     * - 페어링되지 않은 워치의 요청은 거절(WATCH_NOT_PAIRED)
     */
    public ActivitySessionResponse createActivityLog(ActivitySessionRequest request) {
        if (request == null || request.summary() == null) {
            throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
        }

        String deviceId = request.summary().deviceId();  // 워치에서 온 암호화된 deviceId
        if (deviceId == null || deviceId.isBlank()) {
            throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
        }

        // 이 deviceId가 어떤 유저에 페어링되어 있는지 확인
        User pairedUser = userRepository.findByWatchDeviceId(deviceId)
                .orElseThrow(() -> new BusinessException(ExceptionType.WATCH_NOT_PAIRED));

        Long pairedUserId = pairedUser.getId();

        // sessionId 생성 (deviceId + startTime 기준 deterministic)
        Long startTimeEpoch = request.summary().startTime();
        String sessionId = generateSessionId(deviceId, startTimeEpoch);

        // DTO → 엔티티 변환
        List<GarminActivityLog> entities = request.toEntities(pairedUserId, sessionId);
        if (entities.isEmpty()) {
            throw new BusinessException(ExceptionType.INVALID_PAYLOAD); // or custom
        }
        List<GarminActivityLog> savedLogs = repository.saveAll(entities);

        return ActivitySessionResponse.fromEntities(savedLogs);
    }

    private String generateSessionId(String deviceId, Long startTimeEpochSec) {
        if (deviceId == null || startTimeEpochSec == null) {
            return UUID.randomUUID().toString();
        }
        String raw = deviceId + ":" + startTimeEpochSec;
        return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
