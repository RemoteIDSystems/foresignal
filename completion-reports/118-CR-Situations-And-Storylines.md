# 118-CR-Situations-And-Storylines

| | |
|---|---|
| Prompt | 118-Situations-And-Storylines.md |
| Status | Complete (tests written, not run; owner decisions under Issues) |
| Implementation commit | `020028e2c21327a5448b4e176cb29bd77702f3df` |
| Date | 2026-10-17 |

## Summary

Situations: persistent storylines that group intelligence events, findings, alerts, anomalies, records and
investigations, with a lifecycle, daily deterministic snapshots, an optional self-refreshing LLM brief, a
deterministic linker, operator and agent actions, REST, SSE, MCP, notifications and `foresignal-intel` records.
Compiles; tests written, not run.

- **Data model (`ai.foresignal.situation`, Hibernate `ddl-auto=update`):** `situation` (`SituationEntity`: theme,
  status, severity and `severity_override`, `region_codes` text[], `geom` = buffered hull, `centroid`, `area_km2`,
  started/last-activity/closed times, `topic_ids`/`entity_ids`/`investigation_ids` text[], `summary`, `brief` +
  model, time and manifest, `origin`, `linker_version`, `merged_into`, `private`, `version` optimistic lock, plus
  the derived `member_count`, `signal_families`, `corroboration_share`, `manifest_id` and the brief bookkeeping
  `brief_core_mark`/`brief_pending`); `situation_member` (ULID id, unique `(situation_id, member_type, ref, seq)`,
  pinned `seq`/`version`, kind, family, title, severity, theme, `observed_at`, role, `join_reason` jsonb,
  corroboration, and what the member carried when linked: geometry, regions, entities, `evidence_refs`, source,
  `private_member`); `situation_snapshot` (PK `(situation_id, day)`, `member_counts`, `new_members`, `area_km2`,
  `region_codes`, `key_values` empty before 119, `asof_seq`, `manifest_id`); `situation_log` (actor, action, the SSE
  event it announced, before/after); `situation_theme_cache` (LLM classifications by text hash). Indexes
  `(status, last_activity_at DESC)` (JPA), GiST(`geom`) and GIN(`region_codes`) (`SpatialIndexInitializer`).
- **Linker (`SituationLinker`, `situation-linker 1.0.0`):** a `ChangeBus` subscriber with the durable cursor
  `situation.cursor` (`SituationCursorConsumer` joins 019's consumer clamp), 10k-seq chunks on its own virtual
  thread, the chunk end never past the trigger engine's cursor (so the alerts of a seq exist before it is read).
  `LinkerInputReader` normalizes the four input kinds (intel events `emerging|active`, findings ≥ notable, alerts
  tagged `situation` or critical, `activity-up|down` anomalies) with regions (040), entities (098 `entity_link`),
  themes, evidence refs and the embedding doc id. `SituationScorer` + `LinkerWeights` implement every constant of
  §2 (region 0.30/0.20, distance 0.25 × max(0, 1 − d/300|500), entity 0.25, topic 0.15, text 0.20 ×
  max(0,(cos − 0.70)/0.30), theme −0.20, recency 0.5 + 0.5·exp(−h/72)); best ≥ `linkThreshold` (0.45) joins,
  otherwise an active warning+ event, a significant finding or a critical alert seeds "{type} — {region}". Shared
  members are not re-linked (directly, through an intel event pinning the record, or the record an alert fired on).
  `dryRun` runs the identical procedure over an in-memory overlay of the stored situations
  (`POST /v1/situations/linker/test`). Chunks that joined or seeded record a 116 manifest on the situations
  touched.
- **Themes (`ThemeTable`, `intel/themes.json`):** event types, finding types, rule tags, record kinds and keyword
  rules; operator rows from `intel.themes` merged first; optional LLM classification (`intel.themes.llmClassify`,
  default false; effort low, budgeted, egress-checked, validated against the fixed list, cached by SHA-256,
  `themeSource: llm`).
- **Lifecycle, summary, corroboration (`SituationLifecycle`, `SituationDeriver`, `SummaryTemplate`,
  `CorroborationRollup`, `SituationGeometry`):** emerging → active at ≥ 2 core families **and** ≥ 2 independent
  evidence groups (116 roots; echoes never twice) or by operator; open → dormant after `dormancyWindow` (7 d);
  dormant → active on a new core member; dormant → closed after 90 d; closed final. Severity = max core severity
  unless overridden. Template summary 1.0.0. Per-member corroboration (corroborated / uncorroborated / unknown /
  n/a) through the new `EvidenceService.independentGroupsOfEvents`; `corroborationShare`; caveat
  `UNCORROBORATED_ONLY`. Geometry: convex hull buffered 25 km in a local km projection, simplified to ≤ 500
  vertices, with area.
- **Snapshots (`SituationSnapshots`, `SituationScheduler`):** 00:10 UTC for the previous day, written once per
  non-closed situation, manifest recorded; `changes(since)` diffs the snapshot of `since`'s UTC day (or the newest
  before it) with now: status, severity, new members by family/type, area delta, regions added/removed.
