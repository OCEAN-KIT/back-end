package com.ocean.piuda.admin.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.admin.export.enums.ExportFormat;
import com.ocean.piuda.admin.export.enums.ExportStatus;
import com.ocean.piuda.submission.enums.SubmissionStatus;
import com.ocean.piuda.admin.export.dto.request.ExportByIdsRequest;
import com.ocean.piuda.admin.export.dto.request.ExportRequest;
import com.ocean.piuda.admin.export.dto.response.ExportJobResponse;
import com.ocean.piuda.admin.export.entity.ExportJob;
import com.ocean.piuda.admin.export.repository.ExportJobRepository;
import com.ocean.piuda.submission.entity.*;
import com.ocean.piuda.submission.repository.SubmissionRepository;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final ExportJobRepository exportJobRepository;
    private final SubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper;

    /**
     * 필터 조건에 따른 CSV 파일 생성 (전체 다운로드)
     */
    @Transactional(readOnly = true)
    public byte[] generateExportFile(ExportRequest request) {
        try {
            List<Submission> submissions = getSubmissionsForExport(request);
            return generateCSV(submissions);
        } catch (Exception e) {
            log.error("Export 파일 생성 실패: {}", e.getMessage(), e);
            throw new BusinessException(ExceptionType.EXPORT_FAILED);
        }
    }

    /**
     * ID 목록에 따른 CSV 파일 생성 (선택 다운로드)
     */
    @Transactional(readOnly = true)
    public byte[] generateExportFileByIds(ExportByIdsRequest request) {
        try {
            List<Submission> submissions = getSubmissionsForExportByIds(request.getIds());

            // 요청한 ids 순서대로 정렬 (사용자가 선택한 순서 유지)
            Map<Long, Integer> order = new HashMap<>();
            for (int i = 0; i < request.getIds().size(); i++) {
                order.put(request.getIds().get(i), i);
            }
            submissions.sort(Comparator.comparingInt(s -> order.getOrDefault(s.getSubmissionId(), Integer.MAX_VALUE)));

            return generateCSV(submissions);
        } catch (Exception e) {
            log.error("Export(IDs) 파일 생성 실패: {}", e.getMessage(), e);
            throw new BusinessException(ExceptionType.EXPORT_FAILED);
        }
    }

    /**
     * Export 이력 저장 (전체 다운로드용)
     */
    @Transactional
    public ExportJobResponse saveExportHistory(ExportRequest request, String requestedBy) {
        try {
            String filtersJson = request.getFilters() != null
                    ? objectMapper.writeValueAsString(request.getFilters())
                    : "{}";

            ExportJob exportJob = ExportJob.builder()
                    .requestedBy(requestedBy)
                    .format(request.getFormat())
                    .status(ExportStatus.READY)
                    .filtersJson(filtersJson)
                    .completedAt(LocalDateTime.now())
                    .build();

            ExportJob saved = exportJobRepository.save(exportJob);
            return ExportJobResponse.from(saved);
        } catch (Exception e) {
            log.error("Export 이력 저장 실패: {}", e.getMessage(), e);
            throw new BusinessException(ExceptionType.EXPORT_FAILED);
        }
    }

    /**
     * Export 이력 저장 (ID 기반 다운로드용)
     */
    @Transactional
    public ExportJobResponse saveExportHistory(ExportByIdsRequest request, String requestedBy) {
        try {
            String filtersJson = objectMapper.writeValueAsString(
                    Map.of("ids", request.getIds())
            );

            ExportJob exportJob = ExportJob.builder()
                    .requestedBy(requestedBy)
                    .format(request.getFormat())
                    .status(ExportStatus.READY)
                    .filtersJson(filtersJson)
                    .completedAt(LocalDateTime.now())
                    .build();

            ExportJob saved = exportJobRepository.save(exportJob);
            return ExportJobResponse.from(saved);
        } catch (Exception e) {
            log.error("Export(IDs) 이력 저장 실패: {}", e.getMessage(), e);
            throw new BusinessException(ExceptionType.EXPORT_FAILED);
        }
    }

    /**
     * Export 이력 조회
     */
    @Transactional(readOnly = true)
    public List<ExportJobResponse> getExportHistory() {
        return exportJobRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ExportJobResponse::from)
                .collect(Collectors.toList());
    }

    // =================================================================================
    //  Internal Helper Methods
    // =================================================================================

    /**
     * 필터 조건으로 Submission 목록 조회
     */
    private List<Submission> getSubmissionsForExport(ExportRequest request) {
        try {
            ExportRequest.ExportFilters filters = request.getFilters();

            LocalDateTime startDate = filters != null && filters.getDateFrom() != null
                    ? filters.getDateFrom().atStartOfDay()
                    : null;
            LocalDateTime endDate = filters != null && filters.getDateTo() != null
                    ? filters.getDateTo().atTime(23, 59, 59)
                    : null;

            // 페이징 없이 전체 조회 (unpaged)
            return submissionRepository.findWithFilters(
                    null,
                    SubmissionStatus.APPROVED,
                    null,
                    startDate,
                    endDate,
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();

        } catch (Exception e) {
            log.error("제출 데이터 조회 실패: {}", e.getMessage(), e);
            return submissionRepository.findAll().stream()
                    .filter(s -> s.getStatus() == SubmissionStatus.APPROVED)
                    .collect(Collectors.toList());
        }
    }

    /**
     * ID 목록으로 Submission 목록 조회 (상세 정보 포함)
     */
    private List<Submission> getSubmissionsForExportByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        // N+1 문제를 방지하기 위해 Fetch Join이 적용된 쿼리 사용
        return submissionRepository.findAllByIdsAndStatusWithDetails(ids, SubmissionStatus.APPROVED);
    }

    /**
     * CSV 파일 바이트 생성 로직 (핵심)
     */
    private byte[] generateCSV(List<Submission> submissions) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 1) UTF-8 BOM 추가 (Excel에서 한글 깨짐 방지)
        baos.write(0xEF); baos.write(0xBB); baos.write(0xBF);

        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            // 2) Excel용 구분자 힌트
            writer.write("sep=,\r\n");

            // 3) Header 작성 (기획서 순서 반영)
            // 기본정보: 현장명, 날짜, 회차, 활동유형, 공동작업자
            // 환경정보: 평균수심, 최대수심, 수온, 시야, 파도, 서지, 조류
            // 상세정보: 활동상세내용(Type Specific - 모든 필드 포함), 수거량/규모
            // 기타: 작업내용(후기), 첨부파일수, 작성자, 이메일, 제출일시
            writer.write("제출ID,현장명,날짜,회차,활동유형,공동작업자," +
                    "평균수심(m),최대수심(m),수온(°C),시야,파도,서지,조류," +
                    "활동상세내용(Type Specific),수거량/규모," +
                    "작업내용(Description),첨부파일수,작성자,이메일,제출일시\r\n");

            DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (Submission submission : submissions) {
                // --- [1] 기본 정보 ---
                writer.write(submission.getSubmissionId() + ",");
                writer.write(escapeCsv(submission.getSiteName()) + ",");
                writer.write((submission.getRecordDate() != null ? submission.getRecordDate().toString() : "") + ",");
                writer.write(submission.getDivingRound() + ",");
                writer.write((submission.getActivityType() != null ? submission.getActivityType().name() : "") + ",");
                writer.write(escapeCsv(submission.getParticipantNames()) + ",");

                // --- [2] 공통 환경 기록 ---
                if (submission.getBasicEnv() != null) {
                    BasicEnv env = submission.getBasicEnv();
                    writer.write((env.getAvgDepthM() != null ? env.getAvgDepthM().toString() : "") + ",");
                    writer.write((env.getMaxDepthM() != null ? env.getMaxDepthM().toString() : "") + ",");
                    writer.write((env.getWaterTempC() != null ? env.getWaterTempC().toString() : "") + ",");
                    writer.write((env.getVisibilityStatus() != null ? env.getVisibilityStatus().name() : "") + ",");
                    writer.write((env.getWaveStatus() != null ? env.getWaveStatus().name() : "") + ",");
                    writer.write((env.getSurgeStatus() != null ? env.getSurgeStatus().name() : "") + ",");
                    writer.write((env.getCurrentStatus() != null ? env.getCurrentStatus().name() : "") + ",");
                } else {
                    writer.write(",,,,,,,"); // 환경정보 7개 필드 공란 처리
                }

                // --- [3] 유형별 활동 상세 기록 (조건부) ---
                String activityDetails = "";
                String amountOrScale = "";

                if (submission.getActivityTransplant() != null) {
                    // [이식] 대상종, 장소, 방식, 건강상태
                    ActivityTransplant t = submission.getActivityTransplant();
                    activityDetails = String.format("대상:%s, 장소:%s, 방식:%s, 건강상태:%s",
                            t.getSpeciesType(), t.getLocationType(), t.getMethodType(), t.getHealthStatus());
                    amountOrScale = t.getScale(); // 이식 규모

                } else if (submission.getActivityGrazerRemoval() != null) {
                    // [조식동물] 대상생물(List), 밀도, 범위, 비고
                    ActivityGrazerRemoval g = submission.getActivityGrazerRemoval();
                    String targets = g.getTargetSpecies() != null ? g.getTargetSpecies().toString() : "[]";
                    activityDetails = String.format("대상:%s, 밀도:%s, 범위:%s(%s)",
                            targets, g.getDensityBeforeWork(), g.getWorkScope(),
                            g.getNote() != null ? g.getNote() : "");
                    amountOrScale = g.getCollectionAmount(); // 수거량

                } else if (submission.getActivitySubstrateImprovement() != null) {
                    // [부착기질] 대상, 기질상태
                    ActivitySubstrateImprovement s = submission.getActivitySubstrateImprovement();
                    activityDetails = String.format("대상:%s, 기질상태:%s",
                            s.getTargetType(), s.getSubstrateState());
                    amountOrScale = s.getWorkScope(); // 작업 범위

                } else if (submission.getActivityMonitoring() != null) {
                    // [모니터링] 적지조사(좌표,방위,지형,갯녹음,분포,암반,적합성) + 해조류(ID,상태,정밀측정)
                    ActivityMonitoring m = submission.getActivityMonitoring();
                    StringBuilder sb = new StringBuilder();

                    // a. 적지조사
                    sb.append(String.format("[적지조사] 입수:%s, 출수:%s, 방위:%s, 지형:%s, 갯녹음:%s, 조식동물분포:%s, 암반:%s, 적합성:%s",
                            m.getEntryCoordinate(), m.getExitCoordinate(), m.getDirection(),
                            m.getTerrain(), m.getBarrenExtent(), m.getGrazerDistribution(),
                            m.getRockFeatures() != null ? m.getRockFeatures().toString() : "[]",
                            m.getSuitability()));

                    // b. 해조류 상태
                    sb.append(String.format(" / [해조류] ID:%s, 상태:%s",
                            m.getSeaweedIdNumber(), m.getSeaweedHealthStatus()));

                    // 정밀 측정 (값이 있을 때만 표시)
                    if (m.getLeafLength() != null || m.getMaxLeafWidth() != null) {
                        sb.append(String.format(", 엽장:%s, 엽폭:%s", m.getLeafLength(), m.getMaxLeafWidth()));
                    }
                    activityDetails = sb.toString();
                    amountOrScale = "-"; // 모니터링은 별도 수거량 없음

                } else if (submission.getActivityMarineCleanup() != null) {
                    // [해양정화] 폐기물유형, 방식, 미수거규모
                    ActivityMarineCleanup c = submission.getActivityMarineCleanup();
                    String wastes = c.getWasteTypes() != null ? c.getWasteTypes().toString() : "[]";
                    activityDetails = String.format("유형:%s, 방식:%s, 미수거규모:%s",
                            wastes, c.getMethod(), c.getUncollectedScale());
                    amountOrScale = c.getCollectionAmount(); // 수거량
                }

                writer.write(escapeCsv(activityDetails) + ",");
                writer.write(escapeCsv(amountOrScale) + ",");

                // --- [4] 기타 정보 ---
                writer.write(escapeCsv(submission.getWorkDescription() != null ? submission.getWorkDescription() : "") + ",");
                writer.write((submission.getAttachmentCount() != null ? submission.getAttachmentCount().toString() : "0") + ",");
                writer.write(escapeCsv(submission.getAuthorName()) + ",");
                writer.write(escapeCsv(submission.getAuthorEmail() != null ? submission.getAuthorEmail() : "") + ",");
                writer.write((submission.getSubmittedAt() != null ? submission.getSubmittedAt().format(dtf) : ""));

                writer.write("\r\n"); // 행 끝
            }
            writer.flush();
        }

        return baos.toByteArray();
    }

    /**
     * CSV 특수문자 처리 (따옴표, 콤마, 개행)
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 파일명 생성 유틸
     */
    public String generateFileName(ExportFormat format) {
        String extension = format == ExportFormat.CSV ? ".csv" : ".xlsx";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("submissions_export_%s%s", timestamp, extension);
    }
}
