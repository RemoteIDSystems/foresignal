# Foresignal — Invariants registry

Machine-checkable list of the rules the build must keep. Each invariant has a stable id, an owner prompt (the prompt that must first make it true), the component that enforces it, and the test(s) that assert it. Later prompts may add tests but must never weaken an invariant; changing one needs a planner edit here first.

**How it is used:**
- Prompts cite invariant ids when they touch an owning component ("keeps ING-1, ING-2").
- Every checkpoint (012, 022, 041, 057, 097, 129, 132) verifies that each invariant whose owner prompt is at or before it is asserted by at least one existing test. From 129 on, `InvariantRegistryTest` parses this file and checks that the named test classes exist.
- Test names below are the expected class names. A prompt that names its test differently updates this file in the same commit.
- Detailed definitions live in the referenced documents; this file is the index.

Format: `| id | invariant | owner | enforced by | tests |`

## Ingest, time and history (`docs/Core-Data-Model.md` §1–§9)

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| ING-1 | Only the writer inserts `record_version` and writes `record_head`, `record_live`, `seq_clock` (Core I1); exclusive writer lock in production | 009 | `IngestWriter`, advisory lock | `IngestWriterExclusivityIT` |
| ING-2 | `seq` is allocated inside the writer transaction; every seq of a batch exceeds every seq allocated earlier (Core I2) | 009 | writer allocator | `SeqAllocatorIT` |
| ING-3 | `ingested_at` never decreases with seq (Core I3) | 009 | writer clock guard | `IngestClockGuardTest` |
| ING-4 | Per entity, accepted `observed_at` never decreases; future values clamped; LATE dropped (Core I4) | 008 | normalizer, time contract | `TimeContractTest` |
| ING-5 | Every head has exactly one live row and the reverse (Core I10) | 009 | writer upserts, load repair | `HeadLiveConsistencyIT` |
| ING-6 | `record_version` is changed only by native SQL (Core I9) | 007 | code structure | `NoJpaWritesToRecordVersionTest` |
| ING-7 | Hash input excludes volatile fields; identical content produces no new version (Core §4) | 008 | `ContentHasher` | `ContentHasherTest` |
| ING-8 | Continuously observed entities keep a version no older than the effective keyframe plus the poll interval (Core I6) | 008 | policy engine, validator | `IngestPolicyEngineTest`, `IngestPolicyValidatorTest` |
| HIST-1 | Partitions exist ahead of the writer with ≥ 1.5 blocks of headroom; drops use DETACH CONCURRENTLY and recover | 007 | `PartitionManager` | `PartitionManagerIT` |
| HIST-2 | Current-state reads touch only `record_head ⋈ record_live` (Core I5) | 014 | records API | `CurrentReadsUseHeadLiveIT` |
| HIST-3 | `asOf` reads never return versions committed after the requested transaction time; below the purge horizon they fail with `410 RESYNC_REQUIRED` | 014 | asOf query builder | `AsOfQueryIT` |

## Retention

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| RET-1 | A head is purged only when every version of it is gone (Core I7) | 019 | `RetentionJob`, `HeadPurge` | `RetentionJobIT` |
| RET-2 | Retention never deletes rows a durable consumer has not processed (consumer clamp), except after the documented release (2 × window) with an alarm | 019 | consumer clamp registry | `ConsumerClampIT` |
| RET-3 | Adapters set retention by analytic class (Conventions); intelligence tables are never touched by `RetentionJob` | 019 | descriptors, `RetentionResolver` | `RetentionResolverTest`, `FamilyDefaultsTest` |
| RET-4 | `RetentionState.purgeSeq` advances only when heads are purged | 019 | `RetentionState` | `PurgeSeqIT` |

## Sources, budgets and upstream etiquette

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| SRC-1 | Every upstream request goes through the budgeted client; no adapter opens its own HTTP connection | 005 | `UpstreamClient` | `UpstreamClientIsTheOnlyHttpClientTest`, `UpstreamClientIT` |
| SRC-2 | No source exceeds its per-minute or per-day budget, including WebSocket connects | 005 | `BudgetGovernor` | `BudgetGovernorTest`, `WebSocketBudgetIT` |
| SRC-3 | Backoff after 429/503 survives restarts and is honoured before any request | 005 | `BackoffRegistry` | `BackoffRegistryTest`, `BackoffRegistryIT` |
| SRC-4 | Usage counters are exact under concurrency and survive flushes | 005 | `UsageMeter` | `UsageMeterIT` |
| SRC-5 | Credentials never appear in logs, traces or recorded URLs | 003, 005 | `UrlRedactor`, `RequestRecorder` | `UrlRedactorTest`, `RequestRecorderTest`, `FetchTraceTest` |
| SRC-6 | Only enabled sources are fetched; a client request never causes an upstream fetch | 010 | scheduler | `CollectorSchedulerIT`, `NoClientTriggeredFetchIT` |
| SRC-7 | LAN-typed parameters resolve only to private addresses and are pinned at connect time | 004 | `ParamValidator`, `PinnedTarget` | `ParamValidatorTest`, `PinnedTargetTest`, `LocalAddressesTest` |

