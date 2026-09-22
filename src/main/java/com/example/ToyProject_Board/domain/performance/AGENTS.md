# performance

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Performances (공연) are the top-level unit. Teams belong to a performance, and schedule requests are scoped to one. Admins create and delete them, and anyone can read them.

## Key Files
| File | Description |
|------|-------------|
| `Performance.java` | Entity (`performances`): name, performanceDate (`LocalDate`), description, `@OneToMany(mappedBy="performance") teams` (inverse side, no cascade) |
| `controller/PerformanceController.java` | `/api/performances`: `POST` (admin), `GET`, `GET {id}` (public), `DELETE {id}` (admin, 204) |
| `service/PerformanceService.java` | create, getAll, getOne, and delete (deletes schedule requests, then each team's members, then teams, then the performance) |
| `repository/PerformanceRepository.java` | Plain `JpaRepository` |
| `dto/request/PerformanceCreateRequest.java` | name and performanceDate required, description optional |
| `dto/response/PerformanceResponse.java` | Built from the entity |

## For AI Agents

### Working In This Directory
- Deleting a performance deletes everything under it, by hand. If a new entity references `Performance` or `Team`, add it to `PerformanceService.delete` before the parent rows are removed.
- Not-found here throws `PERFORMANCE_NOT_FOUND` (PF001). `TeamService.createTeam` throws `TEAM_PERFORMANCE_NOT_FOUND` (T003) for the same situation. Keep whichever code the calling domain already uses.

### Testing Requirements
- `PerformanceServiceTest` (Mockito) with `PerformanceFixture`. There is no controller test yet.

## Dependencies

### Internal
- `team/` (Team, TeamRepository, TeamMemberRepository), `schedule/repository/ScheduleRequestRepository`, `user/repository/UserRepository`

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
