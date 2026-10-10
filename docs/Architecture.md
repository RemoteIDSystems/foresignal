# Elaro — Architecture

Version 1.0 · 2026-10-10 · Companion to the [Product Charter](Product-Charter.md) and [OpenAPI spec](OpenAPI.json).

---

## 1. Overview

Elaro is a single Spring Boot process backed by one PostgreSQL + PostGIS database. Inside the process, five subsystems communicate through narrow interfaces:

```
                 ┌──────────────────────────── Elaro process ─────────────────────────────┐
                 │                                                                        │
  Upstreams ◄────┤  ① Collection                ② Ingest              ③ Distribution      │
  (HTTP, WS,     │  ┌───────────────┐   results ┌─────────────┐ batch  ┌───────────────┐   │
   MQTT…)        │  │ Scheduler     ├──────────►│ Ingest      ├───────►│ Change bus    │   │
                 │  │ Adapters      │  (queue)  │ writer      │ events │  ├─ SSE hub   ├───┼──► Frontends
                 │  │ Upstream      │           │ (single     │        │  ├─ Trigger   │   │    Agents
                 │  │ client +      │           │  thread)    │        │  │   engine ──┼───┼──► Webhooks
                 │  │ budgets       │           └──────┬──────┘        └───────────────┘   │
                 │  └───────┬───────┘                  │                                  │
                 │          │ usage/health             │ versions                         │
                 │          ▼                          ▼                                  │
                 │  ┌──────────────────────── ④ Store (Postgres + PostGIS) ──────────┐    │
                 │  │ settings · credentials · usage · health · record_version ·     │    │
                 │  │ record_head · trigger_rule · alert · webhook · app_setting     │    │
                 │  └───────────────────────────────▲────────────────────────────────┘    │
                 │                                  │                                     │
                 │  ⑤ API (REST /v1, GeoJSON, MVT, SSE, OpenAPI) + admin-token filter     │
                 └────────────────────────────────────────────────────────────────────────┘
```

**Key principle:** the *only* thing that causes an upstream request is an enabled source in `source_settings` (or an explicit operator fetch/probe). Nothing a client reads or streams ever reaches the collector.

## 2. Technology stack

| Concern | Choice | Notes |
|---|---|---|
| Runtime | Java 21 LTS (Temurin) | Virtual threads (`spring.threads.virtual.enabled=true`) |
| Framework | Spring Boot 4.1.x | Web MVC (not WebFlux); SSE via `SseEmitter` |
| JSON | Jackson 3 (Boot 4 default, `tools.jackson.*` packages) | Payloads stored as `jsonb` |
| Persistence | Spring Data JPA, Hibernate 7, `hibernate-spatial` + JTS | `ddl-auto=update` in development; **no Flyway until production** |
| Bulk writes | `JdbcTemplate` batch on the ingest hot path | Entities still own the schema definition |
| Database | PostgreSQL 17 + PostGIS 3.5 | Image `postgis/postgis:17-3.5` |
| Rate limiting | Bucket4j (`bucket4j_jdk17-core`) | One bucket set per source |
| HTTP client | `java.net.http.HttpClient` | Wrapped by `UpstreamClient` |
| WebSocket client | `java.net.http.WebSocket` | For streaming adapters |
| MQTT (later waves) | Eclipse Paho v5 | For streaming adapters |
| OpenAPI | Authored `docs/OpenAPI.json` served at `/v1/openapi.json`; springdoc-openapi (Boot 4-compatible line) for Swagger UI at `/docs` and drift testing | See §10 |
| Metrics | Micrometer + Prometheus registry | `/actuator/prometheus` |
| Build | Gradle 9.x, Kotlin DSL, wrapper committed | Single module |
| Tests | JUnit 5, AssertJ, Mockito, Testcontainers 2.x, WireMock 3.x, JaCoCo | ≥ 90% line coverage gate |
| Packaging | Multi-stage Dockerfile → `eclipse-temurin:21-jre`; Docker Compose | |

Exact dependency versions are pinned in `gradle/libs.versions.toml` and verified against Maven Central at implementation time.

## 3. Package layout

Single Gradle module, base package `ai.elaro`.

