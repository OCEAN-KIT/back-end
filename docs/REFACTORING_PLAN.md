# OC Diver App 리팩토링 계획서

## 1. 기존 코드 문제점 분석

### 1.1 중복 및 구조적 문제

#### 문제점 1: Activity 엔티티의 Null 폭증
- **현황**: `Activity` 테이블에 모든 작업 유형의 필드가 한 테이블에 뭉쳐있음
- **영향**: 
  - TRANSPLANT 작업 시 GRAZER_REMOVAL 관련 필드는 모두 NULL
  - 테이블 스키마가 비대해지고 가독성 저하
  - 작업 유형 추가 시 기존 테이블 수정 필요

#### 문제점 2: 요구사항 필드 부족
- **부족한 필드**:
  - 구조물 유형 (enum)
  - 다이빙 회차 (1~5)
  - 작업 유형별 상세 필드 (요구사항 [C]의 세부 항목들)
- **현재 상태**: 기본적인 필드만 존재

#### 문제점 3: 상태 관리 부족
- **현황**: `SubmissionStatus`에 `DRAFT` 상태 없음
- **영향**: 임시저장 기능 구현 불가

#### 문제점 4: 공동 작업자 저장 방식
- **현황**: `Participants` 엔티티에 단순 저장
- **요구사항**: comma-separated text 또는 배열/별도 테이블
- **영향**: 다중 작업자 관리 어려움

#### 문제점 5: 작업 유형별 검증 로직 부재
- **현황**: 작업 유형별 필수값 검증 없음
- **영향**: 잘못된 데이터 저장 가능

#### 문제점 6: BasicEnv 필드 불일치
- **요구사항**: 평균 수심, 최대 수심, 시야/파도/서지/조류 (BAD/NORMAL/GOOD)
- **현황**: `depthM` (단일), `visibilityM` (Float), `currentState` (LOW/MEDIUM/HIGH)
- **영향**: 요구사항과 불일치

### 1.2 승인/반려 플로우
- **현황**: `SubmissionCommandService`에 집중되어 있으나 상태 전이 검증 부족
- **개선 필요**: 상태 전이 검증 로직 강화

### 1.3 미디어 업로드
- **현황**: `Attachment` 엔티티 존재, presigned URL 기능 확인 필요
- **개선 필요**: AWS S3 presigned URL 발급 로직 통합

---

## 2. 도메인 모델 비교 및 선택

### 안 A: 부모(Submission) + 유형별 서브테이블(1:1) 구조

```
Submission (1) ── (1) ActivityTransplant
              ├── (1) ActivityGrazerRemoval
              ├── (1) ActivitySubstrateImprovement
              ├── (1) ActivityMonitoring
              └── (1) ActivityMarineCleanup
```

**장점**:
- ✅ **쿼리성**: SQL JOIN으로 유형별 필드 조회 용이
- ✅ **검증**: DB 레벨 NOT NULL 제약으로 타입별 필수값 강제
- ✅ **리포팅**: GROUP BY, 집계 함수 사용 용이
- ✅ **CSV 추출**: 유형별 컬럼을 직접 SELECT 가능
- ✅ **인덱싱**: 유형별 필드에 인덱스 생성 가능
- ✅ **타입 안정성**: JPA 엔티티로 컴파일 타임 검증

**단점**:
- ❌ **확장성**: 새 작업 유형 추가 시 새 테이블 생성 필요
- ❌ **마이그레이션**: 유형 추가 시 DDL 변경 필요

### 안 B: 부모 + 유형별 JSONB(payload) + 스키마 버전 관리

```
Submission (1) ── (1) Activity
                      └── payload (JSONB)
                      └── schema_version (INT)
```

**장점**:
- ✅ **확장성**: 새 작업 유형 추가 시 테이블 변경 불필요
- ✅ **유연성**: 동적 필드 추가 가능
- ✅ **마이그레이션**: 스키마 버전으로 이전 데이터 호환성 관리

