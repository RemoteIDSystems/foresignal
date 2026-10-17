# 119-CR-Change-Points-And-Event-Studies

| | |
|---|---|
| Prompt | 119-Change-Points-And-Event-Studies.md |
| Status | Complete (tests written, not run; owner decisions under Issues) |
| Implementation commit | `9493f2eb62ac8fedd76521f5f08ba40e1b12c5a1` |
| Date | 2026-10-17 |

## Summary

The discovery layer's foundations and its first three detectors. Region rollups, one series reader over series,
rollups and rule samples, an analysis-series registry, a shared signal-family registry, the `analytic_finding` table with
the DERIVED source `foresignal-analytics`, and a statistics package. Detectors: change points (CUSUM and BOCPD), event
studies (batch, live pre-event signals, unexplained moves) and co-occurrence mining with false-discovery control. Every
finding stores a base rate, caveats, its detector `{name, version, params}` and a 116 manifest. Compiles; tests written,
not run.

- **Dependencies (verified on Maven Central 2026-10-17, Apache-2.0):** `commons-statistics-distribution` and
  `-inference` 1.3, `commons-numbers-gamma` 1.3, `commons-rng-simple` and `-sampling` 1.7. They are recorded in
  `gradle/libs.versions.toml` and `THIRD_PARTY_NOTICES.md`. No GPL library and no Commons Math 3.
- **Region rollups (`ai.foresignal.stats`):**
  - `activity_rollup` gains a nullable `region_code`. Resolution 2 is region-hour and resolution 3 is region-day. On
    region rows `cell = stableHash64(region_code)` (the first eight bytes of its SHA-256).
  - `RegionRollupComputer` assigns each record its 040 tag from `record_region`: the country, `CC-admin1` for sources
    listed in the app setting `stats.regionLevel`, or `ZZ` when untagged. It writes per-source rows plus one
    cross-source `*` row per (kind, region). The `*` row covers non-private sources only and counts each 116 canonical
    event once.
  - `RegionRollupJob` keeps the durable cursor `stats.region.cursor`. It rolls closed buckets only, using
    `RollupJob.closed`, and waits until the running 040 tagger and 116 resolver have passed the bucket. Days are computed
    from raw rows. A run catches up at most 6 h, and the first start backfills from the oldest version within
    `foresignal.stats.backfill`. `RegionRollupCursorConsumer` registers the cursor with the consumer clamp.
  - Retention: `RetentionSettings.stats.regionHourly` (1095 d) and `regionDaily` (3650 d) are now stored, and the
    `StatsRollupRetentionStep` deletes by them.
  - `/v1/stats/timeseries` (`groupBy=region`, `region=`) and `/v1/stats/top` (`by=region`, `region=`) are served from
    region rows by `RegionStatsService`.
- **Signal families:** `intel/signal-families.json` holds the 17 families, aliases and 135 ordered match rules (source,
  kind, intel event type, finding type, rule tag). `SignalFamilyRegistry` tries operator rows from
  `intel.signalFamilies` first. Every 101 `signal_family` string resolves (renames under Deviations).
- **Series access (`ai.foresignal.analytics.series`):**
  - `SeriesRef` parses and formats `series:`, `rollup:` (region, `cell:h3` or `*`) and `rule:` references; anything
    else is `SERIES_REF_INVALID`.
  - `SeriesReader` returns regular points `{periodStart, value, knownAt, coverageOk}` with explicit gaps:
    - series points: the newest version with `ingested_at ≤ asOf`, whose ingest time is its `knownAt`;
    - rollups: a covered empty bucket is 0, `knownAt` is the bucket close, and coverage is the 100 `CoverageGate`
      (`evaluateAsOf`) per bucket, cached;
    - rule samples: summed to hours, `knownAt` is the bucket end.
  - `GET /v1/analytics/series/data` withholds raw values where `EgressPolicy.decide` refuses a source on the path.
