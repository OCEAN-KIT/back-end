package com.ocean.piuda.ai.groq.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record GroqPromptRequest(

        @NotBlank(message = "prompt는 비어 있을 수 없습니다.")
        String prompt,          // 필수: 사용자 프롬프트

        // 선택: 기본 false. true면 groq/compound + Web Search 사용
        Boolean useRealtime

) { }
