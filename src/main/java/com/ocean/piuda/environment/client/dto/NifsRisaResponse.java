package com.ocean.piuda.environment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
    @Slf4j // 내부 클래스에 직접 로그 어노테이션 적용
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

        // 파싱된 값 캐싱용 (JSON 직렬화 제외)
        private transient Integer obsLayIntCached;
        private transient Double waterTempCached;

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

        /**
         * 수층(Integer) 파싱 + 캐싱
         */
        public Integer getObsLayInt() {
            if (obsLayIntCached != null) {
                return obsLayIntCached;
            }

            if (obsLay == null || obsLay.isBlank()) {
                return null;
            }

            try {
                obsLayIntCached = Integer.parseInt(obsLay.trim());
                return obsLayIntCached;
            } catch (NumberFormatException e) {
                log.warn("수층(obs_lay) 파싱 실패: sta_cde={}, obs_lay={}", staCde, obsLay);
                return null;
            }
        }

        /**
         * 수온(Double) 파싱 + 캐싱
         * - 빈 값 / -99 / -99.0 등은 데이터 없음으로 처리
         */
        public Double getWaterTemp() {
            if (waterTempCached != null) {
                return waterTempCached;
            }

            if (wtrTmp == null || wtrTmp.isBlank()) {
                return null;
            }

            String value = wtrTmp.trim();
            if ("-99".equals(value) || "-99.0".equals(value)) {
                // 관측값 없음
                return null;
            }

            try {
                waterTempCached = Double.parseDouble(value);
                return waterTempCached;
            } catch (NumberFormatException e) {
                log.warn("수온(wtr_tmp) 파싱 실패: sta_cde={}, wtr_tmp={}", staCde, wtrTmp);
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