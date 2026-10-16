# 092-CR-Internet-Measurement

| | |
|---|---|
| Prompt | 092-Internet-Measurement.md |
| Status | Complete (post-review fixes applied 2026-10-16) |
| Implementation commit | 1a65fc2dcd9caaa5367636270742d436871c221f (`092: Internet Measurement (complete)`; the first, partial commit was 0eced87a) |
| Post-review fix commit | f5ec714 (`092: fix post-review defects`) |
| Date | 2026-10-14 (post-review fixes 2026-10-16) |

## Summary

The internet-measurement sources land, so rules and the 099 corroboration layer can tell a national outage from
a cable cut, a BGP hijack or a cloud-provider incident. The work arrived in two commits: 0eced87a delivered the
polling sources and fixtures, 1a65fc2d added RIS Live, the Atlas intraday path and the remaining tests. A code
review then found defects in both; they are fixed in f5ec714 and listed under "Post-review fixes".

**Shared pieces.**
- `ai.foresignal.geo.H3Cells` (new): `latLngToCell`, `cellToPolygon(long|String)` and `isValid`. Since the
  post-review fix, `cellToPolygon` returns a `Geometry` with every longitude in [-180, 180]: a cell across the
  antimeridian is split into a two-part `MultiPolygon` (before, its negative longitudes were shifted past 180,
  which the normalizer rejects). Reuses the already-wired `com.uber:h3` 4.5.0.
- `org.apache.commons:commons-compress` 1.27.1 added (bzip2), with a `THIRD_PARTY_NOTICES.md` entry.

**Sources.**
- `cloudflare-radar-outages` (kind `internet-outage`): Radar outage annotations, Bearer token, RECEIVED time
  base, country-centroid geometry (approximate), CC BY-NC 4.0. Query values are URL-encoded; Radar's `location`
  filter takes one alpha-2 code, so one configured code goes upstream and several are filtered locally. The parser
  reads the documented shape (`outage.outageCause`, `asnsDetails[].location.code`) and the flat one.
- `cloudflare-radar-bgp` (kinds `bgp-hijack`, `bgp-leak`): paged ≤ 5 pages per endpoint, stopping at
  `result_info.total_count`; AS names from `result.asn_info`; geometry = hijacker and victim countries (a leak's
  countries) as a Point or MultiPoint; `involvedAsn`/`involvedCountry` sent upstream when one value is set
  (Radar takes one), filtered locally for several; `observedAt` = `max_msg_ts`, falling back to `max_hijack_ts`
  for a whole response that lacks it (logged); the volatile counters are in the payload. Interval 15 min
  (min 5 min), budget 10/min and 1,000/day. CC BY-NC 4.0.
- `ripe-atlas-probes` (kind `probe-status`): the bzip2 `meta-latest` dump by conditional GET, streamed and
  capped at 256 MB decompressed, status filter (default 1,2), status 0–4, `observedAt` = `status_since`;
  `address_v4`/`address_v6`/`description` are never stored. The dump is checked at most every 6 h; with
  `intradayDisconnected` and a 30-minute interval the runs in between page the Probe API's disconnected list
  (≤ 5 pages of 500) into whole probe records. Interval 24 h (min 30 min), budget 6/min and 260/day.
- `gpsjam-daily` (kind `jamming-cell`): manifest-gated fetch of `{date}-h3_4.csv`, H3 res 4 geometry,
  `pctBad = 100·(bad−1)/(good+bad)`, levels, tombstoned cells; local-only, disabled by default. Budget 2/min and
  8/day (a new day costs two requests in one run).
- `StatusPageFamily` (kinds `service-status`, `service-incident`): 11 reviewed Statuspage v2 pages (GitHub,
  Atlassian, Discord, Reddit, Dropbox, Twilio, Zoom, Anthropic, Cloudflare, DigitalOcean, OpenAI). The indicator
  is read from the top-level `status` object; a resolved incident leaves `summary.json` and is emitted once from
  `incidents.json` then tombstoned; a 304 changes nothing; `incidents.json` is fetched at most once per run. The
  OpenAI page (incident.io) serves no `incidents` array in `summary.json`, so its open incidents are the
  unresolved ones of `incidents.json`.
- `GoogleStatusFamily` (kind `service-incident`): `status-gcp` and `status-google-workspace` (`incidents.json`).
- `status-aws` and `status-azure` as 073 feed-kit specs (`kits/feeds/status.json`); the feed-kit id pattern now
  admits the reviewed `status-` prefix.
