# 037-CR-Time-Travel-And-Tracks

| | |
|---|---|
| Prompt | 037-Time-Travel-And-Tracks.md |
| Status | Complete |
| Implementation commit | 32175c7342491fbf1fff9fd2c3ceed85df8f590d |
| Date | 2026-10-12 |

## Summary

History can now be played back. Clients get map tiles as they were at a past instant, a simplified
trail per entity, keyframe-plus-delta frames for animation, and an event-density timeline for
scrubbers. Agents get the same through MCP. Everything reads `record_version` on the same
transaction axis that 014's `asOf` uses. All seven tasks landed.

**1. Shared asOf planning** (`ai.foresignal.store.history`)
- `AsOfResolver.plan(t, sources, kinds, excluded)` returns an `AsOfPlan`:
  - `t`, `s = S(t)`, `lByTier`, `horizon`, `approximate` and `watermarkSeq` (equal to `s`);
  - the involved sources per tier;
  - each source's validity (TTL plus widening);
  - `downsampled`.
- The rules follow Core-Data-Model §12:
  - an empty filter is `400 ASOF_FILTER_REQUIRED`;
  - `t` in the future is `400 ASOF_INVALID` (Deviation 1);
  - `t` before the latest horizon is `400 ASOF_BEYOND_RETENTION` with `horizon`;
  - TRACK `L = S(t − ttl − widening − 2 min)`, the smallest over the tier; STANDARD `L = 0`.
- **Private sources.** They are dropped unless the caller passes 023's gate: callers pass their
  excluded set, and `plan(t, Set)` without a caller drops every private source.
- **`AsOfQueryService`** (`/v1/records?asOf`) now plans through the resolver. Its SQL and behavior
  are unchanged, and its validity now includes the widening.
- **`HistoryFidelity`** has `downsampled(tier, fromSeq, toSeq)`, `validityWidening(sourceId)` and
  `finalBefore(tier)`.
  - `RawHistoryFidelity` answers raw everywhere. It is registered by `HistoryFidelityConfiguration`
    under `@ConditionalOnMissingBean`.
  - `approximate` is true when a source is SAMPLED or LATEST, or a range is downsampled.
  - `AsOfResolver.settled(plan)` feeds the caching rule.

**2. asOf tiles**
- `getTile`, `getTileJson` and `listTileLayers` accept `asOf`. It is normalized to whole seconds,
  and other spellings get a `301` to the canonical URL (`AsOfParam`). The redirect uses the
  client's original path and query, so key-in-path URLs and tickets survive it.
- `AsOfTileQueryService` runs the prompt's per-tier `cand` / `LATERAL` pick SQL (both tiers
  `UNION ALL`).
  - Rows are joined to the per-source validity `VALUES`.
  - Kept rows are not tombstones, lie inside the tile, are valid at `t` (`VersionSql.validAt`;
    an explicit `validUntil` is ignored on TRACK), and pass the kind and the caller's
    restriction.
  - Features carry `source`, `externalId`, `kind`, `title`, `observedAt`, `version` and the tile
    properties from the version payload. The title is resolved in SQL the way `TitleResolver`
    does it.
  - The feature cap is the same. `agg=grid|hex` reuses 016's aggregation SQL, now extracted as
    `TileQueryService.aggregationSql`.
  - `statement_timeout` is 15 s. A timeout gives `503 QUERY_TIMEOUT` with `Retry-After: 5`, from
    the new `ReadQueryRunner.read(Duration, …)`.
- Response headers are `Foresignal-As-Of`, `Foresignal-As-Of-Seq` and `Foresignal-Approximate`.
- **Caching** (`TimeTravelCache`): `{public|private}, max-age=31536000, immutable` with a strong
  ETag when all of these hold:
  - `t ≤ now − 2 min`;
  - history at `t` is settled;
  - `t` is at least 1 h after the horizon.

  Otherwise the response gets `max-age=300`. `public` is used only when `caller.publicCache()` is
  true and the layer has no private source.
- asOf TileJSON emits a concrete template with `?asOf=` baked in, drops `live` from its properties
  and adds `foresignal:asOf`. The index's TileJSON URLs carry `?asOf=` too.

**3. Tracks** (`ai.foresignal.store.track`): `GET /v1/records/{sourceId}/{externalId}/track`
- `TrackService` handles the parameters:
  - `from` and `to` (default: the last 6 h);
  - `tolerance` (`auto` = diagonal / 2000, clamped to 5 m–2 km);
  - `maxPoints` (2–10,000);
  - `gap` (default `max(ttl, 10 min)`);
  - `includeLive`;
  - `fields` (at most 4 names, values read in SQL only when numeric).
