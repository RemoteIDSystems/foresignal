# 005-CR-Upstream-Client-And-Budgets

| | |
|---|---|
| Prompt | 005-Upstream-Client-And-Budgets.md |
| Status | Complete |
| Implementation commit | 6849d58114731206a9593e1123d7509f4b43bdfe |
| Date | 2026-10-11 |

## Summary

Every upstream request is now budgeted, metered, identifiable and polite, and
there is no other way to make one. `UpstreamClient` holds the only two
`HttpClient` instances in the application and hands adapters a `SourceHttp`
already bound to their source; the SPI gives them no way to build their own, and
`UpstreamClientIsTheOnlyHttpClientTest` scans `src/main/java` to keep it that
way.

The single request path applies its policies in a fixed order, and the order is
part of the guarantee: **backoff is checked before the budget is consumed**, so
an upstream that throttles us cannot also drain our daily allowance. A refusal
sends nothing at all — `BudgetGovernor.tryConsume` returns false, the meter
records a *refusal* rather than a request, and `BudgetExceededException` is
thrown before a socket is opened. The integration tests assert this against
WireMock's own view (`verify(0, …)`) rather than against Elaro's counters,
because "we chose not to ask" is only credible if the stub never saw the
request.

`BudgetGovernor` keeps both limits in one Bucket4j bucket so they are consumed
atomically; two buckets would let a request that passed the minute check but
failed the day check silently spend a minute token. The daily bandwidth refills
intervally aligned to the next UTC midnight, and is **seeded from
`UsageMeter.requestsToday`** when the bucket is created, so a restart cannot be
used to reset the allowance. A budget edit rebuilds the bucket from scratch with
`initialTokens = perDay − usedToday` rather than through
`replaceConfiguration`, whose token-inheritance strategies are proportional or
additive where this needs to be exact. Bucket4j runs off the injected `Clock`
through `ClockTimeMeter`, which is what lets a test assert the midnight reset
instead of waiting a day for it.

`UsageMeter` counts per `(source, minute)` in memory and flushes once a minute
into `source_usage` with an accumulating `on conflict` upsert. The ingest hot
path can record tens of thousands of events a minute, so a write per event would
cost more than the work being measured. A flush **removes each key** rather than
clearing the map, so an increment arriving mid-flush lands either in the
snapshot or in a fresh entry — never in a bucket about to be discarded; the
concurrency test drives 4,000 records against a flush loop and asserts the total
exactly. Reads merge persisted rows with unflushed counters, so usage never
appears to drop to zero at the top of a minute — which matters because that
figure is what seeds the daily budget.

`BackoffRegistry` honours `Retry-After` (delta-seconds or HTTP-date, clamped to
[5 s, 1 h]), falls back to 30 s × 2ⁿ capped at an hour, and persists the
deadline to `source_health.backoff_until` in its own `REQUIRES_NEW` transaction
— separate on purpose, because a backoff must survive the rollback of the fetch
that triggered it. It also parses OpenSky's
`X-Rate-Limit-Retry-After-Seconds`.

On the v1.2 additions: `FetchTrace` carries a redacted URL, status, ETag, byte
count and a **streaming SHA-256** and never a body; `getRange` accepts only 206
(or 200 at offset 0) and rejects a short body, because an upstream that ignored
`Range` would otherwise have a GRIB index parsed from the wrong offset;
`download` streams to a capped temp file under `${ELARO_DATA_DIR}/rasters/tmp`;
`listS3` does anonymous ListObjectsV2 with StAX hardened against XXE (a remote
XML document with entity resolution left on is a file-read and SSRF primitive);
and `mbPerDay` is charged in advance from a known size and corrected afterwards.
`LAN_URL` requests connect to the address pinned during validation, keep the
configured `Host`, and follow no redirects — re-resolving the name is the DNS
rebinding hole prompt 004's validation exists to close.

Redaction is built from the source's **stored credential values**, not from
parameter names: an upstream is free to call its key anything, and a name-based
guess would miss it. `Authorization` is redacted whole regardless.

