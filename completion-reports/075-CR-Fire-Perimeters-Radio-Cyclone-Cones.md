# 075-CR-Fire-Perimeters-Radio-Cyclone-Cones

| | |
|---|---|
| Prompt | 075-Fire-Perimeters-Radio-Cyclone-Cones.md |
| Status | Complete |
| Implementation commit | 2bd802e12bd26b0a43b789a6ada53e1cce6d7b28 |
| Date | 2026-10-13 |

## Summary

Seven sources are in, all disabled by default and each with a license: US wildfire perimeters, InciWeb incident
summaries, three CWFIS layers, the Radio Browser station directory and NHC forecast geometry. They are built on the
prompt 073 kits. HMS smoke and fire stay in prompt 048, and no adapter here fetches an HTML page.

**1. Shared helpers** (`ai.foresignal.source.support`):
- **`ScopedFetchContext`:** a `FetchContext` whose adapter state is namespaced as `{scope}/{key}`. The health keys
  pass through unscoped. NHC uses it so that its four ArcGIS layers do not overwrite each other's `arcgis:meta`.
- **`JsonProps`:** typed reads of one JSON member (text, number, integer, epoch ms, ISO). They return null rather than
  a default.
- **`MappedRecord`:** a record or a skip reason (`missing-id`, `missing-observed-at`, `bad-geometry`,
  `duplicate-id`).
- **`SkipCounter`:** counts skips per `source` and `reason`.

**2. `nifc-perimeters`** (`NifcPerimetersAdapter`, `NifcPerimeterMapper`; ported from GEV):
- **Fetch:** `ArcGisQueryClient` with GEV's query (`where=1=1`, the sixteen fields, `maxAllowableOffset=0.001`,
  `outSR=4326`, `f=geojson`), at most 5 pages of 2000. An unchanged `dataLastEditDate` short-circuits the run.
- **Mapping:** `externalId` is `attr_UniqueFireIdentifier`, else `attr_IrwinID`; a feature with neither is skipped.
  `observedAt = poly_DateCurrent`, `eventTime = attr_FireDiscoveryDateTime`.
- **Payload:** the prompt's renamed keys, with `gisAcres` rounded to 1 acre and `personnel` volatile. Geometry over
  9,500 vertices is simplified by `FeatureGeometry`.
- **Descriptor:** 5 min, STANDARD, ALL, no TTL, `zeroResultsNormal`, title `name`, rank `gisAcres`, the prompt's
  tiles, retention 1825 d.
- **Absence:** a perimeter missing from two consecutive complete fetches is tombstoned.

**3. `inciweb-incidents`** (`InciwebIncidentsAdapter`, `InciwebItemMapper`). The mapper is registered in
`FeedItemMappers.REVIEWED` under the name `inciweb`.
- **Fetch:** a conditional GET of the RSS feed, parsed by `FeedParser` and mapped by `FeedRecords` under a coded
  `FeedSourceSpec` with `itemMapper: inciweb`.
- **Mapper (pure):**
  - the title is split into `unitCode` and `name`;
  - `Last updated`, the incident type, `State` and the `Incident Overview` (≤ 2 KB) are read from the description;
  - coordinates are read as D M S without symbols, minutes and seconds below 60, north and west;
  - a position outside the US envelope gets no geometry, and the adapter counts it as `geometryRejected`.
- **Descriptor:** `observedAtMode` RECEIVED, `eventTime = pubDate`, TTL 14 d on receipt, never tombstoned,
  `embedFields name, overview`, 30 min, 1/min and 100/day.

**4. `cwfis-*`** (`CwfisLayer`, `CwfisAdapter`, `CwfisFamily`, `CwfisMappers`). All three use the WFS kit with
`count=1000` and `startIndex` paging; coordinates are lon,lat, with a North America bbox for the kit's axis check.
- **`cwfis-perimeters`:** m3 polygons, described as not agency perimeters. `externalId` is
  `{firstdate}@{lat},{lon}` of the centroid at 2 dp (see Deviations). `observedAt = lastdate`,
  `eventTime = firstdate`. Payload `hotspotCount, firstDetected, lastDetected, areaHa, perimeterSource`. 3 h, absence
  tombstones.