**단점**:
- ❌ **쿼리성**: JSONB 쿼리는 복잡하고 성능 저하 가능
- ❌ **검증**: 애플리케이션 레벨 검증만 가능 (DB 제약 불가)
- ❌ **리포팅**: JSONB 필드 집계/필터링 복잡
- ❌ **CSV 추출**: JSON 파싱 후 컬럼 변환 필요
- ❌ **인덱싱**: JSONB 인덱스는 제한적

### 선택: **안 A (서브테이블 구조)**

**선택 이유**:
1. **행정기관 보고/CSV 추출**: 유형별 컬럼을 직접 조회 가능
2. **통계/필터링**: SQL 집계 함수 활용 용이
3. **데이터 무결성**: DB 레벨 제약으로 데이터 품질 보장
4. **성능**: JOIN이 JSONB 쿼리보다 효율적

---

## 3. 리팩토링 후 ERD

```
┌─────────────────────────────────────────────────────────┐
│                    Submission                            │
├─────────────────────────────────────────────────────────┤
│ PK submission_id (BIGINT)                               │
│    site_name (VARCHAR(200)) NOT NULL                    │
│    structure_type (ENUM) NOT NULL                        │
│    record_date (DATE) NOT NULL                          │
│    diving_round (INT) CHECK (1-5)                       │
│    activity_type (ENUM) NOT NULL                         │
│    status (ENUM) NOT NULL [DRAFT/SUBMITTED/APPROVED/    │
│                            REJECTED]                     │
│    author_name (VARCHAR(100)) NOT NULL                  │
│    author_email (VARCHAR(200))                          │
│    work_description (TEXT)                               │
│    admin_memo (TEXT)                                     │
│    latitude (DECIMAL(9,6))                               │
│    longitude (DECIMAL(9,6))                              │
│    submitted_at (TIMESTAMP)                              │
│    created_at, modified_at                               │
└─────────────────────────────────────────────────────────┘
                            │
                            │ 1:1
                            ├──────────────────┐
                            │                  │
        ┌───────────────────┴───┐   ┌─────────┴──────────┐
        │      BasicEnv          │   │    Participants   │
        ├───────────────────────┤   ├───────────────────┤
        │ PK env_id             │   │ PK participant_id │
        │ FK submission_id      │   │ FK submission_id  │
        │    avg_depth_m        │   │    leader_name     │
        │    max_depth_m        │   │    participant_   │
        │    water_temp_c       │   │      names (TEXT) │
        │    visibility_status  │   │                   │
        │      (ENUM)           │   │                   │
        │    wave_status (ENUM) │   │                   │
        │    surge_status (ENUM)│   │                   │
        │    current_status     │   │                   │
        │      (ENUM)           │   │                   │
        └───────────────────────┘   └───────────────────┘
                            │
                            │ 1:1 (조건부)
                            │
        ┌───────────────────┴───────────────────────────┐
        │                                               │
┌───────┴────────┐  ┌──────────────┐  ┌──────────────┐
│ActivityTransplant│ │ActivityGrazer│ │ActivitySubstr│
├─────────────────┤ │  Removal     │ │  Improvement │
│PK activity_id   │ ├──────────────┤ ├──────────────┤
│FK submission_id │ │PK activity_id│ │PK activity_id│
│  species_type   │ │FK submission │ │FK submission │
│  location_type   │ │  target_     │ │  target_type │
│  method_type    │ │    species[] │ │  work_scope  │
│  scale          │ │  density     │ │  substrate_  │
│  zone           │ │  scope       │ │    state     │
│  health_status  │ │  note        │ │              │
└─────────────────┘ │  amount      │ └──────────────┘
                    └──────────────┘
        │
┌───────┴────────┐  ┌──────────────┐
│ActivityMonitor │  │ActivityMarine│
│  ing           │  │  Cleanup     │
├────────────────┤  ├──────────────┤
│PK activity_id  │  │PK activity_id│
│FK submission_id│  │FK submission │
│  entry_coord   │  │  waste_types │
│  exit_coord    │  │  method      │
│  direction     │  │  amount      │
│  terrain       │  │  uncollected │
│  barren_extent │  │    scale     │
│  grazer_dist   │  │              │
│  rock_features │  │              │
│  suitability   │  │              │
│  seaweed_data  │  │              │
│  precision     │  │              │
│  measurements  │  │              │
└────────────────┘  └──────────────┘

┌─────────────────────────────────────────────────────────┐
│                    Attachment                           │
├─────────────────────────────────────────────────────────┤
│ PK attachment_id                                        │
│ FK submission_id                                        │
│    file_name (VARCHAR(255))                            │
│    file_url (VARCHAR(500)) NOT NULL                    │
│    presigned_url (VARCHAR(500))                        │
│    presigned_expires_at (TIMESTAMP)                     │
│    mime_type (VARCHAR(50))                              │
│    file_size (INT)                                      │
│    uploaded_at (TIMESTAMP)                              │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                    RejectReason                         │
├─────────────────────────────────────────────────────────┤
│ PK reason_id                                            │
│ FK submission_id (UNIQUE)                               │
│    template_code (VARCHAR(100))                         │
│    message (VARCHAR(500)) NOT NULL                      │
│    rejected_by (VARCHAR(100)) NOT NULL                  │
│    rejected_at (TIMESTAMP)                              │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                    AuditLog                             │
├─────────────────────────────────────────────────────────┤
│ PK log_id                                               │
│ FK submission_id                                        │
│    action (ENUM) NOT NULL                               │
│    performed_by (VARCHAR(100)) NOT NULL                  │
│    comment (VARCHAR(500))                               │
│    created_at                                           │
└─────────────────────────────────────────────────────────┘
```

