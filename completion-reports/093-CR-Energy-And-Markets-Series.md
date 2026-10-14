# 093-CR-Energy-And-Markets-Series

| | |
|---|---|
| Prompt | 093-Energy-And-Markets-Series.md |
| Status | Complete |
| Implementation commit | 17b7f29ef7eb2fec4fa57546785fb077efc22500 |
| Date | 2026-10-14 |

## Summary

Energy and markets time series land as records **without geometry**, so the same versioning, triggers, briefings and
MCP tools apply to oil prices, US grid demand, EU gas storage, monthly generation mix, grid-frequency excursions and
GB carbon intensity. A revision becomes a new version; `series-latest` gives rules one entity per series.

**Shared framework (`ai.foresignal.source.series`).**
- `SeriesPoint` builds a `series-point` record (`externalId = {seriesId}:{period}`, no geometry, `eventTime =
  periodStart`) and a `series-latest` record (`{seriesId}:latest`), parses upstream numeric strings and rejects NaN,
  and provides the STANDARD/ALL descriptor defaults, the payload `fieldSpecs` and `latestPerSeries` (which skips
  forecasts, so a forecast-only series gets no latest record).
- `SeriesPeriods` maps an upstream period label (`YYYY-MM-DD`, `YYYY-MM-DDTHH`, `YYYY-MM`, ISO) plus a fixed
  frequency to start/end.
- `FrequencyExcursionDetector` (pure, shared) turns samples into `frequency-excursion` records (nominal 50 Hz;
  operational/statutory bands; severe below 49.2 Hz) and one `grid-frequency` state record per poll
  (`normal|low|high|no-data`), keeping the last processed sample time so a restart does not double-count.

**Sources built (all STANDARD/ALL, TTL none, `ttlAnchor=OBSERVED`, `titleField=seriesName`, retention 3650 d, the
`series` class).** `eia-petroleum`, `eia-930`, `ercot-public` (disabled by default), `gie-agsi`,
`ember-electricity`, `grid-frequency-gb`, `grid-frequency-fingrid` (disabled by default) and `gb-carbon-intensity`.
EIA sends its key as `api_key` in the URL (redacted by the upstream client); AGSI uses `x-key` and turns the HTTP-200
`access denied` body into a rejected credential; Fingrid uses `x-api-key`; ERCOT uses a cached Azure B2C token plus
`Ocp-Apim-Subscription-Key`.

