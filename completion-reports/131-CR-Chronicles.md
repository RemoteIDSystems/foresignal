# 131-CR-Chronicles

| | |
|---|---|
| Prompt | 131-Chronicles.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `7b2238fb1eae0eb5adcc0586cc6b9e729b1dc3d4` |
| Date | 2026-10-17 |

## Summary

This prompt adds **chronicles**: named monitoring threads that capture their own history. Each one has layers
(selector plus capture mode), a chronological thread with per-person unread counts, a replay of what it captured,
and a lifecycle (proposed → active ⇄ paused → stopped → archived → stopped). Global retention never removes a
chronicle's copy. The code lives in the new package `ai.foresignal.chronicle`. It compiles. **The tests are written
but were not run** (owner directive 2026-10-17): no test result, coverage figure or guard result in this report is
measured.

- **Storage** (`ChronicleDdl`, `ChronicleSchemaInitializer`, `ChronicleStorage`): `chronicle_version` is LIST
  partitioned on `chronicle_id`, one leaf per chronicle, created by bootstrap SQL and never by Hibernate. Orphan
  leaves are dropped on ingest-state reset.
- **Capture** (`ChronicleCaptureService`, `CaptureDecider`, `ChronicleLayerCompiler`, `CompiledLayer`,
  `ChronicleIndex`, `ChronicleSizes`):
  - The six capture modes are `full`, `downsampled`, `events-only`, `latest-per-interval`, `rollup-only` and
    `frames`.
  - Capture always reads `record_version` by seq in chunks; the ChangeBus only wakes the loop.
  - Per-chronicle cursors (`chronicle.cursor_seq`) join the retention consumer clamp through the new
    `retention/RetentionConsumerProvider` SPI (`ChronicleCursorConsumers`).
  - Gaps are detected from the retention floors and written as `capture-gap` items.
  - The size cap warns at 90% and falls back to events-only at 100%, with thread items and inbox notices.
- **Thread and SSE** (`ChronicleThreadService`, `ChronicleSignals`, `ChronicleTopic`, `ChronicleFilter`):
  - Item types: alerts, findings, situation changes, hypothesis band changes, forecast resolutions, first
    appearances, layer edits, gaps, cap warnings and fallbacks, status changes and backfills.
  - Read cursors per principal.
  - SSE topic `chronicle` (`chronicle.item`, `chronicle.status`; cursor key `k`).
- **Lifecycle and CRUD** (`ChronicleService`, `ChronicleSelectorValidator`, `ChronicleEstimator`,
  `ChronicleBackfill`, `ChronicleArchiver`, `ChronicleJobs`, `ChronicleRetentionJob`):
  - Display ids are `CHR-yyyy-nnnn`.
  - Each chronicle owns a 129 workspace.
  - Delete needs the display id as confirmation.
  - Archive writes a verified gzip JSONL cold package; restore reloads it.
  - The chronicle's own retention runs after stop.
  - Estimates are projected from the last 24 h of global history.
- **Replay** (`ChronicleReplayService`): state at a moment, MVT tiles, frames, tracks, series and a summary.
  `TileQueryService.envelope` and `clipped` were made public for it.
- **Sharing** (`ChronicleShareableSource`, `ChronicleObjectResolver`): 130 access list and TLP. The new object type
  `chronicle` raises the contract version to `1.2`. A workspace owned by a chronicle derives its role from the
  chronicle (`ShareableSources`).
- **Frames** (`raster/FramePin`, `ChronicleFramePin`): `RasterRetentionStep` skips pinned frames.
- **Export** (`ChronicleExporter`, `export/CustomExport`, `ExportJobService.submitCustom`): `geoparquet`, `gpkg`
  and `json-bundle` jobs over chronicle storage. TLP is sent as `Foresignal-TLP`, and LOCAL_ONLY data never leaves.
- **Proposals** (`ChronicleProposals`, `ChronicleSuggester`, `analyst/ChronicleProposalHandler`):
  - Adds the `propose_chronicle` action to the 102 pipeline and the 128 proposer (`AnalystActions`,
    `AnalystExecutionService`, `CycleOutputTools`, `CycleRoles`).
  - A deterministic proposer runs on situations turning active at warning or above, and on new hypothesis-set
    themes.
  - Auto-activation is capped by `chronicle.autoActivatePerDay` and `chronicle.autoActivateCapGb`.
- **API, MCP, docs:**
  - `api/ChroniclesController` covers all 25 operations, with `AccessPolicy` rows and 7 Problem codes.
  - MCP: `list_chronicles`, `get_chronicle_thread` and `get_chronicle_state` (read); `create_chronicle` and
    `add_chronicle_layer` (agent).
  - The spec marks 25 operations implemented. It also adds `json-bundle` to `ExportJob.format`,
    `CHRONICLE_VERSION_STALE` to the 412 description, the `chronicle` SSE topic, the contract example `1.2` and a
    changelog entry. `pending-operations.txt` loses 25 lines.
  - The Postman generator and collection were updated.
  - `docs/Client-Contract.md` was replaced whole. The Agent Guide gains the topic, five tool rows and a
    "Chronicles" subsection. The Operator Guide gains "Chronicles (prompt 131)".