---

## 4. 핵심 Entity/DTO 클래스 스케치

### 4.1 Submission 엔티티 (수정)

```java
@Entity
@Table(name = "submission")
public class Submission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;
    
    @Column(nullable = false, length = 200)
    private String siteName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StructureType structureType;  // NEW
    
    @Column(nullable = false)
    private LocalDate recordDate;  // 기본값: 작성일
    
    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer divingRound;  // NEW: 1~5
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.DRAFT;  // DRAFT 추가
    
    @Column(nullable = false, length = 100)
    private String authorName;
    
    @Column(length = 200)
    private String authorEmail;
    
    @Column(columnDefinition = "TEXT")
    private String workDescription;  // 작업 내용 (기존 feedbackText)
    
    @Column(columnDefinition = "TEXT")
    private String adminMemo;  // 관리자 검수 메모
    
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;
    
    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;
    
    private LocalDateTime submittedAt;
    
    // 관계
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private BasicEnv basicEnv;
    
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Participants participants;
    
    // 작업 유형별 Activity (조건부 1:1)
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityTransplant activityTransplant;
    
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityGrazerRemoval activityGrazerRemoval;
    
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivitySubstrateImprovement activitySubstrateImprovement;
    
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityMonitoring activityMonitoring;
    
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityMarineCleanup activityMarineCleanup;
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();
    
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private RejectReason rejectReason;
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();
    
    // 상태 전이 검증
    public void submit() {
        if (this.status != SubmissionStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태만 제출 가능");
        }
        this.status = SubmissionStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }
    
    public void approve() {
        if (this.status != SubmissionStatus.SUBMITTED) {
            throw new IllegalStateException("SUBMITTED 상태만 승인 가능");
        }
        this.status = SubmissionStatus.APPROVED;
    }
    
    public void reject(String reason, String rejectedBy) {
        if (this.status != SubmissionStatus.SUBMITTED) {
            throw new IllegalStateException("SUBMITTED 상태만 반려 가능");
        }
        this.status = SubmissionStatus.REJECTED;
        // RejectReason 설정
    }
}
```

### 4.2 BasicEnv 엔티티 (수정)

```java
@Entity
@Table(name = "basic_env")
public class BasicEnv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long envId;
    
    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    
    @Column(nullable = false)
    private LocalDate recordDate;
    
    @Column(nullable = false)
    private Double avgDepthM;  // 평균 수심
    
    @Column(nullable = false)
    private Double maxDepthM;  // 최대 수심
    
    @Column(nullable = false)
    private Double waterTempC;  // 수온
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarineCondition visibilityStatus;  // BAD/NORMAL/GOOD
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarineCondition waveStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarineCondition surgeStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarineCondition currentStatus;
}
```

