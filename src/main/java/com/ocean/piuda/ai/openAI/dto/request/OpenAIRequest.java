package com.ocean.piuda.ai.openAI.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class OpenAIRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        // "system" | "user" | "assistant"
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Basic {
        private String model;
        private String prompt;                // 기존 호환용
        private List<ChatMessage> messages;   // 신규 메시지 배열
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextCompose {
        private String model;
        private List<String> sentences;
    }
}
