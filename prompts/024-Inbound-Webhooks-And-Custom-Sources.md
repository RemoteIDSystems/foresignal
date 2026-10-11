# 024 — Inbound Webhooks And Custom Sources

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/024-CR-Inbound-Webhooks-And-Custom-Sources.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/023-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §16 (inbound webhooks and custom push sources), §15 (scopes)
- `docs/Core-Data-Model.md` §2 (time contract; the "Push" row), §3 (`IngestRecord`, descriptor fields, limits), §4 (recipe and lazy migration), §5.1 (`IngestPolicyValidator`), §8 (`fetch_log`, `trigger=PUSH`, `principal`), *Per-prompt changes* → "Other affected prompts" → 020 (= this prompt)
- `docs/Gap-Analysis.md` §4c (ingest format SPI, ntfy/Gotify-compatible publish), §8.A item 13
- `docs/Weather-Imagery-Design.md` §6 table row `POST /v1/ingest/{sourceId}/media`, `/rasters` (format names reserved for prompt 054)
- `docs/Product-Charter.md` §8.9 (FR-41, FR-42)
- `docs/OpenAPI.json`: tag `Ingest`, schemas `CustomSource*`, `IngestRecords`, `IngestOutcome`, `IngestKeyResponse`, `NormalizedRecordPreview`, `FeatureCollection`
- External formats: Alertmanager webhook payload v4 (https://prometheus.io/docs/alerting/latest/configuration/#webhook_config), CloudEvents 1.0 JSON format + HTTP binding + HTTP webhook abuse protection (https://github.com/cloudevents/spec), OASIS CAP 1.2, ntfy publish API (https://docs.ntfy.sh/publish/), Gotify message API (https://gotify.net/api-docs), Standard Webhooks signature scheme (https://www.standardwebhooks.com)

## Objective

Let anything push data into Elaro (SDR decoders, AIS-catcher, Home Assistant, scripts, n8n, agents, Alertmanager, CAP publishers, and every homelab tool that already speaks ntfy or Gotify). Pushed records enter the single v1.2 writer as `IngestRecord`s and get the same versioning, policies, live/touch semantics, change stream, triggers, search and retention as polled data. Custom sources declare their own time base, hash recipe and limits.

## Tasks

### 1. Custom sources (`ai.elaro.inbound`)

- `CustomSourceEntity` (`custom_source`) + repository. Columns per Architecture §7.1 plus: `observed_at_mode`, `ttl_anchor`, `volatile_fields` jsonb, `significant_fields` jsonb, `title_field`, `field_specs` jsonb, `private` bool, `licence`, `records_per_minute` int, `formats` jsonb (per-format options, §3), `verifiers` jsonb (non-secret part), `verifier_secret_ciphertext`/`_nonce` (encrypted with `CredentialCipher`), `ingest_key_hash` (indexed, unique), `ingest_key_prefix`.
- **Descriptor built from the row** (`CustomSourceDescriptors.toDescriptor`): mode `PUSH`, `custom=true` (add the flag to `SourceDescriptor` if 004 did not), `historyTier=STANDARD` (fixed; custom sources are never TRACK), `observedAtMode` (UPSTREAM default, or RECEIVED), `ttlAnchor` (RECEIVED default, or OBSERVED), `recordTtl`, `volatileFields`, `significantFields`, `titleField`, `fieldSpecs`, `private` (prompt 008 fields; if absent, add them as optional descriptor fields), `licence`, `defaultBudget.perMinute = rateLimitPerMinute`, per-day budget `rateLimitPerMinute × 1440`, `zeroResultsNormal=true`.
- **`CustomSourceService`:**
  - **create:** id `^custom-[a-z0-9]+(-[a-z0-9]+)*$`, unique across all sources (static and dynamic); kinds follow the id pattern; field names (`tileProperties`, `searchFields`, `embedFields`, `volatileFields`, `significantFields`, `titleField`) are top-level keys matching `^[A-Za-z0-9_]{1,64}$`. Run the 004 registry validation and `IngestPolicyValidator` (§5.1) against the built descriptor. Generate the key (`eik_` + base64url(32 random bytes)), store SHA-256 hash + 8-char prefix, persist, `SourceRegistry.registerDynamic`, create settings with `enabled=true`, return the key once (`CustomSourceCreated`).
  - **update:** id immutable. Removing a kind that has current heads → 409 `KIND_IN_USE`. `observedAtMode` may change only while the source has no heads, else 409 `OBSERVED_AT_MODE_LOCKED`. Re-validate (a SAMPLED/LATEST effective policy with emptied `significantFields` → 400 `INGEST_POLICY_INVALID`), re-register, and publish `SourceRegistryChangedEvent` + `SourceSettingsChangedEvent` so the writer refreshes its `SourceIngestProfile`. A changed `volatileFields`/`significantFields` changes the hash recipe, and the writer's **lazy per-source recipe migration** (prompt 009, Core-Data-Model §4) runs before that source's next batch. Check that 009 implemented it; if not, add the recipe check there.
  - **delete:** unregister, delete row and settings; records age out through retention. **rotateKey.** **Startup:** register every persisted custom source before the scheduler, writer consumers and trigger engine start.
- `/v1/sources` shows custom sources with `custom=true`. Health uses push outcomes: success when the request was accepted, failure on a whole-request failure; zero-result detection off.

### 2. Ingest pipeline and auth

- **`IngestAuthenticator`** implements the `INGEST_KEY_OR_ADMIN` extension point from 003. Accepted credentials, checked in constant time against the hash:
  1. `X-Elaro-Ingest-Key: eik_…`
  2. `Authorization: Bearer eik_…` (the token filter must hand `eik_` bearers on ingest paths to this authenticator, not reject them as unknown API tokens)
  3. `Authorization: Basic` with the key as the password (any username)
  4. compat only (§4): ntfy `?auth=` (base64 of an Authorization header value), Gotify `X-Gotify-Key` / `?token=`
  5. a bearer API token with `admin` scope
  Redact `auth`/`token` query parameters in every log line and in `fetch_log.url_redacted`.
- **`PushIngestService.ingest(sourceId, principal, List<NormalizedRecord>, receivedAt)`**, shared by every endpoint:
  - source must exist in PUSH mode (custom or built-in) → else 404; disabled → 409 `SOURCE_DISABLED`
  - request rate limit via `BudgetGovernor` (`perMinute`) → 429 + `Retry-After`; **records/min cap**: a second Bucket4j bucket per source sized `records_per_minute` (default 6,000, max 60,000), `tryConsume(n)` for the whole request, else 429 `INGEST_RECORD_RATE` + `Retry-After` and a `REFUSED` `fetch_log` row (reuse prompt 010's writer for non-OK rows)
  - body ≤ 5 MB (413), ≤ 10,000 records (400)
  - wrap each record as `IngestRecord(record, receivedAt, arrival)`; build `FetchLogEntry(trigger=PUSH, status=OK, principal, received_at, bytes, body_sha256, url_redacted, licence)`; submit one `IngestBatch` (queue timeout 30 s → 503 `INGEST_BUSY`) and await the `IngestOutcome`
  - `principal` = `ingest-key:<prefix>`, `token:<name>`, or `compat-ntfy:<prefix>` / `compat-gotify:<prefix>`
  - count usage (requests, bytes) like upstream traffic, plus `UsageMeter.recordIngest`
- **Response:** `IngestOutcome` (v1.2 shape: `seen, persisted, live, touched, touchesThrottled, late, skipped, rejected, fromSeq, toSeq, byReason, rejections[{index, externalId, reason}]`). Keep `changed` as a deprecated alias of `persisted`. HTTP 200 when the request itself was valid, even with per-record rejections.
- **Time base:** under UPSTREAM a record without `observedAt` is rejected `MISSING_OBSERVED_AT` (no silent default); under RECEIVED the writer stamps it. Never mix within a source.

### 3. IngestFormat SPI (`ai.elaro.inbound.format`)

- `interface IngestFormat { String id(); Set<MediaType> mediaTypes(); ParsedIngest parse(IngestRequest req, SourceDescriptor d, FormatOptions opts); }` where `IngestRequest` carries raw body bytes, headers and query; `ParsedIngest` = records + per-item rejections. Formats are Spring beans collected by `IngestFormatRegistry`.
- **Endpoints:** `POST /v1/ingest/{sourceId}` (auto: `native` for JSON) and `POST /v1/ingest/{sourceId}/{format}`. Unknown format → 404 `FORMAT_UNKNOWN`. Reserve `media` and `rasters` (prompt 054 registers them) and `mapping-preview` (059). `GET /v1/ingest/formats` (admin) lists formats with their options.
- **`native`:** GeoJSON FeatureCollection (`id` → externalId, required; properties `kind` (optional with a single kind), `observedAt`, `eventTime`, `validUntil` (upstream-asserted only), `tombstone`, `quality`; the rest → payload) or `{"records": [...]}` in `NormalizedRecordPreview` shape.
- **`alertmanager`** (webhook payload `version: "4"`): one record per `alerts[]` item. externalId = `fingerprint`; kind = option `kindLabel` value, else the single kind; `eventTime = startsAt`; `observedAt = status == firing ? startsAt : endsAt` (monotonic per fingerprint; repeats are equal timestamps → touches); payload `{status, labels, annotations, generatorURL, receiver, groupKey}`; geometry from labels `latitude`/`longitude` (or `lat`/`lon`) when both parse. Option `resolved: tombstone|record` (default `tombstone`). Alertmanager cannot sign; it authenticates with Bearer or Basic via `http_config`.
- **`cloudevents`:** structured (`application/cloudevents+json`), batch (`application/cloudevents-batch+json`) and binary mode (`ce-id`, `ce-source`, `ce-type`, `ce-subject`, `ce-time` headers, body = data). externalId = `subject` ?? `source + "/" + id`; kind from option `typeKinds` (map of CE `type` → kind), else the single kind, else reject; `observedAt = time`; payload = `data` (object) or `{"data": value}` plus `{ceType, ceSource}`; geometry from `data.geometry` (GeoJSON) or `data.lat`/`data.lon`. Answer the webhook abuse-protection handshake: `OPTIONS` with `WebHook-Request-Origin` → 200 with `WebHook-Allowed-Origin` (and `WebHook-Allowed-Rate` = the source's per-minute budget).
- **`cap`** (CAP 1.2 XML, `application/cap+xml` or `application/xml`): write the shared parser `ai.elaro.format.cap.CapParser` here (prompts 067 and 089 reuse it). Secure XML only (DTDs and external entities disabled). `msgType Alert` → record externalId = `identifier`; `Update` → new record plus tombstones for every `references` entry (`sender,identifier,sent`); `Cancel` → tombstones only. `observedAt = sent`; `eventTime = info.onset ?? info.effective`; `validUntil = info.expires`. Geometry: `polygon` pairs are **"lat,lon"** (swap to lon/lat), `circle` "lat,lon radiusKm" → 64-segment polygon; several areas → MultiPolygon. Payload: `event, headline, description, instruction, severity, urgency, certainty, areaDesc, sender, status, msgType, language, geocodes`. `status` other than `Actual` is rejected unless option `acceptNonActual=true`.
- **`mapping`:** minimal `RecordMapping` (stored per custom source under `formats.mapping`): `recordsPath`, `externalId`, `kind`, `observedAt` (+ `observedAtFormat`: ISO, epoch-s, epoch-ms), `lat`/`lon` or `geometry`, `payload` (list of `{from, to}`), all as JSON Pointer paths. Class names `RecordMapping` and `RecordMappingEngine`; prompt 059 extends them with units, preview, LLM drafts and presets.
- **Per-format signature verifiers** (`SignatureVerifier` SPI), configured per source per format under `verifiers.{format}`: `none`; `hmac-sha256` (header configurable, default `X-Signature-256`, value `sha256=<hex>` over the raw body); `standard-webhooks` (`webhook-id`, `webhook-timestamp`, `webhook-signature: v1,<base64>` over `id.timestamp.body`, ±5 min tolerance, replayed `webhook-id` rejected for 10 min); `elaro` (`X-Elaro-Timestamp` + `X-Elaro-Signature: sha256=HMAC("<ts>.<body>")`, the scheme of Elaro's own webhook channel, for Elaro-to-Elaro). When a verifier is configured the signature must verify (401 `SIGNATURE_INVALID`) in addition to the key, unless `keyOptional=true`, in which case a valid signature alone authenticates. Secrets are write-only.

### 4. ntfy- and Gotify-compatible publish

Paths outside `/v1` (add explicit `AccessPolicy` rules, auth = §2 `IngestAuthenticator`; the `topic`/key resolves the source):
- **ntfy:** `PUT|POST /compat/ntfy/{topic}` (body = message; headers `X-Title`/`Title`/`t`, `X-Priority`/`Priority`/`p` (1–5 or min…max), `X-Tags`/`Tags`/`ta`, `X-Click`), `GET|POST /compat/ntfy/{topic}/{publish|send|trigger}?message=&title=&priority=&tags=`, and JSON publish `POST /compat/ntfy` with `{"topic", "message", "title", "priority", "tags", "click"}` (Uptime Kuma uses this shape). `topic` = the custom source id. Respond with an ntfy-style JSON message object (`id`, `time`, `event: "message"`, `topic`, `message`).
- **Gotify:** `POST /compat/gotify/message` (JSON or form: `title`, `message`, `priority`, `extras`); the key identifies the source. Respond with a Gotify message object (`id`, `appid: 1`, `date`, …).
- Mapping for both: kind = the source's single kind (else `notification`, which must be one of its kinds); externalId = `X-Elaro-External-Id` header, ntfy tag `id=<x>`, or Gotify `extras["elaro::record"].externalId`, else a ULID per message; payload `{title, message, priority, tags, click, topic}`. Recommend `observedAtMode=RECEIVED` and a TTL for these sources in the docs.

### 5. Spec, Postman, docs

- `docs/OpenAPI.json`: `CustomSourceInput`/`CustomSource` gain `observedAtMode`, `ttlAnchor`, `volatileFields`, `significantFields`, `titleField`, `fieldSpecs`, `private`, `licence`, `recordsPerMinute`, `formats`, `verifiers` (secrets write-only); v1.2 `IngestOutcome`; new operations `ingestRecordsFormat`, `ingestWebhookHandshake` (OPTIONS), `listIngestFormats`, `ntfyPublish*`, `ntfyPublishJson`, `gotifyPublish`. Use the same `x-elaro-scope` value as `ingestRecords` for ingest-key operations, `admin` for `listIngestFormats`; add security schemes `ingestKeyHeader`, `ingestKeyBearer`, `ingestKeyBasic`. Error codes `KIND_IN_USE`, `OBSERVED_AT_MODE_LOCKED`, `INGEST_RECORD_RATE`, `FORMAT_UNKNOWN`, `SIGNATURE_INVALID`, `SOURCE_DISABLED`, `INGEST_BUSY`. Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`) with BODIES examples for each format and compat endpoint. Update `AccessPolicy` (`/compat/ntfy/**`, `/compat/gotify/**`, `/v1/ingest/**`).
- `docs/Operator-Guide.md` "Inbound webhooks": creating a custom source (time base, TTL, volatile/significant fields and what editing them does), curl for each format, Home Assistant `rest_command`, Python, AIS-catcher-style GeoJSON, Alertmanager `webhook_configs` with bearer auth, Uptime Kuma (ntfy) and Proxmox (Gotify) setups, signature options, a geofence rule on a custom kind.

## Tests to write (do not run)

- `CustomSourceServiceIT`: create (key once, descriptor fields, STANDARD tier, settings enabled), duplicate id vs static → 409, `KIND_IN_USE`, `OBSERVED_AT_MODE_LOCKED`, SAMPLED policy without significant fields → 400, delete unregisters, rotate key, startup re-registration; editing `volatileFields` triggers recipe migration and an identical re-push yields 0 versions.
- `IngestControllerIT` (seed only through the endpoints): native GeoJSON and records, both content types; key via header, Bearer, Basic; admin token; wrong key → 401; non-push → 404; disabled → 409; > 5 MB → 413; > 10,000 → 400; request rate and records/min caps → 429 with `Retry-After` and a REFUSED fetch row; `MISSING_OBSERVED_AT` under UPSTREAM; RECEIVED stamping; `fetch_log` row with `trigger=PUSH` and principal; identical re-push is a touch; change events; a trigger on the custom kind fires.
- `AlertmanagerFormatTest`, `CloudEventsFormatTest` (structured, batch, binary, handshake), `CapParserTest` (lat/lon swap, circle, Update/Cancel tombstones, XXE payload rejected, non-Actual), `RecordMappingEngineTest`, `GeoJsonIngestMapperTest`.
- `SignatureVerifierTest`: each scheme valid/invalid/expired/replayed; `keyOptional`.
- `CompatPublishIT`: ntfy PUT, GET `/publish`, JSON publish, `?auth=`; Gotify header and `?token=`; response shapes; query secrets redacted in logs and `fetch_log`.

## Acceptance criteria

- A curl POST of a FeatureCollection to a new custom source produces records in `/v1/records`, change events on SSE, and alerts when a rule matches.
- An Alertmanager v4 payload, a CloudEvent, a CAP alert and an ntfy publish each create the expected records; a resolved alert or CAP Cancel tombstones them.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `024: Inbound Webhooks And Custom Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/024-CR-Inbound-Webhooks-And-Custom-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `024: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
