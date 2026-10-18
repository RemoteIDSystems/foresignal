# Foresignal Weather & Imagery subsystem: final design (replaces roadmap prompt 058)

> Historical design input, folded into Architecture.md v2.0 on 2026-10-18.

Prepared 2026-10-10. Nothing in the repo was modified.

I read these files:
- `/Users/adamallard/Documents/GitLab/foresignal/docs/Architecture.md`: §1, §4–§11 (with §6 hashing, §8 the trigger SQL and §11 retention), §14, §15 and §26
- `/Users/adamallard/Documents/GitLab/foresignal/docs/Roadmap.md`: the table; prompts 003–028 are Ready and 029–063 are To write
- `/Users/adamallard/Documents/GitLab/foresignal/docs/Source-Catalog.md`: line 48
- `/Users/adamallard/Documents/GitLab/foresignal/docs/Product-Charter.md`: line 58 and FR-55

This version uses the reviewed draft and applies every blocker and major review issue, plus most minor ones. §0.2 lists each issue and where it is resolved.

---

## 0. Decisions

### 0.1 Settled choices

| Question | Decision | Why |
|---|---|---|
| GRIB2/NetCDF decoder | **NetCDF-Java 5.11.0** (`cdm-core`, `grib`) with `libaec-native` 1.1.7.0. It is fetched only from `https://artifacts.unidata.ucar.edu/all/` through Gradle `exclusiveContent`. | One Java 21 stack decodes GFS (5.3), MRMS (5.41) and GOES NetCDF4, and IFS (5.42) later. No sidecar is needed. |
| Where frames live | `raster_frame` is the source of truth. **Observation** products write one `raster-frame` record version per frame. **Forecast** products write one version per *completed run*. | Frame history becomes record versions, so SSE, resume, MCP and triggers work with no new code. Forecast runs do not flood with one version per step. |
| URL namespace | `/v1/products` | Matches charter FR-55 |
| Rendering | On-the-fly XYZ PNG-8 tiles from compressed native-grid frames, with an index-map cache. At ingest the newest frame is warmed for the places AOI up to the animation zoom. | Measured 4–8 ms per tile. Pre-rendering a full pyramid is not affordable at a 2-minute cadence. |
| Immutable caching | Only **content-addressed** URLs are immutable. The URL must include a frame token `{timeKey}~{sha8}`, a versioned style `name@v`, a pinned `run` for forecasts, and the frame must be `complete`. Everything else gets a short max-age plus an ETag. `public` is sent only when `openReads` covers the path; otherwise `private`. | Fixes partial frames, reprocessed frames, style revisions and shared caches serving authenticated tiles |
| Map-client auth | **Tile keys.** A read-only key limited to path prefixes, carried as a path segment: `/v1/k/{key}/products/...`. Bearer headers remain for clients that can send them. | MapLibre Native/maplibre_gl, Cesium and `EventSource` cannot reliably send headers. A path segment keeps cache keys stable and avoids CORS preflights. |
| Overloaded render queue | Bounded wait of 1.5 s or less, priority ordering and per-client fairness. After that, 503 `RENDER_BUSY` with `Retry-After`. | Shed load only after a short queue |
| Raster alerts | Derived records only, plus `cooldownScope` and `near` in the trigger engine | The existing engine, SSE, MVT, Ask and briefings work unchanged |
| "Unknown" vs "nothing nearby" | **Two different representations.** "None within 100 km" is the numeric sentinel `999`. "Unknown" keeps the last known values and sets section `status: stale\|no-coverage` with `asOf`. `null` is never used for a metric. | Stale or missing data can no longer fire an all-clear |
| SatDump | **One source per station: `satdump-{station}`.** It has an optional `/api` loop and an inbox scan loop that share `adapter_state`. | One source owns `satellite-pass`, and the RF statistics and products join inside that source |
| SatDump polar georeferencing | Foresignal *reads* any EPSG:4326/3857 GeoTIFF in a pass folder. That reader works with any SatDump version. Only **one** producer path gets fixtures in v1: whichever version the owner's station runs (see §1.5). It ships in a later prompt (043), after station core. | Avoids building against two moving SatDump versions at once |
| SatDump geostationary | Foresignal warps from the `geos` `projection_cfg` using the public CGMS LRIT/HRIT and GOES-R PUG formulas | Works on SatDump 1.2 and 2.0, needs no GPL code and no hook |
| Charter non-goal "no hosting/proxying imagery" | **Amend it.** Foresignal hosts products derived from data it ingests. It never proxies basemaps or third-party tiles. GIBS and nowCOAST are `kind: external` catalogue entries that clients fetch directly. | Keeps Architecture §1 intact |
| God's Eye View (GEV) port | Only `server/providers/wind/{catalog,gfs,grid,decode}.js` logic and `src/layers/wind/*` (the web particle layer) are ported, under an MIT notice. `weather.js` is a *reference only*, used for the external nowCOAST catalogue entry. No proxy is ported. | Resolves the draft's inconsistency |

**Rule added to Architecture §1 and §26:** no client request ever causes an upstream fetch. That covers tiles, samples, media, maps and SatDump polar plots. Upstream data is pulled only by enabled, budgeted sources on their own schedule.

### 0.2 Review resolution index

| Issue (severity) | Resolved in |
|---|---|
| Stale data reads as an all-clear (blocker) | §7.1, §7.3, §7.4 |
| `satellite-pass` written by two sources (blocker) | §4.1, §4.4 |
| Immutable URLs over content that can change (blocker) | §6.1 |
| Remote clients cannot authenticate tiles or SSE (blocker) | §6.7 |
| `public` caching and the style version missing from URLs (major) | §6.1 |
| SatDump `finish_processing`, scripting plugin, 1.2 vs 2.0 (major) | §4.0, §4.5, §1.5 |
| `/api` exists only in the legacy CLI (major) | §4.0, §4.2 |
| Two SatDump versions at once (major) | §0.1, §4.5, prompts 036/043 |
| DCS volume unbounded (major) | Deferred: §5.3, prompt 074 |
| Per-kind retention (major) | §2.4 |
| Heartbeats create versions (major) | §7.1 |
| `latest` tiles mix frames (major, two issues) | §6.1, §6.3, §6.8, §6.10 |
| Memory on a 32 GB host (major) | §2.6 |
| Block too large; over-built serving; heavy ingest; attachment surface (major ×4) | §5, §6, §8, §10, §11 |
| Empty tiles and missing zoom metadata (major) | §6.3 |
| flutter_map retina recipe wrong (major) | §6.10 |
| Animation holes under load (major) | §6.3 render queue, `/loop` |
| `/image` placement and time formats (major) | §6.1, §6.3 `/loop` |
| No wind for Flutter or Cesium (major) | §6.4 (speed raster plus barb MVT) |
| HTTP/1.1 connection limit (major) | §6.7 (Caddy `web` profile) |
| Native legends (major) | §6.3 JSON legend |
| Terrain-RGB problems (major) | Deferred to 073: §11 |
| Forecast and mixed-time navigation (major) | §6.1 aliases, §6.2 seamless timeline |
| All minor issues (toolchain, GFSv17, Teams, catalogue facts, ECMWF, MapLibre v6, LAN_URL/INBOX hardening, GLM limits, `.fsg` header, MRMS codes, cooldown SQL, crash window, Unidata repository, Orekit and polarplot churn, externals metadata, expiry, offline, MCP ergonomics, CORS) | Inline throughout. The adaptive-MRMS blind spot is gone because adaptive cadence was dropped. |

---

## 1. Goals and scope

### 1.1 Goals
1. **Weather becomes an intelligence input.** Radar, lightning, fire, severe-storm objects and forecast thresholds become records the trigger engine can alert on. Missing data must never read as an all-clear.
2. **Self-hosted overlays for every client.** MapLibre GL JS, maplibre_gl (Flutter desktop and mobile), flutter_map and Cesium all get animation, legends, point readout and wind. Remote clients work through tile keys.
3. **The owner's ground station is a first-class source.** Station status, passes, RF statistics, thumbnails and GOES HRIT imagery come first. Polar overlays follow. The station keeps giving a weather picture when the internet is down.
4. **Agents can read the weather.** Sampling, series and area stats are exposed through REST, MCP and Ask, with explicit validity time, run, age and coverage. Alerts carry a deterministic weather context block.
5. **It fits a home server.** Two memory profiles, about 5–8.5 GB/day of downloads by default, about 10 GB of rolling raster disk, and a budget on every upstream request.

### 1.2 What is ingested and what stays client-direct

| Category | Ingested in Weather v1 | Ingested later | Client-direct (`kind: external`) |
|---|---|---|---|
| Radar | MRMS reflectivity (QC composite), precip rate + flag, MESH-60m; MRMS ProbSevere v3 as records | Second wave (prompt 040): rotation-60m, radar-only QPE 1h/24h, FFG ratio 1h. Later: NEXRAD L3 for the nearest sites, MRMS lightning probability, VIL | nowCOAST radar mosaics |
| Satellite | GOES-19 ABI CMIPC C13; GOES FDC fire as records; SatDump GOES HRIT imagery | SatDump polar overlays (043); GOES C08/C09, C02, mesoscale sectors, GOES-18 | NASA GIBS GeoColor, AirMass, Dust, FireTemp, VIIRS, IMERG, Black Marble |
| Lightning | GOES GLM L2 LCFA (ring plus archive); `lightning-cluster` records; place metrics | SatDump GRB `glm-local` | nowCOAST lightning density |
| Models | GFS 0.25° (wind, gust, t2m, apcp); HRRR (REFC, gust, MASSDEN, 10 m wind) | ECMWF IFS, RTMA-RU, NBM, GFS-Wave, AIFS | — |
| Records | NWS alerts, SPC outlooks and storm reports, AWC METARs, HMS smoke/fire, NWS gridpoints for places | AirNow, TAF, EMWIN fallback | nowCOAST SST, surge, tropical layers |
| Own station | `satdump-{station}`: ground-station status, `satellite-pass` with RF statistics, thumbnails, xRIT geostationary frames | Polar GeoTIFF overlays and footprints (043); DCS/EMWIN with a mandatory filter (074) | SatDump's own UI |

**Skipped:**
- RainViewer: satellite and nowcast were removed from the free tier on 1 Jan 2026, and free use is limited to personal and educational use.
- Blitzortung: non-commercial, no redistribution.
- MRMS `NLDN_*`: vendor-derived; avoided to keep redistribution clear (a conservative choice, not a requirement).
- Synoptic Open Access: non-commercial.
- Meteostat: its licence pages conflict.
- Himawari, GK-2A and Meteosat: not visible from Texas, or licensed.
- DWD and OPERA: Europe.
- NEXRAD Level II: heavy, and MRMS covers display.
- CAMS: needs an account and an asynchronous API; HRRR-Smoke is better in the US.

### 1.3 Priorities (Texas home operator)
- **v1 (P0):**
  - MRMS reflectivity, precip and MESH; GOES-19 C13; GFS; HRRR
  - ProbSevere, NWS alerts, SPC outlooks, METARs, GLM, FDC
  - Places with place-weather
  - SatDump station core
  - Map-client access
- **v1 (P1):** MRMS second wave; storm reports; HMS; NWS gridpoints; SatDump polar overlays; notification images; the weather context block; briefings.
- **v1.1:** IFS, `forecast-area`, TITAN-lite tracking, model disagreement, correlation annotators.
- **Later:** extended products, interop and offline packs, Terrain-RGB, WebP.

