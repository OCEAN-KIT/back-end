package com.ocean.piuda.ai.groq.dto.response;

import lombok.Builder;


@Builder
public record EvaluateHarmfulnessResponse(
        int degree,
        GroqMeta meta

) { }
