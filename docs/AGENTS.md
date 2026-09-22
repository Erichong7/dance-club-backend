# docs

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Korean-language reference docs, maintained by hand. Each file opens with a warning that it is a snapshot and that Swagger UI (`/swagger-ui.html`) is authoritative. They are excluded from the Docker image by `.dockerignore`.

## Key Files
| File | Description |
|------|-------------|
| `API_SPEC.md` | Endpoint-by-endpoint request and response spec for auth, user, performance, team, and schedule, plus the common error format |
| `IMPLEMENTATION.md` | Domain structure, business rules (signup approval, team roles, next-week deadline, room-assignment priority), and design notes |

## For AI Agents

### Working In This Directory
- When you change an endpoint, a validation rule, an `ErrorCode`, or scheduling logic, update the matching section here in the same change. Nothing enforces sync, so these docs drift silently.
- Write in Korean to match the existing text, and keep the snapshot warning at the top of each file.
- The code decides what is correct. If a doc disagrees with the code, fix the doc unless the user says the code is wrong.

### Testing Requirements
None. These are docs only.

## Dependencies

### Internal
- Describes `src/main/java/com/example/ToyProject_Board/domain/**` and `global/exception/ErrorCode.java`

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
