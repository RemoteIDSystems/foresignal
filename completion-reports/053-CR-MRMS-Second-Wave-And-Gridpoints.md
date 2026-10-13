# 053-CR-MRMS-Second-Wave-And-Gridpoints

| | |
|---|---|
| Prompt | 053-MRMS-Second-Wave-And-Gridpoints.md |
| Status | Complete |
| Implementation commit | 90f84dd8d3a6f107fd5499bdb87d81492072d892 |
| Date | 2026-10-13 |

## Summary

This prompt adds the P1 hydrology and severe-storm layers:
- four more MRMS products on the existing `mrms` source;
- an `nws-gridpoints` source: an official NWS forecast and a 72-hour series for each place;
- `spc-storm-reports`, moved here from 089;
- the new data in the `precip` and `forecast` sections of `place-weather`.

There are no new API operations.

**1. MRMS second wave** (`MrmsAdapter`, `MrmsCodec`)

Every upstream fact below was checked on 2026-10-13 against a full-day S3 listing, the captured files decoded
with NetCDF-Java, and the MRMS GRIB2 table that NetCDF-Java ships (`UserTable_MRMS_v12.2.csv`).

| Product | Field | Stored | Default `takeEvery` | Gap |
|---|---|---|---|---|
| `mrms-rotation-60m` | 209/3/3 at 500 m | u16 s⁻¹ (code × 10⁻⁴ − 10⁻⁴) | 1 | 6 min |
| `mrms-qpe-1h` | 209/6/2 | u16, 0.1 mm | 5 | 30 min |
| `mrms-qpe-24h` | 209/6/41 | u16, 0.1 mm | 1 | 150 min |
| `mrms-ffg-ratio-1h` | 209/12/26 at the surface | u16 percent, 0.1 % | 5 | 30 min |

- **Thinning.** Each product has a fixed `takeEvery` parameter: `rotationTakeEvery`, `qpe1hTakeEvery`,
  `qpe24hTakeEvery` and `ffgTakeEvery` (1–30, checked in `checkParams`).
  - `MrmsAdapter.thin` cuts the key timestamps into epoch-aligned `2 min × takeEvery` buckets. It commits
    the first listed file of each bucket after the cursor's bucket.
  - Files it passes over are never downloaded, and the cursor moves past them.
  - It never adapts to the weather.
- **Rotation is reduced 2 × 2 by maximum.** The rotation file is on a 0.005° grid (14,000 × 7,000), so it is
  reduced to the 0.01° grid the other products share, keeping every peak.
  - `MrmsCodec.rotationCoarsened` does the reduction in one pass. `MrmsAdapter.coarsen` builds the coarse
    grid, and it equals the reflectivity grid exactly.
  - The frame records `stats.reducedFrom = "14000x7000"`.
- **Sentinels.**
  - Rotation: the MRMS table gives 0 for both "missing" and "no coverage". So 0 is stored as code 1
    (`noEcho`, 0 s⁻¹), and only a bitmap-missing sample is no coverage (code 0).
  - QPE: −3 (no coverage) and −1 (missing) are both stored as 65,535 (no coverage).
  - FFG ratio: −999 is stored as 65,535 (no coverage).
