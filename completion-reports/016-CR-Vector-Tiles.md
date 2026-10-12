# 016-CR-Vector-Tiles

| | |
|---|---|
| Prompt | 016-Vector-Tiles.md |
| Status | Complete |
| Implementation commit | c7c88fa49cfd087b694467c5a262c51e8d91664d |
| Date | 2026-10-11 |

## Summary

This prompt serves Mapbox Vector Tiles of current live state, with TileJSON discovery and
server-side aggregation at low zooms. The three operations assigned to 016 in `docs/OpenAPI.json`
are implemented and marked `x-foresignal-status: implemented`:

- `getTile`: `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt`
- `listTileLayers`: `GET /v1/tiles.json`
- `getTileJson`: `GET /v1/tiles/{layer}.json`

**Layer resolution (`ai.foresignal.store.tiles.TileLayerResolver`).**

- A registered source id wins. Otherwise the name must be a kind that a registered source declares.
  Anything else is `404 UNKNOWN_LAYER` (new `ErrorCodes.UNKNOWN_LAYER`).
- Excluded sources are removed before anything else is computed: private sources, for anonymous
  `openReads` callers, via 014's `ReadCallerResolver.excludedSources`. A private source layer, or a
  kind whose only sources are private, is therefore `404` for anonymous callers.
- `TileLayer` holds:
  - the visible sources, sorted by id;
  - the union of their `tileProperties`, in first-seen order. Names must match
    `^[A-Za-z0-9_]{1,64}$`; an invalid name is dropped with a WARN, and the base property names are
    not duplicated;
  - `aggBelowZoom`: the minimum of the non-null values, or null;
  - `rankFields`: filled only when every source declares a numeric `rankField`;
  - the attribution.
- The layer filter is one composable SQL fragment (`TileLayer.filterSql`). A source layer is
  `h.source_id = :id`; a kind layer is `h.kind = :kind AND h.source_id = ANY(:visibleSources)`.
  All values are bound; only validated property names are interpolated.

**Raw tiles (`TileQueryService.raw`).** The query follows the prompt's SQL:

- `record_head h JOIN record_live l`; geometry and `observedAt` come from `l`.
- Static properties come from `h`. Tile properties come from the merged payload
  `jsonb_strip_nulls(h.payload || l.kinematic)`, through a `LATERAL`.
- Rows are selected with `l.geom && ST_Transform(env, 4326)`, which uses the existing GiST index. No
  new index was added.
- Filters: `NOT h.tombstone`, plus expiry against the application clock.
- Order: `l.observed_at DESC`. `LIMIT cap+1` detects truncation.
- `ST_AsMVT` receives only the `geom` and `props` columns. `props` is `jsonb`, so typed values
  survive (`mag` numeric, `title` string, `live` boolean).
- The feature properties are `source`, `externalId`, `kind`, `title`, `observedAt` (floor of epoch
  seconds), `version`, `live` (`l.dirty`) and the tile properties. Null values are stripped. Base
  properties win over a tile property with the same name.
- `jsonb_build_object` calls are chunked to 40 pairs, because PostgreSQL functions take at most 100
  arguments.
- `record_head.title` exists, so no SQL `COALESCE` title was needed.

**Aggregated tiles (`TileQueryService.aggregated`).**

- `agg=auto|none|grid|hex` (`AggMode`). `auto` uses hex when `z < aggBelowZoom`.
- The same layer, private and expiry filters apply. Each record is reduced to
  `ST_PointOnSurface(l.geom)` in EPSG:3857, so a polygon counts once.
- Each record counts in exactly one tile, the one whose half-open envelope `[xmin,xmax) × [ymin,ymax)`
  holds its point, and in exactly one cell:
  - **grid**: cell `(floor(x/size), floor(y/size))`, clamped to the tile's own range;
  - **hex**: an `ST_HexagonGrid(size, env)` cell it intersects, the lowest `(i, j)` on a shared
    boundary.
- `size` = tile width / `cellsPerTile`.
- Cell properties: `count`, `latestObservedAt`, `kinds` (a JSON string), `agg`, and `maxRank` only
  for rankable layers.
