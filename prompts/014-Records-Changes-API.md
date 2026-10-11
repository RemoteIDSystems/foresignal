# 014 — Records Changes API

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/014-CR-Records-Changes-API.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/013-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §1 (I5), §2 (time columns, `effective_time`), §8 (`record_head`, `record_live`, current payload, `seq_clock`, `S(T)`), §12 (query paths and asOf, **implement exactly**), §13 (windows, keyframes), §14 (observer gating), *Per-prompt changes* → the *Records Changes API* entry, *Spec changes*
- `docs/Gap-Analysis.md` §1 item 1 (offline sync), §3 rows "GeoJSON title" and "Streaming formats", §8.A item 4 ("Extend 011" there; 011 is now this prompt)
- `docs/Weather-Imagery-Design.md` §6.8 and §10.1 (Records/SSE row: `externalId` filter, `raster-frame` payload always inline)
- `docs/OpenAPI.json`: operations tagged `Records` and `Changes` (except `/v1/changes/stream`, prompt 015), schemas `Record`, `RecordPage`, `RecordVersion` (create if absent), `Feature`, `FeatureCollection`, `Change`, `ChangePage`, `KindSummary`, `Geometry`
- Completion Reports of 007 (tables, `seq_clock`), 008 (descriptor extras: `titleField`, `fieldSpecs`, `kindDisplay`, `validFrom`, `private`), 009 (`ChangeBus.lastPublishedSeq()`, `LiveBus`, `TestRecords`)

## Objective

Serve stored data to clients: current state from `record_head ⋈ record_live`, version history, playback ranges, transaction-time `asOf`, and the ordered change feed. The API must let an offline client keep a provably correct mirror (snapshot watermark, purge horizon, `validUntil` on changes), and must be cheap on mobile links (field projection, precision, simplification, streaming formats, ETags).

**No index on `record_live.observed_at`. Do not add one.** Touches must stay HOT updates. `order=observed` sorts the matched set per page by design.

## Tasks

### 1. Shared parameters (`ai.foresignal.api.support`)

- `BboxParser` → `BboxFilter(List<Envelope>)`: `minLon,minLat,maxLon,maxLat`, lon in [-180, 180], lat in [-90, 90], `minLat < maxLat`. **When `minLon > maxLon` the box crosses the antimeridian: split it into `[minLon,180]` and `[-180,maxLon]`** and OR the two envelopes in SQL. Replace the v1.1 rejection; prompt 015 and 016 reuse this parser.
- `NearParser`: `near=lon,lat` (GeoJSON order, same as bbox) plus `radiusKm` (0 < r ≤ 2000, required with `near`). SQL: a degree-envelope prefilter `l.geom && ST_Expand(point, :dLat/:dLon)` (computed conservatively in Java, with `cos(lat)` clamped and antimeridian split), then `ST_DWithin(l.geom::geography, :pt::geography, :radiusM)`.
- Multi-value `source`, `kind`, `externalId` (≤ 100 values) accept repeated params and comma-separated values.
- `CursorCodec`: opaque base64url JSON with a version prefix. Variants: `observed` `{o, s, e}`, `id` `{s, e}`, `distance` `{d, s, e}`, `seq` `{q}`, plus an optional pinned `w` (snapshot seq). Tampered or mismatched cursors → 400 `INVALID_CURSOR`.
- `FieldSelector`: `fields=` is a comma list of top-level properties (`source, externalId, kind, title, observedAt, geometry, …`) and `payload.<dot.path>` entries. Unknown top-level names → 400. Projection is applied in SQL for payload paths (`jsonb_build_object` of the selected `#>` paths) and in the mapper for top-level fields.
- `precision` 0–7 (default 7) → `ST_AsGeoJSON(geom, :precision)`. Storage precision is unchanged.
- `simplify` metres (0–100,000): `ST_Transform(ST_SimplifyPreserveTopology(ST_Transform(geom, 3857), :m / cos(radians(ST_Y(ST_Centroid(geom))))), 4326)`; points pass through untouched. Document that topology is preserved per feature only.
- `limit`: default 500, max 5,000 (history 50,000); out of range → 400.
- Statement timeout `foresignal.api.query.statementTimeout` (default 30 s) on every query in this prompt (`SET LOCAL statement_timeout` in a read-only transaction).

### 2. Current state (`ai.foresignal.store.query.RecordQueryService`)

Native SQL via `NamedParameterJdbcTemplate`; geometry as raw `ST_AsGeoJSON` text. Current reads touch **only** `record_head h JOIN record_live l USING (source_id, external_id)` (I5).

