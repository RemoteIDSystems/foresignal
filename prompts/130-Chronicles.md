# 130 — Chronicles

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/130-CR-Chronicles.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/129-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §12.3 (chronicles) and `docs/Client-Contract.md` §11 (chronicles)
- `docs/Core-Data-Model.md` §8 (`seq_clock`), §9 (partitioning), §12 (asOf), §13 (retention, consumer clamp)
- Completion Reports of 014/037 (asOf, tracks, frames), 016 (tiles), 036 (places, saved views, watchlists), 038 (rollups), 042–046 (raster frames), 044 (downsampling), 101 (investigations), 111 (exports, holds, cold archive), 116 (observability, manifests), 117 (attention topics), 118 (situations), 119 (findings, series), 122 (ledger), 124 (hypotheses), 127 (cycles, proposals), 128 (objects, workspaces), 129 (principals, sharing, TLP)

## Objective

Add **chronicles**: named, saved, shareable monitoring threads that bundle the layers a person chooses (sources, kinds, filters, places or regions, entities, situations, topics) and, while active, **record their own history** of everything that matches, independent of global retention. A chronicle can be paused or stopped (capture ends, history stays), replayed at any time like a recording, extended with new layers, shared with others, archived or deleted. Each chronicle has a live **thread** of updates and a workspace whose layout the client arranges. The analysis cycles can **propose** chronicles when a situation emerges; people accept, edit, keep or delete them.

Example: "Middle East war 2026": ACLED and UCDP events for the region, GDELT timelines for its topics, military aircraft and AIS near the Gulf and Red Sea, NOTAMs and TFRs, GPS-jamming cells, outages, energy prices and positioning, the linked situations and hypothesis sets. Months after it is stopped, it still replays exactly as it was recorded.

## Principles (binding)

- **A chronicle owns its copy.** Captured data lives in chronicle storage and is never deleted by global retention; only the chronicle's own retention policy or its deletion removes it.
- **Capture is explicit and bounded.** Every layer declares a capture mode; the chronicle shows its projected storage before activation and enforces a size cap.
- **Faithful replay.** Replay shows what the chronicle recorded at each moment, keyed by transaction time (`seq`) and observation time, with no look-ahead.
- **Same rules as everything else:** `private`, LOCAL_ONLY (062) and TLP (129) apply to captured data and to every share or export.

## Tasks

### 1. Model (`ai.foresignal.chronicle`)

- `chronicle`: `id` (ULID), `display_id` (`CHR-{yyyy}-{nnnn}`), `name`, `description`, `owner`, `visibility`, `tlp`, `status` (proposed | active | paused | stopped | archived), `area` (geometry, places, region codes; optional), `started_at`, `stopped_at`, `capture_from_seq`, `capture_to_seq`, `retention` (`forever` default, or a duration after stop), `size_cap_bytes` (default `chronicle.defaultCapGb` 20 GB), `bytes_used`, `workspace_id` (128; context and opaque layout), `situation_ids`, `hypothesis_set_ids`, `investigation_ids`, `topic_ids`, `origin` (operator | agent | analyst), `proposal_id`, `version`, timestamps.
- `chronicle_layer`: `id`, `chronicle_id`, `name`, `selector` jsonb (one of: `sources[]` + `kinds[]` + conditions (017 condition grammar); a saved view (036); a watchlist (036); `entities[]` (098); `situations[]` (118, membership-driven); `findings` (types, families); `series[]` (refs); `rasterProducts[]` (042); `alerts` (rule ids or tags)), `area_override`, `capture` (`full` | `downsampled` {interval, default 60 s per entity} | `events-only` (versions whose persist reason is FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE or TRIGGER_EDGE) | `latest-per-interval` {interval} | `rollup-only` (counts per region and hour, no records) | `frames` {every N frames for raster products}), `enabled`, `added_at`, `removed_at`, `estimate` jsonb (projected rows and bytes per day).
- **Storage:** `chronicle_version` partitioned **by chronicle** (LIST on `chronicle_id`; one leaf per chronicle, created on activation, dropped on deletion), columns mirroring `record_version` (source, external id, kind, seq, observed/event/ingested times, geometry, payload, persist reason) plus `layer_id`. Indexes per leaf: `(seq)`, `(layer_id, observed_at)`, GiST(`geom`), `(source_id, external_id, seq)`. Raster frames are captured by reference to content-addressed frames (042) with a 111 retention hold so they are not deleted. `chronicle_event` holds snapshots of non-record objects (alerts, findings, situation snapshots, hypothesis revisions, forecasts, assessments) as JSON with their refs, so the thread survives their owners' retention.

