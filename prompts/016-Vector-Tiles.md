# 016 — Vector Tiles

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/016-CR-Vector-Tiles.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/015-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §8 (`record_live`, current payload), §12 (tiles row), *Per-prompt changes* → the *Vector Tiles* entry
- `docs/Gap-Analysis.md` §3 rows "TileJSON 3.0 + tiles index", "Aggregated tiles", "GeoJSON `title`"; §8.A item 6 ("Extend 013" there; 013 is now this prompt)
- `docs/OpenAPI.json` operation `getTile`
- TileJSON 3.0.0 spec: https://github.com/mapbox/tilejson-spec/tree/master/3.0.0
- PostGIS docs for `ST_AsMVT`, `ST_AsMVTGeom`, `ST_TileEnvelope`, `ST_SquareGrid`, `ST_HexagonGrid` (PostGIS ≥ 3.1). A `jsonb` column passed to `ST_AsMVT` expands into feature properties with native types.
- Completion Reports of 008 (`aggBelowZoom`, `fieldSpecs`, `kindDisplay`, `private`), 009 (`TestRecords`) and 014 (`TitleResolver`, compression config)

## Objective

Serve Mapbox Vector Tiles of **current live state**, so map clients render dense layers (thousands of aircraft, fire pixels) smoothly and see live positions before any new version is persisted. Make the tiles discoverable through TileJSON, and replace the silent per-tile cap at low zooms with server-side grid or hex aggregation.

## Tasks

### 1. Layer resolution (`ai.foresignal.store.tiles.TileLayerResolver`)

- If `layer` equals a registered source id, filter by `source_id = layer`. Else, if `layer` is a kind declared by any registered source, filter by `kind = layer`. Otherwise 404 `UNKNOWN_LAYER`.
- Properties: `source`, `externalId`, `kind`, `title`, `observedAt` (epoch seconds, number), `version`, `live` (bool, `l.dirty`), plus the union of `tileProperties` from the relevant descriptors.
- Validate property names against `^[A-Za-z0-9_]{1,64}$` before interpolating them into SQL. Values are always bound parameters; only validated identifiers are interpolated.
- Resolve `aggBelowZoom` for the layer: the minimum over its sources' non-null values; null when none declares it (never aggregate).
- Exclude `private` sources (008) for anonymous `openReads` callers before any feature or count is computed. Restricted tokens (035) will add filters here; keep the layer filter a single composable SQL fragment.

### 2. Raw tile query (`TileQueryService`)

```sql
WITH bounds AS (SELECT ST_TileEnvelope(:z, :x, :y) AS env),
mvtgeom AS (
  SELECT ST_AsMVTGeom(ST_Transform(l.geom, 3857), bounds.env, 4096, 64, true) AS geom,
         jsonb_build_object('source', h.source_id, 'externalId', h.external_id, 'kind', h.kind,
                            'title', h.title, 'version', h.version, 'live', l.dirty,
                            'observedAt', extract(epoch from l.observed_at)::bigint
                            /* , 'mag', (h.payload || l.kinematic)->'mag', … per tile property */) AS props
  FROM record_live l JOIN record_head h USING (source_id, external_id), bounds
  WHERE <layer filter>
    AND NOT h.tombstone
    AND (l.valid_until IS NULL OR l.valid_until > now())
    AND l.geom && ST_Transform(bounds.env, 4326)
  ORDER BY l.observed_at DESC
  LIMIT :cap + 1
)
SELECT (SELECT ST_AsMVT(t.*, :layerName, 4096, 'geom')
          FROM (SELECT geom, props FROM mvtgeom ORDER BY (props->>'observedAt')::bigint DESC LIMIT :cap) t),
       (SELECT count(*) FROM mvtgeom);
```

Only `geom` and `props` may reach `ST_AsMVT`; any extra column becomes a feature property.

- Geometry and time come from `record_live` (live positions); static properties from `record_head`; tile properties from the merged payload `jsonb_strip_nulls(h.payload || l.kinematic)`. If `record_head.title` does not exist, build `title` with the 014 `TitleResolver` logic in SQL (`COALESCE` over the resolved paths) and record it.
- The `&&` filter uses the GiST index on `record_live.geom` (4326). Do not add indexes on `record_live` other than that one.
- Validate `z` ∈ [0, 16] and `0 ≤ x, y < 2^z`; otherwise 400.
- Cap features at `foresignal.tiles.maxFeatures` (default 50,000). When `count > cap`, send `Foresignal-Tile-Truncated: true`. Above `aggBelowZoom` this is the only remaining cap.

### 3. Aggregated tiles (`agg`)

- Query parameter `agg=auto|none|grid|hex` (default `auto`). `auto` aggregates with `hex` when `z < aggBelowZoom`; `none` forces raw; `grid`/`hex` force aggregation at any zoom.
- Cells are built in EPSG:3857 so they line up across tile edges: `ST_SquareGrid(:size, bounds.env)` or `ST_HexagonGrid(:size, bounds.env)` with `size = (tile width in metres) / foresignal.tiles.cellsPerTile` (default 32). Assign each record to a cell by `ST_Intersects(cell.geom, ST_Transform(ST_PointOnSurface(l.geom), 3857))`, so a polygon counts once.
- Cell properties (flat, no nested objects): `count`, `latestObservedAt` (epoch s), `kinds` (JSON string of kind → count), and `maxRank` only when every source of the layer declares a numeric rank field (008 `kindDisplay` rank hint or `rankField`); omit it otherwise. Emit the cell polygon via `ST_AsMVTGeom`. Layer name stays `:layerName`; add a property `agg: "grid"|"hex"` so styles can switch.
- Apply the same layer, private and expiry filters before counting, so counts never include records the caller cannot see.
- Aggregated responses use `Cache-Control: public, max-age=60` (`foresignal.tiles.aggMaxAge`); raw tiles keep `max-age=5`. Use `private` instead of `public` when `openReads` is off.

