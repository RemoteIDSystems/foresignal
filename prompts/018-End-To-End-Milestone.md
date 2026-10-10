# 018 — End To End Milestone

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/018-CR-End-To-End-Milestone.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/017-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Product-Charter.md` §14 (success criteria)
- `docs/Architecture.md` (whole, for the operator guide)
- All Completion Reports so far (`completion-reports/001…017`). Reconcile any open issues they list that are in scope here.

## Objective

Prove the core (prompts 001–017) with the milestone, written as an end-to-end test:

> enable USGS through the API → quakes are ingested → a revision creates a new version → a `mag >= 6` rule fires → the alert arrives on SSE and at a notification channel

Then make the project usable by a human: README quickstart, operator guide and a smoke script.

## Tasks

### 1. End-to-end test (`ai.elaro.e2e.MilestoneE2EIT`)

Full application context (`@IntegrationTest`, real HTTP on a random port), PostGIS via Testcontainers, and WireMock serving USGS fixtures (base URL override mechanism from prompt 009). Steps, each with explicit assertions:

1. Read the admin token captured at startup (expose a test-only hook, or set a known hash in test config; document the approach). Request a write without the token → 401.
2. `GET /v1/sources` → `usgs-earthquakes` is present, `enabled=false`, status `disabled`.
3. `POST /v1/triggers` with `{name:"Big quake", source:"usgs-earthquakes", conditions:[{path:"mag",op:">=",value:6}], messageTemplate:"M{{payload.mag}} {{payload.place}}"}` → 201.
4. `POST /v1/channels` with `{type:"webhook", name:"e2e", config:{url:<second WireMock receiver>}, secrets:{hmacSecret:"s3cret"}, events:["alert"]}` → 201. Also create a `slack` channel pointing at a third WireMock stub, to prove a formatted channel type.
5. Open SSE connections: `/v1/changes/stream?source=usgs-earthquakes` and `/v1/alerts/stream`.
6. `PUT /v1/sources/usgs-earthquakes {"enabled":true}`. Within 5 s:
   - change events arrive for every fixture feature
   - `GET /v1/records?source=usgs-earthquakes&format=geojson` returns the FeatureCollection
   - `GET /v1/sources/usgs-earthquakes/health` returns `healthy`
   - usage shows ≥ 1 request
7. Switch the WireMock stub to a fixture in which one feature is revised to `mag 6.4`. Trigger `POST /v1/sources/usgs-earthquakes/fetch`. Then:
   - a version-2 change event arrives
   - `/versions` shows 2 versions
   - an `alert` SSE event arrives with the rendered message
   - the webhook receiver gets a POST with a valid HMAC signature, and the Slack stub gets a Block Kit payload containing the rendered message
8. `GET /v1/alerts` lists it. `POST /v1/alerts/{id}/ack` sets it acknowledged.
9. `GET /v1/tiles/earthquake/0/0/0.mvt` → 200 with features.
10. `PUT /v1/sources/usgs-earthquakes {"enabled":false}` → no further upstream requests over the next 5 s (WireMock request count is stable).
11. `PUT /v1/settings/retention` with a short window, then `POST /v1/settings/retention/run` → counts returned.

### 2. Smoke script (`scripts/smoke.sh`)

A POSIX shell script using `curl` and `jq` against a running instance (`ELARO_URL`, default `http://localhost:8080`; `ELARO_TOKEN` env). It:

- checks `/v1/system`
- lists sources
- enables `usgs-earthquakes`
- waits for records
- creates and deletes a test trigger
- prints a summary

It exits non-zero on failure. This will be used in prompt 062.

### 3. Documentation

- **`README.md` quickstart:**
  - `docker compose up -d` → find the admin token with `docker compose logs elaro | grep -A3 "ELARO ADMIN TOKEN"` → enable USGS with curl → watch the stream with `curl -N localhost:8080/v1/changes/stream`
  - local development: `docker compose up -d db` + `./gradlew bootRun`
  - links to `/docs` and the docs folder
  - Postman: import the collection and environment, set `adminToken`, run the folder "00 Quickstart — USGS end to end" (also runnable headless: `npx newman run postman/Elaro.postman_collection.json --folder "00 Quickstart — USGS end to end" --env-var adminToken=$ELARO_TOKEN`)
- **`docs/Operator-Guide.md`:** create or extend it. Sections:
  - first start and the token
  - rotating and resetting the token
  - enabling sources and setting credentials
  - budgets and how to read health and usage
  - retention
  - triggers (examples: magnitude threshold, geofence polygon, onlyNew)
  - notification channels: every type, with setup steps per destination (Slack/Mattermost incoming webhooks, Discord, Teams workflows, Telegram bot, Matrix, ntfy, Gotify, Pushover, email), plus webhook signature verification
  - streaming for frontends (SSE resume, filters)
  - tiles
  - export/import
  - backups (`pg_dump` plus `secret.key`; note that **credentials cannot be decrypted without `secret.key`**)
  - reverse proxy (Caddy example, with SSE buffering off)
  - troubleshooting with `/v1/sources/health` and probe
- **Docs index:** update `docs/Roadmap.md` §4 to mark prompts 001–018 as **Done**, using the commit hashes from the Completion Reports.

## Tests to write (do not run)

- `MilestoneE2EIT` as above. Make it robust: Awaitility with explicit timeouts, and SSE readers on background threads with bounded waits.
- `scripts/smoke.sh` is not unit tested. Make sure it is `shellcheck`-clean if `shellcheck` is available; it is not required.

## Acceptance criteria

- The E2E test covers every step above.
- README, Operator Guide and smoke script exist.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `018: End To End Milestone`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/018-CR-End-To-End-Milestone.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `018: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
