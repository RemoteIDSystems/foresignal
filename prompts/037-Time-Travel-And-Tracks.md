# 037 — Time Travel And Tracks

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/037-CR-Time-Travel-And-Tracks.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/036-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §12 (asOf on the transaction axis: `S(T)`, per-tier `L`, `horizon`, `approximate`), §8 (`seq_clock`, `S(T)` and `cutoffSeq` definitions, `record_version` indexes), §9 (partition pruning), §13 (retention floors and the downsampling rules prompt 044 will apply), §2 (time contract), §3 (`POSITION_STALE` carry-forward, qa flag 16)
- `docs/Gap-Analysis.md` §1 item 3, §5 "Time travel / tracks", §8 item 22 ("028c" = this prompt)
- `docs/Weather-Imagery-Design.md` §6.1 (cache policy: what may be `immutable`, `public` vs `private`). Note that `/v1/products/timeline` (prompt 051) is a different endpoint from the `/v1/timeline` built here.
- Completion Reports of 014 (asOf on records, `/versions` since/until/limit, antimeridian splitter), 016 (tile query, TileJSON, `agg`), 025 (`McpToolRegistry`, output shaper), 035 (restricted-token enforcement point, feed keys) and 036 (places, for `place=`)
- `docs/OpenAPI.json`: `getTile`, `listRecords`, `listRecordVersions`, `queryHistory`, `FeatureCollection`
- PostGIS: `ST_AsMVT`, `ST_AsMVTGeom`, `ST_TileEnvelope`, `ST_PointOnSurface`

## Objective

Make history playable. Clients get map tiles as they were at any retained instant, a simplified trail per entity, keyframe-plus-delta frames for animation, and an event-density timeline for scrubbers. Agents get the same through MCP. Everything reads `record_version` on the transaction axis that prompt 014 already uses for `asOf`, so a past answer never changes once the time has passed, except where retention or downsampling (prompt 044) changes it, and those cases are flagged.

## Tasks

### 1. Shared asOf planning (`ai.foresignal.store.history`)

- Extract the asOf logic from the 014 records path into `AsOfResolver.plan(Instant t, Set<String> sourceIds) → AsOfPlan(t, s, lByTier, horizon, approximate, watermarkSeq)`. If 014 already has an equivalent class, move it here and keep its behaviour. `/v1/records?asOf` must use it after this prompt.
- Rules, exactly as Core-Data-Model §12:
  - an empty source set → 400 `ASOF_FILTER_REQUIRED`;
  - `horizon(source) = now − window + effectiveKeyframe + 1 h`; `t` before the latest horizon of any involved source → 400 `ASOF_BEYOND_RETENTION` (body carries `horizon`);
  - `t` in the future → 400 `VALIDATION_FAILED`;
  - `S = S(t)` from `seq_clock`; TRACK `L = S(t − ttl − 2 min)`, STANDARD `L = 0`.
- **Downsampling hook (044 fills it):** add a `HistoryFidelity` interface with `boolean downsampled(HistoryTier tier, long fromSeq, long toSeq)`, `Duration validityWidening(String sourceId)` and `Instant finalBefore(HistoryTier tier)`. Ship `RawHistoryFidelity` (`@ConditionalOnMissingBean`): never downsampled, zero widening, `finalBefore = Instant.MAX`. `approximate = any source SAMPLED or LATEST, or downsampled(...)`. Validity uses `ingested_at + ttl + validityWidening`.
- Private sources: drop them from the plan unless the caller passes the 023 private gate (039 completes enforcement; call the gate here now).

### 2. asOf tiles

