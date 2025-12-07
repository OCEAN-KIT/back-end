package com.ocean.piuda.environment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.environment.client.dto.KmaSeaObsResponse;
import com.ocean.piuda.environment.properties.MarineApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * KMA 해양기상종합관측 API 클라이언트
 */
@Slf4j
@Component
public class KmaSeaObsClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    // [필수] KMA는 EUC-KR 인코딩을 사용함
    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    private final WebClient webClient;
    private final MarineApiProperties properties;
    @SuppressWarnings("unused")
    private final ObjectMapper objectMapper;

    public KmaSeaObsClient(
            @Qualifier("marineWebClient") WebClient marineWebClient,
            MarineApiProperties properties,
            ObjectMapper objectMapper
    ) {
        this.webClient = marineWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 해양기상 관측값 조회
     */
    public Mono<List<KmaSeaObsResponse.Item>> fetchObservations(String stationId, LocalDateTime dateTime) {
        String tm = dateTime.format(DATE_TIME_FORMATTER);

        return webClient.get()
                .uri(uriBuilder -> {
                    MarineApiProperties.Kma config = properties.getKma();
                    return uriBuilder
                            .scheme("https")
                            .host(config.getHost())
                            .path(config.getSeaObsPath())
                            .queryParam("tm", tm)
                            .queryParam("stn", stationId)
                            .queryParam("authKey", config.getKey())
                            .build();
                })
                .retrieve()
                // [중요] byte[]로 받아서 EUC-KR로 디코딩
                .bodyToMono(byte[].class)
                .map(bytes -> new String(bytes, EUC_KR))
                .map(response -> {
                    try {
                        return parseKmaTextResponse(response, stationId);
                    } catch (Exception e) {
                        log.error("KMA 해양기상 응답 파싱 실패: stationId={}, dateTime={}", stationId, dateTime, e);
                        if (response != null) {
                            log.debug("KMA 응답 내용: {}", response.substring(0, Math.min(500, response.length())));
                        }
                        return new ArrayList<KmaSeaObsResponse.Item>();
                    }
                })
                .doOnError(error -> log.error("KMA 해양기상 API 호출 실패: stationId={}", stationId, error))
                .onErrorResume(error -> Mono.just(new ArrayList<>()));
    }

    public Mono<KmaSeaObsResponse.Item> fetchLatestObservation(String stationId) {
        return fetchObservations(stationId, LocalDateTime.now())
                .flatMap(items -> items.isEmpty()
                        ? Mono.empty()
                        : Mono.just(items.get(0)));
    }

    /**
     * KMA 텍스트 형식 응답 파싱
     */
    private List<KmaSeaObsResponse.Item> parseKmaTextResponse(String text, String targetStationId) {
        List<KmaSeaObsResponse.Item> items = new ArrayList<>();

        if (text == null || text.trim().isEmpty() || text.contains("unexpected errors")) {
            log.warn("KMA API 응답이 비어있거나 오류가 있습니다.");
            return items;
        }

        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#") || line.isEmpty() || line.startsWith("START") || line.startsWith("END")) {
                continue;
            }

            if (line.startsWith("B,") || line.startsWith("C,")) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length < 14) {
                        continue;
                    }

                    String stn = parts[2].trim();
                    if (targetStationId != null && !targetStationId.equals("0") && !targetStationId.equals(stn)) {
                        continue;
                    }

                    KmaSeaObsResponse.Item item = new KmaSeaObsResponse.Item();
                    item.setStn(stn);
                    item.setStnNm(parts[3].trim());
                    item.setTm(parts[1].trim());

                    try {
                        item.setLon(Double.parseDouble(parts[4].trim()));
                        item.setLat(Double.parseDouble(parts[5].trim()));
                    } catch (Exception e) {
                        log.warn("좌표 파싱 실패: {}", line);
                    }

                    // WH
                    try {
                        String whStr = parts[6].trim();
                        if (!whStr.equals("-99.0") && !whStr.equals("-99") && !whStr.isEmpty()) {
                            item.setWh(Double.parseDouble(whStr));
                        }
                    } catch (Exception e) {
                        log.debug("파고(WH) 파싱 실패: stn={}, value={}", stn, parts[6]);
                    }

                    // WD
                    try {
                        String wdStr = parts[7].trim();
                        if (!wdStr.equals("-99") && !wdStr.equals("-99.0") && !wdStr.isEmpty()) {
                            item.setWd(Double.parseDouble(wdStr));
                        }
                    } catch (Exception e) {
                        log.debug("풍향(WD) 파싱 실패: stn={}, value={}", stn, parts[7]);
                    }

                    // WS
                    try {
                        String wsStr = parts[8].trim();
                        if (!wsStr.equals("-99.0") && !wsStr.equals("-99") && !wsStr.isEmpty()) {
                            item.setWs(Double.parseDouble(wsStr));
                        }
                    } catch (Exception e) {
                        log.warn("풍속(WS) 파싱 실패: stn={}, value={}, error={}", stn, parts[8], e.getMessage());
                    }

                    // TW
                    try {
                        String twStr = parts[10].trim();
                        if (!twStr.equals("-99.0") && !twStr.equals("-99") && !twStr.isEmpty()) {
                            item.setTw(Double.parseDouble(twStr));
                        }
                    } catch (Exception e) {
                        log.debug("해수온(TW) 파싱 실패: stn={}, value={}", stn, parts[10]);
                    }

                    items.add(item);
                } catch (Exception e) {
                    log.warn("KMA 데이터 라인 파싱 실패: {}", line, e);
                }
            }
        }

        return items;
    }
}