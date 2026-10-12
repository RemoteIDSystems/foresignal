# 014-CR-Records-Changes-API

| | |
|---|---|
| Prompt | 014-Records-Changes-API.md |
| Status | Complete |
| Implementation commit | ec1a43c1113af860155c1928e9208cec7f9413d7 |
| Date | 2026-10-11 |

## Summary

This prompt serves stored data to clients. The seven operations assigned to 014 in
`docs/OpenAPI.json` are implemented and marked `x-foresignal-status: implemented`:

- `listRecords`, `getRecord`, `listRecordVersions`, `queryHistory`
- `listKinds`, `getKindSchema`
- `listChanges`

`streamChanges` (`/v1/changes/stream`) stays with prompt 015.

**Shared parameters (`ai.foresignal.api.support`).**

- `BboxParser` returns a `BboxFilter(List<Envelope>)`. A box with `minLon > maxLon` crosses the
  antimeridian and is split into `[minLon, 180]` and `[-180, maxLon]`; the SQL ORs the two. The
  v1.1 rejection is gone.
- `NearParser` returns a `NearFilter` (`near=lon,lat` plus `radiusKm`, 0 < r ≤ 2000, both required
  together). `prefilter()` builds a conservative degree envelope: the longitude span uses
  `cos(lat)` clamped to 0.01, the box is split at the antimeridian, and near a pole it covers every
  longitude. The SQL then applies `ST_DWithin(geom::geography, point::geography, radiusM)`.
- `QueryParams` parses every scalar and multi-value parameter, so each malformed value is a
  `400 VALIDATION_FAILED` with `errors[].field`. `source`, `kind` and `externalId` accept repeated
  and comma-separated values; the limit is 100 values.
- `CursorCodec` and `Cursor` produce opaque `c1.<base64url JSON>.<check>` cursors. The variants
  are observed `{o,s,e}`, id `{s,e}`, distance `{d,s,e}`, seq `{q}` and history `{o,q}`, each with
  an optional pinned `w`. Every cursor also carries a query fingerprint. A tampered, truncated or
  malformed cursor, or one from another variant or query, is `400 INVALID_CURSOR`.
- `FieldSelector` and `FieldSelection` parse the projection. Payload paths are projected in SQL
  (`jsonb_build_object` of `#>` lookups, keyed by dot path; the mapper nests them back).
  Top-level fields are projected in the mapper. Unknown top-level names are a 400.
- `GeometryOptions` handles `precision` 0–7 (`ST_AsGeoJSON(geom, p)` / `ST_AsText(geom, p)`) and
  `simplify` 0–100,000 m. Simplification runs in EPSG:3857 with the tolerance divided by
  `cos(centroid latitude)`. Points and multipoints pass through. Topology is preserved per feature
  only, which is documented.
- `ResponseFormat` chooses json, geojson, ndjson, geojsonseq or csv. `?format=` wins over `Accept`.
- `ETags` provides strong tags, `If-None-Match` (weak comparison, per RFC 9110), `304`,
  `Vary: Accept, Accept-Encoding, Authorization`, and `Cache-Control: private, no-cache`
  (`public, no-cache` while `openReads` is on).
- `StreamWriter` writes ndjson, geojsonseq (RFC 8142 `0x1E` separators) and csv (Jackson 3
  `tools.jackson.dataformat:jackson-dataformat-csv`).
- `ReadCaller` and `ReadCallerResolver` give the caller's scope and the `security` setting, plus
  the sources an anonymous caller may not see.
- `ApiProperties` (`foresignal.api.*`) holds `query.statementTimeout` (30 s),
  `changes.inlinePayloadKinds` (`[raster-frame]`) and `stream.maxRows` (500,000).

**Queries (`ai.foresignal.store.query`).**

- `ReadQueryRunner` runs every read query in one read-only transaction. Each transaction starts
  with `SET LOCAL statement_timeout`. SQLState `57014` becomes `503 QUERY_TIMEOUT`. Streaming
  queries use a template with fetch size 1,000.
