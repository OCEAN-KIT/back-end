package com.ocean.piuda.record.submission.controller;

import com.ocean.piuda.admin.submission.dto.request.CreateSubmissionRequest;
import com.ocean.piuda.admin.submission.dto.response.SubmissionDetailResponse;
import com.ocean.piuda.admin.submission.dto.response.SubmissionListResponse;
import com.ocean.piuda.admin.submission.service.SubmissionCommandService;
import com.ocean.piuda.admin.submission.service.SubmissionQueryService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.global.api.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/record/submissions")
@RequiredArgsConstructor
@Tag(
        name = "Record Submission",
        description = "record 앱에서 사용하는 제출 API입니다. 현재 로그인한 사용자의 제출 생성/조회 기능을 제공합니다."
)
public class RecordSubmissionController {

    private final SubmissionCommandService submissionCommandService;
    private final SubmissionQueryService submissionQueryService;

    @GetMapping
    @Operation(summary = "내 제출 목록 조회", description = "현재 로그인한 사용자의 제출 목록만 조회합니다.")
    public ApiData<PageResponse<SubmissionListResponse>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiData.ok(submissionQueryService.getMySubmissions(pageable));
    }

    @GetMapping("/{submissionId}")
    @Operation(summary = "내 제출 상세 조회", description = "현재 로그인한 사용자의 제출물일 때만 상세를 조회합니다.")
    public ApiData<SubmissionDetailResponse> getMySubmissionDetail(
            @PathVariable Long submissionId
    ) {
        return ApiData.ok(submissionQueryService.getMySubmissionDetail(submissionId));
    }

    @PostMapping
    @Operation(summary = "기록 제출", description = "record 앱에서 새로운 활동 기록을 제출합니다.")
    public ApiData<SubmissionDetailResponse> submitSubmission(
            @RequestBody @Valid CreateSubmissionRequest request
    ) {
        return ApiData.ok(submissionCommandService.submitSubmission(request));
    }
}