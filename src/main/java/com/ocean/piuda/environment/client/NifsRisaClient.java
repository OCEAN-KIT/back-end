package com.ocean.piuda.environment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.environment.client.dto.NifsRisaResponse;
import com.ocean.piuda.environment.domain.StationSource;
import com.ocean.piuda.environment.properties.MarineApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NIFS RISA 실시간 해양수산환경 관측 API 클라이언트
 */
@Slf4j
@Component
public class NifsRisaClient {

    private static final String BASE_URL = "https://www.nifs.go.kr/bweb/OpenAPI_json";
    private static final List<Integer> LAYER_PRIORITY = List.of(2, 1, 3); // 중층 -> 표층 -> 저층 순

    private final WebClient webClient;
    private final MarineApiProperties properties;
    private final ObjectMapper objectMapper;

    public NifsRisaClient(@Qualifier("marineWebClient") WebClient webClient,
                         MarineApiProperties properties,
                         ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 관측소 목록 조회
     */
    public Mono<List<NifsRisaResponse.NifsRisaItem>> fetchStations() {
        String url = String.format("%s?id=risaList&key=%s", BASE_URL, properties.getNifs().getKey());

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        NifsRisaResponse parsed = objectMapper.readValue(response, NifsRisaResponse.class);
                        if (parsed.getBody() != null && parsed.getBody().getItem() != null) {
                            return parsed.getBody().getItem();
                        }
                        return new ArrayList<NifsRisaResponse.NifsRisaItem>();
                    } catch (Exception e) {
                        log.error("NIFS RISA 응답 파싱 실패: {}", response.substring(0, Math.min(200, response.length())), e);
                        return new ArrayList<NifsRisaResponse.NifsRisaItem>();
                    }
                })
                .doOnError(error -> log.error("NIFS RISA API 호출 실패", error))
                .onErrorResume(error -> Mono.just(new ArrayList<NifsRisaResponse.NifsRisaItem>()));
    }

    /**
     * 특정 관측소의 최신 관측값 조회
     * obs_lay 우선순위: 2(중층) -> 1(표층) -> 3(저층)
     * 
     * 주의: NIFS RISA API는 sta_cde 파라미터를 넣어도 모든 관측소 데이터를 반환하므로,
     * 응답에서 해당 관측소만 필터링해야 함
     */
    public Mono<NifsRisaResponse.NifsRisaItem> fetchLatestObservation(String stationId) {
        String url = String.format("%s?id=risaList&key=%s&sta_cde=%s",
                BASE_URL, properties.getNifs().getKey(), stationId);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        NifsRisaResponse parsed = objectMapper.readValue(response, NifsRisaResponse.class);
                        if (parsed.getBody() == null || parsed.getBody().getItem() == null || parsed.getBody().getItem().isEmpty()) {
                            log.warn("NIFS RISA API 응답이 비어있습니다: stationId={}", stationId);
                            return Mono.empty();
                        }

                        List<NifsRisaResponse.NifsRisaItem> allItems = parsed.getBody().getItem();
                        
                        // 해당 관측소의 데이터만 필터링 (sta_cde로 매칭)
                        List<NifsRisaResponse.NifsRisaItem> stationItems = allItems.stream()
                                .filter(item -> stationId.equals(item.getStaCde()))
                                .toList();

                        if (stationItems.isEmpty()) {
                            log.warn("NIFS RISA API에서 해당 관측소 데이터를 찾을 수 없습니다: stationId={}, 전체 관측소 수={}", 
                                    stationId, allItems.size());
                            return Mono.empty();
                        }

                        // 우선순위에 따라 관측값 선택 (중층 -> 표층 -> 저층)
                        for (Integer layer : LAYER_PRIORITY) {
                            for (NifsRisaResponse.NifsRisaItem item : stationItems) {
                                if (item.getObsLayInt() != null && item.getObsLayInt().equals(layer)) {
                                    log.debug("NIFS RISA 관측값 조회 성공: stationId={}, layer={}, temp={}", 
                                            stationId, layer, item.getWaterTemp());
                                    return Mono.just(item);
                                }
                            }
                        }

                        // 우선순위에 맞는 레이어가 없으면 첫 번째 항목 반환
                        NifsRisaResponse.NifsRisaItem firstItem = stationItems.get(0);
                        log.debug("NIFS RISA 관측값 조회 성공 (우선순위 레이어 없음): stationId={}, layer={}, temp={}", 
                                stationId, firstItem.getObsLayInt(), firstItem.getWaterTemp());
                        return Mono.just(firstItem);
                    } catch (Exception e) {
                        log.error("NIFS RISA 관측값 파싱 실패: stationId={}, response={}", stationId, 
                                response.substring(0, Math.min(200, response.length())), e);
                        return Mono.empty();
                    }
                })
                .doOnError(error -> log.error("NIFS RISA 관측값 조회 실패: stationId={}", stationId, error))
                .onErrorResume(error -> {
                    log.warn("NIFS RISA 관측값 조회 에러 처리: stationId={}, error={} (관측소가 API에서 제공되지 않을 수 있음)", 
                            stationId, error.getMessage());
                    return Mono.empty();
                });
    }
}