- **`cwfis-fwi-stations`:** `sortBy=aes`; `externalId` is `aes`, else `wmo:{wmo}`. The surface entry wins over an
  upper-air entry with the same `aes`. Padded strings are trimmed. The prompt's payload has UCUM `fieldSpecs`. TTL
  36 h OBSERVED, retention 365 d.
- **`cwfis-hotspots`:** `externalId` is `rep_date:lat:lon:satellite` at 5 dp; the payload holds the FBP fields. TTL
  24 h OBSERVED, retention 7 d. The description names FIRMS as the primary source.
- **Not ingested:** `public:fdr_current_shp`.

**5. `radio-browser`** (`RadioBrowserAdapter`, `RadioMirrors`, `RadioBrowserParser`; ported from GEV):
- **Mirror discovery:** host names are filtered by `^[a-z0-9-]+\.api\.radio-browser\.info$`, de-duplicated, shuffled
  and cached for 6 h in the adapter state. When discovery fails, the fallback is `de1`, `de2`, `nl1`.
- **Failover:** a 5xx or an I/O failure moves to the next mirror within the same fetch, and every attempt is a
  budgeted request.
- **Crawl:** pages are read until a short page or `maxStations` (default 40,000).
- **Payload:** the prompt's keys; the excluded counters are never read, and an exact (0,0) gets no geometry.
- **Descriptor:** 6 h, STANDARD, ALL, no TTL, absence tombstones only after an untruncated crawl, retention 90 d. It
  carries the license statement captured from `api.radio-browser.info`.

**6. `nhc-cyclone-cones`** (`NhcCycloneConesAdapter`, `NhcLayerResolver`, `NhcConeMapper`):
- **Layers:** the ids are resolved by name, cached for 24 h; a missing name is an `UpstreamFormatException`. Each of
  the four layers is queried with `ArcGisQueryClient` under its own `ScopedFetchContext`.
- **Storm id:** the same format as 067's id (`ep202026`). It is read from the `idp_source` prefix, else built from
  basin + `stormnum` + year.
- **Time:** `observedAt = idp_filedate`, and `eventTime = validtime` for points. `DD/HHMM` is placed in the product
  time's month, with month rollover.
- **Payload:** the allowlist, with 9999 left out.
- **Absence:** tombstones are tracked per kind; an empty layer counts as complete. 10 min, `zeroResultsNormal`.

**7. Docs:**
- **`docs/Source-Catalog.md`:** rows for all seven sources, a prompt 075 note (HMS lives in 048, the CWFIS/FIRMS
  overlap, `fdr_current_shp` → 106, RSS only for InciWeb), the roadmap line, and a refined InciWeb row under
  "External provider ideas".
- **`docs/OpenAPI.json`:** kind notes for `fire-perimeter`, `fire-weather`, `radio-station` and the four `cyclone-*`
  kinds, plus the `fire-detection` and `fire-incident` notes for `cwfis-hotspots` and `inciweb-incidents`. A
  changelog entry was added; there is no operation change.
- **`docs/Adapter-Guide.md`:** §22 gains a bullet on using kit pieces in hand-written adapters.
- **`THIRD_PARTY_NOTICES.md`:** four GEV port rows and a "Not ported" paragraph.

## Files created / modified

