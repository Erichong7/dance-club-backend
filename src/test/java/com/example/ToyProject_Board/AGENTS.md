# ToyProject_Board (tests)

**Parent context:** `../../../../../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
JUnit 5 tests mirroring the main package layout. They run on in-memory H2 using `src/test/resources/application.yml`.

## Key Files
| File | Description |
|------|-------------|
| `ToyProjectBoardApplicationTests.java` | `@SpringBootTest` context-load smoke test |
| `domain/support/ControllerTestSupport.java` | Base class for controller tests. `@MockitoBean`s `JwtAuthenticationFilter` and `RequestLoggingFilter` as pass-throughs and sets the principal to `userId = 1L` by default. Call `setAuthentication(id)` to change it |
| `domain/user/UserFixture.java` | `create()`, `createWithId(id)`, `createAdmin()`, `createAdminWithId(id)`, and `create(email, nickname, status)` for persistence tests. Uses `ReflectionTestUtils` for `id`/`role` |
| `domain/team/TeamFixture.java`, `domain/performance/PerformanceFixture.java` | Entity builders for tests |
| `domain/<feature>/<Feature>ServiceTest.java` | Mockito unit tests (auth, user, post, team, performance, schedule) |
| `domain/<feature>/controller/*ControllerTest.java` | `@WebMvcTest` slices (auth, user, post, team, schedule) |
| `domain/user/repository/UserRepositoryImplTest.java` | `@DataJpaTest` + `@Import({QuerydslConfig.class, JpaConfig.class})` for the QueryDSL search |
| `domain/schedule/ScheduleServiceConcurrencyTest.java` | `@SpringBootTest` multithreaded save test against real H2 |
| `domain/user/AuthServiceIntegrationTest.java` | `@SpringBootTest` checking that `reissue`/`logout` actually save the refresh token. The class deliberately has no `@Transactional`, so the service's own transaction boundary is what gets tested |

## For AI Agents

### Working In This Directory
- **Service tests:** `@ExtendWith(MockitoExtension.class)`, `@InjectMocks` service, `@Mock` repositories. No Spring context. Stub `userRepository.findById` with a fixture admin for admin-only paths.
- **Controller tests:** `@WebMvcTest(XxxController.class)` + `@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class})`, extend `ControllerTestSupport`, and `@MockitoBean` the service. Use `MockMvc` with JSON bodies.
- Entity ids come from JPA, so set them with `ReflectionTestUtils.setField(entity, "id", 1L)` or use the fixture `*WithId` helpers. Do not add setters to entities for tests.
- Name test methods in Korean snake_case (`게시글_작성_성공`, `관리자가_아닌_사용자의_게시글_작성_실패`), usually with a Korean `@DisplayName` ("<동작> 성공" / "<동작> 실패 - <이유>").
- The existing assertion style is `assertThatThrownBy(...).isInstanceOf(RuntimeException.class).hasMessage("<ErrorCode의 한국어 메시지>")`. That means **editing an `ErrorCode` message breaks tests**. Follow the same style in new tests, or assert `BusinessException` + `errorCode` if the user wants stricter checks.
- Time-dependent schedule tests compute dates relative to `LocalDate.now()` (next week's Monday or Thursday), not hard-coded dates. Keep it that way. Note that the service uses KST while these tests use the JVM zone, so they can disagree near midnight on a non-KST machine.
- Do not leave `@Disabled` tests or empty test bodies behind.

### Testing Requirements
- `./gradlew test`, or `--tests "<FQN>[.method]"` for a subset. `@SpringBootTest` classes are slower, so prefer unit or slice tests for new logic.
- Exception: anything that depends on a write reaching the database (transaction boundaries, flush, unique constraints) needs an integration test without `@Transactional` on the test class. Mockito cannot see these bugs.
- `@SpringBootTest` classes share one cached context and H2 database, and they don't clean up. Use unique emails and team names in each test.

## Dependencies

### External
- JUnit 5, Mockito, AssertJ, Spring Boot test starters (`webmvc-test`, `data-jpa-test`, `security-test`, `validation-test`), H2

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
