# Foresignal — Architecture

Version 1.2 · 2026-10-10 · Companion to the [Product Charter](Product-Charter.md) and [OpenAPI spec](OpenAPI.json).

> **v1.2 note:** [Core-Data-Model.md](Core-Data-Model.md) is authoritative for the data model, and supersedes §4–§11 of this document wherever they conflict. It covers time semantics, persisted vs. live state, ingest policies, partitioned history, the writer, SSE live events, trigger edges, asOf and retention. [Weather-Imagery-Design.md](Weather-Imagery-Design.md) is authoritative for weather, raster products, imagery and SatDump. A later docs prompt folds both into this file.
>
> Correction: `seq` is **monotonic in commit order**, not gap-free; gaps from rolled-back allocations are expected and harmless.
>
> Access rule: any unmatched `/v1/**` path requires `admin` (fail closed), so a new endpoint is never accidentally world-writable.

---

## 1. Overview

Foresignal is a single Spring Boot process backed by one PostgreSQL database with PostGIS and pgvector. Inside the process, five core subsystems communicate through narrow interfaces (diagram below). Around them sit the operator and intelligence features: scoped tokens, inbound webhooks, notification channels, the MCP server, and an optional LLM subsystem that powers semantic search, Ask, briefings and alert enrichment (§15–§24).

```
                 ┌──────────────────────────── Foresignal process ─────────────────────────────┐
                 │                                                                        │
  Upstreams ◄────┤  ① Collection                ② Ingest              ③ Distribution      │
  (HTTP, WS,     │  ┌───────────────┐   results ┌─────────────┐ batch  ┌───────────────┐   │
   MQTT…)        │  │ Scheduler     ├──────────►│ Ingest      ├───────►│ Change bus    │   │
                 │  │ Adapters      │  (queue)  │ writer      │ events │  ├─ SSE hub   ├───┼──► Frontends
                 │  │ Upstream      │           │ (single     │        │  ├─ Trigger   │   │    Agents
                 │  │ client +      │           │  thread)    │        │  │   engine ──┼───┼──► Channels
                 │  │ budgets       │           └──────┬──────┘        └───────────────┘   │
                 │  └───────┬───────┘                  │                                  │
                 │          │ usage/health             │ versions                         │
                 │          ▼                          ▼                                  │
                 │  ┌──────────────────────── ④ Store (Postgres + PostGIS) ──────────┐    │
                 │  │ settings · credentials · usage · health · record_version ·     │    │
                 │  │ record_head · trigger_rule · alert · channels · app_setting …  │    │
                 │  └───────────────────────────────▲────────────────────────────────┘    │
                 │                                  │                                     │
                 │  ⑤ API (REST /v1, GeoJSON, MVT, SSE, MCP, OpenAPI) + token filter      │
                 └────────────────────────────────────────────────────────────────────────┘
```

**Key principle:** the *only* thing that causes an upstream request is an enabled source in `source_settings` (or an explicit operator fetch/probe). Nothing a client reads or streams ever reaches the collector. The same holds for the LLM: nothing calls a model except an explicit Ask, a scheduled briefing, an enrichment-enabled rule, or the embedding worker for sources configured to embed. All of these are budgeted like sources.

## 2. Technology stack

| Concern | Choice | Notes |
|---|---|---|
| Runtime | Java 21 LTS (Temurin) | Virtual threads (`spring.threads.virtual.enabled=true`) |
| Framework | Spring Boot 4.1.x | Web MVC (not WebFlux); SSE via `SseEmitter` |
| JSON | Jackson 3 (Boot 4 default, `tools.jackson.*` packages) | Payloads stored as `jsonb` |
| Persistence | Spring Data JPA, Hibernate 7, `hibernate-spatial` + JTS | `ddl-auto=update` in development; **no Flyway until production** |
| Bulk writes | `JdbcTemplate` batch on the ingest hot path | Entities still own the schema definition |
| Database | PostgreSQL 17 + PostGIS 3.5 + pgvector | Custom image built from `docker/db/Dockerfile` (`postgis/postgis:17-3.5` plus `postgresql-17-pgvector`) |
| Rate limiting | Bucket4j (`bucket4j_jdk17-core`) | One bucket set per source |
| HTTP client | `java.net.http.HttpClient` | Wrapped by `UpstreamClient` |
| WebSocket client | `java.net.http.WebSocket` | For streaming adapters |
| MQTT | Eclipse Paho v5 | For streaming adapters |
| Vectors | pgvector + `hibernate-vector` | Embeddings for semantic search (§20) |
| MCP | MCP Java SDK (`io.modelcontextprotocol.sdk`) with the Spring WebMVC streamable-HTTP transport | `/mcp` (§18) |
| LLM: Anthropic | Official Anthropic Java SDK `com.anthropic:anthropic-java` | Default model `claude-opus-5-5` (§19) |
| LLM: others | `java.net.http` clients for OpenAI-compatible APIs and Ollama | Local or alternative providers (§19) |
| Email | Jakarta Mail via `spring-boot-starter-mail` | Email notification channel (§17) |
| Web Push | JDK crypto (ECDH P-256, HKDF, AES-128-GCM, ES256 VAPID) | Browser and UnifiedPush push (§24) |
| OpenAPI | Authored `docs/OpenAPI.json` served at `/v1/openapi.json`; springdoc-openapi (Boot 4-compatible line) for Swagger UI at `/docs` and drift testing | See §10 |
| Metrics | Micrometer + Prometheus registry | `/actuator/prometheus` |
| Build | Gradle 9.x, Kotlin DSL, wrapper committed | Single module |
| Tests | JUnit 5, AssertJ, Mockito, Testcontainers 2.x, WireMock 3.x, JaCoCo | ≥ 90% line coverage gate |
| Packaging | Multi-stage Dockerfile → `eclipse-temurin:21-jre`; Docker Compose | |

Exact dependency versions are pinned in `gradle/libs.versions.toml` and verified against Maven Central at implementation time.

## 3. Package layout

Single Gradle module, base package `ai.foresignal`.

```
ai.foresignal
├── ForesignalApplication
├── config          Jackson, CORS, virtual threads, OpenAPI metadata, clock, schema bootstrap
├── security        Tokens and scopes, AccessPolicy, TokenFilter, CredentialCipher, SecretKeyStore
├── source          SourceAdapter SPI, descriptors, registry, settings, credentials
│   └── adapters    One sub-package per upstream: adapters.usgs, adapters.adsblol, …
├── collect         CollectorScheduler, UpstreamClient, BudgetGovernor, UsageMeter,
│                   HealthTracker, Backoff, StreamingSupervisor
├── ingest          IngestQueue, IngestWriter, ContentHasher, NormalizedRecord
├── store           Entities (RecordVersion, RecordHead, …), repositories, spatial queries,
│                   TileQuery
├── stream          ChangeBus, ChangeBatch, SseHub, StreamFilter
├── trigger         TriggerRule, TriggerEngine, RuleSqlBuilder, Alert, AlertService,
│                   EnrichmentWorker
├── notify          Notification channels, formatters per type, delivery worker, Web Push
├── inbound         Custom push sources, ingest endpoint support
├── mcp             MCP server and tool catalog
├── llm             Provider registry, LlmClient implementations, budgets, usage
├── search          Embedding worker, semantic and hybrid search
├── ask             Ask Foresignal: tool loop, threads, proposed actions
├── briefing        Briefing definitions, scheduler, digest and LLM renderers
├── retention       RetentionSettings, RetentionJob
├── settings        AppSettingService (typed key/value), export/import
├── system          SystemInfoService
├── evidence        Originators, canonical events, evidence groups (116)
├── observability   Detection-probability estimates, coverage layer (116)
├── manifest        Analysis manifests and replay (116)
├── attention       Attention topics (117)
├── situation       Situations, linker, snapshots, briefs (118)
├── analytics       Series reader, findings, change points, event studies, co-occurrence,
│                   signal families, math (119)
├── precursor       Precursor mining, validation, library, live watch (120)
├── baserate        Base rates, reference-history imports, analogs (121)
├── forecast        Ledger: questions, forecasts, resolvers, scoring, ensemble (122)
├── hypothesis      Hypothesis sets, indicators, updater, ACH, assumptions (125)
├── outlook         Series forecasts, regional outlooks, indices (126)
├── impact          Impact projection (127)
├── benchmark       Analytic benchmark harness and gating (123)
├── cycle           Analysis cycles, assessments, intel board (128)
├── object          Object references, envelopes, relation graph, annotations,
│                   workspaces and context sync, sensor discovery (129)
├── collab          Principals, groups, access lists, TLP, tags, comments, attachments,
│                   evidence items, saved searches (130)
├── chronicle       Chronicles: capture, partitioned history, thread, replay (131);
│                   network: fingerprints, discovery, access requests (134)
├── report          Reports, renditions, signed intelligence packages (132)
├── media           News articles, stories, outlet registry, rating providers, coverage (133)
├── social          Platform adapters, posts, term index, volume series and baselines (135);
│                   narratives, claims, media fingerprints, propagation, coordination (136);
│                   social–physical comparisons, attention gaps, cues, lead times (137)
└── api             Controllers, DTOs, ProblemDetail handlers, pagination cursors
```

Dependency rule: `api → (service packages) → store`. Adapters depend only on `source` SPI types and `collect.UpstreamClient`; they never touch the store, the scheduler or each other.

## 4. Source adapter SPI

### 4.1 Interfaces

```java
public interface SourceAdapter {
    SourceDescriptor descriptor();
}

public interface PollingAdapter extends SourceAdapter {
    /** Fetch once. All network access MUST go through ctx.http(). */
    FetchResult fetch(FetchContext ctx) throws AdapterException;
}

public interface StreamingAdapter extends SourceAdapter {
    /** Open the stream and push records to the sink until the handle is closed. */
    StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException;
}
```

```java
public record SourceDescriptor(
    String id,                       // "usgs-earthquakes" — stable, kebab-case
    String name,
    String description,
    Set<String> kinds,               // {"earthquake"}
    String homepage,
    String attribution,              // plain text; licence handling deferred
    Mode mode,                       // POLLING | STREAMING
    List<CredentialField> credentialFields, // e.g. [apiKey] or [clientId, clientSecret]; any required → must be set before enable
    List<ParamSpec> params,          // adapter parameters with defaults + validation
    Duration minInterval,            // floor for operator-set interval (polling)
    Duration defaultInterval,
    Budget defaultBudget,            // perMinute, perDay
    Duration recordTtl,              // validUntil = observedAt + ttl (null = no expiry)
    boolean zeroResultsNormal,       // suppress silent-break detection
    List<String> tileProperties,     // payload fields copied into MVT features
    List<String> searchFields        // payload fields concatenated into record_head.search_text (default: name, title, callsign, place)
) {}
```

```java
public record NormalizedRecord(
    String externalId,               // unique within the source
    String kind,
    Instant observedAt,
    Geometry geometry,               // JTS, SRID 4326, nullable
    Instant validUntil,              // nullable; overrides descriptor TTL
    Map<String, Object> payload,     // adapter-defined, JSON-serialisable
    boolean tombstone                // true = record removed upstream
) {}

public record FetchResult(List<NormalizedRecord> records, boolean complete) {}
// complete = true means "this is the full current set"; records absent from a complete
// result are NOT auto-tombstoned in v1 (adapters emit explicit tombstones when they can).
```