- **Created, main:**
  - `source/support/`: `ScopedFetchContext`, `JsonProps`, `MappedRecord`, `SkipCounter`;
  - `source/adapters/fire/`: `package-info`, `FireSources`, `FireFetch`, `NifcPerimeterMapper`,
    `NifcPerimetersAdapter`, `InciwebItemMapper`, `InciwebIncidentsAdapter`, `CwfisLayer`, `CwfisMappers`,
    `CwfisAdapter`, `CwfisFamily`;
  - `source/adapters/radio/`: `package-info`, `RadioMirrors`, `RadioBrowserParser`, `RadioBrowserAdapter`;
  - `source/adapters/nhc/`: `NhcLayerResolver`, `NhcConeMapper`, `NhcCycloneConesAdapter`.
- **Modified, main:** `source/kit/feed/FeedItemMappers` (the `inciweb` mapper is reviewed).
- **Created, test:**
  - `fire/`: `NifcPerimeterMapperTest`, `InciwebItemMapperTest`, `CwfisMappersTest`, `FireAdaptersTest`;
  - `radio/`: `RadioBrowserParserTest`, `RadioMirrorDiscoveryTest`;
  - `nhc/`: `NhcConeMapperTest`, `NhcLayerResolverTest`;
  - `source/adapters/FireAndCycloneIngestIT`.
- **Created, fixtures** (each directory with a README): `nifc-perimeters/`, `inciweb-incidents/`,
  `cwfis-perimeters/`, `cwfis-fwi-stations/`, `cwfis-hotspots/`, `radio-browser/`, `nhc-cyclone-cones/`.
- **Modified, test:**
  - `testsupport/weather/WeatherIngestSupport`: the fire and radio base-URL properties, plus `nifc-perimeters` and
    `nhc-cyclone-cones` in the backoff list;
  - `retention/FamilyDefaultsTest`: the five new adapter classes.
- **Docs:** `docs/Source-Catalog.md`, `docs/OpenAPI.json`, `docs/Adapter-Guide.md`, `THIRD_PARTY_NOTICES.md`. The
  Postman collection was regenerated with no change.

## Deviations from the prompt

1. **NHC track id is `stormId:track`, not `stormId`.** Records are keyed by `(source_id, external_id)`
   (Core-Data-Model §9; `record_head` has that primary key), so a cone and a track with the same id in one source
   would overwrite each other. The cone keeps `stormId`, as the prompt says. The payload's `stormId` is always the
   bare 067 id.
2. **`warningType` was added to `cyclone-warning`.** Without it, the `tcww` code (TWA, TWR, HWA, HWR) survives only
   inside the id. It has been added to the OpenAPI `payloadFields`.
3. **CWFIS perimeter ids use the fallback** (`firstdate@lat,lon`). Two captures a day apart were not possible within
   one run. The single capture already shows the GeoServer ids are not stable: they are unrelated to `firstdate`, and
   the highest ids carry the newest `lastdate`. The capture also contained two identical upstream rows, and the
   second is skipped as `duplicate-id`.
4. **Budgets raised where a run needs more requests than the prompt's per-minute value allows.** The governor is a
   greedy per-minute bucket that refuses rather than queues, so these runs would fail every time at the prompt's
   values. Daily values are unchanged.
   - `radio-browser`: 60/min, because a crawl is one discovery plus ~12–20 pages;
   - `nifc-perimeters`: 6/min, for metadata + 5 pages;
   - `nhc-cyclone-cones`: 10/min, because the first run of a day is the service document + 4 × (metadata + query).
   - CWFIS budgets were not given; they are 10/min and 200/day for perimeters and stations, and 30/min and
     1,000/day for hotspots.
5. **`order=name`, not `order=stationuuid`.** `stationuuid` is not a documented `order` value; the docs list name,
   url, …, changetimestamp, random. The captured rows came back sorted by name anyway.
6. **CWFIS stations page with `sortBy=aes`.** Without a sort order GeoServer refuses `startIndex` on this type:
   "Cannot do natural order without a primary key".
7. **InciWeb is a hand-written adapter on the feed kit, not a `kits/feeds` spec.** The feed-spec validator requires
   `feed-…` ids, and the kit descriptor's fixed title, search, embed and fieldSpecs would not match the source's
   payload. The adapter uses `FeedParser` and `FeedRecords` with a coded spec, and the mapper is in `REVIEWED`.
