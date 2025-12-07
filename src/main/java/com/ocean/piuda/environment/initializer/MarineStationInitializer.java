package com.ocean.piuda.environment.initializer;

import com.ocean.piuda.environment.client.KmaSeaObsClient;
import com.ocean.piuda.environment.client.NifsRisaClient;
import com.ocean.piuda.environment.client.dto.KmaSeaObsResponse;
import com.ocean.piuda.environment.client.dto.NifsRisaResponse;
import com.ocean.piuda.environment.domain.MarineStation;
import com.ocean.piuda.environment.domain.StationSource;
import com.ocean.piuda.environment.repository.MarineStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 해양 관측소 데이터 초기화
 * 애플리케이션 시작 시 외부 API 및 CSV에서 관측소 목록을 가져와 데이터베이스에 저장
 *
 * [주요 변경 사항]
 * 1. 성능 최적화 (Batch Processing):
 * - 기존: for문 내에서 매번 DB 조회(Select) 및 저장(Insert) -> N+1 문제 발생
 * - 변경: 전체 ID 메모리 로드 -> 중복 검사 -> saveAll()로 일괄 저장
 * 2. 예외 처리 강화:
 * - CSV 파일이 없거나 API 호출 실패 시, 전체 로직이 중단되지 않고 해당 단계만 건너뛰도록 처리
 * 3. CSV 인코딩 처리:
 * - UTF-8 -> EUC-KR 순으로 시도 (한글 깨짐 방지)
 * 4. KHOA 파일명 및 구조 변경:
 * - 파일명: khoa-stations.csv
 * - 컬럼: [0]ID, [1]이름, [2]위도, [3]경도
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // 다른 초기화 이후 실행
public class MarineStationInitializer implements CommandLineRunner {

    private final NifsRisaClient nifsRisaClient;
    private final KmaSeaObsClient kmaSeaObsClient;
    private final MarineStationRepository marineStationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("해양 관측소 데이터 초기화 시작...");