### 4.3 Participants 엔티티 (수정)

```java
@Entity
@Table(name = "participants")
public class Participants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;
    
    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    
    @Column(name = "leader_name", length = 100)
    private String leaderName;
    
    @Column(name = "participant_names", columnDefinition = "TEXT")
    private String participantNames;  // comma-separated 또는 JSON 배열
    
    // 또는 별도 테이블로 분리
    // @OneToMany
    // private List<Participant> participants;
}
```

### 4.4 작업 유형별 Activity 엔티티 예시

#### ActivityTransplant

```java
@Entity
@Table(name = "activity_transplant")
public class ActivityTransplant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    
    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransplantSpeciesType speciesType;  // 감태/다시마/곰피/모자반/대황/기타
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransplantLocationType locationType;  // 어초/암반/기타
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransplantMethodType methodType;  // 로프/연승/종자/직접이식/모듈/기타
    
    @Column(nullable = false)
    private String scale;  // 이식 규모
    
    @Column(nullable = false, length = 1)
    private String zone;  // A/B/C/D
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HealthStatus healthStatus;  // A/B/C/D
}
```

#### ActivityGrazerRemoval

```java
@Entity
@Table(name = "activity_grazer_removal")
public class ActivityGrazerRemoval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    
    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "grazer_removal_target_species", 
                     joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "species")
    private List<GrazerSpeciesType> targetSpecies;  // 복수 enum
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DensityLevel densityBeforeWork;  // LOW/MID/HIGH
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkScope workScope;  // LOCAL/ZONE/WIDE
    
    @Column(columnDefinition = "TEXT")
    private String note;  // 보충 설명
    
    @Column(nullable = false)
    private String collectionAmount;  // 수거량
}
```

#### ActivityMonitoring

```java
@Entity
@Table(name = "activity_monitoring")
public class ActivityMonitoring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    
    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;
    
    @Embedded
    private Coordinate entryCoordinate;  // 입수좌표
    
    @Embedded
    private Coordinate exitCoordinate;  // 출수좌표
    
    private String direction;  // 진행방위
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TerrainType terrain;  // 암반/모래/혼합/기타
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BarrenExtent barrenExtent;  // NONE/ONGOING/SEVERE
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DensityLevel grazerDistribution;  // LOW/MID/HIGH
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "monitoring_rock_features",
                     joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "feature")
    private List<RockFeature> rockFeatures;  // 복수 enum
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Suitability suitability;  // SUITABLE/UNSUITABLE
    
    @OneToMany(mappedBy = "activityMonitoring", cascade = CascadeType.ALL)
    private List<SeaweedStatus> seaweedStatuses;  // 해조류 상태 리스트
    
    private Boolean precisionMeasurement;  // 정밀측정 여부
    
    @Embedded
    private MeasurementData measurements;  // 엽장/최대엽폭
}
```

### 4.5 Enum 정의

```java
// 구조물 유형
public enum StructureType {
    CROSS_REEF,      // 십자형 어초
    CAGE,            // 가두리
    OTHER            // 기타
}

// 작업 유형 (기존 확장)
public enum ActivityType {
    TRANSPLANT,              // 이식
    GRAZER_REMOVAL,          // 조식동물 작업
    SUBSTRATE_IMPROVEMENT,   // 부착기질 개선
    MONITORING,              // 모니터링
    MARINE_CLEANUP           // 해양정화
}

// 상태 (DRAFT 추가)
public enum SubmissionStatus {
    DRAFT,      // 임시저장
    SUBMITTED,  // 제출
    APPROVED,   // 승인
    REJECTED    // 반려
}

// 해양 조건
public enum MarineCondition {
    BAD,
    NORMAL,
    GOOD
}

// 건강 상태
public enum HealthStatus {
    A,  // 활착양호
    B,  // 생육정상
    C,  // 부분스트레스
    D   // 쇠약/탈락진행
}

// 밀도/분포 레벨
public enum DensityLevel {
    LOW,
    MID,
    HIGH
}

// 작업 범위
public enum WorkScope {
    LOCAL,
    ZONE,
    WIDE
}
```

