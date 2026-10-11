# 118 — Change Points And Event Studies

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/118-CR-Change-Points-And-Event-Studies.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/117-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2, §3 (DERIVED sources), §4.2 (region rollups), §4.3 (series), §5.1–§5.4 and §5.6 (findings, change points, event studies, co-occurrence, series registry), §12 (libraries)
- `docs/Core-Data-Model.md` §8 (`seq_clock`, `S(T)`), §13 (consumer clamp)
- Completion Reports of 038 (`activity_rollup`, RollupJob, stats API), 040 (region tags), 052 (DERIVED runtime), 088 (PortWatch), 093 (series), 100 (`activity_sample`, slot baseline, CoverageGate), 101 (`signal_family`, `elaro-intel`), 103 (job framework, caveat generator, Wilson interval), 116 (series metadata, topics), 117 (situations)

## Objective

Build the discovery layer's foundations and first three detectors. Foundations: region rollups, one `SeriesReader` over series, rollups and rule samples, an analysis-series registry, a shared signal-family registry, the `analytic_finding` table with the `elaro-analytics` DERIVED source, and a small statistics package. Detectors: change points (CUSUM and BOCPD), event studies (batch, live pre-event signals, unexplained moves) and co-occurrence mining with false-discovery control. Every finding states how often something this strong happens by chance.

## Principles (binding)

