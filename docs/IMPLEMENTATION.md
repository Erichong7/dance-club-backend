# 댄스 동아리 연습 일정 관리 시스템 — 구현 설명

> ⚠️ **이 문서는 스냅샷입니다.** 실제 최신 동작은 서버 실행 후 `http://localhost:8080/swagger-ui.html`에서 확인하는 것을 권장합니다. 이 문서는 도메인 구조와 비즈니스 규칙의 큰 그림을 파악하기 위한 보조 자료입니다.

## 개요

구글 Docs로 수동 관리하던 댄스 동아리 연습 일정을 REST API로 자동화한 시스템입니다.

**핵심 플로우**
```
신규 회원 → 회원가입 신청 (승인 대기)
관리자   → 회원가입 승인/거절
관리자   → 공연(Performance) 생성
관리자   → 공연에 소속된 팀(Team) 생성 및 팀원 배정
팀장/부팀장 → 연습 일정 신청 (신청 가능 기간은 항상 '다음 주'만 해당)
관리자   → 배정 실행 버튼 → 자동 연습실 배정
```

---

## 도메인 구조

```
domain/
├── user/
│   ├── User.java              ← role, signupStatus, phoneNumber 포함
│   ├── UserRole.java          ← USER / ADMIN
│   ├── SignupStatus.java      ← REQUESTED / APPROVED / REJECTED
│   ├── controller/
│   │   ├── AuthController.java    ← 회원가입/로그인/토큰/가입 승인·거절
│   │   └── UserController.java    ← 내 정보 조회, 회원 검색/삭제
│   ├── service/
│   │   ├── AuthService.java
│   │   └── UserService.java
│   ├── repository/
│   └── dto/
├── post/
│   ├── Post.java               ← 게시글 엔티티
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── dto/
├── performance/
│   ├── Performance.java        ← 공연 엔티티 (팀들을 1:N으로 보유)
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── dto/
├── team/
│   ├── Team.java               ← 팀 엔티티, 반드시 하나의 Performance에 소속
│   ├── TeamMember.java         ← 팀원 엔티티 (역할 포함)
│   ├── TeamMemberRole.java     ← LEADER / DEPUTY / MEMBER
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── dto/
└── schedule/
    ├── ScheduleRequest.java    ← 연습 신청 엔티티
    ├── RoomType.java           ← 연습실 종류 Enum
    ├── ScheduleStatus.java     ← 신청 상태 Enum
    ├── controller/
    ├── service/
    ├── repository/
    └── dto/

global/
├── jwt/                        ← JwtUtil, JwtAuthenticationFilter
├── security/                   ← SecurityConfig, JsonAuthenticationEntryPoint, JsonAccessDeniedHandler
├── config/                     ← JpaConfig, QuerydslConfig, SwaggerConfig
├── exception/                  ← ErrorCode, BusinessException, ErrorResponse, GlobalExceptionHandler
└── logging/                    ← 요청/응답, 예외 로깅
```

---

## 엔티티 상세

### User

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| email | String | 이메일 (유니크) |
| password | String | 암호화된 비밀번호 |
| nickname | String | 닉네임 |
| phoneNumber | String | 전화번호 |
| refreshToken | String | 리프레시 토큰 |
| role | UserRole | USER(기본값) / ADMIN |
| signupStatus | SignupStatus | REQUESTED(기본값) / APPROVED / REJECTED |
| createdAt | LocalDateTime | 생성일 |

> ADMIN 계정을 만들려면 DB에서 직접 role을 변경해야 합니다.
> `UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';`
> 회원가입 직후 `signupStatus`는 `REQUESTED`이며, 관리자가 승인(`APPROVED`)하기 전까지 로그인할 수 없습니다.

---

### Performance (공연)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| name | String | 공연 이름 |
| performanceDate | LocalDate | 공연 날짜 |
| description | String | 공연 설명 (선택) |
| teams | List\<Team\> | 이 공연에 소속된 팀 목록 (1:N, 조회 전용) |
| createdAt | LocalDateTime | 생성일 |

---

### Team (팀)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| name | String | 팀 이름 (전체 유니크) |
| performance | Performance | 소속 공연 (N:1, 필수) |
| createdAt | LocalDateTime | 생성일 |

> 팀은 공연과 독립적으로 존재하지 않고 **반드시 하나의 공연에 소속**됩니다. 공연이 여러 개면 같은 팀명이어도 매 공연마다 새 Team 레코드를 만들어야 합니다.

