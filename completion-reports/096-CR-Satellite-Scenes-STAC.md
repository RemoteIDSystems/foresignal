# 096-CR-Satellite-Scenes-STAC

| | |
|---|---|
| Prompt | 096-Satellite-Scenes-STAC.md |
| Status | Complete (post-review fixes 2026-10-16) |
| Implementation commit | c0fc6425d435ec7c11da6b020045091fa93f3692 |
| Post-review fix commit | 8a04064d6bba9f8f3f31f8b5444d3b2f153c265d (plus the framework commit 94a06bdd0c778319829d3d1be589a805432ddfea) |
| Date | 2026-10-14; corrected 2026-10-16 |

## Summary

New Sentinel-2 and Landsat (optionally Sentinel-1) scenes over the operator's area of interest are recorded
as `satellite-scene` records from the Earth Search STAC API, footprints and links only. Foresignal never
downloads, proxies or signs an imagery asset.

**1. `EarthSearchFamily` (`source/adapters/stac`).** One `EarthSearchAdapter` per feed of the reviewed,
code-reviewed list `StacSources.REVIEWED` (validated at startup): `stac-sentinel2`
(`sentinel-2-c1-l2a`), `stac-landsat` (`landsat-c2-l2`) and `stac-sentinel1` (`sentinel-1-grd`, off by
default). Every source is STANDARD/ALL, no TTL, `observedAtMode=UPSTREAM`, `zeroResultsNormal=true`,
interval 1 h (min 15 min), budget 4/min and 300/day, `maxResponseBytes` 8 MB, `defaultRetention` 90 d (the
`bulk-event` class). `aoi` (a bbox or a 036 place id) is required, so enabling without it fails with 400
`VALIDATION_FAILED` (the `ParamSpec` is required). A bbox that crosses the antimeridian is split into two
requests. A place AOI is searched with the place's own `area` (since the review; before it, a ±0.25° box around
the place point), split into eastern and western requests when it straddles the antimeridian.