### 1.4 Source Catalog corrections (verified 2026-10-10)
- **NEXRAD:**
  - Level II moved to `unidata-nexrad-level2`; `noaa-nexrad-level2` returns AccessDenied.
  - Level III is at `unidata-nexrad-level3`.
- **GOES and polar satellites:**
  - GOES-East is GOES-19 (`noaa-goes19`).
  - NOAA-18 (6 Jun 2025), NOAA-19 (13 Aug 2025) and NOAA-15 (19 Aug 2025) are decommissioned, so 137 MHz APT is gone.
- **MRMS:**
  - ProbSevere v3 is at `noaa-mrms-pds/ProbSevere/`, about 0.25–0.27 MB per file.
  - The lightning-probability prefix is `CONUS/LightningProbabilityNext30minGrid_scale_1/`.
  - NCEP `mrms.ncep.noaa.gov/2D/.../latest.grib2.gz` honours Range. v1 uses only the S3 listing.
- **HRRR and RRFS:**
  - HRRR `wrfsfcf01` is about 142 MB.
  - RRFS has only retro and sample prefixes on NODD. HRRR is not retired; keep the model adapter swappable.
- **GFS:** PNS 26-29 proposes GFSv17 in October 2026. PNS 26-30 removes GRIB1, goessimp/special files and the time-averaged CRAIN/CSNOW/CFRZR/CICEP fields. There is **no evidence that the 0.5° or 1° files are dropped**. Foresignal uses only `pgrb2.0p25` with configurable path templates, and matches fields by `.idx` variable and level string, never by message number.
- **ECMWF:** the whole real-time catalogue has been CC BY 4.0 since 1 Oct 2025. The open subset is still `ifs/0p25` (plus `aifs-single` and `aifs-ens`), with a 0.1° move announced for 2026. At 00/12z, steps run to 144 h, 3-hourly.
- **AWC:** the cache file updates every minute. AWC asks for at most 1 request/min per thread and 100/min overall, so a 5-minute poll complies. Record this in the descriptor notes.

### 1.5 Questions for the owner before the SatDump prompts
1. Which SatDump runs at the station: 2.0-alpha (which commit) or 1.2.2? Headless `satdump legacy live|autotrack`, or the GUI? Only the legacy CLI has `/api`.
2. Is the station on the bench server, or on a remote Pi at the antenna?
3. Hardware: is there an L-band dish for GOES HRIT (1694.1 MHz)? Is there a 137 MHz antenna for Meteor-M2-3/M2-4 LRPT?
4. Is the bench server 32 GB or 64 GB? (This selects the memory profile.)
5. Will clients reach Foresignal from outside the LAN (VPN, a Tailscale-style overlay, or a public reverse proxy)? This sets the tile-key and Caddy defaults.
6. What are the home location and the other places?

---

## 2. Architecture

### 2.1 Components

```
 Upstreams (NODD S3, NCEP, AWC, NWS, SPC, NESDIS)            SatDump (separate GPL process)
        │ budgeted ctx.http(): listS3 / getRange / download        │ /api (optional)   │ output dir (ro mount)
        ▼                                                          ▼                   ▼
 ┌─ ① Source adapters (POLLING; existing scheduler, budgets, health, probe) ───────────────────────┐
 │  rasters: mrms · goes-east-abi · gfs · hrrr      station: satdump-{station} (api loop + inbox loop) │
 │  records: nws-alerts · mrms-probsevere · spc-* · awc-metars · goes-glm · goes-fdc · hms-* · nws-gridpoints │
 └──────┬──────────────────────────────────────────────────────────┬──────────────────────────────┘
        │ temp file → ② Raster pipeline (bounded decode pool)        │ NormalizedRecords
        │   decode → quantise → stats → footprint → ③ FrameStore     │
        │   → raster_frame row → raster-frame record ─────────────────┤
        ▼                                                             ▼
 ④ Catalogue (ProductSpec registry, raster_product/grid/frame)   IngestQueue → single writer → ChangeBus
        ▼                                                             │   ├─ SSE (raster-frame payload inline)
 ⑤ Serving: TileRenderer · RenderQueue · FieldService · SampleService │   └─ TriggerEngine → alerts → channels
    · Loop/Image · Legend · MapRenderer (static PNG)                   │
        ▲                                                             │
 ⑥ DERIVED generators (no TTL; emit on change; tombstone; reconcile) ◄┘ frame events, GLM ring
    wx-places · wx-areas · wx-cells
```

New packages under `ai.foresignal`:
- `raster`: ProductSpec, ProductRegistry, FrameStore, ElgCodec, GridDef, RasterDecoder SPI, RasterRetentionStep
- `raster.render`: TileRenderer, IndexMapCache, RenderQueue, StyleRegistry, LegendService, LoopService, MapRenderer
- `raster.sample`
- `media`: MediaStore, ImageGuard, ThumbnailService
- `place`
- `derive`: the DERIVED runtime, shared with prompt 070 (old 057)
- `access`: TileKeyFilter

Adapters go only through `ctx.http()`. They gain `ctx.rasters()` and `ctx.media()` and never touch the store directly.

### 2.2 Reuse of the source framework, and where it differs

| Concern | Extension |
|---|---|
| Descriptor | Optional `List<ProductSpec> products`; `kindRetention` map (§2.4); `zeroResultsNormal=true` for product sources |
| Scheduler | Heavy decodes run on a bounded platform pool behind a semaphore (1 concurrent in the small profile, 2 in the large), not on virtual threads |
| UpstreamClient (005) | `getRange(url, start, end, maxBytes)`: only 206 (or 200 when start=0), short bodies rejected, no redirects. `download(url, Path tmp, maxBytes)` streams to `/data/rasters/tmp`. Per-descriptor response cap overrides the 32 MB default. `listS3(bucket, prefix, startAfter)`. |
| BudgetGovernor | Optional `mbPerDay`, charged in advance from the known size (S3 `Size`, `.idx` offsets) and corrected afterwards |
| Health (006) | Frame-age rule: degraded when the newest frame is older than `3 × cadence` (or `maxGap`). `/health` adds `newestFrameAt` and `frameAgeSeconds`. Silent-format-change detection: a missing `.idx` field or variable degrades health and writes no empty frame. |
| Probe | Lists the newest object (or reads the `.idx`), range-reads one message, decodes the header only, and returns a grid summary. Writes nothing. |
| Retention (016) | `retention.kinds` (§2.4), plus `RasterRetentionStep` (ladder, quota, orphan sweep) |
| Modes | `DERIVED`: fed by ChangeBus or frame events; enabled, parameterised, health-tracked and retained like any source. **Derived records have `recordTtl = null`**; liveness comes from explicit tombstones and startup reconciliation. |
| `LAN_URL` param | Validation resolves the host once and checks the address is loopback, RFC 1918, link-local or ULA (a `.local` name must resolve to one of those). The connection is made to the **pinned IP**, with the original Host header and redirects disabled. This defeats DNS rebinding. `local-adsb-*` reuses it. |
| `INBOX_PATH` param | Realpath must sit under `FORESIGNAL_INBOX_DIR` (default `${FORESIGNAL_DATA_DIR}/inbox`). Every scanned file is opened with `NOFOLLOW_LINKS` and its realpath is re-checked against the root, per file and per scan. |
| ChangeBus / SSE (011, 012) | `externalId` filter on `/v1/changes` and `/v1/changes/stream`. `kind=raster-frame` events always carry their small payload (under 1 KB). |

### 2.3 Data model (Hibernate entities; GiST via SchemaBootstrap; Flyway in 079)

**`raster_product`**: `id` PK (kebab) · `source_id` · `category` (radar, satellite, model, lightning, imagery) · `kind` (raster | field | external) · `spec` jsonb · `dynamic` bool · `created_at` · `updated_at`.

`spec` holds:
- units, dtype, scale, offset, nodata, codes (categorical and special values)
- styles, defaultStyle
- `temporal{type observation|forecast|pass, cadence, maxGap}`
- `spatial{coverage: CONUS|global|station-disc|swath, nativeMaxZoom, minZoom}`
- retention ladder, structured attribution

**`raster_grid`**: `id` = sha256 of the canonical definition · `type` (latlon | geos | lcc | affine) · `definition` jsonb · `nx` · `ny` · `bbox` geometry(Polygon, 4326).

**`raster_frame`**:
- Key: PK (`product_id`, `frame_id`). `frame_id` is time-based and immutable, e.g. `20261010T143240Z` or `2026101012Z-f006`.
- Times: `valid_time`, `reference_time`, `forecast_step`, `obs_start`, `obs_end`
- Storage: `grid_id`, `path`, `bytes`, **`sha256` of the decoded content** (it supplies the URL token `sha8`), `dtype`, `codec`
- Content: `stats` jsonb, `footprint` geometry(MultiPolygon, 4326)
- Provenance and state: `upstream_key`, `status` (partial | complete), `thinned`, `ingested_at`, `expires_at` (computed from the ladder; recomputed when it thins)
- Indexes: (`product_id`, `valid_time` DESC); (`product_id`, `reference_time`, `forecast_step`); GiST(`footprint`)

**`media`**: `id` (ULID) · `sha256` · `content_type` · `width` · `height` · `bytes` · `path` · `role` (image, thumbnail, projected, map) · `source_id` · `record_source_id` · `record_external_id` · `licence` · `created_at` · `expires_at` (nullable; null follows the owning record).

**`place`**: `id` · `name` · `geom` Point · `radius_km` (default 20) · `timezone` · `kind` (user, airport, asset) · `tags` text[] · `products` jsonb · `created_at` · `updated_at`.

**Tile keys** reuse `api_token` (019):
- `type = tile_key`, scope `read`, `prefixes text[]`, `expires_at` (at most 30 d), prefix `fsk_`.
- Hashed and revocable like any token.

**Changes to existing tables:**
- `trigger_rule`:
  - `cooldown_scope` (record | rule, default record)
  - `near` jsonb (`{placeId|point, radiusKm}`), materialised into `geofence` with `ST_Buffer(geography)` and re-materialised when the place moves
- `alert`: `context` jsonb.
- Channel config jsonb: `attachMap` (none | static), `mapProducts`, `mapRadiusKm`, `maxAttachmentBytes`.

**`.fsg` frame file** (little-endian, 64-byte header, golden-file test):

| Offset | Size | Field |
|---|---|---|
| 0 | 4 | magic `FSRG` |
| 4 | 2 | version u16 = 1 |
| 6 | 1 | dtype u8: 1 u8, 2 u16, 3 i16, 4 f32, 5 rgba8 |
| 7 | 1 | codec u8: 0 none, 1 deflate |
| 8 | 4 | nx u32 |
| 12 | 4 | ny u32 |
| 16 | 2 | bands u16 |
| 18 | 2 | flags u16 (bit 0 = partial) |
| 20 | 4 | reserved, must be 0 |
| 24 | 8 | scale f64 |
| 32 | 8 | offset f64 |
| 40 | 8 | nodata f64 (raw-code value) |
| 48 | 8 | payloadLength u64 (compressed) |
| 56 | 4 | crc32c u32 of the payload |
| 60 | 4 | reserved, must be 0 |

The payload is band-sequential, with rows north to south. Physical value = `code × scale + offset`. The decoded content is hashed as sha256 for `raster_frame.sha256`.

**MRMS reflectivity u8 codes:**
- 0 = no coverage (MRMS −999)
- 1 = no echo, or below −32 dBZ (MRMS −99)
- 2..255 = `clamp(round(dBZ×2)+66, 2, 255)`, so dBZ = (code − 66)/2

