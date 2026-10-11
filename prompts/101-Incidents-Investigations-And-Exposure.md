# 101 — Incidents Investigations And Exposure

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/101-CR-Incidents-Investigations-And-Exposure.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/100-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §5 rows "Correlation incidents", "Impact and exposure", "Case files / alert status"; §8 item 56 (its "057b" is this prompt)
- `docs/Core-Data-Model.md` §10 (ChangeBus, ChangeSummary), §11 (triggers), §12 (current reads), §13 (consumer clamp), §14 (provenance and coverage)
- Completion Reports of 017 (alert lifecycle, feedback enum, AlertBus), 015 (multiplexed `/v1/events/stream`), 025 (MCP registry), 036 (places), 039 (`private` enforcement), 040 (region tags), 052 (DERIVED runtime), 054 (GeoTIFF reader), 068/087/088/092/093/094 (sources the recipes use), 098 (entities), 099 (derived layers, corroboration), 100 (`rule-windows` anomalies)
- GHSL population grid: `https://human-settlement.emergency.copernicus.eu/download.php` (GHS-POP; CC BY 4.0, verify the current epoch and the tiled EPSG:4326 30-arcsec layout); H3 Java bindings `com.uber:h3` (Apache-2.0, verify the current 4.x version on Maven Central)

## Objective

Make the **intelligence event** a first-class object: a typed, located and time-bounded finding. It is built by deterministic correlation recipes from pinned record versions, carries supporting and contradicting evidence, a confidence score and evidence quality, and keeps the detector version and parameters needed to reproduce it. Add **investigations** (operator case files) and an ExposureService that says what is inside an affected area. Events stream over SSE, appear in MCP and can raise alerts through ordinary rules.

## Analytic principles (binding)

1. **Deterministic first.** Recipes create events and members. An LLM may explain existing evidence (102, 030) but never adds members, raises confidence or changes the type.
2. **Reproducible.** Every event stores `detector {name, version, params}` and `recipeId`/`recipeVersion`. Every member pins `(source, externalId, seq, version)`.
3. **Confidence is a score, not a calibrated probability.** It becomes calibrated only when 103 validation sets `calibrated=true`. Responses carry `confidenceMethod`.
4. **Coverage-aware.** Missing confirming data lowers evidence quality. It never counts as contradiction. Known coverage gaps become alternatives ("receiver coverage gap"), not evidence.
5. **Evidence, inference and speculation are separated.** Every event response has three sections:
   - `evidence`: members (observed records, alerts, anomalies);
   - `inference`: type, severity, confidence, geometry and times derived by the detector;
   - `speculation`: alternatives and any LLM narrative, always labelled.

> **v1.3 note:** `signal_family` values must come from one shared registry; prompt 119 creates `intel/signal-families.json` and reconciles the strings used here. Use the family names listed in `docs/Intelligence-Design.md` §5.4 where they fit. Prompt 118 links intelligence events into situations, and prompt 116 retrofits `EventScorer` to count independent evidence groups instead of sources.

> **v1.3 note:** prompt 130 extends investigations with owners, members and access lists, TLP labels, status, and links to workspaces, hypothesis sets, questions, chronicles (131) and reports (132). Keep `investigation` open for those columns.

## Tasks

### 1. Data model (`ai.foresignal.intel`)

- **`intelligence_event`:**
  - Identity and state: `id` (ULID), `type` (registry: `maritime-disruption`, `aviation-anomaly`, `infrastructure-disruption`, `weather-impact`, `seismic`, `custom`), `title`, `severity` (info | warning | critical), `status` (emerging | active | resolved | dismissed).
  - Times and place: `start_time`, `end_time` (null while open), `last_evidence_at`, `geom` (Geometry 4326, the affected area), `centroid`, `region_tags` text[] (040).
  - Assessment: `confidence` (0–1), `confidence_label` (low < 0.4 ≤ moderate < 0.7 ≤ high), `confidence_method`, `calibrated` bool (default false), `evidence_quality` (strong | moderate | weak | insufficient).
  - Provenance: `detector` jsonb, `recipe_id`, `recipe_version`, `origin` (recipe | analyst | agent | operator).
  - Speculation and context: `explanation` (deterministic text), `alternatives` jsonb (`[{hypothesis, basis, supportingMemberIds}]`), `narrative` (nullable LLM text, flagged), `exposure` jsonb (snapshot, §5).
  - Housekeeping: `dedup_key`, `merged_into`, `private` bool, `version` int (optimistic locking), `created_at`, `updated_at`.
  - Indexes: GiST(`geom`), `(status, updated_at DESC)`, `(type, start_time)`.
