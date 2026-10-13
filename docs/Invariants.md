# Foresignal — Invariants registry

Machine-checkable list of the rules the build must keep. Each invariant has a stable id, an owner prompt (the prompt that must first make it true), the component that enforces it, and the test(s) that assert it. Later prompts may add tests but must never weaken an invariant; changing one needs a planner edit here first.

**How it is used:**
- Prompts cite invariant ids when they touch an owning component ("keeps ING-1, ING-2").
- Every checkpoint (012, 022, 041, 057, 097, 124, 139, 142) verifies that each invariant whose owner prompt is at or before it is asserted by at least one existing test. From 124 on, `InvariantRegistryTest` parses this file and checks that the named test classes exist.
- Test names below are the expected class names. A prompt that names its test differently updates this file in the same commit.
- Detailed definitions live in the referenced documents; this file is the index.

Format: `| id | invariant | owner | enforced by | tests |`

## Ingest, time and history (`docs/Core-Data-Model.md` §1–§9)

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| ING-1 | Only the writer inserts `record_version` and writes `record_head`, `record_live`, `seq_clock` (Core I1); exclusive writer lock in production | 009 | `IngestWriter`, advisory lock | `IngestWriterIT` |
| ING-2 | `seq` is allocated inside the writer transaction; every seq of a batch exceeds every seq allocated earlier (Core I2) | 009 | writer allocator | `IngestWriterIT` |
| ING-3 | `ingested_at` never decreases with seq (Core I3) | 009 | writer clock guard | `IngestClockGuardTest`, `IngestWriterIT` |
| ING-4 | Per entity, accepted `observed_at` never decreases; future values clamped; LATE dropped (Core I4) | 008 | normalizer, time contract | `TimeContractTest` |
| ING-5 | Every head has exactly one live row and the reverse (Core I10) | 009 | writer upserts, load repair | `IngestWriterIT` |
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
| SRC-5 | Credentials never appear in logs, traces or recorded URLs | 003, 005 | `UrlRedactor`, `RequestRecorder` | `UrlRedactorTest`, `RequestRecorderTest`, `FetchTraceTest`, `IngestVariantsIT` and `SampleCaptureIT` (059: path keys, `PASSWORD`, `PASSKEY` and secret members never reach the fetch log, the audit log, the log output or captured samples) |
| SRC-6 | Only enabled sources are fetched; a client request never causes an upstream fetch | 010 | scheduler | `CollectorSchedulerIT`, `NoClientTriggeredFetchIT`, `NoUpstreamFetchTest` (055: static maps make no outbound call, a public basemap URL is refused), `WeatherContextServiceTest` (056: alert weather context reads stored frames and records only) |
| SRC-7 | LAN-typed parameters resolve only to private addresses and are pinned at connect time | 004 | `ParamValidator`, `PinnedTarget` | `ParamValidatorTest`, `PinnedTargetTest`, `LocalAddressesTest` |

