# 036-CR-Places-Views-And-Watchlists

| | |
|---|---|
| Prompt | 036-Places-Views-And-Watchlists.md |
| Status | Complete |
| Implementation commit | 3d8cf4b8cbb2cf200a3353a0ae968051f83a407a |
| Date | 2026-10-12 |

## Summary

Places, saved views, watchlists and moving-anchor ("near me") rules are in, with one shared
vocabulary for "where" and "what" across rules, views, tiles, SSE, search, briefings, Ask, MCP and
the weather area of interest. All seven tasks landed.

**1. Places** (`ai.foresignal.place`)
- `PlaceEntity` (`place`) holds a point with `radius_km`, a polygon, or a route with `buffer_m`.
  - `area` is derived on every save by `PlaceGeometry`: a geography buffer for points and routes,
    the fixed polygon itself for polygons. It is split at the antimeridian and capped at 10,000
    vertices, and a GiST index is added through `SpatialIndexInitializer`.
  - Every save bumps `version` and publishes `PlaceChangedEvent` after commit.
- `PlaceService` handles validation (the id `aoi` is reserved), CRUD and a cache, and it is the
  trigger `PlaceLookup`.
- Deleting a referenced place is refused with `409 PLACE_IN_USE`. The problem's `references` member
  lists the rules, views and briefings. `force=true` deletes it anyway and disables the rules.
- `PlaceService` installs `PlaceAreas`, so a token's `placeIds` restriction (035's hook) resolves to
  the union of the places' areas. An unknown place fails closed.
- `PlacesAoiService` computes the union of the places, each buffered to at least
  `max(radiusKm, 150 km)`, with its envelope and a version, and publishes `PlacesAoiChangedEvent`.
  - With no places it returns the default Texas box, and AOI consumers get a `HealthRule` warning.
  - It is cached and recomputed on `PlaceChangedEvent`.
- `PlacesController` serves the six operations. Reads are `read` and always authenticated; writes
  are `agent`.

**2. Near rules**
- `near` takes `placeIds` or `placeId`, plus `radiusKm`. The fence is the union of the places'
  areas, grown by `radiusKm`.
- `geofence_origin` (`inline` or `near`) and the place versions are stored. `TriggerMapper` strips
  `placeVersions` from the `Near` output.
- A place save re-materializes every referencing rule in the same transaction
  (`RuleValidator.rematerialize`): the fence, the hysteresis band and the versions. A rule that can
  no longer be built is disabled, with `lastError` saying why.
- The gate then compares persisted state under the current fence, so a stationary entity never
  fires because of an edit.
- Alerts carry `context.near = {placeIds, placeVersions}`. Templates get `{{place.name}}` and
  `{{place.distanceKm}}`, never coordinates.

**3. Saved views** (`ai.foresignal.view`)
- `SavedViewEntity` (`saved_view`) holds a view; `SavedViewService` validates, stores and compiles
  it.
- `RecordPredicate` is the compiled form views share with rules.
  - `RuleSqlBuilder` now renders conditions through `RecordPredicate.conditionParts`.
  - It renders over any alias or payload expression, and evaluates in Java with the operators'
    gate meaning.
- A view runs as a request filter: a new `RecordFilter` member of `ReadRestriction`, ANDed by
  `RestrictionClamp`. So every 035 read path intersects it with the token restriction.
- `ViewsController` serves the definitions and the renderings:
  - records, in all 014 formats with the `fields` projection;
  - tiles, through a `VIEW` tile layer, with the ETag prefixed `v<version>`;
  - TileJSON, whose template carries `?v=<version>`;
  - `stream`.
- `view=`, `place=` and `inWatchlist=` are accepted on records, history, changes, tiles, both
  streams (`changes.*` on the event stream, and in PATCH blocks) and search, through `ReadScopes`
  (`RecordScopes`).
- A view that references a place or a watchlist needs a token to render (401).

**4. Watchlists** (`ai.foresignal.watch`)
- Lists live in `watch_list` and their items in `watch_list_item`, unique per list.
- `Identity` handles normalization and validation per identity, including the IMO check digit.
- Imports are CSV or JSON, in `merge` or `replace` mode, with `dryRun`. Bodies are limited to 5 MB
  (413) and the media type is checked (415). The CSV export round-trips with the import. A list
  holds at most 100,000 items (`WATCHLIST_TOO_LARGE`).
