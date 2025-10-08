package com.ocean.piuda.ai.openAI.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ocean.piuda.ai.openAI.dto.request.OpenAIRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class OpenAIApiService {
    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(30))
            .build();

    /** 재시도 없이 단 한 번만 호출하는 executeRequest */
    private String executeRequest(ObjectNode json) throws IOException {
        RequestBody body = RequestBody.create(JSON, MAPPER.writeValueAsBytes(json));

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody rb = response.body();
                return rb != null ? rb.string() : "";
            }
            int code = response.code();
            String respText = safeBody(response);
            log.warn("OpenAI non-2xx: code={}, body={}", code, respText);
            throw buildApiException(code, respText);
        }
    }

    /** 에러 본문 안전 추출 */
    private static String safeBody(Response response) {
        try {
            ResponseBody rb = response.body();
            return rb != null ? rb.string() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /** OpenAI 형식의 에러 본문을 읽어 의미 있는 메시지로 래핑 */
    private static IOException buildApiException(int code, String respText) {
        try {
            JsonNode node = MAPPER.readTree(respText == null ? "{}" : respText);
            String msg = node.path("error").path("message").asText("");
            String type = node.path("error").path("type").asText("");
            String param = node.path("error").path("param").asText("");
            String full = String.format("OpenAI API error %d: %s (type=%s, param=%s)", code, msg, type, param);
            return new IOException(full);
        } catch (Exception e) {
            return new IOException("OpenAI API error " + code + ": " + respText);
        }
    }

    /** 공통 페이로드 빌더 */
    private static ObjectNode basePayload(String model, Integer maxTokens) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model);
        ArrayNode messages = MAPPER.createArrayNode();
        root.set("messages", messages);
        if (maxTokens != null && maxTokens > 0) root.put("max_tokens", maxTokens);
        return root;
    }

    /** 메시지 노드 */
    private static ObjectNode message(String role, String content) {
        ObjectNode m = MAPPER.createObjectNode();
        m.put("role", role);
        m.put("content", content == null ? "" : content);
        return m;
    }

    // ===================== 퍼블릭 API =====================

    public String enhanceWriting(OpenAIRequest.Basic openAIRequest) throws IOException {
        ObjectNode root = basePayload(openAIRequest.getModel(), 512);
        ArrayNode messages = (ArrayNode) root.get("messages");
        messages.add(message("system",
                "You are a helpful assistant. Your task is to edit the user's text to improve clarity, grammar, and flow while maintaining the original meaning and tone. Do not change the context or style unless asked."));
        messages.add(message("user", Optional.ofNullable(openAIRequest.getPrompt()).orElse("")));
        log.info("Request Body: {}", root);
        return executeRequest(root);
    }

    public String complete(OpenAIRequest.Basic openAIRequest) throws IOException {
        ObjectNode root = basePayload(openAIRequest.getModel(), 512);
        ArrayNode messages = (ArrayNode) root.get("messages");
        messages.add(message("system", "You are a helpful assistant."));
        messages.add(message("user", Optional.ofNullable(openAIRequest.getPrompt()).orElse("")));
        log.info("Request Body: {}", root);
        return executeRequest(root);
    }

    /** gpt-4o-mini 고정 사용 */
    public String evaluateHarmfulness(String prompt) throws IOException {
        ObjectNode root = basePayload("gpt-4o-mini", 32);
        ArrayNode messages = (ArrayNode) root.get("messages");
        messages.add(message("system",
                "You are a content moderation assistant. Analyze the following text for any harmful, offensive, or inappropriate content. Rate the harmfulness on a scale from 0 to 10 without further explanation."));
        messages.add(message("user", Optional.ofNullable(prompt).orElse("")));
        log.info("Request Body for Harmfulness Evaluation: {}", root);
        String responseBody = executeRequest(root);
        return extractHarmfulnessRating(responseBody);
    }

    public String composeText(OpenAIRequest.TextCompose openAIRequest) throws IOException {
        String prompt = String.join(" ", openAIRequest.getSentences()).trim();
        ObjectNode root = basePayload(openAIRequest.getModel(), 512);
        ArrayNode messages = (ArrayNode) root.get("messages");
        messages.add(message("system",
                "You are a writing assistant. Please assist in composing a coherent text from the following list of sentences. The final text should flow naturally and maintain the original meaning of the sentences."));
        messages.add(message("user", prompt));
        log.info("Compose Text Request Body: {}", root);
        return executeRequest(root);
    }

    private String extractHarmfulnessRating(String responseBody) throws IOException {
        JsonNode rootNode = MAPPER.readTree(responseBody);
        String content = rootNode.path("choices").get(0).path("message").path("content").asText();
        String ratingText = content.replaceAll("[^0-9]", "");
        return ratingText.isEmpty() ? "0" : ratingText;
    }
}
