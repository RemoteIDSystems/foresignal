# 074-CR-Road-Events-And-Work-Zones

| | |
|---|---|
| Prompt | 074-Road-Events-And-Work-Zones.md |
| Status | Complete |
| Implementation commit | 4eb56e85bc9784fadeb3872dd7a116340fb43595 |
| Date | 2026-10-13 |

## Summary

Road events from four open formats now land in one normalized `road-event` kind with one shared payload. That means
triggers, tiles and briefings treat a BC closure, a US work zone, a Finnish roadwork and a Dutch accident alike. 35
sources register, and every one is disabled by default.

**1. Shared model** (`ai.foresignal.source.adapters.roads`):
- **`RoadEventPayload`:** an allowlisted builder with nulls omitted.
  - **Closed vocabularies:** `category`, `severity`, `direction`, `status` and `format` refuse any other value, so a
    parser bug throws instead of inventing a category.
  - **Fields:** `subtype` (lower-kebab), `headline` (≤ 200 characters), `description` (plain text, ≤ 4 KB, cut on a
    character boundary), `roads` (≤ 10, de-duplicated), `isClosure`, `lanesAffected`, `speedLimitKph` (missing or
    ≤ 0 is omitted), `startsAt`/`endsAt` (ISO), `url` (absolute http(s) only) and `language`.
- **`RoadEvents.record`:** `kind = road-event`; `observedAt` = the format's update time; `eventTime` null;
  `validFrom` = the scheduled start; `validUntil` = the upstream end of the last interval only. An inverted range
  drops `validFrom`. A geometry over 9,500 vertices goes through `FeatureGeometry.fit` and gets
  `geometrySimplified`/`simplifyToleranceDeg`.
- **`RoadEventDescriptors`** (shared):
  - STANDARD, ALL (no keyframe), UPSTREAM, `ttlAnchor` RECEIVED, no record TTL, no significant or volatile fields;
  - title `headline`; search `headline, description, roads`; tiles `category, severity, isClosure, headline`;
    embeddings `headline, description`; interval `startsAt`/`endsAt`;
  - the `construction` icon; `zeroResultsNormal`; retention 1825 d (`event`); `fieldSpecs` for every key.
- **`ParsedEvents`:** records, ended ids and skips by reason (`missing-id`, `missing-observed-at`, `duplicate-id`,
  `bad-geometry`), plus the next page.
- **`RoadFetch`:**
  - status and truncation checks; validators are forgotten on failure;
  - ended ids are tombstoned at once;
  - `AbsenceTombstoner` runs after complete fetches only, with a per-scope option;
  - the counter `foresignal.roads.skipped{source,reason}`;
  - the test property `foresignal.test.adapters.roads.base-url`, which rebases URLs as `/{host}{path}?{query}`.

**2. `open511-drivebc`** (`Open511Server.REVIEWED`, `Open511Family`, `Open511Adapter`, `Open511Parser`):
- **Fetch:** the prompt's URL. It follows `pagination.next_url`, resolved against the page and limited to the same
  scheme, host and port, for at most 10 pages; an unfinished set is partial. 5 min (min 2), 4/min, 1,500/day,
  16 MB.
- **Mapping:** the prompt's category table; `isClosure` when a road `state` is `CLOSED`; `observedAt = updated`;
  `ARCHIVED` → an immediate tombstone; the `+…` extensions are dropped.

**3. `wzdx-*`:**
- **Snapshot:** `src/main/resources/roads/wzdx-feeds.json`, taken 2026-10-13 from 43 registry rows. It keeps 31
  feeds and lists the other 12 under `excluded` with a reason each.
- **Keys:** 8 feeds need a key (`cdot`, `iltollway`, `mtc`, `nps`, `ohio`, `ptc`, `txdot_v4_2`, `vdot`). Each
  declares a required `apiKey` credential, sent as the query parameter its registry URL showed.
- **Validation:** `WzdxRegistry` validates the snapshot at startup with the kit URL rules (no credential-looking
  parameter or token).
- **Fetch:** conditional GET; interval = the update frequency clamped to [5, 30] min; 2/min, 600/day.
- **`WzdxParser`** reads 4.0–4.2: the version comes from `feed_info.version`, else `road_event_feed_info.version`.
  3.x and CWZ get `unsupported WZDx version …`.
- **Mapping:**
  - the prompt's category table; `isClosure` = `all-lanes-closed`;
  - severity from `vehicle_impact`;
  - `completed`/`cancelled` → an immediate tombstone; `planned`/`pending` → `status = planned`.

