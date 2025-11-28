package com.ocean.piuda.environment.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 해양 환경 API 키 설정 Properties
 */
@Component
@ConfigurationProperties(prefix = "marine")
@Getter
@Setter
public class MarineApiProperties {

    private Nifs nifs = new Nifs();
    private Khoa khoa = new Khoa();
    private Kma kma = new Kma();

    @Getter
    @Setter
    public static class Nifs {
        /**
         * NIFS RISA API 키
         */
        private String key;
    }

    @Getter
    @Setter
    public static class Khoa {
        /**
         * KHOA 조위 API 키 (URL Encoding)
         */
        private String keyEncoding;

        /**
         * KHOA 조위 API 키 (Decoding)
         */
        private String keyDecoding;
    }

    @Getter
    @Setter
    public static class Kma {
        /**
         * KMA 해양기상종합관측 API 키
         */
        private String key;
    }
}