## Files created / modified

**Created (`ai.elaro.collect`)** — 21 files
- `UpstreamClient` (the shared clients, `forSource`, redactor and pinned-host
  wiring), `BoundSourceHttp` (the single request path)
- `BudgetGovernor`, `BudgetStatus`, `ClockTimeMeter`
- `UsageMeter`, `UsageTotalsWindow`, `UsageSeriesPoint`, `UsageResolution`,
  `IngestCounts`
- `BackoffRegistry`
- `PersistentAdapterStateStore` (backed by `source_health.adapter_state`)
- `FetchTrace`, `RequestRecorder`, `RecordedExchange`, `UrlRedactor`
- `S3ListingParser`
- `BudgetExceededException`, `InBackoffException`, `UpstreamHttpException`,
  `UpstreamIOException`

**Created (`ai.elaro.source.spi`)**
- `DownloadResult`, `S3Listing`, `S3Object`

**Modified**
- `source.spi.SourceHttp` — added `getRange`, `download`, `listS3` (declared in
  004 with the other three)
- `source.spi.SourceDescriptor` — added `userAgentOverride` (default null)
- `source.spi.Budget` — added `mbPerDay`, `hasByteBudget()`, `bytesPerDay()`
- `source.spi.StreamContext` — added `recordReceivedBytes(long)`, the metering
  helper for bytes arriving on an open stream
- `source.SourceService` — `effectiveBudget` now carries the descriptor's
  `mbPerDay`
- `store.SourceUsageEntity` — added `records_seen`, `versions`, `live_updates`,
  `touches`, `late`, `skipped`, `rejected`, `version_bytes`
- `ElaroApplication` — calls
  `UpstreamClient.allowHostHeaderForPinnedRequests()` before Spring starts
- `collect/package-info.java` — the no-other-HttpClient rule
- Test fixtures and `SourceRegistryTest`/`SourceServiceIT` — updated for the
  3-arg `Budget`

No dependency changes: `com.bucket4j:bucket4j_jdk17-core:8.21.0` and the
WireMock test dependency were already in place from 004.

## Deviations from the prompt

1. **Two `HttpClient` instances, not one.** The prompt says "one shared
   `HttpClient` … `Redirect.NORMAL`", but `getRange`, `listS3` and pinned LAN
   requests must follow *no* redirects, and `followRedirects` is fixed at build
   time on an `HttpClient`. There are therefore two, identical but for the
   redirect policy. The acceptance criterion is unaffected — both live in
   `UpstreamClient`, and nothing outside the package builds one.

2. **`Bandwidth.builder()`, not the 7.x helper names.** Bucket4j 8.21 replaced
   `Bandwidth.classic(...)`/`Refill.intervallyAligned(...)` with a builder;
   `refillGreedy` and `refillIntervallyAligned` are builder methods. The
   semantics the prompt asked for are unchanged.

3. **`jdk.httpclient.allowRestrictedHeaders` is set in a static initializer
   *and* from `main`.** The JDK reads that property once, when its HTTP
   utilities first initialize — which the first `HttpClient` or `HttpRequest`
   anywhere in the JVM triggers, not necessarily ours. Setting it from `main`
   before Spring starts is the only way to reliably win that race. When
   something wins it anyway, a pinned request is sent without the `Host` header
   rather than failing: connecting to the pinned address is the protection, and
   the `Host` is a courtesy to multi-name receivers.

4. **`mbPerDay` comes from the descriptor, not from settings.** There is no
   column for it, and it reflects how large the upstream's objects are rather
   than an operator preference. If it should become editable, it needs a column
   and an OpenAPI property.

5. **`StreamContext.recordReceivedBytes` is abstract, not a default no-op.** The
   prompt asked for "a helper on `StreamContext`"; nothing implements that
   interface yet (prompt 010 does), so an abstract method breaks nothing and a
   default would let a streaming source go silently unmetered.