`FetchContext` exposes: `http()` (budgeted `UpstreamClient` bound to this source), `params()` (validated adapter params), `credentials()` (decrypted `Map<String,String>` keyed by credential field name; empty if none), `clock()`, `lastSuccessAt()`, and a small per-source key/value `state()` (e.g. last-modified cursors) persisted in `source_health.adapter_state`.

### 4.2 Adapter rules

1. Self-contained: one package per adapter containing the adapter class, its parser, and DTOs. No cross-adapter imports.
2. No direct `HttpClient` construction — always `ctx.http()`.
3. Parsers are pure functions (`bytes/string → List<NormalizedRecord>`) so they are testable against fixtures without network access.
4. Each adapter ships: a fixture under `src/test/resources/fixtures/<source-id>/`, a parser contract test, and a WireMock-backed adapter test.
5. Adapters are Spring `@Component`s; the `SourceRegistry` discovers them by type. Duplicate ids fail startup.
   - **Adapter families:** when one parser serves many feeds (GTFS-RT transit agencies, CCTV catalogue packs, GBFS systems, adsb.lol regions), implement a `SourceAdapterFamily` bean that returns one adapter instance per feed from a static, code-reviewed feed list (e.g. `transit-mbta`, `transit-hsl`). Each feed is a separate source with its own settings, budget and health. Feed lists are never supplied by API callers, which prevents SSRF.
6. Code ported from God's Eye View carries a header comment naming the origin file and the MIT notice; `THIRD_PARTY_NOTICES.md` lists it.

## 5. Collection

### 5.1 CollectorScheduler

- On startup, and every **2 seconds** (reconcile tick), it compares the set of enabled sources in `source_settings` with its running set.
  - Newly enabled polling source → schedule its first run immediately, then every `interval`.
  - Newly enabled streaming source → `StreamingSupervisor.start(source)`.
  - Disabled or deleted → cancel future runs; close stream handles. In-flight fetches finish but their results are discarded if the source was disabled before commit.
- Setting changes made through the API also call `scheduler.reconcileNow()` so changes apply immediately rather than at the next tick.
- A single platform thread drives timing (`ScheduledExecutorService`); each fetch executes on a **virtual thread**. A per-source `AtomicBoolean running` prevents overlapping fetches; a run that would overlap is skipped and counted.
- Before each run the scheduler checks `HealthTracker.backoffUntil(source)`; if in backoff, the run is skipped.
- Each fetch has an overall timeout (default 60 s, per-descriptor override).

### 5.2 UpstreamClient and BudgetGovernor

`UpstreamClient` is the **only** path to the network. For each request:

1. `BudgetGovernor.tryConsume(sourceId)` — checks two Bucket4j bandwidths: `perMinute` (greedy refill) and `perDay` (interval refill at UTC midnight). If refused → `BudgetExceededException`; the meter records `refused`; no request is sent.
2. Send with per-request timeout (default 20 s) and gzip accepted. The `User-Agent` is `Foresignal/<version> (+https://foresignal.ai; install=<id>)`, where `<id>` is a random 8-character install id generated on first start (stored in `app_setting`). Upstreams can then rate-limit or contact one misbehaving install without blocking every Foresignal user (a lesson from ShadowBroker). Never spoof browser User-Agents.
   - **Conditional GET:** `ctx.http().getConditional(url, stateKey)` stores `ETag`/`Last-Modified` in `adapter_state` and sends `If-None-Match`/`If-Modified-Since`. A `304` returns `NotModified`; the run counts as a success with zero changes and does not trigger zero-result degradation. CelesTrak's fair-use policy requires this.
   - **Response cap:** bodies are read with a byte cap (default 32 MB, per-descriptor override) to protect the heap.
3. `UsageMeter.record(sourceId, bytes, status)`.
4. HTTP 429/503 → `HealthTracker.backoff(sourceId, retryAfter)`; exponential backoff (30 s × 2ⁿ, max 1 h) when no `Retry-After`.
5. Non-2xx → `UpstreamHttpException` carrying status and a truncated body.

Budgets are rebuilt when settings change. On startup, the daily bucket is seeded from today's `source_usage` totals so restarts cannot reset the daily allowance.

**Streaming budgets:** for streaming adapters, a "request" is a connection attempt or subscription message; the descriptor's budget therefore bounds reconnect storms. Adapters must also declare `maxSubscriptions` in params where relevant (the Mesh.md lesson: no wildcard fan-out).

### 5.3 UsageMeter

In-memory `LongAdder` counters per `(source, minute)`; flushed to `source_usage` every 60 s (and on shutdown) with an upsert. Live (unflushed) counts are merged into API responses.

### 5.4 HealthTracker

Persists `source_health` (one row per source). It updates on every run with: last attempt, last success, last error (message, class, HTTP status), consecutive failures, records in last fetch, rolling counts of zero-result runs, and backoff-until.

Status derivation (evaluated on read):

| Status | Rule |
|---|---|
| `disabled` | source not enabled |
| `idle` | enabled, no attempt yet |
| `failing` | ≥ 3 consecutive failures, or in backoff |
| `degraded` | last run failed (< 3 consecutive), or the last success had 0 records while ≥ 3 of the previous 5 successes had records (unless `zeroResultsNormal`), or > 20% budget refusals in the last hour |
| `healthy` | otherwise |

### 5.5 StreamingSupervisor

Owns stream handles for streaming adapters. On disconnect it reconnects with exponential backoff, and each attempt consumes budget. Records from streams go to the `RecordSink`, which **coalesces by `externalId`** (latest wins) and flushes to the ingest queue every 2 s or every 2,000 records, whichever comes first.

## 6. Ingest and the change sequence

### 6.1 Single-writer design

All writes to `record_version` and `record_head` happen on **one dedicated ingest writer thread** consuming a bounded `IngestQueue` (capacity 64 batches). This is deliberate:

- **Gap-free, commit-ordered `seq`.** Concurrent writers can commit sequence values out of order (tx A takes seq 100, tx B takes 101, B commits first). A reader that has advanced past 101 would never see 100. With a single writer, commit order equals `seq` order, so `seq` is a reliable cursor for SSE resumption, trigger catch-up and `/v1/changes`.
- **Simple batching.** One transaction per batch, with JDBC batch inserts.

Polling fetch threads block if the queue is full (natural back-pressure). Streaming sinks coalesce while waiting.

### 6.2 Write algorithm (per batch of records from one source)

1. Normalize: apply descriptor TTL to compute `validUntil` when absent; force SRID 4326.
2. `ContentHasher`: SHA-256 over a canonical JSON of `{kind, observedAt, geometry(WKT), validUntil, payload (sorted keys), tombstone}`.
3. Load existing heads: `SELECT external_id, content_hash, version FROM record_head WHERE source_id = ? AND external_id = ANY(?)`.
4. For each record whose hash differs or which has no head: assign `version = head.version + 1` (or 1), and take `seq` from sequence `record_seq`.
5. Batch `INSERT INTO record_version …`; batch upsert `record_head` (`ON CONFLICT (source_id, external_id) DO UPDATE`).
6. Commit. Then publish `ChangeBatch(sourceId, fromSeq, toSeq, List<ChangeSummary>)` on the `ChangeBus`.

Unchanged records are dropped silently; the run still counts as a success with `recordsSeen` versus `recordsChanged` both reported to health.

### 6.3 Sequence

`record_seq` is a Postgres sequence created by Hibernate (`@SequenceGenerator(sequenceName = "record_seq", allocationSize = 1)`, so the DB sequence increments by 1). `RecordVersion` is `@Immutable`; Hibernate never inserts it. The writer allocates a block per batch with `SELECT nextval('record_seq') FROM generate_series(1, :n)`. With one writer, values are monotonic in commit order. Gaps from rolled-back transactions are harmless because readers only use `seq` as a "greater than" cursor.

## 7. Data model

Hibernate generates the tables from entities. Indexes that JPA cannot express (GiST, partial) are created by `SchemaBootstrap` (§7.3).

### 7.1 Tables

**`source_settings`**: `source_id` PK · `enabled` bool · `interval_seconds` int · `params` jsonb · `budget_per_minute` int · `budget_per_day` int · `retention` varchar (ISO-8601 duration, nullable → global default) · `updated_at`

**`source_credential`**: `source_id` PK · `ciphertext` bytea · `nonce` bytea · `updated_at`

**`source_health`**: `source_id` PK · `last_attempt_at` · `last_success_at` · `last_error_at` · `last_error` text · `last_http_status` int · `consecutive_failures` int · `last_records_seen` int · `last_records_changed` int · `recent_record_counts` jsonb (last 5) · `backoff_until` · `adapter_state` jsonb

**`source_usage`**: PK (`source_id`, `minute` timestamptz) · `requests` · `bytes` · `errors` · `refused`

**`record_version`** (append-only)

| Column | Type | Notes |
|---|---|---|
| `seq` | bigint PK | from `record_seq` |
| `source_id` | varchar(64) | |
| `external_id` | varchar(256) | |
| `version` | int | 1-based per record |
| `kind` | varchar(64) | |
| `observed_at` | timestamptz | |
| `ingested_at` | timestamptz | |
| `valid_until` | timestamptz null | |
| `geom` | geometry(Geometry,4326) null | |
| `payload` | jsonb | |
| `content_hash` | char(64) | |
| `tombstone` | bool | |

Unique (`source_id`, `external_id`, `version`); index (`ingested_at`); index (`source_id`, `observed_at`); GiST (`geom`).

**`record_head`**: PK (`source_id`, `external_id`) · `seq` (FK → record_version) · `version` · `kind` · `observed_at` · `valid_until` · `geom` · `content_hash` · `tombstone`. Denormalized so current-state, bbox and tile queries touch only this table, joining `record_version` for payload. Indexes: GiST (`geom`), (`kind`), (`valid_until`).

**`trigger_rule`**: `id` uuid PK · `name` · `enabled` · `source_id` null · `kind` null · `conditions` jsonb (list of `{path, op, value}`) · `geofence` geometry(MultiPolygon,4326) null · `mode` varchar(8) default `match` · `only_new` bool · `cooldown_seconds` int · `message_template` text · `created_at` · `updated_at`

**`alert`**: `id` bigint identity PK · `rule_id` · `seq` · `source_id` · `external_id` · `fired_at` · `message` · `acknowledged_at` null. Index (`rule_id`, `source_id`, `external_id`, `fired_at`) for cooldown checks; index (`fired_at`).

**`notification_channel`** (§17): `id` uuid PK · `name` · `type` · `config` jsonb (non-secret settings) · `secret_ciphertext` / `secret_nonce` (secret settings, encrypted JSON) · `events` jsonb (subset of `alert`, `briefing`, `system`) · `rule_ids` jsonb null (null = all rules) · `min_severity` · `title_template` · `body_template` · `wait_for_enrichment_seconds` · `enabled` · `created_at` · `updated_at`

