package com.ocean.piuda.ai.gemini.controller;

import com.ocean.piuda.ai.gemini.dto.request.GeminiRequest;
import com.ocean.piuda.ai.gemini.dto.response.GeminiResponse;
import com.ocean.piuda.ai.gemini.service.GeminiVisionService;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/gemini")
@RequiredArgsConstructor
@Tag(name = "Gemini Vision API", description = "Gemini 기반 단일 이미지 설명/분석 API")
public class GeminiVisionController {

    private final GeminiVisionService service;

    @PostMapping(
            value = "/vision/describe",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "이미지 설명/분석",
            description = """
                    단일 이미지를 업로드하고, 프롬프트를 JSON 파트로 함께 전달하세요.
                    - Multipart 파트: image(파일), request(JSON)
                    - 20MB 초과 파일은 Files API 사용을 권장합니다.
                    """
    )
    public ApiData<GeminiResponse> describe(
            @Parameter(name = "image", required = true,
                    content = @Content(mediaType = "image/*",
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("image") MultipartFile image,
            @Parameter(name = "request", required = false,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeminiRequest.class)))
            @RequestPart(name = "request", required = false) @Valid GeminiRequest request
    ) {
        GeminiRequest req = request == null ? GeminiRequest.builder().build() : request;
        return ApiData.ok(service.describe(image, req));
    }
}
