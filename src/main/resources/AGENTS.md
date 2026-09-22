# resources

**Parent context:** `../../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
Runtime configuration for the application. It is used locally and in production.

## Key Files
| File | Description |
|------|-------------|
| `application.yml` | Datasource from `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`, `ddl-auto: update`, `show-sql: true`, and JWT settings (`JWT_SECRET`; access defaults to 30 min, refresh to 7 days, both in ms) |

## For AI Agents

### Working In This Directory
- Never hard-code secrets here. Add new settings as `${ENV_VAR:default}` placeholders.
- Do not change `ddl-auto` to `create`/`create-drop` in this file. Production reads it, and a previous fix exists specifically to prevent a DB wipe on deploy.
- `JWT_SECRET` must be at least 256 bits (32+ bytes). A *missing* secret fails at startup (unresolved placeholder). A *too-short* secret does not: `Keys.hmacShaKeyFor` runs on each token operation, so it only fails at the first login or authenticated request.
- When you add a property, add the matching one to `src/test/resources/application.yml` too. Otherwise `@SpringBootTest` fails to resolve the placeholder.
- There are no profiles (`application-*.yml`). Local and production differ only through env vars.

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