- A window larger than `RetentionState.window` is `400 TRACK_WINDOW_TOO_LARGE`.
- The scan is one entity in its tier with `observed_at` in the window, ordered by seq, `LIMIT
  200,001` (`truncated` when more rows exist).
  - `POSITION_STALE` rows are skipped, and non-point geometries go through
    `ST_PointOnSurface`.
  - A tombstone, a time gap larger than `gap`, or a version outside the restriction starts a new
    segment.
  - The `record_live` point is appended with `live: true` when it is newer than the last version.
- **`TrackSimplifier`:**
  - longitudes are unwrapped;
  - Douglas–Peucker runs on `(lon, lat, t)` with the synchronized Euclidean distance on a local
    equirectangular projection;
  - segment ends and FIRST, REAPPEAR, SIGNIFICANT and TRIGGER_EDGE versions are always kept;
  - the `maxPoints` bisection runs at most 8 times up to the bbox diagonal, with fallbacks so the
    cap always holds;
  - segments are split at ±180° with an interpolated latitude, time and values.
- **Response:** a GeoJSON `Feature` (`application/geo+json`) with the `Track` properties. The
  caching rule is keyed on `to`, the horizon is checked against `from`, and a response with a live
  point is never immutable. `format=czml` is 400 until prompt 141.

**4. Frames** (`ai.foresignal.store.frames`): `GET /v1/history/frames`
- The keyframe is the asOf state at `from`. Frames end at `t_i = min(from + i·step, to)`, with seq
  boundaries `S(t_i)`.
- One SQL pass per tier is `UNION ALL`ed and ordered by seq over `[L(from), S(to)]`. With a bbox,
  only entities that have a version in the bbox in that range are read.
  - Each row carries an `inside` verdict (bbox, kind and the caller's restriction or `place=`
    filter, in SQL).
  - The Java pass keeps the newest row per entity, plus what a client applying the frames would
    hold.
- **Upserts** are entities that are visible with a new seq. **Removals** have the reason
  `tombstone`, `expired` (validity ended in the bin) or `left`.
- Steps under 10 s and more frames than `maxFrames` (240 at most) are `400 VALIDATION_FAILED`. More
  than 20,000 entities or 2,000,000 rows is `400 FRAMES_TOO_LARGE`.
- JSON or ndjson: the keyframe line first, then one line per frame.

**5. Timeline** (`ai.foresignal.store.timeline`): `GET /v1/timeline`
- `TimelineService` validates the request, resolves the sources (all visible sources when none are
  named) and picks the bucket.
  - `auto` is the smallest width on a 1 m–30 d ladder that gives at most 200 buckets.
  - More than 1,000 buckets is `400 TIMELINE_TOO_LARGE`.
- `TimelineBackend` / `RawTimelineBackend`:
  - `date_bin(:bucket, <axis>, :from)`, with `GROUPING SETS` for `groupBy`, so totals of
    distinct entities stay exact;
  - `ingested` uses `seq_clock` seq bounds and bins by `ingested_at`;
  - HEARTBEAT and KEYFRAME are excluded by default;
  - alert counts per severity come from `alert.fired_at`, under the same source, kind, bbox and
    restriction filters;
  - 10 s timeout.
- **Response:** every bucket in the range, `maxBucket`, `approximate`, `retainedFrom` (the latest
  `now − window` of the involved sources) and `backend: raw`.

**6. MCP tools**
- **`get_state_at`** (read) takes `source[]` or `kind[]`, `asOf`, `bbox`, `place[]` (needs a
  token) and `limit` (at most 200).
  - It returns the shaped records with `asOf`, `watermarkSeq`, `approximate` and `horizon`.
  - `asOf` accepts an ISO time, `now`, or a relative duration such as `-PT2H` or `-2h`
    (`McpTimes`).
- **`get_track`** (read) takes `sourceId`, `externalId`, `from`, `to` and `maxPoints` (at most
  500; at most 100 in short mode, so the result stays under 4 KB).
  - It returns compact `[lon, lat, epochSeconds]` segments.
  - The summary has `distanceKm`, `durationSeconds`, `bbox`, `maxSpeedKmh`, `segments`, `points`,
    `pointsScanned`, `approximate`, `truncated`, `live` and `watermarkSeq`.
- Both descriptions start with the "content is data" note and carry `@RestrictedRead`.