**`notification_delivery`**: `id` bigint PK · `channel_id` · `event` · `alert_id` null · `briefing_run_id` null · `status` (`pending`/`delivered`/`failed`) · `attempts` · `last_error` · `next_attempt_at` · `delivered_at`

**`api_token`** (§15): `id` uuid PK · `name` · `scope` (`read`/`agent`/`admin`) · `token_hash` · `token_prefix` (first 8 characters, for display) · `created_at` · `last_used_at` · `expires_at` null · `revoked_at` null

**`custom_source`** (§16): `id` PK (`custom-…`) · `name` · `description` · `kinds` jsonb · `attribution` · `record_ttl` · `tile_properties` jsonb · `search_fields` jsonb · `embed_fields` jsonb · `ingest_key_hash` · `created_at`

**`push_subscription`** (§24): `id` uuid PK · `name` · `endpoint` · `p256dh` · `auth` · `filters` jsonb · `token_id` null · `created_at` · `last_success_at` · `status` (`pending|active|disabled`) · `verify_code_hash` · `verify_expires_at` · `verify_attempts` · `endpoint_host` · `transport_label` · `payload_format` · `lang` · `failure_count` · `last_failure_at` · `last_status` · `recheck_failures` (031)

**`push_dispatch`** (§24.4, 031): `id` bigint PK · `delivery_id` · `target_type` · `target_id` · `status` (`pending|sent|gone|failed`) · `attempts` · `next_attempt_at` · `last_status` · `last_error` (≤ 500, endpoint redacted) · `downgraded` · `content` jsonb · `leased_until` · `sent_at` · `created_at`

**`push_provider`** (§24.3, 032): `id` slug PK · `type` (`apns|fcm`) · `app_ids` jsonb · `config` jsonb · `secret_ciphertext` · `secret_nonce` · `enabled` · `last_test_at` · `last_error` · `updated_at`

**`push_device`** (§24.3, 032): `id` uuid PK · `transport` (`apns|fcm`) · `platform` (`ios|ipados|macos|android`) · `app_id` · `environment` (`sandbox|production`, APNs) · `token_ciphertext` · `token_nonce` · `token_hash` (SHA-256, unique with `transport`) · `token_suffix` · `token_id` · `name` · `device_name` · `app_version` · `filters` jsonb · `content_level` (`minimal|short`) · `lang` · `status` (`active|disabled`) · `created_at` · `last_success_at` · `last_failure_at` · `failure_count` · `last_status`

**`llm_provider`** (§19): `id` slug PK · `type` (`anthropic`/`openai-compatible`/`ollama`) · `base_url` · `chat_model` · `embedding_model` null · `embedding_dimensions` null · `options` jsonb · `secret_ciphertext` / `secret_nonce` · `requests_per_minute` · `tokens_per_day` · `enabled` · `updated_at`

**`llm_usage`**: PK (`provider_id`, `minute`) · `requests` · `input_tokens` · `output_tokens` · `cached_tokens` · `errors` · `refused`

**`embedding_document`** (§20): PK (`doc_type`, `doc_id`) · `source_id` null · `kind` null · `seq` null · `geom` null · `observed_at` · `text` · `text_hash` · `model` · `embedding` vector(N) · `tsv` tsvector (generated). HNSW index on `embedding` (cosine), GIN on `tsv`, GiST on `geom`.

**`ask_thread`** / **`ask_message`** / **`ask_action`** (§21): threads, messages (role, content jsonb, usage) and proposed actions (`type`, `payload` jsonb, `status` `pending`/`executed`/`rejected`, `executed_at`).

**`briefing`** / **`briefing_run`** (§22): definitions (`name`, `enabled`, `schedule` cron, `timezone`, `scope` jsonb, `lookback`, `mode` `digest`/`llm`, `instructions`, `channel_ids` jsonb, `next_run_at`) and runs (`status`, `period_from`, `period_to`, `content_markdown`, `citations` jsonb, `stats` jsonb, `error`, `usage` jsonb).

`trigger_rule` also carries `severity` (`info`/`warning`/`critical`, default `warning`), `enrich` bool, `enrich_radius_km`, and `last_error` / `last_error_at`. `alert` also carries `severity`, `summary` null and `enriched_at` null.

**`app_setting`**: `key` PK · `value` jsonb · `updated_at`. Keys include `retention`, `cors`, `trigger.cursor`, `stream.limits`.

**`system_secret`**: `key` PK · `value` bytea. Holds the VAPID key pair (private key encrypted). The bootstrap admin token became an `api_token` row in §15.

### 7.2 Secrets

- **Admin token:** 32 random bytes, base64url, prefixed `fsr_`. Generated on first start when `admin_token_hash` is absent; printed once in a clearly delimited log block. Only the SHA-256 hash is stored. Rotation: `POST /v1/system/admin-token/rotate` returns a new token once. Recovery: starting with env `FORESIGNAL_RESET_ADMIN_TOKEN=true` regenerates and prints a new token.
- **Credential encryption:** AES-256-GCM. The key is generated on first start and stored in a file on the data volume (`${FORESIGNAL_DATA_DIR:-./data}/secret.key`, mode 0600), never in the database, so a database dump alone does not reveal upstream keys.

### 7.3 SchemaBootstrap

Hibernate cannot create the PostGIS extension or GiST indexes. `SchemaBootstrap` runs in two phases:

1. **Before** the JPA `EntityManagerFactory` initializes (a `DataSource`-level initializer that the EMF depends on): `CREATE EXTENSION IF NOT EXISTS postgis` (required), then `CREATE EXTENSION IF NOT EXISTS vector` (optional; when it is unavailable, semantic search reports itself disabled with a clear reason and everything else works).
2. **After** Hibernate DDL (`ApplicationRunner`, highest precedence): `CREATE INDEX IF NOT EXISTS … USING GIST (geom)` on `record_version` and `record_head`, plus any partial indexes.

When Flyway is introduced for production, both phases move into the baseline migration and this class is deleted.

## 8. Distribution

### 8.1 ChangeBus

An in-process publisher (`ApplicationEventPublisher` or a small listener registry). Subscribers: `SseHub`, `TriggerEngine`. Events carry summaries (`seq, sourceId, externalId, kind, version, observedAt, bbox/point, tombstone`), not payloads.

### 8.2 SseHub (`/v1/changes/stream`)

- Each SSE connection registers a `StreamFilter` (sources, kinds, bbox) and is backed by an `SseEmitter` with a bounded per-client buffer (default 1,000 events). On overflow the client receives a `reset` event and must catch up via `/v1/changes`.
- **Resume:** the SSE `id` is `seq`. On connect with `Last-Event-ID` (or `?after=`), the hub first replays from the database (`seq > after`, filtered, up to a limit of 10,000) and then switches to live events.
- Event types: `change` (summary plus optional payload when `?payload=true`), `heartbeat` (every 15 s), `reset`.
- `/v1/alerts/stream` follows the same pattern with alert events, using alert `id` as the event id.

### 8.3 TriggerEngine

- Subscribes to `ChangeBus`. For each `ChangeBatch` it evaluates all enabled rules whose source/kind filter could match the batch.
- Each rule compiles (`RuleSqlBuilder`) into one parameterized query over `record_version` for `seq BETWEEN :from AND :to`:
  - thresholds → `(payload #>> :path)::numeric >= :value` (numeric ops), or text comparison for `==`/`!=` on strings;
  - geofence → `ST_Intersects(geom, :fence)`;
  - `only_new` → `version = 1`;
  - **`mode`** (`match` | `enter` | `exit`). Edge modes compare each version with its predecessor, because the store keeps every version. Let `P(x)` be the rule's full predicate (conditions plus geofence) applied to row `x`, and `p` the previous version: `LEFT JOIN record_version p ON p.source_id = v.source_id AND p.external_id = v.external_id AND p.version = v.version - 1`.
    - `enter` = `P(v) AND (p IS NULL OR NOT P(p))`.
    - `exit` = `p IS NOT NULL AND P(p) AND NOT P(v)`. A tombstone counts as an exit when `P(p)` held.
    - If retention has removed `p` while `v.version > 1`, an edge rule does not fire. Fail quiet, never noisy.
    - This gives Velocity-style enter/exit alerting with no in-memory state, so it survives restarts.
    - The predicate has the same shape when applied to `p`. `RuleSqlBuilder` renders it twice with a table alias parameter;
  - cooldown → `NOT EXISTS (SELECT 1 FROM alert a WHERE a.rule_id = :rule AND a.source_id = v.source_id AND a.external_id = v.external_id AND a.fired_at > now() - :cooldown)`;
  - tombstones never match.
- Matches are inserted into `alert` with the rule's `severity`. Each alert is published on the `AlertBus`, whose listeners are the alert SSE stream, notification channels (§17) and enrichment (§23).
- **Durable cursor:** after processing a batch, the engine stores `trigger.cursor = toSeq` in `app_setting`. On startup it catches up from the cursor to the current max `seq` in chunks before subscribing live.
- Message templates support `{{name}}`, `{{source}}`, `{{externalId}}`, `{{kind}}`, `{{observedAt}}` and `{{payload.<path>}}`. Default: `"{{name}}: {{source}}/{{externalId}}"`.
- `POST /v1/triggers/{id}/test` runs the same SQL over a recent window (default the last 24 h, `?since=`) and returns matches without inserting alerts.

### 8.4 Notification delivery

See §17. Outbound webhooks are one channel type among many.

## 9. Query paths

| Endpoint | Strategy |
|---|---|
| `GET /v1/records` | Native SQL over `record_head h JOIN record_version v USING (seq)`, filters on `h.source_id`, `h.kind`, `h.geom && ST_MakeEnvelope(…)`, `observed_at` window, `valid_until > now()` unless `includeExpired`, `NOT tombstone`. Keyset pagination on (`observed_at DESC, seq DESC`). GeoJSON built in SQL with `ST_AsGeoJSON(geom)::jsonb`. |
| `GET /v1/records/{source}/{id}/versions` | `record_version` by (`source_id`, `external_id`) ordered by `version`. |
| `GET /v1/history` | `record_version` by time range and bbox, ordered by `observed_at`, paginated; capped at 50,000 per page. |
| `GET /v1/changes` | `record_version WHERE seq > :after ORDER BY seq LIMIT :n`. |
| `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt` | `ST_AsMVT` over `record_head` where `geom && ST_TileEnvelope(z,x,y)`, current only; `layer` matches a kind or a source id; feature properties = `source, externalId, kind, observedAt` plus the descriptor's `tileProperties`. `Cache-Control: max-age=5`. Zoom 0–16. |

Free-text `q` matches `ILIKE` on a generated text column of selected payload fields (`title`, `name`, `callsign`, `place`). The descriptor may declare which fields feed it; in v1 it is computed in the writer into a `search_text` column on `record_head`.

## 10. API conventions

