# 092-CR-Internet-Measurement

| | |
|---|---|
| Prompt | 092-Internet-Measurement.md |
| Status | Partial |
| Implementation commit | 0eced87af16c8101b0a863567fbd3824a154114a |
| Date | 2026-10-14 |

## Summary

The internet-measurement sources land, so rules and the 099 corroboration layer can tell a national outage from
a cable cut, a BGP hijack or a cloud-provider incident.

**Shared pieces.**
- `ai.foresignal.geo.H3Cells` (new): `latLngToCell`, `cellToPolygon(long|String)` (JTS polygon, SRID 4326,
  antimeridian-normalised) and `isValid`. Reuses the already-wired `com.uber:h3` 4.5.0.
- `org.apache.commons:commons-compress` 1.27.1 added (bzip2), with a `THIRD_PARTY_NOTICES.md` entry.

**Sources built.**
- `cloudflare-radar-outages` (kind `internet-outage`): Radar outage annotations, Bearer token, RECEIVED time
  base, country-centroid geometry (approximate), CC BY-NC 4.0.
- `cloudflare-radar-bgp` (kinds `bgp-hijack`, `bgp-leak`): paged ≤ 5 each, volatile `max_msg_ts`/`leak_count`,
  CC BY-NC 4.0.
- `ripe-atlas-probes` (kind `probe-status`): the bzip2 `meta-latest` dump, status filter (default 1,2), status
  0–4, `observedAt` = `status_since`; `address_v4`/`address_v6`/`description` are never stored.
- `gpsjam-daily` (kind `jamming-cell`): manifest-gated fetch of `{date}-h3_4.csv`, H3 res 4 geometry,
  `pctBad = 100·(bad−1)/(good+bad)`, levels, tombstoned cells; local-only, disabled by default.
- `StatusPageFamily` (kinds `service-status`, `service-incident`): 11 reviewed Statuspage v2 pages (GitHub,
  Atlassian, Discord, Reddit, Dropbox, Twilio, Zoom, Anthropic, Cloudflare, DigitalOcean, OpenAI); a resolved
  incident leaves `summary.json` and is emitted once from `incidents.json` then tombstoned.
- `GoogleStatusFamily` (kind `service-incident`): `status-gcp` and `status-google-workspace` (`incidents.json`).
- `status-aws` and `status-azure` as 073 feed-kit specs (`kits/feeds/status.json`); the feed-kit id pattern now
  admits the reviewed `status-` prefix.

**Fixtures.** Live captures (Atlas bzip2, gpsjam manifest+day, GitHub summary/incidents, GCP/Workspace
incidents, AWS/Azure feeds) and synthetic Cloudflare fixtures (no Radar token), each with a README.

## Why Partial

- **`ris-live` (optional, streaming) is not built.** The prompt marks it optional and disabled by default; the
  WebSocket prefix-visibility monitor and `PrefixVisibilityTracker` were not implemented.
- **`odin` is not built** (verified: no published licence/terms/rate limit), recorded as a "Considered" note.
- **The Atlas `intradayDisconnected` param is declared but not implemented** (the intraday
  `atlas.ripe.net/api/v2/probes?status=2` refresh).
- **Some tests are thinner than the prompt lists:** `CloudflareParserTest` asserts the BGP descriptor (kinds,
  volatile fields) rather than parsing a hijack/leak body; there are no WireMock per-source adapter tests or
  `InternetMeasurementIngestIT`/`RisLiveAdapterTest`; `PrefixVisibilityTrackerTest` does not exist. These are the
  pieces to finish (see Notes).

## Files created / modified

**Created, main**
- `geo/package-info.java`, `geo/H3Cells.java`
- `source/adapters/internet/package-info.java`, `CloudflareOutageAdapter`, `CloudflareBgpAdapter`,
  `AtlasProbesAdapter`, `GpsjamAdapter`, `StatusPageAdapter`, `StatusPageFamily`, `GoogleStatusAdapter`,
  `GoogleStatusFamily`
- `src/main/resources/kits/feeds/status.json`

**Modified, main**
- `build.gradle.kts`, `gradle/libs.versions.toml` (commons-compress)
- `source/kit/feed/FeedSpecValidator.java` (id pattern admits `status-`)

**Created, test**
- `geo/H3CellsTest`, `internet/{GpsjamParserTest, AtlasDumpParserTest, StatusPageParserTest, CloudflareParserTest}`
- fixtures `gpsjam-daily/`, `ripe-atlas-probes/`, `status-github/`, `status-gcp/`, `status-google-workspace/`,
  `status-aws/`, `status-azure/`, `cloudflare-radar-outages/`, `cloudflare-radar-bgp/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (six 092 adapters classified)
- `source/kit/feature/FeatureSpecValidatorTest.java` (two new feed ids)
- `testsupport/weather/WeatherIngestSupport.java` (092 base-URL properties and SOURCES)

**Docs**
- `docs/OpenAPI.json` (changelog), `docs/Source-Catalog.md` (new section), `docs/Operator-Guide.md` (token,
  recipes), `THIRD_PARTY_NOTICES.md` (commons-compress)

## Deviations from the prompt

1. **`ai.foresignal.geo.H3Cells` is new**, alongside the existing `stats.H3Cells` (038); the former wraps the
   raw cell→polygon conversion the sources need.
2. **The feed-kit id pattern now admits `status-`**, as 088 added `imf-portwatch` to the feature-kit pattern.
3. **Cloudflare fixtures are synthetic** (no Radar token available); the sources are implemented and verified for
   shape only. Recapture at 143.
4. **RIS Live, ODIN and the Atlas intraday refresh** are not built (see "Why Partial").

## Issues found / open questions

1. **gpsjam serves gzip and the Atlas dump is bzip2**; the probe used `--compressed` for gpsjam and kept the
   `.bz2` for the test to decompress.
2. **Cloudflare Radar needs a token** — unverified live.
3. **Instructions in data:** none.

## Tests written (not run)

- `H3CellsTest`, `GpsjamParserTest` (formula, thresholds), `AtlasDumpParserTest` (bzip2, dropped host fields),
  `StatusPageParserTest` (Statuspage and Google), `CloudflareParserTest` (outage parse; BGP descriptor).
- Missing (to finish): `CloudflareBgpParserTest`, per-source WireMock adapter tests, `PrefixVisibilityTrackerTest`,
  `InternetMeasurementIngestIT`, `RisLiveAdapterTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `docs/OpenAPI.json`: valid JSON.

## Notes for the next prompt

1. **Finish 092 before 097:** the RIS Live adapter + `PrefixVisibilityTrackerTest`; the Atlas intraday refresh;
   the Cloudflare BGP body parse and the WireMock adapter tests; `InternetMeasurementIngestIT`.
2. **097 checkpoint:** first run of the 092 tests; `WeatherIngestSupport` gained the 092 properties;
   `FamilyDefaultsTest` gained six entries; `FeatureSpecValidatorTest.shipped` now expects `status-aws`/`status-azure`.
3. **099 (derived layers):** outages/BGP/jamming corroborate IODA (068).
4. **143:** recapture the Cloudflare fixtures with a Radar token; the other fixtures per their READMEs.
5. **144 (Flyway):** no schema change. New adapter state: `atlas:ids`, `gpsjam:lastDate`, `gpsjam:ids`,
   `status:open`, `status:summary`, `google-status:index`.

Implementation commit: 0eced87af16c8101b0a863567fbd3824a154114a