## Files created / modified

**Created (main):**
- `chronicle/` (47 files): `CaptureDecider`, `CapturedRow`, `CaptureMode`, `ChronicleArchiver`, `ChronicleArea`, `ChronicleBackfill`, `ChronicleCaptureService`, `ChronicleCursorConsumers`, `ChronicleDdl`, `ChronicleEntity`, `ChronicleEstimator`, `ChronicleEventEntity`, `ChronicleEventRepository`, `ChronicleExporter`, `ChronicleFilter`, `ChronicleFrameEntity`, `ChronicleFramePin`, `ChronicleIndex`, `ChronicleJobs`, `ChronicleLayerCompiler`, `ChronicleLayerEntity`, `ChronicleLayerRepository`, `ChronicleNotices`, `ChronicleObjectResolver`, `ChronicleProperties`, `ChronicleProposals`, `ChronicleReadCursorEntity`, `ChronicleReplayService`, `ChronicleRepository`, `ChronicleRetentionJob`, `ChronicleRollupEntity`, `ChronicleSchemaInitializer`, `ChronicleSelector`, `ChronicleSelectorValidator`, `ChronicleSeriesPointEntity`, `ChronicleSeriesSampler`, `ChronicleService`, `ChronicleShareableSource`, `ChronicleSignals`, `ChronicleSizes`, `ChronicleStorage`, `ChronicleSuggester`, `ChronicleThreadService`, `ChronicleTopic`, `ChronicleVocabulary`, `CompiledLayer`, `package-info`.
- `api/ChroniclesController`, `analyst/ChronicleProposalHandler`, `retention/RetentionConsumerProvider`,
  `raster/FramePin`, `export/CustomExport`.
- `mcp/tools/{ListChroniclesTool,GetChronicleThreadTool,GetChronicleStateTool,CreateChronicleTool,AddChronicleLayerTool}`.

**Modified (main):**
- `config/StartupOrder` (`CHRONICLES = 42`) and `config/SchemaBootstrap`.
- `retention/RetentionJob`, `raster/RasterRetentionStep`, `store/tiles/TileQueryService`,
  `export/ExportJobService`.
- `api/error/ErrorCodes`, `settings/SettingKeys`, `collab/MeEvents`, `collab/ShareableSources`,
  `object/ObjectTypes`, `security/AccessPolicy`.
- `analyst/AnalystActions`, `analyst/AnalystExecutionService`, `cycle/CycleOutputTools`, `cycle/CycleRoles`.
- `application.yml` (`foresignal.chronicles`).

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Client-Contract.md`, `docs/Agent-Guide.md`,
`docs/Operator-Guide.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−25).

**Tests:** the new classes below, plus the helper `chronicle/ChronicleItSupport`. Modified:
- `stream/sse/EventSchemaConformanceTest`: `chronicle.item` and `chronicle.status` factories.
- test `application-test.yml`: auto-start, scheduled retention and async jobs are off.

## Deviations from the prompt

1. **Bytes are estimated per captured row**, not read from the relation size.
2. **The cursor lives in `chronicle.cursor_seq`** and reaches the retention clamp through the
   `RetentionConsumerProvider` SPI, under consumer names `chronicle.cursor.{id}`.
3. **Capture always reads `record_version` by seq**; the ChangeBus only wakes the loop.
4. **Gaps are detected from the retention floors.**
5. **Frames are pinned through the `FramePin` SPI**, not through a 111 hold.
6. **The archive package is gzip JSON Lines**, not GeoParquet, because no Parquet reader exists to restore from;
   GeoParquet is available through export.
7. **Notifications are personal only**: `chronicle.proposed`, `chronicle.cap_warning` and `chronicle.gap` go to the
   owner's inbox and personal channels, not to the broadcast fan-out.
8. **The proposer prompt template is unchanged**; the tool description carries `propose_chronicle`.
9. **A workspace owned by a chronicle derives its role from the chronicle.**
10. **The suggestion is deterministic** (no LLM).
11. **Hypothesis band changes and forecast resolutions come from polls** of `hypothesis_log` and
    `forecast_question` (`chronicle.threadCursors`).
12. **First-appearance items are capped** per layer per hour (`first-appearances-per-hour`, 20).
13. **`downsampled` keeps every event version** in addition to one per interval.
14. **`frames` keeps the first frame and then every Nth.**
15. **The area does not apply to entities, situations and series** unless a layer sets its own.
16. **`discoverability` is `none`**; its fields belong to 134.
17. **Edited by script, not rewritten by hand:**
    - The spec was edited by script, as in every earlier prompt.
    - The Operator Guide was read end to end in chunks and written whole by script. One section was appended, and
      two sentences were updated: the contract version, and "chronicles and reports of later prompts".
    - The Agent Guide was read in full, then edited by script.
    - `docs/Client-Contract.md` was replaced whole.

