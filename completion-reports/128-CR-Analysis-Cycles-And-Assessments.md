# 128-CR-Analysis-Cycles-And-Assessments

| | |
|---|---|
| Prompt | 128-Analysis-Cycles-And-Assessments.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `901bcdc7d3dd11c21a86991690823fcc4b9a2b65` |
| Date | 2026-10-17 |

## Summary

This prompt adds analysis cycles and assessments under `ai.foresignal.cycle`. Daily, weekly and monthly cycles build a
deterministic packet of what changed, run up to four budgeted LLM roles, and always publish an assessment that builds
on the previous one. The monthly cycle triggers the maintenance jobs, and the intel board summarizes the layer. It
compiles. **The tests are written but were not run** (owner directive 2026-10-17): no test result, coverage figure or
guard result in this report is measured.

- **Cycles** (`AnalysisCycleEntity`, table `analysis_cycle`; `CycleService`, `CycleInputs`, `CycleScope`):
  - The columns are those of the prompt, plus `max_tokens_per_cycle`, `built_in`, `running_since` (the one-run
    claim) and timestamps.
  - On first start (no cycle and no run) the service creates "Daily brief" (06:00), "Weekly assessment" (Monday 07:00)
    and "Monthly outlook" (the 1st, 08:00). All three are **disabled**, use the `server` setting's timezone and run all
    four roles.
  - `CycleScheduler` follows the 029/102 pattern: a 30-second poll with `FOR UPDATE SKIP LOCKED`, a persisted
    `next_run_at`, and a missed run that runs once with trigger `missed`. It runs at most one run per cycle and two in
    total.
  - Manual runs, on-demand situation assessments and the maintenance trigger go through the same scheduler. The
    conflicts are `CYCLE_RUN_IN_PROGRESS`, `LLM_NOT_CONFIGURED` (only when roles need a provider) and
    `MAINTENANCE_IN_PROGRESS`.
- **Packet** (`CyclePacketReader` reads, `CyclePacketBuilder` is pure, format `cycle-packet` 1.0.0):
  - Situations: new, activated, escalated, dormant and closed, from the 118 situation log with before and after.
  - Hypotheses: revisions with drivers, band changes, indicators observed, absent and of unknown coverage, assumptions
    broken and questioned, and sets due for review. A daily cycle reviews daily sets, a weekly one daily and weekly
    sets, a monthly one all.
  - Findings, with their base rates and caveats.
  - Questions opened, due within the horizon (7, 30 or 90 days) and resolved with every forecaster's score, plus
    scorecard changes.
  - Index moves of 5 points or more, and outlook windows resolved outside p10–p90.
  - Failing and degraded sources, `LOW_REPORTING_DENSITY` regions and the 125 collection gaps.
  - Scope applies to every section. Each section is ranked by score, then recency. The stored packet is complete; the
    LLM view keeps 30 items per section, records what it cut in `truncatedForLlm`, and leaves private items out unless
    the provider's egress allows them.
- **Roles** (`CycleRoles` on `CycleLlm`; prompt templates `cycle-analyst-system`, `cycle-red-team-system`,
  `cycle-forecaster-system` and `cycle-proposer-system` @1.0.0):
  - **Shared machinery.** Every role is a 028 `ToolLoop` over the 102 read catalog, which now includes
    `get_assessment`, `list_assessments` and `get_intel_board`. The roles keep the 102 egress handling: the tool
    caller, `ToolOutputFilter` and delimited untrusted data. They use a stable system prompt with a fixed tool order,
    the profile's limits capped by the run's remaining `max_tokens_per_cycle`, and `stop_reason` checks. A refusal
    skips the role.
  - **Analyst.** It writes 3–7 key judgments. Citations are checked against tool output and the packet
    (`CycleCitations`). A probability without a valid question or set is removed. The ICD 203 term is derived from the
    number. Text failing `EstimativeLanguage.lint` is regenerated once, then downgraded or dropped (`JudgmentLinter`).
    A likelihood term is stripped from the confidence statement. Each judgment explains its change against the
    previous assessment.
  - **Red team.** It challenges up to three sets (`RedTeamSelection`: the two that moved most, then the highest
    stakes). Each note has the runner-up case, disconfirming evidence, assumptions at risk and a premortem. Where the
    set has a ledger question, the red team records a forecast as `llm-redteam:{profile}`.
  - **Forecaster.** It takes questions by value of information (`QuestionPriority`), up to `maxQuestions`. Each sample
    runs an outside-view loop (criteria quoted verbatim, `get_base_rate`, `find_analogs`, `record_outside_view`) and
    then an inside-view loop on the same conversation (`record_forecast`). The samples are combined by median log-odds
    (`SampleAggregator`) and recorded as `llm:{profile}:{provider}`, one forecaster per configured provider, with the
    typed claims, `knowledgeCutoff` and inputs (samples, model, effort, prompt hash, tool order). LLM forecasts never
    touch hypothesis probabilities; the 122 ensemble caps and weights them.
  - **Proposer.** It makes 102 proposals: `propose_hypothesis_set` (validated as a 125 draft), `propose_indicator`,
    `propose_watch`, `propose_question` (validated by 122) and `propose_situation_merge`. The four new types are in
    `AnalystActions` and are executed by `AnalystExecutionService`. Proposals stay pending unless the profile allows
    actions, the type is allowed and `BenchmarkGate.proposerAutoExecutionAllowed(null)` passes.
  - **Failures.** A budget, a refusal or a failure marks only that role (`skipped` or `failed`).