- `SeqClockQuery` implements `cutoffSeq(t)`, `S(T)` and `lastSeqBefore(t) = S(t − 1 µs)`.
- `RecordQueryService` covers:
  - current listings, streamed listings and `get`
  - `kinds`
  - versions, history and streamed history
  - changes and streamed changes, plus `purgeHorizon`

  Current reads use only `record_head h JOIN record_live l` (I5). The payload is
  `jsonb_strip_nulls(h.payload || l.kinematic)` and the geometry is `l.geom`. The filters are
  source, kind, externalId, bbox, near, `NOT h.tombstone`, expiry, text, `timeField`, the snapshot
  pin and the keyset. The orders are `observed` (DESC keyset), `id` (ASC keyset, which pins the
  watermark) and `distance`. The watermark is read from `ChangeBus.lastPublishedSeq()` before the
  query. `include=observer` probes the head's version with `seq = h.seq`.
- `AsOfQueryService` implements Core-Data-Model §12 step by step:
  - a filter is required
  - the horizon is `now − window + min(keyframe, window/4) + 1 h`
  - `T` must not be in the future, and the forbidden combinations are refused
  - `S = S(T)`
  - one `DISTINCT ON` query runs per tier, with `L = S(T − ttl − 2 min)` for TRACK and `L = 0` for
    STANDARD, keeping only valid, non-tombstoned rows; bbox, near and kind apply to the chosen
    version
  - the keyset is `(source_id, external_id)`, and the two tiers are merged in Java
  - the response carries `approximate` when a SAMPLED or LATEST source is involved
- Results use `RecordRow` and `RecordRowMapper`. Every query selects one fixed label set, with
  typed `NULL`s for the labels it does not have.

**Retention state (`ai.foresignal.retention`).** `RetentionState` has `purgeSeq`,
`lowestRetainedSeq` and `window`. `DefaultRetentionState` returns 0 for both seqs, and its window is
the one `SourceIngestProfiles` resolves: `trackWindow` for TRACK, else the effective retention,
else the global default. `purgeHorizon` is the largest `purgeSeq` over the filtered sources, or
over all sources when unfiltered. `after < purgeHorizon` returns `410 RESYNC_REQUIRED` with the
`purgeHorizon` and `watermarkSeq` extensions.

**Output.** `RecordMapper` produces:

- `Record`, with `ingestedAt` only for versions, history and asOf
- `RecordVersion`, with `persistReason`, `prevSeq`, `historyTier`, `ingestedAt`, `receivedAt`,
  `batchId`, `downsampled=false` and gated `observer`
- `Change`, with `prevSeq`, `historyTier`, `persistReason`, `observedAt`, `eventTime`,
  `validUntil`, `validFrom`, `qaFlags` and `bbox`
- GeoJSON `Feature` and CSV rows

`title` is `record_head.title` for current rows. For versions it is resolved from the version
payload with the shared `TitleResolver` and the source's `titleField`. Payload, geometry, quality
and observers are written as raw PostgreSQL JSON text (`RawJson`). `KindSchemaBuilder` merges
`fieldSpecs` into JSON Schema 2020-12. Conflicting types become `oneOf`, each variant lists its
`x-sources`, and the conflicting sources go into `x-conflicts`. `x-display` comes from
`kindDisplay`.

**Controllers.** `RecordsController` and `ChangesController`. Streaming formats are written
synchronously to the response, with the `Foresignal-Watermark-Seq` header, after a
`count(*) … LIMIT cap+1` pre-check (`400 RESULT_TOO_LARGE`; nothing is ever truncated silently).
An unknown source, whether in a path or in a filter, is 404. An unknown record is 404.

**Other changes.**

- `SecuritySettings.exposeObserver` (default false). Values stored without it read as false.
- `LiveBus.lastPublishedBatchNo()`.
- `ForesignalException.withProperty(...)`; both exception handlers copy the extension members.
- New error codes `INVALID_CURSOR`, `ASOF_FILTER_REQUIRED`, `ASOF_BEYOND_RETENTION`, `ASOF_INVALID`,
  `RESYNC_REQUIRED`, `RESULT_TOO_LARGE` and `QUERY_TIMEOUT`.
