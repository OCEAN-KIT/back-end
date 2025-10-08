package com.ocean.piuda.ai.gemini.service;


import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.ocean.piuda.ai.gemini.dto.request.GeminiRequest;
import com.ocean.piuda.ai.gemini.dto.response.GeminiMeta;
import com.ocean.piuda.ai.gemini.dto.response.GeminiResponse;
import com.ocean.piuda.ai.gemini.util.GeminiSdkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiTextService {

    private final Client client;

    @Value("${gemini.model.text}")
    private String defaultTextModel;


    private static final String SYS_TEXT =
            "너는 신뢰할 수 있는 AI 어시스턴트다. 사실과 논리를 중시하고, 모르는 것은 모른다고 말하라.";


    public GeminiResponse complete(GeminiRequest req) {
        String model = defaultTextModel;
        GenerateContentResponse resp = client.models.generateContent(model, req.prompt(), null);

        String text = GeminiSdkUtil.extractText(resp);
        GeminiMeta meta = GeminiSdkUtil.toMeta(model, resp);
        return GeminiResponse.builder().content(text).meta(meta).build();
    }
}