- **Assessments** (`AssessmentEntity`, `AssessmentComposer`, `AssessmentDiff`, `CycleRunner`):
  - The status is `ok`, `partial`, `digest-only` or `failed`.
  - A cycle assessment has the eight sections plus "What we cannot see". An on-demand assessment has bottom line, key
    insights, what changed, outlook, risks, speculation and sources.
  - The bottom line is deterministic without an LLM; the analyst's judgments replace it, and the red team's notes
    augment the hypotheses. Every section carries `textOrigin` and data.
  - "What changed" diffs judgments, hypothesis probabilities and situations against a snapshot the previous assessment
    stored. The markdown is rendered to HTML with the 029 `BriefingHtmlRenderer`.
  - Each assessment carries the footer, a 116 manifest, its model, effort and prompt templates.
  - Delivery: `assessment.published` on SSE (topic `assessments`, cursor key `m`, `AssessmentsTopic`); the 018
    notification event `assessment.published` (in the inbox, and to channels at or above `notify_min_priority`, which
    is `normal` daily and `high` weekly and monthly); and the 027 embedding (public assessments only). A failed run
    writes a `failed` assessment and emits `cycle.run_failed`.
  - The export is `getAssessmentBundle` (`AssessmentBundleService`): cited findings, base rates, questions with
    forecasts and scores, forecasts, and sets with recent revisions, each with an envelope, provenance and manifests.
    `format=geoparquet` returns a ZIP of the 111 `GeoParquetWriter` files.
- **Monthly maintenance** (`MonthlyMaintenance`, `MaintenanceActions`, `DefaultMaintenanceActions`, table
  `maintenance_run`):
  - The eleven steps run in order, and a failure never stops the later ones: 116 re-estimation; 123 null-case sampling
    and the historical suite; the 121 base-rate refresh (which also clears the 127 impact-rate cache); the 120 re-test;
    the 122 refit, ensemble refresh and extremizing check; the 126 index fits and outlook refits; series-method
    re-selection; archival proposals; and the 122 retrospective evaluation of `llm` forecasters when the provider has
    a `knowledgeCutoff`.
  - `LlmRetrospectiveRunner` implements the 122 `RetrospectiveRunner`.
  - The monthly crons of 121, 122, 123 and 126 are removed, together with 126's 30-day age rules (see Deviation 3).
- **Routine reviews.** `CycleRunner` records the 125 review of every in-scope set due for review: a revision with cause
  `review` that changes no probability.
- **Intel board** (`IntelBoardService`, `IntelBoardBuilder`):
  - `GET /v1/intel/board` returns the top five situations, band changes and observed indicators of the last 24 h,
    questions due in 7 days, the latest assessment per cadence, the 90-day calibration (ensemble next to the operator
    from 20 resolved forecasts) and the benchmark status.
  - It stays within 8 KB through a documented truncation order.
  - The ETag excludes `generatedAt`; responses carry `Cache-Control: max-age=60`. Private objects are excluded for
    callers without private access.
- **API, MCP, docs:**
  - `api/CycleController` and `api/AssessmentController` serve all 17 operations, with `AccessPolicy` rows and the
    Problem codes `UNKNOWN_CYCLE`, `UNKNOWN_CYCLE_RUN`, `UNKNOWN_ASSESSMENT`, `CYCLE_RUN_IN_PROGRESS` and
    `MAINTENANCE_IN_PROGRESS`.
  - MCP: `get_assessment`, `list_assessments` and `get_intel_board` (read), and `run_cycle` (agent, cost-bearing).
  - The spec marks the operations implemented and has reconciliations plus a changelog entry. `pending-operations.txt`
    loses its 17 lines. Postman BODIES and captures cover creating a weekly cycle and running it; the collection was
    regenerated.
  - The Operator Guide gains "Analysis cycles (prompt 128)" and six corrected sentences. The Agent Guide gains four
    tool rows and "Assessments and the intel board". `docs/Invariants.md` gains test names.