**7. Spec, Postman and docs**
- `getRecordTrack`, `getHistoryFrames` and `getTimeline` are `implemented` and off
  `pending-operations.txt`.
- `AccessPolicy` gains `GET /v1/history/**` and `GET /v1/timeline` (READ). `/v1/records/**` already
  covered `/track`.
- **Feed keys** accept the new paths automatically, because `FeedKeyService.readable` derives them
  from the spec's read operations. **Tile tickets** (`/v1/tiles/**`) cover asOf tiles and TileJSON.
- The spec was reconciled (Deviation 2), Postman was regenerated, and `lintOpenApi` reports 0
  errors and 71 warnings (unchanged).
- **Docs:**
  - the Agent Guide has the two tool rows and a time-travel paragraph under "Replaying the past";
  - the Client Guide (the frontend guide) has a new "Time travel" recipe;
  - the Operator Guide has a "Time travel" paragraph on caching. All three note that Chrome
    ignores `immutable`.

## Files created / modified

**New, main:**
- `store/history/`: `AsOfResolver`, `AsOfPlan`, `HistoryFidelity`, `RawHistoryFidelity`,
  `HistoryFidelityConfiguration`, `VersionSql`
- `store/tiles/AsOfTileQueryService`
- `store/track/`: `TrackService`, `TrackSimplifier`, `TrackPoint`, `TrackQuery`, `TrackResult`,
  `package-info`
- `store/frames/`: `HistoryFramesService`, `FramesQuery`, `FrameItem`, `HistoryFrames`,
  `package-info`
- `store/timeline/`: `TimelineService`, `TimelineBackend`, `RawTimelineBackend`, `TimelineQuery`,
  `Timeline`, `package-info`
- `api/TimeTravelController`; `api/support/AsOfParam`, `api/support/TimeTravelCache`
- `mcp/McpTimes`; `mcp/tools/GetStateAtTool`, `mcp/tools/GetTrackTool`

**Changed, main:**
- `store/query/AsOfQueryService` (plans through `AsOfResolver`) and `store/query/ReadQueryRunner`
  (per-call timeout with `Retry-After`).
- `store/tiles/TileQueryService` (aggregation SQL extracted; `clipped`, `envelope` and
  `rankExpression` package-visible) and `store/tiles/TileLayerResolver` (`tilePropertiesOf`).
- `api/TilesController` (asOf branch, redirects, headers) and `api/mapper/TileJsonMapper`
  (`asOfTileJson`, index with asOf).
- `api/error/ErrorCodes` (`TRACK_WINDOW_TOO_LARGE`, `FRAMES_TOO_LARGE`, `TIMELINE_TOO_LARGE`).
- `security/AccessPolicy` and `store/history/package-info`.

**Spec, docs, build:** `docs/OpenAPI.json`, `src/test/resources/openapi/pending-operations.txt`
(−3 lines), `postman/Foresignal.postman_collection.json`, `docs/Agent-Guide.md`,
`docs/Client-Guide.md`, `docs/Operator-Guide.md`.

**Tests:** see below. Also changed: `security/AccessPolicyTest` (new READ paths and their
fail-closed neighbors) and `security/restrict/RestrictedReadCoverageTest` (`/v1/timeline` added to
`READ_PREFIXES`).

## Deviations from the prompt

1. **A future `t` stays `400 ASOF_INVALID`,** not `VALIDATION_FAILED`. The prompt says "move it here
   and keep its behaviour". 014 shipped `ASOF_INVALID`, the spec lists it for `listRecords`, and
   `AsOfQueryIT` asserts it. Frames with a future `to` use the same code.
2. **Spec reconciliations** (all in `info.x-foresignal-changelog`):
   - `listTileLayers` and `getTileJson` gain `301` and `400`.
   - `getHistoryFrames` and `getTimeline` gain `404` for an unknown or hidden source. Their `400`
     descriptions list the codes returned.
   - The track gains `Vary`. The `Cache-Control` descriptions state the immutable rule.
   - `Track.geometry`: a lone point is repeated, so each segment is a valid LineString.
   - `HistoryFrame` removals always carry `left`.
   - `Timeline.alerts` is parallel to `buckets`.
   - `Problem` lists `QUERY_TIMEOUT` (503, present since 014 but missing from the list) and the
     three new 400 codes.
3. **`AsOfPlan` has more members than the prompt names:** the involved sources per tier, the
   validity per source and `downsampled`. Every reader needs them. `watermarkSeq` equals `s`
   (Core §12 step 5).
