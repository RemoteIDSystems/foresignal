# Third-party notices

Foresignal itself is distributed under the MIT licence (see [LICENSE](LICENSE)). This
file records third-party works whose code has been incorporated into Foresignal, and
the notices those works require.

Foresignal's reuse policy (Architecture §16) is deliberately narrow:

| Repository | Licence | Use in Foresignal |
|---|---|---|
| God's Eye View | MIT | Fetch and parse logic may be ported to Java, with an origin header comment in the ported file and an entry in the table below. |
| ShadowBroker | AGPL-3.0 | Reference only — endpoints, parameters and upstream quirks. No code copied. |
| Velocity | AGPL-3.0 | Reference only. No code copied. |
| World Monitor | AGPL-3.0 | Reference only. No code copied. |
| SatDump | GPL-3.0 | Reference only (prompt 049). **SatDump is not distributed with Foresignal**: no SatDump code, headers, structs or projection code is incorporated, and no Foresignal image contains SatDump binaries or SatDump-produced images. The operator builds and runs SatDump as a separate program (the compose `satdump` profile uses an image the operator builds from SatDump's own Dockerfile); Foresignal reads its output files and its HTTP `/api`. The optional 2.0 hook `contrib/satdump/foresignal_hook.as` (prompt 107) is **Foresignal-authored MIT** code that runs inside SatDump's own scripting plugin; it is a separate, operator-copied file, never bundled or linked, and it copies no SatDump source, headers or structs. |
| MeshCore | MIT | Protocol reference (prompt 083): the companion protocol constants and frame layouts of `source.formats.meshcore` were written from MeshCore's `docs/companion_protocol.md`, `examples/companion_radio/MyMesh.cpp` and `src/helpers/esp32/SerialWifiInterface.cpp` (consulted, not copied; see "MeshCore" below). |
| Meshtastic firmware and protobufs | GPL-3.0 | Reference only (prompt 083): the JSON field names of the firmware's MQTT output were checked against `MeshPacketSerializer.cpp`. **No Meshtastic code, protobuf class or `.proto` file is in this repository**; every Meshtastic path reads JSON. |

Data fetched from upstream providers at runtime is **not** covered by Foresignal's
licence. Each provider's terms continue to apply; see
[`docs/Source-Catalog.md`](docs/Source-Catalog.md) for per-source details.

---

## God's Eye View

Portions of Foresignal's adapter fetch and parse logic are ported from
[God's Eye View](https://github.com/bilawalsidhu/gods-eye-view), which is
distributed under the MIT licence. Ported files carry an origin comment naming
the source file, and are listed in the table below.

Only source code is ported. The upstream `LICENSE` additionally carries a
notice covering bundled datasets and 3D models that are *not* MIT-licensed;
none of those assets are used by Foresignal.

### Licence

```
MIT License

Copyright (c) 2026 Bilawal Sidhu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Ported files

Ported from God's Eye View commit `591f299d11f38a612629a274463196d57ae3862e` (prompts 051, 062, 063, 064, 065, 067, 069, 070, 071, 072 and 075). Each
file carries an origin header naming its source file and this notice.

| Foresignal file | GEV origin file |
|---|---|
| `src/main/java/ai/foresignal/raster/model/GribIdx.java` | `server/providers/wind/gfs.js` (`parseGfsIdx`) |
| `src/main/java/ai/foresignal/raster/model/IdxRangePlanner.java` | `server/providers/wind/gfs.js` (`windMessageRanges`) and the 8 MB message budget of `server/providers/wind/decode.js` |
| `src/main/java/ai/foresignal/raster/model/ModelCycleSelector.java` | `server/providers/wind/catalog.js` (`selectLatestGfsCycle`, `gfsObjectKey`) |
| `src/main/java/ai/foresignal/raster/model/WindResampler.java` | `server/providers/wind/grid.js` (`resampleWindGrid`) |
| `src/main/resources/raster/styles/wind-speed@1.json` (color stops) | `src/layers/wind/fields.js` (`WIND_FIELDS.speed`) |
| `src/test/java/ai/foresignal/raster/model/WindResamplerTest.java` (test cases) | `src/data/windGrid.test.mjs` |
| `src/test/java/ai/foresignal/raster/model/GribIdxTest.java` (test cases) | `src/data/windGfs.test.mjs` |
| `src/main/java/ai/foresignal/source/formats/readsb/ReadsbAircraftMapper.java` (readsb field mapping, prompt 062) | `src/data/adsbLolFallback.js` (`normalizeAdsbLolAircraftState`) |
| `src/main/java/ai/foresignal/source/formats/readsb/ReadsbDocument.java` (`now` in seconds or milliseconds) | `src/data/adsbLolFallback.js` (`normalizeAdsbLolPointResponse`) |
| `src/main/java/ai/foresignal/source/formats/readsb/AircraftFields.java` (unit conversions, emitter categories) | `src/data/adsbLolFallback.js` (`FOOT_TO_M`, `KNOT_TO_MPS`, `FPM_TO_MPS`, `emitterCategory`) |
| `src/main/java/ai/foresignal/source/adapters/adsblol/AdsbLol.java` (30 s after a 429 without `Retry-After`) | `server/providers/aircraft/adsb-lol.js` (`RATE_LIMIT_COOLDOWN_MS`, `cooldownFor`) |
| `src/main/java/ai/foresignal/source/adapters/opensky/OpenSkyTokens.java` (OAuth token cache, 60 s refresh margin) | `server/providers/aircraft/opensky.js` (`getOpenSkyToken`) |
| `src/main/java/ai/foresignal/source/adapters/opensky/OpenSkyCredits.java` (credit governor on `x-rate-limit-remaining`) | `server/providers/aircraft/opensky.js` (credit governor, `openskyAdaptiveTtlMs`) |
| `src/main/java/ai/foresignal/source/adapters/aisstream/AisStreamMessageParser.java` (recognized message types, the liveness rule, `AUTH_TEXT`, the SOG/COG/heading sentinels and `time_utc` parsing, prompt 063) | `src/data/aisStreamAdapter.js` (`AIS_RECOGNIZED_MESSAGE_TYPES`, `aisEnvelopeMmsi`, `isRecognizedAisEnvelope`, `parseAisEnvelope`, `AUTH_TEXT`) and `server/providers/vessels/ais-store.js` (`normalizedSpeedOverGround`, `normalizedCourseOverGround`, `normalizedHeading`, `normalizeAisTimestamp`) |
| `src/main/java/ai/foresignal/source/adapters/aisstream/VesselStateStore.java` (merging static data into per-MMSI vessel state, the bounded cache) | `server/providers/vessels/ais-store.js` (`ingestAisStreamEnvelope`, `mergeAisStaticIntoLiveVessel`, `pruneAisStreamCache`) |
| `src/main/java/ai/foresignal/codec/ais/VesselStateMerger.java` (the same per-MMSI merge, generalized for every vessel source, prompt 085) | `server/providers/vessels/ais-store.js` (`ingestAisStreamEnvelope`, `mergeAisStaticIntoLiveVessel`, `pruneAisStreamCache`) |
| `src/main/java/ai/foresignal/source/adapters/aisstream/AisStreamSession.java` (liveness only from a recognized envelope, auth versus transport errors, report-fast/recycle-slow silence watchdog: 120 s and 2.5 times that) | `src/data/aisStreamAdapter.js` (`classifyAisFailure`, liveness credit) and `src/data/aisWatchdog.js` (`staleMs`, `recycleAfterMs`) |
| `src/main/java/ai/foresignal/source/adapters/aisstream/AisStreamAdapter.java` (stream URL, default message types and world box, one connection per key, hourly retry of a rejected key) | `server/providers/vessels/ais-live.js` (`AISSTREAM_URL`, `AISSTREAM_DEFAULT_MESSAGE_TYPES`, `AISSTREAM_DEFAULT_BBOXES`, `AISSTREAM_AUTH_PROBE_MS`) |
| `src/main/java/ai/foresignal/source/adapters/celestrak/CelestrakPolicy.java`, `CelestrakGpParser.java` and `CelestrakGroups.java` (an error page or empty body is a failure, never data; the `^[a-z0-9-]+$` group check; CelesTrak's descriptive-User-Agent practice, which `UpstreamClient` already follows, prompt 064) | `server/providers/space/celestrak.js` (`fetchUpstream`, the group pattern of `celestrakProxy`) and `src/data/spaceProviderRequests.js` (`celestrakTleUrl`) |
| `src/main/java/ai/foresignal/source/adapters/launchlibrary/LaunchLibraryAdapter.java` (the optional `Authorization: Token` header, sent only when a token is set, prompt 064) | `server/providers/space/launch-library.js` (`launchLibraryRequestHeaders`) |
| `src/main/java/ai/foresignal/source/adapters/firms/FirmsCsvParser.java` (columns located by header name, the unpadded `acq_time`, `isLikelyCsv`, VIIRS letter versus MODIS numeric confidence, prompt 065) | `src/data/firmsCsv.js` (`isLikelyCsv`, `parseFirmsCsv`, `acquisitionMsUtc`) and `src/data/firmsAdapt.js` (`normalizeConfidence`) |
| `src/main/java/ai/foresignal/source/adapters/firms/FirmsAdapter.java` (the four default products fetched sequentially, never without a key, errors as HTML or text rather than CSV, the 2 h forward slack, prompt 065) | `server/providers/firms.js` (`firmsProxy`, `fetchSource`, `refreshUpstream`) and `src/data/firmsCsv.js` (`FORWARD_SLACK_MS` of `filterTrailing24h`) |
| `src/main/java/ai/foresignal/source/adapters/nhc/NhcCurrentStormsParser.java` (the `activeStorms` cap of 32, the `(al\|ep\|cp)` + six digits id pattern, unique ids, sanitized strings, the advisory-number pattern and its normalization, numbers accepted as strings or numbers within a range, `www.nhc.noaa.gov`-only links, prompt 067) | `server/providers/cyclones.js` (`parseCycloneStatus`, `text`, `advisory`, `number`, `officialLink`) |
| `src/main/java/ai/foresignal/source/adapters/local/LocalReceivers.java` (local feed rules: a path ending in `aircraft.json`, no credentials, query or fragment, loopback and private hosts only, prompt 069) | `server/providers/local-receivers.js` (`parseEntry`, `LOCAL_RECEIVER_*` constants) and `src/data/tapAddress.js` (the receiver-tap address rule) |
| `src/main/java/ai/foresignal/source/adapters/local/LocalAdsbAdapter.java` (band 1090/978, the 2 s timeout, staleness by the feed's own `now` at 10 s, the receiver signal per aircraft, prompt 069) | `server/providers/local-receivers.js` (`readFeed`, `LOCAL_RECEIVER_STALE_MS`, `LOCAL_RECEIVER_TIMEOUT_MS`) and `src/sources/adsbRecords.js` (`normalizeDump1090Aircraft`: `rssiDbfs`, `band`) |
| `src/main/java/ai/foresignal/source/adapters/transit/TransitFeeds.java` (the reviewed feed registry: ids, operators, VehiclePositions URLs, the Entur header, licenses, license URLs, attributions, default modes and the admission rules, prompt 070) | `src/data/transitFeeds.js` (`TRANSIT_FEED_REGISTRY`) |
| `src/main/java/ai/foresignal/source/adapters/transit/TransitModes.java` (the transit modes and the MBTA, Entur, HSL and Metro Transit route-mode rules, prompt 070) | `src/data/transitFeeds.js` (`TRANSIT_MODES`, `mbtaRouteMode`, `enturRouteMode`, `hslRouteMode`, `metroTransitRouteMode`, `transitModeFor`) |
| `src/main/java/ai/foresignal/source/adapters/transit/GtfsRtVehicleParser.java` (plausible positions, vehicle id else entity id, bearing folding, negative speeds dropped, 6-decimal coordinates, newest duplicate kept, an over-long string drops the record, prompt 070) | `src/data/gtfsRealtime.js` (`isPlausibleVehiclePosition`, `normalizeVehicleEntity`, `decodeVehiclePositions`) |
| `src/main/java/ai/foresignal/source/adapters/transit/GtfsRt.java` (the entity cap and the 256-character string bound, prompt 070) | `src/data/gtfsRealtime.js` (`GTFS_MAX_ENTITIES`, `GTFS_MAX_STRING_CHARS`, `readBoundedString`) |
| `src/main/java/ai/foresignal/source/adapters/transit/TransitVehicleAdapter.java` and `TransitAlertsAdapter.java` (only registered feed URLs are fetched, with conditional requests and identifying headers, prompt 070; documentation only, no code beyond the rules above) | `src/sources/transitService.js` (`fetchTransitFeed`, `createTransitService`) |
| `src/main/java/ai/foresignal/source/adapters/gbfs/GbfsSystems.java` (the reviewed bikeshare system registry: ids, cities, providers, feed URLs, the BCycle URL builder, https-only and endpoint-file validation, unique ids, prompt 071) | `src/layers/bikeshare/registry.js` (`RAW_GBFS_CITY_REGISTRY`, `buildBcycleUrls`, `bcycleEntry`, `normalizeRegistryEntry`) |
| `src/main/java/ai/foresignal/source/adapters/gbfs/GbfsSystems.java` (the host and path allowlist, prompt 071) | `src/data/gbfsSource.js` (`GBFS_ALLOWED_HOSTS`, `isAllowedGbfsHost`, `isAllowedGbfsPath`) |
| `src/main/java/ai/foresignal/source/adapters/gbfs/GbfsAdapter.java` and `GbfsDescriptors.java` (no redirects followed, any 3xx refused with the redirect host reported, the 5 MB body cap, prompt 071) | `server/providers/gbfs.js` (`fetchGbfsUpstream`, `gbfsRedirectHost`, `GBFS_MAX_BODY_BYTES`) |
| `src/main/java/ai/foresignal/source/adapters/gbfs/GbfsParser.java` (station field normalization, boolean coercion, non-negative integers, the stations-array lookup, prompt 071) | `src/sources/gbfsStations.js` (`normalizeGbfsBool`, `toNonNegativeInteger`, `extractStationsArray`, `parseStationInformation`, `parseStationStatus`) |
| `src/main/java/ai/foresignal/source/adapters/cctv/CctvPacks.java` (the camera-catalog packs: endpoints, frame-origin pins, bounding boxes, the `Digitraffic-User` header, TfL's optional `app_key`, prompt 072) | `server/providers/cctv/constants.js`, `server/providers/cctv/catalog.js` (`LIVE_PACKS`) and `server/providers/cctv/normalize.js` (`isLikely*Coordinate`, `isPlausibleLatLon`) |
| `src/main/java/ai/foresignal/source/adapters/cctv/CctvJsonParsers.java` (per-pack field mapping for TfL, Caltrans, Ontario 511, Digitraffic, DriveBC, NSW, Calgary, Austin, DelDOT, Vegvesen and the bundled packs, prompt 072) | `server/providers/cctv/sources.js` (`load*SourcesFromOpenData`, `normalizeOntarioCctvUrl`, `pickOntarioCctvView`, `driveBcImageCredit`, `nswCameraToSource`, `normalizeCalgaryImageUrl`, `calgaryCameraId`, `calgaryCameraName`, `vegvesenCameraToSource`, `loadTallinnSourcesFromCatalog`, `loadWarendorfSourcesFromCatalog`) and `server/providers/cctv/normalize.js` (`fintrafficCameraName`) |
| `src/main/java/ai/foresignal/source/adapters/cctv/DatexCameraParser.java` (the DATEX II location and image join, prompt 072) | `server/providers/cctv/sources.js` (`parseTarkteeDatexLocations`, `parseTarkteeDatexImages`, `loadTarkteeSourcesFromDatex`) |
| `src/main/java/ai/foresignal/source/adapters/cctv/Compass.java` (direction words and abbreviations, prompt 072) | `src/data/directionText.js` (`directionToHeading`) and `server/providers/cctv/sources.js` (`DRIVEBC_ORIENTATION_HEADINGS`) |
| `src/main/java/ai/foresignal/source/adapters/cctv/CctvPackAdapter.java` (redirects refused, the OGC `rel=next` paging, the two DATEX documents, prompt 072) | `server/providers/cctv/sources.js` and `server/providers/cctv/catalog.js` |
| `src/main/resources/cctv/packs/tallinn.json` and `warendorf.json` (copied unchanged; sibling `NOTICE`, prompt 072) | `config/cctv_sources.tallinn.json` and `config/cctv_sources.warendorf.json` |
| `src/main/java/ai/foresignal/source/adapters/fire/NifcPerimeterMapper.java` (the WFIGS perimeter field list and per-feature normalization, prompt 075) | `server/providers/firePerimeters.js` (the query's `outFields`, `maxAllowableOffset`) and `src/layers/perimeters/records.js` (`normalizeFirePerimeterSnapshot`) |
| `src/main/java/ai/foresignal/source/adapters/radio/RadioMirrors.java` (the mirror host pattern, discovery with the 6 h cache, the fallback mirrors, prompt 075) | `server/providers/radio/transport.js` (`radioMirrorOrigin`), `server/providers/radio/catalog.js` (`mirrors`, `fetchPath`) and `server/providers/radio/constants.js` (`RADIO_FALLBACK_MIRRORS`, `RADIO_MIRROR_CACHE_MS`) |
| `src/main/java/ai/foresignal/source/adapters/radio/RadioBrowserParser.java` (text cleaning, the station UUID pattern, station normalization, prompt 075) | `src/sources/radioBrowser.js` (`cleanRadioText`, `RADIO_UUID_RE`, `normalizeRadioBrowserStation`) |
| `src/main/java/ai/foresignal/source/adapters/nhc/NhcConeMapper.java` (the storm id read from `idp_source`, prompt 075) | `server/providers/cyclones.js` (the `idp_source` pattern of the forecast-layer parser) |

Not ported (prompt 075): GEV's InciWeb routes (`fetchIndex`, the `POST /api/single-publication/` index, and
`fetchPublication`, which scrapes incident pages; `server/providers/firePerimeters.js` and
`src/layers/perimeters/inciweb.js`), the radio stream proxy and address pinning (`server/providers/radio/transport.js`
apart from `radioMirrorOrigin`; Foresignal never fetches a stream) and the radio catalog's tag queries and
click-count ordering.

Not ported from `server/providers/cctv/` (prompt 072): the frame proxy and stream relay (`media.js`, `stream.js`,
`range.js`), ground heights (`groundHeights.js`), the catalog cap and round-robin (`cap.js`), distance
prioritization (`prioritizeSources`), the synthetic heading and pose priors (`fallbackHeadingFromId`, pitch, field
of view, range, mount height), the NSW browser User-Agent (`NSW_IMAGE_USER_AGENT`), the TxDOT loader and the
Shinjuku pack.

`decode.js` (ecCodes WASM) is not ported: Foresignal decodes with NetCDF-Java. The particle layer
(`src/layers/wind/*`) is not part of Foresignal; the Frontend Guide tells web clients which of its
modules they may reuse under this notice.

---

## Libraries

Foresignal depends on libraries through Gradle; they are not copied into the source tree. This
section lists dependencies a prompt asked to record here.

### ZXing core

- **Artifact:** `com.google.zxing:core` 3.5.4 (Maven Central), used unmodified.
- **Use:** renders the SVG QR codes of device-pairing payloads
  (`GET /v1/pairing-codes/{id}/qr.svg`, prompt 023). The tests use its QR reader to decode them.
- **Licence:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>).
- **Project:** <https://github.com/zxing/zxing>, Copyright the ZXing authors.

### networknt json-schema-validator

- **Artifact:** `com.networknt:json-schema-validator` 3.0.8 (Maven Central), used unmodified, with its
  runtime dependencies `com.ethlo.time:itu` and `tools.jackson.dataformat:jackson-dataformat-yaml`
  (both Apache License, Version 2.0).
- **Use:** validates EDP manifests, items and provider payload schemas (JSON Schema 2020-12) at run
  time (prompt 024); earlier only the contract tests used it (prompt 020).
- **Licence:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>).
- **Project:** <https://github.com/networknt/json-schema-validator> (its LICENSE file is the plain Apache 2.0 text).

### RE2/J

- **Artifact:** `com.google.re2j:re2j` 1.8 (Maven Central), used unmodified; no runtime dependencies.
- **Use:** evaluates the `pattern` and `patternProperties` of EDP payload schemas in linear time
  through the validator's regular-expression hook (prompt 024, EDP §7).
- **Licence:** the Go license, a BSD-3-Clause license (<https://golang.org/LICENSE>).
- **Project:** <https://github.com/google/re2j>, Copyright The Go Authors.

### MCP Java SDK

- **Artifacts:** `io.modelcontextprotocol.sdk:mcp-core` and `io.modelcontextprotocol.sdk:mcp-json-jackson3`
  2.0.1 (Maven Central), used unmodified, with their runtime dependency `io.projectreactor:reactor-core`
  (3.8.7 from the Spring Boot BOM; Apache License, Version 2.0) and its `org.reactivestreams:reactive-streams`
  1.0.4 (MIT-0).
- **Use:** the MCP server at `/mcp` (prompt 025): the servlet transports (streamable HTTP and the optional
  legacy HTTP+SSE), the session and protocol classes and the JSON binding; the tests use its client.
- **Licence:** MIT License (<https://github.com/modelcontextprotocol/java-sdk/blob/main/LICENSE>).
- **Project:** <https://github.com/modelcontextprotocol/java-sdk>, Copyright (c) 2025 the original author or
  authors.

### Anthropic Java SDK

- **Artifact:** `com.anthropic:anthropic-java` 2.71.0 (Maven Central; with `anthropic-java-core` and
  `anthropic-java-client-okhttp`), used unmodified. Runtime dependencies: OkHttp 4.12.0 and Okio 3.6.0
  (Apache License, Version 2.0), the Kotlin standard library and `kotlin-reflect` (2.3.21 from the Spring Boot
  BOM; Apache License, Version 2.0), Jackson 2 with `jackson-module-kotlin` (2.21.5 from the Spring Boot BOM;
  Apache License, Version 2.0), `com.github.victools:jsonschema-generator` and its modules 4.38.0 (Apache
  License, Version 2.0), `io.swagger.core.v3:swagger-annotations` 2.2.50 (Apache License, Version 2.0),
  `com.google.errorprone:error_prone_annotations` 2.50.0 (Apache License, Version 2.0) and
  `com.standardwebhooks:standardwebhooks` 1.2.0 (MIT).
- **Use:** LLM provider type `anthropic` (prompt 026): messages (beta path, with the server-side refusal
  fallback), streaming with its message accumulator, and the Models API. The tests point it at WireMock; no
  test calls Anthropic.
- **Licence:** MIT License (<https://github.com/anthropics/anthropic-sdk-java/blob/main/LICENSE>).
- **Project:** <https://github.com/anthropics/anthropic-sdk-java>, Copyright Anthropic.

### commonmark-java

- **Artifacts:** `org.commonmark:commonmark` and `org.commonmark:commonmark-ext-gfm-tables` 0.30.1 (Maven
  Central), used unmodified; no runtime dependencies.
- **Use:** converts briefing runs' markdown to HTML for their HTML renditions (prompt 029), with raw HTML
  escaped rather than passed through and unsafe link targets dropped.
- **License:** BSD 2-Clause License (<https://github.com/commonmark/commonmark-java/blob/main/LICENSE.txt>).
- **Project:** <https://github.com/commonmark/commonmark-java>, Copyright (c) 2015, Robin Stocker.

### OWASP Java HTML Sanitizer

- **Artifact:** `com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer` 20261011.1 (Maven
  Central), used unmodified; no runtime dependencies (its SpotBugs annotations are provided-scope).
- **Use:** the allowlist applied to every briefing HTML rendition (prompt 029): headings, lists, tables,
  links with `rel="noopener nofollow"`, code and emphasis only.
- **License:** dual Apache License, Version 2.0 or BSD 2-Clause License, at the user's choice; used under
  the Apache License, Version 2.0 (<https://github.com/OWASP/java-html-sanitizer/blob/main/COPYING>).
- **Project:** <https://github.com/OWASP/java-html-sanitizer>, Copyright (c) 2011, Mike Samuel.


### H3 (Java binding)

- **Artifact:** `com.uber:h3` 4.5.0 (Maven Central), used unmodified; no runtime dependencies. The jar bundles
  the H3 C library compiled as a JNI library per platform (linux-x64, linux-arm64, darwin-x64, darwin-arm64 and
  others), extracted to `java.io.tmpdir` on first use.
- **Use:** activity rollups and stats grids (prompt 038): the H3 cell of each version's centroid, parent cells
  for coarser grids, and cell boundaries for the GeoJSON grid.
- **License:** Apache License, Version 2.0 (<https://github.com/uber/h3-java/blob/master/LICENSE>), covering
  both the Java binding and the bundled H3 core library.
- **Project:** <https://github.com/uber/h3-java>, Copyright Uber Technologies, Inc.

### Apache Commons Compress

- **Artifact:** `org.apache.commons:commons-compress` 1.27.1 (Maven Central), used unmodified.
- **Use:** bzip2 decompression of the RIPE Atlas probe dump (prompt 092).
- **License:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>).
- **Project:** <https://commons.apache.org/proper/commons-compress/>, Copyright The Apache Software Foundation.

### Apache Commons Statistics, Numbers and RNG (prompt 119)

- **Artifacts (Maven Central, used unmodified):** `org.apache.commons:commons-statistics-distribution` and
  `commons-statistics-inference` 1.3 (with `commons-statistics-descriptive` and `commons-statistics-ranking` 1.3);
  `commons-numbers-gamma` 1.3 (with `commons-numbers-core`, `-arrays`, `-combinatorics`, `-fraction` and
  `-rootfinder` 1.3); `commons-rng-simple` and `commons-rng-sampling` 1.7 (with `commons-rng-client-api` and
  `commons-rng-core` 1.7).
- **Use:** the discovery statistics package `ai.foresignal.analytics.math` (prompt 119): chi-squared, normal and
  Poisson distributions, the regularized beta for negative-binomial tails, and seeded random sources for the CUSUM
  alarm-threshold simulation and the placebo date sampler.
- **License:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>).
- **Project:** <https://commons.apache.org/proper/commons-statistics/>,
  <https://commons.apache.org/proper/commons-numbers/>, <https://commons.apache.org/proper/commons-rng/>, Copyright
  The Apache Software Foundation.

### Hipparchus (prompt 126)

- **Artifact (Maven Central, used unmodified):** `org.hipparchus:hipparchus-optim` 4.0.3 (with `hipparchus-core`
  4.0.3). The same version already reaches the runtime classpath through Orekit (see below); prompt 126 declares it
  directly because the forecasting code calls it.
- **Use:** the BOBYQA bounded optimizer behind the maximum-likelihood fits of the ETS forecasting methods
  (`ai.foresignal.outlook.series`, `series-forecast` 1.0.0).
- **License:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>), confirmed in the POM's
  `<licenses>` block and the project's `LICENSE.txt` on 2026-10-17.
- **Project:** <https://hipparchus.org>, Copyright the Hipparchus project.

### Caffeine

- **Artifact:** `com.github.ben-manes.caffeine:caffeine` 3.2.4 (Maven Central; the version the Spring Boot 4.1.1 BOM
  manages), used unmodified. Runtime dependencies: `org.jspecify:jspecify` and
  `com.google.errorprone:error_prone_annotations` (both Apache License, Version 2.0).
- **Use:** the raster frame cache with a byte weigher (prompt 042); prompt 045 adds the tile and index-map caches.
- **License:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>).
- **Project:** <https://github.com/ben-manes/caffeine>, Copyright Ben Manes.

### Jackson CBOR dataformat

- **Artifact:** `tools.jackson.dataformat:jackson-dataformat-cbor` 3.1.5 (Maven Central; the version the Jackson BOM
  imported by Spring Boot 4.1.1 manages), used unmodified.
- **Use:** reading SatDump's `product.cbor` files (prompt 049).
- **License:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>).
- **Project:** <https://github.com/FasterXML/jackson-dataformats-binary>, Copyright FasterXML, LLC.

### NetCDF-Java (GRIB2 and NetCDF4 decoders, prompt 046)

- **Artifacts:** `edu.ucar:cdm-core`, `edu.ucar:grib` and (prompt 106) `edu.ucar:cdm-radial` 5.11.0, versions from `edu.ucar:netcdf-java-bom` 5.11.0,
  with their Unidata runtime modules `edu.ucar:httpservices`, `edu.ucar:udunits` and `edu.ucar:libaec-jna`
  5.11.0. Fetched **only** from Unidata's repository <https://artifacts.unidata.ucar.edu/all/> (not on Maven
  Central; the one exception to that rule, Architecture §2), checksum-pinned in `gradle/verification-metadata.xml`.
  Used unmodified.
- **Use:** `ai.foresignal.raster.decode`: GRIB2 messages through `Grib2RecordScanner` (MRMS, and the self-test's GFS
  and ECMWF messages) and NetCDF4/HDF5 variables in enhanced mode (GOES-R ABI); `cdm-radial`'s NIDS IOSP reads NEXRAD
  Level III products 153/154 (`ai.foresignal.source.adapters.nexrad`). The `netcdf4` module (native netCDF-C) is not
  used.
- **License:** BSD 3-Clause License (<https://github.com/Unidata/netcdf-java/blob/maint-5.x/LICENSE>), Copyright (c)
  1998-2025, University Corporation for Atmospheric Research/Unidata.
- **Project:** <https://github.com/Unidata/netcdf-java>.

### libaec (native library)

- **Artifact:** `edu.ucar.unidata:libaec-native` 1.1.7.0 (Unidata repository), used unmodified. The jar bundles the
  libaec shared library per platform (linux-x86-64, linux-aarch64, darwin, win32) with its license file; JNA extracts
  it to `jna.tmpdir` (`/data/tmp` in the container) and loads it.
- **Use:** CCSDS/AEC decompression for GRIB2 data representation template 5.42 (ECMWF open data; the decoder
  self-test's fixture).
- **License:** BSD 2-Clause License, as shipped in the jar (`*/libaec-LICENSE.txt`): Copyright 2026 Mathis Rosenhauer,
  Moritz Hanke, Joerg Behrens, Luis Kornblueh. (The artifact's pom names NetCDF-Java's BSD 3-Clause license for the
  packaging.)
- **Project:** <https://gitlab.dkrz.de/k202009/libaec>.

### jj2000

- **Artifact:** `edu.ucar:jj2000` 5.4 (Unidata repository), a runtime dependency of `edu.ucar:grib`, used unmodified.
- **Use:** JPEG 2000 decoding of GRIB2 template 5.40. No Foresignal product uses template 5.40 yet; it is on the
  classpath because NetCDF-Java's GRIB module declares it.
- **License:** the JJ2000 copyright notice (<https://github.com/Unidata/jj2000/blob/master/COPYRIGHT>): permission to
  use the module in products conforming to the JPEG 2000 standard (ISO/IEC 15444), with no license granted for
  non-conforming products and a warning that use may infringe patents. Copyright (c) 1999/2000 JJ2000 Partners.

### JNA

- **Artifact:** `net.java.dev.jna:jna` 5.19.1 (Maven Central), a runtime dependency of `edu.ucar:libaec-jna`, used
  unmodified.
- **Use:** loads the libaec native library.
- **License:** dual Apache License, Version 2.0 or LGPL 2.1 or later (`SPDX-License-Identifier: Apache-2.0 OR
  LGPL-2.1-or-later`), at the user's choice; used under the Apache License, Version 2.0.
- **Project:** <https://github.com/java-native-access/jna>.

Other runtime dependencies NetCDF-Java brings from Maven Central, all used unmodified: Guava and `failureaccess`
(Apache-2.0), `jsr305` (BSD-3-Clause), JCommander (Apache-2.0), Apache Commons Math 3 (Apache-2.0; only NetCDF-Java
calls it), JDOM 2 (the JDOM license, Apache-style), Joda-Time (Apache-2.0), Protocol Buffers Java (BSD-3-Clause),
Apache HttpClient, HttpCore and HttpMime 4 with Commons Logging and Commons Codec (Apache-2.0).

### Orekit (prompts 054 and 064)

- **Artifact:** `org.orekit:orekit` 13.1.9 (Maven Central), used unmodified. Runtime dependencies: Hipparchus 4.0.3
  (`org.hipparchus:hipparchus-core`, `-geometry`, `-ode`, `-fitting`, `-optim`, `-filtering` and `-stat`, Maven
  Central, Apache License, Version 2.0, Copyright the Hipparchus project; <https://hipparchus.org>).
- **Use:** `ai.foresignal.orbit`: SGP4 propagation of a SatDump pass's TLE for its approximate swath (prompt 054);
  prompt 064 reuses it in `ai.foresignal.space.PassPredictor` (predicted passes, `ElevationDetector` and
  `ElevationExtremumDetector` on a `TopocentricFrame`) and its startup self-test. Loaded only on those code paths,
  with its own data context (below).
- **License:** Apache License, Version 2.0 (the jar's `META-INF/LICENSE.txt`). Its `META-INF/NOTICE.txt` reads
  "OREKIT, Copyright 2002-2025 CS GROUP" and credits the authors whose work Orekit includes or translates, among them
  the SGP4 work of David A. Vallado, Paul Crawford, Richard Hujsak and T.S. Kelso, and of Felix R. Hoots and Ronald L.
  Roehrich; the notice ships unchanged inside the jar.
- **Project:** <https://www.orekit.org>.

### Jayway JsonPath (prompt 059)

- **Artifact:** `com.jayway.jsonpath:json-path` 3.0.0 (Maven Central), used unmodified. Its `json-smart` runtime
  dependency is excluded; `slf4j-api` comes from the Spring Boot BOM.
- **Use:** `ai.foresignal.mapping`: the paths of a `RecordMapping`, evaluated over Jackson 3 trees with the
  library's own `Jackson3JsonNodeJsonProvider` and `Jackson3MappingProvider`. Deep scan, function calls and regex
  filters are refused when a mapping is compiled.
- **License:** Apache License, Version 2.0 (the pom's `<licenses>`).
- **Project:** <https://github.com/json-path/JsonPath>.

### HiveMQ MQTT Client (prompt 060)

- **Artifact:** `com.hivemq:hivemq-mqtt-client-shaded` 1.4.0 (Maven Central), used unmodified.
- **Use:** `ai.foresignal.mqtt`: the supervised connections of the `mqtt` channel (MQTT 3.1.1 and 5, TLS,
  WebSocket); the MQTT source adapters reuse them.
- **License:** Apache License, Version 2.0 (the pom's `<licenses>`). Project: <https://github.com/hivemq/hivemq-mqtt-client>.
- **Relocated inside the shaded jar** (under `com.hivemq.client.internal.shaded`), each checked in its own pom:
  - Netty 4.1.137.Final (`io.netty:netty-buffer`, `-codec`, `-codec-http`, `-common`, `-handler`, `-transport`):
    Apache License, Version 2.0;
  - JCTools 4.0.7 (`org.jctools:jctools-core`): Apache License, Version 2.0;
  - Dagger 2.42 (`com.google.dagger:dagger`) and `javax.inject`: Apache License, Version 2.0.
- **Runtime dependencies** (not relocated):
  - RxJava 2.2.21 (`io.reactivex.rxjava2:rxjava`): Apache License, Version 2.0;
  - Reactive Streams 1.0.4 (`org.reactivestreams:reactive-streams`): MIT-0 (MIT No Attribution);
  - JetBrains annotations 26.1.0 (`org.jetbrains:annotations`): Apache License, Version 2.0.
- **Test only:** Testcontainers' `testcontainers-toxiproxy` 2.0.5 (MIT) with `eu.rekawek.toxiproxy:toxiproxy-java`
  2.1.11 (Apache License, Version 2.0), and the `eclipse-mosquitto:2` and `ghcr.io/shopify/toxiproxy:2.12.0` images, which are not
  distributed.

### Java-WebSocket (prompt 063)

- **Artifact:** `org.java-websocket:Java-WebSocket` 1.6.0 (Maven Central), used unmodified. Its only runtime
  dependency is `slf4j-api` (version from the Spring Boot BOM).
- **Use:** `ai.foresignal.collect.DeflateWebSocket`, the `permessage-deflate` (RFC 7692) path of
  `SourceHttp.openWebSocket(…, WebSocketOptions)`, which the JDK's WebSocket client cannot negotiate; first used by
  `aisstream`. Its `WebSocketServer` is also the loopback server of the streaming tests.
- **License:** MIT License (the pom's `<licenses>`; Copyright (c) 2010-2020 Nathan Rajlich in the source headers). Project:
  <https://github.com/TooTallNate/Java-WebSocket>.

### GTFS-Realtime bindings (prompt 070)

- **Artifact:** `org.mobilitydata:gtfs-realtime-bindings` 0.2.0 (Maven Central), used unmodified: the Java classes
  generated from MobilityData's `gtfs-realtime.proto` (`com.google.transit.realtime.GtfsRealtime`).
- **Use:** parses GTFS-RT VehiclePositions and ServiceAlerts in `ai.foresignal.source.adapters.transit`.
- **License:** Apache License, Version 2.0 (the pom's `<licenses>`). Project:
  <https://github.com/MobilityData/gtfs-realtime-bindings>.

### Protocol Buffers for Java (prompt 070)

- **Artifact:** `com.google.protobuf:protobuf-java` 4.36.1 (Maven Central), used unmodified; the runtime of the
  GTFS-Realtime bindings, also required by NetCDF-Java.
- **License:** BSD 3-Clause License, Copyright 2008 Google Inc. Project: <https://github.com/protocolbuffers/protobuf>.

### jsoup (prompt 073)

- **Artifact:** `org.jsoup:jsoup` 1.23.2 (Maven Central), used unmodified. No required runtime dependencies (its optional
  `re2j` is already a dependency).
- **Use:** `ai.foresignal.source.kit.feed.FeedText`: feed-kit item titles and summaries become plain text with
  `Jsoup.clean(…, Safelist.none())`; nothing is rendered as HTML.
- **License:** MIT License (`META-INF/jsoup/LICENSE` in the jar: "Copyright (c) 2009-2026 Jonathan Hedley"). Project:
  <https://jsoup.org/>.

### Apache PDFBox (prompt 130)

- **Artifact:** `org.apache.pdfbox:pdfbox` 3.0.8 (Maven Central), used unmodified, with its runtime dependencies
  `org.apache.pdfbox:pdfbox-io` and `org.apache.pdfbox:fontbox` 3.0.8 and `commons-logging`.
- **Use:** `ai.foresignal.collab.TextExtractor`: the text of uploaded PDF attachments is extracted with
  `Loader.loadPDF` and `PDFTextStripper` for search. Nothing is rendered or executed.
- **License:** Apache License 2.0 (`META-INF/LICENSE` and `META-INF/NOTICE` in the jar; the NOTICE reads "Apache PDFBox,
  Copyright 2002-2026 The Apache Software Foundation" and credits the Adobe Glyph List, Copyright 1997, 1998, 2002,
  2007, 2010 Adobe Systems Incorporated). Project: <https://pdfbox.apache.org/>.

### Lingua (prompt 135)

- **Artifact:** `com.github.pemistahl:lingua` 1.2.2 (Maven Central; the latest release, checked 2026-10-17), used
  unmodified. Runtime dependencies: `com.squareup.moshi:moshi` and `moshi-kotlin` 1.13.0 and `it.unimi.dsi:fastutil`
  8.5.8 (all Apache License, Version 2.0), and the Kotlin standard library (already on the classpath through the
  Anthropic Java SDK; the Spring Boot BOM manages its version). The jar is about 80 MB because it carries the language
  models of every supported language.
- **Use:** `ai.foresignal.social.SocialLanguages`: the language of a social post the platform did not tag, restricted
  to the configured `social.languages`, in low-accuracy mode; nothing is sent anywhere.
- **License:** Apache License, Version 2.0 (the pom's `<licenses>`). Project: <https://github.com/pemistahl/lingua>,
  Copyright Peter M. Stahl.

### MeshCore (prompt 083)

- **Consulted:** the MIT-licensed MeshCore repository (<https://github.com/meshcore-dev/MeshCore>, local reference clone
  at commit `a366955cb2f67b8e6842d4f00d2b6a554dddd88a`): `docs/companion_protocol.md` and `docs/payloads.md`, and the
  companion firmware's command, response and push codes and frame layouts. Foresignal's Java implementation
  (`ai.foresignal.source.formats.meshcore`) was written from them; no MeshCore source file is incorporated.
- **Prompt 084** consulted the same commit for the send commands (`CMD_SEND_TXT_MSG` 0x02, `CMD_SEND_CHANNEL_TXT_MSG`
  0x03, `CMD_SEND_CHANNEL_DATA` 0x3E), `RESP_CODE_SENT`, `PUSH_CODE_SEND_CONFIRMED`, `MAX_CHANNEL_DATA_LENGTH`,
  `MAX_TEXT_LEN` and `docs/number_allocations.md` (`ai.foresignal.notify.types.MeshCoreChannel`,
  `ai.foresignal.notify.mesh.MeshCoreSender`); written from them, nothing copied.
- **License:** MIT License, Copyright (c) 2025 Scott Powell / rippleradios.com.

### meshcore_py (prompt 083, bridge dependency, not vendored)

- **Used by:** `contrib/meshcore-bridge/bridge.py`, an optional script the operator runs next to a MeshCore companion.
  It depends on the `meshcore` package from PyPI (<https://github.com/meshcore-dev/meshcore_py>, version 2.3.15
  checked 2026-10-13), which the operator installs with pip. No meshcore_py code is copied into this repository, and
  nothing in the Foresignal application uses it.
- **License:** MIT (`license = "MIT"` in its `pyproject.toml`).

### Mobility Database catalog snapshot (prompt 070)

- **Data:** `src/main/resources/transit/mobility-gtfs-rt.csv`, a filtered copy (active, keyless, https GTFS-RT feeds;
  seven columns) of MobilityData's `https://files.mobilitydatabase.org/feeds_v2.csv`, generated by
  `scripts/update-mobility-catalog.py`; the snapshot date is in `src/main/resources/transit/README.md`.
- **License:** the catalog metadata is published by MobilityData under CC0 1.0
  (<https://creativecommons.org/publicdomain/zero/1.0/>). The feeds it lists keep their own licenses, named in
  `urls.license` where known.

### GDAL (optional, not in the default image)

- **Package:** `gdal-bin` from the base image's distribution, installed **only** when the image is built with
  `--build-arg WITH_GDAL=true` (prompt 054). Run as a separate program (`gdalwarp`) through `ProcessBuilder`; no
  JNI binding, nothing linked.
- **License:** MIT/X style (<https://gdal.org/license.html>), Copyright the GDAL/OGR project contributors; the
  distribution package's own copyright file lists the bundled third-party code.

---

## Orekit data (prompt 054)

`src/main/resources/orekit-data/` holds a pinned minimal subset of the
[orekit-data](https://gitlab.orekit.org/orekit/orekit-data) repository (commit
`2422a7d06ee2cc3b5a503a47271203b0706f3e82`, 2026-10-06), loaded by `ai.foresignal.orbit.OrekitData` through a
classpath crawler:

| File | Content | Origin |
|---|---|---|
| `tai-utc.dat` | UTC−TAI leap-second history to 2017-01-01 (unchanged; SHA-256 `3524e1ae…10c3`) | US Naval Observatory; a US Government work, public domain |
| `finals2000A.all` | IERS Earth orientation parameters (IAU 2000), rows from 2024-01-01 onward of the repository's file (SHA-256 of the full file `9fbc14ae…30cd`), with predictions to late 2026 | IERS Rapid Service/Prediction Center at the US Naval Observatory; a US Government work, public domain |

Replace both from the same repository (or the USNO originals) when the predictions run out; outside the EOP range
Orekit uses zero corrections, which is far below an approximate swath's accuracy.

---

## Reference data (prompt 040)

Foresignal bundles two files generated from public datasets and downloads the rest at run time
(`src/main/resources/reference/manifest.json`, `docs/Operator-Guide.md` "Reference data and region tags").

### Natural Earth

- **Data:** 10m cultural vectors (admin-0 countries and their point-of-view variants, admin-1 states and
  provinces, admin-2 US counties, marine polygons) as GeoJSON from
  <https://github.com/nvkelso/natural-earth-vector>, release tag v5.1.2 (commit
  f1890d9f152c896d250a77557a5751a93d494776), each file pinned by SHA-256 in the manifest.
- **Bundled:** `src/main/resources/reference/countries.csv` (codes, names, label points) and
  `src/main/resources/reference/countries-simple.geojson.gz` (simplified admin-0 boundaries), generated by
  `scripts/build-countries.py`.
- **Licence:** public domain (<https://www.naturalearthdata.com/about/terms-of-use/>). No credit is required;
  Foresignal gives it anyway: "Made with Natural Earth. Free vector and raster map data @ naturalearthdata.com."
- Natural Earth draws de facto boundaries, which are not an endorsement of any claim.

### GeoNames

- **Data:** the bulk dumps `cities15000.zip` (optionally `cities1000.zip` or `cities500.zip`),
  `admin1CodesASCII.txt` and `countryInfo.txt` from <https://download.geonames.org/export/dump/>, downloaded at
  run time (never the `api.geonames.org` web services). The country names of `countryInfo.txt` also feed the
  aliases of the bundled `countries.csv`.
- **Licence:** Creative Commons Attribution 4.0 (<https://creativecommons.org/licenses/by/4.0/>), as stated in
  the dump's `readme.txt`.
- **Attribution:** "Place names from GeoNames (https://www.geonames.org/), CC BY 4.0". It is shown in
  `GET /v1/reference` and in every response field derived from a nearest place (`RegionTags.attribution`,
  GeoJSON `properties.nearAttribution`).

## Ukraine boundaries (prompt 081)

- **Data:** geoBoundaries (<https://www.geoboundaries.org>), gbHumanitarian UKR ADM1 (boundary UKR-ADM1-82583831,
  27 units) and ADM2 (boundary UKR-ADM2-22097711, 139 units), the `_simplified` GeoJSON of release commit `9469f09` of
  <https://github.com/wmgeolab/geoBoundaries>. Original source: State Scientific Production Enterprise "Kartographia",
  via the Humanitarian Data Exchange (OCHA common operational dataset), year represented 2022.
- **Bundled:** `src/main/resources/geo/ukr/adm1.geojson` and `adm2.geojson` (each polygon simplified to at most 1,500
  vertices, coordinates rounded to 4 decimals, Foresignal ids and names; 1.70 MB together), generated by
  `scripts/build-ukr-regions.py`; the crosswalks `alerts-in-ua-uids.json` and `siren-region-ids.json` beside them map
  the alerts.in.ua location UIDs and the siren.pp.ua region ids to those units. Details in `geo/ukr/NOTICE`.
- **License:** Creative Commons Attribution 3.0 IGO (<https://creativecommons.org/licenses/by/3.0/igo/>).
- **Attribution:** "Boundaries: geoBoundaries (Runfola et al. 2020), State Scientific Production Enterprise
  Kartographia via OCHA HDX, CC BY 3.0 IGO." The boundaries are administrative, as of 2022, and say nothing about
  control of territory.

## Static map basemap (prompt 055)

### Natural Earth 1:50m

- **Data:** 50m physical and cultural vectors (`ne_50m_land`, `ne_50m_lakes`, `ne_50m_coastline`,
  `ne_50m_admin_0_boundary_lines_land`, `ne_50m_admin_1_states_provinces_lines`) as GeoJSON from
  <https://github.com/nvkelso/natural-earth-vector>, release tag v5.1.2.
- **Bundled:** `src/main/resources/basemap/ne50m.bin`, generated by `scripts/build-basemap.py` (coordinates
  quantized to microdegrees and delta-encoded; no simplification). The build never downloads it again.
- **License:** public domain (<https://www.naturalearthdata.com/about/terms-of-use/>); attribution appreciated.
  Every static map credits it: "Basemap: Natural Earth".
- Natural Earth draws de facto boundaries, which are not an endorsement of any claim.

## Map sprite and KML icons (prompt 058)

The MapLibre sprite (`src/main/resources/map/sprite/foresignal{,@2x}.{json,png}`) and the 32 px KML icons
(`src/main/resources/map/icons/*.png`) are generated by `scripts/build-sprite.sh` from the two icon sets
below. Temaki names are rewritten from underscores to hyphens, and a few aliases (for example `earthquake`,
`lightning`) reuse an icon of either set under the name an adapter uses.

### Maki

- **Icons:** SVG icons from <https://github.com/mapbox/maki>, release tag v8.0.1.
- **License:** CC0 1.0 Universal (public domain dedication).

### Temaki

- **Icons:** SVG icons from <https://github.com/rapideditor/temaki>, release tag v5.13.0.
- **License:** CC0 1.0 Universal (public domain dedication).

### spreet (tool only, not shipped)

- **Tool:** <https://github.com/flother/spreet> v0.13.1, which builds the SDF sprite sheets. The script
  downloads it into a temporary directory at development time; it is not part of the application, the
  build or the image.
- **License:** MIT.

## Decoder self-test data (prompt 046)

`src/main/resources/raster/selftest/` bundles four small upstream files (provenance in its `README.md`):

- **ECMWF:** one message of the IFS open-data forecast of 2026-10-12 00z (land-sea mask). License: Creative Commons
  Attribution 4.0 (<https://creativecommons.org/licenses/by/4.0/>). Attribution: "Copyright © 2026 European Centre for
  Medium-Range Weather Forecasts (ECMWF). Source: www.ecmwf.int", unmodified. The `ecmwf-ifs` source (prompt 105)
  ingests the whole real-time IFS 0.25° catalogue under the same CC BY 4.0 licence; its derived outputs carry
  "Contains modified Copernicus/ECMWF data: ECMWF open data, CC BY 4.0. Changes made: resampled, quantised, derived
  fields."
- **NOAA:** one GFS message (NOAA/NCEP), one MRMS field (NOAA/NSSL) and one GOES-19 ABI file (NOAA), all US public
  domain, from the NOAA Open Data Dissemination buckets on AWS.

The test fixtures under `src/test/resources/fixtures/mrms/` are NOAA MRMS files (US public domain); the GOES file the
tests download is NOAA open data too.

## Color ramps of the raster styles (prompt 045)

The style files under `src/main/resources/raster/styles/` sample nine evenly spaced colors from two published
scientific colormaps. Each style's `notice` field names its origin.

### viridis

- **Used by:** no shipped style since prompt 051, which reseeded `wind-speed@1` from God's Eye View
  (`WIND_FIELDS.speed`, MIT; see the God's Eye View section above). Kept for the record of 045.
- **Source:** the `_viridis_data` table of matplotlib's `lib/matplotlib/_cm_listed.py`
  (<https://github.com/matplotlib/matplotlib>); "mpl-colormaps by Nathaniel Smith & Stefan van der Walt"
  (<https://github.com/BIDS/colormap>).
- **License:** CC0 1.0 Universal. "To the extent possible under law, the persons who associated CC0 with
  mpl-colormaps have waived all copyright and related or neighboring rights to mpl-colormaps."

### cmocean

- **Used by:** `temperature@1` (`thermal`) and `smoke@1` (`matter`).
- **Source:** `cmocean/rgb/thermal-rgb.txt` and `cmocean/rgb/matter-rgb.txt` of
  <https://github.com/matplotlib/cmocean>, fetched 2026-10-12.
- **License:** MIT License, Copyright (c) 2015 Kristen M. Thyng.

  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  documentation files (the "Software"), to deal in the Software without restriction, including without
  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
  Software, and to permit persons to whom the Software is furnished to do so, subject to the following
  conditions:

  The above copyright notice and this permission notice shall be included in all copies or substantial portions
  of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
  TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  DEALINGS IN THE SOFTWARE.

## GHSL GHS-POP (population data, operator-installed)

- **Data:** Global Human Settlement Layer, GHS-POP (population), the 30-arcsec EPSG:4326 tiled GeoTIFF.
- **Not bundled:** Foresignal ships no population data. The operator downloads the GHS-POP tiles from
  the Copernicus Emergency Management Service (<https://human-settlement.emergency.copernicus.eu/download.php>)
  and imports them with `POST /v1/packs/population/import`; the data stays on the operator's host.
- **Use:** the optional population estimate of the ExposureService (prompt 101): each pixel's population is
  aggregated to the H3 cell (res 5 and 7) containing its centre and summed inside a queried area.
- **Licence:** CC BY 4.0. Attribution "© European Union, GHSL" (European Commission, Joint Research Centre,
  Global Human Settlement Layer), as required by the licence.

## OpenStreetMap (data, operator-supplied) and osmium (optional external tool)

- **Data:** OpenStreetMap, imported only from an operator-supplied Geofabrik extract processed locally
  to a GeoJSON sequence (prompt 104). Foresignal ships no OSM data and never calls the Overpass API or
  any `openstreetmap.org` service.
- **Licence:** Open Database License (ODbL) 1.0. Attribution "© OpenStreetMap contributors". The
  derived infrastructure graph is a Derived Database; redistributing it (exports, sinks, federation)
  must keep the ODbL and share-alike terms.
- **osmium:** `osmium-tool` (GPL-3.0) is documented as an optional external command the operator may
  run to produce the extract. It is **not linked, bundled or invoked** by Foresignal; the optional
  `WITH_OSMIUM=true` image only documents the operator's own use.

## News and media-rating data providers (prompt 133)

None of this data is bundled: Foresignal fetches or imports it at run time, keeps each rating's provider, license,
attribution and date beside it, and returns the attribution with every rating it serves
(`GET /v1/media/outlets`, `/v1/media/providers`). Per-source details are in
[`docs/Source-Catalog.md`](docs/Source-Catalog.md), "News and media ratings (prompt 133, v1.3)".

### GDELT

- **Data:** news article metadata (title, link, outlet domain, language, source country, time) from the GDELT DOC 2.0
  API (`mode=ArtList`), source `gdelt-articles` (prompt 133); the same API's timelines feed `gdelt-timelines`
  (prompt 117), and the GDELT 2.0 event export feeds `gdelt-events` (prompt 066). No article body, byline or image is
  fetched.
- **License:** GDELT's open terms: unrestricted use and redistribution, with a citation of the GDELT Project and a
  link to it (<https://www.gdeltproject.org/about.html#termsofuse>).
- **Attribution:** "The GDELT Project (https://www.gdeltproject.org/)", carried by the descriptors of `gdelt-articles`
  and `gdelt-timelines` (and, without the trailing slash, `gdelt-events`).

### Wikidata

- **Data:** outlet facts (official website, country, owner, parent organization, instance of) and political
  alignment (P1387), queried from the Wikidata Query Service (<https://query.wikidata.org/sparql>) by the `wikidata`
  rating provider.
- **License:** CC0 1.0 Universal (<https://creativecommons.org/publicdomain/zero/1.0/>). No attribution is required;
  Foresignal credits it anyway: "Wikidata (CC0)".

### The Partisan Web

- **Data:** the outlet rating files `organisations/*.md` of <https://github.com/stockHuman/tpw-data>, read by the
  `partisan-web` rating provider; Foresignal stores ratings derived from their front matter (social and governmental
  bias, factual sourcing, editorial, pseudoscience, conspiracy and messaging scores, state ownership tags). The
  `ownership` field is not imported.
- **License:** Creative Commons Attribution 4.0 International (<https://creativecommons.org/licenses/by/4.0/>), the
  repository's `LICENSE` file, confirmed 2026-10-17. The README badge reads CC BY 4.0 but links to CC BY-SA 4.0; the
  `LICENSE` file is the one followed.
- **Attribution (required):** "The Partisan Web (github.com/stockHuman/tpw-data), CC BY 4.0", stored with every
  derived rating. Changes made: values rescaled to Foresignal's dimensions (lean −2..+2, factuality 1..5).

### AllSides (operator import only)

- **Data:** AllSides media bias ratings, present only if the operator imports them from their own copy (provider
  `allsides`, disabled by default). Foresignal fetches nothing from allsides.com.
- **License:** reported as CC BY-NC 4.0 (<https://creativecommons.org/licenses/by-nc/4.0/>), **not confirmed**:
  allsides.com refused automated reads (HTTP 403) on 2026-10-17. Non-commercial use only if that license holds; the
  ratings stay `LOCAL_ONLY` unless the operator records permission.
- **Attribution (required):** "AllSides Media Bias Ratings by AllSides.com".

### Media Bias/Fact Check (operator import only)

- **Data:** Media Bias/Fact Check ratings, present only if the operator imports them from their own export (provider
  `mbfc`, disabled by default). Access is the operator's own paid subscription (MBFC data API through RapidAPI,
  <https://mediabiasfactcheck.com/mbfcs-data-api>); Foresignal does not call the API.
- **License:** the Media Bias/Fact Check API terms and RapidAPI's terms, which exclude redistribution. The ratings are
  always `LOCAL_ONLY` and are never redistributed.
- **Attribution:** "Media Bias/Fact Check".

## Social platform data (prompt 135)

None of this data is bundled: the platform adapters fetch it at run time with the operator's own credentials, after
the operator enables them (only `bluesky-jetstream`, which needs no account, is on by default). Posts are kept for the
operator's own analysis with their platform, URL and handle, for the platform's retention (`social.postRetention`,
30 days by default), and deleted posts are tombstoned where deletion sync is on. The license entries of
`provenance/licences.json` keep these posts off federation, share links, sinks and exports (redistribution `FORBIDDEN`
for X, Reddit, YouTube, Threads and Instagram; `LOCAL_ONLY` for Bluesky and Mastodon). Per-source details, costs and
terms summaries are in [`docs/Source-Catalog.md`](docs/Source-Catalog.md), "Social platforms (prompts 135–137)".

| Platform | Access | Terms |
|---|---|---|
| Bluesky | Jetstream v2 (open) and AppView search | Bluesky terms of service; the authors keep their rights |
| Mastodon | the instances the operator lists | each instance's rules; the authors keep their rights |
| Reddit | Data API with the operator's OAuth app | Reddit Data API Terms (non-commercial) |
| YouTube | Data API v3 with the operator's key | YouTube API Services Terms of Service |
| X | API v2 pay-per-use with the operator's bearer token | X Developer Agreement and Policy |
| Threads, Instagram | Meta Graph APIs with the operator's tokens | Meta Platform Terms |

## webp-imageio (Java library, bundled)

- **Library:** `com.github.usefulness:webp-imageio` 0.10.0 (Apache-2.0), an ImageIO plugin that encodes and
  decodes WebP (prompt 108). It bundles `libwebp` (BSD-3-Clause, © Google) natives for linux-x86_64,
  linux-aarch64, macOS and Windows.
- **Use:** the `.webp` tile and `/image` encodings. A startup health check encodes one 256×256 tile; on
  failure WebP is disabled with a `/v1/system` reason and `.webp` answers 406 `FORMAT_UNAVAILABLE`.
- **Licences:** Apache-2.0 (the Java wrapper) and BSD-3-Clause (libwebp). `libwebp` is bundled, not
  separately installed.

## sqlite-jdbc (Java library, bundled)

- **Library:** `org.xerial:sqlite-jdbc` 3.53.4.0 (Apache-2.0), which bundles the SQLite native library
  (public domain). Used only to write MBTiles (prompt 108) and GeoPackage (prompt 111) export files; Foresignal's own stores stay
  PostgreSQL/PostGIS.
- **Licence:** Apache-2.0 (SQLite itself is public domain).

## Export libraries (Java, bundled; prompt 111)

Bulk exports, snapshots and the cold archive (prompt 111) write GeoParquet, FlatGeobuf, GeoPackage and GPX
files. The licenses below are those each artifact declares in its Maven POM (checked on Maven Central,
2026-10-16).

| Library | Version | License | Use |
|---|---|---|---|
| `org.apache.parquet:parquet-column`, `parquet-hadoop` (with `parquet-common`, `parquet-encoding`, `parquet-format-structures`, `parquet-jackson`) | 1.18.1 | Apache-2.0 | GeoParquet writer (Hadoop-free path: `LocalOutputFile`, `ParquetFileWriter`, column stores) |
| `com.github.luben:zstd-jni` | 1.5.7-15 | BSD-2-Clause (bundles the Zstandard native library, BSD) | ZSTD compression of Parquet pages |
| `org.xerial.snappy:snappy-java` | 1.1.10.8 | Apache-2.0 | Transitive dependency of `parquet-hadoop`; not used by Foresignal's code |
| `io.airlift:aircompressor` | 2.0.3 | Apache-2.0 | Transitive dependency of `parquet-hadoop`; not used by Foresignal's code |
| `commons-pool:commons-pool` | 1.6 | Apache-2.0 (inherited from `commons-parent`) | Transitive dependency of `parquet-hadoop` |
| `org.wololo:flatgeobuf` | 3.29.0 | ISC | FlatGeobuf headers, features and geometry encoding |
| `com.google.flatbuffers:flatbuffers-java` | 24.12.23 | Apache-2.0 | FlatBuffers runtime of `flatgeobuf` |

GeoPackage files are written with the already bundled `sqlite-jdbc` (above). `org.apache.hadoop:hadoop-common`
3.4.1 (Apache-2.0) is a **compile-only** dependency, needed because some `parquet-hadoop` constructors name a
Hadoop class; it is not packaged, and no Hadoop class is on the runtime classpath (`ParquetClasspathTest`).
The GPX 1.1 schema in the test resources (`src/test/resources/export/gpx-1.1.xsd`) is TopoGrafix's published
schema, used only to validate test output; it is not part of the application.
The OASIS CAP 1.2 schema in the test resources (`src/test/resources/cap/CAP-v1.2.xsd`, Copyright OASIS Open 2010,
from https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.xsd) is used only to validate the CAP messages the
tests write (prompt 112); it is not part of the application.
