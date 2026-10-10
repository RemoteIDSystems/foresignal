# 005 — Upstream Client And Budgets

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/005-CR-Upstream-Client-And-Budgets.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/004-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §5.2 (UpstreamClient and BudgetGovernor), §5.3 (UsageMeter)
- `docs/Product-Charter.md` FR-8, FR-9, FR-12, G2
- `docs/Source-Catalog.md` (skim the notes column for upstream etiquette: Retry-After, conditional GET, no concurrency)

## Objective

Make every upstream request budgeted, metered, identifiable and polite. After this prompt it is impossible for adapter code to reach the network except through a source-bound `SourceHttp` that enforces the source's budget.

## Tasks

### 1. BudgetGovernor (`ai.elaro.collect`)

- **Buckets:** one Bucket4j `Bucket` per source with two limits:
  - **per minute:** capacity `perMinute`, greedy refill of `perMinute` per 1 minute
  - **per day:** capacity `perDay`, refilled *intervally aligned* to UTC midnight (`refillIntervallyAligned(perDay, Duration.ofDays(1), nextUtcMidnight)` or the Bucket4j 8.x equivalent)
- Use the injected `Clock` through a Bucket4j `TimeMeter` adapter so tests can control time.
- **Seeding:** when a bucket is created (startup, or the first use of a source), deduct today's already-used requests (from `UsageMeter.requestsToday(sourceId)`) from the daily limit so restarts can't reset the allowance.
- **Rebuilding:** rebuild a source's bucket on `SourceSettingsChangedEvent` when its budget changed, preserving today's usage.
- **API:**
  - `boolean tryConsume(String sourceId)`
  - `BudgetStatus status(String sourceId)`: perMinute, perDay, usedToday, remainingToday, minuteRemaining
- Thread-safe.

### 2. UsageMeter

- In-memory `ConcurrentHashMap<UsageKey(sourceId, minuteStart), Counters(LongAdder requests, bytes, errors, refused)>`.
- `record(sourceId, bytes, success)` and `recordRefused(sourceId)`.
- `flush()`: `@Scheduled(fixedDelay = 60s)` and on `@PreDestroy`. Upsert into `source_usage` with `INSERT … ON CONFLICT (source_id, minute) DO UPDATE SET requests = source_usage.requests + EXCLUDED.requests, …`, using a JDBC batch. Remove flushed counters atomically (or swap maps) so no increments are lost.
- **Queries:** merge persisted rows with unflushed in-memory counters.
  - `requestsToday`, `lastHour(sourceId)` (requests, bytes, errors, refused)
  - `series(sourceId, from, to, resolution)`: minute/hour/day buckets via `date_trunc`, merged with live counters; used in prompt 010

### 3. Backoff (`BackoffRegistry`)

- Per-source `backoffUntil`, persisted to `source_health.backoff_until`. Create the row if missing; prompt 006 builds on this table.
- `onThrottled(sourceId, Optional<Duration> retryAfter)`:
  - if `Retry-After` is present (seconds or HTTP-date), clamp it to [5 s, 1 h]
  - otherwise use exponential backoff 30 s × 2ⁿ (n = consecutive throttles), capped at 1 h
- `onSuccess(sourceId)` resets n.
- `isInBackoff(sourceId)` and `backoffUntil(sourceId)`.

### 4. SourceHttp implementation (`UpstreamClient`)

`UpstreamClient.forSource(String sourceId, SourceDescriptor d, RequestRecorder recorder /* nullable */)` returns a `SourceHttp` bound to that source.

