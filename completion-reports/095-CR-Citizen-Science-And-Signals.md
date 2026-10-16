# 095-CR-Citizen-Science-And-Signals

| | |
|---|---|
| Prompt | 095-Citizen-Science-And-Signals.md |
| Status | Complete (post-review fixes 2026-10-16) |
| Implementation commit | 32be0a365cdac7fc83a9c91f6c84f4de4c0f1c08 |
| Post-review fix commit | 707c24a5cc0413038a8900cebfcd996cdd30ab97 (plus the framework commit 94a06bdd0c778319829d3d1be589a805432ddfea) |
| Date | 2026-10-14; corrected 2026-10-16 |

## Summary

Four contributor-generated sources and one stream-derived signal land as ordinary records on the v1.2
pipeline, each minimizing personal information and keeping per-record licenses.

**1. `inat-observations` (`source/adapters/inat`).** A required `aoi` (bbox or place id: no global
polling; since the review a box may not cross the antimeridian and may be at most 15° on a side). Each run
sends one `updated_since` request for revisions and an ascending `id_above` walk for new observations, together
at most the per-minute budget of requests (2 by default, so one page of each; never more than 5 pages), and a
walk that does not finish resumes from the stored cursor. `externalId = inat:{id}`, STANDARD/ALL, no TTL,
`observedAt = updated_at`, `eventTime = time_observed_at ?? observed_on` (date-only → local midnight via the 040
timezone). `geoprivacy`/`taxon_geoprivacy = private` rows and rows without coordinates are skipped; obscured rows
keep their randomized upstream coordinates with the upstream `accuracyMeters`; `user`, descriptions and comments
are never read. `photoUrl` only when its host is exactly `inaturalist-open-data.s3.amazonaws.com` and its license
is open. `Cache-Control: max-age` is honored (a run inside it sends nothing). An `ApiVersion` seam documents v2 as
preferred upstream.

**2. `ebird-notable` (`source/adapters/ebird`).** Disabled by default, `apiKey` in `x-ebirdapitoken`, params
`lat`/`lng` or `place`, `distKm` 1–50, `backDays` 1–30, `includeRecent`. `observedAtMode = RECEIVED`;
`obsDt` local text kept as `obsDtLocal` and mapped to `eventTime` via the 040 timezone. Private locations
rounded to 0.05° with a 5000 m accuracy and neither `locName` nor `locId`. Non-commercial with attribution;
`LOCAL_ONLY`.

**3. CDC NWSS (`source/adapters/cdc`).** A shared `SodaClient` reads a caller-limited number of pages with a
resumable cursor (`date_updated` plus the rows already read at that timestamp; `$where=date_updated >= …`,
`$order=date_updated ASC, :id ASC`, an offset that skips the rows already read), optional `X-App-Token`.
`cdc-nwss-wval` (`atcp-73re`) emits `wastewater-level` state, one record per site and pathogen: its latest
`week_end`. `cdc-nwss-detections` reads one current dataset per pathogen (default `h5`, `measles`, `mpox`),
sharing the 4/min budget among them round-robin across runs, and emits `wastewater-detection` folded to the
latest sample per site and target. Both drop a row older than the stored head of its entity (a re-dated older
sample). Records are placed at county or state county-union interior points with `geometryApproximate`.
WastewaterSCAN rows are CC BY-NC 4.0 and dropped unless `includeWastewaterScan`, then carrying `licenceOverride`.

