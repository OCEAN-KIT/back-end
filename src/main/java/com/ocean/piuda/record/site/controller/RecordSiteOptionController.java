package com.ocean.piuda.record.site.controller;

import com.ocean.piuda.admin.site.dto.response.SiteNameOptionResponse;
import com.ocean.piuda.admin.site.service.SiteNameOptionService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/record/site-options")
@RequiredArgsConstructor
@Tag(
        name = "Record Site Options",
        description = "record 앱 작성 화면에서 사용하는 현장 명칭 선택지 조회 API입니다."
)
public class RecordSiteOptionController {

    private final SiteNameOptionService siteNameOptionService;

    @GetMapping
    @Operation(summary = "활성 현장 명칭 목록 조회", description = "record 작성 화면 드롭다운에 표시할 활성 현장 명칭 목록을 조회합니다.")
    public ApiData<List<SiteNameOptionResponse>> getActiveOptions() {
        return ApiData.ok(siteNameOptionService.getActiveOptions());
    }
}