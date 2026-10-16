# 088-CR-Maritime-Intelligence

| | |
|---|---|
| Prompt | 088-Maritime-Intelligence.md |
| Status | Complete |
| Implementation commit | 562c42402ddebdb5528cf739d9587a50827e3308 |
| Date | 2026-10-14 |

## Summary

Maritime safety and trade intelligence:
- NGA navigational warnings, with geometry recovered from the free text;
- IMF PortWatch chokepoint transits, the port catalog with daily port activity, and disruptions;
- Global Fishing Watch vessel events.

All sources are disabled by default and live in the new package `ai.foresignal.source.adapters.maritime`, except
the disruptions, which are a feature-kit spec. The optional `gfw-sar-detections` and the conditional `uscg-lnm-*`
were not built: their verification conditions were not met (see below).

**1. `nga-msi-warnings`** (`NgaMsiAdapter`, `NgaMsiParser`, `MsiCoordinateExtractor`):
- **Request:** a conditional GET of `broadcast-warn?status=active&output=json`.
- **Verified 2026-10-14:** with Foresignal's own User-Agent the endpoint answered HTTP 200 (206 KB, 386 warnings),
  with no browser User-Agent and no Referer. The response carries `Cache-Control: max-age` and no validators.
  Neither header is ever sent.
- **`MsiCoordinateExtractor`** (pure):
  - **Position formats:** `DD-MM.mmN DDD-MM.mmW` and `DD-MM-SS.sN …`, with or without the space. Range checks:
    degrees at most 90/180, minutes and seconds below 60, and seconds only after whole minutes.
  - **Rules, in the prompt's order:**
    1. **Polygon** (a MultiPolygon for lettered areas);
    2. **64-vertex geodesic circle** for `WITHIN n MILES/NM/METERS… OF <position>`. Number words count, and miles are
       nautical;
    3. **LineString** for `TRACKLINE` or `BETWEEN … AND …` (a MultiLineString for lettered tracklines);
    4. **Point or MultiPoint**;
    5. **No geometry**, with `geometryFromText=false`.
  - **Antimeridian** (post-review fix): every geometry lies within ±180°. A circle, polygon or trackline that
    crosses 180° is made continuous and cut at ±180° into a MultiPolygon or MultiLineString; a circle enclosing a
    pole is a polar cap.
- **Results on the live list:**

  | Rule | Warnings |
  |---|---|
  | Polygon | 35 |
  | Circle | 3 |
  | Trackline | 15 |
  | Point or MultiPoint | 315 |
  | No position | 18 (4.7%) |

- **Record:**
  - externalId `navArea/msgYear/msgNumber`;
  - `observedAt` = `issueDate`;
  - payload: the prompt's fields plus `title`, the first line (≤ 120 characters);
  - `text` is capped at 8,000 characters, embedded, and searchable together with `authority`;
  - `cancels` holds externalIds parsed from `CANCEL HYDROLANT 927/24, …` lines.
- **Tombstones:** `LiveIdSet` tombstones a warning once it leaves the list. An empty list tombstones nothing. A
  warning whose `issueDate` does not parse is skipped as a record but keeps its id in the live set, so it is not
  tombstoned while still active.
- **Schedule and retention:** 15 min, 2/min and 200/day, retention 1825 d, `US-PD`.

