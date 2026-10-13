# 071-CR-GBFS-Bikeshare

| | |
|---|---|
| Prompt | 071-GBFS-Bikeshare.md |
| Status | Complete |
| Implementation commit | 38ade4310733b1fb81f80ffbadef87ac3fe7a45c |
| Date | 2026-10-13 |

## Summary

The `gbfs-*` family is in: one `gbfs-{id}` source per reviewed docked bikeshare system. Each source merges
`station_information` and `station_status` into one `bikeshare-station` record per station. It reads GBFS 1.x, 2.x
and 3.0 documents, requests only reviewed URLs, and never follows a redirect.

**1. Reviewed list** (`GbfsSystem`, `GbfsSystems`):
- **Ported from GEV `RAW_GBFS_CITY_REGISTRY`,** including the BCycle URL builder (`bcycleUrls`) and the registry
  validation. Each entry has `id, city, provider, stationInformationUrl, stationStatusUrl, gbfsVersion, license,
  licenseUrl, attribution`.
- **30 systems, not 32,** after checking every GEV URL on 2026-10-13:
  - **Moved** (the old BCycle URLs answer 404):
    - Tucson (Tugo) → `tucson.publicbikesystem.net/customer/gbfs/v2/en/`;
    - Fort Worth (Trinity Metro) → `fortworth.publicbikesystem.net/...`;
    - Buffalo (Reddy) → `https://reddybikeshare.socialbicycles.com/opendata/` (GBFS 1.0).

    The new URLs come from MobilityData `systems.csv`, which was read and not vendored. Each was verified with a 200.
  - **Dropped:**
    - Fort Lauderdale (Broward B-cycle): 404 on every file, including `gbfs.json`;
    - Columbus (CoGo): `gbfs.cogobikeshare.com` no longer resolves, and Lyft's `cmh` feed lists `stations: []`.
- **Host allowlist:** derived from the list, 10 hosts.
  - `isAllowedHost` drops GEV's `*.publicbikesystem.net` wildcard: only listed hosts pass.
  - `isAllowedPath` requires the path to end in `station_information.json` or `station_status.json`.
  - `isAllowed` also requires https, no port, no query, no fragment and no user-info.
- **Startup check:** `GbfsSystems.problems` validates the list (ids, file names per URL, version, https license
  URL), and `GbfsFamily` refuses to start on any problem.
- No `gbfs.json` discovery.
- **Licenses:** I read `system_information.json` for all 30 systems, plus Austin's 3.0 feed. **None declares
  `license_id`, `license_url` or attribution fields**, so every system carries `license = "operator terms"`:
  - **Data pages:**
    - Citi Bike `citibikenyc.com/data-sharing-policy`;
    - Divvy, Capital Bikeshare and Bluebikes `…/data-license-agreement`;
    - Bay Wheels `lyft.com/bikes/bay-wheels/system-data`;
    - BIKETOWN `biketownpdx.com/system-data`;
    - Indego `rideindego.com/about/data/`;
    - Metro Bike `bikeshare.metro.net/about/data/`.
  - **Home pages,** where no data page was found:
    - **Operator sites:** CapMetro, Biki, Bike Chattanooga, Tugo, Trinity Metro, Reddy.
    - **BCycle systems:** the `url` from each system's own `system_information`, upgraded to https. Five of those
      sites did not answer at capture: Cincinnati, El Paso (502), Des Moines, Omaha and Las Vegas.

  The prompt asks for the systems on "operator terms" to be listed: that is all 30.

**2. Descriptor** (`GbfsDescriptors`, `GbfsFamily`):
- **Fetching:** POLLING 60 s (min 30 s), 3/min and 2000/day, 5 MB cap, `fetchTimeout` 15 s, `followRedirects(false)`.
- **Policy:** STANDARD, `LATEST{20m, 30s}`, TTL 30 min, RECEIVED anchor. `1200 ≤ 1800 − 60 − 30` holds, and
  `IngestPolicyValidator` passes.
- **Significant fields:** `name, capacity, isInstalled, isRenting, isReturning, availability`.
- **Display and search:** title `name`; search `name, address`; tiles `bikesAvailable, docksAvailable, availability,
  ebikesAvailable`; `zeroResultsNormal` false; kind display `bicycle #1971C2`.
