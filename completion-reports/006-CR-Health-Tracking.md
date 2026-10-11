# 006-CR-Health-Tracking

| | |
|---|---|
| Prompt | 006-Health-Tracking.md |
| Status | Complete |
| Implementation commit | 61701e414c19032e14d188e2455891e76dd93db8 |
| Date | 2026-10-11 |

## Summary

Every source can now diagnose itself. `HealthTracker` records what happens on each
attempt into `source_health`. `HealthEvaluator`, a pure function, derives the status
from that row, last-hour usage, the live backoff deadline and the stream state.
`HealthService` builds the full OpenAPI `SourceHealth` view and feeds the status slice
into `SourceService`'s summaries and details.

This prompt was started by an earlier session that was interrupted before committing.
The owner authorized continuing, so this run reviewed that uncommitted work file by
file, kept what was correct, and finished what was missing: `HealthServiceIT`, extra
`UsageMeterIT` coverage, and a fix that lets contributed rules report a frame time for
a failing source.

**`HealthTracker` writes only its own columns.** `BackoffRegistry` and
`PersistentAdapterStateStore` already create `source_health` rows and own
`backoff_until` and `adapter_state`. Every tracker statement is therefore a JDBC upsert
or update that names only the health columns. A whole-entity JPA save would write back
a stale backoff deadline or ETag. Each write runs in its own `REQUIRES_NEW`
transaction, because a failure is most worth recording when the surrounding fetch is
being rolled back. Recording never throws.

What the tracker does with each event:
- **Success.** The record-count ring of 5 is read with `SELECT … FOR UPDATE`, so two
  concurrent successes cannot lose an entry. A 304 stays out of the ring and never sets
  `last_data_at`. A zero-record success enters the ring but does not set
  `last_data_at`.
- **Failure.** The stored error is `SimpleName: message`, masked with the source's
  stored credential values through 005's `UrlRedactor`, and cut to 2,000 characters.
  If the credentials cannot be loaded, the message is withheld rather than stored
  unmasked. `last_http_status` comes from an `UpstreamHttpException` anywhere in the
  cause chain.
- **Budget refusals and backoff skips.** `BudgetExceededException` and
  `InBackoffException` are not failures, and `recordRefused` writes nothing.
  `UsageMeter` already counted the refusal on the request path.

**The evaluator applies the table in order.** The rules are: disabled, idle, failing
(in backoff, or 3 or more consecutive failures), last run failed, coverage dropped,
possible format change, more than 20% refusals, stream down for more than 2 minutes,
healthy. All thresholds are named constants.

Coverage dropped means receivers or expected fell to half or less of the median of the
earlier ring entries, judged only once at least 3 earlier values exist. The
format-change rule reproduces the acceptance criterion: a zero after 3 or more
non-empty counts among the earlier 4, unless `zeroResultsNormal`. Coverage is checked
first, so the format-change rule only fires while coverage is steady.

Contributed `HealthRule` beans then run for any running source, and the more severe
status wins. `FrameAgeRule` is the helper for product sources: degraded when the newest
frame is older than `maxGap`, or than 3 × cadence when there is no `maxGap`.

**The view covers every `SourceHealth` field this prompt owns.** That includes
`errorRateLastHour` (0 with no requests), budget figures from `BudgetGovernor.status`,
the last-hour counts, `coverage`, `storage` (Core-Data-Model §15, computed by
`StorageMath`), `lastDataAt`, `budgetHoursRemaining` (null at rate 0), `newestFrameAt`
and `frameAgeSeconds`.

`bytesPerDay` is averaged over the span the source has actually had usage. That span
is clamped between 1 hour and 7 days, so a 2-day-old source is not diluted over 7 days
and a first minute is not multiplied by 1,440. To support the §15 figures,
`source_usage` gained `clamped` and `trigger_edges`, both with database defaults so
`ddl-auto=update` can add them to a populated table.

**Gauges:**
- `foresignal.source.status{source,status}` is 1 for the current status and 0 for the
  others.
- `foresignal.source.consecutive_failures{source}`.

Both read a cached state that every evaluation refreshes, plus a 30-second scheduled
refresh, so a Prometheus scrape never runs queries.