- `AccessPolicy`: `GET /v1/kinds/*/schema → READ`.
- `server.compression` (`FORESIGNAL_COMPRESSION`, default true) with the six listed mime types.
  `text/event-stream` is not among them.
- `docs/Agent-Guide.md` (new): "Reading records", "Keeping a mirror", "Replaying the past (asOf)"
  and "Home Assistant" (the `geo_json_events` URL; works only with `openReads`).

**No index on `record_live.observed_at` was added.** `CurrentReadsUseHeadLiveIT` asserts that none
exists.

## Files created / modified

**Created (main)**
- `api/RecordsController.java`, `api/ChangesController.java`
- `api/dto/RecordPage.java`, `RecordVersionPage.java`, `FeatureCollectionPage.java`,
  `ChangePage.java`, `KindSummaryDto.java`
- `api/mapper/RecordMapper.java`, `KindSchemaBuilder.java`
- `api/support/`: `package-info`, `ApiProperties`, `BboxFilter`, `BboxParser`, `NearFilter`,
  `NearParser`, `QueryParams`, `TimeField`, `Cursor`, `CursorCodec`, `FieldSelection`,
  `FieldSelector`, `GeometryOptions`, `ResponseFormat`, `RawJson`, `ReadCaller`,
  `ReadCallerResolver`, `ETags`, `StreamWriter`
- `store/query/`: `ReadQueryRunner`, `PgArrays`, `SqlParams`, `SelectList`, `SeqClockQuery`,
  `RecordRow`, `RecordRowMapper`, `ListOrder`, `GeometryEncoding`, `CurrentQuery`,
  `HistoryQuery`, `ChangesQuery`, `Slices`, `RecordQueryService`, `AsOfQueryService`
- `retention/RetentionState.java`, `DefaultRetentionState.java`

**Modified (main)**
- `api/error/ErrorCodes.java`, `ForesignalException.java`, `GlobalExceptionHandler.java`
- `security/AccessPolicy.java`, `settings/SecuritySettings.java`, `stream/LiveBus.java`
- `src/main/resources/application.yml` (`foresignal.api.*`, `server.compression`)

**Tests**
- New unit tests: `api/support/BboxParserTest`, `NearParserTest`, `CursorCodecTest`,
  `FieldSelectorTest`, `QueryParamsTest`, `ETagsTest`, `ResponseFormatTest`,
  `GeometryOptionsTest`, `StreamWriterTest`; `api/mapper/RecordMapperTest`,
  `KindSchemaBuilderTest`; `store/query/SeqClockQueryTest`, `SqlHelpersTest`;
  `settings/SecuritySettingsTest`
- New integration tests: `store/query/RecordQueryServiceIT`, `AsOfQueryIT`, `ChangesQueryIT`,
  `CurrentReadsUseHeadLiveIT`; `api/RecordsControllerIT`, `ChangesControllerIT`, `KindSchemaIT`,
  `StreamingFormatsIT`
- New test support: `testsupport/records/RecordsTestSources` (private, raster-frame and two schema
  sources, plus a `@Primary` `StubRetentionState`) and `Queries` (query builders)
- Modified: `security/AccessPolicyTest`, `stream/LiveBusTest`

**Docs and build**
- `docs/OpenAPI.json` (status flips and reconciliations, Deviation 9)
- `docs/Agent-Guide.md` (new)
- `gradle/libs.versions.toml` and `build.gradle.kts`: `tools.jackson.dataformat:jackson-dataformat-csv`.
  The version (3.1.5) comes from the Jackson BOM that Spring Boot 4.1.1 imports. 3.1.5 exists on
  Maven Central, and the license is Apache-2.0.