---

### TeamMember (팀 멤버)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| team | Team | 소속 팀 (FK) |
| user | User | 멤버 유저 (FK) |
| role | TeamMemberRole | LEADER / DEPUTY / MEMBER |
| createdAt | LocalDateTime | 가입일 |

**역할 규칙**
- 한 팀에 LEADER는 최대 1명
- 한 팀에 DEPUTY(부팀장)는 최대 1명
- 같은 유저가 같은 팀에 중복 등록 불가 (`team_id`, `user_id` 유니크 제약)

---

### ScheduleRequest (연습 신청)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| performance | Performance | 소속 공연 (FK) |
| team | Team | 신청 팀 (FK) |
| submittedBy | User | 신청한 유저 (FK) |
| startAt | LocalDateTime | 연습 시작 일시 |
| endAt | LocalDateTime | 연습 종료 일시 (자정을 넘기면 익일로 자동 보정) |
| alternativeRoom | RoomType | 예비 연습실 (신청 시 지정, 자동 배정에서 우선 연습실을 못 받으면 여기로 배정됨) |
| assignedRoom | RoomType | 실제 배정된 연습실 (배정 전 null) |
| status | ScheduleStatus | 현재 상태 |
| adminNote | String | 관리자 메모 (반려 사유 등, 선택) |
| createdAt | LocalDateTime | 신청일 |
| updatedAt | LocalDateTime | 최종 수정일 |

> API 요청/응답 DTO(`ScheduleCreateRequest` 등)는 여전히 `practiceDate` + `startTime`/`endTime`(날짜와 시간 분리)을 입출력하지만, 서비스 계층에서 이를 `startAt`/`endAt`(합쳐진 `LocalDateTime`)으로 변환해 저장합니다. `endTime`이 `startTime`보다 이르면 자정을 넘긴 연습으로 간주해 종료를 다음 날로 처리합니다.

---

## Enum 목록

### UserRole
| 값 | 설명 |
|----|------|
| USER | 일반 사용자 (기본값) |
| ADMIN | 관리자 (회장단) |

### SignupStatus
| 값 | 설명 |
|----|------|
| REQUESTED | 가입 신청 후 승인 대기 (기본값) — 로그인 불가 |
| APPROVED | 관리자 승인 완료 — 로그인 가능 |
| REJECTED | 관리자 거절 — 로그인 불가 |

### TeamMemberRole
| 값 | 설명 |
|----|------|
| LEADER | 팀장 — 연습 신청 가능 |
| DEPUTY | 부팀장 — 팀장 부재 시 신청 가능 |
| MEMBER | 일반 팀원 — 신청 불가 |

### RoomType (연습실 종류)
| 값 | 설명 |
|----|------|
| CLUB_ROOM | 동방 (동아리방) |
| STUDENT_UNION_BASEMENT | 학생회관 지하 |
| UNDERGROUND_PARKING | 지하 주차장 |
| EXTERNAL | 외부 연습실 |
| CHEER_ROOM | 치어룸 |

### ScheduleStatus (신청 상태)
| 값 | 설명 |
|----|------|
| PENDING | 대기 중 (배정 전) |
| APPROVED | 승인됨 (연습실 배정 완료) |
| REJECTED | 거절됨 |
| CANCELLED | 취소됨 |

---

## 비즈니스 규칙

### 회원가입 승인
- 회원가입 직후 `signupStatus = REQUESTED` — 이 상태로는 로그인 불가 (`SIGNUP_PENDING`, 403)
- 관리자가 승인(`PATCH /api/auth/{id}/approve`)해야 로그인 가능
- 관리자가 거절(`PATCH /api/auth/{id}/reject`)하면 영구적으로 로그인 불가 (`SIGNUP_REJECTED`, 403)

### 신청 권한
- **LEADER 또는 DEPUTY**만 신청 가능 (`ScheduleService.create`)
- 신청하는 유저는 해당 팀의 멤버여야 함 (아니면 `NOT_TEAM_MEMBER`, 403)
- MEMBER가 신청하면 `SCHEDULE_CREATE_FORBIDDEN` (403)

### 신청 가능 기간 — "다음 주"만 허용
연습일이 속한 주(월요일 시작)의 **전주 일요일**이 신청 마감일입니다. 이 마감일을 기준으로 두 가지를 함께 검사합니다 (`ScheduleService.validateDeadline`):

