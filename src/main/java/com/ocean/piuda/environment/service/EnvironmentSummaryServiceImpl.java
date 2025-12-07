package com.ocean.piuda.environment.service;

import com.ocean.piuda.environment.client.KhoaTideClient;
import com.ocean.piuda.environment.client.KmaSeaObsClient;
import com.ocean.piuda.environment.client.NifsRisaClient;
import com.ocean.piuda.divePoint.entity.DivePoint;
import com.ocean.piuda.environment.domain.MarineStation;
import com.ocean.piuda.environment.domain.StationSource;
import com.ocean.piuda.environment.dto.EnvironmentSummaryRequest;
import com.ocean.piuda.environment.dto.EnvironmentSummaryResponse;
import com.ocean.piuda.divePoint.repository.DivePointRepository;
import com.ocean.piuda.environment.repository.MarineStationRepository;
import com.ocean.piuda.environment.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 해양 환경 요약 서비스 구현체 (Reactive)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnvironmentSummaryServiceImpl implements EnvironmentSummaryService {

    private final NifsRisaClient nifsRisaClient;
    private final KhoaTideClient khoaTideClient;
    private final KmaSeaObsClient kmaSeaObsClient;
    private final MarineStationRepository marineStationRepository;
    private final DivePointRepository divePointRepository;

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");
    // [추가] 매직 넘버 추출: fallback으로 시도할 최대 관측소 수
    private static final int MAX_FALLBACK_STATIONS = 9;

    /**
     * 한 요청 내에서 재사용할 관측소/좌표 컨텍스트
     */
    private record StationContext(
            double lat,
            double lon,
            List<MarineStation> nifsStations,
            List<MarineStation> kmaStations,
            List<MarineStation> khoaStations,
            MarineStation nifsNearest,
            MarineStation kmaNearest,
            MarineStation khoaNearest,
            EnvironmentSummaryResponse.NearestStations nearestStations
    ) {}

    @Override
    public Mono<EnvironmentSummaryResponse> getEnvironmentSummary(EnvironmentSummaryRequest request) {
        // 1) DB 접근 / 관측소 리스트 조회 / 최근접 관측소 선택은 blocking → boundedElastic에서 수행
        return Mono.fromCallable(() -> buildStationContext(request))
                .subscribeOn(Schedulers.boundedElastic())
                // 2) 외부 API는 reactive로 병렬 호출
                .flatMap(ctx -> {
                    Mono<EnvironmentSummaryResponse.Water> waterMono =
                            fetchWaterData(ctx.nifsNearest(), ctx.lat(), ctx.lon(), ctx.nifsStations());

                    Mono<EnvironmentSummaryResponse.Wave> waveMono =
                            fetchWaveData(ctx.kmaNearest(), ctx.lat(), ctx.lon(), ctx.kmaStations());

                    Mono<EnvironmentSummaryResponse.Tide> tideMono =
                            fetchTideData(ctx.khoaNearest());

                    return Mono.zip(
                            waterMono.defaultIfEmpty(EnvironmentSummaryResponse.Water.builder().build()),
                            waveMono.defaultIfEmpty(EnvironmentSummaryResponse.Wave.builder().build()),
                            tideMono.defaultIfEmpty(EnvironmentSummaryResponse.Tide.builder().build())
                    ).map(tuple -> EnvironmentSummaryResponse.builder()
                            .location(EnvironmentSummaryResponse.Location.builder()
                                    .lat(ctx.lat())
                                    .lon(ctx.lon())
                                    .nearestStations(ctx.nearestStations())
                                    .build())
                            .timestamp(ZonedDateTime.now(ZONE_SEOUL))
                            .water(tuple.getT1())
                            .wave(tuple.getT2())
                            .tide(tuple.getT3())
                            .meta(EnvironmentSummaryResponse.Meta.builder()
                                    .rawSources(List.of(
                                            StationSource.NIFS_RISA.name(),
                                            StationSource.KMA_SEA_OBS.name(),
                                            StationSource.KHOA_SURVEY_TIDE.name()
                                    ))
                                    .note("실시간 관측자료는 품질 검증 전 데이터일 수 있음")
                                    .build())
                            .build());
                });
    }

    /**
     * 좌표/포인트 처리 + 관측소 리스트 조회 + 최근접 관측소 선택까지 한 번에 수행
     */
    private StationContext buildStationContext(EnvironmentSummaryRequest request) {
        // 기준 좌표
        double lat;
        double lon;

        if (request.isPointIdBased()) {
            DivePoint point = divePointRepository.findById(request.pointId())
                    .orElseThrow(() -> new IllegalArgumentException("다이빙 포인트를 찾을 수 없습니다: " + request.pointId()));
            lat = point.getLat();
            lon = point.getLon();
        } else if (request.isCoordinateBased()) {
            lat = request.lat();
            lon = request.lon();
        } else {
            throw new IllegalArgumentException("lat/lon 또는 pointId가 필요합니다.");
        }

        log.info("환경 요약 조회 시작: lat={}, lon={}, pointId={}", lat, lon, request.pointId());

        // 관측소 리스트를 각 source별로 딱 한 번씩만 조회
        List<MarineStation> nifsStations =
                marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.NIFS_RISA);
        List<MarineStation> kmaStations =
                marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.KMA_SEA_OBS);
        List<MarineStation> khoaStations =
                marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.KHOA_SURVEY_TIDE);

        MarineStation nifsNearest = findNearestStation(lat, lon, StationSource.NIFS_RISA, nifsStations, khoaStations);
        MarineStation kmaNearest = findNearestStation(lat, lon, StationSource.KMA_SEA_OBS, kmaStations, null);
        MarineStation khoaNearest = findNearestStation(lat, lon, StationSource.KHOA_SURVEY_TIDE, khoaStations, null);

        if (nifsNearest == null && kmaNearest == null && khoaNearest == null) {
            log.warn("모든 관측소 데이터가 없습니다. 관측소 데이터를 초기화해야 합니다.");
        }

        EnvironmentSummaryResponse.NearestStations nearestStations =
                EnvironmentSummaryResponse.NearestStations.builder()
                        .nifs(buildStationInfo(nifsNearest, lat, lon, khoaStations))
                        .kma(buildStationInfo(kmaNearest, lat, lon, null))
                        .khoa(buildStationInfo(khoaNearest, lat, lon, null))
                        .build();

        return new StationContext(
                lat,
                lon,
                nifsStations,
                kmaStations,
                khoaStations,
                nifsNearest,
                kmaNearest,
                khoaNearest,
                nearestStations
        );
    }

    /**
     * 가장 가까운 관측소 찾기 (이미 조회한 리스트 활용)
     */
    private MarineStation findNearestStation(double lat,
                                             double lon,
                                             StationSource source,
                                             List<MarineStation> sourceStations,
                                             List<MarineStation> khoaStationsForNifsFallback) {
        if (sourceStations == null || sourceStations.isEmpty()) {
            log.warn("관측소 데이터가 없습니다: source={}, lat={}, lon={}", source, lat, lon);
            return null;
        }

        // 유효한 좌표를 가진 관측소만
        List<MarineStation> stationsWithLocation = sourceStations.stream()
                .filter(station -> station.getLat() != null && station.getLon() != null)
                .filter(station -> station.getLat() != 0.0 && station.getLon() != 0.0)
                .toList();

        // NIFS RISA 이고 좌표 있는 관측소가 없으면 KHOA 좌표를 참고해서 첫 관측소를 선택
        if (source == StationSource.NIFS_RISA && stationsWithLocation.isEmpty()
                && khoaStationsForNifsFallback != null && !khoaStationsForNifsFallback.isEmpty()) {
            MarineStation nearestKhoa = khoaStationsForNifsFallback.stream()
                    .filter(khoa -> khoa.getLat() != null && khoa.getLon() != null)
                    .filter(khoa -> khoa.getLat() != 0.0 && khoa.getLon() != 0.0)
                    .min(Comparator.comparingDouble(station ->
                            DistanceCalculator.calculateDistance(lat, lon, station.getLat(), station.getLon())))
                    .orElse(null);

            if (nearestKhoa != null) {
                MarineStation nifsStation = sourceStations.get(0); // 좌표 없는 NIFS 중 첫 번째
                double distance = DistanceCalculator.calculateDistance(
                        lat, lon, nearestKhoa.getLat(), nearestKhoa.getLon());
                log.info("NIFS RISA 관측소 매칭 (KHOA 좌표 참고): stationId={}, name={}, khoaStation={}, distance={}km",
                        nifsStation.getExternalStationId(), nifsStation.getName(),
                        nearestKhoa.getName(), Math.round(distance * 10.0) / 10.0);
                return nifsStation;
            }
        }

        if (stationsWithLocation.isEmpty()) {
            log.warn("위치 정보가 있는 관측소가 없습니다: source={}, lat={}, lon={}", source, lat, lon);
            return null;
        }

        MarineStation nearest = stationsWithLocation.stream()
                .min(Comparator.comparingDouble(station ->
                        DistanceCalculator.calculateDistance(lat, lon, station.getLat(), station.getLon())))
                .orElse(null);

        if (nearest != null) {
            double distance = DistanceCalculator.calculateDistance(
                    lat, lon, nearest.getLat(), nearest.getLon());
            log.debug("가장 가까운 관측소 찾음: source={}, stationId={}, name={}, distance={}km",
                    source, nearest.getExternalStationId(), nearest.getName(),
                    Math.round(distance * 10.0) / 10.0);
        }

        return nearest;
    }

    /**
     * 수온/염분/용존산소 데이터 조회 (이미 조회한 NIFS 리스트로 fallback 수행)
     */
    private Mono<EnvironmentSummaryResponse.Water> fetchWaterData(MarineStation station,
                                                                  double requestLat,
                                                                  double requestLon,
                                                                  List<MarineStation> allNifsStations) {
        if (station == null) {
            log.warn("수온 데이터 조회 실패: 관측소가 null입니다");
            return Mono.just(EnvironmentSummaryResponse.Water.builder().build());
        }

        return fetchWaterDataFromStation(station)
                .flatMap(firstResult -> {
                    // 첫 관측소에 유효한 수온 데이터가 있으면 그대로 사용
                    if (firstResult.midLayerTemp() != null || firstResult.surfaceTemp() != null) {
                        return Mono.just(firstResult);
                    }

                    log.info("수온 데이터가 없어 다음 가까운 NIFS RISA 관측소를 시도합니다: stationId={}",
                            station.getExternalStationId());

                    // 이미 조회한 NIFS 관측소 리스트에서 다음 가까운 관측소 찾기
                    List<MarineStation> candidates = allNifsStations.stream()
                            .filter(s -> s.getLat() != null && s.getLon() != null)
                            .filter(s -> s.getLat() != 0.0 && s.getLon() != 0.0)
                            .filter(s -> !Objects.equals(
                                    s.getExternalStationId(), station.getExternalStationId()))
                            .toList();

                    if (candidates.isEmpty()) {
                        log.warn("다음 가까운 NIFS RISA 관측소를 찾을 수 없습니다.");
                        return Mono.just(firstResult);
                    }

                    MarineStation nextStation = candidates.stream()
                            .min(Comparator.comparingDouble(s ->
                                    DistanceCalculator.calculateDistance(
                                            requestLat, requestLon, s.getLat(), s.getLon())))
                            .orElse(null);

                    if (nextStation == null) {
                        log.warn("다음 가까운 NIFS RISA 관측소를 찾을 수 없습니다.");
                        return Mono.just(firstResult);
                    }

                    double distance = DistanceCalculator.calculateDistance(
                            requestLat, requestLon, nextStation.getLat(), nextStation.getLon());
                    log.info("다음 가까운 NIFS RISA 관측소 시도: stationId={}, name={}, distance={}km",
                            nextStation.getExternalStationId(), nextStation.getName(),
                            Math.round(distance * 10.0) / 10.0);

                    return fetchWaterDataFromStation(nextStation)
                            .map(nextResult -> {
                                if (nextResult.midLayerTemp() != null || nextResult.surfaceTemp() != null) {
                                    log.info("다음 관측소에서 수온 데이터 획득: stationId={}, temp={}",
                                            nextStation.getExternalStationId(), nextResult.midLayerTemp());
                                    return nextResult;
                                } else {
                                    log.warn("다음 관측소에서도 수온 데이터를 얻지 못했습니다: stationId={}",
                                            nextStation.getExternalStationId());
                                    return firstResult;
                                }
                            });
                });
    }

    /**
     * 특정 NIFS 관측소에서 수온/염분/용존산소 데이터 조회
     */
    private Mono<EnvironmentSummaryResponse.Water> fetchWaterDataFromStation(MarineStation station) {
        String stationId = station.getExternalStationId();
        log.info("수온 데이터 조회 시작: stationId={}, name={}", stationId, station.getName());

        return nifsRisaClient.fetchLatestObservation(stationId)
                .map(observation -> {
                    Double waterTemp = observation.getWaterTemp();
                    log.info("수온 데이터 조회 성공: stationId={}, temp={}, layer={}",
                            stationId, waterTemp, observation.getObsLayInt());

                    return EnvironmentSummaryResponse.Water.builder()
                            .midLayerTemp(waterTemp)
                            .surfaceTemp(waterTemp) // 중층 데이터를 표층으로도 사용
                            .salinity(observation.getSalinity())
                            .dissolvedOxygen(observation.getDissolvedOxygen())
                            .build();
                })
                .switchIfEmpty(Mono.fromRunnable(() ->
                                log.warn("수온 데이터 조회 실패: observation이 null입니다. stationId={} (API에서 제공하지 않을 수 있음)",
                                        stationId))
                        .then(Mono.just(EnvironmentSummaryResponse.Water.builder().build())))
                .onErrorResume(e -> {
                    log.error("수온 데이터 조회 실패: stationId={}", stationId, e);
                    return Mono.just(EnvironmentSummaryResponse.Water.builder().build());
                });
    }

    /**
     * 파고/풍향/풍속 데이터 조회
     * - 첫 관측소에서 파고만 있고 풍향/풍속이 없으면
     * 미리 조회한 KMA 관측소 리스트를 이용해
     * 최대 MAX_FALLBACK_STATIONS 개까지 다른 관측소에서 풍향/풍속을 보충
     */
    private Mono<EnvironmentSummaryResponse.Wave> fetchWaveData(MarineStation station,
                                                                double requestLat,
                                                                double requestLon,
                                                                List<MarineStation> allKmaStations) {
        if (station == null) {
            log.warn("파고 데이터 조회 실패: 관측소가 null입니다");
            return Mono.just(EnvironmentSummaryResponse.Wave.builder().build());
        }

        return fetchWaveDataFromStation(station)
                .flatMap(firstResult -> {
                    Double significantWaveHeight = firstResult.significantWaveHeight();

                    // 풍향/풍속이 있거나, 파고 자체도 없으면 더 시도하지 않음
                    if ((firstResult.windDirectionDeg() != null || firstResult.windSpeedMs() != null)
                            || significantWaveHeight == null) {
                        return Mono.just(firstResult);
                    }

                    log.info("풍향/풍속 데이터가 없어 다음 가까운 KMA 관측소를 시도합니다: stationId={}",
                            station.getExternalStationId());

                    // 이미 조회한 KMA 관측소 리스트에서 fallback 후보 정렬
                    List<MarineStation> candidates = allKmaStations.stream()
                            .filter(s -> s.getLat() != null && s.getLon() != null)
                            .filter(s -> s.getLat() != 0.0 && s.getLon() != 0.0)
                            .filter(s -> !Objects.equals(
                                    s.getExternalStationId(), station.getExternalStationId()))
                            .sorted(Comparator.comparingDouble(s ->
                                    DistanceCalculator.calculateDistance(
                                            requestLat, requestLon, s.getLat(), s.getLon())))
                            .limit(MAX_FALLBACK_STATIONS) // 1차 관측소 포함 총 10개가 되도록 제한
                            .toList();

                    if (candidates.isEmpty()) {
                        log.warn("다음 가까운 KMA 관측소를 찾을 수 없습니다.");
                        return Mono.just(firstResult);
                    }

                    return Flux.fromIterable(candidates)
                            .concatMap(nextStation -> {
                                double distance = DistanceCalculator.calculateDistance(
                                        requestLat, requestLon, nextStation.getLat(), nextStation.getLon());
                                log.info("다음 가까운 KMA 관측소 시도: stationId={}, name={}, distance={}km",
                                        nextStation.getExternalStationId(), nextStation.getName(),
                                        Math.round(distance * 10.0) / 10.0);

                                return fetchWaveDataFromStation(nextStation)
                                        .filter(wave -> wave.windDirectionDeg() != null
                                                || wave.windSpeedMs() != null)
                                        .doOnNext(wave -> log.info("관측소에서 풍향/풍속 데이터 획득: stationId={}, wd={}, ws={}",
                                                nextStation.getExternalStationId(),
                                                wave.windDirectionDeg(), wave.windSpeedMs()));
                            })
                            .next()
                            .map(waveWithWind -> EnvironmentSummaryResponse.Wave.builder()
                                    .significantWaveHeight(significantWaveHeight)
                                    .windDirectionDeg(waveWithWind.windDirectionDeg())
                                    .windSpeedMs(waveWithWind.windSpeedMs())
                                    .build())
                            .switchIfEmpty(Mono.fromRunnable(() ->
                                            log.warn("여러 관측소를 시도했지만 풍향/풍속 데이터를 얻지 못했습니다."))
                                    .then(Mono.just(firstResult)));
                });
    }

    /**
     * 특정 KMA 관측소에서 파고/풍향/풍속 데이터 조회
     */
    private Mono<EnvironmentSummaryResponse.Wave> fetchWaveDataFromStation(MarineStation station) {
        String stationId = station.getExternalStationId();
        log.info("파고 데이터 조회 시작: stationId={}, name={}", stationId, station.getName());

        return kmaSeaObsClient.fetchLatestObservation(stationId)
                .map(observation -> {
                    log.info("파고 데이터 조회 성공: stationId={}, wh={}, wd={}, ws={}",
                            stationId, observation.getWh(), observation.getWd(), observation.getWs());

                    return EnvironmentSummaryResponse.Wave.builder()
                            .significantWaveHeight(observation.getWh())
                            .windDirectionDeg(observation.getWd())
                            .windSpeedMs(observation.getWs())
                            .build();
                })
                .switchIfEmpty(Mono.fromRunnable(() ->
                                log.warn("파고 데이터 조회 실패: observation이 null입니다. stationId={}", stationId))
                        .then(Mono.just(EnvironmentSummaryResponse.Wave.builder().build())))
                .onErrorResume(e -> {
                    log.error("파고 데이터 조회 실패: stationId={}", stationId, e);
                    return Mono.just(EnvironmentSummaryResponse.Wave.builder().build());
                });
    }

    /**
     * 조위 데이터 조회 (Reactive)
     */
    private Mono<EnvironmentSummaryResponse.Tide> fetchTideData(MarineStation station) {
        if (station == null) {
            return Mono.just(EnvironmentSummaryResponse.Tide.builder().build());
        }

        String stationId = station.getExternalStationId();

        return khoaTideClient.fetchLatestTideLevel(stationId)
                .flatMap(observation -> {
                    // 관측시각 파싱 (yyyyMMddHHmm)
                    ZonedDateTime observedAt = null;
                    if (observation.getObsDt() != null && observation.getObsDt().length() >= 12) {
                        try {
                            String dtStr = observation.getObsDt();
                            LocalDate date = LocalDate.parse(dtStr.substring(0, 8),
                                    DateTimeFormatter.ofPattern("yyyyMMdd"));
                            LocalTime time = LocalTime.parse(dtStr.substring(8, 12),
                                    DateTimeFormatter.ofPattern("HHmm"));
                            observedAt = ZonedDateTime.of(date, time, ZONE_SEOUL);
                        } catch (Exception e) {
                            log.warn("조위 관측시각 파싱 실패: {}", observation.getObsDt(), e);
                        }
                    }

                    return Mono.just(EnvironmentSummaryResponse.Tide.builder()
                            .tideLevelCm(observation.getTideLevel())
                            .tideObservedAt(observedAt)
                            .build());
                })
                .switchIfEmpty(Mono.fromRunnable(() ->
                                log.warn("조위 데이터 조회 실패: observation이 null입니다. stationId={}", stationId))
                        .then(Mono.just(EnvironmentSummaryResponse.Tide.builder().build())))
                .onErrorResume(e -> {
                    log.error("조위 데이터 조회 실패: stationId={}", stationId, e);
                    return Mono.just(EnvironmentSummaryResponse.Tide.builder().build());
                });
    }

    /**
     * 관측소 정보 + 거리 정보 빌드
     * - NIFS 좌표가 0.0,0.0인 경우 KHOA 관측소 좌표를 참고해서 거리 계산
     */
    private EnvironmentSummaryResponse.StationInfo buildStationInfo(MarineStation station,
                                                                    double lat,
                                                                    double lon,
                                                                    List<MarineStation> khoaStations) {
        if (station == null) {
            return null;
        }

        double stationLat = station.getLat();
        double stationLon = station.getLon();

        if (station.getExternalSource() == StationSource.NIFS_RISA
                && (stationLat == 0.0 || stationLon == 0.0)
                && khoaStations != null && !khoaStations.isEmpty()) {

            MarineStation nearestKhoa = khoaStations.stream()
                    .filter(s -> s.getLat() != null && s.getLon() != null)
                    .filter(s -> s.getLat() != 0.0 && s.getLon() != 0.0)
                    .min(Comparator.comparingDouble(s ->
                            DistanceCalculator.calculateDistance(lat, lon, s.getLat(), s.getLon())))
                    .orElse(null);

            if (nearestKhoa != null) {
                stationLat = nearestKhoa.getLat();
                stationLon = nearestKhoa.getLon();
            }
        }

        double distance = DistanceCalculator.calculateDistance(lat, lon, stationLat, stationLon);

        return EnvironmentSummaryResponse.StationInfo.builder()
                .id(station.getExternalStationId())
                .name(station.getName())
                .distanceKm(Math.round(distance * 10.0) / 10.0)
                .build();
    }
}