- **Brief (`SituationBriefService`, template `situation-brief-system@1.0.0`):** off by default
  (`situation.briefs.enabled`); triggers activation, severity change, ≥ 3 new core members; debounce
  `briefMinInterval` (30 min), cap `maxPerDay` (50, attempts counted from the log); 028 `ToolLoop` with the read
  catalog + `record_note`, effort low, packet = the situation's members, 102 `AnalystOutputValidator`; analyst
  egress rules (private situation never to a remote provider, excluded sources left out); previous brief to the log;
  failure/budget keeps it; async, never blocks linking; pending briefs retried every 5 min.
- **Actions (`SituationService`):** create (all-or-nothing verified members), patch (rename, theme, override,
  activate/reopen, close, topics/entities/investigations), add member (201/200, `moveFromSituationId`), remove,
  merge (older id survives, merged one closed with `merged_into`, record tombstoned), split; every change logged
  with the actor; `SITUATION_VERSION_CONFLICT`, `SITUATION_CLOSED`, `SITUATION_MEMBER_NOT_FOUND`,
  `UNKNOWN_SITUATION`.
- **API/SSE/MCP/notifications/records:** `api/SituationsController` (13 operations, `AccessPolicy` rows; brief
  `AGENT_ALWAYS_AUTHENTICATED`); topic `situations` (cursor key `u`, order 55; filters `situations.status`,
  `situations.theme`; private-aware); MCP `list_situations`, `get_situation`, `get_situation_changes` (read
  catalog, added to the analyst's tool list) and `create_situation`, `link_to_situation`, `merge_situations`
  (agent); notification event types `situation.activated` / `situation.escalated` (channel events, inbox lists),
  off by default; `SituationRecordPublisher` driven by `ForesignalIntelGenerator` publishes kind `situation`
  (emit on payload change, tombstone on merge, on turning private, or 1825 d after closure).
- **Spec, docs, tooling:** the 13 operations `implemented` and removed from `pending-operations.txt`; four Problem
  codes; changelog 118; Postman regenerated with bodies for create, update, add member, merge, split and the linker
  test, and captures for `situationId`/`situationMemberId`; Operator Guide "Situations (prompt 118)"; Agent Guide
  tool rows; `docs/Invariants.md` INT-3, INT-4 and EGR-1 test names.

## Files created / modified

**Created (main):** `situation/{SituationEntity,SituationMemberEntity,SituationSnapshotEntity,SituationLogEntity,
SituationRepository,SituationMemberRepository,SituationSnapshotRepository,SituationLogRepository,
ThemeClassificationEntity,ThemeClassificationRepository,ThemeTable,SituationThemes,SituationMemberTypes,Severities,
SituationLifecycle,SituationGeometry,LinkerWeights,LinkerInput,LinkerInputReader,SituationScorer,SituationSimilarity,
SituationLinker,SituationCursorConsumer,SituationSettings,SummaryTemplate,CorroborationRollup,SituationDeriver,
SituationService,SituationViews,SituationMapper,SituationEvent,SituationEventBus,SituationListener,SituationFilter,
SituationsTopic,SituationRecordPublisher,SituationSnapshots,SituationNotifications,SituationBriefService,
SituationScheduler,package-info}`, `api/SituationsController`, `mcp/tools/{ListSituationsTool,GetSituationTool,
GetSituationChangesTool,CreateSituationTool,LinkToSituationTool,MergeSituationsTool}`; resources
`intel/themes.json`, `llm/templates/situation-brief-system@1.0.0.md`.

**Modified (main):** `intel/ForesignalIntelGenerator` (kinds, `embedFields("title", "summary")`, situation
publisher), `evidence/EvidenceService` (`independentGroupsOfEvents`), `analyst/AnalystRunner` (situation read tools;
`toolCaller` public), `llm/PromptTemplates`, `notify/{ChannelTypeSpec,ChannelInputParser,inbox/InboxService}`,
`security/AccessPolicy`, `api/error/ErrorCodes`, `settings/SettingKeys`, `config/{StartupOrder,
SpatialIndexInitializer}`, `resources/application.yml`.

**Docs/spec/tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`, `docs/Invariants.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−13), `src/test/resources/application-test.yml`.