## Security and egress

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| SEC-1 | Unmatched `/v1/**` paths require `admin` (fail closed) | 003 | `AccessPolicy` | `AccessPolicyTest` |
| SEC-2 | The admin token is stored only as a hash and printed only in its first-start banner | 003 | `AdminTokenService`, `TokenHasher` | `AdminTokenServiceIT`, `TokenHasherTest` |
| SEC-3 | Credentials are encrypted at rest with the install key | 003 | `CredentialCipher`, `SecretKeyStore` | `CredentialCipherTest`, `SecretKeyStoreTest` |
| SEC-4 | Token scope ordering `read < agent < admin` is enforced on every operation and MCP tool; restricted tokens stay within their restrictions | 023, 035 | `AccessPolicy`, restricted-token filter | `ScopeTest`, `RestrictedTokenIT` |
| SEC-5 | Every LLM-spending path is `alwaysAuthenticated`, even under `openReads` | 023 | `AccessPolicy` | `AlwaysAuthenticatedIT` |
| EGR-1 | Private records never leave through non-private paths (API, tiles, SSE, search, LLM context, MCP, exports, sinks, federation, share links) | 039 | `EgressPolicy` | `PrivateEgressIT` |
| EGR-2 | `LOCAL_ONLY` raw values are served only to callers allowed local egress; derived standardised statistics may be shared | 062 | `EgressPolicy` | `LocalOnlyEgressIT` |
| EGR-3 | Models never change configuration; model-initiated writes are proposals executed only on confirmation or with `allowActions` and agent scope | 028 | proposal pipeline | `ProposedActionsIT` |

## Streaming and triggers

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| SSE-1 | No duplicate and no missing `change` across the replay→live switch; resync on gaps | 015 | `SseHub` | `SseReplayLiveBoundaryIT` |
| TRG-1 | For every enabled rule, persisted head state equals latest state after each observation, except a flip held by `edgeMinInterval` (Core I8) | 017 | `TriggerPersistGate` | `TriggerPersistGateTest`, `RuleSqlBuilderIT` |
| TRG-2 | Geofence edges with `hysteresisMeters` alternate and never fire from jitter inside the band | 017 | trigger engine | `GeofenceHysteresisIT` |

## Intelligence (`docs/Intelligence-Design.md`)

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| INT-1 | Detectors, scorers, updaters and forecasters are deterministic for identical inputs, seeds and `asOfSeq` | 100 | all analytic components | `DeterminismTest` |
| INT-2 | No look-ahead: live detection uses only data with `knownAt` ≤ decision time; replays and evaluations read through `asOf` | 100 | `SeriesReader`, backtest framework | `LookAheadTest`, `AnomalyBacktestIT` |
| INT-3 | LLM output never creates evidence, changes membership or changes a deterministic score | 101 | validators | `AnalystOutputValidatorTest`, `EventScorerTest` |
| INT-4 | Every analytic response separates evidence, inference and speculation | 101 | response builders | `IntelControllersIT` |
| EVD-1 | Re-reports from one originator never count as independent confirmation (confidence, corroboration, evidence quality, hypothesis updates) | 116 | `EvidenceResolver`, `EventScorer` 1.1, `HypothesisUpdater` | `EvidenceResolverIT`, `EventScorerIndependenceTest`, `DuplicateInflationTest` |
| OBS-1 | An absence or "no event" conclusion requires healthy sources and `p_detect ≥ absenceMinDetect`; otherwise `unknown` / `LOW_DETECTABILITY` | 116 | `ObservabilityService` | `AbsenceRetrofitIT`, `ObservabilityServiceTest` |
| OBS-2 | Retrospective analysis uses the observability estimate valid at the time | 116 | time-versioned estimates | `CaptureRecaptureTest` |
| MAN-1 | Every analytic output from 100 onward stores a `manifest_id`; deterministic manifests replay identically | 116 | `ManifestService` | `ManifestReplayIT`, `ManifestReplayCheckTest` |
| FDR-1 | Mining controls the false-discovery rate and validates survivors on a later, disjoint window before `validated` | 120 | precursor mining | `PrecursorValidationIT`, `FdrTest` |
| LED-1 | Forecasts are immutable; `made_at` and `asof_seq` are server-set; annulled questions are never scored | 122 | ledger | `ForecastControllersIT`, `ForecastScorerTest` |
| LED-2 | LLM retrospective evaluation excludes questions opened before the model's knowledge cutoff | 122 | retrospective evaluation | `RetrospectiveEvaluationIT` |
| LED-3 | Probabilities are stored as numbers; generated text passes `EstimativeLanguage.lint` (no mismatched term, no mixed rows, no likelihood term with a confidence level in one sentence) | 122 | `EstimativeLanguage` | `EstimativeLanguageTest`, `AssessmentLintIT` |
| BEN-1 | Benchmarks never write production analytic tables | 123 | benchmark runner | `BenchmarkIsolationIT` |
| BEN-2 | Autonomous proposals and LLM ensemble weight degrade when a dependency's latest benchmark status is `fail` | 123 | `BenchmarkGate` | `BenchmarkRunIT` |
| HYP-1 | Hypothesis probabilities change only through revisions with cause, actor, `asof_seq` and rationale; speculative links never exceed their cap without a logged override | 124 | `HypothesisUpdater` | `HypothesisUpdaterTest`, `HypothesisLifecycleIT` |
| OBJ-1 | Workspace context updates are versioned; a stale `If-Match` is rejected; clients ignore their own `originClientId` | 128 | workspace service | `WorkspaceSyncIT` |
| OBJ-2 | Projected relations mirror their source structures after reconcile; only operator relations are editable | 128 | `RelationProjector` | `RelationProjectorIT` |

## Process

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| PRC-1 | Every API operation is in `docs/OpenAPI.json` with `x-foresignal-scope`, in AccessPolicy and in the Postman collection; `pending-operations.txt` lists only unbuilt operations and is empty at 132 | 020 | contract tests | `OpenApiContractIT`, `PostmanCoverageTest` |
| PRC-2 | No GPL/AGPL code or dependency in the application | 001 | dependency and reuse checks | `LicenceAllowlistTest` |
