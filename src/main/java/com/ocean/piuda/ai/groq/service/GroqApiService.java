package com.ocean.piuda.ai.groq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ocean.piuda.ai.groq.dto.request.EvaluateHarmfulnessRequest;
import com.ocean.piuda.ai.groq.dto.request.GroqPromptRequest;
import com.ocean.piuda.ai.groq.dto.response.EvaluateHarmfulnessResponse;
import com.ocean.piuda.ai.groq.dto.response.GroqMeta;
import com.ocean.piuda.ai.groq.dto.response.GroqPromptResponse;
import com.ocean.piuda.ai.groq.util.GroqApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqApiService {

    private final RestTemplate restTemplate;
    private final GroqApiUtil groqApiUtil;

    @Value("${groq.api.key}")
    private String apiKey;

    /** 디폴트 모델 */
    @Value("${groq.api.model}")
    private String defaultModel;
    private String REAL_TIME_MODEL= "groq/compound";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String GROQ_CHAT_URL = "https://api.groq.com/openai/v1/chat/completions";

    /** 서버 고정 시스템 프롬프트(오프라인 모델용) */
    private static final String SYS_OFFLINE =
            "인터넷이나 최신 데이터에 직접 접근할 수 없다. 최신/실시간 정보를 요구받으면 "
                    + "'실시간 정보에 접근 불가'라고 분명히 알리고, 비실시간 일반 지식만 답하라. "
                    + "최종 답변 본문에는 절대 URL을 포함하지 말라.";

    /** 서버 고정 시스템 프롬프트(compound 모델용) */
    private static final String SYS_COMPOUND =
            "최신 정보가 필요한 경우 내장 웹검색/브라우저 도구를 사용해 사실을 검증하라. "
                    + "그러나 최종 답변 본문에는 URL을 포함하지 말고, 출처는 도구 실행 결과(executed_tools)에만 남겨라.";

    /** 유해성 평가: 항상 오프라인(툴 차단) */
    public EvaluateHarmfulnessResponse evaluateHarmfulness(EvaluateHarmfulnessRequest request) {
        final String model = defaultModel; // 고정
        final String userPrompt =
                "Return only a number from 0 to 10 indicating the degree of harmfulness of the text. "
                        + "Higher means more harmful. No explanations.\n\nText:\n"
                        + String.valueOf(request.sentence());

        ArrayNode messages = groqApiUtil.buildMessages(SYS_OFFLINE, userPrompt);
        ObjectNode payload = groqApiUtil.buildRequestPayload(model, messages);

        ResponseEntity<String> resp = groqApiUtil.postChat(payload);
        String raw = resp.getBody();

        int degree = -1;
        try {
            JsonNode rootNode = MAPPER.readTree(raw);
            String content = rootNode.path("choices").path(0).path("message").path("content").asText("");
            String digits = content.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) degree = Integer.parseInt(digits);
        } catch (Exception e) {
            log.warn("Failed to parse harmfulness score", e);
        }

        GroqMeta meta = groqApiUtil.buildMeta(resp, raw, model);
        return EvaluateHarmfulnessResponse.builder()
                .degree(degree)
                .meta(meta)
                .build();
    }

    /** 일반 프롬프트: useRealtime=true면 compound, 아니면 오프라인 */
    public GroqPromptResponse complete(GroqPromptRequest req) {
        final boolean useRealtime = Boolean.TRUE.equals(req.useRealtime());
        final String model = groqApiUtil.resolveModel(useRealtime);
        final String sysPrompt = groqApiUtil.resolveSystemPrompt(model);

        ArrayNode messages = groqApiUtil.buildMessages(sysPrompt, String.valueOf(req.prompt()));
        ObjectNode payload = groqApiUtil.buildRequestPayload(
                model, messages
        );

        ResponseEntity<String> resp = groqApiUtil.postChat(payload);
        String raw = resp.getBody();

        String content = groqApiUtil.extractCleanContent(raw); // URL 제거
        GroqMeta meta = groqApiUtil.buildMeta(resp, raw, model);

        return GroqPromptResponse.builder()
                .content(content)
                .meta(meta)
                .build();
    }

}
