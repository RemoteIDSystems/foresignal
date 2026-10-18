# 137-CR-Social-Physical-Correlation

| | |
|---|---|
| Prompt | 137-Social-Physical-Correlation.md |
| Status | Complete (implemented and compiles; tests written and **not run** under the owner directive of 2026-10-17; the xAI backend and the four adapter cue paths have never run against a live API) |
| Implementation commit | `7a8d959ded5de6a4acd2d19102da0435fcd0ebaf` |
| Date | 2026-10-18 |

## Summary

This prompt compares what people say with what Foresignal observes:

- **Comparisons:** significant narratives' asserted claims are tested against independent physical evidence in the
  same place and window. Physical evidence means canonical events (116) and non-social findings (119) with a
  non-social originator. Absence is said only where `ObservabilityService.canSeeAbsence` holds. Statuses are
  `corroborated`, `unobserved`, `contradicted`, `insufficient-coverage` and `pending`. Each change is a revision with
  a reason and a manifest, re-evaluated every 15 minutes until the window plus the family's publication lag closes.
- **Attention gaps:** significant physical events that drew far less discussion than similar past events
  (log-normal fit, region-family fallback, 10th percentile) become `attention-gap` findings, only under adequate social
  coverage.
- **Cues:** physical findings, situations, alerts and canonical events cue targeted, budgeted searches on Bluesky,
  Reddit, X and Threads, plus optional xAI `x_search`. Caps are 50 cues a day and $1 a cue, and X spend is reserved and
  charged. Results are stored as `collection = cue`, counted SAMPLED, never baselined, and never corroborate their own
  trigger (`CUE_TRIGGER_MATCH`).
- **Lead times:** signed lead of first post over physical, news and official observation. Statistics are split at
  the 120 discovery fraction, so only out-of-sample groups support a lead statement.
- **Ledger:** organic pending comparisons open manual-resolver questions in category `social`. The comparison
  resolves them (insufficient coverage annuls with `COVERAGE_GAP`). The `social-claim` forecaster gives Jeffreys
  base rates with back-off.
- **Downstream:** situations get a `social` section. Hypothesis indicators get a new `reported` waiting state, and
  `observed` comes only from physical evidence. Assessments and packets get the `social-versus-observed` section and
  the `assessment.social` column. Chronicles get the `social-narratives` and `social-volume` layers. Notifications get
  `social.divergence` and `social.attention_gap`. Precursor mining gets social volume series as antecedents of their
  region's physical series. The benchmark gets component `social-physical` with six scenarios.
- **Surface:** `SocialCorrelationController` has 8 operations (comparisons, cues, lead times, settings). MCP tools are
  `compare_claim`, `get_social_lead_times` and `run_social_cue`. SSE events are `comparison.updated`, `cue.completed`
  and `attention.gap`. Problem codes are `UNKNOWN_COMPARISON`, `UNKNOWN_CUE` and `CUE_LIMIT_REACHED`.

## Files created / modified

**Created (main):**
- `social/correlation/`: `package-info`, `CorrelationVocabulary`, `CompareRules`, `SocialSettingsService`,
  `SocialComparisonEntity`, `SocialComparisonRevisionEntity`, `SocialCueEntity`, `ComparisonStore`,
  `PhysicalEvidenceSource`, `JdbcPhysicalEvidence`, `SocialPhysicalComparator`, `ComparisonService`,
  `ComparisonQuestions`, `SocialClaimForecaster`, `NarrativeSituationLinker`, `SocialHypothesisBridge`,
  `SocialNotifications`, `ChronicleSocialWriter`, `AttentionGapDetector`, `CueQueryBuilder`, `XaiXSearchBackend`,
  `SocialCueService`, `CueTriggers`, `CorrelationViews`, `LeadTimeStatistics`, `SocialSections`, `CorrelationJobs`.
- `social/CueRequests`, `api/SocialCorrelationController`, `mcp/tools/CompareClaimTool`,
  `mcp/tools/GetSocialLeadTimesTool`, `mcp/tools/RunSocialCueTool`, `benchmark/component/SocialPhysicalBenchmark`.
- Resources: `intel/social/social-compare.json`, `intel/social/region-languages.json`.

**Modified (main):** `SettingKeys`, `ErrorCodes`, `AccessPolicy`, `NarrativeStore`, `NarrativeViews`,
`NarrativeService`, `SocialEvidenceGrades`, `LlmClaimExtraction`, `SocialRuntime`, `SocialCollector`,
`BlueskySearchAdapter`, `RedditAdapter`, `ThreadsAdapter`, `XAdapter`, `HypothesisVocabulary`, `IndicatorEntity`,
`HypothesisScheduler`, `IndicatorWatchService`, `HypothesisService`, `SituationViews`, `AssessmentEntity`,
`AssessmentService`, `AssessmentComposer`, `CycleRunner`, `ChronicleSelector`, `ChronicleLayerCompiler`,
`ChronicleSelectorValidator`, `ChronicleSeriesSampler`, `ChannelTypeSpec`, `ChannelInputParser`,
`AnalysisSeriesRegistrar`, `CandidateGenerator`, `ScenarioType`, `BenchmarkInputs`, `AnalystRunner`,
`application.yml`, `intel/benchmark-thresholds.json`.

**Docs:** `docs/OpenAPI.json` (8 operations implemented, changelog entry), `docs/Operator-Guide.md` (replaced whole:
new section "Social versus physical (prompt 137)" and updates to channels, discovery, social signals and narratives),
`src/test/resources/openapi/pending-operations.txt` (8 lines removed, 31 left). Postman regenerated: no diff.

**Tests:** created `testsupport/social/CorrelationTestSupport` and the ten classes below; modified
`EventSchemaConformanceTest`, `ScenarioGeneratorTest`, `application-test.yml`.