A round-trip test runs against sentinel fixtures. Verify the sentinel values at implementation.

### 2.4 Storage layout and retention

```
${FORESIGNAL_DATA_DIR}/rasters/tmp/                                  staging (atomic rename)
${FORESIGNAL_DATA_DIR}/rasters/frames/{productId}/{yyyy}/{mm}/{dd}/{frameId}.fsg
${FORESIGNAL_DATA_DIR}/rasters/fields/{productId}/{run}/{step}.f32.gz   GEV-format, precompressed
${FORESIGNAL_DATA_DIR}/rasters/glm/{sat}/{yyyymmdd}.bin                  16 B per flash
${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/{passId}/               provenance copies
${FORESIGNAL_DATA_DIR}/media/{ab}/{sha256}.{ext}
${FORESIGNAL_DATA_DIR}/cache/netcdf/                                     NetCDF-Java DiskCache (pinned)
${FORESIGNAL_DATA_DIR}/tmp/  (java.io.tmpdir, jna.tmpdir; exec allowed)
${FORESIGNAL_INBOX_DIR}/satdump/{station}/                               ro bind mount
```

**Quantisation:**
- MRMS reflectivity: u8 codes above
- Precip rate: u16
- GOES IR: u16 (0.01 K, offset 150)
- Model scalars: i16 with scale and offset
- Wind fields: f32
- SatDump composites: rgba8

**Per-kind record retention (amends 016).** Resolution order:
1. `retention.kinds.{kind}`
2. `retention.sources.{id}`
3. descriptor `kindRetention[kind]`
4. `retention.default`

The RetentionJob runs per (source, kind) pair. Settings shape: `{"default":"7d","kinds":{"satellite-pass":"365d"},"sources":{...}}`.

| Kind (descriptor default) | Retention |
|---|---|
| `raster-frame` | 7 d (versions outlive frames; a removed frame returns 410) |
| `satellite-pass` | 365 d |
| `ground-station` | 30 d |
| `place-weather` | 14 d |
| `wx-area`, `fire-detection`, `fire-cluster`, `smoke-polygon`, `metar` | 7 d |
| `radar-cell`, `lightning-cluster`, `probsevere-object` | 3 d |
| `nws-alert`, `storm-report` | 30 d |
| `spc-outlook` | 14 d |

**Raster ladders:**

| Product | Default |
|---|---|
| MRMS | every frame for 6 h, then 10-min frames to 48 h |
| GOES C13 | 5-min frames for 24 h, then 30-min to 72 h |
| Models | current complete run + previous complete run |
| SatDump geostationary | 72 h |
| SatDump polar | 30 d |
| GLM archive | 14 d |
| Media | follows the owning record; map attachments 7 d |

**Order of operations and crash safety:**
- Retention deletes the row and then unlinks the file. The API then returns 410 `FRAME_EXPIRED`.
- The hourly orphan sweeper does three things: removes files with no row, removes rows with no file, and **re-emits the `raster-frame` record for any frame newer than its record head** (this covers a crash between the frame commit and the enqueue).
- `raster.diskQuotaGb` (default 200) evicts oldest-first and degrades `/v1/system` at 90%.

### 2.5 Budgets (defaults; operator-overridable)

| Source | perMinute / perDay | mbPerDay | Interval |
|---|---|---|---|
| `mrms` (refl, precip + flag, MESH; second wave adds 4) | 30 / 12 000 | 4 000 | 2 min, every frame |
| `goes-east-abi` | 10 / 2 000 | 2 500 | 5 min |
| `goes-glm` | 10 / 10 000 | 3 000 | 30 s |
| `goes-fdc` | 4 / 600 | 300 | 5 min |
| `gfs` | 60 / 3 000 | 1 000 | 15-min check, cycle-gated |
| `hrrr` | 60 / 4 000 | 2 000 | 10-min check; every 3 h runs (6 h in the small profile) |
| `mrms-probsevere` | 2 / 800 | 300 | 2 min |
| `awc-metars` | 2 / 400 | 100 | 5 min |
| `nws-alerts`, `spc-*`, `hms-*`, `nws-gridpoints` | small, per source | — | 1–30 min |
| `satdump-{station}` | 60 / 50 000 (LAN only); each inbox scan counts as one request | — | api 5 s; scan 30 s |

### 2.6 Memory and concurrency: two profiles

Selected by `FORESIGNAL_PROFILE=small|large`, never by `MaxRAMPercentage`. Architecture §14 documents both.

| | Small (32 GB host) | Large (64 GB host) |
|---|---|---|
| JVM | `-Xmx4g`, G1 | `-Xmx8g`, generational ZGC |
| Container limit | 6 GB | 11 GB |
| `raster.frameCacheMb` / `tileCacheMb` / `indexCacheMb` | 768 / 128 / 64 | 1536 / 256 / 128 |
| Render pool | min(cores − 2, 6) | cores − 2 |
| Decode concurrency | 1 | 2 |
| HRRR runs | every 6 h | every 3 h |
| GLM ring and `radar-cell` scope | places AOI only | AOI (CONUS opt-in) |

All caches are Caffeine with byte weighers. Eviction rates and hit ratios appear in the `/v1/system` `rasters` block so prompt 078 can tune them from evidence. The docs state plainly that Ollama (8–20 GB) plus Postgres plus the small profile is the limit of a 32 GB host. Optional GDAL subprocesses (up to 1.5 GB RSS) are off the hot path.

---

## 3. Toolchain (versions verified October 2026; licences for an MIT project)

| Concern | Choice | Licence | Notes |
|---|---|---|---|
| GRIB2 and NetCDF4/HDF5 | `edu.ucar:netcdf-java-bom:5.11.0` → `grib`, `cdm-core`; runtime `edu.ucar.unidata:libaec-native:1.1.7.0` | BSD-3; libaec BSD-2; jj2000 permissive; JNA under its Apache-2.0 option | Repository `https://artifacts.unidata.ucar.edu/all/` (the old `/repository/unidata-all/` 301-redirects). Declared with Gradle **`exclusiveContent`** for `edu.ucar` and `edu.ucar.unidata`, verification metadata committed, and a build check that fails if any other group resolves from that repository. This is an explicit exception to the Maven-Central rule (Architecture §2 and §26). Fallback: a vendored local file repository. Use `Grib2RecordScanner.readData` (no `.gbx9`/`.ncx4`). Never pass anything except Foresignal temp paths to `NetcdfFiles.open` (it accepts URLs: SSRF). No `netcdf4` module. Set `-Djna.tmpdir`/`-Djava.io.tmpdir=/data/tmp`. A startup self-test decodes bundled 5.3, 5.41 and 5.42 fixtures; on failure the affected products are disabled with a health reason. |
| CBOR | `tools.jackson.dataformat:jackson-dataformat-cbor` (Boot BOM, 3.x) | Apache-2.0 | SatDump `product.cbor` |
| GeoTIFF read (043) | JDK `javax.imageio` TIFF plus a small GeoKey parser (tags 33550, 33922, 34735) | JDK | EPSG:4326 and 3857 only. BigTIFF is rejected with a health reason (or routed to optional GDAL). |
| Image guard | `ImageGuard` reads the header and rejects anything over 12 000 px on a side or 150 MP **before** decoding | — | Applies to every PNG, TIFF and JPEG from the inbox or ingest |
| Reprojection | In-house `latlon`, `geos` (GOES-R PUG / CGMS), `lcc` (HRRR), `affine` 4326/3857 | — | GeoTools excluded (LGPL, heavy). proj4j deferred. |
| PNG | JDK ImageIO, PNG-8 with `IndexColorModel` and tRNS | JDK | The only tile format in v1 |
| Frame compression | JDK `Deflater(1)` | JDK | aircompressor dropped (v3 needs Java 22+; v2 is in maintenance) |
| Caches / geometry | Caffeine 3.x; JTS 1.20 | Apache-2.0; EPL/EDL | |
| SGP4 (043 and celestrak-gp 044) | `org.orekit:orekit` 13.1.x with a pinned minimal `orekit-data` | Apache-2.0 | One library for SatDump approximate swaths and predicted passes. Not loaded before 043. |
| Static maps (045) | Natural Earth 1:50m, preprocessed into a compact binary at build | PD | No third-party basemap |
| Barb MVT | PostGIS `ST_AsMVT` over unnested arrays | — | Reuses the 013 path; no new dependency |
| GDAL (043, optional) | Build-arg `WITH_GDAL=false`; `ProcessBuilder` only | MIT/X | Exotic CRS and BigTIFF only. No AGPL drivers (e.g. Grok JPEG 2000). No JNI. |

**Licence boundaries:**
- **SatDump (GPL-3.0).** A separate operator-supplied process; Foresignal communicates only through files and HTTP.
  - No SatDump source, headers, structs or projection code in Foresignal. Parsers are written from observed output and fixtures. The geostationary maths comes from the public CGMS and PUG specifications.
  - Foresignal images contain no SatDump binaries or SatDump-produced images.
  - Any Foresignal-authored AngelScript hook (deferred, 074) is MIT, lives as a separate file and is never linked.
- **AGPL.** Velocity, Shadowbroker and World Monitor are reference only.
- **MIT.** GEV wind ports carry file-level origin headers and a `THIRD_PARTY_NOTICES.md` entry (Copyright 2026 Bilawal Sidhu).
- **Notices** must also list jj2000, libaec, JNA, Natural Earth, viridis/cmocean, and ECMWF CC BY 4.0 (when IFS lands).

---

## 4. SatDump integration

### 4.0 Station requirements (operator docs, compose profile, health messages)
- **`/api` exists only in the headless legacy CLI** (`src-cli/legacy/...`). For RF and status data, run `satdump legacy autotrack … --http_server 0.0.0.0:8081` or `legacy live … --http_server`. A GUI-run station works in **products-only mode**.
- **`finish_processing: true`** is required for live and autotrack, with **no `server_mode`**. Without it there is no pipeline run, possibly no `dataset.json`, and no hook.
- A scripting plugin is needed only by the deferred 2.0 hook (074). It is EXPERIMENTAL and OFF by default (`PLUGIN_SCRIPTING`); the official Dockerfile enables it.
- **Never fetch `/fft.jpeg`**: requesting it turns the FFT on.
- Pin a SatDump commit or version. Keep a fixture per output type, including a `/api` JSON fixture per pinned version, so removal or drift is caught.

### 4.1 One source per station: `satdump-{station}`
- Params:
  - `apiUrl` (`LAN_URL`, optional)
  - `inboxPath` (`INBOX_PATH`, optional; at least one of the two is required)
  - `location` (station point)
  - `satNames` (an optional override of the normalisation table)
- Two loops in one adapter: the `/api` loop (5 s, configurable 2–30 s) and the inbox scan (30 s). They share `adapter_state`, which holds recent RF sessions (last 50), processed paths and pending passes.
- Kinds: `ground-station`, `satellite-pass`, `raster-frame` (through the pipeline). `dcp-observation` and `emwin-product` are deferred to 074.

### 4.2 `/api` loop (optional)
- **Lenient parsing**: module stats keys differ by decoder.
- **`ground-station` record** (`externalId = {station}`):
  - Payload: `state` (idle | tracking | receiving | locked | unknown), `currentTarget`, `frequencyHz`, `az`, `el`, `nextAos`, `nextLos`, `rotatorEngaged`, `satdumpMode`.
  - A new version is written only on a state change or a quantised change (az/el to 1°, SNR to 1 dB). With no `apiUrl`, `state: unknown` and the source stays healthy.
