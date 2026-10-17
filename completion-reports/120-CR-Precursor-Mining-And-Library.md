# 120-CR-Precursor-Mining-And-Library

| | |
|---|---|
| Prompt | 120-Precursor-Mining-And-Library.md |
| Status | Complete (tests written, not run; owner decisions under Issues) |
| Implementation commit | `80e8e05e8e15a6116623fc4ac93764cfc1b79364` |
| Date | 2026-10-17 |

## Summary

This prompt turns 103's one-off precursor test into a discovery and validation loop in the new package
`ai.foresignal.precursor`. Candidates are generated, tested on a discovery window with false-discovery control, and
confirmed on a later window that discovery never saw. Results go to a precursor library with a lifecycle. The prompt
also adds a monthly re-test and a live watch that writes `precursor-active` findings. Everything is deterministic, uses
no LLM, and every output says "association in stored data; not causation". Compiles; tests written, not run.

- **Naming (task 1):** nothing was renamed. 103 already used the name *precursor test* (`precursor_test`,
  `/v1/precursor-tests`, MCP `test_precursor`, job type `precursor`), so no aliases are needed and there is no
  `PrecursorRenameIT`.
- **Candidates (`CandidateGenerator`, pure):**
  - Operator pairs (`precursor_candidate`) are always included and never capped.
  - Automatic event pairs: the top 12 families per country, counted over the discovery window. Every ordered pair of
    two different families is tried with `minLag` 0 and each `maxLag` of 1, 3, 7 and 14 days. The spatial rule is
    `sameRegion` by country, or by admin1 when at least 80% of both families' occurrences in that country carry an
    admin1 code. `ZZ` forms no pairs.
  - Automatic series pairs: ordered pairs of enabled daily and weekly analysis series that share a region, a family or
    a topic. Lags are 1…14 days, or 1…8 weeks when either series is weekly.
  - The cap is `analysis.precursors.maxTestsPerRun` (500). Automatic candidates are ranked by the antecedent's count
    (or usable points), then the pair's overlap, then the key. Validated, stale and retired library patterns are left
    out. The cap and everything it cut are reported on the batch.
- **Families and occurrences (`PrecursorOccurrences`):**
  - Families are intel event types, alert rule tags, anomaly classifications per rule, change points and
    unexplained moves per signal family, and situation activations per theme.
  - Each query runs in its own read transaction with the 103 60 s statement timeout.
  - Alerts are collapsed to their 116 canonical events (the first alert per canonical event counts).
  - Mining reads non-private data only.