- Base path `/v1`. JSON uses camelCase. Timestamps are ISO-8601 UTC. Durations accept ISO-8601 (`PT15M`, `P7D`) or shorthand (`15m`, `7d`, `12h`).
- Errors use RFC 9457 `application/problem+json` (Spring `ProblemDetail`) with a `code` extension (e.g. `CREDENTIALS_REQUIRED`, `BUDGET_EXCEEDED`, `VALIDATION_FAILED`).
- Pagination: opaque `cursor` plus `limit` (default 500, max 5,000); responses include `nextCursor`.
- GeoJSON via `Accept: application/geo+json` or `?format=geojson`. Features carry `id = "<source>:<externalId>"` and properties `{source, externalId, kind, version, seq, observedAt, validUntil, payload}`.
- **Auth:** a token filter checks `Authorization: Bearer fsr_…` against the scope that `AccessPolicy` requires for the method and path (§15). Data GETs are open by default (`security.openReads=true`). Writes and configuration reads need the scope the policy lists. Comparison is constant-time against stored hashes.
- **CORS:** allowed origins come from `app_setting.cors` (default `*` for GET/HEAD/OPTIONS; writes require an explicit origin list).
- **The spec is authored, not generated.** `docs/OpenAPI.json` is copied onto the classpath at build time and served verbatim at `/v1/openapi.json`; Swagger UI at `/docs` points to it. springdoc generates a spec from the code at the internal path `/v1/openapi-generated.json`, which exists only so a contract test can assert the implementation matches the authored spec (same paths, methods and operationIds). See §12.

### 10.1 API evolution

`/v1` is **additive only** (NFR-10). Prompt 020 wrote these rules down; `OpenApiContractIT`, `SpecAnnotationsTest`, `PostmanCollectionContractTest` and `./gradlew lintOpenApi` enforce the parts a machine can check.

- **Allowed in `/v1`:** new operations, new optional request fields, new response fields, new enum values, new error codes and new SSE event types.
- **Clients must be tolerant:** they ignore unknown fields, unknown enum values and unknown SSE event names. The generated SDKs (prompt 141) must be configured to do the same, for example without strict enum deserialization.
- **Never in `/v1`:** removing or renaming a field, changing a field's type, tightening the validation of an existing field, or adding a required request field. Such a change goes to `/v2`.
- **Deprecation:** mark the operation or field `deprecated: true` in the spec, and send the `Deprecation` (RFC 9745) and `Sunset` (RFC 8594) response headers. It stays for at least one minor release before it is removed in `/v2`.
- **Every spec change** regenerates Postman (`./gradlew generatePostman`), passes `./gradlew lintOpenApi`, is recorded in `info.x-foresignal-changelog`, and bumps `info.version` by a minor version when it adds to the API. After v1.0.0, prompt 144 adds oasdiff breaking-change checks in CI.
- **Every operation** carries `x-foresignal-scope` (`open`, `read`, `agent`, `admin` or `peer`), `x-foresignal-prompt` and `x-foresignal-status`. An operation stays on `src/test/resources/openapi/pending-operations.txt` until it is implemented.
- **Error codes** are stable identifiers in `ErrorCodes`; a code is never reused for a different condition.
- **Events:** SSE payloads are described in the spec, and each SSE operation maps its `event:` names to schemas in `x-foresignal-sse-events`. Outbound requests (the notification webhook, the watchdog ping) are described in the top-level `webhooks` object.

## 11. Retention

`RetentionJob` runs every hour (and on demand via `POST /v1/settings/retention/run`):

1. For each source: window = source override ?? global default (default `7d`).
2. Delete `record_head` rows whose `observed_at < now() - window` (current but stale), then delete `record_version` rows with `ingested_at < now() - window` and `seq` not referenced by any head. Work in chunks of 10,000 rows per transaction to avoid long locks.
3. Delete `alert` older than `alerts` retention (default `90d`), `source_usage` and `llm_usage` older than `usage` retention (default `30d`), delivered or failed `notification_delivery` older than 7 days, `briefing_run` and `ask_thread` older than `briefings` / `ask` retention (default `180d` / `90d`), and `embedding_document` rows whose record no longer exists.

Retention settings shape: `{"default":"7d","alerts":"90d","usage":"30d","briefings":"180d","ask":"90d","sources":{"usgs-earthquakes":"3650d"}}`.

## 12. Testing strategy

Per the methodology, tests are **written with every prompt but executed only once the project is code-complete** (prompt 061, Full Test Run And Fix).

| Layer | Tooling | Scope |
|---|---|---|
| Unit | JUnit 5, AssertJ, Mockito | Hashing, budget math, health derivation, rule SQL builder, duration parsing, cursor encoding, template rendering, parsers |
| Adapter contract | Fixture files + parser tests | Each adapter's parser against a recorded upstream response |
| Adapter integration | WireMock | Each adapter through `UpstreamClient` (budget, headers, 429 backoff) |
| Persistence integration | Testcontainers with the Foresignal DB image (PostGIS + pgvector, built from `docker/db/Dockerfile`, a compatible substitute for `postgres`) | Writer versioning, head upserts, spatial queries, MVT, retention, trigger SQL, vector search |
| LLM | WireMock stubs for the Anthropic, OpenAI-compatible and Ollama APIs; a deterministic `FakeLlmClient` | Tool loops, budgets, fallbacks, prompt-injection containment. **No test calls a real model.** |
| Notifications | WireMock receivers per channel type; GreenMail (or equivalent) for SMTP | Payload formats, retries, rate limits |
| API integration | `@SpringBootTest` + MockMvc + Testcontainers | Every endpoint, auth rules, ProblemDetail errors, SSE replay and resume |
| OpenAPI contract | Test comparing springdoc output to `docs/OpenAPI.json` paths and operations | Prevents drift |
| End-to-end | Testcontainers + WireMock USGS | Enable source → ingest → revision → trigger → SSE alert → channel delivery received |

JaCoCo enforces ≥ 90% line coverage (`jacocoTestCoverageVerification`), excluding generated code and `ForesignalApplication`.

## 13. Configuration

File-based configuration is limited to bootstrap concerns, all overridable by environment variables:

| Env | Default | Purpose |
|---|---|---|
| `FORESIGNAL_DB_URL` | `jdbc:postgresql://localhost:5433/foresignal` | Database |
| `FORESIGNAL_DB_USER` / `FORESIGNAL_DB_PASSWORD` | `foresignal` / `foresignal` | Database |
| `FORESIGNAL_PORT` | `8080` | HTTP port |
| `FORESIGNAL_DATA_DIR` | `./data` | Secret key file location |
| `FORESIGNAL_RESET_ADMIN_TOKEN` | `false` | Regenerate admin token on start |

Everything else — sources, budgets, credentials, retention, triggers, channels, tokens, LLM providers, briefings, CORS — is stored in the database and managed through the API.

## 14. Deployment

```
docker-compose.yml
├── db      build: docker/db (PostGIS + pgvector)  volume pgdata  port 5433:5432  healthcheck pg_isready
├── foresignal   build: .   volume foresignal-data:/data  port 8080  depends_on db (healthy)  restart: unless-stopped
└── ollama  ollama/ollama  volume ollama  port 11434   profile "local-llm" (optional; `docker compose --profile local-llm up -d`)
```

- `docker compose up -d` serves both the always-on bench server and occasional use.
- The JAR also runs standalone (`java -jar foresignal.jar`) against any Postgres 15+ with PostGIS. pgvector is optional.
- For internet exposure, put it behind a reverse proxy with TLS. Documentation will include a Caddy example.

## 15. Access tokens and scopes

- **Scopes**, from least to most privileged: `read` < `agent` < `admin`.

| Scope | Can |
|---|---|
| `read` | Every data GET (needed only when `security.openReads=false`), semantic search, MCP read tools, Ask without actions, Web Push subscription |
| `agent` | `read` plus: create, update and delete triggers and briefings; acknowledge alerts; run briefings; Ask with actions; MCP write tools |
| `admin` | Everything: sources, credentials, custom sources, channels, LLM providers, tokens, settings, retention, CORS, system |

- **Tokens** live in `api_token`. The plaintext (`fsr_` + 32 random bytes, base64url) is shown once at creation; only the SHA-256 hash is stored. Tokens can expire and be revoked, and `last_used_at` is updated at most once a minute.
- **Bootstrap token:** the first-start admin token (§7.2) is the `api_token` row named `bootstrap` with scope `admin`. Rotation and reset keep working as described in §7.2.
- **`AccessPolicy`** is the single table mapping (method, path pattern) to a minimum scope. The token filter enforces it, and the MCP server uses the same scopes for tools.
- **Settings:** `app_setting['security'] = {"openReads": true, "anonymous": {…}, "metricsOpen": true}`. When `openReads` is false, data GETs require at least `read`. `metricsOpen: false` makes `/actuator/prometheus` admin-only.
- **API:** `GET/POST /v1/tokens`, `DELETE /v1/tokens/{id}` (admin).

### 15.1 Read restrictions (035)

- **`ReadRestriction`** (`ai.foresignal.security.restrict`): `sources`, `kinds`, `bbox` and/or `area` (MultiPolygon 4326, ≤ 2,000 vertices; their intersection when both are set), `endpoints`, `excludePrivate`, `maxRequestsPerMinute`. Stored on `api_token` as columns (`allowed_sources`, `allowed_kinds`, `allowed_bbox`, `allowed_area`, `allowed_endpoints`, `exclude_private`, `max_requests_per_minute`). Restrictions on admin tokens are refused (`400 RESTRICTION_NOT_ALLOWED`).
- **`RequestPrincipal`** (request attribute): a token, a feed key or an anonymous caller, with its effective restriction. `TokenFilter` binds it for tokens and anonymous reads; `PathKeyFilter` binds it for feed keys. Endpoint allowlists are checked in the filters (`403 ENDPOINT_NOT_ALLOWED` for token patterns, `403 RESTRICTED` for the anonymous profile and feed-key prefixes).
- **`RestrictionClamp`** is the one place that turns a restriction into predicates. **Intersect, never union:** the restriction's predicate (`source_id = ANY`, `kind = ANY`, `geom && envelope AND ST_Intersects(geom, region)`) is ANDed into every query, so an absent request filter becomes the restriction and a disjoint one yields an empty result. A single record or alert outside it is `404`, never `403`. Streams, push fan-out and citations use its in-memory `test()`; alerts are judged by the geometry of their record snapshot. A record without geometry never passes a spatial restriction. Responses to a restricted caller are `Cache-Control: private`.
- **Coverage:** records, history, versions, changes, kinds and kind schemas, every records format, tiles (inside the MVT SQL) and TileJSON, the change/live/touch lanes, `/v1/events/stream` (a PATCHed filter keeps the restriction), `/v1/alerts/stream`, `/v1/notifications/stream`, search, alerts, the inbox and badge, the digest, briefings (a run uses the creating token's `restriction_snapshot`; a restricted caller sees only briefings made under a restriction within its own), MCP tools (through `ToolContext.restriction()`), Ask (the tool loop runs with the caller's principal) and push fan-out (targets of a restricted token receive only alerts that pass `test()`).
- **Rule for new read paths:** every new endpoint, MCP tool or export that returns records or alerts must apply `RestrictionClamp` and carry `@RestrictedRead`; `RestrictedReadCoverageTest` fails otherwise. The 023 private gate stays orthogonal; `excludePrivate` adds private-source exclusion to restricted principals.
- **Anonymous profile:** `security.anonymous` is applied to tokenless reads while `openReads` is on. Default: no source, kind or area limit, private sources excluded, data GETs only, 120 requests per minute. `GET /v1/system` reports `security.warnings` `ANONYMOUS_UNRESTRICTED` (also logged) when `openReads` is on, `publicBaseUrl` is not loopback or private, and the profile limits nothing.
- **Feed keys:** `api_token` rows with `type = feed_key`, prefix `fsf_`, scope `read`, `prefixes text[]`, a mandatory restriction and `expires_at` ≤ 365 d (`GET/POST /v1/feed-keys`, `DELETE /v1/feed-keys/{id}`, admin). `PathKeyFilter` (before `TokenFilter`) resolves `/v1/k/{key}/<rest>`: constant-time lookup, `GET`/`HEAD` only (`405`), `/v1/<rest>` under one of the prefixes (`403 RESTRICTED`), invalid `401 INVALID_FEED_KEY`, expired or revoked `410 FEED_KEY_GONE`; it then forwards the request to the canonical path within the filter chain. Responses carry `Referrer-Policy: no-referrer`, `X-Robots-Tag: noindex` and `Cache-Control: private`. The key is redacted in the Tomcat access log (pattern on the `ai.foresignal.logUri` attribute) and in the Caddy example; Micrometer tags use route templates. Resolvers are pluggable by prefix (`PathKeyResolver`): 050 adds `fsk_` tile keys and 112 `fss_` share keys on the same filter.