- **RF session**:
  - From tracking start to LOS, samples of `snr`, `peak_snr`, `viterbi_ber`, `deframer_state`, `rs_avg` and az/el about every 2 s go into an in-memory ring.
  - The ring is flushed to `adapter_state` **every 30 s and at LOS**.
  - At LOS the session aggregate is computed: `snrMaxDb`, `snrMeanDb`, `berMin`, `berMean`, `syncedFraction`, `firstLockAt`, `lastLockAt`, `firstLockElevationDeg`, `lastLockElevationDeg`, and an SNR-against-elevation series of about 60 points.
- **Polar plot**:
  - During active passes the latest `/polarplot.jpeg` is held **in memory only**, overwritten each time, not stored in media.
  - It is served at `GET /v1/ground-stations/{sourceId}/polarplot.jpeg` (read scope; 404 when no pass is active; never fetched on client demand).
- **Health**: failing when `apiUrl` is configured and unreachable. A failed `/api` contract-test fixture means the version drifted.

### 4.3 Inbox loop
- **Scanning**: a full scan every 30 s. `WatchService` is only a hint, because inotify is unreliable on NFS, SMB and bind mounts. `NOFOLLOW_LINKS` and per-file realpath checks apply (§2.2).
- **Readiness**:
  - A polar pass folder is ready when `dataset.json` exists and every file size has been stable for 2 scans.
  - An xRIT timestamp folder (`IMAGES/<sat>/<region>/<ts>/`) produces a **`partial`** frame as soon as one channel validates. The frame becomes `complete` after 120 s of quiet or when the next timestamp folder appears. `product.cbor` is rewritten as each channel arrives: read it, validate it, retry.
- **Parsing**:
  - `dataset.json`: `satellite`, `timestamp`, `products[]`.
  - `product.cbor`: `instrument`, `type`, `product_timestamp`, `images[]`, `projection_cfg`.
  - PNG through ImageGuard, then ImageIO (16-bit grayscale supported).
- **Health reasons**:
  - "Pass ended {t} but no dataset.json after 20 min. Is `finish_processing: true` set, and server_mode off?"
  - "FY-4/MSG HRV has no usable projection."
  - "BigTIFF not supported."

### 4.4 Pass lifecycle and join (fixes the dual-writer blocker)
- **Satellite key**: a normalised satellite name (e.g. `METEOR-M2 3`, `Meteor-M2-3` and NORAD 57166 all map to `meteor-m2-3`). The table lives in a code fixture and can be overridden with `satNames`.
- **externalId** = `{station}:{satKey}:{aosEpochMinute}`:
  - Minted from the RF session (tracking start) when one exists.
  - Otherwise minted from `dataset.json` `timestamp`.
- **Join**: a decoded product attaches to the RF session with the same `satKey` whose window [firstLock − 5 min, LOS + 5 min] contains the product timestamp. If there is no match, a new pass is minted from the product.
- **Versions**:
  - `receiving` (at AOS)
  - `recorded` (at LOS, with `rf{}`)
  - `decoded` or `failed`. `failed` means no products by LOS + 20 min while an RF session had lock.
  - `predicted` is added by 044 `celestrak-gp`.
- **Payload**: satellite, satKey, norad?, pipelineId, frequencyHz, aos, los, maxElevationDeg, `rf{}`, instruments[], `products[{productId, frameKey, composite, thumbMediaId, tilesTemplate}]`, tle, satdumpVersion, structured attribution.
- **Geometry**: actual footprint (043), then approximate swath (043), then the station point (v1 core).

### 4.5 Georeferencing
1. **Geostationary xRIT (v1 core, 036).** Covers GOES-19/18 HRIT and Elektro-L, plus GK-2A only with legitimately obtained KMA keys.
   - `projection_cfg {type: geos, lon0, sweep_x, scalar_x/y, offset_x/y, width, height, altitude}` maps to an Foresignal `geos` grid, with the sweep axis respected.
   - It renders through the same tile path as GOES ABI.
   - Footprint = the visible disc seen from `lon0`. A coastline fixture test validates the geometry.
   - FY-4 and MSG HRV are rejected with a health reason.
2. **Polar (043).**
   - Foresignal accepts any GeoTIFF in EPSG:4326 or 3857 that matches `polarGlob` (default `**/*.tif`) in a ready pass folder, and loads it as an RGBA `affine` frame.
   - Footprint: scan each alpha-mask row for its leftmost and rightmost valid pixel, build the polygon, simplify it with JTS, and split it at the antimeridian.
   - The producer is documented for **one** version, from the owner's answer (§1.5):
     - **If 1.2.2:** composite `"project": {"config": {"type": "equirec"}, "img_format": ".tif", "draw_map_overlay": false}`.
     - **If 2.0-alpha:** preset `project` keys are ignored, so the hook in 074 is needed. Until then, passes get thumbnails and approximate swaths only.
   - **If the owner does not answer:** default to 1.2.2 composite `project`, best-effort.
3. **Fallback (043).**
   - No overlay tiles are produced.
   - The approximate swath uses Orekit SGP4 from the TLE in `product.cbor` between the first and last timestamps, ± half swath (`corr_swath`, otherwise a per-instrument table: MSU-MR about 2800 km, AVHRR about 2900 km).
   - Foresignal never reimplements SatDump's raytracer.
4. **Other CRS**: optional GDAL `gdalwarp -t_srs EPSG:4326`, or rejected with a health reason.

### 4.6 Thumbnails and media
- Each composite PNG produces a 256 px and a 1024 px JPEG (PNG when it has alpha), stored as media and linked from `products[].thumbMediaId`.
- The original GeoTIFF and the provenance files are copied under `rasters/satdump/{station}/{passId}`.
- `.cadu` and basebands are never copied; they stay under SatDump-side retention.

### 4.7 Deployment
- **Same host:** compose profile `satdump` with `network_mode: host`, `/dev/bus/usb` passed through, a shared volume that Foresignal mounts `:ro`, and the documented CLI line from §4.0.
- **Remote Pi:**
  - (a) rsync or Syncthing to `${FORESIGNAL_INBOX_DIR}`; the 30-s rescan picks it up. This is the recommended path in v1.
  - (b) Push to `POST /v1/ingest/{sourceId}/media` and `/rasters`: 043, ingest key, 25 MB per file, ImageGuard.
- **Texas targets:**
  - GOES-19 HRIT/EMWIN on 1694.1 MHz: about 1 m dish, LNA, and an LTE filter.
  - Meteor-M2-3/M2-4 LRPT on 137.9/137.1 MHz: QFH or V-dipole.
  - Optional Metop-B/C AHRPT on 1701.3 MHz.
  - Drop `noaa_apt`, `noaa_dsb` and `noaa_hrpt` from defaults.
- **Do not use SatDump's webhook plugin as an ingest path.** It posts plain text only.

---

## 5. Source list

### 5.1 Weather v1

**Raster products** (MRMS uses one access path: S3 ListObjectsV2 with `start-after`, every frame, no adaptive cadence):

| Product | Access | Cadence and latency | Upstream size | Stored |
|---|---|---|---|---|
| `mrms-reflectivity` | `noaa-mrms-pds/CONUS/MergedReflectivityQCComposite_00.50/{YYYYMMDD}/` | 2 min, about 1 min latency | 1.0–2.4 MB | u8 7000×3500 |
| `mrms-precip-rate` (+ PrecipFlag band) | `PrecipRate_00.00`, `PrecipFlag_00.00` | 2 min | 0.6–1.1 MB each | u16 + u8 band |
| `mrms-mesh-60m` | `MESH_Max_60min_00.50` | 2 min | 40–150 KB | u8 |
| `goes-east-ir` | `noaa-goes19/ABI-L2-CMIPC/{Y}/{DOY}/{HH}/…M6C13_G19…` | 5 min, about 30 s latency | 3.4–3.9 MB | u16 `geos` 2500×1500 |
| `gfs` (UGRD/VGRD 10 m field, GUST, TMP 2 m, APCP) | `noaa-gfs-bdp-pds/gfs.{D}/{HH}/atmos/gfs.t{HH}z.pgrb2.0p25.f{FFF}` + `.idx` ranges (GEV port) | 4 runs/day; f000–f048 3-hourly, f054–f120 6-hourly | about 0.8 MB per field per step | i16 1440×721; field `.f32` at 1° and 0.25° |
| `hrrr` (REFC, GUST, MASSDEN 8 m, UGRD/VGRD 10 m) | `noaa-hrrr-bdp-pds/hrrr.{D}/conus/hrrr.t{HH}z.wrfsfcf{FF}.grib2` + `.idx` | every 3 h (6 h small), f00–f18 | about 1 MB per field | `lcc` 1799×1059 i16 |

**Record sources:**

| Source | Access | Cadence | Records |
|---|---|---|---|
| `nws-alerts` (moved from old 034) | `api.weather.gov/alerts/active` | 1 min | Alert polygons |
| `mrms-probsevere` | `noaa-mrms-pds/ProbSevere/{D}/` | 2 min | `probsevere-object` (ID → externalId); filtered to the places-AOI bbox; tombstoned when absent |
| `spc-outlooks` | `day{1,2,3}otlk_{cat,torn,wind,hail}.lyr.geojson` | 15 min, conditional GET | Outlook polygons |
| `spc-storm-reports` (moved from old 051) | SPC daily CSV | 10 min | Points |
| `awc-metars` | `aviationweather.gov/data/cache/metars.cache.csv.gz` | 5 min | Station point; version = report time |
| `goes-glm` | `noaa-goes19/GLM-L2-LCFA/` (20-s files, about 0.36 MB) | 30 s | Flashes go to the ring and archive only; `lightning-cluster` records; place metrics |
| `goes-fdc` | `noaa-goes19/ABI-L2-FDCC/` | 5 min | `fire-detection` (`g19:{col}:{row}`, mask 10–13/30–33, FRP to 2 significant figures, tombstoned after 3 missing scans); `fire-cluster` |
| `hms-smoke`, `hms-fire` | NESDIS HMS KML/Text | 30 min | Smoke polygons, fire points |
| `nws-gridpoints` | `/points/{lat},{lon}` → `forecastGridData` per place | 1 per place per hour | Summarised into place-weather; series cached |
| `satdump-{station}` | §4 | 5 s / 30 s | §4 |

**Derived (DERIVED mode):** `wx-places`, `wx-areas`, `wx-cells` (untracked cells plus the simple centroid ETA).

**Places AOI** (defined in 039 and used by ProbSevere, GLM, cells, wx-areas and warming):
- The union of each place's buffer of max(`radiusKm`, 150 km). Its envelope is used for upstream filters.
- With no places, `weather.defaultAoi` applies (default: Texas bbox), with a health warning.

**External catalogue entries** (no fetching):
- **Each entry carries:** `externalTemplate`, `tilingScheme`, `tileSize`, `maxNativeZoom`, `bounds`, `timeDimension{format, period, typicalLatency, defaultLag}` and structured attribution.
- **GIBS WMTS** (`epsg3857/best`): GOES-East/West GeoColor, Band 13, Band 2, Air Mass, Dust, FireTemp, VIIRS true colour, IMERG, Black Marble. Per-layer `maxNativeZoom` comes from the Level6–Level9 matrix sets.
- **nowCOAST** GeoServer WMS and GWC WMTS: radar, lightning density, SST, surge, tropical.
- **CORS:** both return `Access-Control-Allow-Origin: *` (checked 2026-10-10).

