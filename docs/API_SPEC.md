# API 명세서

> **Base URL:** `http://localhost:8080`
> **인증:** JWT Bearer 토큰 (`Authorization: Bearer <token>`)
> **Content-Type:** `application/json`

---

## 목차

1. [인증 API](#1-인증-api)
2. [공연 API](#2-공연-api)
3. [팀 API](#3-팀-api)
4. [연습 일정 API](#4-연습-일정-api)

---

## 1. 인증 API

### 1-1. 회원가입

**POST** `/api/auth/signup`

**인증 불필요**

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "홍길동"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| email | String | O | 이메일 형식 |
| password | String | O | 최소 8자 |
| nickname | String | O | 비어있으면 안 됨 |

**성공 응답** `200 OK`
```
회원가입 성공
```

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 400 | 이메일 형식 오류, 비밀번호 8자 미만, 닉네임 빈값 |
| 409 | 이미 가입된 이메일 |

---

### 1-2. 로그인

**POST** `/api/auth/login`

**인증 불필요**

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123"
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

| 상태코드 | 원인 |
|----------|------|
| 400 | 이메일/비밀번호 빈값 |
| 404 | 존재하지 않는 이메일 |
| 401 | 비밀번호 불일치 |

---

### 1-3. 토큰 재발급

**POST** `/api/auth/reissue`

**Header:** `Refresh-Token: <refreshToken>`

**성공 응답** `200 OK`
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci..."
}
```

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 401 | 만료되거나 유효하지 않은 리프레시 토큰 |
| 401 | 저장된 토큰과 불일치 |

---

### 1-4. 로그아웃

**POST** `/api/auth/logout`

**인증 필요**

**성공 응답** `200 OK`
```
로그아웃 성공
```

---

## 2. 공연 API

> 공연 생성/삭제는 **ADMIN** 권한 필요

### 2-1. 공연 생성

**POST** `/api/performances`

**권한:** ADMIN

**Request Body**
```json
{
  "name": "2024 정기공연",
  "performanceDate": "2024-12-31",
  "description": "연말 정기공연입니다"
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
  "name": "2024 정기공연",
  "performanceDate": "2024-12-31",
  "description": "연말 정기공연입니다",
  "createdAt": "2024-06-20T10:00:00"
}
```

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 400 | name 빈값, performanceDate 누락 또는 형식 오류 |
| 401 | 토큰 없음 또는 만료 |
| 500 | 관리자 권한 없음 (RuntimeException — 추후 403으로 개선 가능) |

---

### 2-2. 공연 목록 조회

**GET** `/api/performances`

**권한:** 인증 필요

**성공 응답** `200 OK`
```json
[
  {
    "id": 1,
    "name": "2024 정기공연",
    "performanceDate": "2024-12-31",
    "description": "연말 정기공연입니다",
    "createdAt": "2024-06-20T10:00:00"
  }
]
```

---

### 2-3. 공연 단건 조회

**GET** `/api/performances/{id}`

**권한:** 인증 필요

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "name": "2024 정기공연",
  "performanceDate": "2024-12-31",
  "description": "연말 정기공연입니다",
  "createdAt": "2024-06-20T10:00:00"
}
```

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 500 | 존재하지 않는 공연 ID (RuntimeException) |

---

### 2-4. 공연 삭제

**DELETE** `/api/performances/{id}`

**권한:** ADMIN

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 500 | 관리자 권한 없음 또는 존재하지 않는 공연 ID |

---

## 3. 팀 API

> 팀 생성 및 멤버 관리는 **ADMIN** 권한 필요

### 3-1. 팀 생성

**POST** `/api/teams`

**권한:** ADMIN

**Request Body**
```json
{
  "name": "WAVY"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| name | String | O | 비어있으면 안 됨, 중복 불가 |

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "name": "WAVY",
  "createdAt": "2024-06-20T10:00:00"
}
```

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 400 | name 빈값 |
| 500 | 관리자 권한 없음 또는 이미 존재하는 팀 이름 |

---

### 3-2. 팀 목록 조회

**GET** `/api/teams`

**권한:** 인증 필요

**성공 응답** `200 OK`
```json
[
  {
    "id": 1,
    "name": "WAVY",
    "createdAt": "2024-06-20T10:00:00"
  },
  {
    "id": 2,
    "name": "GROOVE",
    "createdAt": "2024-06-20T10:05:00"
  }
]
```

---

### 3-3. 팀 상세 조회 (멤버 포함)

**GET** `/api/teams/{id}`

**권한:** 인증 필요

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "name": "WAVY",
  "createdAt": "2024-06-20T10:00:00",
  "members": [
    {
      "id": 1,
      "userId": 2,
      "nickname": "김팀장",
      "role": "LEADER",
      "createdAt": "2024-06-20T10:10:00"
    },
    {
      "id": 2,
      "userId": 3,
      "nickname": "이부팀장",
      "role": "DEPUTY",
      "createdAt": "2024-06-20T10:11:00"
    },
    {
      "id": 3,
      "userId": 4,
      "nickname": "박팀원",
      "role": "MEMBER",
      "createdAt": "2024-06-20T10:12:00"
    }
  ]
}
```

---

### 3-4. 팀 멤버 추가

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

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "userId": 2,
  "nickname": "김팀장",
  "role": "LEADER",
  "createdAt": "2024-06-20T10:10:00"
}
```

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 400 | userId 또는 role 누락 |
| 500 | 이미 팀에 속해있는 멤버 |
| 500 | 이미 팀장(LEADER)이 존재하는데 LEADER 추가 시도 |
| 500 | 이미 부팀장(DEPUTY)이 존재하는데 DEPUTY 추가 시도 |
| 500 | 존재하지 않는 userId 또는 teamId |

---

### 3-5. 팀 멤버 역할 변경

**PUT** `/api/teams/{id}/members/{userId}/role`

**권한:** ADMIN

**Request Body**
```json
{
  "role": "DEPUTY"
}
```

**성공 응답** `200 OK`
```json
{
  "id": 3,
  "userId": 4,
  "nickname": "박팀원",
  "role": "DEPUTY",
  "createdAt": "2024-06-20T10:12:00"
}
```

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 400 | role 누락 |
| 500 | 팀 멤버가 아님 |
| 500 | 변경하려는 role이 이미 다른 멤버에게 있음 |

---

### 3-6. 팀 멤버 제거

**DELETE** `/api/teams/{id}/members/{userId}`

**권한:** ADMIN

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 500 | 팀 멤버가 아님 |

---

## 4. 연습 일정 API

### 4-1. 연습 신청

**POST** `/api/schedules`

**권한:** 해당 팀의 LEADER 또는 DEPUTY

**Request Body**
```json
{
  "performanceId": 1,
  "teamId": 1,
  "practiceDate": "2024-07-10",
  "startTime": "18:00:00",
  "endTime": "20:00:00"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| performanceId | Long | O | 존재하는 공연 ID |
| teamId | Long | O | 존재하는 팀 ID |
| practiceDate | String | O | `YYYY-MM-DD` |
| startTime | String | O | `HH:MM:SS` |
| endTime | String | O | `HH:MM:SS` |

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "performanceId": 1,
  "performanceName": "2024 정기공연",
  "teamId": 1,
  "teamName": "WAVY",
  "submittedByNickname": "김팀장",
  "practiceDate": "2024-07-10",
  "startTime": "18:00:00",
  "endTime": "20:00:00",
  "assignedRoom": null,
  "status": "PENDING",
  "adminNote": null,
  "createdAt": "2024-06-20T10:00:00",
  "updatedAt": "2024-06-20T10:00:00"
}
```

> `assignedRoom`은 관리자가 배정 실행 전까지 `null`입니다.

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 400 | 필수 필드 누락 또는 형식 오류 |
| 500 | 해당 팀의 멤버가 아님 |
| 500 | MEMBER 역할로 신청 시도 ("팀장 또는 부팀장만 신청할 수 있습니다") |
| 500 | 신청 마감일(전주 일요일) 초과 ("제출 기한이 지났습니다") |
| 500 | 당일 팀 신청 시간 합산 2시간 초과 ("팀별 하루 최대 2시간을 초과합니다") |

---

### 4-2. 주간 일정 조회

**GET** `/api/schedules?performanceId={id}&weekStart={date}`

**권한:** 인증 필요

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| performanceId | Long | O | 공연 ID |
| weekStart | String | O | 주 시작일 (월요일), `YYYY-MM-DD` |
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 20) |

**예시:** `GET /api/schedules?performanceId=1&weekStart=2024-07-08`

**성공 응답** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "performanceId": 1,
      "performanceName": "2024 정기공연",
      "teamId": 1,
      "teamName": "WAVY",
      "submittedByNickname": "김팀장",
      "practiceDate": "2024-07-10",
      "startTime": "18:00:00",
      "endTime": "20:00:00",
      "assignedRoom": "CLUB_ROOM",
      "status": "APPROVED",
      "adminNote": null,
      "createdAt": "2024-06-20T10:00:00",
      "updatedAt": "2024-06-21T09:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### 4-3. 신청 단건 조회

**GET** `/api/schedules/{id}`

**권한:** 인증 필요

**성공 응답** `200 OK` — 4-1 응답과 동일한 구조

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 500 | 존재하지 않는 신청 ID |

---

### 4-4. 팀 신청 목록 조회

**GET** `/api/schedules/team/{teamId}`

**권한:** 해당 팀 멤버

**Query Parameters:** `page`, `size` (선택)

**성공 응답** `200 OK` — 페이지네이션된 ScheduleResponse 목록

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 500 | 해당 팀의 멤버가 아님 |

---

### 4-5. 신청 취소

**POST** `/api/schedules/{id}/cancel`

**권한:** 신청한 본인 또는 ADMIN

**성공 응답** `204 No Content`

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 500 | 취소 권한 없음 (본인도 ADMIN도 아님) |
| 500 | 이미 REJECTED 또는 CANCELLED 상태 |

---

### 4-6. 신청 거절 (관리자)

**POST** `/api/schedules/{id}/reject`

**권한:** ADMIN

**Request Body**
```json
{
  "adminNote": "해당 시간대에 다른 일정이 있습니다"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| adminNote | String | X | 거절 사유 |

**성공 응답** `200 OK`
```json
{
  "id": 1,
  "status": "REJECTED",
  "adminNote": "해당 시간대에 다른 일정이 있습니다",
  ...
}
```

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 500 | 관리자 권한 없음 |
| 500 | PENDING 상태가 아닌 신청 거절 시도 |

---

### 4-7. 방 수동 재배정 (관리자)

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

**성공 응답** `200 OK` — 수정된 ScheduleResponse

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 400 | room 누락 |
| 500 | 관리자 권한 없음 |
| 500 | APPROVED 상태가 아닌 신청에 재배정 시도 |

---

### 4-8. 자동 배정 실행 (관리자) ⭐

**POST** `/api/schedules/assign?performanceId={id}&weekStart={date}`

**권한:** ADMIN

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| performanceId | Long | O | 공연 ID |
| weekStart | String | O | 배정할 주의 월요일, `YYYY-MM-DD` |

**예시:** `POST /api/schedules/assign?performanceId=1&weekStart=2024-07-08`

**동작 설명**
- `weekStart` ~ `weekStart+6일(일요일)` 사이의 PENDING 신청들을 자동으로 방에 배정합니다
- 배정 우선순위: 동방 → 학생회관 지하 → 치어룸(수요일 18:30~20:30) → 지하 주차장
- 같은 팀이 이미 이번 주에 동방을 받았으면 다음 신청은 동방을 건너뜁니다

**성공 응답** `200 OK`
```json
[
  {
    "id": 1,
    "teamName": "WAVY",
    "practiceDate": "2024-07-10",
    "startTime": "18:00:00",
    "endTime": "20:00:00",
    "assignedRoom": "CLUB_ROOM",
    "status": "APPROVED",
    ...
  },
  {
    "id": 2,
    "teamName": "GROOVE",
    "practiceDate": "2024-07-10",
    "startTime": "18:00:00",
    "endTime": "20:00:00",
    "assignedRoom": "STUDENT_UNION_BASEMENT",
    "status": "APPROVED",
    ...
  },
  {
    "id": 3,
    "teamName": "WAVY",
    "practiceDate": "2024-07-10",
    "startTime": "18:00:00",
    "endTime": "20:00:00",
    "assignedRoom": "STUDENT_UNION_BASEMENT",
    "status": "APPROVED",
    ...
  }
]
```

> WAVY 팀이 같은 날 두 번 신청: 첫 번째는 동방, 두 번째는 (이미 동방 받았으므로) 학생회관 지하

**실패 응답**

| 상태코드 | 원인 |
|----------|------|
| 500 | 관리자 권한 없음 |
| 500 | 존재하지 않는 performanceId |

---

## 전체 플로우 예시

### 시나리오: 공연 연습 일정 잡기

```
1. 관리자 로그인
   POST /api/auth/login

2. 공연 생성
   POST /api/performances
   { "name": "2024 정기공연", "performanceDate": "2024-12-31" }

3. 팀 생성
   POST /api/teams  { "name": "WAVY" }
   POST /api/teams  { "name": "GROOVE" }

4. 팀원 추가
   POST /api/teams/1/members  { "userId": 2, "role": "LEADER" }
   POST /api/teams/1/members  { "userId": 3, "role": "MEMBER" }
   POST /api/teams/2/members  { "userId": 4, "role": "LEADER" }

5. 팀장 로그인 후 연습 신청 (전주 일요일까지)
   POST /api/schedules
   { "performanceId": 1, "teamId": 1, "practiceDate": "2024-07-10",
     "startTime": "18:00:00", "endTime": "20:00:00" }

6. 관리자가 배정 실행 (일요일 마감 후)
   POST /api/schedules/assign?performanceId=1&weekStart=2024-07-08

7. 결과 조회
   GET /api/schedules?performanceId=1&weekStart=2024-07-08
```

---

## 공통 에러 응답 형식

현재는 Spring 기본 에러 응답 형식을 사용합니다.

**400 Bad Request (검증 실패)**
```json
{
  "timestamp": "2024-06-20T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/schedules"
}
```

**401 Unauthorized (토큰 없음/만료)**
```json
{
  "timestamp": "2024-06-20T10:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/performances"
}
```

> 비즈니스 예외(권한 없음, 중복, 마감 초과 등)는 현재 500으로 반환됩니다.
> 추후 GlobalExceptionHandler 추가 시 적절한 상태코드로 분리할 수 있습니다.