## Security and egress

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| SEC-1 | Unmatched `/v1/**` paths require `admin` (fail closed) | 003 | `AccessPolicy` | `AccessPolicyTest` |
| SEC-2 | The admin token is stored only as a hash and printed only in its first-start banner | 003 | `AdminTokenService`, `TokenHasher` | `AdminTokenServiceIT`, `TokenHasherTest` |
| SEC-3 | Credentials are encrypted at rest with the install key | 003 | `CredentialCipher`, `SecretKeyStore` | `CredentialCipherTest`, `SecretKeyStoreTest` |
| SEC-4 | Token scope ordering `read < agent < admin` is enforced on every operation and MCP tool; restricted tokens, feed keys and the anonymous profile stay within their restrictions on every read path | 023, 035 | `AccessPolicy`, `TokenFilter`, `PathKeyFilter`, `RestrictionClamp` | `ScopeTest`, `TokenFilterIT`, `AccessPolicySyncTest`, `RestrictionLeakMatrixIT` and `AskRestrictionIT` (035), `RestrictedReadCoverageTest` (035), `RestrictionClampTest`, `PathKeyFilterIT`, `TileKeyFilterIT` and `TileKeyRedactionTest` (050: tile keys stay within their prefixes and restrictions, one lookup and one enforcement point), `McpServerIT`, `McpToolRegistryTest`, `AgentWorkflowIT` (034, an agent token gets no admin tools and 403 on admin REST), `PlacesIT` and `McpPlaceToolsIT` (036: places are always authenticated, no coordinates for read tokens), `WatchlistRuleIT` (036: sensitive alerts hidden from anonymous callers and feed keys), `SavedViewsIT` (036: `view=` intersected with a restricted token) |
| SEC-5 | Every LLM-spending path is `alwaysAuthenticated`, even under `openReads` | 023 | `AccessPolicy`, `McpToolRegistry` (cost-bearing tools) | `AlwaysAuthenticatedIT`, `McpServerIT`, `AskServiceIT` (028), `BriefingsControllerIT` (029, run and preview), `MappingSuggestIT` (059, mapping drafts need a token) |
| EGR-1 | Private records never leave through non-private paths (API, tiles, SSE, search, LLM context, MCP, exports, sinks, federation, share links) | 039 | `EgressPolicy` | `PrivateEgressIT`, `TileKeyFilterIT` (050, never through a tile key), `EgressPolicyTest` (026, LLM context), `EmbeddingWorkerIT` (027, private sources never embedded), `AskServiceIT` (028, private records never reach a non-local provider), `LlmBriefingIT` (029, private records withheld from a non-local provider), `EnrichmentContextIT` (030, private records withheld from a non-local provider), `AskWeatherToolsIT` (056, a non-local provider's excluded sources reach the weather tools), `FeedsControllerIT` (058, private-source alerts reach Atom and JSON Feed only through the private gate), `KmlServiceTest` (058, KML records pass `EgressPolicy.allows(FEED)`), `MapStyleServiceTest` (058, a hidden private source is absent from the MapLibre style), `MappingSuggestIT` (059, a private source's sample never reaches a remote provider) |
| EGR-2 | `LOCAL_ONLY` raw values are served only to callers allowed local egress; derived standardised statistics may be shared | 062 | `EgressPolicy` | `LocalOnlyEgressIT` |
| EGR-3 | Models never change configuration; model-initiated writes are proposals executed only on confirmation or with `allowActions` and agent scope | 028 | proposal pipeline (`AskActionTools`, `AskActionService`) | `AskActionsIT`, `PromptInjectionIT`, `EnrichmentPromptInjectionIT` (030, no tools offered), `EnrichmentWeatherInputIT` (056, injected NWS alert text stays data), `MappingSuggestIT` (059, an LLM mapping draft is never stored or applied; instructions in the sample stay data) |

## Streaming and triggers

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| SSE-1 | No duplicate and no missing `change` across the replay→live switch; resync on gaps | 015 | `SseHub` | `SseReplayLiveBoundaryIT` |
| TRG-1 | For every enabled rule, persisted head state equals latest state after each observation, except a flip held by `edgeMinInterval` (Core I8) | 017 | `TriggerPersistGate` | `TriggerPersistGateTest`, `RuleSqlBuilderIT`, `RecordPredicateTruthTableTest` (036: views and `inWatchlist` share the truth table), `PlacesIT` (036: editing a place fires nothing for a stationary entity) |
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
| HYP-1 | Hypothesis probabilities change only through revisions with cause, actor, `asof_seq` and rationale; speculative links never exceed their cap without a logged override | 125 | `HypothesisUpdater` | `HypothesisUpdaterTest`, `HypothesisLifecycleIT` |
| OBJ-1 | Workspace context updates are versioned; a stale `If-Match` is rejected; clients ignore their own `originClientId` | 129 | workspace service | `WorkspaceSyncIT` |
| OBJ-2 | Projected relations mirror their source structures after reconcile; only operator relations are editable | 129 | `RelationProjector` | `RelationProjectorIT` |

## Collaboration, chronicles, exchange, news, social and external data

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| COL-1 | Work products are invisible to principals without access; every share and ACL change is audited | 130 | `AccessResolver` | `AccessResolverTest`, `CollaborationEgressTest` |
| COL-2 | TLP bounds every share, link, export, rendition and package; derived products inherit the strictest input label | 130 | `EgressPolicy` | `TlpPolicyTest`, `ReportTlpTest` |
| COL-3 | Uploaded files are never executed or served as active content | 130 | attachment service | `AttachmentIT` |
| CHR-1 | A chronicle's captured history is never removed by global retention; only its own retention or deletion removes it | 131 | chronicle storage | `ChronicleCaptureIT`, `ChronicleRetentionIndependenceIT` |
| CHR-2 | Chronicle capture never blocks ingest; capacity limits degrade visibly to events-only, never silently | 131 | `ChronicleCaptureService` | `ChronicleCaptureIT`, `ChronicleLifecycleIT` |
| RPT-1 | Published report revisions are immutable and cite pinned versions | 132 | report service | `ReportPublishIT` |
| RPT-2 | Packages are signature- and hash-verified before import; imported material is read-only and never enters local evidence or the ledger unless copied deliberately | 132 | exchange service | `PackageSigningTest`, `ExchangeRoundTripIT` |
| NEWS-1 | Media ratings come only from declared providers with licence and date; no model generates them | 133 | rating providers | `RatingProvidersTest` |
| NEWS-2 | News reprints of one originator count as one evidence group; no author names are stored | 133 | 116 resolver, news adapters | `NewsStoryClusteringIT`, `NewsIndependenceTest` |

