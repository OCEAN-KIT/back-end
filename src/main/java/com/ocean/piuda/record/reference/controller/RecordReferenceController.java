package com.ocean.piuda.record.reference.controller;

import com.ocean.piuda.bio.dto.response.SpeciesResponse;
import com.ocean.piuda.bio.service.SpeciesService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.site.dto.response.SiteNameOptionResponse;
import com.ocean.piuda.site.service.SiteNameOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
@Tag(
        name = "Record Reference",
        description = "record 앱 작성 화면에서 사용하는 참조 데이터 조회 API입니다."
)
public class RecordReferenceController {

    private final SpeciesService speciesService;
    private final SiteNameOptionService siteNameOptionService;

    @GetMapping("/species")
    @Operation(
            summary = "생물종 목록 조회",
            description = "record 앱에서 선택 가능한 전체 생물종 목록을 조회합니다."
    )
    public ApiData<List<SpeciesResponse>> getAllSpecies() {
        return ApiData.ok(speciesService.getAllSpecies());
    }

    @GetMapping("/species/search")
    @Operation(
            summary = "생물종 검색",
            description = "이름 기반으로 생물종을 검색합니다."
    )
    public ApiData<List<SpeciesResponse>> searchSpecies(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiData.ok(speciesService.searchByName(keyword));
    }

    @GetMapping("/site-options")
    @Operation(
            summary = "활성 현장 명칭 목록 조회",
            description = "record 작성 화면 드롭다운에 표시할 활성 현장 명칭 목록을 조회합니다."
    )
    public ApiData<List<SiteNameOptionResponse>> getActiveSiteOptions() {
        return ApiData.ok(siteNameOptionService.getActiveOptions());
    }
}