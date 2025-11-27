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

import com.ocean.piuda.environment.util.DistanceCalculator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 해양 관측소 데이터 초기화
 * 애플리케이션 시작 시 외부 API에서 관측소 목록을 가져와 데이터베이스에 저장
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
            // NIFS RISA 관측소 데이터 초기화 (CSV 파일에서 읽기)
            initializeNifsRisaStationsFromCsv();

            // KMA 관측소 데이터 초기화
            initializeKmaStations();

            // KHOA 조위관측소 데이터 초기화 (CSV 파일에서 읽기)
            initializeKhoaStationsFromCsv();

        } catch (Exception e) {
            log.error("해양 관측소 데이터 초기화 실패", e);
        }
    }

    /**
     * NIFS RISA 관측소 데이터 초기화
     * 1. CSV 파일(관측소 정보.csv)에서 모든 관측소 좌표 정보 읽기
     * 2. NIFS RISA API에서 관측소 목록 조회 (이름 매칭용)
     * 3. DB에 저장: CSV의 모든 관측소를 저장 (API에서 제공하지 않더라도 좌표 기반 매칭을 위해)
     */
    private void initializeNifsRisaStationsFromCsv() {
        // 기존 NIFS RISA 관측소 데이터 삭제 (좌표 업데이트를 위해)
        marineStationRepository.deleteByExternalSource(StationSource.NIFS_RISA);
        log.info("기존 NIFS RISA 관측소 데이터 삭제 완료.");

        try {
            // 1. CSV 파일에서 모든 NIFS 관측소 좌표 정보 읽기
            log.info("CSV 파일에서 NIFS 관측소 좌표 정보 읽기 중...");
            Map<String, NifsStationCsvRecord> csvStations = loadNifsStationsFromCsv();
            log.info("CSV에서 {}개의 NIFS 관측소 좌표 정보 발견", csvStations.size());

            // 2. NIFS RISA API에서 관측소 목록 조회 (이름 매칭용, 선택적)
            Map<String, String> apiStationNames = new java.util.HashMap<>();
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

            // 3. CSV의 모든 관측소를 DB에 저장
            int savedCount = 0;
            int apiMatchedCount = 0;

            for (NifsStationCsvRecord csvRecord : csvStations.values()) {
                String stationId = csvRecord.stationId;
                String stationName = csvRecord.stationName;
                Double lat = csvRecord.lat;
                Double lon = csvRecord.lon;

                // 이미 존재하는지 확인
                if (marineStationRepository
                        .findByExternalSourceAndExternalStationId(StationSource.NIFS_RISA, stationId)
                        .isPresent()) {
                    continue;
                }

                // API에서 이름이 있으면 사용, 없으면 CSV 이름 사용
                String finalName = apiStationNames.getOrDefault(stationId, stationName);
                if (apiStationNames.containsKey(stationId)) {
                    apiMatchedCount++;
                }

                // NIFS RISA 관측소 저장
                MarineStation station = MarineStation.builder()
                        .externalSource(StationSource.NIFS_RISA)
                        .externalStationId(stationId)
                        .name(finalName != null ? finalName : "관측소_" + stationId)
                        .lat(lat)
                        .lon(lon)
                        .isActive(true)
                        .build();

                try {
                    marineStationRepository.save(station);
                    savedCount++;
                    log.debug("NIFS 관측소 저장: ID={}, name={}, lat={}, lon={}", 
                            stationId, finalName, lat, lon);
                } catch (Exception e) {
                    log.error("NIFS 관측소 저장 실패: ID={}, 이름={}", stationId, finalName, e);
                }
            }

            log.info("NIFS RISA 관측소 데이터 저장 완료: 총 {}개 저장 (CSV 기준), {}개 API 이름 매칭", savedCount, apiMatchedCount);

        } catch (Exception e) {
            log.error("NIFS RISA 관측소 데이터 초기화 실패", e);
        }
    }

    /**
     * NIFS 관측소 CSV 파일에서 좌표 정보 읽기
     * CSV 형식: 해역코드, 해역, 관측소, 설치일자, 종료일자, 위도(°N), 경도(°E), ...
     */
    private Map<String, NifsStationCsvRecord> loadNifsStationsFromCsv() {
        Map<String, NifsStationCsvRecord> records = new java.util.HashMap<>();

        try {
            ClassPathResource resource = new ClassPathResource("nifs-stations.csv");

            // CSV 파일 읽기 (인코딩: UTF-8 시도 후 EUC-KR)
            List<Charset> charsets = List.of(StandardCharsets.UTF_8, Charset.forName("EUC-KR"));

            for (Charset charset : charsets) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), charset))) {
                    String line;
                    int lineNumber = 0;

                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        
                        // 헤더 라인 건너뛰기 (첫 2줄)
                        if (lineNumber <= 2) {
                            continue;
                        }
                        
                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        String[] parts = parseCsvLine(line);
                        // CSV 형식: 해역코드, 해역, 관측소, 설치일자, 종료일자, 위도(°N), 경도(°E), ...
                        if (parts.length < 7) {
                            log.warn("CSV 라인 컬럼 수 부족: parts.length={}, line={}", parts.length, line);
                            continue;
                        }

                        try {
                            String stationId = parts[0].trim(); // 해역코드 (sta_cde와 매칭)
                            String region = parts[1].trim(); // 해역
                            String stationName = parts[2].trim(); // 관측소 이름
                            String installDate = parts[3].trim(); // 설치일자
                            String endDate = parts[4].trim(); // 종료일자
                            Double lat = Double.parseDouble(parts[5].trim()); // 위도
                            Double lon = Double.parseDouble(parts[6].trim()); // 경도

                            if (stationId.isEmpty() || lat == null || lon == null) {
                                continue;
                            }

                            // 종료일자가 있으면 제외 (운영 중인 관측소만)
                            if (!endDate.isEmpty()) {
                                continue;
                            }

                            records.put(stationId, new NifsStationCsvRecord(stationId, stationName, lat, lon));
                        } catch (NumberFormatException e) {
                            log.warn("CSV 라인 숫자 파싱 실패: line={}, error={}", line, e.getMessage());
                            continue;
                        }
                    }
                    
                    if (!records.isEmpty()) {
                        log.info("CSV 파일 읽기 성공 (인코딩: {}): {}개 관측소", charset.name(), records.size());
                        return records;
                    }
                } catch (Exception e) {
                    log.warn("CSV 파일 읽기 실패 (인코딩: {}): {}", charset.name(), e.getMessage());
                    records.clear();
                }
            }
        } catch (Exception e) {
            log.error("CSV 파일 읽기 실패", e);
        }

        return records;
    }

    /**
     * NIFS 관측소 CSV 레코드
     */
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

    /**
     * CSV 파일에서 관측소 위치 정보 읽기
     */
    private List<CsvStationRecord> loadCsvStations() {
        List<CsvStationRecord> records = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource("nifs-tide-stations.csv");

            // CSV 파일 읽기 (인코딩: UTF-8 시도 후 EUC-KR)
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] parts = parseCsvLine(line);
                    if (parts.length < 5) {
                        continue;
                    }

                    try {
                        // CSV 형식: 관측소ID, 관측소 유형, 조위관측소 명(한글명), 조위관측소 위도, 조위관측소 경도, 조위관측소 영문명
                        if (parts.length < 5) {
                            log.warn("CSV 라인 컬럼 수 부족: parts.length={}, line={}", parts.length, line);
                            continue;
                        }
                        String stationId = parts[0].trim();
                        String stationType = parts[1].trim(); // 관측소 유형 (사용 안 함)
                        String nameKor = parts[2].trim(); // 조위관측소 명
                        Double lat = Double.parseDouble(parts[3].trim()); // 조위관측소 위도
                        Double lon = Double.parseDouble(parts[4].trim()); // 조위관측소 경도
                        String nameEng = parts.length > 5 ? parts[5].trim() : "";

                        if (stationId.isEmpty() || lat == null || lon == null) {
                            continue;
                        }

                        records.add(new CsvStationRecord(stationId, nameKor, nameEng, lat, lon));
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                // UTF-8 실패 시 EUC-KR로 재시도
                log.warn("UTF-8 인코딩 실패, EUC-KR로 재시도: {}", e.getMessage());
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), "EUC-KR"))) {
                    String line;
                    boolean isFirstLine = true;

                    while ((line = reader.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }
                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        String[] parts = parseCsvLine(line);
                        if (parts.length < 5) {
                            continue;
                        }

                        try {
                            // CSV 형식: 관측소ID, 관측소 유형, 조위관측소 명(한글명), 조위관측소 위도, 조위관측소 경도, 조위관측소 영문명
                            if (parts.length < 5) {
                                log.warn("CSV 라인 컬럼 수 부족: parts.length={}, line={}", parts.length, line);
                                continue;
                            }
                            String stationId = parts[0].trim();
                            String stationType = parts[1].trim(); // 관측소 유형 (사용 안 함)
                            String nameKor = parts[2].trim(); // 조위관측소 명
                            Double lat = Double.parseDouble(parts[3].trim()); // 조위관측소 위도
                            Double lon = Double.parseDouble(parts[4].trim()); // 조위관측소 경도
                            String nameEng = parts.length > 5 ? parts[5].trim() : "";

                            if (stationId.isEmpty() || lat == null || lon == null) {
                                continue;
                            }

                            records.add(new CsvStationRecord(stationId, nameKor, nameEng, lat, lon));
                        } catch (NumberFormatException ex) {
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("CSV 파일 읽기 실패", e);
        }

        return records;
    }

    /**
     * CSV에서 매칭하는 관측소 찾기 (이름 매칭 우선, 실패 시 좌표 기반)
     */
    private Optional<CsvStationRecord> findMatchingCsvStation(String apiStationName, List<CsvStationRecord> csvRecords) {
        if (apiStationName == null || apiStationName.isEmpty()) {
            return Optional.empty();
        }

        // 1. 이름 정확 매칭 시도
        Optional<CsvStationRecord> exactMatch = csvRecords.stream()
                .filter(csv -> csv.nameKor != null && csv.nameKor.equals(apiStationName))
                .findFirst();

        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        // 2. 이름 부분 매칭 시도 (공백 제거 후 비교)
        String normalizedApiName = apiStationName.replaceAll("\\s+", "");
        Optional<CsvStationRecord> partialMatch = csvRecords.stream()
                .filter(csv -> csv.nameKor != null) 
                .filter(csv -> {
                    String normalizedCsvName = csv.nameKor.replaceAll("\\s+", "");
                    return normalizedCsvName.contains(normalizedApiName) || 
                           normalizedApiName.contains(normalizedCsvName);
                })
                .findFirst();

        if (partialMatch.isPresent()) {
            return partialMatch;
        }

        // 3. 이름 키워드 매칭 시도 (주요 지역명 추출)
        String[] keywords = extractKeywords(apiStationName);
        if (keywords.length > 0) {
            Optional<CsvStationRecord> keywordMatch = csvRecords.stream()
                    .filter(csv -> csv.nameKor != null)
                    .filter(csv -> {
                        for (String keyword : keywords) {
                            if (csv.nameKor.contains(keyword)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .findFirst();

            if (keywordMatch.isPresent()) {
                return keywordMatch;
            }
        }

        return Optional.empty();
    }

    /**
     * 관측소 이름에서 주요 키워드 추출
     */
    private String[] extractKeywords(String name) {
        // 주요 지역명 키워드 (예: 포항, 부산, 제주 등)
        String[] commonKeywords = {"포항", "부산", "제주", "울산", "목포", "여수", "거제", "통영", 
                                   "완도", "진도", "안산", "인천", "평택", "영광", "무안", "강화",
                                   "울진", "동해", "속초", "묵호", "거문도", "마라도", "추자도",
                                   "서귀포", "완도", "진도", "안흥", "대산", "태안", "영덕"};
        
        List<String> found = new ArrayList<>();
        for (String keyword : commonKeywords) {
            if (name.contains(keyword)) {
                found.add(keyword);
            }
        }
        return found.toArray(new String[0]);
    }

    /**
     * 좌표 기반으로 가장 가까운 CSV 관측소 찾기
     * NIFS RISA API 관측소 이름에서 지역 키워드를 추출하여, 해당 지역의 CSV 관측소를 찾습니다.
     * 키워드 매칭이 실패하면 모든 CSV 관측소 중에서 첫 번째를 반환 (임시)
     */
    private Optional<CsvStationRecord> findNearestCsvByCoordinates(String apiStationName, List<CsvStationRecord> csvRecords) {
        if (apiStationName == null || apiStationName.isEmpty() || csvRecords.isEmpty()) {
            return Optional.empty();
        }

        // 지역 키워드 추출
        String[] keywords = extractKeywords(apiStationName);
        if (keywords.length > 0) {
            // 키워드가 포함된 CSV 관측소 찾기
            List<CsvStationRecord> candidateRecords = csvRecords.stream()
                    .filter(csv -> csv.nameKor != null)
                    .filter(csv -> {
                        for (String keyword : keywords) {
                            if (csv.nameKor.contains(keyword)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .toList();

            if (!candidateRecords.isEmpty()) {
                return Optional.of(candidateRecords.get(0));
            }
        }

        // 키워드 매칭 실패 시, 모든 CSV 관측소 중 첫 번째 반환 (임시)
        // 실제로는 더 정교한 매칭 로직이 필요하지만, 일단 모든 관측소에 좌표를 할당
        log.warn("키워드 매칭 실패, 첫 번째 CSV 관측소 사용: apiStationName={}", apiStationName);
        return csvRecords.stream().findFirst();
    }

    /**
     * NIFS RISA 관측소와 CSV 관측소 수동 매핑
     * API 관측소 이름과 CSV 관측소 이름이 정확히 일치하지 않는 경우를 위한 수동 매핑
     * 
     * 주의: CSV 파일의 관측소 ID는 KHOA 조위관측소 ID이므로, 
     * NIFS RISA 관측소와는 다른 시스템입니다.
     * 따라서 이름 기반 매칭만 사용합니다.
     */
    private Optional<CsvStationRecord> findManualMapping(String apiStationId, String apiStationName, List<CsvStationRecord> csvRecords) {
        // 현재는 수동 매핑 테이블 없음 (이름 기반 매칭만 사용)
        // 필요시 여기에 특정 관측소에 대한 수동 매핑 추가 가능
        return Optional.empty();
    }

    /**
     * CSV 라인 파싱 (쉼표로 구분, 인용부호 처리)
     */
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

    /**
     * CSV 레코드 임시 클래스
     */
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

    /**
     * KMA 관측소 데이터 초기화
     */
    private void initializeKmaStations() {
        if (marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.KMA_SEA_OBS).size() > 0) {
            log.info("KMA 관측소 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        try {
            // KMA API에서 전체 관측소 목록 조회 (stn=0)
            List<KmaSeaObsResponse.Item> kmaItems = kmaSeaObsClient
                    .fetchObservations("0", LocalDateTime.now())
                    .block();

            if (kmaItems == null || kmaItems.isEmpty()) {
                log.warn("KMA 관측소 데이터를 가져올 수 없습니다.");
                return;
            }

            log.info("KMA 관측소 {}개 발견", kmaItems.size());

            // 관측소 ID별로 그룹화 (같은 관측소의 중복 데이터 제거)
            Map<String, KmaSeaObsResponse.Item> uniqueStations = kmaItems.stream()
                    .filter(item -> item.getStn() != null && item.getLat() != null && item.getLon() != null)
                    .collect(Collectors.toMap(
                            KmaSeaObsResponse.Item::getStn,
                            item -> item,
                            (existing, replacement) -> existing // 중복 시 기존 것 유지
                    ));

            int savedCount = 0;
            for (KmaSeaObsResponse.Item item : uniqueStations.values()) {
                // 이미 존재하는지 확인
                if (marineStationRepository
                        .findByExternalSourceAndExternalStationId(StationSource.KMA_SEA_OBS, item.getStn())
                        .isEmpty()) {
                    MarineStation station = MarineStation.builder()
                            .externalSource(StationSource.KMA_SEA_OBS)
                            .externalStationId(item.getStn())
                            .name(item.getStnNm() != null ? item.getStnNm() : "KMA_" + item.getStn())
                            .lat(item.getLat())
                            .lon(item.getLon())
                            .isActive(true)
                            .build();
                    marineStationRepository.save(station);
                    savedCount++;
                }
            }

            log.info("{}개의 KMA 관측소 데이터 저장 완료", savedCount);

        } catch (Exception e) {
            log.error("KMA 관측소 데이터 초기화 실패", e);
        }
    }

    /**
     * KHOA 조위관측소 데이터 초기화 (CSV 파일에서 읽기)
     * CSV 파일의 조위관측소 데이터를 DB에 저장
     */
    private void initializeKhoaStationsFromCsv() {
        // 이미 데이터가 있으면 건너뛰기
        if (marineStationRepository.findByExternalSourceAndIsActiveTrue(StationSource.KHOA_SURVEY_TIDE).size() > 0) {
            log.info("KHOA 조위관측소 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        try {
            // CSV 파일에서 조위관측소 위치 정보 읽기
            log.info("CSV 파일에서 KHOA 조위관측소 위치 정보 읽기 중...");
            List<CsvStationRecord> csvRecords = loadCsvStations();
            log.info("CSV에서 {}개의 조위관측소 위치 정보 발견", csvRecords.size());

            int savedCount = 0;
            for (CsvStationRecord csvRecord : csvRecords) {
                // 이미 존재하는지 확인 (stationId 기준)
                if (marineStationRepository
                        .findByExternalSourceAndExternalStationId(StationSource.KHOA_SURVEY_TIDE, csvRecord.stationId)
                        .isPresent()) {
                    continue;
                }

                // 유효한 좌표인지 확인
                if (csvRecord.lat == null || csvRecord.lon == null || 
                    csvRecord.lat == 0.0 || csvRecord.lon == 0.0) {
                    log.warn("유효하지 않은 좌표: stationId={}, lat={}, lon={}", 
                            csvRecord.stationId, csvRecord.lat, csvRecord.lon);
                    continue;
                }

                MarineStation station = MarineStation.builder()
                        .externalSource(StationSource.KHOA_SURVEY_TIDE)
                        .externalStationId(csvRecord.stationId) // CSV의 stationId 사용 (예: DT_0009)
                        .name(csvRecord.nameKor != null ? csvRecord.nameKor : csvRecord.nameEng)
                        .lat(csvRecord.lat)
                        .lon(csvRecord.lon)
                        .isActive(true)
                        .build();

                try {
                    marineStationRepository.save(station);
                    savedCount++;
                } catch (Exception e) {
                    log.error("KHOA 관측소 저장 실패: ID={}, 이름={}", csvRecord.stationId, csvRecord.nameKor, e);
                }
            }

            log.info("KHOA 조위관측소 데이터 저장 완료: 총 {}개 저장", savedCount);

        } catch (Exception e) {
            log.error("KHOA 조위관측소 데이터 초기화 실패", e);
        }
    }
}