- **Testing (`PrecursorMiner`):**
  - Each candidate's range is the last `analysis.precursors.lookbackDays` (365), clipped to where both sides have data.
    The oldest `analysis.precursors.discoveryFraction` (2/3) is discovery and the newest third is validation. Ranges
    under 60 days, coverage gaps over 30%, fewer than 10 antecedents or too little series overlap are `insufficient`.
  - Event pairs use the 103 tester (circular-shift baseline, lift, permutation p, reverse test) with 999 shifts.
  - A miss counts only when 116 `canSeeAbsence` holds for the consequent's family in the antecedent's country over
    the lag window, cached by day. Otherwise the antecedent is excluded and counted as `lowDetectabilityExcluded`, with
    a `LOW_DETECTABILITY` caveat. The check applies when the consequent's signal family is a 116 canonical-event family.
  - Series pairs use `LeadLagCcf` (`leadlag-ccf` 1.0.0) on residuals from `Prewhitening`:
    - counts: the 119 slot baseline, residual `A(x) − A(μ₀)`;
    - other series: their own seasonal median and MAD, per window;
    - statistic: the largest |cross-correlation| over lags 1…L, with the best lag and its sign;
    - null: 500 circular block-shift surrogates, block = max(7, first lag where the ACF falls below 0.2);
    - p = (1 + #surrogates ≥ observed) ÷ 501;
    - the reverse direction is tested too, and `coMovement` is set when it is as strong.
  - FDR (`Fdr`): Benjamini–Hochberg at q = 0.10 across the batch, or Benjamini–Yekutieli when one series appears in
    more than one pair. Raw and adjusted p are both stored.
  - Survivors are re-tested on the validation window with the same definition:
    - events: `notable`, lift ≥ 1.5, and the Wilson lower bound above the base rate;
    - series: the same sign and p ≤ 0.05.
    - A survivor that fails stays `candidate`, with the reason.
- **Library and batches:**
  - `precursor` has every column of the prompt plus `candidate_key` (unique SHA-256 of the definition), `families`,
    `hits`, `stale_count`, `operator_validated`, `last_retest`, `history`, `caveats`, `manifest_id` and the timestamps.
  - `precursor_batch` has every column of the prompt plus `job_id`, `mode`, `trigger`, `status`, `insufficient`,
    `windows`, `error` and `manifest_id`. Batches and survivors each store a 116 manifest.
  - Descriptions are deterministic (`PrecursorText`): the prompt's sentence for events, and a series variant.
- **Lifecycle (`PrecursorRules`):** the re-test runs on the newest third of the range.
  - `validated → stale` when the lift falls below 1.2 or the interval includes the base rate. For series, when the
    sign flips or p rises above 0.05.
  - Stale twice in a row → `retired`. A stale precursor that validates again returns to `validated`.
  - Operator-pinned precursors keep their status.
  - Each transition is written to `history` and announced as `precursor.validated`, `precursor.stale` or
    `precursor.retired` on the SSE topic `analytics`.
- **Live watch (`PrecursorLiveWatch`):**
  - It polls every minute. New occurrences come from the intel event log (`created`), the situation log
    (`situation.activated`), the finding log (change points and unexplained moves), alerts and anomaly observations,
    each by its own id cursor (`precursor.cursor.streams`).
  - The durable seq cursor `precursor.cursor` records the change-stream position the watch has passed and joins the
    consumer clamp (`PrecursorCursorConsumer`).
  - A match with a validated event precursor's antecedent (form, regions, minimum severity) records one
    `precursor-active` finding through `FindingService.record`. It carries `stats.precursorId`, `expectedFrom` and
    `expectedTo`, and the probability `(hits + 1)/(n + 2)` shrunk toward the base rate by `n/(n + 20)`.
    `analytic_finding.precursor_id` is set.
  - At 06:15 UTC a series check fires when a validated series antecedent's latest standardized residual, known by now,
    reaches its threshold (2.0).
  - Restarts are exactly once: a re-found occurrence produces an identical finding, which `FindingService` leaves
    unchanged.
- **Schedule (`PrecursorScheduler`, UTC):**
  - Mining at 01:30 as a 103 job (`precursor-mining`, skipped while one is queued or running).
  - The live watch every minute and the series check at 06:15.
  - All of it is gated by `foresignal.analytics.scheduled`.
- **API, MCP, spec and docs:**
  - `api/PrecursorController` serves the 10 operations, including `retestPrecursors`. `PrecursorService` holds the
    one-run lock (409 `PRECURSOR_MINING_IN_PROGRESS`) and `PrecursorJobRunner` is the 103 `JobRunner`.
  - Problem codes: `UNKNOWN_PRECURSOR` (404) and `PRECURSOR_MINING_IN_PROGRESS` (409). `AccessPolicy` has the rows.
  - MCP tools: `list_precursors` and `get_precursor` (read; they join the read catalog and the analyst's tool list)
    and `mine_precursors` (agent, `mode` `mine` or `retest`).
  - Spec: the statuses are `implemented`, the 10 lines are gone from `pending-operations.txt`, and there is a changelog
    entry. Postman is regenerated with bodies for the candidate and the patch, captures for the precursor, batch,
    candidate and job ids, and new variables.
  - Docs: Operator Guide "Precursors (prompt 120)", Agent Guide tool rows, and Invariants test names (RET-2, INT-1,
    FDR-1).

## Files created / modified

**Created (main):**
- `precursor/{package-info,Candidate,CandidateGenerator,LeadLagCcf,Prewhitening,Fdr,PrecursorRules,PrecursorText,PrecursorSettings,PrecursorOccurrences,PrecursorMiner,PrecursorJobRunner,PrecursorService,PrecursorViews,PrecursorLiveWatch,PrecursorCursorConsumer,PrecursorScheduler,PrecursorEntity,PrecursorBatchEntity,PrecursorCandidateEntity,PrecursorRepository,PrecursorBatchRepository,PrecursorCandidateRepository}`
- `analytics/finding/PrecursorSummaries`, `api/PrecursorController`
- `mcp/tools/{ListPrecursorsTool,GetPrecursorTool,MinePrecursorsTool}`

**Modified (main):**
- `backtest/PrecursorTester`: adds a `test(request, shifts)` overload; 103's default of 200 shifts is unchanged.
- `analytics/finding/{FindingService,FindingEvent,AnalyticFindingEntity}`:
  - `announcePrecursor`;
  - replay of `precursor.*` log rows with the `PrecursorEvent` payload;
  - `precursor_id` set on `precursor-active` findings.
- `analytics/changepoint/ChangePointDetector`: `countBaseline` is now public, for pre-whitening.
- `security/AccessPolicy`, `api/error/ErrorCodes` and `settings/SettingKeys` (5 keys).
- `analyst/AnalystRunner`: adds the two read tools.

**Docs, spec and tooling:**
- `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md` and `docs/Invariants.md`.
- `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`.
- `src/test/resources/openapi/pending-operations.txt` (−10).

**Tests:**
- New: the classes below.
- Modified: `stream/sse/EventSchemaConformanceTest` (the three `precursor.*` events).

## Deviations from the prompt

1. **No rename:** 103 already uses the name "precursor test" (Task 1 applies only to the older name `hypothesis`).
2. **Event-tester shifts:** mining calls 103's tester with 999 shifts instead of its default 200. The smallest p-value
   is then 1/1000, not 1/201. At 1/201 a single strong pattern could not survive BH in a batch of a few dozen tests.
   This is a new overload; the 103 default and the one-off test are unchanged.
3. **Live watch input:** the prompt describes a ChangeBus/AlertBus subscriber. The watch instead reads the typed logs
   behind those streams by id cursors, in commit order. The in-memory buses drop events when a listener lags, and the
   typed logs carry fields the record payloads lack, such as intel event region tags.
   `precursor.cursor` is the change-stream seq the watch has passed and is registered in the consumer clamp as
   Conventions require. Each stream reads at most 1,000 rows per poll and catches up over later polls.
4. **"Ranges below retention":** read as "shorter than 60 days" after clipping to the lookback (365 days, within every
   class's retention) and to where both sides have data.
5. **Detectability:** `canSeeAbsence` is applied when the consequent's signal family is a 116 canonical-event family
   (seismic, fire, conflict, outage, disaster, weather-alert). Derived consequents (markets, attention, situations,
   anomaly classes) have no observability model. For them only the 30% coverage-gap rule applies, as in 103.
6. **Canonical events:** only alerts rest on records and are collapsed to canonical events. Intel events, situations,
   findings and anomaly observations are already derived objects.
7. **Private data:** mining reads non-private data only, so precursors and `precursor-active` findings are never
   private and EGR-1 holds trivially. An operator who wants a precursor over a private source cannot have one yet.
8. **Monthly re-test trigger:** 128 is not built. The re-test runs on `POST /v1/precursors/retest` (and
   `mine_precursors` with `mode=retest`). Until 128, nothing schedules it monthly.
9. **Re-test storage:** re-test statistics go to a new field `lastRetest`, so the original validation window stays
   intact. The headline statistics (`hitRate`, `baseRate`, `lift`, `nAntecedent`) follow the latest out-of-sample
   window: validation, then each re-test. The probability of a live finding uses them.
10. **Series live probability:** the prompt's formula needs hits and n. For series these come from threshold
    exceedances on the validation window: antecedent |z| ≥ 2 followed at the best lag by consequent z ≥ 2 in the
    expected direction. The base rate is the share of consequent buckets beyond the threshold in that direction.
11. **Spec reconciliations (additive, with a changelog entry):** `Precursor.lastRetest` and `history`;
    `PrecursorBatch.insufficient` and `windows`; `PrecursorWindowStats.coverageGapFraction`, `blockLength` and `reason`;
    `BacktestJob.result` may be a `PrecursorBatch`; the two Problem codes.
12. **Re-test batches** record `method: BH` and `q: 0.10` because the schema requires them. A re-test makes no
    multiple-testing adjustment: each library pattern is re-tested on its own. Its `survivors` field is the patterns
    still holding.
13. **Surrogates:** "circular block-shift" is read as shifting the whole antecedent circularly by an offset of at least
    one block length. This keeps its autocorrelation intact.

## Issues found / open questions

- **Owner decisions:**
  - (a) 999 shifts for mining (Deviation 2);
  - (b) the log-based live watch instead of bus subscriptions (Deviation 3);
  - (c) non-private-only mining (Deviation 7);
  - (d) no monthly schedule until 128 (Deviation 8);
  - (e) the appended Operator and Agent Guide sections versus "documents are replaced whole" in
    `~/Documents/GitLab/CLAUDE.md` (the same open question as 116–119);
  - (f) nightly cost: up to 500 tests. Each event test runs 999 shifts in both directions, plus a detectability lookup
    per miss, cached per day and country. Each series test runs 500 surrogates × L lags in both directions.
- **Retest description:** the description is written at validation and kept through re-tests. The current numbers are
  in the headline fields and `lastRetest`.
- **New tables via Hibernate:** `precursor` (indexes `precursor_key_idx` unique and `precursor_status_idx`),
  `precursor_batch` (`precursor_batch_started_idx`) and `precursor_candidate`.
- **Checks run:**
  - US English guard (`python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`): 14 tests OK on the final tree.
    The wire values `cancelled` (batch and job status) and the registry family `ais-behaviour` (in a test fixture) were
    kept unchanged.
  - `./gradlew lintOpenApi`: BUILD SUCCESSFUL, "Your API description is valid". Its 70 warnings are of kinds that
    predate this prompt.
- Nothing was fetched from the web. No file or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `precursor/CandidateGeneratorTest` | Top-K per region (12 of 14), ordered pairs, `ZZ` excluded; the lag grid and `sameRegion` country or admin1; cap and ranking with operator pairs first; operator pairs beyond the cap; library keys excluded; series pairs by region, family and topic with daily and weekly lags; determinism |
| `precursor/LeadLagCcfTest` | Planted positive (lag 3) and negative (lag 2) dependence at the right lag and sign; independent AR(1) not significant (fixed seed); the reverse co-movement flag; the block length rule; insufficient overlap and gaps; determinism |
| `precursor/FdrTest` | BH and BY on a known p-vector and the R reference vector; survivors; method by overlap; edge cases |
| `precursor/PrecursorRulesTest` | Event and series validation and staleness reasons; the lifecycle transitions; the shrunk probability; ISO durations and the exact description sentence; candidate keys; pre-whitening; miner helpers |
| `precursor/PrecursorValidationIT` | A planted precedence valid in both windows becomes `validated` (batch sizes, cap, adjusted p, manifest, SSE log); one present only in discovery stays `candidate` with the reason; a decayed pattern goes `stale`, then `retired`, with history and events; retired patterns are not mined again |
| `precursor/PrecursorLiveWatchIT` | An antecedent occurrence writes one `precursor-active` finding with the shrunk probability, window, members and caveats; other regions, families and candidate precursors write nothing; cursor restart exactly once; the consumer clamp cursor; selector matching |
| `api/PrecursorControllersIT` | Library list and filters, get, `UNKNOWN_PRECURSOR`; PATCH retire and pin (403 for read, history, `OPERATOR_VALIDATED`, 400s); batches (403 for read, 404); candidates create, validate, list, delete; mine and retest jobs with the 409 lock; MCP names, scopes and shapes |
| `stream/sse/EventSchemaConformanceTest` (modified) | `precursor.validated`, `precursor.stale` and `precursor.retired` payloads against `PrecursorEvent` |

## Compile result

`./gradlew compileJava compileTestJava` gave `BUILD SUCCESSFUL`. `python3 scripts/generate-postman.py docs/OpenAPI.json
postman` printed "836 requests across 80 tag folders; quickstart 12 steps; covers all ops: True".

## Notes for the next prompt

- **121 (base rates, reference history):** add `referenceFamily` to `PrecursorOccurrences.resolve` and `familyCounts`
  (it yields nothing today) and to `EventSelectorResolver`.
- **122 (forecast ledger):** subscribe to `precursor-active` findings. They are on the `analytics` topic and in
  `analytic_finding` with `precursor_id` set. `stats` holds `precursorId`, `expectedFrom`, `expectedTo`, `probability`,
  `hits`, `n`, `baseRate` and `shrinkWeight`. The precursor forecaster's probability is `stats.probability`.
- **128 (cycles):** call `PrecursorService.submit("retest", "cycle", null)` monthly. It throws
  `ConflictException(PRECURSOR_MINING_IN_PROGRESS)` while a run is queued or running.
- **Tests:** `foresignal.analytics.scheduled` is false in tests. Drive `PrecursorMiner.run(mode, trigger, null,
  JobMonitor.NONE)`, `PrecursorLiveWatch.poll()`, `rewind(Cursors)` and `scanSeries(now)`.
- **144 (Flyway):** capture `precursor`, `precursor_batch`, `precursor_candidate` and their indexes.
  - Settings: `analysis.precursors.maxTestsPerRun`, `analysis.precursors.lookbackDays`,
    `analysis.precursors.discoveryFraction`, `precursor.cursor` and `precursor.cursor.streams`.

Implementation commit: `80e8e05e8e15a6116623fc4ac93764cfc1b79364`
