# 064-CR-Space-Adapters

| | |
|---|---|
| Prompt | 064-Space-Adapters.md |
| Status | Complete |
| Implementation commit | c5232536277c7fd20ee64db377831b11d3c4ee77 |
| Date | 2026-10-13 |

## Summary

Foresignal now has five space sources, Orekit-predicted satellite passes, and a CelesTrak-compatible element
feed for Gpredict, Look4Sat and SatDump:
- `celestrak-gp` (GP element sets) and `celestrak-socrates` (conjunctions), both under CelesTrak's 2026 usage
  policy;
- `launch-library` (launches);
- `spacetrack-tip` (re-entry predictions; optional, LOCAL_ONLY);
- `satellite-passes` (DERIVED predicted passes over places), plus predicted passes in SatDump stations;
- `/compat/celestrak/gp.php` and its NORAD alias. `getCelestrakGp` and `getCelestrakGpAlias` are `implemented`
  and off `pending-operations.txt`.

Fixtures were captured live on 2026-10-13: four CelesTrak requests in all, each a different query, and one Launch
Library request. The Space-Track fixture is synthetic, because there is no account.

**1. `celestrak-gp`** (`ai.foresignal.source.adapters.celestrak`).
- **Descriptor:**
  - kind `satellite`;
  - params `group` (default `active`, must be on the allowlist), `memberGroups` (string-list restricted to a
    reviewed allowlist: the 47 group links of `celestrak.org/NORAD/elements/` on 2026-10-13, without `iridium-NEXT`,
    which fails `^[a-z0-9-]+$`, and without `active` for member groups) and `format` (`csv` default, `json`);
  - interval 2 h minimum and default; budget 3/min, 48/day; 32 MB;
  - STANDARD, ALL; TTL 3 d anchored at receipt; UPSTREAM time base, `observedAt = EPOCH`; no geometry;
  - title `OBJECT_NAME`; search `OBJECT_NAME, OBJECT_ID`; `zeroResultsNormal` false;
  - licence "CelesTrak (data from 18th SDS via Space-Track)".
- **Fetch** (strictly sequential):
  - one `getConditional` of `GROUP={group}&FORMAT={format}`;
  - then at most two member groups whose membership is older than 24 h, never-fetched groups first, then the
    oldest (`Memberships`: `members:{group}` delta-encoded, `membersAt:{group}`);
  - records carry the OMM keys verbatim (numbers as numbers) plus sorted `groups`, with `externalId` =
    `NORAD_CAT_ID` (up to 9 digits).
