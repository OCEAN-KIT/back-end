package com.ocean.piuda.ai.gemini.dto.response;


import lombok.Builder;

@Builder
public record GeminiResponse(
        String content,
        GeminiMeta meta
) { }
