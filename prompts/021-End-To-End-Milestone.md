# 021 — End To End Milestone

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/021-CR-End-To-End-Milestone.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/020-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Product-Charter.md` §14 (success criteria)
- `docs/Core-Data-Model.md` *Per-prompt changes* → "018 End To End Milestone" (= 021), §5.3 (policy defaults), §13 (tiers and windows), §16 (binds, storage projection, NTP)
- `docs/Gap-Analysis.md` §6 (Remote access / TLS, mDNS, Backups rows) and §8.A item 11 ("extend 018" = 021)
- `docs/Architecture.md` (whole, for the Operator Guide)
- All Completion Reports `001…020`. Reconcile the open issues they list that are in scope here and record each in the CR.

## Objective

Prove the core (001–020) with one end-to-end test:

> enable USGS through the API → quakes are ingested → an identical re-poll is only a touch → a revision persists version 2 → a `mag >= 6` rule fires → the alert arrives on SSE and at three channels → asOf still shows version 1 → the alert resolves and the Discord message is edited in place

Then make the project usable by a human: README quickstart, Operator Guide (including remote access, mDNS, backups, NTP and storage), and a smoke script.

## Tasks

### 1. End-to-end test (`ai.foresignal.e2e.MilestoneE2EIT`)

Full context (`@IntegrationTest`, real HTTP on a random port), the Foresignal DB image via Testcontainers, WireMock serving USGS fixtures (base-URL override from 011) and separate WireMock receivers for the channels. Use field names exactly as 017 and 018 built them (check their CRs). Seed nothing with raw SQL. Steps, each with explicit assertions:

1. Obtain the admin token (a test-only hook or a known hash in test config; document which). A write without it → 401.
2. `GET /v1/sources` → `usgs-earthquakes` present, `enabled=false`, status `disabled`, `historyTier=STANDARD`, `licence` set.
3. `POST /v1/triggers`: `{name:"Big quake", sources:["usgs-earthquakes"], mode:"enter", autoResolve:true, conditions:[{path:"mag",op:">=",value:6}], messageTemplate:"M{{payload.mag}} {{payload.place}}"}` → 201.
4. `POST /v1/channels` three times: `webhook` (`hmacSecret:"s3cret"`, `events:["alert"]`), `slack`, and `discord` (its stub returns `{"id":"111"}` for `?wait=true`), each pointing at its own receiver.
5. Open SSE: `/v1/changes/stream?source=usgs-earthquakes&touches=true` and `/v1/alerts/stream`.
6. `PUT /v1/sources/usgs-earthquakes {"enabled":true}`. Within 5 s: one `change` per fixture feature (`persistReason=FIRST`); `GET /v1/records?source=usgs-earthquakes&format=geojson` returns the FeatureCollection with `watermarkSeq`; health `healthy`; usage ≥ 1 request; `GET /v1/sources/usgs-earthquakes/fetches` shows one `OK` row with `versionsWritten` = feature count.
7. `POST /v1/sources/usgs-earthquakes/fetch` with the identical fixture → **no** `change` event (bounded wait), at least one `touch` event, the newest fetch row has `versionsWritten = 0`, and every record is still version 1.
8. Record `T1 = now`. Switch the stub to a fixture where one feature becomes `mag 6.4`; fetch. Then: a version-2 `change` with `persistReason=SIGNIFICANT`; `/versions` shows 2; an alert `fired` SSE event with the rendered message; the webhook receiver gets a POST with a valid `X-Foresignal-Signature` and an `X-Foresignal-Origin` header; Slack gets Block Kit containing the message; Discord gets a POST with `?wait=true`; a `notification` row exists.
9. `GET /v1/records?source=usgs-earthquakes&asOf=<T1>` → the revised feature at version 1 with its old magnitude, `asOf` and `horizon` in the response.
10. `GET /v1/alerts` lists the alert; `POST /v1/alerts/{id}/ack` → acknowledged.
11. Switch to a fixture where the feature becomes `mag 5.8`; fetch. Then: version 3; the alert is `resolved` (SSE lifecycle event); Discord receives `PATCH …/messages/111`; Slack receives a follow-up message.
12. `GET /v1/tiles/earthquake/0/0/0.mvt` → 200 with features.
13. `GET /v1/system` anonymously → no `history`; with the admin token → `history` reports two tiers and `warnings` is an array.
14. `PUT /v1/sources/usgs-earthquakes {"enabled":false}` → WireMock request count stable over 5 s.
15. `POST /v1/settings/retention/run` → a `RetentionRunResult` with counts; nothing current is deleted (all records still readable).

Make it robust: Awaitility with explicit timeouts, SSE readers on background threads with bounded queues and waits, and a "no event" assertion that waits a fixed bounded time after a heartbeat.

### 2. Smoke script (`scripts/smoke.sh`)

POSIX shell with `curl` and `jq` against a running instance (`FORESIGNAL_URL`, default `http://localhost:8080`; `FORESIGNAL_TOKEN`): check `/v1/system` and `/actuator/health/readiness`; list sources; enable `usgs-earthquakes`; wait for records and assert the page carries a numeric `watermarkSeq`; create and delete a test trigger; print a summary. Exit non-zero on failure. Prompt 137 uses it. `shellcheck`-clean if available.