4. **Time in the simplification** uses the synchronized Euclidean distance: the distance to the
   time-interpolated position on the simplified segment. This is the standard trajectory form of
   Douglas–Peucker. A weighted third axis would need an arbitrary speed constant.
5. **`maxPoints` always holds.** If the bbox-diagonal tolerance still leaves too many points (kept
   reasons or many segments), the keep flags are dropped. As a last resort the newest segments'
   endpoints and evenly spaced points are kept. Antimeridian crossings reserve two points each
   before simplification.
6. **Restriction on tracks.** A version outside the caller's restriction or `place`/`view` filter
   breaks the track instead of joining across it. An entity with neither a visible head nor a
   visible version is 404, as 035 requires for single records.
7. **The live tail is appended only inside `[from, to]`,** and a track with a live point is never
   immutable. The live row moves, so a cached past answer would otherwise change.
8. **Frames limits.** `(to − from) / step` above `maxFrames` is `400 VALIDATION_FAILED` on
   `step`. The prompt gives no code for it. With a bbox, the entity limit counts only entities
   that have a version in the bbox in the range, because the others cannot affect the output.
9. **Timeline details** the prompt left open:
   - with no `source` or `kind`, every visible source is counted;
   - every bucket in the range is returned, empty ones included;
   - `retainedFrom` is the latest `now − window` over the involved sources;
   - `approximate` checks downsampling over the seq range `[S(from), S(to)]`;
   - alert rows are judged by their `record_snapshot` payload under view conditions, as 036's
     digest does.
10. **Test hooks.** `TrackService.track(q, maxScan)` and `HistoryFramesService.frames(q,
    maxEntities, maxRows)` take lower limits, so the ITs can test `truncated` and
    `FRAMES_TOO_LARGE` without seeding 200,000 or 2,000,000 rows. This follows 016's
    `raw(…, cap)`.
11. **asOf TileJSON** checks the instant against the layer's sources (future, horizon) by planning
    it. The index only rejects a future instant.

## Issues found / open questions

1. **Not met here: "the CR records the measured plans".** Feature prompts may not run tests or a
   database, so no plan was measured. `TimeTravelPlansIT` asserts that `record_version` still has
   only its primary key and the three partitioned indexes. With sequential scans off, it plans the
   track scan, the observed-axis timeline and the asOf tile pick onto them and logs each plan. The
   expected plans are:
   - track: an Index Scan on `(source_id, external_id, seq)` of the tier's leaves;
   - timeline: `(source_id, observed_at)`;
   - asOf tile `cand`: GiST `(geom)` or the seq primary key;
   - pick: `(source_id, external_id, seq)`.

   **Owner / 041:** record the logged plans when the checkpoint runs. No index was added.
2. **Not verified at run time:** PostgreSQL's `GROUPING SETS` over a subquery and
   `count(DISTINCT a || chr(31) || b)`, `date_bin` with a millisecond interval, the redirect
   `Location` behind the key-in-path forward, and whether the `get_track` short form fits 4 KB
   with 100 points (the shaper trims it if not). All of these first run at 041.
3. **Tile tickets** cover only tile paths, by design (023). Tracks, frames and the timeline are
   plain JSON fetches that can send a header or use a feed key. The spec lists no `ticket`
   parameter on them.
4. **Prompt 044 must implement `HistoryFidelity`**: `downsampled` over seq ranges,
   `validityWidening` = `downsampleInterval`, and `finalBefore` = the instant before which track
   leaves are already rewritten. A component bean replaces `RawHistoryFidelity`.
5. **Prompt 038** can add an `activity_rollup` `TimelineBackend` (declared `@Primary`, delegating
   the retained range to `RawTimelineBackend`) and set `backend` to `rollup` or `mixed`.
6. **Data:** no instructions directed at an agent were found in data, fixtures or tool output. No
   web pages were fetched.
7. Owner decisions carried from 012–036 are still open, among them HIST-3's `410` wording, which
   014 raised.

## Tests written (not run)

Every IT seeds records only through `TestRecords`; alerts, which are not records, go through
`AlertRepository`. The time-travel ITs share one new context, `TimeTravelTestSources`. It adds a
TRACK ALL mover, a private mover, a STANDARD zone source and a settable `StubHistoryFidelity` on
top of `TileTestSources`.

