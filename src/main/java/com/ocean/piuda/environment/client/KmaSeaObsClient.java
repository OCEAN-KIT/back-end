package com.ocean.piuda.environment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.environment.client.dto.KmaSeaObsResponse;
import com.ocean.piuda.environment.properties.MarineApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * KMA 해양기상종합관측 API 클라이언트
 */
@Slf4j
@Component
public class KmaSeaObsClient {

    private static final String BASE_URL = "https://apihub.kma.go.kr/api/typ01/url/sea_obs.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final WebClient webClient;
    private final MarineApiProperties properties;
    private final ObjectMapper objectMapper;

    public KmaSeaObsClient(@Qualifier("marineWebClient") WebClient webClient,
                          MarineApiProperties properties,
                          ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 해양기상 관측값 조회
     *
     * @param stationId 관측소 번호 (0이면 전체)
     * @param dateTime 관측시각
     * @return 관측값 리스트
     */
    public Mono<List<KmaSeaObsResponse.Item>> fetchObservations(String stationId, LocalDateTime dateTime) {
        String tm = dateTime.format(DATE_TIME_FORMATTER);
        String url = String.format("%s?tm=%s&stn=%s&authKey=%s",
                BASE_URL, tm, stationId, properties.getKma().getKey());

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        // KMA API는 텍스트 형식으로 응답
                        return parseKmaTextResponse(response, stationId);
                    } catch (Exception e) {
                        log.error("KMA 해양기상 응답 파싱 실패: stationId={}, dateTime={}", stationId, dateTime, e);
                        log.debug("KMA 응답 내용: {}", response.substring(0, Math.min(500, response.length())));
                        return new ArrayList<KmaSeaObsResponse.Item>();
                    }
                })
                .doOnError(error -> log.error("KMA 해양기상 API 호출 실패: stationId={}", stationId, error))
                .onErrorResume(error -> Mono.just(new ArrayList<KmaSeaObsResponse.Item>()));
    }

    /**
     * 최신 해양기상 관측값 조회
     */
    public Mono<KmaSeaObsResponse.Item> fetchLatestObservation(String stationId) {
        return fetchObservations(stationId, LocalDateTime.now())
                .map(items -> items.isEmpty() ? null : items.get(0));
    }

    /**
     * KMA 텍스트 형식 응답 파싱
     * 형식: B, 202511261200, 22101, 관측소명, 126.01880000, 37.23610000, 0.5, 284, 2.2, 4.2, 15.2, 8.9, 1026.3, 49.0, ,=
     * 컬럼: 타입, 시각, 관측소ID, 관측소명, 경도, 위도, WH(파고), WD(풍향), WS(풍속), GST, TW(해수온), TA, PA, HM, ...
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
            // 주석이나 헤더 라인 건너뛰기
            if (line.startsWith("#") || line.isEmpty() || line.startsWith("START") || line.startsWith("END")) {
                continue;
            }

            // 데이터 라인 파싱 (B 또는 C로 시작)
            if (line.startsWith("B,") || line.startsWith("C,")) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length < 14) {
                        continue;
                    }

                    String stn = parts[2].trim();
                    // 특정 관측소만 조회하는 경우 필터링
                    if (targetStationId != null && !targetStationId.equals("0") && !targetStationId.equals(stn)) {
                        continue;
                    }

                    KmaSeaObsResponse.Item item = new KmaSeaObsResponse.Item();
                    item.setStn(stn);
                    item.setStnNm(parts[3].trim());
                    item.setTm(parts[1].trim());

                    // 경도, 위도
                    try {
                        item.setLon(Double.parseDouble(parts[4].trim()));
                        item.setLat(Double.parseDouble(parts[5].trim()));
                    } catch (Exception e) {
                        log.warn("좌표 파싱 실패: {}", line);
                    }

                    // WH (파고), WD (풍향), WS (풍속), TW (해수온)
                    // 컬럼 순서: TP(0), TM(1), STN_ID(2), STN_KO(3), LON(4), LAT(5), WH(6), WD(7), WS(8), WS_GST(9), TW(10), TA(11), PA(12), HM(13)
                    try {
                        String whStr = parts[6].trim();
                        if (!whStr.equals("-99.0") && !whStr.equals("-99") && !whStr.isEmpty()) {
                            item.setWh(Double.parseDouble(whStr));
                        }
                    } catch (Exception e) {
                        log.debug("파고(WH) 파싱 실패: stn={}, value={}", stn, parts[6]);
                    }

                    try {
                        String wdStr = parts[7].trim();
                        if (!wdStr.equals("-99") && !wdStr.equals("-99.0") && !wdStr.isEmpty()) {
                            item.setWd(Double.parseDouble(wdStr));
                        }
                    } catch (Exception e) {
                        log.debug("풍향(WD) 파싱 실패: stn={}, value={}", stn, parts[7]);
                    }

                    try {
                        String wsStr = parts[8].trim();
                        if (!wsStr.equals("-99.0") && !wsStr.equals("-99") && !wsStr.isEmpty()) {
                            Double ws = Double.parseDouble(wsStr);
                            item.setWs(ws);
                            log.debug("풍속(WS) 파싱 성공: stn={}, ws={}", stn, ws);
                        } else {
                            log.debug("풍속(WS) 데이터 없음: stn={}, value={}", stn, wsStr);
                        }
                    } catch (Exception e) {
                        log.warn("풍속(WS) 파싱 실패: stn={}, value={}, error={}", stn, parts[8], e.getMessage());
                    }

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

