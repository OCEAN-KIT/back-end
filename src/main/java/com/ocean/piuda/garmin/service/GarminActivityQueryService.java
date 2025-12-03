package com.ocean.piuda.garmin.service;


import com.ocean.piuda.garmin.dto.request.GarminActivityPageRequest;
import com.ocean.piuda.garmin.dto.response.ActivitySessionResponse;
import com.ocean.piuda.garmin.entity.GarminActivityLog;
import com.ocean.piuda.garmin.repository.GarminActivityLogRepository;
import com.ocean.piuda.global.api.dto.PageResponse;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GarminActivityQueryService {

    private final GarminActivityLogRepository repository;

    public ActivitySessionResponse searchByLogId(Long id) {
        GarminActivityLog referenceLog = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        List<GarminActivityLog> sessionLogs;
        sessionLogs = repository.findAllBySessionId(referenceLog.getSessionId());

        return ActivitySessionResponse.fromEntities(sessionLogs);
    }

    /**
     * 로그인 유저 기준 세션 단위 목록 조회 (페이지네이션)
     */
    public PageResponse<ActivitySessionResponse> searchMySessions(Long userId, GarminActivityPageRequest req) {
        var pageable = req.toPageable();

        // 세션당 대표 로그들만 페이지네이션 조회
        Page<GarminActivityLog> headPage = repository.findSessionHeadsByUserId(userId, pageable);

        // 각 대표 로그의 sessionId 기준으로 세션 전체를 다시 조회 → 세션 단위 응답으로 변환
        List<ActivitySessionResponse> content = headPage.getContent().stream()
                .map(head -> {
                    List<GarminActivityLog> sessionLogs = repository.findAllBySessionId(head.getSessionId());
                    return ActivitySessionResponse.fromEntities(sessionLogs);
                })
                .toList();

        // PageResponse.of를 쓰기 위해 Page<T> 형태로 한번 래핑
        Page<ActivitySessionResponse> mapped = new PageImpl<>(
                content,
                pageable,
                headPage.getTotalElements()
        );

        return PageResponse.of(mapped);
    }

    /**
     * sessionId(String) 기준 세션 조회
     */
    public ActivitySessionResponse searchBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
        }

        List<GarminActivityLog> sessionLogs = repository.findAllBySessionId(sessionId);

        if (sessionLogs == null || sessionLogs.isEmpty()) {
            throw new BusinessException(ExceptionType.RESOURCE_NOT_FOUND);
        }

        return ActivitySessionResponse.fromEntities(sessionLogs);
    }


}