| Test | What it covers |
|---|---|
| `store/history/AsOfResolverTest` | Filter required; future `t` (`ASOF_INVALID`); horizon with the `horizon` property, and the latest horizon over tiers; `S` and per-tier `L` (smallest over TRACK sources, STANDARD 0); approximate under SAMPLED but not ALL; kinds to sources; the private gate with and without a caller; stub fidelity widening (validity and `L`) and the downsampled flag; `settled`; the plan's validity rule; `RawHistoryFidelity` |
| `store/tiles/AsOfTileQueryIT` | The TRACK mover in tile A at T1, in B only at T2, never at its old position; feature properties without `live`; gone after a tombstone and after TTL expiry; the STANDARD polygon present long after (`L = 0`); private source excluded for anonymous callers; aggregated asOf counts. Over HTTP: immutable and byte-identical on repeat with the three headers and a 304; not immutable within 2 min or while pending downsampling, immutable again once downsampled (`approximate: true`); 301 to the canonical form (fraction, offset), keeping the other parameters; future 400; TileJSON template and index with `asOf` |
| `api/support/TileCacheHeadersTest` | The immutable rule at each boundary; the four `Cache-Control` values; public versus private (openReads, private sources, restricted callers); canonical parsing; the redirect `Location` through wrappers (key-in-path, ticket, duplicate `asOf`) and from form parameters |
| `store/track/TrackSimplifierTest` | Douglas–Peucker vectors (offset point, zigzag corners); the time axis; kept points; the `maxPoints` search and both fallbacks; auto tolerance and its clamps; antimeridian split east and west with interpolated time, latitude and values; helpers |
| `store/track/TrackServiceIT` | Segments at a gap and at a tombstone; parallel `times` and `values`; `POSITION_STALE` skipped; the SAMPLED live tail (`live`, `approximate`, never immutable) and a repeated lone point; `truncated`; `TRACK_WINDOW_TOO_LARGE` and the other 400s and 404s; the private gate. Over HTTP: geo+json, immutable for a closed window, `max-age=300` with live, czml 400 |
| `store/frames/HistoryFramesIT` | **Keyframe plus frames 1..i equals `AsOfQueryService` asOf at every frame end**, with and without a bbox, TRACK and STANDARD together; `left`, `tombstone` and `expired` removals in the right frame; item fields (centroid, title, props); `geometry=full`; the step, frame-count, filter, entity and row limits; future `to`; JSON and ndjson over HTTP; a hidden source 404 |
| `store/timeline/TimelineServiceIT` | Buckets on the observed axis with empties; `groupBy` kind and source; `measure=entities`; keyframes excluded unless asked; the alert overlay by severity; `ingested` through `seq_clock`; bbox; auto buckets; `TIMELINE_TOO_LARGE`; HTTP document, 400s and a private source 404 |
| `mcp/McpTimeTravelToolsIT` | `get_state_at` at ISO, `now`, `-PT90M` and `-10m`, with `asOf`, `watermarkSeq`, `approximate` and `horizon`; the limit cap, missing arguments, a future instant, `place` without a token and private sources; `get_track` short form within 4 KB with its summary, long form, the `maxPoints` cap and 404s; both listed for anonymous callers only while `openReads` is on; `McpTimes` |
| `store/history/TimeTravelPlansIT` | No new index on `record_version`; the track, timeline and asOf tile shapes plan onto the existing indexes (plans logged for 041) |

`AccessPolicySyncTest` and `PostmanCollectionContractTest` need no change. The three new operations
are GETs with no request body, and their spec scopes match the new rules.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 0 errors, 71 warnings (unchanged).

No new dependencies.

## Notes for the next prompt

1. **038 (stats, rollups):** implement `TimelineBackend` over `activity_rollup` for ranges before
   `retainedFrom`, make it `@Primary`, and delegate the retained part to `RawTimelineBackend`. The
   `Timeline.backend` member is already in the spec.
2. **039 (private sources, EgressPolicy):** `AsOfResolver.plan` takes the caller's excluded set
   from `ReadCallerResolver.excludedSources`. Fold that into `EgressPolicy` together with the
   other private gates. `TrackResult.publicAllowed` and `TilesController.publicAsOf` already keep
   private sources out of `public` caching.
3. **044 (downsampling):** implement `HistoryFidelity` (see Issue 4). The asOf tiles, tracks,
   frames, timeline and `/v1/records?asOf` pick it up through `AsOfResolver`. Responses over a
   rewritten range become `approximate`, and their caching waits for `finalBefore`.
4. **141 (CZML):** `getRecordTrack` answers `format=czml` with 400 for now. Render
   `TrackResult` (segments with parallel `times`) as CZML there.
5. **041 checkpoint:** the new ITs first run there, in the `TimeTravelTestSources` context. Record
   `TimeTravelPlansIT`'s logged plans, and check Issue 2.
