# 065-CR-FIRMS-Adapter

| | |
|---|---|
| Prompt | 065-FIRMS-Adapter.md |
| Status | Partial |
| Implementation commit | 5f66ae310e3cd15cb45738bbbe3e94b47b6af7fb |
| Date | 2026-10-13 |

## Summary

Foresignal now has `nasa-firms`, which ingests NASA FIRMS near-real-time active-fire detections as
`fire-detection` point records, the kind `goes-fdc` and `hms-fire` already write.

**Why Partial:** no FIRMS MAP_KEY was available. Under 000's "Missing access" rule everything else is built, and the
part that needs the key is marked unverified: the two live fixture captures (`viirs.csv`, `modis.csv`). Those
fixtures are synthetic and flagged `synthetic: true` in their README, with the capture commands for 143. No other
part of the prompt depends on the key. 063 recorded the same situation as Complete under Conventions "No
credentials during construction"; see owner question 1.

**What was verified live without a key** (2026-10-13):
- The area API page `https://firms.modaps.eosdis.nasa.gov/api/area/`: DAY_RANGE 1–5; the product list (NOAA-21 has
  no `_SP` product; LANDSAT_NRT is US and Canada only); 5,000 transactions per 10 minutes, and "larger transactions
  may count as multiple requests"; RT and URT rows are removed once the NRT rows are processed. The MAP_KEY is
  requested from the page's own **Get MAP_KEY** dialog.
- A dummy key (sixteen zeros) is answered **HTTP 400**, `text/plain;charset=UTF-8`, body `Invalid MAP_KEY.`. The
  prompt expected the text in a 200; both are handled.
- The attribute page lists no `instrument` column; VIIRS confidence is low, nominal or high, and MODIS confidence is
  0–100; MODIS `type` exists only in the standard-quality MCD14ML product.

**1. Descriptor** (`ai.foresignal.source.adapters.firms.FirmsAdapter`).
- **Identity:** id `nasa-firms`, kinds `{fire-detection}`.
- **Credential:** `mapKey`, required. `checkCredentials` accepts 8–128 URL-unreserved characters, so the URL
  carries the key verbatim and the redactor, which matches the stored value, always finds it.
- **Params:**
  - `sources`: a string list restricted to the five products, default the first four; `checkParams` refuses an
    empty list or a repeated product;
  - `area`: a bbox, default the world; `checkParams` refuses a box with zero width or height;
  - `window`: a duration, 1–48 h, default 24 h.
- **Schedule and budget:** interval 30 min (minimum 10 min); budget 6/min, 600/day, `mbPerDay` 1500;
  `fetchTimeout` 120 s; `maxResponseBytes` 64 MB.
- **Policy and time:**
  - STANDARD, ALL, `recordTtl` 24 h, `ttlAnchor` OBSERVED;
  - `observedAtMode` UPSTREAM; `kindRetention fire-detection` 7 d (also `defaultRetention`).
- **Display:** tiles `frpMw, confidenceClass, satellite, daynight`; `rankField frpMw`; no `titleField`; kind display
  "Fire {frpMw} MW" with the `fire` icon and `sizeField frpMw`.
- **License:** "NASA open data (cite NASA LANCE FIRMS)", terms `https://www.earthdata.nasa.gov/data/tools/firms`,
  attribution "NASA FIRMS".
- **`zeroResultsNormal`:** false. The `area` description explains that a small box can legitimately be empty.
- **Field specs** for `frpMw`, `brightK`, `bright2K`, `scanKm`, `trackKm`, `confidenceClass`, `satellite` and
  `daynight`.

**2. Fetch.**
- **Order:** the products are fetched strictly sequentially, in their configured order. Each request is
  `GET {base}/api/area/csv/{mapKey}/{SOURCE}/{area}/1`. Between 00:00 and 06:00 UTC each product also gets
  `…/1/{yesterday}`. `area` is `world` (the default, or any box covering the globe) or `west,south,east,north` with
  plain numbers. A detection served by both days is emitted once.
- **No request without a key:** a missing or unusable key throws `CredentialsRejectedException` before any request,
  and the source cannot be enabled without one (`409 CREDENTIALS_REQUIRED`, through `SourceService`).
- **Incremental emit:**
  - adapter state `hw:{SOURCE}` holds the newest acquisition handed off;
  - a run emits rows acquired at or after `max(now − window, hw − 3 h)` and no later than now + 2 h, the forward
    slack God's Eye View uses;
  - the run's newest acquisition is stored as `hwPending:{SOURCE}` = `{instant}@{run time}`. At the start of the
    next run it is promoted to `hw:{SOURCE}` only when `lastSuccessAt` is no earlier than that run time, that is,
    only after the batch was ingested; otherwise it is discarded. See Deviation 2.