## Files created / modified

**Created (main):**
- `cycle/`: `package-info`, `CycleVocabulary`, `AnalysisCycleEntity`, `AnalysisCycleRepository`, `CycleRunEntity`,
  `CycleRunRepository`, `AssessmentEntity`, `AssessmentRepository`, `AssessmentLogEntity`, `AssessmentLogRepository`,
  `MaintenanceRunEntity`, `MaintenanceRunRepository`, `CycleScope`, `CycleInputs`, `CycleService`, `CyclePacketItem`,
  `CyclePacketBuilder`, `CyclePacketReader`, `CycleState`, `CycleStateReader`, `CycleCitations`, `CycleOutputTools`,
  `CycleLlm`, `RoleContext`, `RoleOutcome`, `RoleSkippedException`, `TokenBudget`, `JudgmentLinter`,
  `SampleAggregator`, `QuestionPriority`, `RedTeamSelection`, `CycleRoles`, `AssessmentDiff`, `AssessmentComposer`,
  `AssessmentFilter`, `AssessmentsTopic`, `AssessmentEvents`, `AssessmentDelivery`, `CycleRunner`, `CycleScheduler`,
  `MaintenanceActions`, `DefaultMaintenanceActions`, `MonthlyMaintenance`, `LlmRetrospectiveRunner`,
  `AssessmentService`, `AssessmentBundleService`, `IntelBoardBuilder`, `IntelBoardService`.
- `api/CycleController`, `api/AssessmentController`.
- `mcp/tools/{GetAssessmentTool,ListAssessmentsTool,GetIntelBoardTool,RunCycleTool}`.
- Resources `llm/templates/cycle-{analyst,red-team,forecaster,proposer}-system@1.0.0.md`.

**Modified (main):**
- `analyst/AnalystActions`: four proposal types.
- `analyst/AnalystExecutionService`: executes the four new types.
- `analyst/AnalystRunner`: three read tools and `readToolNames()`.
- `llm/PromptTemplates`: four templates; `ROLES` moved to `Map.ofEntries`.
- `notify/ChannelTypeSpec`, `notify/ChannelInputParser` and `notify/inbox/InboxService`: the event
  `assessment.published`.
- `api/error/ErrorCodes`, `security/AccessPolicy`, `config/StartupOrder` (`CYCLES = 34`).
- `forecast/LedgerScheduler`, `baserate/BaseRateScheduler`, `benchmark/BenchmarkScheduler` and
  `outlook/OutlookScheduler`: the monthly crons removed.
- `outlook/series/SeriesForecastService`: `reselectAll`; a method is now selected only when missing.
- `outlook/regional/RegionalOutlookService`: `refitAll`; `refitDue` now refits families without a fit.

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`,
`docs/Invariants.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−17).

**Tests:** the new classes below. Modified: `stream/sse/EventSchemaConformanceTest`, which gains payload factories for
`assessment.published` and `cycle.run_failed`.

## Deviations from the prompt

1. **Proposals need a 102 profile.** Every proposal row needs a profile. Without `analystProfileId` the other roles use
   the assigned chat provider with the 102 default limits, and the proposer is `not-configured`. Proposals carry the
   cycle run id as `run_id`.
2. **Effort cap.** "Unless the profile caps it" is read as: the cadence's effort (`medium` daily, `high` otherwise) or
   the on-demand request's effort, lowered to the profile's `effort` when that is lower. The 102 profile default is
   `low`, so a cycle with a default profile runs at `low`.
3. **Monthly maintenance without an enabled monthly cycle.** The defaults are disabled, so removing the 121, 122, 123
   and 126 crons alone would stop base-rate refreshes, refits, re-tests and fits on a default install.
   `CycleScheduler.monthlyFallback` therefore runs the maintenance on the 1st at 03:30 UTC whenever no monthly cycle is
   enabled. The spec's `MaintenanceRun.trigger` gains `schedule`. 126's 30-day age rules are gone: a series method is
   selected when missing and re-selected monthly, and outlook families are fitted when missing and refitted monthly.