**4. `wiki-edit-surge` (`source/adapters/wikistream`).** Disabled by default. A pure `RecentChangeEvent`
parser and a pure `SurgeDetector` (sliding window, hashed editor names in memory only, bands, new-page rule,
60-minute linger/tombstone, 200,000-page cap with eviction; an evicted surge is tombstoned by the next sweep).
`WikiStreamAdapter` holds one SSE connection over a new `SourceHttp.openStream` SPI, resumes with a verbatim
`Last-Event-ID`, reconnects immediately on a clean ~15-minute server close, and backs off 1 s → 5 s → 30 s →
120 s with jitter (429/503 wait 30 s). The last event id is persisted every 1,000 events, every 30 s and when a
connection ends, and the detector is swept every minute while connected. Records are pushed only when the
banded state changes; a surge is geotagged once through a `*.wikipedia.org`-allowlisted query and every phase
carries the point. Usernames, comments and parsed comments are never stored.

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
   a ±0.25° box around the place point. A box that crosses the antimeridian or exceeds 15° on a side is refused
   by `checkParams` (the review's choice of limit; the prompt names none).
4. **`id_above` seeding runs `order=desc` on the first run.** With no cursor the first walk is newest-first (to
   seed the cursor) and later walks are ascending `id_above`; the cursor is advanced to each page's max id.
5. **NWSS levels use the state county-union interior point.** `counties_served` is a list of county *names*,
   not FIPS, so it cannot be resolved offline; detections use `county_fips` as the prompt says, and levels fall
   back to the state (USPS→FIPS crosswalk in `UsStates`). The prompt's "first county in `counties_served`" is
   therefore not implemented.
6. **Geotag host derived from `title_url`.** `RecentChangeEvent` keeps `server_url` in the parser but the
   detector does not carry it; the adapter derives the query origin from the surge's `title_url` and requires a
   `*.wikipedia.org` host (else no geometry). Tests with a non-wikipedia `title_url` avoid any real network
   call.
7. **Error backoff honors `Retry-After` only as a floor.** `UpstreamHttpException` does not expose the header,
   so the adapter's internal loop waits a fixed 30 s on a 429/503 (the real `UpstreamClient` also records the
   upstream's own `Retry-After` in `BackoffRegistry`).
8. **`qualityGrade` filtering bounds the "quality-grade change" test.** A record whose grade drops below the
   configured grade leaves the kept set, so `SignalsIngestIT` proves SIGNIFICANT with a changed identification
   (`taxonName`/`commonName`) that keeps `quality_grade = research`; a below-threshold update is skipped rather
   than versioned.
9. **`wiki-edit-surge` `mbPerDay` is an estimate (3 GB/day).** The IT harness does not measure the live
   firehose during the build (no network); the Operator Guide tells the operator to measure their own volume.
10. **CDC dates read as UTC.** Socrata floating timestamps have no zone; `CdcDates` reads them as UTC.
11. **Requests per run follow the descriptor's default per-minute budget** (iNat 2, NWSS 4). An operator who
    raises the budget does not get more requests per run; the cursors catch up over more runs instead.
12. **Re-dated older rows are rejected against the stored heads** through the `CurrentRecords` SPI (this
    source's own current records), not against adapter state, which would have to hold one value per site.

## Issues found / open questions

1. **Tests not run** (guardrail 1); they run first at checkpoint 097, and the review fixes at the next
   checkpoint.
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
7. **SODA `:id` ordering is unverified against the live CDC portal** (no network during the review); it is the
   Socrata system field documented for stable paging. Re-confirm at 143.
8. **`docs/Source-Catalog.md` still describes the iNat `Cache-Control` handling as a 10-minute interval floor** and
   the NWSS cursor as `date_updated > '{cursor}'`; it was not edited in the review (not in the review's doc
   scope) and should be brought in line by its owner.

## Tests written (not run)

- **Parser contracts with fixtures:** `InatParserTest` (kept/skip counts, obscured accuracy, date-only
  timezone, photo host rule and host equality, no user fields, flags, format change, 2D geometry;
  `FieldSpecContract`), `EbirdParserTest` (local time to UTC, private-location rounding and accuracy without
  `locName`/`locId`, absent `howMany`, format change; `FieldSpecContract`), `NwssWvalParserTest`
  (WastewaterSCAN filter/override, one-decimal `wval`, time base, latest week wins; `FieldSpecContract`),
  `NwssDetectionParserTest` (latest sample per site and target, `"No"` → false, WastewaterSCAN override;
  `FieldSpecContract`).
- **Pure event/detector:** `RecentChangeEventTest` (envelope, verbatim `meta.id` array, canary/bot/type/
  namespace/wiki filters, malformed payload), `SurgeDetectorTest` (threshold boundary, bands, new-page rule,
  60-minute tombstone, eviction cap, an evicted surge tombstoned, editor hashing).
- **WireMock adapter tests:** `InatObservationsAdapterTest` (the real 2/min budget; `id_above` + `updated_since`
  pass; a run fits the budget; a part-way refusal keeps progress; `max-age` honored; descriptor validates),
  `EbirdNotableAdapterTest` (header, 403, `includeRecent`, local-only), `NwssAdaptersTest` (the real 4/min
  budget; `>=` cursor with `:id` order and app token; a backlog resumed within one timestamp; detection
  datasets round-robin; a re-dated older week rejected; descriptors validate), `WikiStreamAdapterTest`
  (15-edit surge record, `Last-Event-ID` on reconnect, the event id persisted at connection end rather than per
  event, throttle/ladder delays, geotag host allowlist).
- **`SignalsIngestIT` (`@IntegrationTest`, real scheduler/executor/writer):** an iNaturalist revision is a
  SIGNIFICANT version and no stored payload contains `user`/`login`/`description`; an unchanged NWSS re-poll
  writes no version.

## Compile result

- `./gradlew compileJava compileTestJava`: **BUILD SUCCESSFUL** (at 32be0a3 and again after the review fixes).
- No new dependencies (module changes only: an SSE read over `java.net.http` in `BoundSourceHttp`).
- `docs/OpenAPI.json`: unchanged (no operations added or modified); `pending-operations.txt` unchanged; Postman
  not regenerated (no examples changed).

## Notes for the next prompt

1. **Next checkpoint:** run the 095 tests including the review additions. `FamilyDefaultsTest` has five entries
   (`InatObservationsAdapter` event, `EbirdNotableAdapter` event, `NwssWvalAdapter` state,
   `NwssDetectionAdapter` event, `WikiStreamAdapter` bulk-event); nine fixture files exist; five source ids
   register (`inat-observations`, `ebird-notable`, `cdc-nwss-wval`, `cdc-nwss-detections`, `wiki-edit-surge`).
2. **`SourceHttp.openStream` + `HttpStream`** is the streaming-HTTP seam (prompt 095). A later SSE source
   should reuse it; the default throws for hand-built test doubles, so a new test context must implement it. A
   stream's adapter state writes through (it is not staged like a polling fetch's): persist a cursor
   periodically, as `wiki-edit-surge` does.
3. **`PlaceTimeZones` / `GazetteerTimeZones`** back local-time conversion for any later source that reads a
   wall-clock time with no offset.
4. **109:** read the NWSS `licenceOverride` payload field (`CC BY-NC 4.0 (WastewaterSCAN)`) and the
   `ebird-notable` / `inat-observations` per-record licenses. `submarine-cables` (094) is already local-only;
   `gvp-volcanoes` (094) is non-commercial by its licence text but its egress is **not** restricted (the
   original version of this note said it was local-only, which was wrong). 110's egress reads the same flags.
5. **143:** recapture the five synthetic fixture sets with real captures per their READMEs and confirm the
   current CDC NWSS dataset ids and SODA `:id` ordering.
6. **144 (Flyway):** no schema change; `GazetteerTimeZones` is a read of 040's existing `gazetteer_place`. New
   adapter-state keys: `inat:freshUntil`, `nwss-wval:cursor:skip`, `nwss-det:{dataset}:cursor:skip`,
   `nwss-det:next`.

## Post-review fixes (2026-10-16)

Commit: `707c24a5cc0413038a8900cebfcd996cdd30ab97` (one Operator-Guide spelling fix landed with this report's
commit). The framework commit `94a06bdd0c778319829d3d1be589a805432ddfea` makes every polling fetch's adapter
state (the iNat `MAX_ID_KEY` and the NWSS cursors included) commit only after its records are written.

| # | Finding | Fix |
|---|---|---|
| 1 | iNat 2/min budget vs up to 6 requests per run | Requests per run = the default per-minute budget (revisions first, at least one page, at most 5); the cursor is stored page by page; a part-way refusal or I/O error after the first request ends the run with what was read. Tests use the real budget (`InatObservationsAdapterTest`). The budget itself is the documented 2/min (prompt and Source-Catalog), so it was kept. |
| 2 | NWSS 20 pages per dataset vs 4/min | `SodaClient.read(…, maxPages)` with the resumable `(date_updated, skip)` cursor; `cdc-nwss-wval` reads 4 pages per run, detections share 4 pages round-robin across datasets. Tested. |
| 3 | NWSS one record per row, older week could win | `NwssWvalParser` folds to the latest `week_end` per site and pathogen; both adapters drop a row older than the stored head. Tested. |
| 4 | `LAST_EVENT_ID` written per event | Kept in memory; persisted every 1,000 events, every 30 s and at connection end. Tested. |
| 5 | Geotag only on RISING | Every phase calls the cached geotag. |
| 6 | Sweep, eviction, AOI, photo host, eBird `locId`, `max-age` | The detector is swept every minute during a connection; an evicted surge is tombstoned; iNat AOI boxes are validated (no antimeridian swap, ≤ 15° a side); the photo host must equal the bucket host; eBird drops `locId` for private locations; iNat honors `Cache-Control: max-age`. |

CR corrections made in this document: Note 4 claimed `gvp-volcanoes` is local-only (the 094 design makes it
non-commercial only; the code does not restrict its egress, and the code was left as designed); the summary and
the iNat descriptor said private observations were placed at randomized coordinates, but the parser skips them
as the prompt requires (the descriptor text now says so); the summary described a five-page walk that the
2/min budget could not serve.

Implementation commit: 32be0a365cdac7fc83a9c91f6c84f4de4c0f1c08
