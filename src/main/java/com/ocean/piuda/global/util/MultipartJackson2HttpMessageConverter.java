package com.ocean.piuda.global.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * Swagger가 multipart/form-data의 JSON 파트를 Content-Type 없이 보내
 * application/octet-stream으로 처리되는 경우를 흡수하기 위한 컨버터.
 * 읽기(read)만 지원하고 쓰기(write)는 막음.
 */
@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    public MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        // 이 컨버터가 처리할 미디어타입을 octet-stream으로 명시
        super(objectMapper, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false; // 쓰기는 지원하지 않음
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }
}