package com.ocean.piuda.ai.groq.dto.response;

import lombok.Builder;

@Builder
public record GroqPromptResponse(
        String content,   // 모델 응답 텍스트
        GroqMeta meta
) { }
