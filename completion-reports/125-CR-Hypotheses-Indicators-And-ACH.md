# 125-CR-Hypotheses-Indicators-And-ACH

| | |
|---|---|
| Prompt | 125-Hypotheses-Indicators-And-ACH.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `507b2fb78eb60b915c563545f1f722233c906039` |
| Date | 2026-10-17 |

## Summary

This prompt builds rolling hypotheses under `ai.foresignal.hypothesis`. It compiles. **The tests are written but were
not run** (owner directive 2026-10-17): no test result, coverage figure or guard result in this report is measured.

- **Data model** (all via Hibernate): `hypothesis_set`, `hypothesis`, `hypothesis_revision` (append-only, every column
  `updatable = false`), `indicator`, `ach_evidence`, `key_assumption`, `collection_gap` and `hypothesis_log` (the SSE
  cursor). Every field the prompt lists is present, plus:
  - on the set: `review_flagged`, `resolved_outcome`, `last_band_notified_at` and `pending_band_change` (the 6-hour
    band-change rate limit), `last_revision_id` and `last_revision_at`;
  - on the indicator: `emerging_rule_id` (the first-stage rule) and `p_detect`;
  - on the evidence row: `refs` (re-reports of one evidence group), `evidence_group_key` and `applied` (the log-odds a
    manual row applied, reversed on removal).
- **`HypothesisUpdater`** (`indicator-bayes` 1.0.0, pure):
  - LR classes (high 5.0/0.2, medium 2.5/0.4, low 1.4/0.7, neutral 1.0) or an explicit value bounded to [0.05, 20];
  - the default absence LR `1/√LR`, scaled by `1 + (absenceLr − 1) × pDetect`;
  - one evidence group counts once (`same-evidence-group`);
  - within a family only the largest |ln LR| per hypothesis per UTC day counts (`same-family-day`); a stronger later
    driver applies only its excess;
  - damping: net evidence change per hypothesis per UTC day within ±1.5 logits (`damped`);
  - clamping to [0.02, 0.98] while active, via a box-constrained renormalization in exclusive sets;
  - the speculative-link cap `min(0.98, posterior with LR = (1 − placebo) ÷ placebo bounded to [1, 20])` (`capped`);
  - base-rate refreshes, reviews and overrides are non-evidence drivers (neither suppressed nor damped).
- **`ConfidenceAssessor`** 1.0.0, **`AchMatrix`**, **`InformationValue`** 1.0.0 and **`RevisionRationale`** are pure
  classes implementing §4.7, §3, §6a and §4.8.
- **`RevisionWriter`** is the single write path of probabilities: it runs the updater (or a logged direct change),
  recomputes confidence (per hypothesis from the observed indicators bearing on it, and for the set), writes the
  revision with drivers and the lint-clean rationale, records the `indicator-bayes` ledger forecast when the set has an
  open question, and emits `set.revised`, `set.band_changed` and the rate-limited `hypothesis.band_changed`.
- **`IndicatorWatchService`**: one 017 rule per `rule`, `series`, `intelEvent` and `situation` signal (and an optional
  `emerging: {rule}` first-stage rule), kept in sync on edit, disabled on retirement and once the indicator is
  observed or absent. It subscribes to the alert bus (watch rules) and the finding bus (`precursor-active`), and
  follows 122 resolutions for `question` signals.
- **`HypothesisService`**: creation (structure and reference validation, label references resolved, caps computed,
  ledger question opened, `created` revision), patches with optimistic locking, hypotheses, indicators (manual state
  changes), observation and absence, ACH evidence (one row per evidence group), key assumptions (a broken linchpin
  flags the set for review and lowers confidence), reviews (archive past the horizon without a question), resolution
  (confirm/refute, retire waiting indicators, fill `situation_outcome.hypothesis_outcome`) and base-rate refresh.
- **`HypothesisScheduler`** (off with `foresignal.analytics.scheduled=false`):
  - every 15 minutes: the `expected_by` judgment, then the band-change flush;
  - hourly: the collection gaps;
  - daily at 02:50 UTC: the base-rate refresh.
- **`HypothesisAnalysis`**: the ACH matrix, expected information gain with `p_detect`, collection gaps with
  recommendations, and the observability judgment of an indicator.
- **`HypothesisDrafter`**: a 028 tool loop at effort `medium` over a read catalog; it refuses a proposal before
  `get_base_rate` and `find_analogs`, then validates the draft and returns warnings. Nothing is persisted.
