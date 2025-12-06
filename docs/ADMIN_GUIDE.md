# OceanCampus Admin API 문서

## 📋 목차

1. [개요](#개요)
2. [인증](#인증)
3. [Dashboard](#dashboard)
4. [Submission 관리](#submission-관리)
5. [Export 기능](#export-기능)
6. [공통 응답 형식](#공통-응답-형식)
7. [에러 코드](#에러-코드)

---

## 개요

OceanCampus Admin API는 해양 활동 제출 데이터를 관리하는 관리자 전용 API입니다.

- **Base URL**: `http://localhost:8080/api/admin`
- **인증**: JWT Bearer Token (ROLE_ADMIN 권한 필요)
- **Content-Type**: `application/json`

---

## 인증

모든 Admin API는 `ROLE_ADMIN` 권한이 필요합니다.

### Admin 계정 생성

애플리케이션 시작 시 `AdminUserInitializer`가 자동으로 Admin 계정을 생성합니다 (prod 프로필 제외).

**기본 계정 정보:**
- **Username**: `admin@admin.com`
- **Password**: `password`
- **Role**: `ADMIN`

### 로그인

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin@admin.com",
  "password": "password"
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "access": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

모든 Admin API 요청 시 `Authorization: Bearer {access_token}` 헤더를 포함해야 합니다.

---

## Dashboard

### 대시보드 통계 조회

전체 제출 현황 통계를 조회합니다.

**요청:**
```http
GET /api/admin/dashboard/summary
Authorization: Bearer {access_token}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "totalSubmissions": 120,
    "pending": 20,
    "approved": 80,
    "rejected": 10,
    "deleted": 10
  }
}
```

**응답 코드:**
- `200 OK`: 성공
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: Admin 권한 없음

---

## Submission 관리

### 1. 제출 데이터 생성

Admin이 직접 제출 데이터를 생성합니다.

**요청:**
```http
POST /api/admin/submissions
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "siteName": "포항 해안가",
  "activityType": "TRANSPLANT",
  "submittedAt": "2025-01-15T09:00:00",
  "authorName": "홍길동",
  "authorEmail": "hong@example.com",
  "feedbackText": "깨끗해진 바다가 뿌듯합니다",
  "latitude": 36.0322,
  "longitude": 129.3650,
  "basicEnv": {
    "recordDate": "2025-01-15",
    "startTime": "09:00:00",
    "endTime": "12:30:00",
    "waterTempC": 18.5,
    "visibilityM": 15.0,
    "depthM": 10.5,
    "currentState": "MEDIUM",
    "weather": "SUNNY"
  },
  "participants": {
    "leaderName": "홍길동",
    "participantCount": 5,
    "role": "CITIZEN_DIVER"
  },
  "activity": {
    "type": "TRANSPLANT",
    "details": "이식 작업을 수행했습니다. 총 50개를 이식했습니다.",
    "collectionAmount": 50.0,
    "durationHours": 3.5
  },
  "attachments": [
    {
      "fileName": "photo1.jpg",
      "fileUrl": "public/user_objects/2025-01-15/photo1.jpg",
      "mimeType": "image/jpeg",
      "fileSize": 512000
    }
  ]
}
```

**Request Body 필드:**
- `siteName` (필수): 현장명
- `activityType` (필수): 활동유형 (`TRANSPLANT`, `TRASH_COLLECTION`, `RESEARCH`, `MONITORING`, `OTHER`)
- `submittedAt` (선택): 제출일시 (없으면 현재 시간 사용)
- `authorName` (필수): 작성자명
- `authorEmail` (선택): 작성자 이메일
- `feedbackText` (선택): 활동 후기
- `latitude` (선택): 위도
- `longitude` (선택): 경도
- `basicEnv` (선택): 기본환경 정보
- `participants` (선택): 참여자 정보
- `activity` (필수): 활동 정보
- `attachments` (선택): 첨부파일 목록

**응답:**
```json
{
  "success": true,
  "data": {
    "submissionId": 6,
    "siteName": "포항 해안가",
    "status": "PENDING",
    ...
  }
}
```

**응답 코드:**
- `200 OK`: 생성 성공
- `400 Bad Request`: 필수 필드 누락 또는 형식 오류
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: Admin 권한 없음

---

### 2. 제출 목록 조회

제출 목록을 페이지네이션과 함께 조회합니다. 검색, 필터링, 정렬을 지원합니다.

**요청:**
```http
GET /api/admin/submissions?keyword=포항&status=PENDING&page=0&size=20&sortBy=submittedAt&sortDir=DESC
Authorization: Bearer {access_token}
```

**Query Parameters:**
- `keyword` (optional): 검색 키워드 (현장명, 작성자)
- `status` (optional): 상태 필터 (`PENDING`, `APPROVED`, `REJECTED`, `DELETED`)
- `activityType` (optional): 활동 유형 (`TRANSPLANT`, `TRASH_COLLECTION`, `RESEARCH`, `MONITORING`, `OTHER`)
- `startDate` (optional): 시작 날짜 (ISO 8601 형식: `2025-01-01T00:00:00`)
- `endDate` (optional): 종료 날짜 (ISO 8601 형식: `2025-01-31T23:59:59`)
- `page` (default: 0): 페이지 번호
- `size` (default: 20): 페이지 크기
- `sortBy` (default: `submittedAt`): 정렬 필드
- `sortDir` (default: `DESC`): 정렬 방향 (`ASC`, `DESC`)

**응답:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "submissionId": 1,
        "siteName": "포항 해안가",
        "activityType": "TRANSPLANT",
        "submittedAt": "2025-01-15T09:00:00",
        "status": "PENDING",
        "authorName": "홍길동",
        "attachmentCount": 3,
        "feedbackText": "깨끗해진 바다가 뿌듯합니다"
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

---

### 3. 제출 상세 조회

특정 제출 데이터의 상세 정보를 조회합니다.

**요청:**
```http
GET /api/admin/submissions/{submissionId}
Authorization: Bearer {access_token}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "submissionId": 1,
    "siteName": "포항 해안가",
    "activityType": "TRANSPLANT",
    "submittedAt": "2025-01-15T09:00:00",
    "status": "PENDING",
    "authorName": "홍길동",
    "authorEmail": "hong@example.com",
    "attachmentCount": 3,
    "feedbackText": "깨끗해진 바다가 뿌듯합니다",
    "latitude": 36.0322,
    "longitude": 129.3650,
    "basicEnv": {
      "recordDate": "2025-01-15",
      "startTime": "09:00:00",
      "endTime": "12:30:00",
      "waterTempC": 18.5,
      "visibilityM": 15.0,
      "depthM": 10.5,
      "currentState": "MEDIUM",
      "weather": "SUNNY"
    },
    "participants": {
      "leaderName": "홍길동",
      "participantCount": 5,
      "role": "CITIZEN_DIVER"
    },
    "activity": {
      "type": "TRANSPLANT",
      "details": "이식 작업을 수행했습니다. 총 50개를 이식했습니다.",
      "collectionAmount": 50.0,
      "durationHours": 3.5
    },
    "attachments": [
      {
        "attachmentId": 1,
        "fileName": "photo1.jpg",
        "fileUrl": "public/user_objects/2025-01-15/photo1.jpg",
        "mimeType": "image/jpeg",
        "fileSize": 512000,
        "uploadedAt": "2025-01-15T09:00:00"
      }
    ],
    "rejectReason": null,
    "auditLogs": [
      {
        "logId": 1,
        "action": "SUBMITTED",
        "performedBy": "system",
        "comment": null,
        "createdAt": "2025-01-15T09:00:00"
      }
    ],
    "createdAt": "2025-01-15T09:00:00",
    "modifiedAt": "2025-01-15T09:00:00"
  }
}
```

**응답 코드:**
- `200 OK`: 성공
- `404 Not Found`: 제출 데이터를 찾을 수 없음

---

### 4. 검수 로그 조회

특정 제출 데이터의 검수 이력을 조회합니다.

**요청:**
```http
GET /api/admin/submissions/{submissionId}/logs
Authorization: Bearer {access_token}
```

**응답:**
```json
{
  "success": true,
  "data": [
    {
      "logId": 1,
      "action": "SUBMITTED",
      "performedBy": "system",
      "comment": null,
      "createdAt": "2025-01-15T09:00:00"
    },
    {
      "logId": 2,
      "action": "APPROVED",
      "performedBy": "admin@oceancampus.kr",
      "comment": null,
      "createdAt": "2025-01-16T10:00:00"
    }
  ]
}
```

---

### 5. 단건 승인

특정 제출 데이터를 승인합니다.

**요청:**
```http
POST /api/admin/submissions/{submissionId}/approve
Authorization: Bearer {access_token}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "submissionId": 1,
    "status": "APPROVED",
    ...
  }
}
```

**응답 코드:**
- `200 OK`: 성공
- `404 Not Found`: 제출 데이터를 찾을 수 없음
- `409 Conflict`: 이미 승인/반려/삭제된 제출

---

### 6. 단건 반려

특정 제출 데이터를 반려합니다.

**요청:**
```http
POST /api/admin/submissions/{submissionId}/reject
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "reason": {
    "templateCode": "PHOTO_INSUFFICIENT",
    "message": "사진이 부족합니다. 최소 3장 이상의 사진을 첨부해주세요."
  }
}
```

**Request Body:**
```json
{
  "reason": {
    "templateCode": "PHOTO_INSUFFICIENT",  // 선택사항
    "message": "사진이 부족합니다. 최소 3장 이상의 사진을 첨부해주세요."  // 필수
  }
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "submissionId": 1,
    "status": "REJECTED",
    "rejectReason": "사진이 부족합니다. 최소 3장 이상의 사진을 첨부해주세요.",
    ...
  }
}
```

**응답 코드:**
- `200 OK`: 성공
- `404 Not Found`: 제출 데이터를 찾을 수 없음
- `409 Conflict`: 이미 승인/반려/삭제된 제출
- `422 Unprocessable Entity`: 반려 사유가 공란

---

### 7. 단건 삭제

특정 제출 데이터를 영구 삭제합니다.

**요청:**
```http
DELETE /api/admin/submissions/{submissionId}
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "reason": "중복 테스트 데이터"  // 선택사항
}
```

**응답:**
- `204 No Content`: 삭제 성공

**응답 코드:**
- `204 No Content`: 삭제 성공
- `404 Not Found`: 제출 데이터를 찾을 수 없음

---

### 8. 일괄 승인

여러 제출 데이터를 한 번에 승인합니다.

**요청:**
```http
POST /api/admin/submissions/bulk/approve
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "ids": [1, 2, 3]
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "approved": [1, 2],
    "skipped": [3]
  }
}
```

---

### 9. 일괄 반려

여러 제출 데이터를 한 번에 반려합니다.

**요청:**
```http
POST /api/admin/submissions/bulk/reject
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "ids": [1, 2],
  "reason": {
    "templateCode": "PHOTO_INSUFFICIENT",
    "message": "사진이 부족합니다."
  }
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "rejected": [1, 2],
    "conflicts": []
  }
}
```

---

### 10. 일괄 삭제

여러 제출 데이터를 한 번에 영구 삭제합니다.

**요청:**
```http
DELETE /api/admin/submissions/bulk
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "ids": [1, 2, 3],
  "reason": "테스트 데이터"  // 선택사항
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "deleted": [1, 2],
    "failed": [3]
  }
}
```

---

## Export 기능

### 1. 데이터 내보내기 (CSV 다운로드)

승인된 제출 데이터를 CSV 파일로 내보냅니다.

**요청:**
```http
POST /api/admin/exports/download
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "format": "CSV",
  "filters": {
    "dateFrom": "2025-01-01",
    "dateTo": "2025-01-31"
  }
}
```

**Request Body:**
```json
{
  "format": "CSV",  // 필수: "CSV" 또는 "XLSX" (현재는 CSV만 지원)
  "filters": {  // 선택사항
    "dateFrom": "2025-01-01",  // 시작 날짜 (YYYY-MM-DD)
    "dateTo": "2025-01-31"     // 종료 날짜 (YYYY-MM-DD)
  }
}
```

**응답:**
- Content-Type: `text/csv; charset=UTF-8`
- Content-Disposition: `attachment; filename="submissions_export_20250105_143022.csv"`
- 파일이 바로 다운로드됩니다.

**CSV 컬럼:**
- 제출ID, 현장명, 활동유형, 제출일, 작성자, 이메일, 위도, 경도
- 수심(m), 수온(°C), 시야(m), 날씨, 조류상태
- 참여인원, 대표자명, 역할
- 세부내용, 수거량, 활동후기, 첨부파일수

**응답 코드:**
- `200 OK`: 파일 다운로드 성공
- `400 Bad Request`: 요청 형식 오류 (format 누락 등)
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: Admin 권한 없음

---

### 2. 내보내기 이력 조회

내보내기 작업 이력을 조회합니다.

**요청:**
```http
GET /api/admin/exports
Authorization: Bearer {access_token}
```

**응답:**
```json
{
  "success": true,
  "data": [
    {
      "jobId": 1,
      "requestedBy": "admin@admin.com",
      "format": "CSV",
      "status": "READY",
      "downloadUrl": null,
      "createdAt": "2025-01-05T14:30:22",
      "completedAt": "2025-01-05T14:30:23",
      "filtersJson": "{\"dateFrom\":\"2025-01-01\",\"dateTo\":\"2025-01-31\"}"
    }
  ]
}
```

---

## 공통 응답 형식

### 성공 응답

```json
{
  "success": true,
  "data": { ... }
}
```

### 에러 응답

```json
{
  "success": false,
  "errors": {
    "message": "에러 메시지",
    "code": "ERROR_CODE"
  },
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

---

## 에러 코드

### Admin 관련 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `AD001` | 404 Not Found | 제출 데이터를 찾을 수 없습니다 |
| `AD002` | 409 Conflict | 이미 승인된 제출입니다 |
| `AD003` | 409 Conflict | 이미 반려된 제출입니다 |
| `AD004` | 409 Conflict | 이미 삭제된 제출입니다 |
| `AD005` | 422 Unprocessable Entity | 반려 사유를 입력해주세요 |
| `AD006` | 404 Not Found | 내보내기 작업을 찾을 수 없습니다 |
| `AD007` | 422 Unprocessable Entity | 내보내기 파일이 아직 준비되지 않았습니다 |
| `AD008` | 500 Internal Server Error | 내보내기 생성에 실패했습니다 |

---

## Enum 값

### SubmissionStatus
- `PENDING`: 검수 대기
- `APPROVED`: 승인됨
- `REJECTED`: 반려됨
- `DELETED`: 삭제됨

### ActivityType
- `TRANSPLANT`: 이식
- `TRASH_COLLECTION`: 폐기물수거
- `RESEARCH`: 연구
- `MONITORING`: 모니터링
- `OTHER`: 기타

### CurrentState
- `LOW`: 약한 조류
- `MEDIUM`: 보통 조류
- `HIGH`: 강한 조류

### Weather
- `SUNNY`: 맑음
- `CLOUDY`: 흐림
- `RAINY`: 비
- `WINDY`: 바람
- `OTHER`: 기타

### ParticipantRole
- `CITIZEN_DIVER`: 시민 다이버
- `RESEARCHER`: 연구자
- `LOCAL_MANAGER`: 지역 관리자
- `OTHER`: 기타

### AuditAction
- `SUBMITTED`: 제출됨
- `APPROVED`: 승인됨
- `REJECTED`: 반려됨
- `DELETED`: 삭제됨
- `COMMENT`: 코멘트

### ExportFormat
- `CSV`: CSV 형식
- `XLSX`: Excel 형식 (향후 지원 예정)

### ExportStatus
- `PROCESSING`: 처리 중
- `READY`: 준비 완료
- `FAILED`: 실패

---

## 자동 초기화

### AdminUserInitializer

애플리케이션 시작 시 (`!prod` 프로필) Admin 계정을 자동으로 생성합니다.

- **위치**: `com.ocean.piuda.admin.initializer.AdminUserInitializer`
- **계정**: `admin@admin.com` / `password`
- **실행 순서**: `@Order(0)`

### SubmissionDataInitializer

애플리케이션 시작 시 (`!prod` 프로필) 테스트용 Submission 데이터를 자동 생성합니다.

- **위치**: `com.ocean.piuda.admin.submission.initializer.SubmissionDataInitializer`
- **데이터**: 승인/대기/반려 상태의 샘플 데이터 5건
- **실행 순서**: `@Order(1)`

---

## 테스트 가이드

자세한 테스트 가이드는 `EXPORT_TEST_GUIDE.md`를 참조하세요.

### 빠른 테스트 체크리스트

1. 애플리케이션 실행
2. Admin 로그인하여 Access Token 획득
3. Dashboard 통계 조회
4. Submission 목록 조회
5. Submission 상세 조회
6. Submission 승인/반려/삭제 테스트
7. Export CSV 다운로드 테스트

---

## 참고사항

- 모든 날짜/시간은 ISO 8601 형식을 사용합니다 (예: `2025-01-15T09:00:00`)
- 이미지 파일 URL은 현재 DB에 저장된 S3 Key를 그대로 반환합니다
- Export는 승인된 데이터만 내보냅니다
- 일괄 처리 시 일부 실패해도 나머지는 처리됩니다 (응답의 `skipped`, `conflicts`, `failed` 필드 확인)

