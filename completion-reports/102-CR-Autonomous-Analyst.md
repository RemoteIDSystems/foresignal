# 102-CR-Autonomous-Analyst

| | |
|---|---|
| Prompt | 102-Autonomous-Analyst.md |
| Status | Complete (post-review fixes applied 2026-10-16) |
| Implementation commit | `8dac10543cbdc9f149fbdcb659ac55a8872d7891` |
| Post-review fix commit | `c27889ea080240d28a3a054fc742e8c4d98888fb` |
| Date | 2026-10-15 (fixes 2026-10-16) |

## Summary

Added a scheduled LLM analyst. Package `ai.foresignal.analyst` holds profiles, the run pipeline
(`AnalystScheduler` + `AnalystRunner`), the pure output validator and change detector, notes,
proposals and their execution, the evaluation log and feedback. On each run it detects new in-scope
inputs (no LLM), builds a ranked packet, drives the 028 `ToolLoop` over the read tool catalog plus
output tools under the provider's egress rules, validates every claim and proposal, persists and
delivers notes, records proposals (auto-executing only when allowed) and logs the run. Compiles; tests
written, not run.

- **Profiles** (`AnalystProfileEntity`, `AnalystSchedule`, `AnalystScope`, `AnalystService`): schedule
  (cron+tz or `every` ≥ 10 m, default 30 m), scope (validated: sources, kinds, places, bbox, event types,
  rule tags, `minSeverity`), lookback, instructions, provider, effort, budgets
  (`maxRunsPerDay`/`maxTokensPerRun`/`maxToolCalls`/`maxWallSeconds`), actions (`allowActions`,
  `allowedActions`, `maxAutoActionsPerDay`), delivery (`channelIds`, `notifyMinPriority`), owner token,
  cursor. Validation: enabling without a chat provider is 409 `LLM_NOT_CONFIGURED`; `allowActions`
  requires an agent-scope owner; a restricted token cannot own an action-enabled profile.
- **Run pipeline**: `AnalystScheduler` polls every 30 s (briefings pattern), claims due profiles with
  `FOR UPDATE SKIP LOCKED`, computes the next fire time from now (missed runs run once), marks
  `running_since` (one run per profile) and executes on a virtual thread under a global `Semaphore(2)`.
  Manual runs (`POST /run`, MCP `run_analyst`) use the same path (`AnalystScheduler.runNow`): an atomic
  claim (409 `ANALYST_RUN_IN_PROGRESS`), the same executor and semaphore. `AnalystRunner` detects changes
  (`AnalystChangeDetector`: cursor, loop guard on `origin=analyst`, ranking by severity→confidence→recency,
  cap 30) within the profile's scope and lookback (default and cap 7 days), enforces `maxRunsPerDay`,
  builds the input packet, runs the `ToolLoop` with the read tools plus output tools, validates, persists
  and delivers.
- **Egress**: the tools read as a caller whose excluded sources are `EgressPolicy.llmExcludedSources` of
  the configured provider; every tool result passes `ToolOutputFilter`; the packet leaves out private
  events, alerts and anomalies unless the destination may receive private records. The packet and tool
  results are delimited as `{"tool": ..., "data": ...}` untrusted data.
- **Output tools** (`AnalystOutputTools`): `record_note`, `propose_watch`, `propose_intel_event`,
  `propose_investigation_item`, `annotate_event`. `AnalystOutputValidator` (pure) enforces the binding
  principles: every claim typed; an uncited `evidence` claim downgrades to `speculation`; citations must
  name exactly an item a tool returned (`AnalystCitations`); an absence claim warned; a proposed event
  needs ≥ 2 verified members and a known type and keeps only member identities (any model-supplied
  confidence, weight, family or role is discarded); a proposal type must be allowed; a note over 2,000
  words truncated.
- **Proposals and execution** (`AnalystExecutionService`): confirm/reject (409 `PROPOSAL_NOT_PENDING`),
  7-day expiry, and auto-execution requiring `allowActions`, the allowed type, the owner token still
  active with agent scope and the daily cap. Each proposal executes in its own transaction and is written
  to the 023 audit log. Watches are created enabled and tagged `analyst` and `origin:analyst`; events
  `origin=analyst`, status `emerging`, scored by `EventScorer`; investigation items only into open
  investigations; `annotate_event` only appends labeled speculation. The analyst never deletes or
  disables.