- **`intelligence_event_member`:**
  - Columns: event_id, `role` (supporting | contradicting | context), `member_type` (record | alert | anomaly | event), source_id, external_id, `seq`, `version`, kind, observed_at, `join_reason` jsonb (`{clause, distanceKm, dtMinutes, matchedKeys, regionTag}`), `signal_family` (e.g. `ais-behaviour`, `nav-warning`, `internet-measurement`), weight, `snapshot` jsonb (≤ 16 KB, like `alert.record_snapshot`), added_at, added_by.
  - Unique `(event_id, member_type, source_id, external_id, seq)`.
- **`intelligence_event_log`:** event_id, at, actor (token id or `recipe:<id>`), action (created, member_added, status, merged, edited, reopened), before/after jsonb.
- **Private:** an event with any member from a `private` source is `private=true` and follows 039 enforcement everywhere (lists, SSE, MCP, tiles, LLM egress).
- **Records.** Publish every event as a record of the DERIVED source `foresignal-intel` (kind `intelligence-event`, externalId = event id). The payload holds type, severity, status, confidence, label, evidenceQuality, memberCount, signalFamilies, title and `automated: true`, emitted only on change.
  - Tombstone on dismiss or merge.
  - This gives SSE changes, tiles, search and **alerts from events through ordinary 017 rules** (for example `kind=intelligence-event`, `severity == critical`, `enter`). Add that recipe to the docs.
- Add `intel.cursor` (§2) to the retention consumer clamp (019 registry, or extend the clamp).

### 2. Correlation engine and recipes

- **`CorrelationEngine`:**
  - A ChangeBus subscriber with a durable cursor `intel.cursor`. It catches up in 10k-seq chunks, like the TriggerEngine.
  - Inputs: record versions of seed kinds, `rule-windows` anomaly records (100) and alerts (AlertBus, matched by rule tag or id).
  - It runs on its own virtual thread and never blocks ingest.
- **`correlation_recipe`:**
  - Columns: id, version, enabled, `definition` jsonb, params overrides, last_run_at, last_error.
  - Built-ins load from `src/main/resources/intel/recipes/*.json`, are versioned, and are updated on start when the bundled version is newer. Operator param overrides are kept.
- **Definition:**
  - `seed {kinds[], sources[], conditions[]}`;
  - `joins[] {kinds[], conditions[], withinKm | sameRegion: country|admin1 | sameEntity, window {before, after}, role, signalFamily, weight}`;
  - `minMembers`, `minSignalFamilies`, `activateAt {members, confidence}`;
  - `contradictions[] {kinds|healthOf: [sources], withinKm, window, alternative}`;
  - `type`, `severityMap`, `dedup {radiusKm, window}`, `quietPeriod` (resolve), `reopenWindow`.
  - Validate the definition with the 017 condition validator.
- **Built-in recipes.** Check each seed kind exists (094, 099, 087, 088, 092, 068). Load a recipe disabled, with a warning, when its kinds are missing.
  1. **`maritime-disruption`:**
     - seeds: 099 AIS gap, loiter, rendezvous and chokepoint course-change records; 088 NGA MSI warnings and PortWatch chokepoint transit drops;
     - joins within 150 km and 12 h; `minSignalFamilies` 2;
     - contradictions: AIS source coverage drop in the area → alternative "AIS receiver coverage gap"; `gpsjam` cell overlap → alternative "GNSS interference".
  2. **`aviation-anomaly`:**
     - seeds: 099 emergency squawks, holding patterns, GPS jamming cells; 087 TFRs, FAA NAS ground stops and delays, SIGMETs;
     - joins within 100 km and 2 h;
     - contradictions: a convective SIGMET or NWS warning → alternative "weather"; ADS-B coverage drop → "coverage gap".
  3. **`infrastructure-disruption`:**
     - seeds: IODA outages (068); Cloudflare Radar outages, BGP events and status pages (092); grid-frequency excursions (093);
     - joins on `sameRegion: admin1` (else country) and 6 h; context members: severe-weather records (047) and reference facilities (094) in the area;
     - contradictions: only one measurement platform reports → alternative "measurement artefact".