### 4.6 DTO 스케치

#### CreateSubmissionRequest (수정)

```java
public class CreateSubmissionRequest {
    @NotBlank
    private String siteName;
    
    @NotNull
    private StructureType structureType;
    
    private LocalDate recordDate;  // null이면 현재 날짜
    
    @NotNull
    @Min(1) @Max(5)
    private Integer divingRound;
    
    @NotNull
    private ActivityType activityType;
    
    @NotBlank
    private String authorName;
    
    private String authorEmail;
    
    private String workDescription;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    @Valid
    @NotNull
    private BasicEnvDto basicEnv;
    
    @Valid
    private ParticipantsDto participants;
    
    // 작업 유형별 DTO (조건부)
    @Valid
    @ConditionalRequired(activityType = ActivityType.TRANSPLANT)
    private TransplantActivityDto transplantActivity;
    
    @Valid
    @ConditionalRequired(activityType = ActivityType.GRAZER_REMOVAL)
    private GrazerRemovalActivityDto grazerRemovalActivity;
    
    // ... 기타 작업 유형
    
    @Valid
    private List<AttachmentDto> attachments;
}
```

---

## 5. 주요 API 엔드포인트

| Method | Endpoint | 설명 | 상태 전이 |
|--------|----------|------|----------|
| POST | `/api/submissions` | 기록 생성/임시저장 | → DRAFT |
| PUT | `/api/submissions/{id}` | 기록 수정 (DRAFT만) | DRAFT → DRAFT |
| POST | `/api/submissions/{id}/submit` | 제출 | DRAFT → SUBMITTED |
| GET | `/api/submissions` | 목록 조회 (필터: 현장/기간/작업유형/상태/다이버) | - |
| GET | `/api/submissions/{id}` | 상세 조회 | - |
| POST | `/api/admin/submissions/{id}/approve` | 관리자 승인 | SUBMITTED → APPROVED |
| POST | `/api/admin/submissions/{id}/reject` | 관리자 반려 | SUBMITTED → REJECTED |
| GET | `/api/submissions/export/csv` | CSV 스트리밍 다운로드 | - |
| POST | `/api/submissions/{id}/attachments/presigned-url` | 미디어 presigned URL 발급 | - |
| POST | `/api/submissions/{id}/attachments` | 미디어 메타데이터 저장 | - |

---

## 6. 검증(Validation) 전략

### 6.1 작업 유형별 필수값 검증

```java
@Component
public class ActivityValidator {
    
    public void validate(ActivityType type, Object activityData) {
        switch (type) {
            case TRANSPLANT:
                validateTransplant((TransplantActivityDto) activityData);
                break;
            case GRAZER_REMOVAL:
                validateGrazerRemoval((GrazerRemovalActivityDto) activityData);
                break;
            // ... 기타 유형
        }
    }
    
    private void validateTransplant(TransplantActivityDto dto) {
        if (dto.getSpeciesType() == null) {
            throw new ValidationException("대상 종류는 필수입니다");
        }
        if (dto.getLocationType() == null) {
            throw new ValidationException("이식 장소는 필수입니다");
        }
        // ... 기타 필수 필드
    }
}
```

### 6.2 상태 전이 검증

```java
public class SubmissionStatusValidator {
    
    private static final Map<SubmissionStatus, Set<SubmissionStatus>> ALLOWED_TRANSITIONS = Map.of(
        SubmissionStatus.DRAFT, Set.of(SubmissionStatus.DRAFT, SubmissionStatus.SUBMITTED),
        SubmissionStatus.SUBMITTED, Set.of(SubmissionStatus.APPROVED, SubmissionStatus.REJECTED),
        SubmissionStatus.APPROVED, Set.of(),  // 최종 상태
        SubmissionStatus.REJECTED, Set.of()   // 최종 상태
    );
    
    public void validateTransition(SubmissionStatus from, SubmissionStatus to) {
        Set<SubmissionStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new IllegalStateException(
                String.format("상태 전이 불가: %s → %s", from, to)
            );
        }
    }
}
```

