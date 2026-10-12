# 038-CR-Stats-Rollups-And-Glance

| | |
|---|---|
| Prompt | 038-Stats-Rollups-And-Glance.md |
| Status | Complete |
| Implementation commit | 99e4fe6997eeb64c4d108c9c64ffcdd966850bb2 |
| Date | 2026-10-12 |

## Summary

Clients now get cheap aggregates instead of paging raw history: an activity rollup that outlives
retention, time series, grids and top-N, a count mode, a ≤ 2 KB glance and Prometheus metric
projections. All six tasks landed. The eight 038 operations are `implemented` and off
`pending-operations.txt`.

**1. `activity_rollup`** (`ai.foresignal.stats`)
- `ActivityRollupEntity` with the key `(resolution, bucket_start, source_id, kind, cell)` and the
  indexes `(resolution, bucket_start)` and `(source_id, resolution, bucket_start)`.
  - `resolution` is generic (`RollupResolution` HOUR = 0, DAY = 1), so 119 can add 2 and 3.
  - `cell` is an H3 index at `foresignal.stats.cell-res` (default 4), 0 for no geometry, and -1 for
    a per-`(source, kind)` totals row (Deviation 2).
  - `cell_lon`/`cell_lat` hold the cell centre.
  - Measures are `versions`, `entities`, `firsts`, `tombstones`, `edges`, plus `changes`
    (Deviation 3). HEARTBEAT and KEYFRAME are never counted. Rows hold no entity ids.
- `RollupJob` runs every 5 min (`foresignal.stats.scheduled`; off in the test profile).
  - It keeps the durable cursor `app_setting['stats.rollup.cursor'] = {lastHour, lastDay, seq,
    since}`, written in the same transaction as each bucket's rows.
  - Hour `H` covers `seq ∈ (S(H⁻), S((H+1h)⁻)]`. It closes at `H + 1h + 2 min` once the writer's last
    ingested time has passed `H + 1h`, or after 10 quiet minutes. Nothing closes while the writer is
    `CLOCK_BEHIND` (Deviation 4).
  - Each bucket is one streamed pass (fetch size 5,000). Centroids are mapped to H3 in Java by
    `RollupComputer`.
  - Days are recomputed from raw rows once their 24 hours are done.
  - The first start backfills from the oldest version, bounded by `foresignal.stats.backfill` (30d).
    At most 6 hours are rolled up per run.
- **Retention:**
  - `RollupCursorConsumer` (`stats.rollup.cursor`) joins the consumer clamp. Its `seq` is the last
    seq rolled into both its hour and its day.
  - `StatsRollupRetentionStep` (order 730) applies `RetentionSettings.stats = {hourly: 35d,
    daily: 1825d}`. Both are stored and validated; `regionHourly`/`regionDaily` are accepted and not
    stored until 119.
- **Privacy and opt-outs:**
  - Private sources are skipped unless `statsIncludePrivate` is set, and `statsEnabled=false`
    excludes a source.
  - Both flags are on `SourceSettings`/`SourceSettingsUpdate` and stored in `app_setting['stats']`
    (Deviation 5).
- `POST /v1/stats/rollup/rebuild` (admin, 202) plans the closed hours and days in range and runs them
  in the background. A bucket is recomputed only for sources whose raw versions in it are all still
  retained.

**2. Stats API** (`StatsService`, `StatsController`; read scope, `@RestrictedRead`)
- **`getStatsTimeseries`:**
  - Buckets `1h|1d|auto`; auto is hourly up to 7 days. More than 2,000 buckets is
    `400 STATS_TOO_LARGE`.
  - Measures and `groupBy` follow the prompt.
  - `ingested` reads the rollup for closed buckets inside its range and retention, and raw rows for
    the rest.
  - Other axes read raw rows and need `from ≥ retainedFrom`.
  - `alerts` reads `alert.fired_at`.
  - `backend` is `rollup`, `raw` or `mixed`.
- **`getStatsGrid`:**
  - Rollup mode returns an H3 `FeatureCollection`: parents via `cellToParent`, polygons via
    `cellToBoundary`, split at the antimeridian.
  - Raw mode uses `ST_HexagonGrid` in EPSG:3857, transformed back, over at most 7 days.
  - More than 10,000 cells is `STATS_TOO_LARGE`. The response carries `cellSystem`.
- **`getStatsTop`:** `by=source|kind|cell|entity`, `limit` ≤ 100. `entity` reads raw rows inside
  retention, with titles.
