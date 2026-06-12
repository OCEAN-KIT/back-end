package com.ocean.piuda.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(JacksonConfig.class)
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializesLocalDateAsYearMonthDayArray() {
        JsonNode json = objectMapper.valueToTree(new LocalDatePayload(LocalDate.of(2025, 6, 12)));

        JsonNode recordDate = json.get("recordDate");
        assertThat(recordDate.isArray()).isTrue();
        assertThat(recordDate.get(0).asInt()).isEqualTo(2025);
        assertThat(recordDate.get(1).asInt()).isEqualTo(6);
        assertThat(recordDate.get(2).asInt()).isEqualTo(12);
    }

    @Test
    void keepsLocalDateTimeAsIsoDateTimeString() {
        JsonNode json = objectMapper.valueToTree(
                new LocalDateTimePayload(LocalDateTime.of(2025, 6, 12, 9, 30, 15))
        );

        assertThat(json.get("submittedAt").asText()).isEqualTo("2025-06-12T09:30:15");
    }

    private record LocalDatePayload(LocalDate recordDate) {
    }

    private record LocalDateTimePayload(LocalDateTime submittedAt) {
    }
}
