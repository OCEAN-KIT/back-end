package com.ocean.piuda.ai.gemini.util;


import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.ocean.piuda.ai.gemini.dto.response.GeminiMeta;

import java.util.List;
import java.util.Objects;

public class GeminiSdkUtil {

    // 이미지 + 프롬프트
    public static List<Content> buildUserContents(byte[] imageBytes, String mimeType, String promptOrNull) {
        Part image = Part.fromBytes(imageBytes, mimeType);
        if (promptOrNull == null || promptOrNull.isBlank()) {
            return List.of(Content.fromParts(image));
        }
         return List.of(
             Content.builder()
                    .role("user")
                    .parts(image, Part.fromText(promptOrNull))
                    .build()
         );
    }



    public static String extractText(GenerateContentResponse resp) {
        try { return Objects.toString(resp.text(), ""); }
        catch (Exception e) { return ""; }
    }

    public static GeminiMeta toMeta(String model, GenerateContentResponse resp) {
        var usageOpt = resp.usageMetadata();
        Integer pt = null, ct = null, tt = null;
        if (usageOpt.isPresent()) {
            var u = usageOpt.get();
            pt = u.promptTokenCount().orElse(null);
            ct = u.candidatesTokenCount().orElse(null);
            tt = u.totalTokenCount().orElse(null);
        }
        return GeminiMeta.builder()
                .model(model)
                .promptTokens(pt)
                .candidatesTokens(ct)
                .totalTokens(tt)
                .build();
    }
}
