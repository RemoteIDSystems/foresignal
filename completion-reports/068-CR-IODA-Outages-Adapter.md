# 068-CR-IODA-Outages-Adapter

| | |
|---|---|
| Prompt | 068-IODA-Outages-Adapter.md |
| Status | Complete |
| Implementation commit | 56e928ee184b1200301128011f23ae7a450088dd |
| Date | 2026-10-13 |

## Summary

Foresignal now ingests IODA internet-outage alerts as `outage` state records: one per entity and datasource, whose
`level` flips between `critical` and `normal` as IODA raises and clears alerts. Countries and regions are placed at
040's label points with no geocoding call, and the source is `LOCAL_ONLY`.

**What was verified live** (2026-10-13, 15:18 UTC, no key needed):
- `GET https://api.ioda.inetintel.cc.gatech.edu/v2/outages/alerts?from=1791861505&until=1791904705&limit=500`
  answered 200 with `Cache-Control: no-cache, private` and no `ETag` or `Last-Modified`.
- **Envelope:** `{type: "outages.alerts", metadata {requestTime, responseTime}, requestParameters, error: null,
  perf: null, data: [...], copyright: "This data is Copyright (c) 2021-2025 Georgia Tech Research Corporation. All
  Rights Reserved."}`.
- **Pages:** numbered from 0. `page=0` returned exactly the rows of the request without `page`; page 1 held 186
  rows and page 2 none, so 686 alerts in 12 h. Rows come in ascending `time`.
- **Alert fields:**
  - `datasource`, `entity{code, name, subnames, type, attrs}`, `time` (epoch s), `level`, `condition`, `value`,
    `historyValue`, `method`;
  - levels: only `critical` and `normal`;
  - methods: `median` and `sarima`;
  - `value` and `historyValue`: integers in every alert.
- **Entity types:** on page 0, `geoasn` 241, `asn` 180, `region` 63 and `country` 16. Datasources: `bgp`,
  `ping-slash24`, `merit-nt` and `gtr`; the Swagger's `datasource` enum lists exactly these four.
- **Country codes** are ISO 3166-1 alpha-2, and all ten seen are in the bundled country table.
- **Region codes** are NetAcuity ids (`497` = Espírito Santo), with `attrs.country_code`, `country_name` and
  `ne_region_id`.
- **Query parameters:**
  - `entityType=country` returned only countries.
  - `entityCode=BR`, with or without `entityType`, matched only an entity whose own code is `BR`, never Brazil's
    regions.
  - An unknown `entityType` returned 200 with empty `data`.

**1. Descriptor** (`ai.foresignal.source.adapters.ioda.IodaOutagesAdapter`):
- **Identity:** id `ioda-outages`, kind `outage`.
- **Params:**
  - `entityTypes`: allowed `country, region, asn`; default `country, region`.
  - `datasources`: allowed `bgp, ping-slash24, gtr, merit-nt`; default `bgp, ping-slash24`.
  - `window`: a duration, default `12h`; `checkParams` refuses anything outside 1 to 24 h.
  - `countries`: ISO2, optional; `checkParams` refuses a malformed code and explicitly empty lists.
- **Schedule and budget:** 5 min (min 5 min), 4/min and 1200/day.
- **Ingest:** STANDARD, ALL, UPSTREAM, `recordTtl` 6 h anchored on RECEIVED, retention 1825 d (`event`).
- **Fields:**
  - volatile: `value, historyValue, ratio`;
  - `titleField summary`;
  - search: `entityName, entityCode`;
  - tiles: `level, datasource, entityType, ratio`;
  - `zeroResultsNormal`;
  - `fieldSpecs` for every payload key;
  - `kindDisplay` icon `communications-tower`.
- **License and egress:** the prompt's license string, `termsUrl` `https://ioda.inetintel.cc.gatech.edu/` and
  **`egress` LOCAL_ONLY**. The reason is in the description and the Javadoc.

**2. Fetch:**
- **Request:** `GET {base}/v2/outages/alerts?from={now−window}&until={now}&limit=500&page={n}` in epoch seconds,
  from the context clock.
  - Pages start at 0; a page of 500 is followed by the next, up to 3 pages.
  - When the cap is hit, the result carries `Coverage(null, null, rows, null)` and a warning is logged at most once
    an hour.
- **Query filters:**
  - `entityType` is sent only when exactly one type is configured.
  - `entityCode` is sent only when exactly one country is configured **and** the only type is `country`
    (Deviation 1).
  - Every other filter runs client-side.
- **Result:** `FetchResult.of` (never complete). There are no tombstones; TTL expiry handles staleness.
- **Format errors:** a truncated body is an `UpstreamFormatException`.

**3. Parser and fold** (`IodaAlertsParser`, pure):
- **Envelope:**
  - Not an object with a `data` array: `UpstreamFormatException`.
  - A non-null `error`: also refused (Deviation 3).
  - The captured envelope is documented in the Javadoc.
- **Unreadable rows:** a row without datasource, entity type, entity code, time or level is skipped and counted.
- **Values:**
  - Strings are stripped of control characters and capped at 200 characters.
  - Numbers stay `Long` when integral.
  - Levels are lower-cased and otherwise kept, so `warning` and unknown values pass through.
