# team

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Dance teams inside a performance, and their members. A member's team role decides who may submit schedule requests (LEADER and DEPUTY) and who may delete them (LEADER).

## Key Files
| File | Description |
|------|-------------|
| `Team.java` | Entity (`teams`): name (**globally unique**, not per performance), `@ManyToOne performance` (EAGER by default, **nullable** at the DB level. The service always sets it, but `TeamFixture.create()` builds teams without one) |
| `TeamMember.java` | Entity (`team_members`), unique on (`team_id`, `user_id`). LAZY team and user, plus a `role`. Changed through `updateRole` |
| `TeamMemberRole.java` | `LEADER`, `DEPUTY`, `MEMBER` |
| `controller/TeamController.java` | `/api/teams`: `POST` (admin), `GET`, `GET {id}` (public, detail includes members), `POST {id}/members`, `PUT {id}/members/{targetUserId}/role`, `DELETE {teamId}`, `DELETE {id}/members/{targetUserId}` |
| `service/TeamService.java` | createTeam (duplicate name check, performance must exist), addMember and updateMemberRole (at most one LEADER and one DEPUTY per team), delete (schedule requests, then members, then team), removeMember |
| `repository/TeamRepository.java` | `existsByName`, `findByPerformance` |
| `repository/TeamMemberRepository.java` | Lookups by team, user, and role, plus `deleteByTeam`/`deleteByUser`/`deleteByTeamAndUser` |
| `dto/request/` | `TeamCreateRequest` (name, performanceId), `AddMemberRequest` (userId, role), `UpdateMemberRoleRequest` |
| `dto/response/` | `TeamResponse`, `TeamDetailResponse` (team + members), `TeamMemberResponse` |

## For AI Agents

### Working In This Directory
- All writes are admin-only (`verifyAdmin`). Team leaders cannot manage their own roster.
- One LEADER and one DEPUTY per team, checked in the service. There is no DB constraint. `updateMemberRole` checks whether *any* member already holds the target role, so re-assigning a user's current LEADER role to that same user fails with `TEAM_LEADER_EXISTS`. Hand over leadership by demoting the old leader first.
- A user can belong to several teams. `GET /api/users/me` returns all of them.
- A user who is LEADER of any team cannot be deleted (`UserService`, U011).

### Testing Requirements
- `TeamServiceTest` (Mockito) with `TeamFixture`, and `controller/TeamControllerTest` (`@WebMvcTest`).

## Dependencies

### Internal
- `performance/` (Performance, PerformanceRepository), `user/` (User, UserRepository), `schedule/repository/ScheduleRequestRepository`

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