- **Notes**: markdown with typed claims and citations, priority, private flag, delivered on
  `analyst.note` when priority ≥ `notifyMinPriority`, embedded when an embedding client is configured
  (public notes only), and visible in the 033 inbox.
- **Evaluation**: `analyst_run` logs status, cursors, input counts, provider/model/effort, tokens
  (in/out/cached), tool calls, duration, note/proposal/auto-executed counts, dropped citations,
  downgraded claims, warnings, prompt hash and `analyst-1.0.0`; `analyst_feedback` records ratings;
  `GET /v1/analyst/evaluation` aggregates per profile. Prompt/response content is stored only when
  `llm.logContent` is on.
- **API/MCP/SSE/spec**: `AnalystController` (profiles, runs, notes, proposals, evaluation); six MCP
  tools plus `find_similar_events`; SSE topic `analyst` (`analyst.note`/`analyst.proposal`, cursor key
  `b`); problem codes; `AccessPolicy` rows; the 16 prompt-102 operations flipped to `implemented` and
  removed from `pending-operations.txt`; Postman regenerated in the implementation commit.

## Files created / modified

Created (main): `analyst/` (`AnalystProfileEntity`, `AnalystRunEntity`, `AnalystNoteEntity`,
`AnalystProposalEntity`, `AnalystFeedbackEntity` and their repositories, `AnalystClaim`,
`AnalystCursor`, `AnalystActions`, `AnalystSchedule`, `AnalystViews`, `AnalystService`,
`AnalystChangeDetector`, `AnalystOutputValidator`, `AnalystOutputTools`, `AnalystRunner`,
`AnalystScheduler`, `AnalystExecutionService`; post-review: `AnalystCitations`, `AnalystScope`),
`api/AnalystController`, `stream/sse/AnalystTopic`,
`mcp/tools/{ListAnalystNotesTool,GetAnalystNoteTool,ListAnalystProposalsTool,ConfirmAnalystProposalTool,
RejectAnalystProposalTool,RunAnalystTool,FindSimilarEventsTool}`.

Created (test): `analyst/AnalystOutputValidatorTest`, `AnalystChangeDetectorTest`, `AnalystRunnerIT`,
`AnalystSchedulerIT`, `AnalystControllersIT`; post-review: `AnalystCitationsTest`, `AnalystScopeTest`.

