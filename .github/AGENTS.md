# .github

**Parent context:** `../AGENTS.md`
**Generated:** 2026-09-22 · **Updated:** 2026-09-22

## Purpose
GitHub Actions CI/CD. It deploys continuously to a single EC2 host.

## Key Files
| File | Description |
|------|-------------|
| `workflows/deploy.yml` | On push to `main`: build the image with the root `Dockerfile`, push `<DOCKERHUB_USERNAME>/dance-club-backend:latest`, SSH to EC2, replace the `dance-club-backend` container (port 8080, `--env-file /home/ubuntu/app.env`, `--restart unless-stopped`), and prune images older than 24h |

## For AI Agents

### Working In This Directory
- Every merge or push to `main` ships to production. The workflow runs no test step, so run `./gradlew test` locally before pushing.
- Required secrets: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `EC2_HOST`, `EC2_USERNAME`, `EC2_SSH_KEY`. Runtime env vars live on the host in `/home/ubuntu/app.env`, not in the repo.
- Keep the `docker image prune` step. It was added to stop the EC2 disk from filling up.
- A new required env var has to be added to `app.env` on the host before deploying. Otherwise the container fails at startup.

### Testing Requirements
Workflow changes can only be verified by pushing. Review diffs carefully and prefer small, reversible edits.

## Dependencies

### External
- `actions/checkout@v4`, `docker/login-action@v3`, `docker/build-push-action@v6`, `appleboy/ssh-action@v1.0.0`

## Manual Notes

Notes under this heading are written by humans and preserved on regeneration.