- Features are emitted in a fixed `(i, j)` order, so the bytes and the ETag stay stable.

**TileJSON (`api/mapper/TileJsonMapper`).**

- The index is `{tilejson: "3.0.0", layers: [{id, type, name, tilejson, aggBelowZoom}]}`.
- The per-layer TileJSON 3.0.0 document has every key the prompt lists, including:
  - `attribution`, joined with " · ";
  - `vector_layers[0].fields`, from `fieldSpecs` as "label (unit)", else
    "Number"/"Boolean"/"String";
  - the extension keys `foresignal:aggBelowZoom` (explicit null) and `foresignal:properties`.
- `<base>` is `server.publicBaseUrl` when set, else the request's scheme, host and port (default
  ports omitted, IPv6 bracketed). `X-Forwarded-*` headers are ignored.

**Controller (`api/TilesController`).** The three routes are bound explicitly.

- Tiles:
  - `200 application/vnd.mapbox-vector-tile`, including for an empty body;
  - a strong `ETag` from a hash of the bytes (new `ETags.forBytes`), and `304`;
  - `Cache-Control: public, max-age=5` raw, `max-age=60` aggregated, `private` when `openReads` is
    off;
  - `Vary: Accept-Encoding, Authorization`;
  - `Foresignal-Tile-Truncated: true` when the cap was hit;
  - `400 VALIDATION_FAILED` for z outside 0–16, x or y outside 0..2^z−1, malformed numbers, or a
    bad `agg`.
- TileJSON responses: a strong ETag hashed from the body, `304`, and
  `Cache-Control: public, max-age=300`.

**Other changes.**

- `TileProperties` (`foresignal.tiles.*`): `maxFeatures` 50,000, `cellsPerTile` 32 (must be even,
  2–256), `aggMaxAge` 60 s, `rawMaxAge` 5 s. The defaults are also in `application.yml`.
- `settings/ServerSettings` (`publicBaseUrl` only) and `SettingKeys.SERVER = "server"`, added to
  `SettingKeys.ALL`.
- `AccessPolicy`: `GET /v1/tiles.json → READ`.
- `docs/Adapter-Guide.md` §11: when to declare `aggBelowZoom`, and which tile properties a style
  needs (`track`, `frp`, `mag`).
- The spec reconciliations listed in Deviation 6. Postman was regenerated.

## Files created / modified

**Created (main)**

- `store/tiles/`:
  - `package-info`, `TileProperties`, `TileLayerType`, `TileShape`, `AggMode`, `TileCoordinates`
  - `TileLayer`, `TileLayerResolver`, `TileResult`, `TileQueryService`
- `api/TilesController.java`, `api/mapper/TileJsonMapper.java`
- `settings/ServerSettings.java`

**Modified (main)**

- `api/error/ErrorCodes.java` (`UNKNOWN_LAYER`)
- `api/support/ETags.java` (`forBytes`)
- `security/AccessPolicy.java`
- `settings/SettingKeys.java`
- `src/main/resources/application.yml` (`foresignal.tiles.*`)

**Tests**

- New unit tests: `store/tiles/TileLayerResolverTest`, `store/tiles/TileCoordinatesTest`.
- New integration tests: `store/tiles/TileQueryServiceIT`, `store/tiles/AggregatedTilesIT`,
  `api/TileJsonIT`, `api/TilesControllerIT`.
- New test support: `testsupport/tiles/MvtDecoder` (a test-only MVT protobuf decoder), `TileMath`
  and `TileTestSources`.
- Modified: `security/AccessPolicyTest`.

**Docs and tooling**

- `docs/OpenAPI.json`, `docs/Adapter-Guide.md`
- `postman/Foresignal.postman_collection.json` (regenerated; three descriptions changed)

No new dependency. No Flyway, Liquibase or SQL files. No new index. No `.gitlab-ci.yml` changes.
Nothing was ported from God's Eye View.

## Deviations from the prompt

