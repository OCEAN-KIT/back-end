package com.ocean.piuda.garmin.util;


import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType; // WATCH_API_KEY_INVALID 필요
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WatchApiKeyValidator {

    @Value("${ocean.watch.api-key:}")
    private String configuredKey;

    public void validate(String incomingKey) {
        if (incomingKey == null || !incomingKey.equals(configuredKey)) {
            log.warn("Suspicious Watch Access Attempt. Key: {}", incomingKey);
            throw new BusinessException(ExceptionType.WATCH_API_KEY_INVALID);
        }
    }
}