- **Same-event dedup.** A candidate merges into an open event of the same type when the geometries intersect after buffering by `dedup.radiusKm` and the times overlap within `dedup.window`, or when they share a member. Merging two open events keeps the older id, moves members, sets `merged_into`, tombstones the merged record and logs the merge. Never merge across types.
- **Lifecycle:**
  - `emerging` until `activateAt` is met, then `active`;
  - `resolved` after `quietPeriod` with no new supporting evidence, or when every member is tombstoned or expired;
  - `reopened` when new evidence arrives within `reopenWindow`;
  - `dismissed` only by an operator or agent.
- **Confidence (pure `EventScorer`, version `1.0.0`):**
  - `c = 1 − Π_f (1 − w_f · r_f)` over distinct signal families, where `r_f` is the best member's source reliability (109 grade later; default 0.6) and `w_f` the recipe weight;
  - then multiply by `(1 − 0.25)` per active contradiction, and cap at 0.95.
- **Evidence quality:**
  - strong: ≥ 3 families from ≥ 2 independent sources;
  - moderate: 2 families;
  - weak: 1 family;
  - insufficient: members only from sources currently unhealthy.
- **Explanation** is deterministic templated text that cites members, e.g. "3 signals from 2 independent sources within 80 km over 4 h: …".
- `POST /v1/intel/recipes/{id}/test {fromSeq, toSeq}` dry-runs the recipe into an in-memory store and returns would-be events. Larger runs are 103.

### 3. Manual and agent events

- `POST /v1/intel/events` (agent) creates an event with `origin=agent|operator`. It needs ≥ 1 member that exists (verified by seq). The scorer computes confidence; client-supplied confidence is rejected.
- `PATCH` changes title, status, a severity override (logged) and notes. Member add and remove are agent-scoped and logged.

### 4. Investigations

- `investigation`: id, `display_id` (human-readable, `INV-{yyyy}-{nnnn}`, sequential per year), title, status (open | closed), summary (markdown, operator-authored), tags, created_by, created_at, updated_at.
- `investigation_item`: investigation_id, item_type (event | alert | record | anomaly | place | briefing_run | note), ref, pinned `seq`, `snapshot` (≤ 64 KB, taken when added, so evidence survives retention), note, added_at, added_by.
- Adding an alert to an investigation sets its 017 status to `investigating`, if that status exists.
- `GET /v1/investigations/{id}/export?format=json|md|geojson`: md is a report with item citations `[source/externalId@vN]`. All formats carry the "Automated analysis inputs; verify before acting" header and licence attributions.

### 5. ExposureService

- `exposure(geometry, options)` returns:
  - counts of current reference facilities inside the area by kind (094: power-plant, airport, port, cable landing, data centre, nuclear facility, spaceport), from head ⋈ live;
  - places (036) inside;
  - current counts of watchlist entities (036/098) and live aircraft and vessels inside;
  - optional `population`.
- Every section has `status` (ok | unavailable | not-installed) and attribution.
- **Population pack (optional):**
  - `POST /v1/packs/population/import {path}` (admin). `path` is validated like `INBOX_PATH` under `${FORESIGNAL_DATA_DIR}/packs`.
  - It imports GHSL GHS-POP tiles in EPSG:4326 with the 054 GeoTIFF reader, streaming rows and rejecting BigTIFF. Each pixel's population is aggregated to the H3 cell containing its centre, at resolutions 5 and 7.
  - Store in `population_h3(h3 bigint, res smallint, pop real, epoch smallint, PK(h3, res))`, with an import record (file sha256, epoch, licence `CC-BY-4.0`, attribution "© European Union, GHSL").
  - Query: polyfill the geometry at res 7 (res 5 when the area is > 50,000 km²) and sum.
  - Return `{estimate, epoch, method: "GHSL→H3 r7 centroid-in-polygon", note: "modelled residential population; not a casualty or impact estimate"}`.
- Events store an `exposure` snapshot, recomputed (debounced 5 min) when the geometry changes.

### 6. API, SSE, MCP