**Tests:** the eight classes below; modified `stream/sse/EventSchemaConformanceTest` (the seven `situation.*`
events) and `config/SpatialIndexInitializerTest` (pin brought up to date: it already lacked the 098 and 114
statements).

## Deviations from the prompt

1. **Corroboration for intelligence-event members** uses the canonical events of the event's pinned member records
   (the strongest one decides); records use their own. The situation's independent evidence groups are the
   distinct 116 originator roots of all core members' records (unlinked records count by their source's root).
2. **Signal family of an intel-event member** is the first (sorted) of the event record's `signalFamilies`; anomalies
   join as `context` members with family `activity-anomaly` (they never drive status or severity); records use
   `SignalFamilies.forKind`, or the kind itself where that falls back to `conflict` for an unknown kind.
3. **Finding severities** map notable → info, significant → warning, critical → critical (`Severities.normalize`).
4. **Emerging situations also go dormant** after `dormancyWindow` (the spec names only active → dormant), so they
   stop being candidates and can close.
5. **Score rounding:** the score is rounded to nine decimals so 0.30 + 0.15 equals the 0.45 threshold exactly.
   `maxDistanceKm` is 500 when either the input or the situation is maritime. Distance is measured to the buffered
   situation geometry.
6. **Text similarity** compares the input record's embedding with the embedding of the situation's own
   `foresignal-intel` record (title + summary stem); to make that record embeddable `foresignal-intel` now declares
   `embedFields("title", "summary")`, which also embeds intelligence-event titles. Nothing is embedded for the
   linker itself; a freshly seeded situation has no vector until its record is indexed.
7. **Summary "Last activity {ago}"** is rendered at read time; the stored summary is the rendering at change time,
   and the record payload carries the stem without that sentence (an added `summary` field, plus `private: false`,
   recorded in the spec changelog).
8. **LLM theme classification at temperature 0** is not expressible: `ChatRequest` has no temperature member. It
   uses effort low, a 16-token cap, a one-word instruction and validation against the fixed list.
9. **Linker chunk end** is bounded by the trigger engine's cursor when the engine runs (alerts are inputs).
10. **Alerts on `situation` records** are never linker inputs (they would feed a situation back into itself).
11. **Actor** on REST changes is the token id (`CurrentToken.id()`); origin is `operator` for admin tokens and
    `agent` otherwise. A merge closes the merged situation (status `closed`, `closedAt`) as well as setting
    `merged_into`.
12. **Brief daily cap** counts brief attempts (stored and failed `brief` log rows); a capped or debounced attempt
    writes no row. A brief of a private situation is refused when the provider may not receive private records.
13. **Members/timeline paging** uses offset cursors (opaque); the situation list uses a keyset cursor on
    `(last_activity_at, id)` as specified. `since` on the list filters `updated_at`.
14. **Not computed yet:** `LOW_REPORTING_DENSITY` caveats and `coverage` (no regional density source before 119/133),
    `social` (137) and snapshot `key_values` (119) are returned null/empty.
15. **Spec reconciliations (additive):** `streamEvents` gains `situations.status` and `situations.theme`; Problem
    lists the four situation codes; changelog entry. Docs were extended with new sections, as earlier prompts did.

## Issues found / open questions

- **Owner decisions:** (a) Deviation 6, embedding `foresignal-intel` records (adds embedding cost for intelligence
  events and situations); (b) Deviation 4, emerging → dormant; (c) Deviation 2, anomalies as context members;
  (d) appended doc sections vs `~/Documents/GitLab/CLAUDE.md` "documents are replaced whole" (same open question as
  116/117); (e) the theme rows of `intel/themes.json` (for example seismic events and earthquakes as `mixed` /
  `weather`) are judgments.
- **Pre-existing test drift** found while touching them: `SpatialIndexInitializerTest`'s pin lacked the 098 and 114
  statements (fixed here); `EventSchemaConformanceTest` still lacks the 101/102 `intel.*` and `analyst.*` entries
  (noted by 116 for checkpoint 124); `RestrictedReadCoverageTest` expects every MCP tool to carry `@RestrictedRead`
  and some 101 agent tools do not (the six new tools do).