### 6.3 범위 검증

```java
public class RangeValidator {
    
    @AssertTrue(message = "수심은 0 이상이어야 합니다")
    public boolean isValidDepth(Double depth) {
        return depth == null || depth >= 0;
    }
    
    @AssertTrue(message = "수온은 -2°C ~ 40°C 범위여야 합니다")
    public boolean isValidWaterTemp(Double temp) {
        return temp == null || (temp >= -2 && temp <= 40);
    }
    
    @AssertTrue(message = "다이빙 회차는 1~5 사이여야 합니다")
    public boolean isValidDivingRound(Integer round) {
        return round == null || (round >= 1 && round <= 5);
    }
}
```

---

## 7. 마이그레이션 전략

### 7.1 단계별 마이그레이션

#### Phase 1: 기존 데이터 백업 및 새 스키마 생성
```sql
-- 1. 기존 테이블 백업
CREATE TABLE submission_backup AS SELECT * FROM submission;
CREATE TABLE activity_backup AS SELECT * FROM activity;
CREATE TABLE basic_env_backup AS SELECT * FROM basic_env;

-- 2. 새 스키마 생성
-- Submission 테이블에 새 컬럼 추가
ALTER TABLE submission ADD COLUMN structure_type VARCHAR(50);
ALTER TABLE submission ADD COLUMN diving_round INTEGER CHECK (diving_round BETWEEN 1 AND 5);
ALTER TABLE submission ADD COLUMN work_description TEXT;
ALTER TABLE submission ADD COLUMN admin_memo TEXT;
ALTER TABLE submission ALTER COLUMN status SET DEFAULT 'DRAFT';

-- BasicEnv 수정
ALTER TABLE basic_env ADD COLUMN avg_depth_m DOUBLE PRECISION;
ALTER TABLE basic_env ADD COLUMN max_depth_m DOUBLE PRECISION;
ALTER TABLE basic_env RENAME COLUMN depth_m TO avg_depth_m;  -- 임시
ALTER TABLE basic_env ADD COLUMN visibility_status VARCHAR(20);
ALTER TABLE basic_env ADD COLUMN wave_status VARCHAR(20);
ALTER TABLE basic_env ADD COLUMN surge_status VARCHAR(20);
ALTER TABLE basic_env RENAME COLUMN current_state TO current_status;
```

#### Phase 2: 작업 유형별 테이블 생성
```sql
-- ActivityTransplant
CREATE TABLE activity_transplant (
    activity_id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE REFERENCES submission(submission_id),
    species_type VARCHAR(50) NOT NULL,
    location_type VARCHAR(50) NOT NULL,
    method_type VARCHAR(50) NOT NULL,
    scale VARCHAR(200) NOT NULL,
    zone VARCHAR(1) NOT NULL CHECK (zone IN ('A', 'B', 'C', 'D')),
    health_status VARCHAR(1) NOT NULL CHECK (health_status IN ('A', 'B', 'C', 'D'))
);

-- ActivityGrazerRemoval
CREATE TABLE activity_grazer_removal (
    activity_id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE REFERENCES submission(submission_id),
    density_before_work VARCHAR(20) NOT NULL,
    work_scope VARCHAR(20) NOT NULL,
    note TEXT,
    collection_amount VARCHAR(200) NOT NULL
);

CREATE TABLE grazer_removal_target_species (
    activity_id BIGINT NOT NULL REFERENCES activity_grazer_removal(activity_id),
    species VARCHAR(50) NOT NULL,
    PRIMARY KEY (activity_id, species)
);

-- ... 기타 작업 유형 테이블
```

#### Phase 3: 데이터 마이그레이션
```sql
-- 기존 Activity 데이터를 유형별 테이블로 분리
-- (기존 activity.type에 따라 분기)

-- TRANSPLANT 데이터 마이그레이션
INSERT INTO activity_transplant (submission_id, species_type, location_type, ...)
SELECT submission_id, 
       -- 기존 필드 매핑 또는 기본값
       'OTHER' as species_type,
       'OTHER' as location_type,
       ...
FROM activity
WHERE type = 'TRANSPLANT';

-- ... 기타 유형
```