| DISC-1 | Chronicle relationships are disclosed only within visibility, discoverability and TLP; sketches never carry content, titles or owners unless revealed | 134 | `ChronicleDiscoveryJob`, disclosure rules | `DisclosureTest`, `FederatedSketchIT` |
| DISC-2 | Shared evidence counts once across chronicles and clusters | 134 | evidence keys, cluster timeline | `ChronicleDiscoveryIT` |
| DISC-3 | Circular reporting is counted as one evidence group in hypotheses and confidence | 134 | `CircularReportingDetector` | `CircularReportingTest` |
| MEM-1 | Observations are never edited by interpretation; chronicles copy observations and snapshot interpretation; nothing downstream mutates an upstream memory | 116 | code structure, writers | `MemorySeparationTest` |
| SOC-1 | A social narrative or claim never counts as independent corroboration of a physical event or of itself; reposts and quotes inherit the original's originator | 136 | `NarrativeClusterer`, evidence groups, `SocialPhysicalComparator` | `SocialIndependenceTest`, `NarrativeClustererTest` |
| SOC-2 | `unobserved` comparisons require `canSeeAbsence` ok; attention gaps require adequate social coverage; otherwise `insufficient-coverage` / no finding | 137 | `SocialPhysicalComparator`, `AttentionGapDetector` | `SocialAbsenceTest`, `AttentionGapDetectorTest` |
| SOC-3 | Sampled and cue-found posts never feed volume baselines, and a cue never corroborates its own trigger | 137 | `SocialBaseline`, `SocialCueService` | `SocialCueLoopTest`, `SocialBaselineTest` |
| SOC-4 | A paid social call is estimated first and refused when the monthly cap would be exceeded | 135 | `XSpend`, 005 budgets | `XSpendTest` |
| SOC-5 | "Leads by" statements use only out-of-sample comparisons and validated precursor tests | 137 | lead-time statistics, 120 | `SocialLeadLagIT` |
| EXT-1 | Nothing from a pending or suspended external source is stored; manifest changes to streams, kinds, item types, modes or schemas return the source to pending | 024 | `EdpManifestService`, push endpoint | `EdpIngestIT`, `EdpManifestServiceTest` |
| EXT-2 | Atomic EDP items enter series, evidence, indicators or forecasts only through an enabled binding, and bound events count once per declared originator (honouring `aggregates`) | 138 | `BindingRouter`, originator registry | `BindingRouterIT`, `EdpEvidenceIndependenceTest` |
| EXT-3 | External forecasts never carry a `made_at` before Foresignal received the item, and external forecasters join the ensemble only after 30 resolved linked questions | 138 | forecast binding, ensemble | `EdpForecastBindingTest` |
| EXT-4 | A provider-supplied availability time (`knownAt`, `publishedAt`, `madeAt`) never makes information available before Foresignal received it: availability is `max(provider time, receipt)` | 024 | EDP item mapper, 093/116/122 readers | `EdpTimeTest` |
| EXT-5 | Only an explicit `delete` or absence from a final, fully received and validated snapshot of the same scope (within the window and the absent-ratio limit) removes an EDP entity | 024 | snapshot staging | `EdpSnapshotIT` |
| EXT-6 | Provider URLs never reach private, loopback, link-local or metadata addresses (including by DNS rebinding or redirects) unless the operator allowed private networks for that source; payload schemas cannot fetch remote references or run unbounded patterns | 024 | `EdpUrlGuard`, schema limits | `EdpUrlGuardTest`, `EdpSchemaSafetyTest` |

## Process

| Id | Invariant | Owner | Enforced by | Tests |
|---|---|---|---|---|
| PRC-1 | Every API operation is in `docs/OpenAPI.json` with `x-foresignal-scope`, in AccessPolicy and in the Postman collection; `pending-operations.txt` lists only unbuilt operations and is empty at 142 | 020 | contract tests | `OpenApiContractIT`, `PostmanCollectionContractTest` |
| PRC-2 | No GPL/AGPL code or dependency in the application | 001 | dependency and reuse checks | `LicenseAllowlistTest` |
