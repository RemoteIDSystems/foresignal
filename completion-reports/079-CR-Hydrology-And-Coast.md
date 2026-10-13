# 079-CR-Hydrology-And-Coast

| | |
|---|---|
| Prompt | 079-Hydrology-And-Coast.md |
| Status | Complete |
| Implementation commit | f31c9d5657d67f938fa31fead23bb853320f5269 |
| Date | 2026-10-13 |

## Summary

Seven river, snow, drought and coastal water sources are added under `ai.foresignal.source.adapters.hydro`, one
sub-package each:
- gauge, snow and tide readings use the Core-Data-Model §2 measurement time base (`observedAt = eventTime` =
  measurement time, `ttlAnchor=OBSERVED`) and the `state` retention class (365 d);
- drought areas and flood warnings use the `event` class (1825 d);
- all are STANDARD, policy ALL, and disabled by default;
- `-999`, `-9999` and `0001-01-01T00:00:00Z` become omitted fields;
- every numeric fieldSpec has a UCUM unit, except where the unit varies per record (see Deviations).

**Shared** (`hydro.HydroSources`):
- the test property `foresignal.test.adapters.hydro.base-url`;
- the skip counter `foresignal.hydro.skipped`;
- sentinel-aware number and instant readers, a JSON reader that turns `{"error": {"message": …}}` into
  `UpstreamFormatException`;
- the box-size check, refresh stamps, and field-spec helpers.

It reuses the general helpers of `env.EnvSources` (`Collector`, `round`, `validPoint`, `pack`/`unpack`, `body`).

**1. `usgs-water`** (`UsgsWaterAdapter`, `UsgsLatestContinuousParser`):
- reads `latest-continuous` through the prompt 073 `OgcFeaturesClient`: cursor paging, at most 20 pages;
- `bbox` (≤ 5° × 5°) or `monitoringLocations` (≤ 100) is required; `parameterCodes` defaults to `00060,00065`;
- optional `apiKey` credential, sent as `X-Api-Key`; `X-RateLimit-Remaining` is kept in state and logged;
- names come from `monitoring-locations` once per 24 h, cached in state;
- text values become numbers; `Ice`/`Eqp` go to `qualifier`; a `qualifier` list is joined with commas;
- TTL 3 h; 15 min (min 5 min).

**2. `nwps-gauges`** (`NwpsGaugesAdapter`, `NwpsGaugeMapper`):
- `bbox` (≤ 10°) is required; `maxGauges` defaults to and is capped at 300 (by lid);
- thresholds come from `/gauges/{lid}` at most once per gauge per 24 h, at most 10 per run (unknown first, then the
  oldest); a 404 caches "no thresholds"; cache entries of gauges no longer in the box are removed;
- stage and flow are sorted by unit; forecast fields appear only when the forecast is current; `stageToMinor` is
  added;
- a gauge whose observation time is the sentinel is skipped;
- TTL 3 h; 15 min (min 10 min); `tileProperties = name, floodCategory, stage`; no significant fields.

**3. `snotel`** (`SnotelAdapter`, `AwdbParser`):
- `states` or `stations` is required (≤ 200);
- the `stationTriplets=*:{ST}:SNTL` station list is read daily, or again when the parameters change;
- data comes in batches of 50 triplets;
- units: in → mm and cm, °F → °C, ft → m;
- `observedAt` is the newest value date at 00:00 in the station's `dataTimeZone`, as UTC;
- TTL 48 h; 6 h.

**4. `usdm`** (`UsdmAdapter`, `UsdmMapper`):
- conditional GET of `usdm_current.json`; a 304 is unchanged;
- map date = the latest Tuesday on or before `Last-Modified`, confirmed by a HEAD of `usdm_{yyyyMMdd}.json`; on a 404
  it steps back one week, once;
- each category is simplified by `FeatureGeometry` to ≤ 9,500 vertices; payload `category, label, mapDate,
  geometrySimplified, simplifyToleranceDeg`;
- every category missing from the map is tombstoned at once;
- no TTL; the required attribution.

**5. `noaa-coops-tides`** (`NoaaCoopsTidesAdapter`, `CoopsMapper`):
- `stations` (≤ 20) is checked against `stations.json?type=waterlevels`, cached 7 d without Great Lakes stations;
  unknown stations are skipped and leave the source degraded;
- each run reads `water_level&date=latest` (MLLW, metric, GMT);
- every 6 h it reads `predictions&interval=hilo` for today and tomorrow (next high and low after now);
- daily it reads `floodlevels.json` and `datums.json`;
- flood levels (feet above the station datum) are converted to meters above MLLW; `floodCategory` uses NOS, NWS as
  fallback;