### 3. Deployment additions

- **Compose `tailscale` profile:** a `tailscale/tailscale` service (pin a version verified on Docker Hub) with `TS_AUTHKEY` (operator env), `TS_HOSTNAME=foresignal`, `TS_STATE_DIR=/var/lib/tailscale` (named volume), `TS_SERVE_CONFIG=/config/serve.json`, and `docker/tailscale/serve.json` proxying HTTPS 443 on the tailnet name to `http://foresignal:8080`. Foresignal's own port stays bound to `${FORESIGNAL_BIND:-127.0.0.1}`.
- `docker/caddy/Caddyfile.example`: reverse proxy with automatic HTTPS (or `tls internal` for LAN), `flush_interval -1` for SSE, and a comment that forwarded headers are trusted only after 023's trusted-proxy setting. Prompt 050 adds the Caddy compose profile.
- `docker/avahi/foresignal.service`: an Avahi service file advertising `_http._tcp` and `_foresignal._tcp` on port 8080 with TXT `path=/v1` and `api=v1` only. **Never put the install id or any stable identifier in TXT records.**

### 4. Documentation

- **`README.md` quickstart:** `docker compose up -d` → admin token via `docker compose logs foresignal | grep -A3 "FORESIGNAL ADMIN TOKEN"` → enable USGS with curl → `curl -N localhost:8080/v1/changes/stream`; local development (`docker compose up -d db` + `./gradlew bootRun`); links to `/docs`, the docs folder and the Operator Guide; Postman (import collection and environment, set `adminToken`, run "00 Quickstart — USGS end to end", or headless with `npx newman run postman/Foresignal.postman_collection.json --folder "00 Quickstart — USGS end to end" --env-var adminToken=$FORESIGNAL_TOKEN`).
- **`docs/Operator-Guide.md`** (create or extend; keep sections written by 018 and 019):
  - first start and the token; rotating and resetting it
  - enabling sources, credentials, budgets, reading health, usage and `/fetches`
  - **ingest policies, tiers and windows:** ALL/SAMPLED/LATEST, keyframes and the retention keyframe, STANDARD vs TRACK, `trackWindow`, per-kind retention, why TRACK rejects per-source retention
  - triggers (magnitude threshold, geofence polygon, enter/exit with auto-resolve, cooldown) and the alert lifecycle
  - notification channels (link to 018's section), streaming for frontends (snapshot + `watermarkSeq` + SSE resume, `reset`, `resync`, `410 RESYNC_REQUIRED`), tiles, asOf
  - export/import
  - **Storage projection:** at full load expect about 10–15 GB/day of history writes plus 15–20 GB/day of WAL; a 3d track window holds about 40M rows (≈ 20 GB with indexes); read `/v1/system.history` and health `storage.projectedBytes`; the PostgreSQL flags from Core §16.
  - **Clock and NTP:** the host must sync time before Foresignal starts (chrony or systemd-timesyncd; check `timedatectl`); the writer's clock guard holds ingest after a backward clock step; forward jumps only alarm.
  - **Binds:** 8080 and 5433 bind to `127.0.0.1` by default; LAN access needs `FORESIGNAL_BIND=0.0.0.0`; read the `/v1/system` warnings when `openReads` is on.
  - **Remote access & TLS:** Tailscale Serve (the `tailscale` profile, or `tailscale serve --bg 8080` on the host) as the recommended path; Caddy as the alternative; set `publicBaseUrl` to the HTTPS name; never expose 5433; port-forwarding without TLS is unsupported.
  - **mDNS:** install the Avahi file, what it advertises and why it carries no identifier.
  - **Backups:** `pg_dump -Fc` (partitioned history dumps and restores normally) plus `${FORESIGNAL_DATA_DIR}/secret.key`; **credentials and channel secrets cannot be decrypted without `secret.key`**; a restore drill; stop Foresignal before restoring.
  - troubleshooting with `/v1/sources/health`, probe, readiness and the catch-up group.
- **`docs/Roadmap.md` §4:** mark 001–021 **Done**.

## Tests to write (do not run)

- `MilestoneE2EIT` as above.
- `ComposeFilesTest` (unit): parses `docker-compose.yml`; default binds are `127.0.0.1`; the `tailscale` service exists only under its profile; `foresignal` has a readiness healthcheck. Parses `docker/avahi/foresignal.service` and asserts no TXT record contains the install id pattern.

## Acceptance criteria

- The E2E test covers every step above, including the touch-only re-poll, asOf and the in-place edit.
- README, Operator Guide sections, smoke script, the tailscale profile, the Caddy example and the Avahi file exist.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `021: End To End Milestone`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/021-CR-End-To-End-Milestone.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `021: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