- **Retention:** 365 d (the `state` class).
- **Parameter:** `language` (default `en`), checked as a BCP 47 tag.
- **License fields:** license, terms URL and attribution come from the list entry.

**3. Fetch and merge** (`GbfsAdapter`, `GbfsParser`):
- **Information:** fetched with a conditional GET when the in-memory cache is empty, older than max(its `ttl`, 1 h),
  or was read in another `language`.
  - When nothing is cached (after a restart), the stored validators are forgotten first. Otherwise the first request
    after a restart would get a 304 and no stations.
- **Status:**
  - **Skipped** (`unchanged`) while the previous `last_updated + ttl` is later than now plus the descriptor's poll
    interval. `ttl 0` means always refresh. A `ttl` above 1 h is honored only up to 1 h, as a safety cap.
  - **Fetched** otherwise with a conditional GET; a 304 is `unchanged`.
- **Failures:**
  - **Redirects:** any 3xx throws `UpstreamFormatException("redirect refused: {host} (to {Location host})")`. The
    target is never requested, and the run counts as a failure, not a backoff.
  - **Bad bodies:** a non-200 2xx, a truncated body or an unparseable document is a format error, and its
    validators are forgotten.
- **Parser** (pure):
  - **Envelopes:** both, plus GEV's locale wrapper under `data`.
  - **Names:** a string, or v3 localized names, picked by exact tag, then primary subtag, then the first entry.
  - **Times:** epoch seconds or numeric strings, or RFC 3339. A time before 2000-01-01 counts as missing (see
    Deviation 4).
  - **Counts:** `num_vehicles_available` or `num_bikes_available`, and `num_vehicles_disabled` or
    `num_bikes_disabled`. `ebikesAvailable` is `num_ebikes_available`, else BCycle's
    `num_bikes_available_types.electric`.
  - **Booleans:** GEV's `normalizeGbfsBool`.
  - **Duplicates:** when a station id repeats, the newest report wins.
- **Records:**
  - `externalId = station_id`, point from information, `observedAt = last_reported`; the payload is exactly the 14
    keys of the prompt and the OpenAPI kind entry.
  - **`availability`:** `out-of-service`, then `empty`, `low` (≤ 2 bikes), `full`, `ok`. A low count wins over 0
    docks, as the prompt's order says.
- **Counters:**
  - stations with no usable `last_reported`: `foresignal.gbfs.untimed{source}`;
  - status-only stations: `foresignal.gbfs.unmatched{source}`.
- **Result:** `complete = true`; no tombstones.

**4. Fixtures** (`src/test/resources/fixtures/gbfs/`, README with date, URLs and the license check):
- **Captured 2026-10-13 16:46–16:52 UTC,** each trimmed to 25 matching ids:
  - `citibike/` (Lyft 2.3);
  - `boulder-bcycle/` (BCycle 1.1);
  - `austin-capmetro/` (publicbikesystem 2.3);
  - `austin-capmetro-v3/`, the same system's real 3.0 feed. It is not hand-made.
- **Hand-made** (`synthetic/`):
  - **Named in the prompt:** `status-only-station.json`, `bool-as-int.json`, `ttl-future.json`,
    `localized-name-v3.json`;
  - **Extra:** `information.json` and `missing-last-reported.json`;
  - **Redirect:** a WireMock 302 in the test, not a file.

**5. Docs:**
- **`docs/Source-Catalog.md`:** the `gbfs-{id}` row replaced, and the roadmap line marks it built in 071.
- **`docs/Operator-Guide.md`:** a "Bikeshare (prompt 071)" section with the 30 sources, and "Proposing another
  system" (a code review only).
- **`docs/Adapter-Guide.md` §9:** notes on reviewed lists, derived allowlists, test rebasing and refused redirects.
  It also says a `systems.csv` catalog resolver like 070's could follow, with CC BY 3.0 attribution.
- **`docs/OpenAPI.json`:** no operation changes. The `bikeshare-station` kind note is expanded and a changelog entry
  is added.
- **`THIRD_PARTY_NOTICES.md`:** four GEV rows (`registry.js`, `gbfsSource.js`, `gbfs.js`, `gbfsStations.js`), with
  071 added to the prompt list.

## Files created / modified

