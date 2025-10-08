package com.ocean.piuda.ai.gemini.controller;

import com.ocean.piuda.ai.gemini.dto.request.GeminiRequest;
import com.ocean.piuda.ai.gemini.dto.response.GeminiResponse;
import com.ocean.piuda.ai.gemini.service.GeminiTextService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/gemini")
@RequiredArgsConstructor
@Tag(name = "Gemini Text API", description = "Gemini 기반 자연어 처리 AI API")
public class GeminiTextController {

    private final GeminiTextService service;

    @PostMapping("/text/complete")
    @Operation(
            summary = "프롬프트 처리",
            description = """
                    텍스트 프롬프트를 Gemini 모델로 보냅니다.
                    """
    )
    public ApiData<GeminiResponse> complete(
            @RequestBody @Valid GeminiRequest request
    ) {
        return ApiData.ok(service.complete(request));
    }
}