**4. `digitraffic-road`** (`DigitrafficRoadAdapter`, `DigitrafficRoadParser`):
- **Fetch:** one `getConditional(uri, "msg:" + type, headers)` per value of `situationTypes` (string-list, default
  `TRAFFIC_ANNOUNCEMENT, ROAD_WORK`), with `Digitraffic-User: Foresignal/<version>` and gzip.
- **Absence** is tracked per type. When every type answers 304 the run is unchanged; when only some do, the result
  is partial.
- **Mapping:** the prompt's table, plus:
  - closures from feature names (Finnish, Swedish and English) or a `ROAD_CLOSED` phase restriction;
  - speed from km/h features; severity from road-work phase severity;
  - `ended`/`retracted` announcements and `earlyClosing` → an immediate tombstone;
  - `contact` is never read.
- **Schedule and license:** 5 min (min 1), 6/min, 3,000/day; CC BY 4.0.

**5. `ndw-situations`, `ndw-srti`** (`NdwFamily`, `NdwSituationsAdapter`, `Datex3SituationParser`):
- **Fetch:** conditional GET of the gzip file. The body is gunzipped by its magic bytes, with a 64 MB cap.
- **Parsing:** streaming StAX on `CapParser.secureInputFactory()`; a DOCTYPE or entity reference is refused. One
  record per situation:
  - category from the first record;
  - `isClosure` for `roadClosed`/`carriagewayClosures`;
  - geometry from all records' `posList` ("lat lon", swapped) and point coordinates;
  - lanes, speed, public comments (Dutch);
  - the interval: the earliest start, and the latest end only when every record has one.
- **Schedule and license:** 5 min (min 1), 4/min, 2,000/day, 8 MB; CC0 1.0.

**6. Docs:**
- **`docs/Source-Catalog.md`:**
  - a new section "Road events and work zones (prompt 074)" with a row per source and the shared defaults;
  - the WZDx snapshot procedure;
  - the DATEX/Digitraffic decision;
  - the roadmap line.
- **`docs/Adapter-Guide.md`:** new §23 "Normalized kinds across formats".
- **`docs/OpenAPI.json`:** the `road-event` kind note is expanded and its `payloadFields` gain the two
  simplification keys. A changelog entry is added; there is no operation change.
- **`docs/Invariants.md`:** SRC-5 adds `RoadEventAdaptersTest`.

## Files created / modified

- **Created, main** (`src/main/java/ai/foresignal/source/adapters/roads/`): `package-info`, `RoadEventPayload`,
  `RoadEvents`, `RoadEventDescriptors`, `ParsedEvents`, `RoadFetch`, `Open511Server`, `Open511Parser`,
  `Open511Adapter`, `Open511Family`, `WzdxFeed`, `WzdxRegistry`, `WzdxParser`, `WzdxAdapter`, `WzdxFamily`,
  `DigitrafficRoadParser`, `DigitrafficRoadAdapter`, `Datex3SituationParser`, `NdwSituationsAdapter`, `NdwFamily`.
- **Created, resource:** `src/main/resources/roads/wzdx-feeds.json`.
- **Created, test** (`src/test/java/ai/foresignal/source/adapters/roads/`): `RoadEventPayloadTest`,
  `Open511ParserTest`, `WzdxParserTest`, `DigitrafficRoadParserTest`, `Datex3SituationParserTest`,
  `WzdxRegistrySnapshotTest`, `RoadEventAdaptersTest`, `RoadEventsIngestIT`.
- **Created, fixtures** (each directory with a README):
  - `open511-drivebc/`: `events.json` and `edge-cases.json`;
  - `wzdx/`: WSDOT 4.2, UDOT and NDDOT 4.0, Québec 3.1, `edge-4.2.json` and `cwz-1.0.json`;
  - `digitraffic-road/`: traffic announcements, road works, weight restrictions and edge cases;
  - `ndw-situations/`: `.xml.gz`, trimmed `.xml`, `edge-cases.xml` and `xxe.xml`;
  - `ndw-srti/`: `.xml.gz` and trimmed `.xml`.
- **Modified, test:** `testsupport/weather/WeatherIngestSupport` (the roads base-URL property, plus
  `digitraffic-road` and `open511-drivebc` in the backoff list) and `retention/FamilyDefaultsTest` (the four
  adapters as `event`).
- **Docs:** `docs/Source-Catalog.md`, `docs/Adapter-Guide.md`, `docs/OpenAPI.json`, `docs/Invariants.md`. The
  Postman collection was regenerated with no change.

## Deviations from the prompt

