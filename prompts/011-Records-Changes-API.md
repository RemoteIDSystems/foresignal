# 011 — Records Changes API

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/011-CR-Records-Changes-API.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/010-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/OpenAPI.json`: operations tagged `Records` and `Changes` (except `/v1/changes/stream`, which is prompt 012), plus schemas `Record`, `RecordPage`, `Feature`, `FeatureCollection`, `Change`, `ChangePage`, `KindSummary`, `Geometry`
- `docs/Architecture.md` §9 (query paths), §10 (pagination, GeoJSON)

## Objective

Serve stored data to frontends: current state, version history, playback ranges and the ordered change feed, as JSON or GeoJSON, with efficient keyset pagination.

## Tasks

### 1. Query layer (`ai.elaro.store.query`)

Write native SQL through `JdbcTemplate` / `NamedParameterJdbcTemplate`; these queries are performance-sensitive. Return geometry as `ST_AsGeoJSON(geom, 7)::text`, embedded as raw JSON (no JTS round trip).

- **`RecordQueryService.current(RecordFilter f, Cursor c, int limit)`:**
  - `FROM record_head h JOIN record_version v ON v.seq = h.seq`
  - filters: `h.source_id = ANY(:sources)`, `h.kind = ANY(:kinds)`, `h.geom && ST_MakeEnvelope(:minLon,:minLat,:maxLon,:maxLat,4326)` (plus `ST_Intersects` for accuracy), `h.observed_at >= :since`, `< :until`, `NOT h.tombstone`
  - expiry: `(h.valid_until IS NULL OR h.valid_until > now())` unless `includeExpired`
  - text search: `h.search_text ILIKE '%' || :q || '%'`, with `%`/`_` in `q` escaped
  - order by `h.observed_at DESC, h.seq DESC`; keyset cursor on `(observed_at, seq)`
  - fetch `limit + 1` rows to compute `nextCursor`
- **`version(source, externalId)`** returns the current record; **`versions(source, externalId)`** returns all versions ordered by version ascending.
- **`history(filter with required since, optional until, cursor, limit ≤ 50000)`:** over `record_version` ordered `observed_at ASC, seq ASC`.
- **`changes(after, filter, includePayload, limit ≤ 10000)`:** `record_version WHERE seq > :after ORDER BY seq ASC`. Returns `lastSeq` (the last row's seq, or `after` when empty) and `hasMore`.
- **`kinds()`:** `SELECT kind, count(*), array_agg(DISTINCT source_id) FROM record_head WHERE NOT tombstone AND (valid_until IS NULL OR valid_until > now()) GROUP BY kind`.
- Add a supporting index `record_head (observed_at DESC, seq DESC)` via JPA `@Index` if useful, and document it.

### 2. Parameters and validation (`ai.elaro.api.support`)

- `BboxParser`: `minLon,minLat,maxLon,maxLat`, with lon in [-180, 180], lat in [-90, 90], min < max. **Antimeridian-crossing boxes are rejected** in v1 (400 with an explanatory message).
- Multi-value `source` / `kind` accept both repeated params and comma-separated values.
- `CursorCodec`: an opaque base64url JSON (`{"o":"<observedAt>","s":<seq>}` or `{"i":<id>}`) with a version prefix. A tampered or invalid cursor gives 400 `INVALID_CURSOR`.
- `limit`: bounds per the spec; out of range gives 400.

### 3. Output formats

- JSON: the `Record` DTO. `id` = `"<source>:<externalId>"`. `attribution` comes from the descriptor.
- **GeoJSON:** chosen by `?format=geojson` **or** `Accept: application/geo+json`; `format` takes precedence.
  - Register the media type `application/geo+json`.
  - `Feature.properties` = `{source, externalId, kind, version, seq, observedAt, validUntil, payload}`.
  - `FeatureCollection` includes `nextCursor` and `attributions` (the distinct attribution strings of the sources present).
- Records without geometry appear in JSON responses. In GeoJSON they are included with `geometry: null`, as RFC 7946 allows.

### 4. Controllers

`RecordsController` (`listRecords`, `getRecord`, `listRecordVersions`, `queryHistory`, `listKinds`) and `ChangesController` (`listChanges`), matching the spec exactly. An unknown source or record gives 404.

## Tests to write (do not run)

Seed data through `IngestWriter` or direct `JdbcTemplate` inserts in test setup.

- `RecordQueryServiceIT`:
  - each filter alone and combined
  - bbox intersects (points and polygons)
  - expiry excluded/included
  - tombstones excluded
  - text search escaping
  - keyset pagination across pages with equal timestamps, with no duplicates or gaps
  - history ordering; changes `after` semantics and `hasMore`
  - kinds aggregation
- `RecordsControllerIT` / `ChangesControllerIT`: JSON and GeoJSON via both negotiation methods; spec field names; 400s (bad bbox, antimeridian, bad cursor, limit); 404s.
- `CursorCodecTest`, `BboxParserTest`.

## Acceptance criteria

- All `Records` operations and `listChanges` match `docs/OpenAPI.json`.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `011: Records Changes API`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/011-CR-Records-Changes-API.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `011: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
