# 070-CR-Transit-GTFS-RT

| | |
|---|---|
| Prompt | 070-Transit-GTFS-RT.md |
| Status | Complete |
| Implementation commit | 3805046c93e24a4b15ba84b62cd40c832d8d1d46 |
| Date | 2026-10-13 |

## Summary

The `transit-*` families are in: GTFS-RT VehiclePositions as TRACK `transit-vehicle` records and ServiceAlerts as
`transit-alert` records. Both come from a static, code-reviewed feed list, plus a Mobility Database resolver that
enables more keyless feeds by id from a vendored CC0 snapshot. No transit URL ever comes from an API caller.

**1. Dependency** (`gradle/libs.versions.toml`, `build.gradle.kts`):
- **Bindings:** `org.mobilitydata:gtfs-realtime-bindings:0.2.0`. Verified on Maven Central: the latest release,
  2026-07-29, Apache-2.0, depends on `protobuf-java` 4.34.0.
- **Every new proto field is there:** its proto already has `communication_period`, `impact_period`, `cause_detail`,
  `effect_detail`, `image` and `SPECIAL_EVENT`, so no unknown-field reading was needed.
- **protobuf-java is pinned to 4.36.1, not 4.34.0.** `./gradlew dependencyInsight` showed two things:
  - the Spring Boot 4.1.1 BOM already forced 4.35.1, which is below NetCDF-Java `cdm-core`'s declared 4.36.1;
  - pinning 4.34.0 would have downgraded it further.
- **How the pin works:** 4.36.1 satisfies both, because protobuf accepts a newer runtime within major 4. The BOM
  property is overridden (`extra["protobuf-java.version"]`).
- The old `com.google.transit` artifact is not used.

**2. Parsing** (`GtfsRt`):
- `FeedMessage.parseFrom` runs on a body capped at 16 MB; a truncated body is refused.
- An HTML or JSON 200 is detected before parsing: the first byte that is not a space, tab, CR or LF is `<`, `{` or
  `[`. These cannot start a FeedMessage.
- A parse failure, a missing header, or more than 50,000 entities is an `UpstreamFormatException`, and the
  `getConditional` validators are forgotten.