- **Created, main:** `source/adapters/gbfs/` with `package-info`, `GbfsSystem`, `GbfsSystems`, `GbfsParser`,
  `GbfsDescriptors`, `GbfsAdapter`, `GbfsFamily`.
- **Created, test:**
  - `source/adapters/gbfs/`: `GbfsParserTest`, `GbfsSystemsTest`, `GbfsAdapterTest`, `GbfsIngestIT`;
  - `fixtures/gbfs/`: the README, 4 captured pairs and 6 hand-made files.
- **Modified, test:**
  - `testsupport/weather/WeatherIngestSupport`: the GBFS base-URL property, and `gbfs-boulder-bcycle` added to the
    backoff list;
  - `retention/FamilyDefaultsTest`: `GbfsAdapter` as `state`.
- **Docs:** `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Adapter-Guide.md`, `docs/OpenAPI.json`,
  `THIRD_PARTY_NOTICES.md`. The Postman collection was regenerated with no change.

## Deviations from the prompt

1. **30 systems instead of 32** (see Summary 1): 3 URLs updated to the operators' current feeds and 2 dead systems
   dropped. Leaving them in would ship sources that fail every run.
2. **The redirect hook already existed.** `SourceDescriptor.followRedirects` came in prompt 064: with false,
   `BoundSourceHttp` uses the `Redirect.NEVER` client and hands back the 3xx as a response. CelesTrak relies on
   that. So the `UpstreamFormatException` is thrown in `GbfsAdapter`, not in `UpstreamClient`; the behavior is what
   the prompt asks for. The shared client still records the 3xx as a request that succeeded at the HTTP level, so
   there is no backoff; the adapter's exception fails the run.
3. **Record components are `license`/`licenseUrl`**, not `licence`/`licenceUrl`: the project spells identifiers in
   US English, as 070 did. The descriptor wire field stays `licence`.
4. **Placeholder times count as missing.** The prompt says a station without `last_reported` is dropped and counted.
   Lyft sends `last_reported: 86400` for stations that never reported: 54 of Citi Bike's 2519, 1 of Divvy's, 2 of
   Bluebikes'. I treat any time before 2000-01-01 as missing (dropped and counted), not as a 1970 observation.
5. **`ebikesAvailable` is absent for v3.0 documents.** Which `vehicle_types_available` entries are electric is
   published only in `vehicle_types.json`, which is outside the reviewed path allowlist. For 1.x and 2.x it is
   `num_ebikes_available`, else BCycle's `num_bikes_available_types.electric`. In the parser test, v2.3 and v3.0
   payloads have the same shape, on the same system (Austin).
6. **`gbfsVersion` describes the system; it does not switch the parser.** `2.x` covers GBFS 1.x too: BCycle publishes
   1.1, Bluebikes 1.1 and Reddy 1.0, with the same station fields. The parser reads either shape whatever the entry
   says. Every reviewed entry is `2.x`, because GEV's URLs are the v2 ones; the publicbikesystem.net systems also
   publish 3.0.
7. **"The next scheduled run" is now plus the descriptor's poll interval.** `FetchContext` does not expose an
   operator's interval. With a longer operator interval the skip is conservative: it can request when it did not
   need to, but it never skips data. The honored status `ttl` is capped at 1 h (not stated in the prompt).
8. **Extra behaviors** (not stated):
   - a change of `language` refreshes the information cache;
   - information returning 304 with nothing cached is a format error, after forgetting the validators;
   - information entries at 0/0, out of range or without coordinates are skipped;
   - a name falls back to `short_name`.
9. **The keyframe step in `GbfsIngestIT` accepts KEYFRAME or HEARTBEAT.** Under row 8 of the engine, the keyframe is
   HEARTBEAT when the live row is clean and KEYFRAME when it is dirty. I could not confirm, without running it,
   whether touches clear the dirty flag after a LIVE update.

## Issues found / open questions

1. **`docs/Source-Catalog.html`** (the shareable HTML catalog) was not regenerated. 070 did not update it either, so
   it lags the Markdown for both 070 and 071.
2. **Five BCycle home pages** (Cincinnati, El Paso, Des Moines, Omaha, Las Vegas) did not answer on 2026-10-13. Their
   feeds did, so the systems stay in, but their license links may be dead.
3. **Instructions in data:** none. The GEV files, `systems.csv`, every `system_information.json` and the captured
   feeds were read; nothing addressed an agent.

