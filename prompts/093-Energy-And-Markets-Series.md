# 093 — Energy And Markets Series

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/093-CR-Energy-And-Markets-Series.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/092-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `eia-petroleum` / `gie-agsi` / `ember-electricity` ("records without geometry, `kind=series-point`, series id plus period as `externalId`") and `polymarket` / `kalshi`
- `docs/Gap-Analysis.md` §4b row "Grid ops (EIA-930), carbon intensity (GB)"; §8 item 51 (labelled 055 = this prompt)
- `docs/Core-Data-Model.md` §2 (row "Measurements (OpenAQ, NDBC, EIA)": `eventTime` = measurement period, `observedAt` = measurement time, `ttlAnchor=OBSERVED`), §3, §5.3 ("Everything else": STANDARD, ALL), §13 (STANDARD retention, head purge)
- `docs/Adapter-Guide.md`; the 008 Completion Report (`fieldSpecs` with UCUM units, `titleField`)
- Upstream docs: EIA `https://www.eia.gov/opendata/documentation.php` and terms `…/opendata/terms-of-service.php`; GIE API manual v013 `https://www.gie.eu/transparency-platform/GIE_API_documentation_v013.pdf` and `https://agsi.gie.eu/data-usage`; Ember `https://api.ember-energy.org/v1/docs`; Elexon Insights `https://data.elexon.co.uk/bmrs/api/v1` and its BMRS licence; Fingrid `https://data.fingrid.fi/en/instructions`; Carbon Intensity `https://carbon-intensity.github.io/api-definitions/`; ERCOT `https://developer.ercot.com/applications/pubapi/` (registration, known limits) and `https://www.ercot.com/help/terms`

## Objective

Bring energy time series into Foresignal as records without geometry, so the same versioning, triggers, briefings and MCP tools apply to oil prices, US grid demand, EU gas storage, monthly generation mix, grid-frequency excursions and GB carbon intensity. Each data point is a record (`kind=series-point`, `externalId = {seriesId}:{period}`), and an upstream revision becomes a new version. Prediction markets are evaluated and excluded on terms (§8).

## Tasks

### 1. Series framework (`ai.foresignal.source.series`)

- **`SeriesPoint` builder** producing a `NormalizedRecord`:
  - `kind = series-point`, `externalId = {seriesId}:{period}`, geometry null;
  - payload `series, seriesName, dataset, frequency` (`PT15S|PT30M|PT1H|P1D|P1W|P1M`), `period` (upstream label), `periodStart`, `periodEnd` (ISO), `value` (number; parse upstream numeric strings, reject NaN), `unit` (UCUM, e.g. `[bbl_us]`, `MW.h`, `TW.h`, `%`, `g/(kW.h)`, `Hz`), `area`, optional `extra` (flat scalars ≤ 16 keys), `status` (`final|preliminary|estimated|forecast`);
  - `eventTime = periodStart`.
- **Time base (fixed per source):** sources without forecasts use `observedAtMode=UPSTREAM` with `observedAt = periodEnd`. Sources that mix forecasts and actuals (EIA-930 `DF`, carbon intensity) use `observedAtMode=RECEIVED`. Never switch per record.
- **Descriptor defaults:** STANDARD, ALL, no TTL, `ttlAnchor=OBSERVED`, `titleField = seriesName`, `fieldSpecs` for every payload key, `searchFields = [seriesName, area]`. `defaultRetention`: 90 d for sub-daily sources, 730 d for daily, weekly and monthly sources.
- **Trailing-window re-fetch:** each poll re-reads a trailing window (per source below) so revisions become `CHANGE` or `SIGNIFICANT` versions and unchanged points become throttled touches.
- **`series-latest` records:** for every series, the adapter also emits `externalId = {seriesId}:latest`, kind `series-latest`, carrying the newest non-forecast point (`period`, `value`, `unit`, `status`), so edge rules (`value > x`, enter/exit) work on one entity per series.
- **Index:** `SchemaBootstrap` adds a partial expression index on `record_head (source_id, (payload->>'series'), event_time) WHERE kind = 'series-point'`.

### 2. EIA (`eia-petroleum`, `eia-930`)

