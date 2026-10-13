# 049-CR-SatDump-Station-Core

| | |
|---|---|
| Prompt | 049-SatDump-Station-Core.md |
| Status | Complete |
| Implementation commit | e2a6c7c803ab64aa5f8098022e56ad4b03bc97bb |
| Date | 2026-10-12 |

## Summary

The owner's SatDump ground station is now a source. Each name in `FORESIGNAL_SATDUMP_STATIONS` becomes
one STREAMING `satdump-{station}` source with two loops sharing `adapter_state`:
- an optional `/api` loop: station state, RF sessions and the live polar plot;
- an inbox loop over SatDump's output directory: passes, composites, thumbnails and GOES/Elektro xRIT
  frames.

Passes are `satellite-pass` records joined to their RF statistics inside the one source. Foresignal talks
to SatDump only through HTTP and files, and no SatDump code is in the repository. All nine tasks are done.
`getGroundStationPolarplot` is implemented.

**1. Hooks.** Checked against the code.
- Present and reused:
  - `ParamType.LAN_URL` and `INBOX_PATH`, `InboxFiles`;
  - the 005 pinned connection (`BoundSourceHttp.PinnedTarget`: pinned IP, original Host header, no
    redirects);
  - `ctx.rasters()`, `ProductRegistry.registerDynamic`, `ctx.media()`, `ImageGuard`/`GuardedImages`,
    `ThumbnailService`, `GeosGrid`, the `rgb@1` and `ir-gray@1` styles;
  - places (036) and the MCP registry (025).
- Added, all through SPI types so the adapter keeps the Architecture §3 dependency rule:
  - **`FetchContext.chargeRequest(label)`**, through `SourceHttp.chargeRequest`. `BoundSourceHttp` takes
    one budget unit and meters a zero-byte success; there is no backoff check because nothing is sent.
  - **`FetchContext.inbox()`**: the SPI type `InboxAccess`, implemented by `InboxFiles`, which gains
    `listDirectories` and `stat` (both refuse links and re-check the realpath) and `readAll`.
  - **`MediaSink.decode(bytes, type)`**: decoding through `GuardedImages`, in both the storing and the
    probing sink.
  - **`RasterSink.registerProduct(spec)`** (to `ProductRegistry.registerDynamic`, skipped when unchanged)
    and **`RasterSink.storeProvenance(path, bytes)`** (staged write under `rasters/satdump/`, refusing
    `..`). The probing sink does neither.
  - **`FrameInput.footprint`**: an optional footprint override. The geostationary frames use it for the
    visible disc.
  - **`raster.decode.GeosGrids`**: a `geos` `GridDefinition` from scan-plane scalars and offsets, the
    visible disc, and point visibility.
  - **SPI `PlacePoints`** (implemented by `PlaceService.point`) and **SPI `LiveImageHolder`**
    (implemented by the new in-memory `media.LiveImageStore`, which guards every JPEG).
  - **`AdapterStateStore.fail(reason)` / `clearFailing()`** (key `health.failing`).
    `AdapterStateHealthRule` reports `failing` from it, ahead of a degraded reason.
  - **`SourceAdapter.checkParams(params)`**: cross-parameter rules. `ParamValidator` runs them after the
    per-parameter checks and reports them as `params.*` field errors of `400 VALIDATION_FAILED`.
- Dependency: `tools.jackson.dataformat:jackson-dataformat-cbor` 3.1.5. The version comes from the Boot
  4.1.1/Jackson BOM and resolves from Maven Central; `THIRD_PARTY_NOTICES.md` has the entry.

**2. Verified SatDump facts.** Read in the local checkout, for format facts only. The checkout's HEAD is
`57b1cd666f99`; `1aedd6020`, the "Merge pull request #1226" import named in the prompt, is in its history.