### 15.2 Rate limits and auth-failure backoff (035)

- **Per-client rate limits.** `ClientRateLimitFilter` runs right after `TokenFilter` and keeps in-memory Bucket4j buckets per client key: `token:<id>`, `feed:<id>`, `ingest:<principal>` or `ip:<address>`. The address is `request.getRemoteAddr()` as Tomcat's RemoteIpValve resolved it through `FORESIGNAL_TRUSTED_PROXIES`; a raw `X-Forwarded-For` is never read. Classes: `tiles` (tile and TileJSON GETs), `stream` (SSE opens, 30 per minute and 8 concurrent per client), `expensive` (`/v1/search`, Ask, `/v1/sample…`, and LLM-backed MCP tools charged by `McpToolRegistry`), `write`, `read`; tokenless callers are also charged to `anonymous` per IP. A token's `maxRequestsPerMinute` replaces `read`. Admin tokens are exempt from `read` and `tiles` only. Over a limit: `429 RATE_LIMITED` with `Retry-After`. Limits live in `app_setting['ratelimits']` (`GET/PUT /v1/settings/ratelimits`); `metricsPerMinute` optionally limits `/actuator/prometheus` per IP.
- **Auth-failure backoff.** `AuthFailureBackoff` counts failed credential checks per client IP (bearer tokens, tickets, ingest keys, feed keys, action capabilities). After 5 failures within 60 s each further failure is answered `429` with `Retry-After` doubling from 1 s to 300 s. The credential is always evaluated first, so a correct one is accepted (no lockout of the owner behind NAT) and resets the address.
- **Metrics:** `foresignal.ratelimit.rejected{class}`, `foresignal.auth.failures`, `foresignal.auth.backoff.active`.

### 15.3 Outbound URL policy (035)

Every operator-supplied outbound URL passes `ai.foresignal.net.OutboundUrlPolicy` when it is saved (`400 URL_NOT_ALLOWED` on the field; a host that does not resolve yet is accepted where noted in the spec) and again on every send (a delivery failure). It checks the shape first: `https`, plus `http` in the LAN profiles, and no user info. The host must be a strict dotted-quad IPv4 literal, a valid IPv6 literal (no zone id), or a name; other numeric IPv4 forms (decimal, octal, hex, short) are refused. A name is resolved once, and every address must pass. IPv4 embedded in mapped, compatible, NAT64, 6to4 and Teredo forms is extracted and judged. Addresses are classified against the IANA special-purpose registries (RFC 6890 as updated by RFC 8190), multicast and the cloud metadata addresses.

Profiles:

- `PUBLIC_ONLY`: globally routable only.
- `LAN_ALLOWED`: public plus loopback, RFC 1918, ULA and 100.64/10. Used for channels, LLM `baseUrl`, FCM `tokenUri` and the watchdog.
- `LAN_ONLY`: loopback, RFC 1918 and ULA. Used for `LAN_URL` parameters.
- `PUSH_ENDPOINT`: `PUBLIC_ONLY` plus `push.allowedPrivateHosts`.

Link-local, metadata (169.254.169.254, fd00:ec2::254, 100.100.100.200), unspecified, multicast, broadcast, documentation, reserved and the other not-forwardable blocks are refused in every profile. Admin overrides in `app_setting['outbound']` (`allowHosts`, `allowCidrs`, `denyHosts`, `denyCidrs`; `GET/PUT /v1/settings/outbound`) apply everywhere: deny wins, and allow never unblocks the always-blocked set.

Senders connect to the checked address with the original `Host` and TLS name and never follow redirects. `PinnedHttp` does this for channels and the LLM clients (including the Anthropic SDK, through a replaced transport), `PushHttp` for Web Push, `UpstreamClient` for `LAN_URL`, and `EdpHttpClient` for EDP providers, which re-checks each of at most 3 same-host redirects. SMTP, the FCM token endpoint and the watchdog check just before connecting and rely on the JVM DNS cache. With a JVM or environment HTTP proxy configured, `LAN_ONLY` is refused and a startup warning says address checks are bypassed for proxied requests. New features that take an operator URL (generic feeds, dataset-by-URL, kits) must use this policy (Adapter Guide §12).

## 16. Inbound webhooks and custom push sources

Some data can't be polled: a local SDR, an ais-catcher instance, Home Assistant, a script, an n8n flow or an OpenClaw agent that wants to report something. These push into Foresignal.

- **Mode `PUSH`:** the adapter SPI's `Mode` gains `PUSH`. The scheduler never polls push sources, and budgets apply as inbound rate limits (`perMinute` requests).
- **Custom sources** are created at runtime through `POST /v1/custom-sources` (admin), stored in `custom_source`, and registered in `SourceRegistry` as dynamic descriptors.
  - The id must start with `custom-`.
  - The source declares its kinds, attribution, TTL, `tileProperties`, `searchFields` and `embedFields`.
  - It appears in `/v1/sources` with settings, health and retention like any other source.
  - Creation returns an **ingest key** once; it can be rotated. Deleting a custom source stops ingest; its records age out through retention.
- **Ingest endpoint:** `POST /v1/ingest/{sourceId}` accepts either a GeoJSON FeatureCollection (`id` maps to `externalId`, and properties `kind` and `observedAt` are required unless the source has exactly one kind, in which case `observedAt` defaults to now) or `{"records": [...]}` in the `NormalizedRecordPreview` shape.
  - Auth: `X-Foresignal-Ingest-Key: <key>`, or a bearer token with `admin` scope.
  - Limits: 5 MB per request and 10,000 records.
  - Records go through the same `IngestQueue`, so versioning, change stream, triggers, embeddings and retention all apply.
  - Response: an `IngestOutcome` (seen, changed, rejected with reasons).
- Built-in adapters may also declare `PUSH` mode, for example a future ais-catcher receiver.

## 17. Notification channels

One delivery system serves alerts, briefings and system events across many destinations.

| Type | Non-secret config | Secret config | Delivery |
|---|---|---|---|
| `webhook` | `url`, `method` (POST/PUT), `headers`, `bodyTemplate` (optional; default Foresignal JSON) | `hmacSecret`, secret header values | JSON; `X-Foresignal-Event`, `X-Foresignal-Delivery`, `X-Foresignal-Timestamp`, `X-Foresignal-Signature: sha256=HMAC("<timestamp>.<body>")` |
| `slack` | `username`, `iconEmoji` | `webhookUrl` (incoming webhook) | Block Kit message, mrkdwn |
| `mattermost` | `channel`, `username`, `iconUrl` | `webhookUrl` (incoming webhook) | Slack-compatible payload with Mattermost markdown and attachments |
| `discord` | `username` | `webhookUrl` | Embeds; colour by severity |
| `teams` | — | `workflowUrl` (Workflows/Power Automate webhook) | Adaptive Card |
| `google-chat` | — | `webhookUrl` | Card v2 |
| `telegram` | `chatId` | `botToken` | Bot API `sendMessage`, HTML parse mode |
| `matrix` | `homeserverUrl`, `roomId` | `accessToken` | `m.room.message` with `formatted_body` |
| `ntfy` | `serverUrl` (default `https://ntfy.sh`), `topic` | `accessToken` (optional) | Title, priority from severity, tags, click URL |
| `gotify` | `serverUrl` | `appToken` | Priority from severity |
| `pushover` | — | `appToken`, `userKey` | Priority from severity |
| `email` | `smtpHost`, `smtpPort`, `tls` (`starttls`/`ssl`/`none`), `from`, `to[]` | `username`, `password` | Multipart text + HTML |
| `webpush` | `quietHoursMode` (`defer`/`downgrade`/`drop`); one built-in channel "Push" | — | Fans out to Web Push and UnifiedPush subscriptions and to APNs and FCM devices (§24) |

- **Message model:** every event becomes a `NotificationMessage`: `event` (`alert`/`briefing`/`system`/`test`), `title`, `body` (markdown), `severity` (`info`/`warning`/`critical`), `url` (API link to the record, alert or run), `data` (the full alert or briefing-run JSON). A formatter per type converts markdown to the destination's format and escapes it correctly (for example Telegram HTML, Slack mrkdwn, Matrix HTML).
- **Routing:** a channel subscribes to events, optionally limited by `rule_ids` and `min_severity`.
  - `title_template` and `body_template` use the same placeholder syntax as trigger messages, plus `{{severity}}`, `{{summary}}` and `{{url}}`.
  - System events: source turned `failing`, LLM budget exhausted, delivery permanently failed.
- **Delivery:** `notification_delivery` rows are claimed with `FOR UPDATE SKIP LOCKED`.
  - Each channel has a rate limit sized to its destination: Slack about 1/s, Discord 5 per 2 s, Telegram 20/min per chat, ntfy and Gotify 10/s, Pushover 2/s, email 30/min.
  - `429` responses honour `Retry-After`.
  - Retry ladder: 10 s, 1 m, 5 m, 30 m, 2 h, then `failed` (which also raises a system event, never sent back to the failing channel).
- **Enrichment wait:** `wait_for_enrichment_seconds` (0–60) delays alert delivery so the LLM summary (§23) can be included.
- **Security:** channels are admin-only, and secret config is encrypted with `CredentialCipher` and never returned. Private-network destinations (a LAN Gotify or ntfy) are allowed, because channels are operator-configured. Outbound traffic is not counted against source budgets.
- **API:** CRUD under `/v1/channels`, `POST /v1/channels/{id}/test`, `GET /v1/channels/{id}/deliveries`, `GET /v1/channels/types` (the type catalogue with config field specs).
- **Mute (033):** `POST /v1/triggers/{id}/mute` (`until` or `for`, 1 h to 7 d) sets `trigger_rule.muted_until`. A muted rule still records alerts and they stay in the inbox, but they are routed to no channel and its open alerts stop escalating; `DELETE …/mute` clears it.

