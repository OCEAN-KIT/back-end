package com.ocean.piuda.ai.gemini.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.ocean.piuda.ai.gemini.dto.request.GeminiRequest;
import com.ocean.piuda.ai.gemini.dto.response.GeminiMeta;
import com.ocean.piuda.ai.gemini.dto.response.GeminiResponse;
import com.ocean.piuda.ai.gemini.util.GeminiSdkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiVisionService {

    private final Client client;

    @Value("${gemini.model.vision}")
    private String defaultVisionModel;

    private static final long MAX_INLINE_BYTES = 20L * 1024 * 1024;

    public GeminiResponse describe(MultipartFile image, GeminiRequest req) {

        byte[] bytes = readBytesWithLimit(image);
        String mime = safeMime(image);

        List<Content> contents =
                GeminiSdkUtil.buildUserContents(bytes, mime, req.prompt());



        GenerateContentResponse resp = client.models.generateContent(defaultVisionModel, contents, null);

        String text = GeminiSdkUtil.extractText(resp);
        GeminiMeta meta = GeminiSdkUtil.toMeta(defaultVisionModel, resp);
        return GeminiResponse.builder().content(text).meta(meta).build();
    }

    private static byte[] readBytesWithLimit(MultipartFile image) {
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("image 파일이 필요합니다.");
        if (image.getSize() > MAX_INLINE_BYTES) {
            throw new IllegalArgumentException("이미지 용량이 너무 큽니다(<=20MB). Files API 사용 권장");
        }
        try { return image.getBytes(); }
        catch (IOException e) { throw new IllegalStateException("이미지 바이트를 읽을 수 없습니다.", e); }
    }

    private static String safeMime(MultipartFile file) {
        String c = file.getContentType();
        return (c == null || c.isBlank()) ? MediaType.IMAGE_JPEG_VALUE : c;
    }
}