- **Products.**
  - Styles: `rotation@1` and `ffg-ratio@1` are new, with legends and meanings ("≥ 0.010 s⁻¹: strong low-level
    rotation", "≥ 100%: rainfall exceeds flash-flood guidance"). `qpe@1` already existed (051).
  - All four products have native max zoom 8, `coverage: CONUS` and `maxZoomForAnimation` 7, so they are
    warmed like reflectivity.
  - Ladders: the MRMS default for three of them; `mrms-qpe-24h` keeps frames to 48 h (hourly files).
  - Health: the existing per-product frame-age rule, using each product's `maxGap`.
- **Measured budget.** From the full listing of 2026-10-12:

  | Product | Every file | At the defaults |
  |---|---|---|
  | Rotation | 187 MB/day | 187 MB/day |
  | QPE 1 h | 607 MB/day | 121 MB/day |
  | QPE 24 h | 130 MB/day | 130 MB/day |
  | FFG ratio | 104 MB/day | 21 MB/day |
  | **Second wave** | **1.03 GB/day** | **≈ 0.46 GB/day** |

  - The first wave measured 2,082 MB/day, so all seven products total ≈ 2.5 GB/day.
  - That is under 3,500, so `mbPerDay` stays 4,000.
  - Request budget: about 9,700 a day, within the existing 12,000.
  - The figures are in the `MrmsAdapter` Javadoc and the Source Catalog.

**2. `nws-gridpoints`** (`NwsGridpointsAdapter`, `NwsGridpointsParser`; POLLING, kind `nws-forecast`)
- **Places.**
  - The places come from a new SPI default, `PlacePoints.summaries()`, which `PlaceService` implements.
  - With `allPlaces` (default true) every place is selected; otherwise only places whose `products` contain
    `nws`.
  - At most `maxPlaces` places are forecast (default 50, by id). More raise a health warning.
- **Resolution.**
  - `/points/{lat},{lon}`, with 4 decimals and trailing zeros removed.
  - It is cached in adapter state as `point:{id}`, and resolved again weekly, after a 404 from the gridpoint,
    or when the place's point moves (that also drops the old gridpoint's validators).
  - A 404 from `/points` marks the place as outside NWS coverage. It is named in the health warning and
    retried weekly.
  - `forecastGridData` is followed only when it is an `https://api.weather.gov/gridpoints/{WFO}/{x},{y}`
    URL, and it is requested under the configured base.
- **Fetching.**
  - Each place is fetched hourly at a fixed minute derived from its id, so the places spread over the hour.
  - At most 4 places per 60-second run, which keeps the source under 10 requests a minute.
  - The gridpoint request is a conditional GET (`If-Modified-Since`); a 304 keeps the record.
  - 429 and 503 honor `Retry-After` through the source backoff and end the run.
  - A 500 or other failure of one place keeps its last forecast and retries it in 10 minutes. Three in a row
    degrade the source.
  - The User-Agent and contact are the client's, as for `nws-alerts`.
- **Parsing.**
  - ISO-8601 interval `validTime`s are expanded into an hourly series for 72 h from the current hour.
  - `quantitativePrecipitation` is divided evenly over its interval's hours.
  - Wind is converted from km/h to m/s.
- **Record** (`place:{placeId}`):
  - payload `place`, `wfo`, `gridX`, `gridY`, `popMax24h`, `qpf24hMm`, `tMaxC24h`, `tMinC24h`,
    `gustMaxMs24h`, `hazards[]` (at most 10; type `phenomenon.significance`, for example `FZ.W`, plus its
    `validTime`) and `forecastUpdated`;
  - `observedAt` = `updateTime`; no TTL; STANDARD, ALL, US-PD, `titleField` `place`; retention 7 d;
  - fieldSpecs for every key; **no geometry** (Deviation 4).
- **Series cache.**
  - Table `forecast_series_cache(place_id, provider, updated_at, valid_from, series jsonb)`: the new
    `place.ForecastSeriesCacheEntity` (Hibernate DDL), written through the new SPI `ForecastSeriesSink`.
  - `place.ForecastSeriesCache` implements the sink. It replaces the row on each successful fetch and
    deletes a deleted place's rows on `PlaceChangedEvent`.
  - The adapter tombstones the record of a place that left the selection, and drops its series and state, on
    its next run.

**3. `spc-storm-reports`** (`SpcStormReportsAdapter`, `SpcStormReportsParser`; POLLING, kind `storm-report`)
- **Fetching.** `today.csv` every 10 minutes and `yesterday.csv` hourly, both as conditional GETs (ETag).
- **Parsing.**
  - The three CSV sections are detected by their own headers.
  - Comments are parsed with quote handling; an unquoted comma is joined back.
  - The office (`wfo`) comes from the comment's trailing `(XXX)`.
  - `comments` is kept to 500 characters.