- **Registry (`ai.foresignal.analytics.registry`):**
  - `analysis_series` has every column of the prompt plus `volume`, `created_at` and `updated_at`.
  - `AnalysisSeriesService` handles list (keyset on `ref`), upsert (always an operator row; 201 or 200) and delete.
  - `AnalysisSeriesRegistrar` runs nightly. Candidates are region-day `*` rollups with 30 non-zero days in 90, daily,
    weekly or hourly `series-point` series, PortWatch chokepoints, and anomaly rule groups with 8 weeks of samples. It
    caps automatic rows at `analysis.series.max` minus the operator rows, busiest first, with default watch-pair series
    first. Operator rows are never touched.
  - Default watch pairs come from `intel/watch-pairs.json`. Missing series are skipped with a warning.
- **Statistics (`ai.foresignal.analytics.math`):**
  - `RobustStats`: median, MAD × 1.4826 with a floor, Anscombe transform, quantiles.
  - `CountTails`: Poisson tails, method-of-moments negative-binomial tails through the regularized beta, and Fisher's
    combination.
  - `Cusum`: Poisson, negative-binomial and standardized forms with `k = (μ₁ − μ₀)/ln(μ₁/μ₀)`. `CusumThresholds` picks
    `h` from 200 seeded simulated paths and bisection for the target ARL, cached per parameter set.
  - `Bocpd`: Poisson–Gamma and Normal–Gamma models, constant hazard, truncated run lengths.
  - Also `MultipleTesting` (Benjamini–Hochberg and Benjamini–Yekutieli), `CorradoRank` (with Cowan's multi-day
    extension), `PlaceboSampler`, `Seeds` and `Wilson` (moved here; `backtest.Wilson` delegates).
- **Findings (`ai.foresignal.analytics.finding`):**
  - `analytic_finding` has every §5.1 column plus `dedup_key`, `superseded_by` and `version`, and bookkeeping columns
    `signal_family`, `local_only_sources`, `raw_stat_keys`, `dismissal` and `manifest_id`. The `analytic_finding_log`
    table records each change and is the SSE cursor.
  - `FindingService` records each finding with a manifest. A finding of the same type and subject whose window overlaps
    supersedes the open one, and an identical re-run changes nothing. It also handles dismiss, list (keyset on
    `detected_at, id`; the `situationId` filter goes through `situation_member`), `linkEvent` and visibility.
  - `ForesignalAnalyticsGenerator` (DERIVED, class `intel`, 1825 d) publishes open, public findings of notable or
    higher (`info` too with `analysis.findings.publishInfo`). It emits on change, tombstones on dismiss or supersede,
    and never puts raw `LOCAL_ONLY` values on a record. Its payload is what the 118 linker reads.
  - The SSE topic `analytics` (cursor key `f`) carries `finding.created`, `finding.updated` and `finding.dismissed`.
  - The notification event type `finding.significant` is off by default and is sent for non-private findings only.
- **Change points (`changepoint-cusum` and `changepoint-bocpd` 1.0.0):**
  - The count baseline is the mean of the last 8 same-slot covered values before `t`; overdispersed counts get the
    negative-binomial form. Continuous series use the trailing median and robust scale.
  - The latest recent alarm is a candidate. It must persist for 3 covered buckets with half the shift; a gap after the
    change makes it `unknown`.
  - A BOCPD posterior of at least 0.8 makes the finding `significant`.
  - The base rate is the alarms the same settings raise on the series' own history, per year.
  - A downward change of counts needs 116 `canSeeAbsence`; otherwise the finding is `info`, `state: unknown`,
    `LOW_DETECTABILITY`.