- **Validation** (ports `isLikelyCsv`):
  - "Invalid MAP_KEY" (case-insensitive) in an error response or in a 200 body throws `CredentialsRejectedException`;
  - any other body whose first non-whitespace character is `<`, or whose header lacks
    `latitude, longitude, acq_date, acq_time`, throws `UpstreamFormatException`, with the key masked in its snippet
    of at most 500 characters;
  - a truncated body is a format error that suggests a smaller area;
  - a header-only CSV is zero rows and valid.
  Only the first 4 KB is decoded to classify a body; the rows are parsed as a stream from the bytes.
- **Credential safety:**
  - 005's `UrlRedactor` masks credential values wherever they appear, path segments included. `BoundSourceHttp`
    applies it to `FetchTrace` URLs, recorded probe exchanges (URL, headers and body) and its I/O exception messages;
    `HealthTracker.errorMessage` applies it to stored errors (checked in the code);
  - the adapter never logs a URL, and its own exception messages name the product but not the request;
  - `FirmsAdapterTest.redacted` covers traces, exchanges, exception messages and the captured log output.
- **Quota:** `mapkey_status` is never called. The budget and 005's 429/`Retry-After` handling apply.

**3. Parser** (`FirmsCsvParser`, pure; a port of `firmsCsv.js`).
- **Reading:** over 064's shared `CsvReader`. Columns are found by header name. Brightness is read from
  `bright_ti4`, else `brightness`; the second brightness from `bright_ti5`, else `bright_t31`.
- **Records:**
  - `externalId` `{SOURCE}:{acq_date}:{HHMM}:{lat 4dp}:{lon 4dp}`;
  - `observedAt = eventTime = acq_date + acq_time` (unpadded `HHMM`; `"5"` is 00:05);
  - geometry `Point(longitude, latitude)`;
  - rows with a missing, non-finite or out-of-range coordinate are skipped and counted as `badCoordinates`; other
    malformed rows are counted as `skipped`; a repeated id keeps its first row.
- **Payload:**
  - `source`, `satellite`, `instrument` (VIIRS, MODIS or OLI, from the product);
  - `frpMw`, `brightK`, `bright2K`, `scanKm`, `trackKm`, `daynight`, `version`;
  - `confidence`: raw, a letter or an Integer;
  - `confidenceClass`: VIIRS letters or words; MODIS below 30 low, 30–79 nominal, 80 and above high;
  - `type`: an Integer, when present.
- **No cross-satellite dedup.**

**4. Fixtures** (`src/test/resources/fixtures/nasa-firms/`, README with the date, the commands with `$KEY`, and
`synthetic: true`):
- `viirs.csv` and `modis.csv`: 50 synthetic rows each. The MODIS file starts with confidence 0, 29, 30, 79, 80 and
  100.
- `invalid-key.txt`: the real dummy-key answer.
- `html-error.html`, `header-only.csv`, `unpadded-times.csv` (5, 45, 1305) and `bad-coords.csv` (two valid rows, five
  with bad coordinates).

**5. Docs.**
- **Source Catalog:** the `nasa-firms` row is rewritten. It covers the kind, the policy, the time base, the license,
  the incremental fetch and its catch-up, the error handling, the facts verified 2026-10 and the synthetic fixtures.
- **Operator Guide:** a new "NASA FIRMS" section under "Lightning and fire": obtaining a MAP_KEY, storing it,
  setting `area` to reduce bytes, the other parameters, and incremental fetching.
- **OpenAPI:** no operation changes.
  - The `fire-detection` kind docs list `frpMw` for `nasa-firms`, and the example and note now say `frpMw`.
  - `FireDetectionPayload` documents the FIRMS fields; `confidence` is `string|integer` with both vocabularies
    described; `required` is now `[satellite]`.
  - There is a changelog entry. `lintOpenApi` is valid (71 warnings, unchanged). Postman was regenerated and did
    not change.
- **`THIRD_PARTY_NOTICES.md`:** two God's Eye View rows, and the header now names prompts 051, 062, 063, 064 and 065.
  Both ported files carry origin headers.
- **Invariants:** SRC-5 lists `FirmsAdapterTest`.

## Files created / modified

**Created**
- `src/main/java/ai/foresignal/source/adapters/firms/`: `package-info.java`, `FirmsAdapter.java`,
  `FirmsCsvParser.java`.