- Payload: `jsonb_strip_nulls(h.payload || l.kinematic)`. Geometry: `l.geom`.
- Filters: `source_id = ANY`, `h.kind = ANY`, `external_id = ANY`, bbox (`l.geom && env` plus `ST_Intersects`), near, `NOT h.tombstone`, `(l.valid_until IS NULL OR l.valid_until > now())` unless `includeExpired`, text search `h.search_text ILIKE` with `%`/`_` escaped.
- **`timeField`** (`effective` default | `observed` | `event` | `ingested`) selects the column for `since`/`until`: `COALESCE(h.event_time, l.observed_at)`, `l.observed_at`, `h.event_time`, or for `ingested` the seq bounds `h.seq >= cutoffSeq(since) AND h.seq < cutoffSeq(until)` through `seq_clock`.
- **`order`**: `observed` (default; keyset `(l.observed_at, source_id, external_id)` DESC) or `id` (keyset `(source_id, external_id)` ASC; the snapshot order). **`sort=distance`** requires `near`+`radiusKm`; it orders by `ST_Distance(l.geom::geography, :pt::geography)`, keyset `(distance, source_id, external_id)`, and each item carries `distanceKm`. `sort=distance` and `order` are mutually exclusive (400).
- **Snapshot watermark:** read `watermarkSeq = ChangeBus.lastPublishedSeq()` **before** the query and return it on every page. When the client passes `asOfSeq` or uses `order=id`, the cursor pins `w = asOfSeq ?? watermarkSeq` from page 1, and every page (page 1 included) filters `h.seq <= :w`; the response echoes `asOfSeq`. Entities updated after `w` are left out and arrive through `/v1/changes?after=w`. Document the client contract in the operation description: (1) open SSE with `after = watermarkSeq`, (2) page the snapshot with `order=id`, (3) apply an event only if its `observedAt` ≥ the stored one.
- **Private sources:** if a descriptor has `private=true` (008), exclude its records for anonymous `openReads` callers. Prompt 039 completes enforcement on every path.
- `get(source, externalId, include)`: head plus live. `include=observer` reads `observer` from the head's version (`WHERE seq = h.seq`, one leaf probe per tier) and is returned only to agent or admin tokens, or when the new `security.exposeObserver` setting (default false) is on; otherwise the field is omitted (not 403). Add `exposeObserver` to `SecuritySettings`.
- `kinds()`: head ⋈ live, current only, `GROUP BY h.kind` with count and sources, plus a `display` block from 008's `kindDisplay` when declared.

### 3. Versions, history, changes

- **`versions(source, externalId)`**: `WHERE source_id=? AND external_id=? ORDER BY seq`, with `since`/`until` on `observed_at`, `limit` (default 500, max 5,000) and a `seq` cursor. Items carry `persistReason, ingestedAt, receivedAt, batchId, quality, observer` (gated as above) and `downsampled` (false until prompt 044).
- **`history`**: over `record_version` by `timeField` (default `observed`) and bbox, keyset `(observed_at, seq)`; `ingested` becomes seq bounds via `seq_clock` so partitions prune. Required `since`; cap 50,000 per page.
- **`changes(after, filter, includePayload, limit ≤ 10000)`**: `seq > :after ORDER BY seq`, filters `source`, `kind`, `externalId`, bbox. Returns `lastSeq`, `hasMore`, `watermarkSeq` and `purgeHorizon`. Each `Change` carries `prevSeq, historyTier, persistReason, observedAt, eventTime, validUntil, qaFlags`. Payload loads use `seq = ANY(?)`. **Kinds listed in `foresignal.api.changes.inlinePayloadKinds` (default `[raster-frame]`) always include their payload**, even with `payload=false` (Weather §6.8).
- **Purge horizon → `410 RESYNC_REQUIRED`:** define `ai.foresignal.retention.RetentionState` if 019 has not (it publishes the real values later): `long purgeSeq(String sourceId)` (the global seq allocated when retention last **purged heads** of that source, i.e. entities disappeared without a tombstone; routine version deletes do **not** advance it, otherwise every client would have to resync after each hourly retention run; 0 = never), `long lowestRetainedSeq(Set<String> sources)`, and `Duration window(String sourceId)`. Ship a default bean returning 0 for both seqs and `SourceService.effectiveRetention`. `purgeHorizon` = max `purgeSeq` over the filtered sources (all sources when unfiltered). When `after < purgeHorizon`, return 410 problem `code RESYNC_REQUIRED` with `purgeHorizon` and `watermarkSeq` extensions; the client re-snapshots.

### 4. asOf (transaction time; Core-Data-Model §12, exactly)

