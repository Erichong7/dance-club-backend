# post

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
A simple notice board. Only admins create, update, or delete posts. Anyone, including anonymous users, can list and read them.

## Key Files
| File | Description |
|------|-------------|
| `Post.java` | Entity (`posts`): title, `TEXT` content, LAZY `user` (author), `createdAt`/`updatedAt` auditing. Changed through `update(title, content)` |
| `controller/PostController.java` | `/api/posts`: `POST`, `GET` (paged), `GET {id}`, `PUT {id}`, `DELETE {id}` |
| `service/PostService.java` | create, getList, getOne, update, delete. Every write calls `verifyAdmin` |
| `repository/PostRepository.java` | JPA repo plus `deleteByUser` (used by user deletion) |
| `dto/request/` | `PostCreateRequest`, `PostUpdateRequest` (title and content required) |
| `dto/response/` | `PostResponse` (detail), `PostListResponse` (list item) |

## For AI Agents

### Working In This Directory
- Posts are admin-only for writes. Any admin can edit any post; there is no author-ownership check. Keep it that way unless asked.
- All `GET /api/posts/**` routes are public through `SecurityConfig`. A new GET under this prefix is public automatically.
- CLAUDE.md and README use `PostServiceTest.createSuccess` as a single-method test example, but no such method exists. The real names are Korean, for example `PostServiceTest.게시글_작성_성공`.

### Testing Requirements
- `PostServiceTest` (Mockito) and `controller/PostControllerTest` (`@WebMvcTest`, validation-failure cases for blank title and content).

## Dependencies

### Internal
- `user/User`, `user/repository/UserRepository`, `global/exception`

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