8. **Guam and the far Aleutians are read east of 180°.** "Longitude west" cannot place Guam inside the US envelope
   the prompt includes.
9. **Additions:**
   - `online` (radio) is volatile: it flips with Radio Browser's checks while `observedAt` (`lastchangetime`) stays
     fixed;
   - search fields for `nifc-perimeters` and `inciweb-incidents`;
   - simplification flags on CWFIS and NHC records;
   - an empty but complete layer counts toward absence for NIFC, CWFIS and NHC, as 074 did, so off-season
     perimeters and dissipated storms are retired after two empty fetches.
10. **Retention by class:**
    - perimeters, incidents and cyclone kinds: `event`, 1825 d;
    - fire-weather stations: `state`, 365 d;
    - CWFIS hotspots: `fire-detection` 7 d, as `goes-fdc` and `nasa-firms`;
    - radio stations: `reference`, 90 d.
11. **Spring constructors:** every new component has its injection constructor marked `@Autowired`
    (`NifcPerimetersAdapter`, `InciwebIncidentsAdapter`, `CwfisFamily`, `RadioBrowserAdapter`,
    `NhcCycloneConesAdapter`).
12. **Fixture privacy:** InciWeb `dc:creator` user names were replaced with `redacted`.

## Issues found / open questions

1. **The 070–073 families still have two constructors without `@Autowired`** (074 Issue 1). They were not touched.
2. **GEV has no InciWeb RSS route.** GEV uses the index and scraped pages (`server/providers/firePerimeters.js`), so
   the InciWeb catalog row names no GEV reference.
3. **CWFIS station values look like sentinels.** `rh: 0` at a coastal station and `pres`/`vis` of 0 suggest zeros
   stand in for missing values. `rh` is stored as given; `pres` and `vis` are not in the payload.
4. **US English:**
   - `check_us_english.py` on the new files and the added doc lines: the remaining hits are allowed exceptions (the
     `licence` builder and wire field, "Open Government Licence – Canada", `HARBOUR` in captured station names);
   - "Fire Behaviour Prediction" was reworded to "Canadian FBP System fire-behavior predictions";
   - the workspace guard `test_us_english.py` ran 14 tests, OK (it scans the MeshDTN repositories, not this one).
5. **`docs/Source-Catalog.html`** was not regenerated (as in 070–074).
6. **Instructions in data:** none. The WFIGS, NHC and CWFIS responses, the InciWeb feed, the Radio Browser servers
   and stations and its docs page were read, and nothing addressed an agent.

**Owner questions (non-blocking):**
1. Accept the raised per-minute budgets (Deviation 4), or keep the prompt's and change the fetch pattern?
2. Accept `stormId:track` for tracks, or prefix every NHC kind (`stormId:cone`, …) for symmetry?
3. Should the CWFIS station sentinels (`rh: 0`) be dropped once their meaning is confirmed?

## Tests written (not run)

- **`NifcPerimeterMapperTest`:** 25 perimeters, the payload allowlist, one record in full, simplification of the
  10,750-vertex perimeter, the IRWIN fallback, missing time, null and broken geometry, `SnapshotContract` (a
  `personnel` change keeps the hash), `FieldSpecContract`.
- **`InciwebItemMapperTest`:** the mapper is reviewed and the spec passes the kit rules except the id; 25 records,
  20 points, 2 refused; the Bull Fire in full; DMS variants (decimal and three-digit seconds, a space before `°`);
  refused lines; an envelope table; edge cases (Alaska, outside, Guam, bad seconds, no coordinates); title split;
  sections and the 2 KB cut; snapshot; fieldSpecs.
