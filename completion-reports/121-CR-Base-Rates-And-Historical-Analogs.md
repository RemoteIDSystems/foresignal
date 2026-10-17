# 121-CR-Base-Rates-And-Historical-Analogs

| | |
|---|---|
| Prompt | 121-Base-Rates-And-Historical-Analogs.md |
| Status | Complete (tests written, not run; owner decisions under Issues) |
| Implementation commit | `90b58ac8ac3ff4d476aeff57265b26a9d5a819dc` |
| Date | 2026-10-17 |

## Summary

This prompt gives every probability an outside view. It adds three packages:

- `ai.foresignal.baserate.reference`: reference-history imports kept as weekly counts;
- `ai.foresignal.baserate`: the base-rate library;
- `ai.foresignal.baserate.analog`: historical analogs.

Everything is deterministic and uses no LLM. Every output stores a 116 manifest. Compiles; tests written, not run.

- **Reference history (`reference_history`, `reference_import`):**
  - Rows are counts per family, 040 region, ISO week (Monday, UTC) and source, with `fatalities`, `licence`, `egress`
    and `import_id`. The primary key is the prompt's.
  - Each import is a 103 job of type `reference-import` and is idempotent per `(dataset, normalized params)`
    (`import_key`, a SHA-256). A repeat request returns the import while it is queued or running; otherwise it re-runs
    it.
  - Imports run in week-aligned chunks: a year, or a country-year for ACLED. Each chunk is replaced whole in one
    transaction (`ReferenceHistoryWriter`) and recorded in `progress.done`. A resumed import skips finished chunks, and
    a re-run writes identical rows.
  - **UCDP GED (`UcdpGedImporter`):** the operator's CSV, or the downloaded ZIP holding it, under
    `${FORESIGNAL_DATA_DIR}/imports`. The path is re-checked like an `INBOX_PATH` (`InboxFiles.rootedAt`).
    - The columns are those of the real GED 26.1 header, read from `ged261-csv.zip` on 2026-10-17.
    - Alternatively, the UCDP API with `useApi` and `version`, through the `ucdp-ged` token and budget.
    - Violence types 1 and 2 map to `conflict:battles`, type 3 to `conflict:violence-civilians`.
    - Region: the country by name (former names in parentheses are tried), else the point, else `ZZ`. A matching
      `adm_1` adds an admin-1 row. Fatalities are `best`.
  - **ACLED (`AcledHistoryImporter`):**
    - Reads through the 081 client: `AcledAdapter.historyPage`, with the adapter's in-memory tokens and the `acled`
      source's credentials and budget.
    - Works by country and year. Event types map to battles, explosions-remote, violence-civilians, protests and
      riots; strategic developments are read but not counted.
    - Rows are `LOCAL_ONLY` under `ACLED-EULA`. Without both credentials the import is recorded as `skipped` with the
      reason `credentials absent`.
  - **USGS ComCat (`UsgsComCatImporter`):**
    - Uses the FDSN `query?format=csv&minmagnitude&starttime&endtime&orderby=time-asc&limit=20000` per year, verified
      live on 2026-10-17.
    - A year the service refuses (more than 20,000 events) or that comes back full is split in two by time; events
      are de-duplicated by id. Only `type=earthquake` counts.
    - Families `seismic:m4.5`, `seismic:m5` and `seismic:m6` (cumulative).
    - Region from the 040 tagger. Offshore events take the nearest `ref_country` within 200 km (`ST_DWithin` on
      geography), else `ZZ`.
  - **GDACS (`GdacsHistoryImporter`):**
    - Uses `/gdacsapi/api/Events/geteventlist/search` with `alertlevel=orange;red`, per hazard and year, 100 per page.
      Verified against the published Swagger document and a live call on 2026-10-17.
    - Families `disaster:gdacs-orange+`, `disaster:gdacs-red` and `disaster:gdacs-{hazard}-orange+`.
  - **Own history (`OwnHistoryFold`, nightly at 02:15 UTC):**
    - Folds 116 canonical events (never raw records) into `source=foresignal`.
    - Families: the bare canonical family, plus the seismic bands from `band_value`.
    - Only complete weeks are folded. Each run re-folds four weeks behind the last folded week
      (`baserate.fold.lastWeek`). Merged events and events seen only by private sources are left out.
  - The HTTP paths go through `UpstreamClient` (`ReferenceHttp`). USGS and GDACS use a budgeted system client,
    `reference-import`, at 30/min and 2,000/day. UCDP and ACLED use their sources' own clients. A per-minute refusal
    waits once (65 s at most); a spent daily budget fails the chunk, so a re-submit resumes there.