## Deviations from the prompt

- **Ledger resolver:** questions use the `manual` resolver driven by the comparison, not a new resolver kind.
  Cue-found claims open no question.
- **Claims compared:** only asserted claims with a family other than `attention` and point or locality precision
  (admin-1 for large-area families).
- **Physical evidence:** canonical events plus non-social findings. Intel events and anomaly records are not read
  directly.
- **Negations:** "contradicted" negations are configured generically per family. Few families have one, and they need
  a geometry, so `contradicted` rarely fires except from `media-reuse`.
- **Precursor streams:** claim-type streams are not registered as 120 candidates (there is no selector form for
  them). Social volume series are registered with family `social` and a region, as antecedents only.
- **Lead statistics:** a comparison counts toward each platform of its narrative; `socialFirst` is platform level.
- **xAI backend:** keeps only cited post ids and URLs, never model text. Off by default; uses LLM provider id `xai`
  (openai-compatible).
- **Cue-capable platforms:** EDP `transport.search` is not called (no protocol exists). YouTube and Mastodon are not
  cue-capable; only Bluesky search, Reddit, X and Threads have cue paths.
- **Object types:** no 129 object type for `social-comparison`; the contract stays 1.5.
- **LLM threshold:** `llmClaimMinEvidenceGroups` is fixed at 5, and `PUT /v1/settings/social` refuses another value.
- **Overrides and languages:** track-record overrides are keyed by author key or channel. The region-language table
  is a curated resource.
- **Attention normalization:** attention is normalized by the region's baseline on complete-stream platforms.
- **136 bug fixed:** `NarrativeStore.upsertClaim` reset credibility on every upsert; the status-based credibility is
  now preserved. `social.trackRecord` thresholds are now honored by `SocialEvidenceGrades`.

## Issues found / open questions

Owner decisions:
- Whether `contradicted` should have richer negating sources per family (agency "all clear" feeds).
- Whether YouTube and Mastodon should get cue paths.
- Whether `social-comparison` should become a 129 object type (contract bump).
- Whether claim-type streams should become 120 candidates once a selector form exists.
- Whether the xAI backend should be enabled on any install, given its per-post billing.

## Unverified

- No test has run, so nothing in this report is measured behavior.
- The xAI `/v1/responses` `x_search` request and response shape follows docs.x.ai as read on 2026-10-18; it has never
  been called.
- The cue query syntax for Bluesky, Reddit, X and Threads has not been exercised live.

## Schema for 144

- Tables: `social_comparison`, `social_comparison_revision`, `social_cue`.
- Column: `assessment.social` (jsonb).
- App settings: `social.compare`, `social.attentionGap`, `social.cues`, `social.trackRecord`.
- Properties: `foresignal.social.correlation-scheduled`, `foresignal.social.cues-async` (both off in
  `application-test.yml`).

## Guards not run

The US English guard (`style/test_us_english.py`), the catalog version-agreement check, `lintOpenApi`, the OpenAPI
pending-operations test and every other guard were **not run** (owner directive). New identifiers and prose were
written in US English. Existing persisted keys were left unchanged.

## External data and instructions

- Read docs.x.ai (x-search tool and citations pages) on 2026-10-18 for the `/v1/responses` `x_search` contract and
  price ($5 per 1,000 posts as of 2026-09-21).
- No instructions to the agent were found in any page, file or tool output.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `social/correlation/SocialPhysicalComparatorTest` | Each status; windows and tolerances per family; large-area admin-1; detectability gating; lead times; narrative status rollup |
| `social/correlation/AttentionGapDetectorTest` | Log-normal and fallback expectations; percentile; coverage gating (`LOW_SOCIAL_COVERAGE`); finding draft and event |
| `social/correlation/SocialCueServiceTest` | Input parsing; daily cap with Retry-After; platform disabled; cost cap and X estimate; query building per language and platform |
| `social/correlation/SocialCueLoopTest` | A cue never corroborates its trigger (`CUE_TRIGGER_MATCH`), opens no question, raises no further cue |
| `social/correlation/SocialAbsenceTest` | Unobserved only under adequate coverage; insufficient-coverage otherwise; question annulment |
| `social/correlation/SocialIndependenceTest` | Narratives never count as physical evidence groups; hypothesis observed only from physical evidence |
| `social/correlation/SocialLeadLagIT` | Out-of-sample split at the discovery fraction; lead-time groups; notable threshold |
| `social/correlation/SocialIntegrationIT` | Situation section, hypothesis reported/observed/absent, assessment section, chronicle items, notifications |
| `social/correlation/SocialBenchmarkScenariosTest` | The six social scenarios and the `social-physical` component |
| `social/correlation/SocialCorrelationControllersIT` | All 8 operations, scopes, problem codes, MCP tool shapes |
| `stream/sse/EventSchemaConformanceTest` (added cases) | `comparison.updated`, `cue.completed`, `attention.gap` |
| `benchmark/scenario/ScenarioGeneratorTest` (updated) | 16 synthetic scenarios |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: exit 0.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: no diff.

## Notes for the next prompt

- Comparisons are read through `ComparisonStore` and `CorrelationViews`. Statuses are written back to claims and
  narratives through `NarrativeStore.updateClaimStatus`.
- Cue requests flow through `CueRequests` and `SocialRuntime.drainCues`/`reportCue`. A new cue-capable adapter
  implements the same drain-and-report pattern.
- **144 (Flyway):** capture the tables and column above.
- **Tests:** use `CorrelationTestSupport`.

Tests were written but **NOT run** (owner directive 2026-10-17).

Implementation commit: `7a8d959ded5de6a4acd2d19102da0435fcd0ebaf`
