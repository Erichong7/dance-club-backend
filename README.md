# dance-club-backend

명지대학교 댄스 동아리 **MGH**의 연습 일정 조율을 위한 백엔드 API입니다. 구글 Docs로 수동 관리하던 공연별 연습실 배정 프로세스를 REST API로 자동화합니다.

## 핵심 플로우

```
신규 회원 → 회원가입 신청 (관리자 승인 대기)
관리자   → 회원가입 승인/거절
관리자   → 공연(Performance) 생성
관리자   → 공연에 소속된 팀 생성 및 팀원 배정
팀장/부팀장 → 연습 일정 신청 (신청 가능 기간은 항상 '다음 주'만 해당)
관리자   → 배정 실행 → 신청 건들을 연습실에 자동 배정
```

## API 문서는 어디서 보나요?

**가장 정확한 최신 API 명세는 Swagger UI입니다.** 서버를 실행한 뒤 아래 주소에서 실제 코드로부터 생성된 엔드포인트·요청/응답 스키마를 바로 확인하고 호출까지 해볼 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```

`docs/API_SPEC.md`, `docs/IMPLEMENTATION.md`는 전체 흐름과 비즈니스 규칙을 빠르게 훑어보기 위한 **보조 문서**입니다. 사람이 손으로 관리하기 때문에 코드가 바뀐 직후에는 실제 동작과 어긋날 수 있습니다 — 엔드포인트 세부 스펙을 확인할 때는 항상 Swagger UI를 우선으로 삼으세요.

## 기술 스택

- **Java 17**, **Spring Boot 4.0.6**
- Spring Data JPA, QueryDSL (OpenFeign fork, Hibernate 7 호환)
- Spring Security + JWT (jjwt 0.12.3)
- MySQL 8.0 (운영/개발), H2 (테스트)
- springdoc-openapi (Swagger UI)
- Lombok

## 주요 기능

- **인증/회원가입 승인**: 회원가입 후 `REQUESTED` 상태로 대기, 관리자 승인(`APPROVED`) 전까지 로그인 불가. 로그인/로그아웃, Refresh Token Rotation(RTR) 기반 토큰 재발급
- **회원 관리**: 내 정보 조회, 관리자용 회원 검색(닉네임/이메일/가입 상태)·삭제(팀장인 경우 위임 후에만 삭제 가능, 본인 계정 삭제 불가)
- **공연 관리**: 공연 생성/조회/삭제 (관리자 전용, 조회는 비로그인도 가능)
- **팀 관리**: 팀은 반드시 하나의 공연에 소속됨. 팀 생성/삭제, 팀원 추가/역할 변경/제거 — 팀당 LEADER 1명·DEPUTY 1명 제한
- **연습 일정**:
  - 팀장/부팀장만 신청 가능, 신청 가능 기간은 항상 "다음 주"로 제한 (전주 일요일 마감)
  - 관리자의 자동 배정 알고리즘: 동방 → 학생회관 지하 → 치어룸(수요일 한정) → 예비 연습실(신청 시 지정) 순으로 배정하며, 팀별 동방 최소 1회를 보장
  - 관리자가 절차 없이 직접 등록·즉시 승인하는 수동 배정 기능
  - 신청 반려/취소, 연습실 재배정, 신청 삭제(관리자 또는 팀장)
  - 공연·팀·주 단위 일정 조회
- **게시판**: 관리자 작성 공지/게시글 CRUD (목록·상세 조회는 비로그인도 가능)
- **구조화된 에러 응답**: 비즈니스 예외를 `{code, message, errors}` 형태의 JSON으로 응답 (도메인별 에러 코드 체계)
- **로깅**: HTTP 요청/응답 및 예외 로깅

## 패키지 구조

```
com.example.ToyProject_Board
├── domain/
│   ├── user/         회원가입·로그인·가입 승인, 회원 조회/검색/삭제 (User, UserRole, SignupStatus)
│   ├── post/          게시판 CRUD
│   ├── performance/    공연 관리
│   ├── team/           팀/팀원 관리 (Team은 Performance에 소속, TeamMember, TeamMemberRole)
│   └── schedule/       연습 일정 신청·자동/수동 배정 (ScheduleRequest, RoomType, ScheduleStatus)
│       # 각 도메인은 controller / service / repository / dto 로 구성됩니다.
└── global/
    ├── jwt/            JWT 발급·검증 필터
    ├── security/        Spring Security 설정
    ├── config/          JPA, QueryDSL, Swagger 설정
    ├── exception/        ErrorCode 기반 전역 예외 처리
    └── logging/         요청/응답 및 예외 로깅
```

## 시작하기

### 1. 사전 준비

Docker로 MySQL을 먼저 띄웁니다.

```bash
docker-compose up -d
```

### 2. 환경 변수 설정

애플리케이션 실행 전 아래 환경 변수가 필요합니다.

| 변수 | 설명 |
|------|------|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | MySQL 사용자명 |
| `DB_PASSWORD` | MySQL 비밀번호 |
| `DB_NAME` | 데이터베이스 이름 |

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

기동 후 Swagger UI에서 API를 확인할 수 있습니다: `http://localhost:8080/swagger-ui.html`

### 관리자 계정 만들기

회원가입은 기본적으로 `USER` 권한 + `REQUESTED`(승인 대기) 상태로 생성됩니다. 최초의 `ADMIN`을 만들려면 DB에서 직접 role과 승인 상태를 변경해야 합니다.

```sql
UPDATE users SET role = 'ADMIN', signup_status = 'APPROVED' WHERE email = 'admin@example.com';
```

## 테스트

```bash
# 전체 테스트
./gradlew test

# 특정 클래스
./gradlew test --tests "com.example.ToyProject_Board.domain.post.PostServiceTest"

# 특정 메서드
./gradlew test --tests "com.example.ToyProject_Board.domain.post.PostServiceTest.createSuccess"
```

- 서비스 테스트: `@ExtendWith(MockitoExtension.class)` 기반 순수 단위 테스트
- 컨트롤러 테스트: `@WebMvcTest` + `ControllerTestSupport`로 인증 목킹

## 더 읽어보기

- [`docs/API_SPEC.md`](docs/API_SPEC.md) — 엔드포인트별 요청/응답 스펙 (보조 문서, Swagger UI가 우선)
- [`docs/IMPLEMENTATION.md`](docs/IMPLEMENTATION.md) — 도메인 구조와 비즈니스 규칙 상세 설명 (보조 문서)