- Pure, versioned detectors; reproducible from stored data, a definition and `asOfSeq`.
- **No look-ahead:** live detection uses only series points with `knownAt` ≤ decision time; batch studies use `asOf` replays.
- **Every finding carries a base rate** (its detector's false-alarm rate on the same series, or the placebo distribution) and its caveats.
- **Coverage-aware:** buckets with `coverageOk=false` are excluded; a candidate spanning a coverage gap becomes `unknown`, never a "drop".
- **`LOCAL_ONLY` series:** findings over them carry standardised statistics (z, CAR, p) but not raw values, except for callers allowed local egress (062).

## Tasks

### 1. Region rollups (extends 038)

- Add resolutions 2 (region-hour) and 3 (region-day) to `activity_rollup`, with a nullable `region_code` text column and `cell = stableHash64(region_code)` for those rows. Region = the record head's 040 country code, or `CC-admin1` when the source's setting `statsRegionLevel=admin1`. Records without a region tag go to region `ZZ`.
- `RegionRollupJob` mirrors 038's `RollupJob` (durable cursor `stats.region.cursor` in `app_setting`, closed buckets only, ≤ 6 h catch-up per run, days computed from raw rows, never by summing hours) and joins 019's consumer clamp. First start backfills from the oldest retained seq (bounded by `elaro.stats.backfill`).
- Retention: `RetentionSettings.stats.regionHourly` 1095 d, `regionDaily` 3650 d.
- `/v1/stats/series` and `/v1/stats/top` accept `groupBy=region` and `region=` filters.

### 2. Signal families (`ai.elaro.analytics.family`)

- `src/main/resources/intel/signal-families.json`: rules mapping `(sourceId | kind | intel event type | finding type | anomaly rule tag)` → family. Families: `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`, `sanctions`, `health`, `space`. 101's `signal_family` strings must resolve through this registry; reconcile and record any renames in the CR.
- `SignalFamilyRegistry.familyOf(...)`, operator extensions through `intel.signalFamilies` (settings).

### 3. Series access (`ai.elaro.analytics.series`)

- `SeriesRef` parse/format: `series:{sourceId}/{seriesId}`, `rollup:{hour|day}/{sourceId}/{kind}/{region|cell:h3}`, `rule:{ruleId}/{groupKey}`.
- `SeriesReader.read(ref, from, to, asOf)` → regular points `{periodStart, value, knownAt, coverageOk}` with explicit gaps. `knownAt` = ingest time of the supplying version (via `seq_clock`); rollups use bucket close time; rule samples use their write time. Coverage for rollups comes from the involved source's `fetch_log` per bucket (the 100 CoverageGate logic, reused); series points are `coverageOk=true` unless the source was failing over the period.
- `GET /v1/analytics/series/data?ref&from&to&asOf` (read; LOCAL_ONLY values withheld per the principle).

### 4. Analysis series registry

- `analysis_series`: `ref` (PK), `enabled`, `detectors` text[] (`changepoint-cusum`, `changepoint-bocpd`, `forecast` later), `frequency` (hour | day | week), `family`, `region_code`, `watch_pairs` jsonb (event-study pairs, §7), `origin` (auto | operator), `last_run_at`, `last_error`.
- Nightly auto-registration (02:00 UTC): region-day rollups with ≥ 30 non-zero days in the last 90; all `series-point` series from 093 and 116; PortWatch chokepoint series (088); anomaly rule groups (100) with ≥ 8 weeks of samples. Cap `analysis.series.max` (2,000), keeping the highest-volume refs. Operator rows are never removed automatically.
- API: `GET /v1/analytics/series?family&region&enabled&cursor` (read), `PUT /v1/analytics/series` (agent; upsert by ref), `DELETE /v1/analytics/series?ref=` (agent).

### 5. Statistics package (`ai.elaro.analytics.math`)

Dependencies: Apache Commons Statistics (distributions, inference; verify the current 1.x on Maven Central), Commons Numbers, Commons RNG (deterministic seeded sampling). No GPL libraries. Implement and golden-test:
- robust location and scale (median, MAD × 1.4826 with a floor), Anscombe transform `2√(x + 3/8)`;
- Poisson and negative binomial tails (method-of-moments NB as in 100), Fisher's combination of p-values;
- CUSUM: Poisson/NB form with `k = (μ₁ − μ₀) / ln(μ₁/μ₀)` and time-varying μ₀; standardised Gaussian form; `h` chosen by simulation for a target in-control ARL (seeded, cached per series and parameter set);
- BOCPD with Poisson–Gamma and Normal–Gamma conjugate models, constant hazard, run-length truncation;
- Benjamini–Hochberg and Benjamini–Yekutieli;
- Corrado rank test; deterministic placebo date sampler;
- reuse 103's Wilson interval (move it here if it lives in `backtest`, keeping a delegate).

### 6. Findings (`ai.elaro.analytics.finding`)

- `analytic_finding` as in Intelligence-Design §5.1 (all columns), plus `dedup_key`, `superseded_by`, `version`. Indexes: `(finding_type, detected_at DESC)`, GIN(`region_codes`), GiST(`geom`).
- **DERIVED source `elaro-analytics`** (052 runtime): one `analytic-finding` record per finding (`externalId` = finding id; payload `findingType`, `severity`, `title`, `regionCodes`, `seriesRefs`, key stats, `baseRate`, `automated: true`), emitted on create and on change; tombstoned on dismiss or supersede. Analytic class `intel`.
- Severity: `significant` when the detector's p (or placebo p) ≤ 0.001 and the effect passes its threshold; `notable` when ≤ 0.01; otherwise `info` (stored, not published as a record unless `analysis.findings.publishInfo=true`).
- Dedup: a new finding of the same type, series and overlapping window supersedes the older one.
- Offer `notable`+ findings to the 117 linker (the linker reads `analytic-finding` records from the ChangeBus).
- API: `GET /v1/analytics/findings?type&severity&region&ref&since&situationId&cursor` (read), `GET /v1/analytics/findings/{id}` (read; evidence / inference / caveats sections), `PATCH /v1/analytics/findings/{id}` (agent; dismiss with reason).
- SSE topic `analytics`: `finding.created|updated|dismissed`. Notification event type `finding.significant` (default off).

### 7. Detectors

**7.1 Change points** (Intelligence-Design §5.2):
- Daily series run nightly at 03:00 UTC on the last closed day; hourly series (opt-in) run at :20 each hour. Each run reads the series through `SeriesReader` with `asOf` = run start.
- `changepoint-cusum` 1.0.0 and `changepoint-bocpd` 1.0.0 with the documented defaults (`minRelShift` 0.5, target ARL 365 buckets, `minPersistence` 3, hazard 1/250, truncation 300). A CUSUM candidate confirmed by BOCPD posterior ≥ 0.8 is `significant`; CUSUM alone is `notable`.
- Base rate: the number of alarms the same settings produce over the series' own retained history, per year.
- Output `change-point` findings with `stats {changeAt, levelBefore, levelAfter, effect, detectionDelay, bocpdPosterior}`.

**7.2 Event studies** (Intelligence-Design §5.3):
- `EventStudyEngine` (`event-study` 1.0.0): event selector (103 selector types, plus `situationActivated`, `changePoint` by series family, explicit timestamps), target `SeriesRef`, transform by series `family` (116 metadata), windows by frequency (daily: estimation `[−250, −11]` with minimum 60 points; pre-event `[−5, −1]`; event `[0, +1]`; weekly: `[−52, −3]`, `[−2, −1]`, `[0, +1]`), CAR, CAAR with clustered overlapping events, standardised test, Corrado rank test, and placebo test (500 deterministic pseudo-events from periods without qualifying events; seed = hash of definition and range).
- **Batch:** `POST /v1/analytics/event-studies` (agent) submits a 103 job with `type=event-study`; result: per-event abnormal paths, CAAR per window, test statistics, placebo distribution summary, caveats (103's plus `FEW_EVENTS`, `LAGGED_PUBLICATION`, `DAILY_RESOLUTION_ONLY` for market families).
- **Live `pre-event-signal`:** on a qualifying event (intel event ≥ warning becoming active, situation activated, significant change point), evaluate the `watch_pairs` whose event family matches, over the pre-event window, using only points with `knownAt` ≤ the event's `observedAt`. Placebo p ≤ 0.01 and |CAR| ≥ `minEffect` (2.0 SD) → finding. When the pre-event window is not yet fully published (weekly series with a lag), schedule a re-check after `releaseLagDays` and mark `LAGGED_PUBLICATION`.
- **Live `unexplained-move`:** daily at 04:00 UTC, for every watched series of family price, volume, open-interest, position, attention or tone: a move with |z| ≥ 3 against the estimation window and no qualifying event in the series' linked regions, themes or topic within ±2 days → `unexplained-move` finding. A qualifying event within the next 7 days links to the finding (`stats.linkedEventId`, `stats.leadDays`) and creates a `pre-event-signal` finding that references it.
- **Default watch pairs** (in `src/main/resources/intel/watch-pairs.json`, loaded into the registry, operator-editable): conflict family (Middle East and Gulf regions) × {COT WTI `mm_net`, FRED `DCOILWTICO`, EIA WTI and Brent spot, the linked topics' GDELT volume and tone and Wikipedia views}; maritime family × {PortWatch chokepoint transits, COT WTI `mm_net`}; outage and infrastructure families × {the region's GDELT volume}. Missing series are skipped with a warning.

**7.3 Co-occurrence** (Intelligence-Design §5.4):
- `CoOccurrenceMiner` (`cooccurrence` 1.0.0), daily at 05:00 UTC over region-day rollups grouped by signal family: per family and region an NB exceedance p-value against its day-of-week baseline (8–12 weeks, coverage-ok days); for each region and family pair with both p ≤ 0.05, Fisher's combined p; BH at q = 0.05 across all tests of the day. Expected joint rate from each family's historical exceedance frequency; lift over the trailing year in `stats`.
- Survivors → `co-occurrence` findings (`notable`; `significant` when BH-adjusted p ≤ 0.001 and lift ≥ 3).

### 8. MCP, spec, docs

- MCP: `list_findings`, `get_finding`, `get_series_data`, `list_analysis_series` (read; added to the read catalogue); `run_event_study`, `upsert_analysis_series` (agent).
- `docs/OpenAPI.json`: `SeriesRef`, `SeriesPoint`, `AnalysisSeries`, `AnalyticFinding`, `FindingPage`, `EventStudyRequest`, `EventStudyResult`, stats region parameters; tag `Analytics`; `x-elaro-scope` everywhere; SSE schemas. Regenerate Postman with BODIES for upsert series, event study and dismiss finding.
- AccessPolicy rows as listed. Problem codes: `UNKNOWN_FINDING` (404), `SERIES_REF_INVALID` (400), `SERIES_NOT_FOUND` (404, reuse 093's), `EVENT_STUDY_INVALID` (400).
- `docs/Operator-Guide.md` "Discovery": what each detector does in plain words, how to read base rates and placebo p-values, `DAILY_RESOLUTION_ONLY`, watch pairs, tuning and cost.
- `docs/Agent-Guide.md`: always quote a finding's base rate and caveats; never call a pre-event signal evidence of wrongdoing.

## Tests to write (do not run)

- Statistics golden tests: MAD, Anscombe, NB tail, Fisher, BH/BY against reference values, Corrado on a planted shift, BOCPD posterior on a planted change, CUSUM ARL simulation determinism.
- `RegionRollupJobIT`: hour and day rows by region, admin1 opt-in, `ZZ` for untagged, cursor restart exactly-once, retention step.
- `SeriesReaderIT`: each ref type; `knownAt` respected under `asOf`; gaps and coverage flags.
- `AnalysisSeriesRegistryIT`: auto-registration thresholds and cap; operator rows kept.
- `ChangePointDetectorTest`: planted step detected with the right delay; no alarm on a stationary seasonal series (fixed seed); coverage gap → unknown; BOCPD confirmation upgrades severity.
- `EventStudyEngineTest`: planted pre-event abnormality found with low placebo p; no look-ahead (a later point changes nothing); weekly windows; clustered overlapping events.
- `LiveEventStudyIT`: a seeded intel event triggers watch pairs and a `pre-event-signal`; lagged weekly data schedules a re-check; an unexplained move later linked to an event.
- `CoOccurrenceMinerTest`: planted joint exceedance survives BH; independent noise does not (fixed seed).
- `FindingServiceIT`: dedup and supersede, `elaro-analytics` record emitted only on change, dismiss tombstones, `LOCAL_ONLY` values withheld from non-local callers, SSE topic.
- `AnalyticsControllersIT`: endpoints, scopes, MCP tool shapes.

## Acceptance criteria

- Region rollups, the series reader and the registry give every detector one consistent, coverage-aware view of series.
- Change points, event studies (batch and live) and co-occurrence findings are reproducible, versioned and carry base rates and caveats.
- Live detection never uses data that was not yet known.
- Findings are records of `elaro-analytics`, so ordinary rules can alert on them, and the situation linker can attach them.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `118: Change Points And Event Studies`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/118-CR-Change-Points-And-Event-Studies.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `118: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
