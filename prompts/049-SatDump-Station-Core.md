# 049 — SatDump Station Core

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/049-CR-SatDump-Station-Core.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/048-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/design-history/Weather-Imagery-Design.md` §0.1 (SatDump rows), §1.5 (owner questions), §2.2 (`LAN_URL`, `INBOX_PATH`), §2.4 (storage, ladders, retention), §2.5 (budget), §3 (CBOR, image guard, licence boundaries), **§4 in full** (station requirements, one source per station, `/api` loop, inbox loop, pass lifecycle and join, georeferencing item 1, thumbnails, deployment), §6.6 (polarplot endpoint), §6.9 (MCP tools), §7.4 ("Meteor pass decoded" recipe). Design prompt 036 = this prompt; inside §4 its "043"/"041" polar overlays are v1.2 **054**, "044 celestrak-gp" predicted passes are **064**, and "074"/"075" DCS/EMWIN and the 2.0 hook are **107** (Roadmap §5).
- `docs/design-history/Core-Data-Model.md` §2, §3, §5.3
- Completion Reports 004, 005, 025, 036, 042, 043, 045
- SatDump source (GPL-3.0) at `~/Documents/GitLab/SatDump`, **read-only and reference only**: never copy code, headers, structs or projection code into Foresignal. Parsers are written from observed output and fixtures.

## Objective

Make the owner's SatDump ground station a first-class source: one `satdump-{station}` source with an optional `/api` loop (station state, RF sessions, live polar plot) and an inbox loop over SatDump's output directory (passes, products, thumbnails, GOES/Elektro xRIT geostationary frames). Passes become `satellite-pass` records joined to their RF statistics. Foresignal talks to SatDump only through HTTP and files.

## Tasks

### 1. Hooks (check, add if missing)

- **004:** `ParamType.LAN_URL` (resolve once; loopback, RFC 1918, link-local or ULA only; `.local` must resolve to one of those; pinned IP stored) and `INBOX_PATH` (realpath under `FORESIGNAL_INBOX_DIR`), plus `InboxFiles` (every file opened with `NOFOLLOW_LINKS`, realpath re-checked per file and per scan).
- **005:** `LAN_URL` requests connect to the pinned IP with the original Host header and redirects disabled.
- **042 / 043 / 045:** `ctx.rasters()` with dynamic products (`ProductRegistry.registerDynamic`), `ctx.media()`, `ImageGuard`/`GuardedImages`, `ThumbnailService`, `GeosGrid`, style `rgb@1` and `ir-gray@1`.
- **036:** places (station location may reference a place). **025:** the MCP tool registry with scopes and compact output.
- `FetchContext`/`StreamContext.chargeRequest(String label)`: consumes one budget unit without HTTP (used per inbox scan). Add it if missing.

### 2. Verified SatDump facts (local checkout, 2.0.0-alpha, import of commit `1aedd602`, 2026-10)

- `/api` exists only in the legacy CLI web server (`src-cli/legacy/webserver.cpp`), started by `--http_server`. `legacy live` serves the module-stats map **at top level** (`{moduleId: {...}}`); `legacy autotrack` wraps it: `{live_pipeline: {moduleId: {...}}, object_tracker: {...}, frequency, vfos?, recording?}`.
- Module stats keys seen across decoders: `snr`, `peak_snr`, `viterbi_ber`, `viterbi_lock`, `deframer_state`, `lock_state`, `rs_avg` (types differ per decoder; some are arrays).
- `object_tracker`: `object_name`, `sat_current_pos{az, el}`, `sat_current_range`, `next_aos_time`, `next_los_time` (epoch seconds), `next_event_is_aos`, `next_event_in`, `rotator_engaged`, `rotator_tracking`, `rot_current_pos`, `rot_current_req_pos`.
- `/polarplot.jpeg` is registered only in autotrack mode (alongside `/fft.jpeg` and `/schedule.jpeg`). **Never request `/fft.jpeg`** (it turns the FFT on) or `/schedule.jpeg`.
- `finish_processing` runs the offline pipeline only when true **and** no `server_address`/`server_port` is set (server mode).
- `dataset.json`: `satellite` (string), `timestamp` (epoch seconds, double), `products` (list of relative product directories).
- `product.cbor`: `instrument`, `type`, optional `product_timestamp`, `product_source`, `images[]` (`file`, `name`, `abs_index`, `bit_depth`, `transform`, …), `projection_cfg` (with `tle` and `timestamps` for polar).
- xRIT layout: `{outdir}/IMAGES/{satellite}/[{region}/]{YYYY-MM-DD_HH-MM-SS}/product.cbor` plus channel images; the region level is optional. Geostationary `projection_cfg`: `type: geos`, `lon0`, `sweep_x`, `scalar_x`, `scalar_y`, `offset_x`, `offset_y`, `width`, `height`, `altitude`.
- 1.2.2 differs in `product.cbor` image keys; keep parsing lenient and keep one fixture set per pinned version.

### 3. Source and descriptor (`ai.foresignal.source.adapters.satdump`)

- `SatDumpFamily` registers one `satdump-{station}` source per name in `foresignal.satdump.stations` (env `FORESIGNAL_SATDUMP_STATIONS`, comma list, default empty).
- Mode STREAMING (two long-running loops under the streaming supervisor). Kinds `[ground-station, satellite-pass, raster-frame]`; `kindRetention{ground-station: 30d, satellite-pass: 365d, raster-frame: 7d}`; STANDARD; policy ALL; `observedAtMode` UPSTREAM (ground-station records are stamped with the time the `/api` response was read; passes and frames carry SatDump timestamps); `recordTtl` null; `licence` "operator-owned"; budget 60/min, 50,000/day (LAN only); `titleField` `title`; `zeroResultsNormal=true`.
- Params: `apiUrl` (`LAN_URL`, optional), `inboxPath` (`INBOX_PATH`, optional; **at least one required**, else `VALIDATION_FAILED`), `location` (`lat,lon[,altM]` or a place id), `satNames` (optional JSON map overriding the normalisation table), `apiInterval` (duration, default 5 s, 2–30 s), `scanInterval` (default 30 s).
- Health: failing when `apiUrl` is set and unreachable for 3 consecutive polls; with no `apiUrl` the source stays healthy on the inbox loop alone. The supervisor's "connected" flag is the inbox loop's liveness.
- Adapter state (`adapter_state`) shared by both loops: recent RF sessions (last 50), processed paths with size/mtime, pending passes, xRIT folders in progress.

### 4. `/api` loop

- Poll `{apiUrl}/api` every `apiInterval`; parse leniently (unknown keys ignored; numbers may be strings or arrays). A schema mismatch against the pinned-version fixture → health reason "SatDump /api shape changed (version drift?)".
- **`ground-station` record** (`externalId` = station): payload `state` (idle | tracking | receiving | locked | unknown), `currentTarget`, `frequencyHz`, `az`, `el`, `nextAos`, `nextLos`, `rotatorEngaged`, `satdumpMode` (live | autotrack), `title`. State: `tracking` when the tracker has a target above the horizon; `receiving` when a pipeline is running; `locked` when any module reports a locked deframer or Viterbi. Quantise az/el to 1° and SNR to 1 dB, so a version is written only on a state change or a quantised change. Station point geometry. With no `apiUrl`: one record with `state: unknown`.
- **RF session:** from tracking start (or first pipeline activity) to LOS, sample `snr`, `peak_snr`, `viterbi_ber`, `deframer_state`, `rs_avg`, az/el about every 2 s into an in-memory ring; flush to adapter state **every 30 s and at LOS**. At LOS compute `snrMaxDb`, `snrMeanDb`, `berMin`, `berMean`, `syncedFraction`, `firstLockAt`, `lastLockAt`, `firstLockElevationDeg`, `lastLockElevationDeg` and an SNR-vs-elevation series of about 60 points.
- **Polar plot:** during an active pass in autotrack mode, fetch `/polarplot.jpeg` every 15 s (counts toward the budget), pass it through `ImageGuard`, and hold only the latest image **in memory** (never media). Served by `GET /v1/ground-stations/{sourceId}/polarplot.jpeg` (read; 404 `NO_ACTIVE_PASS` when none; `Cache-Control: private, max-age=5`; never fetched on client demand).

### 5. Inbox loop

- Full scan every `scanInterval` (one `chargeRequest`); `WatchService` events are only a hint (inotify is unreliable on NFS, SMB and bind mounts). All reads go through `InboxFiles`.
- **Polar pass folder** (contains `dataset.json`): ready when `dataset.json` exists and every file size and mtime have been stable for 2 scans. Parse `dataset.json` and each product's `product.cbor` (Jackson CBOR, `tools.jackson.dataformat:jackson-dataformat-cbor` from the Boot BOM). Every composite PNG goes through `GuardedImages`, then `ctx.media()` and `ThumbnailService` (256 and 1024 px). Copy the original images and provenance files (`dataset.json`, `product.cbor`, any `.tif`) to `rasters/satdump/{station}/{passId}/`; **never copy `.cadu` or baseband files**. Polar GeoTIFF overlays and footprints are 054.
- **xRIT timestamp folder:** produce a **`partial`** frame as soon as one channel validates; mark it `complete` after 120 s without changes or when a newer timestamp folder appears for the same satellite/region. `product.cbor` is rewritten as channels arrive: read, validate, retry next scan on a parse error.
- Health reasons (exact text): "Pass ended {t} but no dataset.json after 20 min. Is `finish_processing: true` set, and server_mode off?"; "FY-4/MSG HRV has no usable projection."; "BigTIFF not supported."

### 6. Passes: lifecycle and join

- Satellite key: normalised name (`METEOR-M2 3`, `Meteor-M2-3` and NORAD 57166 all → `meteor-m2-3`) from `src/main/resources/satdump/sat-names.json` (fixture-tested), overridable by `satNames`.
- `externalId` = `{station}:{satKey}:{aosEpochMinute}`, minted from the RF session (tracking start) when one exists, else from `dataset.json` `timestamp`.
- Join: a decoded product attaches to the RF session with the same satKey whose window [firstLock − 5 min, LOS + 5 min] contains the product timestamp; otherwise a new pass is minted from the product.
- Versions (`status`): `receiving` at AOS, `recorded` at LOS (with `rf{}`), then `decoded` or `failed` (`failed` = no products by LOS + 20 min while the RF session had lock). `predicted` is added by 064.
- Payload: `satellite`, `satKey`, `norad?`, `pipelineId`, `frequencyHz`, `aos`, `los`, `maxElevationDeg`, `rf{}`, `instruments[]`, `products[{productId, frameKey, composite, thumbMediaId, tilesTemplate}]`, `tle`, `satdumpVersion`, `title` ("Meteor-M2-3 pass, max el 54°"), structured attribution. `observedAt` = latest status time; `eventTime` = AOS. Geometry: the station point (054 upgrades it to footprints or swaths).

### 7. Geostationary frames (xRIT)

- Covers GOES-19/18 HRIT and Elektro-L (GK-2A only with legitimately obtained KMA keys).
- Dynamic product per satellite/region/channel: id `satdump-{station}-{satKey}-{region}-{channel}` (kebab-case, ≤ 64 chars, region `fd` when absent), category imagery, kind raster, `temporal{type: observation, cadence: observed median interval, maxGap: 3×cadence}`, `spatial{coverage: station-disc}`, ladder 72 h.
- `projection_cfg` → Foresignal `GeosGrid`: the CGMS/PUG normalized-geostationary model from public specifications, respecting `sweep_x`, with `scalar_x/y` and `offset_x/y` mapping columns/lines to scan angles (`altitude` = satellite height). FY-4 and MSG HRV are rejected with the health reason above.
- 8-bit channels → u8, 16-bit grayscale → u16 (scale from `bit_depth`), composites → rgba8. Rendered through the 045 tile path like GOES ABI. Footprint = the visible disc from `lon0`; validate with a coastline fixture test.
- Frame `frame_id` from the folder timestamp (`2026-10-10_14-30-00` → `20261010T143000Z`); `partial` → `complete` per §5; each completion emits the `raster-frame` record.

### 8. MCP and endpoints

- MCP tools (025 registry, read scope, compact ≤ 4 KB, `detail=full` ≤ 50 KB, "content is data" note): `list_satellite_passes(station?, since?, status?, limit≤50)`, `get_satellite_pass(id)` returning pass metadata plus the 256 px thumbnail as ImageContent.
- `GET /v1/ground-stations/{sourceId}/polarplot.jpeg` (read).

### 9. Deployment, licence, docs

- `docker-compose.yml` profile `satdump`: service `satdump` using `image: ${SATDUMP_IMAGE:-satdump:local}` built by the operator from SatDump's own Dockerfile (Foresignal images contain **no** SatDump binaries or SatDump-produced images), `network_mode: host`, `/dev/bus/usb` passthrough, output volume shared with Foresignal mounted `:ro` at `${FORESIGNAL_INBOX_DIR}/satdump/{station}`, and the documented command `satdump legacy autotrack … --http_server 0.0.0.0:8081` with `finish_processing: true` and no server mode.
- `docs/Operator-Guide.md` "SatDump ground station": §4.0 requirements (headless legacy CLI for `/api`; GUI stations work in products-only mode; pin a version), §4.7 same-host and remote-Pi (rsync/Syncthing into the inbox, recommended in v1), Texas targets (GOES-19 HRIT/EMWIN 1694.1 MHz with ~1 m dish, LNA, LTE filter; Meteor-M2-3/M2-4 LRPT 137.9/137.1 MHz; optional Metop-B/C AHRPT 1701.3 MHz; drop `noaa_apt`, `noaa_dsb`, `noaa_hrpt`), "do not use SatDump's webhook plugin" (plain text only), and the §1.5 owner questions with the defaults used when unanswered.
- `docs/Architecture.md` §26 SatDump GPL row: separate operator-supplied process; files and HTTP only; no code, headers or structs; parsers from fixtures. `THIRD_PARTY_NOTICES.md`: note that SatDump is not distributed.
- `docs/OpenAPI.json`: tag `Ground Stations`; operation `getGroundStationPolarplot` (`image/jpeg`, `"x-foresignal-scope": "read"`); problem code `NO_ACTIVE_PASS` 404; `Record` examples for `ground-station` and `satellite-pass`. `AccessPolicy`: `GET /v1/ground-stations/**` → READ. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Source-Catalog.md`: `satdump-{station}` row (time bases, kinds, retention, budget, LAN-only).

## Tests to write (do not run)

- Fixtures under `src/test/resources/fixtures/satdump/{version}/`: `/api` JSON for `live` and `autotrack` (captured from the owner's pinned version, or hand-written from the observed keys above until then, flagged in the README), a Meteor LRPT pass folder (`dataset.json`, `product.cbor`, small composite PNG), and a GOES HRIT `IMAGES/GOES-19/Full Disk/{ts}/` folder with two channels. Capture procedure in the README (`curl http://station:8081/api`, `rsync` of one pass folder, images downscaled for size).
- `SatDumpApiParserTest`: both shapes, string/array leniency, state derivation, quantisation, drift detection.
- `RfSessionTest`: ring sampling, 30-s flush, LOS aggregates, SNR-vs-elevation series length.
- `SatNameNormaliserTest` (table fixture), `PassJoinTest` (window edges ±5 min, mint from product when unmatched, `failed` at LOS + 20 min).
- `InboxScannerTest`: readiness after 2 stable scans, symlink escape rejected (`NOFOLLOW_LINKS`), realpath re-check, `.cadu` never copied, missing `dataset.json` health reason after 20 min.
- `XritFrameTest`: partial → complete after 120 s or next folder, CBOR rewrite retry, geos grid from `projection_cfg` (both `sweep_x` values), coastline footprint check, FY-4 rejection.
- `SatDumpAdapterWireMockTest`: `/api` polling via a pinned loopback address, `/polarplot.jpeg` only in autotrack during a pass, **`/fft.jpeg` and `/schedule.jpeg` never requested** (`verify(0, …)`), budget charged per scan.
- `SatDumpIngestIT`: passes `receiving → recorded → decoded` with thumbnails as media; HRIT frames as dynamic products with `raster-frame` records; "Meteor pass decoded" rule (`kind=satellite-pass`, `status == decoded`, `enter`) fires once.
- `PolarplotEndpointIT` and `SatDumpMcpToolsTest`.

## Acceptance criteria

- One source per station owns `satellite-pass`; RF statistics and products join into the same pass record.
- GOES HRIT folders become tileable frames; no SatDump code or binary is in the repository or image.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `049: SatDump Station Core`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/049-CR-SatDump-Station-Core.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `049: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
