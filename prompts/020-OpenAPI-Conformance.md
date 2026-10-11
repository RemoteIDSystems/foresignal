# 020 — OpenAPI Conformance

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/020-CR-OpenAPI-Conformance.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–134).
- **Previous Completion Report:** read `completion-reports/019-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §10 ("The spec is authored, not generated"), §12 (OpenAPI contract test)
- `docs/Core-Data-Model.md` → *Spec changes (OpenAPI)* (every schema, parameter and error code listed there must be in the spec)
- `docs/Gap-Analysis.md` §3 ("Event contracts" row), §9 (AsyncAPI rejected for now), §8.A item 10 ("extend 017" = 020)
- `docs/Roadmap.md` §6 (API tooling)
- `docs/OpenAPI.json` (whole document) and `scripts/generate-postman.py`
- Completion Reports 013–019: every spec edit they made, and any they deferred

## Objective

Make the authored spec the served contract and add automated checks so the implementation cannot drift from `docs/OpenAPI.json`: a route-level contract test, schema conformance for SSE and webhook event payloads (described with the OAS 3.1 `webhooks` object), a required lint, and a Postman contract test. Write down the API-evolution rules that keep `/v1` additive.

## Tasks

### 1. Serve the authored spec

- `processResources` (001) copies `docs/OpenAPI.json` to `static/v1/openapi.json`. Verify `GET /v1/openapi.json` returns it byte-identical with `Content-Type: application/json`, open (no auth). If static handling conflicts with `/v1/**` controllers or the token filter, serve it from a small controller streaming the classpath resource.
- Do not rewrite the server URL; keep the document byte-identical.
- `/docs` serves Swagger UI pointed at `/v1/openapi.json`. Verify the springdoc line in `gradle/libs.versions.toml` is the Boot 4-compatible one; if none works, serve a static Swagger UI page from a pinned CDN version and record the deviation.

### 2. Generated spec for drift detection

- springdoc generates from code at `/v1/openapi-generated.json`, `springdoc.paths-to-match=/v1/**`, static spec path excluded, hidden from Swagger UI.
- Every endpoint gets `@Operation(operationId = "...", tags = "...")` and every controller `@Tag`. Do not duplicate schema documentation in annotations.

### 3. Complete the spec

- Walk Core-Data-Model *Spec changes* item by item and confirm each schema, property, parameter, event type and error code exists (013–019 should have added them). Add any that are missing and list them in the CR.
- **Event payload schemas** (components; 015 added several, so add only what is missing and reuse its names): `ChangeEvent` (reuse `Change`), `LiveEvent`, `TouchEvent`, `ResyncEvent`, `ResetEvent`, `HeartbeatEvent`, `HelloEvent` and `FilterAppliedEvent` (015 multiplexed stream), `AlertEvent` (017 lifecycle: `type` fired|acknowledged|snoozed|resolved, `alert`), `NotificationMessage` (018), `WebhookEnvelope` (018 default webhook body).
- On every SSE operation (`streamChanges`, `streamAlerts`, 015's `/v1/events/stream`), document `text/event-stream` with a `oneOf` of the event schemas and a vendor extension `x-foresignal-sse-events` mapping each SSE `event:` name to its schema `$ref` and stating whether it carries an `id:` line (only `change`).
- **OAS 3.1 top-level `webhooks`:** add `notification` (the generic `webhook` channel request): POST, headers `X-Foresignal-Event`, `X-Foresignal-Delivery`, `X-Foresignal-Timestamp`, `X-Foresignal-Signature`, `X-Foresignal-Origin`, body `WebhookEnvelope`, expected `2xx`; describe the signature recipe (`sha256=HMAC("<timestamp>.<body>")`) and the retry ladder. Add `watchdogHeartbeat` (019) as a bodiless GET/POST. Mark both `"x-foresignal-scope": "open"` (outbound; scope does not apply) and exclude `webhooks` from the AccessPolicy sync check.

### 4. Contract test (`OpenApiContractIT`)

- Load `docs/OpenAPI.json` from the repo path and fetch `/v1/openapi-generated.json` from the running app (`@IntegrationTest`).
- **Pending list:** `src/test/resources/openapi/pending-operations.txt` lists operationIds the spec already contains but that later prompts implement, one per line with the implementing prompt (`createStreamTicket 023`). Seed it from the spec itself: every operation whose `x-foresignal-status` is `planned` and whose `x-foresignal-prompt` is later than 020 (the complete target spec is authored up front; Conventions "API-first"), with that prompt number. Add `SpecAnnotationsTest`: every operation has `x-foresignal-scope`, `x-foresignal-prompt` and `x-foresignal-status`; `implemented` operations are not pending; `planned` operations owned by prompts ≤ the current one fail with "implement or re-assign". Each later prompt deletes its lines when it adds the endpoint; 137 requires the file to be empty.
- Assert:
  - `(path template, method)` pairs: authored minus pending == generated, both directions; normalise path variable names
  - a pending operation that **is** implemented fails the test ("remove it from pending-operations.txt")
  - operationIds match per pair
  - authored 2xx status codes ⊆ generated ones, except an explicit, commented exception list where springdoc's inference is unreliable
  - `GET /v1/openapi.json` is byte-identical to `docs/OpenAPI.json`
- Print a readable diff (missing in code / missing in spec / stale pending entries).

### 5. Event schema conformance (`EventSchemaConformanceTest`, unit)

- Use a JSON Schema 2020-12 validator (networknt `json-schema-validator`; verify the current version on Maven Central) to validate serialised instances of the real DTOs against the spec schemas (resolve `$ref`s within the document): an SSE `change`, `live`, `touch`, `resync`, `reset` payload as produced by 015's serializers, an `AlertEvent` for each lifecycle type, a `NotificationMessage`, and the webhook channel's default body as rendered by 018's `webhook` type.
- Keep a mapping table event name → DTO factory in the test; a new SSE event type without a schema fails.

### 6. Postman stays in sync

- The collection is generated by `scripts/generate-postman.py` (Python 3 stdlib only); never hand-edit it. Regenerate it now: `python3 scripts/generate-postman.py docs/OpenAPI.json postman`. Update the script's `BODIES` map so every example body stays valid for the implemented validation; update the "00 Quickstart — USGS end to end" folder if endpoint shapes changed.
- `PostmanCollectionContractTest` (unit, no Spring): every operationId appears in some request description as `` operationId: `<id>` `` (**fails when any operation is missing from the collection**, Roadmap §6); auth follows `x-foresignal-scope` (`admin` → bearer `{{adminToken}}`, `agent` → `{{agentToken}}`, `ingestRecords` → `X-Foresignal-Ingest-Key: {{ingestKey}}`, `read`/`open` → `noauth`); every JSON example body deserialises into its request DTO with Jackson and passes Bean Validation (skip bodies of pending operations whose DTO does not exist yet, using the pending list).
- Gradle task `generatePostman` (Exec), not wired to `compileJava`.

### 7. Lint (required) and SDK smoke (optional)

- `redocly.yaml` at the repo root: `recommended` ruleset plus `operation-operationId-unique`, `operation-4xx-response` (error), `no-unused-components` (warn), and a configurable assertion rule requiring `x-foresignal-scope` on every operation (enum `open|read|agent|admin`).
- Gradle task `lintOpenApi`: `npx --yes @redocly/cli@2.41.1 lint docs/OpenAPI.json` (verify the latest 2.x on npm; Node ≥ 20.19 required). **Wire it into `check`** (not `compileJava` or `test`). When `npx` is missing it fails with an install hint unless `-PskipOpenApiLint=true` is passed. Fix every lint error in the spec now.
- Optional `checkSdkGeneration` (not wired to anything): runs `@openapitools/openapi-generator-cli` (pinned) for `typescript-fetch` and `dart-dio` into `build/sdk-check` and fails on generator errors. Record known 3.1 nullable-union issues for dart-dio in the CR (prompt 136 owns the pre-processed spec).

### 8. API-evolution convention

Add `docs/Architecture.md` §10.1 "API evolution" (and link it from README "Development"):
- `/v1` is additive only (NFR-10). Allowed: new operations, new optional request fields, new response fields, new enum values, new error codes, new SSE event types.
- Clients must ignore unknown fields, unknown enum values and unknown SSE event names; the generated SDKs (136) must be configured accordingly.
- Never remove or rename fields, change types, tighten validation of existing fields, or add required request fields. Breaking changes go to `/v2`.
- Deprecation: `deprecated: true` in the spec plus `Deprecation` (RFC 9745) and `Sunset` (RFC 8594) headers; at least one minor release before removal in `/v2`.
- Every spec change regenerates Postman and passes `lintOpenApi`; `info.version` bumps minor on additions. After v1.0.0, 139 adds oasdiff breaking-change checks in CI.
- Every operation carries `x-foresignal-scope`; error codes are stable identifiers in `ErrorCodes`.

## Tests to write (do not run)

- `OpenApiContractIT` as above (including the stale-pending failure).
- `OpenApiServingIT`: content type, open access, byte equality, `/docs` reachable (200 or redirect to the Swagger UI index).
- `EventSchemaConformanceTest` as above.
- `PostmanCollectionContractTest` as above.
- `OpenApiStructureTest` (unit): the document parses as OAS 3.1; `webhooks.notification` exists with the signature headers; every SSE operation has `x-foresignal-sse-events`; every operation has `x-foresignal-scope`.

## Acceptance criteria

- Every controller endpoint has an operationId matching the spec; every unimplemented spec operation is on the pending list with its prompt.
- Event payloads and the webhook body are described in the spec and validated against real DTOs.
- `./gradlew lintOpenApi` passes.
- The CR lists the pending operations.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 134, 137, 138, 139).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 139).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `020: OpenAPI Conformance`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/020-CR-OpenAPI-Conformance.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `020: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