- every data request carries `application=Foresignal`; TTL 1 h; 6 min.

**6. Optional sources:**
- **`ea-flood-warnings`** (`EaFloodWarningsAdapter`, `EaFloodParser`):
  - conditional GET; times read in Europe/London;
  - level 4 and vanished warnings are tombstoned;
  - message ≤ 4 KB, embedded;
  - polygons are fetched from a URL built from the area id, at most 30 per run, kept 30 d in state as base64 WKB.
- **`eccc-hydrometric`** (`EcccHydrometricAdapter`):
  - a prompt 073 feature spec built in code (box parameter, `datetime={now−2h}/..`, `sortby=-DATETIME`), run
    through `KitFeatureAdapter`;
  - the new spec option `latestPerKey` keeps the newest row per `STATION_NUMBER`.

**Kit and SPI additions:**
- `FeatureSourceSpec.latestPerKey`, honored by `KitFeatureAdapter` and checked by `FeatureSpecValidator`;
- `FeatureDescriptors.builder(spec)`;
- `OgcFeaturesClient.fetch(ctx, request, onPage)`, which hands each page's response to the caller;
- `SourceHttp.head(URI)`: a default that refuses, implemented by `BoundSourceHttp` through the single request path,
  so it is budgeted, metered and traced.

**Docs:**
- `docs/Source-Catalog.md`: a "Hydrology and coast (prompt 079)" section with all seven rows. It covers the required
  parameters, the USDM attribution, the NWPS threshold refresh cost, trigger examples and the legacy NWIS retirement,
  plus a prompt 079 note.
