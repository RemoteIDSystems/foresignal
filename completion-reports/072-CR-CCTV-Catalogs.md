# 072-CR-CCTV-Catalogs

| | |
|---|---|
| Prompt | 072-CCTV-Catalogs.md |
| Status | Complete |
| Implementation commit | 2f7c42b443f2c505e565071fdd81649c6d33a003 |
| Date | 2026-10-13 |

## Summary

The `cctv-*` family is in. It registers 24 sources, one per reviewed pack, and every one is disabled by default. Each
source reads a public traffic-camera **catalog** from an official open-data endpoint and turns it into `camera`
records: location, published direction, a still-image or official stream URL, and a `frameAccess` hint. Frames are
never fetched, proxied or stored; clients load them from the upstream host.

**1. Family and shared pieces** (`ai.foresignal.source.adapters.cctv`):
- **Registration:** `CctvCatalogFamily` (`SourceAdapterFamily`) registers one `CctvPackAdapter` (`PollingAdapter`)
  per `CctvPack` of the static list `CctvPacks.REVIEWED`.
  - `CctvPacks.problems` validates the list at startup, and an invalid list stops Foresignal.
  - **Checks:** kebab-case `cctv-` ids, unique; https home and terms pages; https catalog URLs or an existing bundled
    resource; frame origins that are URL prefixes, each host listed in `hosts.json`; a bounding box; intervals and
    budget; a credential with its query parameter; no `User-Agent`, `Referer` or `Origin` among the extra headers.
  - **Test property:** `foresignal.test.adapters.cctv.base-url` rebases every catalog URL as `/{host}{path}?{query}`.
- **`CctvPack`:** id, name, provider, homepage, attribution, license, terms URL, catalog URLs or bundled resource,
  parser (`CctvPackParser`: `List<CameraEntry> parse(byte[] body, PackContext pc)`), allowed frame origins,
  bounding box, intervals, budget, optional credential and its query parameter, extra headers, response cap, and the
  maximum number of pages.
  - `PackContext` carries the pack, the bodies of further catalog URLs (Estonia's images document), skip counters by
    reason and the OGC next page.
- **`CameraRecordMapper`** (pure, shared by every pack):
  - **Identity:** `kind = camera`, `externalId` = the upstream camera id (`stationId:presetId` for Digitraffic),
    geometry `Point(lon, lat)`.
  - **Skips, counted by reason:** no coordinates, outside the pack's box (ported from GEV `isLikely*Coordinate`, plus
    null island and range checks), duplicate id (the first entry wins).
  - **Payload allowlist** (nulls omitted): `name, provider, region, road, direction, headingDeg, status, imageUrl,
    videoUrl, refreshSeconds, frameAccess, frameInsecure`.
  - **Frame URLs:** each must be an absolute http(s) URL without user info that starts with one of the pack's allowed
    origins. The check compares canonical URLs (lowercase scheme and host, no default port, no fragment). A URL that
    fails is dropped; the camera is kept with `status` unchanged and no `frameAccess`.
  - **`frameAccess`:** the `hosts.json` hint of the kept image URL's host, else the video URL's.
  - **`frameInsecure: true`:** an `http:` frame URL (Warendorf only). Calgary's host serves https, so the parser
    upgrades its URLs.
- **`Compass`:** whole-field compass text only (`N`, `N-E`, `Northeast`, `Westbound`, `WB`, 16-point
  abbreviations). There is no free-text inference and no fallback. GEV's `fallbackHeadingFromId` and its
  pitch/fov/range/mount priors are not ported.
- **Frame-access hints:** `src/main/resources/cctv/hosts.json` lists 16 hosts as `{access, cors, notes}`, from the
  probe described under Fixtures. `CctvFrameHosts` loads it.
  - **`browser`:** `webcams.transport.nsw.gov.au` (it answers non-browser clients with 200 `text/html`).
  - **`direct`:** every other host.
  - **Unverified:** `webcam.warendorf.de` did not answer the probe. `www.kreis-warendorf.de` is allowed by the
    bundled pack but no camera uses it.
  - No `Referer`/`Origin` requirements: none is documented. No User-Agent override exists anywhere in the package.
