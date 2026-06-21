package com.ocean.piuda.admin.bio.controller;

import com.ocean.piuda.bio.dto.request.SpeciesRequest;
import com.ocean.piuda.bio.dto.response.SpeciesResponse;
import com.ocean.piuda.bio.service.SpeciesService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/species")
@RequiredArgsConstructor
@Tag(
        name = "Admin Species",
        description = "관리자 생물종 마스터 데이터 관리 API입니다."
)
public class AdminSpeciesController {

    private final SpeciesService speciesService;

    @GetMapping
    @Operation(summary = "생물종 목록 조회")
    public ApiData<List<SpeciesResponse>> getAll() {
        return ApiData.ok(speciesService.getAllSpecies());
    }

    @GetMapping("/search")
    @Operation(summary = "생물종 검색")
    public ApiData<List<SpeciesResponse>> search(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiData.ok(speciesService.searchByName(keyword));
    }

    @PostMapping
    @Operation(summary = "신규 생물종 등록")
    public ApiData<SpeciesResponse> create(
            @Valid @RequestBody SpeciesRequest.Create req
    ) {
        return ApiData.ok(speciesService.createSpecies(req));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "생물종 수정")
    public ApiData<Boolean> update(
            @PathVariable Long id,
            @RequestBody SpeciesRequest.Update req
    ) {
        speciesService.updateSpecies(id, req);
        return ApiData.ok(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "생물종 삭제")
    public ApiData<Boolean> delete(
            @PathVariable Long id
    ) {
        speciesService.deleteSpecies(id);
        return ApiData.ok(true);
    }
}