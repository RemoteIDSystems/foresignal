# 013 — Vector Tiles

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/013-CR-Vector-Tiles.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/012-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §9 (tiles row)
- `docs/OpenAPI.json` operation `getTile`
- PostGIS docs for `ST_AsMVT`, `ST_AsMVTGeom` and `ST_TileEnvelope` (PostGIS ≥ 3.0). Note: a `jsonb` column passed to `ST_AsMVT` is expanded into feature properties with native types.

## Objective

Serve Mapbox Vector Tiles of current records, so map frontends can render dense layers (thousands of aircraft, fire pixels) smoothly.

## Tasks

### 1. Layer resolution (`ai.elaro.store.tiles.TileLayerResolver`)

- If `layer` equals a registered source id, filter by `source_id = layer`. Else, if `layer` is a kind declared by any registered source, filter by `kind = layer`. Otherwise return 404 `UNKNOWN_LAYER`.
- Properties for the layer: `source`, `externalId`, `kind`, `observedAt` (epoch seconds, number), plus the union of `tileProperties` from the relevant source descriptors.
- Validate property names against `^[A-Za-z0-9_]{1,64}$` before they are interpolated into SQL. Pass values as parameters; only validated identifiers are ever interpolated.

### 2. Tile query (`TileQueryService`)

```sql
WITH bounds AS (SELECT ST_TileEnvelope(:z, :x, :y) AS env),
mvtgeom AS (
  SELECT ST_AsMVTGeom(ST_Transform(h.geom, 3857), bounds.env, 4096, 64, true) AS geom,
         jsonb_build_object('source', h.source_id, 'externalId', h.external_id, 'kind', h.kind,
                            'observedAt', extract(epoch from h.observed_at)::bigint
                            /* , 'mag', v.payload->'mag', … for each tile property */) AS props
  FROM record_head h JOIN record_version v ON v.seq = h.seq, bounds
  WHERE <layer filter>
    AND NOT h.tombstone
    AND (h.valid_until IS NULL OR h.valid_until > now())
    AND h.geom && ST_Transform(bounds.env, 4326)
)
SELECT ST_AsMVT(mvtgeom.*, :layerName, 4096, 'geom') FROM mvtgeom;
```

- Validate `z` ∈ [0, 16] and `0 ≤ x, y < 2^z`; otherwise 400.
- Cap features per tile at 50,000, by `observed_at DESC` in a subquery with `LIMIT`. Document that clients should use higher zooms for dense layers.
- Consider a functional index if needed; record what you chose. The existing GiST index on `record_head.geom` (4326) supports the `&&` filter against the transformed envelope.

### 3. Controller

- `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt` (operationId `getTile`). Make sure Spring does not treat `.mvt` as a format suffix, and that `{y}` binds correctly.
- Return `200` with `Content-Type: application/vnd.mapbox-vector-tile`, even for an empty tile (empty body allowed).
- Headers: `Cache-Control: public, max-age=5`, plus an `ETag` based on a hash of the bytes; return `304` on `If-None-Match`.
- Open (no auth); CORS read rules apply.

## Tests to write (do not run)

- `TileLayerResolverTest`: source vs kind resolution, union of properties, rejection of invalid property names, unknown layer.
- `TileQueryServiceIT`:
  - seed points and polygons in known tiles
  - decode the returned MVT in the test to assert feature count, properties and typed values (`mag` numeric). Add a test-scoped MVT decoder dependency (e.g. `no.ecc.vectortile:java-vector-tile`) or decode minimally with protobuf; verify the dependency on Maven Central.
  - expired and tombstoned records are excluded
  - the per-tile cap
  - an empty tile returns 200 with an empty body
- `TilesControllerIT`: content type, cache headers, ETag/304, 400 for out-of-range z/x/y, 404 for an unknown layer.

## Acceptance criteria

- `getTile` matches `docs/OpenAPI.json`.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 040.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `013: Vector Tiles`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/013-CR-Vector-Tiles.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `013: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