- The linker relies on 040 region tags; without reference tables the bundled country polygons give countries only
  (no first-level regions), so in such deployments region scores top out at 0.20.
- New tables via Hibernate: `situation`, `situation_member`, `situation_snapshot`, `situation_log`,
  `situation_theme_cache`; new indexes `situation_status_activity_idx`, `situation_member_pin_key`,
  `situation_member_situation_idx`, `situation_member_ref_idx`, `situation_log_situation_idx`,
  `situation_geom_gist`, `situation_region_codes_gin`.
- US English guard (`python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`): ran on the final tree, 14 tests
  OK. Existing wire names (`ais-behaviour`, the spec's own "labelled" wording) were left unchanged.
- `./gradlew lintOpenApi`: BUILD SUCCESSFUL.
- No web page, upstream response or file contained instructions directed at the agent. Nothing was fetched; no
  dependency was added.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `situation/SituationLinkerScoreTest` | Every `LinkerWeights` constant; region, distance (300/500 km), entity, topic, text-similarity terms; theme penalty and compatibility; clamp; recency decay; threshold boundary exactly 0.45; prefilter; seeded titles |
| `situation/SituationLinkerIT` (`TestRecords`) | Critical event seeds an emerging situation; a second family from another source joins and activates it; unrelated region seeds its own; low-severity unmatched input is skipped; shared entity joins across > 800 km; rewound cursor exactly-once; dry run equals live over the same range and stores nothing |
| `situation/SituationLifecycleTest` | emerging → active → dormant → active → closed, closed final; emerging → dormant; operator moves; summary template; geometry (buffer, area, vertices, determinism); severity mapping |
| `situation/SituationSnapshotIT` | Snapshot written once per day; `/changes` over two days (severity, new members, area, baseline day); no baseline before start; closed situations skipped |
| `situation/SituationMergeSplitIT` | Merge keeps the older id, moves members, logs both sides, tombstones the merged record; split moves members and logs both; 409 version conflict on patch, merge and split; closed is final |
| `situation/SituationBriefIT` (fake `LlmClient`) | Typed claims stored, invalid citations dropped, members/status/severity unchanged; debounce then retry; daily cap (and 429 on request); failure keeps the previous brief; disabled → no call, 409 |
| `situation/ThemeTableTest` | Bundled rows; operator rows and keyword rules first; keyword classification; LLM off by default; LLM on, labeled `llm`, cached by hash, invalid answers ignored |
| `situation/SituationControllersIT` (MockMvc) | CRUD in three sections, list filters, patch with merge-patch and 409, member add 201/200/400, removal, timeline, changes; scopes; brief 409; private situations on every path and never published; SSE events and filter; linker dry-run validation; MCP tool shapes |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `python3 scripts/generate-postman.py docs/OpenAPI.json
postman` → "836 requests across 80 tag folders; quickstart 12 steps; covers all ops: True".

## Notes for the next prompt

- **119 (findings):** publish `analytic-finding` records with `severity` (`notable|significant|critical`),
  `findingType`, `signalFamily`, `title`, a geometry and optionally `evidenceRefs` (`record:` refs); the linker
  reads them as they are. Snapshot `key_values` and `SituationChanges.keyValues` are the place for linked analysis
  series; `intel/signal-families.json` should reconcile the families used here (`activity-anomaly`,
  `analytic-finding`, `intel-event`).
- **121 (analogs), 128 (assessments), 137 (social):** read situations through `SituationService.visible` /
  `SituationViews.full`; `Situation.social` and `coverage` are null placeholders.
- **Tests:** `foresignal.situations.auto-start` and `foresignal.situations.scheduled` are false in tests; drive
  `SituationLinker.catchUp()` (it implements `IngestStateReset`), `SituationSnapshots.snapshot(day)`,
  `SituationService.sweep(now, window)` and `SituationBriefService.generate(id, force)`.
- **144 (Flyway):** capture the five tables, their indexes above and the two `SpatialIndexInitializer` statements.
  Settings: `situation.cursor`, `situation.linkThreshold`, `situation.dormancyWindow`, `situation.reopenWindow`,
  `situation.briefs.enabled`, `situation.briefs.maxPerDay`, `situation.briefMinInterval`, `intel.themes`,
  `intel.themes.llmClassify`.

Implementation commit: `020028e2c21327a5448b4e176cb29bd77702f3df`
