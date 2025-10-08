package com.ocean.piuda.ai.groq.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

/**
 * 모든 응답에 공통으로 담길 메타 정보
 */
@Builder
public record GroqMeta(
        String model,                         // 호출한 모델
        Integer promptTokens,                 // usage.prompt_tokens
        Integer completionTokens,             // usage.completion_tokens
        Integer totalTokens,                  // usage.total_tokens
        String requestId,                     // x-request-id
        String rateLimitRequestsRemaining,    // x-ratelimit-remaining-requests
        String rateLimitTokensRemaining,      // x-ratelimit-remaining-tokens (있을 때)
        String rateLimitReset ,                // x-ratelimit-reset (있을 때)
        JsonNode executedTools                  // ← choices[0].message.executed_tools 원본 JSON(있으면)

) { }