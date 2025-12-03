# 해양 환경 요약 API 모듈

OC Community(OC Underwater) 앱에서 사용하는 해양 환경 요약 정보를 제공하는 REST API 모듈입니다.

## 📋 목차

- [개요](#개요)
- [기능](#기능)
- [API 엔드포인트](#api-엔드포인트)
- [외부 API 연동](#외부-api-연동)
- [데이터베이스 스키마](#데이터베이스-스키마)
- [설정](#설정)
- [사용 방법](#사용-방법)
- [테스트](#테스트)

---

## 개요

이 모듈은 다음 3개의 외부 해양 관측 API를 통합하여 실시간 해양 환경 정보를 제공합니다:

1. **NIFS RISA** - 국립수산과학원 실시간 해양수산환경 관측 (수온/염분/용존산소)
2. **KHOA** - 국립해양조사원 조위 관측
3. **KMA** - 기상청 해양기상종합관측 (파고/풍향/풍속)

사용자가 제공한 좌표 또는 다이빙 포인트 ID를 기반으로 가장 가까운 관측소를 자동으로 매칭하여 통합 환경 정보를 반환합니다.

---

## 기능

### 주요 기능

- ✅ 좌표 기반 환경 요약 조회
- ✅ 다이빙 포인트 ID 기반 환경 요약 조회
- ✅ Haversine 공식을 사용한 거리 기반 관측소 자동 매칭
- ✅ 3개 외부 API 통합 (NIFS, KHOA, KMA)
- ✅ 실시간 관측 데이터 조회
- ✅ 관측소 정보 및 거리 정보 포함

### 제공 정보

- **수온/염분/용존산소**: NIFS RISA 중층 데이터 우선 사용
- **파고/풍향/풍속**: KMA 해양기상 관측 데이터
- **조위**: KHOA 조위 관측 데이터
- **관측소 정보**: 각 소스별 가장 가까운 관측소 및 거리

---

## API 엔드포인트

### 환경 요약 조회

**엔드포인트**: `GET /api/environment/summary`

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `lat` | Double | 조건부 | 위도 (한국 해역: 약 33 ~ 38도) |
| `lon` | Double | 조건부 | 경도 (한국 해역: 약 124 ~ 132도) |
| `pointId` | Long | 조건부 | 다이빙 포인트 ID |

**참고**: `pointId`가 제공되면 `lat`/`lon`은 무시됩니다. `pointId`가 없으면 `lat`과 `lon`이 모두 필요합니다.

#### 응답 예시

```json
{
  "location": {
    "lat": 36.3500,
    "lon": 129.7833,
    "nearestStations": {
      "nifs": {
        "id": "fjdfc",
        "name": "평택 조위",
        "distanceKm": 250.6
      },
      "kma": {
        "id": "22106",
        "name": "포항",
        "distanceKm": 0.5
      },
      "khoa": {
        "id": "DT_0009",
        "name": "포항",
        "distanceKm": 2.1
      }
    }
  },
  "timestamp": "2025-11-30T12:00:00+09:00",
  "water": {
    "midLayerTemp": 18.3,
    "surfaceTemp": 17.9,
    "salinity": null,
    "dissolvedOxygen": null
  },
  "wave": {
    "significantWaveHeight": 3.4,
    "windDirectionDeg": 304,
    "windSpeedMs": 14.0
  },
  "tide": {
    "tideLevelCm": 87.0,
    "tideObservedAt": "2025-11-30T11:50:00+09:00"
  },
  "meta": {
    "rawSources": ["NIFS_RISA", "KMA_SEA_OBS", "KHOA_SURVEY_TIDE"],
    "note": "실시간 관측자료는 품질 검증 전 데이터일 수 있음"
  }
}
```

**참고**: 
- KMA 관측소는 **B 타입 (BUOY)**을 사용하면 풍향/풍속 데이터를 제공합니다.
- C 타입 (파고BUOY) 관측소는 풍향/풍속 데이터가 없을 수 있습니다.
- 예시 좌표는 포항 해역 (B 타입 관측소 22106 근처)입니다.

#### 사용 예시

**좌표 기반 조회 (포항 해역 - B 타입 관측소 사용):**
```bash
# 포항 해역 (B 타입 관측소 22106 근처)
curl -X GET "http://localhost:8080/api/environment/summary?lat=36.3500&lon=129.7833"
```

**포인트 ID 기반 조회:**
```bash
curl -X GET "http://localhost:8080/api/environment/summary?pointId=1"
```

---

## 외부 API 연동

### 1. NIFS RISA (국립수산과학원)

- **URL**: `https://www.nifs.go.kr/OpenAPI_json`
- **API Key**: `application.yml`의 `marine.nifs.key` 설정값 사용
- **제공 데이터**: 수온, 염분, 용존산소
- **수층 우선순위**: 중층(2) → 표층(1) → 저층(3)

### 2. KHOA 조위 관측 (국립해양조사원)

- **URL**: `https://apis.data.go.kr/1192136/surveyTideLevel`
- **API Key**: `application.yml`의 `marine.khoa.key-encoding` 설정값 사용 (URL Encoding)
- **제공 데이터**: 조위(cm), 관측시각

### 3. KMA 해양기상종합관측 (기상청)

- **URL**: `https://apihub.kma.go.kr/api/typ01/url/sea_obs.php`
- **API Key**: `application.yml`의 `marine.kma.key` 설정값 사용
- **제공 데이터**: 파고(WH), 풍향(WD), 풍속(WS), 해수면 온도(TW)
- **관측소 타입**:
  - **B 타입 (BUOY)**: 파고, 풍향, 풍속, 해수온 모두 제공 ✅
  - **C 타입 (파고BUOY)**: 파고, 해수온만 제공 (풍향/풍속 없음)
  - **권장**: 풍향/풍속 데이터가 필요한 경우 B 타입 관측소 사용

---

## 데이터베이스 스키마

### MarineStation (관측소)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT | PK, 자동증가 |
| `external_source` | VARCHAR(50) | 외부 API 소스 (NIFS_RISA, KMA_SEA_OBS, KHOA_SURVEY_TIDE) |
| `external_station_id` | VARCHAR(100) | 외부 API에서 사용하는 관측소 ID |
| `name` | VARCHAR(200) | 관측소 이름 |
| `lat` | DECIMAL(9,6) | 위도 |
| `lon` | DECIMAL(9,6) | 경도 |
| `is_active` | BOOLEAN | 활성화 여부 |
| `created_at` | TIMESTAMP | 생성 시각 |
| `modified_at` | TIMESTAMP | 수정 시각 |

**인덱스**:
- `idx_source_ext`: `(external_source, external_station_id)`
- `idx_lat_lon`: `(lat, lon)`

### MarineObservation (관측값)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT | PK, 자동증가 |
| `station_id` | BIGINT | FK, 관측소 ID |
| `observed_at` | TIMESTAMP WITH TIME ZONE | 관측 시각 |
| `layer` | VARCHAR(50) | 수층 정보 |
| `water_temp` | DECIMAL(5,2) | 수온 (℃) |
| `salinity` | DECIMAL(5,2) | 염분 (psu) |
| `dissolved_oxygen` | DECIMAL(5,2) | 용존산소 (mg/L) |
| `wave_height` | DECIMAL(5,2) | 파고 (m) |
| `wind_direction` | DECIMAL(5,2) | 풍향 (도) |
| `wind_speed` | DECIMAL(5,2) | 풍속 (m/s) |
| `tide_level` | DECIMAL(6,2) | 조위 (cm) |
| `raw_payload` | TEXT | 원본 API 응답 JSON |
| `created_at` | TIMESTAMP | 생성 시각 |
| `modified_at` | TIMESTAMP | 수정 시각 |

**인덱스**:
- `idx_station_observed`: `(station_id, observed_at)`

### DivePoint (다이빙 포인트)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT | PK, 자동증가 |
| `name` | VARCHAR(200) | 포인트 이름 |
| `lat` | DECIMAL(9,6) | 위도 |
| `lon` | DECIMAL(9,6) | 경도 |
| `region_name` | VARCHAR(200) | 지역명 |
| `description` | TEXT | 설명 |
| `created_at` | TIMESTAMP | 생성 시각 |
| `modified_at` | TIMESTAMP | 수정 시각 |

**인덱스**:
- `idx_dive_point_lat_lon`: `(lat, lon)`

---

## 설정

### application.yml 설정

```yaml
marine:
  nifs:
    key: qPwOeIrU-2511-VFADBV-1349
  khoa:
    key-encoding: "Mw1rP8Brftnpf8vM%2BR%2FA4E%2FA9lhDJDLGNHoe6hmcFPpqGvHF4HdQGXWYHsKSqw2%2Bz5BWuJGa1Db9jTqeHcWe0Q%3D%3D"
    key-decoding: "Mw1rP8Brftnpf8vM+R/A4E/A9lhDJDLGNHoe6hmcFPpqGvHF4HdQGXWYHsKSqw2+z5BWuJGa1Db9jTqeHcWe0Q=="
  kma:
    key: WgCdt_1OQHOAnbf9TpBzWA
```

### WebClient 설정

`MarineWebClientConfig`에서 해양 환경 API 호출용 WebClient를 설정합니다:
- 타임아웃 설정
- 최대 메모리 버퍼 크기: 10MB

---

## 사용 방법

### 1. 서버 실행

```bash
./gradlew bootRun
```

### 2. 관측소 데이터 초기화

**중요**: API를 사용하기 전에 관측소 데이터를 데이터베이스에 저장해야 합니다.

관측소 데이터는 다음 방법으로 초기화할 수 있습니다:

1. **NIFS RISA 관측소 목록 조회 및 저장**
   - `NifsRisaClient.fetchStations()` 호출
   - 응답 데이터를 `MarineStation` 엔티티로 변환하여 저장

2. **KMA 해양기상 관측소 목록 조회 및 저장**
   - KMA API에서 관측소 목록 조회
   - `MarineStation` 엔티티로 저장

3. **KHOA 조위 관측소 목록 조회 및 저장**
   - KHOA API에서 관측소 목록 조회
   - `MarineStation` 엔티티로 저장

**초기화 스크립트 예시** (별도 구현 필요):

```java
@Service
@RequiredArgsConstructor
public class MarineStationInitializer {
    private final NifsRisaClient nifsRisaClient;
    private final MarineStationRepository stationRepository;
    
    @Transactional
    public void initializeStations() {
        // NIFS 관측소 목록 조회 및 저장
        List<NifsRisaResponse.NifsRisaItem> stations = 
            nifsRisaClient.fetchStations().block();
        
        stations.forEach(item -> {
            MarineStation station = MarineStation.builder()
                .externalSource(StationSource.NIFS_RISA)
                .externalStationId(item.getObsPostId())
                .name(item.getObsPostNm())
                .lat(item.getObsLat())
                .lon(item.getObsLon())
                .isActive(true)
                .build();
            stationRepository.save(station);
        });
    }
}
```

### 3. API 호출

**좌표 기반 조회 (포항 해역 - B 타입 관측소 사용):**
```bash
# 포항 해역 (B 타입 관측소 22106 근처) - 풍향/풍속 데이터 포함
curl -X GET "http://localhost:8080/api/environment/summary?lat=36.3500&lon=129.7833"
```

**다른 B 타입 관측소 예시:**
```bash
# 덕적도 해역 (B 타입 관측소 22101)
curl -X GET "http://localhost:8080/api/environment/summary?lat=37.2361&lon=126.0188"

# 칠발도 해역 (B 타입 관측소 22102)
curl -X GET "http://localhost:8080/api/environment/summary?lat=34.7933&lon=125.7769"
```

**포인트 ID 기반 조회:**
```bash
curl -X GET "http://localhost:8080/api/environment/summary?pointId=1"
```

---

## 테스트

### 1. 단위 테스트

각 클라이언트와 서비스의 단위 테스트를 작성할 수 있습니다:

```java
@SpringBootTest
class EnvironmentSummaryServiceTest {
    @Autowired
    private EnvironmentSummaryService service;
    
    @Test
    void testGetEnvironmentSummary() {
        EnvironmentSummaryRequest request = EnvironmentSummaryRequest.builder()
            .lat(36.3500)  // 포항 해역 위도 (B 타입 관측소 22106 근처)
            .lon(129.7833)  // 포항 해역 경도 (B 타입 관측소 22106 근처)
            .build();
        
        EnvironmentSummaryResponse response = 
            service.getEnvironmentSummary(request);
        
        assertNotNull(response);
        assertNotNull(response.getLocation());
    }
}
```

### 2. 통합 테스트

실제 외부 API를 호출하는 통합 테스트:

```bash
# 1. 서버 실행
./gradlew bootRun

# 2. API 호출 테스트 (B 타입 관측소 사용 - 풍향/풍속 데이터 포함)
curl -X GET "http://localhost:8080/api/environment/summary?lat=36.3500&lon=129.7833" \
  -H "Content-Type: application/json"
```

### 3. 테스트 시나리오

#### 성공 케이스
- ✅ 유효한 좌표로 조회
- ✅ 유효한 포인트 ID로 조회
- ✅ 모든 외부 API가 정상 응답

#### 실패 케이스
- ❌ 좌표와 포인트 ID 모두 없음
- ❌ 존재하지 않는 포인트 ID
- ❌ 외부 API 호출 실패 (에러 처리 확인)
- ❌ 관측소 데이터가 없는 경우

---

## 주의사항

1. **관측소 데이터 초기화 필수**: API를 사용하기 전에 관측소 데이터를 데이터베이스에 저장해야 합니다.

2. **외부 API 의존성**: 이 모듈은 외부 API의 가용성에 의존합니다. 외부 API가 다운되거나 응답이 지연될 수 있습니다.

3. **데이터 품질**: 실시간 관측 데이터는 품질 검증 전 데이터일 수 있으므로, 실제 사용 시 주의가 필요합니다.

4. **비동기 처리**: 현재 구현은 `block()`을 사용하여 동기적으로 처리하지만, 향후 완전한 비동기 처리로 개선할 수 있습니다.

5. **에러 처리**: 외부 API 호출 실패 시 해당 필드는 `null`로 반환됩니다. 클라이언트에서 이를 고려해야 합니다.

---

## 향후 개선 사항

- [ ] 완전한 비동기 처리 (Mono/Flux 활용)
- [ ] 관측소 데이터 자동 동기화 스케줄러
- [ ] 관측값 캐싱 (Redis 등)
- [ ] 관측소 데이터 초기화 스크립트 자동화
- [ ] 더 상세한 에러 메시지 및 상태 코드
- [ ] API 응답 시간 모니터링

---

## 관련 파일

- **엔티티**: `domain/MarineStation.java`, `domain/MarineObservation.java`, `domain/DivePoint.java`
- **클라이언트**: `client/NifsRisaClient.java`, `client/KhoaTideClient.java`, `client/KmaSeaObsClient.java`
- **서비스**: `service/EnvironmentSummaryServiceImpl.java`
- **컨트롤러**: `controller/EnvironmentSummaryController.java`
- **설정**: `config/MarineWebClientConfig.java`, `properties/MarineApiProperties.java`

