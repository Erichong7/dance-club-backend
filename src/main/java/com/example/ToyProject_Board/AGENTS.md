# ToyProject_Board (application root package)

**Parent context:** `../../../../../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Root package of the Spring Boot application. Business features live in `domain/` and cross-cutting infrastructure in `global/`.

## Key Files
| File | Description |
|------|-------------|
| `ToyProjectBoardApplication.java` | `@SpringBootApplication` entry point. Component scan covers everything below this package |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `domain/` | Feature modules: user, post, performance, team, schedule (see `domain/AGENTS.md`) |
| `global/` | Security, JWT, exception handling, logging, and config (see `global/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- Keep new beans under this package so component scanning finds them.
- Request path: `RequestLoggingFilter`, then `JwtAuthenticationFilter`, then Spring Security authorization (`SecurityConfig`), then the controller. The controller gets the caller via `@AuthenticationPrincipal Long userId`, and the service does role and ownership checks.

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