- Credential `apiKey`, sent as the `api_key` query parameter (EIA requires it in the URL; verify the 005 redaction masks it in `FetchTrace` and logs). `licence = "US-PD"`, attribution "Source: U.S. Energy Information Administration". The petroleum spot prices are sourced by EIA from Refinitiv (LSEG); record `licenceNote` "spot prices sourced from Refinitiv via EIA; third-party rights may apply".
- Response (verified 2026-10, API 2.1.14): `response.{total (string), dateFormat, frequency, data[], warnings[]}`; `value` is a **string**. A `warnings[]` entry "incomplete return" means the 5,000-row cap was hit: request fewer rows, never accept a silently truncated window.
- Limits: 5,000 rows per JSON request; the FAQ advises under about 9,000 requests/hour and under 5/s. Over-use suspends the key temporarily, so treat 429 and 403 suspension responses as backoff.
- **`eia-petroleum`:** param `spotSeries` (default `RWTC, RBRTE`, from `petroleum/pri/spt`, daily rows, released weekly), `stockSeries` (default `WCESTUS1`, from `petroleum/stoc/wstk`, weekly WPSR on Wednesdays). Before coding, confirm each default id against the route metadata (the route without `/data`). Requests: `…/data/?frequency=daily&data[0]=value&facets[series][]=…&sort[0][column]=period&sort[0][direction]=desc&length=60`. Interval 1 h (min 30 min), budget 4/min and 200/day, trailing 60 rows.
- **`eia-930`:** `electricity/rto/region-data/data/?frequency=hourly&data[0]=value&facets[respondent][]=…&facets[type][]=D|DF|NG|TI` and, when `fuelMix=true` (default), `electricity/rto/fuel-type-data/data/` with `facets[fueltype][]`. Param `respondents` (default `US48, ERCO, CISO, PJM, MISO, NYIS, ISNE, SWPP`). Period `YYYY-MM-DDTHH` (UTC); units field `value-units`. Demand lags 1–2 h; NG, interchange and fuel mix up to a day; recent hours are revised. Interval 30 min (min 15 min), trailing 48 h, `start` set explicitly, one request per dataset per poll (split by respondent when a window would exceed 5,000 rows). Series ids `eia930:{respondent}:{type}` and `eia930:{respondent}:fuel:{fueltype}`; DF rows have `status=forecast`.

### 3. `ercot-public` (optional, disabled by default)

