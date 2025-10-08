package com.ocean.piuda.ai.groq.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ocean.piuda.ai.groq.dto.response.GroqMeta;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqApiUtil {
    private final RestTemplate restTemplate;

    @Value("${groq.api.key}")
    private String apiKey;

    /** 디폴트 모델 (오프라인/기본) */
    @Value("${groq.api.model}")
    private String defaultModel;

    /** 실시간 검색 모델 */
    private static final String REAL_TIME_MODEL = "groq/compound";

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

    /* ============================== 유틸 ============================== */

    public static boolean isCompound(String model) {
        return model != null && model.startsWith("groq/compound");
    }

    public HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    public static String header(ResponseEntity<?> resp, String name) {
        if (resp == null || resp.getHeaders() == null) return null;
        List<String> v = resp.getHeaders().get(name);
        if (v != null && !v.isEmpty()) return v.get(0);
        return resp.getHeaders().entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(name))
                .map(e -> (e.getValue() != null && !e.getValue().isEmpty()) ? e.getValue().get(0) : null)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    /** 본문 내 URL 제거(마크다운 링크 및 맨몸 URL) */
    public static String stripUrls(String text) {
        if (text == null) return "";
        // [text](url) -> text
        text = text.replaceAll("\\[([^\\]]+)\\]\\((https?://[^)\\s]+)\\)", "$1");
        // bare URL 제거
        text = text.replaceAll("https?://\\S+", "");
        // 잉여 공백 정리
        return text.replaceAll("[ \\t]+\\n", "\n").trim();
    }

    /** 모델 선택 (useRealtime=true면 compound로 강제) */
    public String resolveModel(boolean useRealtime) {
        return useRealtime ? REAL_TIME_MODEL : defaultModel;
    }

    /** 서버 고정 시스템 프롬프트 */
    public String resolveSystemPrompt(String model) {
        return isCompound(model) ? SYS_COMPOUND : SYS_OFFLINE;
    }

    /** 공통: messages 생성 */
    public ArrayNode buildMessages(String systemContent, String userContent) {
        ArrayNode messages = MAPPER.createArrayNode();

        ObjectNode sys = MAPPER.createObjectNode();
        sys.put("role", "system");
        sys.put("content", systemContent);
        messages.add(sys);

        ObjectNode user = MAPPER.createObjectNode();
        user.put("role", "user");
        user.put("content", userContent);
        messages.add(user);

        return messages;
    }

    /** 공통: 요청 payload 생성 */
    public ObjectNode buildRequestPayload(String model, ArrayNode messages) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model);
        root.set("messages", messages);
        if (!isCompound(model)) {
            // 오프라인 모델은 툴 사용 차단
            root.put("tool_choice", "none");
        }
        return root;
    }

    /** 공통: HTTP 호출 */
    public ResponseEntity<String> postChat(ObjectNode payload) {
        try {
            return restTemplate.exchange(
                    GROQ_CHAT_URL, HttpMethod.POST,
                    new HttpEntity<>(payload.toString(), buildHeaders()),
                    String.class
            );
        } catch (HttpClientErrorException e) {
            throw new BusinessException(
                    ExceptionType.NOT_VALID_REQUEST_FIELDS_ERROR,
                    Map.of("status", e.getStatusCode().value(),
                            "reason", "Groq 4xx",
                            "body", e.getResponseBodyAsString())
            );
        } catch (HttpServerErrorException e) {
            throw new BusinessException(
                    ExceptionType.UNEXPECTED_SERVER_ERROR,
                    Map.of("status", e.getStatusCode().value(),
                            "reason", "Groq 5xx",
                            "body", e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            throw new BusinessException(
                    ExceptionType.UNEXPECTED_SERVER_ERROR,
                    Map.of("reason", "Groq call failed",
                            "message", e.getMessage())
            );
        }
    }

    /** 공통: content 추출(+URL 제거) */
    public String extractCleanContent(String raw) {
        try {
            JsonNode rootNode = MAPPER.readTree(raw);
            String content = rootNode.path("choices").path(0).path("message").path("content").asText("");
            return stripUrls(content);
        } catch (Exception e) {
            log.warn("Failed to parse content from Groq response", e);
            return "";
        }
    }

    /** 공통: 메타 생성 (usage, executed_tools 포함) */
    public GroqMeta buildMeta(ResponseEntity<String> resp, String raw, String modelUsed) {
        Integer pt = null, ct = null, tt = null;
        JsonNode executedTools = null;

        try {
            if (raw != null) {
                JsonNode rootNode = MAPPER.readTree(raw);
                JsonNode usage = rootNode.path("usage");
                if (!usage.isMissingNode()) {
                    pt = usage.path("prompt_tokens").isInt() ? usage.path("prompt_tokens").asInt() : null;
                    ct = usage.path("completion_tokens").isInt() ? usage.path("completion_tokens").asInt() : null;
                    tt = usage.path("total_tokens").isInt() ? usage.path("total_tokens").asInt() : null;
                }
                JsonNode executed = rootNode.path("choices").path(0).path("message").path("executed_tools");
                if (executed.isArray() && executed.size() > 0) {
                    executedTools = executed;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse usage/executed_tools", e);
        }

        String requestId = header(resp, "x-request-id");
        String rlReqRemain = header(resp, "x-ratelimit-remaining-requests");
        String rlTokRemain = header(resp, "x-ratelimit-remaining-tokens");
        String rlReset     = header(resp, "x-ratelimit-reset");

        return GroqMeta.builder()
                .model(modelUsed)
                .promptTokens(pt)
                .completionTokens(ct)
                .totalTokens(tt)
                .requestId(requestId)
                .rateLimitRequestsRemaining(rlReqRemain)
                .rateLimitTokensRemaining(rlTokRemain)
                .rateLimitReset(rlReset)
                .executedTools(executedTools) 
                .build();
    }

}