`listRecords?asOf=T`:
1. A `source` or `kind` filter is required → else 400 `ASOF_FILTER_REQUIRED`.
2. `horizon(source) = now − window + effectiveKeyframe + 1 h`, with `effectiveKeyframe = min(policy keyframe, window/4)`; window from `RetentionState.window` (TRACK: `retention.trackWindow`, default 3d). T before the horizon of any involved source → 400 `ASOF_BEYOND_RETENTION` (include `horizon`).
3. T in the future, or combined with `asOfSeq`, `order=observed`, `sort=distance`, `includeExpired` or `since`/`until` → 400 `ASOF_INVALID`.
4. `S = S(T)` from `seq_clock` (§8; add a `SeqClockQuery` helper if 009 did not). Per tier, one query: TRACK `L = S(T − ttl − 2 min)`, STANDARD `L = 0`:
   ```sql
   SELECT DISTINCT ON (source_id, external_id) …
   FROM record_version v JOIN (VALUES …) s(source_id, ttl) USING (source_id)
   WHERE history_tier = :tier AND seq BETWEEN :L AND :S
   ORDER BY source_id, external_id, seq DESC
   ```
   Keep rows where `NOT tombstone AND (valid_until_explicit > T OR (valid_until_explicit IS NULL AND (s.ttl IS NULL OR ingested_at + s.ttl > T)))`; on TRACK ignore the explicit value. Apply bbox, near and kind to the chosen version. Keyset on `(source_id, external_id)` across both tier queries (merge in Java).
5. Response adds `asOf`, `watermarkSeq = S`, `approximate` (true when any source is SAMPLED or LATEST; prompt 044 adds the downsampled case) and `horizon`. Granularity is one batch; valid-time asOf is not offered (use `/v1/history`).

### 5. Output formats and caching

- **DTO** (`Record`): `id = "<source>:<externalId>"`, `version, seq, observedAt, persistedObservedAt, eventTime, validFrom` (if 008 added it), `validUntil, tombstone, kind, title, geometry, payload, quality, live (= l.dirty), qaFlags, attribution`, optional `distanceKm`, `ingestedAt` (versions, history and asOf only; drop it from `required`).
- **`title`**: from `record_head.title` (008/009). If the column is missing, compute it at read time with a shared `TitleResolver` (descriptor `titleField`, else the first non-empty of `title > name > callsign > place`, else `kind externalId`) and note it in the Completion Report. MCP (025) and Ask (028) reuse the resolver.
- **GeoJSON** (`?format=geojson` or `Accept: application/geo+json`; `format` wins): `Feature.properties = {source, externalId, kind, title, version, seq, observedAt, validUntil, live, payload}`; `FeatureCollection` adds `nextCursor`, `attributions`, `watermarkSeq`, `asOf`, `approximate`, `horizon`. Null geometry is allowed (RFC 7946).
- **Streaming formats** `format=ndjson` (`application/x-ndjson`), `geojsonseq` (`application/geo+json-seq`, RFC 8142 record separators) and `csv` (geometry as WKT, payload paths from `fields=` as columns; requires `fields=` for payload columns) on `/v1/records`, `/v1/history` and `/v1/changes`. Stream with `RowCallbackHandler` (fetch size 1,000) without paging, capped at `foresignal.api.stream.maxRows` (default 500,000). Run a `count(*)` over `… LIMIT cap+1` first; over the cap → 400 `RESULT_TOO_LARGE` (no silent truncation). Send `Foresignal-Watermark-Seq` as a header. Use Jackson 3's `tools.jackson.dataformat:jackson-dataformat-csv`.
- **ETags** (strong) with `If-None-Match` → 304, `Vary: Accept, Accept-Encoding, Authorization`, `Cache-Control: private, no-cache` (`public` when `openReads` is on): single record `"<version>.<l.observed_at epoch ms>"`; lists and kinds `hash(normalized query, lastPublishedSeq, LiveBus last batch number, now truncated to the minute)`. Add `lastPublishedBatchNo()` to `LiveBus` if 009 lacks it.
- **Compression:** `server.compression.enabled` (toggle `FORESIGNAL_COMPRESSION`, default true) with mime types `application/json, application/geo+json, application/x-ndjson, application/geo+json-seq, application/problem+json, application/vnd.mapbox-vector-tile`; never `text/event-stream`.

### 6. Kind schema

`GET /v1/kinds/{kind}/schema` (`getKindSchema`, read): merge the `fieldSpecs` of every source emitting the kind into JSON Schema 2020-12 (`type`, `description`, `enum` from `enumLabels`, `x-unit` UCUM code, `x-unitLabel`, `x-label`, `x-format`) plus `x-display` from `kindDisplay`. Conflicting types across sources → `oneOf` and an `x-conflicts` list of source ids. Unknown kind → 404. ETag from a hash of the merged document. If 008 did not add `fieldSpecs`, add an optional `Map<String, FieldSpec>` to the descriptor builder first.