- **`ai.foresignal.source.support.AbsenceTombstoner`** (new package, for 074 and 075 too):
  - **Rule:** an id missing from **two consecutive** complete fetches is tombstoned.
  - **State:** `absence:ids` (the last complete set) and `absence:missed` (ids missed once). Each is sorted, and
    gzipped and base64-encoded behind `gz:` above 64 KB. An optional scope suffix keeps independent catalogs apart.
  - **Outcomes:** the caller passes `COMPLETE`, `NOT_MODIFIED`, `TRUNCATED` or `PARSE_ERROR`. Only `COMPLETE` moves
    the state.
- **Descriptor** (`CctvDescriptors`, all packs):
  - **History:** STANDARD, `IngestPolicy.All(null)`, `observedAtMode` RECEIVED, `ttlAnchor` RECEIVED, no record TTL;
    no significant, volatile or embed fields.
  - **Display and search:** `zeroResultsNormal` false; search `name, road, region`; tiles `name, direction, status,
    frameAccess`; title `name`; kind display `{name}` with the sprite's `camera` icon.
  - **fieldSpecs:** every key, with `headingDeg` in `deg` and `refreshSeconds` in `s`.
  - **Retention:** 90 d (the `reference` class).
  - **Fetching:** optional param `bbox`; `followRedirects(false)`; 30 s fetch timeout.

**2. Packs** (all endpoints re-checked 2026-10-13):

| Source | Catalog | Interval, budget, cap | Notes |
|---|---|---|---|
| `cctv-tfl` | `api.tfl.gov.uk/Place/Type/JamCam` | 60 min, 2/min 200/day | Optional `appKey` → `app_key`. `available=true` → `active`, else `offline`. Image on the `jamcams.tfl.gov.uk` S3 bucket; MP4 `videoUrl` ignored. |
| `cctv-caltrans-d1` … `d12` | `cwwp2.dot.ca.gov/data/d{N}/cctv/cctvStatusD{NN}.json` | 15 min, 8 MB | Id = the `TVnnn` code of `locationName`, else the image directory. Stream on `wzmedia.dot.ca.gov`. `refreshSeconds` = minutes × 60. |
| `cctv-ontario511` | `511on.ca/api/v2/get/cameras?format=json&lang=en` | 15 min, 2/min 200/day | **Required** `key` (query). Views on `511on.ca` or `*.traveliq.co` are rebuilt on `https://511on.ca/map/Cctv/{viewId}`. |
| `cctv-digitraffic` | `tie.digitraffic.fi/api/weathercam/v1/stations` | 15 min | `Digitraffic-User: Foresignal/<version>` (from `ForesignalVersion`). Presets of `GATHERING` stations only. |
| `cctv-drivebc` | `www.drivebc.ca/api/webcams/` | 15 min | `orientation` → heading. A GEV-filtered image credit becomes `provider`. |
| `cctv-nsw` | `data.livetraffic.com/cameras/traffic-cam.json` | 15 min | `frameAccess: browser`. |
| `cctv-calgary` | `data.calgary.ca/resource/k7p9-kppz.json?$limit=5000` | 15 min | Id = the frame file (`loc74`); http upgraded to https. |
| `cctv-austin` | `data.austintexas.gov/resource/b4k4-adkb.json?$limit=5000` | 15 min | `TURNED_ON` only. |
| `cctv-deldot` | `tmc.deldot.gov/json/videocamera.json` | 15 min | HLS only, pinned to `video.deldot.gov/live/…/playlist.m3u8`. |
| `cctv-vegvesen` | the OGC API Features `CctvSimple` items URL of the prompt | 15 min, 8 MB, 5 pages | Follows `rel=next` on the same endpoint. HLS only for the agency's manifest path. |
| `cctv-estonia` | Tarktee DATEX II locations + images | 15 min, 4/min 400/day | Hardened StAX (`CapParser.secureInputFactory`), joined by location id. |
| `cctv-tallinn`, `cctv-warendorf` | `classpath:cctv/packs/{tallinn,warendorf}.json` | 24 h, no request | GEV packs copied unchanged, with a sibling `NOTICE`. |

**Not ported:** TxDOT (an internal ITS map endpoint) and GEV's Shinjuku pack.

**3. Fetch** (`CctvPackAdapter`):
- **How each pack is read:**
  - a single catalog URL uses `getConditional(uri, "catalog", headers)`, and a 304 is `unchanged()`;
  - Estonia requests both documents;
  - Vegvesen pages up to 5 times, and an unfinished catalog is returned partial (`FetchResult.of`);
  - a bundled pack reads its resource.
- **Credentials:** added as query parameters, and the client redacts them. A missing required key is an
  `AdapterException` before any request.