- **`CwfisMappersTest`:** descriptors (TTL, anchors, retention, license, `sortBy`); perimeters (24 plus a duplicate,
  the id format, simplification); id stability against renumbering; stations (trimmed, surface wins, WMO fallback);
  hotspots (id, payload); snapshots; fieldSpecs.
- **`RadioBrowserParserTest`:** 25 stations with excluded fields never present; edge cases (tab name, null island,
  unsafe links, tag cap, bad id, no change time, range, repeat); helpers; snapshot with a flipped `online`.
- **`NhcConeMapperTest`:** all layers (storm id equals 067's pattern, dropped fields); cones and tracks; warning
  ordinals; points (`validTime`, 9999 omitted); the storm-id fallback; `validTime` rollover; snapshot; fieldSpecs.
- **`NhcLayerResolverTest`:** name → id, a renumbered service, a missing name throws, the cache.
- **`RadioMirrorDiscoveryTest`** (WireMock): host-pattern rejection, shuffle and cache (6 h), failover on 500, no
  failover on 404, the fallback list, the crawl cap, absence, retryable classification.
- **`FireAdaptersTest`** (WireMock):
  - descriptors;
  - NIFC paging with GEV's query and the `lastEditDate` short-circuit;
  - the 5-page cap and an ArcGIS error body;
  - CWFIS `startIndex` paging with `sortBy`, the swapped-axis sample, and perimeter absence;
  - InciWeb conditional GET and 304, and HTML forgetting the validators.
- **`FireAndCycloneIngestIT`** (shares `WeatherIngestSupport`'s context):
  - every source registered and disabled, and the HMS ids unchanged;
  - NIFC ingest and a tombstone after two misses;
  - an NHC advisory change writes version 2 of the cone;
  - an empty off-season service keeps health `HEALTHY`.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi`: valid; 71 warnings, unchanged.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests, covers all ops; no change.
- No project tests were run (feature prompt).
- **Scratch check:** a throwaway `main` outside the repository ran `DescriptorValidator` on all seven descriptors (no
  problems) and parsed every fixture, to get the exact counts used in the assertions. No project test was executed.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK. It scans the MeshDTN repositories, not this one.

## Notes for the next prompt

1. **076 onward:** reuse `ScopedFetchContext` for any stateful kit client called more than once per fetch, and
   `JsonProps`, `MappedRecord` and `SkipCounter` for pure mappers. Adapters do not import each other.
2. **097 checkpoint:**
   - first run of every 075 test;
   - `FireAndCycloneIngestIT` uses `WeatherIngestSupport` (new properties `foresignal.test.adapters.fire.base-url` and
     `foresignal.test.adapters.radio.base-url`; NHC cones reuse `foresignal.test.adapters.nhc.base-url` as
     `/{host}{path}`);
   - check the hand-computed counts and the WireMock query matching (ArcGIS `orderByFields=OBJECTID` on the NHC
     MapServer, whose OID field is `objectid`, worked live);
   - 074 Issue 1 (constructors) still applies to 070–073.
3. **143:**
   - recapture per the fixture READMEs;
   - capture CWFIS m3 twice a day apart to confirm the id finding;
   - re-check Radio Browser discovery (only `de1` listed on 2026-10-13) and the crawl size against the budget;
   - confirm WFIGS `editingInfo` and the NHC layer names.
4. **144 (Flyway):** no schema change. Adapter state:
   - `nifc-perimeters`: `arcgis:meta`, `arcgis:lastEdit`, `absence:*`;
   - `inciweb-incidents`: `etag:feed`, `lastmod:feed`;
   - `cwfis-perimeters`: `absence:*`;
   - `radio-browser`: `radio:mirrors`, `radio:mirrorsAt`, `absence:*`;
   - `nhc-cyclone-cones`: `nhc:layers`, `nhc:layersAt`, `layer-{points,track,cone,warning}/arcgis:meta`,
     `absence:ids:{kind}`, `absence:missed:{kind}`.
5. **Owner decisions pending:** the three questions above, plus those carried from 012–074.
