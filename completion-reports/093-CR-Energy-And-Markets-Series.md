# 093-CR-Energy-And-Markets-Series

| | |
|---|---|
| Prompt | 093-Energy-And-Markets-Series.md |
| Status | Complete (post-review fixes applied 2026-10-16) |
| Implementation commit | 17b7f29ef7eb2fec4fa57546785fb077efc22500 |
| Post-review fix commit | 123a6b7 (`093: fix post-review defects`) |
| Date | 2026-10-14 (post-review fixes 2026-10-16) |

## Summary

Energy and markets time series land as records **without geometry**, so the same versioning, triggers, briefings and
MCP tools apply to oil prices, US grid demand, EU gas storage, monthly generation mix, grid-frequency excursions and
GB carbon intensity. A revision becomes a new version; `series-latest` gives rules one entity per series. A code
review found defects in the implementation commit; they are fixed in 123a6b7 and listed under "Post-review fixes".

**Shared framework (`ai.foresignal.source.series`).**
- `SeriesPoint` builds a `series-point` record (`externalId = {seriesId}:{period}`, no geometry, `eventTime =
  periodStart`) and a `series-latest` record (`{seriesId}:latest`), parses upstream numeric strings and rejects NaN,
  and provides the STANDARD/ALL descriptor defaults, the payload `fieldSpecs` and `latestPerSeries` (which skips
  forecasts, so a forecast-only series gets no latest record). Frequencies: `PT15S, PT15M, PT30M, PT1H, P1D, P1W,
  P1M` (`PT15M`, ERCOT's settlement interval, added post-review).
- `SeriesPeriods` maps an upstream period label (`YYYY-MM-DD`, `YYYY-MM-DDTHH`, `YYYY-MM`, ISO) plus a fixed
  frequency to start/end; `weekEnding` maps a week labeled by its last day (EIA weekly series).
- `FrequencyExcursionDetector` (pure, shared) turns samples into `frequency-excursion` records (nominal 50 Hz;
  operational/statutory bands; severe below 49.2 Hz) and one `grid-frequency` state record per poll
  (`normal|low|high|no-data`), keeping the last processed sample time so a restart does not double-count.

**Sources built (all STANDARD/ALL, TTL none, `ttlAnchor=OBSERVED`, `titleField=seriesName`, retention 3650 d, the
`series` class).** `eia-petroleum`, `eia-930`, `ercot-public` (disabled by default), `gie-agsi`,
`ember-electricity`, `grid-frequency-gb`, `grid-frequency-fingrid` (disabled by default) and `gb-carbon-intensity`.
EIA sends its key as `api_key` in the URL (redacted by the upstream client); AGSI uses `x-key` and turns the HTTP-200
`access denied` body into a rejected credential; Fingrid uses `x-api-key`; ERCOT uses a cached Azure B2C token plus
`Ocp-Apim-Subscription-Key`.

- `eia-930` reads each dataset with `start` set, `length=5000` (EIA's cap) and `offset` paging until
  `response.total` rows have arrived. The default window (8 respondents × 4 types × 48 h ≈ 1,550 region rows;
  8 × 10 fuels × 48 h ≈ 3,850 fuel rows) is one page per dataset; at most two pages per dataset per run (four
  requests, the per-minute budget); a window that still does not fit is a partial result with the source degraded.
- `eia-petroleum` weekly stocks are periods ending on their label date.
- `ercot-public` converts market time from US Central (`America/Chicago`) with the zone's rules; the fall-back
  repeated hour is identified by `DSTFlag` and labeled `…-DST`, a spring-forward gap hour is skipped; `hourEnding`
  arrives as `HH:MM` text. The hourly load report is read once an hour, the hub prices every run.
- `gie-agsi` sends one `type=eu` request (the EU aggregate) plus one `country={cc}` request per configured country.
- `grid-frequency-gb` and `grid-frequency-fingrid` ask for at most the last hour after an outage; the GB attribution
  names the current year.

**API / MCP / schema.** `GET /v1/series` (catalog from `series-latest`) and
`GET /v1/series/{sourceId}/{seriesId}` (points in `periodStart` order, JSON or CSV), backed by
`SeriesQueryService` native SQL using the new partial expression index; MCP `get_series`; AccessPolicy `read` rules;
`docs/OpenAPI.json` statuses set to `implemented` plus a changelog entry; Postman regenerated (no diff).
`SchemaBootstrap` (via `SpatialIndexInitializer`) adds
`record_head (source_id, (payload->>'series'), event_time) WHERE kind='series-point'`. Since the post-review fix,
every series read (REST and MCP) applies the caller's excluded sources and restriction, like the record reads: both
controller methods and `GetSeriesTool` carry `@RestrictedRead`, a named private source the caller may not see is a
404, and on the Ask path the LLM-excluded sources come through the tool caller.

**Docs.** `docs/Source-Catalog.md` gains an "Energy and markets series" section and updated external-provider notes
for Kalshi/Polymarket (no adapter for either); `docs/Operator-Guide.md` gains keys, ERCOT registration and US-only
access, and two rule recipes.

## Files created / modified

**Created, main**
- `source/series/{package-info,SeriesPoint,SeriesPeriods,FrequencyExcursionDetector}.java`
- `source/adapters/eia/{package-info,EiaApi,EiaParser,EiaPetroleumAdapter,Eia930Adapter}.java`
- `source/adapters/ercot/{package-info,ErcotParser,ErcotAdapter}.java`
- `source/adapters/gie/{package-info,AgsiParser,AgsiAdapter}.java`
- `source/adapters/ember/{package-info,EmberParser,EmberAdapter}.java`
- `source/adapters/elexon/{package-info,ElexonFrequencyParser,GridFrequencyGbAdapter}.java`
- `source/adapters/fingrid/{package-info,FingridFrequencyParser,GridFrequencyFingridAdapter}.java`
- `source/adapters/carbon/{package-info,CarbonIntensityParser,CarbonIntensityAdapter}.java`
- `api/SeriesController.java`, `api/SeriesQueryService.java`,
  `api/dto/{SeriesInfoDto,SeriesPageDto,SeriesPointDto,SeriesPointPageDto}.java`
- `mcp/tools/GetSeriesTool.java`

**Modified, main**
- `config/SpatialIndexInitializer.java` (the partial expression index)
- `security/AccessPolicy.java` (two `read` rules for `/v1/series`)

**Created, test**
- `source/series/{SeriesPointTest,FrequencyExcursionDetectorTest}.java`
- `source/adapters/eia/{EiaParserTest,EiaAdapterTest,SeriesIngestIT}.java`; post-review `EiaApiTest`
- `source/adapters/ercot/{ErcotParserTest,ErcotAdapterTest}.java`
- `source/adapters/gie/{AgsiParserTest,AgsiAdapterTest}.java`
- `source/adapters/ember/{EmberParserTest,EmberAdapterTest}.java`
- `source/adapters/elexon/{ElexonFrequencyParserTest,GridFrequencyAdaptersTest}.java`; post-review
  `GridFrequencyGbWindowTest`
- `source/adapters/fingrid/FingridFrequencyParserTest.java`; post-review `GridFrequencyFingridWindowTest`
- `source/adapters/carbon/{CarbonIntensityParserTest,CarbonIntensityAdapterTest}.java`
- post-review `mcp/tools/GetSeriesToolTest.java`
- fixtures `eia-petroleum/`, `eia-930/`, `gie-agsi/`, `ember-electricity/`, `gb-carbon-intensity/`,
  `grid-frequency-gb/`, `grid-frequency-fingrid/`, `ercot-public/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (eight 093 adapters classified)
- `testsupport/weather/WeatherIngestSupport.java` (093 base-URL properties and SOURCES)
- post-review `security/restrict/RestrictedReadCoverageTest.java` (`/v1/series` added to `READ_PREFIXES`)

**Docs**
- `docs/OpenAPI.json` (statuses + changelog; post-review: a `404` on `listSeries`), `docs/Source-Catalog.md`
  (post-review: EIA-930 paging, weekly periods, ERCOT time and budget, AGSI requests and budget, frequency
  catch-up), `docs/Operator-Guide.md`
- `src/test/resources/openapi/pending-operations.txt` (two lines removed)

## Deviations from the prompt

1. **Retention 3650 d, not 90/730 d.** Conventions' analytic-retention table puts `series-point`/`series-latest` in
   the `series` class (3650 d) and says a shorter window named for an `event` or `series` kind is overridden. The
   prompt's "90 d sub-daily, 730 d daily/weekly/monthly" therefore does not apply; `defaultRetention` is 3650 d for
   every series source (the grid-frequency sources are the `event` class, 1825 d).
2. **`eia-petroleum` is `LOCAL_ONLY`.** The prompt did not name an egress, but Source-Catalog (v1.3) records that the
   EIA spot prices are LSEG-sourced, so the source is `LOCAL_ONLY` with the `licenceNote` in its descriptor
   description (there is no `licenceNote` field on `SourceDescriptor`).
3. **Shared package `ai.foresignal.source.series`.** The prompt names it explicitly; Adapter-Guide suggests
   `source.formats.*` for shared code. The prompt wins; the package is not an adapter package, so no cross-adapter
   import is introduced.
4. **ERCOT `settlementPoint` is a single-value query parameter**, so the adapter makes one request per hub (5 default).
   To fit the prompt's 600/day budget the hourly load report is read once an hour rather than every 15-minute run:
   24 + 96 × 5 = 504 report requests plus about 26 token requests a day. (The implementation commit read the load
   every run, 576 report requests plus the token requests, about 602 a day, over the budget.) The B2C `client_id`
   `fec253ea-0d06-4272-a5e6-b478baeecd70` is ERCOT's published value, taken from its authentication guide.
5. **AGSI budget 6/min and 60/day, not 2/min and 40/day.** The GIE API manual v013 (§7.1) serves the EU aggregate
   with `type=eu` and a country only with `country=`; a run is therefore one request plus one per country, six with
   the default five countries. The prompt's 2/min refused the third request of a run and 40/day did not cover eight
   runs. AGSI's own limit is 60/min. (The implementation commit sent one `type=eu` request and filtered it to
   `countries`, always keeping the EU row, which made the filter a no-op.)
6. **Fixtures.** `gb-carbon-intensity` and `grid-frequency-gb` are real captures (2026-10-14). The keyed or
   authenticated sources (`eia-petroleum`, `eia-930`, `gie-agsi`, `ember-electricity`, `grid-frequency-fingrid`,
   `ercot-public`) have synthetic fixtures built from published docs/examples, marked `synthetic: true` with
   "recapture with a real key" in their READMEs (Conventions, "No credentials during construction").
7. **Forecast-only series get no `series-latest`.** As the prompt's `SeriesPointTest` requires ("skips forecasts"),
   EIA-930 `DF` and the carbon regional series (forecast only) are not in the catalog; their points are still
   served by `/v1/series/{source}/{id}`.
8. **"Disabled by default" is the framework default.** Every built-in source seeds a disabled settings row
   (`SourceService.seedDefaults`), so `ercot-public` and `grid-frequency-fingrid` are disabled like all sources; no
   descriptor flag was added.
9. **ERCOT repeated-hour labels.** The fall-back repeated hour's `period` (and so its `externalId`) gains the suffix
   `-DST` (`2026-11-01T2-DST`); every other label is unchanged.
10. **A spatially restricted token sees no series.** Series records have no geometry, and `RestrictionClamp` fails a
    geometry-less row closed under a spatial restriction, as it does for every other read.

## Issues found / open questions

1. **The series reads skipped private-source and restriction filtering** in the implementation commit:
   `SeriesController`, `SeriesQueryService` and `GetSeriesTool` applied neither the caller's excluded sources nor its
   restriction, so a private source's series reached anonymous callers, restricted tokens and (through Ask) LLM
   destinations that may not receive it, and `GetSeriesTool` carried `@RestrictedRead` without clamping. The
   original version of this issue said only that `/v1/series` was not clamped and omitted the private-source and
   MCP marker gap. Fixed in 123a6b7.
2. **`docs/OpenAPI.json` says "LOCAL_ONLY sources' values are served only to callers allowed local egress"** on
   `listSeries`. No such filter exists on this read path (nor, as far as this pass found, on the record reads):
   `LOCAL_ONLY` is applied to egress paths (sinks, federation, shares, feeds). The document and the code disagree;
   left for a decision.
3. **AGSI aggregate codes** (`EU` vs `eu`) and per-country row shapes are assumed from the manual; confirm against a
   real key.
4. **ERCOT is unreachable from outside the US** (403 → a `geo-blocked` health reason) and needs a personally
   registered account; the adapter is unverified live. The `hourEnding` text type and the `DSTFlag` field are taken
   from ERCOT's published OpenAPI spec (`github.com/ercot/api-specs`, `pubapi/pubapi-apim-api.json`, read
   2026-10-16); which occurrence `DSTFlag` marks (here: the second, standard-time one) is not stated there.
5. **EIA over-use suspends the key** (429/403 → backoff), and the FAQ advises under ~9,000 requests/hour and under
   5/s; the descriptor budgets (4/min, 200/day each) sit well inside.
6. **EIA units.** `EiaApi.ucum` maps every unit the default series return; an unexpected unit is passed through as
   EIA's text rather than dropped, so it is not UCUM until mapped.
7. **Kalshi and Polymarket** are not built; their verified 2026-10 notes are recorded under "External provider
   ideas" (Kalshi's terms restrict storing data without written consent; Polymarket's gamma API is not blocked).
8. **Instructions in data:** none.

## Tests written (not run)

- `SeriesPointTest` (externalId, keys, units, numeric-string parsing/NaN rejection, `series-latest` skipping
  forecasts, `SeriesPeriods`, week-ending periods, `PT15M`).
- `FrequencyExcursionDetectorTest` (in-band, short excursion, statutory/severe, spanning two polls, restart without
  double-counting, stale → `no-data`, state round-trip).
- Parser contracts with fixtures: `EiaParserTest` (string values, `warnings` incomplete → error, `value-units`),
  `ErcotParserTest` (column mapping by `fields`), `AgsiParserTest` (status N skipped, HTTP-200 error body),
  `EmberParserTest`, `ElexonFrequencyParserTest`, `FingridFrequencyParserTest`,
  `CarbonIntensityParserTest` (regional without `actual`); `EiaApiTest` (units, query copies).
- WireMock adapter tests: `EiaAdapterTest` (`api_key` redacted in traces/recorded exchanges, incomplete error,
  missing key, Retry-After backoff, eia-930 DF forecast, offset paging to `response.total`, a too-large window
  partial and degraded, weekly week-ending periods), `AgsiAdapterTest` (`x-key`, one `country=` request per
  country, the default budget, access-denied), `EmberAdapterTest` (options short-circuit),
  `CarbonIntensityAdapterTest` (endpoints, pt24h once an hour), `GridFrequencyAdaptersTest` (Elexon no key, Fingrid
  `x-api-key`, Retry-After), `GridFrequencyGbWindowTest`/`GridFrequencyFingridWindowTest` (capped catch-up window,
  attribution year), `ErcotAdapterTest` (token fetched once and reused for 55 min, 401 re-fetch, both DST
  transitions, hourly load gating, the daily budget).
- `GetSeriesToolTest` (the caller's excluded sources and restriction reach every read; a hidden series is missing).
- `SeriesIngestIT` (`@IntegrationTest`): a revised EIA value is version 2 and moves `series-latest`; an unchanged
  re-poll adds no version; `/v1/series` lists the catalog; `/v1/series/eia-petroleum/eia:RWTC` returns points in
  period order and as CSV; an unknown series is 404; a private source's series are hidden from anonymous callers
  (404 when named) and shown to a token with the private grant; a source-restricted and a spatially restricted token
  see none.
- `RestrictedReadCoverageTest` now covers `/v1/series`, so a future series endpoint without `@RestrictedRead` fails.

## Compile result

- `./gradlew compileJava compileTestJava`: **BUILD SUCCESSFUL** (2026-10-16, after the post-review fixes).
- `docs/OpenAPI.json`: valid JSON; `listSeries`/`getSeriesPoints` set to `implemented`.

## Post-review fixes (2026-10-16)

| # | Severity | Finding | Fix |
|---|---|---|---|
| 1 | HIGH | `eia-930` sent `length=48` for windows of about 1,550 and 3,850 rows, so about 97 % of points were silently missing; `total` was never checked | `length=5000` with `offset` paging to `response.total`, at most two pages per dataset per run; a shortfall is partial and degraded; tests `eia930Paging`, `eia930Truncated` |
| 2 | HIGH | Series reads skipped private-source and restriction filtering (EGR-1); `GetSeriesTool` carried `@RestrictedRead` without clamping | `SeriesQueryService.Visibility` (excluded sources + `RestrictionClamp`) on every query; controller resolves the caller (hidden source → 404) and carries `@RestrictedRead`; the tool passes `ToolContext.excludedSources()`/`restriction()`, which on Ask include `EgressPolicy.llmExcludedSources`; `RestrictedReadCoverageTest` covers `/v1/series`; tests in `SeriesIngestIT` and `GetSeriesToolTest` |
| 3 | HIGH | ERCOT anchored market days and hours to UTC | US Central with zone rules; `DSTFlag` for the repeated hour (`-DST` label), gap hour skipped; `hourEnding` text parsed (real rows were all being skipped as non-numeric); tests `fallBack`, `springForward` |
| 4 | MEDIUM | AGSI's `countries` filter was a no-op (one `type=eu` request, EU row always kept) | One `country=` request per country per the GIE manual; budget raised to fit (Deviation 5); test `countries` |
| 5 | MEDIUM | ERCOT ran about 602 requests a day against 600 | Hourly load gating (Deviation 4); test `dailyBudget`, `loadHourly` |
| 6 | MEDIUM | Weekly EIA labels (week ending) were read as period starts, putting `observedAt` a week ahead | `SeriesPeriods.weekEnding`; test `weeklyIsWeekEnding` |
| 7 | LOW | `PT15M` not an accepted frequency; `get_series` description typo `eia: RWTC`; `EiaApi.ucum` fell back to raw text; Elexon attribution year fixed at class load; GB/Fingrid catch-up window unbounded | `PT15M` added; typo fixed; more EIA spellings mapped (fallback kept and recorded, Issue 6); descriptor rebuilt when the year turns; window capped at one hour |

**CR corrections.** The previous version claimed "one request per dataset per poll (a 48-hour window is far under the
5,000-row cap)", which was true of the row count but not of the request it sent (`length=48`); Issue 1 understated the
visibility gap (it omitted private sources and the MCP marker); ERCOT's UTC anchoring and its daily request count
(Deviation 4 omitted the token requests) were not disclosed. All corrected above.

## Notes for the next prompt

1. **097 checkpoint:** first run of the 093 tests. `WeatherIngestSupport` gained the 093 base-URL properties and
   SOURCES entries; `FamilyDefaultsTest` gained eight entries (six `series`, two `event`); `pending-operations.txt`
   lost `listSeries` and `getSeriesPoints`.
2. **143:** recapture the synthetic fixtures (`eia-petroleum`, `eia-930`, `gie-agsi`, `ember-electricity`,
   `grid-frequency-fingrid`, `ercot-public`) with real keys, per their READMEs.
3. **144 (Flyway):** no schema change; the new DDL is one `CREATE INDEX IF NOT EXISTS` in
   `SpatialIndexInitializer.STATEMENTS` (the partial expression index), which moves into the baseline with the rest.
   Adapter state: `freq:{grid}` (the detector state), `ember:month:{dataset}`, `carbon:pt24h:hour`, `ercot:loadAt`.
4. **117 (markets and attention):** reuses the `series-point`/`series-latest` model and the `/v1/series` endpoints;
   its `SeriesInfo` extension (`x-foresignal-extended-by: 117`) is additive. New series reads must go through
   `SeriesQueryService.Visibility`.
5. **099 (derived layers):** the grid-frequency excursions, carbon intensity and fuel mix corroborate the energy
   picture; `/v1/series` is the read surface for a derived `SeriesReader`.

Implementation commit: 17b7f29ef7eb2fec4fa57546785fb077efc22500
