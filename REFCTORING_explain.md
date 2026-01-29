# OC Diver App - Backend

Spring Boot 기반 해양 활동 기록 관리 시스템 백엔드입니다.

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [리팩토링 개요](#리팩토링-개요)
- [아키텍처](#아키텍처)
- [API 엔드포인트](#api-엔드포인트)
- [데이터 모델](#데이터-모델)
- [검증 전략](#검증-전략)
- [주요 변경사항](#주요-변경사항)

---

## 프로젝트 개요

OC Diver App은 해양 활동(이식, 조식동물 작업, 부착기질 개선, 모니터링, 해양정화)을 기록하고 관리하는 시스템입니다. 관리자는 제출된 기록을 검수하고 승인/반려할 수 있으며, CSV 내보내기 기능을 제공합니다.

---

## 주요 기능

### 1. 기록 관리
- **임시저장 (DRAFT)**: 작업 중인 기록을 임시저장하여 나중에 이어서 작성 가능
- **제출 (SUBMITTED)**: 기록을 제출하여 관리자 검수 대기
- **승인/반려**: 관리자가 제출된 기록을 검수하고 승인 또는 반려 처리
- **수정**: 임시저장된 기록은 수정 가능

### 2. 작업 유형별 기록
- **이식 (TRANSPLANT)**: 해조류 이식 작업 기록
- **조식동물 작업 (GRAZER_REMOVAL)**: 조식동물 제거 작업 기록
- **부착기질 개선 (SUBSTRATE_IMPROVEMENT)**: 부착기질 개선 작업 기록
- **모니터링 (MONITORING)**: 해조류 모니터링 기록
- **해양정화 (MARINE_CLEANUP)**: 해양 폐기물 수거 기록

### 3. 검수 및 내보내기
- 일괄 승인/반려
- CSV 내보내기 (스트리밍 방식)
- 검수 이력 조회

---

## 리팩토링 개요

### 목표
기존 코드의 중복을 제거하고, 확장 가능한 구조로 개선했습니다.

### 주요 개선사항

1. **도메인 분리**
   - 기본 정보 (항상 저장)
   - 공통 환경 기록 (항상 저장)
   - 작업 유형별 기록 (조건부 저장)
   - 공통 후처리 (작업 내용, 미디어, 상태)

2. **작업 유형별 스키마 분리**
   - 각 작업 유형별로 별도 엔티티 생성
   - Null 폭증 문제 해결
   - 확장 가능한 구조

3. **검증 로직 개선**
   - 작업 유형별 조건부 검증
   - 상태 전이 검증 강화

4. **API 구조 개선**
   - 임시저장/제출 분리
   - 명확한 상태 관리

---

## 아키텍처

### 엔티티 구조

```
Submission (기본 정보)
├── BasicEnv (공통 환경 기록)
├── Participants (참여자 정보)
├── ActivityTransplant (이식 작업)
├── ActivityGrazerRemoval (조식동물 작업)
├── ActivitySubstrateImprovement (부착기질 개선)
├── ActivityMonitoring (모니터링)
├── ActivityMarineCleanup (해양정화)
├── Attachment (첨부파일)
└── AuditLog (검수 이력)
```


## API 엔드포인트

### 기록 생성 및 제출

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/admin/submissions/draft` | 기록 임시저장 (DRAFT) |
| POST | `/api/admin/submissions/submit` | 기록 바로 제출 (SUBMITTED) |
| GET | `/api/admin/submissions/draft/{id}` | 임시저장된 기록 조회 |
| PUT | `/api/admin/submissions/draft/{id}` | 임시저장된 기록 수정 (선택적 제출) |

### 기록 조회

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/admin/submissions` | 기록 목록 조회 (필터링, 페이지네이션) |
| GET | `/api/admin/submissions/{id}` | 기록 상세 조회 |
| GET | `/api/admin/submissions/{id}/logs` | 검수 이력 조회 |

### 검수 처리

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/admin/submissions/{id}/approve` | 단건 승인 |
| POST | `/api/admin/submissions/{id}/reject` | 단건 반려 |
| POST | `/api/admin/submissions/bulk/approve` | 일괄 승인 |
| POST | `/api/admin/submissions/bulk/reject` | 일괄 반려 |
| DELETE | `/api/admin/submissions/{id}` | 단건 삭제 |
| DELETE | `/api/admin/submissions/bulk` | 일괄 삭제 |

---

## 데이터 모델

### Submission (기본 정보)

```java
- submissionId: Long
- siteName: String (필수)
- structureType: StructureType (선택, 기본값: OTHER)
- customStructureType: String (OTHER일 때 커스텀 텍스트)
- recordDate: LocalDate (기본값: 현재 날짜)
- divingRound: Integer (1~5, 필수)
- activityType: ActivityType (필수)
- status: SubmissionStatus (기본값: DRAFT)
- authorName: String (필수)
- authorEmail: String
- workDescription: String
- latitude: BigDecimal
- longitude: BigDecimal
- submittedAt: LocalDateTime (DRAFT일 때는 null)
```

### BasicEnv (공통 환경 기록)

```java
- recordDate: LocalDate
- avgDepthM: Double (평균 수심, m)
- maxDepthM: Double (최대 수심, m)
- waterTempC: Double (수온, °C)
- visibilityStatus: MarineCondition (시야: BAD/NORMAL/GOOD)
- waveStatus: MarineCondition (파도: BAD/NORMAL/GOOD)
- surgeStatus: MarineCondition (서지: BAD/NORMAL/GOOD)
- currentStatus: MarineCondition (조류: BAD/NORMAL/GOOD)
```

### 작업 유형별 Activity

#### ActivityTransplant (이식)
```java
- speciesType: TransplantSpeciesType (감태/다시마/곰피/모자반/대황/기타)
- locationType: TransplantLocationType (어초/암반/기타)
- methodType: TransplantMethodType (로프/연승/종자/직접이식/이식용 모듈/기타)
- scale: String (이식 규모)
- zone: String (A/B/C/D)
- healthStatus: HealthStatus (A/B/C/D)
```

#### ActivityGrazerRemoval (조식동물 작업)
```java
- targetSpecies: Set<GrazerSpeciesType> (성게/소라/전복/불가사리/기타)
- densityBeforeWork: DensityLevel (LOW/MID/HIGH)
- workScope: WorkScope (LOCAL/ZONE/WIDE)
- supplementaryExplanation: String (보충 설명)
- collectionAmount: String (수거량)
```

#### ActivitySubstrateImprovement (부착기질 개선)
```java
- targetType: SubstrateTargetType (암반/어초/구조물/기타)
- workScope: String (작업 범위)
- substrateState: String (작업 후 기질 상태)
```

#### ActivityMonitoring (모니터링)
```java
// 적지조사
- entryCoordinate: String (입수 좌표)
- exitCoordinate: String (출수 좌표)
- direction: String (진행방위)
- terrain: TerrainType (암반/모래/혼합/기타)
- barrenExtent: BarrenExtent (없음/진행중/심각)
- grazerDistribution: DensityLevel (낮음/중간/높음)
- rockFeatures: List<RockFeature> (매끈/균열/석회조류우점/혼합/해조류식생)
- suitability: Suitability (적합/부적합)

// 해조류 상태
- seaweedIdNumber: String (식별번호)
- seaweedHealthStatus: SeaweedHealthStatus (양호/쇠약/탈락)
- precisionMeasurement: Boolean (정밀측정 여부)
- leafLength: String (엽장)
- maxLeafWidth: String (최대엽폭)
```

#### ActivityMarineCleanup (해양정화)
```java
- wasteTypes: Set<WasteType> (그물/통발/기타어구/낚시도구/플라스틱/기타)
- method: CleanupMethodType (수작업/인양백/크레인)
- collectionAmount: String (수거량)
- uncollectedScale: UncollectedScale (소/중/대)
```

---

## 검증 전략

### 1. 작업 유형별 조건부 검증

`ActivityValidator`가 `activityType`에 따라 해당 DTO만 검증합니다.

```java
// 예: TRANSPLANT 선택 시
{
  "activityType": "TRANSPLANT",
  "transplantActivity": { ... },  // ✅ 검증됨
  "grazerRemovalActivity": null    // ✅ 무시됨
}
```

**중요**: `@Valid`를 제거하여 Spring의 자동 검증을 방지하고, `ActivityValidator`에서만 검증합니다.

### 2. 상태 전이 검증

`SubmissionStatusValidator`가 상태 전이를 검증합니다.

```
DRAFT → SUBMITTED (제출)
SUBMITTED → APPROVED (승인)
SUBMITTED → REJECTED (반려)
```

### 3. 필수 필드 검증

- **기본 정보**: `siteName`, `divingRound`, `activityType`, `authorName` 필수
- **공통 환경 기록**: 모든 필드 필수
- **작업 유형별 기록**: 선택한 작업 유형의 필수 필드만 검증

---

## 주요 변경사항

### 1. Enum 추가/수정

#### 새로 추가된 Enum
- `StructureType`: 구조물 유형 (십자형 어초/가두리/기타)
- `MarineCondition`: 해양 조건 (나쁨/보통/좋음)
- `HealthStatus`: 건강 상태 (A/B/C/D)
- `DensityLevel`: 밀도 레벨 (낮음/중간/높음, 없음/진행중/심각)
- `WorkScope`: 작업 범위 (국지적/구역별/광범위)
- `SeaweedHealthStatus`: 해조류 생육 상태 (양호/쇠약/탈락)

#### 작업 유형별 Enum
- `TransplantSpeciesType`, `TransplantLocationType`, `TransplantMethodType`
- `GrazerSpeciesType`
- `SubstrateTargetType`
- `CleanupMethodType`, `WasteType`, `UncollectedScale`

#### 수정된 Enum
- `SubmissionStatus`: `DRAFT` 상태 추가
- `ActivityType`: `GRAZER_REMOVAL`, `SUBSTRATE_IMPROVEMENT`, `MARINE_CLEANUP` 추가

### 2. 엔티티 변경

#### Submission
- 새 필드: `structureType`, `customStructureType`, `recordDate`, `divingRound`, `workDescription`, `adminMemo`
- 상태 기본값: `DRAFT`
- `submittedAt`: nullable (DRAFT일 때는 null)
- 작업 유형별 Activity 관계 추가 (1:1)
- 상태 전이 메서드: `submit()`, `approve()`, `reject()`

#### BasicEnv
- 필드 변경: `avgDepthM`, `maxDepthM`, `waterTempC`, `visibilityStatus`, `waveStatus`, `surgeStatus`, `currentStatus`
- 기존 필드 deprecated 처리 (하위 호환성)

#### Participants
- `participantNames`: comma-separated 텍스트

### 3. 작업 유형별 Activity 엔티티 생성

각 작업 유형별로 별도 엔티티를 생성하여 Null 폭증 문제를 해결했습니다.

- `ActivityTransplant`
- `ActivityGrazerRemoval`
- `ActivitySubstrateImprovement`
- `ActivityMonitoring`
- `ActivityMarineCleanup`

### 4. 검증 로직 분리

- `ActivityValidator`: 작업 유형별 조건부 검증
- `SubmissionStatusValidator`: 상태 전이 검증

### 5. API 구조 개선

#### 임시저장/제출 분리
- `POST /api/admin/submissions/draft`: 임시저장
- `POST /api/admin/submissions/submit`: 바로 제출
- `GET /api/admin/submissions/draft/{id}`: 임시저장 조회
- `PUT /api/admin/submissions/draft/{id}`: 임시저장 수정 (선택적 제출)

#### 상태 관리
- DRAFT: 임시저장 상태
- SUBMITTED: 제출 완료 (검수 대기)
- APPROVED: 승인됨
- REJECTED: 반려됨
- DELETED: 삭제됨

---

## 파일 구조

### 새로 생성된 파일

#### Enum
```
src/main/java/com/ocean/piuda/admin/common/enums/
├── StructureType.java
├── MarineCondition.java
├── HealthStatus.java
├── DensityLevel.java
├── WorkScope.java
├── SeaweedHealthStatus.java
├── TransplantSpeciesType.java
├── TransplantLocationType.java
├── TransplantMethodType.java
├── GrazerSpeciesType.java
├── SubstrateTargetType.java
├── CleanupMethodType.java
├── WasteType.java
└── UncollectedScale.java
```

#### Entity
```
src/main/java/com/ocean/piuda/admin/submission/entity/
├── ActivityTransplant.java
├── ActivityGrazerRemoval.java
├── ActivitySubstrateImprovement.java
├── ActivityMonitoring.java
└── ActivityMarineCleanup.java
```

#### Validator
```
src/main/java/com/ocean/piuda/admin/submission/validator/
├── ActivityValidator.java
└── SubmissionStatusValidator.java
```

### 주요 수정 파일

- `Submission.java`: 새 필드 추가, 상태 전이 메서드 추가
- `BasicEnv.java`: 필드 변경
- `Participants.java`: 필드 변경
- `CreateSubmissionRequest.java`: 작업 유형별 DTO 추가, 조건부 검증
- `SubmissionCommandService.java`: 작업 유형별 처리 로직 추가
- `SubmissionController.java`: 임시저장/제출 엔드포인트 분리

---

## 사용 예시

### 1. 기록 임시저장

```json
POST /api/admin/submissions/draft
{
  "siteName": "제주 해역",
  "structureType": "CROSS_REEF",
  "divingRound": 1,
  "activityType": "TRANSPLANT",
  "authorName": "홍길동",
  "basicEnv": {
    "recordDate": "2024-01-15",
    "avgDepthM": 5.0,
    "maxDepthM": 10.0,
    "waterTempC": 18.5,
    "visibilityStatus": "GOOD",
    "waveStatus": "NORMAL",
    "surgeStatus": "NORMAL",
    "currentStatus": "NORMAL"
  },
  "transplantActivity": {
    "speciesType": "ECKLONIA_CAVA",
    "locationType": "ARTIFICIAL_REEF",
    "methodType": "ROPE",
    "scale": "100m²",
    "zone": "A",
    "healthStatus": "A"
  }
}
```

### 2. 기록 제출

```json
POST /api/admin/submissions/submit
{
  // 위와 동일한 구조
}
```

### 3. 임시저장된 기록 수정 및 제출

```json
PUT /api/admin/submissions/draft/1?submit=true
{
  // 수정할 필드들
}
```

---

## 주의사항

1. **하위 호환성**: 기존 `Activity` 엔티티는 deprecated 처리했으나 유지됨
2. **데이터베이스 마이그레이션 필요**: 새 컬럼 추가 및 작업 유형별 테이블 생성 필요
3. **검증 로직**: `@Valid`를 제거하여 `ActivityValidator`에서만 검증하도록 변경됨
4. **상태 전이**: 상태 전이는 엔티티의 비즈니스 메서드(`submit()`, `approve()`, `reject()`)를 통해서만 가능

---

## 개발 환경

- Java 17+
- Spring Boot 3.x
- PostgreSQL 14+
- PostGIS (지리 정보 시스템)

---

## 라이선스

[라이선스 정보]

---

## 기여자

[기여자 목록]