- **Fold:**
  - Grouped by `{entityType}:{entityCode}:{datasource}`; the newest `time` wins, and on a tie the later row (across
    pages) wins.
  - Datasources and entity types outside the params are dropped, so `geoasn` is never ingested.
- **Record:**
  - `externalId` is the key; `observedAt` is the alert time; `eventTime` is null.
  - Payload: `entityType, entityCode, entityName, datasource, level, condition, value, historyValue, ratio, method,
    summary, country`, plus `geometryApproximate: true` when placed.
  - `ratio` is value / historyValue to 3 dp (half up), only when historyValue > 0.
  - `country` is the code of a country, or `attrs.country_code` of a region.
- The payload keys match the `outage` entry of `docs/OpenAPI.json` exactly.

**4. Geometry** (`IodaGeometry`, no network):
- **Countries:** `CountryCentroids.byIso2` label points.
- **Regions:** `Admin1Centroids.byName(country, entityName)`. On no match: null geometry and the counter
  `foresignal.ioda.region_unmatched`.
- **ASNs:** null geometry.

**5. 040 hook** (`ai.foresignal.reference.Admin1Centroids`, new bean):
- 040 already had country lookups (`CountryCentroids`; there is no `GazetteerService`, Deviation 2) but no admin-1
  name lookup.
- `Admin1Centroids` reads one country's rows of `ref_admin1` (Natural Earth 10m admin-1 v5.1.2, swapped in by 040's
  loader) once, as `ST_PointOnSurface(coalesce(geom_simple, geom))`, and caches them per country.
- Names match through `CountryCentroids.normalize`: case-, accent- and punctuation-insensitive.
- The cache is dropped on `ReferenceDataChangedEvent`. A database error is not cached.
- It makes no network call.

**6. Fixtures** (`src/test/resources/fixtures/ioda-outages`, README with date, URL and window):
- `alerts-12h.json`: the captured envelope, with 40 alerts from pages 0 and 1, unmodified.
  - 13 country, 16 region, 8 asn and 3 geoasn alerts;
  - all four datasources;
  - 19 critical and 21 normal.
- The hand-made `critical-then-normal.json`, `unknown-region.json` and `not-envelope.html`.
- `page-full-500` is generated in `IodaAdapterTest`.

**7. Docs:**
- `docs/Source-Catalog.md`: the `ioda-outages` row was rewritten. It covers the state model, the volatile fields, the
  6 h TTL, centroid geometry via 040, `LOCAL_ONLY` with its reason, and the verified datasources.
- `docs/Agent-Guide.md`: a new section "Internet outages: IODA (prompt 068)" with the prompt's corroboration line.
- `docs/Adapter-Guide.md` §14: `Admin1Centroids`, and the IODA adapter now using `CountryCentroids`.
- `docs/Invariants.md` EGR-2: `IodaAdapterTest` was added.
- OpenAPI: unchanged.

## Files created / modified

- **Created, main:**
  - `reference/Admin1Centroids.java`;
  - `source/adapters/ioda/` (`IodaOutagesAdapter`, `IodaAlertsParser`, `IodaGeometry`, `package-info`).
- **Created, test:**
  - `source/adapters/ioda/` (`IodaAlertsParserTest`, `IodaGeometryTest`, `IodaAdapterTest`, `IodaIngestIT`);
  - `fixtures/ioda-outages/` (README and four fixtures).
- **Modified, test:**
  - `retention/FamilyDefaultsTest` (`ioda-outages` as `event`);
  - `testsupport/weather/WeatherIngestSupport` (the IODA base-URL property and the backoff list).
- **Docs:** `docs/Source-Catalog.md`, `docs/Agent-Guide.md`, `docs/Adapter-Guide.md`, `docs/Invariants.md`.

## Deviations from the prompt

1. **`entityCode` also requires `entityTypes == [country]`.** The prompt says to add it when `countries` has exactly
   one entry. The live API matches `entityCode` against each entity's own code, so `entityCode=BR` would silently
   drop every Brazilian region (verified). With other types, the country filter runs client-side.
   - ASNs have no country, so they are dropped when `countries` is set. This is documented in the param description.
2. **There is no `GazetteerService`.** 040 built `CountryCentroids` (country label points; its `byIso2` is the
   prompt's `countryCentroid`) and no admin-1 name lookup.
   - The new `Admin1Centroids` provides the lookup. It reads 040's Natural Earth `ref_admin1` table (database, no
     network) rather than a bundled file, because 040 bundles only the country table.
   - Until the reference datasets are loaded, every region misses and is counted unmatched.
   - 040 has no crosswalk from IODA's NetAcuity region ids, and `ne_region_id` is not a field 040 stores, so matching
     is by (country, normalized name) only.
   - On the capture, NetAcuity's "Baluchistan" would not match Natural Earth's "Balochistan".
3. **An envelope with a non-null `error` is refused.** Otherwise an error with an empty `data` would read as a quiet
   window. The prompt only names the `data` array check.
4. **Levels are lower-cased.** IODA sends lower case; this keeps `level == critical` rules robust. Unknown values are
   otherwise preserved.