**API / MCP / schema.** `GET /v1/series` (catalogue from `series-latest`) and
`GET /v1/series/{sourceId}/{seriesId}` (points in `periodStart` order, JSON or CSV), backed by
`SeriesQueryService` native SQL using the new partial expression index; MCP `get_series`; AccessPolicy `read` rules;
`docs/OpenAPI.json` statuses set to `implemented` plus a changelog entry; Postman regenerated (no diff).
`SchemaBootstrap` (via `SpatialIndexInitializer`) adds
`record_head (source_id, (payload->>'series'), event_time) WHERE kind='series-point'`.

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
- `source/adapters/eia/{EiaParserTest,EiaAdapterTest,SeriesIngestIT}.java`
- `source/adapters/ercot/{ErcotParserTest,ErcotAdapterTest}.java`
- `source/adapters/gie/{AgsiParserTest,AgsiAdapterTest}.java`
- `source/adapters/ember/{EmberParserTest,EmberAdapterTest}.java`
- `source/adapters/elexon/{ElexonFrequencyParserTest,GridFrequencyAdaptersTest}.java`
- `source/adapters/fingrid/FingridFrequencyParserTest.java`
- `source/adapters/carbon/{CarbonIntensityParserTest,CarbonIntensityAdapterTest}.java`
- fixtures `eia-petroleum/`, `eia-930/`, `gie-agsi/`, `ember-electricity/`, `gb-carbon-intensity/`,
  `grid-frequency-gb/`, `grid-frequency-fingrid/`, `ercot-public/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (eight 093 adapters classified)
- `testsupport/weather/WeatherIngestSupport.java` (093 base-URL properties and SOURCES)

**Docs**
- `docs/OpenAPI.json` (statuses + changelog), `docs/Source-Catalog.md`, `docs/Operator-Guide.md`
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
4. **ERCOT `settlementPoint` is a single-value query parameter**, so the adapter makes one request per hub (5 default)
   plus the load request (6/run; 576/day, under the 600/day budget). The B2C `client_id`
   `fec253ea-0d06-4272-a5e6-b478baeecd70` is ERCOT's published value, taken from its authentication guide.
5. **AGSI uses one `type=eu&size=30` request** and filters the returned rows to `countries` (the EU row is always
   kept); a fallback keeps every row when none match, so the source works whether AGSI returns the aggregate only or
   every country. This keeps the run inside the 40/day budget.
6. **Fixtures.** `gb-carbon-intensity` and `grid-frequency-gb` are real captures (2026-10-14). The keyed or
   authenticated sources (`eia-petroleum`, `eia-930`, `gie-agsi`, `ember-electricity`, `grid-frequency-fingrid`,
   `ercot-public`) have synthetic fixtures built from published docs/examples, marked `synthetic: true` with
   "recapture with a real key" in their READMEs (Conventions, "No credentials during construction").
7. **Forecast-only series get no `series-latest`.** As the prompt's `SeriesPointTest` requires ("skips forecasts"),
   EIA-930 `DF` and the carbon regional series (forecast only) are not in the catalogue; their points are still
   served by `/v1/series/{source}/{id}`.
8. **"Disabled by default" is the framework default.** Every built-in source seeds a disabled settings row
   (`SourceService.seedDefaults`), so `ercot-public` and `grid-frequency-fingrid` are disabled like all sources; no
   descriptor flag was added.

## Issues found / open questions

1. **`/v1/series` is not `@RestrictedRead`-clamped.** The catalog paths are not in the coverage test's
   `READ_PREFIXES`, so no annotation was required, but a restricted read token is not clamped on the new endpoints
   (a known gap to address if a later prompt adds series to the leak matrix). Recorded here rather than fixed, to
   stay in scope.
2. **AGSI `type=eu` semantics** are assumed (aggregate plus, or instead of, per-country rows); the adapter tolerates
   both. Confirm against a real key.
3. **ERCOT is unreachable from outside the US** (403 → a `geo-blocked` health reason) and needs a personally
   registered account; the adapter is unverified live.
4. **EIA over-use suspends the key** (429/403 → backoff), and the FAQ advises under ~9,000 requests/hour and under
   5/s; the descriptor budgets (4/min, 200/day each) sit well inside.
5. **Kalshi and Polymarket** are not built; their verified 2026-10 notes are recorded under "External provider
   ideas" (Kalshi's terms restrict storing data without written consent; Polymarket's gamma API is not blocked).
6. **Instructions in data:** none.

## Tests written (not run)

- `SeriesPointTest` (externalId, keys, units, numeric-string parsing/NaN rejection, `series-latest` skipping
  forecasts, `SeriesPeriods`).
- `FrequencyExcursionDetectorTest` (in-band, short excursion, statutory/severe, spanning two polls, restart without
  double-counting, stale → `no-data`, state round-trip).
- Parser contracts with fixtures: `EiaParserTest` (string values, `warnings` incomplete → error, `value-units`),
  `ErcotParserTest` (column mapping by `fields`), `AgsiParserTest` (status N skipped, HTTP-200 error body),
  `EmberParserTest`, `ElexonFrequencyParserTest`, `FingridFrequencyParserTest`,
  `CarbonIntensityParserTest` (regional without `actual`).
- WireMock adapter tests: `EiaAdapterTest` (`api_key` redacted in traces/recorded exchanges, incomplete error,
  missing key, Retry-After backoff, eia-930 DF forecast), `AgsiAdapterTest` (`x-key`, countries, access-denied),
  `EmberAdapterTest` (options short-circuit), `CarbonIntensityAdapterTest` (endpoints, pt24h once an hour),
  `GridFrequencyAdaptersTest` (Elexon no key, Fingrid `x-api-key`, Retry-After), `ErcotAdapterTest` (token fetched
  once and reused for 55 min, 401 re-fetch).
- `SeriesIngestIT` (`@IntegrationTest`): a revised EIA value is version 2 and moves `series-latest`; an unchanged
  re-poll adds no version; `/v1/series` lists the catalogue; `/v1/series/eia-petroleum/eia:RWTC` returns points in
  period order and as CSV; an unknown series is 404.

## Compile result

- `./gradlew compileJava compileTestJava`: **BUILD SUCCESSFUL**.
- `docs/OpenAPI.json`: valid JSON; `listSeries`/`getSeriesPoints` set to `implemented`.

## Notes for the next prompt

1. **097 checkpoint:** first run of the 093 tests. `WeatherIngestSupport` gained the 093 base-URL properties and
   SOURCES entries; `FamilyDefaultsTest` gained eight entries (six `series`, two `event`); `pending-operations.txt`
   lost `listSeries` and `getSeriesPoints`.
2. **143:** recapture the synthetic fixtures (`eia-petroleum`, `eia-930`, `gie-agsi`, `ember-electricity`,
   `grid-frequency-fingrid`, `ercot-public`) with real keys, per their READMEs.
3. **144 (Flyway):** no schema change; the new DDL is one `CREATE INDEX IF NOT EXISTS` in
   `SpatialIndexInitializer.STATEMENTS` (the partial expression index), which moves into the baseline with the rest.
   New adapter state: `freq:{grid}` (the detector state), `ember:month:{dataset}`, `carbon:pt24h:hour`.
4. **117 (markets and attention):** reuses the `series-point`/`series-latest` model and the `/v1/series` endpoints;
   its `SeriesInfo` extension (`x-foresignal-extended-by: 117`) is additive.
5. **099 (derived layers):** the grid-frequency excursions, carbon intensity and fuel mix corroborate the energy
   picture; `/v1/series` is the read surface for a derived `SeriesReader`.

Implementation commit: 17b7f29ef7eb2fec4fa57546785fb077efc22500