Confirmed:
- the `/api`, `/polarplot.jpeg`, `/fft.jpeg` and `/schedule.jpeg` handlers;
- the autotrack wrapper and the `object_tracker` keys;
- `dataset.json` (`satellite`, `timestamp`, `products`);
- the `product.cbor` keys;
- the xRIT layout and the geostationary `projection_cfg`: a projected coordinate is
  `pixel × scalar + offset` in meters of the PROJ `geos` plane.

One fact differs from the prompt; see Deviation 1.

**3. Source and descriptor** (`source.adapters.satdump`). `SatDumpFamily` reads
`foresignal.satdump.stations` (`application.yml`, `${FORESIGNAL_SATDUMP_STATIONS:}`). A name must be
kebab-case and at most 56 characters, otherwise startup fails.

Descriptor:
- STREAMING; kinds `[ground-station, satellite-pass, raster-frame]` with 30 d / 365 d / 7 d retention;
- STANDARD, ALL, UPSTREAM, no record TTL;
- licence `operator-owned`; budget 60/min and 50,000/day;
- titleField `title`, `zeroResultsNormal`, `expiresAt` volatile.

Params:
- `apiUrl` (`LAN_URL`) and `inboxPath` (`INBOX_PATH`), at least one of the two;
- `location` (`lat,lon[,altM]` or a place id, required);
- `satNames` (a JSON object);
- `apiInterval` (5 s, 2–30 s) and `scanInterval` (30 s, 5 s–10 min).

**4. `/api` loop** (`SatDumpStation`, `SatDumpApiParser`, `GroundStationRecords`, `RfSession`).
- **Parsing:** lenient (strings and arrays: best for SNR, mean for BER and RS). The skeleton is checked
  against the pinned shape; a mismatch shows "SatDump /api shape changed (version drift?)".
- **`ground-station` record:**
  - state is `locked`, then `receiving`, then `tracking`, then `idle`, in that order; `unknown` without
    `apiUrl`;
  - az/el are quantized to 1° and SNR to 1 dB, and a record is emitted only when the payload changed;
  - it carries the station point.
- **RF session:**
  - opens at tracking start (autotrack) or the first pipeline activity (live);
  - samples about every 2 s into a ring of at most 2,000, flushed to `rf.active` every 30 s and at LOS;
  - LOS is when the tracker loses the target or it changes (autotrack), or after 2 idle polls (live);
  - at LOS: the `rf{}` aggregates, a 60-point SNR-against-elevation series, and the last 50 summaries in
    `rf.sessions`.
- **Polar plot:** every 15 s during an autotrack pass, guarded, held in memory, cleared at LOS.
  `/fft.jpeg` and `/schedule.jpeg` are never requested.