- **Count mode:** `count=true` on `listRecords`, `queryHistory` and `search` returns
  `{count, exact, watermarkSeq}` (`CountResult`, `LIMIT 1,000,001`).
- **Timeouts:** every stats query runs with a 10 s `statement_timeout` (503 `QUERY_TIMEOUT` with
  `Retry-After`).
- `RollupTimelineBackend` (`@Primary`) serves `/v1/timeline` `ingested` buckets that start before
  `retainedFrom` from the rollup when it can answer exactly. Everything else, alerts included, comes
  from `RawTimelineBackend`.

**3. Glance** (`stats.glance`)
- `GlanceService` assembles:
  - `status`: the worst of the writer, readiness and the visible sources' health;
  - `alerts.open` (`state = firing`, unsuppressed) and `latest` (the newest 3);
  - `sources` tallies, `briefing` (the newest visible completed run), `activity.versionsLastHour`,
    `lastSeq` and `links` (`clientLinks`);
  - the sections of the `GlanceContributor` SPI (ordered beans, each with a byte budget).
- `GlanceAssembler` keeps the JSON within 2,048 bytes. It drops `latest` entries first, then sections
  from the last one, then links and the briefing. The text form has at most 8 lines of 40 ASCII
  characters.
- **Anonymous callers** see `status`, `serverTime`, `alerts.latest` and `lastSeq` only. Counts are
  added when `glance.exposeCountsAnonymously` is true; links, briefing and sections never are.
  Private-source alerts follow the 023 gate, and `place` narrows the alerts.
- `GlanceController` caches each glance for 15 s per (caller class, place, format). It sends a strong
  ETag (304 on a match) and `Cache-Control: private, max-age=15`.
- MCP `get_glance` (read, `@RestrictedRead`).

**4. Metric projections** (`stats.metrics`)
- `GET/PUT /v1/settings/metrics` (admin, under `/v1/settings/**`) store `app_setting['metrics']`.
  `MetricSettingsService` validates:
  - the name regex, unique names, a known source and a declared kind;
  - the 017 path syntax (`RuleValidator.PATH`), never a `volatileFields` root;
  - at most 4 labels;
  - `maxSeries` from 1 to 10,000;
  - private sources only with `allowPrivate`.
- `ProjectionService` keeps a dedicated `PrometheusMeterRegistry` that is never a bean.
  - It seeds from `record_head ⋈ record_live` (the merged payload) and follows ChangeBus and LiveBus.
    Touches move the observed time.
  - Values are numbers or numeric strings; anything else counts
    `foresignal.projection.coercion_failures{projection}`.
  - Series are dropped on tombstone, expiry or purge (a sweep every minute).
  - Each value has a companion `_observed_timestamp_seconds`.
  - `maxSeries` caps each projection. A global cap of 10,000 raises a `system.metrics_series_cap`
    event once.
- `GET /v1/metrics` (read) serves the text format 0.0.4, or OpenMetrics by `Accept`.
- `OperatorGauges` gains `foresignal.alerts.open{severity}`; `data_age_seconds` already existed (019).

**5. Briefing digest:** the previous equal-length period now comes from `StatsService.totals`. Whole
closed hours come from the rollup and the edges from raw rows, so `previous` survives retention. The
old previous-period query is gone (Deviation 6). The golden digest fixture is unchanged, because
`DigestRenderer` and the `DigestStats` shape did not change.

**6. Spec, Postman, docs**
- `docs/OpenAPI.json`: the eight operations are `implemented`, and the reconciliations are listed
  under Deviation 8 and in `info.x-foresignal-changelog`.
- `BODIES` gains `rebuildStatsRollup` and `updateMetricSettings` (a USGS magnitude gauge), and
  Postman is regenerated.
- `AccessPolicy`: GET `/v1/stats/**`, `/v1/glance` and `/v1/metrics` are READ; POST
  `/v1/stats/rollup/rebuild` is ADMIN.
- Operator Guide: a new "Stats, glance and metrics" section with the rollup storage estimate, the
  Prometheus scrape job with a bearer token, Grafana Infinity, TRMNL, MagicMirror, a Home Assistant
  REST sensor, and Garmin/Wear OS. The retention section gains `stats`.
- Agent Guide: `get_glance` and count mode.
- Dependency `com.uber:h3` 4.5.0 (Apache-2.0), with an entry in `THIRD_PARTY_NOTICES.md`.

## Files created / modified