- **API, SSE, MCP, notifications and records**:
  - `api/HypothesisController` serves the 20 operations, and `AccessPolicy` has the rows.
  - SSE topic `hypotheses` (`HypothesesTopic`, cursor key `h`).
  - The nine MCP tools; the five read tools are also in the analyst's read catalog.
  - Notification events `hypothesis.band_changed` (inbox for daily and weekly sets), `indicator.observed` and
    `assumption.broken` (channel opt-in).
  - The `foresignal-intel` record kind `hypothesis-set` (`HypothesisSetRecordPublisher`).
  - The eight Problem codes.
- **Spec and docs**: the 20 operations are `implemented` and their lines are gone from `pending-operations.txt`. The
  spec gains the Problem codes, ids on the seven SSE events and a changelog entry. Postman bodies cover the worked
  example set, an indicator, a hypothesis, evidence, an assumption, a review and a draft; the collection is
  regenerated. The Operator Guide gains "Rolling hypotheses (prompt 125)"; the Agent Guide gains nine tool rows and
  "Hypotheses and estimative language".

## Files created / modified

**Created (main):**
- `hypothesis/`: `package-info`, `HypothesisVocabulary`, `HypothesisSetEntity`, `HypothesisEntity`,
  `HypothesisRevisionEntity`, `IndicatorEntity`, `AchEvidenceEntity`, `KeyAssumptionEntity`, `CollectionGapEntity`,
  `HypothesisLogEntity`, their eight repositories, `LikelihoodRatios`, `HypothesisUpdater`, `ConfidenceAssessor`,
  `AchMatrix`, `InformationValue`, `RevisionRationale`, `HypothesisInputs`, `HypothesisViews`, `HypothesisEvents`,
  `HypothesesTopic`, `HypothesisFilter`, `HypothesisNotifications`, `RevisionWriter`, `IndicatorWatchService`,
  `HypothesisService`, `HypothesisAnalysis`, `HypothesisScheduler`, `HypothesisDrafter`,
  `HypothesisSetRecordPublisher` and `HypothesisQuestionHook`.
- `forecast/QuestionResolutionListener`.
- `api/HypothesisController`.
- `mcp/tools/`: `ListHypothesisSetsTool`, `GetHypothesisSetTool`, `GetAchMatrixTool`, `GetInformationValueTool`,
  `ListCollectionGapsTool`, `CreateHypothesisSetTool`, `AddEvidenceTool`, `ReviewHypothesisSetTool` and
  `DraftHypothesisSetTool`.

**Modified (main):**
- `forecast/LedgerCoordinator`: after a resolution commits, calls every `QuestionResolutionListener`.
- `intel/ForesignalIntelGenerator`: the `hypothesis-set` kind and its publisher.
- `notify/NotificationRouter`: alerts of rules tagged `origin-hypothesis` route only to channels that name the rule in
  `ruleIds` (still recorded and in the inbox).