1. **Digitraffic v2 per-type routes, not v1 `messages?situationType=`.** Digitraffic's OpenAPI
   (`https://tie.digitraffic.fi/swagger/openapi.json`, read 2026-10-13) marks the v1 route deprecated: "Will be
   removed after 2026-10-20". The v2 routes are `/api/traffic-message/v2/{traffic-announcements, roadworks,
   weight-restrictions, exempted-transports}`.
   - The response shape is the same, except that enumerations are lower case with spaces. The parser normalizes
     both forms.
   - The parameter values, the per-type requests, `getConditional(uri, "msg:" + type)`, the header, ETag and
     Last-Modified are all as the prompt describes.
   - `inactiveHours`/`includeAreaGeometry` do not exist on v2.
2. **Two WZDx key feeds are excluded:** `mdot_4` and `ordot` need a key, but their registry URLs show no parameter,
   so the registry does not document where the key goes. Header placement is supported in code but used by no
   vendored feed.
3. **Repeated WZDx feature ids.** UDOT listed 744 features under 171 ids, WSDOT 545 under 539, with different
   properties.
   - Every feature of a repeated id gets `id~` plus 8 hex characters of SHA-256 over data source, direction, start,
     end, creation date and first coordinate, so no entry is dropped.
   - An exact repeat is skipped.
4. **Headlines.** DriveBC's `headline` is just the event type (`CONSTRUCTION`), so the description's first sentence
   is used when the headline repeats the type. Other cases:
   - WZDx: `name` (4.2), else the first sentence;
   - NDW: the first public comment, else the record's type code in words with its cause, for example "Carriageway
     closures (road maintenance)".
5. **Mapping choices the prompt leaves open:**
   - WZDx severity from `vehicle_impact` (WZDx has none);
   - DATEX `ReroutingManagement` → restriction, `AbnormalTraffic`/`GeneralNetworkManagement` → other;
   - `RoadOrCarriagewayOrLaneManagement` without a closure type → roadwork when the cause is maintenance or
     construction, else restriction;
   - Digitraffic `pos`/`neg` directions → unknown, because they are relative to the road address.
6. **Open511 schedule intervals are read as UTC.** DriveBC's are zone-less, and the capture shows they are UTC: an
   event created at 11:02 PDT starts at `18:02`.
7. **Empty documents count as complete for absence.** 072's adapter skipped absence for empty catalogs, but road
   feeds are legitimately empty (Digitraffic weight restrictions). Two consecutive empty well-formed documents are
   still required.
8. **Retention 1825 d (`event`)** and `zeroResultsNormal` are not named by the prompt. Redirects follow the default,
   because NCDOT's registry URL redirects to `www.drivenc.gov`.
9. **Payload additions:** `geometrySimplified`/`simplifyToleranceDeg`, as the 073 kit does, when simplification ran.
10. **Spring constructors:** the four new Spring beans mark their injection constructor `@Autowired`, because each
    has two public constructors.
11. **Fixture privacy:** UDOT's feed-info contact (a person's name and e-mail) was replaced with `redacted` in the
    fixture.

## Issues found / open questions

1. **Possible startup defect in earlier families (for 097).** `TransitFamily` (070), `GbfsFamily` (071),
   `CctvCatalogFamily` (072) and `FeatureKitFamily` (073) each have two public constructors and no `@Autowired`.
   Spring cannot then choose a constructor and falls back to a no-arg one, which these classes do not have. Pre-057
   classes with two constructors (`NwsAlertsAdapter`) use `@Autowired`. I did not change them: out of scope, and not
   verified because tests are not run.
2. **WZDx license text:** the registry records no license per feed, so every feed says "Publisher terms (listed in
   the USDOT WZDx Feed Registry …)". The registry itself is "Public Domain U.S. Government". Some feeds declare
   `feed_info.license` (UDOT and NDDOT: CC0), which is not read.
3. **`wzdx-civiclink-crewcast`** is a community-run feed on `replit.app` (issuing organization "CivicLink"). It
   meets the prompt's mechanical rules, so it is included.
4. **Other Open511 servers:** `api.511.org` answers 401 without a key and uses `/traffic/events`, so it was not
   added. `www.open511.org` did not answer.
5. **NDW's CC0 statement** is on `https://www.ndw.nu/copyright` and covers the website's content (images excepted).
   Applying it to the open-data files is the reading the prompt gives.
6. **Unreachable at capture:** `mn.carsprogram.org` (MnDOT) timed out, and NCDOT answered with a 302 redirect.
7. **US English:**
   - `check_us_english.py` on the new files and the added doc lines: the only hits are allowed exceptions:
     - the `licence` builder and wire field;
     - "Open Government Licence – British Columbia" (a license name);
     - the WZDx `cancelled` enumeration value, and its alternate spelling matched defensively in Digitraffic.
   - The workspace guard `test_us_english.py` ran 14 tests, OK. It scans the MeshDTN repositories, not this one.