Modified: `api/error/ErrorCodes`, `security/AccessPolicy`, `config/StartupOrder` (`ANALYST = 36`),
`notify/ChannelTypeSpec`, `notify/ChannelInputParser` (message), `notify/inbox/InboxService`,
`search/DocInput` (`ANALYST_NOTE`), `docs/OpenAPI.json`, `src/test/resources/openapi/pending-operations.txt`,
`postman/` (regenerated), `docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

## Deviations from the prompt

- **Tool-loop wiring.** `AnalystRunner` builds its own `ToolLoop` (the 028 `AskService` loop is
  private), driving `ToolCatalog` with a synthetic read-only "analyst" `McpCaller` and the read tools
  named in the prompt plus `find_similar_events`. It reuses Ask's pieces for egress and delimiting
  (`EgressPolicy.llmExcludedSources`, `ToolOutputFilter`, `AskPrompts.wrap`) and the 028
  `CitableExtractor`s/`CitationSet` for citations. Effort comes from the profile; `Purpose.ANALYST` is
  used; budgets, usage metering and content logging come from the governed `LlmService.client(...)`.
  `maxToolCalls` bounds the loop's model calls (`ToolLoop.Limits.maxIterations`).
- **Citations.** The 028 `CitationVerifier` answer-text format is not used: claims carry citation ids,
  each of which must exactly name a returned record version (`source:externalId@seq` or
  `source:externalId`), alert (`alert:id`), event (`event:id` or the bare id) or anomaly (`anomaly:id`).
- **Absence/coverage check is keyword-based.** The validator warns on absence keywords without a
  citation; it does not join a citation to a 100 anomaly row's coverage-gate result.
- **Private notes and proposals.** A run in which any private record, event, alert or anomaly reached the
  model (possible only with a local provider and `llm.allowPrivateLocal`) produces private notes and
  proposals. A private note is never embedded; its notification carries `data.sourceId` of a private
  source, so channels without `privatePayload` receive the redacted form and the inbox and `analyst`
  topic hide it from callers without private access; `GET /v1/analyst/notes`, `PATCH`, note feedback,
  `list_analyst_notes`/`get_analyst_note` and the proposal lists hide private ones from such callers.
  The redacted channel text is the generic private-alert wording.
- **Scope.** `places`/`bbox` filter events (by centroid) and alerts (by their record snapshot's
  geometry); `sources`, `ruleTags` and `minSeverity` filter alerts and anomalies (anomalies by their
  rule's sources and tags); `eventTypes`/`minSeverity` filter events. Events are not filtered by source.
- **`maxRunsPerDay`** counts today's (UTC) runs that called or tried to call a model (`ok`, `partial`,
  `failed`); a capped run is recorded `budget_exceeded` with the reason and makes no model call. Manual
  runs still answer 202 and the run records the cap.
- **Confirm requires `allowActions`.** `confirm` refuses (409 `PROPOSAL_NOT_PENDING`) when the profile
  does not allow actions; the payload stays a suggestion. This follows the prompt's objective ("execute
  only when … `allowActions=true`").
- **SSE topic.** `AnalystTopic` extends the package-private `AbstractNotificationTopic` and overrides
  `subscribe` so a message's own `event` (`analyst.note`/`analyst.proposal`) is the SSE name; replay
  pages the `notification` table by id.
- **Channel/inbox events extended.** `analyst.note` was added to `ChannelTypeSpec.ALL_EVENTS`,
  `ChannelInputParser`, and `InboxService.EVENTS`/`baseWhere`/`head` so notes reach channels and the
  inbox. The OpenAPI `Notification`/`NotificationMessage` event enums were left as authored.
- **Embedding is not searchable.** `DocInput.ANALYST_NOTE` exists and public notes are embedded
  synchronously when configured, but `SearchRequest.DOC_TYPES` was not extended, so semantic search
  does not return notes.
- **Feedback ratios** in the evaluation endpoint are global (`useful`/`wrong` counts), not per profile.
- **Watch tags.** The prompt says `tags: [analyst]` (§4) and `origin:analyst` (§2.5); watches carry both.

## Issues found / open questions

- The change detector reads at most 500 anomaly observations of the lookback window (newest first);
  there is no repository query by classification.
- `AnalystChangeDetector` caps the packet at 30; the packet carries compact summaries, not the full
  evidence sections.
- `docs/Operator-Guide.md` and `docs/Agent-Guide.md` were not updated for the post-review behavior
  (private notes, `origin:analyst`, scope fields); the OpenAPI `AnalystProfile.scope` schema was not
  re-checked against `AnalystScope`.

## Tests written (not run)

- `AnalystOutputValidatorTest` — uncited evidence → speculation; unknown citation dropped; event
  proposal with < 2 members rejected; disallowed proposal dropped; absence claim warned; > 2000-word
  note truncated; LLM-supplied confidence, weight, family and role discarded.
- `AnalystCitationsTest` — every documented citation form valid when returned; substrings, other
  versions and unreturned ids invalid.
- `AnalystScopeTest` — empty scope, `minSeverity` on alert severities (rejects `normal`), rule tags,
  area, malformed members.
- `AnalystChangeDetectorTest` — noop when nothing is new; the loop guard excludes `origin=analyst`;
  ranking by severity/confidence/recency; cursor advance and counts.
- `AnalystRunnerIT` (shared Ask context, scripted `FakeLlmClient`) — a note with typed claims persisted
  and delivered with exact citations; packet and tool results delimited; private records never sent to a
  remote provider; a private note with a local provider (and hidden from readers over HTTP); proposals
  pending without `allowActions`; auto-executed with an agent owner (watch enabled, `origin:analyst`);
  not executed after the owner is revoked; daily cap; LLM confidence discarded and the event scored by
  `EventScorer` (`emerging`, `origin=analyst`); budget refusal → `budget_exceeded` + one system event;
  call limit → `partial`; `logContent=false` stores no content; `maxRunsPerDay` makes no model call; the
  default `notifyMinPriority` with a scope `minSeverity` runs; `POST /run` 401 without a token under
  `openReads`, 403 for read, 409 while running; `allowActions` needs agent.
- `AnalystSchedulerIT` — a disabled profile is not claimed; a noop run; a missed schedule runs once and
  is not replayed; a running profile is claimed by neither the poll nor `runNow` (409) and a stale claim
  is reclaimed.
- `AnalystControllersIT` — profile create/read/list, evaluation aggregates, notes/proposals/runs pages.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL (implementation and post-review commits).

## Notes for the next prompt

- Analysts live in `ai.foresignal.analyst`; 128 reuses `AnalystProfileEntity`, `AnalystRunner`,
  `AnalystOutputValidator` and the proposal pipeline for the analysis-cycle roles, so keep
  `allowedActions` extensible (it is a set of string constants in `AnalystActions`). New callers must keep
  the egress handling (`toolCaller`, `ToolOutputFilter`, private notes) and the per-proposal transactions.
- The feature is inert without a chat provider (409 `LLM_NOT_CONFIGURED`); the `analyst` SSE topic
  carries notes and proposals.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

## Post-review fixes (2026-10-16)

Commit `c27889ea080240d28a3a054fc742e8c4d98888fb` (`102: fix post-review defects`). It builds on the 101
fixes (`b456967682f9528881a33ef20b48687494118fdc`): server-derived member weights and families and the
private-event rule.

1. **Every run failed (critical).** Detection called `Severity.fromWire(notifyMinPriority)` (`normal`);
   it now uses the scope's `minSeverity` (validated as `info|warning|critical` when a profile is saved).
2. **EGR-1 (critical).** The tool caller excluded nothing and the packet and tool output were unfiltered;
   the runner now applies the provider destination's `llmExcludedSources`, `ToolOutputFilter` and a
   private-aware packet, and notes/proposals of runs that saw private data are private (see
   "Deviations"). `find_similar_events` hides private events.
3. **INT-3 analyst path (high).** `propose_intel_event` keeps member identities only; the validator and
   the executor drop model numbers; `IntelEventService` derives weights and families (101 fix).
4. **Untrusted tool results (high).** Packet and tool results are delimited with `AskPrompts.wrap` and the
   system prompt marks data as never instructions; proposals stay non-executing unless `allowActions` and
   an agent-scope owner.
5. **Rollback-only transactions (high).** `autoExecute` is no longer transactional and each proposal
   executes in a `REQUIRES_NEW` transaction, so one failing proposal cannot roll back the others.
6. **Unbounded manual runs (high).** `POST /run` and `run_analyst` use `AnalystScheduler.runNow` (atomic
   claim, 409, global semaphore); the runner enforces `maxRunsPerDay`.
7. **Citation check (medium).** Exact matching (`AnalystCitations`) replaces `corpus.indexOf`.
8. **Unbounded anomaly read (medium).** Bounded to 500 observations of the lookback window.
9. **Private investigation items (medium).** Proposals add items with the owner's private access only
   (101 fix enforces it).
10. **Watch tags (low).** Watches carry `origin:analyst` (and `analyst`).
- **Undisclosed gaps closed:** scope, lookback and `maxRunsPerDay` are used; proposal executions are
  written to the 023 audit log (`targetType` `analyst_proposal`, with profile and run ids); analyst events
  start `emerging`.
- **Corrections to this report:** the original claimed a note citing private evidence is private (the
  flag was hard-coded false), one run per profile (manual runs bypassed the claim and never persisted
  it), ranking honoring `minSeverity` and scope filtering (the profile's scope and `minSeverity` were
  never read; detection parsed the note priority as a severity and failed), and that budgets applied per
  profile (`maxRunsPerDay` and the lookback were stored but unused). It also listed the audit-log gap and
  the missing scripted runner tests as open items; both are now addressed.