1. 마감일이 이미 지났으면 → `SCHEDULE_DEADLINE_PASSED` (400) — 이미 시작했거나 지나간 주는 신청 불가
2. 마감일이 "오늘 기준 돌아오는 일요일"보다 뒤라면 → `SCHEDULE_TOO_FAR_IN_ADVANCE` (400) — 다다음 주 이후는 신청 불가

결과적으로 **항상 바로 다음 주(아직 시작되지 않은, 가장 가까운 월~일)** 에 대해서만 신청할 수 있습니다. 예: 오늘이 7/6(월)이면 7/13(월)~7/19(일) 주에 대한 신청만 가능하고, 7/20 이후 주나 이미 지난 주(7/12 이전 마감)는 거부됩니다.

- 시작 시간과 종료 시간이 같으면 `SCHEDULE_INVALID_TIME_RANGE` (400)
- 종료 시간이 시작 시간보다 이르면 자정을 넘긴 연습(익일 종료)으로 자동 처리됩니다.

> ⚠️ 과거 문서에 있던 "팀별 하루 최대 2시간 제한"은 현재 `ScheduleService.validateDailyLimit()`가 주석 처리되어 있어 **실제로 적용되지 않습니다.**

### 자동 배정 알고리즘

관리자가 `POST /api/schedules/assign?performanceId={id}&weekStart={date}` 호출 시 `ScheduleService.assignWeek()`가 실행됩니다.

```
해당 주(weekStart ~ weekStart+6일)의 PENDING 신청들을 createdAt 오름차순(선착순)으로 정렬

각 신청에 대해:
  1. 동방(CLUB_ROOM)
     - 이번 주 해당 팀이 아직 동방을 받지 않았고
     - 겹치는 시간대에 이미 배정된 동방 슬롯이 없으면 → 동방 배정

  2. 학생회관 지하(STUDENT_UNION_BASEMENT)
     - 겹치는 시간대에 이미 배정된 슬롯이 없으면 → 배정

  3. 치어룸(CHEER_ROOM)
     - 수요일(Wednesday)이고
     - 시작 >= 18:30 AND 종료 <= 20:30 이고
     - 그 날짜에 이미 배정된 팀이 3팀 미만이면 → 배정

  4. 예비 연습실(alternativeRoom)
     - 위 세 곳 모두 해당 없으면 → 신청 시 지정한 alternativeRoom으로 배정
```

**동방 최소 1회 보장 원리**: 팀이 이미 이번 주에 동방을 배정받았으면 다음 신청은 동방을 건너뛰고 다른 공간부터 시도합니다. 이렇게 하면 동방을 아직 받지 못한 팀들에게 빈 동방 슬롯이 돌아갑니다.

> 자정을 넘기는 연습(예: 23:00~02:00)도 날짜별로 나누지 않고 `LocalDateTime` 구간으로 겹침을 판단하므로 정확히 처리됩니다.

### 수동 배정 (관리자 직접 등록)

`POST /api/schedules/assign/manual`로 관리자는 신청 절차 없이 팀의 연습 일정을 직접 등록하면서 연습실을 지정해 즉시 `APPROVED` 상태로 생성할 수 있습니다. 이때 신청자(`submittedBy`)는 해당 팀의 LEADER로 자동 설정되며, 팀에 LEADER가 없으면 `TEAM_LEADER_NOT_FOUND`(404)가 발생합니다.

### 회원 삭제 제약

관리자가 회원을 삭제(`DELETE /api/users/{id}`)하면 해당 회원의 게시글, 연습 신청, 팀 멤버십이 함께 삭제됩니다. 단, 본인 계정은 삭제할 수 없고(`SELF_DELETE_FORBIDDEN`), 삭제 대상이 어떤 팀의 LEADER로 있다면 먼저 팀장을 위임해야 삭제할 수 있습니다(`TEAM_LEADER_DELETE_FORBIDDEN`).

---

## 인증 방식

`POST /api/auth/signup|login|reissue`, `GET /api/posts/**`, `GET /api/schedules`·`GET /api/schedules/{id}`·`GET /api/schedules/team/{teamId}/week`, `GET /api/teams`·`GET /api/teams/{id}`, `GET /api/performances`·`GET /api/performances/{id}`, Swagger 경로를 제외한 **모든 API는 JWT Bearer 토큰이 필요**합니다 (`global/security/SecurityConfig`).

