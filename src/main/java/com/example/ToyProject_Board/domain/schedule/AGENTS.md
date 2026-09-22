# schedule

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
The core business logic. Team leaders and deputies request practice slots for next week. Admins run a greedy weekly assignment into rooms, or create approved slots manually. Requests can also be rejected, cancelled, re-roomed, or deleted.

## Key Files
| File | Description |
|------|-------------|
| `ScheduleRequest.java` | Entity (`schedule_requests`): performance, team, submittedBy, `startAt`/`endAt` (`LocalDateTime`), required `alternativeRoom`, nullable `assignedRoom`, `status` (default PENDING), `adminNote`. Transitions: `approve(room)`, `reject(note)`, `cancel()`, `reassignRoom(room)` |
| `RoomType.java` | `CLUB_ROOM` (동방), `STUDENT_UNION_BASEMENT` (학생회관 지하), `UNDERGROUND_PARKING`, `EXTERNAL`, `CHEER_ROOM` (치어룸) |
| `ScheduleStatus.java` | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED` |
| `controller/ScheduleController.java` | `/api/schedules`: `POST` (create), `GET` (by performance and week, public), `GET team/{teamId}/week` (public), `GET {id}` (public), `GET team/{teamId}` (members only), `POST {id}/cancel`, `POST {id}/reject`, `PUT {id}/room`, `POST assign/manual`, `POST assign`, `DELETE {id}` |
| `service/ScheduleService.java` | All rules below |
| `repository/ScheduleRequestRepository.java` | Derived queries by performance, team, `startAt` range, and status, plus `deleteBy{Team,Performance,SubmittedBy}` |
| `dto/request/` | `ScheduleCreateRequest` (performanceId, teamId, practiceDate, startTime, endTime, alternativeRoom), `ScheduleAssignRequest` (manual: same fields but `room`), `AssignRoomRequest`, `ScheduleRejectRequest` |
| `dto/response/ScheduleResponse.java` | Built from the entity |

## Business Rules (ScheduleService)
- **Who may create:** a LEADER or DEPUTY of the team (non-member → `NOT_TEAM_MEMBER`, MEMBER → `SCHEDULE_CREATE_FORBIDDEN`).
- **Time range:** date plus start and end `LocalTime`. If end is before start, the slot runs past midnight and `endAt` is on the next day. start == end → `SCHEDULE_INVALID_TIME_RANGE`.
- **Deadline (KST):** the deadline for a practice date is the Sunday before that date's Monday-start week. Today (in `Asia/Seoul`) after the deadline → `SCHEDULE_DEADLINE_PASSED`. A deadline later than this coming Sunday → `SCHEDULE_TOO_FAR_IN_ADVANCE`. In effect, only next week can be requested, until Sunday of the current week.
- **Weekly auto-assign (`assignWeek`):** takes PENDING requests in the 7 days from `weekStart`, ordered by `createdAt` ascending (first come, first served), and approves each one into the first room that fits:
  1. `CLUB_ROOM`, only if the team has had no club-room slot yet this week and there is no overlap. This gives each team at least one club-room slot.
  2. `STUDENT_UNION_BASEMENT`, if there is no overlap.
  3. `CHEER_ROOM`, only on Wednesday with the whole slot inside 18:30–20:30, at most 3 teams per day.
  4. Otherwise the request's `alternativeRoom`, with no conflict check.

  Overlap means `a.start < b.end && a.end > b.start`, checked on full `LocalDateTime` ranges so overnight slots compare correctly. Slots that were already APPROVED before the run are **not** loaded into the conflict lists.
- **Manual assign (`assign/manual`):** admin only. Creates an APPROVED request submitted as the team's LEADER (none → `TEAM_LEADER_NOT_FOUND`). Skips the deadline check.
- **Cancel:** the submitter or an admin. REJECTED or CANCELLED requests → `SCHEDULE_ALREADY_CLOSED`. **Reject:** admin, PENDING only. **Reassign room:** admin, APPROVED only. **Delete:** an admin or the team's LEADER (a DEPUTY cannot).

## For AI Agents

### Working In This Directory
- Always use `LocalDate.now(KST)` for date logic. Two recent fixes addressed server-timezone bugs because EC2 runs in UTC.
- `validateDailyLimit` (2 hours per team per day) and `MAX_DAILY_MINUTES` are commented out and refer to old field names (`practiceDate`/`startTime`). Rewrite against `startAt`/`endAt` before you re-enable them.
- The entity stores `startAt`/`endAt`. DTOs take `practiceDate` + `startTime`/`endTime`, and `toOvernightAwareRange` converts between them. Keep that conversion in one place.
- If you change the room priority or the cheer-room constants, update `docs/IMPLEMENTATION.md` and the README feature list.

### Testing Requirements
- `ScheduleServiceTest` (Mockito) covers deadlines, overnight ranges (`익일 종료`, overnight conflict detection, and overnight slots never landing in the cheer room), and assignment priority. Dates are computed from `LocalDate.now()` relative to next week, so never hard-code dates.
- `ScheduleServiceConcurrencyTest` is a `@SpringBootTest` on H2 that saves 50 requests in parallel threads.
- `controller/ScheduleControllerTest` (`@WebMvcTest`).

## Dependencies

### Internal
- `performance/`, `team/` (TeamMember roles), `user/`, `global/exception`

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
