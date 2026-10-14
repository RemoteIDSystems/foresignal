# 095-CR-Citizen-Science-And-Signals

| | |
|---|---|
| Prompt | 095-Citizen-Science-And-Signals.md |
| Status | Complete |
| Implementation commit | 32be0a365cdac7fc83a9c91f6c84f4de4c0f1c08 |
| Date | 2026-10-14 |

## Summary

Four contributor-generated sources and one stream-derived signal land as ordinary records on the v1.2
pipeline, each minimising personal information and keeping per-record licences.

**1. `inat-observations` (`source/adapters/inat`).** A required `aoi` (bbox or place id: no global
polling), one ascending `id_above` walk (≤ 5 pages of 200) for new observations plus one `updated_since`
request for revisions per run. `externalId = inat:{id}`, STANDARD/ALL, no TTL, `observedAt = updated_at`,
`eventTime = time_observed_at ?? observed_on` (date-only → local midnight via the 040 timezone).
Obscured/private rows are skipped or keep their randomised upstream coordinates with the upstream
`accuracyMeters`; `user`, descriptions and comments are never read. `photoUrl` only on the
`inaturalist-open-data` host with an open licence. A `ApiVersion` seam documents v2 as preferred upstream.

**2. `ebird-notable` (`source/adapters/ebird`).** Disabled by default, `apiKey` in `x-ebirdapitoken`, params
`lat`/`lng` or `place`, `distKm` 1–50, `backDays` 1–30, `includeRecent`. `observedAtMode = RECEIVED`;
`obsDt` local text kept as `obsDtLocal` and mapped to `eventTime` via the 040 timezone. Private locations
rounded to 0.05° with a 5000 m accuracy and no `locName`. Non-commercial with attribution; `LOCAL_ONLY`.

**3. CDC NWSS (`source/adapters/cdc`).** A shared `SodaClient` (`$where=date_updated > '{cursor}'` paging,
optional `X-App-Token`, cursor = max `date_updated`). `cdc-nwss-wval` (`atcp-73re`) emits `wastewater-level`
state; `cdc-nwss-detections` reads one current dataset per pathogen (default `h5`, `measles`, `mpox`) and
emits `wastewater-detection` folded to the latest sample per site and target. Records are placed at county
or state county-union interior points with `geometryApproximate`. WastewaterSCAN rows are CC BY-NC 4.0 and
dropped unless `includeWastewaterScan`, then carrying `licenceOverride`.

**4. `wiki-edit-surge` (`source/adapters/wikistream`).** Disabled by default. A pure `RecentChangeEvent`
parser and a pure `SurgeDetector` (sliding window, hashed editor names in memory only, bands, new-page rule,
60-minute linger/tombstone, 200,000-page cap with eviction). `WikiStreamAdapter` holds one SSE connection
over a new `SourceHttp.openStream` SPI, resumes with a verbatim `Last-Event-ID`, reconnects immediately on a
clean ~15-minute server close, and backs off 1 s → 5 s → 30 s → 120 s with jitter (429/503 wait 30 s).
Records are pushed only when the banded state changes; a rising surge is geotagged once through a
`*.wikipedia.org`-allowlisted query. Usernames, comments and parsed comments are never stored.

**5. Framework and docs.** `SourceHttp.openStream` + `HttpStream` (implemented by `BoundSourceHttp`) and an
SPI `PlaceTimeZones` with `reference/GazetteerTimeZones` (nearest `gazetteer_place.timezone`). `Source-Catalog.md`
gains a five-row section; `Operator-Guide.md` gains a "Citizen science and signals" section. No API
operations change, so `docs/OpenAPI.json`, `pending-operations.txt` and Postman are unchanged.

## Files created / modified

**Created, main**
- `source/adapters/inat/{package-info,InatParser,InatObservationsAdapter}.java`
- `source/adapters/ebird/{package-info,EbirdParser,EbirdNotableAdapter}.java`
- `source/adapters/cdc/{package-info,SodaClient,CdcDates,CdcText,UsStates,NwssGeometry,NwssWvalParser,NwssWvalAdapter,NwssDetectionParser,NwssDetectionAdapter}.java`
- `source/adapters/wikistream/{package-info,RecentChangeEvent,SurgeDetector,WikiStreamAdapter}.java`
- `source/spi/{HttpStream,PlaceTimeZones}.java`
- `reference/GazetteerTimeZones.java`