- **Convective date.**
  - The current day D is the UTC date of `now − 12 h`. `today.csv` covers D and `yesterday.csv` covers D − 1.
  - A file whose `Last-Modified` is before 12 UTC on its date still holds the previous day.
  - Rows before 1200 fall on the next calendar date.
- **externalId:** the first 12 hex digits of SHA-256 over `type|convDate|HHMM|lat|lon|location`. Today's
  reports seen again in `yesterday.csv` keep their ids.
- **Magnitudes:**
  - tornado: the rating string (`EF1`);
  - wind: **mph** (Deviation 3);
  - hail: inches;
  - `UNK` is omitted, together with its units.
- **Record:**
  - title "Hail 1.75 in — 5 WNW Oliver, GA";
  - point geometry; `eventTime` = `observedAt` = the report time;
  - never tombstoned; no TTL; `zeroResultsNormal`;
  - `embedFields` `comments, location`; fieldSpecs for every key; retention 30 d;
  - budget 2/min and 400/day.

**4. Place weather** (`WxPlacesGenerator`, `PlaceWeatherState`, `WxAreasGenerator`, `ForecastSeriesService`)
- **`precip` section.** It adds `qpe1hMmMax`, `qpe24hMmMax` and `ffgRatio1hPctMax`: maxima within the place
  radius, in whole units.
  - Each input is its own `WxSection`, judged against its product's gap.
  - `precip.status` is the worst of the rate and of the inputs that have had data, in the order unavailable,
    then no-coverage, then stale. `asOf` is the oldest last-good time among the inputs that are not ok.
  - An input whose product is disabled or not installed leaves the section entirely
    (`WxSection.clear()`).
  - `reconcile` splits a stored precip section back into its parts.
- **`forecast.nws`** = `{status, popMax24h, qpf24hMm, gustMaxMs24h}`, from the place's `nws-forecast` head.
  - `wx-places` now also subscribes to `nws-forecast` changes of `nws-gridpoints`, and re-judges them on its
    60 s tick.
  - It is `stale` when `forecastUpdated` is older than 6 h (values kept, `asOf` = `forecastUpdated`), and
    `unavailable` while the source is disabled or failing, or has no forecast for the place.
- **`wx-areas`** accepts the fields `rotation` (s⁻¹; `maxValue` to 0.001), `qpe1h` and `ffg` (gap 30 min).
- **Forecast series.**
  - `ForecastSeriesService.PROVIDERS` gains `nws`, read from the cache for a place. Its status is `stale`
    after 6 h, its `run` is null, and it is left out when the caller may not read `nws-gridpoints`.
  - `GET /v1/places/{id}/weather` and the `weather_forecast` MCP tool pass the place id. For `nws` the tool
    reports `coverage: place` and `updatedAt`.
- **Recipes** in `docs/Operator-Guide.md` "Weather alerts":
  - "Flash-flood guidance exceeded near home": `precip.ffgRatio1hPctMax >= 100`, `enter`;
  - "Tornado report within 25 km": `kind=storm-report`, `type == tornado`, `near{home, 25}`, `match`,
    `onlyNew`.

**5. Spec, Postman, docs**
- `docs/OpenAPI.json`:
  - `Record.x-foresignal-kinds` gains `nws-forecast` and `storm-report`;
  - the `place-weather` note documents the new precip keys and `forecast.nws`; the `wx-area` note the new
    fields;
  - `StormReportPayload` wind units become mph, and `magnitudeUnits` gets the enum `EF|mph|in`;
  - `NwsForecastPayload` is documented without geometry;
  - product examples: second-wave `maxGap` values and `maxZoomForAnimation`;
  - a changelog entry.
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged). The Postman generator ran with no diff, since no
  operation changed.