- **Base-rate library (`base_rate`, `region_profile`):**
  - `BaseRateCalculator` (`baserate` 1.0.0, pure) chooses one source per region and week by priority, so the same
    events are never counted twice:
    - conflict: ACLED, then UCDP, then Foresignal;
    - seismic: USGS, then Foresignal;
    - disaster: GDACS, then Foresignal;
    - any other family: the longest coverage first.
  - Coverage comes from the imports' completed chunks and the fold's span.
  - Windows are non-overlapping runs of whole weeks (1, 4 or 13). A window with an uncovered week is dropped.
  - Output: `ratePerWindow`, `pAtLeastOne` and its 95% Jeffreys interval (Commons Statistics `BetaDistribution`),
    `nWindows`, `nEvents`, the period, `sourceMix` and `licences`.
  - Conditional rates keep the windows preceded within `withinDays` (rounded up to whole weeks) by an antecedent event
    in covered weeks. The unconditional rate is reported alongside.
  - Caveats (`BaseRateCaveats`):
    - `SMALL_SAMPLE`: fewer than 30 windows;
    - `MIXED_SOURCES`;
    - `LOW_REPORTING_DENSITY`: a region in decile 1;
    - `LOW_DETECTABILITY`: own history used where 116 `pDetect` is below `absenceMinDetect`;
    - `LOCAL_ONLY_WITHHELD`: an ACLED per-source count that `EgressPolicy.decide` refuses on the path.
  - Reporting density (`ReportingDensity`): mean weekly conflict events over ten years (the largest source per
    country) divided by the median of the country's population quintile, then deciles over all countries. Population
    comes from World Bank `SP.POP.TOTL` (117), else the gazetteer.
  - Region classes (`RegionClasses`): `continent:XX` from the gazetteer and `income:{band}` from the World Bank GDP per
    capita.
  - Automatic rates cover every family × region with at least one event and at least 52 covered weeks, for P7D and
    P28D. They are replaced in place, and rates no longer produced are removed. Region profiles are recomputed first.
  - Triggers: a 103 job `base-rate-refresh`, run monthly (the 1st at 03:30 UTC), after every finished import, or on
    request (409 `BACKTEST_IN_PROGRESS` while one is queued or running).
  - Ad-hoc queries return the stored rate with the same definition key, else compute one and cache it for 24 hours.