### 5.2 Second wave (prompt 040)
- MRMS `RotationTrack60min_00.50`, `RadarOnly_QPE_01H_00.00`, `MultiSensor_QPE_24H_Pass2_00.00` and `FLASH_QPE_FFG01H_00.00`. These are added to the existing `mrms` source.
- FFG ratio is stored as a percentage, so 0.1 precision is enough later.

### 5.3 Later (v1.1 and extended)
- **IFS (071):**
  - Prefer the `ecmwf-forecasts` AWS mirror, with `data.ecmwf.int` as fallback.
  - Configurable resolution segment and step list.
  - The grid comes from the GRIB header; normalise lo1 = 180.
  - Serial requests, backing off on 503.
  - CC BY 4.0 notice with "changes made" on every derived output.
- **Products (072):** GOES C08/C09, C02, mesoscale sectors, GOES-18; RTMA-RU; NBM; GFS-Wave; NEXRAD L3 for 1–3 nearest sites; SWPC OVATION; Geo-Polar SST via ERDDAP; AirNow; AIFS; MRMS `LightningProbabilityNext30minGrid_scale_1`, VIL and EchoTop50; TAF.
- **SatDump extras (074):**
  - DCS/EMWIN with a mandatory bbox (default the places AOI) and an optional DCP address/agency allowlist. Messages without a PDT location are dropped. Emit on change; 3 d retention; per-scan record cap with a health warning.
  - The 2.0 hook, `glm-local`, the EMWIN warnings fallback and the OMM feed.

---

## 6. API design (additive to `/v1`; camelCase; ProblemDetail; `x-foresignal-scope` on every operation)

### 6.1 Conventions: time, frame keys and caching
- **Time in JSON.** Every frame object carries:
  - `time` (ISO extended UTC) and `timeKey` (path form `20261010T143240Z`)
  - `frameKey` (`{timeKey}~{sha8}`)
  - `expiresAt`, `status`
  - `run` and `step` for forecasts

  Responses carry `serverTime` and `earliest`. **The server hands out fully expanded URLs and templates; clients never build time strings.** An ISO-extended time in a path is canonicalised with a 301.
- **Frame-key aliases:**
  - `latest`: the newest *complete* observed frame. For forecasts, the newest complete run's step nearest now.
  - `now`: the frame whose valid time is at or before now with the smallest gap (within `maxGap`).
  - `run=latest|previous|<runIso>`.
  - Partial frames are excluded unless `includePartial=true`.
- **Forecast concrete times** require `run`. With none, the server returns 400 `RUN_REQUIRED` (`run=latest` is allowed and is not immutable).
- **Style versions** use `style=name@v` (e.g. `nws-reflectivity@3`). TileJSON, frames, `/loop` and SSE always emit the versioned form.
- **Cache policy (one rule for tiles, images, fields and media):**

| URL | Cache-Control |
|---|---|
| Content-addressed (`~sha8`, `@v`, pinned run, `complete`) | `{public\|private}, max-age=31536000, immutable`, strong ETag |
| Anything else (aliases, bare `timeKey`, unversioned style, partial frame) | `{public\|private}, max-age=min(cadence/2, 60s)`, ETag, `checkNotModified` before rendering, `Foresignal-Frame-Time` header |

  - `public` only when `openReads=true` covers the path; otherwise `private`.
  - A stale `sha8` for a reprocessed frame returns 404 `UNKNOWN_FRAME` with a `Link` header to the current key.
  - Retention-removed frames return 410 `FRAME_EXPIRED`. With `?onExpired=transparent`, the transparent tile is served with `Foresignal-Frame-Expired: true`.
  - PNG is not gzipped; field binaries are precompressed and sent with `Content-Encoding`.

### 6.2 Catalogue, frames, timeline

| Method and path | Scope | Notes |
|---|---|---|
| `GET /v1/products?category&kind&bbox&source&q&compact` | read | Per product: id, title, kind, category, source, units, `coverage` (CONUS \| global \| station-disc \| swath) with a one-line meaning; `temporal{type, cadence, maxGap, earliest, latest, retention, runs[]}`; `spatial{bounds, minZoom, maxZoom (native), center}`; `render{defaultStyle (versioned), styles[], defaultOpacity}`; `legend` (short block plus link); attribution object; `status{health, stale, lastFrameAt, reason}`; links. Supports conditional GET (ETag) and `compact=true`. |
| `GET /v1/products/{id}` | read | |
| `GET /v1/products/{id}/frames?from&to&run&limit&cursor&includePartial` | read | Cursor page of frames, each with expanded `tiles` and `image` templates and `expiresAt`. Prefetch hint: `{recommendedFrames: 12, maxZoomForAnimation}`. |
| `GET /v1/products/{id}/frames/latest` | read | Short max-age |
| `GET /v1/products/timeline?products=a,b&from&to` and `?mode=seamless&past=mrms-reflectivity&future=hrrr-refc` | read | Ticks marked `observed\|forecast`, the frame per product per tick (honouring `maxGap`), `run` used, `nowIndex`. Never mixes runs. External entries get synthesised ticks with `availability: unverified`. Ships in 038. |

### 6.3 Tiles, images, loops, legends

| Method and path | Scope | Notes |
|---|---|---|
| `GET /v1/products/{id}/tiles/{frameKey}/{z}/{x}/{y}[@2x].png?style&run&empty` | read | WebMercatorQuad, 256 px (512 px for `@2x`). Above native maxzoom: 400 `TILE_OUT_OF_RANGE` (clients overzoom). An empty or out-of-bounds tile returns a **shared transparent PNG with one fixed ETag**, cached immutable; `?empty=204` opts into 204. |
| `GET /v1/products/{id}/tilejson?frameKey&style` | read | TileJSON 3.0.0 with a **concrete content-addressed template** (an alias is resolved when the JSON is generated; the JSON itself has a short max-age), plus `bounds`, `minzoom`, `maxzoom` (native: MRMS 8, GOES C13 7, GFS 4, HRRR 8), `center`, `tileSize: 256`, `scale` variants and attribution. |
| `GET /v1/products/loop?products=a[,b]\|past&future&bbox\|place&radiusKm&frames≤24&from&to&width≤1024&crs=EPSG:3857&style` | read | Primary mobile and animation path. The server snaps the bbox once and returns `{crs, bounds[w,s,e,n], coordinates[[tl],[tr],[br],[bl]], width, height, frames[{time, timeKey, frameKey, kind, run?, step?, url, bytes, expiresAt}], legendUrl, attribution[], serverTime}`. |
| `GET /v1/products/{id}/image/{frameKey}.{png\|jpg}?bbox&width&height&crs&style` | read | Up to 2048 px. Returns the `Foresignal-Image-Bounds` header. |
| `GET /v1/styles`, `/v1/styles/{id}` | read | Versioned JSON compiled to 256-entry LUTs: `nws-reflectivity`, `ir-gray`, `ir-enhanced`, `precip-rate`, `mesh`, `temperature`, `wind-speed`, `smoke`, `rgb` (SatDump). Only registered names are accepted (400 `UNKNOWN_STYLE`). |
| `GET /v1/products/{id}/legend?style&units=metric\|imperial` (JSON) and `.png?width&orientation&theme=dark\|light&units` | read | JSON: `{styleId, styleVersion, type: continuous\|discrete\|categorical, units, unitsLabel, stops[{value, color, label}], ticks[], categories[{code, color, label}], specials[{kind: noCoverage\|noEcho\|noData, color, label}], meaning[{min, max, text}]}`. Example meaning: "≥ 50 dBZ: heavy rain, hail possible". |

**Render queue:**
- Priority order: newest frame first, then interactive requests, then prefetch (marked with `Foresignal-Prefetch: 1` or `?prefetch=1`).
- Fair queueing per token or tile key (otherwise per IP), single-flight per tile key, and a bounded wait of 1.5 s or less.
- After that, 503 `RENDER_BUSY` with `Retry-After: 1`.
- **Warming:** at ingest, the newest frame is rendered for the places AOI at z ≤ `maxZoomForAnimation` (radar 7, IR 6), at 1× and 2×, into the in-memory tile cache. That is a few dozen tiles at 4–8 ms each.

### 6.4 Fields and wind

| Method and path | Notes |
|---|---|
| `GET /v1/products/gfs/fields/{frameKey}.json?run&res=1\|0.25&bbox` | The GEV `schemaVersion: 1` manifest plus cycle, grid, `components[{name, offset}]`, byteLength, sha256, attribution, stale, and a content-addressed `data.f32` URL |
| `GET …/fields/{frameKey}.f32` | Planar little-endian Float32 (u, v), rows north to south, longitudes 0..359, precompressed gzip. 1° is 521 KB raw; 0.25° is 8.3 MB, so a bbox is recommended. Read directly by the ported GEV particle layer. |
| `GET /v1/products/{id}/vectors/{frameKey}/{z}/{x}/{y}.mvt?run` (`gfs`, `hrrr`) | Thinned points (about 32 px spacing per zoom) with `speedMs`, `fromDeg`, `barbKt` (5-kt bucket). Rendered as rotated symbols in MapLibre and maplibre_gl, a `MarkerLayer`/`CustomPainter` in flutter_map, and billboards in Cesium. Built with `ST_AsMVT` over unnested arrays. Cached like tiles. |
| Tiles with `style=wind-speed@v` | A speed raster derived from u/v; works in every client |

Particles stay a web-only progressive enhancement. `.i16` and `.png` field encodings are deferred.

### 6.5 Sampling and places

| Method and path | Scope | Notes |
|---|---|---|
| `GET /v1/sample?lon&lat\|place&products&time=latest\|now\|<iso>\|-PT1H\|+PT6H&units` | read | Per product: `value` (or `{u, v}` with `derived{speedMs, fromDeg, compass}`), units, label, `frameKey`, `validTime`, `localTime` (place timezone), `ageMinutes`, `run`, method, `status: ok\|no-coverage\|no-data\|stale\|unavailable`, and `statusText`. For no-coverage: "outside product coverage; this is not evidence of clear weather". Attribution object. |
| `POST /v1/sample` (up to 100 points) | read | |
| `GET /v1/products/{id}/series?lon&lat\|place&from&to&run` | read | Observed past plus forecast future; capped at 500 steps |
| `POST /v1/products/{id}/stats` | read | min, max, mean, p95, coveragePct, and areaKm² above each threshold |
| `GET/POST /v1/places`, `GET/PUT/DELETE /v1/places/{id}` | read / agent | |
| `GET /v1/places/{id}/weather` | read | The place-weather record plus cached forecast series |

Flutter hover readout uses a debounced `/v1/sample` (150 ms or more, on tap or long-press).

### 6.6 Media, maps, ingest

| Method and path | Scope | Notes |
|---|---|---|
| `GET /v1/media/{id}`, `/v1/media/{id}/thumbnail?w=256\|1024` | read | Content-addressed, so immutable |
| `GET /v1/ground-stations/{sourceId}/polarplot.jpeg` | read | In-memory latest image only (§4.2) |
| `GET /v1/maps/static?products&center&radiusKm\|bbox&time&width≤1280&height&alert&place` | read | Ships in 045. Static PNG: Natural Earth, alert geometry, range rings, timestamps, legend, attribution, and an "Automated — not an official warning" footer. |
| `POST /v1/ingest/{sourceId}/media`, `/rasters` | ingest key | Ships in 043. 25 MB per file (413 `MEDIA_TOO_LARGE`); ImageGuard. |

