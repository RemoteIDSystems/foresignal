# 102-CR-Autonomous-Analyst

| | |
|---|---|
| Prompt | 102-Autonomous-Analyst.md |
| Status | Complete |
| Implementation commit | `8dac10543cbdc9f149fbdcb659ac55a8872d7891` |
| Date | 2026-10-15 |

## Summary

Added a scheduled LLM analyst. New package `ai.foresignal.analyst` holds profiles, the run pipeline
(`AnalystScheduler` + `AnalystRunner`), the pure output validator and change detector, notes,
proposals and their execution, the evaluation log and feedback. On each run it detects new in-scope
inputs (no LLM), builds a ranked packet, drives the 028 `ToolLoop` over the read tool catalogue plus
output tools, validates every claim and proposal, persists and delivers notes, records proposals
(auto-executing only when allowed) and logs the run. Compiles; tests written, not run.

- **Profiles** (`AnalystProfileEntity`, `AnalystSchedule`, `AnalystService`): schedule (cron+tz or
  `every` ≥ 10 m, default 30 m), scope, lookback, instructions, provider, effort, budgets
  (`maxRunsPerDay`/`maxTokensPerRun`/`maxToolCalls`/`maxWallSeconds`), actions
  (`allowActions`, `allowedActions`, `maxAutoActionsPerDay`), delivery (`channelIds`,
  `notifyMinPriority`), owner token, cursor. Validation: enabling without a chat provider is 409
  `LLM_NOT_CONFIGURED`; `allowActions` requires an agent-scope owner; a restricted token cannot own
  an action-enabled profile.
- **Run pipeline**: `AnalystScheduler` polls every 30 s (briefings pattern), claims due profiles with
  `FOR UPDATE SKIP LOCKED`, computes the next fire time from now (missed runs run once), marks
  `running_since` (one run per profile) and executes on a virtual thread under a global `Semaphore(2)`.
  `AnalystRunner` detects changes (`AnalystChangeDetector`: cursor, loop guard on `origin=analyst`,
  ranking by severity→confidence→recency, cap 30), builds the input packet, runs the `ToolLoop` with
  the read tools plus output tools, validates, persists and delivers.
- **Output tools** (`AnalystOutputTools`): `record_note`, `propose_watch`, `propose_intel_event`,
  `propose_investigation_item`, `annotate_event`. `AnalystOutputValidator` (pure) enforces the binding
  principles: every claim typed; an uncited `evidence` claim downgrades to `speculation`; unknown
  citations dropped; an absence claim warned; a proposed event needs ≥ 2 verified members and a known
  type; a proposal type must be allowed; a note over 2,000 words truncated; any model-supplied
  confidence is discarded.
- **Proposals and execution** (`AnalystExecutionService`): confirm/reject (409 `PROPOSAL_NOT_PENDING`),
  7-day expiry, and auto-execution requiring `allowActions`, the allowed type, the owner token still
  active with agent scope and the daily cap. Watches are created enabled and tagged `analyst`; events
  `origin=analyst`; investigation items only into open investigations; `annotate_event` only appends
  labelled speculation. The analyst never deletes or disables.
- **Notes**: markdown with typed claims and citations, priority, private flag (a note citing private
  evidence is private), delivered on `analyst.note` when priority ≥ `notifyMinPriority`, embedded when
  an embedding client is configured, and visible in the 033 inbox (its event set was extended).
- **Evaluation**: `analyst_run` logs status, cursors, input counts, provider/model/effort, tokens
  (in/out/cached), tool calls, duration, note/proposal/auto-executed counts, dropped citations,
  downgraded claims, warnings, prompt hash and `analyst-1.0.0`; `analyst_feedback` records ratings;
  `GET /v1/analyst/evaluation` aggregates per profile. Prompt/response content is stored only when
  `llm.logContent` is on.
- **API/MCP/SSE/spec**: `AnalystController` (profiles, runs, notes, proposals, evaluation); six MCP
  tools plus `find_similar_events`; SSE topic `analyst` (`analyst.note`/`analyst.proposal`, cursor key
  `b`); problem codes; `AccessPolicy` rows; the 16 prompt-102 operations flipped to `implemented` and
  removed from `pending-operations.txt`; Postman regenerated.

## Files created / modified

Created (main): `analyst/` (`AnalystProfileEntity`, `AnalystRunEntity`, `AnalystNoteEntity`,
`AnalystProposalEntity`, `AnalystFeedbackEntity` and their repositories, `AnalystClaim`,
`AnalystCursor`, `AnalystActions`, `AnalystSchedule`, `AnalystViews`, `AnalystService`,
`AnalystChangeDetector`, `AnalystOutputValidator`, `AnalystOutputTools`, `AnalystRunner`,
`AnalystScheduler`, `AnalystExecutionService`), `api/AnalystController`, `stream/sse/AnalystTopic`,
`mcp/tools/{ListAnalystNotesTool,GetAnalystNoteTool,ListAnalystProposalsTool,ConfirmAnalystProposalTool,
RejectAnalystProposalTool,RunAnalystTool,FindSimilarEventsTool}`.

