# 013 — Sources API

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/013-CR-Sources-API.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/012-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/OpenAPI.json`: **every** operation tagged `Sources` (paths `/v1/sources…`) and their schemas. This is the contract; match paths, methods, status codes, field names and operationIds exactly, after applying the spec edits in task 5.
- `docs/Core-Data-Model.md` §2 (time settings), §3 (descriptor additions), §5.1 (policy JSON and validation), §8 (`fetch_log`, `source_usage`, `source_health` additions), §13 (windows, `RETENTION_TIER_FIXED`), §14 (licence exposure), §15 (health `storage`), *Per-prompt changes* → the *Sources API* entry, *Spec changes*
- `docs/Architecture.md` §10 (API conventions)
- Completion Reports of 004 (descriptor, `IngestPolicyValidator`, `SourceService`), 005 (`FetchTrace`, usage counters), 006 (health view), 008 (descriptor extras), 010 (`FetchExecutor`, `fetch_log` rows, probe previews)

## Objective

Expose source management, the subscription control point, over REST: the catalogue with every v1.2 descriptor field, settings including the ingest policy, credentials, fetch-now, probe, health with coverage and storage, usage, and the per-fetch provenance log. Operators use this surface to see what each source costs in storage and to tune its policy.

## Tasks

### 1. DTOs and mappers (`ai.elaro.api.dto`, `ai.elaro.api.mapper`)

Java records mirroring the OpenAPI schemas: `SourceSummary`, `SourceDetail`, `SourceDescriptorDto`, `SourceSettingsDto`, `SourceSettingsUpdate`, `CredentialsUpdate`, `SourceHealthDto`, `UsageSeries`, `UsagePoint`, `FetchOutcomeDto`, `ProbeResultDto`, `NormalizedRecordPreviewDto`, `ParamSpecDto`, `CredentialFieldDto`, `BudgetDto`, plus new `IngestPolicyDto`, `MotionSpecDto`, `FieldThresholdDto`, `CoverageDto`, `QualityDto`, `ObserverDto`, `StorageDto`, `FetchLogEntryDto`, `FetchPage`.

- Durations are serialized as shorthand (prompt 002 serializers). Mappers live in `ai.elaro.api.mapper`; controllers stay thin. Bean Validation annotations on input DTOs where the spec has constraints.
- **`SourceDescriptorDto`** carries every descriptor field: the v1.1 set plus `licence`, `termsUrl`, `historyTier` (`STANDARD|TRACK`), `observedAtMode`, `ttlAnchor`, `defaultIngestPolicy`, `volatileFields`, `significantFields`, `motion`, `thresholds`, `defaultRetention`, `kindRetention`, `skewTolerance`, `allowNullIsland`, `mode` (now including `DERIVED`), `products` (ids only). Also the 008 extras if present on the descriptor: `titleField`, `entityKeys`, `fieldSpecs`, `kindDisplay`, `aggBelowZoom`, `startField`, `endField`, `private`. Check the descriptor record; map whatever 008 added and record any field that is missing in the Completion Report.
- **`IngestPolicyDto`** is a discriminated union on `mode` (`ALL|SAMPLED|LATEST`) with `minInterval`, `keyframe` (a duration or the string `none`), `minDeviationMeters`, `thresholds` (bool), `edgeMinInterval`. Reuse 004's `IngestPolicyCodec` for parsing so the API and `source_settings.ingest_policy` share one JSON shape.
- **`SourceSettingsDto`** adds `ingestPolicy` (nullable; the operator override) and two read-only fields: `effectiveIngestPolicy` (from `SourceService.effectiveIngestPolicy`) and `effectiveRetention` (from `SourceService.effectiveRetention`; for TRACK this is `retention.trackWindow`, default 3d, until prompt 019 adds the setting).
- **`SourceSummary`** adds `historyTier`, `licence`, `status` and `lastDataAt`. `credentialsRequired` is true if any credential field is required.

### 2. `SourcesController` (`/v1/sources`)

| Method / path | operationId | Behaviour |
|---|---|---|
| GET `/v1/sources` | `listSources` | All sources, sorted by id |
| GET `/v1/sources/health` | `listSourceHealth` | Optional repeatable `status` filter. **Declare this mapping so it is not captured by `/{sourceId}`.** |
| GET `/v1/sources/{sourceId}` | `getSource` | 404 for unknown |
| PUT `/v1/sources/{sourceId}` | `updateSource` | Partial update; 200 with `SourceDetail`; 400 validation; 400 `INGEST_POLICY_INVALID`; 400 `RETENTION_TIER_FIXED`; 409 `CREDENTIALS_REQUIRED` |
| PUT `/v1/sources/{sourceId}/credentials` | `setSourceCredentials` | 204 |
| DELETE `/v1/sources/{sourceId}/credentials` | `deleteSourceCredentials` | 204 |
| POST `/v1/sources/{sourceId}/fetch` | `fetchSourceNow` | 200 `FetchOutcome`; 409 / 429 as in prompt 010 |
| POST `/v1/sources/{sourceId}/probe` | `probeSource` | Query params `maxBytes` (1024–10485760, default 262144) and `seconds` (1–60, default 10) |
| GET `/v1/sources/{sourceId}/health` | `getSourceHealth` | |
| GET `/v1/sources/{sourceId}/usage` | `getSourceUsage` | `from`, `to` (defaults now−24h / now), `resolution` (minute, hour, day; default hour). Reject ranges over 31 days at minute resolution (400). Response includes `totals`. |
| GET `/v1/sources/{sourceId}/fetches` | `listSourceFetches` | **New, admin.** Keyset on `fetch_log.id` descending: `after` (exclusive id cursor), `limit` (1–1000, default 100), optional repeatable `status` (`OK, NOT_MODIFIED, FAILED, REFUSED, DISCARDED`) and `trigger` (`SCHEDULED, MANUAL, STREAM, PUSH`). Uses index `(source_id, started_at DESC)`; order by `id DESC` within it. Response `FetchPage {items, nextAfter}`. |

- **Ingest policy errors:** when `IngestPolicyValidator` rejects the body (§5.1: `2 s ≤ minInterval ≤ keyframe`, `edgeMinInterval ≤ minInterval`, `keyframe ≤ ttl − pollInterval − 30 s`, `keyframe: none` only without TTL, SAMPLED/LATEST need `significantFields`), return a 400 problem with `code INGEST_POLICY_INVALID` and every violation in `errors[]` (`field` like `ingestPolicy.keyframe`). An explicit `"ingestPolicy": null` clears the override; an omitted field leaves it unchanged.
- **Retention on TRACK** sources is rejected by `SourceService` with `RETENTION_TIER_FIXED`; make sure it maps to 400 with that code, and that the message names `retention.trackWindow` as the control.
- Writes require the admin token through the existing filter; list protected operations in the controller Javadoc.
- Unknown `sourceId` → 404 problem with `code NOT_FOUND`.
- `licence` and `termsUrl` appear in both summary and detail (Core-Data-Model §14).

### 3. Outcomes, previews, health and usage

- **`FetchOutcome`** gains `persisted`, `live`, `touched`, `touchesThrottled`, `late`, `skipped`, `rejected`, `byReason` (map of the nine `PersistReason` names to counts) and `fetchId` (the `fetch_log` row). Keep `recordsChanged` and fill it with `persisted` (document it as an alias).
- **Probe previews** (`NormalizedRecordPreview`) add `eventTime`, `quality` and `observers`, as 010 produces them.
- **`SourceHealthDto`** adds `coverage` (last `Coverage`: receivers, expected, actual, region bbox), `storage` (`observationsPerMinute, versionsPerMinute, persistRatio, triggerEdgeShare, bytesPerDay, projectedBytes, lateLastHour, clampedLastHour`), `lastDataAt`, `budgetHoursRemaining` (null when the last hour's rate is 0), and the optional `newestFrameAt` / `frameAgeSeconds` from 006's `HealthRule` hook. Take all values from `HealthService`; do not recompute here.
- **`UsagePoint`** adds the ingest counters 005 accumulates: `recordsSeen, versions, liveUpdates, touches, late, skipped, rejected, versionBytes`. `totals` sums them too.
- **`FetchLogEntryDto`**: `id, trigger, status, startedAt, receivedAt, ingestedAt, httpStatus, requestCount, urlRedacted, etag, bytes, bodySha256, adapterVersion, licence, principal, recordsSeen, rejected, late, skipped, versionsWritten, liveUpdates, touches, fromSeq, toSeq, coverage, error`. Never add request or response bodies. `urlRedacted` is already redacted by 005; do not re-derive it.

### 4. Query for fetches

Add `FetchLogQuery` (`ai.elaro.store.query`, `NamedParameterJdbcTemplate`): `WHERE source_id = :s AND (:after IS NULL OR id < :after) [AND status = ANY(:st)] [AND trigger = ANY(:tr)] ORDER BY id DESC LIMIT :limit + 1`. Fetch one extra row for `nextAfter`. If 010 built a JPA `FetchLogRepository`, reuse its entity for mapping but keep the keyset query native.

### 5. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - Schemas per *Spec changes*: `SourceDescriptor` (+ the fields in task 1), `SourceSettings` and `SourceSettingsUpdate` (+ `ingestPolicy` nullable; `effectiveIngestPolicy`, `effectiveRetention` read-only on `SourceSettings`), new `IngestPolicy` (`oneOf` All/Sampled/Latest with `discriminator` on `mode`), `MotionSpec`, `FieldThreshold`, `Quality`, `Observer`, `Coverage`, `SourceHealth` (+ `coverage`, `storage`, `lastDataAt`, `budgetHoursRemaining`, `newestFrameAt`, `frameAgeSeconds`), `FetchOutcome` (+ counts), `NormalizedRecordPreview` (+ `eventTime`, `quality`, `observers`), `UsagePoint` (+ counters), new `FetchLogEntry`, `FetchPage`.
  - New operation `listSourceFetches` with `"x-elaro-scope": "admin"`; `updateSource` documents 400 `INGEST_POLICY_INVALID` and `RETENTION_TIER_FIXED`.
  - Add both codes to the error-code list in the `Problem` schema description.
- `AccessPolicy`: add `rule(HttpMethod.GET, "/v1/sources/*/fetches", ADMIN)` **above** the `GET /v1/sources/**` READ rule, and extend `AccessPolicyTest` for it.
- Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`. Add an example body for `updateSource` with an `ingestPolicy` (SAMPLED) in the script's `BODIES` map.
- MCP: prompt 025 registers source tools on top of `SourceService` and `HealthService`; keep those services free of web types so 025 can call them directly.
- `docs/Operator-Guide.md`: a short "Sources" section: tiers, policies and how to override one, the fetch log, reading `storage` and `budgetHoursRemaining`.

## Tests to write (do not run)

- `SourcesControllerIT` (`@IntegrationTest` + MockMvc + fake adapters):
  - every operation's happy path, with the response JSON shape checked against the spec field names, including the new descriptor, settings and health fields
  - 401 without the token on each write and on `listSourceFetches`
  - 404 unknown source; 400 validation (interval below minimum, bad duration, unknown param) with `errors[]`; 409 credentials required
  - `ingestPolicy` round trip (set SAMPLED, read back, clear with explicit null); invalid policy → 400 `INGEST_POLICY_INVALID` with field paths; TRACK retention → 400 `RETENTION_TIER_FIXED`
  - `effectiveIngestPolicy` falls back to the descriptor default
  - credentials never appear in any response body (search bodies for the secret value)
  - the `health` path is not shadowed by `{sourceId}`
  - usage series resolution handling and the ingest counters
- `SourceFetchesIT`: seed `fetch_log` through 010's `FetchExecutor` with a fake adapter (OK, NOT_MODIFIED, FAILED); keyset paging with no duplicates, status and trigger filters, `nextAfter` null on the last page.
- `FetchOutcomeMapperTest`: `byReason` includes all nine reasons (zeros included); `recordsChanged == persisted`.
- `SourceMapperTest`: descriptor and settings mapping, duration shorthand, `keyframe: none`, discriminated policy JSON.

## Acceptance criteria

- Every `Sources` operation in `docs/OpenAPI.json`, including `listSourceFetches`, is implemented with the same path, method and status codes.
- Descriptor, settings, outcome and health DTOs carry every field listed in Core-Data-Model *Spec changes* for them.
- `GET /v1/sources/{id}/fetches` requires admin; the other GETs stay read.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 118, 119, 120).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 120).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `013: Sources API`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/013-CR-Sources-API.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `013: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