- **Failures:** a 3xx (`redirect refused`), another non-200 status, a truncated body or a document that does not parse
  is an `UpstreamFormatException`, and the validators are forgotten. Error statuses (Ontario `400 Invalid Key`) are
  `UpstreamHttpException` from the client.
- **Tombstones:** only after a complete, non-empty fetch.
- **Counter:** skips go to `foresignal.cctv.skipped{source, reason}`.

**4. Fixtures** (`src/test/resources/fixtures/cctv-*/`, each with a README giving the URL, command, headers,
trimming and the frame-access probe):
- **Captured** 2026-10-13 17:17 UTC with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT"` (plus
  `Digitraffic-User` for Digitraffic), each trimmed to ≤ 25 cameras with the envelope kept: TfL, Caltrans D4,
  Digitraffic, DriveBC, NSW, Calgary, Austin, DelDOT, Vegvesen, and Estonia (both documents).
- **Kept on purpose:** offline cameras, NSW's real off-host frame URL, a `REMOVED_TEMPORARILY` station and the
  non-`TURNED_ON` Austin rows.
- **Ontario 511 is `synthetic: true`:** the published sample plus hand-made rows. Recapture it with a real key.
- **Hand-made edge cases:**
  - `cctv-caltrans-d4/edge-cases.json` (wrapped JSON);
  - `cctv-nsw/edge-cases.geojson` (GeoJSON);
  - `cctv-tfl/edge-cases.json` (JSON array);
  - `cctv-estonia/edge-*.xml` (XML), plus `xxe-locations.xml`.

  They cover no coordinates, out of bounds, off-host URLs, out of service, duplicate ids and missing ids.

**5. Docs:**
- **`docs/Source-Catalog.md`:**
  - the `cctv-*` row is replaced by one row per pack (the 12 Caltrans districts share one row);
  - a "Camera catalogs (prompt 072)" note: frames never stored, clients fetch, see `frameAccess`;
  - the TxDOT exclusion, also named in "External provider ideas";
  - the roadmap line marks 072.
- **`docs/Adapter-Guide.md` §21:** "Catalog sources and client-fetched media": time base, the shared mapper, allowed
  origins, the frame-access probe, `AbsenceTombstoner`, query credentials and bundled catalogs.
- **`docs/OpenAPI.json`:** no operation change. The `camera` kind note is expanded and a changelog entry is added.
- **`docs/Invariants.md`:** `CctvPackAdapterTest` added to SRC-5.
- **`THIRD_PARTY_NOTICES.md`:** six GEV rows (`server/providers/cctv/*` and the two config packs), 072 added to the
  prompt list, and a "not ported" paragraph.

## Files created / modified

- **Created, main:**
  - `source/adapters/cctv/`: `package-info`, `CameraEntry`, `CctvPackParser`, `PackContext`, `CctvPack`,
    `CctvPacks`, `CctvJsonParsers`, `DatexCameraParser`, `Compass`, `CameraRecordMapper`, `CctvFrameHosts`,
    `CctvDescriptors`, `CctvPackAdapter`, `CctvCatalogFamily`;
  - `source/support/`: `package-info`, `AbsenceTombstoner`;
  - resources: `cctv/hosts.json`, `cctv/packs/{tallinn.json, warendorf.json, NOTICE}`.
- **Created, test:**
  - `source/adapters/cctv/`: `CctvPackParsersTest`, `CameraRecordMapperTest`, `CctvPackAdapterTest`,
    `CctvFamilyRegistryTest`, `CctvIngestIT`;
  - `source/support/AbsenceTombstonerTest`;
  - 11 fixture directories.
- **Modified, test:**
  - `testsupport/weather/WeatherIngestSupport`: the CCTV base-URL property, and `cctv-caltrans-d4` added to the
    backoff list;
  - `retention/FamilyDefaultsTest`: `CctvPackAdapter` as `reference`.
- **Docs:** `docs/Source-Catalog.md`, `docs/Adapter-Guide.md`, `docs/OpenAPI.json`, `docs/Invariants.md`,
  `THIRD_PARTY_NOTICES.md`. The Postman collection was regenerated with no change.

## Deviations from the prompt

1. **No `headingDeg` for Caltrans.** The CWWP2 field description says `direction` is the "direction of travel cctv is
   located on", which is the road, not the camera's facing. The text is kept in `direction`; turning it into a
   heading would be the kind of synthetic value the prompt forbids. GEV did convert it.