6. **`correctByteCharge` is additional API.** The prompt's "corrected afterwards
   from actual bytes" needs a method to do the correcting; `tryConsume(sourceId,
   expectedBytes)` and `correctByteCharge(sourceId, expected, actual)` are the
   pair.

## Issues found / open questions

1. **A refused request is not retried, and nothing yet records that it was
   wanted.** `UsageMeter` counts refusals and `UsageTotalsWindow.refusalRate()`
   exposes the share, but no component acts on it. A source configured to want
   more than it is allowed is a *misconfiguration*, not an upstream fault, and
   prompt 006's health model should say so rather than reporting the source as
   degraded.

2. **A byte budget is enforced only where a size is known in advance.** `listS3`
   sizes and `.idx` offsets give one; an ordinary `get` does not, so its bytes
   are metered after the fact and can overshoot `mbPerDay` by one response (at
   most `maxResponseBytes`). Charging a pessimistic `maxResponseBytes` up front
   would refuse almost everything, so the overshoot is deliberate.

3. **`UsageMeter.flush` logs and swallows a database failure.** Failing the
   scheduler thread would stop all later flushes, but the lost minute matters:
   it is what seeds the daily budget after a restart, so a flush failure
   silently widens the allowance. Worth a health signal in 006.

4. **A 304 consumes budget, as the prompt specifies.** It cost a round trip, so
   this is right, but it means a conditional-GET source polling every 30 s
   spends its daily allowance on unchanged responses. The scheduler in 010
   should treat a long run of 304s as a reason to widen the interval.

5. **WebSocket received bytes depend on the adapter calling
   `recordReceivedBytes`.** `openWebSocket` meters the connection as one
   request; everything after it is visible only to the adapter. Nothing enforces
   the call, so a streaming adapter that forgets it will under-report. Prompt
   010's stream supervisor is the place to wrap the listener and make it
   automatic.

## Tests written (not run)

Per guardrail 1, none were executed.

- `BudgetGovernorTest` (`MutableClock`, no database): per-minute capacity,
  greedy partial refill, daily exhaustion across refilling minutes, UTC-midnight
  reset (and that it does *not* reset just before midnight), seeding from prior
  usage, usage beyond the allowance clamping at zero, rebuild-on-change
  preserving usage, unchanged-budget no-op, `mbPerDay` advance charge and
  correction, status reporting, and 16 threads racing `tryConsume` never
  exceeding capacity
- `UsageMeterIT`: per-minute rows, accumulating upsert across two flushes of one
  minute, ingest-counter accumulation, UTC-day and last-hour windows, half-open
  `[from, to)` bounds, live-plus-persisted merge, flush being invisible to
  readers, restart seeing only flushed usage, series at minute/hour/day
  resolution with live counters merged into the right bucket, refusal rate, and
  two concurrency tests (4,000 records against a concurrent flush loop; 8
  threads × 500 mixed records summing exactly)
- `BackoffRegistryTest`: exponential growth and cap, `Retry-After` in
  delta-seconds and HTTP-date form, clamping at both ends, reset on success
- `BackoffRegistryIT`: row creation, in-place update, read-through from a cold
  cache, a restart still holding the brake, an expired persisted deadline being
  ignored without erasing the row, success clearing the stored deadline, and
  other `source_health` columns surviving the write
- `UpstreamClientIT` (WireMock, 50 tests): refused requests never reaching the
  stub, backoff blocking without consuming budget, 429 with `Retry-After`,
  OpenSky's header, clamping, 503 exponential backoff, 404 without backoff,
  User-Agent format, adapter override rejected, descriptor override honoured,
  gzip decoding, truncation at and exactly on the cap, POST bodies, conditional
  GET headers and 304 handling (including a 304 leaving the stored validators
  alone), credential and `Authorization` redaction in captures and traces,
  metering counts, `getRange` (206, a 200 at a non-zero start rejected, a short
  body rejected, an inverted range caught before any request, no redirects, the
  per-read cap), `download` (file, size, hash, cap, budget, errors, 503),
  `listS3` (parsing, `startAfter` paging, an empty listing, unparseable XML),
  `LAN_URL` pinning (pinned address with the original `Host`, no redirects,
  ordinary hosts unaffected), and a `DERIVED` source having no client at all
- `WebSocketBudgetIT`: a connection attempt consuming one unit and being metered
  as one request, a reconnect storm bounded by the per-minute budget with the
  server never seeing the refused attempt, refill in the next minute, backoff
  blocking without spending budget, User-Agent sent and not overridable by the
  caller, descriptor override honoured, and a failed connect metered as an error
  while still costing budget. WireMock has no WebSocket support, so the fixture
  is a ~120-line server that completes the RFC 6455 opening handshake and
  nothing else, which also lets the test read exactly which headers were sent
- `FetchTraceTest`, `UrlRedactorTest`, `RequestRecorderTest`,
  `S3ListingParserTest` (including XXE), `PinnedTargetTest`
- `UpstreamClientIsTheOnlyHttpClientTest`: scans `src/main/java` for
  `HttpClient.newHttpClient` / `HttpClient.newBuilder` outside
  `ai.elaro.collect` — the acceptance criterion as an assertion

## Compile result

```
$ ./gradlew compileJava compileTestJava --no-daemon
BUILD SUCCESSFUL in 3s
```

No errors, no warnings. Acceptance criteria confirmed: the only `HttpClient`
construction in `src/main/java` is `UpstreamClient.build`, asserted by test
rather than by inspection; no Flyway, Liquibase or `.sql` files were added.

## Notes for the next prompt

1. **`source_health` already exists and has rows.** `BackoffRegistry.persist`
   upserts `(source_id, consecutive_failures, recent_record_counts,
   adapter_state, backoff_until)`, creating the row if missing, and
   `PersistentAdapterStateStore` writes `adapter_state`. Prompt 006 must **not**
   assume it owns row creation, and must leave `backoff_until` and
   `adapter_state` alone when it writes its own columns.

2. **Health has three signals ready to read.** `UsageMeter.lastHour(sourceId)`
   gives requests, bytes, errors and refused plus `refusalRate()`;
   `BackoffRegistry.isInBackoff`/`backoffUntil`/`consecutiveThrottles` give the
   throttle state; `BudgetGovernor.status(sourceId)` gives what is left. Issue 1
   above matters here: a high refusal rate is a misconfiguration, not a degraded
   upstream.

3. **Distinguish the three exception types.** `UpstreamHttpException` means the
   upstream is reachable and unhappy with us (consecutive-failure material),
   `UpstreamIOException` means we could not reach it at all, and
   `InBackoffException` / `BudgetExceededException` mean **we chose not to ask**
   and must not count as failures. Conflating the last two with the first would
   make a well-behaved source look broken.

4. **`FetchTrace` is produced but not yet consumed.** `SourceHttp` records one
   per request; prompt 008's `FetchExecutor` turns them into `fetch_log` rows.
   The trace holds a streaming SHA-256 and never a body, so `fetch_log` cannot
   be a body archive — `RequestRecorder` is the probe path for that, with its
   own per-body cap.

5. **Wrap the WebSocket listener in 010.** See issue 5: received bytes are only
   metered if the adapter calls `StreamContext.recordReceivedBytes`. The stream
   supervisor should wrap the listener it is given so the adapter cannot forget.

6. **`UsageMeter.series(…)` is ready for the usage endpoint.** Minute, hour and
   day resolutions, live counters merged, empty buckets omitted — a caller
   wanting a dense series fills the gaps itself rather than receiving thousands
   of zero rows for a quiet source.

7. **`UsageMeter.recordIngest(sourceId, IngestCounts)`** is the hook for the
   ingest counters on the same per-minute row. Prompt 008 should call it once
   per batch, not once per record.

8. **The `mbPerDay` overshoot in issue 2** is worth remembering when the raster
   prompts set byte budgets: an ordinary `get` can exceed the budget by one
   response, so size the budget with `maxResponseBytes` of headroom.