### 6.7 Access for map clients
- **Tile keys** (ship in 037):
  - `POST /v1/tile-keys {name, prefixes: ["/v1/products/","/v1/media/","/v1/changes/stream"], ttl ≤ 30d}` (agent scope) returns `fsk_…` once. `GET` and `DELETE` work like tokens.
  - Clients use `/v1/k/{key}/products/...`. `TileKeyFilter` validates the key, the prefix and the GET method, then forwards to the canonical path.
  - Also accepted for `/v1/k/{key}/changes/stream`, for browser `EventSource`.
  - The path-segment form keeps the cache key stable for the key's lifetime.
  - Docs: rotate weekly; keys appear in proxy logs, so they are read-only and prefix-limited by design.
- **CORS:**
  - `Expose-Headers: Foresignal-Frame-Time, Foresignal-Image-Bounds, Foresignal-Frame-Expired, Retry-After, Link, ETag`
  - `Allow-Headers: Authorization, If-None-Match, Foresignal-Prefetch`
  - `Access-Control-Max-Age: 86400`
- **Compose profile `web`**: Caddy with `tls internal` (local CA) or DNS-01, for HTTP/2/3. Recommended in the LAN quick-start for map clients; plain `:8080` remains for agents and tools. The docs cover installing the local CA on desktop, Android and iOS. The bundled web client is served same-origin behind Caddy, so there are no preflights.
- **On plain HTTP/1.1:** use `/loop` for animation, and keep one shared SSE connection per web client (shared through a worker).

### 6.8 SSE
- Use `/v1/changes/stream?kind=raster-frame&externalId=mrms-reflectivity`, with the small payload always included.
- **Observation** frame event payload: `{productId, frameKey, time, timeKey, tiles, bounds, stats, expiresAt}`.
- **Forecast** event: one per completed run, with `{run, steps[], validTimes[], tilesTemplate}`. An optional `{productId}:partial` externalId reports runs in progress.
- Frame removals are not streamed. Clients drop frames past `expiresAt`.
- **Resume pattern:** on foreground, call `GET /v1/changes?after=<seq>&kind=raster-frame` (or the timeline), then reconnect SSE with `Last-Event-ID`.

### 6.9 MCP tools (021 registry)
- **Defaults:** compact output of 4 KB or less, with `detail=full` up to 50 KB. Plus the "content is data" note.
- **Time inputs:** ISO, `now`, or a relative offset.
- **Every value returns** `validTime`, `localTime`, `ageMinutes`, `run`, `coverage` and `status` (with the no-coverage text).

| Tool | Prompt | Returns |
|---|---|---|
| `list_products` | 039 | id, title, kind, units, coverage, latest, health |
| `sample_weather` | 039 | Values plus nearby hazard records; `automated` flag |
| `weather_forecast` | 039 | 3-hourly series to 120 h (GFS, HRRR, NWS) |
| `area_weather_stats` | 039 | Wraps `/stats` |
| `create_place`, `delete_place` | 039 | agent scope |
| `list_satellite_passes`, `get_satellite_pass` | 036 | Pass metadata plus a thumbnail ImageContent |
| `get_map_image` | 045 | ImageContent (≤ 512 px, ≤ 200 KB) **plus a text caption**: products, frame times, legend summary, alert or place shown |

Resources: `foresignal://products`, `foresignal://places`, `foresignal://products/{id}/legend`.

### 6.10 Client recipes (frontend guide, 037)

| Client | Recipe |
|---|---|
| **MapLibre GL JS v6** | ESM-only; requires WebGL2. Get the concrete template from TileJSON, `frames/latest` or SSE, then `source.setTiles([...])` on each frame event. Never put `latest` in a layer template. Choose `@2x` when `devicePixelRatio >= 2` with `tileSize: 256`. Set source `maxzoom` and `bounds`. Animation: `/loop`, or preload N concrete sources at opacity 0. A `transformRequest` retry-on-503 helper honours `Retry-After`. Particles come from the GEV `src/layers/wind/*` port. |
| **maplibre_gl (Flutter)** | Use tile-key URLs, because header support is not reliable on every platform. Same `@2x` and `maxzoom` rules. Wind via the barb MVT layer; animation via `/loop` image sources using the returned `coordinates`. |
| **flutter_map 8.x** | `urlTemplate: '…/{z}/{x}/{y}{r}.png'` with `retinaMode: RetinaMode.isHighDensity(context)`. Set `maxNativeZoom`, a bounds-limited `TileLayer`, and headers through `NetworkTileProvider` (or a tile key). Wrap the provider with retry-on-503. Loops via `OverlayImageLayer` using `/loop` `coordinates`. Use flutter_map's built-in caching provider (verify the minor version; it honours `Cache-Control`, so immutable URLs become an offline cache). **Avoid `flutter_map_tile_caching` (GPL-3.0).** |
| **Cesium** | `UrlTemplateImageryProvider` with `WebMercatorTilingScheme`, `maximumLevel` = native zoom, `rectangle` = bounds, and tile-key URLs. Wind via barb MVT points, or the `wind-speed` raster. |
| **Legends** | Native clients render from the JSON legend. Web may use the PNG. |
| **Attribution** | Always the structured object (`{text, html, url, licence, licenceUrl, requiredDisplay}`). flutter_map uses `RichAttributionWidget`. |

### 6.11 AccessPolicy, problem codes, OpenAPI
- **AccessPolicy (deny-by-default):**
  - READ: GET on products, styles, sample, media, maps, places and ground-stations; POST on sample and stats.
  - AGENT: POST/PUT/DELETE on places; POST/DELETE on tile-keys.
  - `INGEST_KEY_OR_ADMIN` already covers `/v1/ingest/**`.
- **New problem codes:**

| Code | Status |
|---|---|
| `UNKNOWN_PRODUCT`, `UNKNOWN_FRAME`, `UNKNOWN_PLACE` | 404 |
| `FRAME_EXPIRED` | 410 |
| `TILE_OUT_OF_RANGE`, `UNKNOWN_STYLE`, `RUN_REQUIRED` | 400 |
| `PRODUCT_UNAVAILABLE` | 503 |
| `RENDER_BUSY` | 503 + `Retry-After` |
| `MEDIA_TOO_LARGE` | 413 |
| `INVALID_TILE_KEY` | 401 |

- **OpenAPI:** tags Products, Sampling, Places, Media, Maps, Ground Stations. Binary types `image/png`, `image/jpeg`, `application/octet-stream`, `application/vnd.mapbox-vector-tile`.
- **`/v1/system` `rasters` block:** profile, products, frames, storeBytes, diskQuotaPct, cache hit/eviction rates, renderQueue, renderBusyLastHour, mbDownloadedToday.

---

## 7. Alerting and intelligence integration

### 7.1 Principles for derived records
- **Emit only on change** of the quantised payload. `observedAt` stays at the frame in which the change was seen.
- **No `validUntil`**: derived records have TTL null, so no heartbeat versions are written. Generators **tombstone explicitly** when an object disappears, and on startup they **reconcile** state from `record_head` (rebuild the rolling windows; tombstone records for deleted places and cells).
- **Fields that would change on every frame are not in the payload.** Frame ids, timestamps and ages are excluded; `asOf` appears only while a section is not `ok`, and stays fixed while it is not `ok`.
- **Unknown is not clear:**
  - Each section (`radar`, `hail`, `precip`, `lightning`, `cell`, `smoke`) has `status: ok|stale|no-coverage|unavailable`.
  - When input is older than `maxGap` (MRMS 6 min, GLM 2 min), comes from a failing source, or lacks coverage, the section **keeps its previous metric values**, sets the status, and adds `asOf` (the time of the last good data).
  - "None within 100 km" is the numeric **`999`**, never null.
- Every derived payload carries `automated: true, notAWarning: true`. NWS alerts stay authoritative.
- **Derived-record rule recipes use `enter`/`exit` only** (or an explicit cooldown), never bare `match`.

### 7.2 Derived kinds (v1)

| Kind | externalId | Produced by |
|---|---|---|
| `place-weather` | `place:{id}` | `wx-places`, on each relevant frame event (and a 60-s staleness check). Writes only on change. |
| `wx-area` | `{field}:{threshold}:{aoi}` (e.g. `refl:50:home`, `mesh:25:home`) | `wx-areas`, every 6–10 min |
| `radar-cell` | `c-{ulid}` | `wx-cells`, AOI only:<br>• 40 dBZ mask, 3×3 opening, 8-connected union-find, split at 50 dBZ cores<br>• run-rectangle union to polygons<br>• properties maxDbz, areaKm², meshMax, glmFlashes5m<br>• **simple motion:** nearest-centroid match to the previous frame (≤ 30 km per 10 min), least squares over the last 3 matches; `motion{speedKmh, bearingDeg, method: centroid-v1}` |
| `lightning-cluster` | cluster id | DBSCAN (eps 8 km, minPts 3, 5-min window) over the AOI GLM ring |
| `fire-detection`, `fire-cluster`, `probsevere-object`, `satellite-pass`, `ground-station` | §4–§5 | Source adapters |

**place-weather payload:**
```json
{"place":"Home","radiusKm":20,
 "radar":{"status":"ok","maxDbzInRadius":52.5,"nearest40Km":8,"nearest50Km":14,"nearest50Km10m":12,"nearest60Km":999},
 "hail":{"status":"ok","meshMaxMmInRadius":19,"meshNearest25Km":999},
 "precip":{"status":"ok","rateMmH":25},
 "lightning":{"status":"stale","asOf":"2026-10-10T14:30:00Z","nearestKm15m":6.0,"nearestKm30m":4.0,"count15km15m":14},
 "cell":{"status":"ok","eta50Min":25,"etaCellId":"c-…","etaSpeedKmh":45,"etaBearingDeg":250},
 "smoke":{"status":"ok","hrrrPm25Max24h":38},
 "forecast":{"gfs":{"gustMax24h":21,"precip24hMm":30},"nws":{"popMax24h":70}},
 "automated":true,"notAWarning":true}
```
- **Rounding:** distances to 0.5–1 km; ETAs to 5 min.
- **Hysteresis:** the `*10m` and `*30m` fields are rolling minimums.
- **Performance:** per place, the grid cells within 100 km are precomputed and sorted by distance, so one scan per frame gives every threshold (under 5 ms for 50 places).
- **GLM limits:** the place-weather docs state that GLM's footprint over Texas is about 8–10 km and its detection efficiency is below 100%. GLM-only lightning rules should use 15 km or more, and all-clears are advisory.

### 7.3 Trigger engine additions (amend 014 now; 039 adds them if missing)
1. **`cooldownScope: record|rule`.** With `rule`, the cooldown check is `NOT EXISTS (… a.rule_id = :rule AND a.fired_at > now() - :cooldown)`, and the batch is reduced with `ROW_NUMBER() OVER (PARTITION BY rule_id ORDER BY seq) = 1`.
2. **`near: {placeId|point, radiusKm}`**, materialised into `geofence`.

Note: a tombstone counts as an exit when P(previous) held. Deleting a place can therefore fire exit rules, and the docs say so. Generators never tombstone because of staleness.

### 7.4 Rule recipes (no new engine code)