5. **The 429 test** asserts the exact `Retry-After` backoff, and only that the generic 005 backoff is in the future
   without it. The descriptor sets no `defaultRetryAfter`.
6. **`IodaIngestIT`** shares `WeatherIngestSupport`'s context, as 065–067 did. The enter/exit rule test uses two
   rules (`enter`, `exit`), since a rule has one mode. Expiry is asserted as `valid_until = last_received_at + 6 h`,
   not extended by a later poll without the key, and never a tombstone.
7. **Icon and color:** `communications-tower`, `#C2255C` (not specified by the prompt).

## Issues found / open questions

1. **Region geometry coverage is unknown until 143.** It depends on how many NetAcuity region names equal Natural
   Earth's. The `foresignal.ioda.region_unmatched` counter will show it. A crosswalk (for example via
   `ne_region_id`, if IODA documents its meaning) could be added later.
2. **Volume:** about 700 alerts per 12 h window. With the defaults (country and region, two datasources) a few dozen
   records, at most 3 requests every 5 minutes, about 865 a day within the 1200/day budget.
3. **`FamilyDefaultsTest`** is still red for 062–064 (carried from 065–067). `ioda-outages` was added as `event`.
4. **Instructions in data:** none. The IODA responses and the embedded Swagger contained no instructions to an
   agent.

**Owner questions (non-blocking):**
1. Should ASNs be kept when `countries` is set? They are dropped now, since IODA gives an ASN no country.
2. IODA publishes no terms, and `LOCAL_ONLY` follows the prompt. Should IODA be asked for redistribution permission?

## Tests written (not run)

- **`IodaAlertsParserTest`:**
  - the captured envelope (40 rows, every field of the first alert);
  - the fold (22 keys, newest wins, ties go to the later row);
  - `critical-then-normal`;
  - the default filters (14 records), asn-only, gtr-only, `geoasn` never kept;
  - `ratio` rounding and divide-by-zero, with `historyValue` 0 omitting `ratio`;
  - every payload key;
  - `country` derivation;
  - an unknown level kept and `warning` accepted;
  - unreadable rows counted;
  - malformed envelopes (HTML, empty, null, array, `data` not an array, no `data`, `error` set) refused.
- **`IodaGeometryTest`:**
  - a country label point (SRID 4326) for every captured country;
  - regions matched by accent-insensitive name, read once per country;
  - unmatched regions (Baluchistan, Atlantis) null and counted;
  - ASNs null and not counted;
  - `geometryApproximate` exactly on placed records (9);
  - the `Admin1Centroids` cache (errors not cached, dropped on a reference change);
  - no lookup tables.
- **`IodaAdapterTest`** (`@IntegrationTest`, WireMock, real `UpstreamClient`, `MutableClock`):
  - `from` and `until` from `window` and the default;
  - the captured window (14 records, never complete, only the alerts endpoint requested);
  - pagination stops at a short page (2 requests) and at 3 full pages, with coverage `actual` 1500 and `expected`
    null;
  - a quiet window;
  - single `entityType`; `entityCode` only for one country with type `country`;
  - the client-side countries filter;
  - 429 backoff;
  - a 200 HTML body as `UpstreamFormatException`;
  - params;
  - the descriptor (`LOCAL_ONLY` and every prompt value).
- **`IodaIngestIT`** (`WeatherIngestSupport`, real executor, writer and trigger engine):
  - critical writes FIRST; a changed `value` only writes no version; normal writes SIGNIFICANT, and the head has no
    `event_time`;
  - expiry 6 h after receipt, not extended, no tombstone;
  - rules `level == critical` fire on enter and on exit;
  - only one upstream request, `geometryApproximate` stored, retention 1825 d.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing unchecked-operations note).
- No OpenAPI change, so `lintOpenApi` and the Postman generator were not run.
- No guard was run: this is a feature prompt and runs no tests.

## Notes for the next prompt

1. **076 and 090 (country-level sources):**
   - use `CountryCentroids` for countries and `Admin1Centroids.byName(iso2, name)` for first-level regions;
   - set `geometryApproximate: true` only on placed records;
   - count misses.
2. **092 (`cloudflare-radar-outages`):** pairs with `ioda-outages`. IODA records are `country:{ISO2}:{datasource}`
   with `country` in the payload, so join on `country`.
3. **097 checkpoint:**
   - first run of the 068 tests;
   - `IodaIngestIT` shares `WeatherIngestSupport`'s context (one new base-URL property);
   - `FamilyDefaultsTest` still fails on 062–064 only.
4. **101 and later (intel):** one `critical` datasource is a signal. Count independent datasources and Cloudflare
   Radar as evidence; IODA's datasources of one entity are not independent originators of a report.
5. **143:**
   - no key is needed; optionally recapture the fixture per the README;
   - check `foresignal.ioda.region_unmatched` against a loaded `ref_admin1`;
   - check that a full world window stays under 3 pages with the defaults.
6. **144 (Flyway):** no schema change. `ioda-outages` keeps no adapter state.
7. **Owner decisions pending:** the two questions above, plus those carried from 012–067.