**2. IMF PortWatch** (`PortWatchSupport`, on 073's `ArcGisQueryClient`):
- **How every layer is read:**
  - each layer goes through a `ScopedFetchContext` (scopes `points`, `ports` and `daily`), so the layers' metadata
    and edit dates never overwrite each other;
  - the daily tables are read with `date > DATE '{lastDate − 7 days}'` (a 7-day overlap, post-review fix, so IMF
    revisions of recent days produce versions), ordered by `date,ObjectId`, in pages of 1,000;
  - a read cut by the page cap stores a resume point (`cp:resume`, `ports:resume`), the day before the newest day it
    reached, and the next run continues that pass;
  - when a daily table's edit date is unchanged, no table query is sent and the run is `unchanged`.
- **`imf-portwatch-chokepoints`:**
  - the 28 points are cached in state and re-read only when their layer changes;
  - one record per chokepoint-day (`portid:YYYY-MM-DD`), with `eventTime` = `observedAt` = the day and the
    chokepoint point as geometry;
  - payload: the prompt's fields; rank field `nTotal`;
  - backfill 30 d, at most 10 pages per run; retention 3650 d (`series`).
- **`imf-portwatch-ports`:**
  - **Catalog:** 2,065 ports, read only when the layer's edit date changes. Ports missing from a complete read are
    tombstoned.
  - **Daily activity** (`portid:YYYY-MM-DD`): `name` was added to the payload so the source has one title field.
    Backfill 14 d, at most 15 pages per run.
  - **Param `ports`:** at most 200 ids, applied as `portid IN (…)`. Changing it starts the source over, including
    the live set of ports (`live:port`, `gone:port`), so narrowing the filter tombstones nothing.
  - **Retention:** 90 d for `port`, 3650 d for `port-activity`.
- **`imf-portwatch-disruptions`** (`kits/features/imf-portwatch.json`):
  - the field map is the prompt's list (`name` = `eventname`) plus `eventId`, `fromDate` and `toDate`. The kit
    requires its time fields to be payload targets;
  - `htmldescription` is not mapped;
  - `validFrom`/`validUntil` come from `fromdate`/`todate`;
  - absence tombstones;
  - `maxAllowableOffset` 0.005°, because some polygons are larger than 1.5 MB;
  - `FeatureSpecValidator.ID_PATTERN` now admits the reviewed `imf-portwatch-` prefix.
- **All three:** 12 h, 20/min and 200/day. Licence "IMF PortWatch terms (attribution: IMF PortWatch)". The
  `termsUrl` is `https://www.imf.org/en/About/copyright-and-terms` (see Deviations 5).

**3. `gfw-events`** (`GfwEventsAdapter`):
- **Credential:** `apiToken`, sent as `Authorization: Bearer`. A 401 or 403 is `CredentialsRejectedException`; a 429
  goes through the standard `Retry-After` backoff.
- **GET form:** `datasets[0]`, `start-date`, `end-date`, `limit=500` and `offset`.
- **With `bbox`:** the documented POST form (see Deviations 7).
- **Datasets:** the four event datasets confirmed on the key-concepts page:
  - `public-global-fishing-events:latest` (the default);
  - `public-global-encounters-events:latest`;
  - `public-global-loitering-events:latest`;
  - `public-global-port-visits-events:latest`.
- **Paging:** by `nextOffset` until `total`, at most 10 pages per run. A cursor is kept in state (`gfw:cursor`) and
  the next run resumes the same window, unless that window ended before the configured window would start (the
  source was disabled for a while): then a fresh window is read.
- **Record:**
  - kind by event type;
  - `eventTime` = `start`, `observedAt` = `end`, and `start`/`end` in the payload as interval fields;
  - a Point;
  - the prompt's payload fields.
- **Schedule and retention:** 1 h (minimum 30 min), 10/min and 300/day, retention 1825 d.
- **Licence:** exactly "CC BY-NC 4.0".

**4. `gfw-sar-detections`: not built.** GFW serves `public-global-sar-presence` only as gridded 4Wings data
(reports, tiles, bins, and per-cell interaction with `{detections, timestamp, vessel_id}`). There are no detection
ids or positions, so no official endpoint returns individual detections. This is recorded in the Source Catalog
and in the OpenAPI kind note.

**5. USCG LNM: not built** (a "planned" catalog row).
- **What exists:** the NAVCEN MSI page links "LNM Geojson Files" by notice category (`hazNav_1.geojson`,
  `marEvent_1.geojson`, `tmpChange_1.geojson`, …).
- **Why they do not qualify:**
  - they are chunked by a counter in `fileIndexNew.json`, which the page's script reads;
  - they are not split by district;
  - neither the page nor the LNM App User Guide documents them as an interface.

  The prompt's condition, a documented and stable per-district download, is not met. No PDF or HTML is parsed.

**6. Fixtures** (captured 2026-10-14 with an identifiable User-Agent; a README in each folder):
- **`nga-msi-warnings/`:** the active list trimmed to 30 warnings covering every rule, plus seven hand-made warnings:
  NM and meter circles, `BETWEEN … AND`, a DMS polygon without the space with a cancel, invalid minutes, and
  lettered areas and tracklines.
- **`imf-portwatch/`:** the five layer documents and one trimmed page of each layer.
- **`gfw-events/`:** synthetic, built from GFW's documented example responses, one per dataset plus the POST
  example. They need recapture with a real key.

**7. Spec and docs:**
- **OpenAPI:**
  - no new operations;
  - built notes on the nine kinds, with these payload changes: `maritime-warning` gains `title`; `port-activity`
    gains `name`; `port-disruption` lists `eventId, fromDate, toDate`; the GFW kinds list `start, end`;
  - `notice-to-mariners` and `sar-vessel-detection` are documented as not built;
  - a changelog entry was added.
- **Source Catalog:**
  - new section "Maritime intelligence (prompt 088)", covering the PortWatch weekly cadence and terms, the GFW licence
    and limits, the NGA User-Agent finding and the LNM and SAR decisions;
  - the three survey rows and their roadmap bullets are updated;
  - the `gfw-sar-detections` review row is updated.
- **Invariants:** `GfwEventsAdapterTest` was added to SRC-3 (429 backoff) and SRC-5 (the token is never in a URL,
  a trace or a recorded exchange).

## Files created / modified

- **Created, main:**
  - `source/adapters/maritime/`: `package-info`, `MaritimeSources`, `MsiCoordinateExtractor`, `NgaMsiParser`,
    `NgaMsiAdapter`, `PortWatchSupport`, `PortWatchChokepointsAdapter`, `PortWatchPortsAdapter`, `GfwEventsAdapter`;
  - resource `kits/features/imf-portwatch.json`.
- **Modified, main:**
  - `kit/feature/ArcGisQueryClient`: new public `cachedMeta(state)`, used to read a layer's edit date;
  - `kit/feature/FeatureSpecValidator`: the ID pattern.
- **Created, test:**
  - `maritime/{MsiCoordinateExtractorTest, NgaMsiParserTest, NgaMsiAdapterTest, PortWatchAdapterTest,
    GfwEventsAdapterTest}`;
  - `adapters/MaritimeIngestIT`;
  - fixtures `nga-msi-warnings/`, `imf-portwatch/`, `gfw-events/`.
- **Modified, test:**
  - `FamilyDefaultsTest`: four adapters added;
  - `FeatureSpecValidatorTest.shipped`: now also expects `imf-portwatch-disruptions`;
  - `WeatherIngestSupport`: the maritime base-URL property and the backoff list.
- **Docs:** `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Invariants.md`.

## Deviations from the prompt

1. **NGA polygon keyword.** The live texts say "IN AREA(S) BOUND BY" (38 warnings); `BOUNDED BY` and `AREA BOUNDED`
   occur in none. All three phrases are accepted. Circle distances also appear as words ("WITHIN THREE MILES OF"),
   so number words are read. A `WITHIN … OF TRACKLINE` phrase falls through to the trackline rule, because rule 2
   needs the position right after `OF`.
2. **Lettered NGA areas and tracklines** become MultiPolygon or MultiLineString (A., B., … with enough positions
   each). The prompt names only Polygon and LineString.
3. **NGA `title` payload field.** `titleField` needs a payload key, so the first line is stored as `title`.
4. **An empty NGA list does not tombstone** and is returned as not complete. NGA always has hundreds of warnings in
   force, so an empty array is treated as an upstream fault.
5. **PortWatch `termsUrl`.** The PortWatch FAQ's "Copyright & Usage" link (`imf.org/external/terms.htm`) now lands
   on an IMF 404 page. The current IMF terms page is recorded instead; its "Special Terms for Statistical Data" allow
   reuse with attribution.
6. **`imf-portwatch-ports` `observedAt`.** The prompt asks for RECEIVED on the port catalog, but a source's time
   base is fixed (Core-Data-Model §2, "never mixed within a source") and the same source carries daily measurements.
   Port records therefore use the layer's edit date as their publication time (UPSTREAM), as Core-Data-Model allows
   for catalogs. `port-activity` gains `name` for the source's title field.
7. **GFW POST body.** The documentation's custom-polygon field is `geometry`; `region` is for an existing region id
   (`region.dataset`/`region.id`, post-example5). The body sends `geometry` with the box as a Polygon.
8. **GFW page cap and cursor** (10 pages per run). These are not in the prompt; without them a worldwide 10-day
   window would exhaust the 300-a-day budget in one run.
9. **GFW rate limits are now published:** 50,000 a day and 1,500,000 a month per user. The prompt says they were
   not published. The prompt's budget is kept.
10. **Disruption payload** carries `eventId, fromDate, toDate` beyond the prompt's list, because the kit requires
    `idField` and the time fields to be payload targets. `observedAt` is RECEIVED, since the prompt names none.
11. **Retention classes** (the prompt names none):
    - warnings and GFW events: `event` (1825 d);
    - chokepoint and port activity: `series` (3650 d);
    - the port catalog: `reference` (90 d);
    - disruptions: 1825 d.

    `FamilyDefaultsTest` checks `imf-portwatch-ports` as `reference`.
12. **PortWatch page caps per run** (10 and 15 pages), with a resume. They keep each run within the 20/min budget;
    a first 14-day ports backfill therefore takes two runs.

## Issues found / open questions

1. **Owner question (current reads of daily records):** chokepoint-day and port-day records have no TTL, as the
   prompt specifies none. Every past day therefore stays "current" in `/v1/records` and tiles; for ports that is
   about 2,000 records a day. Should these kinds get a `recordTtl` (for example 21 days, OBSERVED)? That needs either
   a separate source per kind or per-kind TTL support in the descriptor.
2. **Owner question (USCG LNM):** NAVCEN does publish the LNM GeoJSON files openly (by category, with ETag and
   Last-Modified, refreshed every 15 minutes) but documents no interface. Should a `uscg-lnm-*` family be built on
   `fileIndexNew.json` anyway, or wait for documentation?
3. **Owner question (GFW volume and retention):** with no box, worldwide fishing events at 1825 d are a large
   store. Is `event` right, or should `fishing-event` be `bulk-event` (90 d)?
4. **Owner question (validator prefixes):** `FeatureSpecValidator.ID_PATTERN` now lists two reviewed prefixes
   (`faa-uas`, `imf-portwatch`). See 087's open question 4.
5. **Unverified (143):**
   - GFW with a real token: dataset ids, `vessel.flag`, the POST form, rate-limit headers;
   - a fresh NGA capture: the capture's newest `issueDate` is May 2024 although it was served on 2026-10-14 (a CDN
     cache hit);
   - a PortWatch weekly update observed end to end.