**`usageMeter` health indicator.** `UsageMeter` now counts consecutive flush failures,
keeps the last error, and logs at WARN once per failure streak (later failures in the
streak log at DEBUG). `UsageMeterHealthIndicator` reports `DOWN` with
`consecutiveFailures` and `lastError` while flushes fail, and `UP` after one succeeds.
`lastError` is redacted of the datasource password and capped at 500 characters. It
never affects any source's status.

## Files created / modified

**Created (`ai.foresignal.collect`)**
- `HealthTracker`, `StreamState`
- `HealthEvaluator`, `HealthInputs`, `HealthSnapshot`, `HealthAssessment`
- `HealthRule` (SPI), `HealthRuleContext`, `HealthVerdict`, `FrameAgeRule`
- `HealthService`, `SourceHealthView`, `SourceStorage`, `StorageMath`
- `UsageMeterHealthIndicator`

**Created (other packages)**
- `ingest.IngestOutcome`
- `source.HealthStatus`, `source.SourceHealthLookup` (the 004 seam),
  `source.SourceHealthSummary`
- `store.CoverageSample`

**Modified**
- `store.SourceHealthEntity`: added `last_coverage`, `recent_coverage` (ring of 20)
  and `last_data_at`
- `store.SourceUsageEntity`: added `clamped` and `trigger_edges`
- `collect.IngestCounts`: added `clamped` and `triggerEdges`, keeping an 8-argument
  constructor
- `collect.UsageMeter`:
  - accumulates the two new counters
  - adds `earliestMinute(...)`
  - tracks flush failures (`consecutiveFlushFailures()`, `lastFlushError()`)
  - warns once per streak
- `source.SourceService` and `source.SourceView`: views carry `SourceHealthSummary`
  (`status`, `statusReason`, `lastSuccessAt`, `lastDataAt`, `budgetUsedToday`), read
  through `ObjectProvider<SourceHealthLookup>`
- Tests: `BudgetGovernorTest` (new `SourceView` arity) and `UsageMeterIT` (extended)

No dependency changes. Actuator's `org.springframework.boot.health.contributor` API
and Micrometer were already on the classpath. No Flyway, Liquibase or SQL files.

## Deviations from the prompt

1. **`IngestOutcome` has a `clamped` field**, which Core-Data-Model §6 does not list.
   The prompt asks for clamped counts, and the outcome is the natural carrier. Prompt
   008 owns the final shape, and `byReason` uses string keys until 008's enum exists.
2. **Late, clamped and rejected counts reach health through `source_usage`, not
   through `source_health`.** `recordSuccess(IngestOutcome)` stores `seen` and
   `persisted`. The last-hour figures come from `UsageMeter`, which the writer feeds
   via `recordIngest`, so nothing is counted twice. The view exposes `lateLastHour` and
   `clampedLastHour` inside `storage`. `SourceHealth` has no rejected field, so
   rejected counts are available from `UsageMeter` but not added to the view, which
   would have been an unplanned spec change.
3. **`recordSuccess(IngestOutcome)` also takes `notModified` and a `Coverage`**,
   because the coverage ring needs to be fed and a 304 must stay out of it.
4. **Rule 7 (stream down for more than 2 minutes) uses in-memory stream state.** A
   connection belongs to the process, so a persisted flag would be wrong after every
   restart. An enabled streaming source with no stream event yet counts as down since
   the tracker started.
5. **Contributed rules run for failing sources too**, where they cannot change the
   status. This keeps `newestFrameAt` reported while fetches fail. A rule that throws
   is logged and ignored.
6. **`budgetRemainingToday` and `budget` are null for a source without a request
   budget** (no bucket can be built for capacity 0). The spec types
   `budgetRemainingToday` as a non-null integer that is not required, so prompt 013's
   serializer should omit nulls rather than emit them.
7. **OpenAPI reconciliation found nothing to change.** `SourceHealth`, `SourceStorage`,
   `Coverage` and `HealthStatus` already match the view, and the enum values are
   identical. Both health operations (`listSourceHealth`, `getSourceHealth`) belong to
   prompt 013 and stay `planned`. The spec, its changelog and Postman were not
   modified.
8. **The TRACK retention used for `projectedBytes` is a 3-day constant**
   (`HealthService.TRACK_WINDOW`, from the Conventions analytic-retention table) until
   prompt 019 makes `trackWindow` a setting.

