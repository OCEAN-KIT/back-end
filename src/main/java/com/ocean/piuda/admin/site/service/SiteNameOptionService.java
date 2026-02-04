package com.ocean.piuda.admin.site.service;


import com.ocean.piuda.admin.site.dto.request.CreateSiteOptionRequest;
import com.ocean.piuda.admin.site.dto.request.UpdateSiteOptionRequest;
import com.ocean.piuda.admin.site.dto.response.SiteNameOptionResponse;
import com.ocean.piuda.admin.site.entity.SiteNameOption;
import com.ocean.piuda.admin.site.repository.SiteNameOptionRepository;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteNameOptionService {

    private final SiteNameOptionRepository siteNameOptionRepository;

    /**
     * [user] 활성화된 현장 명칭 목록 조회
     */
    public List<SiteNameOptionResponse> getActiveOptions() {
        return siteNameOptionRepository.findAllByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(SiteNameOptionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * [Admin] 현장 명칭 추가
     */
    @Transactional
    public SiteNameOptionResponse createOption(CreateSiteOptionRequest request) {
        if (siteNameOptionRepository.existsByName(request.name())) {
            throw new BusinessException(ExceptionType.DUPLICATE_VALUE_ERROR);
        }

        SiteNameOption option = SiteNameOption.builder()
                .name(request.name())
                .isActive(true)
                .build();

        return SiteNameOptionResponse.from(siteNameOptionRepository.save(option));
    }

    /**
     * [Admin] 현장 명칭 비활성화 (soft delete)
     */
    @Transactional
    public void deactivateOption(Long optionId) {
        SiteNameOption option = siteNameOptionRepository.findById(optionId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        option.deactivate();
    }


    /**
     * [Admin] 현장 명칭 수정 (이름 변경 또는 활성 상태 변경)
     */
    @Transactional
    public SiteNameOptionResponse updateOption(Long optionId, UpdateSiteOptionRequest request) {
        SiteNameOption option = siteNameOptionRepository.findById(optionId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        // 이름 변경 요청이 있고, 기존 이름과 다를 경우 중복 체크 수행
        if (request.name() != null && !request.name().equals(option.getName())) {
            if (siteNameOptionRepository.existsByName(request.name())) {
                throw new BusinessException(ExceptionType.DUPLICATE_VALUE_ERROR);
            }
        }

        option.update(request.name(), request.isActive());

        return SiteNameOptionResponse.from(option);
    }
}