8. **`docs/Source-Catalog.html`** was not regenerated (as in 070–073).
9. **Instructions in data:** none. The Open511, WZDx, Digitraffic and NDW responses, the registry, the Digitraffic
   OpenAPI and the license pages were read, and nothing addressed an agent.

**Owner questions (non-blocking):**
1. Fix the missing `@Autowired` in the 070–073 families now, or leave it to 097?
2. Read `feed_info.license` per WZDx feed for the descriptor license, or keep "publisher terms"?
3. Keep the community CivicLink feed?

## Tests written (not run)

- **`RoadEventPayloadTest`:** null omission, key order, truncation (headline, 4 KB UTF-8, surrogates, 10 roads),
  closure flag, speed sentinels, URL filter, status from the schedule, refused vocabulary values, kebab and text
  helpers.
- **`Open511ParserTest`:** the DriveBC fixture (25 events, category/severity/status counts, 5 closures, 13
  `validUntil`), one event in full, edge cases, parameterized category and severity tables, simplification of a
  20,000-vertex line, `SnapshotContract`, `FieldSpecContract`, format errors.
- **`WzdxParserTest`:** WSDOT 4.2, UDOT and NDDOT 4.0 (repeated ids kept apart), 3.1/CWZ/no version refused, edge
  cases, parameterized category and impact tables, `SnapshotContract`, `FieldSpecContract`.
- **`DigitrafficRoadParserTest`:** announcements (8, 3 closures), road works (14 plus 3 ended), empty weight
  restrictions, contact dropped, edge cases, category and severity tables, enumeration normalization,
  `SnapshotContract`.
- **`Datex3SituationParserTest`:** trimmed situations (30) and SRTI (20), the whole gzip files (995 and 169), the
  axis order (every coordinate inside lat 50.5–53.7, lon 3.2–7.3), edge cases, category and severity tables, XXE, the
  decompression bomb and corrupt gzip, `SnapshotContract`.
- **`WzdxRegistrySnapshotTest`:** 31 feeds and the snapshot date, no tokens or credential parameters, unique ids,
  key feeds declaring `apiKey`, valid descriptors, frequency clamping.
- **`RoadEventAdaptersTest`** (`@IntegrationTest`, WireMock, real client):
  - all 35 descriptors validate;
  - Open511 `next_url` paging, the 10-page limit and an off-host link;
  - Digitraffic per-type conditional GET with `Digitraffic-User`, gzip and 304, the `situationTypes` param and the
    partial result;
  - NDW gzip and 304, and an HTML body;
  - WZDx 4.2 and 3.1, the key required before any request, query-key redaction in traces, exchanges and logs, header
    placement;
  - Open511 absence: two misses → one tombstone.
- **`RoadEventsIngestIT`** (shares `WeatherIngestSupport`'s context):
  - 35 sources registered and disabled;
  - Digitraffic road works → 14 versions with `valid_from`/`valid_until_explicit`, no `contact`; absent twice → one
    tombstone;
  - an Open511 event turning `ARCHIVED` is tombstoned at once.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi`: valid; 71 warnings, unchanged.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests, covers all ops; no change.
- No project tests were run (feature prompt).
- **Scratch check:** a throwaway `main` outside the repository loaded every family, ran `DescriptorValidator` (no
  problems) and parsed every fixture to get the exact counts in the assertions. No project test was executed.

## Notes for the next prompt

1. **075:** reuse `RoadFetch`'s pattern (`AbsenceTombstoner` with real outcomes, scopes per document) and
   `FeatureGeometry.fit` for large perimeters. Adapters do not import each other; shared helpers belong in
   `source/support` or `source/kit`.
2. **097 checkpoint:**
   - first run of every 074 test;
   - `RoadEventsIngestIT` uses `WeatherIngestSupport` (one new property, `foresignal.test.adapters.roads.base-url`);
   - check Issue 1 (constructor selection in the 070–073 families) first, because it would stop context startup;
   - check the hand-computed counts.
3. **143:**
   - recapture per the fixture READMEs;
   - verify a keyed WZDx feed with a real key (`FORESIGNAL_VERIFY_*`);
   - recheck MnDOT reachability and DriveBC paging (no `next_url` was ever seen).
4. **144 (Flyway):** no schema change. Adapter state:
   - `open511-*`: `absence:ids`, `absence:missed`;
   - `wzdx-*`: `etag:feed`, `lastmod:feed`, `absence:*`;
   - `digitraffic-road`: `etag:msg:{TYPE}`, `lastmod:msg:{TYPE}`, `absence:ids:{TYPE}`, `absence:missed:{TYPE}`;
   - `ndw-*`: `etag:situations`, `lastmod:situations`, `absence:*`.
5. **Owner decisions pending:** the three questions above, plus those carried from 012–073.
