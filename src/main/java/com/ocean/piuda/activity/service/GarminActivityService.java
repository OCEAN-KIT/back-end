package com.ocean.piuda.activity.service;


import com.ocean.piuda.activity.dto.request.ActivityIngestRequest;
import com.ocean.piuda.activity.dto.response.ActivitySummaryResponse;
import com.ocean.piuda.activity.entity.GarminActivityLog;
import com.ocean.piuda.activity.repository.GarminActivityLogRepository;
import com.ocean.piuda.activity.util.ActivityMapper;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GarminActivityService {

    private final GarminActivityLogRepository repository;
    private final ActivityMapper mapper;

    @Transactional
    public Long ingest(Long userId, ActivityIngestRequest request) {
        var s = request.summary();
        if (s.activityType()== null || s.endTime() == null) {
            throw new BusinessException(ExceptionType.NOT_VALID_REQUEST_FIELDS_ERROR);
        }

        GarminActivityLog saved = repository.save(mapper.toEntity(request, userId));
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public ActivitySummaryResponse  getOne(Long id) {
        GarminActivityLog e = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        Long startEpoch = e.getStartTime() == null ? null
                : e.getStartTime().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        Long endEpoch = e.getEndTime() == null ? null
                : e.getEndTime().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();

        return new ActivitySummaryResponse(
                e.getId(),
                e.getUserId(),
                e.getGarminActivityType(),
                e.getGridId(),
                e.getTotalCount(),
                startEpoch,
                endEpoch,
                e.getStartLat(),
                e.getStartLon(),
                e.getEndLat(),
                e.getEndLon()
        );
    }
}
