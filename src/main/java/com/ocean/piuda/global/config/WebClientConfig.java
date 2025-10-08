package com.ocean.piuda.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {


    /**
     * 공통 REST API 호출용 기본 WebClient
     */
    @Bean
    @Qualifier("defaultWebClient")
    public WebClient defaultWebClient(WebClient.Builder builder) {
        return builder.build();
    }
}