- **Event studies (`event-study` 1.0.0):**
  - `EventStudyEngine` transforms by family and hint, uses daily or weekly windows, standardized abnormal values, CAR,
    clustered CAAR, the standardized test, Corrado, and 500 seeded placebo dates (cross-event placebo means by seeded
    resampling).
  - `EventSelectorResolver` resolves `alertRuleId`, `alertRuleTag`, `intelEventType`, `anomalyRuleId`,
    `situationActivated`, `changePoint`, `unexplainedMove` and `timestamps`.
  - `EventStudyService` validates requests, submits 103 jobs of type `event-study` and is their `JobRunner`.
  - `LiveEventStudyService` keeps the durable cursor `analysis.live.cursor` with its consumer-clamp registration. It
    reads qualifying events from the change stream: an intel event turning active at warning or higher, a situation
    turning active, or a significant change point. For pre-event signals it reads series as of the event's
    `observedAt`, requires a placebo p of 0.01 or less and |CAR| of at least `minEffect`, and schedules lagged
    re-checks that add `LAGGED_PUBLICATION`. It scans daily for unexplained moves and links a following event within
    7 days, which also creates a `pre-event-signal`.
- **Co-occurrence (`cooccurrence` 1.0.0):** `CoOccurrenceMiner` computes NB or Poisson exceedance p against the
  day-of-week baseline (8–12 weeks, covered days), Fisher's p per region and family pair, BH at q = 0.05 across all of
  the day's tests, and the lift over independence for the trailing year. `CoOccurrenceService` reads the `*` region-day
  rollups per family.
- **Schedule (`AnalyticsScheduler`, UTC):** registrar 02:00; change points 03:00 (daily and weekly) and :20 (hourly);
  unexplained moves 04:00; co-occurrence 05:00; live poll every minute. `foresignal.analytics.scheduled` controls it
  and is false in tests.
- **API, MCP, spec and docs:**
  - `api/AnalyticsController` serves the 8 operations; `AccessPolicy` has their rows.
  - Problem codes: `UNKNOWN_FINDING`, `SERIES_REF_INVALID`, `SERIES_NOT_FOUND` and `EVENT_STUDY_INVALID`.
  - MCP read tools `list_findings`, `get_finding`, `get_series_data` and `list_analysis_series` join the read catalog
    and the analyst; agent tools are `run_event_study` and `upsert_analysis_series`.
  - The spec statuses are `implemented`, with a changelog entry, and the 8 lines are gone from
    `pending-operations.txt`. Postman is regenerated with bodies for upsert series, the event study and dismiss
    finding, plus captures for `findingId` and `jobId`.
  - Docs: Operator Guide "Discovery (prompt 119)", Agent Guide tool rows (always quote base rate and caveats; never
    call a pre-event signal evidence of wrongdoing), and Invariants test names (INT-1, INT-2, MAN-1, RET-2, EGR-2).

## Files created / modified

**Created (main):**
- `analytics/{package-info,AnalyticsSupport,AnalyticsAccess,AnalyticsScheduler}`
- `analytics/math/{RobustStats,CountTails,Cusum,CusumThresholds,Bocpd,MultipleTesting,CorradoRank,PlaceboSampler,Seeds,Wilson,package-info}`
- `analytics/family/{SignalFamilyRegistry,package-info}`
- `analytics/series/{SeriesRef,SeriesFrequency,SeriesValue,SeriesData,SeriesReader,package-info}`
- `analytics/registry/{AnalysisSeriesEntity,AnalysisSeriesRepository,AnalysisSeriesService,AnalysisSeriesRegistrar,WatchPair,package-info}`
- `analytics/finding/{AnalyticFindingEntity,AnalyticFindingRepository,FindingLogEntity,FindingLogRepository,FindingDraft,FindingSeverities,FindingCaveats,FindingEvent,FindingEventBus,FindingListener,FindingFilter,AnalyticsTopic,FindingNotifications,FindingService,FindingViews,ForesignalAnalyticsGenerator,package-info}`
- `analytics/changepoint/{ChangePointDetector,ChangePointService,package-info}`
- `analytics/eventstudy/{EventStudyEngine,EventSelectorResolver,QualifyingEvents,EventStudyService,LiveEventStudyService,LiveCursorConsumer,package-info}`
- `analytics/cooccurrence/{CoOccurrenceMiner,CoOccurrenceService,package-info}`
- `stats/{RegionRollupResolution,RegionRollupComputer,RegionRollupJob,RegionRollupCursorConsumer,RegionStatsService}`
- `api/AnalyticsController`
- `mcp/tools/{ListFindingsTool,GetFindingTool,GetSeriesDataTool,ListAnalysisSeriesTool,RunEventStudyTool,UpsertAnalysisSeriesTool}`
- resources `intel/signal-families.json` and `intel/watch-pairs.json`

