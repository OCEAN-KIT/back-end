package com.ocean.piuda.environment.service;

import com.ocean.piuda.environment.client.KhoaTideClient;
import com.ocean.piuda.environment.client.KmaSeaObsClient;
import com.ocean.piuda.environment.client.NifsRisaClient;
import com.ocean.piuda.environment.client.dto.KhoaTideResponse;
import com.ocean.piuda.environment.client.dto.KmaSeaObsResponse;
import com.ocean.piuda.environment.client.dto.NifsRisaResponse;
import com.ocean.piuda.environment.domain.*;
import com.ocean.piuda.environment.dto.EnvironmentSummaryRequest;
import com.ocean.piuda.environment.dto.EnvironmentSummaryResponse;
import com.ocean.piuda.environment.repository.DivePointRepository;
import com.ocean.piuda.environment.repository.MarineStationRepository;
import com.ocean.piuda.environment.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 해양 환경 요약 서비스 구현체
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

    private static final DateTimeFormatter NIFS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EnvironmentSummaryResponse getEnvironmentSummary(EnvironmentSummaryRequest request) {
        // 1. 기준 좌표 확보
        double lat, lon;
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

        // 2. 가장 가까운 관측소 매칭
        MarineStation nifsStation = findNearestStation(lat, lon, StationSource.NIFS_RISA);
        MarineStation kmaStation = findNearestStation(lat, lon, StationSource.KMA_SEA_OBS);
        MarineStation khoaStation = findNearestStation(lat, lon, StationSource.KHOA_SURVEY_TIDE);

        if (nifsStation == null && kmaStation == null && khoaStation == null) {
            log.warn("모든 관측소 데이터가 없습니다. 관측소 데이터를 초기화해야 합니다.");
        }

        // 3. 각 API 호출 (비동기로 병렬 처리 가능하지만, 여기서는 순차 처리)
        EnvironmentSummaryResponse.Water water = fetchWaterData(nifsStation, lat, lon);
        EnvironmentSummaryResponse.Wave wave = fetchWaveData(kmaStation, lat, lon);
        EnvironmentSummaryResponse.Tide tide = fetchTideData(khoaStation);

        // 4. 최종 JSON 조립
        return EnvironmentSummaryResponse.builder()
                .location(EnvironmentSummaryResponse.Location.builder()
                        .lat(lat)
                        .lon(lon)
                        .nearestStations(EnvironmentSummaryResponse.NearestStations.builder()
                                .nifs(buildStationInfo(nifsStation, lat, lon))
                                .kma(buildStationInfo(kmaStation, lat, lon))
                                .khoa(buildStationInfo(khoaStation, lat, lon))
                                .build())
                        .build())
                .timestamp(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
                .water(water)
                .wave(wave)
                .tide(tide)
                .meta(EnvironmentSummaryResponse.Meta.builder()
                        .rawSources(List.of("NIFS_RISA", "KMA_SEA_OBS", "KHOA_SURVEY_TIDE"))
                        .note("실시간 관측자료는 품질 검증 전 데이터일 수 있음")
                        .build())
                .build();
    }

    /**
     * 가장 가까운 관측소 찾기
     * NIFS RISA의 경우 좌표가 없는 경우 KHOA 조위관측소 좌표를 참고하여 매칭
     */
    private MarineStation findNearestStation(double lat, double lon, StationSource source) {
        List<MarineStation> stations = marineStationRepository.findByExternalSourceAndIsActiveTrue(source);

        if (stations.isEmpty()) {
            log.warn("관측소 데이터가 없습니다: source={}, lat={}, lon={}", source, lat, lon);
            return null;
        }

        // 위치 정보가 있는 관측소만 필터링 (0.0, 0.0 좌표 제외 - 임시 좌표)
        List<MarineStation> stationsWithLocation = stations.stream()
                .filter(station -> station.getLat() != null && station.getLon() != null)
                .filter(station -> station.getLat() != 0.0 && station.getLon() != 0.0)
                .toList();

        // NIFS RISA의 경우 좌표가 없는 관측소가 있으면 KHOA 조위관측소 좌표를 참고하여 매칭
        if (source == StationSource.NIFS_RISA && stationsWithLocation.isEmpty()) {
            log.info("NIFS RISA 관측소에 좌표가 없어 KHOA 조위관측소 좌표를 참고하여 매칭 시도");
            List<MarineStation> khoaStations = marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.KHOA_SURVEY_TIDE);
            List<MarineStation> khoaWithLocation = khoaStations.stream()
                    .filter(station -> station.getLat() != null && station.getLon() != null)
                    .filter(station -> station.getLat() != 0.0 && station.getLon() != 0.0)
                    .toList();

            if (!khoaWithLocation.isEmpty()) {
                // 가장 가까운 KHOA 관측소 찾기
                MarineStation nearestKhoa = khoaWithLocation.stream()
                        .min(Comparator.comparingDouble(station ->
                                DistanceCalculator.calculateDistance(lat, lon, station.getLat(), station.getLon())))
                        .orElse(null);

                if (nearestKhoa != null) {
                    // NIFS 관측소는 좌표가 없으므로, 가장 가까운 KHOA 관측소를 기준으로 
                    // 첫 번째 NIFS 관측소를 반환 (실제로는 모든 NIFS 관측소가 동일하게 처리됨)
                    // 거리는 KHOA 관측소 기준으로 계산
                    MarineStation nifsStation = stations.get(0);
                    double distance = DistanceCalculator.calculateDistance(lat, lon, nearestKhoa.getLat(), nearestKhoa.getLon());
                    log.info("NIFS RISA 관측소 매칭 (KHOA 좌표 참고): stationId={}, name={}, khoaStation={}, distance={}km",
                            nifsStation.getExternalStationId(), nifsStation.getName(), nearestKhoa.getName(), Math.round(distance * 10.0) / 10.0);
                    return nifsStation; // NIFS 관측소 반환 (관측소 ID는 유효하지만 좌표는 없음)
                }
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
            double distance = DistanceCalculator.calculateDistance(lat, lon, nearest.getLat(), nearest.getLon());
            log.debug("가장 가까운 관측소 찾음: source={}, stationId={}, name={}, distance={}km", 
                    source, nearest.getExternalStationId(), nearest.getName(), Math.round(distance * 10.0) / 10.0);
        }

        return nearest;
    }

    /**
     * 수온/염분/용존산소 데이터 조회
     * 관측소가 NIFS RISA API에서 제공하지 않으면 다음 가까운 관측소를 시도
     * 
     * @param station 첫 번째 관측소
     * @param requestLat 요청한 위도
     * @param requestLon 요청한 경도
     */
    private EnvironmentSummaryResponse.Water fetchWaterData(MarineStation station, double requestLat, double requestLon) {
        if (station == null) {
            log.warn("수온 데이터 조회 실패: 관측소가 null입니다");
            return EnvironmentSummaryResponse.Water.builder().build();
        }

        // 첫 번째 관측소 시도
        EnvironmentSummaryResponse.Water result = fetchWaterDataFromStation(station);
        
        // 수온 데이터가 null인 경우 다음 가까운 관측소 시도
        if (result.midLayerTemp() == null && result.surfaceTemp() == null) {
            log.info("수온 데이터가 없어 다음 가까운 NIFS RISA 관측소를 시도합니다: stationId={}", station.getExternalStationId());
            
            // 요청 좌표 기준으로 다음 가까운 관측소 찾기
            List<MarineStation> allNifsStations = marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.NIFS_RISA);
            List<MarineStation> stationsWithLocation = allNifsStations.stream()
                    .filter(s -> s.getLat() != null && s.getLon() != null)
                    .filter(s -> s.getLat() != 0.0 && s.getLon() != 0.0)
                    .filter(s -> !s.getExternalStationId().equals(station.getExternalStationId())) // 현재 관측소 제외
                    .toList();
            
            if (!stationsWithLocation.isEmpty()) {
                // 요청 좌표 기준으로 거리순 정렬하여 다음 가까운 관측소 찾기
                MarineStation nextStation = stationsWithLocation.stream()
                        .min(Comparator.comparingDouble(s ->
                                DistanceCalculator.calculateDistance(
                                        requestLat, requestLon,
                                        s.getLat(), s.getLon())))
                        .orElse(null);
                
                if (nextStation != null) {
                    double distance = DistanceCalculator.calculateDistance(
                            requestLat, requestLon,
                            nextStation.getLat(), nextStation.getLon());
                    log.info("다음 가까운 NIFS RISA 관측소 시도: stationId={}, name={}, distance={}km",
                            nextStation.getExternalStationId(), nextStation.getName(), Math.round(distance * 10.0) / 10.0);
                    
                    EnvironmentSummaryResponse.Water nextResult = fetchWaterDataFromStation(nextStation);
                    
                    // 다음 관측소에서 수온 데이터를 얻었으면 반환
                    if (nextResult.midLayerTemp() != null || nextResult.surfaceTemp() != null) {
                        log.info("다음 관측소에서 수온 데이터 획득: stationId={}, temp={}",
                                nextStation.getExternalStationId(), nextResult.midLayerTemp());
                        return nextResult;
                    } else {
                        log.warn("다음 관측소에서도 수온 데이터를 얻지 못했습니다: stationId={}", nextStation.getExternalStationId());
                    }
                }
            } else {
                log.warn("다음 가까운 NIFS RISA 관측소를 찾을 수 없습니다.");
            }
        }
        
        return result;
    }

    /**
     * 특정 관측소에서 수온/염분/용존산소 데이터 조회
     */
    private EnvironmentSummaryResponse.Water fetchWaterDataFromStation(MarineStation station) {
        String stationId = station.getExternalStationId();
        log.info("수온 데이터 조회 시작: stationId={}, name={}", stationId, station.getName());

        try {
            NifsRisaResponse.NifsRisaItem observation = nifsRisaClient
                    .fetchLatestObservation(stationId)
                    .block(); // 비동기 호출을 동기로 변환

            if (observation == null) {
                log.warn("수온 데이터 조회 실패: observation이 null입니다. stationId={} (API에서 제공하지 않을 수 있음)", stationId);
                return EnvironmentSummaryResponse.Water.builder().build();
            }

            Double waterTemp = observation.getWaterTemp();
            log.info("수온 데이터 조회 성공: stationId={}, temp={}, layer={}", 
                    stationId, waterTemp, observation.getObsLayInt());

            return EnvironmentSummaryResponse.Water.builder()
                    .midLayerTemp(waterTemp)
                    .surfaceTemp(waterTemp) // 중층 데이터를 표층으로도 사용
                    .salinity(observation.getSalinity()) // 현재 API에서 제공하지 않음
                    .dissolvedOxygen(observation.getDissolvedOxygen()) // 현재 API에서 제공하지 않음
                    .build();
        } catch (Exception e) {
            log.error("수온 데이터 조회 실패: stationId={}", stationId, e);
            return EnvironmentSummaryResponse.Water.builder().build();
        }
    }

    /**
     * 파고/풍향/풍속 데이터 조회
     * 풍향/풍속이 null이면 최대 3번째 관측소까지 시도
     * 
     * @param station 첫 번째 관측소
     * @param requestLat 요청한 위도
     * @param requestLon 요청한 경도
     */
    private EnvironmentSummaryResponse.Wave fetchWaveData(MarineStation station, double requestLat, double requestLon) {
        if (station == null) {
            log.warn("파고 데이터 조회 실패: 관측소가 null입니다");
            return EnvironmentSummaryResponse.Wave.builder().build();
        }

        // 첫 번째 관측소 시도
        EnvironmentSummaryResponse.Wave result = fetchWaveDataFromStation(station);
        Double significantWaveHeight = result.significantWaveHeight(); // 첫 번째 관측소의 파고 저장
        
        // 풍향/풍속이 모두 null이고, 파고 데이터는 있는 경우 다음 가까운 관측소 시도 (최대 10개까지 또는 풍향/풍속 데이터를 찾을 때까지)
        if (result.windDirectionDeg() == null && result.windSpeedMs() == null && significantWaveHeight != null) {
            log.info("풍향/풍속 데이터가 없어 다음 가까운 KMA 관측소를 시도합니다: stationId={}", station.getExternalStationId());
            
            // 요청 좌표 기준으로 다음 가까운 관측소 찾기
            List<MarineStation> allKmaStations = marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.KMA_SEA_OBS);
            List<MarineStation> stationsWithLocation = allKmaStations.stream()
                    .filter(s -> s.getLat() != null && s.getLon() != null)
                    .filter(s -> s.getLat() != 0.0 && s.getLon() != 0.0)
                    .filter(s -> !s.getExternalStationId().equals(station.getExternalStationId())) // 현재 관측소 제외
                    .sorted(Comparator.comparingDouble(s ->
                            DistanceCalculator.calculateDistance(requestLat, requestLon, s.getLat(), s.getLon())))
                    .toList();
            
            // 이미 시도한 관측소 목록
            List<String> triedStationIds = new ArrayList<>();
            triedStationIds.add(station.getExternalStationId());
            
            // 최대 9개 더 시도 (총 10개까지) 또는 풍향/풍속 데이터를 찾을 때까지
            int maxAttempts = Math.min(9, stationsWithLocation.size());
            for (int i = 0; i < maxAttempts; i++) {
                MarineStation nextStation = stationsWithLocation.get(i);
                
                if (triedStationIds.contains(nextStation.getExternalStationId())) {
                    continue;
                }
                
                double distance = DistanceCalculator.calculateDistance(
                        requestLat, requestLon,
                        nextStation.getLat(), nextStation.getLon());
                log.info("다음 가까운 KMA 관측소 시도 ({}/{}): stationId={}, name={}, distance={}km",
                        i + 2, maxAttempts + 1, nextStation.getExternalStationId(), nextStation.getName(), Math.round(distance * 10.0) / 10.0);
                
                EnvironmentSummaryResponse.Wave nextResult = fetchWaveDataFromStation(nextStation);
                triedStationIds.add(nextStation.getExternalStationId());
                
                // 다음 관측소에서 풍향/풍속 데이터를 얻었으면 병합
                if (nextResult.windDirectionDeg() != null || nextResult.windSpeedMs() != null) {
                    log.info("관측소에서 풍향/풍속 데이터 획득: stationId={}, wd={}, ws={}",
                            nextStation.getExternalStationId(), nextResult.windDirectionDeg(), nextResult.windSpeedMs());
                    return EnvironmentSummaryResponse.Wave.builder()
                            .significantWaveHeight(significantWaveHeight) // 첫 번째 관측소의 파고 사용
                            .windDirectionDeg(nextResult.windDirectionDeg())
                            .windSpeedMs(nextResult.windSpeedMs())
                            .build();
                } else {
                    log.warn("관측소에서 풍향/풍속 데이터를 얻지 못했습니다: stationId={}", nextStation.getExternalStationId());
                }
            }
            
            log.warn("{}개 관측소를 모두 시도했지만 풍향/풍속 데이터를 얻지 못했습니다.", maxAttempts + 1);
        }
        
        return result;
    }

    /**
     * 특정 관측소에서 파고/풍향/풍속 데이터 조회
     */
    private EnvironmentSummaryResponse.Wave fetchWaveDataFromStation(MarineStation station) {
        String stationId = station.getExternalStationId();
        log.info("파고 데이터 조회 시작: stationId={}, name={}", stationId, station.getName());

        try {
            KmaSeaObsResponse.Item observation = kmaSeaObsClient
                    .fetchLatestObservation(stationId)
                    .block();

            if (observation == null) {
                log.warn("파고 데이터 조회 실패: observation이 null입니다. stationId={}", stationId);
                return EnvironmentSummaryResponse.Wave.builder().build();
            }

            log.info("파고 데이터 조회 성공: stationId={}, wh={}, wd={}, ws={}", 
                    stationId, observation.getWh(), observation.getWd(), observation.getWs());

            return EnvironmentSummaryResponse.Wave.builder()
                    .significantWaveHeight(observation.getWh())
                    .windDirectionDeg(observation.getWd())
                    .windSpeedMs(observation.getWs())
                    .build();
        } catch (Exception e) {
            log.error("파고 데이터 조회 실패: stationId={}", stationId, e);
            return EnvironmentSummaryResponse.Wave.builder().build();
        }
    }

    /**
     * 조위 데이터 조회
     */
    private EnvironmentSummaryResponse.Tide fetchTideData(MarineStation station) {
        if (station == null) {
            return EnvironmentSummaryResponse.Tide.builder().build();
        }

        try {
            KhoaTideResponse.Item observation = khoaTideClient
                    .fetchLatestTideLevel(station.getExternalStationId())
                    .block();

            if (observation == null) {
                return EnvironmentSummaryResponse.Tide.builder().build();
            }

            // 관측시각 파싱 (yyyyMMddHHmm 형식)
            ZonedDateTime observedAt = null;
            if (observation.getObsDt() != null && observation.getObsDt().length() >= 12) {
                try {
                    String dtStr = observation.getObsDt();
                    LocalDate date = LocalDate.parse(dtStr.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
                    LocalTime time = LocalTime.parse(dtStr.substring(8, 12), DateTimeFormatter.ofPattern("HHmm"));
                    observedAt = ZonedDateTime.of(date, time, ZoneId.of("Asia/Seoul"));
                } catch (Exception e) {
                    log.warn("조위 관측시각 파싱 실패: {}", observation.getObsDt(), e);
                }
            }

            return EnvironmentSummaryResponse.Tide.builder()
                    .tideLevelCm(observation.getTideLevel())
                    .tideObservedAt(observedAt)
                    .build();
        } catch (Exception e) {
            log.error("조위 데이터 조회 실패: stationId={}", station.getExternalStationId(), e);
            return EnvironmentSummaryResponse.Tide.builder().build();
        }
    }

    /**
     * 관측소 정보 빌드
     */
    private EnvironmentSummaryResponse.StationInfo buildStationInfo(MarineStation station, double lat, double lon) {
        if (station == null) {
            return null;
        }

        // NIFS RISA 관측소의 경우 좌표가 없으면 (0.0, 0.0)이므로, KHOA 조위관측소 좌표를 참고하여 거리 계산
        double stationLat = station.getLat();
        double stationLon = station.getLon();
        
        if (station.getExternalSource() == StationSource.NIFS_RISA && 
            (stationLat == 0.0 || stationLon == 0.0)) {
            // 가장 가까운 KHOA 조위관측소 좌표를 참고하여 거리 계산
            List<MarineStation> khoaStations = marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.KHOA_SURVEY_TIDE);
            List<MarineStation> khoaWithLocation = khoaStations.stream()
                    .filter(s -> s.getLat() != null && s.getLon() != null)
                    .filter(s -> s.getLat() != 0.0 && s.getLon() != 0.0)
                    .toList();

            if (!khoaWithLocation.isEmpty()) {
                MarineStation nearestKhoa = khoaWithLocation.stream()
                        .min(Comparator.comparingDouble(s ->
                                DistanceCalculator.calculateDistance(lat, lon, s.getLat(), s.getLon())))
                        .orElse(null);

                if (nearestKhoa != null) {
                    stationLat = nearestKhoa.getLat();
                    stationLon = nearestKhoa.getLon();
                }
            }
        }

        double distance = DistanceCalculator.calculateDistance(lat, lon, stationLat, stationLon);

        return EnvironmentSummaryResponse.StationInfo.builder()
                .id(station.getExternalStationId())
                .name(station.getName())
                .distanceKm(Math.round(distance * 10.0) / 10.0) // 소수점 첫째자리까지
                .build();
    }
}