- `docs/Source-Catalog.md`:
  - new section "MRMS second wave, gridpoints and storm reports (prompt 053)", with the four products (sizes,
    `takeEvery`, MB/day) and both record sources;
  - `spc-storm-reports` moved out of the 089 ("→ 051") list;
  - the `mrms`, `wx-places` and `wx-areas` rows updated.
- `docs/Operator-Guide.md`: the recipes, NWS forecast setup, and the new `wx-areas` fields.
- `docs/Agent-Guide.md`: the `weather_forecast` row.
- Fixture READMEs: `mrms` (extended), `nws-gridpoints`, `spc-storm-reports`.

## Files created / modified

**Created, main:**
- `source/adapters/nws/NwsGridpointsAdapter`, `NwsGridpointsParser`
- `source/adapters/spc/SpcStormReportsAdapter`, `SpcStormReportsParser`
- `source/spi/ForecastSeriesSink`
- `place/ForecastSeriesCache`, `place/ForecastSeriesCacheEntity`
- `resources/raster/styles/rotation@1.json`, `ffg-ratio@1.json`

**Modified, main:**
- `source/adapters/mrms/`: `MrmsAdapter`, `MrmsCodec`, `package-info`
- `source/adapters/wx/`: `WxPlacesGenerator`, `PlaceWeatherState`, `WxSection` (`clear()`), `WxAreasGenerator`
- `source/adapters/nws/package-info`, `source/adapters/spc/package-info`
- `source/spi/PlacePoints` (`summaries()`, `Summary`)
- `place/PlaceService` (implements `summaries()`)
- `raster/sample/ForecastSeriesService`
- `api/PlaceWeatherController`, `mcp/tools/WeatherForecastTool`

**Build:** `build.gradle.kts` pins the 4.7 MB QPE 24 h fixture in `downloadTestFixtures`.

