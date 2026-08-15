# API 명세서

> **Base URL:** `http://localhost:8080`
> **인증:** JWT Bearer 토큰 (`Authorization: Bearer <token>`)
> **Content-Type:** `application/json`

> ⚠️ **이 문서는 스냅샷입니다.** 코드가 바뀌면 이 문서는 갱신되기 전까지 실제 동작과 어긋날 수 있습니다.
> 항상 최신 상태를 보장하는 건 서버가 직접 코드에서 생성하는 **Swagger UI**입니다.
> 서버 실행 후 `http://localhost:8080/swagger-ui.html` 에서 실제 엔드포인트·요청/응답 스키마를 확인하세요.
> 이 문서는 전체 흐름을 빠르게 파악하기 위한 보조 자료로 활용하는 것을 권장합니다.

---

## 목차

1. [인증 API](#1-인증-api)
2. [회원 API](#2-회원-api)
3. [공연 API](#3-공연-api)
4. [팀 API](#4-팀-api)
5. [연습 일정 API](#5-연습-일정-api)
6. [공통 에러 응답 형식](#공통-에러-응답-형식)

---

## 1. 인증 API

### 1-1. 회원가입

**POST** `/api/auth/signup`

**인증 불필요**

**Request Body**
```json
{
  "email": "hong@example.com",
  "password": "password1234",
  "passwordConfirm": "password1234",
  "nickname": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| email | String | O | 이메일 형식 |
| password | String | O | 최소 8자 |
| passwordConfirm | String | O | password와 일치해야 함 |
| nickname | String | O | 비어있으면 안 됨 |
| phoneNumber | String | O | `010-1234-5678` 형식 |

가입 직후 계정 상태는 `signupStatus = REQUESTED`이며, **관리자 승인 전까지 로그인할 수 없습니다.**

**성공 응답** `200 OK`
```
회원가입 요청 성공
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | 이메일 형식 오류, 비밀번호 8자 미만, 닉네임/전화번호 빈값 또는 형식 오류 |
| 400 | U009 | password와 passwordConfirm 불일치 |
| 409 | U003 | 이미 가입된 이메일 |

---

### 1-2. 로그인

**POST** `/api/auth/login`

**인증 불필요**

**Request Body**
```json
{
  "email": "hong@example.com",
  "password": "password1234"
}
```

**성공 응답** `200 OK`
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci..."
}
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | 이메일/비밀번호 빈값 |
| 401 | U004 | 존재하지 않는 이메일 또는 비밀번호 불일치 |
| 403 | U005 | 회원가입이 거절된 계정 |
| 403 | U006 | 아직 관리자 승인 전인 계정 |

---

### 1-3. 토큰 재발급

**POST** `/api/auth/reissue`

**Header:** `Refresh-Token: <refreshToken>`

Refresh Token Rotation(RTR) 방식으로, 재발급 시 기존 리프레시 토큰은 즉시 무효화됩니다.

**성공 응답** `200 OK`
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci..."
}
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 401 | U007 | 만료되었거나 유효하지 않은 리프레시 토큰 |
| 401 | U008 | 저장된 토큰과 불일치 (이미 사용되었거나 재발급된 토큰) |

---

### 1-4. 로그아웃

**POST** `/api/auth/logout`

**인증 필요**

현재 로그인된 사용자의 리프레시 토큰을 `null`로 무효화합니다.

**성공 응답** `200 OK`
```
로그아웃 성공
```

---

### 1-5. 회원가입 요청 목록 조회

**GET** `/api/auth/signup-requests`

**권한:** ADMIN

`APPROVED` 상태가 아닌(즉 `REQUESTED` 또는 `REJECTED`) 회원가입 요청들을 조회합니다.

**Query Parameters:** `page`, `size` (기본 size=10, 정렬은 생성일 내림차순 고정)

**성공 응답** `200 OK`
```json
{
  "content": [
    {
      "id": 5,
      "email": "new@example.com",
      "nickName": "신입회원",
      "signupStatus": "REQUESTED"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |

---

### 1-6. 회원가입 승인

**PATCH** `/api/auth/{requestedId}/approve`

**권한:** ADMIN

> 경로가 `/api/auth/signup-requests/...`가 아니라 `/api/auth/{requestedId}/approve` 임에 주의하세요.

**성공 응답** `200 OK`
```
회원가입 승인 성공
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |
| 404 | U001 | 존재하지 않는 유저 ID |

---

### 1-7. 회원가입 거절

**PATCH** `/api/auth/{requestedId}/reject`

**권한:** ADMIN

**성공 응답** `200 OK`
```
회원가입 거절 성공
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |
| 404 | U001 | 존재하지 않는 유저 ID |

---

## 2. 회원 API

### 2-1. 내 정보 조회

**GET** `/api/users/me`

**권한:** 인증 필요

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "email": "hong@example.com",
  "nickName": "홍길동",
  "role": "USER",
  "teamIds": [1, 3],
  "teamNames": ["스트릿댄스팀", "코레오팀"]
}
```

---

### 2-2. 회원 검색

**GET** `/api/users/search?nickname={nickname}&email={email}&signupStatus={status}`

**권한:** ADMIN

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| nickname | String | X | 닉네임 부분 일치 검색 |
| email | String | X | 이메일 부분 일치 검색 |
| signupStatus | String | X | `REQUESTED` / `APPROVED` / `REJECTED` |
| page, size | int | X | 페이지네이션 (기본 size=10, 생성일 내림차순 고정) |

**성공 응답** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "email": "hong@example.com",
      "nickName": "홍길동",
      "phoneNumber": "010-1234-5678",
      "role": "USER",
      "signupStatus": "APPROVED",
      "createdAt": "2026-03-01T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |

---

### 2-3. 회원 삭제

**DELETE** `/api/users/{id}`

**권한:** ADMIN

관리자가 특정 회원을 삭제합니다. 삭제 시 해당 회원의 게시글, 연습 신청, 팀 멤버십이 함께 삭제됩니다.

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |
| 403 | U010 | 본인 계정을 삭제하려고 시도함 |
| 404 | U001 | 존재하지 않는 회원 ID |
| 409 | U011 | 삭제 대상이 팀장으로 있는 팀이 존재함 (먼저 팀장을 위임해야 함) |

---

## 3. 공연 API

> 공연 생성/삭제는 **ADMIN** 권한 필요. 목록/단건 조회는 인증 없이 가능합니다.

### 3-1. 공연 생성

**POST** `/api/performances`

**권한:** ADMIN

**Request Body**
```json
{
  "name": "2026 봄 정기공연",
  "performanceDate": "2026-05-20",
  "description": "학내 대강당에서 진행되는 정기 공연입니다."
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| name | String | O | 비어있으면 안 됨 |
| performanceDate | String | O | `YYYY-MM-DD` 형식 |
| description | String | X | — |

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "name": "2026 봄 정기공연",
  "performanceDate": "2026-05-20",
  "description": "학내 대강당에서 진행되는 정기 공연입니다.",
  "teams": [],
  "createdAt": "2026-03-01T10:00:00"
}
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | name 빈값, performanceDate 누락 또는 형식 오류 |
| 401 | C003 | 토큰 없음 또는 만료 |
| 403 | U002 | 관리자 권한 없음 |

---

### 3-2. 공연 목록 조회

**GET** `/api/performances`

**인증 불필요**

**성공 응답** `200 OK` — `PerformanceResponse` 배열 (각 공연에 등록된 `teams` 목록 포함)

---

### 3-3. 공연 단건 조회

**GET** `/api/performances/{id}`

**인증 불필요**

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 404 | PF001 | 존재하지 않는 공연 ID |

---

### 3-4. 공연 삭제

**DELETE** `/api/performances/{id}`

**권한:** ADMIN

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |
| 404 | PF001 | 존재하지 않는 공연 ID |

---

## 4. 팀 API

> **팀은 반드시 하나의 공연(Performance)에 소속됩니다.** 공연마다 팀을 새로 만들어야 합니다.
> 팀 생성/제거 및 멤버 관리는 **ADMIN** 권한 필요. 목록/상세 조회는 인증 없이 가능합니다.

### 4-1. 팀 생성

**POST** `/api/teams`

**권한:** ADMIN

**Request Body**
```json
{
  "performanceId": 1,
  "name": "스트릿댄스팀"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| performanceId | Long | O | 존재하는 공연 ID |
| name | String | O | 비어있으면 안 됨, 전체 팀 중 중복 불가 |

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "name": "스트릿댄스팀",
  "createdAt": "2026-03-01T10:00:00"
}
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | name 빈값 |
| 403 | U002 | 관리자 권한 없음 |
| 404 | T003 | 존재하지 않는 performanceId |
| 409 | T002 | 이미 존재하는 팀 이름 |

---

### 4-2. 팀 목록 조회

**GET** `/api/teams`

**인증 불필요**

**성공 응답** `200 OK`
```json
[
  { "id": 1, "name": "스트릿댄스팀", "createdAt": "2026-03-01T10:00:00" },
  { "id": 2, "name": "코레오팀", "createdAt": "2026-03-01T10:05:00" }
]
```

---

### 4-3. 팀 상세 조회 (멤버 포함)

**GET** `/api/teams/{id}`

**인증 불필요**

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "name": "스트릿댄스팀",
  "createdAt": "2026-03-01T10:00:00",
  "members": [
    { "id": 1, "userId": 2, "nickname": "김팀장", "role": "LEADER", "createdAt": "2026-03-01T10:10:00" },
    { "id": 2, "userId": 3, "nickname": "이부팀장", "role": "DEPUTY", "createdAt": "2026-03-01T10:11:00" }
  ]
}
```

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 404 | T001 | 존재하지 않는 팀 ID |

---

### 4-4. 팀원 추가

**POST** `/api/teams/{id}/members`

**권한:** ADMIN

**Request Body**
```json
{
  "userId": 2,
  "role": "LEADER"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| userId | Long | O | 존재하는 유저 ID |
| role | String | O | `LEADER` / `DEPUTY` / `MEMBER` |

**성공 응답** `200 OK` — `TeamMemberResponse`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | userId 또는 role 누락 |
| 403 | U002 | 관리자 권한 없음 |
| 404 | T001 | 존재하지 않는 팀 ID |
| 404 | U001 | 존재하지 않는 userId |
| 409 | T004 | 이미 해당 팀에 속해있는 멤버 |
| 409 | T005 | 이미 팀장(LEADER)이 존재하는데 LEADER 추가 시도 |
| 409 | T006 | 이미 부팀장(DEPUTY)이 존재하는데 DEPUTY 추가 시도 |

---

### 4-5. 팀원 역할 변경

**PUT** `/api/teams/{id}/members/{targetUserId}/role`

**권한:** ADMIN

**Request Body**
```json
{
  "role": "DEPUTY"
}
```

**성공 응답** `200 OK` — 수정된 `TeamMemberResponse`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | role 누락 |
| 403 | U002 | 관리자 권한 없음 |
| 404 | T001 | 존재하지 않는 팀 ID |
| 404 | U001 | 존재하지 않는 userId |
| 404 | T007 | 팀 멤버가 아님 |
| 409 | T005 / T006 | 변경하려는 role이 이미 다른 멤버에게 있음 |

---

### 4-6. 팀원 제거

**DELETE** `/api/teams/{id}/members/{targetUserId}`

**권한:** ADMIN

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |
| 404 | T001 | 존재하지 않는 팀 ID |
| 404 | U001 | 존재하지 않는 userId |
| 404 | T007 | 팀 멤버가 아님 |

---

### 4-7. 팀 삭제

**DELETE** `/api/teams/{teamId}`

**권한:** ADMIN

팀 자체를 삭제합니다. (기존 문서에 없던 신규 엔드포인트)

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |
| 404 | T001 | 존재하지 않는 팀 ID |

---

## 5. 연습 일정 API

> `GET /api/schedules`, `GET /api/schedules/{id}`, `GET /api/schedules/team/{teamId}/week` 는 인증 없이 조회 가능합니다.
> 그 외 조회(`GET /api/schedules/team/{teamId}`)를 포함한 나머지 엔드포인트는 모두 인증이 필요합니다.

### 5-1. 연습 일정 신청

**POST** `/api/schedules`

**권한:** 해당 팀의 LEADER 또는 DEPUTY

**Request Body**
```json
{
  "performanceId": 1,
  "teamId": 1,
  "practiceDate": "2026-07-15",
  "startTime": "18:00:00",
  "endTime": "20:00:00",
  "alternativeRoom": "UNDERGROUND_PARKING"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| performanceId | Long | O | 존재하는 공연 ID |
| teamId | Long | O | 존재하는 팀 ID |
| practiceDate | String | O | `YYYY-MM-DD` |
| startTime | String | O | `HH:MM:SS` |
| endTime | String | O | `HH:MM:SS`. `startTime`보다 앞서면 자정을 넘긴 연습(익일 종료)으로 처리됨. `startTime`과 같을 수는 없음 |
| alternativeRoom | String | O | 자동 배정에서 우선 연습실(동방/학생회관 지하/치어룸)을 받지 못했을 때 배정될 예비 연습실 |

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "performanceId": 1,
  "performanceName": "2026 봄 정기공연",
  "teamId": 1,
  "teamName": "스트릿댄스팀",
  "submittedByNickname": "김팀장",
  "startAt": "2026-07-15T18:00:00",
  "endAt": "2026-07-15T20:00:00",
  "assignedRoom": null,
  "status": "PENDING",
  "adminNote": null,
  "createdAt": "2026-07-08T10:00:00",
  "updatedAt": "2026-07-08T10:00:00"
}
```

> `assignedRoom`은 관리자가 배정을 실행하기 전까지 `null`입니다.
> 응답의 시간 필드는 `practiceDate` + `startTime`/`endTime`이 아니라 합쳐진 `startAt`/`endAt`(`LocalDateTime`)으로 내려옵니다.

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | 필수 필드 누락 또는 형식 오류 |
| 400 | S009 | 시작 시간과 종료 시간이 같음 |
| 400 | S008 | 제출 기한이 지났습니다 (이미 시작했거나 지나간 주에 대한 신청) |
| 400 | S010 | 다음 주 연습만 신청할 수 있습니다 (2주 이상 앞선 신청) |
| 404 | PF001 / T001 / U001 | 존재하지 않는 공연/팀/유저 |
| 403 | T009 | 해당 팀의 멤버가 아님 |
| 403 | S002 | MEMBER 역할로 신청 시도 ("팀장 또는 부팀장만 신청할 수 있습니다") |

> ⚠️ 팀별 하루 최대 2시간 제한은 현재 서비스 코드에서 **주석 처리되어 비활성화**되어 있습니다. (`ScheduleService.validateDailyLimit`)

---

### 5-2. 주간 연습 일정 조회

**GET** `/api/schedules?performanceId={id}&weekStart={date}`

**인증 불필요**

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| performanceId | Long | O | 공연 ID |
| weekStart | String | O | 조회할 주의 시작일, `YYYY-MM-DD` (weekStart ~ weekStart+6일 범위) |
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 20, 정렬 기준은 `startAt` 오름차순) |

**성공 응답** `200 OK` — 페이지네이션된 `ScheduleResponse` 목록

---

### 5-3. 공연/팀별 주간 연습 일정 조회

**GET** `/api/schedules/team/{teamId}/week?performanceId={id}&weekStart={date}`

**인증 불필요**

특정 공연에서 특정 팀이 신청한 해당 주(weekStart 기준)의 일정만 조회합니다.

**Query Parameters:** `performanceId`(필수), `weekStart`(필수), `page`, `size`

**성공 응답** `200 OK` — 페이지네이션된 `ScheduleResponse` 목록

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 404 | PF001 / T001 | 존재하지 않는 공연/팀 ID |

---

### 5-4. 연습 일정 단건 조회

**GET** `/api/schedules/{id}`

**인증 불필요**

**성공 응답** `200 OK` — 5-1 응답과 동일한 구조

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 404 | S001 | 존재하지 않는 신청 ID |

---

### 5-5. 팀별 연습 일정 조회 (전체)

**GET** `/api/schedules/team/{teamId}`

**권한:** 해당 팀 멤버 (인증 필요)

주 단위 필터 없이 해당 팀이 신청한 모든 일정을 조회합니다.

**Query Parameters:** `page`, `size` (기본 size=20, `startAt` 내림차순)

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 404 | T001 | 존재하지 않는 팀 ID |
| 403 | T009 | 해당 팀의 멤버가 아님 |

---

### 5-6. 연습 일정 취소

**POST** `/api/schedules/{id}/cancel`

**권한:** 신청한 본인 또는 ADMIN

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 404 | S001 | 존재하지 않는 신청 ID |
| 403 | S003 | 취소 권한 없음 (본인도 ADMIN도 아님) |
| 409 | S004 | 이미 REJECTED 또는 CANCELLED 상태 |

---

### 5-7. 연습 일정 반려 (관리자)

**POST** `/api/schedules/{id}/reject`

**권한:** ADMIN

**Request Body**
```json
{
  "adminNote": "해당 시간대는 다른 팀과 중복됩니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| adminNote | String | X | 반려 사유 |

**성공 응답** `200 OK` — `status: "REJECTED"`로 갱신된 `ScheduleResponse`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |
| 404 | S001 | 존재하지 않는 신청 ID |
| 409 | S005 | PENDING 상태가 아닌 신청 반려 시도 |

---

### 5-8. 연습실 재배정 (관리자)

**PUT** `/api/schedules/{id}/room`

**권한:** ADMIN

**Request Body**
```json
{
  "room": "STUDENT_UNION_BASEMENT"
}
```

| 값 | 설명 |
|----|------|
| `CLUB_ROOM` | 동방 |
| `STUDENT_UNION_BASEMENT` | 학생회관 지하 |
| `UNDERGROUND_PARKING` | 지하 주차장 |
| `EXTERNAL` | 외부 연습실 |
| `CHEER_ROOM` | 치어룸 |

**성공 응답** `200 OK` — 수정된 `ScheduleResponse`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | room 누락 |
| 403 | U002 | 관리자 권한 없음 |
| 404 | S001 | 존재하지 않는 신청 ID |
| 409 | S006 | APPROVED 상태가 아닌 신청에 재배정 시도 |

---

### 5-9. 연습실 수동 배정 (관리자)

**POST** `/api/schedules/assign/manual`

**권한:** ADMIN

관리자가 팀의 연습 일정을 신청 절차 없이 직접 등록하면서 연습실을 지정해 즉시 승인(`APPROVED`) 상태로 생성합니다. 신청자는 해당 팀의 LEADER로 자동 지정됩니다.

**Request Body**
```json
{
  "performanceId": 1,
  "teamId": 1,
  "practiceDate": "2026-07-15",
  "startTime": "18:00:00",
  "endTime": "20:00:00",
  "room": "CLUB_ROOM"
}
```

**성공 응답** `200 OK` — 즉시 `APPROVED` 상태로 생성된 `ScheduleResponse`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 400 | C001 | 필수 필드 누락 |
| 403 | U002 | 관리자 권한 없음 |
| 404 | PF001 / T001 | 존재하지 않는 공연/팀 |
| 404 | T008 | 대상 팀에 팀장(LEADER)이 없음 |

---

### 5-10. 주간 연습실 일괄 자동 배정 (관리자) ⭐

**POST** `/api/schedules/assign?performanceId={id}&weekStart={date}`

**권한:** ADMIN

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| performanceId | Long | O | 공연 ID |
| weekStart | String | O | 배정할 주의 시작일, `YYYY-MM-DD` |

**동작 설명**
- `weekStart` ~ `weekStart+6일` 사이의 `PENDING` 상태 신청들을 `createdAt` 오름차순(선착순)으로 배정합니다.
- 배정 우선순위: 동방(CLUB_ROOM) → 학생회관 지하(STUDENT_UNION_BASEMENT) → 치어룸(CHEER_ROOM, 수요일 18:30~20:30 이내 & 팀 3개 미만) → 신청 시 지정한 `alternativeRoom`
- 같은 팀이 이번 주에 이미 동방을 받았으면 이후 신청은 동방을 건너뜁니다 (팀별 동방 최소 1회 보장).
- 동방/학생회관 지하는 시간이 겹치면 같은 슬롯에 중복 배정하지 않습니다.

**성공 응답** `200 OK` — 배정된 `ScheduleResponse` 목록 (모두 `APPROVED`)

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 403 | U002 | 관리자 권한 없음 |
| 404 | PF001 | 존재하지 않는 performanceId |

---

### 5-11. 연습 일정 삭제

**DELETE** `/api/schedules/{id}`

**권한:** ADMIN 또는 해당 팀의 LEADER

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 코드 | 원인 |
|----------|------|------|
| 404 | S001 | 존재하지 않는 신청 ID |
| 403 | S007 | 삭제 권한 없음 (ADMIN도 해당 팀 LEADER도 아님) |

---

## 전체 플로우 예시

### 시나리오: 회원가입부터 공연 연습 일정 배정까지

```
1. 신규 회원가입 (승인 대기 상태로 생성됨)
   POST /api/auth/signup
   { "email": "hong@example.com", "password": "password1234",
     "passwordConfirm": "password1234", "nickname": "홍길동",
     "phoneNumber": "010-1234-5678" }

2. 관리자 로그인
   POST /api/auth/login

3. 관리자가 회원가입 요청 목록 조회 후 승인
   GET   /api/auth/signup-requests
   PATCH /api/auth/5/approve

4. 공연 생성
   POST /api/performances
   { "name": "2026 봄 정기공연", "performanceDate": "2026-05-20" }

5. 팀 생성 (공연에 소속됨)
   POST /api/teams  { "performanceId": 1, "name": "스트릿댄스팀" }
   POST /api/teams  { "performanceId": 1, "name": "코레오팀" }

6. 팀원 추가
   POST /api/teams/1/members  { "userId": 2, "role": "LEADER" }
   POST /api/teams/1/members  { "userId": 3, "role": "MEMBER" }
   POST /api/teams/2/members  { "userId": 4, "role": "LEADER" }

7. 팀장 로그인 후 연습 신청 (신청 가능 기간은 항상 '다음 주'만 해당)
   POST /api/schedules
   { "performanceId": 1, "teamId": 1, "practiceDate": "2026-07-15",
     "startTime": "18:00:00", "endTime": "20:00:00",
     "alternativeRoom": "UNDERGROUND_PARKING" }

8. 관리자가 배정 실행 (신청 마감 이후)
   POST /api/schedules/assign?performanceId=1&weekStart=2026-07-13

9. 결과 조회
   GET /api/schedules?performanceId=1&weekStart=2026-07-13
```

---

## 공통 에러 응답 형식

모든 비즈니스 예외 및 검증 실패는 `GlobalExceptionHandler`가 가로채 아래의 **구조화된 JSON**으로 응답합니다. (더 이상 Spring 기본 에러 페이지나 무조건적인 500이 아닙니다.)

**형식**
```json
{
  "code": "S008",
  "message": "제출 기한이 지났습니다 (연습일 기준 이전 주 일요일까지 신청 가능합니다)",
  "errors": null
}
```

**Bean Validation 실패 시** (`errors` 필드에 필드별 상세 사유 포함, `400 Bad Request` + 코드 `C001`)
```json
{
  "code": "C001",
  "message": "입력 값이 올바르지 않습니다",
  "errors": [
    { "field": "name", "reason": "must not be blank" }
  ]
}
```

**인증 실패 / 권한 없음**

| 상태코드 | 코드 | 설명 |
|----------|------|------|
| 401 | C003 | 인증이 필요합니다 (JWT 없음/만료 — `JsonAuthenticationEntryPoint`) |
| 403 | C004 | 접근 권한이 없습니다 (`JsonAccessDeniedHandler`) |
| 403 | U002 | 관리자 권한이 필요합니다 (`ErrorCode.ADMIN_ONLY`, 서비스 계층에서 직접 검사) |

**예상치 못한 서버 오류**

| 상태코드 | 코드 | 설명 |
|----------|------|------|
| 500 | C002 | 서버 내부 오류가 발생했습니다 |

> 전체 에러 코드 목록은 `global/exception/ErrorCode.java`를 참고하세요. (도메인 접두사: `C`=공통, `U`=회원/인증, `P`=게시글, `T`=팀, `S`=연습 일정, `PF`=공연)
