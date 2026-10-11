# 036 — Places Views And Watchlists

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/036-CR-Places-Views-And-Watchlists.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/035-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 items 2 and 7, §5 (Watchlists, Moving-anchor rows), §6 (Places, saved views row), §8 item 21 ("028b")
- `docs/Weather-Imagery-Design.md` §2.3 (`place` entity; `trigger_rule.near`, `cooldown_scope`), §5.1 "Places AOI", §6.5 (`/v1/places`), §6.9 (place MCP tools, `foresignal://places`), §6.11 (AccessPolicy, `UNKNOWN_PLACE`), §7.3 (near materialised into `geofence`; deleting a place), §10.2 row "033 Places-And-AOI" (= this prompt)
- `docs/Core-Data-Model.md` §11 (edge SQL, `TriggerPersistGate` exact evaluation, shared operator truth table), §12 (query paths)
- Completion Reports of 008 (`entityKeys` descriptor field), 014 (`near`/`radiusKm`, formats), 015 (filters, PATCH), 016 (tiles, TileJSON), 017 (set operators, source/kind arrays, `near`, `cooldownScope`, buffered LineString fences), 023 (`alwaysAuthenticated`, audit), 025, 028, 029, 035 (`RestrictionClamp`, `ReadRestriction.placeIds` hook)

## Objective

Give every rule, view, briefing, Ask scope and client one shared vocabulary for "where" and "what": named places (point + radius, polygon, route + buffer) with a places AOI that weather sources use, saved views rendered by records, tiles and SSE, watchlists with CSV import and an `inWatchlist` operator that resolves per-kind identity paths, and moving-anchor "near me" rules whose anchor is an ordinary record.

## Tasks

### 1. Places (`ai.foresignal.place`)

- `PlaceEntity` (`place`), generalising the Weather design's point-only table: `id` slug PK (`^[a-z0-9]+(-[a-z0-9]+)*$`, ≤ 64), `name`, `shape` (`point|polygon|route`), `geom` geometry(Geometry, 4326) (Point, Polygon/MultiPolygon, LineString), `radius_km` (point; default 20, 0.1–500), `buffer_m` (route; 10–50,000), `area` geometry(MultiPolygon, 4326) **derived** on save (`ST_Buffer(geom::geography, r)::geometry` for points and routes, the polygon itself for polygons; `GeometryFixer`, ≤ 10,000 vertices), `timezone` (IANA, default server zone), `kind` (`user|airport|asset`), `tags` text[], `products` jsonb (weather product ids for place-weather, used by 052), `sensitive` (default true), `version` int, `created_at`, `updated_at`. GiST on `area` (SchemaBootstrap; 134 adds it to Flyway).
- API: `GET /v1/places` (filter `kind`, `tag`), `POST /v1/places`, `GET/PUT/DELETE /v1/places/{placeId}`. Reads need `read` and are `alwaysAuthenticated` (a home location is sensitive even with `openReads`); writes need `agent`. 404 `UNKNOWN_PLACE`. `DELETE` → 409 `PLACE_IN_USE` listing referencing rules, views and briefings, unless `?force=true`, which disables those rules (document that tombstoned place-weather records can fire exit rules, per the Weather design).
- Every save bumps `version` and publishes `PlaceChangedEvent(placeId, version, deleted)`.
- **Places AOI** (`PlacesAoiService`): `aoi()` = union over places of `area` buffered to at least `max(radiusKm, weather.aoiBufferKm)` (default 150 km); `envelope()` for upstream filters; `version()`; publishes `PlacesAoiChangedEvent`. With no places, return `weather.defaultAoi` (default: Texas bbox `-106.65,25.84,-93.51,36.50`) and expose a health warning through the 006 `HealthRule` SPI for AOI-consuming sources. Cache, recompute on `PlaceChangedEvent`. Weather sources (047, 048, 049, 052) and render warming (045) call it.
- Query integration: `place=<id>[,<id>]` (plus optional `radiusKm` extra buffer) on `/v1/records`, `/v1/history`, tiles, `/v1/changes/stream`, `/v1/events/stream` filters and `/v1/search`, resolved to `area` and intersected (`ST_Intersects` with `&&` prefilter). Using `place=` requires a token (places are not anonymous). 035's `ReadRestriction.placeIds` resolves through this service.
- Templates: `{{place.name}}`, `{{place.distanceKm}}` for near rules; never raw coordinates.

