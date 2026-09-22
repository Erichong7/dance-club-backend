# global

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Cross-cutting infrastructure shared by every domain: authentication, authorization rules, error handling, request logging, and framework config.

## Key Files
| File | Description |
|------|-------------|
| `security/SecurityConfig.java` | Stateless filter chain with CSRF off. Holds the public-endpoint allowlist and registers both filters and the JSON 401/403 handlers. Defines the `BCryptPasswordEncoder` bean |
| `security/JsonAuthenticationEntryPoint.java` | 401 → `ErrorResponse(C003 UNAUTHENTICATED)` as JSON |
| `security/JsonAccessDeniedHandler.java` | 403 → `ErrorResponse(C004 ACCESS_DENIED)` as JSON |
| `jwt/JwtUtil.java` | Issues and validates HS256 access and refresh tokens. The subject is the userId. The key comes from `jwt.secret` |
| `jwt/JwtAuthenticationFilter.java` | Reads the `Authorization: Bearer` token and, if valid, sets a `UsernamePasswordAuthenticationToken` whose principal is the `Long userId` and whose authority list is empty |
| `logging/RequestLoggingFilter.java` | Logs `METHOD URI STATUS durationMs userId=` per request. Runs before the JWT filter |
| `exception/ErrorCode.java` | Every business error: HTTP status, code, and Korean message. Prefixes: `C` common, `U` user/auth, `P` post, `T` team, `S` schedule, `PF` performance |
| `exception/BusinessException.java` | RuntimeException that carries an `ErrorCode` |
| `exception/ErrorResponse.java` | Record `{code, message, errors?}`. `errors` (field, reason) appears only for validation failures |
| `exception/GlobalExceptionHandler.java` | Maps `BusinessException` → its status, `MethodArgumentNotValidException` → 400 C001 with field errors, and anything else → 500 C002 |
| `config/JpaConfig.java` | `@EnableJpaAuditing` so `@CreatedDate`/`@LastModifiedDate` work |
| `config/QuerydslConfig.java` | `JPAQueryFactory` bean |
| `config/SwaggerConfig.java` | OpenAPI info and a global `bearerAuth` security scheme |

## For AI Agents

### Working In This Directory
- **Public endpoints:** when you add an unauthenticated GET or POST, add its exact path pattern to `SecurityConfig.authorizeHttpRequests`. Everything else requires a valid JWT. Today the public ones are signup, login, reissue, and GET on posts, schedules (`/api/schedules`, `/{id}`, `/team/*/week`), teams, performances, and Swagger.
- **Wildcards make new routes public silently.** `GET /api/posts/**`, `/api/schedules/*`, `/api/teams/*`, and `/api/performances/*` are permitted, so any new GET matching those patterns needs no token. On a public route `@AuthenticationPrincipal Long userId` is `null`, and a service calling `verifyAdmin(null)` then fails with a 500 instead of a 401. Check the matcher before you add a GET that needs login.
- **Roles are not in Spring Security.** The JWT carries only the userId, and authorities are empty. Admin and team-role checks happen in each service (`verifyAdmin`). Do not add `@PreAuthorize`/`hasRole` without also putting roles into the token.
- **New errors:** add an `ErrorCode` constant under the right prefix section, taking the next unused number in that section, and throw it with `new BusinessException(ErrorCode.X)`. Do not return error bodies from controllers by hand.
- The security handlers use Jackson 3 (`tools.jackson.databind.ObjectMapper`), not `com.fasterxml.jackson.databind`.
- `JwtAuthenticationFilter` only sets the SecurityContext. It does not set a request attribute, whatever older notes may say.
- Access and refresh tokens are signed the same way and have the same shape. The only difference is expiry, and refresh tokens are checked against `User.refreshToken`. **Known security gap:** `JwtAuthenticationFilter` accepts any validly signed token, so a 7-day refresh token also works as a Bearer access token. The fix would be a token-type claim (such as `typ`) that the filter checks.
- `iat` has one-second precision, so two tokens for the same user issued in the same second are identical strings.

### Testing Requirements
- Controller slice tests must `@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class})` and mock both filters (handled by `ControllerTestSupport`). If `SecurityConfig` gains a new constructor dependency, update those imports or mocks.
- Changing a status code in `ErrorCode` can break controller tests that assert on status.

## Dependencies

### Internal
- `domain/*` services throw `BusinessException`. Controllers rely on the principal type set here (`Long`).

### External
- Spring Security, jjwt 0.12.x (`Jwts.parser().verifyWith(...)` API), springdoc-openapi, QueryDSL JPA

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
