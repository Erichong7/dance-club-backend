# 댄스 동아리 연습 일정 관리 시스템 — 구현 설명

## 개요

구글 Docs로 수동 관리하던 댄스 동아리 연습 일정을 REST API로 자동화한 시스템입니다.

**핵심 플로우**
```
관리자 → 공연(Performance) 생성
팀원 → 팀 배정 (관리자가 팀 생성 및 멤버 추가)
팀장/부팀장 → 연습 일정 신청 (전주 일요일까지)
관리자 → 배정 실행 버튼 → 자동 방 배정
```

---

## 도메인 구조

```
domain/
├── user/
│   ├── User.java              ← role 필드 추가 (USER/ADMIN)
│   └── UserRole.java          ← 신규 Enum
├── performance/
│   ├── Performance.java       ← 공연 엔티티
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── dto/
├── team/
│   ├── Team.java              ← 팀 엔티티
│   ├── TeamMember.java        ← 팀원 엔티티 (역할 포함)
│   ├── TeamMemberRole.java    ← LEADER / DEPUTY / MEMBER
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── dto/
└── schedule/
    ├── ScheduleRequest.java   ← 연습 신청 엔티티
    ├── RoomType.java          ← 연습실 종류 Enum
    ├── ScheduleStatus.java    ← 신청 상태 Enum
    ├── controller/
    ├── service/
    ├── repository/
    └── dto/
```

---

## 엔티티 상세

### User (기존 → 수정)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| email | String | 이메일 (유니크) |
| password | String | 암호화된 비밀번호 |
| nickname | String | 닉네임 |
| refreshToken | String | 리프레시 토큰 |
| **role** | **UserRole** | **USER(기본값) / ADMIN** |
| createdAt | LocalDateTime | 생성일 |

> ADMIN 계정을 만들려면 회원가입 후 DB에서 직접 변경해야 합니다.
> `UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';`

---

### Performance (공연)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| name | String | 공연 이름 |
| performanceDate | LocalDate | 공연 날짜 |
| description | String | 공연 설명 (선택) |
| createdAt | LocalDateTime | 생성일 |

---

### Team (팀)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| name | String | 팀 이름 (유니크) |
| createdAt | LocalDateTime | 생성일 |

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
- 같은 유저가 같은 팀에 중복 등록 불가

---

### ScheduleRequest (연습 신청)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| performance | Performance | 소속 공연 (FK) |
| team | Team | 신청 팀 (FK) |
| submittedBy | User | 신청한 유저 (FK) |
| practiceDate | LocalDate | 연습 날짜 |
| startTime | LocalTime | 시작 시간 |
| endTime | LocalTime | 종료 시간 |
| assignedRoom | RoomType | 배정된 연습실 (배정 전 null) |
| status | ScheduleStatus | 현재 상태 |
| adminNote | String | 관리자 메모 (거절 사유 등, 선택) |
| createdAt | LocalDateTime | 신청일 |
| updatedAt | LocalDateTime | 최종 수정일 |

---

## Enum 목록

### UserRole
| 값 | 설명 |
|----|------|
| USER | 일반 사용자 (기본값) |
| ADMIN | 관리자 (회장단) |

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
| APPROVED | 승인됨 (방 배정 완료) |
| REJECTED | 거절됨 |
| CANCELLED | 취소됨 |

---

## 비즈니스 규칙

### 신청 권한
- **LEADER 또는 DEPUTY**만 신청 가능
- MEMBER가 신청하면 403 에러

### 신청 마감일
- 연습일이 속한 주의 **전주 일요일**까지만 신청 가능
- 예시: 6/25(목) 연습 → 6/22(일)까지 신청
- 마감 이후 신청 시 400 에러

### 하루 최대 2시간
- 팀별로 하루에 최대 2시간까지만 신청 가능 (외부 연습실 포함)
- 이미 2시간이 차있는데 추가 신청하면 400 에러

### 자동 배정 알고리즘
관리자가 `POST /api/schedules/assign` 호출 시 실행됩니다.

```
신청들을 createdAt 기준 오름차순(선착순)으로 정렬

각 신청에 대해:
  1. 동방(CLUB_ROOM)
     - 이번 주 해당 팀이 아직 동방을 받지 않았고
     - 해당 날짜/시간에 동방이 비어있으면 → 동방 배정

  2. 학생회관 지하(STUDENT_UNION_BASEMENT)
     - 해당 날짜/시간에 비어있으면 → 배정

  3. 치어룸(CHEER_ROOM)
     - 수요일(Wednesday)이고
     - 시작 >= 18:30 AND 종료 <= 20:30 이고
     - 이미 배정된 팀이 3팀 미만이면 → 배정

  4. 지하 주차장(UNDERGROUND_PARKING)
     - 위 세 곳 모두 해당 없으면 → 기본 배정
```

**동방 최소 1회 보장 원리**: 팀이 이미 이번 주에 동방을 배정받았으면 다음 신청은 동방을 건너뛰고 다른 공간부터 시도합니다. 이렇게 하면 동방을 아직 받지 못한 팀들에게 빈 동방 슬롯이 돌아갑니다.

---

## 인증 방식

모든 신규 API는 JWT Bearer 토큰이 필요합니다.

```
Authorization: Bearer <accessToken>
```

`POST /api/auth/login`으로 토큰을 발급받고, `Authorization` 헤더에 포함해 요청합니다.

---

## 파일 목록 (신규 추가)

| 경로 | 설명 |
|------|------|
| `domain/user/UserRole.java` | UserRole Enum |
| `domain/performance/Performance.java` | 공연 엔티티 |
| `domain/performance/service/PerformanceService.java` | 공연 서비스 |
| `domain/performance/controller/PerformanceController.java` | 공연 컨트롤러 |
| `domain/performance/repository/PerformanceRepository.java` | 공연 레포지토리 |
| `domain/performance/dto/...` | 공연 DTO |
| `domain/team/Team.java` | 팀 엔티티 |
| `domain/team/TeamMember.java` | 팀 멤버 엔티티 |
| `domain/team/TeamMemberRole.java` | TeamMemberRole Enum |
| `domain/team/service/TeamService.java` | 팀 서비스 |
| `domain/team/controller/TeamController.java` | 팀 컨트롤러 |
| `domain/team/repository/TeamRepository.java` | 팀 레포지토리 |
| `domain/team/repository/TeamMemberRepository.java` | 팀 멤버 레포지토리 |
| `domain/team/dto/...` | 팀 DTO |
| `domain/schedule/ScheduleRequest.java` | 연습 신청 엔티티 |
| `domain/schedule/RoomType.java` | RoomType Enum |
| `domain/schedule/ScheduleStatus.java` | ScheduleStatus Enum |
| `domain/schedule/service/ScheduleService.java` | 연습 신청 서비스 (자동 배정 포함) |
| `domain/schedule/controller/ScheduleController.java` | 연습 신청 컨트롤러 |
| `domain/schedule/repository/ScheduleRequestRepository.java` | 연습 신청 레포지토리 |
| `domain/schedule/dto/...` | 연습 신청 DTO |