```
Authorization: Bearer <accessToken>
```

`POST /api/auth/login`으로 토큰을 발급받고, `Authorization` 헤더에 포함해 요청합니다. `JwtAuthenticationFilter`가 토큰을 검증해 `userId`를 `SecurityContext`의 인증 주체로 설정하며, 컨트롤러는 `@AuthenticationPrincipal Long userId`로 이를 받습니다.

**관리자 권한 검사는 Spring Security 역할 기반이 아니라 서비스 계층에서 직접 수행**됩니다. 즉 `@PreAuthorize` 없이, 각 서비스 메서드가 `userId`로 `User`를 조회해 `role == ADMIN`인지 확인하고 아니면 `BusinessException(ADMIN_ONLY)`(403)를 던지는 방식입니다.

## 에러 응답

모든 예외는 `GlobalExceptionHandler`가 `ErrorCode` 기반의 구조화된 JSON(`{code, message, errors}`)으로 변환해 응답합니다. 전체 에러 코드는 `global/exception/ErrorCode.java`에 정의되어 있으며, 자세한 형식과 코드별 발생 조건은 [`API_SPEC.md`](API_SPEC.md)를 참고하세요.

---

## 파일 목록 (도메인/전역 신규 및 주요 파일)

| 경로 | 설명 |
|------|------|
| `domain/user/UserRole.java` | UserRole Enum |
| `domain/user/SignupStatus.java` | SignupStatus Enum (회원가입 승인 상태) |
| `domain/performance/Performance.java` | 공연 엔티티 |
| `domain/performance/service/PerformanceService.java` | 공연 서비스 |
| `domain/performance/controller/PerformanceController.java` | 공연 컨트롤러 |
| `domain/performance/repository/PerformanceRepository.java` | 공연 레포지토리 |
| `domain/performance/dto/...` | 공연 DTO |
| `domain/team/Team.java` | 팀 엔티티 (Performance에 소속) |
| `domain/team/TeamMember.java` | 팀 멤버 엔티티 |
| `domain/team/TeamMemberRole.java` | TeamMemberRole Enum |
| `domain/team/service/TeamService.java` | 팀 서비스 |
| `domain/team/controller/TeamController.java` | 팀 컨트롤러 |
| `domain/team/repository/TeamRepository.java` | 팀 레포지토리 |
| `domain/team/repository/TeamMemberRepository.java` | 팀 멤버 레포지토리 |
| `domain/team/dto/...` | 팀 DTO |
| `domain/schedule/ScheduleRequest.java` | 연습 신청 엔티티 (startAt/endAt, alternativeRoom) |
| `domain/schedule/RoomType.java` | RoomType Enum |
| `domain/schedule/ScheduleStatus.java` | ScheduleStatus Enum |
| `domain/schedule/service/ScheduleService.java` | 연습 신청 서비스 (자동/수동 배정 포함) |
| `domain/schedule/controller/ScheduleController.java` | 연습 신청 컨트롤러 |
| `domain/schedule/repository/ScheduleRequestRepository.java` | 연습 신청 레포지토리 |
| `domain/schedule/dto/...` | 연습 신청 DTO |
| `domain/user/service/AuthService.java` | 회원가입/로그인/토큰/가입 승인·거절 서비스 |
| `domain/user/service/UserService.java` | 내 정보 조회, 회원 검색/삭제 서비스 |
| `domain/user/controller/AuthController.java` | 인증 컨트롤러 |
| `domain/user/controller/UserController.java` | 회원 컨트롤러 |
| `global/exception/ErrorCode.java` | 도메인별 에러 코드 정의 |
| `global/exception/BusinessException.java` | ErrorCode를 담는 커스텀 런타임 예외 |
| `global/exception/ErrorResponse.java` | 에러 응답 DTO |
| `global/exception/GlobalExceptionHandler.java` | 전역 예외 처리 (`@RestControllerAdvice`) |
| `global/jwt/JwtUtil.java` | JWT 생성/검증 |
| `global/jwt/JwtAuthenticationFilter.java` | JWT 인증 필터 |
| `global/security/SecurityConfig.java` | Spring Security 설정 (permitAll 경로 포함) |
| `global/logging/` | 요청/응답 및 예외 로깅 |