Created (test): `analyst/AnalystOutputValidatorTest`, `AnalystChangeDetectorTest`, `AnalystRunnerIT`,
`AnalystSchedulerIT`, `AnalystControllersIT`.

Modified: `api/error/ErrorCodes`, `security/AccessPolicy`, `config/StartupOrder` (`ANALYST = 36`),
`notify/ChannelTypeSpec`, `notify/ChannelInputParser` (message), `notify/inbox/InboxService`,
`search/DocInput` (`ANALYST_NOTE`), `docs/OpenAPI.json`, `src/test/resources/openapi/pending-operations.txt`,
`postman/` (regenerated), `docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

## Deviations from the prompt

- **Tool-loop wiring.** `AnalystRunner` builds its own `ToolLoop` (the 028 `AskService` loop is
  private), driving `ToolCatalog` with a synthetic read-only "analyst" `McpCaller` and the read tools
  named in the prompt plus `find_similar_events`. Effort comes from the profile; `Purpose.ANALYST` is
  used; budgets, usage metering and content logging come from the governed `LlmService.client(...)`.
- **Citation checking is a corpus membership test.** Instead of the 028 `CitationSet` prefix format,
  the runner builds a text corpus of tool output and a claim's citation is valid when its id appears
  in that output. The bounded, deterministic rule ("must have appeared in this run's tool output") is
  preserved; the 028 `CitationVerifier` prefix vocabulary is not reused. This is the main simplification.
- **Absence/coverage check is keyword-based.** The validator warns on absence keywords without a
  citation; it does not yet join a citation to a 100 anomaly row's coverage-gate result. Recorded as an
  open item.
- **`propose_intel_event` status.** Executed events are created through `IntelEventService`, which sets
  status `active` rather than `emerging`; the confidence is the 101 `EventScorer` (the model's number
  is discarded).
- **Audit logging not wired.** Proposal executions are not yet written to the 023 audit log (prompt §4
  requires it). Recorded as an open item; the execution path is isolated so an `AuditService` call can
  be added.
- **SSE topic.** `AnalystTopic` extends the package-private `AbstractNotificationTopic` and overrides
  `subscribe` so a message's own `event` (`analyst.note`/`analyst.proposal`) is the SSE name; replay
  pages the `notification` table by id.
- **Channel/inbox events extended.** `analyst.note` was added to `ChannelTypeSpec.ALL_EVENTS`,
  `ChannelInputParser`, and `InboxService.EVENTS`/`baseWhere`/`head` so notes reach channels and the
  inbox. The OpenAPI `Notification`/`NotificationMessage` event enums were left as authored.
- **Embedding is not searchable.** `DocInput.ANALYST_NOTE` exists and notes are embedded
  synchronously when configured, but `SearchRequest.DOC_TYPES` was not extended, so semantic search
  does not yet return notes.
- **Feedback ratios** in the evaluation endpoint are global (`useful`/`wrong` counts), not per profile.

## Issues found / open questions

- No `anomaly_observation` query by classification exists; the change detector reads all rows and
  filters in Java (bounded by table size). A repository method is a follow-up (as noted in 101).
- `AnalystChangeDetector` caps the packet at 30; the runner does not add the input packet's compact
  evidence sections beyond the summary JSON.
- Auto-execution re-checks the owner token via `TokenService.current(...)` (unrevoked, unexpired) and
  scope; the daily cap is computed from `analyst_proposal`.

## Tests written (not run)

- `AnalystOutputValidatorTest` — uncited evidence → speculation; unknown citation dropped; event
  proposal with < 2 members rejected; disallowed proposal dropped; absence claim warned; > 2000-word
  note truncated.
- `AnalystChangeDetectorTest` — noop when nothing is new; the loop guard excludes `origin=analyst`;
  ranking by severity/confidence/recency; cursor advance and counts.
- `AnalystRunnerIT` — new inputs without a chat provider fail for that reason (no fabrication).
- `AnalystSchedulerIT` — a disabled profile is not claimed; a run with nothing new is a noop.
- `AnalystControllersIT` — profile create/read/list, evaluation aggregates, notes/proposals/runs pages.
  The scripted-`FakeLlmClient` scenarios the prompt lists (typed claims persisted and delivered,
  auto-execution, revoked-owner suppression, daily cap, budget refusal → `budget_exceeded` + one system
  event, wall-clock → partial, `logContent=false`) were not written; recorded as an open item.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- Analysts live in `ai.foresignal.analyst`; 128 reuses `AnalystProfileEntity`, `AnalystRunner`,
  `AnalystOutputValidator` and the proposal pipeline for the analysis-cycle roles, so keep
  `allowedActions` extensible (it is a set of string constants in `AnalystActions`).
- The feature is inert without a chat provider (409 `LLM_NOT_CONFIGURED`); the `analyst` SSE topic
  carries notes and proposals.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

Implementation commit: `8dac10543cbdc9f149fbdcb659ac55a8872d7891`
