# 124-CR-Checkpoint-Intelligence-Foundations

| | |
|---|---|
| Prompt | 124-Checkpoint-Intelligence-Foundations.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `90f47f512539334b90d9964c42563897966c10f9` |
| Date | 2026-10-17 |

## Summary

**No test result in this report is a measured result.** Under the owner directive of 2026-10-17 (the override banner
at the top of `prompts/124-*.md` and `prompts/000-Run-Sequence.md`), tests were written but not run for this report.

- The implementation commit was produced by an earlier agent. That agent ran the suite several times and fixed
  failures, but it was stopped mid-run, before the directive took effect.
- Its runs were interrupted and no result was recorded. **None of them is recorded here as a pass.** There are no
  pass/fail counts per run, no benchmark metrics and no coverage figures.
- The diff itself mentions two of those runs, in a `build.gradle.kts` comment: "run 2: OutOfMemoryError in a raster
  test", and "Run 3 still ran out of memory with the cap". That comment is the only trace of them.
- **Deferred to the owner at code-complete:**
  - the full suite (`./gradlew test`);
  - the 123 synthetic benchmark baseline (`SyntheticBaselineTest`);
  - the coverage pass;
  - all checkpoint verification.
- `./gradlew compileJava compileTestJava` succeeds on the committed tree. It was re-run for this report: exit code 0.

The commit is 273 files, +7196/−490. By path:

| Path | Files |
|---|---|
| `src/main/java` | 128 modified, 2 new |
| `src/main/resources` | 3 |
| `src/test/java` | 24 new, 82 modified |
| `src/test/resources` | 26 new, 2 modified |
| Docs, spec, Postman and build | `docs/Agent-Guide.md`, `docs/Invariants.md`, `docs/OpenAPI.json`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`, `build.gradle.kts` |

The rest of this report is grouped by the prompt's focus areas, then by the other production fixes the runs turned
up. Every statement comes from the diff of `90f47f51`.

## Production fixes, by focus area

### 1. Golden statistics
- `analytics/math/Wilson`: at 0 successes the lower bound is pinned to exactly 0, and at `total` successes the upper
  bound to exactly 1. Floating-point cancellation had reported values such as 0.9999999999999999.
- `forecast/language/EstimativeLanguage`: a probability within `BOUND_TOLERANCE` (1e-12) of a band bound snaps to
  that bound. For example, `1 − 0.55` is `0.44999999999999996` and had landed in the neighboring band.

### 2. Determinism
- No production change is attributed to determinism alone. `DeterminismTest` is new (see Tests).

### 3. No look-ahead (INT-2)
- **Live pre-event check (119), the dead check that 123 reported:**
  - New `LiveEventStudyService.preEventReadOffset(w)` reads `estFrom − PlaceboSampler.DEFAULT_COUNT − (span + 10) − 2`
    buckets before the event. Before the fix it read only `estFrom − 2`, so the placebo p was always `NaN`.
  - `EventStudyEngine`: a placebo position must also be a bucket of the series (`p < n`).
  - `EventStudyBenchmark` reads the same range.
  - `ScenarioGenerator` now has `MARKET_DAYS` 600 → 1200 and a new `MARKET_FIRST_EVENT` = 900, so 500 placebo dates
    fit before the first event.
- **`PrecursorTester.test`:** only occurrences inside `[rangeStart, rangeEnd)` take part (new `inRange`). The comment
  says the 103 service and the 120 miner already pass windowed lists, so nothing changes for them.
- **`BaseRateService.computeAsOf`:**
  - the regions of a definition without codes are those with rows before the cutoff week (`regions(definition, before)`);
  - coverage is clipped at the cutoff (new `BaseRateCalculator.Coverage.clippedAt`);
  - detectability is estimated at `asOf` instead of the clock (OBS-2).
- **`AnalogService`:**
  - situation analogs exclude situations with `started_at` after now;
  - the base rate of `critical` counts only situations started, and outcomes computed, by now.
- **`JdbcResolutionData`:** when no change is logged by the time asked, a situation's state is the `before` of the
  first later log row, never the current state.

### 4. Evidence and observability (EVD-1, OBS-1)
- **`ChangePointDetector` persistence:**
  - Confirmation now needs at least `minPersistence` covered buckets after the alarm, and at least two thirds of
    them, each to show the shift, judged against their own in-control baseline.
  - Before, the average from the change through the alarm decided. The comment says the 123 spike scenario
    confirmed a change point in every replicate.
  - New `chainStart`: a sustained shift that re-alarms reports the first alarm of the chain.
- **`ReportingDensity`:** countries with equal population share a quintile group.
- **`CorrelationEngine`, `IntelEventService.rescore`, the merge path, `IntelligenceEventEntity` and
  `IntelEventMapper`:** new denormalized `member_count`, emitted as `memberCount` in event summaries. New
  `IntelMemberCountBackfill` fills null counts at `ApplicationReadyEvent`.

### 5. Manifests (MAN-1)
- No production change. `ManifestReplayCheckIT` is new (see Tests).

### 6. Ledger integrity (LED-1)
- No ledger production change. `LedgerIntegrityIT` is new (see Tests).

### 7. Egress (EGR-1, EGR-2)
- **Analyst proposals:**
  - New `AnalystExecutionService.visible`, plus `confirm` and `reject` overloads that take `privateAccess`, and
    `AnalystService.proposalFeedback(..., privateAccess)`.
  - A private proposal is `404 UNKNOWN_PROPOSAL` to a caller without private access.
  - Wired through `AnalystController` and the MCP tools `confirm_analyst_proposal` and `reject_analyst_proposal`.
- **`IntelTopic` replay:** applies the live filter (private events, and the type and status filters).
- **`AnalystTopic`:**
  - Payloads now follow the spec: `analyst.note` is `{note}` and `analyst.proposal` is `{change, proposal}`, built
    from the stored rows. An event whose row is gone is skipped.
  - The replay keeps only analyst events that pass the filter.
- **New `analyst/AnalystProposalEvents`:** publishes `analyst.proposal` changes (created, executed, rejected,
  expired, failed). The publisher was missing since 102.
  - Each change is stored as a notification delivered to no channel.
  - A private proposal carries a private `sourceId` and `private: true`, so the topic's filter withholds it.
  - Called from `AnalystRunner` and `AnalystExecutionService`.
- **`@RestrictedRead` on 19 MCP tools and resources of 098–111:** `add_to_investigation`, `confirm_analyst_proposal`,
  `create_export`, `create_intel_event`, `create_investigation`, `find_entity`, `get_backtest`, `get_entity`,
  `get_export`, `get_window_state`, `list_analyst_proposals`, `list_anomalies`, the product legend resource,
  `reject_analyst_proposal`, `run_analyst`, `run_backtest`, `test_precursor`, `update_intel_event_status` and
  `watch_for_unusual_activity`.
- **`S3Uploader`:** every request connects through `PinnedHttp` to the address the outbound policy resolved, over
  HTTP/1.1, so a DNS answer that changes after the check cannot redirect an upload. The constructor gains
  `PinnedHttp`.
- **Image decoding through the guard:**
  - `GeoTiffValues` decodes through `GeoTiffReader.decode`, after `inspect`, instead of `ImageIO.read`.
  - `RasterRenderService` decodes rendered loop frames through `GuardedImages`. The constructor gains it.

### 8. Durable cursors
- **Resets after the record history and `record_seq` are reset:**
  - `CorrelationEngine` and `SituationLinker` implement `resetState()` by setting the cursor to 0 and removing the
    stored cursor setting.
  - `catchUp()` restarts from 0 when the stored cursor is past the last published seq.
  - `EntityLinker` gains `IngestStateReset` and the same guard.
  - `CorrelationEngine.rewind` is now public, for the retention test support.
- **App settings:**
  - New `AppSettingService.remove` and `AppSettingRepository.deleteKey`.
  - `put` of a new key is a native upsert (`AppSettingRepository.upsert`), because two first writers of one cursor
    key had failed on the primary key.
- **`@PreDestroy closeAll()` on the buses** (`ChangeBus`, `LiveBus`, `AlertBus`, `IntelEventBus`, `FindingEventBus`
  and `SituationEventBus`), and `stop()` on `CompactOutboxService` and `EntityLinker`. Delivery threads had kept
  closed test contexts reachable until the heap ran out.
- **`build.gradle.kts`:** `spring.test.context.cache.maxSize=12`.

### 9. Benchmark (123)
- `ForecasterBenchmark`: analog windows are non-overlapping (`t -= h`). Before, the windows overlapped three weeks
  in four.
- `PrecursorBenchmark.effectiveHitRate` counts chance hits from every consequent.
- **`ScenarioGenerator`, `true-precursor` scenario:**
  - `PLANTED_HIT_RATE` 0.6 → 0.7;
  - antecedents 0.2 → 0.15 a day (`PRECURSOR_ANTECEDENTS_PER_DAY`);
  - background consequents 0.1 → 0.02 a day (`PRECURSOR_BACKGROUND_PER_DAY`).

  The comment says the old rates capped the observed lift near 1.5, below the miner's notable rule of 2.
- **`intel/benchmark-thresholds.json`:** the `forecaster:precursor` row `reliabilityMaxDeviation` has its warn
  threshold raised from 0.2 to 0.3. The note gives the reason: the 120 shrinkage of n/(n + 20).
- The market-scenario changes are listed under area 3.

### 10. API surface
- **`@ApiResponse` annotations** on the non-200 statuses: 62 annotations on 54 operations across 22 controllers.
- **Status changes:** `@ResponseStatus(CREATED)` on `analystNoteFeedback`, `analystProposalFeedback`,
  `addIntelEventMember`, `addInvestigationItem` and `createPrecursorTest`. Each now answers 201.
- **`docs/OpenAPI.json`:**
  - `"id": true` and an `idValue` on the 24 SSE event entries of 101–122;
  - the retention property `entityLinks` (098, default 365d).
- **`scripts/generate-postman.py`:**
  - 19 new example bodies for 098–111 operations;
  - the `updateAttentionTopic`, `mergeSituation` and `splitSituation` bodies use literal ids, because their ULID and
    slug patterns cannot match a `{{variable}}`.
- **`postman/Foresignal.postman_collection.json`:** regenerated. 22 bodies changed from `{}`.
- **`src/test/resources/openapi/pending-operations.txt`:** not modified by this commit.

### 11. Invariants
- **`docs/Invariants.md`:** test names added to the rows below.

| Invariant | Tests added |
|---|---|
| EGR-1 | `IntelligenceEgressTest` |
| INT-2 | `IntelligenceLookAheadIT` |
| EVD-1 | `DuplicateInflationIT` |
| OBS-1 | `ChangePointAbsenceTest`, `PrecursorAbsenceTest` |
| MAN-1 | `ManifestReplayCheckIT` |
| LED-1 | `LedgerIntegrityIT` |

- **New `InvariantRegistryTest`** (see Tests).

### 12. Coverage
- Not measured (directive).

## Other production fixes the runs turned up

These fixes are outside the intelligence focus areas.

**Startup and wiring:**
- `@Autowired` on the injection constructor of the five multi-constructor beans 123 reported: `CanonicalRules`,
  `OriginatorRegistry`, `ObservabilityDeclarations`, `BuildInfo` and `PolGenerator`.
- `@Lazy` injection on `PolGenerator`, `PolPrivateGenerator`, `RuleWindowsGenerator` and `RunAnalystTool`.

**SQL and JDBC:**
- `jsonb_exists` instead of `?` in `AnalysisSeriesRegistrar`.
- `CAST(? AS text[])` in `EntityKeyExpansion` and `EvidenceQueries`.
- `persisted_observed_at` in the `EntityLinker` backfill.
- `saveAndFlush` on superseded keys in `EntityResolver`.
- `LIMIT\s` in a `TripHazards` text block.
- `backtest_job.type` length 16 → 32.
- `BacktestJobService` takes a transaction-scoped advisory lock per token before its one-job-in-flight check.
- `BaseRateService` writes `region_profile` as an upsert followed by a delete of stale regions.

**Analyst (102):**
- The watch tag `origin:analyst` becomes `origin-analyst`, because the rule tag pattern has no colon.
- Proposal count keys are plural.
- Null-safe priority, type, status and rating checks.
- A failure reason lists the field errors.
- `saveIfPresent` stops a deleted profile from being re-inserted after a run.

**Federation (113):**
- `OriginLedger.claim` returns a `Release`, and new `release` and `releaseAll` restore the previous entry when a write
  fails.
- Wired into `FederationIngestService`, `FederationIngestor`, `PeerRuntime`, `PeerSession` and
  `CompactBundleReceiver`.

**Other components:**
- `Dedup`: open-ended intervals are compared without arithmetic on `Instant.MAX`.
- `WindowEngine`: only an explicit `messageTemplate` overrides the pending message.
- `TokenFilter`: a successful share-link write is no longer audited twice.
- `OgcFeatureService`: a per-source title `CASE`.
- `OgcController`: a null valid time in CoverageJSON.
- `ArchiveService`: the hive tier segment is `std`/`trk`.
- `GeoParquetWriter`: the record consumer is flushed before the block.
- `ImageEncoding`: a WebP compression type is set.
- `RasterTilesController` and `RasterRenderService`: the image cache key includes the format.
- `PngUvEncoder`: a flat channel gets a unit range centered on its value.
- `ApiTokenEntity`: a null policy column is handled.
- `AisGapGenerator`: reads one vessel past the cap.
- `WxStormTracksGenerator`: no duplicate first position.

**Adapters, checked against new captures:**
- `EcmwfRunIngest.indexKey`: `…fc.grib2` → `…fc.index`, verified 2026-10-17 according to the comment. A message
  shared by two products is committed from a copy.
- `AifsAdapter`: the `tp` scale changes from 1000 to 1.
- `NbmAdapter`: sky cover is `TCDC:surface`, level type 1.
- `SstAdapter`: reads `time_coverage_start`, puts the griddap bounds in parentheses and fixes the east bound.
- `RtmaAdapter`: product category `analysis` → `model`.
- `TafGroups`: accepts `FM\d{6}`.
- `AwcTafsAdapter`: descriptor fields.
- `EmwinTextParser`: VTEC times are read as yymmdd.
- `SatDumpDescriptor`: more significant fields and new icons.
- `FdsnEventAdapter`: a bad HTTP status raises `AdapterException`.
- `SwpcAuroraAdapter`: a `maxAge` ladder.
- `AtlasProbesAdapter`: a streaming mapper.
- `SystemCanaryAdapter`: the `operator-owned` terms.

**Resources:**
- `intel/originators.json`: originators `eccc` and `eia`, and source mappings for `digitraffic-road`,
  `eccc-hydrometric`, `eia-930` and `eia-petroleum`.
- `provenance/licences.json`: aliases for many descriptor terms, and a new `CC-BY-SA-2.0` entry.

**Build:**
- `build.gradle.kts`: a pinned RTMA-RU fixture (5.6 MB byte range), fetched by `downloadTestFixtures`.

## Deviations from the prompt

1. **Tests were not run** (owner directive). None of these acceptance criteria was checked by running anything:
   - `./gradlew test` passes;
   - the named tests pass;
   - the benchmark has no `fail`;
   - pass/fail counts, benchmark metrics and coverage are recorded.

   The required tests exist in the tree.
2. **The prompt's test names already existed.** `DuplicateInflationTest`, `ManifestReplayCheckTest`, `DeterminismTest`
   (in `trigger/window`) and `LookAheadTest` (in `trigger/window`) predate this commit. The commit adds:
   - `analytics/DeterminismTest` and `analytics/LookAheadTest` for 100–123;
   - `DuplicateInflationIT`, `ManifestReplayCheckIT` and `IntelligenceLookAheadIT` for the database paths.
3. **Golden values.** The new `GoldenValuesTest` sits beside the existing `StatisticsGoldenTest`.
   - Its javadoc says the expected values come from separate Python 3 standard-library scripts written from the
     published definitions, on 2026-10-17, and pasted in as literals.
   - The scripts are not in the commit.
4. **Scope.** The commit includes changes beyond "fixes only". Each one is presented in the diff as the fix for a
   failure:
   - `AnalystProposalEvents`;
   - `memberCount` and its backfill;
   - the `AnalystTopic` payload change;
   - the adapter corrections against new captures;
   - the `originators.json` and `licences.json` additions.
5. **Benchmark changes are not code defects.** The scenario rates, `MARKET_DAYS` and the precursor warn threshold
   were changed. 123's notes said threshold failures are results to check before changing code. See owner decisions.

## Issues found / open questions

**Owner decisions:**
- (a) **A persisted value changed.** The rule tag `origin:analyst` is now `origin-analyst`. Watches stored with the
  old tag keep it. The project rule says such values are reported, not changed.
- (b) **Benchmark changes:**
  - the `forecaster:precursor` `reliabilityMaxDeviation` warn threshold, 0.2 → 0.3;
  - the `true-precursor` scenario rates;
  - the 1200-day market scenario.
- (c) **New captured fixtures.** They were added under `src/test/resources/fixtures/`, each with a README: AIFS,
  ECMWF IFS, EPA UV, GFS-Wave, NBM, OSM Monaco, RTMA-RU (`.idx` only), SST and WPC HeatRisk.
- (d) **The Agent Guide was appended to.** Tool rows for 100–111 were added to the existing table, which conflicts
  with "documents are replaced whole" (the same open question as 116–123).

**Guards not run:** the US English guard (`style/test_us_english.py`) and `lintOpenApi` were not run for this
report, under the no-tests directive.

**Schema via Hibernate, for prompt 144:**
- new column `intelligence_event.member_count` (integer, nullable);
- `backtest_job.type` widened to 32.

No new tables and no new setting keys.

No upstream was fetched. No file or tool output contained instructions directed at the agent.

## Tests written (not run)

**New (`src/test/java`):**

| Test | What it covers |
|---|---|
| `IntelligenceEgressTest` | EGR-1/EGR-2 over 098–123. Private items are absent from lists and 404 on direct reads for anonymous, read and agent callers, and visible to admin. The same holds for MCP output and SSE replay (analytics, intel, situations, forecasts). Tiles, feeds and federation never emit a private item or a raw `LOCAL_ONLY` value. `LOCAL_ONLY` inputs are refused on re-serving paths. A private source never reaches a remote LLM. 5 tests |
| `InvariantRegistryTest` | Every `docs/Invariants.md` row owned by a completed prompt names an existing test class, and every class a row names exists, apart from a `LATER` list. 3 tests |
| `analytics/DeterminismTest` | INT-1: each 100–123 component, in benchmark pure mode, gives identical output twice, including its permutation, placebo, surrogate and bootstrap statistics. 4 tests |
| `analytics/LookAheadTest` | INT-2 in pure form: event-study live and batch reads, precursor discovery and validation windows, base rates and change points under late revisions. 4 tests |
| `analytics/IntelligenceLookAheadIT` | INT-2 on the database paths: analogs, base rates as of a time, retrospective ledger evaluation, the live precursor watch and the live pre-event check. 5 tests |
| `analytics/changepoint/ChangePointAbsenceTest` | OBS-1 for 119: an unsupported downward count change is `unknown`. 2 tests |
| `analytics/math/GoldenValuesTest` | Golden values for CUSUM (Poisson and Gaussian), BOCPD, BH/BY, Wilson, Brier, log, RPS, CRPS, Murphy, PAV with CORP, and Platt. 8 tests |
| `benchmark/SyntheticBaselineTest` | The 123 synthetic suite at the bundled parameters: every component is `pass` or `warn`. Writes `build/reports/benchmark/synthetic-baseline.json`, the baseline for 139. 1 test |
| `evidence/DuplicateInflationIT` | EVD-1 end to end: 20 GDELT echoes of one outlet form one group through the resolver, `independentCount`, 099 corroboration and `rescore`. 1 test |
| `federation/OriginLedgerReleaseIT` | A claim's `Release` restores the previous entry or removes a created one, and never undoes a newer claim. 3 tests |
| `forecast/LedgerIntegrityIT` | LED-1:<br>- forecasts are immutable through the API and the entity;<br>- `asOfSeq` is server-set;<br>- annulled questions are never scored, even when scores are recomputed;<br>- time averaging carries forecasts forward;<br>- the LLM cap and Platt fits stay with their forecasters.<br>5 tests |
| `manifest/ManifestReplayCheckIT` | MAN-1: every `manifest_id`-like column is listed with its covering test, and stored ids resolve. Every replayer (anomaly, event scorer, backtest) replays `identical`. 2 tests |
| `precursor/PrecursorAbsenceTest` | OBS-1 for 120: an unseen consequent is excluded under `LOW_DETECTABILITY`, not counted as a miss. 2 tests |
| Adapter capture contract tests | The production adapter against the 2026-10-17 captures, through the new `testsupport/raster/FixtureHttp` and `GridStats`: `AifsAdapterContractTest`, `EcmwfAdapterContractTest`, `EpaUvAdapterContractTest`, `GfsWaveAdapterContractTest`, `HeatRiskAdapterContractTest`, `NbmAdapterContractTest`, `OsmInfraParserCaptureTest`, `RtmaAdapterContractTest` (its GRIB test skips when the pinned fixture is absent) and `SstAdapterContractTest` |

**Tests extended:**

| Test | Addition |
|---|---|
| `ConsumerClampIT` | The evidence, situation, region-rollup and precursor cursors are registered, without duplicates |
| `EventSchemaConformanceTest` | The four `intel.event.*` events and `analyst.note`/`analyst.proposal` |
| `ChangeBusTest` | `closeAll` ends delivery threads |
| `AlertBusTest` | Closing the context ends delivery threads |
| `DescriptorValidatorTest` | The missing-terms warning |
| `PostmanCollectionContractTest` | 6 validators (`createExport`, `createPack`, `createAttentionTopic`, `updateAttentionTopic`, `createBaseRate`, `queryBaseRate`) and 50 named exemptions, each with the IT that covers it |

**Tests changed, with the reason the diff gives:**

*Analyst and intelligence:*
- `AnalystRunnerIT`: the tag is `origin-analyst`.
- `InvestigationsControllerIT`: an item add is 201.
- `EventScorerTest`: weights of 0.6, because 0.5 gives 0.657, which is moderate; asserts 0.737856.
- `CorrelationEngineIT`, `SituationLinkerIT` and the other intel and situation ITs:
  - kind `test-record`, because ingest rejects undeclared kinds (`UNKNOWN_KIND`);
  - per-test dynamic sources;
  - reference fixtures loaded;
  - in `SituationLinkerIT`, events timed at the last activity so a 0.45 score does not decay.
- `InvestigationIT`: the snapshot record stays within the ingest limits.

*Analytics and backtests:*
- `ChangePointDetectorTest`: the stationary case asserts at most 20 alarms in 200 seeds instead of none for one
  seed, because about 3.8% is expected.
- `CoOccurrenceMinerTest`: the association is planted on three more days.
- `AnomalyBacktestIT`: hour-of-day warm-up is 14 days.
- `AnomalyClassifierTest`: a p99 needs 100 samples.
- `PoissonTailTest`: values from the closed form 65/(24e).
- `PrecursorLiveWatchIT`: the cursor ends at the log head.
- `BacktestControllersIT` and `RuleBacktestIT`: timing-independent setup, and a disabled rule.

*Raster and adapters:*
- `ImageEndpointIT`: `.webp` is accepted (108).
- `ProductTilesIT` and `StyleRegistryTest`: 25 styles.
- `RasterExportRulesTest` and `WmtsControllerTest`: frame keys with suffixes.
- `RasterCachePolicyTest`: a new region key.
- `WorldCrs84QuadTest`: square z0 tiles.
- `DataTileEncoderTest`, `PngUvEncoderTest` and `TitanLiteTrackerTest`: the expected values were wrong. The IoU is
  1/3.
- `SampleEndpointIT`: an unknown `record=` is 404 (106).
- `HrrrAdapterWireMockTest`, `MrmsAdapterWireMockTest`, `SatDumpDescriptorTest` and `SpaceAdaptersTest`: the
  products and kinds of 106 and 107.
- `NidsDecoderTest`: the 18-byte header.
- `PortCallTest`: port geometry.

*Descriptors and terms:*
- `NuclearReactorsUsAdapterTest`: 24 h cadence.
- `AgsiParserTest`: 60 a day.
- `UsgsIngestIT`: the resolved catalog id.
- `SourceRegistryTest`: a fixture with terms (109).

*Security and retention:*
- `CompactMessageCodecTest`: sizes +2 bytes.
- `EgressPolicyTest`: `ARCHIVE` allows private records, and the cache is invalidated.
- `S3UploaderTest` and `S3UploadIT`: `PinnedHttp`, and a DNS-named endpoint.
- `TokenFilterIT` and `ActionEndpointIT`: reset the 035 auth backoff.
- `CalendarFeedIT` and `OgcFeaturesIT`: feed keys need restrictions, and URL encoding.
- `KinematicsApiIT` and `ApproachConditionIT`: avoid the (0,0) null island.
- `RecordLifecycleIT` and `RetentionItSupport`: catch up `intel.cursor`.
- `FamilyDefaultsTest`: classifies four adapters and adds `cap_message`.
- `AdapterInventoryTest`: skips DERIVED generators.

*Other:*
- `ManifestServiceTest`: a truly missing registry.
- `JacksonDurationIT`: new retention keys.
- `UpstreamClientIsTheOnlyHttpClientTest`: allows `sink/SinkTcpServer`.
- `SeriesMetadataTest`: only stubbed sources registered.
- Fixtures `homeassistant.json` and `webhook.json`: `exercise: false`.

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree, re-run for this report: exit code 0.

## Notes for the next prompt

**Carried-forward items, checked against the diff of `90f47f51`:**

| Item | Status after 124 |
|---|---|
| Postman validator gap (56 operations, reported by 122) | **Addressed in code, unverified.** 6 validator entries plus 50 named exemptions, 56 in all, in `PostmanCollectionContractTest`. The test has not been run. |
| `EventSchemaConformanceTest` lacks `intel.*` and `analyst.*` (116, 118) | **Addressed in code, unverified.** Entries were added for the four `intel.event.*` events and both `analyst.*` events. `AnalystTopic` payloads were changed to the spec shapes, and `IntelTopic.payload` was made public. |
| `@RestrictedRead` missing on MCP tools (118) | **Addressed in code, unverified.** Added to the 19 tools and resources listed above. Many older tools still carry none, presumably through `RestrictedReadCoverageTest.EXEMPT_TOOLS`. Whether that test passes is unknown until it runs. |
| `OriginLedger` records an arrival before the write (113) | **Addressed.** `Release`, `release` and `releaseAll` are wired into every federation write path. `OriginLedgerReleaseIT` is written, not run. |
| The 103 job framework does not recover stale `running` jobs (121) | **Open.** The diff of `BacktestJobService` adds only the advisory lock. `grep -rn "ApplicationReadyEvent\|stale\|recover" src/main/java/ai/foresignal/backtest/BacktestJobService.java` prints nothing. |
| Spring 7.0.9 multi-constructor beans `CanonicalRules`, `OriginatorRegistry`, `ObservabilityDeclarations`, `BuildInfo` and `PolGenerator` (123) | **Addressed.** All five constructors have `@Autowired`. That the context starts is unverified, because nothing has run. |
| The dead live pre-event check (119, reported by 123) | **Addressed in code, unverified.** `LiveEventStudyService.preEventReadOffset`, the bound in `EventStudyEngine` and the 1200-day market scenario. |

**Other notes:**
- **The roadmap is stale.** In `docs/Roadmap.md` §4, prompts 097–124 still show `Ready` in the Status column; for
  example, line 225 shows 124 as `Ready`. The owner decides whether to update it.
- **139 (checkpoint):** compares against the `SyntheticBaselineTest` output
  (`build/reports/benchmark/synthetic-baseline.json`). That baseline does not exist yet. It is produced when the
  owner runs the suite at code-complete.
- **At code-complete, the owner's first run should expect:**
  - possible heap pressure; the context cache is capped at 12;
  - `RtmaAdapterContractTest` needs `./gradlew downloadTestFixtures`, or its GRIB test skips.
- **144 (Flyway):** capture `intelligence_event.member_count` and `backtest_job.type` `varchar(32)`.
  `IntelMemberCountBackfill` is idempotent and can stay or become a migration.

Implementation commit: `90f47f512539334b90d9964c42563897966c10f9`