- **Schema:** `chronicle_version` is a partitioned table, so its parent and per-chronicle leaves are created by bootstrap SQL in the same mechanism 007 uses (`SchemaBootstrap` statement list), never by Hibernate; record the statements for 143's Flyway baseline.

### 2. Capture

- `ChronicleCaptureService`: a ChangeBus/AlertBus subscriber with one durable cursor per active chronicle (`chronicle.cursor.{id}`, in 019's consumer clamp **with the clamp release rule**; a chronicle that falls behind beyond the release is marked `gap` for that interval and the thread says so).
- For each new record version: evaluate active chronicles' layer selectors (pre-filtered by an in-memory index of source and kind, then area via JTS prepared geometries) and write matches per capture mode. Writes are batched and asynchronous; ingest is never blocked (back-pressure drops to a catch-up mode reading `record_version` by seq while it is still retained).
- **Backfill on activation:** optionally (`backfill: {from}`), copy matching history still retained globally (TRACK raw window and downsampled leaves, STANDARD retention, rollups), clearly marked `backfilled`, with caveats where the global data was already thinned or gone.
- **Layer edits** apply from the edit's seq onward; removing a layer keeps its captured data unless `purge=true`.
- **Size cap:** at 90% of the cap, notify the owner; at 100%, downsampled and full layers switch to `events-only` and the thread says so (never silent loss).
- Pause/resume, stop (capture ends; history kept), archive (export to a 111 cold-archive package and drop the leaf; restorable), delete (drop the leaf and snapshots; requires owner and confirmation token).

### 3. Thread, replay and views

- **Thread:** `GET /v1/chronicles/{id}/thread?after&types&cursor` (read): a chronological feed of notable items (alerts, findings, situation changes, hypothesis band changes, forecast resolutions, first appearances, layer edits, capture gaps), each with its envelope; per-principal read cursors and unread counts (033 pattern); SSE topic `chronicle` (`chronicle.item`, `chronicle.status`).
- **Replay:**
  - `GET /v1/chronicles/{id}/state?at=` (read): the state of every layer at time T from chronicle storage (latest captured version per entity with `observed_at ≤ T` and still valid by its TTL), paged by layer.
  - `GET /v1/chronicles/{id}/tiles/{z}/{x}/{y}.mvt?at&layers` (read): vector tiles over chronicle storage (016 machinery), so a globe replays without touching global history.
  - `GET /v1/chronicles/{id}/frames?from&to&step&layers` (read): a frame index for playback (counts and changed entities per step), and `GET /v1/chronicles/{id}/tracks/{source}/{externalId}?from&to` (read).
  - `GET /v1/chronicles/{id}/series?ref&from&to` (read): captured series points and rollups.
- **Workspace:** each chronicle owns a 128 workspace; clients store their auto-arranged layout there (opaque) and open the chronicle with its context (time range = chronicle span, layers = chronicle layers). Deep links (128) carry `chronicle:{id}`.
- **Summary:** `GET /v1/chronicles/{id}/summary` (read): layer counts over time, top entities, situations, hypothesis sets and forecasts, storage used, capture gaps; reused by 131 reports.

### 4. Proposals by the analysis cycles

- Add proposal type `propose_chronicle` to 127's proposer (and the 102 pipeline): when a situation turns `active` with severity ≥ warning, or a hypothesis set opens on a new theme, the proposer suggests a chronicle: name, area (situation geometry buffered, or its regions), layers inferred from the situation's member sources and kinds, linked topics (117), relevant series (119 registry for those regions), the situation and sets; capture modes from defaults by kind (TRACK → `downsampled` 60 s; event classes → `full`; series → `full`; rasters → `frames` every 4th); storage estimate. Proposed chronicles are created with `status = proposed` (no capture) and appear in the owner's inbox; accepting activates them; `allow_actions` may auto-activate within a per-day cap (default 2) and a size cap (default 5 GB each).
- Operators and agents may also ask: `POST /v1/chronicles/suggest {situationId | area + topic}` (agent) returns a draft without creating it.

### 5. API, MCP, sharing, docs

- CRUD: `GET/POST /v1/chronicles`, `GET/PATCH/DELETE /v1/chronicles/{id}` (read scope sees chronicles shared with the caller; create needs agent), `POST /v1/chronicles/{id}/layers`, `PATCH/DELETE /v1/chronicles/{id}/layers/{layerId}`, `POST /v1/chronicles/{id}/activate|pause|resume|stop|archive|restore` (owner or editor), `GET /v1/chronicles/{id}/estimate` (projected storage for the current layers), `POST /v1/chronicles/{id}/export` (111 formats: GeoParquet, GeoPackage, JSON bundle; respects TLP and LOCAL_ONLY).
- Sharing, ACLs and TLP from 129; chronicles appear in `shared-with-me`.
- MCP: `list_chronicles`, `get_chronicle_thread`, `get_chronicle_state` (read); `create_chronicle`, `add_chronicle_layer` (agent).
- Notifications (018 event types): `chronicle.proposed`, `chronicle.cap_warning`, `chronicle.gap`.
- `docs/OpenAPI.json`: `Chronicle`, `ChronicleInput`, `ChronicleLayer`, `CaptureMode`, `ChronicleThreadItem`, `ChronicleState`, `ChronicleFrameIndex`, `ChronicleEstimate`, `ChronicleSummary`; tag `Chronicles`; `x-foresignal-scope`; Postman BODIES for creating the Middle East example with five layers, activation, a replay state query.
- AccessPolicy rows. Problem codes: `UNKNOWN_CHRONICLE`, `UNKNOWN_CHRONICLE_LAYER` (404), `CHRONICLE_CAP_EXCEEDED` (409), `CHRONICLE_STATE_INVALID` (409), `CHRONICLE_LAYER_INVALID` (400).
- `docs/Operator-Guide.md` "Chronicles": choosing capture modes, storage estimates and caps, stop vs archive vs delete, replay, proposals; `docs/Client-Contract.md` §11 kept in sync.

## Tests to write (do not run)

- `ChronicleSelectorTest`: every selector type, area overrides, conditions.
- `CaptureModeTest`: full, downsampled (one per entity per interval), events-only reasons, latest-per-interval, rollup-only, frames.
- `ChronicleCaptureIT` (`TestRecords`): matching versions captured; global retention deleting the source rows leaves the chronicle intact; cursor restart exactly-once; ingest latency unaffected under a burst (back-pressure path); layer add and remove from the edit seq; purge option.
- `ChronicleBackfillIT`: retained history copied and marked `backfilled`; caveats where data was thinned.
- `ChronicleReplayIT`: `state?at` matches the recorded truth at several instants; tiles and tracks over chronicle storage; no look-ahead.
- `ChronicleLifecycleIT`: pause, resume, stop, archive to a cold package and restore, delete drops the leaf; size cap warning and fallback to events-only.
- `ChronicleProposalIT` (fake LLM proposer and deterministic suggestion): proposal created on a situation activation, accepted to active, auto-activation cap.
- `ChronicleSharingIT`: ACL and TLP on thread, replay and export.
- `ChronicleControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- A chronicle with layers from many sources records its own history while active, keeps it after stop regardless of global retention, and replays state, tiles, tracks and series at any past moment.
- Storage is estimated, capped and never silently lost.
- Chronicles can be proposed by the analysis cycles, accepted, edited, shared under TLP, archived, restored and deleted.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `130: Chronicles`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/130-CR-Chronicles.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `130: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
