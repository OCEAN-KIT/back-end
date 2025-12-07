package com.ocean.piuda.environment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.environment.client.dto.NifsRisaResponse;
import com.ocean.piuda.environment.properties.MarineApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * NIFS RISA 실시간 해양수산환경 관측 API 클라이언트
 */
@Slf4j
@Component
public class NifsRisaClient {

    private static final List<Integer> LAYER_PRIORITY = List.of(2, 1, 3); // 중층 -> 표층 -> 저층 순
    // [필수] NIFS는 EUC-KR 인코딩을 사용함 (헤더도 없음)
    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    private final WebClient webClient;
    private final MarineApiProperties properties;
    private final ObjectMapper objectMapper;

    public NifsRisaClient(
            @Qualifier("marineWebClient") WebClient marineWebClient,
            MarineApiProperties properties,
            ObjectMapper objectMapper
    ) {
        this.webClient = marineWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 관측소 목록 조회
     */
    public Mono<List<NifsRisaResponse.NifsRisaItem>> fetchStations() {
        return webClient.get()
                .uri(uriBuilder -> {
                    MarineApiProperties.Nifs config = properties.getNifs();
                    return uriBuilder
                            .scheme("https")
                            .host(config.getHost())
                            .path(config.getRisaPath())
                            .queryParam("id", "risaList")
                            .queryParam("key", config.getKey())
                            .build();
                })
                .retrieve()
                // [중요] byte[]로 받아서 EUC-KR로 디코딩
                .bodyToMono(byte[].class)
                .map(bytes -> new String(bytes, EUC_KR))
                .map(response -> {
                    try {
                        NifsRisaResponse parsed = objectMapper.readValue(response, NifsRisaResponse.class);
                        if (parsed.getBody() != null && parsed.getBody().getItem() != null) {
                            return parsed.getBody().getItem();
                        }
                        return new ArrayList<NifsRisaResponse.NifsRisaItem>();
                    } catch (Exception e) {
                        String preview = response != null
                                ? response.substring(0, Math.min(200, response.length()))
                                : "null";
                        log.error("NIFS RISA 응답 파싱 실패: {}", preview, e);
                        return new ArrayList<NifsRisaResponse.NifsRisaItem>();
                    }
                })
                .doOnError(error -> log.error("NIFS RISA API 호출 실패", error))
                .onErrorResume(error -> Mono.just(new ArrayList<>()));
    }

    /**
     * 특정 관측소의 최신 관측값 조회
     */
    public Mono<NifsRisaResponse.NifsRisaItem> fetchLatestObservation(String stationId) {
        return webClient.get()
                .uri(uriBuilder -> {
                    MarineApiProperties.Nifs config = properties.getNifs();
                    return uriBuilder
                            .scheme("https")
                            .host(config.getHost())
                            .path(config.getRisaPath())
                            .queryParam("id", "risaList")
                            .queryParam("key", config.getKey())
                            .queryParam("sta_cde", stationId)
                            .build();
                })
                .retrieve()
                // [중요] byte[]로 받아서 EUC-KR로 디코딩
                .bodyToMono(byte[].class)
                .map(bytes -> new String(bytes, EUC_KR))
                .flatMap(response -> {
                    try {
                        NifsRisaResponse parsed = objectMapper.readValue(response, NifsRisaResponse.class);
                        if (parsed.getBody() == null || parsed.getBody().getItem() == null
                                || parsed.getBody().getItem().isEmpty()) {
                            log.warn("NIFS RISA API 응답이 비어있습니다: stationId={}", stationId);
                            return Mono.empty();
                        }

                        List<NifsRisaResponse.NifsRisaItem> allItems = parsed.getBody().getItem();
                        List<NifsRisaResponse.NifsRisaItem> stationItems = allItems.stream()
                                .filter(item -> stationId.equals(item.getStaCde()))
                                .toList();

                        if (stationItems.isEmpty()) {
                            log.warn("NIFS RISA API에서 해당 관측소 데이터를 찾을 수 없습니다: stationId={}, 전체 관측소 수={}",
                                    stationId, allItems.size());
                            return Mono.empty();
                        }

                        for (Integer layer : LAYER_PRIORITY) {
                            for (NifsRisaResponse.NifsRisaItem item : stationItems) {
                                if (item.getObsLayInt() != null && item.getObsLayInt().equals(layer)) {
                                    log.debug("NIFS RISA 관측값 조회 성공: stationId={}, layer={}, temp={}",
                                            stationId, layer, item.getWaterTemp());
                                    return Mono.just(item);
                                }
                            }
                        }

                        NifsRisaResponse.NifsRisaItem firstItem = stationItems.get(0);
                        log.debug("NIFS RISA 관측값 조회 성공 (우선순위 레이어 없음): stationId={}, layer={}, temp={}",
                                stationId, firstItem.getObsLayInt(), firstItem.getWaterTemp());
                        return Mono.just(firstItem);
                    } catch (Exception e) {
                        String preview = response != null
                                ? response.substring(0, Math.min(200, response.length()))
                                : "null";
                        log.error("NIFS RISA 관측값 파싱 실패: stationId={}, response={}", stationId, preview, e);
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