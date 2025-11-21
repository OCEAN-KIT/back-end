# Mission Module Manual

이 문서는 `com.ocean.piuda.mission` 패키지에 구현된 REST API를 로컬에서 빠르게 검증하기 위한 안내서입니다.

## 1. 선행 조건
- JDK 17 이상
- Spring Boot 3.5.x (이미 프로젝트에 포함)
- 로컬 DB(H2 인메모리 또는 MySQL). `application.yml`에서 사용하는 프로필을 확인하세요.

### 애플리케이션 실행
```bash
./gradlew bootRun
```

## 2. 공통 정보
- Base URL: `http://localhost:8080/api/missions`
- 모든 응답은 JSON
- `FakeCurrentUserProvider`가 기본 ADMIN/USER를 결정하므로, 필요 시 해당 클래스에서 반환값을 바꿔 권한 시나리오를 테스트할 수 있습니다.

## 3. API 테스트 가이드

### 3.1 미션 생성 (ADMIN만)
```bash
curl -X POST http://localhost:8080/api/missions \
  -H "Content-Type: application/json" \
  -d '{
        "title": "월포 해변 생태 조사",
        "targetBioGroup": "FISH",
        "pointId": 1001,
        "description": "다이버 모집",
        "regionName": "포항 월포",
        "startDate": "2025-12-01",
        "endDate": "2025-12-31",
        "status": "PLANNED",
        "coverMediaUrl": "https://example.com/cover.png"
      }'
```

### 3.2 단건 조회 (권한 제한 없음)
```bash
curl http://localhost:8080/api/missions/1
```

### 3.3 목록 조회 + 필터
```bash
curl "http://localhost:8080/api/missions?status=ACTIVE&targetBioGroup=FISH&regionName=포항&page=0&size=10"
```

### 3.4 부분 수정 (ADMIN 또는 owner)
```bash
curl -X PATCH http://localhost:8080/api/missions/1 \
  -H "Content-Type: application/json" \
  -d '{ "status": "ACTIVE", "coverMediaUrl": "https://example.com/active.png" }'
```

### 3.5 삭제 (ADMIN 또는 owner)
```bash
curl -X DELETE http://localhost:8080/api/missions/1
```

## 4. 권한 시나리오 확인
1. `FakeCurrentUserProvider#getCurrentUserRole()`를 `Role.USER`로 변경.
2. POST/PATCH/DELETE 호출 시 `MissionAccessDeniedException`이 발생하는지 확인.
3. OWNER 권한 테스트: `getCurrentUserId()` 값을 미션의 `ownerId`로 맞추고 PATCH/DELETE 재시도.

## 5. 예외 동작
- 없는 ID 조회/수정/삭제 → `404 MissionNotFoundException`
- 권한 위반 → `403 MissionAccessDeniedException`

## 6. 추가 확인 포인트
- DTO 검증 실패 시 Spring 기본 `MethodArgumentNotValidException` 사용
- `MissionSearchCondition`으로 status/bioGroup/regionName 조합 필터링
- `Mission` 엔티티는 `BaseEntity`를 상속하여 `createdAt/modifiedAt` 자동 관리 (JPA Auditing 활성화 필요)

