# 010 — Sources API

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/010-CR-Sources-API.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/009-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/OpenAPI.json`: **every** operation tagged `Sources` (paths `/v1/sources…`) and their schemas. This is the contract; match paths, methods, status codes, field names and operationIds exactly.
- `docs/Architecture.md` §10 (API conventions)

## Objective

Expose source management, the subscription control point, over REST: the catalogue, settings, credentials, fetch-now, probe, health and usage.

## Tasks

### 1. DTOs (`ai.elaro.api.dto`)

Java records mirroring the OpenAPI schemas: `SourceSummary`, `SourceDetail`, `SourceDescriptorDto`, `SourceSettingsDto`, `SourceSettingsUpdate`, `CredentialsUpdate`, `SourceHealthDto`, `UsageSeries`, `UsagePoint`, `FetchOutcomeDto`, `ProbeResultDto`, `NormalizedRecordPreviewDto`, `ParamSpecDto`, `CredentialFieldDto`, `BudgetDto`.

- Durations are serialized as shorthand (prompt 002 serializers).
- Mappers live in `ai.elaro.api.mapper`. Keep controllers thin.
- Bean Validation annotations on input DTOs where the spec has constraints.

### 2. `SourcesController` (`/v1/sources`)

| Method / path | operationId | Behaviour |
|---|---|---|
| GET `/v1/sources` | `listSources` | All sources, sorted by id |
| GET `/v1/sources/health` | `listSourceHealth` | Optional repeatable `status` filter. **Declare this mapping so it is not captured by `/{sourceId}`.** |
| GET `/v1/sources/{sourceId}` | `getSource` | 404 for unknown |
| PUT `/v1/sources/{sourceId}` | `updateSource` | Partial update; 200 with `SourceDetail`; 400 validation; 409 `CREDENTIALS_REQUIRED` |
| PUT `/v1/sources/{sourceId}/credentials` | `setSourceCredentials` | 204 |
| DELETE `/v1/sources/{sourceId}/credentials` | `deleteSourceCredentials` | 204 |
| POST `/v1/sources/{sourceId}/fetch` | `fetchSourceNow` | 200 `FetchOutcome`; 409 / 429 as in prompt 008 |
| POST `/v1/sources/{sourceId}/probe` | `probeSource` | Query params `maxBytes` (1024–10485760, default 262144) and `seconds` (1–60, default 10) |
| GET `/v1/sources/{sourceId}/health` | `getSourceHealth` | |
| GET `/v1/sources/{sourceId}/usage` | `getSourceUsage` | Query params `from`, `to` (defaults now-24h / now), `resolution` (minute, hour, day; default hour). Reject ranges over 31 days at minute resolution (400). Response includes `totals`. |

- Writes require the admin token through the existing filter. No extra annotations are needed, but list the protected operations in the controller Javadoc.
- Unknown `sourceId` → 404 problem with `code NOT_FOUND`.
- `SourceSummary.credentialsRequired` is true if any credential field is required.

## Tests to write (do not run)

- `SourcesControllerIT` (`@IntegrationTest` + MockMvc + fake adapters):
  - every operation's happy path, with the response JSON shape checked (field names per the spec)
  - 401 without the token on each write
  - 404 unknown source
  - 400 validation (interval below minimum, bad duration, unknown param) with `errors[]`
  - 409 credentials required
  - credentials never appear in any response body (search the bodies for the secret value)
  - the `health` path is not shadowed by `{sourceId}`
  - usage series resolution handling
- `SourceMapperTest`: descriptor and settings mapping, duration shorthand.

## Acceptance criteria

- Every `Sources` operation in `docs/OpenAPI.json` is implemented with the same path, method and status codes.
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
2. **Commit** all changes with the message `010: Sources API`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/010-CR-Sources-API.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `010: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