### 17.1 Acting from notifications (033)

- **Capabilities:** a firing alert's first send carries single-use action capabilities (`ack`, `snooze` 1 h, `mute` 4 h by default; snooze 15 m–24 h, mute 1 h–7 d), minted at render time per delivery (`action_capability`: `token_hash` SHA-256, `alert_id`, `action`, `params`, `channel_id`, `delivery_id`, `device_id`, `expires_at` 12 h by default and at most 24 h, `used_at`, `used_via`, `result`).
  - Token: `eac_<base64url(16 random bytes)>.<base64url(HMAC-SHA256(k, random)[0..8])>`, 38 characters, so `ea:` + token fits Telegram's 64-byte `callback_data`. `k` is 32 bytes in `system_secret['action.hmac']`, encrypted with the install key. The MAC is checked in constant time before any lookup.
  - `GET /v1/actions/{token}` never acts: it returns a server-rendered confirm page (no JavaScript, no external assets; `Cache-Control: no-store`, `Referrer-Policy: no-referrer`, `X-Robots-Tag: noindex`, `Content-Security-Policy: default-src 'none'; style-src 'unsafe-inline'; form-action 'self'`). `POST` executes once, in one transaction with `SELECT … FOR UPDATE`; JSON for `Accept: application/json`, else an HTML page. An `Origin` that is neither the `publicBaseUrl` origin nor the request's own origin is refused (403). Both share 30 requests/min per client IP; the path is open in `AccessPolicy` and the token is redacted (`/v1/actions/eac_…`) in the audit log and problem documents.
  - Ack records `acknowledged_by = action:<channelType>:<channelName>` (`action:device:<key>` for app capabilities); an already acknowledged or resolved alert returns `already-<state>`. Every execution is audited (actor kind `action`). Acknowledging or resolving an alert by any path supersedes its open capabilities.
- **Per type:** ntfy gets two `http` POST actions with `clear=true` plus `view, Open`; Web Push gets `actions` (≤ 2) and `confirmUrl` (declarative payloads navigate to the confirm page); native pushes get the category `FORESIGNAL_ALERT_ACTIONABLE` and the app fetches its own capabilities from `GET /v1/notifications/{id}`; Telegram gets an inline keyboard executed by `TelegramCallbackPoller` (long-polled `getUpdates`, offset in `app_setting['telegram.<channelId>.offset']`, presser checked against `allowedUserIds` or the chat, `answerCallbackQuery`, then the message is edited and the keyboard removed; the bot token must be dedicated to Foresignal); Matrix gets confirm-page links and, with `actions.reactions`, a ✅ reaction from `allowedSenders` acknowledges (`/sync` filtered to the room and `m.reaction`, `since` persisted); Pushover's emergency receipts are polled every 60 s and an acknowledged receipt acknowledges the alert as `pushover:<device>`, and its URL is the acknowledge confirm page; webhook gets an `actions` array; every other type gets confirm-page links in the body when `actions.links` is on. `config.actions.enabled` defaults to true for ntfy, webpush, telegram, matrix and pushover and to false elsewhere.

### 17.2 Notification inbox (033)

- Every persisted `notification` row (`alert`, `briefing`, `system`) is an inbox item for the `inbox` retention window (default 30 d; separate from `notifications`). `GET /v1/notifications` lists newest first (keyset on id) with the live alert state; `GET /v1/notifications/{id}` adds links and capabilities minted for the calling device.
- Read state is per device: `notification_read_cursor` (`device_key`, `read_up_to_id`, `updated_at`) plus sparse `notification_read` rows above it (deleted when the cursor passes them, at most 500 per device). The device key is the token id of a token with device metadata, else `<tokenId>:<Foresignal-Device-Id>`, else `400 DEVICE_ID_REQUIRED`. `POST /v1/notifications/read` moves the cursor or adds sparse reads; reading never acknowledges an alert.
- `GET /v1/notifications/badge` counts unread system notifications and unread fired alerts still firing (the same count is `aps.badge` per native device). `GET /v1/notifications/stream` is the `inbox` topic of the 015 SSE hub: `notification` events (id = notification id, replayed after `Last-Event-ID` with the device's read state) and id-less `badge` events to the device's other streams when its read state changes.
- Cursors of devices unseen for 90 d are deleted.

### 17.3 Presence deferral and escalation (033)

- **Presence:** `client_presence` (`device_key`, `kind` desktop/mobile/web, `state` active/idle/background, `expires_at`). Clients heartbeat with `POST /v1/clients/presence` (ttl ≤ 300 s); an open inbox or alert stream counts as `background` only. A channel with `deferWhileActiveSeconds` (0–600) holds a non-critical `alert.fired` that long while any desktop is active (`deferred_reason = presence`). At claim time the send is marked `suppressed` if the alert was acknowledged, snoozed or resolved, or the notification read on a desktop, meanwhile. Critical alerts and system events are never deferred; stale presence fails open.
- **Escalation:** `escalation_policy` (`steps` ≤ 5 with strictly increasing `afterMinutes` 1–1440, `channelIds`, `includePush`; optional `repeat {everyMinutes, maxTimes ≤ 10}`; `stopOn`: ack always, resolve and snooze by default) is referenced by `trigger_rule.escalation_policy_id`. When such a rule's alert fires, `alert_escalation` is created (the rule's own routing is the first notification). The `EscalationWorker` (15 s, `FOR UPDATE SKIP LOCKED`, advanced in the claim transaction) sends each due step as an `alert.escalated` notification titled `[Escalation <n>]` with fresh capabilities to the step's channels, repeats the last step, then marks the row `exhausted`. Quiet hours apply except to critical alerts. Ack, resolve and snooze (per `stopOn`) and a rule mute stop it at once: pending escalation deliveries become `suppressed` and Pushover receipts of escalation messages are canceled.

## 18. MCP server

- **Transport and auth:** the MCP Java SDK with the Spring WebMVC **streamable HTTP** transport at `/mcp`. Bearer tokens are used, and when `openReads` is on, read tools also work without a token.
- **Tools** are thin wrappers over the service layer (not HTTP loopback), each declaring the scope it needs:

| Group | Tools |
|---|---|
| Records | `query_records`, `get_record`, `get_record_history`, `get_changes`, `list_kinds` |
| Search | `search` (semantic, text or hybrid) |
| Sources | `list_sources`, `source_health` |
| Alerts | `list_alerts`, `get_alert`, `acknowledge_alert` (agent) |
| Watches | `list_watches`, `create_watch` (agent), `update_watch` (agent), `delete_watch` (agent), `test_watch` |
| Briefings | `list_briefings`, `create_briefing` (agent), `run_briefing` (agent), `get_briefing_run` |
| System | `system_status` |

- **Output shaping:** `detail: short|long`, list caps with `*_total` and `truncated` flags, and optional geometry simplification. This keeps agent context budgets small.
- **Resources and prompts:** resources `foresignal://sources` and `foresignal://kinds`, and a `situation-report` prompt.
- **Clients:** OpenClaw (as an MCP skill), Claude Desktop and Claude Code, n8n, and any MCP-capable agent.

## 19. LLM subsystem

Optional. Without a configured provider, Foresignal is fully functional: briefings use digest mode, search uses text matching, enrichment is skipped, and Ask returns `409 LLM_NOT_CONFIGURED`.

- **Providers** live in `llm_provider` and are managed through `/v1/llm/providers`. Roles are set in `app_setting['llm'] = {chatProvider, embeddingProvider, askEnabled, enrichmentEnabled, logContent}`.