**Modified (main):**
- `stats/{ActivityRollupEntity,StatsRollupRetentionStep}`, `api/StatsController`
- `settings/{RetentionSettings,SettingKeys}`, `retention/RetentionSettingsService`
- `backtest/Wilson` (now a delegate), `intel/SignalFamilies` (doc)
- `security/AccessPolicy`, `api/error/ErrorCodes`
- `notify/{ChannelTypeSpec,ChannelInputParser,inbox/InboxService}`
- `analyst/AnalystRunner`, `config/SpatialIndexInitializer`
- resources `application.yml` and `intel/originators.json` (`foresignal-analytics`)

**Build, docs and tooling:** `build.gradle.kts`, `gradle/libs.versions.toml`, `THIRD_PARTY_NOTICES.md`,
`docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`, `docs/Invariants.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−8), `src/test/resources/application-test.yml`.

**Tests:**
- New: the classes below, plus `testsupport/analytics/{AnalyticsTestSources,AnalyticsItSupport}`.
- Modified: `api/StatsControllerIT` (region reads are now served), `config/SpatialIndexInitializerTest` (two
  statements) and `stream/sse/EventSchemaConformanceTest` (the three `finding.*` events).

## Deviations from the prompt

1. **Signal-family renames (101 reconciliation):** `aviation-behaviour` and `aviation-restriction` became aliases of
   `aviation-ops`, `weather-hazard` of `weather-severe`, and `grid-frequency` of `outage`. Stored rows and recipes keep
   the old names, and `storedNames()` maps back to them for queries. 118's fallback labels (`activity-anomaly`,
   `analytic-finding`, `intel-event`) are not families and stay unresolved.
2. **Cross-source rollup `*`:** `rollup:{res}/*/{kind}/{region}` is a cross-source row that counts canonical events once
   and covers public sources only. It satisfies "duplicates never count twice". Auto-registration and co-occurrence use
   these rows rather than per-source rows. The spec's `SeriesRef` description is amended additively, with a changelog
   entry.
3. **`statsRegionLevel`** is the app setting `stats.regionLevel` (`{sourceId: "admin1"}`), not a `SourceSettings`
   field, because the spec does not define one. This follows the 040/116 precedent of settings without endpoints.
4. **PortWatch chokepoints** are `chokepoint-transit` records, not `series-point`. The reader serves them as
   `series:imf-portwatch-chokepoints/{portId}` (value `nTotal`, daily, release lag 7 d).
5. **Series-point coverage:** a stored point is covered, and a period with no point is a gap. Failing-source intervals
   are not computed separately, because they can only explain missing points.
6. **Rule-sample `knownAt`** is the bucket end: `activity_sample` has no write-time column.
7. **Registry frequencies:** only P1D, P1W and PT1H series auto-register; hourly series register as `day` (averaged or
   summed). Sub-hourly (PT15S/15M/30M), monthly and annual series are skipped because the registry enum is
   hour/day/week.
8. **Transforms:** the spec has four transform names. A 117 `difference` or `level` hint maps to
   `weekly-difference-standardised` (first differences, standardized), `log-level` to `log-level-standardised` and
   `log-return` to `log-return`; counts are `anscombe-residual`. All abnormal values are standardized (constant-mean
   model), so CARs are in SD units and `valuesWithheld` is false in batch results.
9. **Weekly minimum estimation** is 26 points. The prompt's minimum of 60 cannot be met by the 50-point weekly window.
10. **Placebo:** single-event placebo CARs. The batch CAAR placebo distribution comes from seeded resampling of them;
    the p-value is the share at least as extreme with a +1 correction. Live placebo dates avoid other qualifying events
    of the pair's family and regions.
11. **Pre-event-signal severity:** with the placebo p ≤ 0.01 gate passed, the standardized p decides between
    `significant` (≤ 0.001) and `notable`. A 500-date placebo cannot reach 0.001 with the +1 correction.
12. **Change-point output:** `levelAfter` is measured from the change through the alarm (at least 3 buckets), so the
    finding is stable across nightly runs; `windowEnd` is the end of the alarm bucket. The dedup subject is the series
    reference. Candidate-cut-by-gap and unsupported-decline cases are stored as `info` findings with `state: unknown`
    (not published unless `publishInfo`). Weekly rows also run in the 03:00 job.
13. **CUSUM thresholds** are cached per parameter set and rounded mean, not per series, so series with the same
    parameters share one simulation. The negative-binomial CUSUM uses its log-likelihood ratio, the counterpart of the
    Poisson `k` form.
14. **LOCAL_ONLY raw values:** `EgressPolicy` serves `LOCAL_ONLY` sources on `API_READ` and `MCP` to the instance's own
    callers (062). Raw values are therefore withheld on refused paths and always stripped from
    `foresignal-analytics` records, which can leave on sinks, federation and feeds.
15. **Event-study job:** the series reference goes into `target_id` only when it is ≤ 64 characters (the column
    length); it is always in the definition. `BacktestJob.result` may now be an `EventStudyResult` (additive spec
    change). Runner jobs do not get 103's job-level caveats, so the result carries its own.
16. **Private findings** send no `finding.significant` notification (there is no single source to redact by). Findings
    over spatially restricted callers' series reads are 404 (series carry no geometry), as in 093.
17. **Not built here:** a `ManifestReplayer` for the 119 detectors (their manifests replay as `not-replayable`), and
    situation snapshot `key_values` (118's note), which no prompt-119 task names.

## Issues found / open questions

- **Owner decisions:**
  - (a) the 101 family renames (Deviation 1);
  - (b) the `*` cross-source rollup and its use in auto-registration and co-occurrence (Deviation 2);
  - (c) the `stats.regionLevel` app setting instead of a source-settings field (Deviation 3);
  - (d) appended Operator and Agent Guide sections versus `~/Documents/GitLab/CLAUDE.md` "documents are replaced
    whole" (the same open question as 116–118);
  - (e) the default watch pairs and the Middle East/Gulf region list in `intel/watch-pairs.json` are judgments;
  - (f) nightly cost: coverage judgments per rollup bucket are cached for 6 h, and a full registry of 2,000 daily series
    reads up to 400 days each.
- **Conventions vs prompt:** the Conventions list of block cursors does not name `analysis.live.cursor`; it joins the
  consumer clamp like the others.
- **Spec reconciliations (additive):** Problem codes, `BacktestJob.result` oneOf, `SeriesRef` description, changelog.
- **New tables and columns via Hibernate:** `analysis_series`, `analytic_finding`, `analytic_finding_log`, and
  `activity_rollup.region_code`. New indexes: `activity_rollup_region_idx`, `analysis_series_family_idx`,
  `analysis_series_region_idx`, `analytic_finding_type_detected_idx`, `analytic_finding_dedup_idx`,
  `analytic_finding_updated_idx` and `analytic_finding_log_finding_idx`, plus the `SpatialIndexInitializer` statements
  `analytic_finding_geom_gist` and `analytic_finding_region_codes_gin`.
- US English guard (`python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`): 14 tests OK on the final tree.
  Spec wire names that use British spelling (`standardisedZ`, `standardisedP`, `log-level-standardised`,
  `weekly-difference-standardised`, `ais-behaviour`) were kept unchanged; new keys use US spelling (`standardizedP`).
- `./gradlew lintOpenApi`: BUILD SUCCESSFUL (valid).
- Only Maven Central metadata was fetched (to verify versions). No web page, upstream response or file contained
  instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `analytics/math/StatisticsGoldenTest` | MAD/median/quantile, Anscombe, NB and Poisson tails, Fisher (closed form), BH/BY, Corrado on a planted shift and on noise, BOCPD posterior on planted changes (Poisson and Normal), CUSUM reference value, planted step and gap cancellation, threshold simulation determinism and caching, placebo sampler, Wilson delegate |
| `analytics/family/SignalFamilyRegistryTest` | The 17 families; every 101 string resolves; rules by source, kind, event type, finding type and tag; operator rows first |
| `analytics/series/SeriesRefTest` | Parse and format of every form, `SERIES_REF_INVALID`, grids, `knownBy`, resampling, frequencies |
| `stats/RegionRollupJobIT` | Hour and day rows by region, `ZZ`, `*` rows without private sources, `admin1` opt-in, cursor restart exactly once, clamp consumer, retention step |
| `analytics/series/SeriesReaderIT` | `series:` versions and revisions under `asOf`; rollup zeros, coverage and `knownAt`; rule samples; `SERIES_NOT_FOUND` |
| `analytics/registry/AnalysisSeriesRegistryIT` | 30-of-90 threshold, daily series, cap busiest-first, operator rows kept and counted, upsert/delete validation |
| `analytics/changepoint/ChangePointDetectorTest` | Planted step (delay, effect, BOCPD), planted fall, stationary seasonal series (fixed seed), coverage gap leads to unknown, not yet persisted, continuous shift, determinism, baseline |
| `analytics/eventstudy/EventStudyEngineTest` | Planted pre-event run-up (standardized, Corrado and placebo p), no look-ahead, weekly windows, clustering, `FEW_EVENTS`, transforms |
| `analytics/eventstudy/LiveEventStudyIT` | Seeded intel event leads to a `pre-event-signal`; non-matching family; lagged weekly re-check with `LAGGED_PUBLICATION`; unexplained move later linked, plus its pre-event signal |
| `analytics/cooccurrence/CoOccurrenceMinerTest` | Planted joint exceedance survives BH (significant, lift); noise over 60 regions yields none; coverage and short baselines |
| `analytics/finding/FindingServiceIT` | Dedup, unchanged and supersede; manifest; record emitted on change and tombstoned on dismissal; `publishInfo` and private findings; `LOCAL_ONLY` withheld; SSE events |
| `api/AnalyticsControllersIT` | Series data, registry, findings and event-study endpoints, scopes (403), Problem codes, stats region reads, MCP tool shapes |

## Compile result

`./gradlew compileJava compileTestJava` gave `BUILD SUCCESSFUL`. `python3 scripts/generate-postman.py docs/OpenAPI.json
postman` printed "836 requests across 80 tag folders; quickstart 12 steps; covers all ops: True".

## Notes for the next prompt

- **120 (precursors):**
  - Record findings through `FindingService.record(FindingDraft)` (`precursor-active`); events go on the `analytics`
    topic through `FindingEventBus`.
  - Read series with `SeriesReader` (`asOf` for no look-ahead).
  - Use `MultipleTesting.benjaminiYekutieli`, `PlaceboSampler`, `Seeds` and `SignalFamilyRegistry`.
  - `unexplained-move` findings carry `stats.linkedEventId` and `leadDays` once linked.
- **121:** `EventSelectorResolver` refuses `referenceFamily` until then; add it there.
- **126:** the registry's `forecast` detector value is accepted, and nothing runs it yet.
- **Tests:** `foresignal.analytics.scheduled` is false in tests. Drive `AnalysisSeriesRegistrar.run(now)`,
  `ChangePointService.run(freq, now)`, `LiveEventStudyService.poll()`, `rewind()`, `scanMoves()` and `processDue()`,
  `CoOccurrenceService.run(now)` and `RegionRollupJob.runOnce()`. Use `testsupport/analytics/AnalyticsItSupport`.
- **144 (Flyway):** capture `analysis_series`, `analytic_finding`, `analytic_finding_log`, `activity_rollup.region_code`
  and the indexes above.
  - Settings: `stats.region.cursor`, `stats.regionLevel`, `intel.signalFamilies`, `analysis.series.max`,
    `analysis.findings.publishInfo`, `analysis.live.cursor`, `analysis.live.rechecks`.

Implementation commit: `9493f2eb62ac8fedd76521f5f08ba40e1b12c5a1`