1. **No MVT decoder dependency.** The suggested `no.ecc.vectortile:java-vector-tile` is not on Maven
   Central: `https://repo1.maven.org/maven2/no/ecc/vectortile/` returns 404, and
   `search.maven.org` finds 0 artifacts for `g:no.ecc.vectortile`. I wrote a small test-scoped
   protobuf decoder instead (`testsupport/tiles/MvtDecoder`, MVT 2.1 wire format, all `Value`
   variants, geometry commands). This avoids adding `protobuf-java` and `jts` versions to the
   build. `io.github.sebasbaumh:mapbox-vector-tile-java:25.1.0` (Apache-2.0) exists on Central and
   could replace it.
2. **Each record counts in exactly one tile.** The prompt's `&&` filter plus `ST_Intersects` would
   count a point on a shared tile edge, or on a cell boundary, twice. The test the prompt asks for
   forbids that. I added:
   - half-open tile membership in EPSG:3857;
   - for hex, `DISTINCT ON` the record, which keeps the lowest `(i, j)` cell.
   One consequence: a hex cell that straddles a tile edge is drawn by both tiles, and each counts
   the records on its own side. Grid cells always line up with tile edges, so they never straddle
   one. See Issue 1.
3. **Grid cells are computed arithmetically, not with `ST_SquareGrid`.** Each cell is
   `(floor(x/size), floor(y/size))`, which is the same origin-(0,0) tiling as `ST_SquareGrid`, and
   its polygon is built with `ST_MakeEnvelope`. That makes the grouping a hash aggregate instead of
   an n × 1,024 spatial join. The index is clamped to the tile's own range, so a floating-point
   rounding error at the edge cannot move a point into a neighboring tile's cell. `cellsPerTile`
   must be even so the origin is a cell edge at every zoom; zoom 0 needs this. Hex still uses
   `ST_HexagonGrid`.
4. **Two small SQL changes:**
   - **Expiry uses the application `Clock`**, as 014's read paths do (014 Deviation 1). The prompt
     wrote `now()`.
   - **`observedAt` is `floor(extract(epoch …))::bigint`.** A plain `::bigint` cast on `numeric`
     rounds, so it could round half a second up.
5. **SQL hardening beyond the prompt:**
   - Before `ST_Transform(…, 3857)`, geometry is clipped with
     `ST_ClipByBox2D(l.geom, ±180, ±85.06)`, so a polar vertex cannot make the transform fail.
   - Raw rows are ordered by `observed_at DESC, source_id, external_id`, and cells by `(i, j)`, so
     unchanged data gives identical bytes and an identical ETag.
   - Rows whose `ST_AsMVTGeom` result is null are left out before `ST_AsMVT`.
