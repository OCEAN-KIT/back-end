package com.ocean.piuda.record.bio.controller;

import com.ocean.piuda.bio.dto.response.SpeciesResponse;
import com.ocean.piuda.bio.service.SpeciesService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/record/species")
@RequiredArgsConstructor
@Tag(
        name = "Record Species",
        description = "record 앱에서 사용하는 생물종 조회 API입니다."
)
public class RecordSpeciesController {

    private final SpeciesService speciesService;

    @GetMapping
    @Operation(summary = "생물종 목록 조회", description = "record 앱에서 선택 가능한 전체 생물종 목록을 조회합니다.")
    public ApiData<List<SpeciesResponse>> getAll() {
        return ApiData.ok(speciesService.getAllSpecies());
    }

    @GetMapping("/search")
    @Operation(summary = "생물종 검색", description = "이름 기반으로 생물종을 검색합니다.")
    public ApiData<List<SpeciesResponse>> search(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiData.ok(speciesService.searchByName(keyword));
    }
}