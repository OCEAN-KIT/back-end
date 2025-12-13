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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

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

        // role은 user/model만 허용 → user로만 content 구성
        List<Content> contents = List.of(
                Content.builder()
                        .role("user")
                        .parts(Part.fromText(userPrompt == null ? "" : userPrompt))
                        .build()
        );

        // system은 config.systemInstruction으로
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(
                        Content.builder()
                                .role("user") // (여기도 user/model만 허용이라 user로 넣습니다)
                                .parts(Part.fromText(systemInstruction))
                                .build()
                )
                .temperature(0.2f)
                .build();

        // 재시도는 원하면 여기서 감싸면 됨(5xx만)
        GenerateContentResponse resp = client.models.generateContent(model, contents, config);

        String text = GeminiSdkUtil.extractText(resp);
        GeminiMeta meta = GeminiSdkUtil.toMeta(model, resp);
        return GeminiResponse.builder().content(text).meta(meta).build();
    }
}