- **Analogs:**
  - `SignatureBuilder` (`situation-signature` 1.0.0) builds named features:
    - standardized family counts over the first N = min(7, day) days, scaled by the family's weekly SD of members
      over the last year;
    - a log-linear escalation slope;
    - theme, continent and income-band one-hots;
    - log entity counts per 098 type;
    - the severity path: max, last, and the share of days at warning or above.
  - `SignatureService` computes a signature on activation and daily for 30 days (every 5 minutes, idempotent per
    day). It backfills situations without a signature (200 per run) and computes outcomes for dormant, closed or
    30-day-old situations: duration, peak severity, and follow-on families within 7 and 30 days of day 7, from the
    region-day rollups and `reference_history`.
  - `AnalogSearch` (`analog-search` 1.0.0, pure): feature cosine top 50 and embedding cosine top 50 (the situation
    records' 027 vectors in the active slot), fused by reciprocal rank (k = 60). It excludes time overlap and merge
    groups, returns the top k with `countFound`, and falls back to `feature-only`, labeled, without embeddings.
  - The outcome distribution is the frequency among analogs with known outcomes, shrunk by n ÷ (n + 10) toward the
    base rate, with a Jeffreys interval. For a family outcome the base rate is `pAtLeastOne` of the matching rate for
    the target's countries; for `critical`, the share of past situations that reached critical.
  - Region-period analogs: `reference_history` weeks plus the not-yet-folded canonical events, against two-week
    windows of the same continent's countries over ten years that ended at least 30 days before the target.
- **API, MCP, spec and docs:**
  - `api/BaseRateController` serves the 12 operations, and `AccessPolicy` has the rows (rates and analogs are read;
    store, delete and refresh are agent; imports are admin).
  - MCP `get_base_rate` and `find_analogs` (read) are in the analyst's read-tool list and the Agent Guide.
  - `referenceFamily` now resolves in `EventSelectorResolver` (event studies) and `PrecursorOccurrences` (precursor
    resolve, family counts, keys, labels, signal family), as 120's notes asked.
  - Spec: the statuses are `implemented`, the 12 lines are gone from `pending-operations.txt`, the Problem codes are
    listed and there is a changelog entry. Postman is regenerated with bodies for an import, a query, a custom rate
    and an analog query, captures for the import, rate and refresh job, and new variables.
  - Docs: Operator Guide "Base rates and analogs (prompt 121)", the Source Catalog reference-datasets section, Agent
    Guide rows, and Invariants test names (EGR-2, INT-1).

## Files created / modified

**Created (main):**
- `baserate/{package-info,BaseRateCalculator,BaseRateCaveats,BaseRateDefinition,BaseRateEntity,BaseRateRepository,BaseRateService,BaseRateViews,BaseRateRefreshRunner,BaseRateScheduler,RegionClasses,RegionProfileEntity,RegionProfileRepository,ReportingDensity}`
- `baserate/reference/{package-info,ReferenceHistoryEntity,ReferenceImportEntity,ReferenceImportRepository,ReferenceFamilies,WeeklyCounts,ReferenceHistoryWriter,ReferenceRegionResolver,ReferenceImporter,ImportRun,ImportParams,ReferenceHttp,UcdpGedImporter,UsgsComCatImporter,GdacsHistoryImporter,AcledHistoryClient,AcledAdapterHistoryClient,AcledHistoryImporter,OwnHistoryFold,ReferenceImportService,ReferenceImportJobRunner,ReferenceImportViews}`
- `baserate/analog/{package-info,SituationSignatureEntity,SituationSignatureRepository,SituationOutcomeEntity,SituationOutcomeRepository,SignatureBuilder,AnalogSearch,SignatureService,AnalogService,AnalogScheduler}`
- `api/BaseRateController`, `mcp/tools/{GetBaseRateTool,FindAnalogsTool}`

**Modified (main):**
- `source/adapters/acled/AcledTokens`: an overload that takes a `SourceHttp`, source id and time. The fetch path is
  unchanged.
- `source/adapters/acled/AcledAdapter`: `historyUri` and `historyPage` (a 401 refreshes the token once).
- `analytics/eventstudy/EventSelectorResolver` and `precursor/PrecursorOccurrences`: `referenceFamily`.
- `api/error/ErrorCodes` (5 codes), `security/AccessPolicy`, `settings/SettingKeys` (`baserate.fold.lastWeek`) and
  `analyst/AnalystRunner` (2 read tools).

**Docs, spec and tooling:**
- `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Source-Catalog.md`, `docs/Agent-Guide.md` and
  `docs/Invariants.md`.
- `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`.
- `src/test/resources/openapi/pending-operations.txt` (−12).

**Tests:**
- New: the classes below and `baserate/reference/ReferenceTestSupport`.
- New fixtures in `src/test/resources/fixtures/baserate/`, with a README:
  - `ucdp-ged-sample.csv`: synthetic rows under the real header;
  - `usgs-*.csv`: synthetic rows in the verified layout;
  - `gdacs-search-eq-2023.json`: a real response;
  - `acled-page.json`: synthetic.
- Modified: `api/AnalyticsControllersIT`. Its invalid-selector case now sends an invalid `referenceFamily` value,
  because the form itself is now accepted.

## Deviations from the prompt

1. **UCDP region from the country name, not `country_id`.** In the GED 26.1 header `country_id` is a Gleditsch-Ward
   code (Algeria = 615), not ISO, and no GW crosswalk is bundled. Mapping falls back from the name to the point to
   `ZZ`.
2. **UCDP API needs `params.version`.** The annual GED API version is not guessed. The spec gains
   `ReferenceImportRequest.params.version`, an additive change listed in the changelog. API-mode events whose start
   precedes their year chunk are counted in the chunk's first week.
3. **ACLED without credentials is `skipped` (202), not 409.** This follows the prompt and the spec's `statusReason`
   example. 409 `IMPORT_DATASET_UNAVAILABLE` is kept for a missing UCDP file or a missing UCDP token.
4. **Families added:** `conflict:riots` (ACLED Riots), `disaster:gdacs-red` and the per-hazard GDACS families. ACLED
   strategic developments are not counted.
5. **Own history is folded from canonical events directly**, not from the 119 region-day rollups. The rollups are
   per kind and carry no magnitude. Own families are the bare canonical families plus the seismic bands, so there is
   no Foresignal contribution to subtypes like `conflict:battles`. Own-history coverage runs from its first row to the
   last folded week.
6. **The Jeffreys interval is on `pAtLeastOne`** (a proportion), not on the Poisson rate. `P90D` is 13 whole weeks
   (91 days), because windows are week-aligned.
7. **Reporting density** uses ten-year conflict-family counts (the largest source per country) as "text-based
   sources", with population quintiles as "similar population". No other text-source measure exists yet.
8. **Region classes:** 040 has no region classes. Continent comes from the gazetteer. The income band is approximated
   from World Bank GDP per capita (constant 2015 US$) with fixed thresholds, because the World Bank's GNI-based groups
   are not collected. It is documented as an approximation.
9. **Monthly schedule:** 128 is not built, so `BaseRateScheduler` queues the refresh on the 1st of each month. A
   refresh also follows every finished import, so rates exist right after the first imports.
10. **Spec reconciliations (additive, with a changelog entry):**
    - the target-outcome pattern allows `.`, so `seismic:m4.5/P30D` is valid;
    - `BaseRate.unconditional.nWindows`;
    - the caveat `LOCAL_ONLY_WITHHELD`;
    - `BacktestJob.result` may be a `ReferenceImport` or the new `BaseRateRefreshResult`;
    - the five Problem codes, including `BASE_RATE_NOT_CUSTOM`, which the spec already used;
    - the `EVENT_STUDY_INVALID` note.
11. **Hypothesis outcomes** (prompt 125) do not exist yet. `hypothesisOutcome` is always null.
12. **Signature standardization** divides by the family's weekly SD of situation members over the last year. The
    prompt's "global weekly SD" has no stored source of its own. Region-period signatures use the SD across the
    candidate pool's country-weeks, and weekly totals stand in for daily counts.
13. **`POST /v1/analogs/query`** defaults `to` to now and `from` to `to` minus 14 days when they are omitted (the
    spec marks both required). The server is lenient here.
14. **US English:** new Java identifiers use `license` (`ReferenceFamilies.LICENSES`, `getLicense`). The wire and
    column names `licence` and `licences` are kept as the spec and the prompt name them.

## Issues found / open questions

- **Owner decisions:**
  - (a) ACLED skip versus 409 (Deviation 3);
  - (b) the GDP-based income band (Deviation 8);
  - (c) the monthly refresh scheduled here until 128 (Deviation 9);
  - (d) the reporting-density basis (Deviation 7);
  - (e) the appended Operator and Agent Guide sections versus "documents are replaced whole" in
    `~/Documents/GitLab/CLAUDE.md` (the same open question as 116–120);
  - (f) `OwnHistoryFold` excludes only events whose sources are all private.
- **Cost:** a full UCDP GED file holds about 300,000 rows and is read in one pass, with weekly aggregates in memory.
  USGS 1973 to today is about 53 requests. GDACS 2000 to today is about 160 or more requests. The 103 worker pool has
  one thread by default, so a long import delays other 103 jobs.
- **Restart behavior:** the 103 framework has no restart recovery, so a job interrupted by a restart stays `running`.
  Re-submitting the same import returns it while its status says running. The owner may want 103 to fail stale
  running jobs at startup.
- **New tables via Hibernate:** `reference_history` (`reference_history_import_idx`, `reference_history_region_idx`),
  `reference_import` (`reference_import_key_idx`, `reference_import_created_idx`), `base_rate` (`base_rate_key_idx`,
  `base_rate_family_idx`), `region_profile`, `situation_signature` and `situation_outcome`.
- **Checks run:**
  - US English guard (`python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`): 14 tests OK on the final tree.
  - `./gradlew lintOpenApi`: BUILD SUCCESSFUL, "Your API description is valid". Its 70 warnings are of kinds that
    predate this prompt.
- **Upstreams fetched to verify formats:**
  - the UCDP downloads page and the first 400 KB of `ged261-csv.zip` (header only);
  - the USGS FDSN documentation and two small queries;
  - the GDACS Swagger document and one search page.
  Plain requests with no personal data. No file, page or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `baserate/reference/UcdpImporterTest` | Violence-type families; ISO-week bucketing; country names with former names in parentheses and the `ZZ` fallback; `best` fatalities; CC-BY-4.0 and `OPEN` per row; file hash and period; an identical re-import; ZIP input; parameter validation and availability |
| `baserate/reference/UsgsComCatImporterTest` | WireMock: one query per year with the documented parameters; a 400 (too many) year split by time with duplicates removed; cumulative magnitude families; quarry blasts skipped; the offshore rule; resume skipping finished years |
| `baserate/reference/AcledHistoryImporterTest` | Skipped with `credentials absent`; `LOCAL_ONLY` rows under ACLED-EULA by family and week; duplicate events once; strategic developments not counted; country validation |
| `baserate/BaseRateCalculatorTest` | Jeffreys reference values and boundaries; rate, `pAtLeastOne` and the interval for 7- and 28-day windows; ACLED over UCDP without double counting; USGS before own history; gaps and `SMALL_SAMPLE`; a planted conditional dependency with the unconditional rate; determinism; definitions (validation, canonical key, reference-class text) |
| `baserate/ReportingDensityTest` | Decile assignment; population-quintile medians; the unknown-population group and the median floor; the `LOW_REPORTING_DENSITY` and `LOW_DETECTABILITY` caveats |
| `baserate/analog/SignatureAndAnalogIT` | A signature on activation and one per day, backfill and outcomes (none for merged situations); analogs excluding time overlap and merges, ranked, `feature-only` with the caveat; the outcome distribution shrunk by k ÷ (k + 10) toward the critical base rate; reciprocal-rank fusion and merge-group exclusion; signature math |
| `api/BaseRateControllersIT` | Ad-hoc query (reference class, n, Jeffreys, ACLED over UCDP, LOCAL_ONLY licenses, manifest, 24 h cache, `BASE_RATE_INVALID`); custom rates (403 for read, agent and operator origins, list filters, a stored rate answering the query, `UNKNOWN_BASE_RATE`, delete, `BASE_RATE_NOT_CUSTOM`); refresh job and 409; imports (admin only, ACLED skipped and reused, `IMPORT_DATASET_UNAVAILABLE`, list, delete removing rows, `UNKNOWN_IMPORT`); analogs (404, 400, region-period); MCP names, scopes and shapes |

## Compile result

`./gradlew compileJava compileTestJava` gave `BUILD SUCCESSFUL`. `python3 scripts/generate-postman.py docs/OpenAPI.json
postman` printed "836 requests across 80 tag folders; quickstart 12 steps; covers all ops: True".

## Notes for the next prompt

- **122 (forecast ledger):**
  - Base rates: `BaseRateService.query(definitionMap)` returns a stored or computed rate.
  - Analog forecaster: `AnalogService.forSituation(id, k, "family/PnD" | "critical", privateAccess)`. Read
    `outcomeDistribution.probability`, `analogCount` and `baseRate`.
  - Detector names: `baserate` 1.0.0 and `analog-search` 1.0.0.
- **128 (cycles):** call `BaseRateService.submitRefresh("cycle", null)` monthly. It throws
  `ConflictException(BACKTEST_IN_PROGRESS)` while one runs. Remove the monthly cron in `BaseRateScheduler` then.
- **125 (hypotheses):** fill `situation_outcome.hypothesis_outcome` (`{setId, outcome}`) in
  `SignatureService.outcome`.
- **Tests:** `foresignal.analytics.scheduled` is false in tests. Call these directly:
  - `OwnHistoryFold.fold()`;
  - `SignatureService.computeDue()` (with `clearCache()`);
  - `BaseRateService.refreshAll(JobMonitor.NONE)` (with `clearCache()`);
  - `RegionClasses.reset()`.
  Test base URLs: `foresignal.test.baserate.usgs-url` and `foresignal.test.baserate.gdacs-url`.
- **144 (Flyway):** capture the six tables and indexes above. Setting: `baserate.fold.lastWeek`.

Implementation commit: `90b58ac8ac3ff4d476aeff57265b26a9d5a819dc`