- **Health:** `failing` after 3 consecutive unreachable polls ("SatDump /api unreachable for 3
  consecutive polls: …"), cleared by a good poll.

**5. Inbox loop** (`InboxScanner`, `PolarPassProcessor`, `XritFrames`).
- **Scanning:** a full walk every `scanInterval`, costing one `chargeRequest`; a spent budget skips the
  scan. Every listing and stat goes through `InboxAccess`. The walk is bounded at 8 levels and 50,000
  entries.
- **Polar pass folder:** ready after 2 unchanged scans, and processed once per signature.
  - It parses `dataset.json` and each `product.cbor`.
  - A composite is an image not listed as a channel. Each goes through `GuardedImages`, then
    `ctx.media()`, which stores the 256 and 1024 px thumbnails.
  - The original images and the provenance files go to `rasters/satdump/{station}/{passId}/`.
  - Only the `json`, `cbor`, `png`, `jpg`, `tif` extensions are ever copied, so `.cadu` and basebands are
    not. A BigTIFF is skipped with "BigTIFF not supported.".
- **xRIT:** see 7. A `product.cbor` that does not parse is retried on the next scan.
- **Health reasons**, exact text: the missing `dataset.json` reason, "FY-4/MSG HRV has no usable
  projection.", and "BigTIFF not supported.".

**6. Passes: lifecycle and join** (`PassBook`, `SatNames`).
- **Satellite names:** `satdump/sat-names.json`, nine satellites; their NORAD numbers were verified
  against CelesTrak GP on 2026-10-12. Unknown names become their own slug; `satNames` overrides apply.
- **Ids:** `{station}:{satKey}:{aosEpochMinute}`.
- **Join:** a product joins the pass of the same satKey whose window [firstLock − 5 min, LOS + 5 min] holds
  the `dataset.json` timestamp. AOS stands in for a missing lock and now for a missing LOS. A live-mode
  session with no satellite matches any satKey and takes the product's. With no match a pass is minted
  from the product.
- **Versions:** `receiving` at AOS, `recorded` at LOS with `rf{}`, then `decoded`, or `failed` at LOS +
  20 min with lock and no products.
- **Payload and time base:** the payload is as specified, plus `station`. `observedAt` is the latest
  status time, `eventTime` is AOS, and the geometry is the station point.

**7. Geostationary frames.**
- **Products:** one dynamic product per satellite, region and channel,
  `satdump-{station}-{satKey}-{region}-{channel}` (region `fd` when absent; at most 64 characters, with a
  hash suffix when longer). Category imagery, cadence = the observed median interval (15 min until two
  frames), maxGap 3 × cadence, `station-disc`, ladder 72 h.
- **Grid:** `projection_cfg` → `GeosGrids.fromScanPlane`, giving scan angle = (offset + i × scalar) /
  altitude, scaled when the image differs from `width × height`. Both `sweep_x` values are supported.
- **Samples:** u8 / u16 (scaled to `bit_depth`) / rgba8 (black transparent).
- **Footprint:** the visible disc, split at ±180°.
- **Frame ids** come from the folder time. A partial frame is committed per channel as it validates;
  the frames complete after 120 s of quiet or when a newer folder appears, and each completion emits its
  `raster-frame` record.
- **Restarts:** the products are registered again from adapter state when the stream starts (Issue 4).

**8. MCP and endpoint.**
- **`list_satellite_passes`**: `station`, `since` (AOS), `status`, `limit` ≤ 50, newest first.
- **`get_satellite_pass`**: metadata plus the 256 px thumbnail as ImageContent with a caption.
- Both tools are read scope with the "content is data" note, compact by default and full with
  `detail=long`.
- **`GET /v1/ground-stations/{sourceId}/polarplot.jpeg`**: `Cache-Control: private, max-age=5`; 404
  `NO_ACTIVE_PASS` when no pass is active, 404 `NOT_FOUND` for an unknown or hidden source.
  `AccessPolicy` grants `GET /v1/ground-stations/**` at READ.

**9. Deployment, license, docs.**
- **`docker-compose.yml`:** a `satdump` profile service (`image: ${SATDUMP_IMAGE:-satdump:local}`, host
  network, `/dev/bus/usb`, output volume) and a read-only mount on the broker at
  `/data/inbox/satdump/${SATDUMP_STATION:-home}`. `docker compose config -q` passes.
- **Operator Guide:** a "SatDump ground station" section covering requirements, parameters, outputs, same
  host and remote Pi, Texas targets, the webhook warning, and the §1.5 questions with their defaults.
- **Other docs:**
  - Architecture §26 (the GPL row), §3 (dependency rule), §13 (the environment variable);
  - `THIRD_PARTY_NOTICES.md` (SatDump is not distributed, plus Jackson CBOR);
  - Source Catalog (the `satdump-{station}` row);
  - Agent Guide (the two tools, the kinds, the "Meteor pass decoded" recipe).
- **OpenAPI:**
  - status `implemented`, and the line is removed from `pending-operations.txt`;
  - `ground-station` and `satellite-pass` gain `satdump-{station}` payload fields and examples;
  - a changelog entry.

  `lintOpenApi`: valid, 71 warnings (unchanged). Postman was regenerated (unchanged: 829 requests, all
  operations covered).

**Verified outside the tests** with scratch programs against the compiled classes (JDK 21; no test was
run):

| Check | Result |
|---|---|
| `/api` live fixture | modules `meteor_lrpt_decoder`, `psk_demod`; SNR 11.42, peak 14.8, BER 0.0841, SYNCED, locked |
| `/api` autotrack fixture | `METEOR-M2 3`, 137.9 MHz, az 212.37, el 38.62, SNR "9.6" → 9.6, peak [12.1, 13.4] → 13.4, BER mean 0.1; payload az 212, el 39, snrDb 10 |
| Drift | `{"live_pipeline":{}}`, `[1]` and a tracker without keys → the drift reason |
| Names | `METEOR-M2 3`, `Meteor-M2-3`, `57166` → `meteor-m2-3`; `GOES 19` → `goes-19` |
| GOES-19 HRIT grid (64 × 64 of a 2712 disc) | box −154.33…3.93, ±75.09; disc −156.50…6.10, ±81.33; Galveston inside, Cape Town outside |
| GOES-18 grid and disc | box 143.87…−57.87 (crosses ±180°), disc in 2 parts, Honolulu inside |
| Inbox simulation | xRIT partial commits at scan 0, complete with 2 records at +120 s; pass `decoded` at scan 2 with 1 composite, 5 provenance files, no `.cadu`; symlink skipped; a restart reprocesses nothing |
| `/api` simulation | idle → locked → `receiving`; LOS → `recorded` with `rf{}`; the product joined the same pass id; 4 polar plots in 55 s; `/api` and `/polarplot.jpeg` only; failing on the 3rd failed poll |
| `DescriptorValidator` | no problems, no warnings |

## Files created / modified

Production, created:
- `source/adapters/satdump/`: `package-info`, `SatDumpFamily`, `SatDumpAdapter`, `SatDumpDescriptor`,
  `SatDumpStation`, `StationStore`, `StationLocation`, `SatNames`, `ApiSnapshot`, `SatDumpApiParser`,
  `GroundStationRecords`, `RfSession`, `PassState`, `PassBook`, `SatDumpFiles`, `InboxScanner`,
  `PolarPassProcessor`, `XritFrames`, `XritProducts`, `XritGeometry`;
- `source/spi/`: `InboxAccess`, `PlacePoints`, `LiveImageHolder`;
- `raster/decode/GeosGrids`;
- `media/LiveImageStore`;
- `api/GroundStationsController`;
- `mcp/tools/`: `ListSatellitePassesTool`, `GetSatellitePassTool`, `SatellitePasses`;
- `resources/satdump/sat-names.json`.

Production, modified:
- `source/spi/`: `FetchContext`, `SourceHttp`, `RasterSink`, `MediaSink`, `FrameInput`,
  `AdapterStateStore`, `SourceAdapter`;
- `collect/`: `BoundSourceHttp`, `DefaultFetchContext`, `FetchContextFactory`, `AdapterStateHealthRule`;
- `source/`: `InboxFiles`, `ParamValidator`;
- `raster/RasterPipeline`, `media/MediaSinks`, `place/PlaceService`, `security/AccessPolicy`,
  `api/error/ErrorCodes`;
- `application.yml`, `build.gradle.kts`, `gradle/libs.versions.toml`, `docker-compose.yml`.

Docs and spec: `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Architecture.md`,
`docs/Source-Catalog.md`, `docs/Agent-Guide.md`, `THIRD_PARTY_NOTICES.md`,
`src/test/resources/openapi/pending-operations.txt`.

Tests:
- Created in `source/adapters/satdump/`: the tests listed below, plus the helpers `SatDumpFixtures`,
  `SatDumpTestContext` and `DelegatingContext`.
- Created elsewhere: `mcp/tools/SatDumpMcpToolsTest`, `api/PolarplotEndpointIT`.
- Modified:
  - `ParamValidatorTest` (cross-check), `InboxFilesTest` (`listDirectories`, `stat`, `readAll`),
    `AdapterStateHealthRuleIT` (failing);
  - `RasterTestSources`, which gains the `satdump-itest` station bean (no new Spring context).
- Fixtures: `fixtures/satdump/2.0.0-alpha/` (`/api` live, autotrack and idle; a Meteor LRPT pass folder;
  a GOES-19 HRIT folder with two channels; a truncated `product.cbor`; an FY-4A folder), plus
  `coastline-points.csv`, `generate-synthetic.py`, and a README with the capture procedure. All are
  flagged synthetic.

## Deviations from the prompt

1. **Autotrack takes `http_server` from its config file, not the command line.** The prompt documents
   `satdump legacy autotrack … --http_server 0.0.0.0:8081`. The checkout reads
   `d_settings["http_server"]`, `finish_processing` and `output_folder` from the JSON given as
   `satdump legacy autotrack autotrack.json` (`autotrack_main.cpp`, `autotrack_web.cpp`); only
   `legacy live` parses `--http_server`.
   - The compose service runs `satdump legacy autotrack /config/autotrack.json`.
   - The Operator Guide documents both forms.
2. **The test names use US spelling:** `SatNameNormalizerTest`, not the prompt's `SatNameNormaliserTest`.
   No invariant names it.
3. **MCP `detail=full` is `detail=long`**, the existing 025 argument (`short` ≤ 4 KB, `long` ≤ 50 KB).
4. **SPI additions** instead of adapter imports of `source.InboxFiles`, `media.GuardedImages`,
   `raster.ProductRegistry`, the place service or the controller's store (Architecture §3); listed under
   1. The "at least one of `apiUrl`/`inboxPath`" rule needed a cross-parameter hook (`checkParams`); it
   still answers `400 VALIDATION_FAILED`, with a `params.apiUrl` field error.
5. **Gray xRIT channels use `ir-gray@1` as a display ramp.** Physical value = 330 − 150 × code / max, so
   bright is white; it is not a temperature, and the legend summary says so. Code 0 (space) is nodata.
   There is no plain gray style yet (Issue 5).
6. **The geostationary bounding box** is computed from a lattice over the whole grid
   (`GeosGrids.box`), not from `NetcdfDecoder.outline`. For a full disk the image's four edges lie beyond
   the limb, so `outline` gave a box far too small (−107…4°, −75…10° for GOES-19). A disc crossing ±180°
   gets `west > east`, which `GridDefinition` and the encoder already accept.
7. **A frame footprint can be supplied** (`FrameInput.footprint`): the computed one is the grid box
   clipped to covered rows, not the visible disc the prompt asks for.
8. **`pipelineId` and `satdumpVersion` are left out of pass payloads.** `/api` names modules, not the
   pipeline, and neither `dataset.json` nor `product.cbor` carries the SatDump version; null values are
   dropped (adapter rule 8). Products carry `instrument`, `composite`, `mediaId` and `thumbMediaId`;
   `productId`, `frameKey` and `tilesTemplate` belong to 054's polar overlays.
9. **`satellite-pass` status `failed` replaces the spec note's `missed`**, matching
   Weather-Imagery-Design §4.4 and the prompt; recorded in the spec changelog.
10. **Restoring passes after a restart** is from adapter state. An open RF session restored from
    `rf.active` ends at its last activity, plus at most one poll period, if the first poll finds no
    activity.
11. **Live mode (no tracker):** the session has no satKey (`home:unknown:{minute}`) and joins the first
    product inside its window, adopting the product's satKey; its id keeps `unknown`.
12. **`location` is required.** The prompt lists it without "optional"; every record carries the station
    point.
13. **Test drive:** the ITs drive the station one step at a time (`pollApiOnce`, `scanInboxOnce`,
    `observe`) through the real stream context and a supervisor-style `PolicyAwareRecordSink`, rather than
    through the supervisor's sleeping loops.
14. **Provenance copies are not yet expired.** The prompt names no retention for them;
    Weather-Imagery-Design §2.4 gives "SatDump polar 30 d" (054).

## Issues found / open questions

**Needs the owner:**

1. **Weather-Imagery-Design §1.5 is still unanswered:** SatDump version and mode, host or Pi, antennas,
   memory, remote clients, places. The defaults in the Operator Guide are used, and the fixtures are
   hand-written from the 2.0.0-alpha source until a capture replaces them (README procedure). **Owner:**
   which version and mode does the station run?
2. **Deviation 1 (the autotrack config) contradicts the prompt and Weather-Imagery-Design §4.0/§4.7.**
   **Owner:** should the design text change to the config-file form?
3. **The design's `products[].productId/frameKey/tilesTemplate` for polar passes have no source until
   054.** Confirm 054 fills them.

**Other findings:**
4. **Dynamic products are not reloaded by `ProductRegistry` at startup.** The adapter registers its
   products again when its stream starts. While a SatDump source is disabled, its frames are not thinned
   by `RasterRetentionStep` (it walks only registered products) until the stream runs again. A generic
   reload of `raster_product` rows with `dynamic = true` would fix this; it needs a `ProductSpecJson`
   reader.
5. **A plain gray style** (for example `gray@1`, counts 0 → black, max → white, no units) would be cleaner
   than Deviation 5. The legend currently shows `ir-gray`'s "K" labels on SatDump gray channels.
6. **Provenance retention:** see Deviation 14.
7. **No instructions directed at an agent** appeared in the SatDump source, documentation or anything
   read (the source was read for format facts only).

## Tests written (not run)

| Test | What it covers |
|---|---|
| `satdump/SatDumpApiParserTest` | live and autotrack fixtures; string and array leniency; states idle, tracking, receiving, locked, unknown; quantization (a nudge keeps the payload, a 1.6° move changes it); drift (non-JSON, array, tracker without keys, pipeline without tracker, non-object field; `{}` is not drift); number reduction |
| `satdump/RfSessionTest` | 2-s sampling; 30-s flush cadence; LOS aggregates (SNR max/mean, BER min/mean, synced fraction, lock times and elevations); no-lock and empty cases; 60-point series in time order; ring capacity |
| `satdump/SatNameNormalizerTest` | 23 table cases (names, aliases, NORAD numbers, unknowns as slugs); blanks; display names and NORAD; `satNames` overrides; the parameter's JSON rules |
| `satdump/PassJoinTest` | ids; `receiving → recorded → decoded` with time bases and the title; window edges ±5 min (inside and one second outside); other satKeys; a live session joining any; minting from a product and a second dataset joining it; `failed` exactly at LOS + 20 min with the health reason, then a late dataset clearing it; no lock stays recorded; pruning to 50 and 24 h |
| `satdump/InboxScannerTest` | ready after 2 stable scans and never again; a growing file resets; linked folder and linked `dataset.json` never read (`NOFOLLOW_LINKS`); realpath re-check refusing an outside path and a station dir swapped for a link; copies without `.cadu`, media owned by the pass, one budget unit per scan; spent budget skips; missing `dataset.json` reason after 20 min |
| `satdump/XritFrameTest` | partial then complete after 120 s with one record per channel; a newer folder completes the older at once and sets the cadence to 10 min; a truncated `product.cbor` retried; FY-4 and MSG refusals with the reason; grids for `sweep_x` true and false; the visible disc against 12 coastline points (GOES-19, GOES-18 in two parts, Elektro-L 4); product ids, specs, dtypes, bands; folder paths |
| `satdump/SatDumpDescriptorTest` | descriptor facts; the cross-parameter checks; locations; the family's names and failures |
| `satdump/SatDumpAdapterWireMockTest` | `/api` through the real `UpstreamClient` at a pinned 127.0.0.1 for an unresolvable host, with the original Host header; `/polarplot.jpeg` 3 times in a 35-s autotrack pass, never in live mode or idle; `verify(0, …)` for `/fft.jpeg` and `/schedule.jpeg`; failing after 3 unreachable polls, cleared by a good one; drift degrade and clear; the real budget charged once per scan |
| `satdump/SatDumpIngestIT` | `receiving → recorded → decoded` as one record through the real writer, with `rf.syncedFraction`, the composite and two thumbnails as media; the "Meteor pass decoded" rule (`kind=satellite-pass`, `status == decoded`, `enter`) fires once; HRIT frames as dynamic products, partial then complete, with one `raster-frame` version each; `unknown` ground-station without `apiUrl` |
| `api/PolarplotEndpointIT` | 200 with `private, max-age=5` while held, `NO_ACTIVE_PASS` otherwise; `NOT_FOUND` for unknown and non-SatDump sources; read scope with `openReads` off; the guard drops a stale plot |
| `mcp/tools/SatDumpMcpToolsTest` | read scope, schemas and the untrusted note; all visible stations newest first; status and limit; compact size; one station and an unknown one; the thumbnail as ImageContent with a caption; both id forms; metadata only without or with an expired thumbnail; an unknown pass |
| `ParamValidatorTest` (added) | an adapter's `checkParams` becomes a `params.a` field error |
| `InboxFilesTest` (added) | `listDirectories` skips links; `stat` refuses links and directories; `readAll` cap |
| `AdapterStateHealthRuleIT` (added) | a failing reason wins over a degraded one and clears |

## Compile result

- `./gradlew compileJava compileTestJava`: `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 829 requests, all operations covered
  (collection unchanged).
- New dependency: `tools.jackson.dataformat:jackson-dataformat-cbor` (3.1.5 via the Boot BOM, Apache-2.0).

## Notes for the next prompt

1. **050 (CORS and tile keys):** nothing new here. `/v1/ground-stations/**` is READ; decide whether the
   tile-key prefixes include it.
2. **054 (SatDump polar overlays):**
   - pass folders are processed in `PolarPassProcessor`; the `.tif` copies sit in
     `rasters/satdump/{station}/{passDir}/` (`passDir` = pass id with `:` → `_`);
   - add `productId`, `frameKey` and `tilesTemplate` to `products[]` through `PassBook.decoded`;
   - the TLE is in the pass payload (`tle`);
   - provenance retention (30 d) is still to do (Issue 6).
3. **064 (predicted passes):** `satellite-pass` ids are `{station}:{satKey}:{aosEpochMinute}`;
   `SatNames` gives satKey and NORAD; a predicted version must use the same id to merge.
4. **107 (DCS/EMWIN, the 2.0 hook):** add kinds to `SatDumpDescriptor.build`; the inbox walk is in
   `InboxScanner`.
5. **Any adapter reading files:** use `ctx.inbox()` (`InboxAccess`), never `Files.*` on inbox paths.
   Use `ctx.chargeRequest(label)` for budgeted non-HTTP work, and `AdapterStateStore.fail` for a fatal
   reason while the stream keeps running.
6. **057 checkpoint:**
   - run the 049 tests;
   - `RasterTestSources` now registers `satdump-itest` (disabled) in its contexts, with no new context;
   - `SatDumpIngestIT` and `PolarplotEndpointIT` use that context, and `SatDumpAdapterWireMockTest` the
     plain `@IntegrationTest` one;
   - `SatDumpIngestIT` writes under `${java.io.tmpdir}/foresignal-test-data/inbox/satdump/itest-*` and
     removes its two dynamic products at the end;
   - the coastline expectations (GOES-19 Anchorage at 82.7°, GOES-18 Halifax at 78.3°) are near the limb.
     If the ellipsoid moves one across, check the point rather than the disc.
7. **143:** measure inbox scan time on a real SatDump output tree; the walk is capped at 50,000 entries.
8. **144 (Flyway):** no new tables or columns. `adapter_state` gains the keys `health.failing`,
   `rf.active`, `rf.sessions`, `passes`, `inbox.watches`, `inbox.processed`, `xrit.folders` and
   `xrit.products`.
9. **Owner decisions pending:** Issues 1–3 above, plus those carried from 012–048.