**Docs and spec:** `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

**Test resources:**
- `fixtures/mrms/`: three new `.grib2.gz` files, and the README;
- `fixtures/nws-gridpoints/`: `points-VEF-55-177.json`, `gridpoints-VEF-55-177.json`, README;
- `fixtures/spc-storm-reports/`: `today-20261012.csv`, `yesterday-20261011.csv`, `three-sections.csv`
  (synthetic), `header-only.csv`, README.

**Tests:** see the table below. Test support changed in `DecoderFixtures` (second-wave constants,
`mrmsDownloadedOrSkip`), `SampleFixtures` (`bands`) and `WeatherIngestSupport` (the two new sources in
`SOURCES`).

## Deviations from the prompt

1. **Rotation is reduced to 0.01°.**
   - The prompt assumes the shared MRMS grid, but `RotationTrack60min_00.50` is published at 0.005°
     (98 M cells).
   - Stored as is, a decoded frame would be 196 MB in the frame cache, against the 49 MB of the other
     products (the small profile runs `-Xmx4g`).
   - So the adapter reduces it 2 × 2 by maximum, which keeps every peak and matches native max zoom 8.
2. **Rotation units and zero.**
   - The file holds 0.001 s⁻¹, not s⁻¹. It is stored as s⁻¹ at 10⁻⁴ steps, as the prompt asked.
   - The MRMS table defines 0 as both missing and no coverage, so there is no no-coverage sentinel to map.
     A 0 is stored as `noEcho` (0 s⁻¹) and the coverage meaning says so.
   - Mapping 0 to no coverage would make `wx-areas` rotation areas permanently `no-coverage`. See Issue 2.
3. **SPC wind speed is mph, not knots.**
   - The CSV row `1215,73,22 SE Meadview,…` carries the comment "measured a gust of 73 mph".
   - The report pages do not state the unit.
   - `magnitudeUnits` is `mph`, and the spec's `StormReportPayload` was amended (changelog).
4. **`nws-forecast` has no geometry.**
   - The prompt says "geometry = place point". 052 removed geometry from `place-weather` because a place's
     position is sensitive and would reach read tokens through `/v1/records` under `openReads` (052
     Issue 1, open).
   - The same reasoning applies here, so the record carries none. The spec description says so.
   - `wfo`, `gridX` and `gridY` (required by the spec) still locate the 2.5 km cell. See Issue 1.
5. **QPE 24 h `maxGap` is 150 min, not 90.**
   - Files arrive about 59 minutes after their valid time (`…-030000` was last modified at 03:58:56), so the
     newest frame is 59–119 minutes old.
   - A 90-minute gap would flap the source to degraded every hour. The spec example was updated.
6. **The 10-minute products have a 30-minute gap.** QPE 1 h and the FFG ratio are taken every 10 minutes by
   default, so their gap is 3 × 10 min, the same ratio as the 2-minute products' 6 minutes.
   - The product spec is static, so an operator who raises `takeEvery` past 10 (20 minutes) will see the
     frame-age rule degrade. The parameter description, Source Catalog and Javadoc say how the gap is set.
7. **QPE 24 h fixture is downloaded.** The newest file was 4.7 MB (a wet day), over the 2 MB limit for
   committed fixtures. It is pinned in `downloadTestFixtures`, and its test skips without it, like the HRRR
   fixtures. The other three fixtures are committed (0.15–0.69 MB).
8. **SPC file dates use `Last-Modified`.** "The convective date comes from the file and the clock": a file
   last modified before 12 UTC on its expected date is read as the previous day. This covers the window
   after 12 UTC before SPC rewrites `today.csv`. A missing or unparsable header falls back to the clock.
9. **Precip status merge.** "Each keeps the section's status/asOf rules" is implemented as one `WxSection`
   per input, merged into one status.
   - The section status is the worst of the rate and of the inputs that have had data.
   - An input that never had data cannot make the section unknown; for example, QPE 24 h before its first
     hourly file.
   - A disabled product's input leaves the section.
10. **`nws-gridpoints` tombstones deselected places**, not only deleted ones. A place that leaves the
    selection (for example `allPlaces=false` without the `nws` product, or past `maxPlaces`) is tombstoned
    too, so its record cannot go silently stale.
11. **Accumulation rounding.** Series `precipMm` is rounded to 0.01 mm (hourly shares of 6-hour totals are
    small). Summaries use 0.1, as do the temperatures and gusts.

## Issues found / open questions

**Needs the owner** (non-blocking; defaults chosen and documented):
1. **Place position in `nws-forecast`** (Deviation 4). The record has no geometry, but `wfo`, `gridX` and
   `gridY` locate the place to about 2.5 km for any reader of `/v1/records`. **Owner:**
   - keep this;
   - drop the grid fields from the payload (the spec marks them required);
   - or mark `nws-gridpoints` private by default?
   - This is tied to 052 Issue 1.
2. **Rotation zero** (Deviation 2). MRMS cannot tell "no rotation" from "no radar" on this product.
   **Owner:** accept 0 as `noEcho` (current), or treat 0 as no coverage, which makes rotation areas and
   samples always `no-coverage`?
3. **Retention class of `storm-report`.** It is filed under the provisional `weather` class (30 d, §2.4), as
   047 did for SPC outlooks. Conventions' `event` class (1825 d) arguably covers storm reports. **Owner:**
   which?
4. **Wind units** (Deviation 3). mph is inferred from the data. **Owner:** confirm, or point to an SPC
   statement of the unit.

**Other findings:**
- **Not verified at run time:** no tests ran. In particular:
  - the Spring wiring of `ForecastSeriesCache` (JSON insert) and of the two new adapters;
  - the Hibernate DDL of `forecast_series_cache`;
  - the derived runner's dispatch of `nws-forecast` changes to `wx-places`;
  - the memory peak of a rotation decode: the decoder's 98 M-float array, 392 MB, on the decode pool.
- **Upstream data** was fetched only from official endpoints, with an identifiable User-Agent:
  - anonymous S3 listings and objects from `noaa-mrms-pds`;
  - `api.weather.gov` `/points`, `/gridpoints` and `/alerts/active` (to find a gridpoint with a hazard);
  - `www.spc.noaa.gov` report CSVs, `yesterday.html` and the FAQ.

  The NSSL MRMS table page failed TLS verification locally, so the table NetCDF-Java ships was used
  instead. No content contained instructions directed at an agent.
- Owner decisions carried from 012–052 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `mrms/MrmsSecondWaveDecodeTest` | One real file per product. Rotation: field 3/3, 14,000 × 7,000 at 0.005°, `reduction` 2, `coarsen` equals the reflectivity grid, code counts after the reduction (24,408,713 × code 1, 91,287 values, none no coverage, maximum 491 = 0.049 s⁻¹). QPE 1 h: −3 count, zeros, maximum 39.2 mm. QPE 24 h (downloaded, else skipped): −3 count, maximum 385.5 mm. FFG: −999 count, encoded `lo1`, maximum 90 %, percent round-trip to 0.1 %. Codec edges and block maximum. |
| `mrms/MrmsTakeEveryTest` | 10-minute buckets aligned to :00/:10; the cursor's bucket stays closed; a missing file moves the pick within its bucket; `takeEvery` 1 keeps everything; the same listing gives the same picks; 30-minute buckets. Parameter defaults, `checkParams` and clamping. Product units, styles, gaps, ladders, zooms and decoder requirements. |
| `mrms/MrmsAdapterWireMockTest` (updated) | New: QPE 1 h at `takeEvery` 5 commits 04:20 and 04:30, never downloads 04:22, and moves the cursor past 04:38; rotation is committed on the 0.01° grid with `reducedFrom`. The descriptor lists seven products; disabled products make no request. |
| `nws/NwsGridpointsParserTest` | Real `/gridpoints/VEF/55,177`: 72 steps from 20:00 Z, interval spreading (PT2H), km/h → m/s, direction, PoP, sky cover; accumulation split (0.508 mm/6 h → 0.08, 1.016 mm/6 h → 0.17); summaries (PoP 60, QPF 1.4 mm, 6.7/2.2 °C, gust 19.5 m/s); hazard `FZ.W`; interval parsing; unit conversions and refusals; malformed bodies; the 10-hazard cap. `/points` parsing. |
| `nws/NwsGridpointsAdapterWireMockTest` | Valid descriptor. First run: `/points` then the gridpoint, record payload without geometry, 72-step series, Accept and User-Agent headers. Resolution cached; not due → no request; `If-Modified-Since` → 304 keeps the record. 500 three times → failures counted, degraded after the third, series kept; recovery clears it. Moved place re-resolves without validators. Gridpoint 404 drops the resolution. Outside coverage (404 from `/points`) warns and is not retried hourly. Deselected and deleted places tombstoned, series removed; `allPlaces=false` needs `nws`. `maxPlaces` warning and the 4-per-run cap. Helpers: coordinates, URL allow-list, slots, next due. |
| `nws/NwsForecastIngestIT` | Real fetch path: one version, `observed_at` = `updateTime`, payload, retention 7 d resolved; the cache row (72 items) served as provider `nws` and hidden when `nws-gridpoints` is excluded; 304 keeps one version; place delete clears the cache row at once and tombstones the record on the next run. |
| `spc/SpcStormReportsParserTest` | Three sections (types, magnitudes, units, titles, geometry, the pinned externalId `75f97d5856d0`); `UNK` omitted; convective-day times (1215 → same date, 0353/0115 → next date); quoted and unquoted commas, the office, the 500-character cap, CSV splitting; stable and distinct ids; the real files (1 report today; 15 tornado and 43 wind yesterday); header-only and malformed input. |
| `spc/SpcStormReportsAdapterWireMockTest` | Valid descriptor (30 d, 2/min, 400/day, embed fields, fieldSpecs). ETag conditional GET: 304 on today 10 minutes later, yesterday hourly with its ETag. Rollover at 12 UTC: the same report from both files keeps one id. `convectiveDate` and `fileDate`. Header-only today plus a failing yesterday degrades; both failing throws. A truncated file forgets its validators. |
| `spc/StormReportsIngestIT` | Real fetch path: 59 versions, report time as `observed_at`/`event_time`, title, point geometry, retention 30 d resolved, 304 → NOT_MODIFIED; the same reports under new validators add no versions and no tombstones. |
| `wx/PlaceWeatherSecondWaveTest` | FakeDerived: the precip parts (5 mm/h, 12, 46, 105); a stale FFG part makes precip stale with its value and `asOf`, while QPE 24 h at 140 min is still ok; a disabled input leaves; a failing source ranks unavailable over stale; `forecast.nws` unavailable, ok, stale after 6 h (values and `asOf` kept), and unavailable when disabled; restore splits the parts; `wx-areas` fields, gaps and rounding; the generator's inputs. |
| `wx/PlaceWeatherSecondWaveIT` | Real runtime: frames of the rate, QPE 1 h, QPE 24 h and FFG fill precip; the flash-flood recipe fires once; an `nws-gridpoints` fetch fills `forecast.nws` (PoP 60, QPF 1.3, gust 19.5); 7 h later `forecast.nws.status=stale` with its values and `asOf` 19:46:56, precip stale with values, and no new alert. |
| Updated | `StyleRegistryTest` (twelve styles), `FamilyDefaultsTest` (the two adapters in `weather`; `storm-report` 30 d and `nws-forecast` 7 d), `WeatherIngestSupport` (backoff list). |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 71 warnings (unchanged).

`python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 829 requests, covers all operations, no diff.