- `docs/Operator-Guide.md`: a "Flood watching" recipe (place-scoped box, NWPS and CO-OPS category triggers).
- `docs/Invariants.md`: a SRC-5 test entry.
- **No API change.** The OpenAPI kind entries already list every payload name used (the ECCC note says "per its
  spec").

**Acceptance:**
- All seven sources register disabled, with required parameters enforced by `ParamSpec.required` and `checkParams`.
- No adapter calls `waterservices.usgs.gov` (`grep -r waterservices src/main` matches only Javadoc and descriptor
  text that says it is not used).
- Compiles.

## Files created / modified

- **Created, main:** `source/adapters/hydro/`:
  - `package-info`, `HydroSources`;
  - `usgswater/{package-info, UsgsWaterAdapter, UsgsLatestContinuousParser}`;
  - `nwps/{package-info, NwpsGaugesAdapter, NwpsGaugeMapper}`;
  - `snotel/{package-info, SnotelAdapter, AwdbParser}`;
  - `usdm/{package-info, UsdmAdapter, UsdmMapper}`;
  - `coops/{package-info, NoaaCoopsTidesAdapter, CoopsMapper}`;
  - `ea/{package-info, EaFloodWarningsAdapter, EaFloodParser}`;
  - `eccc/{package-info, EcccHydrometricAdapter}`.
- **Modified, main:**
  - `source/spi/SourceHttp` (`head`), `collect/BoundSourceHttp` (HEAD);
  - `source/kit/feature/{FeatureSourceSpec, KitFeatureAdapter, FeatureSpecValidator, FeatureDescriptors,
    OgcFeaturesClient}`.
- **Created, test:**
  - `hydro/{HydroTestSupport, HydroAdaptersTest, HydroIngestIT}`;
  - `usgswater/UsgsLatestContinuousParserTest`, `nwps/NwpsGaugeMapperTest`, `snotel/AwdbParserTest`,
    `usdm/UsdmMapperTest`, `coops/CoopsMapperTest`, `ea/EaFloodParserTest`, `eccc/EcccHydrometricSpecTest`.
- **Created, fixtures** (each with a README): `usgs-water/`, `nwps-gauges/`, `snotel/`, `usdm/`,
  `noaa-coops-tides/`, `ea-flood-warnings/`, `eccc-hydrometric/`.
- **Modified, test:**
  - `testsupport/weather/WeatherIngestSupport`: the hydro base-URL property, and `nwps-gauges` and `usdm` in the
    backoff list;
  - `retention/FamilyDefaultsTest`: the seven adapters;
  - `kit/feature/FeatureSpecValidatorTest`: `latestPerKey`.
- **Docs:** `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`.

## Deviations from the prompt

1. **USDM is 23.8 MB, not 4.6 MB.** The 2026-10-06 map was 23,840,156 bytes, with no gzip even when asked.
   - `maxResponseBytes` is 48 MB instead of 16 MB, and `mbPerDay` is 100 instead of 30.
   - The per-minute budget is 2, not 1: a 200 needs the GET and the HEAD in the same minute, and the governor refuses
     rather than queues.
2. **The USDM attribution includes NASA.** The USDM permission page (read 2026-10-13) asks for wording that names
   NASA as a fourth partner. The descriptor uses that wording; the prompt's text omits NASA.
3. **Budgets raised where one run needs more than the per-minute budget:**

   | Source | Prompt | Built | Why |
   |---|---|---|---|
   | `usgs-water` | 4/min | 30/min | up to 20 pages plus name batches |
   | `nwps-gauges` | 4/min | 12/min | 1 list plus up to 10 details |
   | `snotel` | 2/min | 6/min | metadata plus 4 batches |
   | `noaa-coops-tides` | 20/min | 40/min | 20 stations plus at most 4 prediction and 4 threshold refreshes |

   Daily budgets are as prompted.
4. **USGS names by site id, not by box.** A 1° × 0.7° box around Denver holds more than 1,000 monitoring locations,
   most of them wells. The adapter therefore asks `monitoring-locations?id={the sites seen}` (100 per request,
   `properties=monitoring_location_name,site_type_code`, `skipGeometry=true`), still once per 24 h.
   - `state_name` is not read: the OpenAPI payload list for `usgs-water` has no state field.
5. **`X-RateLimit-Remaining` goes to adapter state and the log, not the fetch trace.** `FetchTrace` and `fetch_log`
   have no free-form field; adding one is a schema change beyond this prompt. Anonymous USGS responses carried no
   such header.
6. **Units that vary per record carry no fieldSpec unit:**
   - USGS `value`;
   - the NWPS stage, flow and threshold fields (their units are in `stageUnit`/`flowUnit`, as published).
   This follows 078 Deviation 7.
7. **NWPS `floodCategory` is stored as published.** It includes `low_threshold`, which the prompt's list lacks.
8. **CO-OPS details:**
   - Flood levels are referenced to the station datum (STND). The mdapi documentation does not say so. It was
     confirmed from data: The Battery's record 17.329 ft (2012-10-30) minus its MLLW (3.29 ft) is Sandy's 14.04 ft
     MLLW crest.
   - Great Lakes stations (no MLLW) are left out of the valid-station list.
   - Today's and tomorrow's predictions come in one request (`begin_date`/`end_date`).
   - Station validation happens at fetch time, because `checkParams` has no network access. Unknown stations are
     skipped and leave the source degraded; `checkParams` checks the format (7 digits) and the count.
9. **USDM tombstones** are emitted for every category absent from the map, without remembering which ones existed.
   The ingest policy skips a tombstone for an entity with no head.
10. **`eccc-hydrometric` is a hand-written adapter that builds its feature spec in code.** Kit JSON specs must be
    `feature-…`, cannot take operator parameters, and have a fixed `datetime`. Its box limit (10°) and TTL (3 h) are
    my choices; the prompt names neither.
11. **EA polygon URLs are built from the area id** (https), never taken from the data, whose links are `http://`.
12. **Retention classes:**
    - `state` for gauges, snow and tides;
    - `event` for `usdm` and `ea-flood-warnings` (hazard status and warnings, as "weather and CAP alerts").
13. **Spring constructors.** Each new `@Component` has exactly one `@Autowired` injection constructor:
    - `(String, ObjectProvider<MeterRegistry>)` for the five metered adapters, each with an unannotated
      `(String, Supplier<MeterRegistry>)` constructor for tests;
    - `(String)` for `UsdmAdapter` and `NoaaCoopsTidesAdapter`.

## Issues found / open questions

1. **USDM size.** About 24 MB per weekly change, parsed into a tree and simplified per category. The D0 category
   (138k vertices) was not timed here; the D3/D4 fixture took about 0.15 s.
2. **`snotel` uses a 2-day lookback with UTC dates.** A station whose newest day falls outside it produces no record
   (`no-values`).
3. **`docs/Source-Catalog.html`** was not regenerated (as in 070–078).
4. **US English:**
   - `check_us_english.py` on every new file: the only hits are the allowed `licence` builder, the JSON key
     `meta.licence`, and the license names "Open Government Licence".
   - `SourceHttp.java` already contained "honoured"; I did not change it.
5. **Instructions in data:** none. I read the upstream answers and the USDM, CO-OPS mdapi and ECCC pages; nothing in
   them addressed an agent.

**Owner questions (non-blocking):**
1. Accept the USDM attribution with NASA (Deviation 2) and the 48 MB / 100 MB-per-day USDM limits (Deviation 1)?
2. Accept the raised per-minute budgets (Deviation 3)?
3. Should `FetchTrace`/`fetch_log` gain a field for upstream rate-limit headers (Deviation 5)?
4. Retention: is `event` right for `usdm` and `ea-flood-warnings` (Deviation 12)?

## Tests written (not run)

- **Parser and mapper tests** (each also checks the descriptor, the fieldSpecs contract and
  `SnapshotContract.assertStableStaticHash`):
  - `UsgsLatestContinuousParserTest`: 8 readings, `SEASONAL` with a null value, `Ice`/`Eqp`, `-999`, units, the
    name-table round trip, `checkParams`;
  - `NwpsGaugeMapperTest`: sentinels, the threshold join, `stageToMinor`, forecasts, the state round trip, the list
    URL;
  - `AwdbParserTest`: unit conversions, local date → UTC (−8 h), skips, URLs, the triplet query;
  - `UsdmMapperTest`: the map date, simplification flags (D3 ≤ 9,500 vertices and valid; D4 whole), tombstones,
    format errors, the dated URL, attribution;
  - `CoopsMapperTest`: string numbers, hilo selection, the datum conversion (The Battery, San Francisco, NWS fallback,
    no MLLW), categories, error objects, the station list, URLs with `application`;
  - `EaFloodParserTest`: BST times, level-4 tombstone, the 4 KB cap, the WKB round trip;
  - `EcccHydrometricSpecTest`: spec, mapping, `newer`, the required box;
  - `FeatureSpecValidatorTest` (extended): `latestPerKey` needs `time.observedAt`.
- **`HydroAdaptersTest`** (WireMock, real upstream client):
  - USGS cursor paging, `X-Api-Key` absent from traces, exchanges and the log, names once per 24 h, the rate-limit
    state, the site list, no key;
  - NWPS details at most once per gauge per day (`MutableClock`: none at +23 h, again at +24 h), the `maxGauges` cap;
  - USDM 200 with the HEAD confirmation and tombstones, then a 304 with no HEAD; HEAD 404 → previous week;
  - CO-OPS `application=Foresignal` on every data request, unknown stations degrade the source, refresh cadence, an
    error object fails the run;
  - SNOTEL batches and the daily station list;
  - EA polygons fetched once, vanished warnings tombstoned;
  - ECCC paging and the newest row per station.
- **`HydroIngestIT`** (`@IntegrationTest` via `WeatherIngestSupport`):
  - all seven sources are registered and disabled, with required parameters enforced at PUT;
  - an NWPS category change creates a version with `floodCategory` updated;
  - an unchanged USDM week is a 304 with no versions.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `lintOpenApi` and the Postman generator were not needed: `docs/OpenAPI.json` is unchanged.
- No project tests were run (feature prompt).
- **Scratch check:** a throwaway `main` in the scratchpad, outside the repository, did two things (no project test
  was executed):
  - ran `DescriptorValidator` on all seven descriptors (no problems);
  - parsed every fixture to confirm the counts and values used in the assertions.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.

## Notes for the next prompt

1. **080 onward:**
   - hydro sources keep their caches in the adapter state store;
   - `SourceHttp.head` and `OgcFeaturesClient.fetch(ctx, request, onPage)` are available;
   - a feature spec with operator parameters can be built in code as `eccc-hydrometric` does.
   - Adapters do not import each other.
2. **097 checkpoint:**
   - first run of every 079 test;
   - `HydroIngestIT` uses `WeatherIngestSupport` (new property `foresignal.test.adapters.hydro.base-url`);
   - check WireMock's HEAD stubs, the decoding of `*:CO:SNTL` and `2026-10-13T20:15:00Z/..`, the negative-lookahead
     path regex in the EA test, and the hand-computed counts;
   - 074 Issue 1 (constructors in 070–073) still applies.
3. **143:**
   - recapture per the fixture READMEs;
   - with `FORESIGNAL_VERIFY_*`, check whether USGS sends `X-RateLimit-*` with a key;
   - re-check the USDM size and gzip, and the USDM attribution wording;
   - time the D0 simplification.
4. **144 (Flyway):** no schema change. Adapter state:
   - `usgs-water`: `usgs:names` (gzip above 32 KB), `usgs:namesAt`, `usgs:rateLimitRemaining`;
   - `nwps-gauges`: `nwps:gauge:{lid}`, `nwps:lids`;
   - `snotel`: `snotel:stations`, `snotel:stationsAt`, `snotel:query`;
   - `usdm`: `etag:usdm-current`, `lastmod:usdm-current`;
   - `noaa-coops-tides`: `coops:stations`, `coops:stationsAt`, `coops:pred:{id}`, `coops:predAt:{id}`,
     `coops:flood:{id}`, `coops:floodAt:{id}`;
   - `ea-flood-warnings`: `etag:/lastmod:ea-floods`, `ea:active`, `ea:polygon:{id}`, `ea:polygonIndex`.
5. **Owner decisions pending:** the four questions above, plus those carried from 012–078.