### 7. Controllers

`RecordsController` (`listRecords`, `getRecord`, `listRecordVersions`, `queryHistory`, `listKinds`, `getKindSchema`) and `ChangesController` (`listChanges`), matching the spec. Unknown source or record → 404.

### 8. Spec, Postman, docs

- `docs/OpenAPI.json` per *Spec changes* plus: `listRecords` + `asOf`, `asOfSeq`, `order`, `sort`, `timeField`, `include`, `externalId`, `near`, `radiusKm`, `fields`, `precision`, `simplify`, format enum `json|geojson|ndjson|geojsonseq|csv`; `getRecord` + `include`, `fields`, `precision`; `listRecordVersions` + `since`, `until`, `limit`, `cursor`; `queryHistory` + `timeField`, `externalId`, `near`, `radiusKm`, `fields`, `precision`, `simplify`, formats; `listChanges` + `externalId`, formats, 410 response; new `getKindSchema` (`"x-foresignal-scope": "read"`). Schemas `Record`, `RecordVersion`, `RecordPage`, `FeatureCollection`, `Feature.properties`, `Change`, `ChangePage` (+ `watermarkSeq`, `purgeHorizon`), `KindSummary` (+ `display`), `Quality`, `Observer`. Error codes `ASOF_FILTER_REQUIRED`, `ASOF_BEYOND_RETENTION`, `ASOF_INVALID`, `RESYNC_REQUIRED`, `RESULT_TOO_LARGE`. Response media types for the streaming formats and 304s.
- `AccessPolicy`: add `GET /v1/kinds/*/schema` READ (the existing `/v1/kinds` rule matches only the exact path); extend `AccessPolicyTest`.
- Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`).
- `docs/Agent-Guide.md`: a "Keeping a mirror" section (snapshot with `order=id`, `watermarkSeq`, changes replay, 410 handling, `validUntil` expiry) and the Home Assistant `geo_json_events` URL pattern (`format=geojson`, bbox, explicit limit; works only with `openReads`).

## Tests to write (do not run)

Seed only through `TestRecords` (009). Never insert into `record_head`, `record_live` or `record_version` with SQL.

- `RecordQueryServiceIT`: merged payload after a LIVE update (key dropped → absent); each filter alone and combined; antimeridian bbox; near + radius on geography; `sort=distance` keyset across pages; expiry and tombstones; text escaping; `order=observed` and `order=id` paging with equal timestamps (no duplicates or gaps); **snapshot paging stable under mutation** (ingest between pages; pinned `w` excludes the updated entity, `/v1/changes?after=w` delivers it); `timeField` `effective` vs `observed` for a revised quake; `ingested` via `seq_clock`; observer gating per scope and setting; private source hidden from anonymous reads.
- `AsOfQueryIT`: asOf before and after a revision; a stationary keyframed aircraft (TRACK, SAMPLED) at a T between keyframes; a long-valid NWS-style alert (STANDARD, `L = 0`); 400 beyond the horizon; 400 without a filter; 400 `ASOF_INVALID`; `approximate` for SAMPLED.
- `ChangesQueryIT`: `after` semantics, `hasMore`, `validUntil` and `persistReason` present, `externalId` filter, `raster-frame` payload inline with `payload=false`, 410 `RESYNC_REQUIRED` with a stub `RetentionState`.
- `StreamingFormatsIT`: ndjson, geojsonseq (RS separators), csv with `fields`; `RESULT_TOO_LARGE` over a lowered cap.
- `RecordsControllerIT` / `ChangesControllerIT`: JSON and GeoJSON via both negotiation methods, spec field names, `title`, `fields`/`precision`/`simplify`, ETag and 304 on record, list and kinds, 400s (bad bbox, bad near, bad cursor, limit), 404s; `KindSchemaIT` merge and conflicts.
- `CursorCodecTest`, `BboxParserTest` (split), `NearParserTest`, `FieldSelectorTest`, `TitleResolverTest`, `SeqClockQueryTest` (`S(T)` and `cutoffSeq` edge cases).

## Acceptance criteria

- All `Records` operations, `listChanges` and `getKindSchema` match `docs/OpenAPI.json`.
- Current-state queries reference only `record_head` and `record_live`; no index on `record_live.observed_at` exists.
- asOf follows Core-Data-Model §12, with the horizon and the three 400 codes.
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
2. **Commit** all changes with the message `014: Records Changes API`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/014-CR-Records-Changes-API.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `014: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
