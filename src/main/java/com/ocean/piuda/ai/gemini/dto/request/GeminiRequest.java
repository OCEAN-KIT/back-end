package com.ocean.piuda.ai.gemini.dto.request;


import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * 단일 이미지 + 프롬프트로 설명/분석 요청
 */
@Builder
public record GeminiRequest(
        @Size(max = 2000, message = "prompt는 2000자 이하여야 합니다.")
        String prompt
) { }