### 4. TileJSON

- `GET /v1/tiles.json` (`listTileLayers`): `{tilejson: "3.0.0", layers: [{id, type: "source"|"kind", name, tilejson: "<base>/v1/tiles/<id>.json", aggBelowZoom}]}` over every registered source id and kind with at least one non-private source the caller can see.
- `GET /v1/tiles/{layer}.json` (`getTileJson`): TileJSON 3.0.0 with `tilejson`, `name`, `description`, `version` (app version), `attribution` (distinct descriptor attribution strings joined with " · "; prompt 109 replaces this with aggregated attribution), `scheme: "xyz"`, `tiles: ["<base>/v1/tiles/<layer>/{z}/{x}/{y}.mvt"]`, `minzoom: 0`, `maxzoom: 16`, `bounds: [-180, -85.0511, 180, 85.0511]`, `vector_layers: [{id: <layer>, description, minzoom: 0, maxzoom: 16, fields: {name: description}}]`. Field descriptions come from `fieldSpecs` (label and unit) when declared, else "String" or "Number". Add `"foresignal:aggBelowZoom"` and `"foresignal:properties"` (the property list) as extension keys.
- `<base>` is `server.publicBaseUrl` when set, else the request's scheme, host and port. Do not trust `X-Forwarded-*` here; prompt 023 adds trusted-proxy handling.
- `ETag` on both documents (hash of the body; layers change only with the registry), `Cache-Control: public, max-age=300`.
- Route ordering: `/v1/tiles/{layer}.json` must not collide with `/v1/tiles/{layer}/{z}/{x}/{y}.mvt`; disable suffix pattern matching and bind both explicitly.

### 5. Controller

- `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt` (`getTile`) with the new `agg` parameter. Make sure Spring does not treat `.mvt` as a format suffix and `{y}` binds correctly.
- `200` with `Content-Type: application/vnd.mapbox-vector-tile`, even for an empty tile (empty body allowed). `ETag` from a hash of the bytes; `304` on `If-None-Match`. The 014 compression config already lists the MVT media type.
- CORS read rules apply.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: `getTile` + `agg` (enum `auto|none|grid|hex`) and the `Foresignal-Tile-Truncated` response header; new `listTileLayers` and `getTileJson`, both `"x-foresignal-scope": "read"`, with schemas `TileLayerIndex` and `TileJson`. Document the feature properties (including `title`, `version`, `live`) and the aggregate cell properties in the `getTile` description.
- `AccessPolicy`: add `GET /v1/tiles.json` READ (`/v1/tiles/**` does not match it); `/v1/tiles/*.json` is already covered by `/v1/tiles/**`. Extend `AccessPolicyTest`.
- Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`).
- `docs/Adapter-Guide.md`: when to declare `aggBelowZoom` (dense point layers: aircraft, vessels, fire pixels) and which tile properties a style needs (`track` for rotation, `frp` for fire size).

## Tests to write (do not run)

Seed only through `TestRecords`.

- `TileLayerResolverTest`: source vs kind resolution, union of properties, invalid property names rejected, unknown layer, `aggBelowZoom` minimum and null.
- `TileQueryServiceIT` (decode MVT in tests with a test-scoped decoder such as `no.ecc.vectortile:java-vector-tile`; verify the artifact and version on Maven Central):
  - points and polygons in known tiles; feature count, properties and typed values (`mag` numeric, `title` string, `live` boolean)
  - **a live position appears before any new version:** a LIVE observation moves an entity into the tile with no new version; the tile shows it at the new position with `live: true`
  - merged payload property reflects a kinematic key updated by LIVE
  - expired and tombstoned records excluded
  - the cap with `Foresignal-Tile-Truncated`
  - an empty tile returns 200 with an empty body
- `AggregatedTilesIT`: below `aggBelowZoom` the tile holds cells with correct `count` and `kinds`; a point on a shared tile edge is counted in exactly one of the two neighbouring tiles; `agg=none` forces raw; `agg=grid` at high zoom; private source excluded from counts for anonymous callers.
- `TileJsonIT`: index lists sources and kinds; per-layer document validates against TileJSON 3.0.0 required keys; `tiles` URL uses `publicBaseUrl` when set; field descriptions from `fieldSpecs`; ETag/304.
- `TilesControllerIT`: content type, cache headers (raw vs aggregated), ETag/304, 400 for out-of-range z/x/y and bad `agg`, 404 for unknown layer.

## Acceptance criteria

- `getTile`, `listTileLayers` and `getTileJson` match `docs/OpenAPI.json`.
- Tiles read `record_live l JOIN record_head h` with geometry and `observedAt` from `l`.
- Low zooms on layers with `aggBelowZoom` return aggregate cells, never a silently capped sample.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `016: Vector Tiles`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/016-CR-Vector-Tiles.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `016: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