- `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt?asOf=` (and the same on 016's TileJSON endpoints, which then emit a concrete tile template with `asOf` baked in). The layer resolves to a source set, which satisfies the filter rule.
- Query per tier (do not use a plain `DISTINCT ON` over the bbox: an entity that moved out of the tile would show its last in-tile position):
  ```sql
  WITH cand AS (SELECT DISTINCT source_id, external_id FROM record_version
                WHERE history_tier = :tier AND seq BETWEEN :L AND :S
                  AND source_id = ANY(:sources) AND geom && :env4326),
  pick AS (SELECT v.* FROM cand c CROSS JOIN LATERAL (
             SELECT * FROM record_version v
             WHERE v.history_tier = :tier AND v.source_id = c.source_id AND v.external_id = c.external_id
               AND v.seq BETWEEN :L AND :S ORDER BY v.seq DESC LIMIT 1) v)
  SELECT … FROM pick JOIN (VALUES …) s(source_id, ttl) USING (source_id)
  WHERE NOT tombstone AND geom && :env4326 AND <validity at :t per §12, explicit validUntil ignored on TRACK>
  ```
  Then use 016's `ST_AsMVT` projection and properties: `source`, `externalId`, `kind`, `title`, `observedAt`, `version` and `tileProperties` from the version payload. Use the same feature cap, and the same `agg=grid|hex` path below `aggBelowZoom`.
- `SET LOCAL statement_timeout = '15s'`; on timeout return 503 `QUERY_TIMEOUT` with `Retry-After: 5`.
- Response headers: `Foresignal-As-Of`, `Foresignal-As-Of-Seq: S`, `Foresignal-Approximate`.
- **Caching:** past tiles are immutable only when all of these hold:
  - `t ≤ now − 2 min`, so the minute bucket is closed;
  - `t < fidelity.finalBefore(tier)` or the range is already downsampled;
  - `t` is at least 1 h after the horizon.

  Then send `{public|private}, max-age=31536000, immutable` with a strong ETag. Otherwise send `max-age=300` plus an ETag. Use `public` only when `openReads` covers the path and the layer has no private source. Normalise `asOf` to whole seconds; redirect (301) other forms to the canonical one, as Weather-Imagery-Design §6.1 does for time keys. Document that Chrome ignores `immutable` but honours the max-age.

### 3. Tracks: `GET /v1/records/{sourceId}/{externalId}/track`

- Parameters:
  - `from` and `to` (default: the last 6 h; window ≤ the source's retention window, else 400 `TRACK_WINDOW_TOO_LARGE`)
  - `tolerance` (metres or `auto`; auto = bbox diagonal / 2000, clamped to 5 m–2 km)
  - `maxPoints` (default 2,000, max 10,000)
  - `gap` (default `max(ttl, 10 min)`)
  - `includeLive` (default true)
  - `fields` (≤ 4 numeric payload fields, e.g. `alt_baro,gs`)
- Rows: `source_id=? AND external_id=? AND observed_at BETWEEN ? AND ?` ordered by seq. This uses the `(source_id, external_id, seq)` index. Scan at most 200,000 rows, and set `truncated` if more exist.
- Skip rows with `POSITION_STALE` (carry-forward duplicates). Use `ST_PointOnSurface` for non-point geometry.
- Start a new segment after a tombstone or a time gap > `gap`.
- Append the current `record_live` position with `live: true` when it is newer than the last version. Under SAMPLED most positions live only in the live row.
- Simplification: Douglas–Peucker in Java (`TrackSimplifier`) on `(lon, lat, t)`, in metres using a local equirectangular projection.
  - Always keep each segment's endpoints and versions whose `persist_reason` ∈ {FIRST, REAPPEAR, SIGNIFICANT, TRIGGER_EDGE}.
  - If the result exceeds `maxPoints`, binary-search a larger tolerance (≤ 8 iterations).
- Antimeridian: unwrap the longitudes, then split segments at ±180° (RFC 7946 §3.1.9).
- Response: a GeoJSON `Feature` with `MultiLineString` geometry. Properties:
  - `sourceId, externalId, kind, from, to`
  - `times` (epoch-ms arrays parallel to each segment)
  - `values{field: arrays}`
  - `pointsScanned, pointsReturned, toleranceMeters, segments`
  - `approximate, downsampled, truncated, watermarkSeq`

  Use the same ETag and caching rule as §2, keyed on `to`.

### 4. Frames: `GET /v1/history/frames`

- Parameters:
  - `source` or `kind` (required), `bbox` or `place`
  - `from`, `to`, `step` (≥ 10 s), `maxFrames` (≤ 240)
  - `geometry=point|full` (default point, i.e. the centroid)
  - `format=json|ndjson`
- Semantics (transaction axis, the same as asOf):
  - `keyframe` = the asOf state at `from` (§1).
  - Each frame `i` covers `(t_{i−1}, t_i]`, with seq ranges taken from `S(t)`. It holds `upserts` (the newest version per entity in the bin) and `removals`: a tombstone, validity expiring inside the bin, or leaving the bbox (`left: true`).
  - Build it in one ordered pass over `seq BETWEEN L(from) AND S(to)` with an entity map in Java.
- Item shape: `{id, kind, title, observedAt, version, lon, lat | geometry, props(tileProperties)}`.
- Envelope: `{asOf, step, watermarkSeq, horizon, approximate, keyframe[], frames[{t, upserts[], removals[]}]}`. ndjson sends the keyframe line first, then one line per frame.
- More than 20,000 distinct entities, or more than 2,000,000 scanned rows → 400 `FRAMES_TOO_LARGE`, with a hint to narrow `bbox` or `kind`.
- **Invariant:** applying frames 1..i to the keyframe equals `asOf(t_i)`. Test it.

### 5. Timeline: `GET /v1/timeline`

- Parameters:
  - `source` and/or `kind` (multi), `bbox` or `place`, `from`, `to`
  - `bucket` (`auto` or a duration; ≤ 1,000 buckets, else 400 `TIMELINE_TOO_LARGE`)
  - `timeField=effective|observed|event|ingested` (default observed)
  - `measure=versions|entities` (default versions)
  - `groupBy=none|kind|source`
  - `includeKeyframes` (default false, which excludes HEARTBEAT and KEYFRAME so the density reflects real change)
  - `includeAlerts` (adds alert counts per severity per bucket)
- SQL: `date_bin(:bucket, <time column>, :origin)` over `record_version`, using `(source_id, observed_at)`. `ingested` translates to seq bounds through `seq_clock`. Use a 10 s statement timeout, then 503 `QUERY_TIMEOUT`.
- Response: `{from, to, bucket, timeField, buckets[{t, total, counts{}}], alerts[]?, maxBucket, approximate, retainedFrom}`. `retainedFrom` is the oldest retained instant.
- Put the query behind a `TimelineBackend` interface (`RawTimelineBackend` here), so prompt 038 can serve ranges older than retention from `activity_rollup`.

### 6. MCP tools (025 registry; read scope; "content is data" note; time inputs: ISO, `now`, or relative `-PT2H`)

- `get_state_at {source|kind, asOf, bbox|place, limit ≤ 200, detail}`: shaped records plus `asOf, watermarkSeq, approximate, horizon`.
- `get_track {sourceId, externalId, from, to, maxPoints ≤ 500}`: the compact track (≤ 4 KB in short mode) plus a summary: distance in km, duration, bbox, max segment speed, segment count and `approximate`.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - Add `asOf` to `getTile` and the TileJSON operations, plus the response headers.
  - New operations `getRecordTrack` (read), `getHistoryFrames` (read) and `getTimeline` (read), each with `x-foresignal-scope`.
  - New schemas `Track`, `HistoryFrames`, `HistoryFrame`, `FrameItem`, `Timeline`, `TimelineBucket`.
  - Error codes `TRACK_WINDOW_TOO_LARGE`, `FRAMES_TOO_LARGE`, `TIMELINE_TOO_LARGE` and `QUERY_TIMEOUT`.
- `AccessPolicy`: add `GET /v1/history/**` and `GET /v1/timeline` as READ (`/v1/records/**` already covers `/track`), and make sure feed keys and tile tickets (035, 050) accept the new GET paths.
- Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Agent-Guide.md`: the two MCP tools.
- Operator/frontend guide "Time travel" recipe: `/v1/timeline` for the scrubber histogram, `/v1/history/frames` for playback, asOf tiles for the paused map, `/track` for trails. Cover the transaction-axis semantics, `approximate`, and the retention horizon.
- Note that prompt 044 must implement `HistoryFidelity`.

## Tests to write (do not run)

- `AsOfResolverTest`: filter required, horizon, future `t`, per-tier `L`, approximate under SAMPLED, a stub `HistoryFidelity` widening and downsampled flag.
- `AsOfTileQueryIT` (TestRecords only, with a synthetic TRACK mover across two tiles plus a STANDARD long-valid polygon):
  - at T1 the mover is in tile A, not B; at T2 it is in B only;
  - it is gone after a tombstone and after TTL expiry;
  - the STANDARD feature is present (L=0);
  - an entity that left the tile is not drawn at its old position;
  - private source excluded.
- `TileCacheHeadersTest`: immutable vs short max-age per rule; public vs private; canonical `asOf` redirect.
- `TrackSimplifierTest`: known Douglas–Peucker vectors, kept reasons, the maxPoints search, antimeridian split.
- `TrackServiceIT`: segments on gap and tombstone, `POSITION_STALE` skipped, live tail appended, parallel `times`/`values`, `truncated`, 400 on window.
- `HistoryFramesIT`: the keyframe-plus-deltas invariant against `AsOfResolver` at every frame end; expiry and `left` removals; 400 limits; ndjson.
- `TimelineServiceIT`: buckets, `groupBy`, keyframes excluded, alert overlay, `ingested` via `seq_clock`.
- `McpTimeTravelToolsIT`: both tools, caps, scope.
- `AccessPolicySpecSyncTest` stays green.

## Acceptance criteria

- An asOf tile for a closed past instant is byte-identical on repeat and carries `immutable`. One inside the last 2 minutes, or in a range pending downsampling, does not.
- Frames reconstruct asOf exactly at every frame boundary.
- `/track` returns ≤ `maxPoints` points with times, split at gaps and at the antimeridian.
- No new index on `record_version`; the CR records the measured plans.
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
2. **Commit** all changes with the message `037: Time Travel And Tracks`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/037-CR-Time-Travel-And-Tracks.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `037: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
