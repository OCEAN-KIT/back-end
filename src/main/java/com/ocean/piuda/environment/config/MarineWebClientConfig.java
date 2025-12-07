package com.ocean.piuda.environment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 해양 환경 API용 WebClient 설정
 */
@Configuration
public class MarineWebClientConfig {

    /**
     * 해양 환경 API 호출용 WebClient
     * 타임아웃 설정 포함
     */
    @Bean("marineWebClient")
    public WebClient marineWebClient(WebClient.Builder builder) {
        return builder
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
}