**2. Search.** `POST {base}/search` with `collections`, `intersects` (a place AOI) or `bbox` (a box AOI),
`datetime: "{from}/.."`, `query: {"eo:cloud_cover": {"lt": maxCloudCover}}`, `sortby` newest-first,
`limit` 100 and the `fields.include` projection. The `query` extension is used (CQL2 `filter` is silently
ignored by Earth Search). The `rel=next` link's POST body (an opaque `next` token) is followed verbatim, at
most `maxPages` pages per AOI part and, since the review, never more requests per run than the 4/min budget
(shared by the parts of a split AOI); a page whose items are older than `from` ends the run. The newest
acquisition is stored in adapter state after a successful run (`stac:lastAcquisition`; it commits with the
fetch's records), and the next run starts 2 days behind it.

**3. `StacItemParser` (pure).** Kind `satellite-scene`, `externalId = {collection}:{id}`,
`eventTime = properties.datetime`, `observedAt` read from the one property the collection fixes
(`StacSource.observedAtField`: `updated` for all three, decided by the fixtures; an item without it is skipped).
As implemented in c0fc642 the parser chose `updated ?? created` per item, contrary to the prompt; fixed in
8a04064. A malformed footprint, or one whose AOI intersection fails, drops that item only and is counted.
Footprint via Foresignal's GeoJSON reader (raised vertex cap), split at the antimeridian into a MultiPolygon
(the shared `raster.geotiff.Antimeridian` helper) and simplified with `TopologyPreservingSimplifier` below
9,500 vertices. Payload `collection, platform, constellation, instruments, cloudCover` (1 dp, absent for
SAR), `mgrsTile`/`gridCode`, `wrsPath`/`wrsRow`, `sunElevation`, `sarMode`, `polarizations`,
`aoiCoverPct` (footprint ∩ AOI ÷ AOI in a local Lambert azimuthal equal-area plane, 0–100 integer),
`stacItemUrl` (the `self` link), `thumbnailUrl` only for an `https://` thumbnail (Sentinel-2; Landsat and
Sentinel-1 `s3://` thumbnails are dropped, never rewritten or signed), `assetKeys` (sorted, ≤ 40),
`eventTime` and `title` (`{platform} {tile} {cloud}%`). `volatileFields = []`, `significantFields` empty.

**4. Filter guard.** Earth Search ignores a CQL2 `filter` and returns every item, so the parser throws
`UpstreamFormatException("search filter ignored")` when a returned item violates the requested cloud bound
or lies after the window's upper bound; an item older than the window ends the page instead.

**5. Spec, Postman, docs.** No operations change; `pending-operations.txt` and Postman are unchanged.
`docs/OpenAPI.json` reconciles the pre-authored `satellite-scene` kind (adds `title` and `eventTime` to its
payload list, adds a 096 changelog entry). `docs/Source-Catalog.md` gains a three-row section (tier, policy,
`observedAtMode`, retention, licence, the verified-2026-10 notes and the Planetary Computer alternative);
`docs/Operator-Guide.md` gains a "Satellite scenes" section (AOI setup, links-only rule, rule recipes, a
Sentinel-1 flood recipe); `docs/Agent-Guide.md` notes that agents follow `stacItemUrl` and that Foresignal
never fetches imagery.

## Files created / modified

**Created, main** (`src/main/java/ai/foresignal/source/adapters/stac/`)
- `package-info.java`, `StacSource.java`, `StacSources.java`, `Footprints.java`, `StacItemParser.java`,
  `EarthSearchAdapter.java`, `EarthSearchFamily.java`.

**Modified in the review**
- `source/spi/PlacePoints.java` (`area(placeId)`, default empty) and `place/PlaceService.java` (implements it).

**Created, test** (`src/test/java/ai/foresignal/source/adapters/stac/`)
- `StacItemParserTest.java`, `EarthSearchAdapterTest.java`, `StacDescriptorsTest.java`, `StacIngestIT.java`.

**Created, resources**
- `src/test/resources/fixtures/stac-sentinel2/{search.json,README.md}`
- `src/test/resources/fixtures/stac-landsat/{search.json,README.md}`
- `src/test/resources/fixtures/stac-sentinel1/{search.json,README.md}`

**Modified**
- `src/test/java/ai/foresignal/retention/FamilyDefaultsTest.java` (`EarthSearchAdapter` → `bulk-event`).
- `src/test/java/ai/foresignal/testsupport/weather/WeatherIngestSupport.java` (the STAC base-URL property and
  the three source ids).
- `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

## Deviations from the prompt

1. **Geometry reading.** `Geo.fromGeoJson` enforces the 10,000-vertex store limit *while reading*, so a
   large footprint would be rejected before it could be simplified. `Footprints` reads with
   `GeoJsonReader(200_000, 8)` and then simplifies, exactly as 073's `FeatureGeometry` does. SRID 4326 is
   preserved.
2. **Antimeridian split reuses the platform helper.** The prompt's "Read first" points at 073's "shared
   geometry helpers, antimeridian handling if added"; 073 added no shared splitter, so the adapter reuses
   `ai.foresignal.raster.geotiff.Antimeridian` (already used by the SatDump adapter, prompt 054), after
   unwrapping each ring. No new shared helper was introduced.
3. **`title` and `eventTime` are payload keys.** `titleField` must name a top-level payload key and
   `tileProperties` reads only payload keys, so the parser emits both. The pre-authored OpenAPI
   `satellite-scene` payload list did not include them; they were added there (and a changelog entry), an
   additive reconciliation (Conventions, API-first). Every prompt that emits `title` (for example
   `spc-storm-reports`) lists it, so this is consistent.
4. **The filter guard is cloud-first.** The prompt says to throw if an item violates the "cloud or time
   bounds". The cloud bound is what an ignored CQL2 `filter` actually breaks, so it throws. For the time
   bound, an item *after* the window (`now + 1 day`) throws, but an item *older* than the window ends the
   page — otherwise the documented "stop early when items are older than `from`" would be unreachable. Both
   are asserted in `StacItemParserTest`.
5. **Title tile fallback.** The title template is `{platform} {mgrsTile|wrsPath/wrsRow} {cloudCover}%`; the
   Sentinel-2 C1 collection publishes `grid:code` (`MGRS-…`) and no `s2:mgrs_tile`, so `gridCode` is the
   last-resort tile in the title. `mgrsTile` stays a declared (may-be-absent) payload field.
6. **Sentinel-1 "disabled by default" is a reviewed-list flag.** All built-in sources start disabled until
   the operator enables them (`SourceService.defaultRow`), so `StacSource.enabledByDefault` records the
   prompt's intent and `StacDescriptorsTest` asserts it (`stac-sentinel1` false, the other two true).
7. **Test base URL replaces the whole production base.** The family's test property moves the entire
   `https://earth-search.aws.element84.com/v1` base to WireMock, so the stubbed path is `/search`, not
   `/v1/search`. The production request is `POST {base}/search`.
8. **`src/test/.../StacItemParserTest` simplification fixture is generated in-memory** (a 12,000-vertex
   circle) rather than committed, to keep the repository small; the three search fixtures are trimmed real
   captures.
9. **A place area reaches the adapter through a new `PlacePoints.area` SPI method** (adapters see places only
   through SPI types); a place without an area, and every test double, keeps the point box.
10. **`maxPages` is bounded by the request budget.** The prompt's default `maxPages` 5 exceeds the 4/min
    budget it also sets; a run sends at most 4 searches, and a search that the budget cuts short is logged
    (older scenes in the window are then not read, because the prompt's newest-first sort cannot resume).

## Issues found / open questions

1. **Tests not run** (guardrail 1); they run first at checkpoint 097.
2. **Attribution year is fixed at descriptor build time.** The Copernicus attribution is
   `"Contains modified Copernicus Sentinel data " + Year.now()` (the Elexon pattern); a long-running
   instance keeps the build-year string until restart. The per-record acquisition year is not interpolated.
3. **The Sentinel-2 capture returned a top-level `rel=next` link** even though `numberMatched` (3) was below
   the 100 limit. The trimmed fixture drops it, so `EarthSearchAdapterTest.pagination` synthesizes a page
   with a `next` body; a real recapture at 143 should confirm whether the link is always present.
4. **Planetary Computer is not built** (its asset links need SAS signing, which Foresignal will not do); it
   is noted as an alternative in the Source Catalog, and Earth Search stays the default.
5. **`s3://` thumbnails are omitted, never signed.** Landsat (`usgs-landsat`) and Sentinel-1
   (`sentinel-s1-l1c`) thumbnails are requester-pays; a client that wants them must mint its own credentials.
6. **No schema change**, so no Flyway work is expected at 144; the only new persisted state is the adapter
   key `stac:lastAcquisition`.
7. **Budget truncation loses the oldest scenes of a window.** With the newest-first sort the prompt requires,
   a window holding more than 400 scenes per AOI part cannot be resumed; this is logged, and only a first run
   over a very large AOI can hit it.

## Tests written (not run)

- **`StacItemParserTest`** (pure, trimmed fixtures):
  - Sentinel-2 C1: field mapping (collection, platform, constellation, cloudCover rounded to 1 dp,
    `gridCode`, absent `mgrsTile`), time base (`eventTime` = `datetime`, `observedAt` = `updated`), the
    `https://` thumbnail kept, `assetKeys` sorted, `aoiCoverPct` 100, `FieldSpecContract`.
  - Landsat: WRS path/row, `cloudCover` 3.8, the `s3://` thumbnail dropped, `FieldSpecContract`.
  - Sentinel-1: no `cloudCover`, `sarMode`/`polarizations`, the `s3://` thumbnail dropped,
    `FieldSpecContract`.
  - `aoiCoverPct` full (100), partial (1–99), none (0).
  - An antimeridian-crossing footprint becomes a two-part MultiPolygon (SRID 4326).
  - A 12,000-vertex footprint is simplified below `Footprints.MAX_VERTICES`.
  - A cloud-bound violation throws `UpstreamFormatException("search filter ignored")`.
  - An item older than the window sets `olderSeen` and is not emitted.
  - A reprocessed item (only `updated` moved) has an identical payload and a stable static hash
    (`SnapshotContract`).
  - A non-ItemCollection body is a format change.
  - A malformed footprint drops that item only and is counted (`Result.malformed`).
  - `observedAt` comes from the collection's fixed property (an item with only `created` is skipped under
    `updated` and read under `created`); every reviewed feed uses `updated`.
- **`EarthSearchAdapterTest`** (`@IntegrationTest`, WireMock via `AdapterHttpHarness`):
  - the descriptor validates;
  - the POST body carries `collections`, `bbox`, `datetime`, the `query` cloud clause, `sortby`, `limit`
    and the full `fields.include`;
  - the `rel=next` body token is followed verbatim and the run is capped at `maxPages` (2 requests);
  - an item older than the window ends the run after one page;
  - no request is made outside `POST /search`;
  - adapter state advances only after a successful run (a 500 leaves it unset);
  - a 429 with `Retry-After` puts the source in backoff;
  - a place AOI is searched with the place's area (`intersects`, not a point box);
  - a place area across the antimeridian becomes two request parts;
  - a run's searches fit the 4/min budget, shared by the AOI parts.
- **`StacDescriptorsTest`:**
  - the family registers the three feeds and validates the list;
  - Sentinel-1 is off by default, the other two on;
  - every descriptor passes `DescriptorValidator` and `IngestPolicyValidator`, `aoi` is required and the
    defaults (interval, budget, retention, `maxResponseBytes`, `observedAtMode`, search/tile fields, units)
    hold;
  - the licences are per collection; a bad AOI is refused by `checkParams`.
- **`StacIngestIT`** (`@IntegrationTest` + `WeatherIngestSupport`, real fetch path and writer):
  - a first run ingests 2 scenes, each with `persist_reason = FIRST` and `aoiCoverPct = 100`;
  - an identical re-fetch writes no version;
  - a reprocessed item with a changed cloud cover is a `SIGNIFICANT` version 2;
  - an `only_new` rule on `aoiCoverPct >= 80` matches the two first versions only and does not grow after
    the reprocess.

## Compile result

- `./gradlew compileJava compileTestJava`: **BUILD SUCCESSFUL** (the existing unchecked-operations note on
  `StacItemParserTest`).
- No new dependencies (JTS arrives through hibernate-spatial; the antimeridian helper is in-tree).
- `docs/OpenAPI.json`: no operations added or modified; the `satellite-scene` kind list gains `title` and
  `eventTime`, and a 096 changelog entry is added. `pending-operations.txt` is unchanged; Postman was not
  regenerated (no operation or example changed).

## Notes for the next prompt

1. **097 checkpoint** (and the next one for the review additions): first run of every 096 test. New source ids `stac-sentinel2`, `stac-landsat`,
   `stac-sentinel1`; three new fixture directories each with a README; `WeatherIngestSupport` gained the
   STAC base-URL property and the three ids in `SOURCES`; `FamilyDefaultsTest` gained `EarthSearchAdapter`
   (`bulk-event`).
2. **`EarthSearchAdapter` is not a `@Component`;** `EarthSearchFamily` is, and builds one adapter per
   reviewed feed. A new collection is added to `StacSources.REVIEWED` (code-reviewed), never by a caller.
3. **109/110:** the records are `OPEN` and links-only; no licence flag needs reading. The Sentinel-2/S1
   licence is Copernicus, Landsat is US-PD.
4. **143:** recapture the three fixture sets per their READMEs; confirm the Sentinel-2 `rel=next` presence and
   that the C1 collection still uses `grid:code`.
5. **144 (Flyway):** no schema change; the only new adapter-state key is `stac:lastAcquisition`.

## Post-review fixes (2026-10-16)

Commit: `8a04064d6bba9f8f3f31f8b5444d3b2f153c265d`. The framework commit
`94a06bdd0c778319829d3d1be589a805432ddfea` makes `stac:lastAcquisition` commit only after the run's records are
written.

| # | Finding | Fix |
|---|---|---|
| 1 | Place AOI used only the point ±0.25° | `PlacePoints.area` / `PlaceService.area`; `EarthSearchAdapter.areaAoi` searches the area with `intersects`, split east/west when its parts straddle the antimeridian, and `aoiCoverPct` is measured against it. Tested. |
| 2 | One malformed footprint failed the page | The parser catches a per-item failure, drops and counts it (`Result.malformed`, logged by the adapter); `Footprints.aoiCoverPct` retries a `TopologyException` once with `GeometryFixer`. Tested. |
| 3 | 4/min budget below `maxPages` 5 (×2 split) | `pagesPerPart = min(maxPages, budget / parts)`, at least 1. Tested. |
| 4 | `observedAt` chosen per item | `StacSource.observedAtField` (`updated` for all three) passed in `StacItemParser.Options`; an item without it is skipped. Tested. |

CR corrections made in this document: `observedAt` was described as fixed per collection while the code chose
it per item; the paging description did not disclose that `maxPages` could exceed the budget; the place AOI was
described as an `intersects` search of the place without saying it was a box around the point.