- `ris-live` (kind `bgp-prefix-state`; `RisLiveAdapter`, `RisLiveSession`, `PrefixVisibilityTracker`, `Cidr`):
  optional, streaming, disabled by default. One serialized `ris_subscribe` per watched prefix (per prefix and host
  when `hosts` is set) and a `RIS_PEER_STATE` request, never the firehose. Announcements are matched by CIDR
  containment: an exact prefix feeds per-peer visibility, a more-specific the `moreSpecifics` list. Origin = the
  last AS of each path (a trailing AS_SET flattened and flagged); `unexpectedOrigin` compares that origin with
  `expectedOrigins`. A record is emitted when the banded state changes, and every ten minutes from the watchdog
  even when the stream is quiet. When the connection fails each prefix is emitted with `status: no-data` and its
  last visibility; trackers live on the adapter, so a reconnect continues from them. Messages from hosts outside
  `hosts` are ignored. Reconnect after a server "too slow" error; five-minute silence watchdog.

`odin` remains a "Considered" note (no published licence) and is correctly out of scope.

**Fixtures.** Live captures (Atlas bzip2, gpsjam manifest+day, GitHub summary/incidents, OpenAI summary/incidents
(added 2026-10-16), GCP/Workspace incidents, AWS/Azure feeds) and synthetic Cloudflare fixtures (no Radar token),
each with a README.

## Files created / modified

**Created, main**
- `geo/package-info.java`, `geo/H3Cells.java`
- `source/adapters/internet/package-info.java`, `CloudflareOutageAdapter`, `CloudflareBgpAdapter`,
  `AtlasProbesAdapter`, `GpsjamAdapter`, `StatusPageAdapter`, `StatusPageFamily`, `GoogleStatusAdapter`,
  `GoogleStatusFamily` (0eced87a); `PrefixVisibilityTracker`, `RisLiveAdapter`, `RisLiveSession` (1a65fc2d);
  `Cidr` (post-review)
- `src/main/resources/kits/feeds/status.json`

**Modified, main**
- `build.gradle.kts`, `gradle/libs.versions.toml` (commons-compress)
- `source/kit/feed/FeedSpecValidator.java` (id pattern admits `status-`)

**Created, test**
- `geo/H3CellsTest`, `internet/{GpsjamParserTest, AtlasDumpParserTest, StatusPageParserTest, CloudflareParserTest}`
  (0eced87a)
- `internet/{CloudflareBgpParserTest, GoogleStatusParserTest, PrefixVisibilityTrackerTest, RisLiveAdapterTest,
  RisTestContext, InternetAdaptersTest, InternetMeasurementIngestIT}` (1a65fc2d)
