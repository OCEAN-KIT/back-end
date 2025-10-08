package com.ocean.piuda.ai.groq.controller;

import com.ocean.piuda.ai.groq.dto.request.EvaluateHarmfulnessRequest;
import com.ocean.piuda.ai.groq.dto.request.GroqPromptRequest;
import com.ocean.piuda.ai.groq.dto.response.EvaluateHarmfulnessResponse;
import com.ocean.piuda.ai.groq.dto.response.GroqPromptResponse;
import com.ocean.piuda.ai.groq.service.GroqApiService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/groq")
@RequiredArgsConstructor
@Tag(name = "Groq AI API", description = "Groq 기반 자연어 처리 AI API")
public class GroqApiController {

    private final GroqApiService groqApiService;

    @PostMapping("/complete")
    @Operation(
            summary = "프롬프트 처리",
            description = """
                    사용자 프롬프트를 Groq 모델에 전달해 응답을 생성합니다.
                    - `useRealtime=true`면 실시간 웹 검색을 통한 최신 정보가 반영된 응답을 생성하며, 해당 응답의 출처는 `meta.executedTools`에만 담겨 반환됩니다.
                    - 최신정보 반영이 필요 없는 처리는 `useRealtime` 을 명시하지 않으면 됩니다.
                    """
    )
    public ApiData<GroqPromptResponse> complete(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프롬프트/옵션(useRealtime)",
                    required = true
            )
            @RequestBody @Valid GroqPromptRequest request
    ) {
        return ApiData.ok(groqApiService.complete(request));
    }

    @PostMapping("/harmfulness")
    @Operation(
            summary = "문장 유해성 평가",
            description = """
                    입력 문장의 유해성 정도를 0~10 사이 정수로 평가합니다.
                    - 실시간 검색은 사용하지 않으며, 도구 호출을 차단합니다.
                    """
    )
    public ApiData<EvaluateHarmfulnessResponse> evaluateHarmfulness(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "유해성 평가할 문장",
                    required = true
            )
            @RequestBody @Valid EvaluateHarmfulnessRequest request
    ) {
        return ApiData.ok(groqApiService.evaluateHarmfulness(request));
    }
}
