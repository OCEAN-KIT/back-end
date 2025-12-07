package com.ocean.piuda.environment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.environment.client.dto.KhoaTideResponse;
import com.ocean.piuda.environment.properties.MarineApiProperties;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    private final WebClient webClient;
    private final MarineApiProperties properties;
    private final ObjectMapper objectMapper;

    public KhoaTideClient(
            @Qualifier("marineWebClient") WebClient marineWebClient,
            MarineApiProperties properties,
            ObjectMapper objectMapper
    ) {
        this.webClient = marineWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 조위 관측값 조회
     */
    public Mono<KhoaTideResponse.Item> fetchTideLevel(String stationId, LocalDate date, LocalTime time) {
        String dateStr = date.format(DATE_FORMATTER);

        return webClient.get()
                .uri(uriBuilder -> {
                    MarineApiProperties.Khoa config = properties.getKhoa();

                    var builder = uriBuilder
                            .scheme("https")
                            .host(config.getHost())
                            .path(config.getTidePath())
                            .queryParam("ServiceKey", config.getKeyEncoding()) // 인코딩된 키 사용
                            .queryParam("obs_post_id", stationId)
                            .queryParam("date", dateStr)
                            .queryParam("ResultType", "json"); // JSON 포맷 명시

                    if (time != null) {
                        builder.queryParam("time", time.format(TIME_FORMATTER));
                    }

                    return builder.build();
                })
                .retrieve()
                // [확정] KHOA는 UTF-8이므로 String으로 받음
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        if (response == null || response.trim().isEmpty() || response.contains("Unexpected errors")) {
                            log.warn("KHOA API 오류 응답: stationId={}, response={}", stationId, response);
                            return Mono.empty();
                        }

                        // 정상 JSON 응답인지 확인 (간단한 체크)
                        if (!response.trim().startsWith("{")) {
                            log.warn("KHOA API 응답이 JSON이 아닙니다: stationId={}, response={}", stationId, response);
                            return Mono.empty();
                        }

                        KhoaTideResponse parsed = objectMapper.readValue(response, KhoaTideResponse.class);
                        if (parsed.getResponse() == null
                                || parsed.getResponse().getBody() == null
                                || parsed.getResponse().getBody().getItems() == null
                                || parsed.getResponse().getBody().getItems().isEmpty()) {
                            log.warn("KHOA 조위 데이터가 없습니다: stationId={}, date={}", stationId, date);
                            return Mono.empty();
                        }

                        return Mono.just(parsed.getResponse().getBody().getItems().get(0));
                    } catch (Exception e) {
                        log.error("KHOA 조위 응답 파싱 실패: stationId={}, date={}, response={}",
                                stationId, date,
                                response != null ? response.substring(0, Math.min(500, response.length())) : "null",
                                e);
                        return Mono.empty();
                    }
                })
                .doOnError(error -> {
                    if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException webClientError) {
                        if (webClientError.getStatusCode().value() == 500) {
                            log.warn("KHOA 조위 API 서버 오류 (500): stationId={}, 외부 서버 문제 또는 키/파라미터 확인 필요.", stationId);
                        } else {
                            log.error("KHOA 조위 API 호출 실패: stationId={}, status={}",
                                    stationId, webClientError.getStatusCode(), error);
                        }
                    } else {
                        log.error("KHOA 조위 API 호출 실패: stationId={}", stationId, error);
                    }
                })
                .onErrorResume(error -> Mono.empty());
    }

    public Mono<KhoaTideResponse.Item> fetchLatestTideLevel(String stationId) {
        return fetchTideLevel(stationId, LocalDate.now(), null);
    }
}