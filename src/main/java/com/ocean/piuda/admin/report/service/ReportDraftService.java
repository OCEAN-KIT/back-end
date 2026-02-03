package com.ocean.piuda.admin.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.admin.common.enums.ActivityType;
import com.ocean.piuda.admin.common.enums.SubmissionStatus;
import com.ocean.piuda.admin.report.dto.request.ReportDraftByIdsRequest;
import com.ocean.piuda.admin.report.dto.request.ReportDraftByPeriodRequest;
import com.ocean.piuda.admin.report.dto.response.ReportDraftResponse;
import com.ocean.piuda.admin.report.dto.response.ReportPdfResponse;
import com.ocean.piuda.admin.report.enums.ReportDraftType;
import com.ocean.piuda.admin.report.util.ReportPromptBuilder;
import com.ocean.piuda.admin.submission.entity.Submission;
import com.ocean.piuda.admin.submission.repository.SubmissionRepository;
import com.ocean.piuda.ai.gemini.dto.response.GeminiMeta;
import com.ocean.piuda.ai.gemini.dto.response.GeminiResponse;
import com.ocean.piuda.ai.gemini.service.GeminiTextService;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDraftService {

    private final GeminiTextService geminiTextService;
    private final SubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper;
    private final ReportPdfService reportPdfService;

    @Value("${gemini.model.report:${gemini.model.text}}")
    private String reportModel;

    public ReportDraftResponse generateByIds(ReportDraftByIdsRequest request) {
        List<Long> ids = distinctPreserveOrder(request.ids());
        List<Submission> found =
                submissionRepository.findAllByIdsAndStatusWithDetails(ids, SubmissionStatus.APPROVED);

        // 요청 순서 보존 정렬
        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) order.put(ids.get(i), i);
        found.sort(Comparator.comparingInt(s -> order.getOrDefault(s.getSubmissionId(), Integer.MAX_VALUE)));

        List<Long> foundIds = found.stream().map(Submission::getSubmissionId).toList();
        List<Long> missing = ids.stream().filter(id -> !foundIds.contains(id)).toList();

        //  전부 미존재/미승인 이면 “리포트 대상 없음”으로 명확히
        if (found.isEmpty()) {
            throw new BusinessException(ExceptionType.REPORT_DRAFT_SOURCE_EMPTY, Map.of(
                    "reason", "선택한 IDs 중 APPROVED submission이 없습니다.",
                    "requestedIds", ids,
                    "missingIds", missing
            ));
        }

        return generate(found, foundIds, missing, request.prompt(), request.reportType());
    }

    public ReportDraftResponse generateByPeriod(ReportDraftByPeriodRequest request) {
        LocalDateTime start = request.dateFrom().atStartOfDay();
        LocalDateTime end = request.dateTo().atTime(23, 59, 59);

        List<Submission> found =
                submissionRepository.findAllByStatusAndSubmittedAtBetweenWithDetails(
                        SubmissionStatus.APPROVED, start, end
                );

        //  기간에 데이터 없으면 별도 에러
        if (found == null || found.isEmpty()) {
            throw new BusinessException(ExceptionType.REPORT_DRAFT_SOURCE_EMPTY, Map.of(
                    "reason", "선택한 기간에 해당하는 APPROVED submission이 없습니다.",
                    "dateFrom", request.dateFrom(),
                    "dateTo", request.dateTo()
            ));
        }

        List<Long> foundIds = found.stream().map(Submission::getSubmissionId).toList();
        return generate(found, foundIds, List.of(), request.prompt(), request.reportType());
    }

    private ReportDraftResponse generate(
            List<Submission> submissions,
            List<Long> submissionIds,
            List<Long> missingIds,
            String extraPrompt,
            ReportDraftType reportType
    ) {
        if (reportType == null) {
            throw new BusinessException(
                    ExceptionType.NOT_VALID_REQUEST_FIELDS_ERROR,
                    Map.of("reason", "reportType은 필수입니다.")
            );
        }

        // 너무 많으면 프롬프트 폭증 방지 (필요시 조정)
        List<Submission> trimmed = submissions.size() > 50 ? submissions.subList(0, 50) : submissions;

        String submissionsJson = toCompactJson(trimmed);

        String prompt = ReportPromptBuilder.build(submissionsJson, extraPrompt, reportType);

        GeminiResponse gemResp = geminiTextService.generateReportDraft(prompt);
        String raw = gemResp.content();
        GeminiMeta meta = gemResp.meta();
        String cleaned = cleanupJson(raw);

        String content;
        try {
            JsonNode root = objectMapper.readTree(cleaned);
            content = root.path(reportType.jsonKey()).asText("");
        } catch (Exception e) {
            log.warn("Gemini report JSON parse failed. raw={}", raw, e);
            content = raw == null ? "" : raw;
        }

        List<Long> usedIds = trimmed.stream().map(Submission::getSubmissionId).toList();

        ReportDraftResponse.ReportDraftResponseBuilder builder = ReportDraftResponse.builder()
                .submissionIds(usedIds)
                .missingIds(missingIds)
                .reportType(reportType)
                .meta(meta);

        switch (reportType) {
            case INTERNAL_DRAFT -> builder.internalDraft(content);
            case EXTERNAL_NEWSLETTER -> builder.externalNewsletter(content);
            case EXTERNAL_INSTAGRAM -> builder.externalInstagram(content);
            case EXTERNAL_PUBLICATION -> builder.externalPublication(content);
        }

        return builder.build();
    }

    private String toCompactJson(List<Submission> submissions) {
        try {
            List<Map<String, Object>> rows = submissions.stream()
                    .map(this::toPromptRow)
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(rows);
        } catch (Exception e) {
            throw new BusinessException(
                    ExceptionType.UNEXPECTED_SERVER_ERROR,
                    Map.of("reason", "submissions JSON 변환 실패", "message", e.getMessage())
            );
        }
    }

    private Map<String, Object> toPromptRow(Submission s) {
        Map<String, Object> m = new LinkedHashMap<>();

        putIfNotBlank(m, "siteName", s.getSiteName());
        putIfNotNull(m, "submittedAt", s.getSubmittedAt() != null ? s.getSubmittedAt().toString() : null);
        putIfNotNull(m, "activityType", s.getActivityType() != null ? s.getActivityType().name() : null);

        if (s.getBasicEnv() != null) {
            Map<String, Object> env = new LinkedHashMap<>();
            putIfNotNull(env, "waterTempC", s.getBasicEnv().getWaterTempC());
            putIfNotNull(env, "visibilityM", s.getBasicEnv().getVisibilityM());
            putIfNotNull(env, "depthM", s.getBasicEnv().getDepthM());
            putIfNotNull(env, "currentState", s.getBasicEnv().getCurrentState() != null ? s.getBasicEnv().getCurrentState().name() : null);
            putIfNotNull(env, "weather", s.getBasicEnv().getWeather() != null ? s.getBasicEnv().getWeather().name() : null);

            removeNullsDeep(env);
            if (!env.isEmpty()) m.put("basicEnv", env);
        }

        if (s.getActivity() != null) {
            Map<String, Object> a = new LinkedHashMap<>();
            putIfNotNull(a, "type", s.getActivity().getType() != null ? s.getActivity().getType().name() : null);
            putIfNotBlank(a, "details", truncate(s.getActivity().getDetails(), 600));
            putIfNotNull(a, "collectionAmount", s.getActivity().getCollectionAmount());
            putIfNotNull(a, "durationHours", s.getActivity().getDurationHours());

            ActivityType type = s.getActivity().getType();
            if (type == ActivityType.TRANSPLANT || type == ActivityType.MONITORING || type == ActivityType.RESEARCH) {
                putIfNotNull(a, "healthGrade", s.getActivity().getHealthGrade() != null ? s.getActivity().getHealthGrade().name() : null);
                putIfNotNull(a, "growthCm", s.getActivity().getGrowthCm());

                if (s.getActivity().getNaturalReproduction() != null) {
                    Map<String, Object> nat = new LinkedHashMap<>();
                    putIfNotNull(nat, "radiusM", s.getActivity().getNaturalReproduction().getRadiusM());
                    putIfNotNull(nat, "numerator", s.getActivity().getNaturalReproduction().getNumerator());
                    putIfNotNull(nat, "denominator", s.getActivity().getNaturalReproduction().getDenominator());
                    removeNullsDeep(nat);
                    if (!nat.isEmpty()) a.put("naturalReproduction", nat);
                }

                if (s.getActivity().getSurvival() != null) {
                    Map<String, Object> surv = new LinkedHashMap<>();
                    putIfNotNull(surv, "dieCount", s.getActivity().getSurvival().getDieCount());
                    putIfNotNull(surv, "totalCount", s.getActivity().getSurvival().getTotalCount());
                    removeNullsDeep(surv);
                    if (!surv.isEmpty()) a.put("survival", surv);
                }
            }

            removeNullsDeep(a);
            if (!a.isEmpty()) m.put("activity", a);
        }

        putIfNotBlank(m, "feedbackText", truncate(s.getWorkDescription(), 600));  // API 하위 호환성을 위해 키명 유지

        removeNullsDeep(m);
        return m;
    }

    private static void putIfNotNull(Map<String, Object> m, String key, Object value) {
        if (value != null) m.put(key, value);
    }

    private static void putIfNotBlank(Map<String, Object> m, String key, String value) {
        if (value != null && !value.isBlank()) m.put(key, value);
    }

    @SuppressWarnings("unchecked")
    private static void removeNullsDeep(Map<String, Object> m) {
        if (m == null) return;
        m.entrySet().removeIf(e -> e.getValue() == null);

        for (Object v : m.values()) {
            if (v instanceof Map<?, ?> mm) {
                removeNullsDeep((Map<String, Object>) mm);
            }
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        String t = s.trim();
        if (t.length() <= max) return t;
        return t.substring(0, max) + "…";
    }

    private static String cleanupJson(String raw) {
        if (raw == null) return "";
        String t = raw.trim();
        t = t.replaceAll("^```json\\s*", "").replaceAll("^```\\s*", "");
        t = t.replaceAll("\\s*```$", "");
        return t.trim();
    }

    private static List<Long> distinctPreserveOrder(List<Long> ids) {
        if (ids == null) return List.of();
        LinkedHashSet<Long> set = new LinkedHashSet<>(ids);
        return new ArrayList<>(set);
    }

    public ReportPdfResponse generatePdfByIds(ReportDraftByIdsRequest request) {
        ReportDraftResponse draft = generateByIds(request);
        return toPdf(draft);
    }

    public ReportPdfResponse generatePdfByPeriod(ReportDraftByPeriodRequest request) {
        ReportDraftResponse draft = generateByPeriod(request);
        return toPdf(draft);
    }

    private ReportPdfResponse toPdf(ReportDraftResponse draft) {
        String content = switch (draft.reportType()) {
            case INTERNAL_DRAFT -> draft.internalDraft();
            case EXTERNAL_NEWSLETTER -> draft.externalNewsletter();
            case EXTERNAL_INSTAGRAM -> draft.externalInstagram();
            case EXTERNAL_PUBLICATION -> draft.externalPublication();
        };

        String title = "Report - " + draft.reportType().name();
        byte[] pdfBytes = reportPdfService.renderMarkdownToPdf(content, title);

        String fileName = "report_" + draft.reportType().name().toLowerCase() + "_" +
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".pdf";

        return new ReportPdfResponse(fileName, pdfBytes);
    }
}
