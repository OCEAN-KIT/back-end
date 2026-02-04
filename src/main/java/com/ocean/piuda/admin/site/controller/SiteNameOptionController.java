package com.ocean.piuda.admin.site.controller;

import com.ocean.piuda.admin.site.dto.request.CreateSiteOptionRequest;
import com.ocean.piuda.admin.site.dto.request.UpdateSiteOptionRequest;
import com.ocean.piuda.admin.site.dto.response.SiteNameOptionResponse;
import com.ocean.piuda.admin.site.service.SiteNameOptionService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/site")
@RequiredArgsConstructor
@Tag(name = "Site Name Options", description = "현장 명칭 선택지 관리 API")
public class SiteNameOptionController {

    private final SiteNameOptionService service;

    @GetMapping
    @Operation(summary = "현장 명칭 목록 조회 (Active)", description = "작성 화면 드롭다운에 표시할 활성화된 현장 명칭 목록을 조회합니다.")
    public ApiData<List<SiteNameOptionResponse>> getOptions() {
        return ApiData.ok(service.getActiveOptions());
    }

    @PostMapping
    @Operation(summary = "현장 명칭 추가", description = "새로운 현장 명칭 선택지를 추가합니다.")
    public ApiData<SiteNameOptionResponse> createOption(@RequestBody @Valid CreateSiteOptionRequest request) {
        return ApiData.ok(service.createOption(request));
    }

    @DeleteMapping("/{optionId}")
    @Operation(summary = "현장 명칭 비활성화", description = "해당 ID의 현장 명칭을 목록에서 숨김 처리합니다. (기존 기록은 유지됨)")
    public ApiData<Void> deleteOption(@PathVariable Long optionId) {
        service.deactivateOption(optionId);
        return ApiData.ok(null);
    }

    @PatchMapping("/{optionId}")
    @Operation(summary = "현장 명칭 수정", description = "현장 명칭을 변경하거나 활성/비활성 상태를 변경합니다. (변경할 필드만 보내면 됩니다)")
    public ApiData<SiteNameOptionResponse> updateOption(
            @PathVariable Long optionId,
            @RequestBody @Valid UpdateSiteOptionRequest request
    ) {
        return ApiData.ok(service.updateOption(optionId, request));
    }}