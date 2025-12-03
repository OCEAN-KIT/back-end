package com.ocean.piuda.environment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.environment.client.dto.KhoaTideResponse;
import com.ocean.piuda.environment.properties.MarineApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * KHOA 조위 관측 API 클라이언트
 */
@Slf4j
@Component
public class KhoaTideClient {

    private static final String BASE_URL = "https://apis.data.go.kr/1192136/surveyTideLevel";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    private final WebClient webClient;
    private final MarineApiProperties properties;
    private final ObjectMapper objectMapper;

    public KhoaTideClient(@Qualifier("marineWebClient") WebClient webClient,
                         MarineApiProperties properties,
                         ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 조위 관측값 조회
     *
     * @param stationId 관측소 ID
     * @param date 관측일
     * @param time 관측시각 (선택)
     * @return 조위 관측값
     */
    public Mono<KhoaTideResponse.Item> fetchTideLevel(String stationId, LocalDate date, LocalTime time) {
        String dateStr = date.format(DATE_FORMATTER);
        String timeStr = time != null ? time.format(TIME_FORMATTER) : "";

        String url = String.format("%s?ServiceKey=%s&obs_post_id=%s&date=%s",
                BASE_URL, properties.getKhoa().getKeyEncoding(), stationId, dateStr);

        if (!timeStr.isEmpty()) {
            url += "&time=" + timeStr;
        }

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        // 에러 응답 확인
                        if (response == null || response.trim().isEmpty() || response.contains("unexpected errors")) {
                            log.warn("KHOA API 오류 응답: stationId={}, response={}", stationId, response);
                            return null;
                        }

                        log.debug("KHOA 응답: {}", response.substring(0, Math.min(500, response.length())));

                        KhoaTideResponse parsed = objectMapper.readValue(response, KhoaTideResponse.class);
                        if (parsed.getResponse() == null
                                || parsed.getResponse().getBody() == null
                                || parsed.getResponse().getBody().getItems() == null
                                || parsed.getResponse().getBody().getItems().isEmpty()) {
                            log.warn("KHOA 조위 데이터가 없습니다: stationId={}, date={}", stationId, date);
                            return null;
                        }

                        // 최신 관측값 반환
                        return parsed.getResponse().getBody().getItems().get(0);
                    } catch (Exception e) {
                        log.error("KHOA 조위 응답 파싱 실패: stationId={}, date={}, response={}", 
                                stationId, date, response != null ? response.substring(0, Math.min(500, response.length())) : "null", e);
                        return null;
                    }
                })
                .doOnError(error -> {
                    // 500 에러는 외부 서버 문제이므로 WARN 레벨로 로깅
                    if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                        org.springframework.web.reactive.function.client.WebClientResponseException webClientError = 
                                (org.springframework.web.reactive.function.client.WebClientResponseException) error;
                        if (webClientError.getStatusCode().value() == 500) {
                            log.warn("KHOA 조위 API 서버 오류 (500): stationId={}, 이는 외부 API 서버 문제입니다.", stationId);
                        } else {
                            log.error("KHOA 조위 API 호출 실패: stationId={}, status={}", stationId, webClientError.getStatusCode(), error);
                        }
                    } else {
                        log.error("KHOA 조위 API 호출 실패: stationId={}", stationId, error);
                    }
                })
                .onErrorResume(error -> {
                    // 에러 발생 시 null 반환 (이미 doOnError에서 로깅됨)
                    return Mono.justOrEmpty((KhoaTideResponse.Item) null);
                });
    }

    /**
     * 오늘의 최신 조위 관측값 조회
     */
    public Mono<KhoaTideResponse.Item> fetchLatestTideLevel(String stationId) {
        return fetchTideLevel(stationId, LocalDate.now(), null);
    }
}

