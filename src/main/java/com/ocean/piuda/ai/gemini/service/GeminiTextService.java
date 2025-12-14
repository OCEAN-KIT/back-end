package com.ocean.piuda.ai.gemini.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.ocean.piuda.ai.gemini.dto.request.GeminiRequest;
import com.ocean.piuda.ai.gemini.dto.response.GeminiMeta;
import com.ocean.piuda.ai.gemini.dto.response.GeminiResponse;
import com.ocean.piuda.ai.gemini.util.GeminiSdkUtil;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiTextService {

    private final Client client;

    @Value("${gemini.model.text}")
    private String defaultTextModel;

    @Value("${gemini.model.report:${gemini.model.text}}")
    private String reportModel;

    private static final String SYS_TEXT =
            "너는 신뢰할 수 있는 AI 어시스턴트다. 사실과 논리를 중시하고, 모르는 것은 모른다고 말하라.";

    private static final String SYS_REPORT =
            "너는 해양 활동 기록 제출물 기반 리포트 초안 작성 AI다. " +
                    "주어진 데이터에 없는 사실을 만들지 말고, 개인정보(이메일/연락처)/정확 좌표는 대외용에 절대 포함하지 말라. " +
                    "반드시 지정된 JSON 스키마만 출력하라.";

    public GeminiResponse complete(GeminiRequest req) {
        return generate(defaultTextModel, SYS_TEXT, req.prompt());
    }

    public GeminiResponse generateReportDraft(String prompt) {
        return generate(reportModel, SYS_REPORT, prompt);
    }

    private GeminiResponse generate(String model, String systemInstruction, String userPrompt) {

        List<Content> contents = List.of(
                Content.builder()
                        .role("user")
                        .parts(Part.fromText(userPrompt == null ? "" : userPrompt))
                        .build()
        );

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(
                        Content.builder()
                                .role("user")
                                .parts(Part.fromText(systemInstruction))
                                .build()
                )
                .temperature(0.2f)
                .build();

        try {
            GenerateContentResponse resp = client.models.generateContent(model, contents, config);

            String text = GeminiSdkUtil.extractText(resp);
            GeminiMeta meta = GeminiSdkUtil.toMeta(model, resp);
            return GeminiResponse.builder().content(text).meta(meta).build();

        } catch (Exception e) {
            //  원문 메시지 그대로 전달
            String rawMessage = (e.getMessage() == null ? "" : e.getMessage());

            //  statusCode는 "추론" 말고, SDK 예외가 제공하는 값이 있으면 그걸 사용 (없으면 null)
            Integer statusCode = extractStatusCodeIfPresent(e);

            ExceptionType type =
                    (statusCode != null && statusCode == 429)
                            ? ExceptionType.AI_RATE_LIMIT
                            : ExceptionType.AI_GEMINI_ERROR;

            log.warn("Gemini error. type={}, statusCode={}, model={}, exClass={}, message={}",
                    type.name(), statusCode, model, e.getClass().getName(), rawMessage);

            throw new BusinessException(type, Map.of(
                    "provider", "gemini",
                    "model", model,
                    "statusCode", statusCode,
                    "message", rawMessage,
                    "exceptionClass", e.getClass().getName()
            ));
        }
    }

    /**
     * google-genai 라이브러리 버전별로 status code 접근 메서드명이 다를 수 있어서
     * "추론"이 아닌 리플렉션 기반으로 '존재하면 가져오기'만 합니다.
     */
    private Integer extractStatusCodeIfPresent(Exception e) {
        // 후보 메서드들: getCode(), getStatusCode(), statusCode() 등
        for (String method : List.of("getCode", "getStatusCode", "statusCode")) {
            try {
                Object v = e.getClass().getMethod(method).invoke(e);
                if (v instanceof Integer i) return i;
                if (v instanceof Number n) return n.intValue();
            } catch (Exception ignore) {
            }
        }
        return null;
    }
}