| Type | Implementation | Notes |
|---|---|---|
| `anthropic` | Official Anthropic Java SDK (`com.anthropic:anthropic-java`; verify the current version) | Default model `claude-opus-5-5`. Adaptive thinking is always on, so control depth with `effort` (Opus 5.5's default is `medium`; Foresignal sets it explicitly per use: Ask `medium`, briefings `medium`, enrichment `low`). Forced `tool_choice` is not supported, so use `auto` and instruct in the prompt. Enable server-side refusal fallback (`fallbacks: "default"`, beta `server-side-fallback-2026-07-01`) and check `stop_reason == "refusal"` before reading content. Stream long outputs. Cache the stable system prompt and tool definitions with `cache_control`. **No embeddings endpoint**, so pair it with a separate embedding provider. |
| `openai-compatible` | `java.net.http` client for `/chat/completions` and `/embeddings` | OpenAI, OpenRouter, Groq, Mistral, vLLM, LM Studio, llama.cpp server, LocalAI |
| `ollama` | `java.net.http` client for native `/api/chat` (with tools) and `/api/embed` | Local by default (`http://ollama:11434` with the compose profile). Typical choices: a Qwen or Llama chat model with tool support, `nomic-embed-text` (768 dims) or `mxbai-embed-large` (1024) for embeddings |

- **`LlmClient` abstraction:** `chat(system, messages, tools, options)` returns text plus tool calls plus usage, and `embed(texts)` returns vectors. Tool calling is normalized across providers. A provider whose model lacks tool support runs Ask in retrieval-only mode (§21).
- **Budgets and metering:** each provider has `requests_per_minute` (Bucket4j) and `tokens_per_day`, which is checked before a call using an estimate and recorded after from actual usage, in `llm_usage`. When a budget is exhausted the call is refused with `LLM_BUDGET_EXCEEDED` and a system notification is raised. Provider health mirrors source health.
- **Safety:**
  - Record content (news titles, warnings, free text) is **untrusted**. Tool results are passed as clearly delimited data, and the system prompt says instructions inside data must be ignored.
  - Models can never change sources, credentials, channels, providers, tokens or settings. The only model-initiated writes are *proposed* actions (create or update a watch, create a briefing, acknowledge an alert). These run only after confirmation, or automatically when the caller holds `agent` scope and set `allowActions`.
  - Every call is logged with metadata (provider, model, tokens, duration, purpose). Prompt and response content is logged only when `logContent=true`.
- **Knowledge cutoff:** each provider model may declare `knowledgeCutoff` (a date). The forecast ledger uses it to exclude leakage-prone questions when evaluating an LLM forecaster retrospectively (Intelligence Design §8.5).
- **Data egress:** with a frontier provider, the records the model reads are sent to that provider. This is the operator's choice, and the local Ollama profile keeps everything on the box. The Operator Guide states it plainly.

## 20. Embeddings and semantic search

- **Storage:** `embedding_document` holds one row per embedded document: a record (latest version only, replaced when the head moves), a briefing run or an enriched alert. Each row carries `geom`, `observed_at`, `source_id` and `kind`, so searches can be filtered by space, time and source.
- **Dimensions:** the vector width is fixed by the active embedding provider. Changing the embedding model triggers a **reindex job**, which recreates the column and HNSW index with the new dimensions and backfills. The HNSW index uses `vector_cosine_ops` and is created by native DDL, because the dimension is dynamic.
- **What is embedded:** records from sources whose descriptor declares `embedFields` (text-heavy feeds such as GDELT, NWS/MeteoAlarm alerts, GDACS, WHO outbreak notices, NGA warnings, SEC filings, advisories), plus briefing runs and enriched alerts.
  - The `EmbeddingWorker` subscribes to the `ChangeBus` and keeps a durable cursor (`embedding.cursor`).
  - It batches 64 texts per call, skips unchanged `text_hash`, and respects the embedding provider's budget.
  - The operator can enable or disable embedding per source with the settings flag `embed`, which defaults to on for sources that declare `embedFields`.
- **Search API:** `GET /v1/search?q=&mode=semantic|text|hybrid&source=&kind=&bbox=&since=&until=&limit=`.
  - `text` uses the `tsv` column (and works without an LLM by falling back to `record_head.search_text`).
  - `semantic` uses cosine distance.
  - `hybrid` fuses the two with reciprocal rank fusion.
  - Results include score, document type, record reference and a snippet.

## 21. Ask Foresignal

- **Requests:**
  - `POST /v1/ask` takes `{question, threadId?, scope?: {sources, kinds, bbox, since, until}, allowActions?: false}` and returns `{threadId, answer (markdown), citations: [{id, seq, title}], proposedActions: [...], usage}`.
  - `POST /v1/ask/stream` returns the same over SSE (`progress`, `delta`, `citation`, `action`, `done`).
  - Threads persist in `ask_thread` and `ask_message` for follow-up questions.
- **Loop:** the chat provider runs with the read tool catalogue from §18 plus `propose_watch`, `propose_briefing` and `propose_ack`. Limits: at most 12 tool iterations, 120 s, and a per-ask token cap (setting `llm.askMaxTokens`).
- **Citations are verified:** the answer must cite records returned by tools. The server drops citation ids that never appeared in tool output and flags uncited factual claims in `warnings`.
- **Proposed actions** are stored in `ask_action` as `pending`. `POST /v1/ask/actions/{id}/confirm` (agent scope) executes one, and `/reject` discards it. With `allowActions=true` and `agent` scope, actions execute immediately, and are still recorded.
- **Retrieval-only mode** (provider without tools): hybrid search over the scope (top 40) plus recent alerts, then a single completion with the same citation rules.
- **Scope:** Ask requires `read` scope (or open reads). Actions require `agent`.

## 22. Scheduled briefings

- **Definitions** live in `briefing`:
  - schedule: a standard 5-field cron plus an IANA timezone
  - scope: sources, kinds, bbox, rule ids, `includeHealth`
  - `lookback` (default: the time since the previous run, capped at 7 d)
  - `mode`: `digest` or `llm`
  - `instructions` (LLM mode)
  - `channel_ids`
- **`BriefingScheduler`** checks every 30 s for definitions whose `next_run_at` has passed, using Spring `CronExpression` with the timezone. Each run executes on a virtual thread and persists `next_run_at`, so missed runs after downtime run once and are not replayed.
- **Digest mode** is deterministic markdown, no LLM:
  - alerts in the period grouped by severity
  - counts per kind and source with the delta against the previous period
  - top N notable records per kind, ranked by the descriptor's optional `rankField` (e.g. `mag` for quakes, `frp` for fires) or else by recency
  - sources that turned degraded or failing
- **LLM mode:** the digest statistics plus retrieved records (scope-filtered hybrid search on the instructions, plus top-ranked records) go to the chat provider with the instructions. The result is narrative markdown with verified citations. If the LLM fails, the run falls back to digest and is marked `fallback`.
- **Output:** each run is stored in `briefing_run`, embedded (§20), delivered to its channels as a `briefing` event, and available at `GET /v1/briefings/{id}/runs` and `/runs/{runId}`.
- **API:** CRUD under `/v1/briefings`, plus `POST /v1/briefings/{id}/run` (run now) and `POST /v1/briefings/preview` (render without storing or delivering).

## 23. Alert enrichment

- **Rule options:** `enrich` (bool), `enrichRadiusKm` (default 100) and `enrichWindow` (default 24 h).
- **Trigger:** when a rule with `enrich=true` fires and `llm.enrichmentEnabled` is on, the `EnrichmentWorker` (an `AlertBus` listener) gathers context:
  - the record and its version history
  - records of any kind within the radius and window (spatial query)
  - semantic neighbours when embeddings are on
  - recent alerts nearby
- **Summary:** the chat provider (effort `low`) writes a summary of at most 120 words with citations. It is stored in `alert.summary` and `enriched_at`, emitted as an `alert.enriched` SSE event, and embedded.
- **Channels:** delivery includes the summary when the channel's `wait_for_enrichment_seconds` allows. Otherwise the alert is sent immediately without it.
- **Failure:** an enrichment failure never blocks or delays alert delivery beyond the channel's wait.

## 24. Push

Foresignal pushes notifications itself, without a third-party push account for the web: it holds its own VAPID key. All push transports share one SPI and one fan-out (prompt 031), so native push (032) adds transports and target sources without changing the rest.

### 24.1 Web Push

- **Keys:** a VAPID key pair (P-256) is generated on first start: the public point (base64url, 87 characters) in `system_secret` `push.vapid.public`, the PKCS#8 private key encrypted with `CredentialCipher` in `push.vapid.private`. `GET /v1/push/vapid-public-key` is open.
- **Crypto (JDK only):** RFC 8291 message encryption in the RFC 8188 `aes128gcm` coding (ephemeral ECDH P-256 key and random salt per message, HKDF-SHA-256 per RFC 5869, one record with the `0x02` delimiter, body `salt ‖ rs 4096 ‖ idlen 65 ‖ as_public ‖ ciphertext`). Java 21 has no HKDF API, so `Hkdf` implements it on `HmacSHA256`. VAPID (RFC 8292): an ES256 JWT `{aud: origin(endpoint), exp: now + 12 h, sub: push.contact}`, DER signatures converted to raw `R ‖ S`, cached per origin for 11 h, sent as `Authorization: vapid t=<jwt>, k=<publicKey>`.
- **Request:** `Content-Encoding: aes128gcm`, `Content-Type: application/octet-stream`, `TTL` (alert 86400, briefing 43200, system 3600, verification 600), `Urgency` (critical `high`, warning `normal`, info `low`, quiet-hours downgrade `very-low`) and `Topic` = `base64url(SHA-256(key))[0:32]` with key `alert:<ruleId>:<source>:<externalId>`, `briefing:<briefingId>` or `system:<code>`, so the push service replaces an undelivered message about the same entity.
- **Payload:** `{v:1, event, id, alertId?, ruleId?, severity, title, shortTitle, body, url, links {api, app, web}, tags, lang, tag, silent, ts}` (format `foresignal`), or the WebKit declarative wrapper `{web_push: 8030, notification: {title, body, navigate, lang, tag, silent, data}}` (format `declarative`, Safari 18.4+). The plaintext is at most 3,993 bytes; `PushPayloadBudget` shrinks body, then `links.api`, then tags (5), then the title (120 characters), with a minimal `{v, event, id, severity, shortTitle}` fallback.
- **Subscriptions:** `POST /v1/push/subscriptions` (read scope, always authenticated) validates an https endpoint (≤ 1000 bytes, no user info), a 65-byte `p256dh` on the curve and a 16-byte `auth`, and the address policy; caps are 5 pending per token and 200 in total. A new row is `pending` and receives a verification push with a random code; the creating token echoes it (`POST …/{id}/verify`) to make it `active`. Pending rows expire after 10 minutes. Only active rows receive notifications, so a read token cannot use Foresignal as a push amplifier. `DELETE` is for the creating token or an admin; `GET` (admin) lists `endpointHost`, never endpoints. Revoking a token deletes its subscriptions.
- **Address policy:** every address the endpoint host resolves to must be globally routable (no loopback, RFC 1918, CGNAT, link-local, ULA, IPv4-mapped or -compatible IPv6, NAT64, 6to4, Teredo, multicast, reserved, documentation or metadata addresses) unless the host, or every address, matches `push.allowedPrivateHosts` (exact host names or CIDRs, IPv4 ≥ /8, IPv6 ≥ /32). The check runs again before every send and the connection goes to the checked address with the original Host and SNI, never following redirects; three failed rechecks in a row disable a subscription. Prompt 035 replaces the check with `OutboundUrlPolicy` profile `PUSH_ENDPOINT`.

### 24.2 UnifiedPush

Native Android and Linux apps receive through a UnifiedPush distributor, which exposes an RFC 8030 endpoint and decrypts RFC 8291 messages: to Foresignal it is an ordinary Web Push subscription with `transportLabel: unifiedpush` (display and filtering only). The app passes Foresignal's VAPID public key to the distributor (`registerApp(vapid: …)`). A self-hosted distributor on the LAN is reached through `push.allowedPrivateHosts`; https is still required.

### 24.3 Native push (032)

The Flutter apps receive **visible** alerts through APNs (iOS, iPadOS, macOS) and FCM (Android). `ApnsTransport` and `FcmTransport` are further `PushTransport` beans, `NativeDeviceTargetSource` lists the devices and `NativeDeviceTarget(deviceId, transport, platform, appId, environment, token, contentLevel, lang)` joins the sealed `PushTarget`; the fan-out, `push_dispatch`, filters, TTL and quiet-hours handling of §24.4 are reused unchanged.

- **Self-hosted credentials:** each operator configures their own APNs auth key and Firebase service account (`/v1/push/providers`, admin). An APNs key belongs to the Apple team that signs the app and an FCM service account to the app's Firebase project, so native push works only for an operator who builds and signs their own app; a hosted relay is out of scope, and UnifiedPush (§24.2) and ntfy remain the account-free paths. `push_provider` holds `type`, `app_ids`, `config` (APNs `teamId`, `keyId`, `keyEnvironment` `sandbox|production|both`, `timeSensitive`; FCM `projectId`, `clientEmail`, `tokenUri`) and the PEM key encrypted with `CredentialCipher`; responses list only `secretFields`, and the settings bundle carries providers without keys.
- **Devices:** `/v1/push/devices` (read scope, always authenticated) registers an APNs token (64–200 hex, with its `environment`) or an FCM token (≤ 4096) for the calling API token. An enabled provider must serve the app (and, for APNs, cover the environment), else 409 `PUSH_PROVIDER_NOT_CONFIGURED`. Rows are upserted by (`transport`, SHA-256 of the token), so re-registering moves a device to the calling token; at most 20 per token. The token is encrypted; only its last six characters are shown. Revoking (`ApiTokenRevokedEvent`) or expiring (hourly sweep) the API token deletes its devices. `ClientDeviceResolver` identifies a request's device as its API token when the token carries 023 device metadata.
- **Payload:** ids, severity and a minimal text only: by the device's `contentLevel`, `minimal` ("Foresignal" / "Critical alert", "Briefing ready", "System notice") or the opt-in `short` ("Foresignal · critical" / the ≤ 40-character short title). Full titles and bodies never leave through Apple or Google; the app's Notification Service Extension or message handler fetches them over the user's own network path. The deep link `l` comes from 023 `clientLinks`.
- **APNs:** HTTP/2 over TLS (`java.net.http`, one connection per provider and environment) to `api.push.apple.com` or `api.sandbox.push.apple.com`, `POST /3/device/<token>` with an ES256 provider JWT (`kid`, `iss`, `iat`; reused 40 minutes, regenerated once on `ExpiredProviderToken`), `apns-push-type: alert`, `apns-priority` 10 (5 under a quiet-hours downgrade), `apns-expiration` (now + alert 24 h, briefing 12 h, system 1 h), `apns-collapse-id` (`a-` + the Topic) and `apns-id` (the dispatch row as a UUID). Body `{"aps":{"alert","sound","mutable-content":1,"interruption-level","thread-id","category"},"e":{"v","id","a","s","ev","l"}}` within 4096 bytes; `interruption-level` info `passive`, warning `active`, critical `time-sensitive` when the app has that capability, never `critical`; never background pushes. 410 and `BadDeviceToken`/`DeviceTokenNotForTopic` delete the device; 429 `TooManyRequests` and 5xx are retried; 403 and 429 `TooManyProviderTokenUpdates` are provider errors.
- **FCM:** HTTP v1 `POST https://fcm.googleapis.com/v1/projects/<projectId>/messages:send` with an OAuth 2.0 access token minted from the service account (RS256 JWT bearer grant, scope `firebase.messaging`, cached until `expires_in` − 300 s; JDK crypto, no `firebase-admin`). Android receives data messages (`HIGH` for warning and critical, `NORMAL` for info and downgrades; the app must show a notification for every HIGH message); Apple devices registered through FCM also get an `apns` block. `UNREGISTERED`, `SENDER_ID_MISMATCH` and an `INVALID_ARGUMENT` naming the token delete the device; 429 honours `Retry-After`; 500/503 are retried; 401 (after one token refresh) and `THIRD_PARTY_AUTH_ERROR` are provider errors.
- **Provider errors** set `push_provider.last_error` (a reason code, never a key, device token or alert text), raise one `system.push_provider_failed` notification that is never routed to the Push channel, and retry the dispatch after 15 minutes; a success clears the error.

### 24.4 SPI and delivery

- **SPI:** `PushTarget` (sealed; `WebPushTarget(subscriptionId, endpoint, p256dh, auth, payloadFormat, lang)`), `PushTransport` (`id`, `supports(target)`, `send(target, message, options)`), `PushResult` (`SENT | GONE | RETRY(after) | REJECTED(reason) | FAILED(reason)`), `PushTargetSource` (active targets matching a notification by `PushFilters` `events`, `ruleIds`, `minSeverity` and any-of `tags`; lookup by id; outcome hook).
- **Fan-out:** the built-in channel "Push" (type `webpush`, events alert/briefing/system, created at startup, enabled) renders nothing itself. When its `notification_delivery` row is claimed, `PushFanout` inserts one `push_dispatch` row per matching target with the transport-neutral message, and parks the parent (pending, not due). Channel quiet hours follow `config.quietHoursMode`: `defer`, `downgrade` (very-low urgency, silent) or `drop`.
- **Dispatch:** `PushDispatchWorker` polls every 2 s, claims with `FOR UPDATE SKIP LOCKED` and a lease, and sends each row on a virtual thread with the 018 retry ladder per target, so a retry never re-sends to targets that already succeeded. 2xx → `sent` and `last_success_at`; 404/410 → `gone` and the subscription is deleted; 429/503 → retry after `Retry-After`; 413 and other 4xx → `failed`; 5xx and I/O → ladder. A per-host Bucket4j limit (20/s) applies. The parent is `delivered` when every dispatch is terminal and at least one was sent (or there were no targets, or all were gone), `failed` when none was sent and at least one failed.

## 25. Production hardening (prompt 063)

1. Generate a Flyway baseline (`V1__baseline.sql`) from the Hibernate-generated schema plus the `SchemaBootstrap` statements (PostGIS and vector extensions, GiST, HNSW and GIN indexes).
2. Switch `ddl-auto` to `validate`; delete `SchemaBootstrap`. The reindex job (§20) keeps its own DDL for dimension changes.
3. Consider partitioning `record_version` by month if volume demands it (decide from measured volume).

## 26. Reuse policy and reference projects

| Repository | Licence | Use |
|---|---|---|
| God's Eye View | MIT | Port adapter fetch and parse logic to Java; keep the origin and notice in file headers and `THIRD_PARTY_NOTICES.md` |
| ShadowBroker | AGPL-3.0 | Reference only: endpoints, parameters, quirks. No code copied. |
| Velocity | AGPL-3.0 | Reference only |
| World Monitor | AGPL-3.0 | Reference only |
| SatDump | GPL-3.0 | Reference only. Foresignal talks to a separately run SatDump through files and HTTP; never copy code, headers, structs or projection code. |
| MeshCore | MIT | Protocol reference for mesh sources and channels (`docs/companion_protocol.md`, `docs/payloads.md`); talk to companions through `meshcore_py` or the bridge script. |
| MeshDTN | Owner's project (firmware and app are MIT forks of MeshCore and meshcore-open) | Foresignal's store-carry-forward mesh partner: the specifications define what Foresignal sends over mesh. |

### What each project contributed

- **God's Eye View:** the only project whose code is ported. It supplies the camera catalogues, transit, bikeshare, fire perimeters, radio and weather-model (GFS/HRRR wind, 051) sources. See `docs/Source-Catalog.md` (GEV).
- **ShadowBroker:** the second main input to the source inventory: endpoints, parameters and upstream quirks (Source-Catalog, SB). Also the cautionary tale: the Meshtastic project called out its traffic as 1–2 orders of magnitude above typical clients, which is why Foresignal meters every source and enforces budgets.
- **Velocity:** keyless and official feeds Foresignal lacked (Source-Catalog, VEL), and enter/exit geofence alerting (§8).
- **World Monitor:** structured feeds such as cyber, conflict, disasters, sanctions, internet outages and energy series (Source-Catalog, WM).
- **All four together:** the product shape. Each fuses collection, display and alerting into one app; Foresignal separates them into a headless broker that collects quietly, keeps history and alerts, with any map or app as a client (Executive Summary, Product Charter §2).
- **SatDump:** the station integration (049, 054, 107).
- **MeshCore:** mesh sources and outbound mesh channels (083, 084).
- **MeshDTN:** compact intelligence messages for delay-tolerant mesh delivery (084's CBOR profile, 113).

### Where to get them

Prompts reference these at fixed paths under `~/Documents/GitLab/`, as siblings of `foresignal`. Every build host (the Mac or the Linux bench server) needs the same layout. `gods-eye-view` is required, because prompts may port from it; the rest are read-only references.

| Local path | Clone from |
|---|---|
| `~/Documents/GitLab/gods-eye-view` | `http://192.168.1.134/mirrors/gods-eye-view.git` (upstream https://github.com/bilawalsidhu/gods-eye-view) |
| `~/Documents/GitLab/Shadowbroker` | https://github.com/BigBodyCobain/Shadowbroker.git |
| `~/Documents/GitLab/velocity` | https://github.com/AndrewCTF/velocity.git |
| `~/Documents/GitLab/worldmonitor` | https://github.com/koala73/worldmonitor.git |
| `~/Documents/GitLab/SatDump` | `http://192.168.1.134/mirrors/SatDump.git` (upstream https://github.com/SatDump/SatDump) |
| `~/Documents/GitLab/MeshCore` | `http://192.168.1.134/mirrors/MeshCore.git` |
| `~/Documents/GitLab/meshdtn` | https://gitlab.com/adam.allard/meshdtn.git (specifications) |
| `~/Documents/GitLab/meshdtn-firmware` | https://gitlab.com/adam.allard/meshdtn-firmware.git |
| `~/Documents/GitLab/meshdtn-app` | https://gitlab.com/adam.allard/meshdtn-app.git |

## 27. Intelligence layer (v1.3)

Authoritative design: [Intelligence Design](Intelligence-Design.md). Summary of how it sits on the platform:

- **Stack:** signals (records, rollups, series) → evidence (116: canonical events, evidence groups, observability, manifests) → detection (099, 100) → correlation (101, 104) → discovery (119 change points, event studies, co-occurrence; 120 precursors; 126 outlooks and indices; 127 impact projection) → memory (118 situations, 121 base rates and analogs) → reasoning (125 rolling hypotheses) → accountability (122 forecast ledger) → rhythm (128 analysis cycles and assessments).
- **Outputs are platform objects:** typed tables plus records of the DERIVED sources `foresignal-intel`, `foresignal-analytics` and `foresignal-outlook`, so triggers, SSE, tiles, search, briefings and MCP work unchanged. Forecast questions and forecasts are append-only ledger rows.
- **Determinism and versioning:** detectors, scorers, updaters and forecasters are pure, versioned components; outputs store `{name, version, params}` and `asOfSeq`. The LLM explains, challenges, proposes and forecasts (scored like any forecaster); it never creates evidence or changes deterministic scores.
- **Time:** every series point carries `knownAt`; live detection uses only known data; replays and retrospective evaluation read through `asOf` (§9 query paths, Core-Data-Model §12).
- **Durable cursors** (`situation.cursor`, `stats.region.cursor`, `precursor.cursor`, `impact.cursor`, `social.cursor`) join the retention consumer clamp; scheduled jobs (rollups, detectors, mining, resolution, cycles) use the 029/102 scheduler pattern and the 103 job framework.
- **Retention:** analytic retention classes (Conventions) keep event kinds 5 years and series 10 years; intelligence tables are outside `RetentionJob`.
- **Benchmarks** (123) measure analytic usefulness on planted-truth scenarios and a historical corpus and gate autonomous features.
- **Clients** use one object contract, relation graph, annotations and synchronised workspaces (129, [Client Contract](Client-Contract.md)); Foresignal stores client layout only as opaque JSON.
- **Collaboration and products:** principals, groups, access lists and TLP govern work products (130); chronicles keep their own partitioned history (131); reports and signed packages carry analysis between people and instances (132); news coverage and outlet ratings inform evidence quality (133).
- **Social signals** (135–137): platform adapters are ordinary sources (STREAMING or POLLING) with budgets in money as well as calls; posts are `social-post` records; volume series join the analysis-series registry; narratives are canonical events (116) whose evidence groups count origins; comparisons with physical evidence use the observability model, and cues reuse the source framework with per-cue caps.
- **External data** (024, 138): third-party providers speak the [External Data Protocol](External-Data-Protocol.md); an EDP source is a custom source whose descriptor comes from the provider's manifest (`ai.foresignal.inbound.edp`), layer streams are ordinary kinds, and a binding router (`external.binding.cursor`) routes atomic streams into series, evidence, indicators, forecasts, places, situations and chronicles. Fringe adapters live outside the repository.
- **Invariants** are indexed in [Invariants](Invariants.md) and verified at every checkpoint.
- **Wording:** probabilities are numbers; ICD 203 terms are derived and linted by one `EstimativeLanguage` utility.
- **Libraries:** Apache Commons Statistics, Commons Numbers, Commons RNG and Hipparchus; no GPL statistics libraries.