```
ai.elaro
├── ElaroApplication
├── config          Jackson, CORS, virtual threads, OpenAPI metadata, clock, schema bootstrap
├── security        AdminTokenService, AdminTokenFilter, CredentialCipher, SecretKeyStore
├── source          SourceAdapter SPI, descriptors, registry, settings, credentials
│   └── adapters    One sub-package per upstream: adapters.usgs, adapters.adsblol, …
├── collect         CollectorScheduler, UpstreamClient, BudgetGovernor, UsageMeter,
│                   HealthTracker, Backoff, StreamingSupervisor
├── ingest          IngestQueue, IngestWriter, ContentHasher, NormalizedRecord
├── store           Entities (RecordVersion, RecordHead, …), repositories, spatial queries,
│                   TileQuery
├── stream          ChangeBus, ChangeBatch, SseHub, StreamFilter
├── trigger         TriggerRule, TriggerEngine, RuleSqlBuilder, Alert, AlertService,
│                   WebhookDispatcher
├── retention       RetentionSettings, RetentionJob
├── settings        AppSettingService (typed key/value), export/import
├── system          SystemInfoService
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
2. Send with per-request timeout (default 20 s) and gzip accepted. The `User-Agent` is `Elaro/<version> (+https://elaro.ai; install=<id>)`, where `<id>` is a random 8-character install id generated on first start (stored in `app_setting`). Upstreams can then rate-limit or contact one misbehaving install without blocking every Elaro user (a lesson from ShadowBroker). Never spoof browser User-Agents.
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

**`webhook`**: `id` uuid PK · `name` · `url` · `secret` (encrypted) · `rule_ids` jsonb null (null = all rules) · `enabled` · `created_at`

**`webhook_delivery`**: `id` bigint PK · `webhook_id` · `alert_id` · `status` (`pending`/`delivered`/`failed`) · `attempts` · `last_error` · `next_attempt_at` · `delivered_at`

**`app_setting`**: `key` PK · `value` jsonb · `updated_at`. Keys include `retention`, `cors`, `trigger.cursor`, `stream.limits`.

**`system_secret`**: `key` PK · `value` bytea. Holds `admin_token_hash` (SHA-256).

### 7.2 Secrets

- **Admin token:** 32 random bytes, base64url, prefixed `elr_`. Generated on first start when `admin_token_hash` is absent; printed once in a clearly delimited log block. Only the SHA-256 hash is stored. Rotation: `POST /v1/system/admin-token/rotate` returns a new token once. Recovery: starting with env `ELARO_RESET_ADMIN_TOKEN=true` regenerates and prints a new token.
- **Credential encryption:** AES-256-GCM. The key is generated on first start and stored in a file on the data volume (`${ELARO_DATA_DIR:-./data}/secret.key`, mode 0600), never in the database, so a database dump alone does not reveal upstream keys.

### 7.3 SchemaBootstrap

Hibernate cannot create the PostGIS extension or GiST indexes. `SchemaBootstrap` runs in two phases:

1. **Before** the JPA `EntityManagerFactory` initializes (a `DataSource`-level initializer that the EMF depends on): `CREATE EXTENSION IF NOT EXISTS postgis`.
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
- Matches are inserted into `alert`; each alert is published to the alert stream and queued for webhooks.
- **Durable cursor:** after processing a batch, the engine stores `trigger.cursor = toSeq` in `app_setting`. On startup it catches up from the cursor to the current max `seq` in chunks before subscribing live.
- Message templates support `{{name}}`, `{{source}}`, `{{externalId}}`, `{{kind}}`, `{{observedAt}}` and `{{payload.<path>}}`. Default: `"{{name}}: {{source}}/{{externalId}}"`.
- `POST /v1/triggers/{id}/test` runs the same SQL over a recent window (default the last 24 h, `?since=`) and returns matches without inserting alerts.

### 8.4 WebhookDispatcher

- On a new alert, creates `webhook_delivery` rows for matching enabled webhooks.
- A worker polls pending deliveries every 2 s and POSTs `application/json`: `{alert, rule, record}` with headers `X-Elaro-Event: alert`, `X-Elaro-Delivery: <id>`, `X-Elaro-Signature: sha256=<hex HMAC of body with webhook secret>`.
- Retries with backoff (10 s, 1 m, 5 m, 30 m, 2 h), then `failed`. Webhook traffic is outbound to the operator's endpoints and is not counted against source budgets.

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
- **Auth:** `AdminTokenFilter` requires `Authorization: Bearer elr_…` for every non-GET request under `/v1` and for GET on `/v1/settings/**`, `/v1/webhooks/**`, `/v1/system/admin-token/**`. All other GETs are open. Comparison is constant-time against the stored hash.
- **CORS:** allowed origins come from `app_setting.cors` (default `*` for GET/HEAD/OPTIONS; writes require an explicit origin list).
- **The spec is authored, not generated.** `docs/OpenAPI.json` is copied onto the classpath at build time and served verbatim at `/v1/openapi.json`; Swagger UI at `/docs` points to it. springdoc generates a spec from the code at the internal path `/v1/openapi-generated.json`, which exists only so a contract test can assert the implementation matches the authored spec (same paths, methods and operationIds). See §12.

## 11. Retention

`RetentionJob` runs every hour (and on demand via `POST /v1/settings/retention/run`):

1. For each source: window = source override ?? global default (default `7d`).
2. Delete `record_head` rows whose `observed_at < now() - window` (current but stale), then delete `record_version` rows with `ingested_at < now() - window` and `seq` not referenced by any head. Work in chunks of 10,000 rows per transaction to avoid long locks.
3. Delete `alert` older than `alerts` retention (default `90d`), `source_usage` older than `usage` retention (default `30d`), and delivered or failed `webhook_delivery` older than 7 days.

Retention settings shape: `{"default":"7d","alerts":"90d","usage":"30d","sources":{"usgs-earthquakes":"3650d"}}`.

## 12. Testing strategy

Per the methodology, tests are **written with every prompt but executed only once the project is code-complete** (final verification prompts).

| Layer | Tooling | Scope |
|---|---|---|
| Unit | JUnit 5, AssertJ, Mockito | Hashing, budget math, health derivation, rule SQL builder, duration parsing, cursor encoding, template rendering, parsers |
| Adapter contract | Fixture files + parser tests | Each adapter's parser against a recorded upstream response |
| Adapter integration | WireMock | Each adapter through `UpstreamClient` (budget, headers, 429 backoff) |
| Persistence integration | Testcontainers `postgis/postgis:17-3.5` (as a compatible substitute for `postgres`) | Writer versioning, head upserts, spatial queries, MVT, retention, trigger SQL |
| API integration | `@SpringBootTest` + MockMvc + Testcontainers | Every endpoint, auth rules, ProblemDetail errors, SSE replay and resume |
| OpenAPI contract | Test comparing springdoc output to `docs/OpenAPI.json` paths and operations | Prevents drift |
| End-to-end | Testcontainers + WireMock USGS | Enable source → ingest → revision → trigger → SSE alert → webhook received |

JaCoCo enforces ≥ 90% line coverage (`jacocoTestCoverageVerification`), excluding generated code and `ElaroApplication`.

## 13. Configuration

File-based configuration is limited to bootstrap concerns, all overridable by environment variables:

| Env | Default | Purpose |
|---|---|---|
| `ELARO_DB_URL` | `jdbc:postgresql://localhost:5433/elaro` | Database |
| `ELARO_DB_USER` / `ELARO_DB_PASSWORD` | `elaro` / `elaro` | Database |
| `ELARO_PORT` | `8080` | HTTP port |
| `ELARO_DATA_DIR` | `./data` | Secret key file location |
| `ELARO_RESET_ADMIN_TOKEN` | `false` | Regenerate admin token on start |

Everything else — sources, budgets, credentials, retention, triggers, webhooks, CORS — is stored in the database and managed through the API.

## 14. Deployment

```
docker-compose.yml
├── db     postgis/postgis:17-3.5  volume pgdata  port 5433:5432  healthcheck pg_isready
└── elaro  build: .                 volume elaro-data:/data  port 8080  depends_on db (healthy)
           restart: unless-stopped
```

- `docker compose up -d` serves both the always-on bench server and occasional use.
- The JAR also runs standalone (`java -jar elaro.jar`) against any Postgres 15+ with PostGIS available.
- For internet exposure, put it behind a reverse proxy with TLS. Documentation will include a Caddy example.

## 15. Production hardening (final phase)

1. Generate a Flyway baseline (`V1__baseline.sql`) from the Hibernate-generated schema plus `SchemaBootstrap` statements.
2. Switch `ddl-auto` to `validate`; delete `SchemaBootstrap`.
3. Consider partitioning `record_version` by month if volume demands it (decide from measured volume).

## 16. Reuse policy

| Repository | Licence | Use |
|---|---|---|
| God's Eye View | MIT | Port adapter fetch and parse logic to Java; keep the origin and notice in file headers and `THIRD_PARTY_NOTICES.md` |
| ShadowBroker | AGPL-3.0 | Reference only: endpoints, parameters, quirks. No code copied. |
| Velocity | AGPL-3.0 | Reference only |
| World Monitor | AGPL-3.0 | Reference only |