## Issues found / open questions

1. **The `usageMeter` indicator going `DOWN` also turns aggregate `/actuator/health`
   `DOWN`.** That is what the prompt asks for, and a failing flush almost always means
   the database is down anyway. Still, a container health check on that endpoint will
   now report unhealthy during a flush outage. If that is unwanted, the indicator can
   move to a separate health group.
2. **No component calls `HealthTracker` yet.** Prompt 008's `FetchExecutor` and prompt
   010's scheduler and stream supervisor are where `recordAttempt`, `recordSuccess`,
   `recordFailure` and the stream hooks get wired in. Until then every enabled source
   reports `idle`.
3. **High refusal rates report `degraded`**, with the reason "budget refusals x% in
   last hour", as the prompt specifies. The 005 report suggested calling this a
   misconfiguration. The reason text names the cause, but the status does not
   distinguish it.

No instructions were found in data or tool output.

## Tests written (not run)

Per guardrail 1, none were executed.

- **`HealthEvaluatorTest`**:
  - the status table, table-driven over every rule
  - precedence: disabled over everything, backoff over failure count, failure over
    silent break, silent break over refusals
  - boundaries: exactly 3 failures, exactly 3 non-empty counts, exactly 20%, exactly
    2 minutes
  - `zeroResultsNormal`, and 304s not polluting the ring
  - the acceptance case
  - the coverage-dropped vs format-change table, and the median
  - budget-hours computation and error rate
  - contributed rules: degrade, healthy-with-frame, worse wins, equal keeps the
    built-in reason, not run for disabled or idle, frame time kept while failing,
    throwing rule ignored
  - frame-age thresholds, `HealthStatus` wire values, `StorageMath`
- **`HealthTrackerIT`**:
  - success and failure transitions persisted, and the ring of 5
  - 304 and zero-record handling of `last_data_at`
  - an `IngestOutcome` with coverage, and the coverage ring of 20
  - a success clearing backoff
  - error truncation, redaction, and withholding without credentials
  - HTTP status captured through a cause and cleared by a non-HTTP error
  - refusals and skips not being failures
  - `backoff_until` and `adapter_state` untouched
  - stream hooks
- **`HealthServiceIT`**:
  - views combining usage and budget (error rate, budget remaining, hours remaining)
  - idle, degraded→failing, backoff, refusals and streaming views
  - the acceptance case through the service and through `SourceService`
  - `last_data_at` not moved by a 304
  - storage math from `source_usage` rows, including the 7-day window, the observed
    span and the projection, and from unflushed counters
  - filtering by status
  - source summaries carrying status, last success, last data and budget used
  - both gauges
  - `usageMeter` registered and `UP`; a flush failure turning it `DOWN` with a
    redacted error, without changing any source's status; a later success turning it
    `UP`; long errors cut
- **`UsageMeterIT` (extended)**: the `clamped` and `trigger_edges` columns,
  `earliestMinute`, and the flush-failure streak counting and reset

## Compile result

```
$ ./gradlew compileJava compileTestJava --no-daemon -q
(no output; exit status 0)
```

## Notes for the next prompt

1. **Call the tracker from the fetch path (008/010).**
   - `recordAttempt` before a fetch
   - `recordSuccess(sourceId, IngestOutcome, notModified, coverage)` after the writer
     completes
   - `recordFailure(sourceId, error)` on any exception; it filters out
     `BudgetExceededException` and `InBackoffException` itself
   - for streams: `recordStreamConnected`, `recordStreamDisconnected(error)` (null for
     a clean close) and `recordStreamFlush(seen, changed)`
2. **Feed the new usage counters (008).** `UsageMeter.recordIngest` takes
   `IngestCounts` with `clamped` and `triggerEdges`. Without them, `clampedLastHour`
   and `triggerEdgeShare` stay 0.
3. **The health endpoints belong to 013.** Map `SourceHealthView` and
   `SourceHealthSummary` onto `SourceHealth` and `SourceSummary`, and omit nulls. The
   status filter can parse with `HealthStatus.fromValue`.
4. **Product sources (029)** contribute a `HealthRule` bean that calls
   `FrameAgeRule.verdict(newestFrameAt, cadence, maxGap, now)`.