## Issues found / open questions

**Owner decisions:**
- (a) the gzip JSONL archive format instead of GeoParquet (Deviation 6);
- (b) per-row byte estimation for the cap (Deviation 1);
- (c) frame pinning by SPI rather than a 111 hold (Deviation 5);
- (d) personal-only chronicle notifications (Deviation 7);
- (e) auto-activation defaults: 2 per day, 5 GB cap; default cap 20 GB;
- (f) the area not applying to entities, situations and series (Deviation 15).

**Known limits:**
- A stopped chronicle cannot be resumed; a new chronicle continues it.
- Estimates are upper bounds: area and payload conditions are not applied.
- `docs/Invariants.md` CHR-1 and CHR-2 already name `ChronicleCaptureIT`, `ChronicleRetentionIndependenceIT` and
  `ChronicleLifecycleIT`, so it was not changed.

**Schema for prompt 144:**
- Hibernate tables: `chronicle`, `chronicle_layer`, `chronicle_event`, `chronicle_read_cursor`,
  `chronicle_series_point`, `chronicle_rollup` and `chronicle_frame`.
- Bootstrap SQL (not Hibernate): the partitioned `chronicle_version` with its four indexes and one leaf per
  chronicle.
- New properties: `foresignal.chronicles.*` (`auto-start`, `poll-interval`, `chunk-seqs`,
  `first-appearances-per-hour`, `series-sample-interval`, `retention-scheduled`, `async-jobs`).
- New app settings: `chronicle.defaultCapGb`, `chronicle.autoActivatePerDay`, `chronicle.autoActivateCapGb` and
  `chronicle.threadCursors`.

**Guards not run:**
- Not run, under the no-tests directive: the US English guard, `lintOpenApi` and every contract test
  (`AccessPolicySyncTest`, `OpenApiContractIT`, `EventSchemaConformanceTest`).
- New prose was checked by grep for British spellings, and none were found. The existing wire name `licence` was left
  alone.

**Risks for the owner's first test run:**
- The ITs call `capture.catchUp()`, `ChronicleRetentionJob.run()` and the backfill directly, because auto-start,
  scheduled retention and async jobs are off in tests.
- `ChronicleLifecycleIT.sizeCap` derives the cap from the measured per-row size.

**External data:** none was fetched. No file, page or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `chronicle/ChronicleSelectorTest` | Selector parsing and validation for every selector type |
| `chronicle/CaptureModeTest` | Mode parsing, intervals, frame steps, cap fallback |
| `chronicle/ChronicleCaptureIT` | Capture inside the area with first appearances, the copy surviving deletion of global rows, no double capture after a cursor restart, a burst ingested without waiting for capture, layer edits from their seq, events-only and pause (CHR-1, CHR-2) |
| `chronicle/ChronicleBackfillIT` | Backfill of retained history and its thread item, caveats for deleted and thinned history, rollup-only backfill |
| `chronicle/ChronicleReplayIT` | State at several instants with no look-ahead, state paging, tiles and tracks after the global rows are gone, the frame index, series and summary |
| `chronicle/ChronicleLifecycleIT` | Transitions and `409 CHRONICLE_STATE_INVALID`, archive and restore, delete with confirmation, the 90% and 100% cap |
| `chronicle/ChronicleSharingIT` | Invisible until shared, viewer rights, shared-with-me, TLP on shares and exports, private data |
| `chronicle/ChronicleControllersIT` | The 25 REST operations and the MCP tool shapes |
| `chronicle/ChronicleRetentionIndependenceIT` | Global retention never deletes the copy; the cursor clamps retention until capture; own retention after stop |
| `cycle/ChronicleProposalIT` | One proposed chronicle per active situation, accepting a proposal, the 128 proposer's `propose_chronicle`, auto-activation within the daily and size caps |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: exit 0, no errors.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests … covers all ops: True".

## Notes for the next prompt

- **132 (reports):**
  - Follow the chronicle pattern: register a `ShareableSource`, use `AccessResolver.require` and `EgressPolicy.tlp`.
  - A custom export job can reuse `ExportJobService.submitCustom` and `CustomExport`.
- **Retention consumers:** any new durable cursor can join the clamp by implementing `RetentionConsumerProvider`.
- **144 (Flyway):** capture the seven tables and the partitioned `chronicle_version`.
- **Tests:** use `ChronicleItSupport` (`person`, `create`, `activate`, `standardLayer`, `standard`).

Implementation commit: `7b2238fb1eae0eb5adcc0586cc6b9e729b1dc3d4`
