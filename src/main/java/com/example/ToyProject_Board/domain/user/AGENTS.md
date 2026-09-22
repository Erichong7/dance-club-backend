# user

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Accounts and authentication. Signup creates a `REQUESTED` user that cannot log in until an admin approves it. Login issues JWT access and refresh tokens. Reissue uses Refresh Token Rotation. Admins list signup requests, approve or reject them, search users, and delete users.

## Key Files
| File | Description |
|------|-------------|
| `User.java` | Entity (`users` table): unique email, BCrypt password, nickname, phoneNumber, refreshToken, role (default `USER`), signupStatus |
| `UserRole.java` | `USER`, `ADMIN` |
| `SignupStatus.java` | `REQUESTED`, `APPROVED`, `REJECTED` |
| `controller/AuthController.java` | `/api/auth`: `POST signup/login/reissue` (public; reissue reads the `Refresh-Token` header), `POST logout`, `GET signup-requests`, `PATCH {id}/approve`, `PATCH {id}/reject` |
| `controller/UserController.java` | `/api/users`: `GET me` (includes team ids and names), `GET search` (admin), `DELETE {id}` (admin, 204) |
| `service/AuthService.java` | signup (password-confirm and duplicate-email checks), login (status checks come before the password check), reissue (RTR), logout (clears refreshToken), approve and reject |
| `service/UserService.java` | getMyInfo, searchUsers, and deleteUser (refuses self-delete and team LEADERs, then deletes memberships, posts, schedule requests, and the user) |
| `repository/UserRepository.java` | JPA repo plus derived queries (`existsByEmail`, `findByEmail`, `findBySignupStatusNot`). Extends `UserRepositoryCustom` |
| `repository/UserRepositoryCustom.java` / `UserRepositoryImpl.java` | QueryDSL `searchUsers`: nickname and email partial match, status equality, always sorted `createdAt desc`, count query through `PageableExecutionUtils` |
| `dto/request/` | `SignupRequest` (email, password min 8, passwordConfirm, nickname, phone `^01[016789]-\d{3,4}-\d{4}$`), `LoginRequest`, `UserSearchRequest` (bound with `@ModelAttribute`, so it has setters) |
| `dto/response/` | `TokenResponse`, `UserDetailResponse`, `UserSearchResponse`, `SignupRequestListResponse`. All three user responses expose the JSON field `nickName` (camel-cased), while the entity field is `nickname` |

## For AI Agents

### Working In This Directory
- There is no admin-creation API. The first admin is promoted with SQL (see README). Tests build admins with `UserFixture.createAdmin()`, which sets `role` by reflection.
- The `User` builder does not set `role` (it defaults to `USER`) and does not default `signupStatus`. Persisted users must set `signupStatus` because the column is NOT NULL.
- Login order matters and is tested: missing email → `INVALID_CREDENTIALS`, `REJECTED` → `SIGNUP_REJECTED`, `REQUESTED` → `SIGNUP_PENDING`, then wrong password → `INVALID_CREDENTIALS`.
- RTR: `reissue` requires the presented token to equal the stored `User.refreshToken`, then replaces it. A reused old token gets `TOKEN_MISMATCH`. `logout` sets the stored token to null.
- **Every method that changes an entity must have its own `@Transactional`.** The class-level `@Transactional(readOnly = true)` sets Hibernate to `FlushMode.MANUAL`, so changes made through dirty checking are silently discarded. `reissue` and `logout` had exactly this bug, which disabled RTR and logout until it was fixed on 2026-09-22. Mockito tests cannot catch it, so `AuthServiceIntegrationTest` now checks the database directly.
- `getSignupRequests` returns everyone who is not `APPROVED`, which includes `REJECTED` users.
- If you add a new user-owned entity, extend the delete sequence in `UserService.deleteUser`.

### Testing Requirements
- `AuthServiceTest`, `UserServiceTest` (Mockito), `AuthServiceIntegrationTest` (`@SpringBootTest` on H2, checks refresh-token persistence), `controller/AuthControllerTest`, `controller/UserControllerTest` (`@WebMvcTest`), `repository/UserRepositoryImplTest` (`@DataJpaTest` + `@Import({QuerydslConfig, JpaConfig})` on H2).

## Dependencies

### Internal
- `global/jwt/JwtUtil`, `global/exception`, and the repositories `team/TeamMemberRepository`, `post/PostRepository`, `schedule/ScheduleRequestRepository` (used by `UserService`)

### External
- Spring Security `PasswordEncoder` (BCrypt), QueryDSL (generated `QUser`)

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
