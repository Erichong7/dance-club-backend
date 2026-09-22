# src

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Standard Gradle source layout: application code, runtime config, and tests.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `main/java/com/example/ToyProject_Board/` | Application code (see its `AGENTS.md`) |
| `main/resources/` | `application.yml` runtime config (see `main/resources/AGENTS.md`) |
| `test/java/com/example/ToyProject_Board/` | Unit, slice, and integration tests (see its `AGENTS.md`) |
| `test/resources/` | `application.yml` for tests: H2 in-memory database, `ddl-auto: create-drop`, fixed JWT secret |

## For AI Agents

### Working In This Directory
- Test packages mirror main packages. Put a new test in the same package path as the class under test. Test-only helpers (fixtures, `support/`) live alongside.
- `test/resources/application.yml` fully overrides the datasource and JWT settings, so tests never read env vars.

### Testing Requirements
- `./gradlew test` from the repo root.

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