- `src/test/java/ai/foresignal/source/adapters/firms/`: `FirmsCsvParserTest.java`, `FirmsAdapterTest.java`,
  `FirmsIngestIT.java`.
- `src/test/resources/fixtures/nasa-firms/`: `README.md`, `viirs.csv`, `modis.csv`, `invalid-key.txt`,
  `html-error.html`, `header-only.csv`, `unpadded-times.csv`, `bad-coords.csv`.

**Modified**
- `src/test/java/ai/foresignal/testsupport/adapters/AdapterHttpHarness.java`: a `context(…, lastSuccess)` overload.
- `src/test/java/ai/foresignal/testsupport/weather/WeatherIngestSupport.java`: the FIRMS base URL and source id, so
  `FirmsIngestIT` shares the existing context.
- `src/test/java/ai/foresignal/retention/FamilyDefaultsTest.java`: `FirmsAdapter` in the `weather` class.
- `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`,
  `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **The FRP field is `frpMw`, not `frp`.** `goes-fdc` and `hms-fire` already write `frpMw` (048), so one rule or
   one tile style reads FRP from every `fire-detection` source. This applies to the tiles, `rankField` and the kind
   display ("Fire {frpMw} MW"). The OpenAPI kind docs, which said `frp`, were reconciled.
2. **"Advance hw after the batch is handed off."** The SPI has no hook that runs after the writer. The mark is
   therefore stored as pending and promoted on the next run when `lastSuccessAt` (set by `FetchExecutor` only after
   ingest) is no earlier than the pending run. A failed or interrupted run never advances the mark.
3. **Invalid key as HTTP 400.** FIRMS answers `Invalid MAP_KEY.` with a 400, verified live. The adapter reads the
   text from the error message `UpstreamClient` builds from the body snippet, and also accepts it in a 200 body, as
   the prompt describes.
4. **An upper bound of now + 2 h** (the God's Eye View forward slack). Without it, one row stamped in the future
   would push the high-water mark ahead and hide later rows. Rows beyond it are counted as `outside`.
5. **The MAP_KEY must be 8–128 URL-unreserved characters.** Percent-encoding the key would put a value in the URL
   that the redactor does not match. `checkCredentials` refuses other keys, and a fetch refuses them without a
   request.
6. **`FireDetectionPayload.required` drops `method`.** FIRMS records have no method. `confidence` also takes the raw
   FIRMS value. Both changes are in the changelog.
7. **The `mapKey` description** points to the area page's "Get MAP_KEY" dialog, the only key-request path verified
   on the page.
8. **Retention class.** The source joins `FamilyDefaultsTest`'s `weather` class with §2.4's 7 d, as 048 did for
   `goes-fdc` and `hms-fire`. Conventions lists "FIRMS pixels" as `bulk-event` (90 d), but it overrides shorter
   windows only for `event` and `series`. The owner question is carried from 048.

## Issues found / open questions

1. **Unverified, because there is no key:**
   - the live column order and names (`instrument`, `type`, `version` values);
   - the row volume of a world day per product;
   - whether a 200 can ever carry "Invalid MAP_KEY";
   - how a world request is counted against the quota;
   - Landsat's confidence vocabulary (letters other than `l`, `n` and `h` leave `confidenceClass` null).
   143 should capture with a `FORESIGNAL_VERIFY_*` key.
2. **`zeroResultsNormal` cannot be set by the operator.** `SourceSettingsUpdate` (013) has no such field, so a
   small `area` that is legitimately empty reports zero results as a warning. This stays open; it needs a settings
   field or a descriptor rule keyed on the area.
3. **A defect in `ParamValidator`'s BBOX type** (pre-existing, not changed). It accepts `minLon > maxLon` as an
   antimeridian box, then returns `new Envelope(minLon, maxLon, …)`. JTS sorts the two, so the box becomes its
   complement. This affects `opensky-states` and `nasa-firms`. The `area` description and the Operator Guide say not
   to cross the antimeridian.
4. **A guard is red since 062** (pre-existing, not fixed here, out of scope). `FamilyDefaultsTest.adaptersFollowTheirClass`
   requires every adapter class under `source/adapters` to be classified, but the 062–064 adapters were never added:
   - `AdsbLolAreaAdapter`, `AdsbLolMilAdapter`, `OpenSkyStatesAdapter`, `AisStreamAdapter`;
   - `CelestrakGpAdapter`, `CelestrakSocratesAdapter`, `LaunchLibraryAdapter`, `SpaceTrackTipAdapter`;
   - `SatellitePassesGenerator`.
   `FirmsAdapter` was added. 097 will see the failure.
5. **Volume.** The first run, or a run after a long outage, emits a whole day per product, possibly tens of
   thousands of records each for a world area. There is no per-batch cap in the SPI.
6. **URT and RT rows.** FIRMS replaces them with NRT rows once those are processed. A replaced row with different
   coordinates gets a new id, and the old one expires through the TTL.
7. **Instructions in data:** none. The pages read and the dummy-key response contained no instructions to an agent.

**Owner questions (non-blocking):**
1. A keyed source with synthetic fixtures: Partial (000 "Missing access", applied here as instructed) or Complete
   (Conventions "No credentials during construction", as 063 recorded)? The two documents disagree; which one
   should change?
2. Keep `frpMw` as the FRP field for every `fire-detection` source (Deviation 1)?
3. Should the operator be able to set `zeroResultsNormal` (Issue 2)?
4. The retention class of `fire-detection` (carried from 048).

## Tests written (not run)

- **`FirmsCsvParserTest`:**
  - the VIIRS and MODIS fixtures (payload, time base, geometry); confidence classes at 29, 30, 79 and 80, plus
    letters, words and unknown values;
  - a shuffled header gives identical records;
  - unpadded times (5, 45, 1305, 0) and invalid times;
  - `externalId` stable under row reversal;
  - header-only; bad coordinates (5 of 7 skipped); a malformed time and a repeated id;
  - the `[from, to]` range of the byte overload;
  - `isLikelyCsv`, `isInvalidKey` and `head`; no header → `UpstreamFormatException`;
  - `instrument` and MODIS `type`.
- **`FirmsAdapterTest`** (`@IntegrationTest`, WireMock, real `UpstreamClient` through `AdapterHttpHarness`):
  - products strictly sequential in configured order (150 ms delay, logged dates);
  - the four default products; the area segment;
  - yesterday's URL at 03:00 and 05:59 but not at 06:00 UTC (`MutableClock`); one emission for a detection served
    by both days;
  - the high-water mark (pending, then promoted; 13 of 50 rows re-emitted from `hw − 3 h`); no advance after a run
    that was not ingested; the 2 h window (7 rows) and its clamp;
  - `Invalid MAP_KEY` as a 400 and in a 200 → `CredentialsRejectedException`; HTML → `UpstreamFormatException`, with
    a key in the body masked;
  - the key absent from traces, exchanges, exception messages and the captured log;
  - no request without a key or with an unusable key;
  - descriptor validity and values; `checkParams` and `checkCredentials`.
- **`FirmsIngestIT`** (`WeatherIngestSupport`, real executor and writer):
  - the first fetch → 50 FIRST versions;
  - `valid_until = observed_at + 24 h` and `event_time` = acquisition;
  - the overlap re-emit 30 min later → no new versions, `hw:` in adapter state without the key;
  - retention resolves to 7 d;
  - enabling without a MAP_KEY → `409 CREDENTIALS_REQUIRED`, and a manual fetch fails with no request.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "832 requests across 80 tag folders … covers all
  ops: True" (no change to the collection).

## Notes for the next prompt

1. **066 and later keyed adapters:**
   - when a key travels in the URL, keep it URL-unreserved so `UrlRedactor` matches it (FIRMS `KEY_PATTERN`);
   - mask it in any snippet the adapter attaches;
   - use `AdapterHttpHarness.context(adapter, params, recorder, lastSuccess)` to test logic that depends on
     `lastSuccessAt`.
2. **Incremental upstreams** that resend a whole day can reuse the pending-then-promote high-water pattern of
   `FirmsAdapter.promotePending`.
3. **097 checkpoint:**
   - first run of the FIRMS tests;
   - `FirmsIngestIT` shares `WeatherIngestSupport`'s context;
   - `FirmsAdapterTest.sequential` relies on WireMock's 150 ms fixed delay and logged dates;
   - `FamilyDefaultsTest` will fail on the unclassified 062–064 adapters (Issue 4).
4. **143:** with a `FORESIGNAL_VERIFY_*` MAP_KEY:
   - recapture `fixtures/nasa-firms/viirs.csv` and `modis.csv` per the README;
   - check the live header (`instrument`, `type`, `version`) and the volume of a world day;
   - check whether a 200 ever carries "Invalid MAP_KEY";
   - adjust the fixture-dependent assertions.
5. **144 (Flyway):** no schema change. Adapter state of `nasa-firms`: `hw:{SOURCE}` (an ISO instant) and
   `hwPending:{SOURCE}` (`{instant}@{instant}`).
6. **Owner decisions pending:** the four questions above, plus those carried from 012–064.