- `IdentityResolver` reads `entityKeys`.
- `inWatchlist` and `notInWatchlist` are `IdentityOperator` beans. Without a `path`, they switch on
  `source_id` to each source's identity field. Sources without the identity never match, and
  validation warns about them.
- The gate reads `WatchlistSnapshot`, which is refreshed on `WatchlistChangedEvent`. Edits converge
  on each entity's next observation.
- Rules that reference a watchlist mark their alerts `alert.sensitive = true`. The new
  `ReadRestriction.excludeSensitive` hides those alerts from anonymous callers and feed keys, in SQL
  (`NOT a.sensitive` on alert rows) and in memory.
- The shared truth table gained `watchlists` and `watchlistRows`.

**5. Moving anchors**
- `near.anchor` has the fields `{source, externalId}`, `radiusKm` (0 means "the anchor is inside
  the target"), `maxAnchorAge` and `minMoveMeters`. No fence is stored.
- `AnchorPositions` holds the current anchor positions. It is fed by the engine's change batches and
  its own `LiveBus` subscription.
- **Target side.** `RuleSqlBuilder` renders `&& ST_Expand` and `ST_DWithin` on geography
  (`ST_Intersects` when the radius is 0) against the anchor's current position, identically for `v`
  and `p`.
- **Anchor side.** `AnchorEvaluator` runs after each batch and on a 5 s tick. It reads and writes
  `trigger_anchor_state`, honors `minMoveMeters` and the 60 s refresh, and computes enter and exit
  over `record_head ⋈ record_live`. These alerts go through the same `AlertPass`, honor the stored
  cooldown, write no versions, and carry `context.anchor = {source, externalId, seq, distanceKm}`.
- **Staleness.** A stale, tombstoned or expired anchor suspends the rule (`status=stale`, a
  `lastError` starting with `ANCHOR_STALE`, one `system.anchor_stale` `SystemEvent` per transition).
  It resumes on a fresh observation.
- **Gate.** The gate uses the same anchor snapshot. The band near the radius is ambiguous, and an
  ambiguous entity is persisted when its geometry changed.

**6. Scopes, MCP and export**
- **Briefings.** `BriefingScope.placeIds` and `viewId` are validated. `PlaceBriefingScopeResolver`
  (`@Primary`) ANDs their filter into the run's restriction. The digest heading names the places,
  and digest SQL judges alerts by their snapshot payload.
- **Ask.** `AskPlaceScopeResolver` gives the model place names (in `AskScope.placeNames`) and
  narrows the tool caller's restriction.
- **New MCP tools:**
  - read, always authenticated: `list_places`, `get_place`, `records_near_place`;
  - agent: `create_place`, `update_place`, `delete_place`;
  - `list_views` and `get_view_records` (read), `create_view` (agent);
  - agent: `list_watchlists`, `get_watchlist`, `add_to_watchlist`, `remove_from_watchlist`.
- **MCP plumbing.** `ForesignalMcpTool` and `ForesignalMcpResource` gained `alwaysAuthenticated()`.
  `foresignal://places` returns coordinates only to agent tokens.
- **Ask actions.** Ask gained `propose_view`, with a `create_view` handler. The schemas of
  `create_watch` and `update_watch` gained `near`, and the condition `path` became optional.
- **Settings.** `GET /v1/settings/export?includePlaces&includeWatchlists` adds places, views and
  watchlists with a `warnings` list. Import restores them before the triggers that use them.

**7. Spec, Postman and docs**
- All 25 operations are `implemented` and off `pending-operations.txt`.
- `AccessPolicy` gained `/v1/places(/**)`, `/v1/views(/**)` and `/v1/watchlists(/**)`.
- The spec was reconciled (see Deviation 1 and the changelog entry).
- Postman was regenerated, with BODIES for places, views and watchlists, a CSV import example and
  id capture.
- Docs were updated: the Architecture gained §8.5–8.8 and §8.3 and §9 were amended, the Charter
  gained FR-85 to FR-88, the Agent Guide gained tool rows and recipes 7 and 8, the Adapter Guide
  gained §13 (`entityKeys`), and the Operator Guide gained Places, Saved views, Watchlists (identifier
  quality, PIA/LADD, the private-individuals policy), "Near me" rules and the export flags.
  `docs/Invariants.md` names the new tests.

## Files created / modified

**New, main:**
- `place/`: `PlaceEntity`, `PlaceRepository`, `PlaceView`, `PlaceGeometry`, `PlaceService`,
  `PlaceMapper`, `PlacesAoiService`, `PlaceChangedEvent`, `PlacesAoiChangedEvent`,
  `WeatherAoiProperties`
- `view/`: `SavedViewEntity`, `SavedViewRepository`, `SavedViewService`, `SavedViewMapper`,
  `PredicateFilter`, `RecordScopes`, `AskPlaceScopeResolver`, `PlaceBriefingScopeResolver`,
  `ViewActionHandlers`
- `watch/`: `Identity`, `WatchListEntity`, `WatchListItemEntity`, `WatchListRepository`,
  `WatchlistService`, `WatchlistMapper`, `WatchlistSnapshot`, `WatchlistChangedEvent`,
  `IdentityResolver`, `WatchlistOperator`, `WatchlistOperators`, `Csv`
- `trigger/`: `RecordPredicate`, `IdentityOperator`, `NearAnchor`, `AnchorPositions`,
  `AnchorEvaluator`, `AnchorDistance`, `AlertContexts`, `TriggerAnchorStateEntity`
- `security/restrict/`: `RecordFilter`, `RecordFilters`, `ReadScope`, `ReadScopes`, `PlaceAreas`
- `api/`: `PlacesController`, `ViewsController`, `WatchlistsController`
- `mcp/tools/`:
  - places: `PlaceItems`, `ListPlacesTool`, `GetPlaceTool`, `RecordsNearPlaceTool`,
    `CreatePlaceTool`, `UpdatePlaceTool`, `DeletePlaceTool`, `PlacesResource`;
  - views: `ListViewsTool`, `GetViewRecordsTool`, `CreateViewTool`;
  - watchlists: `ListWatchlistsTool`, `GetWatchlistTool`, `AddToWatchlistTool`,
    `RemoveFromWatchlistTool`

**Changed, main:**
- trigger: `RuleCondition` (identity paths), `RuleSpec` (anchor, near, sensitive), `RuleSpecs`,
  `RuleSqlBuilder`, `RuleValidator`, `ValidatedRule`, `TriggerPersistGate`, `TriggerEngine`,
  `PlaceLookup`, `AlertStore` (sensitive, context), `AlertView`, `AlertQuery`, `JsonTrees` (public)
- store: `TriggerRuleEntity` (`geofence_origin`), `AlertEntity` (`sensitive`, `context`)
- restrictions: `ReadRestriction` (filter, `excludeSensitive`, `placeIds`), `RestrictionClamp`,
  `TokenRestrictions` and `ApiTokenEntity` (`placeIds`, `allowed_place_ids`), `RestrictionValidator`,
  `TokenFilter` and `FeedKeyService` (`excludeSensitive`), `SecuritySettingsService`
- queries: `RecordQueryService`, `AsOfQueryService`, `TileQueryService`, `TileLayer`,
  `TileLayerResolver`, `TileLayerType` (`VIEW`), `SearchService`
- API: `ReadCallerResolver` (`resolveScoped`); `RecordsController`, `ChangesController`,
  `TilesController`, `SearchController`, `StreamController`, `AlertsController`, `SettingsController`;
  `AlertMapper`, `TriggerMapper`, `TileJsonMapper`; `ErrorCodes`; `AccessPolicy`
- SSE: `ChangesTopic`, `ChangeFilter`
- briefing and Ask: `BriefingScope`, `BriefingValidator`, `ScopeResolver`, `BriefingRunner`,
  `SqlDigestStatsProvider`, `AskScope`, `AskScopeResolver`, `AskService`, `AskActionTools`
- MCP: `ForesignalMcpTool`, `ForesignalMcpResource`, `McpToolRegistry`, `McpProtocol`, `WatchItems`,
  `AlertItems`
- other: `SettingsTransferService`, `SpatialIndexInitializer`

**Spec, docs, build:** `docs/OpenAPI.json`, `src/test/resources/openapi/pending-operations.txt` (−25
lines), `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`docs/Architecture.md`, `docs/Product-Charter.md`, `docs/Agent-Guide.md`, `docs/Adapter-Guide.md`,
`docs/Operator-Guide.md`, `docs/Invariants.md`, `src/test/resources/trigger/operator-truth-table.json`.

## Deviations from the prompt

1. **Spec reconciliations** (all recorded in `info.x-foresignal-changelog`):
   - `Condition.path` is optional for `inWatchlist` and `notInWatchlist`.
   - New codes `PLACE_EXISTS` and `VIEW_EXISTS` (409) name the conflicts the spec described without
     a code. `updateView` gained a 409.
   - `ANCHOR_STALE` is listed with `status: null`, because it is a `lastError` code, not a response.
   - Problem gained `references` for `PLACE_IN_USE`.
   - `AlertContext` gained `anchor`, `SettingsBundle` gained `warnings`, and
     `EventStreamChangesFilter` gained `place`, `view` and `inWatchlist`.
2. **Re-materialization runs in the place save's own transaction,** not in a listener on
   `PlaceChangedEvent`. The event is published after commit (for the AOI and later prompts). This
   gives the "one transaction" guarantee and avoids the 027 after-commit pitfall.
3. **Request filters live in the restriction.** `place=`, `view=` and `inWatchlist=` become a
   request-scoped `RecordFilter` member of `ReadRestriction`, so `RestrictionClamp` stays the one
   place that turns restrictions into checks. Filters are never stored (`toStored`) and are ignored
   by `within`. A path that has no payload expression fails a filter with conditions closed.
4. **SSE with payload conditions.**
   - Persisted changes are judged with their payload, loaded once per batch only while such a client
     is connected. A version that stops matching is sent as `leftFilter`.
   - Live updates and touches carry no payload, so they are delivered only for entities whose latest
     judged version matched. The filter keeps a bounded membership memory of 100,000 entities.
   - A live-only stream therefore starts sending live updates for an entity after its first
     persisted version.
5. **`radiusKm` with `place=`** buffers the places only where the endpoint has no `near`, or `near`
   is absent. Otherwise it stays `near`'s radius.
6. **Watchlist normalization** can change only while a list is empty, because stored values are
   normalized. The identity is fixed. `inWatchlist=` on a `custom` list matches nothing, since there
   is no identity path; use a view or rule with an explicit `path`.
7. **Places are never anonymous.** `placeIds` is refused for the anonymous profile and for feed keys.
   For tokens, 035's `LATER_MEMBERS` no longer includes it.
8. **AOI settings** are configuration properties (`foresignal.weather.aoi-buffer-km`, `default-aoi`,
   `aoi-sources`), not `app_setting` entries. Weather sources may also call
   `PlacesAoiService.registerConsumer`. The default-AOI health verdict is `DEGRADED`.
9. **Anchor details.**
   - Reverse alerts apply the rule's cooldown against stored alerts with one query per row.
   - The first time a rule sees its anchor, the position is recorded without firing.
   - Backtests (`testTrigger`) of anchored rules match nothing, because past anchor positions are not
     replayed.
   - The gate's Java distance is approximate (haversine for points, a local projection otherwise),
     with an uncertainty band treated conservatively.
10. **Briefing `{place}`** is appended to the digest's title heading (`name · Home, Work`). The
    LLM-mode writer gets the same filter.
11. **Ask views.** The view's predicate narrows the tool caller's restriction. The view's sources and
    kinds become the scope's only when the scope names none.
12. **Place MCP tools** are always authenticated through a new `alwaysAuthenticated()` hint, which is
    separate from `costBearing()` and so not charged to the expensive rate class. Agents get
    coordinates with `detail=long`; read tokens never do.
13. **Watchlist audit** relies on the token filter's generic write audit (target type `watchlists`)
    and the MCP registry's per-call audit. No second audit record is written.

## Issues found / open questions

1. **Owner:** feed keys and the anonymous profile now always exclude sensitive (watchlist) alerts.
   There is no override.
2. **Not verified at run time:** the geography-buffer area tolerances (1% to 1.5%), the anchor
   uncertainty band, and the CSV size-cap test (100,001 rows in a dry run). They are first run at
   checkpoint 041.
3. **No identity-bearing adapters exist yet** (USGS and the canary carry none). The aircraft and AIS
   prompts must declare `entityKeys` (Adapter Guide §13).
4. **Data:** no instructions directed at an agent were found in data, fixtures or tool output. No web
   pages were fetched.
5. Owner decisions carried from 012–035 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `place/PlaceGeometryTest` (PostGIS) | Point and route areas within 1% of π r² and 2·b·L + π b²; polygon fixing; shape checks; the vertex cap (refusal and simplification); antimeridian split |
| `place/PlacesAoiServiceTest` | Default box with `isDefault` and the consumer-only health warning; the 150 km floor, union and envelope; recompute with `PlacesAoiChangedEvent`; delete back to the default |
| `place/PlacesIT` | CRUD, 409 and 400 cases, filters and cursor; 401 for anonymous reads even with `openReads` (places, AOI, `place=`); 403 for read-token writes; `place=` with a `radiusKm` buffer; 409 `PLACE_IN_USE` with references, then `force`; re-materialization with no spurious edges, then convergence after a move |
| `view/SavedViewsIT` | CRUD; records, tiles and stream (replay plus live judgement, `leftFilter`, live membership) render the same set; `view=` intersected with bbox and with a restricted token; `/v1/changes?view=`; projection and CSV; version-keyed ETag and TileJSON; place views need a token |
| `trigger/RecordPredicateTruthTableTest` | SQL and Java agree on every truth-table row, `inWatchlist` and `notInWatchlist` included; identity conditions per source; the whole predicate (sources, kinds, area) |
| `watch/WatchlistsIT` | CSV merge, replace and dry run with invalid rows; MMSI and IMO normalization and check digit; JSON import; remove; normalize and identity rules; export round trip with quoting; audit rows; scopes and 404s; the 100,000-item cap |
| `watch/WatchlistRuleIT` | One rule over four sources fires on the three that declare `icao24` in different fields; adding and removing items act on the next observation only; sensitive alerts hidden from anonymous callers and feed-key restrictions; `inWatchlist=` on records |
| `trigger/MovingAnchorIT` | Target-side enter with distance and template; reverse enter and exit; small moves skipped; staleness with one system event, then resume; radius 0 inside warning polygons |
| `mcp/McpPlaceToolsIT` | Tool scopes; compact output with no place coordinates for read tokens; coordinates for agents with `detail=long`; `records_near_place`; views and watchlist tools; `foresignal://places` |
| Unit | `watch/IdentityTest` (identities, normalizations, CSV, static parsers, operator text), `trigger/AnchorDistanceTest`, `trigger/GateIdentityAnchorTest`, `trigger/AlertContextsTest`, `security/restrict/ReadRestrictionFilterTest` |

**Updated tests:**
- `RuleValidatorTest`: moving anchors, several places, the `placeIds` error field.
- `BriefingValidatorTest`: unknown place message.
- `AskServiceIT`: display name.
- `RestrictedReadCoverageTest`: `/v1/views` and the exempt place, view and watchlist tools.
- `PostmanCollectionContractTest`: watchlist validators; places, views and `importWatchlist` are
  schema-only.

`PlaceTestSources` (test support) adds the aircraft, phone, event and warning sources shared by every
036 IT, so they share one Spring context.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 0 errors, 71 warnings (unchanged).

No new dependencies.

## Notes for the next prompt

1. **037 (tracks, asOf frames)** and later read paths: call `ReadCallerResolver.resolveScoped`, or
   pass the `ReadScope`, so `place=`, `view=` and `inWatchlist=` apply. Pass the row's payload
   expression to `RestrictionClamp` (the six-argument overloads). Without it, a view's conditions
   fail closed.
2. **039 (OwnTracks, private sources):** an OwnTracks source is the natural anchor for "near me"
   rules. Keep it private, and fold `excludeSensitive` into `EgressPolicy` together with the other
   egress rules.
3. **045, 047–049, 052:** read `PlacesAoiService.aoi()` and `envelope()`, listen for
   `PlacesAoiChangedEvent`, and call `registerConsumer(sourceId)` (or list the source in
   `foresignal.weather.aoi-sources`) for the default-AOI health warning. Place-weather (052) reads
   `PlaceEntity.products` and must document that tombstoned place-weather records can fire exit
   rules.
4. **Aircraft and AIS adapters** must declare `entityKeys` for `icao24`, `registration`,
   `callsign`, `mmsi` and `imo` (Adapter Guide §13), or watchlist rules never match them.
5. **041 checkpoint:** the new ITs first run there. They share the `PlaceTestSources` context; the
   anchor tests drive `TriggerEngine.runAnchors()` directly on the `MutableClock`.
