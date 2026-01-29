# 리팩토링 완료 요약 (Phase 1-3)

## 완료된 작업

### Phase 1: 핵심 구조 변경 ✅

#### 1.1 Enum 추가/수정
- ✅ `StructureType` 추가 (CROSS_REEF, CAGE, OTHER)
- ✅ `MarineCondition` 추가 (BAD, NORMAL, GOOD)
- ✅ `HealthStatus` 추가 (A, B, C, D)
- ✅ `DensityLevel` 추가 (LOW, MID, HIGH)
- ✅ `WorkScope` 추가 (LOCAL, ZONE, WIDE)
- ✅ `SubmissionStatus` 수정: DRAFT 상태 추가
- ✅ `ActivityType` 확장: GRAZER_REMOVAL, SUBSTRATE_IMPROVEMENT, MARINE_CLEANUP 추가
- ✅ 작업 유형별 Enum 추가:
  - `TransplantSpeciesType`, `TransplantLocationType`, `TransplantMethodType`
  - `GrazerSpeciesType`
  - `SubstrateTargetType`
  - `CleanupMethodType`, `WasteType`, `UncollectedScale`

#### 1.2 Submission 엔티티 수정
- ✅ 새 필드 추가:
  - `structureType` (구조물 유형)
  - `recordDate` (기록 날짜)
  - `divingRound` (다이빙 회차, 1~5)
  - `workDescription` (작업 내용)
  - `adminMemo` (관리자 검수 메모)
- ✅ 상태 기본값 변경: `DRAFT`로 시작
- ✅ `submittedAt` nullable로 변경 (DRAFT일 때는 null)
- ✅ 작업 유형별 Activity 관계 추가:
  - `activityTransplant` (이식 작업)
  - `activityGrazerRemoval` (조식동물 작업)
  - `activitySubstrateImprovement` (부착기질 개선)
  - `activityMonitoring` (모니터링)
  - `activityMarineCleanup` (해양정화)
- ✅ 상태 전이 메서드 추가: `submit()`, `approve()`, `reject()`

#### 1.3 BasicEnv 엔티티 수정
- ✅ 필드 변경:
  - `avgDepthM` (평균 수심)
  - `maxDepthM` (최대 수심)
  - `waterTempC` (수온)
  - `visibilityStatus`, `waveStatus`, `surgeStatus`, `currentStatus` (MarineCondition enum)
- ✅ 기존 필드 deprecated 처리 (하위 호환성)

#### 1.4 Participants 엔티티 수정
- ✅ `participantNames` 필드 추가 (comma-separated 또는 JSON 배열)
- ✅ 기존 필드 deprecated 처리

#### 1.5 작업 유형별 Activity 엔티티 생성
- ✅ `ActivityTransplant` 생성
- ✅ `ActivityGrazerRemoval` 생성
- ✅ `ActivitySubstrateImprovement` 생성
- ✅ `ActivityMonitoring` 생성 (기본 구조)
- ✅ `ActivityMarineCleanup` 생성

### Phase 2: 검증 및 상태 관리 ✅

#### 2.1 SubmissionStatusValidator 구현
- ✅ 상태 전이 검증 로직
- ✅ 제출/승인/반려 가능 여부 확인 메서드

#### 2.2 ActivityValidator 구현
- ✅ 작업 유형별 필수값 검증
- ✅ TRANSPLANT, GRAZER_REMOVAL, SUBSTRATE_IMPROVEMENT, MARINE_CLEANUP 검증 로직

### Phase 3: API 수정 ✅

#### 3.1 CreateSubmissionRequest DTO 수정
- ✅ 새 필드 추가 (structureType, divingRound, workDescription 등)
- ✅ 작업 유형별 Activity DTO 추가:
  - `TransplantActivityDto`
  - `GrazerRemovalActivityDto`
  - `SubstrateImprovementActivityDto`
  - `MonitoringActivityDto`
  - `MarineCleanupActivityDto`
- ✅ BasicEnvDto 필드 수정
- ✅ ParticipantsDto 필드 수정
- ✅ 하위 호환성을 위한 기존 필드 유지 (deprecated)

#### 3.2 SubmissionCommandService 수정
- ✅ `createSubmission()`: DRAFT 상태로 생성, 작업 유형별 Activity 생성 로직 추가
- ✅ `submitSubmission()`: DRAFT -> SUBMITTED 전이 메서드 추가
- ✅ `approveSubmission()`: 상태 검증 강화
- ✅ `rejectSubmission()`: 상태 검증 강화
- ✅ `createActivityByType()`: 작업 유형별 Activity 생성 로직 분리

#### 3.3 SubmissionController 수정
- ✅ 제출 엔드포인트 추가: `POST /{submissionId}/submit`
- ✅ 상태 필터 설명 업데이트 (DRAFT 추가)

#### 3.4 ExceptionType 추가
- ✅ `SUBMISSION_ALREADY_SUBMITTED` 추가
- ✅ `SUBMISSION_INVALID_STATUS` 추가

## 생성된 파일 목록

### Enum
- `StructureType.java`
- `MarineCondition.java`
- `HealthStatus.java`
- `DensityLevel.java`
- `WorkScope.java`
- `TransplantSpeciesType.java`
- `TransplantLocationType.java`
- `TransplantMethodType.java`
- `GrazerSpeciesType.java`
- `SubstrateTargetType.java`
- `CleanupMethodType.java`
- `WasteType.java`
- `UncollectedScale.java`

### Entity
- `ActivityTransplant.java`
- `ActivityGrazerRemoval.java`
- `ActivitySubstrateImprovement.java`
- `ActivityMonitoring.java`
- `ActivityMarineCleanup.java`

### Validator
- `SubmissionStatusValidator.java`
- `ActivityValidator.java`

## 수정된 파일 목록

### Entity
- `Submission.java` - 새 필드 추가, 상태 전이 메서드 추가
- `BasicEnv.java` - 필드 변경
- `Participants.java` - 필드 변경

### Enum
- `SubmissionStatus.java` - DRAFT 추가
- `ActivityType.java` - 새 작업 유형 추가

### DTO
- `CreateSubmissionRequest.java` - 작업 유형별 DTO 추가

### Service
- `SubmissionCommandService.java` - 작업 유형별 처리 로직 추가

### Controller
- `SubmissionController.java` - 제출 엔드포인트 추가

### Exception
- `ExceptionType.java` - 새 예외 타입 추가

## 다음 단계 (Phase 4-5)

### Phase 4: 마이그레이션
- [ ] 마이그레이션 스크립트 작성
- [ ] 기존 데이터 백업
- [ ] 데이터 마이그레이션

### Phase 5: 나머지 작업 유형 완성
- [ ] ActivityMonitoring 상세 필드 구현
- [ ] Response DTO 수정 (작업 유형별 필드 반영)
- [ ] CSV 내보내기 로직 수정
- [ ] 테스트 작성

## 주의사항

1. **하위 호환성**: 기존 `Activity` 엔티티와 `activity` 필드는 deprecated 처리했으나 유지됨
2. **데이터베이스 마이그레이션 필요**: 새 컬럼 추가 및 작업 유형별 테이블 생성 필요
3. **기존 데이터**: 마이그레이션 전에 백업 필수
4. **테스트**: 컴파일은 통과했으나 실제 동작 테스트 필요