6. **US English:** all new text is US English (the workspace guard passed). `licence(...)` is an existing descriptor
   API. The extractor's regex also matches the upstream spelling `METRES`, which is upstream text.
7. **Instructions in data:** none. NGA warnings, ArcGIS documents, the PortWatch FAQ, the IMF terms, GFW's
   documentation (including its `llms.txt`, which describes the documentation's format) and the NAVCEN pages were
. **Framework (post-review):** the PortWatch adapters write `lastDate` and the resume point to adapter state
   during `fetch`, before the framework has written the records; a failed write would advance the window past days
   that were never stored. This is being fixed centrally (state writes staged until the fetch succeeds) by the owner
   of the fetch framework; the adapters were deliberately not changed to work around it.

## Tests written (not run)

- **`MsiCoordinateExtractorTest`:**
  - both formats with and without the space, and the hemispheres;
  - invalid minutes, seconds and degrees rejected;
  - each rule, including lettered areas and tracklines, word distances, `WITHIN … OF TRACKLINE` and `BETWEEN`
    without two positions;
  - circle radii checked by haversine;
  - the no-coordinate case and the unit conversions.
- **`NgaMsiParserTest`:** the real fixture (30 records, ids, `issueDate` as UTC, payload, geometry kinds, the
  no-geometry flag), the hand-made texts, cancel parsing, skips and the text cap, and a body without the array.
- **`NgaMsiAdapterTest`** (WireMock, real client):
  - Foresignal's User-Agent with no Referer and no Origin;
  - a tombstone on removal (retried once, then none);
  - an empty list tombstones nothing;
  - an unparseable body forgets the validators;
  - 304 handling and the descriptor.
- **`PortWatchAdapterTest`** (WireMock ArcGIS):
  - the chokepoint join and paging with `orderByFields=date,ObjectId`;
  - the metadata short-circuit sends no table query, then the incremental read with the 7-day overlap
    (`date > DATE '2026-09-27'`), which never moves `lastDate` back;
  - window arithmetic;
  - ports: catalog `observedAt` = edit date, activity joined to the ports, a missing port tombstoned, the port
    filter and its restart;
  - the point-table round trip and the descriptors;
  - the disruptions kit spec: validation, the mapping with `validFrom`/`validUntil` (an open event has none) and no
    HTML.
- **`GfwEventsAdapterTest`** (WireMock):
  - the Bearer header, dataset and date parameters, and paging to `total`;
  - the mapping of all four kinds;
  - the 10-page cap and resume;
  - the POST body with a box;
  - a missing token and a 401 are rejected credentials, and the token is absent from URLs, traces and exchanges;
  - a 429 backs the source off and nothing is sent;
  - parameters, skips and the descriptor.
- **`MaritimeIngestIT`:**
  - all five sources register disabled;
  - re-fetching the same chokepoint days adds no version;
  - a canceled NGA warning that leaves the list becomes a tombstone version, and the canceling warning stores
    `cancels`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (only the existing notes).
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "835 requests across 80 tag folders … covers all
  ops: True"; the collection is unchanged.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.
- **Scratch checks** (throwaway `main` classes in the scratchpad, outside the repository, with Gradle's JDK 21; no
  project tests were run):
  - the parser on the full 386-warning capture and both fixtures;
  - the extractor on every input the test asserts;
  - the PortWatch and GFW mappers on every fixture;
  - the four descriptors and the kit adapter through `DescriptorValidator` (no problems);
  - the spec through `KitSpecLoader` and `FeatureSpecValidator` (no problems);
  - the disruption page through `FeatureMapper`.

## Notes for the next prompt

1. **089 onward:**
   - `ArcGisQueryClient.cachedMeta(state)` exposes a layer's cached metadata (its edit date);
   - `ScopedFetchContext` is the pattern for several layers in one source;
   - `MaritimeSources.BASE_URL_PROPERTY` moves every maritime URL under a test base.
2. **097 checkpoint:**
   - first run of every 088 test;
   - `MaritimeIngestIT` uses `WeatherIngestSupport`, where the maritime property is registered;
   - `PortWatchAdapterTest` resets the scoped state keys (`points/`, `daily/`, `ports/` + `arcgis:*`) in
     `@BeforeEach`;
   - `FeatureSpecValidatorTest.shipped` now also expects `imf-portwatch-disruptions`.
3. **099 (derived layers):**
   - chokepoint transits: `chokepoint-transit`, one record per `portid:YYYY-MM-DD`;
   - port calls: `port-activity` (same ids) and `gfw-events` `port-visit`;
   - `port` gives the PortWatch port points;
   - `gfw-sar-detections` is not built, so `derived-dark-vessel-match` has no SAR input.
4. **109 (licence egress):** `gfw-events` carries licence "CC BY-NC 4.0"; the PortWatch sources carry "IMF PortWatch
   terms (attribution: IMF PortWatch)"; `nga-msi-warnings` is `US-PD`.
5. **143:** recapture per the fixture READMEs: GFW with a real token, a fresh NGA list, and a PortWatch weekly
   update.
6. **144 (Flyway):** no schema change. New state lives in `source_health.adapter_state` (`cp:*` including
   `cp:resume`, `ports:*` including `ports:resume`, `gfw:cursor`, the scoped `arcgis:*` keys, `live:*`).
7. **Owner decisions pending:** open questions 1–4 above, plus those carried from 012–087.

## Post-review fixes (2026-10-16)

Fixes commit: 50fc5d865f22ae72617302e0282a8e4b4d2eafd5 (`088: fix post-review defects`). Tests written, not run; `./gradlew compileJava
compileTestJava` succeeds.

1. **MEDIUM, antimeridian** (`MsiCoordinateExtractor`): circles were never wrapped into ±180° (the normalizer then
   rejected the whole record with `COORDINATES_OUT_OF_RANGE`), and polygons or tracklines from 179E to 179W came out
   about 358° wide. Rings and lines are now unwrapped with the existing `raster.geotiff.Antimeridian` helper and cut
   at ±180° into a MultiPolygon or MultiLineString; lettered areas keep their parts; a circle enclosing a pole is a
   polar cap. Tests: a 20-mile circle at 179-55E, a near-180 circle that stays one Polygon, a bounded area and lettered
   areas across 180°, a trackline across 180°, a polar circle (all valid, all inside ±180°). Checked with a scratch
   `main` against the compiled classes (outside the repository; no project test was run).
2. **MEDIUM, PortWatch revisions:** every pass over a changed daily table now re-reads a 7-day overlap
   (`lastDate − 7`), so IMF revisions of past days produce new versions; a pass cut by the page cap continues from a
   stored resume point (`cp:resume`, `ports:resume`) rather than restarting the overlap, so every run makes progress;
   `lastDate` never moves back. The "state committed before records are written" half of the finding is left to the
   framework fix (Issue 8). Tests: window arithmetic and state, a revised day re-emitted with its new value.
3. **LOW, `ports` filter:** changing it now also clears `live:port` and `gone:port`. Test: narrowing from all ports to
   one tombstones nothing.
4. **LOW, NGA `issueDate`:** an unparseable date skips the record but keeps the id in the live set. Test updated.
5. **LOW, GFW stale cursor:** a saved window that ended before the configured window starts is dropped. Test added.
6. **Test gap:** `GfwEventsAdapterTest.credentials` now asserts the traces and recorded exchanges are non-empty before
   checking them for the token.

This report was corrected for the above (the PortWatch window, the `ports` filter reset, the NGA live set, the GFW
cursor, the extractor's antimeridian handling and the new state keys).

Implementation commit: 562c42402ddebdb5528cf739d9587a50827e3308
