# domain

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Business features. Each feature package holds its JPA entity and enums at the root, plus `controller/`, `service/`, `repository/`, and `dto/{request,response}/`.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `user/` | Signup with admin approval, login, RTR token reissue, logout, profile, admin user search and delete (see `user/AGENTS.md`) |
| `post/` | Admin-authored notice board CRUD (see `post/AGENTS.md`) |
| `performance/` | Performances, the top-level grouping for teams and schedules (see `performance/AGENTS.md`) |
| `team/` | Teams within a performance, and members with LEADER/DEPUTY/MEMBER roles (see `team/AGENTS.md`) |
| `schedule/` | Practice slot requests, the next-week deadline, and automatic and manual room assignment (see `schedule/AGENTS.md`) |

## Entity Relationships
```
Performance 1─N Team 1─N TeamMember N─1 User
Performance 1─N ScheduleRequest N─1 Team
                ScheduleRequest N─1 User (submittedBy)
Post N─1 User
```
No relationship uses JPA cascade or orphanRemoval. Services do deletes explicitly, child rows first.

## For AI Agents

### Working In This Directory
- **Cascade deletes are hand-written.** If you add an entity with a FK to `User`, `Team`, or `Performance`, also delete its rows in:
  - `UserService.deleteUser`: team members, then posts, then schedule requests, then the user
  - `TeamService.delete`: schedule requests, then team members, then the team
  - `PerformanceService.delete`: schedule requests, then each team's members, then teams, then the performance

  If you skip this, deletes fail with FK violations in MySQL.
- **Authorization lives in services.** Controllers pass `@AuthenticationPrincipal Long userId` into the service. Each service has its own private `verifyAdmin`, which throws `ADMIN_ONLY`. It takes a `Long userId` everywhere except `PostService`, where it takes a `User`. Team-role checks go through `TeamMemberRepository.findByTeamAndUser`.
- **Cross-domain access goes through repositories, not services.** For example, `UserService` injects `TeamMemberRepository`, `PostRepository`, and `ScheduleRequestRepository`.
- Services are `@Transactional(readOnly = true)` at class level, with `@Transactional` on each write method. Updates rely on dirty checking through entity methods (`approve`, `updateRole`, and so on), not `save`. **If you forget the method-level `@Transactional`, the change is silently not saved**, because read-only sets flush mode to MANUAL, and Mockito tests will still pass. See `user/AGENTS.md`.

### Common Patterns
- Entities: `@Getter @NoArgsConstructor`, `@Builder` on a constructor that omits `id` and audit fields, `@EntityListeners(AuditingEntityListener.class)`, and `@CreatedDate createdAt`. No setters; state changes go through intent-named methods.
- Enums are stored with `@Enumerated(EnumType.STRING)`.
- Response DTOs are built from entities (`new XxxResponse(entity)`). Request DTOs use `@Getter @NoArgsConstructor` plus Bean Validation and `@Schema` annotations with Korean descriptions and examples.
- Controllers carry springdoc `@Tag`, `@Operation`, and `@ApiResponses` in Korean. `AuthController` and `PostController` add an empty `@SecurityRequirements` on public endpoints to hide the lock icon. The public GETs in schedule, team, and performance don't yet.
- Paged lists in user and auth use `@PageableDefault(size = 10, sort = "createdAt", direction = DESC)`. Schedule lists use `size = 20, sort = "startAt"`.

### Testing Requirements
- Every service gets a Mockito unit test and every controller a `@WebMvcTest`. See `src/test/java/com/example/ToyProject_Board/AGENTS.md`.

## Dependencies

### Internal
- `global/exception` (errors), `global/jwt` (used by `AuthService`), `global/config` (QueryDSL)

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
