# Export 기능 테스트 가이드

## 사전 준비

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
# 또는
./gradlew clean build && java -jar build/libs/*.jar
```

### 2. Admin 계정 확인
기본 Admin 계정이 **자동으로 생성**됩니다:
- **Username**: `admin@admin.com`
- **Password**: `password`

> `AdminUserInitializer`가 애플리케이션 시작 시 자동으로 생성합니다 (prod 프로필 제외)
> 
> 만약 계정이 없다면 애플리케이션 로그에서 "Admin 계정 생성 완료" 메시지를 확인하세요.

---

## 테스트 단계

### Step 1: Admin 로그인하여 Access Token 획득

**요청:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin@admin.com",
  "password": "password"
}
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "access": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Access Token 저장:**
- 응답의 `data.access` 값을 복사하여 변수 `ACCESS_TOKEN`에 저장

---

### Step 2: 승인된 Submission 데이터 확인 (선택사항)

**요청:**
```http
GET http://localhost:8080/api/admin/submissions?status=APPROVED
Authorization: Bearer {ACCESS_TOKEN}
```

> **주의**: Export는 `APPROVED` 상태의 데이터만 내보냅니다.
> 승인된 데이터가 없다면 먼저 제출 데이터를 승인해주세요.
> 
> 테스트 데이터는 `SubmissionDataInitializer`가 자동 생성합니다 (prod 프로필 제외).

---

### Step 3: Export 파일 다운로드

#### 3-1. 전체 승인 데이터 내보내기

**요청:**
```http
POST http://localhost:8080/api/admin/exports/download
Authorization: Bearer {ACCESS_TOKEN}
Content-Type: application/json

{
  "format": "CSV"
}
```

**또는 날짜 필터 포함:**
```http
POST http://localhost:8080/api/admin/exports/download
Authorization: Bearer {ACCESS_TOKEN}
Content-Type: application/json

{
  "format": "CSV",
  "filters": {
    "dateFrom": "2025-01-01",
    "dateTo": "2025-01-31"
  }
}
```

**응답:**
- Content-Type: `application/octet-stream`
- Content-Disposition: `attachment; filename="submissions_export_20250105_143022.csv"`
- 파일이 바로 다운로드됩니다.

**CSV 파일 내용 예시:**
```csv
제출ID,현장명,활동유형,제출일,작성자,이메일,위도,경도,수심(m),수온(°C),시야(m),날씨,조류상태,참여인원,대표자명,역할,세부내용,수거량,활동후기,첨부파일수
2,부산 송도해수욕장,TRASH_COLLECTION,2025-01-10T10:00:00,김철수,kim@example.com,35.0784,129.0756,20.0,20.0,5.0,SUNNY,LOW,3,김철수,CITIZEN_DIVER,플라스틱 병 30개 캔 15개를 수거했습니다.,45.0,해변 청소 활동에 참여했습니다,2
5,전라남도 여수 해수욕장,TRASH_COLLECTION,2025-01-07T09:30:00,최지영,choi@example.com,34.7604,127.6622,18.0,19.0,7.0,SUNNY,MEDIUM,6,최지영,CITIZEN_DIVER,쓰레기 100개 수거 완료,100.0,여수 바다가 아름답습니다,5
```

---

### Step 4: Export 이력 조회

**요청:**
```http
GET http://localhost:8080/api/admin/exports
Authorization: Bearer {ACCESS_TOKEN}
```

**응답 예시:**
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
      "filtersJson": "{}"
    }
  ]
}
```

---

## Postman Collection 설정

### 1. Environment Variables 설정
```
ACCESS_TOKEN = (로그인 후 자동 설정)
BASE_URL = http://localhost:8080
```

### 2. Pre-request Script (로그인 요청에)
```javascript
// 로그인 후 토큰 자동 저장
pm.test("Save access token", function () {
    var jsonData = pm.response.json();
    if (jsonData.success && jsonData.data.access) {
        pm.environment.set("ACCESS_TOKEN", jsonData.data.access);
    }
});
```

### 3. Collection 순서
1. **Login** - POST `/api/auth/login`
2. **Get Approved Submissions** - GET `/api/admin/submissions?status=APPROVED` (선택)
3. **Export Download (All)** - POST `/api/admin/exports/download`
4. **Export Download (Filtered)** - POST `/api/admin/exports/download` (필터 포함)
5. **Export History** - GET `/api/admin/exports`

---

## curl 명령어로 테스트

### 1. 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@admin.com",
    "password": "password"
  }'
```

### 2. Export 다운로드 (토큰 저장)
```bash
# 1. 로그인 후 토큰 저장
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@admin.com","password":"password"}' \
  | jq -r '.data.access')

# 2. Export 다운로드
curl -X POST http://localhost:8080/api/admin/exports/download \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"format":"CSV"}' \
  --output submissions_export.csv

# 3. 파일 확인
head -5 submissions_export.csv
```

---

## 테스트 시나리오

### ✅ 성공 케이스
1. **전체 데이터 내보내기**
   - 필터 없이 요청
   - 모든 승인된 데이터가 CSV로 다운로드

2. **날짜 필터 적용**
   - `dateFrom`, `dateTo` 설정
   - 해당 기간의 승인된 데이터만 내보냄

3. **이력 확인**
   - 다운로드 후 이력 조회
   - `status: READY`, `completedAt` 확인

### ⚠️ 주의사항
1. **승인된 데이터가 없는 경우**
   - CSV는 헤더만 포함된 빈 파일 다운로드

2. **권한 확인**
   - `ROLE_ADMIN` 권한이 필요합니다
   - 일반 사용자는 403 Forbidden

3. **대용량 데이터**
   - 메모리에 모든 데이터를 로드하므로 매우 큰 데이터셋은 성능 이슈가 있을 수 있습니다

---

## 문제 해결

### 401 Unauthorized
- Access Token이 없거나 만료됨
- 다시 로그인하여 토큰 재발급

### 403 Forbidden
- Admin 권한이 없는 사용자
- `ROLE_ADMIN` 권한 확인
- Admin 계정이 생성되었는지 확인 (애플리케이션 로그 확인)

### 빈 CSV 파일
- 승인된 Submission 데이터가 없음
- `/api/admin/submissions`에서 승인 처리 필요
- 테스트 데이터가 생성되었는지 확인

### 파일명 인코딩 문제
- 브라우저에 따라 한글 파일명이 깨질 수 있음
- Postman에서는 정상 다운로드됨

---

## 예상 응답 코드

| HTTP Status | 설명 |
|------------|------|
| 200 OK | CSV 파일 다운로드 성공 |
| 400 Bad Request | 요청 형식 오류 (format 누락 등) |
| 401 Unauthorized | 인증 토큰 없음 |
| 403 Forbidden | Admin 권한 없음 |
| 500 Internal Server Error | 서버 오류 |

---

## 자동 초기화 정보

### AdminUserInitializer
- **위치**: `com.ocean.piuda.admin.initializer.AdminUserInitializer`
- **실행 조건**: `!prod` 프로필 (프로덕션 제외)
- **기능**: Admin 계정 자동 생성
- **실행 순서**: `@Order(0)` - 다른 초기화보다 먼저 실행

### SubmissionDataInitializer
- **위치**: `com.ocean.piuda.admin.submission.initializer.SubmissionDataInitializer`
- **실행 조건**: `!prod` 프로필 (프로덕션 제외)
- **기능**: 테스트용 Submission 데이터 자동 생성 (승인/대기/반려 상태 포함)
- **실행 순서**: `@Order(1)` - Admin 계정 생성 후 실행

---

## 빠른 테스트 체크리스트

- [ ] 애플리케이션 실행
- [ ] 로그에서 "Admin 계정 생성 완료" 확인
- [ ] 로그에서 "Submission 테스트 데이터 생성 완료" 확인
- [ ] Admin 로그인하여 Access Token 획득
- [ ] Export API 호출하여 CSV 다운로드
- [ ] 다운로드된 CSV 파일 내용 확인
- [ ] Export 이력 조회 확인
