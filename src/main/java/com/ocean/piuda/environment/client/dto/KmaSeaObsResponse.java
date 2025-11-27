package com.ocean.piuda.environment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * KMA 해양기상종합관측 API 응답 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KmaSeaObsResponse {
    @JsonProperty("list")
    private List<Item> list;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("stn")
        private String stn; // 관측소 번호

        @JsonProperty("stn_nm")
        private String stnNm; // 관측소 이름

        @JsonProperty("tm")
        private String tm; // 관측시각 (yyyyMMddHHmm)

        @JsonProperty("lat")
        private Double lat; // 위도

        @JsonProperty("lon")
        private Double lon; // 경도

        @JsonProperty("WH")
        private Double wh; // 파고 (m)

        @JsonProperty("WD")
        private Double wd; // 풍향 (도)

        @JsonProperty("WS")
        private Double ws; // 풍속 (m/s)

        @JsonProperty("TW")
        private Double tw; // 해수면 온도 (℃)
    }
}