**New, main:**
- `stats/`: `ActivityRollupEntity`, `ActivityRollupId`, `RollupResolution`, `RollupCursor`,
  `RollupComputer`, `RollupJob`, `RollupCursorConsumer`, `StatsRollupRetentionStep`, `StatsProperties`,
  `StatsSettings`, `StatsSettingsService`, `H3Cells`, `StatsMeasure`, `StatsQuery`, `StatsSeries`,
  `StatsGrid`, `StatsTop`, `StatsService`, `RollupTimelineBackend`, `package-info`
- `stats/glance/`: `GlanceService`, `GlanceAssembler`, `GlanceData`, `GlanceContributor`,
  `GlanceSettings`, `package-info`
- `stats/metrics/`: `MetricProjection`, `MetricSettings`, `MetricSettingsService`,
  `ProjectionService`, `package-info`
- `api/StatsController`, `api/GlanceController`, `api/MetricsController`;
  `mcp/tools/GetGlanceTool`; `store/query/CountResult`

**Changed, main:**
- `api/RecordsController` and `api/SearchController` (count mode); `search/SearchService` (`count`,
  head filters extracted); `store/query/RecordQueryService` (`countCurrent`, `countHistory`).
- `api/SourcesController`, `api/dto/SourceSettingsDto`, `api/dto/SourceSettingsUpdate` and
  `api/mapper/SourceMapper` (`statsEnabled`, `statsIncludePrivate`).
- `briefing/DigestStatsService` (previous period from `StatsService`), and the documentation of
  `DigestStatsProvider` and `SqlDigestStatsProvider`; `briefing/BriefingRunRepository` (latest runs).
- `settings/RetentionSettings` (`stats`), `retention/RetentionSettingsService` (stores
  `stats.hourly`/`daily`), `settings/SettingKeys` (4 keys).
- `trigger/AlertStore` (`countBySeverity`), `system/OperatorGauges` (`alerts.open`),
  `security/AccessPolicy`, `api/error/ErrorCodes` (`STATS_TOO_LARGE`), `config/StartupOrder`
  (`METRIC_PROJECTIONS` = 23).

**Build and docs:** `build.gradle.kts`, `gradle/libs.versions.toml`, `THIRD_PARTY_NOTICES.md`,
`docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `scripts/generate-postman.py`,
`docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

**Tests:** see below. Also changed: `security/AccessPolicyTest`,
`security/restrict/RestrictedReadCoverageTest` (`/v1/stats`, `/v1/glance`),
`openapi/PostmanCollectionContractTest` (two validators), `briefing/DigestStatsServiceIT`,
`src/test/resources/application-test.yml` (`foresignal.stats.scheduled: false`) and
`pending-operations.txt` (−8 lines).

## Deviations from the prompt

1. **`RollupCursor.since`.** The cursor also stores the first hour rolled up. Readers serve buckets
   before it from raw rows instead of reading an empty rollup, and a rebuild never creates rows before
   it.
2. **Totals rows (`cell = -1`).** Per-cell distinct entities are not additive across cells: a
   mover would count once per cell. Each `(source, kind)` therefore also gets one row whose
   `entities` is exact over every cell.
   - Queries without a spatial filter read only these rows, so they are exact and cheap.
   - With a bbox, a restricted region or a place filter, cell rows are chosen by their centre.
     Edges are then accurate to a cell, and `entities` counts once per cell (documented).
3. **Extra measure `changes`** (SIGNIFICANT + CHANGE). The digest's `previous.changed` needs it, and
   the five prompt measures cannot produce it. It is internal; the API's `measure` enum is unchanged.
4. **Closed hours on a quiet system.** The prompt's "the writer's last ingested time has passed it"
   alone would never close an hour when nothing is ingested afterwards. A batch takes its
   `ingested_at` when its transaction begins, so an hour also closes after 10 quiet minutes past its
   end. Nothing closes while the writer is `CLOCK_BEHIND`.
5. **Where the per-source flags live.** `statsEnabled` and `statsIncludePrivate`
   (`stats.includePrivate[sourceId]`) live in `app_setting['stats']`, written through
   `updateSource` and shown in `SourceSettings`. This avoids a `source_settings` column and a
   `SourceView` change. Unregistered sources are never rolled up.
6. **Digest counts.** Only the previous period moved to `StatsService.totals`. The current period's
   counts stay on the raw per-reason query: the digest's `byReason`, `routine` and per-reason
   distinct `entities` need a per-reason breakdown, which the rollup does not keep. The current
   period is inside retention whenever `gap` is false.
   - The previous period is now taken on the time axis `[fromTime − length, fromTime)`. For a
     `sinceSeq` cursor this differs slightly from the old seq bounds.
   - It is null when part of the period is neither retained raw nor rolled up.
