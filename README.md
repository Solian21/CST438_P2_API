# CST 438 Project 02: API Template

Starter repo for Project 02. **One person per team** clicks **Use this template → Create a new
repository**, names it after your API, and adds teammates as collaborators.

> Replace this README with your own in Sprint 4. The prompt lists what it must contain.

## What's in here

| Path | What it is | When |
|---|---|---|
| `docs/proposal.md` | Proposal template: pitch, resources, ER sketch, endpoints, choices, risks | **Thu 10/1** |
| `docs/openapi.yaml` | A complete example contract (workout tracker). Replace it with your API. | Sprint 1 |
| `redocly.yaml` | Lint rules for the contract | Sprint 1 |
| `.github/ISSUE_TEMPLATE/user_story.md` | Every new issue starts as a user story with acceptance criteria | now |
| `.github/pull_request_template.md` | Every PR starts with `Closes #` and a test checklist | now |
| `.gitignore` | Gradle, Android, IntelliJ, Node, and **secrets** (`.env`, keystores, `local.properties`) | now |

Where your code goes is up to you: an `api/` and an `android/` folder in this repo (monorepo),
or a second repo for the Android app. Write it down in an ADR.

## First 15 minutes

- [ ] Add teammates: **Settings → Collaborators**
- [ ] Protect `main`: **Settings → Rules → Rulesets → New branch ruleset**. Target the default branch,
      require a PR with **2 approvals** (1 for a team of 3), block force pushes. Enforcement: **Active**.
- [ ] Create milestones `Sprint 1` through `Sprint 4`: **Issues → Milestones**
- [ ] Create a Project board (Kanban) and link it: **Projects tab → Link a project**
- [ ] Start `docs/proposal.md` on a branch:

```bash
git switch -c docs/proposal
# edit docs/proposal.md
git add docs/proposal.md
git commit -m "Draft proposal"
git push -u origin docs/proposal
gh pr create --fill
```

## Run locally with Docker

Starts the API on http://localhost:8080 and a local Postgres on port 5432 (dev only; the deployed API uses Neon):

```bash
docker compose up --build        # Ctrl+C to stop
curl http://localhost:8080/actuator/health
docker compose down              # add -v to wipe the local database
```

## Coverage

`./gradlew check` (run in `api/`) fails if line coverage on `edu.csumb.cst438.api.service` is below 70%.
The report is at `api/build/reports/jacoco/test/html/index.html`.

## Working with the contract

No installs needed beyond Node (`npx` runs the tools).

```bash
# Lint: fix every error before you open a PR
npx @redocly/cli lint docs/openapi.yaml

# Mock server: a fake API built from the contract, at http://127.0.0.1:4010
npx @stoplight/prism-cli mock docs/openapi.yaml
```
The Prism mock server runs at http://127.0.0.1:4010


The mock **validates** your requests and returns the `example:` values from the contract.
**Nothing is saved**: a POST followed by a GET returns the example, not what you posted. The
Android emulator reaches it at `http://10.0.2.2:4010`.

To see the contract rendered, paste `docs/openapi.yaml` into https://editor.swagger.io.

## Links

- Project 02 prompt and rubric: Canvas → Project 02 module
- OpenAPI 3.0 spec: https://spec.openapis.org/oas/v3.0.3
- Prism: https://docs.stoplight.io/docs/prism
- Mermaid ER diagrams: https://mermaid.js.org/syntax/entityRelationshipDiagram.html
- RFC 9457 Problem Details: https://www.rfc-editor.org/rfc/rfc9457

## Project Technologies
- Spring Boot/Java: backend API
- Neon PostgreSQL: shared database
- Kotlin: Android frontend
- Node.js/npx: OpenAPI linting and Prism mock server

## Running the Spring Boot API
1. Create an `.env` file inside the `api` folder:

```properties
DATABASE_URL=jdbc:postgresql://<neon-host>/neondb?sslmode=require&channelBinding=require
DB_USER=neondb_owner
DB_PASS=<your-neon-password>
