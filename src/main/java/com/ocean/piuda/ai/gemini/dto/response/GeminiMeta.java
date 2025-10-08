package com.ocean.piuda.ai.gemini.dto.response;

import lombok.Builder;

@Builder
public record GeminiMeta(
        String model,
        Integer promptTokens,
        Integer candidatesTokens,
        Integer totalTokens
) { }