7. **Rollup grid, top and timeline.**
   - The grid reads closed buckets only: hourly rows up to 7 days, daily rows beyond. The open hour is
     not drawn, and `backend` stays `rollup`, matching the spec enum.
   - `top` sums rollup and raw parts. `entities` is then approximate across the parts.
   - `by=cell` with a view filter (which needs the payload) is refused.
   - The rollup timeline is used only when every involved source is rolled up and the buckets align
     exactly; otherwise it is raw.
8. **Spec reconciliations** (all in the changelog):
   - the stats reads gain 404 and list their 400 causes;
   - `getStatsTop.by` defaults to `source`;
   - the grid documents its default `res`, its resolutions and `sizeKm`. `sizeKm` is ground km at
     the bbox's centre latitude, scaled to Web Mercator, so hexagons stay regular on the map;
   - `getGlance` gains 400, 404 `UNKNOWN_PLACE` and `Cache-Control`; `place` without a token is 401,
     as on every 036 path;
   - `queryHistory`'s JSON 200 becomes `HistoryPage | CountResult`;
   - `MetricProjection.maxSeries` gets maximum 10000; `RetentionSettings.stats` gets defaults;
   - `Problem` lists `STATS_TOO_LARGE`.
9. **Region filters** (`country`, `admin1`, `county`, `region`, `groupBy=region`, `by=region`) are
   `400` until 040 and 119 implement them.
10. **Search count** counts text matches: documents matching the query, plus heads whose search text
    contains it. `mode=semantic` with `count=true` is `400`, because similarity has no match
    boundary.
11. **Metrics visibility.**
    - Private-source projections are shown only to callers that pass the 023 gate (an agent or admin
      token), even with `allowPrivate`.
    - Tokens restricted by area or a request filter see no projections; kind restrictions need a
      projection `kind`.
    - The default labels are `source` and `external_id`. Entities with equal custom label values
      share a series, and the latest write wins.
12. **Test hooks:** `RecordQueryService.countCurrent/countHistory(q, cap)`,
    `StatsService.series(q, timeout)`, `ProjectionService.maxTotalSeries(cap)` and
    `RollupJob.rebuildNow`. They follow 037's lower-limit hooks.

## Issues found / open questions

1. **Not verified at run time:** these first run at 041.
   - `ST_HexagonGrid` over a transformed envelope;
   - `LOCK TABLE` blocking a read-only query into the statement timeout;
   - the exact Prometheus text output (label order, HELP text);
   - SAMPLED live-only updates reaching LiveBus with the merged `kinematic` value;
   - the HEARTBEAT scenario: a TTL source re-observed after its 568 s keyframe.
2. **`JacksonDurationIT.retentionSettingsMatchesContract`** asserts an exact JSON string without
   `audit` and `inbox` (023 and 033), so it already disagreed before this prompt. `stats` adds one
   more member. It was left untouched: **owner / 041**, decide whether to update the string.
3. **Settings export/import** (`SettingsTransferService`) does not carry `app_setting['stats']`,
   `glance` or `metrics`. The prompt does not ask for it.
4. **Storage:** global aircraft at resolution 4 cost about 0.5 million rows a day (Operator Guide).
   Nothing enforces a budget, and the operator lowers `cellRes` or disables the source.
5. **Data:** no instructions directed at an agent were found in data, fixtures or tool output. Two
   Maven Central documents were fetched (h3 metadata and POM) and contained none.
6. Owner decisions carried from 012–037 are still open.

## Tests written (not run)

Every IT seeds records only through `TestRecords`; alerts go through `AlertRepository`. The 038 ITs
share one new context, `StatsTestSources`: 019's retention sources plus `st-points` (STANDARD, two
kinds) and `st-secret` (private). They extend `StatsItSupport`, which extends `RetentionItSupport`.