6. **Spec reconciliations**, all recorded in `info.x-foresignal-changelog`. No parameter or schema
   changed.
   - `getTile`:
     - its `Cache-Control` description now says `max-age=5` raw and `max-age=60` aggregated (it
       said `max-age=300`, beside 037's immutable form);
     - `ETag` is described as a hash of the bytes;
     - `Vary` is documented;
     - the description documents the feature and cell properties, half-open assignment and hex
       seams.
   - `listTileLayers` and `getTileJson`: the descriptions match what is built.
7. **Index visibility.** The spec says layers with "at least one non-private source the caller can
   see". I read that as sources visible to this caller: anonymous callers never see private
   sources, and token callers see them, which matches what `getTile` serves them. A kind spelled
   like a source id is left out of the index, because that name resolves to the source.
8. **`server.publicBaseUrl` is read before prompt 023 builds the server settings.** A minimal
   `ServerSettings(publicBaseUrl)` record is stored under the new key `server`, with
   `@JsonIgnoreProperties(ignoreUnknown = true)`. Prompt 023 adds the other fields and the
   endpoints. Adding the key to `SettingKeys.ALL` changes what `invalidateCache()` announces.
   `AppSettingServiceIT` compares against `SettingKeys.ALL` itself, so it adapts.
9. **The cap is tested at service level.** `TileQueryService.raw(layer, tile, cap)` takes the cap,
   so `TileQueryServiceIT` tests truncation with a cap of 3 without a second Spring context.
   `TilesControllerIT` asserts that the header is absent when the cap is not hit.
10. **Rank hint.** `KindDisplay` has no rank field, so "numeric rank field" means `rankField`. It
    counts only when the name is valid and `fieldSpecs` does not declare it as something other than
    NUMBER or INTEGER. In SQL, a rank value is used only when it is a JSON number.
11. **Suffix matching.** Spring's `PathPatternParser` never does suffix-pattern matching, so nothing
    had to be disabled. All three routes are bound explicitly, and the `{y}.mvt` / `{layer}.json`
    patterns capture correctly. The controller Javadoc records this.
12. **`pending-operations.txt` does not exist yet** (prompt 020 creates it), so there were no lines
    to delete.

## Issues found / open questions

1. **Hex seams.** With the half-open rule, a hex cell that straddles a tile edge shows two partial
   counts, one per tile half. A choropleth can therefore change shade at a tile seam. Grid cells
   never straddle. The alternative is to count every cell intersecting the tile over all of its
   records, querying the tile plus a one-cell margin. That would make both halves agree, but it
   breaks "counted in exactly one tile". **Owner:** keep it as built, or prefer seamless hex counts?
2. **The descriptor warning about tile properties is now stale.** `DescriptorValidator` warns that a
   non-significant `tileProperty` "can show a value older than the latest observation".
   Core-Data-Model §3 asks for that warning. Tiles now read the merged payload, live kinematic keys
   included, so the warning no longer holds for kinematic keys. I left the validator unchanged.
   **Owner:** should the warning (and Core-Data-Model §3's line) be dropped for `tileProperties`?
3. **The PostGIS behavior relied on is unverified until the checkpoint runs:**
   - `ST_AsMVT` over zero rows returning an empty or null `bytea` (both are handled);
   - the edge points the tests use: x = 0 at the z1 meridian and y = 0 at the equator, which
     should transform exactly;
   - `ST_HexagonGrid` covering points on the envelope boundary.
   Checkpoint 022 runs these tests.
4. **CORS:** tiles and TileJSON are GETs, so 015's open CORS issue (PATCH) does not affect them.
5. The owner decisions carried from 012–015 are still open.

No instructions directed at an agent were found in data, fixtures, web pages or tool output.

## Tests written (not run)

As a feature prompt requires, no tests were run. The integration tests seed only through
`TestRecords`. All four tile ITs share one new context:
`@IntegrationTest @AutoConfigureMockMvc @Import(TileTestSources.class)`.

- **`TileLayerResolverTest`**:
  - source vs kind resolution, and a source id winning over a same-named kind;
  - the filter SQL and its bound values;
  - the property union and order;
  - invalid names rejected (hyphen, quote, dot, 65 characters, blank, null) and base names not
    duplicated;
  - unknown layer and `UNKNOWN_LAYER`;
  - the `aggBelowZoom` minimum and null;
  - private exclusion (source layer, kind with only private sources, properties);
  - rank fields: numeric, STRING, INTEGER, invalid;
  - attribution;
  - the index order and the source-id/kind clash;
  - the SQL builders refusing an unvalidated name.
- **`TileCoordinatesTest`**:
  - parsing with field-named 400s;
  - width, cell size, and the first column and row at z0 and z1 and for neighbors;
  - `AggMode` parsing and shapes;
  - `TileProperties` defaults and validation;
  - `TileResult` defaults.
- **`TileQueryServiceIT`**:
  - a point and a polygon in a known z10 tile, with position checked to ±2 tile units;
  - typed values (`mag` numeric, `title` string, `live` false, `version`, `observedAt`);
  - `depth` and `bad-name` absent;
  - **a LIVE observation moving an entity into the tile with no new version** (the outcome shows
    `live = 1`, `persisted = 0`, head version 1): the feature appears at the new position with
    `live: true`, and the merged payload's `track` reflects the LIVE update;
  - expiry by clock;
  - tombstones;
  - the cap (3 of 5, the newest, truncated), with an exact cap not truncated;
  - an empty tile with an empty body;
  - a kind layer over two sources.
- **`AggregatedTilesIT`**:
  - hex below `aggBelowZoom`: one polygon cell with `count` 3, `kinds {tile-aircraft: 3}`,
    `agg: hex`, `latestObservedAt`, and no `maxRank`;
  - raw at `aggBelowZoom`, and `agg=none` forcing raw;
  - `agg=grid` at z9 on a non-aggregating layer: a polygon counts once, `maxRank` 5.5;
  - a point on the z1 meridian edge counted once across the two tiles, for grid and hex, and a point
    on the equator edge counted once for grid;
  - private sources: anonymous count 3 versus token count 5, no `maxRank` on the mixed kind layer,
    `maxRank` 20 on the rankable source layer;
  - through the API: the anonymous count and `public, max-age=60`.
- **`TileJsonIT`**:
  - the index: types, TileJSON URLs, `aggBelowZoom` (6, 5, explicit null), private sources only
    with a token, cache headers;
  - the per-layer document: the TileJSON 3.0.0 required keys and every listed key, `fields` from
    `fieldSpecs` ("Magnitude (1)", "String", "Number", "Boolean"), the extension keys and the
    property list;
  - the kind document: union, attribution, `owner` only with a token, `aggBelowZoom` 5 vs 3;
  - `publicBaseUrl` with a trailing slash, in both documents;
  - the request base (https, port 8443), with `X-Forwarded-Host` ignored;
  - ETag and 304 on both documents;
  - 404 `UNKNOWN_LAYER`, including a private source asked anonymously.
- **`TilesControllerIT`**:
  - the content type, `public, max-age=5`, `Vary`, the `"b-` ETag, the truncation header absent,
    and `{y}` binding;
  - the aggregated `max-age=60` for auto and grid, and raw for `agg=none`;
  - `private, max-age=5` with `openReads` off and a token (401 without one);
  - ETag and 304, and the tag changing after a write;
  - an empty tile is 200 with an empty body;
  - six 400 cases and a 404;
  - the index open;
  - request-base formatting.
- **`AccessPolicyTest`**: `GET /v1/tiles.json` and `GET /v1/tiles/{layer}.json` are READ;
  `POST /v1/tiles.json` and `/v1/tiles.jsonx` fail closed.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 1s
4 actionable tasks: 3 executed, 1 up-to-date
```

With `-q`, the only other output is the javac note that some test sources use unchecked operations.

## Notes for the next prompt

1. **017 onward:** nothing here changes alerts or the SSE hub.
2. **020 (pending operations):** `getTile`, `listTileLayers` and `getTileJson` are implemented; do
   not list them.
3. **022 (checkpoint):**
   - The tile ITs add one cached context (`TileTestSources`).
   - Check Issue 3 when the tests run.
   - The performance smoke can time `TileQueryService` on a dense layer at low zoom, both raw at the
     cap and hex.
4. **023 (server settings, tickets, trusted proxies):**
   - Extend `settings/ServerSettings` (key `server`) rather than creating a second record.
   - `TilesController.baseUrl` is where trusted `X-Forwarded-*` handling belongs.
   - Tickets on tile URLs: `getTile` already ignores unknown query parameters.
5. **035 (restricted tokens):** AND the restriction fragment next to `TileLayer.filterSql` in
   `TileQueryService.commonFilter`.
6. **036 / 037 / 109:**
   - `getTile` declares `place`, `radiusKm`, `view`, `inWatchlist` (036), `asOf` (037) and
     `licencePolicy` (109), which are not read yet.
   - 037 adds the immutable `Cache-Control` for closed instants.
   - 109 replaces `TileLayer.attribution()` with aggregated attribution.
7. **039 (EgressPolicy):** private exclusion for tiles comes from `ReadCallerResolver.excludedSources`
   through `TileLayerResolver.resolve(layer, excluded)`. Move it into `EgressPolicy`.
8. **050 (tile keys, styles):** `/v1/k/{key}/tiles/…` can reuse `TilesController` logic. The styles
   endpoint can read `TileLayer.aggBelowZoom()` and `properties()`.
9. **Owner decisions pending:** Issues 1 and 2, plus those carried from 012–015.
