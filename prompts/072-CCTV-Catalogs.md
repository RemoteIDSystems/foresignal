# 072 — CCTV Catalogs

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/072-CR-CCTV-Catalogs.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/071-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` row `cctv-*` (God's Eye View ports) and the "Excluded by charter" list
- `docs/Core-Data-Model.md` §2 (time contract: "Catalogue and reference" row), §3 (descriptor fields, adapter rules 7–13, limits), §5.3 ("Everything else" defaults)
- `docs/Architecture.md` §4.2 (adapter rules, adapter families, GEV porting rule)
- `docs/Adapter-Guide.md` (prompt 011): package layout, fixture procedure, fieldSpecs contract test
- GEV reference (MIT, portable): `~/Documents/GitLab/gods-eye-view/server/providers/cctv/sources.js`, `normalize.js`, `constants.js`, `catalog.js`, `config/cctv_sources.tallinn.json`, `config/cctv_sources.warendorf.json`
- Upstream docs: TfL Unified API (`api.tfl.gov.uk`), Caltrans CWWP2 (`cwwp2.dot.ca.gov/documentation`), Ontario 511 developer docs (`511on.ca/developers/doc`), Digitraffic weathercam (`www.digitraffic.fi/en/road-traffic/`), DriveBC, Transport for NSW Open Data, Socrata SODA (`dev.socrata.com`), Statens vegvesen Atlas OGC API

## Objective

Ingest public traffic-camera **catalogues** (location, direction, image or stream URL, access hints) from official open-data endpoints as one source per pack. Frames are **never fetched, proxied or stored** by Foresignal; clients load them directly from the upstream host using the URL and the `frameAccess` hint in the record. Port the God's Eye View loaders, minus anything that spoofs a browser or relies on unofficial web-app endpoints.

## Tasks

### 1. Family and shared pieces (`ai.foresignal.source.adapters.cctv`)

- `CctvCatalogFamily implements SourceAdapterFamily` returns one `CctvPackAdapter implements PollingAdapter` per entry of a static, code-reviewed pack list (`CctvPacks`). Pack lists are never caller-supplied.
- Each pack has: id, name, provider, homepage, attribution, licence, termsUrl, catalogue URL(s), parser (`CctvPackParser` functional interface: `List<CameraEntry> parse(byte[] body, PackContext pc)`), allowed frame-URL origins, default interval, budget, optional credential and extra headers.
- `CameraEntry` → `NormalizedRecord` via one shared `CameraRecordMapper` (pure):
  - `kind = camera`; `externalId` = upstream camera id (or `stationId:presetId` for Digitraffic); geometry `Point(lon, lat)`; reject coordinates outside the pack's bounding box (port GEV `isLikely*Coordinate` checks) and count them.
  - Payload allowlist (omit nulls, no counters, no upstream timestamps): `name, provider, region, road, direction` (compass text as published), `headingDeg` (**only** when the upstream publishes a direction; do **not** port GEV `fallbackHeadingFromId` or the pitch/fov/range/mount rendering hints, which are synthetic), `status` (`active|offline`), `imageUrl`, `videoUrl` (only an official HLS/stream URL), `refreshSeconds` (if published), `frameAccess` (`direct|browser|key`), `frameInsecure` (true for an `http:` frame URL that has no https equivalent).
  - Every `imageUrl`/`videoUrl` must parse as an absolute URL whose origin is in the pack's allowed origins; otherwise drop the URL and keep the camera (`status` unchanged, `frameAccess` absent).
- **Frame-access hints:** a classpath resource `cctv/hosts.json` maps frame host → `{access, cors, notes}`. Populate it during fixture capture: for each pack, `HEAD` one frame with Foresignal's User-Agent and no `Referer`. `image/*` → `direct`; an HTML bot wall or 403 → `browser` (works only from a real browser `<img>`); key-gated hosts → `key`. Record `Referer`/`Origin` requirements here only when the upstream documents them. **Never** add a User-Agent override (GEV's `NSW_IMAGE_USER_AGENT` browser string is not ported).
  - Verified 2026-10: `webcams.transport.nsw.gov.au` returns `text/html` to non-browser clients → `browser`; DriveBC `/images/{id}.jpg` and TfL JamCam S3 frames return `image/jpeg` → `direct`.
- **Absence tombstones:** add `ai.foresignal.source.support.AbsenceTombstoner` (reused by prompts 074 and 075). It keeps the id set of the last complete fetch in `AdapterStateStore` (key `absence:ids`, sorted, gzip+base64 above 64 KB) and emits a tombstone for an id missing from **two consecutive** complete fetches. It never tombstones on a 304, a truncated body or a parse error.
- Descriptor (all packs): `historyTier=STANDARD`, policy ALL, `observedAtMode=RECEIVED` (catalogues have no per-camera update time), `ttlAnchor=RECEIVED`, `recordTtl=null`, `zeroResultsNormal=false`, `searchFields=name,road,region`, `tileProperties=name,direction,status,frameAccess`, `titleField=name`, `significantFields` empty (all payload keys significant), `volatileFields` empty, `embedFields` none, `fieldSpecs` for `headingDeg` (`deg`) and `refreshSeconds` (`s`) if the prompt 008 fields exist. Optional param `bbox` (type bbox) filters cameras after parsing. Every pack ships **disabled**.

### 2. Packs

Defaults: interval 15 min (min 10 min), budget 2/min, 200/day, unless stated. Use conditional GET wherever the response carries an ETag or Last-Modified.

| Source id | Catalogue | Notes (verified 2026-10) |
|---|---|---|
| `cctv-tfl` | `https://api.tfl.gov.uk/Place/Type/JamCam` | Keyless; optional credential `appKey` sent as `app_key` query param (redacted). 1.1 MB, no ETag, `Cache-Control max-age=43200` → default interval 60 min. Keep `available=true` → `active`; image from `additionalProperties[key=imageUrl]`; ignore `videoUrl` (MP4 clips, not a stream). Licence "Powered by TfL Open Data" (OGL v2 based). |
| `cctv-caltrans-d1` … `cctv-caltrans-d12` | `https://cwwp2.dot.ca.gov/data/d{N}/cctv/cctvStatusD{NN}.json` | One source per district (12 sources). ETag + Last-Modified. Keep `inService=true` → `active`; image `imageData.static.currentImageURL` on `cwwp2.dot.ca.gov` only; stream `imageData.streamingVideoURL` when present. Up to 2.4 MB per district: set `maxResponseBytes` 8 MB. |
| `cctv-ontario511` | `https://511on.ca/api/v2/get/cameras?format=json&lang=en` | **Now requires a developer key**: without one the server answers 400 `Invalid Key`. Required credential `key` (query param, redacted). Published throttle 10 calls / 60 s → budget 2/min, 200/day. Image origin `https://511on.ca/map/Cctv/`. |
| `cctv-digitraffic` | `https://tie.digitraffic.fi/api/weathercam/v1/stations` | Requires gzip (406 without `Accept-Encoding: gzip`, which `UpstreamClient` sends) and a `Digitraffic-User: Foresignal/<version>` header. ETag + Last-Modified. One record per preset of a station with `collectionStatus=GATHERING`; image `https://weathercam.digitraffic.fi/{presetId}.jpg`. Licence CC BY 4.0, attribution "Fintraffic / digitraffic.fi". |
| `cctv-drivebc` | `https://www.drivebc.ca/api/webcams/` | ETag. Location from GeoJSON `location.coordinates`; image `https://www.drivebc.ca/images/{id}.jpg`; direction from `orientation`; keep the upstream `credit` in `provider` when present. Licence Open Government Licence – British Columbia. |
| `cctv-nsw` | `https://data.livetraffic.com/cameras/traffic-cam.json` | GeoJSON, ETag + Last-Modified, keyless. Fields `title`, `view`, `direction`, `region`, `href`. `frameAccess=browser`. Licence CC BY 4.0 (Transport for NSW). |
| `cctv-calgary` | `https://data.calgary.ca/resource/k7p9-kppz.json?$limit=5000` | Socrata SODA, ETag. Frames on `trafficcam.calgary.ca`. Licence Open Government Licence – City of Calgary. |
| `cctv-austin` | `https://data.austintexas.gov/resource/b4k4-adkb.json?$limit=5000` | Use the SODA resource endpoint, not GEV's `rows.json` download. Keep `camera_status=TURNED_ON` only; frames `https://cctv.austinmobility.io/image/{id}.jpg`. |
| `cctv-deldot` | `https://tmc.deldot.gov/json/videocamera.json` | ETag + Last-Modified. Port GEV's DelDOT loader. |
| `cctv-vegvesen` | `https://ogckart-sn1.atlas.vegvesen.no/ogc/features/v1/collections/datex_3_1:CctvSimple/items?f=application/geo%2Bjson&limit=5000` | OGC API Features (follow `rel=next` links, max 5 pages). Image from `stillImageUrl` on `kamera.atlas.vegvesen.no`; `videoUrl` only when `videoEncodingStandard=hls` and it equals `https://kamera.vegvesen.no/public/{cameraId}/manifest.m3u8`. Licence NLOD. Prompt 073 may later move this onto the OGC kit; keep the parser pure so that is a small change. |
| `cctv-estonia` | `https://tarktee.transpordiamet.ee/api/v1/datex/roadCameraLocations` + `.../roadCameraImages` | DATEX II XML: parse with a hardened StAX reader (no DTD, no external entities). Join locations and images by camera id. |
| `cctv-tallinn`, `cctv-warendorf` | Bundled GEV packs → `src/main/resources/cctv/packs/{tallinn,warendorf}.json` | Static catalogues: no network request. `fetch` reads the resource; interval 24 h. Warendorf has `http:` frames → `frameInsecure=true`. Keep the GEV origin header in the JSON files' sibling `NOTICE`. |

**Not ported:** TxDOT. GEV calls `its.txdot.gov/its/DistrictIts/GetCctvStatusListByDistrict`, the ITS web map's internal endpoint, which has no published terms (charter: no unofficial web-map endpoints). Add it to the Source Catalog "Excluded" list with that reason. Also skip GEV's Shinjuku pack (not in the catalogue).

### 3. Port notes

- Each ported Java file carries a header naming the GEV origin file(s) and the MIT notice. Add a `THIRD_PARTY_NOTICES.md` entry for `server/providers/cctv/*` and the two config packs.
- Drop GEV's frame proxy (`media.js`, `stream.js`, `range.js`), ground heights, the source cap/round-robin (`cap.js`) and distance prioritisation. Foresignal stores every camera of an enabled pack; operators trim with `bbox`.

### 4. Fixtures (`src/test/resources/fixtures/cctv-*/`)

- Capture one real response per pack with `curl -s --compressed -A "Foresignal-fixture/<version>"` (add `Digitraffic-User` for Digitraffic; use your own key for Ontario and strip it from the README). Trim each to ≤ 25 cameras keeping the envelope. Record the capture date, URL and the frame-access probe results in `README.md`.
- Edge cases per family of formats: camera without coordinates, out-of-bbox coordinates, off-host image URL, `inService=false`, duplicate id.

### 5. Docs

- `docs/Source-Catalog.md`: replace the `cctv-*` row with one row per pack (tier STANDARD, policy ALL, no TTL, `observedAtMode=RECEIVED`, licence) and add the TxDOT exclusion. State "frames never stored; clients fetch; see `frameAccess`".
- `docs/Adapter-Guide.md`: a short "Catalogue sources and client-fetched media" section (frame-access probe, allowed origins, `AbsenceTombstoner`).
- No API change.

## Tests to write (do not run)

- `CctvPackParsersTest`: each pack's real fixture parses to the expected count with spot-checked fields; edge cases (skips counted, off-host URLs dropped, synthetic heading never produced, `http:` frames flagged).
- `CameraRecordMapperTest`: payload allowlist, null omission, `SnapshotContract.assertStableStaticHash` across two parses of the same fixture, fieldSpecs contract (no undeclared keys).
- `AbsenceTombstonerTest`: tombstone only after two consecutive complete misses; no tombstone on 304, truncation or parse failure; state round trip above 64 KB.
- `CctvPackAdapterTest` (WireMock): URL and headers per pack (`Digitraffic-User`, TfL `app_key` redacted in traces, Ontario `key` required and `400 Invalid Key` surfacing as `UpstreamHttpException`), conditional GET 304 → notModified, User-Agent is Foresignal's (never a browser string).
- `CctvFamilyRegistryTest`: every pack registers, ids unique, all disabled by default, Ontario requires credentials before enable (409 `CREDENTIALS_REQUIRED`).
- `CctvIngestIT` (`@IntegrationTest` + WireMock): enable `cctv-caltrans-d4` → N records; remove one camera twice → one tombstone; identical re-fetch → no new versions.

## Acceptance criteria

- One source per pack in the registry, all disabled, with licence and terms set.
- No code path fetches, proxies or stores a camera frame; no User-Agent override exists in the package.
- TxDOT is documented as excluded.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `072: CCTV Catalogs`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/072-CR-CCTV-Catalogs.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `072: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