| Test | What it covers |
|---|---|
| `stats/RollupJobIT` | Closed-hour rule (delay, writer passed, quiet, `CLOCK_BEHIND`); exact measures across two hours and kinds, totals rows equal to cell rows; HEARTBEAT excluded; daily distinct entities differ from the sum of hours; cursor survives a restart and a rebuild does not double count; backfill bound and 6 hours per run; private source skipped until `includePrivate`, no entity id column; `statsEnabled=false`; retention clamp held by `stats.rollup.cursor` |
| `stats/StatsRollupRetentionStepIT` | Registration and order; hourly rows gone after 35 d, daily kept; `updateRetention` stores `stats.hourly`/`daily` (region members accepted) and the step follows them |
| `stats/StatsServiceIT` | Backend `rollup` and `mixed` with the open hour; raw axes and the `retainedFrom` 400; skipped private source read raw; exact entities; alerts; the bucket limit and auto; the H3 grid per res-2 parent; the hex3857 grid regular at 60°N (sides equal in EPSG:3857) and its limits; top entity with titles, top kind and cell; 503 `QUERY_TIMEOUT` under an injected 1 s timeout; **counts unchanged after raw deletion**; digest totals (rollup plus raw edge); rollup-backed timeline (`mixed`) |
| `stats/H3CellsTest` | Cells, wrap and invalid input; parent aggregation; closed hexagon boundaries; antimeridian split; rounding |
| `store/query/CountModeIT` | Exact counts for records and history (service and HTTP); `count` with `asOf` 400; `exact=false` above a lowered cap; search count and the semantic refusal |
| `stats/glance/GlanceServiceTest` | ≤ 2,048 bytes under a flood of alerts and contributors; drop order; anonymous view without counts; the opt-in setting; links and contributor budgets and failures; text limits (8 × 40 ASCII, accents folded, seq last); the status rule |
| `api/GlanceControllerIT` | ETag and 304, the 15 s cache, `Cache-Control`; text by `format` and by `Accept`; anonymous without counts or private alerts; the opt-in; an agent sees private alerts; 401 with `openReads` off; `place` narrows, needs a token, unknown place 404 |
| `stats/metrics/ProjectionServiceIT` | Seeding from heads, a new version updates, a tombstone drops; SAMPLED live update; expiry and purge drops; coercion counter and numeric strings; `maxSeries` and the global cap event (once); custom labels and OpenMetrics; validation (name, duplicate, volatile path, private, unknown source, bad path, kind, labels) |
| `api/MetricsEndpointIT` | The text exposition parses (HELP, TYPE, samples, timestamps); OpenMetrics `# EOF`; never on `/actuator/prometheus`; private series need an agent token; anonymous under `openReads`, 401 without; settings admin only and 400 on a bad body; restriction visibility |
| `api/StatsControllerIT` | Timeseries, grid (`application/geo+json`) and top documents; region filters, `groupBy=region` and `by=region` 400; hidden source 404; bad measure, missing `from`, missing bbox 400; rebuild 403 for read and 202 for admin, inverted range 400 |
| `mcp/tools/GetGlanceToolTest` | Read scope and untrusted note; JSON and text; `place` without a token and a bad format refused |
| `briefing/DigestStatsServiceIT` (new test) | The previous period's counts come from `StatsService` and survive deletion of the raw versions (47 hours rolled up first) |

`DigestRendererIT` is unchanged and its golden fixture still matches: the renderer reads `DigestStats`,
whose shape did not change.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 0 errors, 71 warnings (unchanged).

New dependency: `com.uber:h3` 4.5.0 (Apache-2.0). It has no runtime dependencies, and its JNI library
is bundled for linux-x64, linux-arm64, darwin-x64 and darwin-arm64 (verified in the jar).

## Notes for the next prompt

1. **039 (private sources, EgressPolicy):** stats, glance and metrics take the caller's excluded set
   from `ReadCallerResolver.excludedSources`. Fold that into `EgressPolicy`. The rollup's private
   rule (`StatsSettingsService.rolledUp`) reads the descriptor's `private` flag; include the
   operator's `private` tightening there too.
2. **040 (regions, near):** implement `country`, `admin1` and `county` on the stats reads (now
   refused in `StatsController.refuseRegions`). Fill `GlanceData.Latest.near` through a
   `GlanceContributor` or directly.
3. **044 (downsampling):** the rollup reads raw TRACK rows behind its cursor, and the clamp keeps
   them until the day is rolled. Downsampling must not rewrite seqs above `stats.rollup.cursor.seq`,
   or the hour counts would change.
4. **056 (weather at a place):** add a `GlanceContributor` bean with a byte budget. Sections are
   token-only and dropped last-first when the glance is too large.
5. **119 (region rollups):** add resolutions 2 and 3 to `RollupResolution` and a nullable
   `region_code` (make it part of the key). Extend `StatsRollupRetentionStep.windows`, store
   `stats.regionHourly`/`regionDaily` in `RetentionSettings.Stats`, and lift the `groupBy=region` /
   `by=region` refusals. Use `stats.region.cursor` with the same clamp pattern.
6. **041 checkpoint:** the new ITs first run there, in the `StatsTestSources` context. See Issue 1
   and Issue 2.
7. **144 (Flyway):** baseline `activity_rollup` with its two indexes.