- Postman regenerated (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`). The
  collection came out byte-identical, because none of the changes alters a request.

No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` changes. Nothing was ported from God's Eye
View.

## Deviations from the prompt

1. **Expiry uses the application `Clock`, not SQL `now()`.** The `(valid_until IS NULL OR
   valid_until > :now)` filter and the asOf future check bind `clock.instant()`. In production this
   means the same thing. It also lets tests drive time through `MutableClock`, which already drives
   `ingested_at`.
2. **`timeField=ingested` uses exact seq bounds.** It applies `seq > S(since − 1 µs)` and
   `seq <= S(until − 1 µs)` instead of `cutoffSeq(since) … < cutoffSeq(until)`. `cutoffSeq` is
   minute-granular, so `until=now` would silently drop the current minute. `S(T)` is exact to the
   batch and still prunes partitions through `seq_clock`. `SeqClockQuery.cutoffSeq` exists and is
   tested.
3. **History orders on the selected axis.** The spec says "versions ordered by the selected
   timeField". The keyset is `(axis, seq)`: `observed_at` by default, as the prompt says,
   `COALESCE(event_time, observed_at)` for effective, `event_time` for event, and `seq` alone for
   ingested.
4. **Items are ordered maps, not fixed DTO records.** One mapping then honors `fields=` for JSON,
   GeoJSON, ndjson and csv alike. The page wrappers (`RecordPage`, `ChangePage`, …) are records.
5. **Streamed changes load payloads inline.** They select a
   `CASE WHEN kind = ANY(inline) THEN payload END` column. A per-row `seq = ANY(?)` reload does not
   fit a cursor stream. The paged JSON feed loads payloads with `seq = ANY(?)` as the prompt says.
6. **Empty change pages advance `lastSeq`.** When `hasMore` is false, `lastSeq` is at least the
   `watermarkSeq` read before the query. Every seq at or below it was committed before the scan,
   so a filtered client can skip ahead instead of rescanning. The spec description records this.
7. **`FieldSpec` has no `enumLabels`, `unitLabel` or `format` yet.** The spec assigns them to later
   prompts (013 report, Issue 1). The kind schema emits `type`, `description`, `x-unit`, `x-label`,
   `x-datum`, `x-format: timestamp` for `TIMESTAMP` fields (typed `["string","integer"]`) and
   `x-display`. `enum` and `x-unitLabel` appear when those fields exist.
8. **The title comes from the stored column.** `record_head.title` exists (008/009), so current
   rows read it. Versions and asOf rows resolve it from the version payload with the shared
   `TitleResolver`. No "`kind externalId`" fallback was added to the resolver: the stored title is
   nullable and the spec allows `title: null`.
9. **Spec reconciliations**, all in the changelog, and none of them changes an operation:
   - `Feature.properties` gains optional `ingestedAt` (history and asOf features) and
     `persistReason` (history features).
   - The `KindSchema` description documents `x-datum`, the `TIMESTAMP` mapping and the per-variant
     `x-sources`.
   - `ChangePage.lastSeq` documents Deviation 6.
   - The change feed's `geojsonseq` description says its properties are the `Change` fields.
10. **ETags and history.** ETags are sent on `listRecords` (JSON and GeoJSON), `getRecord`,
    `listKinds` and `getKindSchema`, which are the operations whose spec declares `ETag` and `304`.
    History, versions and changes declare neither and send none. Streaming responses send
    `Cache-Control: no-store` and no ETag.
11. **`format=czml` on `/v1/history` is a 400 for now.** It is in the spec enum, but prompt 141
    builds it.
12. **Accepted but inactive parameters.** `include=region` and `include=entity` are accepted and do
    nothing until 040 and 129. Parameters of later prompts (`place`, `view`, `inWatchlist`,
    `count`, `country`, `admin1`, `county`, `licencePolicy`) are not read; their prompts implement
    them.
13. **The asOf merge uses byte order.** The asOf tier queries page and sort with
    `COLLATE "C"`, so the Java merge (`String.compareTo`) and the SQL keyset agree whatever the
    database collation is.
14. **One shared test context.** Every 014 integration test uses `@IntegrationTest`,
    `@AutoConfigureMockMvc` and `@Import(RecordsTestSources.class)`, which adds exactly one new
    cached context. The row-cap test calls `streamCurrent`/`streamHistory`/`streamChanges` with a
    cap of 2 directly, rather than lowering `foresignal.api.stream.maxRows`, which would need a
    second context.
15. **`pending-operations.txt` does not exist yet**; prompt 020 creates it. There were no lines to
    delete.

## Issues found / open questions

1. **`docs/Invariants.md` HIST-3 disagrees with Core-Data-Model §12 and this prompt.** HIST-3 says
   asOf reads "below the purge horizon … fail with `410 RESYNC_REQUIRED`". §12 and the prompt
   (implemented exactly) say an asOf before the retention horizon is `400 ASOF_BEYOND_RETENTION`,
   and `410 RESYNC_REQUIRED` belongs to `/v1/changes`. `AsOfQueryIT` asserts the 400.
   `ChangesQueryIT` and `ChangesControllerIT` assert the 410 on the change feed. **Owner:** which
   one should change?
2. **`SecuritySettings.exposeObserver` is a boxed `Boolean`,** so JSON stored before 014 (without
   the key) still binds under Jackson 3. The accessor is never null.
3. **Only the admin token exists until 019/023.** `include=observer` is therefore effectively
   "any token, or `exposeObserver`". The rule is already written as agent-or-admin
   (`ReadCaller.mayReadObservers`).
4. **The prompt 012 and 013 owner decisions are still open** (`touched` semantics, LGPL
   `geolatte-geom`, `MotionSpec.courseField`, LATEST `minInterval`, clearing TRACK retention).

No instructions directed at an agent were found in data, fixtures or tool output.

## Tests written (not run)

As a feature prompt requires, no tests were run. Every integration test seeds through `TestRecords`
only; none inserts into `record_head`, `record_live` or `record_version` with SQL.

- **`RecordQueryServiceIT`**:
  - the payload merged after a LIVE update, with a dropped key absent
  - each filter alone and combined: source, kind, externalId, bbox, near, text, excluded sources
  - an antimeridian bbox
  - near + radius on the geography, across the antimeridian
  - `sort=distance` keyset across pages
  - expiry with `includeExpired`, and tombstones
  - `%` and `_` escaping
  - `order=observed` and `order=id` paging with equal timestamps (no duplicates or gaps)
  - **snapshot paging stable under mutation**: the pinned `w` excludes the entity updated between
    pages, and `changes(after=w)` delivers it
  - `asOfSeq` pinning
  - mismatched cursors
  - `effective` vs `observed` (and `event`) for a revised quake
  - `ingested` through `seq_clock`
  - `include=observer`, private source exclusion and payload projection
  - kinds; versions with a seq cursor, since/until and 404; history paging and filters
- **`AsOfQueryIT`**:
  - asOf before and after a revision, and before the first ingest
  - a stationary keyframed aircraft (TRACK, SAMPLED) between keyframes, and expired after its TTL
  - a long-valid NWS-style alert (STANDARD, `L = 0`) valid until its explicit expiry
  - TTL validity through `ingested_at + ttl`
  - a tombstone
  - bbox and kind applied to the chosen version, and keyset paging merged across tiers
  - `approximate` for LATEST and SAMPLED
  - 400 `ASOF_BEYOND_RETENTION` with `horizon`, and the horizon moving with the window
  - 400 `ASOF_FILTER_REQUIRED`
  - 400 `ASOF_INVALID` (future, and each forbidden combination)
  - 404 for an unknown source
- **`ChangesQueryIT`**:
  - `after` exclusivity, `hasMore` and `lastSeq` paging, and the watermark skip-ahead
  - `validUntil`, `persistReason`, `prevSeq`, `historyTier` and `bbox`
  - source, kind, externalId and bbox filters, and private exclusion
  - `payload=true` and projected payloads
  - a `raster-frame` payload inline with `payload=false`
  - 410 `RESYNC_REQUIRED` from a stub `RetentionState`, with the horizon computed over the filtered
    sources
- **`StreamingFormatsIT`**:
  - ndjson (via `format` and via `Accept`)
  - geojsonseq (RS separators, precision, null geometry)
  - csv with `fields` (WKT, payload columns, quoting) and the default header
  - history and changes in all three formats
  - `RESULT_TOO_LARGE` over a lowered cap with nothing written, and an exact cap passing
- **`RecordsControllerIT`**:
  - JSON with the spec field names and headers
  - GeoJSON via both negotiation methods, with `format` winning
  - `title`, `fields`, `precision` and `simplify` (a point untouched, a line reduced)
  - ETag and 304 on lists (the tag changes after a write), on a record and on kinds
  - cursor paging, `distanceKm`, and asOf through the API
  - twenty 400 cases, `INVALID_CURSOR`, and 404s
  - private sources hidden from anonymous callers
  - observer gating by token and by `exposeObserver`
  - versions and history fields
  - kinds with `display`
- **`ChangesControllerIT`**: `ChangePage` and `Change` fields; parameters; 410 with extensions in
  JSON and ndjson; 400s; 404.
- **`KindSchemaIT`**: the merge, the `oneOf` conflict with `x-sources` and `x-conflicts`, a
  kind-scoped field, `x-display`, ETag and 304, and 404.
- **`CurrentReadsUseHeadLiveIT`** (HIST-2): the current-state FROM clause, no `record_live` index on
  `observed_at`, and an `EXPLAIN` touching only head and live.
- **Unit tests**:
  - `CursorCodecTest`: round trips, tampering, other variant or query, garbage
  - `BboxParserTest`: the split and every rejection
  - `NearParserTest`: rejections, and the prefilter at mid latitude, across the antimeridian and
    near a pole
  - `FieldSelectorTest`, `QueryParamsTest`, `ETagsTest`, `ResponseFormatTest`,
    `GeometryOptionsTest`, `StreamWriterTest`
  - `RecordMapperTest`: shapes, version title through `TitleResolver`, projection, CSV
  - `KindSchemaBuilderTest`
  - `SeqClockQueryTest`: `cutoffSeq`, and `S(T)` within, before and in the newest minute
  - `SqlHelpersTest`, `SecuritySettingsTest`, `LiveBusTest` (batch number), `AccessPolicyTest` (the
    schema rule and its fail-closed neighbors)

The existing `TitleResolverTest` (008) already covers the resolver. It was not changed, because
the resolver was not.

## Compile result

```
$ ./gradlew clean compileJava compileTestJava
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
BUILD SUCCESSFUL in 6s
5 actionable tasks: 5 executed
```

## Notes for the next prompt

1. **015 (SSE change stream):**
   - Reuse `BboxParser` / `BboxFilter`; a box may now be two envelopes.
   - Read the watermark with `ChangeBus.lastPublishedSeq()`.
   - Call `RecordQueryService.changes` for replay, and `purgeHorizon(sources)` together with
     `RetentionState` for the 410 and `reset` decisions.
   - The `inlinePayloadKinds` setting is `ApiProperties.changes().inlinePayloadKinds()`.
   - `RecordMapper.change` gives the `Change` shape; add the SSE-only fields there.
   - `docs/Agent-Guide.md` exists. Add the "Streaming" section to it.
2. **016 (tiles):** reuse `BboxParser`, `GeometryOptions` and the current-state FROM clause and
   payload (`RecordQueryService.CURRENT_FROM` / `CURRENT_PAYLOAD`).
3. **019 (retention):**
   - Replace `DefaultRetentionState` with the published values. Make the new bean `@Primary` or
     remove the default.
   - `purgeSeq` must advance only on head purges.
   - Tests override retention state through `RecordsTestSources.StubRetentionState`.
4. **020 (pending operations):** the seven 014 operations are implemented; do not list them.
   `streamChanges` (015) and the 039 record operations remain.
5. **025 (MCP), 028 (Ask):** use `RecordQueryService`, `AsOfQueryService` and `RecordMapper`; none
   has web types except `RecordMapper`'s use of `RawJson`. The title resolver is
   `ai.foresignal.ingest.TitleResolver`.
6. **039 (private and egress):** private-source exclusion is in `ReadCallerResolver.excludedSources`
   and is applied on every 014 path. Move it into `EgressPolicy` when 039 builds it.
7. **Test contexts:** new read-API ITs should use `@IntegrationTest`, `@AutoConfigureMockMvc` and
   `@Import(RecordsTestSources.class)` to share the 014 context.
8. **Owner decision pending:** Issue 1 (HIST-3 wording), plus the decisions carried from 012 and
   013.