### 2. Near rules on places and points

- Check 017 for `trigger_rule.near` jsonb and materialisation into `geofence`; add or extend it to: `near: {placeIds: [..] | placeId, radiusKm?}` (geofence = union of `area`, buffered by `radiusKm` when given), `near: {point: [lon, lat], radiusKm}`, and the anchor form in task 5. Store `geofence_origin` (`inline|near`). On `PlaceChangedEvent`, re-materialise every referencing rule in one transaction and refresh the gate snapshot.
- Semantics of a place edit (document): both `v` and `p` are evaluated against the current fence, and the gate compares persisted state under the current fence, so editing a place never fires edges for stationary entities; an entity that moves after the edit converges on its next observation. Record `context.near = {placeIds, placeVersions}` on each alert.

### 3. Saved views (`ai.foresignal.view`)

- `SavedViewEntity` (`saved_view`): `id` uuid, `slug` unique, `name`, `sources` text[], `kinds` text[], `place_ids` text[], `bbox`, `area` MultiPolygon, `conditions` jsonb (the 017 `Condition` grammar including `in/notIn/exists/matches/startsWith` and `inWatchlist`; max 20), `time_field`, `include_expired`, `fields` (projection whitelist of validated top-level-rooted JSON paths, max 50), `version`, `created_at`, `updated_at`.
- `RecordPredicate`: one compiled form shared by rules and views, rendered to SQL by the 017 predicate renderer (alias parameter, bound paths and values) and evaluated in Java by the `TriggerPersistGate` evaluator for streams. Views and rules must consume `trigger/operator-truth-table.json`.
- Rendering: `GET /v1/views/{id}/records` (all 014 formats, keyset pagination, `fields` projection as bound `payload #> :path`), `GET /v1/views/{id}/tiles/{z}/{x}/{y}.mvt` and `/v1/views/{id}/tiles.json` (cache key includes `version`), `GET /v1/views/{id}/stream` (015 SSE with the view's predicate; `leftFilter` semantics unchanged). Also a `view=<id|slug>` parameter on `/v1/records`, `/v1/changes`, `/v1/changes/stream`, `/v1/events/stream` and `/v1/search`, intersected with any other filters and with 035 restrictions.
- CRUD: `GET/POST /v1/views`, `GET/PUT/DELETE /v1/views/{id}` (definitions: read token; writes: agent). View data follows normal data-read rules (`openReads`) unless the view references a place or watchlist, in which case a token is required.

### 4. Watchlists (`ai.foresignal.watch`)

- `watch_list`: `id` uuid, `name`, `identity` (`icao24|registration|callsign|mmsi|imo|custom`), `normalize` (`upper|lower|digits|none`; defaults: icao24/registration/callsign `upper`, mmsi/imo `digits`), `description`, `item_count`, `created_at`, `updated_at`. `watch_list_item`: `list_id`, `value` (normalised), `label`, `notes`, `tags` text[], `added_at`, `origin` (`manual|csv|json`); unique (`list_id`, `value`). Cap 100,000 items per list.
- API (agent scope for reads and writes): CRUD `/v1/watchlists`, `GET/POST/DELETE /v1/watchlists/{id}/items`, `POST /v1/watchlists/{id}/import` (`text/csv` with header `value[,label,notes,tags]`, or JSON array; `mode=merge|replace`; `dryRun=true` returns adds/updates/removals/invalid rows; ≤ 5 MB), `GET /v1/watchlists/{id}/export` (`text/csv`). Validate values per identity (icao24 = 6 hex; mmsi = 9 digits; imo = 7 digits with check digit). Audit every edit (023).
- **Identity paths:** `IdentityResolver` maps (`sourceId`, identity) → payload path from the descriptor's `entityKeys` (008; check it exists and add the field if missing). Update the adapters that exist by now only if they lack `entityKeys`; document in `docs/Adapter-Guide.md` that identity-bearing adapters must declare them (icao24, registration, callsign, mmsi, imo).
- **Operators:** `{op: "inWatchlist", value: "<listId>", path?}` and `notInWatchlist`. With no `path`, render per source: `EXISTS (SELECT 1 FROM watch_list_item w WHERE w.list_id = :list AND w.value = norm(CASE v.source_id WHEN :s1 THEN v.payload #>> :p1 … END))`; sources without the identity never match (rule validation warns and lists them). The gate evaluates against an in-memory `WatchlistSnapshot` (`Map<listId, Set<value>>`) refreshed on `WatchlistChangedEvent`. List edits converge on each entity's next observation (no retroactive alerts); say so in the docs. Add rows to the shared truth table.
- Filters: `inWatchlist=<listId>` on `/v1/records`, tiles, SSE and views (token required; 401 for anonymous and feed keys).
- Privacy: alerts from rules referencing a watchlist get `alert.sensitive = true` and are hidden from anonymous and feed-key principals via 035's clamp. Operator Guide: identifier quality (prefer icao24 and IMO; MMSIs are reused or spoofed), PIA/LADD limits, and a policy against tracking private individuals.

### 5. Moving-anchor "near me" rules

- `near: {anchor: {source, externalId}, radiusKm (0–2000), maxAnchorAge (default PT30M), minMoveMeters (default 200)}`. The anchor is an ordinary record (e.g. a phone through a custom source or 039's OwnTracks source). `radiusKm = 0` means "anchor point inside target geometry" (e.g. inside an NWS warning polygon).
- **Target side** (normal evaluation): `near(x) = x.geom && ST_Expand(:anchor, :deg) AND ST_DWithin(x.geom::geography, :anchor::geography, :m)` (`ST_Intersects` when radius is 0), where `:anchor` is the anchor's **current** live position at evaluation time, used identically for `v` and `p` (never the anchor version at `p`), and `:deg = m / 111,320 / cos(lat)` clamped. Document that durable-cursor catch-up after a restart uses the current anchor position.
- **Anchor side** (reverse evaluation): `AnchorEvaluator` runs in the `TriggerEngine` after normal evaluation of each batch. For a `ChangeSummary` or live update whose (source, externalId) anchors any enabled rule, and that moved ≥ `minMoveMeters` from `trigger_anchor_state.eval_geom` (or 60 s passed), query current targets from `record_head ⋈ record_live` with the rule's source/kind arrays and the rest of its predicate rendered over the merged current payload: `enter` = near(new) ∧ ¬near(old); `exit` = near(old) ∧ ¬near(new) (enter/exit modes only; match-mode rules evaluate the target side only). Cooldown and `cooldownScope` apply. Alerts reference the target's head `seq` and snapshot, with `context.anchor = {source, externalId, seq, distanceKm}`.
- `trigger_anchor_state`: (`rule_id`, `anchor_source`, `anchor_external_id`) PK, `eval_geom` Point, `eval_seq`, `eval_at`, `status` (`active|stale`).
- **Staleness:** when the anchor's `observed_at` is older than `maxAnchorAge`, or it is tombstoned or expired, the rule is suspended (no target-side or reverse alerts), `status = stale`, `trigger_rule.last_error` is set and one `system` notification fires per transition; it resumes on a fresh anchor observation.
- The persist gate uses the same current-anchor snapshot (`AnchorPositions`, updated from LiveBus/ChangeBus). Reverse-path alerts write no record versions.
- Privacy: anchor coordinates never appear in templates, notifications, embeddings or LLM prompts; templates get `{{anchor.distanceKm}}` only. Recommend anchor sources be `private` (enforced by 039).

### 6. Scopes, MCP and export

- Briefing scope (029) and Ask scope (028) accept `placeIds` and `viewId`: resolved server-side to areas and predicates; the LLM receives place **names** and km-rounded distances, never coordinates. Add `{place}` to the briefing digest headings.
- MCP tools (025 registry): `list_places`, `get_place`, `records_near_place` (read); `create_place`, `update_place`, `delete_place` (agent); `list_views`, `get_view_records` (read), `create_view` (agent); `list_watchlists`, `get_watchlist` (agent), `add_to_watchlist`, `remove_from_watchlist` (agent). Resource `foresignal://places` (names, shapes, radius; coordinates only in `detail=long` for agent tokens). Ask gains `propose_view`. `create_watch`/`update_watch` schemas gain `near` and `inWatchlist`.
- Settings export/import (019): places, views and watchlists behind `includePlaces`/`includeWatchlists` flags (default false; export warns that places reveal locations).

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`: tags `Places`, `Views`, `Watchlists`; schemas `Place`, `PlaceInput`, `PlacesAoi`, `SavedView`, `SavedViewInput`, `Watchlist`, `WatchlistInput`, `WatchlistItem`, `WatchlistImportResult`, `Near` (oneOf places/point/anchor), `Condition.op` + `inWatchlist`/`notInWatchlist`; operations above with `x-foresignal-scope` (places/views reads `read`, places/views writes `agent`, watchlists `agent`), `GET /v1/places/aoi` (`read`); `place`, `view`, `inWatchlist` params on records, history, tiles, changes, streams, search; codes `UNKNOWN_PLACE`, `PLACE_IN_USE`, `UNKNOWN_VIEW`, `UNKNOWN_WATCHLIST`, `WATCHLIST_TOO_LARGE`, `ANCHOR_STALE`. Update `AccessPolicy` (specific rules before `/v1/records/**`), BODIES examples (including a CSV import), regenerate Postman.
- `docs/Architecture.md`: new sections Places and AOI, Saved views, Watchlists, Moving anchors (amend §8.3/§11 trigger text). Charter FRs for places, views, watchlists and near-me. `docs/Agent-Guide.md` recipes: "track these 40 tails across every aircraft source", "alert me when an M5+ quake is within 150 km of my phone".

## Tests to write (do not run)

- `PlaceGeometryTest`: point/route buffers on geography (area ≈ π r² within 1%), polygon fixing, vertex cap, antimeridian polygon.
- `PlacesAoiServiceTest`: union and 150 km floor, envelope, empty → default AOI + health warning, recompute on change.
- `PlacesIT`: CRUD, `alwaysAuthenticated` reads (anonymous 401 even with `openReads`), delete 409 and force, rule re-materialisation on edit with no spurious edges for a stationary entity.
- `SavedViewsIT`: records/tiles/stream render the same set for one view; `view=` intersected with other filters and with a restricted token; projection; version-keyed tile cache.
- `RecordPredicateTruthTableTest`: SQL and Java evaluation agree on every row of `operator-truth-table.json`, including `inWatchlist`.
- `WatchlistsIT`: CSV import merge/replace/dryRun with invalid rows, normalisation per identity, export round trip, audit rows, size cap.
- `WatchlistRuleIT`: one rule with `sources=[a,b,c]` and `inWatchlist` fires for listed tails on each source via `entityKeys`; adding an item takes effect on the next observation; anonymous cannot see the resulting sensitive alerts.
- `MovingAnchorIT` (`MutableClock`): target enters while the anchor is still → enter; anchor moves toward a still target → reverse enter; anchor moves away → exit; move < `minMoveMeters` → no evaluation; stale anchor suspends and raises one system event; radius 0 inside a warning polygon.
- `McpPlaceToolsIT`: tool scopes and compact output with no coordinates for read tokens.

## Acceptance criteria

- A place defined once drives rules, views, tiles, SSE, briefings, Ask and the weather AOI.
- One watchlist rule covers the same entities across all sources that declare the identity.
- "Near me" rules fire from either side's movement and go quiet when the anchor is stale.
- No place or anchor coordinates reach anonymous readers, notifications or LLM prompts.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 129, 132, 133, 134).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 134).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `036: Places Views And Watchlists`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/036-CR-Places-Views-And-Watchlists.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `036: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