- Public API base `https://api.ercot.com/api/public-reports` (spec `github.com/ercot/api-specs`). Verified 2026-10: needs a registered account at `apiexplorer.ercot.com`, header `Ocp-Apim-Subscription-Key`, **and** `Authorization: Bearer <id_token>` from the Azure B2C ROPC flow (`POST https://ercotb2c.b2clogin.com/ercotb2c.onmicrosoft.com/B2C_1_PUBAPI-ROPC-FLOW/oauth2/v2.0/token`, `grant_type=password`, `response_type=id_token`, the documented public `client_id` and scope). The token lasts 1 h and cannot be refreshed: cache it in memory for 55 min, never persist it, and re-POST on 401.
- Credentials `username`, `password`, `subscriptionKey` (all required). The operator registers the account personally (ERCOT's terms forbid bot-registered accounts). Limit 30 requests/min: descriptor budget 10/min and 600/day. Access from outside the US is blocked; surface that as a health reason on 403.
- Artifacts: `/np6-345-cd/act_sys_load_by_wzn` (hourly actual load by weather zone, series `ercot:load:{zone}`) and `/np6-905-cd/spp_node_zone_hub` filtered to hubs (param `hubs`, default `HB_HOUSTON, HB_NORTH, HB_SOUTH, HB_WEST, HB_BUSAVG`; 15-min settlement prices, series `ercot:spp:{hub}`, unit `USD/(MW.h)`). Envelope `{_meta, fields[], data[][]}`: map columns by `fields[].name`, never by position. Interval 15 min, trailing 2 operating days.
- **Never** poll `www.ercot.com/api/1/services/read/dashboards/*.json`: it sits behind bot protection (403 to non-browser clients) and website terms §6 forbid load-impacting use. Docs recommend `eia-930` respondent `ERCO` for keyless ERCOT system load.

### 4. `gie-agsi`

- `GET https://agsi.gie.eu/api?type=eu&size=30` plus `?country={cc}&size=30` for param `countries` (default `DE, FR, IT, NL, AT`), header `x-key` (credential `apiKey`). A missing or invalid key returns **HTTP 200** with `{"error":"access denied",…}`: check the body and fail the fetch with an auth error. `size` max 300.
- Published at 19:30 CET with a second run at 23:00 for the previous gas day (verified 2026-10; the 18:00 time in older notes is wrong). Interval 3 h (min 1 h), budget 2/min and 40/day (limit 60/min; repeat over-use can blacklist the account).
- One point per area and gas day: `seriesId = agsi:{area}`, `value = full` (%), `extra` `gasInStorageTWh, injectionGWhD, withdrawalGWhD, netWithdrawalGWhD, workingGasVolumeTWh, trend, consumptionFull`, `status` from `E|C|N` (estimated, confirmed, no data; skip N). Numbers arrive as strings; `updatedAt` is volatile.
- `licence = "GIE AGSI data usage rules (any use incl. commercial, mandatory credit)"`, attribution "GIE AGSI".

### 5. `ember-electricity`

- `GET https://api.ember-energy.org/v1/electricity-generation/monthly?entity_code={codes}&start_date={YYYY-MM}&api_key=…` (credential `apiKey`; 403 without it). Check `GET /v1/options/electricity-generation/monthly/date` first and skip the data call when the newest month is unchanged (adapter state). Param `entities` (default `USA, GBR, DEU, FRA, CHN, IND`), `datasets` (default `electricity-generation`; optional `carbon-intensity`, `electricity-demand`).
- Rows `entity, entity_code, date, series, generation_twh, share_of_generation_pct`: series id `ember:{entity_code}:{series}`, `value = generation_twh`, `extra.sharePct`. Trailing 12 months. Interval 24 h, budget 2/min and 20/day (no published limit; `stats.rate_limit` is logged). Updated twice a month.
- `licence = "CC BY 4.0"`, attribution "Ember". The bulk CSV moved to `files.ember-energy.org` and changed shape in July 2026; use the API.

### 6. Grid-frequency excursions

- **`grid-frequency-gb`:** `GET https://data.elexon.co.uk/bmrs/api/v1/system/frequency?from={lastSample}&to={now}` (no key; 15-s samples about 2 min behind, verified 2026-10). Interval 2 min (min 1 min), budget 2/min and 1,500/day. Attribution "Contains BMRS data © Elexon Limited copyright and database right {year}", `licence = "BMRS Data Licence"`.
- **`grid-frequency-fingrid`** (optional): `GET https://data.fingrid.fi/api/datasets/177/data?startTime&endTime&pageSize=20000`, header `x-api-key` (credential); updated every 3 min; limits 10,000/day and one request per 2 s. Interval 3 min, budget 1/min and 600/day. `licence = "CC BY 4.0"`, attribution "Fingrid".
- `FrequencyExcursionDetector` (pure, shared): nominal 50 Hz; params `operationalBandHz` (GB 0.2, Nordic 0.1) and `statutoryBandHz` (GB 0.5). Records kind `frequency-excursion`, `externalId = {grid}:{startEpoch}`, `eventTime = start`, payload `start, end, durationS, extremeHz, direction` (low/high), `severity` (`operational`, `statutory`, `severe` below 49.2 Hz), `open`. Emit when a run of samples leaves the operational band; update on close. Keep the last processed sample time in adapter state so restarts do not double-count. A `grid-frequency` record `{grid}:state` carries a banded `state` (`normal|low|high|no-data`) for rules; `no-data` when the newest sample is older than 10 min (never report "normal" from stale data).
- NESO's 1-s "System Frequency" data is published only as monthly CSVs; it is not used for live polling.

### 7. `gb-carbon-intensity`

- No auth; `GET https://api.carbonintensity.org.uk/intensity` (national half-hour: `intensity.forecast, actual, index`), `/regional` (per `regionid`: forecast and index only, no `actual`), `/generation` (`generationmix[].{fuel, perc}`). Interval 30 min (min 15 min), budget 3/min and 300/day.
- Series `carbon:gb:national` (`value` = actual when present, else forecast with `status=forecast`; `extra.forecast`, `extra.index`), `carbon:gb:region:{regionid}` (forecast, `extra.shortname`), `genmix:gb` (`extra` = fuel → percent). `observedAtMode=RECEIVED`. Re-read `/intensity/pt24h` once per hour to pick up late actuals.
- `licence = "CC BY 4.0"`, attribution "Carbon Intensity API (NESO, EDF, University of Oxford, WWF)".

### 8. Prediction markets: excluded

- **Kalshi:** market-data reads work without auth (verified 2026-10; fields are now dollar and fixed-point strings such as `yes_bid_dollars`, `volume_fp`), but the Developer Agreement limits API use to a member's own trading and forbids collecting, caching or storing the data, and the Data Terms forbid cached or archived datasets without written consent. Storing versions is the core of Foresignal, so **do not implement an adapter**.
- **Polymarket:** the gamma API is not technically blocked today (contrary to the catalogue's JA3 note); exclude it on terms and product grounds (Foresignal is not a trading tool).
- Record both in `docs/Source-Catalog.md` under "Excluded", with the verification date and "reconsider only with written permission".

### 9. API, MCP, spec, docs

- `GET /v1/series` (read): series catalogue from `series-latest` records (`sourceId, series, seriesName, unit, frequency, latestPeriod, latestValue, status`), filters `source`, `q`.
- `GET /v1/series/{sourceId}/{seriesId}` (read): points ordered by `periodStart`, params `from`, `to`, `limit` ≤ 5,000, `format=json|csv`, using the §1 index; 404 `SERIES_NOT_FOUND`.
- MCP `get_series` (read; compact output, `limit` ≤ 500, `*_total`, `truncated`) in the 025 registry.
- `docs/OpenAPI.json`: both operations with `x-foresignal-scope: read`, schemas `SeriesInfo`, `SeriesPage`, `SeriesPoint`; AccessPolicy rule; regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Source-Catalog.md` rows for every source (tier, policy, retention, `observedAtMode`, licence, the verified 2026-10 differences); `docs/Operator-Guide.md` (keys, ERCOT registration and US-only access, rule recipes: `series-latest` `value > x` enter; `frequency-excursion` `severity in [statutory, severe]` match with `only_new`).

## Tests to write (do not run)

- `SeriesPointTest`: externalId, units, numeric-string parsing, NaN rejection, time base per mode, `series-latest` selection skips forecasts.
- Parser contract tests with real trimmed fixtures (README with URL and capture date): `EiaParserTest` (string values, `warnings` incomplete return → error, `value-units`), `ErcotParserTest` (column mapping by `fields`), `AgsiParserTest` (status N skipped, HTTP-200 error body), `EmberParserTest`, `ElexonFrequencyParserTest`, `FingridParserTest`, `CarbonIntensityParserTest` (regional without `actual`).
- `FrequencyExcursionDetectorTest`: in-band, short excursion, statutory and severe, excursion spanning two polls, restart without double counting, stale data → `no-data`.
- WireMock adapter tests: `api_key` redacted in traces, `x-key`/`x-api-key` headers, ERCOT token fetched once and reused for 55 min then re-fetched on 401, Ember options short-circuit, Retry-After honoured.
- `SeriesIngestIT` (`@IntegrationTest`): a revised EIA value creates version 2 and updates `series-latest`; an unchanged re-poll creates no versions; `/v1/series/{source}/{id}` returns points in period order and as CSV.

## Acceptance criteria

- All included sources register with STANDARD/ALL defaults that pass `IngestPolicyValidator`; `ercot-public` and `grid-frequency-fingrid` are disabled by default.
- No adapter exists for Kalshi or Polymarket, and the catalogue records why.
- `series-point` records carry no geometry, UCUM units and `fieldSpecs` for every key.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `093: Energy And Markets Series`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/093-CR-Energy-And-Markets-Series.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `093: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