4. **Samples.** `ChatRequest` has no temperature, so samples vary by tool-call order ("varied tool-call ordering
   seeds", the prompt's fallback). Odd samples call `get_base_rate` first, even samples `find_analogs` first.
5. **Retrospective LLM forecasts run without tools.** The read tools cannot be forced to a past `asOf`, so
   `LlmRetrospectiveRunner` forecasts from the question alone. Leakage stays controlled by 122's `knowledgeCutoff`
   filter.
6. **Previous assessment.** "Builds on the previous one of its cadence" is implemented as the previous assessment of
   the **same cycle**, or of the same situation for on-demand runs, so the diff compares like scopes. With one cycle
   per cadence (the defaults) these are the same.
7. **Routine reviews and archival.**
   - The cycle records review revisions for due sets. These make no probability change; the cycle never changes a
     probability.
   - Archival "proposals" flag sets past their horizon (`review_flagged`); their next review archives them (125).
   - The review list is reported in the run's `warnings`.
8. **Spec reconciliations** (additive, with a changelog entry):
   - the two `assessments` SSE events carry an id (cursor key `m`);
   - `MaintenanceRun.trigger` gains `schedule`;
   - `CyclePacket` gains `cadence`;
   - the five Problem codes are listed.
9. **Feedback scope.** `submitAssessmentFeedback` is `read` without always-authenticated, matching the spec and 102's
   note feedback (`AccessPolicySyncTest`).
10. **Not built (out of scope):**
    - the benchmark components for the cycle itself (123's note suggests them; this prompt does not ask for them);
    - the 429 `LLM_BUDGET_EXCEEDED` pre-check on `createSituationAssessment` (a budget refusal during the run marks the
      role `skipped`);
    - the `social` and `coverage` blocks of 137, which stay null.
11. **Wire names kept as published** (US English rule): the footer "speculation is labelled" is the spec's published
    default and the prompt's text. It is kept verbatim in assessments, the Operator Guide quotation and the
    `CycleVocabulary.FOOTER` constant.
12. **Edited by script, not rewritten by hand:**
    - The spec, as in every earlier prompt.
    - The Operator Guide (474 KB): a section appended and six stale sentences about monthly schedules and reviews
      replaced, written whole by script. It was not read end to end in this session; the stale sentences were found by
      grep for monthly schedules and "prompt 128".
    - The Agent Guide: rows and a subsection inserted by script; only the tool and wording sections were read.
    - `docs/Invariants.md` was read whole and edited by script.

## Issues found / open questions

**Owner decisions:**
- (a) the maintenance fallback when no monthly cycle is enabled, and the removed age rules (Deviation 3);
- (b) proposer `not-configured` without a profile, and the profile effort as a cap that defaults to `low`
  (Deviations 1, 2);
- (c) automatic routine reviews by enabled cycles (Deviation 7). They write a revision per due set per run of a
  covering cycle;
- (d) the default cycles run all four roles. A manual run of a default cycle without a chat provider is refused with
  `LLM_NOT_CONFIGURED`, while scheduled runs publish digest-only;
- (e) cost: the forecaster makes two tool loops per question, sample and provider. Weekly defaults (40 questions × 3
  samples) can reach the 1.2M-token cap.

**Doc discrepancies, left for the planner:**
- `docs/Intelligence-Design.md` §12 lists MCP `get_assessment`, `get_intel_board` and `run_cycle`; the prompt also asks
  for `list_assessments`, which is built.
- §11.3 says the weekly cycle adds "proposals of sets for active situations without one"; that is left to the proposer
  prompt, not enforced.
- The design document was not edited.

**Known limits:**
- The packet reads at most 2,000 rows per section query.
- Index moves compare the first and last value of the period.
- The red team and forecaster work only on sets and questions visible to the provider's egress.

**Schema via Hibernate, for prompt 144:**
- New tables: `analysis_cycle`, `cycle_run` (indexes `cycle_run_cycle_idx`, `cycle_run_situation_idx`), `assessment`
  (indexes `assessment_cadence_idx`, `assessment_situation_idx`), `assessment_log` and `maintenance_run`.
- No new setting. A new property, `foresignal.cycles.poll-interval` (default 30s).

**Guards not run:** the US English guard, `lintOpenApi` and every contract test were not run (no-tests directive).
New and changed files were checked by grep for British spellings; the only hit is the published footer of Deviation 11.

**Risks for the owner's first test run:**
- `CycleRunIT` and `AssessmentLintIT` script the fake provider by system-prompt prefix and expect the packet to contain
  `question:` refs. Seeded rows are created at the test clock and the clock is advanced one minute so they fall inside
  the period.
- `CycleRunIT.allRoles` expects at least four `llm` forecasts: two questions × two providers.
- `CycleControllersIT.feedback` assumes `openReads` defaults on; the feedback route is `read`.
- `MonthlyMaintenanceIT.retrospectiveOnlyWithCutoff` queues a real 103 evaluation job, which then fails to reach
  `http://llm.test` in the background.

No file, page or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `cycle/CyclePacketBuilderTest` | Every section from seeded inputs with coverage, gaps and previous judgments; first-run flag; scope by region, set (`cadence-match`), category and focus; ranking and the 30-item LLM cap with the full packet stored and `truncatedForLlm`; private items stored but kept from the LLM; review-due cadence rule; duplicate changes |
| `cycle/AssessmentDiffTest` | Judgments re-estimated by question, new, dropped and changed by text; hypothesis moves with band changes; situation changes, new and no longer open; the first assessment |
| `cycle/AssessmentComposerTest` | The cycle and on-demand section lists; deterministic bottom line with numbers and terms that pass the lint; LLM judgments, red-team notes, change against the previous snapshot, footer |
| `cycle/IntelBoardTest` | ≤ 8 KB with large and extreme inputs in the documented truncation order; stable ETag as `generatedAt` moves; private flag passed to every query; calibration block |
| `cycle/SampleAggregatorTest` | Median log-odds, per-outcome medians renormalized, monotone quantile medians, empty input |
| `cycle/QuestionPriorityTest` | Stakes × 4p(1−p), ties by due date then least recently forecast |
| `cycle/JudgmentLinterTest` | Clean text; one regeneration kept; downgrade of term, row and confidence violations; drop and fallback |
| `cycle/RedTeamSelectionTest` | Movers then stakes, at most three |
| `cycle/CycleInputsTest` | Valid weekly cycle with defaults; every validation error; on-demand request defaults |
| `cycle/CycleScopeTest` | Round trip, region containment, filters only where the item has a value, focused scope |
| `cycle/CycleVocabularyTest` | Cadence defaults, priority thresholds, effort caps |
| `cycle/CycleRunIT` | Digest-only without a provider; all four roles with two scripted providers (cited judgments, red-team forecast under `llm-redteam:*`, one `llm:*:*` forecast per provider, hypothesis probabilities unchanged, pending proposal); outside view before inside view and the median log-odds; budget refusal → `skipped` with the assessment written; the predecessor link and period; on-demand sections |
| `cycle/AssessmentLintIT` | A judgment mixing a likelihood term and a confidence level is regenerated once, then downgraded with a warning so the number and term agree; a passing regeneration kept |
| `cycle/MonthlyMaintenanceIT` | Steps in order with a failure recorded and later steps run; retrospective skipped without a cutoff; one maintenance at a time; the default retrospective needs the cutoff; archival flags |
| `cycle/CycleSchedulerIT` | Disabled defaults once; a missed run runs once as `missed`; one run per cycle; `LLM_NOT_CONFIGURED` only with roles; one on-demand run per situation |
| `cycle/CycleControllersIT` | Cycle CRUD with scopes, 400, 403 and 404; run 401, 403 and 202 and runs (agent, packet on detail); assessment formats and private hiding; bundle JSON and ZIP; feedback; on-demand 401, 403, 202 and 404; intel board ETag, 304 and cache header; maintenance trigger; MCP shapes |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: BUILD SUCCESSFUL.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **129 (objects):**
  - Assessments are objects with the ref `assessment:{id}`; `AssessmentService.view` is the full view.
  - Dossiers can list a situation's on-demand assessments with `AssessmentService.list("on-demand", situationId, …)`.
- **137 (social):** fill `CyclePacket.social` and `Assessment.social` in `CyclePacketBuilder` and
  `AssessmentComposer`; they are null today.
- **Tests:**
  - Call `scheduler.setEnabled(false)` and drive `CycleRunner.queue`/`queueSituation` and `execute` directly.
  - `foresignal.analytics.scheduled` is false in tests, so the maintenance fallback never fires. Build
    `MonthlyMaintenance` with a recording `MaintenanceActions` to test the order.
  - Call `IntelBoardService.clearCache()` and `ForecasterRegistry.registerBuiltIns()` after the table truncation.
- **144 (Flyway):** capture the five tables and four indexes above.

Implementation commit: `901bcdc7d3dd11c21a86991690823fcc4b9a2b65`