- `notify/ChannelTypeSpec`, `notify/ChannelInputParser` and `notify/inbox/InboxService`: the three events.
- `baserate/analog/SignatureService`: fills `hypothesisOutcome` from the newest resolved set of the situation.
- `analyst/AnalystRunner` (five read tools), `api/error/ErrorCodes` and `security/AccessPolicy`.

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json` and
`src/test/resources/openapi/pending-operations.txt` (−20).

**Tests:** the new classes below, plus `HypothesisFixtures`. Modified:
- `stream/sse/EventSchemaConformanceTest`: payload factories for the seven `hypotheses` events;
- `openapi/PostmanCollectionContractTest`: validators for the eleven 125 bodies.

## Deviations from the prompt

1. **Watch-rule tags.** The prompt names `indicator:{id}` and `origin:hypothesis`, but a rule tag must match
   `^[a-z0-9][a-z0-9_-]{0,31}$`, which has no colon (the same finding as checkpoint 124's `origin-analyst`). Rules are
   tagged `indicator`, `origin-hypothesis` and `ind-{id in lower case}` (30 characters); the indicator also stores
   `watch_rule_id`. The spec descriptions are reconciled.
2. **"No channels by default".** Rules hold no channel list, and a channel with `ruleIds: null` receives every
   rule's alerts. The router therefore routes alerts of `origin-hypothesis` rules only to channels that name the rule.
3. **Exclusive renormalization.** "Softmax over log-odds" is implemented as a softmax over `ln p + Σ ln LR`. A softmax
   over logits would move the distribution with no evidence at all. Damping and the 1.5 limit apply in that same
   space.
4. **Watch rules stop after a final state.** Observed and absent indicators have their rules disabled (kept with
   their alerts), so they stop producing alerts nobody reads.
5. **Observability family.** The 116 family of an indicator is the family of its watch rule's first kind, else its 119
   signal family. The place is the set's first region, else global. Rules on derived `foresignal-*` sources are judged
   by family over every source.
6. **`q_h` for information value** uses `q = s·LR / (s·LR + 1 − s)`, with the signal base rate `s` from an optional
   `signal.baseRate` (added to `IndicatorSignal`), default 0.5. An independent set sums binary gains.
7. **Manual evidence moves probabilities** through the class its ratings stand for: CC 5.0, C 2.5, I 0.4, II 0.2.
   Removing it reverses exactly what it applied.
8. **Band changes.** SSE `set.band_changed` is emitted on every band change; the rate limit applies to the
   notification. "Default on for daily or weekly" is implemented as: only daily and weekly sets send the
   notification (inbox and channels that list it); monthly sets send none.
9. **Ledger mapping.**
   - A `hypothesis-outcome` or `manual` resolver on an exclusive set opens a multiple-choice question over the
     hypotheses' `ledgerOutcome` (default: the label).
   - Every other case opens a yes/no question, which needs at least one hypothesis mapped to `yes`.
   - The question closes and resolves at the end of the horizon day (UTC).
   - `ledgerQuestion.resolver.params` is flattened into the 122 resolver.
10. **`proposalId`** (create and review) is recorded as a citation `analyst-proposal:{id}`. The 102 proposal types
    have no hypothesis type to confirm.
11. **Emerging stage.** Only `signal.emerging: {rule}` is supported; reaching it does not move the probabilities.
12. **Revision ids are monotonic per set.** ULIDs within one millisecond are random in their low bits, so a candidate
    that does not sort after the previous revision becomes the previous id plus one (under the set's row lock).
13. **Spec reconciliations (additive, with a changelog entry):** the tag wording, `HypothesisSet.reviewFlagged`,
    `AchEvidence.refs`, `IndicatorSignal.baseRate`, `priorBasis.value` (and `refreshedAt` on `Hypothesis`), ids on the
    seven SSE events, and the eight Problem codes.
14. **The draft's `valid`** ignores the advisory `BENCHMARK_DEGRADED` warning (a 123 note: check the benchmark gate).
    A component never benchmarked counts as degraded, so the warning appears until the first benchmark run.
15. **The spec is edited by script**, not rewritten whole (166,000 lines), as in every earlier prompt.

## Issues found / open questions

**Owner decisions:**
- (a) the tag names (Deviation 1);
- (b) the router change for `origin-hypothesis` alerts (Deviation 2);
- (c) the exclusive-set update space (Deviation 3);
- (d) disabling rules after observation (Deviation 4);
- (e) the band-change interpretation (Deviation 8);
- (f) **documents replaced whole.** The Operator Guide (438 KB) and Agent Guide were read end to end by a review
  subagent against the new sections. Its findings that concern 125 were applied:
  - channel and Push event lists;
  - the SSE topic list;
  - the stale "later prompts add tools" sentence;
  - the LLM privacy and always-authenticated lists;
  - the `indicator-bayes` forecaster in the ledger section;
  - clarifications in the new section.

  The files were then written whole by script, not by hand.

**Pre-existing documentation issues the review found, not fixed (outside this prompt's scope):**
- Agent Guide lines 551 and 1127 say "MCP has no admin tools", but the table lists four admin tools (098, 113).
- British spellings in older prose of both guides, for example "labelled", "catalogue", "centre", "honours", and
  "licence" in prose.
- Older lists of LLM uses at Operator Guide 2477 and 2522 omit the analyst and situation briefs.
- No file, page or tool output contained instructions directed at the agent.

**Guards not run:** the US English guard and `lintOpenApi` were not run (no-tests directive). The new and changed
files were checked by grep for British spellings; persisted identifiers such as the family id `ais-behaviour` were
left alone.

**Schema via Hibernate, for prompt 144:** the eight tables above and their indexes (`hypothesis_set_status_idx`,
`hypothesis_set_situation_idx`, `hypothesis_set_question_idx`, `hypothesis_set_idx`, `hypothesis_revision_set_idx`,
`indicator_set_idx`, `indicator_state_idx`, `indicator_rule_idx`, `ach_evidence_set_idx`, `ach_evidence_group_idx`,
`key_assumption_set_idx`, `collection_gap_set_idx`, the unique `collection_gap_indicator_uq`,
`hypothesis_log_set_idx`). No new setting keys.

**Risks for the owner's first test run:**
- Watch-rule creation depends on the rule validator accepting the `foresignal-intel` and `foresignal-analytics`
  sources and kinds in the test registry.
- An intel-event signal with `regionCodes` needs reference data (`geofenceRef`); the ITs avoid it.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `hypothesis/HypothesisUpdaterTest` | Class values; log-odds per class (independent and exclusive); explicit LR bounds; absence LR and detectability scaling; same-family suppression (batch, reversed order, history); same evidence group; daily damping (alone, with history, downward room); clamping active and inactive; exclusive renormalization with `other` and the box projection; independent sets untouched; the speculative cap with damping, the override held and lifted; base-rate refresh, never damped |
| `hypothesis/ConfidenceAssessorTest` | Low, moderate and every condition of high (families, grades, ungraded sources, reporting density); the linchpin downgrade from each level |
| `hypothesis/AchMatrixTest` | The LR → rating boundaries; weights; weighted inconsistency and the least inconsistent hypothesis; non-diagnostic rows (identical and unrated) |
| `hypothesis/RevisionRationaleTest` | The rationale passes the lint and is deterministic; no sentence carries both a term and confidence; percentages stay in their band (including 44.9% and 55.01%); the neutral fallback for labels that contain terms; suppressed, damped, capped and no-move texts |
| `hypothesis/IndicatorWatchIT` | The watch rule and its tags; an alert gives one revision, one ledger forecast, an ACH row and a disabled rule; an echo of the same record is suppressed and attached; `expected_by` with coverage → `absent` and a revision; without coverage → `unknown-coverage` and no revision; retirement disables the rule; a `question` signal observed on resolution |
| `hypothesis/HypothesisLifecycleIT` | Structure validation (other, priors, basis, speculative links, unknown finding, ledger without horizon); question creation, mapping and the first forecast; resolution confirms and refutes; review revisions, `LINT_FAILED` writing nothing, archive past the horizon; the band-change rate limit and the flush; monthly sets silent |
| `hypothesis/DraftHypothesisSetIT` | Fake `LlmClient`: base-rate and analog tools first; an early proposal refused; invalid rule specs and too few indicators as warnings; nothing persisted; HTTP scopes (403, 401, 400) |
| `hypothesis/HypothesisControllersIT` | Endpoints and scopes; optimistic locking; private sets in the API and on the `hypotheses` topic; hypotheses, indicators (manual observation, `INDICATOR_INVALID`, retire), evidence (matrix score, remove 204/404), assumptions (broken linchpin flags review), reviews, revision paging, information value and gaps; MCP scopes and shapes |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: BUILD SUCCESSFUL.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **126 (regional outlooks), 127 (impact):** register forecasters with `ForecasterRegistry.ensure`. `indicator-bayes`
  is registered on first use, not among the built-ins.
- **128 (cycles):**
  - Reviews call `HypothesisService.review(setId, HypothesisInputs.review(...), actor, true)`. Sets due are
    `HypothesisSetRepository.findByStatusAndNextReviewAtLessThanEqual`, and `reviewFlagged` marks a broken linchpin.
  - The cycle reports collection gaps from `GET /v1/collection-gaps` or `HypothesisAnalysis.listGaps`.
  - The cycle may take over the hourly gap refresh and the daily base-rate refresh in `HypothesisScheduler`.
  - Lint assessments with `EstimativeLanguage.lint`.
- **136–137 (social signals):** the evidence source `social-claim` is already accepted, and the indicator state
  `reported` is in `HypothesisVocabulary.INDICATOR_STATES`. The updater acts only on `observed` and `absent`, so a
  `reported` state moves nothing until those prompts decide how it should.
- **Tests:** `foresignal.analytics.scheduled` is false in tests. Call these directly:
  - `HypothesisScheduler.checkExpected/flushBandChanges/refreshGaps/refreshBaseRates`;
  - `IndicatorWatchService.onAlert/onEvent`.

  Call `ForecasterRegistry.registerBuiltIns()` after the table truncation.
- **144 (Flyway):** capture the eight tables and indexes listed above.

Implementation commit: `507b2fb78eb60b915c563545f1f722233c906039`