- **Shared client:** one shared `java.net.http.HttpClient` with HTTP/2 preferred, a 10 s connect timeout, `Redirect.NORMAL` and a virtual-thread executor.
- **Every request does this, in order:**
  1. `BudgetGovernor.tryConsume`; on refusal, `UsageMeter.recordRefused` and throw `BudgetExceededException`. **No request is sent.**
  2. If `BackoffRegistry.isInBackoff`, throw `InBackoffException` (no budget consumed; check this **before** consuming).
  3. Set headers: `User-Agent` from `InstallIdService.userAgent()`, `Accept-Encoding: gzip`, plus caller headers. Callers may not override the User-Agent unless the descriptor sets an explicit `userAgentOverride`. Add that field to the descriptor, default null. It exists for upstreams that require a specific UA format, never for browser spoofing.
  4. Per-request timeout: 20 s default.
  5. Read the body as an `InputStream`, **decompressing gzip yourself** when `Content-Encoding: gzip` (HttpClient does not), up to `maxResponseBytes`. Beyond the cap, set `truncated=true` and stop reading.
  6. `UsageMeter.record(bytes, success = status < 400)`.
  7. On 429 or 503, call `BackoffRegistry.onThrottled` (parsing `Retry-After`, and also `X-Rate-Limit-Retry-After-Seconds`, which OpenSky uses), then throw `UpstreamHttpException(status, snippet)`. Other ≥ 400 statuses throw `UpstreamHttpException` with no backoff. Success calls `BackoffRegistry.onSuccess`.
  8. If the recorder is present, record method, **redacted URL**, status, duration, content type, bytes, truncated flag and body (respecting the recorder's own cap).
- **Redaction:** replace any query-parameter value, path segment or header value that equals a stored credential value for this source with `***`. Also redact the `Authorization` header entirely.
- **`getConditional(uri, stateKey)`:**
  - reads `etag:<stateKey>` and `lastmod:<stateKey>` from the source's `AdapterStateStore`, sends `If-None-Match` / `If-Modified-Since`, and stores the new values on 200
  - a `304` returns `UpstreamResponse.notModified=true` with an empty body; it still consumes budget and is metered as a success
  - `AdapterStateStore` implementation: back it with `source_health.adapter_state` (jsonb). Writes are persisted immediately in a small transaction.
- **`openWebSocket(uri, listener, headers)`:**
  - consumes **one** budget unit per connection attempt, sets the User-Agent, and returns the `WebSocket`
  - metering counts the connection as one request; streaming adapters report received bytes via `UsageMeter.record` through a helper on `StreamContext`
- **Exceptions** (in `ai.elaro.collect`): `BudgetExceededException`, `InBackoffException`, `UpstreamHttpException(int status, String bodySnippet)`, `UpstreamIOException` (wrapping IO and timeout).

### 5. RequestRecorder

A capture buffer used by the probe feature in prompt 008: a list of `RecordedExchange` with a per-body byte cap.

## Tests to write (do not run)

- `BudgetGovernorTest` (with `MutableClock`):
  - per-minute refill, per-day exhaustion and midnight UTC reset
  - seeding from prior usage
  - rebuild on budget change preserves usage
  - concurrency (parallel `tryConsume` never exceeds capacity)
- `UsageMeterIT`: record, flush and upsert accumulation; live plus persisted merge; series at each resolution; no lost increments under concurrent record-during-flush.
- `BackoffRegistryTest`: Retry-After seconds and HTTP-date, clamping, exponential growth and reset, persistence (IT).
- `UpstreamClientIT` (WireMock):
  - refused requests never hit WireMock (`verify(0, …)`)
  - backoff blocks without consuming budget
  - User-Agent format
  - gzip decoding
  - truncation at the cap
  - 429 with `Retry-After` triggers backoff
  - 503 handling, 404 throws without backoff
  - conditional GET sends the right headers and handles 304
  - redaction of credentials in recorded URLs and headers
  - metering counts
- `WebSocketBudgetIT`: a connection attempt consumes budget (use WireMock's WebSocket support if available, otherwise a tiny embedded test server).

## Acceptance criteria

- No code path outside `UpstreamClient` constructs an `HttpClient` for upstream use. A test can assert this by scanning `src/main/java` for `HttpClient.newHttpClient`/`newBuilder` usages outside the `collect` package.
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
2. **Commit** all changes with the message `005: Upstream Client And Budgets`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/005-CR-Upstream-Client-And-Budgets.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `005: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
