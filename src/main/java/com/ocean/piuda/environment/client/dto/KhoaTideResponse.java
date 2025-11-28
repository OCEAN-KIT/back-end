package com.ocean.piuda.environment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * KHOA 조위 API 응답 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KhoaTideResponse {
    @JsonProperty("response")
    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("items")
        private List<Item> items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("obs_post_id")
        private String obsPostId;

        @JsonProperty("obs_post_nm")
        private String obsPostNm;

        @JsonProperty("obs_dt")
        private String obsDt; // 관측일시 (yyyyMMddHHmm)

        @JsonProperty("tide_level")
        private Double tideLevel; // 조위 (cm)
    }
}