| Alert | Rule |
|---|---|
| ≥ 50 dBZ within 20 km | `source=wx-places`, `radar.nearest50Km10m <= 20`, `enter`, warning |
| "Echoes cleared" (verified only) | P = `radar.nearest50Km10m <= 20 OR radar.status != ok`, `exit`. It fires only when the previous version was near *or unknown* and the current one is ok **and** far. |
| Lightning within 15 km, with advisory all-clear | `enter` on `lightning.nearestKm15m <= 15`; all-clear `exit` on `lightning.nearestKm30m <= 15 OR lightning.status != ok` |
| Storm core within 30 min | `cell.eta50Min <= 30`, `enter` |
| Tornado probability enters county | `kind=probsevere-object`, `ProbTor >= 30`, geofence, `enter`, `cooldownScope=rule` |
| Fire near home | `kind=fire-detection`, `near{home, 20}`, `match` with `only_new` |
| Meteor pass decoded | `kind=satellite-pass`, `status == decoded`, `enter` |
| Weather feed down | Not a rule: a degraded source raises the existing 015 system event |

**Integration tests required:**
- The GLM feed stops: no exit alert fires, the section goes `stale`, and the system event fires.
- An unchanged place-weather over 30 min produces no versions and no alerts.
- An MRMS no-coverage frame does not clear the radar section.

### 7.5 Enrichment (026), briefings (025), Ask (024): prompt 046
- **Alert context.** Each geolocated alert gets `alert.context.weather`, computed deterministically from the frame cache:
  - radar maximum within 10 km, plus the nearest cell with its motion and MESH
  - GLM count within 20 km over the last 15 min
  - precip rate
  - intersecting NWS warnings
  - GFS/HRRR wind and gust at the nearest valid time
  - smoke

  Each section carries its `status`. Templates can use `{{context.weather.summary}}`. The LLM enrichment receives the same JSON.
- **Briefings.**
  - Scope gains `places` and `weatherProducts`.
  - The digest gets a "Weather at places" section: **now** (radar, lightning, alerts); **the period** (max dBZ, max MESH, lightning total, hazards that entered the radius); **next 24–72 h** (NWS gridpoints plus GFS and HRRR).
  - Stale sections are labelled stale.
- **Ask.** Tools: `sample_weather`, `weather_forecast`, `area_weather_stats`, `get_map_image`. Answers must cite run, validTime and age, and must never present no-coverage as clear.

---

## 8. Notification image attachments (v1, prompt 045)
- **Model:** `NotificationMessage.attachments: [{filename, contentType, bytes, mediaId}]`. Channel config: `attachMap: none|static`, `mapProducts` (default `["mrms-reflectivity"]` or the product that matched), `mapRadiusKm` (default 50), `maxAttachmentBytes`.
- **Rendering:**
  - One static PNG per alert at dispatch, 800 px, through `MapRenderer`.
  - It waits up to 5 s for a frame newer than the alert.
  - Stored as media with `expires_at` = 7 d and reused by every channel.
  - `satellite-pass` alerts attach the composite thumbnail instead.
- **Size control:** over a channel's cap, the image degrades to 600 px, then to the thumbnail, then to text only.

| Channel | v1 delivery |
|---|---|
| Discord | Multipart `files[0]` + embed `image.url = attachment://map.png` (cap 8 MB; uploads are about 10 MB in practice) |
| Telegram | `sendPhoto` multipart |
| ntfy | `PUT` body with a `Filename` header (needs server attachments enabled; otherwise text) |
| Pushover | `attachment` multipart, ≤ 5,242,880 bytes |
| Email | Inline CID PNG |
| Slack, Mattermost, Teams, Google Chat, Gotify, Matrix, Web Push, webhook | Text plus an in-app link (useful on the LAN or VPN). No signed URLs in v1. |

**Independent fix for 015 now:** Office 365 Incoming Webhooks were retired in Teams in May 2026. 015 must implement Teams **only** through Power Automate Workflows webhook URLs (Adaptive Cards) and reject `webhook.office.com` URLs with a clear validation error. Web Push `image` is ignored by Safari/iOS and Firefox anyway.

No public basemaps. An operator basemap URL is allowed for self-hosted tile servers only; the docs warn about the `tile.openstreetmap.org` usage policy.

---

## 9. Resource budget (v1 defaults)

| Product | Download/day | Decode CPU/day (x86) | Rolling disk |
|---|---|---|---|
| MRMS refl + precip/flag + MESH, every frame | 1.7–3.4 GB | ~10 min | ~2 GB |
| MRMS second wave (040) | ~0.5 GB | ~3 min | ~0.5 GB |
| GOES-19 CMIPC C13 | 1.0–1.1 GB | ~2 min | ~1.2 GB |
| GLM (AOI ring; files are CONUS-wide) | ~1.5 GB | ~2 min | 0.4 GB (14 d) |
| FDC / ProbSevere | 0.15 / 0.19 GB | <1 min | records only |
| GFS (5 messages × 29 steps × 4 runs) | ~0.5 GB | ~1 min | ~0.5 GB |
| HRRR (5 messages × 19 steps; 8 runs, or 4 in small) | 0.4–0.8 GB | 1–3 min | ~0.4 GB |
| Records (NWS, SPC, AWC, HMS, gridpoints) | ~0.1 GB | negligible | records only |
| SatDump (LAN) | 0 | ~2 min | 3–6 GB polar (30 d) + ~1 GB geo (3 d) |
| Derived (AOI) | — | ~10 min | — |
| **Total** | **≈ 5–8.5 GB/day** (≈ 150–250 GB/month) | **≈ 0.05–0.1 core average**; ingest bursts of 1–2 cores; rendering up to the pool size while clients animate | **≈ 10–12 GB** (quota 200 GB) |

**RAM and Postgres:**
- RAM follows §2.6.
- Postgres grows by roughly 0–100 MB/day of weather records, at 3–14 d retention by kind. Place-weather writes only on change, typically tens of versions per place per stormy day.
- Prompt 078 re-measures these figures on the bench server, including a stormy-day soak.

---

## 10. Prompt breakdown

### 10.1 Amend the unexecuted Ready prompts now
Each weather prompt still checks for these hooks and adds any that are missing.

| Prompt | Additions |
|---|---|
| 004 | `ProductSpec` slot, `kindRetention`, `DERIVED` mode, `LAN_URL` (pinned-IP connect) and `INBOX_PATH` (NOFOLLOW, per-file realpath) |
| 005 | `getRange`, `download`, `listS3`, `mbPerDay`, per-descriptor response cap |
| 006 | Hook for the frame-age rule |
| 011 / 012 | `externalId` filter; `raster-frame` payload always inline |
| 014 | `cooldownScope` (the SQL in §7.3) and `near` |
| 015 | `attachments` field; Teams via Workflows only |
| 016 | `retention.kinds` resolution order |

### 10.2 Weather v1 block: new prompts 029–046, inserted after 028 (Agent Integrations)
Only "To write" rows move, and the Ready prompts 001–028 keep their numbers. The block is a thin vertical slice first: core, then tiles, then radar, then SatDump, then client access. Each prompt is sized like the existing ones.

| # | Prompt | Scope | Depends on |
|---|---|---|---|
| **029** | `029-Raster-Products-Core.md` | • Data: `ProductSpec`/`ProductRegistry`; entities `raster_product`, `raster_grid`, `raster_frame`; `.fsg` codec (64-byte header, golden test); `FrameStore` (atomic writes, sha256); `ctx.rasters()`<br>• Records and health: `raster-frame` records (observation per frame, forecast per completed run); frame-age health; product probe<br>• Retention: per-kind retention (if missing from 016); `RasterRetentionStep` (ladder, quota, orphan sweeper with re-emit)<br>• API: `GET /v1/products`, `/frames`, `/frames/latest` with the time and frameKey conventions; `/v1/system` rasters block; AccessPolicy; OpenAPI<br>• Config: memory profiles; UpstreamClient hooks if missing<br>• Tests: synthetic fixture product | 004–008, 011, 012, 016, 017, 019 |
| **030** | `030-Media-Store-And-Image-Guard.md` | `media` entity, `MediaStore` (content-addressed), `ImageGuard` (pixel cap), `ThumbnailService` (JPEG/PNG), `ctx.media()`, `/v1/media/**`, retention linkage | 029 |
| **031** | `031-Raster-Tiles-And-Styles.md` | • Grids: `GridDef` latlon, geos (PUG/CGMS), lcc, affine<br>• Rendering: `TileRenderer` (inverse map, nearest/bilinear), `IndexMapCache`; WebMercatorQuad 256 + `@2x` PNG-8; shared transparent empty tile; `TILE_OUT_OF_RANGE`<br>• Styles: versioned style registry, LUTs; legend JSON + PNG (units, theme, specials, meaning)<br>• Endpoints: TileJSON with concrete templates; `/image`; `/loop`<br>• Caching and load: cache policy (§6.1); render queue (priority, fairness, 1.5-s wait, `RENDER_BUSY`); AOI warming<br>• Catalogue and docs: external catalogue entries (GIBS, nowCOAST) with time and zoom metadata; amend Charter (non-goal, FR-55) and Architecture (§1 rule, new §27) | 029, 030, 013 |
| **032** | `032-Radar-And-Satellite-Rasters.md` | • NetCDF-Java 5.11.0 + libaec (exclusiveContent repository, verification, DiskCache, SSRF guard, tmpdir, self-test, Architecture §2/§26 exception); `RasterDecoder` SPI<br>• `mrms` (refl, precip + flag, MESH; S3 `start-after`; u8 code scheme and sentinel test)<br>• `goes-east-abi` (CMIPC C13 on a `geos` grid)<br>• Fixtures from real samples; Source Catalog fixes (§1.4) | 029–031 |
| **033** | `033-Places-And-AOI.md` | `place` entity and `/v1/places`; the places-AOI definition and service; `near` and `cooldownScope` if missing from 014. Small and self-contained, so SatDump and ProbSevere can use the AOI. | 014, 029 |
| **034** | `034-Severe-Weather-Record-Sources.md` | `nws-alerts` (from old 034), `mrms-probsevere` (AOI bbox, tombstones), `spc-outlooks`, `awc-metars` (cache file, compliance note). Kind retention defaults. | 004–012, 033 |
| **035** | `035-Lightning-And-Fire.md` | `goes-glm` (20-s files, AOI ring, daily archive, `lightning-cluster`), `goes-fdc` (`fire-detection`, `fire-cluster`), `hms-smoke`/`hms-fire`. GLM-limits documentation. | 032, 033 |
| **036** | `036-SatDump-Station-Core.md` | • `satdump-{station}`: optional `/api` loop with ground-station and RF sessions (30-s flush); in-memory polarplot and its endpoint; inbox loop (readiness, `dataset.json`, Jackson CBOR, PNG through ImageGuard)<br>• Passes: `satellite-pass` lifecycle and join (name-normalisation fixture); thumbnails; xRIT geostationary frames via `geos` (partial → complete); dynamic SatDump products<br>• Health reasons (`finish_processing`, server_mode, `/api` drift); MCP `list_satellite_passes`/`get_satellite_pass`<br>• Ops and licence: compose profile `satdump`; operator docs (§4.0, §4.7 sync path); GPL boundary notes; pinned-version `/api` and output fixtures | 029–031, 033 |
| **037** | `037-Map-Client-Access.md` | Tile keys (`/v1/tile-keys`, `TileKeyFilter`, SSE support); the CORS header set and max-age; compose `web` profile (Caddy, `tls internal`, HTTP/2/3) and local-CA docs; frontend guide recipes (§6.10: MapLibre v6, maplibre_gl, flutter_map, Cesium, retry wrapper, resume pattern, caching, FMTC warning) | 031, 032, 019, 012 |
| **038** | `038-Model-Grids-And-Wind.md` | • GEV port (MIT headers): `.idx` parsing, range planning, cycle/step selection<br>• Models: `gfs` (5 messages), `hrrr` (`lcc`; REFC, GUST, MASSDEN, wind)<br>• Endpoints: fields `.json` + `.f32` at 1° and 0.25°+bbox; barb MVT; `wind-speed` style; run/step semantics, `RUN_REQUIRED`, `now` alias; `/v1/products/timeline` incl. `mode=seamless`; GEV web particle-layer port in the frontend guide<br>• Robustness: configurable path templates (GFSv17); field matching by variable and level; silent-format-change wiring | 029–032, 037 |
| **039** | `039-Sampling-And-Place-Weather.md` | `DERIVED` runtime (shared with 070); `SampleService` (`/sample`, `/series`, `/stats`, statuses, relative time); `wx-places` (status sections, sentinel 999, retained values, no TTL, reconciliation); `wx-areas`; `radar-cell` with centroid-v1 motion and ETA; the §7.4 integration tests; MCP `list_products`, `sample_weather`, `weather_forecast`, `area_weather_stats`, place tools (compact output) | 032–035, 038, 014, 021 |
| **040** | `040-MRMS-Second-Wave-And-Gridpoints.md` | MRMS rotation, QPE 1h/24h, FFG ratio (percent); `nws-gridpoints` per place; `spc-storm-reports` (from old 051); feeding the precip and forecast sections of place-weather | 032, 039 |
| **041–042** | (reserved) | Absorbed into 043. The table keeps continuous numbering; see 10.3. | |

