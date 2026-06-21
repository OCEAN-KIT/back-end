package com.ocean.piuda.admin.submission.controller;

import com.ocean.piuda.submission.enums.ActivityType;
import com.ocean.piuda.submission.enums.SubmissionStatus;
import com.ocean.piuda.submission.dto.request.BulkApproveRequest;
import com.ocean.piuda.submission.dto.request.BulkDeleteRequest;
import com.ocean.piuda.submission.dto.request.BulkRejectRequest;
import com.ocean.piuda.submission.dto.request.SingleDeleteRequest;
import com.ocean.piuda.submission.dto.request.SingleRejectRequest;
import com.ocean.piuda.submission.dto.response.AuditLogResponse;
import com.ocean.piuda.submission.dto.response.BulkApproveResponse;
import com.ocean.piuda.submission.dto.response.BulkDeleteResponse;
import com.ocean.piuda.submission.dto.response.BulkRejectResponse;
import com.ocean.piuda.submission.dto.response.SubmissionDetailResponse;
import com.ocean.piuda.submission.dto.response.SubmissionListResponse;
import com.ocean.piuda.submission.service.SubmissionCommandService;
import com.ocean.piuda.submission.service.SubmissionQueryService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.global.api.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Admin Submission",
        description = "관리자 제출 데이터 검수 API입니다. 제출 데이터 조회, 승인, 반려, 삭제 기능을 제공합니다."
)
public class AdminSubmissionController {

    private final SubmissionQueryService submissionQueryService;
    private final SubmissionCommandService submissionCommandService;

    /**
     * Admin 제출 목록 조회.
     */
    @GetMapping
    @Operation(summary = "Admin 제출 목록 조회", description = "전체 제출 데이터를 페이지네이션, 필터링, 정렬하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<PageResponse<SubmissionListResponse>> getSubmissions(
            @Parameter(description = "검색 키워드 (현장명, 작성자)") @RequestParam(required = false) String keyword,
            @Parameter(description = "상태 필터 (SUBMITTED, APPROVED, REJECTED, DELETED)") @RequestParam(required = false) SubmissionStatus status,
            @Parameter(description = "활동 유형 필터") @RequestParam(required = false) ActivityType activityType,
            @Parameter(description = "시작 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 필드") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<SubmissionListResponse> result = submissionQueryService.getSubmissions(
                keyword,
                status,
                activityType,
                startDate,
                endDate,
                pageable
        );

        return ApiData.ok(result);
    }

    /**
     * Admin 제출 상세 조회.
     */
    @GetMapping("/{submissionId}")
    @Operation(summary = "Admin 제출 상세 조회", description = "특정 제출 데이터의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 제출 데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<SubmissionDetailResponse> getSubmissionDetail(
            @Parameter(description = "제출 ID", required = true)
            @PathVariable Long submissionId
    ) {
        SubmissionDetailResponse response = submissionQueryService.getSubmissionDetail(submissionId);
        return ApiData.ok(response);
    }

    /**
     * Admin 검수 로그 조회.
     */
    @GetMapping("/{submissionId}/logs")
    @Operation(summary = "검수 로그 조회", description = "특정 제출 데이터의 상태 변경 이력을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 제출 데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<List<AuditLogResponse>> getSubmissionLogs(
            @Parameter(description = "제출 ID", required = true)
            @PathVariable Long submissionId
    ) {
        List<AuditLogResponse> logs = submissionQueryService.getSubmissionLogs(submissionId);
        return ApiData.ok(logs);
    }

    /**
     * Admin 단건 승인.
     */
    @PostMapping("/{submissionId}/approve")
    @Operation(summary = "단건 승인", description = "특정 제출 데이터를 승인(APPROVED) 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 승인/반려/삭제된 상태라 변경 불가"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<SubmissionDetailResponse> approveSubmission(
            @Parameter(description = "제출 ID", required = true)
            @PathVariable Long submissionId
    ) {
        SubmissionDetailResponse response = submissionCommandService.approveSubmission(submissionId);
        return ApiData.ok(response);
    }

    /**
     * Admin 단건 반려.
     */
    @PostMapping("/{submissionId}/reject")
    @Operation(summary = "단건 반려", description = "특정 제출 데이터를 반려(REJECTED) 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공"),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 승인/반려/삭제된 상태라 변경 불가"),
            @ApiResponse(responseCode = "422", description = "반려 사유 미입력"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<SubmissionDetailResponse> rejectSubmission(
            @Parameter(description = "제출 ID", required = true)
            @PathVariable Long submissionId,
            @RequestBody @Valid SingleRejectRequest request
    ) {
        SubmissionDetailResponse response = submissionCommandService.rejectSubmission(submissionId, request);
        return ApiData.ok(response);
    }

    /**
     * Admin 단건 삭제.
     */
    @DeleteMapping("/{submissionId:[0-9]+}")
    @Operation(summary = "단건 삭제", description = "특정 제출 데이터를 영구 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ResponseEntity<Void> deleteSubmission(
            @Parameter(description = "제출 ID", required = true)
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
     * Admin 일괄 승인.
     */
    @PostMapping("/bulk/approve")
    @Operation(summary = "일괄 승인", description = "여러 제출 데이터를 한 번에 승인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일괄 승인 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<BulkApproveResponse> bulkApprove(
            @RequestBody @Valid BulkApproveRequest request
    ) {
        BulkApproveResponse response = submissionCommandService.bulkApprove(request);
        return ApiData.ok(response);
    }

    /**
     * Admin 일괄 반려.
     */
    @PostMapping("/bulk/reject")
    @Operation(summary = "일괄 반려", description = "여러 제출 데이터를 한 번에 반려합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일괄 반려 완료"),
            @ApiResponse(responseCode = "422", description = "반려 사유 미입력"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<BulkRejectResponse> bulkReject(
            @RequestBody @Valid BulkRejectRequest request
    ) {
        BulkRejectResponse response = submissionCommandService.bulkReject(request);
        return ApiData.ok(response);
    }

    /**
     * Admin 일괄 삭제.
     */
    @DeleteMapping("/bulk")
    @Operation(summary = "일괄 삭제", description = "여러 제출 데이터를 한 번에 영구 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일괄 삭제 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "Admin 권한 없음")
    })
    public ApiData<BulkDeleteResponse> bulkDelete(
            @RequestBody @Valid BulkDeleteRequest request
    ) {
        BulkDeleteResponse response = submissionCommandService.bulkDelete(request);
        return ApiData.ok(response);
    }
}