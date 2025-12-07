package com.ocean.piuda.dashboard.service;

import com.ocean.piuda.dashboard.entity.ProjectArea;
import com.ocean.piuda.dashboard.dto.response.AreaDetailResponse;
import com.ocean.piuda.dashboard.repository.ProjectAreaRepository;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {

    private final ProjectAreaRepository projectAreaRepository;

    public AreaDetailResponse getAreaDetail(Long id) {
        ProjectArea area = projectAreaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        // Entity -> DTO 변환 (Lazy Loading 발생, Transaction 내라 안전)
        return AreaDetailResponse.fromEntity(area);
    }
}