        try {
            // 1. NIFS RISA 관측소 데이터 초기화 (CSV 파일: nifs-stations.csv)
            initializeNifsRisaStationsFromCsv();

            // 2. KMA 관측소 데이터 초기화 (API 호출)
            initializeKmaStations();

            // 3. KHOA 조위관측소 데이터 초기화 (CSV 파일: khoa-stations.csv)
            initializeKhoaStationsFromCsv();

        } catch (Exception e) {
            log.error("해양 관측소 데이터 초기화 중 전체 오류 발생", e);
        }
    }

    /**
     * NIFS RISA 관측소 데이터 초기화 (Batch 최적화 적용)
     */
    private void initializeNifsRisaStationsFromCsv() {
        try {
            // 1. CSV 파일 읽기
            log.info("CSV 파일에서 NIFS 관측소 좌표 정보 읽기 중...");
            Map<String, NifsStationCsvRecord> csvStations = loadNifsStationsFromCsv();

            if (csvStations.isEmpty()) {
                log.info("NIFS 관측소 CSV 데이터가 없거나 파일을 찾을 수 없어 초기화를 건너뜁니다.");
                return;
            }
            log.info("CSV에서 {}개의 NIFS 관측소 좌표 정보 발견", csvStations.size());

            // 2. API 관측소 이름 매칭 (선택적)
            Map<String, String> apiStationNames = new HashMap<>();
            try {
                log.info("NIFS RISA API에서 관측소 목록 조회 중...");
                List<NifsRisaResponse.NifsRisaItem> apiStations = nifsRisaClient.fetchStations().block();

                if (apiStations != null && !apiStations.isEmpty()) {
                    apiStations.stream()
                            .filter(item -> item.getStaCde() != null && !item.getStaCde().isEmpty())
                            .forEach(item -> apiStationNames.put(item.getStaCde(), item.getStaNamKor()));
                    log.info("NIFS RISA API에서 {}개의 관측소 이름 정보 발견", apiStationNames.size());
                }
            } catch (Exception e) {
                log.warn("NIFS RISA API에서 관측소 목록을 가져올 수 없습니다. CSV 데이터만 사용합니다: {}", e.getMessage());
            }

            // 3. DB 중복 체크 (Batch) - 쿼리 1회 실행
            Set<String> existingIds = marineStationRepository
                    .findByExternalSourceAndIsActiveTrue(StationSource.NIFS_RISA)
                    .stream()
                    .map(MarineStation::getExternalStationId)
                    .collect(Collectors.toSet());

            // 4. 저장할 엔티티 생성
            List<MarineStation> stationsToSave = new ArrayList<>();
            int apiMatchedCount = 0;

            for (NifsStationCsvRecord csvRecord : csvStations.values()) {
                String stationId = csvRecord.stationId;

                // 이미 존재하면 건너뜀
                if (existingIds.contains(stationId)) {
                    continue;
                }

                String stationName = csvRecord.stationName;
                Double lat = csvRecord.lat;
                Double lon = csvRecord.lon;

                // API 이름 매칭
                String finalName = apiStationNames.getOrDefault(stationId, stationName);
                if (apiStationNames.containsKey(stationId)) {
                    apiMatchedCount++;
                }

                MarineStation station = MarineStation.builder()
                        .externalSource(StationSource.NIFS_RISA)
                        .externalStationId(stationId)
                        .name(finalName != null ? finalName : "관측소_" + stationId)
                        .lat(lat)
                        .lon(lon)
                        .isActive(true)
                        .build();

                stationsToSave.add(station);
            }

            // 5. 일괄 저장
            if (!stationsToSave.isEmpty()) {
                marineStationRepository.saveAll(stationsToSave);
                log.info("NIFS RISA 관측소 데이터 신규 저장 완료: {}개 (API 이름 매칭: {}개)",
                        stationsToSave.size(), apiMatchedCount);
            } else {
                log.info("NIFS RISA 관측소 데이터가 모두 최신입니다.");
            }

        } catch (Exception e) {
            log.error("NIFS RISA 관측소 데이터 초기화 실패", e);
        }
    }

    /**
     * KMA 관측소 데이터 초기화 (Batch 최적화 적용)
     */
    private void initializeKmaStations() {
        try {
            // 1. DB 중복 체크
            Set<String> existingIds = marineStationRepository
                    .findByExternalSourceAndIsActiveTrue(StationSource.KMA_SEA_OBS)
                    .stream()
                    .map(MarineStation::getExternalStationId)
                    .collect(Collectors.toSet());

            if (!existingIds.isEmpty()) {
                log.info("KMA 관측소 데이터가 이미 {}개 존재합니다.", existingIds.size());
            }

            // 2. API 호출
            List<KmaSeaObsResponse.Item> kmaItems = kmaSeaObsClient
                    .fetchObservations("0", LocalDateTime.now())
                    .block();

            if (kmaItems == null || kmaItems.isEmpty()) {
                log.warn("KMA 관측소 데이터를 가져올 수 없습니다.");
                return;
            }

            log.info("KMA API에서 관측소 {}개 발견", kmaItems.size());

            // 3. 중복 제거 및 엔티티 변환
            Map<String, KmaSeaObsResponse.Item> uniqueStations = kmaItems.stream()
                    .filter(item -> item.getStn() != null && item.getLat() != null && item.getLon() != null)
                    .collect(Collectors.toMap(
                            KmaSeaObsResponse.Item::getStn,
                            item -> item,
                            (existing, replacement) -> existing
                    ));

            List<MarineStation> stationsToSave = new ArrayList<>();

            for (KmaSeaObsResponse.Item item : uniqueStations.values()) {
                if (existingIds.contains(item.getStn())) {
                    continue;
                }

                MarineStation station = MarineStation.builder()
                        .externalSource(StationSource.KMA_SEA_OBS)
                        .externalStationId(item.getStn())
                        .name(item.getStnNm() != null ? item.getStnNm() : "KMA_" + item.getStn())
                        .lat(item.getLat())
                        .lon(item.getLon())
                        .isActive(true)
                        .build();
                stationsToSave.add(station);
            }

            // 4. 일괄 저장
            if (!stationsToSave.isEmpty()) {
                marineStationRepository.saveAll(stationsToSave);
                log.info("{}개의 KMA 신규 관측소 데이터 저장 완료", stationsToSave.size());
            } else {
                log.info("KMA 관측소 데이터가 모두 최신입니다.");
            }

        } catch (Exception e) {
            log.error("KMA 관측소 데이터 초기화 실패", e);
        }
    }

    /**
     * KHOA 조위관측소 데이터 초기화 (Batch 최적화 적용)
     */
    private void initializeKhoaStationsFromCsv() {
        try {
            // 1. DB 중복 체크
            Set<String> existingIds = marineStationRepository
                    .findByExternalSourceAndIsActiveTrue(StationSource.KHOA_SURVEY_TIDE)
                    .stream()
                    .map(MarineStation::getExternalStationId)
                    .collect(Collectors.toSet());

            if (!existingIds.isEmpty()) {
                log.info("KHOA 조위관측소 데이터가 이미 {}개 존재합니다.", existingIds.size());
            }

            // 2. CSV 읽기
            log.info("CSV 파일에서 KHOA 조위관측소 위치 정보 읽기 중...");
            List<CsvStationRecord> csvRecords = loadCsvStations();

            if (csvRecords.isEmpty()) {
                log.info("KHOA 조위관측소 CSV 데이터가 없거나 파일을 찾을 수 없어 초기화를 건너뜁니다.");
                return;
            }
            log.info("CSV에서 {}개의 조위관측소 위치 정보 발견", csvRecords.size());

            // 3. 엔티티 생성
            List<MarineStation> stationsToSave = new ArrayList<>();

            for (CsvStationRecord csvRecord : csvRecords) {
                if (existingIds.contains(csvRecord.stationId)) {
                    continue;
                }

                if (csvRecord.lat == null || csvRecord.lon == null ||
                        csvRecord.lat == 0.0 || csvRecord.lon == 0.0) {
                    log.warn("유효하지 않은 좌표: stationId={}, lat={}, lon={}",
                            csvRecord.stationId, csvRecord.lat, csvRecord.lon);
                    continue;
                }

                MarineStation station = MarineStation.builder()
                        .externalSource(StationSource.KHOA_SURVEY_TIDE)
                        .externalStationId(csvRecord.stationId)
                        .name(csvRecord.nameKor != null ? csvRecord.nameKor : csvRecord.nameEng)
                        .lat(csvRecord.lat)
                        .lon(csvRecord.lon)
                        .isActive(true)
                        .build();

                stationsToSave.add(station);
            }

            // 4. 일괄 저장
            if (!stationsToSave.isEmpty()) {
                marineStationRepository.saveAll(stationsToSave);
                log.info("KHOA 조위관측소 데이터 신규 저장 완료: 총 {}개", stationsToSave.size());
            } else {
                log.info("KHOA 조위관측소 데이터가 모두 최신입니다.");
            }

        } catch (Exception e) {
            log.error("KHOA 조위관측소 데이터 초기화 실패", e);
        }
    }

    // --- CSV Parsing Helper Methods ---

    /**
     * NIFS (수산과학원) CSV 파일 로딩
     * 파일명: nifs-stations.csv
     * 인코딩 순서: UTF-8 -> EUC-KR
     */
    private Map<String, NifsStationCsvRecord> loadNifsStationsFromCsv() {
        Map<String, NifsStationCsvRecord> records = new HashMap<>();
        try {
            ClassPathResource resource = new ClassPathResource("nifs-stations.csv");
            if (!resource.exists()) {
                log.warn("nifs-stations.csv 파일을 찾을 수 없습니다. NIFS 관측소 좌표 초기화를 건너뜁니다.");
                return records;
            }

            // UTF-8 우선 시도
            List<Charset> charsets = List.of(StandardCharsets.UTF_8, Charset.forName("EUC-KR"));

            for (Charset charset : charsets) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), charset))) {

                    String line;
                    int lineNumber = 0;
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        if (lineNumber <= 2 || line.trim().isEmpty()) continue; // 헤더 스킵

                        String[] parts = parseCsvLine(line);
                        if (parts.length < 7) continue;

                        try {
                            String stationId = parts[0].trim();
                            String stationName = parts[2].trim();
                            String endDate = parts[4].trim();
                            Double lat = Double.parseDouble(parts[5].trim());
                            Double lon = Double.parseDouble(parts[6].trim());

                            if (stationId.isEmpty() || lat == null || lon == null || !endDate.isEmpty()) {
                                continue;
                            }
                            records.put(stationId, new NifsStationCsvRecord(stationId, stationName, lat, lon));
                        } catch (NumberFormatException ignored) {}
                    }

                    if (!records.isEmpty()) {
                        log.info("NIFS CSV 파일 읽기 성공 (인코딩: {}): {}개 관측소", charset.name(), records.size());
                        return records;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.error("NIFS CSV 파일 읽기 중 오류", e);
        }
        return records;
    }

    /**
     * KHOA (조위관측소) CSV 파일 로딩
     * 파일명: khoa-stations.csv (변경됨!)
     * 포맷: [0]ID, [1]이름, [2]위도, [3]경도
     * 인코딩 순서: UTF-8 -> EUC-KR
     */
    private List<CsvStationRecord> loadCsvStations() {
        List<CsvStationRecord> records = new ArrayList<>();
        try {
            // [파일명 변경] khoa-stations.csv
            ClassPathResource resource = new ClassPathResource("khoa-stations.csv");
            if (!resource.exists()) {
                log.warn("khoa-stations.csv 파일을 찾을 수 없습니다. KHOA 관측소 초기화를 건너뜁니다.");
                return records;
            }

            // UTF-8 우선 시도
            List<Charset> charsets = List.of(StandardCharsets.UTF_8, Charset.forName("EUC-KR"));

            for (Charset charset : charsets) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), charset))) {

                    String line;
                    int lineNumber = 0;
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        // 헤더 스킵 (첫 번째 줄)
                        if (lineNumber == 1) continue;
                        if (line.trim().isEmpty()) continue;

                        String[] parts = parseCsvLine(line);

                        // [수정] 4개 컬럼만 있으면 됨
                        if (parts.length < 4) continue;

                        try {
                            // [0]: ID
                            String stationId = parts[0].trim();
                            // [1]: 이름
                            String nameKor = parts[1].trim();
                            // [2]: 위도
                            Double lat = Double.parseDouble(parts[2].trim());
                            // [3]: 경도
                            Double lon = Double.parseDouble(parts[3].trim());

                            // 영문명 없음
                            String nameEng = "";

                            if (stationId.isEmpty() || lat == null || lon == null) continue;

                            records.add(new CsvStationRecord(stationId, nameKor, nameEng, lat, lon));
                        } catch (NumberFormatException ignored) {}
                    }

                    if (!records.isEmpty()) {
                        log.info("KHOA CSV 파일 읽기 성공 (인코딩: {}): {}개 관측소", charset.name(), records.size());
                        return records;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.error("KHOA CSV 파일 읽기 중 오류", e);
        }
        return records;
    }

    private String[] parseCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }

    private static class NifsStationCsvRecord {
        final String stationId;
        final String stationName;
        final Double lat;
        final Double lon;
        NifsStationCsvRecord(String stationId, String stationName, Double lat, Double lon) {
            this.stationId = stationId;
            this.stationName = stationName;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private static class CsvStationRecord {
        final String stationId;
        final String nameKor;
        final String nameEng;
        final Double lat;
        final Double lon;
        CsvStationRecord(String stationId, String nameKor, String nameEng, Double lat, Double lon) {
            this.stationId = stationId;
            this.nameKor = nameKor;
            this.nameEng = nameEng;
            this.lat = lat;
            this.lon = lon;
        }
    }
}