2. **TfL `direction` only when the `view` label is wholly a compass direction.** Most labels are free text ("Zoom
   west - Deptford Bridge", "Piccadilly Circus"); in the captured fixture 1 of 24 is a bare direction.
3. **Out-of-service cameras are kept as `offline`** (TfL, Caltrans, Ontario without an enabled view, Digitraffic
   presets out of collection, DriveBC `is_on=false`, DelDOT, Vegvesen faults, Estonia without an image). Entries an
   upstream marks as not a camera at all are skipped and counted `not-listed`: Austin non-`TURNED_ON`, Digitraffic
   stations that are not `GATHERING`, DriveBC `should_appear=false`. GEV dropped all of them; the prompt's
   `status (active|offline)` needs the offline ones.
4. **Allowed origins are URL prefixes**, not bare origins. Some pins are path prefixes: TfL's S3 bucket path, DelDOT
   `/live/`, Vegvesen `/api/images/` and `/public/`. A bare origin would let any object on
   `s3-eu-west-1.amazonaws.com` through.
5. **Bundled packs keep a heading only when GEV marks it `headingConfidence: high`** (curated). The low-confidence
   values are priors, not published directions.
6. **Absence guards beyond the prompt:** an empty catalog and an unfinished paginated catalog tombstone nothing. A
   tombstone needs a complete, non-empty fetch.
7. **Ontario fixture is synthetic** (no key; Conventions "No credentials during construction"). Its frame host was
   probed without a key and serves images. Ontario's 400 surfaces as `UpstreamHttpException`, as the prompt asks.
8. **The DriveBC `credit`** passes through GEV's `driveBcImageCredit` filter before it becomes `provider`.
   Operational notes such as "relies on solar power" are not credits.
9. **Spelling:** the guide section is "Catalog sources and client-fetched media" and identifiers use `license`
   (project rule: US English). The descriptor wire field stays `licence`.
10. **Per-pack choices not in the prompt:**
    - Estonia's budget is 4/min and 400/day, because each run makes two requests;
    - Caltrans and Vegvesen allow 8 MB; the other packs 4 MB;
    - every catalog refuses redirects (all captures answered 200 without one);
    - boxes were added for TfL (51.2–51.8 N, 0.6 W–0.4 E), Caltrans (California) and Warendorf, which GEV did not
      check.

## Issues found / open questions

1. **Estonia's `imageUrl` changes on every new image.** The DATEX file name is timestamped (`987_202610132006.jpg`),
   and Tarktee publishes no stable "latest" URL. Each run with fresh images therefore writes a new version per
   camera: up to about 176 × 96 a day if enabled. I kept `imageUrl` significant, as the prompt asks for no volatile
   fields.
2. **Unverified frame hosts:** `webcam.warendorf.de` did not answer the probe; `www.kreis-warendorf.de` is unused.
3. **TfL terms page:** `tfl.gov.uk/corporate/terms-and-conditions/transport-data-service` answered 403 to a
   non-browser client. The pack uses the verified `https://api-portal.tfl.gov.uk/` for both homepage and terms.
4. **Licenses:**
   - DelDOT, Transpordiamet, Tallinn and Warendorf publish none that I could find, so each is "operator terms";
   - Ontario 511 is "Ontario 511 terms of use" (its disclaimer page names no licence);
   - Austin is Public Domain per its Socrata metadata.
5. **`docs/Source-Catalog.html`** was not regenerated (as in 070 and 071).
6. **US English check:** I ran `check_us_english.py` on the new files and `docs/Adapter-Guide.md`.
   - The only hits are allowed exceptions:
     - license names ("Open Government Licence – …");
     - the NLOD attribution text;
     - the `licence` wire field;
     - a URL;
     - existing Adapter-Guide lines from 067 ("cancellation").
   - The workspace guard `test_us_english.py` ran and printed OK, but it scans only the MeshDTN repositories, not
     this one.
7. **Instructions in data:** none. The GEV files, the captured catalogs, the Ontario and Caltrans documentation pages
   and the packs were read; nothing addressed an agent.

**Owner questions (non-blocking):**
1. Should Estonia's `imageUrl` be declared volatile (or the pack use a LATEST policy) to avoid a version per image?
2. Is "no heading from Caltrans' road direction" right, or should travel direction be published as a heading?
3. Should a source-level `licence` be set for the "operator terms" packs once their operators confirm terms?

## Tests written (not run)

- **`CctvPackParsersTest`:**
  - each real fixture: count, offline count, image/video/heading counts and spot-checked payloads for TfL, Caltrans,
    Digitraffic, DriveBC, NSW, Calgary, Austin, DelDOT, Vegvesen and Estonia, plus synthetic Ontario;
  - edge cases (skips by reason, off-host URLs dropped with the camera kept, `:443` canonicalized, id fallbacks);
  - XXE never resolved; bundled packs (curated headings only, Warendorf `frameInsecure`);
  - no synthetic heading in any pack; the `bbox` filter; format errors for every pack.
- **`CameraRecordMapperTest`:**
  - allowlist plus `FieldSpecContract` on every fixture; null omission;
  - `SnapshotContract.assertStableStaticHash` across two parses of each fixture;
  - frame-URL checks and canonical form; `frameAccess` from the image, else the video host;
  - heading normalization and bounds; the compass table; duplicates.
- **`AbsenceTombstonerTest`:**
  - two consecutive misses; reappearance; 304, truncation and parse error leave the state alone;
  - first fetch and blank ids; scopes; round trip above 64 KB (gzip); a corrupt value; the tombstone records.
- **`CctvPackAdapterTest`** (`@IntegrationTest`, WireMock, real `UpstreamClient`):
  - every descriptor validates;
  - conditional GET, then 304 → `notModified`, with Foresignal's User-Agent;
  - `Digitraffic-User` and gzip;
  - TfL `app_key` sent and redacted from traces, exchanges and logs;
  - Ontario: no key → no request, then `400 Invalid Key` → `UpstreamHttpException`, then success;
  - redirect refused and never followed; HTML body → validators forgotten; 204; truncation leaves the absence state;
  - Vegvesen paging, the 5-page limit, an off-host next link;
  - Estonia's two documents; bundled packs make no request;
  - absence tombstone after two misses, the skip counter, `bbox`; an empty catalog tombstones nothing;
  - a source scan for frame fetching and User-Agent overrides.
- **`CctvFamilyRegistryTest`** (shares `WeatherIngestSupport`'s context):
  - 24 sources registered, unique, with licence and terms, STANDARD/ALL/RECEIVED, disabled by default;
  - schedules and caps; Ontario 409 `CREDENTIALS_REQUIRED`;
  - `problems` cases; every frame host hinted.
- **`CctvIngestIT`:** enable `cctv-caltrans-d4` → 3 FIRST versions; identical re-fetch → none; a camera removed
  twice → exactly one tombstone, and none on later runs.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing unchecked-operations note).
- `./gradlew lintOpenApi`: valid; 71 warnings, the same count as before.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests across 80 tag folders, covers all ops;
  the collection is unchanged.
- No project tests were run, because this is a feature prompt.

## Notes for the next prompt

1. **073 (OGC kit):** `CctvJsonParsers.vegvesen` is pure and reports `rel=next` through `PackContext.nextPage`;
   `CctvPackAdapter.readPages` holds the same-endpoint and 5-page rules. Moving Vegvesen onto the kit means
   replacing those two pieces.
2. **074 and 075 (catalogs):**
   - reuse `ai.foresignal.source.support.AbsenceTombstoner`: call it only with a real `Outcome`, and skip it for an
     empty set;
   - use the `CctvPacks` pattern: a reviewed list validated at startup, allowed origins as URL prefixes, and
     `/{host}{path}` test rebasing.
3. **097 checkpoint:**
   - first run of every 072 test;
   - `CctvIngestIT` and `CctvFamilyRegistryTest` share `WeatherIngestSupport`'s context (one new property,
     `foresignal.test.adapters.cctv.base-url`);
   - check the hand-computed counts in `CctvPackParsersTest`;
   - check whether the XXE test throws or yields an empty name on the JDK reader;
   - `FamilyDefaultsTest` still fails on the unclassified 062–064 adapters only.
4. **143:**
   - recapture per the fixture READMEs, Ontario with a real key (`FORESIGNAL_VERIFY_*`);
   - re-probe the frame hosts, Warendorf especially;
   - Estonia version churn if enabled (Issue 1).
5. **144 (Flyway):** no schema change. Adapter state of `cctv-*`: `etag:catalog`, `lastmod:catalog`, `absence:ids`,
   `absence:missed`.
6. **Owner decisions pending:** the three questions above, plus those carried from 012–071.