On reflection, 041 and 042 should not be left empty. **The final ordered list is:**

| # | Prompt |
|---|---|
| 029 | Raster-Products-Core |
| 030 | Media-Store-And-Image-Guard |
| 031 | Raster-Tiles-And-Styles |
| 032 | Radar-And-Satellite-Rasters |
| 033 | Places-And-AOI |
| 034 | Severe-Weather-Record-Sources |
| 035 | Lightning-And-Fire |
| 036 | SatDump-Station-Core |
| 037 | Map-Client-Access |
| 038 | Model-Grids-And-Wind |
| 039 | Sampling-And-Place-Weather |
| 040 | MRMS-Second-Wave-And-Gridpoints |
| **041** | `041-SatDump-Polar-Overlays.md` (043b): GeoTIFF reader (GeoKeys, BigTIFF rejection), alpha-mask footprints, antimeridian split, Orekit approximate swath, `polarGlob`, documentation for the owner's chosen producer only, `POST /v1/ingest/{id}/media` and `/rasters` (remote Pi), optional GDAL build-arg. Depends on 030, 031, 036, 020. |
| **042** | `042-Static-Maps-And-Notification-Attachments.md`: `MapRenderer` (static PNG), `/v1/maps/static`, `attachments` and the five native channels (§8), caps and downgrade path, MCP `get_map_image` with caption. Depends on 031, 015, 039. |
| **043** | `043-Weather-Intelligence.md`: `alert.context.weather` with statuses, template variables, 026 input; briefing "Weather at places"; Ask tools and citation rules; agent cookbook additions (028 docs). Depends on 039–042, 024–026. |

In §4, §5 and §6 above, references to "043" (polar), "045" (maps) and "046" (intelligence) mean 041, 042 and 043 in this final list. "044 celestrak-gp" means the renumbered old 031, which is **045** (see 10.3).

### 10.3 Renumbering and later prompts
- **Old 029–057 become 044–072** (offset +15), unchanged except:
  - old 031 `celestrak-gp` (new 046) adds predicted `satellite-pass` versions using the Orekit library from 041;
  - old 034 Hazard-Adapters (new 049) keeps `nhc-cyclones` and `gdacs`; `nws-alerts` has moved to 034;
  - old 036 `local-adsb-*` (new 051) reuses `LAN_URL`;
  - old 042 Space-Weather (new 057) is unchanged (OVATION goes to 074);
  - old 051 (new 066) keeps `meteoalarm` and `emsc-seismic`;
  - old 057 Derived-Layers (new 072) reuses the `DERIVED` runtime from 039.

  The predicted-pass reference in §4.4 ("044") therefore means **046**.
- **Old 058 is replaced by this block.** The v1.1 and later weather prompts follow old 057:

| # | Prompt | Scope |
|---|---|---|
| 073 | `073-Weather-v1.1-Models-And-Tracking.md` | ECMWF IFS (§5.3 rules, CC BY 4.0); `forecast-area`; TITAN-lite tracking (IoU on advected polygons, Hungarian assignment, merges and splits, `storm-swath`, GLM lightning jump); model-disagreement flag; correlation annotators (dry-lightning ignition, outage × weather, airports, smoke × AQ) |
| 074 | `074-Weather-Products-Extended.md` | §5.3 product list |
| 075 | `075-SatDump-Extras.md` | Filtered DCS/EMWIN; 2.0 AngelScript hook (MIT, gated on 2.0 reaching a release or on the owner running it); `glm-local`; EMWIN alerts fallback; SatDump-compatible OMM feed from the celestrak-gp cache |
| 076 | `076-Map-Interop-And-Offline.md` | WMTS with Time (QGIS, Cesium), ATAK MOBAC, OGC API Tiles/EDR, MBTiles/PMTiles export, COG export, WorldCRS84Quad, the 512 px pyramid, Terrain-RGB data tiles (`custom` encoding per product, transparent no-data stop, `nearest`), WebP (usefulness 0.11.0, health-checked), `style.json` fragment with merge helper, `.i16`/`.png` fields, GIF, `/v1/places/{id}/pack`, signed links and image fallbacks for the remaining channels once an exposure story exists |

- **Old 059–063 become 077–081:**
  - 077 Licence & Provenance: aggregates the structured attribution.
  - 078 Adapter Doctor.
  - 079 Full Test Run: includes fixture contract tests for every weather and SatDump output.
  - 080 Runtime Verification: re-measures §9 per profile; live MRMS, GOES, GFS, HRRR and SatDump; stormy-day soak; cache tuning from `/v1/system`.
  - 081 Production Hardening: the Flyway baseline includes the raster, media and place tables, the GiST index on `raster_frame.footprint`, tile-key columns, and the `trigger_rule`/`alert` changes.

  References above to "074 runtime verification" and "079 Flyway" mean **080** and **081**.
- **Document updates in the same change:**
  - Roadmap table
  - Source-Catalog line 48 ("radar and IR are self-rendered from NOAA open data; GIBS and nowCOAST are external catalogue entries"), plus the §1.4 fixes
  - Executive-Summary line 46
  - Charter line 58 and FR-55
  - Architecture §2 (stack and the Unidata exception), §3 (packages), §7 (tables), §11 (`retention.kinds`, raster ladders), §13 (`FORESIGNAL_INBOX_DIR`, `FORESIGNAL_PROFILE`, raster settings), §14 (profiles `satdump` and `web`, memory profiles), §26 (SatDump GPL and GEV MIT rows)

### 10.4 To verify at implementation
- **Toolchain:**
  - NetCDF-Java resolves from `/all/` under `exclusiveContent`
  - libaec extracts its native library under the hardened tmpdir
- **Data facts:**
  - MRMS −999/−99 sentinels
  - HRRR MASSDEN units
  - GFSv17 SCN date and any layout change
  - The ECMWF 0.1° change date (for 073)
- **SatDump:** the composite `project` GeoTIFF output on the owner's version, and `/api` keys on the pinned version.
- **Clients:**
  - flutter_map built-in caching version and behaviour
  - maplibre_gl header support (the tile key makes this non-blocking)
  - MapLibre v6 ESM packaging in the web client build
- **Notifications:** ntfy server attachment configuration.

---

## 11. Rejected / deferred

| Item | Status | Reason |
|---|---|---|
| Adaptive MRMS cadence | Rejected | It saves about 1.5 GB/day at the cost of state and a blind spot of up to 10 minutes when storms initiate. Revisit only if 080 shows bandwidth is a problem. |
| NCEP `.latest` as a second MRMS path | Rejected | One access path is enough; S3 listing is budgetable and has history |
| `null` meaning "nothing nearby" | Rejected | It made stale data fire all-clears. Replaced by the sentinel plus section status. |
| Two SatDump sources (station and products) | Rejected | Two writers for one record key, and a join that cannot work |
| Supporting both SatDump 1.2 and 2.0 polar paths in v1 | Rejected | Double the fixtures against an alpha format. One producer path, chosen by the owner. |
| SatDump webhook plugin as ingest | Rejected | Plain text only: no paths, no NORAD ID |
| DCS/EMWIN in v1 | Deferred to 075 | Hemisphere-wide volume; needs a mandatory filter |
| `public, immutable` on every concrete URL | Rejected | Shared-cache leakage and stale styles. Content-addressed rule instead. |
| Bearer-only tile auth and signed one-path links for tiles | Rejected | Fails on maplibre_gl, Cesium and `EventSource`; breaks caching. Tile keys instead. |
| 204 as the default empty tile | Rejected | Client error tiles and retries. Shared transparent PNG instead. |
| Pre-rendered pyramids, COG/TiTiler | Rejected | CPU cost at a 2-minute cadence; heavy dependencies |
| GeoTools | Rejected | LGPL, heavy, OSGeo repository |
| aircompressor zstd | Rejected | v3 needs Java 22+; Deflater(1) is enough |
| Hand-ported Vallado SGP4 | Rejected | Licence provenance unclear and a correctness risk. Orekit (Apache-2.0) is shared with celestrak-gp. |
| Orekit before 041 | Deferred | Only approximate swaths and predicted passes need it |
| Polarplot snapshots in the media store | Rejected | File and row churn; kept in memory instead |
| WorldCRS84Quad, 512 px pyramid, Terrain-RGB, WebP, `style.json`, `.i16`/`.png` fields, GIF, disk tile cache, overviews | Deferred to 076 | Each is a separate code path. v1 clients are served by PNG-8 256/`@2x`, `/loop`, `/sample` and barb MVT. Overviews are unnecessary: nearest sampling at low zoom is cheap. |
| Signed links and image fallbacks for Slack, Teams, Gotify, Matrix, Web Push and webhook | Deferred to 076 | Need internet exposure of tiles, media and maps; no exposure story yet |
| Teams Office 365 Incoming Webhooks | Rejected | Retired May 2026; Workflows only |
| IFS, `forecast-area`, TITAN-lite, TREC nowcast, model-disagreement flag, correlation annotators | Deferred to 073 | Not needed for the v1 goals; centroid-v1 motion covers ETA |
| CONUS-wide radar cells and GLM clusters by default | Rejected | About 300 MB/day of versions; AOI by default, CONUS opt-in on the large profile |
| `flutter_map_tile_caching` in docs | Rejected | GPL-3.0 or commercial; conflicts with an MIT client |
| Proxying nowCOAST/GIBS (GEV `weather.js`) | Rejected | Would break the "no client request causes an upstream fetch" rule; both serve CORS `*` for direct use |
| RainViewer, Blitzortung, MRMS `NLDN_*`, Synoptic, Meteostat, Himawari/GK-2A/Meteosat, DWD/OPERA, NEXRAD L2, CAMS | Skipped | Licence, coverage or weight (§1.2) |
| Removing the claim that GFSv17 drops the 0.5°/1° files | Done | No evidence for it; Foresignal uses 0.25° only, with configurable templates |