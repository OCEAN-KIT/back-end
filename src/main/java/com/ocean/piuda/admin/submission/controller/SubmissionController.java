package com.ocean.piuda.admin.submission.controller;

import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.submission.dto.request.*;
import com.ocean.piuda.admin.submission.dto.response.AuditLogResponse;
import com.ocean.piuda.admin.submission.dto.response.SubmissionDetailResponse;
import com.ocean.piuda.admin.submission.dto.response.SubmissionListResponse;
import com.ocean.piuda.admin.submission.service.SubmissionCommandService;
import com.ocean.piuda.admin.submission.service.SubmissionQueryService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.global.api.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionQueryService submissionQueryService;
    private final SubmissionCommandService submissionCommandService;

    /**
     * 제출 목록 조회
     */
    @GetMapping
    public ApiData<PageResponse<SubmissionListResponse>> getSubmissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<SubmissionListResponse> result = submissionQueryService.getSubmissions(
                keyword, status, activityType, startDate, endDate, pageable
        );
        
        return ApiData.ok(result);
    }

    /**
     * 제출 상세 조회
     */
    @GetMapping("/{submissionId}")
    public ApiData<SubmissionDetailResponse> getSubmissionDetail(@PathVariable Long submissionId) {
        SubmissionDetailResponse response = submissionQueryService.getSubmissionDetail(submissionId);
        return ApiData.ok(response);
    }

    /**
     * 검수 로그 조회
     */
    @GetMapping("/{submissionId}/logs")
    public ApiData<List<AuditLogResponse>> getSubmissionLogs(@PathVariable Long submissionId) {
        List<AuditLogResponse> logs = submissionQueryService.getSubmissionLogs(submissionId);
        return ApiData.ok(logs);
    }

    /**
     * 단건 승인
     */
    @PostMapping("/{submissionId}/approve")
    public ApiData<SubmissionDetailResponse> approveSubmission(@PathVariable Long submissionId) {
        SubmissionDetailResponse response = submissionCommandService.approveSubmission(submissionId);
        return ApiData.ok(response);
    }

    /**
     * 단건 반려
     */
    @PostMapping("/{submissionId}/reject")
    public ApiData<SubmissionDetailResponse> rejectSubmission(
            @PathVariable Long submissionId,
            @RequestBody @Valid SingleRejectRequest request
    ) {
        SubmissionDetailResponse response = submissionCommandService.rejectSubmission(submissionId, request);
        return ApiData.ok(response);
    }

    /**
     * 단건 삭제
     */
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(
            @PathVariable Long submissionId,
            @RequestBody(required = false) SingleDeleteRequest request
    ) {
        if (request == null) {
            request = new SingleDeleteRequest(null);
        }
        submissionCommandService.deleteSubmission(submissionId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 일괄 승인
     */
    @PostMapping("/bulk/approve")
    public ApiData<SubmissionCommandService.BulkApproveResponse> bulkApprove(
            @RequestBody @Valid BulkApproveRequest request
    ) {
        SubmissionCommandService.BulkApproveResponse response = submissionCommandService.bulkApprove(request);
        return ApiData.ok(response);
    }

    /**
     * 일괄 반려
     */
    @PostMapping("/bulk/reject")
    public ApiData<SubmissionCommandService.BulkRejectResponse> bulkReject(
            @RequestBody @Valid BulkRejectRequest request
    ) {
        SubmissionCommandService.BulkRejectResponse response = submissionCommandService.bulkReject(request);
        return ApiData.ok(response);
    }

    /**
     * 일괄 삭제
     */
    @DeleteMapping("/bulk")
    public ApiData<SubmissionCommandService.BulkDeleteResponse> bulkDelete(
            @RequestBody @Valid BulkDeleteRequest request
    ) {
        SubmissionCommandService.BulkDeleteResponse response = submissionCommandService.bulkDelete(request);
        return ApiData.ok(response);
    }
}