No new dependencies.

## Notes for the next prompt

1. **054 and later weather work:**
   - `PlacePoints.summaries()` gives adapters the places (id, name, point, products, version);
   - `ForecastSeriesSink` / `ForecastSeriesCache` store any per-place series under a provider name;
   - `ForecastSeriesService` serves `nws` for a place only.
2. **055 (`get_map_image`):** the four new products render through the existing paths. Rotation's `noEcho`
   (0) is transparent in `rotation@1`.
3. **057 checkpoint:**
   - run `./gradlew downloadTestFixtures` first, or `MrmsSecondWaveDecodeTest.qpe24h` skips;
   - the new ITs share `WeatherIngestSupport` (`NwsForecastIngestIT`, `StormReportsIngestIT`) and
     `PlaceWeatherSupport` (`PlaceWeatherSecondWaveIT`, date 2026-10-12T21:00Z, inside the NWS fixture's
     valid window), so there are no new contexts;
   - the WireMock tests use the default `@IntegrationTest` context;
   - `MrmsSecondWaveDecodeTest.rotation` decodes 98 M floats (392 MB), so check the test JVM heap.
4. **089:** `spc-storm-reports` is done; remove it from 089's list.
5. **143:**
   - measure the MRMS second-wave download (expected about 0.46 GB/day at the defaults; up to about
     5 MB/hour for QPE 24 h on wet days);
   - measure the rotation decode peak (the 392 MB float array) under the small profile;
   - measure `nws-gridpoints` request counts with 50 places (about 1,200 gridpoint requests a day).
6. **144 (Flyway):**
   - new table `forecast_series_cache` (primary key `place_id, provider`; `series jsonb`);
   - new sources `nws-gridpoints` and `spc-storm-reports` get `source_settings` rows;
   - new `mrms` params `rotationTakeEvery`, `qpe1hTakeEvery`, `qpe24hTakeEvery`, `ffgTakeEvery`;
   - new adapter-state keys: `point:`, `next:`, `failures:`, `grid:` (nws-gridpoints) and `yesterdayAt`
     (spc-storm-reports).
7. **Owner decisions pending:** Issues 1–4 above, plus those carried from 012–052.