- **Policy** (`CelestrakPolicy`):
  - a 304, or a 403 whose body says "has not updated since your last successful download", is `unchanged()`, with
    no backoff;
  - any other non-200 throws `UpstreamHaltException` ("CelesTrak returned {status}; automatic retries stopped per
    CelesTrak usage policy — investigate, then use fetch-now"). That includes a redirect: the descriptor sets the
    new `followRedirects(false)`, so a 301 reaches the adapter;
  - so does a 200 whose body is neither the CSV header nor a JSON array;
  - zero objects for `active` is an `UpstreamFormatException`.
- **Bytes:** `ai.foresignal.space.CelestrakBytesHealthRule` reads `UsageMeter.bytesToday`. Above 60 MB a day it
  reports `degraded` and logs once that day.

**2. The halt hook** (SPI and collector).
- **SPI:** `UpstreamHaltException(status, reason[, hold])`. The default hold is 24 h, and the reason always
  contains "automatic retries stopped".
- **`FetchExecutor`:**
  - `BackoffRegistry.halt` holds the source for the hold. It is not clamped to the 1 h ceiling, and it is
    persisted in the new column `source_health.backoff_halted` (Hibernate);
  - the failure is recorded as usual;
  - it publishes the system notification `system.source_halted` (critical) with source, status and `heldUntil`.
- **Health:** a new built-in `HealthEvaluator` rule reports `failing` with the halt reason, without the class
  prefix.
- **`ManualFetchService`:** releases a halt (`BackoffRegistry.clearHalt`) and then fetches. An ordinary backoff
  still answers `409 IN_BACKOFF`.

**3. `celestrak-socrates`.**
- `getConditional` of `/SOCRATES/sort-minRange.csv`. The capture showed it sends `ETag` and `Last-Modified`.
- The file is stream-parsed by `SocratesParser` over the new shared RFC 4180 reader
  `ai.foresignal.source.formats.csv.CsvReader`.
- **Filters:** a row is kept when any matches: `minProb` (1e-5), `maxRangeKm` (1.0) or `watchNorad`. At most 2,000
  records per run.
- **Records:** externalId `{low}-{high}-{TCA minute}`, `eventTime = TCA`, RECEIVED time base, TTL 12 h at
  receipt, the lowerCamel payload of the spec.
- Budget 1/min, 4/day; 64 MB; 180 s fetch timeout. The same halt rule as `celestrak-gp`.

**4. `launch-library`.**
- **Requests:** the 2.3.0 window `now−7d … now+60d`, `limit=100&mode=normal&ordering=net`. `next` is followed for
  at most 3 pages, and only on the configured host; a capped run is a partial result.
- **Auth:** an optional `token`, sent as `Authorization: Token …`.
- **Throttling:** `defaultRetryAfter` 30 min after a 429 without `Retry-After`.
- **Records:** revised-event time base (`net` / `last_updated`); pad point from numbers or numeric strings.
- **Descriptor:**
  - payload as in the spec; `embedFields missionDescription`, `rankField probability`, title `name`;
  - interval fields `windowStart`/`windowEnd`, which the iCal `launches` feed reads;
  - kind display `rocket`.

**5. `spacetrack-tip`.**
- **Credentials:** `identity` and `password`, both required.
- **Login:** a form POST. The `Set-Cookie` pairs become a `Cookie` header, kept in memory and keyed by a SHA-256
  fingerprint of the credentials; the session is re-established once on a 401.
- **Refusals:** a `{"Login":"Failed"}` body, a 401 or 403, or no cookie throws `CredentialsRejectedException`.
- **Query:** `INSERT_EPOCH/%3Enow-0.042`, hourly; budget 2/min, 60/day.
- **Records:** `reentry-prediction`. The confirmed fields map as in the prompt. LAT, LON, INCL, DIRECTION, REV,
  HIGH_INTEREST and NEXT_REPORT become lowerCamel payload keys when present. A `Point(LON, LAT)` comes with
  `geometryNote`.
- **Licence:** `egress: LOCAL_ONLY`.

**6. Predicted passes** (`ai.foresignal.space`).
- **Orekit:** 054's Orekit 13.1.9 and `OrekitData` (leap seconds and EOP on the classpath) are reused; nothing was
  added. Verified locally (jshell-style programs against the Gradle cache jars):
  - the TLE field constructor accepts six-digit numbers;
  - with Orekit's own unit conversions, a TLE built from the OMM numbers equals CelesTrak's parsed TLE;
  - SGP4 needs UTC−TAI, which `tai-utc.dat` supplies;
  - outside the EOP file Orekit applies no corrections (054's no-EOP fallback).
- **`PassPredictor.predict(OmmElements, Observer, from, horizon, minEl)`:**
  - `ElevationDetector` (60 s, 1 ms) and `ElevationExtremumDetector` on a `TopocentricFrame` over WGS84;
  - a 30 min look-back, so a pass in progress keeps its AOS;
  - azimuths at AOS and LOS; a ground track of at most 60 points (`GroundTracks`, split at ±180°, 0.01°).
- **Accuracy:** against an independent Skyfield 1.55 / sgp4 2.27 table, AOS, TCA and LOS agree within about 0.1 s
  and elevation within 0.01°.
- **`OrekitSelfTest`:** on `ApplicationReadyEvent`, on a virtual thread, it checks a bundled ISS pass
  (`space/selftest-iss.json`). It logs an error on failure, and `foresignal.space.self-test=false` disables it.
- **`CelestrakElementsService`:** the current `celestrak-gp` heads (non-tombstoned, non-expired). It also
  implements the new SPI `OrbitalElements` (`byNorad`), so adapters never touch the store.

**7. `satellite-passes`** (DERIVED, `ai.foresignal.source.adapters.passes`).
- **Params:** `norad` (25544, 57166, 59051), `places` (default every non-sensitive point place),
  `minElevationDeg` 10, `horizon` 48h, `maxPassesPerPlace` 100.
- **Inputs:** `celestrak-gp` `satellite` changes recompute the watched number; place edits reconcile; a 5 min tick
  recomputes everything every 30 min.
- **Records:** externalId `place:{place}:{norad}:{aosEpochMinute}`, `eventTime` = `validFrom` = AOS, RECEIVED.
  Times are rounded to 10 s, elevation to 0.1° and azimuths to 1°, so element refreshes do not churn versions.
- **Lifecycle:**
  - tombstoned at LOS + 10 min, or when fresh elements no longer predict the pass; missing elements never
    tombstone;
  - reconcile tombstones the passes of deleted places;
  - `kindRetention satellite-pass` 14 d.
- **`imminent`:** turns true 15 min before AOS (see Deviation 2).

**8. SatDump stations** (049 hook).
- **New params:** `predictNorad` (57166, 59051) and `predictMinElevationDeg` (10).
- **Predictions:** every 30 min, from `OrbitalElements`, a station emits `predicted` versions under
  `{station}:{satKey}:{aosEpochMinute}` (`PassBook.predict`; payload adds `tca` and the azimuths; geometry
  `predicted-track`).
- **Joining:** an RF session (`sessionPassId`) or a `dataset.json` join reuses a predicted pass of the same satKey
  within ±5 min of its AOS, so `receiving`/`recorded`/`decoded` continue the record.
- **No session by LOS + 20 min:** `missed` if no `/api` failure streak overlapped the window, else a tombstone.
- **Withdrawn predictions** are tombstoned.

**9. CelesTrak-compatible output** (`CelestrakCompatController`).
- **Selectors:** `GROUP` (membership, `active` = all current), `CATNR`, `INTDES` (prefix), `NAME`
  (case-insensitive substring). Exactly one is allowed; any other combination, `SPECIAL` or a bad `CATNR` is a
  plain-text 400.
- **`FORMAT`:** `TLE`/`3LE`, `2LE`, `JSON`, `JSON-PRETTY`, `CSV`, `XML` (CCSDS OMM 2.0), `KVN`; unknown or absent
  falls to CSV.
- **TLE:** `OmmFormats` generates the lines with CelesTrak's column layout and mod-10 checksums, CRLF endings and
  24-character name padding. It reproduces CelesTrak's `FORMAT=tle` for the ISS byte for byte and leaves out
  numbers ≥ 100000. An empty result is `No GP data found` (200).
- **Headers:** `ETag` = SHA-256(max seq, query); `Cache-Control: max-age=3600` (`private, …` for a feed key);
  `X-Foresignal-Elements-Age`; `Warning` past 3 days; `If-None-Match` gives a 304.
- **Access:**
  - `AccessPolicy` makes `GET`/`HEAD /compat/celestrak/**` `read` and every other method `admin`;
  - a 035 feed key is accepted as `?key=` on `/compat/celestrak/` (`PathKeyFilter`), and `FeedKeyService` accepts
    `/compat/celestrak/` prefixes;
  - a restriction that hides the source or the kind, or limits the area, yields no element sets.

**10. Docs.**
- **OpenAPI:** the two operations are `implemented`, with their descriptions amended (INTDES prefix, `?key=`
  rules). `SatellitePassPayload` gains `missed`, `tca`, the azimuths, `place` and `imminent`; the `satellite-pass`
  and `reentry-prediction` kind docs are reconciled; there is a changelog entry. `lintOpenApi` passes and Postman
  was regenerated.
- **Source Catalog:** the `celestrak-gp` row is rewritten with the verified policy; `launch-library` is updated;
  `celestrak-socrates`, `spacetrack-tip` and `satellite-passes` are new rows.
- **Guides:** Agent Guide "Space" section (MCP: `query_records kind=satellite-pass`, the trigger recipes); Operator
  Guide "Space sources" (halts, feed keys, client URLs, Space-Track rules); Adapter Guide §17.
- **Invariants:** SRC-3, SRC-6 and EGR-2 list the new tests.
- **`THIRD_PARTY_NOTICES.md`:** two God's Eye View rows; the Orekit use is extended. The GEV-derived files carry
  origin headers.

## Files created / modified

**Created**
- `src/main/java/ai/foresignal/source/spi/UpstreamHaltException.java`, `OrbitalElements.java`.
- `src/main/java/ai/foresignal/space/`: `package-info`, `OmmElements`, `OmmFormats`, `Observer`,
  `PredictedPass`, `PassPredictor`, `GroundTracks`, `CelestrakElementsService`, `CelestrakBytesHealthRule`,
  `OrekitSelfTest`; resource `src/main/resources/space/selftest-iss.json`.
- `src/main/java/ai/foresignal/source/formats/csv/`: `CsvReader`, `package-info`.
- `src/main/java/ai/foresignal/source/adapters/celestrak/`: `package-info`, `CelestrakGpAdapter`,
  `CelestrakGpParser`, `CelestrakGroups`, `CelestrakPolicy`, `Memberships`, `CelestrakSocratesAdapter`,
  `SocratesParser`.
- `src/main/java/ai/foresignal/source/adapters/launchlibrary/`: `package-info`, `LaunchLibraryAdapter`,
  `LaunchLibraryParser`.
- `src/main/java/ai/foresignal/source/adapters/spacetrack/`: `package-info`, `SpaceTrackTipAdapter`, `TipParser`.
- `src/main/java/ai/foresignal/source/adapters/passes/`: `package-info`, `SatellitePassesGenerator`.
- `src/main/java/ai/foresignal/api/CelestrakCompatController.java`.
- Tests:
  - `space/PassPredictorTest`, `space/OmmFormatsTest`;
  - `source/adapters/celestrak/CelestrakGpParserTest`, `MembershipsTest`, `CelestrakGpAdapterTest`,
    `CelestrakSocratesAdapterTest`, `SocratesParserTest`, `SpaceIngestIT`;
  - `source/adapters/launchlibrary/LaunchLibraryAdapterTest`, `source/adapters/spacetrack/SpaceTrackTipAdapterTest`;
  - `source/adapters/passes/SatellitePassesGeneratorTest`, `SatellitePassesDerivedIT`;
  - `source/adapters/satdump/StationPredictedPassIT`;
  - `api/CelestrakCompatControllerIT`, `collect/UpstreamHaltIT`, `collect/UpstreamHaltTest`,
    `source/formats/csv/CsvReaderTest`;
  - the helper `testsupport/adapters/AdapterHttpHarness`.
- Fixtures: `src/test/resources/fixtures/celestrak-gp/` (`stations.csv`, `stations.json` derived, `iss-tle.txt`,
  README), `celestrak-socrates/`, `launch-library/`, `spacetrack-tip/` (synthetic), `space/` (the Skyfield
  reference table).

**Modified**
- `source/spi/SourceDescriptor` (`followRedirects`), `collect/BoundSourceHttp`, `collect/BackoffRegistry` (`halt`,
  `isHalted`, `clearHalt`), `collect/FetchExecutor`, `collect/HealthEvaluator`, `collect/ManualFetchService`,
  `store/SourceHealthEntity` (`backoff_halted`).
- `source/DescriptorValidator` (string-list defaults against `allowed`; see Issue 1).
- `security/AccessPolicy`, `security/feed/FeedKeyService`, `security/restrict/PathKeyFilter`.
- `source/adapters/satdump/PassBook`, `PassState`, `SatDumpStation`, `SatDumpAdapter`, `SatDumpDescriptor`,
  `SatDumpFamily`.
- Tests: `security/AccessPolicyTest`, `source/DescriptorValidatorTest`, `testsupport/weather/WeatherIngestSupport`
  (CelesTrak base URL, so the new ITs add no Spring context).
- `src/test/resources/openapi/pending-operations.txt`.
- `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `docs/Source-Catalog.md`,
  `docs/Agent-Guide.md`, `docs/Operator-Guide.md`, `docs/Adapter-Guide.md`, `docs/Invariants.md`,
  `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **SatDump reads CSV, not JSON.** SatDump's own reader (`src-core/db/kepler/kepler_utils.cpp`,
   `ccsdsOmmToKepler`, in the SatDump tree at 57b1cd666 of 2026-10-12) splits each line into exactly 17
   comma-separated OMM columns. `FORMAT=json` would load nothing. The Operator Guide therefore points SatDump at
   `…/gp.php?GROUP=active&FORMAT=omm`, which falls to CSV here as at CelesTrak. The code was read for verification
   only; nothing was copied.
2. **"Pass in 10 minutes" needed one more version.**
   - 017's `forecastHorizon` matches `valid_from`, and only on new versions (code and Agent Guide), so a pass
     predicted 48 h ahead could never fire as the prompt describes.
   - `satellite-passes` therefore sets `validFrom` = AOS and writes an `imminent: true` version 15 min before AOS.
     It does so on a 5 min tick; the full recompute stays at 30 min.
   - The Agent Guide gives the `forecastHorizon: "15m"` recipe and an `imminent == true` recipe.
3. **There are no "private places".** 039's privacy is per source, not per record. Sensitive places (036) are
   used only when named in `places`, and the Operator Guide says to mark the source private.
4. **SatDump "configured satellites"** did not exist. `predictNorad` and `predictMinElevationDeg` were added to the
   station descriptor. "Healthy during the window" means that no `/api` failure streak overlapped it; a station
   without `apiUrl` counts as healthy.
5. **The `?key=` feed key.** Conventions put keys in `/v1/k/{key}/…` paths, but the compat paths are outside
   `/v1`, and the spec and the prompt define `?key=`. It is accepted only under `/compat/celestrak/`, with the same
   refusals and backoff as path keys. Feed keys may now name `/compat/celestrak/` prefixes. See owner question 1.
6. **New framework pieces the prompt implies but does not name:**
   - the descriptor flag `followRedirects`;
   - the halt persistence column `source_health.backoff_halted`;
   - the system subtype `system.source_halted`;
   - the SPI `OrbitalElements` for the station.
7. **The Launch Library `minInterval` is 5 minutes.** A descriptor cannot depend on credentials. The default is 15
   min, and the description and Operator Guide say not to go lower without a token.
8. **LL2 pad coordinates are JSON numbers** in the 2026-10-13 capture, not strings. Both are parsed.
9. **The SOCRATES body is held in memory.** `getConditional` loads it (about 23 MB, not "more than 10"), because
   `SourceHttp` has no streaming conditional GET. The rows are parsed as a stream, so only the kept records are
   held.
10. **Payload additions, reconciled in the spec:**
    - `satellite-passes`: `imminent`;
    - station predicted versions: `tca`, `aosAzimuthDeg`, `losAzimuthDeg`;
    - `spacetrack-tip`: the optional TIP fields in lowerCamel (`lat`, `lon`, `inclination`, `direction`, `rev`,
      `highInterest`, `nextReport`).
11. **Selector and rounding choices:**
    - `INTDES` is a prefix match, as at CelesTrak;
    - TLE eccentricity rounds half-even, which matches CelesTrak's `.00067245` → `0006724`;
    - TLE output uses CRLF and a 24-character name, as CelesTrak does.
12. **The self-test runs after startup, in the background.** 054 established that nothing Orekit-related loads at
    startup.
13. **054's "cached-TLE fallback in `PolarOverlays.swath`"** is not done. 064's text does not ask for it;
    `OrbitalElements` now makes it a small change.

## Issues found / open questions

1. **A defect from 063, fixed here.** `DescriptorValidator` compared a string-list default's `toString()` with
   `allowed`, so `aisstream`'s `messageTypes` (and now `memberGroups`) would have failed registry validation at
   startup. Each element is now checked, and `DescriptorValidatorTest` covers it.
2. **CelesTrak volume.**
   - SOCRATES was 23,424,045 bytes on 2026-10-13. At two updates a day that is about 46 MB, on top of about 36 MB
     of `active` CSV, so roughly 82 MB a day from CelesTrak with both sources enabled, near the 100 MB risk line.
   - The 60 MB health rule covers `celestrak-gp` only. Should SOCRATES default to one fetch a day, or count toward
     the same rule?
3. **SOCRATES TTL.** The 12 h TTL equals the 12 h interval. A 304 (no new SOCRATES run) does not touch the records,
   so they can expire briefly. This follows the prompt.
4. **Unverified live:**
   - CelesTrak's own XML and KVN byte layout (the output follows CCSDS OMM 2.0);
   - the Space-Track login response and the TIP fields (the fixture is synthetic; recapture in 143);
   - Launch Library with a token;
   - Gpredict and Look4Sat accepting a `?key=` URL.
5. **`finals2000A.all` predictions run out in late 2026** (054's note). Pass times then lose the EOP corrections,
   which is far below the 5 s tolerance.
6. **Instructions in data:** none. The captured pages and bodies contained no instructions to an agent.

**Owner questions (non-blocking):**
1. Keep `?key=` on `/compat/celestrak/`, or add a key-in-path form outside `/v1`?
2. Should `launch-library`'s floor stay at 5 min?
3. Should sensitive places be predicted only when named, as now?
4. Should SOCRATES's volume be limited (Issue 2)?
5. The prompt's "FORMAT=json for SatDump" contradicts SatDump's parser (Deviation 1); which document should change?

## Tests written (not run)

- **`CelestrakGpParserTest`:**
  - the CSV and JSON fixtures give identical element sets and records;
  - six-digit numbers, EPOCH without a zone, the payload round trip;
  - zero objects for `active` is an error; malformed input; skipped and quoted rows; `looksLike`.
- **`CelestrakGpAdapterTest`** (WireMock, real `UpstreamClient`):
  - memberships merged into `groups`; at most two member groups per run, oldest first;
  - requests serial, by served timestamps;
  - the "not updated" 403 is not modified, with no backoff; a member's 403 touches its time;
  - 403, 404, 301 (not followed) and 503 halt with a 24 h hold and the exact reason; a non-CSV 200 halts;
  - an empty `active` is a format error; JSON format; descriptor and `checkParams`.
- **`UpstreamHaltIT`:**
  - the hold is 24 h, persisted, with the reason and a `system.source_halted` event;
  - no fetch 23 h later; fetch-now releases the halt, while an ordinary backoff stays `IN_BACKOFF`;
  - the halt survives a cache reset.
- **`UpstreamHaltTest`:** the health rule and the exception.
- **`SocratesParserTest`:** header-driven columns, each filter, malformed input, a 12 MB synthetic stream within a
  64 MB heap budget and the 2,000 cap. **`CelestrakSocratesAdapterTest`:** ETag then 304, halts, params.
- **`LaunchLibraryAdapterTest`:**
  - the window; pagination capped at 3; a complete last page;
  - Token header only with a credential;
  - 429 → 30 min, or `Retry-After`;
  - pad geometry from numbers and strings; foreign `next` refused.
- **`SpaceTrackTipAdapterTest`:**
  - the cookie reused; re-login on 401;
  - bad credentials by body, by status and when missing → `CredentialsRejectedException`;
  - password and cookie redacted in traces and exchanges;
  - TIP parsing; LOCAL_ONLY.
- **`PassPredictorTest`:**
  - the Skyfield reference table for two sites (AOS/LOS/TCA ±5 s, maximum elevation ±0.2°, azimuth ±1°);
  - no pass below the mask; a six-digit element set;
  - the ground track; the antimeridian split; the self-test.
- **`OmmFormatsTest`:**
  - TLE equals CelesTrak's `FORMAT=tle` byte for byte, and every generated TLE re-parses in Orekit;
  - no alpha-5;
  - the field encodings; CSV and JSON round trips; XML and KVN.
- **`SatellitePassesDerivedIT`** (real DERIVED runtime over a real `celestrak-gp` fetch): six ISS passes for a
  place; an element refresh within rounding writes no version; `imminent` at AOS − 15 min; a tombstone at
  LOS + 10 min. **`SatellitePassesGeneratorTest`:** rounding, the record, `imminent`, the observer, params, the
  descriptor.
- **`StationPredictedPassIT`:**
  - predicted, then `receiving` on the same externalId;
  - no churn within 30 min or on an unchanged prediction;
  - `missed`; missing elements withdraw nothing; an unhealthy window gives a tombstone; a changed orbit withdraws
    the old predictions.
- **`CelestrakCompatControllerIT`** (MockMvc, heads from a real fetch):
  - every FORMAT; unknown and absent → CSV;
  - TLE equals the CelesTrak fixture, with checksums, 69 columns and an Orekit re-parse; ≥ 100000 omitted;
  - selectors; `No GP data found`; the 400s; caching and 304;
  - 401 without read; a feed key as `?key=` (private cache; bad key 401; other prefixes 403; another source
    empty).
- **`SpaceIngestIT`:** identical elements → touches, no versions; a new EPOCH → version 2; an older EPOCH is LATE;
  the "not updated" 403 → NOT_MODIFIED.
- `MembershipsTest`, `CsvReaderTest`, `AccessPolicyTest.celestrakCompat`,
  `DescriptorValidatorTest.listDefaultAgainstAllowed`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (only the existing deprecation and unchecked notes).
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "832 requests across 80 tag folders … covers all
  ops: True".

## Notes for the next prompt

1. **065 and later adapters:**
   - an upstream whose policy says "stop and alert a human" throws `UpstreamHaltException` (Adapter Guide §17);
   - set `followRedirects(false)` when a redirect must be seen;
   - CSV upstreams can use `ai.foresignal.source.formats.csv.CsvReader` (FIRMS is CSV);
   - adapter tests against WireMock can use `testsupport/adapters/AdapterHttpHarness`.
2. **ITs needing a stubbed upstream** can join `WeatherIngestSupport`'s context by adding their base-URL property
   there (064 did, for CelesTrak) instead of adding a context.
3. **107 (SatDump OMM feed, `getCelestrakGroupTle`):**
   - `/compat/celestrak/**` is already `read`, and `?key=` works under it;
   - `OmmFormats` and `CelestrakElementsService` are reusable;
   - SatDump's reader wants 17-column CSV lines;
   - add the cached-TLE fallback to `PolarOverlays.swath` through `OrbitalElements`.
4. **097 checkpoint:**
   - first run of everything above;
   - `SatellitePassesDerivedIT`, `SpaceIngestIT` and `CelestrakCompatControllerIT` share `WeatherIngestSupport`'s
     context; `UpstreamHaltIT` shares `CollectorTestSources`;
   - `SocratesParserTest.streamingCap` measures heap growth after `System.gc()` (64 MB allowance);
   - `CelestrakGpAdapterTest.serial` relies on WireMock's 150 ms fixed delay and logged dates.
5. **143:** with `FORESIGNAL_VERIFY_*` Space-Track credentials, recapture `fixtures/spacetrack-tip/tip.json` per its
   README and confirm the optional TIP fields. Confirm a Launch Library token. Check Gpredict and Look4Sat against
   `/compat/celestrak/gp.php?…&key=`.
6. **144 (Flyway):** new column `source_health.backoff_halted boolean` (nullable). The adapter state of
   `celestrak-gp` holds `members:{group}`, `membersAt:{group}` and `etag:gp:*`/`lastmod:gp:*`. A SatDump station's
   `PassState` JSON gains `predictedLos`, `tca`, `aosAzimuthDeg` and `losAzimuthDeg`.
7. **Owner decisions pending:** the five questions above, plus those carried from 012–063.