#### Phase 4: 기존 테이블 제거 (선택)
```sql
-- 모든 데이터 마이그레이션 완료 후
-- DROP TABLE activity;  -- 신중하게!
```

### 7.2 롤백 계획
- 백업 테이블 유지
- 트랜잭션으로 마이그레이션 실행
- 단계별 검증 후 다음 단계 진행

---

## 8. 중복 제거 포인트 (기존 대비 개선사항)

### 8.1 엔티티 레벨
1. ✅ **Activity 엔티티 분리**: 단일 테이블 → 유형별 서브테이블로 분리하여 NULL 폭증 제거
2. ✅ **BasicEnv 필드 정리**: 요구사항에 맞게 필드명/타입 통일
3. ✅ **Participants 저장 방식**: comma-separated 또는 별도 테이블로 명확화
4. ✅ **상태 관리**: DRAFT 상태 추가로 임시저장 기능 구현

### 8.2 DTO 레벨
1. ✅ **작업 유형별 DTO 분리**: `ActivityDto` → `TransplantActivityDto`, `GrazerRemovalActivityDto` 등
2. ✅ **중첩 DTO 제거**: `CreateSubmissionRequest` 내부 클래스 → 별도 파일로 분리 권장
3. ✅ **검증 어노테이션 통일**: 작업 유형별 필수값 검증 로직 명확화

### 8.3 서비스 레벨
1. ✅ **작업 유형별 처리 로직 분리**: `ActivityValidator`로 검증 로직 분리
2. ✅ **상태 전이 검증**: `SubmissionStatusValidator`로 상태 전이 규칙 명확화
3. ✅ **미디어 업로드 로직 통합**: presigned URL 발급 로직 서비스로 분리

### 8.4 Enum 레벨
1. ✅ **요구사항에 맞는 Enum 추가**: `StructureType`, `MarineCondition`, `HealthStatus` 등
2. ✅ **기존 Enum 확장**: `ActivityType`에 새 유형 추가

### 8.5 데이터베이스 레벨
1. ✅ **제약조건 강화**: CHECK 제약으로 다이빙 회차 범위, 건강 상태 값 제한
2. ✅ **인덱스 최적화**: 작업 유형별 테이블로 인덱스 효율성 향상
3. ✅ **NULL 최소화**: 작업 유형별 필수 필드에 NOT NULL 제약

---

## 9. 구현 우선순위

### Phase 1: 핵심 구조 변경 (1주)
1. Submission 엔티티 수정 (새 필드 추가)
2. BasicEnv 엔티티 수정
3. 작업 유형별 Activity 엔티티 생성 (최소 2개: TRANSPLANT, GRAZER_REMOVAL)
4. Enum 추가/수정

### Phase 2: 검증 및 상태 관리 (3일)
1. ActivityValidator 구현
2. SubmissionStatusValidator 구현
3. 상태 전이 메서드 추가

### Phase 3: API 수정 (1주)
1. CreateSubmissionRequest 수정
2. Service 로직 수정
3. Controller 엔드포인트 수정

### Phase 4: 마이그레이션 (2일)
1. 마이그레이션 스크립트 작성
2. 테스트 환경에서 검증
3. 프로덕션 적용

### Phase 5: 나머지 작업 유형 추가 (1주)
1. 나머지 작업 유형 엔티티 생성
2. 검증 로직 추가
3. 테스트

---

## 10. 주의사항

1. **엔티티명 수정 금지**: 요구사항에 따라 기존 엔티티명(Submission, Activity 등) 유지
2. **AI 기능 삭제**: AI 관련 기능은 리팩토링 범위에서 제외
3. **점진적 마이그레이션**: 한 번에 모든 작업 유형을 마이그레이션하지 말고 단계적으로 진행
4. **데이터 무결성**: 마이그레이션 시 데이터 손실 방지
5. **하위 호환성**: 기존 API는 가능한 한 유지하되, 내부 구조만 개선
