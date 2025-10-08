package com.ocean.piuda.ai.groq.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;


@Builder
public record EvaluateHarmfulnessRequest (
        @NotBlank(message = "sentence는 비어 있을 수 없습니다.")
        String sentence
){ }