# 020 — Inbound Webhooks And Custom Sources

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/020-CR-Inbound-Webhooks-And-Custom-Sources.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/019-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §16 (inbound webhooks and custom push sources)
- `docs/Product-Charter.md` §8.9 (FR-41, FR-42)
- `docs/OpenAPI.json`: operations tagged `Ingest`, schemas `CustomSource*`, `IngestRecords`, `IngestOutcome`, `IngestKeyResponse`, `NormalizedRecordPreview`, `FeatureCollection`

## Objective

Let anything push data into Elaro: local SDR decoders, ais-catcher, Home Assistant, scripts, n8n flows, OpenClaw agents. Pushed records get the same versioning, change stream, triggers, notifications, search and retention as polled data.

## Tasks

### 1. Custom sources (`ai.elaro.inbound`)

- Add `CustomSourceEntity` (`custom_source`) and a repository.
- **`CustomSourceService`:**
  - **create:** validate per `CustomSourceInput`. The id is `custom-…`, unique across *all* sources (static and dynamic); kinds follow the id pattern; field names follow `^[A-Za-z0-9_.]{1,64}$`. Then:
    - generate the ingest key (`eik_` + base64url(32 bytes), stored as a SHA-256 hash with a prefix)
    - persist
    - build a `SourceDescriptor` (mode PUSH, `custom=true`, `defaultBudget.perMinute = rateLimitPerMinute`, a generous per-day budget) and `SourceRegistry.registerDynamic(...)`
    - `SourceService` creates the default settings row with `enabled=true`, because push sources are enabled at creation
    - return the key once
  - **update:** the id is immutable. Removing a kind that has current (non-tombstoned, unexpired) records → 409 `KIND_IN_USE`. Re-register the descriptor.
  - **delete:** unregister, delete the row and settings, and disable ingest. Existing records stay until retention removes them.
  - **rotateKey.**
  - **Startup:** register all persisted custom sources before the scheduler and trigger engine start.
- `/v1/sources` lists custom sources with `custom=true`. Their health uses ingest outcomes: success when records are accepted, failure on a whole-request failure. Zero-result detection is off.

### 2. Ingest endpoint (`POST /v1/ingest/{sourceId}`)

- **Auth:** `X-Elaro-Ingest-Key` matching that source's key (constant-time), or a bearer token with `admin` scope. Prompt 003 marked this path `INGEST_KEY_OR_ADMIN`; implement the extension point now.
- **Target:** the source must exist and be in PUSH mode (custom or built-in), otherwise 404. A disabled source returns 409 `SOURCE_DISABLED`.
- **Limits:** body ≤ 5 MB (413 problem); ≤ 10,000 records. Inbound rate limit through `BudgetGovernor` (`perMinute`), returning 429 with `Retry-After`. Inbound requests count in usage like upstream requests, with bytes received.
- **Content:**
  - **GeoJSON FeatureCollection** (`application/json` or `application/geo+json`), mapped as:
    - `id` (string or number) becomes `externalId`; a feature without an id is rejected
    - properties `kind` (optional when the source has exactly one kind)
    - `observedAt` (ISO-8601; defaults to the request time when absent)
    - `validUntil` (optional)
    - `tombstone` (optional bool)
    - every other property goes into the payload
  - **`{"records": [...]}`** in the `NormalizedRecordPreview` shape, with `geometry` as GeoJSON.
- **Processing:** convert to `NormalizedRecord`s, submit to `IngestQueue` (timeout 30 s, otherwise 503 `INGEST_BUSY`) and wait for the `IngestOutcome`. Per-record rejections (normalizer rules from prompt 007, plus a missing kind or id) are returned as `rejections[{index, externalId, reason}]` with HTTP 200 when at least the request was valid.
- Record which ingest key or token was used in the audit log.

### 3. Documentation

Add an "Inbound webhooks" section to `docs/Operator-Guide.md` covering:

- creating a custom source
- curl examples for both formats
- a Home Assistant `rest_command` example
- a Python snippet
- an ais-catcher-style example (GeoJSON points)
- how pushed records interact with triggers (e.g. a geofence rule on a custom kind)

## Tests to write (do not run)

- `CustomSourceServiceIT`: create (key returned once, descriptor registered, settings enabled), duplicate id against static ids → 409, update with `KIND_IN_USE`, delete unregisters, rotate key, re-registration at startup.
- `IngestControllerIT`:
  - both formats, both content types
  - key auth, admin-token auth, wrong key → 401
  - non-push source → 404; disabled → 409
  - 5 MB+ → 413; > 10,000 records → 400
  - rate limit → 429 with `Retry-After`
  - per-record rejections reported
  - ingest creates versions and change events
  - a trigger rule on the custom kind fires
- `GeoJsonIngestMapperTest`: property mapping, defaults, numeric ids, single-kind default, tombstones.

## Acceptance criteria

- A curl POST of a FeatureCollection to a new custom source produces records visible in `/v1/records`, on the SSE change stream, and in alerts when a rule matches.
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
2. **Commit** all changes with the message `020: Inbound Webhooks And Custom Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/020-CR-Inbound-Webhooks-And-Custom-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `020: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