- `internet/CidrTest` and fixture `status-openai/` (post-review)
- fixtures `gpsjam-daily/`, `ripe-atlas-probes/`, `status-github/`, `status-gcp/`, `status-google-workspace/`,
  `status-aws/`, `status-azure/`, `cloudflare-radar-outages/`, `cloudflare-radar-bgp/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (six 092 adapters classified)
- `source/kit/feature/FeatureSpecValidatorTest.java` (two new feed ids)
- `testsupport/weather/WeatherIngestSupport.java` (092 base-URL properties and SOURCES)

**Docs**
- `docs/OpenAPI.json` (changelog), `docs/Source-Catalog.md` (new section; post-review: corrected intervals and
  budgets, the `ris-live` row), `docs/Operator-Guide.md` (token, recipes; post-review: RIS Live and Atlas
  intraday), `THIRD_PARTY_NOTICES.md` (commons-compress)

## Deviations from the prompt

1. **`ai.foresignal.geo.H3Cells` is new**, alongside the existing `stats.H3Cells` (038); the former wraps the
   raw cell→polygon conversion the sources need. `cellToPolygon` returns `Geometry` (Polygon, or MultiPolygon for
   an antimeridian cell), not `Polygon`.
2. **The feed-kit id pattern now admits `status-`**, as 088 added `imf-portwatch` to the feature-kit pattern.
3. **Cloudflare fixtures are synthetic** (no Radar token available); the sources are verified against the
   documented response shapes only. Recapture at 143.
4. **Budgets differ from the prompt where one run sends more requests than the prompt's per-minute figure.** The
   per-minute bucket holds `perMinute` tokens and a refusal throws, so: `gpsjam-daily` 2/min (manifest + day file;
   the prompt's 1/min could never fetch a day); `cloudflare-radar-bgp` 10/min and 1,000/day (two endpoints × five
   pages; the prompt's 6/min failed a full run); `ripe-atlas-probes` 6/min and 260/day (five intraday pages per
   run, 48 runs a day at 30 minutes; the prompt's 1/min and 10/day could not run the intraday refresh at all).
5. **Atlas intraday records carry every stored field**, not only `id,status,status_since,last_connected`: the
   ingest has no partial-update path, so a status-only record would replace the full one and drop its geometry
   and attributes. The Probe API is asked for the stored fields and never the host-identifying ones. The source's
   minimum interval is 30 minutes (for the intraday runs); the dump itself is not re-checked within 6 hours.
6. **Radar's `involvedAsn`, `involvedCountry` and the outages `location` take a single value** (Cloudflare API
   reference, read 2026-10-16), while the prompt specifies string lists. One value is sent upstream; several are
   applied locally to the unfiltered pages.
7. **The incident.io page needs `incidents.json` every run.** Its `summary.json` has no `incidents` array
   (verified live 2026-10-16), so open incidents cannot be read from it; a run is then two requests, inside the
   2/min budget.
8. **`ris-live` uses a per-minute/day budget (6/min, 144/day)** rather than the prompt's "6/hour"; the descriptor
   budget has no hourly window. The reconnect ladder is the supervisor's exponential `ReconnectPolicy` (5 s to
   5 min with jitter) rather than the exact 5 s → 15 s → 60 s → 300 s rungs.

## Issues found / open questions

1. **gpsjam serves gzip and the Atlas dump is bzip2**; the probe used `--compressed` for gpsjam and kept the
   `.bz2` for the test to decompress.
2. **Cloudflare Radar needs a token**, so it is unverified live.
3. **`H3Cells.cellToPolygon` callers outside 092** (`PopulationPackService.centre`, `GpsJammingGenerator`,
   `LightningHoldoverGenerator`) now receive the split MultiPolygon for an antimeridian cell. Their code compiles
   unchanged; `PopulationPackService.centre` takes the centroid, which for a split cell lies near longitude 0, so
   that caller should use the H3 cell centre instead. Left to its owner.
4. **Conditional-GET tests depend on validators being stored by a direct `fetch` through `AdapterHttpHarness`.**
   If the shared fetch framework moves validator commits to after a successful executor run, the Atlas and
   Statuspage 304 tests in `InternetAdaptersTest` will need to commit the validators the same way.
5. **Instructions in data:** none.

## Tests written (not run)

- `H3CellsTest` (round trip, invalid index, antimeridian split into a valid two-part MultiPolygon within ±180,
  an ordinary cell stays one Polygon)
- `GpsjamParserTest` (formula, thresholds), `AtlasDumpParserTest` (bzip2, dropped host fields)
- `StatusPageParserTest` (GitHub summary with the top-level indicator, an open incident's fields and capped
  update, the incident.io shim fixture, Google)
- `CloudflareParserTest` (outage parse; BGP descriptor), `CloudflareBgpParserTest` (hijack and leak, the
  documented shape with numeric ids, AS names, multi-country geometry and volatile counters, the per-response
  `max_hijack_ts` fallback, the local involved filter)
- `GoogleStatusParserTest`
- `PrefixVisibilityTrackerTest` (announce/withdraw/peer-down, a peer that never carried the prefix, origin = last
  AS, AS_SET, unexpected origin, more-specifics and their cap, no-data keeping visibility), `CidrTest` (IPv4/IPv6
  containment, the `192.168.10.0/24` look-alike, literals only)
- `RisLiveAdapterTest` (embedded WebSocket: serialized narrow subscribe, emission on change only, CIDR
  more-specifics, unexpected origin, per-host subscriptions and filtering, no-data record on disconnect,
  injection-proof subscribe JSON)
- `InternetAdaptersTest` (WireMock, real upstream client: Bearer token sent and redacted from traces, recordings
  and logs; encoded query values and local location filter; 429 with `Retry-After` and no request during
  backoff; BGP paging stopping at `total_count` and AS names; involved filters upstream and local; gpsjam at its
  real default budget with manifest gating; Atlas 304 and the 6 h dump gate; Atlas intraday whole records;
  Statuspage resolved follow-up, 304 keeping the open set, incident.io shim)
- `InternetMeasurementIngestIT` (gpsjam day-2 tombstone; Cloudflare outage re-fetch adds no version; resolved
  Statuspage incident gets a final version and a tombstone)

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (2026-10-16, after the post-review fixes).

## Post-review fixes (2026-10-16)

| # | Severity | Finding | Fix |
|---|---|---|---|
| 1 | CRITICAL | `gpsjam-daily` sent two requests per run against a 1/min bucket, so it never ingested at defaults; tests hid it with a 600/min budget | Budget 2/min, 8/day; `InternetAdaptersTest.gpsjamGatingAtDefaultBudget` runs at the descriptor's own budget |
| 2 | HIGH | Statuspage 304 left the open set empty, so every open incident was re-fetched and tombstoned | A 304 returns `unchanged` before state is touched; test `statusNotModified` |
| 3 | HIGH | Atlas intraday was unreachable (24 h interval vs a 20 h gate), paged 5 requests against 1/min, its status-only records would replace full records, and it parsed the Probe API's status object as -1 (emitting nothing) | 6 h dump gate, 30-minute minimum interval, budget 6/min and 260/day, whole records from the Probe API (status object and GeoJSON geometry parsed, tags as slugs); tests `atlasConditional`, `atlasIntraday` |
| 4 | HIGH | `originsOf` returned every AS in the path, so `unexpectedOrigin` was almost always true | Origin = last AS; a trailing AS_SET is flattened and flagged; tests assert transit ASes are not origins |
| 5 | MEDIUM | RIS Live matched trackers by exact text, computed more-specifics by string prefix, never emitted `no-data`, zeroed visibility when disconnected, ignored `hosts` | `Cidr` bit containment; more-specifics from announcements under the watched prefix; `no-data` record on failure keeping visibility; trackers kept across reconnects; ten-minute emission from the watchdog; per-host subscriptions and filtering |
| 6 | MEDIUM | `H3Cells.cellToPolygon` shifted negative longitudes by +360 | Antimeridian cells split into a valid MultiPolygon within ±180; test `antimeridian` |
| 7 | LOW | Atlas dump decompressed with no cap; Cloudflare query values and RIS subscribe JSON concatenated unencoded | Streamed parse with a 256 MB cap; `URLEncoder` for query values; subscribe JSON built with Jackson |
| — | undisclosed | `cloudflare-radar-bgp` never sent `involvedAsn`/`involvedCountry`, had no AS names, geometry from one country, min interval 15 min | Implemented (Deviation 6); AS names from `asn_info`; all involved countries; min interval 5 min |
| — | undisclosed | OpenAI's incident.io page treated as plain Statuspage | Open incidents from `incidents.json` when `summary.json` has no `incidents` array (Deviation 7) |
| — | found while fixing | The Statuspage indicator was read from `page.status`, which neither Statuspage nor the shim serves, so `indicator` was always null | Read from the top-level `status` object (fallback `page.status`) |
| — | found while fixing | The outage parser read `outageCause`/`outageType` flat and `asnsDetails[].location` as a string; Radar documents `outage.{…}` and `location.code` | Both shapes accepted |
| — | docs | Source-Catalog and Operator-Guide still said RIS Live was not built | Rows and guidance rewritten |

**CR corrections.** The previous version of this report kept text from the partial commit: Deviation 4 said RIS
Live, ODIN and the Atlas intraday refresh were not built; "Notes for the next prompt" said to finish 092 before
097; the closing line cited 0eced87a as the implementation commit; the files list omitted
`PrefixVisibilityTracker`, `RisLiveAdapter`, `RisLiveSession` and the 1a65fc2d tests. It also claimed a `no-data`
status and a working Atlas intraday refresh, neither of which worked until f5ec714. All corrected above.

## Notes for the next prompt

1. **097 checkpoint:** first run of the 092 tests; `WeatherIngestSupport` gained the 092 properties;
   `FamilyDefaultsTest` gained six entries; `FeatureSpecValidatorTest.shipped` now expects `status-aws`/`status-azure`.
2. **099 (derived layers):** outages/BGP/jamming corroborate IODA (068). `H3Cells.cellToPolygon` may return a
   MultiPolygon (Issue 3).
3. **143:** recapture the Cloudflare fixtures with a Radar token; the other fixtures per their READMEs.
4. **144 (Flyway):** no schema change. Adapter state: `atlas:ids`, `atlas:lastDaily`, `gpsjam:lastDate`,
   `gpsjam:ids`, `status:open`, `status:summary`, `google-status:index`.

Implementation commit: 1a65fc2dcd9caaa5367636270742d436871c221f