**Modified, main**
- `source/spi/SourceHttp.java` (`openStream` default)
- `collect/BoundSourceHttp.java` (`openStream` override: backoff, budget, UA, decode)

**Created, test**
- `source/adapters/inat/{InatParserTest,InatObservationsAdapterTest}.java`
- `source/adapters/ebird/{EbirdParserTest,EbirdNotableAdapterTest}.java`
- `source/adapters/cdc/{NwssWvalParserTest,NwssDetectionParserTest,NwssAdaptersTest}.java`
- `source/adapters/wikistream/{RecentChangeEventTest,SurgeDetectorTest,WikiStreamAdapterTest,WikiTestContext}.java`
- `source/adapters/SignalsIngestIT.java`
- fixtures under `src/test/resources/fixtures/{inat-observations,ebird-notable,cdc-nwss-wval,cdc-nwss-detections,wiki-edit-surge}/`
  (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (five adapters classified)

**Docs**: `docs/Source-Catalog.md`, `docs/Operator-Guide.md`.

## Deviations from the prompt

1. **A new `SourceHttp.openStream` SPI was needed for SSE.** The prompt ("SSE `GET …`") assumes a streaming
   HTTP read, but the SPI had only buffered `get` and WebSocket. `openStream(uri, headers)` returns an
   `HttpStream` (status, headers, unread body), implemented only by `BoundSourceHttp` (so `UpstreamClient`
   remains the only HTTP client), after the backoff check, one budget unit and Foresignal's User-Agent.
2. **A `PlaceTimeZones` SPI backs "the 040 timezone of the point".** 040 stored the IANA timezone on
   `gazetteer_place` but exposed no lookup; `reference/GazetteerTimeZones` does a KNN query, caches on 0.05°
   and falls back to UTC. Adapters take it as a dependency (like `CountryCentroids`).
3. **`aoi` is a `String` parameter, not a `BBOX`.** The prompt's `aoi` accepts "bbox or place", and one
   `ParamSpec` has one type; the adapter parses a `west,south,east,north` box or resolves a place id, building
   a ±0.25° box around the place point.
4. **`id_above` seeding dirties to `order=desc` on the first run.** With no cursor the first walk is
   newest-first (to seed the cursor) and later walks are ascending `id_above`; `id_above` is advanced to each
   page's max id.
5. **NWSS levels use the state county-union interior point.** `counties_served` is a list of county *names*,
   not FIPS, so it cannot be resolved offline; detections use `county_fips` as the prompt says, and levels fall
   back to the state (USPS→FIPS crosswalk in `UsStates`). The prompt's "first county in `counties_served`" is
   therefore not implemented.
6. **Geotag host derived from `title_url`.** `RecentChangeEvent` keeps `server_url` in the parser but the
   detector does not carry it; the adapter derives the query origin from the surge's `title_url` and requires a
   `*.wikipedia.org` host (else no geometry). Tests with a non-wikipedia `title_url` avoid any real network
   call.
7. **Error backoff honours `Retry-After` only as a floor.** `UpstreamHttpException` does not expose the header,
   so the adapter's internal loop waits a fixed 30 s on a 429/503 (the real `UpstreamClient` also records the
   upstream's own `Retry-After` in `BackoffRegistry`).
8. **`qualityGrade` filtering bounds the "quality-grade change" test.** A record whose grade drops below the
   configured grade leaves the kept set, so `SignalsIngestIT` proves SIGNIFICANT with a changed identification
   (`taxonName`/`commonName`) that keeps `quality_grade = research`; a below-threshold update is skipped rather
   than versioned.
9. **`wiki-edit-surge` `mbPerDay` is an estimate (3 GB/day).** The IT harness does not measure the live
   firehose during the build (no network); the Operator Guide tells the operator to measure their own volume.
10. **CDG dates read as UTC.** Socrata floating timestamps have no zone; `CdcDates` reads them as UTC.

## Issues found / open questions

1. **Tests not run** (guardrail 1); they run first at checkpoint 097.
2. **`WikiStreamAdapterTest` uses WireMock as the SSE server**, so a connection closes immediately and is
   treated as a short error (1 s backoff). The "clean 15-minute close reconnects without a failure count" is
   implemented but only exercised indirectly; verifying it needs a ≥ 30 s connection (or a clock-driven
   duration), which the build does not run.
3. **The geotag request is never exercised against Wikipedia** (no network in tests); only the host allowlist
   and URL shape are tested.
4. **`GazetteerTimeZones` misses until the reference datasets are loaded** (040); until then date-only iNat
   times and eBird `obsDt` fall back to UTC. That is the intended "no zone" degradation.
5. **`WastewaterSCAN` licence is carried as a payload field** (`licenceOverride`); 109 must read it, as the
   prompt states. Nothing enforces it yet.
6. **`SignalsIngestIT` enables/starts the scheduler for two sources in one context**; if it proves flaky at
   097 the two cases can be split into separate contexts.

## Tests written (not run)

- **Parser contracts with fixtures:** `InatParserTest` (kept/skip counts, obscured accuracy, date-only
  timezone, photo host rule, no user fields, flags, format change, 2D geometry; `FieldSpecContract`),
  `EbirdParserTest` (local time to UTC, private-location rounding and accuracy, absent `howMany`, format
  change; `FieldSpecContract`), `NwssWvalParserTest` (WastewaterSCAN filter/override, one-decimal `wval`,
  time base; `FieldSpecContract`), `NwssDetectionParserTest` (latest sample per site and target, `"No"` →
  false, WastewaterSCAN override; `FieldSpecContract`).
- **Pure event/detector:** `RecentChangeEventTest` (envelope, verbatim `meta.id` array, canary/bot/type/
  namespace/wiki filters, malformed payload), `SurgeDetectorTest` (threshold boundary, bands, new-page rule,
  60-minute tombstone, eviction cap, editor hashing).
- **WireMock adapter tests:** `InatObservationsAdapterTest` (`id_above` + `updated_since` pass, ≤ 5 pages,
  descriptor validates), `EbirdNotableAdapterTest` (header, 403, `includeRecent`, local-only), `NwssAdaptersTest`
  (`$where` cursor + app token, per-pathogen datasets, descriptors validate), `WikiStreamAdapterTest`
  (15-edit surge record, `Last-Event-ID` on reconnect, throttle/ladder delays, geotag host allowlist).
- **`SignalsIngestIT` (`@IntegrationTest`, real scheduler/executor/writer):** an iNaturalist revision is a
  SIGNIFICANT version and no stored payload contains `user`/`login`/`description`; an unchanged NWSS re-poll
  writes no version.

## Compile result

- `./gradlew compileJava compileTestJava`: **BUILD SUCCESSFUL**.
- No new dependencies (module changes only: an SSE read over `java.net.http` in `BoundSourceHttp`).
- `docs/OpenAPI.json`: unchanged (no operations added or modified); `pending-operations.txt` unchanged; Postman
  not regenerated (no examples changed).

## Notes for the next prompt

1. **097 checkpoint:** first run of the 095 tests. `FamilyDefaultsTest` gained five entries
   (`InatObservationsAdapter` event, `EbirdNotableAdapter` event, `NwssWvalAdapter` state,
   `NwssDetectionAdapter` event, `WikiStreamAdapter` bulk-event); nine fixture files exist; five new source ids
   register (`inat-observations`, `ebird-notable`, `cdc-nwss-wval`, `cdc-nwss-detections`, `wiki-edit-surge`).
2. **`SourceHttp.openStream` + `HttpStream`** is the streaming-HTTP seam (prompt 095). A later SSE source
   should reuse it; the default throws for hand-built test doubles, so a new test context must implement it.
3. **`PlaceTimeZones` / `GazetteerTimeZones`** back local-time conversion for any later source that reads a
   wall-clock time with no offset.
4. **109:** read the NWSS `licenceOverride` payload field (`CC BY-NC 4.0 (WastewaterSCAN)`) and the
   `ebird-notable` / `inat-observations` per-record licences. `submarine-cables`/`gvp-volcanoes` (094) are
   already local-only; 110's egress reads the same flags.
5. **143:** recapture the five synthetic fixture sets with real captures per their READMEs and confirm the
   current CDC NWSS dataset ids.
6. **144 (Flyway):** no schema change; `GazetteerTimeZones` is a read of 040's existing `gazetteer_place`.
