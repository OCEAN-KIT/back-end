package com.ocean.piuda.environment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * NIFS RISA API 응답 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifsRisaResponse {
    @JsonProperty("body")
    private Body body;

    @JsonProperty("header")
    private Header header;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("item")
        private List<NifsRisaItem> item;
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
    public static class NifsRisaItem {
        @JsonProperty("sta_cde")
        private String staCde; // 관측소 코드 (기존 obs_post_id)

        @JsonProperty("sta_nam_kor")
        private String staNamKor; // 관측소 이름 (기존 obs_post_nm)

        @JsonProperty("obs_lay")
        private String obsLay; // 수층 (1: 표층, 2: 중층, 3: 저층)

        @JsonProperty("wtr_tmp")
        private String wtrTmp; // 수온 (기존 water_temp)

        @JsonProperty("obs_dat")
        private String obsDat; // 관측일

        @JsonProperty("obs_tim")
        private String obsTim; // 관측시각

        // 관측소 위치 정보는 별도 API로 가져와야 할 수 있음
        // 임시로 null 처리
        public Double getObsLat() {
            return null; // TODO: 관측소 위치 정보 별도 조회 필요
        }

        public Double getObsLon() {
            return null; // TODO: 관측소 위치 정보 별도 조회 필요
        }

        public String getObsPostId() {
            return staCde;
        }

        public String getObsPostNm() {
            return staNamKor;
        }

        public Integer getObsLayInt() {
            try {
                return Integer.parseInt(obsLay);
            } catch (Exception e) {
                return null;
            }
        }

        public Double getWaterTemp() {
            try {
                return Double.parseDouble(wtrTmp);
            } catch (Exception e) {
                return null;
            }
        }

        // 염분과 용존산소는 현재 API 응답에 없음
        public Double getSalinity() {
            return null;
        }

        public Double getDissolvedOxygen() {
            return null;
        }
    }
}

