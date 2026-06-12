package com.ocean.piuda.global.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateArrayCustomizer() {
        return builder -> builder.serializerByType(LocalDate.class, new LocalDateArraySerializer());
    }

    private static class LocalDateArraySerializer extends JsonSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            gen.writeNumber(value.getYear());
            gen.writeNumber(value.getMonthValue());
            gen.writeNumber(value.getDayOfMonth());
            gen.writeEndArray();
        }
    }
}