- **Events and recipes:**
  - `GET /v1/intel/events?type&status&bbox&since&minConfidence&cursor` (read; keyset on `updated_at, id`)
  - `GET /v1/intel/events/{id}` (read; evidence/inference/speculation sections, members, log)
  - `POST /v1/intel/events` (agent), `PATCH /v1/intel/events/{id}` (agent)
  - `POST /v1/intel/events/{id}/members` (agent), `DELETE /v1/intel/events/{id}/members/{memberId}` (agent)
  - `POST /v1/intel/events/{id}/merge {intoId}` (agent)
  - `GET /v1/intel/recipes` (read), `PUT /v1/intel/recipes/{id}` (admin: enabled, params), `POST /v1/intel/recipes/{id}/test` (agent)
- **Investigations:** `GET/POST /v1/investigations`, `GET/PATCH/DELETE /v1/investigations/{id}`, `POST /v1/investigations/{id}/items`, `DELETE /v1/investigations/{id}/items/{itemId}`, `GET /v1/investigations/{id}/export` (agent for writes, read for GETs).
- **Exposure:** `POST /v1/exposure {geometry | placeId | eventId}` (read).
- **SSE:** on the multiplexed `/v1/events/stream`, add topic `intel` with events `intel.event.created|updated|resolved|merged` (payload: summary plus `version`). Clients resync with `GET /v1/intel/events?since`. Respect `private`.
- **MCP:** `list_intel_events`, `get_intel_event` (compact by default; evidence first; speculation labelled), `get_exposure` (read); `create_intel_event`, `update_intel_event_status`, `create_investigation`, `add_to_investigation` (agent).
- **Problem codes:** `UNKNOWN_EVENT`, `UNKNOWN_INVESTIGATION` (404); `EVENT_MEMBER_NOT_FOUND`, `RECIPE_INVALID` (400); `EVENT_VERSION_CONFLICT` (409); `PACK_NOT_INSTALLED` (409).

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - schemas `IntelligenceEvent`, `IntelEventMember`, `IntelEventPage`, `CorrelationRecipe`, `Investigation`, `InvestigationItem`, `Exposure`; tag `Intelligence`; every operation with `x-foresignal-scope`;
  - the SSE event schemas (020 conventions);
  - regenerate Postman, and add BODIES for `createIntelEvent`, `addIntelEventMember`, `mergeIntelEvent`, `updateRecipe`, `testRecipe`, `createInvestigation`, `addInvestigationItem`, `computeExposure`, `importPopulationPack`.
- AccessPolicy rows as listed.
- `docs/Operator-Guide.md` "Intelligence events": principles, recipes and tuning, the confidence formula, dedup, investigations, the population pack download and import steps with GHSL attribution.
- `docs/Agent-Guide.md`: how agents should cite members and present speculation.
- `THIRD_PARTY_NOTICES.md`: GHSL (when installed by the operator; data is not bundled) and H3.

## Tests to write (do not run)

- `EventScorerTest`: families, weights, contradictions, cap, evidence-quality table.
- `RecipeDefinitionValidatorTest`: every field, bad conditions, missing kinds → disabled with a warning.
- `DedupTest`: buffer intersect plus time overlap merges; different type never merges; shared member merges.
- `CorrelationEngineIT` (`TestRecords` seeding):
  - the maritime recipe forms `emerging` → `active` as families arrive;
  - a contradiction lowers confidence and adds an alternative;
  - quiet period → resolved; new evidence reopens;
  - cursor restart gives exactly-once membership;
  - a private member marks the event private;
  - the `foresignal-intel` record is emitted only on change.
- `EventAlertIT`: a 017 rule on `kind=intelligence-event` fires on a critical event.
- `InvestigationIT`: items keep snapshots after the referenced version is purged; md and geojson export.
- `ExposureServiceIT`: facility counts in a polygon; population via a tiny synthetic GeoTIFF fixture imported to H3; `not-installed` without a pack.
- `IntelControllersIT` / `InvestigationsControllerIT`: CRUD, optimistic-lock 409, scopes, SSE topic delivery, MCP tool output shape.

## Acceptance criteria

- Built-in recipes create events from seeded records with pinned members, join reasons, detector version and parameters. Re-running a recipe over the same seq range yields the same events.
- Event responses separate evidence, inference and speculation. Confidence is labelled as a non-calibrated score.
- Alerts can fire from events through ordinary trigger rules. Investigations survive the retention of their source versions.
- Exposure works with no population pack and reports `not-installed` for that section.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `101: Incidents Investigations And Exposure`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/101-CR-Incidents-Investigations-And-Exposure.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `101: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
