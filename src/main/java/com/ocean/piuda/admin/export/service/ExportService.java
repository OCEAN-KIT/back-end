package com.ocean.piuda.admin.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.admin.common.enums.ExportFormat;
import com.ocean.piuda.admin.common.enums.ExportStatus;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.export.dto.request.ExportRequest;
import com.ocean.piuda.admin.export.dto.response.ExportJobResponse;
import com.ocean.piuda.admin.export.entity.ExportJob;
import com.ocean.piuda.admin.export.repository.ExportJobRepository;
import com.ocean.piuda.admin.submission.entity.Submission;
import com.ocean.piuda.admin.submission.repository.SubmissionRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final ExportJobRepository exportJobRepository;
    private final SubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private List<Submission> getSubmissionsForExport(ExportRequest request) {
        try {
            ExportRequest.ExportFilters filters = request.getFilters();

            LocalDateTime startDate = filters != null && filters.getDateFrom() != null 
                    ? filters.getDateFrom().atStartOfDay() 
                    : null;
            LocalDateTime endDate = filters != null && filters.getDateTo() != null 
                    ? filters.getDateTo().atTime(23, 59, 59) 
                    : null;

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

    private byte[] generateCSV(List<Submission> submissions) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);

        writer.write("제출ID,현장명,활동유형,제출일,작성자,이메일,위도,경도,수심(m),수온(°C),시야(m),날씨,조류상태,참여인원,대표자명,역할,세부내용,수거량,활동후기,첨부파일수\n");

        for (Submission submission : submissions) {
            writer.write(String.format("%d,", submission.getSubmissionId()));
            writer.write(escapeCsv(submission.getSiteName()) + ",");
            writer.write(submission.getActivityType().name() + ",");
            writer.write(submission.getSubmittedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ",");
            writer.write(escapeCsv(submission.getAuthorName()) + ",");
            writer.write(escapeCsv(submission.getAuthorEmail() != null ? submission.getAuthorEmail() : "") + ",");
            writer.write(submission.getLatitude() != null ? submission.getLatitude().toString() : "" + ",");
            writer.write(submission.getLongitude() != null ? submission.getLongitude().toString() : "" + ",");

            if (submission.getBasicEnv() != null) {
                writer.write(submission.getBasicEnv().getDepthM() != null ? submission.getBasicEnv().getDepthM().toString() : "");
                writer.write(",");
                writer.write(submission.getBasicEnv().getWaterTempC() != null ? submission.getBasicEnv().getWaterTempC().toString() : "");
                writer.write(",");
                writer.write(submission.getBasicEnv().getVisibilityM() != null ? submission.getBasicEnv().getVisibilityM().toString() : "");
                writer.write(",");
                writer.write(submission.getBasicEnv().getWeather() != null ? submission.getBasicEnv().getWeather().name() : "");
                writer.write(",");
                writer.write(submission.getBasicEnv().getCurrentState() != null ? submission.getBasicEnv().getCurrentState().name() : "");
                writer.write(",");
            } else {
                writer.write(",,,,,");
            }

            if (submission.getParticipants() != null) {
                writer.write(submission.getParticipants().getParticipantCount() != null ? submission.getParticipants().getParticipantCount().toString() : "");
                writer.write(",");
                writer.write(escapeCsv(submission.getParticipants().getLeaderName() != null ? submission.getParticipants().getLeaderName() : "") + ",");
                writer.write(submission.getParticipants().getRole() != null ? submission.getParticipants().getRole().name() : "");
                writer.write(",");
            } else {
                writer.write(",,,");
            }

            if (submission.getActivity() != null) {
                writer.write(escapeCsv(submission.getActivity().getDetails() != null ? submission.getActivity().getDetails() : "") + ",");
                writer.write(submission.getActivity().getCollectionAmount() != null ? submission.getActivity().getCollectionAmount().toString() : "");
                writer.write(",");
            } else {
                writer.write(",,");
            }

            writer.write(escapeCsv(submission.getFeedbackText() != null ? submission.getFeedbackText() : "") + ",");
            writer.write(submission.getAttachmentCount() != null ? submission.getAttachmentCount().toString() : "");
            writer.write("\n");
        }

        writer.flush();
        return baos.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public String generateFileName(ExportFormat format) {
        String extension = format == ExportFormat.CSV ? ".csv" : ".xlsx";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("submissions_export_%s%s", timestamp, extension);
    }

    @Transactional(readOnly = true)
    public List<ExportJobResponse> getExportHistory() {
        return exportJobRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ExportJobResponse::from)
                .collect(Collectors.toList());
    }
}