- Strings over 256 characters drop the record (GEV's bound).

**3. The reviewed list** (`TransitFeeds`, `TransitFeed`, `TransitModes`, `TimeBase`):
- **Ported from GEV `TRANSIT_FEED_REGISTRY`:** the seven feeds of the prompt's table, including the alerts URLs, the
  licenses, the attributions and the route-mode rules (MBTA, Entur, HSL, Metro Transit).
- **Headers:** `TransitFeeds.problems` validates the list at startup. The only header a feed may add is
  `ET-Client-Name`. Entur's value is `foresignal-{installId}`, resolved at fetch time from `InstallIdService`.
- **Time base per feed,** measured on full feeds captured 2026-10-13 (entities carrying a vehicle timestamp):

  | Feed | Timestamped | Time base |
  |---|---|---|
  | MBTA | 555/555 | VEHICLE |
  | CapMetro | 384/384 | VEHICLE |
  | HSL | 788/788 | VEHICLE |
  | OVapi | 3836/3836 | VEHICLE |
  | Entur | 8667/8667 | VEHICLE |
  | TransLink | 7/7 | VEHICLE |
  | Metro Transit | 423/754 (56%) | HEADER |

**4. `transit-{id}`** (`TransitVehicleAdapter`, `GtfsRtVehicleParser`, `TransitDescriptors.vehicles`):
- **Schedule:** POLLING, 30 s (minimum 15 s), 4/min and 3000/day. A conditional GET with the feed's headers; a 304
  is `unchanged`.
- **Policy:** TRACK, `ttlAnchor` OBSERVED, TTL 5 min, SAMPLED `{15s, 240s, 150 m, thresholds, edgeMin 8s}`.
  - significant `routeId, tripId, directionId, label, mode`;
  - motion `{speedMs, MPS, bearing}`, threshold `bearing` 30° (angular).
- **Time:** VEHICLE drops an untimed vehicle and counts it (`foresignal.transit.untimed{source}`); HEADER uses
  `header.timestamp`.
- **Records:**
  - `externalId` is the vehicle id, else the entity id; a duplicate vehicle keeps its newest report;
  - 0/0, out-of-range and missing positions are skipped;
  - coordinates are kept to 6 decimals, bearing folded into 0–360 (1 decimal), speed to 2 decimals;
  - the payload is exactly the prompt's 14 keys, with enums by proto name and `agency` = the feed name.
    `licensePlate` is never read;
  - `quality.positionSource` REPORTED.
- **Display:** title `label`, tiles `routeId, mode, bearing, occupancyStatus`; kind display `bus`, rotated by
  `bearing`.
- **Never tombstoned:** a vehicle that stops reporting expires with the TTL.

**5. `transit-alerts-{id}`** (`TransitAlertsAdapter`, `GtfsRtAlertParser`, `TransitDescriptors.alerts`):
- **Schedule and policy:** 60 s (minimum 30 s), 2/min and 1500/day; STANDARD, ALL, no TTL, `zeroResultsNormal`,
  retention 1825 d (`event`).
- **Time and validity:**
  - `observedAt` = the header timestamp;
  - `validFrom` = `eventTime` = the earliest start of `impact_period`, else of `active_period`. It is null when a
    period has no start;
  - `validUntil` = the latest end, only when every period has one.
- **Payload:**
  - texts chosen by the `language` parameter: the exact tag, then the primary subtag, then the first translation.
    Each is capped at 4 KB, cut on a code-point boundary. The parameter is validated as a BCP 47 tag;
  - enums by name;
  - periods as ISO `{start, end}` pairs, at most 20. `impactPeriods` falls back to `active_period`;
  - `informed`, at most 50.
- **Other descriptor fields:** no geometry; `embedFields` `headerText, descriptionText`.
- **Absence:**
  - **FULL_DATASET:** `LiveIdSet` scope `alerts` (`live:alerts`) tombstones the ids missing from a successful,
    parsed response;
  - **`is_deleted`:** tombstoned on any feed;
  - **DIFFERENTIAL:** tombstones only on `is_deleted`;
  - **304 or failure:** tombstones nothing.
- Tombstones carry the header time.

**6. Mobility Database resolver** (`MobilityCatalog`, `TransitFamily`, `scripts/update-mobility-catalog.py`):
- **The snapshot:** `src/main/resources/transit/mobility-gtfs-rt.csv` holds 1350 rows (228 KB), generated from
  `feeds_v2.csv` downloaded 2026-10-13.
  - Filter: `gtfs_rt`, `active`, `authentication_type 0`, an https `direct_download`.
  - Columns: the prompt's seven.
  - The README beside it notes the date and the CC0 metadata license.
  - The deprecated `sources.csv` is not used.
- **`MobilityCatalog`:** `search(country, query, entityType)` and `get(id)`. Entity types are split on `|`, because
  rows like `tu|vp` and `sa|tu|vp` exist.
- **`foresignal.transit.catalog-feeds`** (`FORESIGNAL_TRANSIT_CATALOG_FEEDS`, default empty):
  - a `vp` row registers `transit-mdb-{id}`, an `sa` row `transit-alerts-mdb-{id}`; a combined row registers both;
  - only the vendored URL is used; time base HEADER, mode `unknown`;
  - the license is `urls.license`, else "see provider terms";
  - each URL is checked against `OutboundUrlPolicy` `PUBLIC_ONLY` at startup;
  - a refusal, an id missing from the snapshot, and a trip-updates-only row or one with no entity type are each
    skipped and logged;
  - a restart is required (documented).

**7. API:** `GET /v1/transit/catalog` (`TransitCatalogController`):
- admin (an AccessPolicy rule), read-only, no POST;
- `country`, `q`, `entityType`, `cursor` (opaque, by id; a bad one is `400 INVALID_CURSOR`), `limit` 1–500;
- each row reports `enabled`, `sourceId` and `sourceIds`;
- status `implemented`, removed from `pending-operations.txt`; Postman regenerated.

**8. SPI:** `SourceHttp.getConditional(uri, stateKey, headers)`:
- **The gap:** Entur needs its header on a conditional GET, and the SPI had no such overload.
- **The overload:** the default handles only the no-header case; `BoundSourceHttp` implements it. A caller's
  `User-Agent`, `If-None-Match` and `If-Modified-Since` are ignored.

**9. Fixtures:** under `src/test/resources/fixtures/transit/`:
- **Captures:** MBTA, Entur (with `ET-Client-Name`) and HSL VehiclePositions and Alerts, taken 2026-10-13 and
  trimmed to 30 entities with the test utility `GtfsRtFixtures trim`. Each has a README (URL, request and response
  headers) and a text dump.
- **Hand-made:** `untimed-vehicle.pb`, `full-dataset-alert-removed.pb`, `alert-legacy-active-period.pb`,
  `alert-impact-period.pb`, `differential-alert-deleted.pb` and `html-200.html`.
- **Test catalog:** `src/test/resources/transit/test-catalog.csv`.

**10. Docs:**
- **`docs/OpenAPI.json`:** operation status, description, `TransitCatalogEntry` amendments, the kind notes of
  `transit-vehicle` and `transit-alert`, and a changelog entry.
- **`docs/Source-Catalog.md`:** the `transit-{id}` row replaced, two new rows, and the verified endpoint table.
- **`docs/Operator-Guide.md`:** "Transit" and "Adding a transit feed from the Mobility Database".
- **`THIRD_PARTY_NOTICES.md`:** five GEV rows, gtfs-realtime-bindings, protobuf-java and the MobilityData snapshot.
- **`application.yml`:** the property.

## Files created / modified

- **Created, main:**
  - `source/adapters/transit/`: `package-info`, `TimeBase`, `TransitModes`, `TransitFeed`, `TransitFeeds`,
    `GtfsRt`, `GtfsRtVehicleParser`, `GtfsRtAlertParser`, `TransitDescriptors`, `TransitVehicleAdapter`,
    `TransitAlertsAdapter`, `MobilityCatalog`, `TransitFamily`;
  - `api/TransitCatalogController`, `api/dto/TransitCatalogEntryDto`, `api/dto/TransitCatalogPage`;
  - `resources/transit/mobility-gtfs-rt.csv` and `README.md`;
  - `scripts/update-mobility-catalog.py`.
- **Modified, main:**
  - `source/spi/SourceHttp`, `collect/BoundSourceHttp` (the headers overload);
  - `security/AccessPolicy`;
  - `ingest/TitleResolver` (`routeId` fallback);
  - `application.yml`, `gradle/libs.versions.toml`, `build.gradle.kts`.
- **Created, test:**
  - `source/adapters/transit/`: `GtfsRtVehicleParserTest`, `GtfsRtAlertParserTest`, `TransitFamilyTest`,
    `TransitAdapterTest`, `MobilityCatalogTest`, `TransitIngestIT`;
  - `api/TransitCatalogControllerIT`;
  - `testsupport/transit/GtfsRtFixtures`;
  - `fixtures/transit/` (4 READMEs plus the root one, 6 captures, 6 hand-made, 11 dumps);
  - `resources/transit/test-catalog.csv`.
- **Modified, test:**
  - `testsupport/weather/WeatherIngestSupport` (three properties and the transit sources);
  - `retention/FamilyDefaultsTest` (the two adapters);
  - `mcp/McpOutputShaperTest` (the fallback list);
  - `openapi/pending-operations.txt`.
- **Docs:** `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `docs/Source-Catalog.md`,
  `docs/Operator-Guide.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **protobuf-java 4.36.1 instead of 4.34.0** (see Summary 1). Pinning the bindings' own version would have
   downgraded below both the Boot BOM (4.35.1) and NetCDF-Java `cdm-core` (4.36.1).
2. **The keyframe constraint is not enforced against an operator's interval.** The prompt says the validator rejects
   intervals above 30 s unless the keyframe is shortened. In the code, `IngestPolicyValidator` checks
   `keyframe ≤ ttl − interval − 30 s` only against the descriptor's default interval (`effectivePollInterval`). A
   settings update that only raises the interval is not checked. The constraint is stated in the descriptor
   Javadoc, the description, the Source Catalog and the Operator Guide, and `TransitFamilyTest` shows it with a 45 s
   descriptor. Issue 1.
3. **Spec amendments to `TransitCatalogEntry`** (changelog entry added):
   - `entityType` was a single-value enum, but real rows are `tu|vp` or `sa|tu|vp`, and 46 snapshot rows declare none. It is now
     a pattern over `|`-joined types, possibly empty;
   - `sourceIds` (array) is added, because a combined row registers two sources. `sourceId` stays, as the first one.
4. **Catalog source ids** are literal `transit-mdb-{id}`, so MDB id `mdb-1602` becomes `transit-mdb-mdb-1602`.
   Ids are lower-cased with other characters turned into hyphens, because 66 snapshot rows have upper case or `_`. One
   that would exceed 64 characters is shortened with an 8-hex SHA-256 suffix (Japanese `odpt-…` ids reach 58
   characters).
5. **An unresolvable catalog host passes the startup policy check** (`OutboundUrlPolicy` `Denied.unresolved`, the
   save-time rule), so a DNS outage at boot does not drop a feed. A private, reserved or metadata answer is still
   refused.
6. **Titles:** the descriptor has one `titleField`, so "label, fallback routeId" is done by appending `routeId` to
   `TitleResolver.FALLBACK_FIELDS`. That shared list also feeds `VersionSql`. `McpOutputShaperTest` still asserted
   the pre-062 four-entry list and was updated to the full list.
7. **Text dumps instead of `protoc --decode_raw`:** `protoc` is not installed, and installing it system-wide is not
   allowed. The dumps are protobuf text format from the bindings, with field names rather than numbers.
   `GtfsRtFixtures trim` was run as a program (`java -cp`, not a test) to trim the captures and write the
   hand-made fixtures.
8. **Every captured alert feed uses only `active_period`** (MBTA 131, Entur 676, HSL 34, Metro Transit 96, OVapi 834,
   TransLink 62). The `impact_period` path is covered by the hand-made fixture only.
9. **`headerText` is capped at 4 KB like `descriptionText`;** the prompt names the cap after `descriptionText` only.
   `causeDetail` and `effectDetail` get the same cap.
10. **Catalog feeds are egress OPEN and send no headers.** An Entur sub-feed listed in the catalog (for example
    `mdb-1797` with `?datasource=GOA`) may therefore be refused by Entur. The Operator Guide says to use
    `entur-norway`.
11. **Alerts retention** is the `event` class, 1825 d (not stated in the prompt; FamilyDefaultsTest classifies it).
12. **Display choices** (not specified): vehicles use icon `bus #2B8A3E` with `rotateField bearing`; alerts use
    `caution #E8590C`; alert tiles are `effect, cause, severityLevel`; vehicles set `zeroResultsNormal` (small
    agencies run no vehicles at night).

## Issues found / open questions

1. **Interval and keyframe:** `SourceService` does not re-check the keyframe constraint when an operator changes
   only the interval (Deviation 2). Invariant ING-8 can be broken for any TRACK source this way, not only transit.
   The fix belongs to `IngestPolicyValidator` and `SourceService.applyIngestPolicy` and touches every source, so it
   was left for 097 or the owner.
2. **CapMetro redirects.** `data.texas.gov/download/…` answers 302 to a per-file URL. `UpstreamClient` follows it
   (`followRedirects` is true by default), and conditional validators apply to the final response.
3. **TransLink returned 7 vehicles at capture time** (night in Brisbane). Its VEHICLE time base rests on a small
   sample; recapture in daytime at 143.
4. **`NhcAdapterTest` (067) reads validators from its `MapState`.** The real client stores them in the persistent
   adapter state, so that assertion probably fails at 097. `TransitAdapterTest` uses
   `UpstreamClient.stateStore(id)` as the context state instead. Not changed here.
5. **Instructions in data:** none. The GEV files, the GTFS-RT proto, `feeds_v2.csv` and the captured feeds were read
   or scanned; nothing addressed an agent.

**Owner questions (non-blocking):**
1. Should the keyframe constraint be enforced against the operator's interval (Issue 1)? That would be a shared change
   to `IngestPolicyValidator`.
2. Should catalog feeds whose `urls.license` is empty start as `LOCAL_ONLY` instead of `OPEN` (Deviation 10)? The
   per-source license override arrives in 109.
3. Is the literal `transit-mdb-mdb-1602` id acceptable, or should the `mdb-` prefix of MDB ids be dropped
   (`transit-mdb-1602`)? It cannot change after 144 without a migration.

## Tests written (not run)

- **`GtfsRtVehicleParserTest`:**
  - the MBTA, Entur and HSL captures (times, coordinates, payload, modes, speed and bearing);
  - the VEHICLE and HEADER time bases, an untimed vehicle and a header without a time;
  - the externalId fallback; 0/0, out-of-range, missing-position and deleted entities;
  - an over-long string; the newest duplicate kept;
  - the route-mode rules of every reviewed operator; bearing and speed rounding;
  - `SnapshotContract.assertStableStaticHash` across a stop change.
- **`GtfsRtAlertParserTest`:**
  - impact periods; an open-ended period with no `validUntil`; legacy `active_period`;
  - the translation choice (exact tag, then subtag, then first);
  - the caps (4 KB on a code point, 50 informed, 20 periods);
  - `is_deleted` and DIFFERENTIAL; no periods;
  - the MBTA and HSL captures.
- **`TransitFamilyTest`:**
  - 13 sources, alerts only where an alerts URL exists;
  - list validation; the Entur header carries the install id;
  - every descriptor valid (`DescriptorValidator`); the vehicle and alert descriptors field by field;
  - the keyframe constraint at 30 s and 45 s;
  - URLs only from the list; the test rebasing; the `language` parameter check.
- **`TransitAdapterTest`** (`@IntegrationTest`, WireMock, real `UpstreamClient`):
  - the conditional GET and a 304;
  - protobuf garbage (validators forgotten); an HTML 200; JSON detection;
  - a FULL_DATASET removal tombstoned; no tombstone after a 304 or a failure; DIFFERENTIAL `is_deleted`;
  - the Entur header and the User-Agent; no header on MBTA;
  - the untimed counter; the `language` parameter.
- **`MobilityCatalogTest`:**
  - the vendored snapshot loads (over 1000 rows, all https, `mdb-1602` is MBTA); row filtering;
  - the search filters; a missing snapshot fails startup;
  - `catalog-feeds` registers only vendored URLs (unknown, trip-update and no-type rows skipped, valid
    descriptors, the license fallback);
  - a URL failing the policy (a private answer or a private literal) is skipped, and an unresolvable host is
    accepted;
  - slugs.
- **`TransitCatalogControllerIT`** (`WeatherIngestSupport` context):
  - admin only; the filters; the enabled flag and `sourceIds`;
  - `mdb-9005` is refused by the URL policy;
  - pagination; the 400s; no POST.
- **`TransitIngestIT`** (`WeatherIngestSupport`, real fetch path and writer):
  - a 200 m move after 20 s is a SAMPLE; a trip change is SIGNIFICANT;
  - an alert is FIRST with `valid_from` stored, then tombstoned when it leaves the full feed;
  - the registry contents.
- **Updated:** `FamilyDefaultsTest`, `McpOutputShaperTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing unchecked-operations note).
- `./gradlew lintOpenApi`: valid; 71 warnings, the same count as before.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests across 80 tag folders, covers all ops.
- No tests and no guards were run: this is a feature prompt.

## Notes for the next prompt

1. **071 (GBFS) and later families:**
   - use `TransitFamily` as the template: a static list validated at startup, a test base-URL property that
     rebases every URL, and catalog-style additions only from a vendored resource plus a bootstrap property;
   - `SourceHttp.getConditional(uri, stateKey, headers)` now exists for upstreams that need an identifying header
     on conditional requests.
2. **097 checkpoint:**
   - first run of every 070 test;
   - `TransitIngestIT` and `TransitCatalogControllerIT` share `WeatherIngestSupport`'s context. Three new
     properties: the transit base URL, the test catalog resource, and `catalog-feeds=mdb-9001,mdb-9003,mdb-9005`;
     the test catalog's hosts are under `.example`, so no DNS query is reported;
   - `FamilyDefaultsTest` still fails on the unclassified 062–064 adapters only;
   - check Issue 4 (`NhcAdapterTest` validators) and Issue 1 (interval versus keyframe).
3. **101 and later (intel):** transit alerts are operator-published and untrusted text (`embedFields`). Vehicle
   positions are reported, not observed by Foresignal.
4. **143:**
   - no key is needed; recapture the fixtures per `fixtures/transit/README.md`;
   - re-measure the time bases, TransLink in daytime;
   - check whether any alert feed has moved to `impact_period`;
   - Entur's full vehicle feed is 1.7 MB every 30 s, about 4.9 GB a day per source if enabled. Its conditional GET
     never matches, because Entur sends no validators.
5. **144 (Flyway):** no schema change. Adapter state:
   - `transit-{id}` / `transit-mdb-{id}`: `etag:vehicles`, `lastmod:vehicles`;
   - `transit-alerts-*`: `etag:alerts`, `lastmod:alerts`, `live:alerts`, `gone:alerts`.
6. **Refreshing the catalog:** `python3 scripts/update-mobility-catalog.py` rewrites the snapshot and the README
   date. Review the diff, since URLs may change hosts.
7. **Owner decisions pending:** the three questions above, plus those carried from 012–069.