**Owner questions (non-blocking):**
1. Should the publicbikesystem.net systems (Austin, Honolulu, Chattanooga, Tucson, Fort Worth) move to their GBFS
   3.0 URLs? The prompt kept GEV's v2 paths, so production uses no 3.0 feed today.
2. Is dropping Broward and CoGo acceptable, or should dead systems stay in the list, disabled?
3. Should a resolver over MobilityData `systems.csv` (CC BY 3.0) be added, like 070's? It is noted in the Adapter
   Guide only.

## Tests written (not run)

- **`GbfsParserTest`:**
  - v2.3 and v3.0 of one system give the same payload keys and values; the envelopes in both time formats;
  - Citi Bike: placeholders dropped and counted, e-bikes, `full`, `empty` and `low`;
  - BCycle e-bikes from `num_bikes_available_types`;
  - localized names: exact tag, primary subtag, first entry, `fr-CA` falling back to `fr`;
  - booleans; the `availability` boundaries (0, 2, 3 bikes; 0 docks; not renting; not installed; precedence);
  - missing and placeholder `last_reported`; status-only stations;
  - duplicates and malformed entries; the locale wrapper and the format errors; times; fallbacks.
- **`GbfsSystemsTest`:**
  - 30 systems; every URL host and path allowlisted; refusals (wildcard, other paths, http, query, port);
  - unique kebab-case ids; `problems` cases; the BCycle builder;
  - every source passes `DescriptorValidator` and `IngestPolicyValidator` (the keyframe constraint);
  - the descriptor field by field; the `language` check; test rebasing keeps the publicbikesystem systems distinct.
- **`GbfsAdapterTest`** (`@IntegrationTest`, WireMock, real `UpstreamClient`):
  - information fetched once, then at 1 h with `If-None-Match`; an information `ttl` of 2 h honored;
  - status skipped while `last_updated + ttl` holds and requested once it no longer outlasts the next run; `ttl 0`;
  - 302 and 301 → `UpstreamFormatException`, with the target never requested (`verify(0, …)`) and no backoff;
  - the status-only counter; 304 for status and information;
  - a restart requests information without validators; an HTML body forgets the validators; HTTP 204.
- **`GbfsIngestIT`** (`WeatherIngestSupport`, real fetch path and writer):
  - FIRST; 5 → 4 bikes is live with no version;
  - 3 → 2 (`ok` → `low`) is SIGNIFICANT; `isRenting` false is SIGNIFICANT (`out-of-service`);
  - unchanged polls are touches; the keyframe comes after 20 min;
  - all 30 sources registered as STANDARD + LATEST with no redirects.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing unchecked-operations note).
- `./gradlew lintOpenApi`: valid; 71 warnings, the same count as before.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests across 80 tag folders, covers all ops;
  the collection is unchanged.
- No project tests or guards were run, because this is a feature prompt. The workspace US English guard
  (`~/Documents/GitLab/meshdtn/style/test_us_english.py`) ran: 14 tests, OK.

## Notes for the next prompt

1. **072 and later families:** follow `GbfsSystems`/`TransitFeeds`:
   - a list validated at startup, with the allowlist derived from it;
   - test rebasing as `/{host}{path}` when hosts share paths;
   - `followRedirects(false)` plus an adapter-level `redirect refused` for upstreams that must not redirect.
2. **097 checkpoint:**
   - first run of every 071 test;
   - `GbfsIngestIT` shares `WeatherIngestSupport`'s context (one new property,
     `foresignal.test.adapters.gbfs.base-url`);
   - check Deviation 9 (which keyframe reason) and tighten the assertion;
   - `FamilyDefaultsTest` still fails on the unclassified 062–064 adapters only.
3. **143:**
   - no key is needed; recapture per `fixtures/gbfs/README.md`;
   - recheck the five unreachable BCycle home pages and any `system_information` license fields;
   - Citi Bike's full status is about 1.1 MB per minute if enabled.
4. **144 (Flyway):** no schema change. Adapter state of `gbfs-{id}`: `etag:information`, `lastmod:information`,
   `etag:status`, `lastmod:status`. The information cache and the status expiry are in memory only.
5. **Owner decisions pending:** the three questions above, plus those carried from 012–070.
