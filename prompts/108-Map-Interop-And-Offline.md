# 108 — Map Interop And Offline

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/108-CR-Map-Interop-And-Offline.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/107-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md`:
  - §10.3 row "076" (this prompt; its 076 = 108)
  - §11 rows deferred to 076 (WorldCRS84Quad, 512 px pyramid, Terrain-RGB, WebP, `style.json`, `.i16`/`.png` fields, GIF, signed links and image fallbacks)
  - §6.1 (content-addressed caching, frame keys, aliases, `public` vs `private`), §6.3 (tiles, `/image`, `/loop`, render queue, `RENDER_BUSY`), §6.4 (fields), §6.7 (tile keys, CORS), §6.10 (client recipes), §8 (attachments and the channel table)
  - Translate numbers with `docs/Roadmap.md` §5: its 031 = 045, 037 = 050, 042 = 055
- Completion Reports of 045 (`GridDef`, `TileRenderer`, `IndexMapCache`, style registry and LUTs, legends, TileJSON, `/loop`, render queue), 050 (tile keys, CORS, Caddy `web` profile), 055 (`MapRenderer`, attachments), 058 (MapLibre styles and sprites), 035 (feed keys, outbound URL policy), 106 (`png-uv` encoder), 018 (channel types), 111 if present (PMTiles writer)
- Specs:
  - OGC WMTS 1.0.0 (07-057r7), including the `Dimension` element for Time
  - OGC API – Tiles Part 1 (20-057), OGC Two Dimensional Tile Matrix Set (17-083r4: `WebMercatorQuad`, `WorldCRS84Quad`)
  - OGC API – EDR 1.1 (19-086r6) with CoverageJSON
  - MBTiles 1.3, PMTiles v3 (`https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md`)
  - COG (OGC 21-026)
  - MapLibre `raster-dem` `encoding: custom` (redFactor, greenFactor, blueFactor, baseShift)
  - MOBAC custom map source XML

## Objective

Open Foresignal's raster products to GIS, TAK, 3D and offline clients without changing the core renderer:
- WMTS with Time, OGC API Tiles and EDR, and a WorldCRS84Quad and 512 px pyramid;
- data tiles that carry values instead of colours;
- WebP, style fragments, more field encodings and animated GIF;
- MBTiles, PMTiles and COG exports, and a per-place offline pack;
- image links for the channels that cannot take attachments, now that feed keys provide a safe exposure path.

The design rule still holds: **no client request ever causes an upstream fetch**.

## Tasks

### 1. Renderer extensions (`ai.foresignal.raster.render`)

- **TileMatrixSet abstraction:** `WebMercatorQuad` (existing) and `WorldCRS84Quad` (EPSG:4326, 2×1 tiles at z0, 256 px). `GridDef` inverse mapping already handles latlon output; add the 4326 tile-to-lonlat transform. URL `…/tiles/{frameKey}/{tms}/{z}/{x}/{y}.{ext}` with `tms` ∈ `WebMercatorQuad|WorldCRS84Quad`. The existing un-prefixed path stays WebMercatorQuad.
- **512 px pyramid:** `tileSize=512` renders natively at 512 (not upscaled 256). It is distinct from `@2x`: z is one less for the same detail. TileJSON advertises `tileSize: 512` variants.
- **WebP:** `com.github.usefulness:webp-imageio`.
  - Verify the current version on Maven Central; design §11 cites 0.11.0, and a 2026-10 search found only 0.8.0 on a mirror. Record the version used.
  - Check its licence and native-library bundling for linux-x86_64 and aarch64.
  - Startup health check: encode one 256×256 tile. On failure, disable WebP with a `/v1/system` reason; `.webp` then returns 406 `FORMAT_UNAVAILABLE`.
  - Lossless for categorical and indexed styles, quality 80 for `rgb` imagery.
  - `.webp` is accepted wherever `.png` is (tiles, `/image`). Same caching rules; the ETag includes the format.
- **Data tiles (Terrain-RGB style)** at `GET /v1/products/{id}/data/{frameKey}/{z}/{x}/{y}.png?encoding=custom|terrarium&tms`:
  - Encode physical values as `v = (R·65536 + G·256 + B) · scale + offset`, with per-product `scale`/`offset` chosen to cover the product range at its native precision. No-data → alpha 0 (the transparent no-data stop). Resample with `nearest` only.
  - `terrarium` is offered only for products whose range fits it.
  - TileJSON for data tiles adds `encoding` and `{redFactor, greenFactor, blueFactor, baseShift}` for MapLibre `raster-dem` `custom`, plus `units` and `nodata`.
  - The docs show client-side colouring and value readout.
- **Field encodings:** `.i16` (planar little-endian int16 u, v with `scale`/`offset` in the manifest) and tiled `.png` fields `…/fields/{frameKey}/{z}/{x}/{y}.png`, reusing the 106 `png-uv` encoder per tile with min/max in the manifest. Add both to the manifest `encodings`.
- **GIF:** `GET /v1/products/loop.gif` takes the `/loop` parameters (frames ≤ 24, width ≤ 800, delay 300–1000 ms).
  - The palette comes from the style LUT (PNG-8 palettes map directly), via the JDK ImageIO GIF writer with animation metadata.
  - Rendered through the render queue at prefetch priority; `RENDER_BUSY` applies.
  - Cached only for content-addressed frame sets (all frames `~sha8`).

### 2. WMTS 1.0.0

- `GET /v1/wmts/1.0.0/WMTSCapabilities.xml`, plus KVP `GET /v1/wmts?SERVICE=WMTS&REQUEST=GetCapabilities|GetTile`.
- **Layers:**
  - one per product × style;
  - `TileMatrixSet` links to both sets;
  - formats `image/png` (and `image/webp` when healthy);
  - `ResourceURL` template `/v1/wmts/1.0.0/{layer}/{style}/{Time}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png`.
- **Time dimension:**
  - `<Dimension><ows:Identifier>Time</ows:Identifier><Default>current</Default>`, with `<Value>` entries listing complete frame times (ISO extended, newest 200).
  - Forecast products expose `Time` values from the latest complete run, and a second dimension `RefTime` (run).
  - `current` maps to the `latest` alias (short cache). A concrete time resolves to the frame and **301-redirects to the content-addressed tile URL**, so caching stays correct.
- Capabilities are generated per request with an ETag, `max-age=60`.
- When accessed through `/v1/k/{key}/wmts/...` (tile key or feed key), capabilities embed that key prefix in every URL. QGIS and Cesium `WebMapTileServiceImageryProvider` (with `times` and `dimensions`) work without headers.
- Docs: QGIS (temporal controller), Cesium.

### 3. OGC API – Tiles and EDR (root `/v1/ogc`)

- Landing `/v1/ogc`, `/v1/ogc/conformance`, `/v1/ogc/api` (a link to the OpenAPI document), `/v1/ogc/collections`, `/v1/ogc/collections/{productId}`, `/v1/ogc/tileMatrixSets[/{id}]`.
- **Tiles:**
  - `/v1/ogc/collections/{productId}/map/tiles` (tileset list) and `/map/tiles/{tms}` (tileset metadata per 20-057);
  - `/map/tiles/{tms}/{z}/{y}/{x}?datetime=` (note the OGC row/col order). `datetime` resolves as WMTS Time, with a 301 to the content-addressed tile.
- **EDR:**
  - `/v1/ogc/collections/{productId}/position?coords=POINT(lon lat)&datetime=&parameter-name=`, and `/area?coords=POLYGON((…))&datetime=` (≤ 100,000 cells, else 413 `EDR_TOO_LARGE`).
  - `/instances` for forecast runs.
  - Output is CoverageJSON (`application/prs.coverage+json`). Position maps to `SampleService`; area is a native-grid subset with the product's units and the nodata mask.
- Collections list products only. Prompt 112 adds Features collections under the same root, so keep the landing and collection code extensible.

### 4. Exports (async jobs, `ai.foresignal.raster.export`)

- `POST /v1/products/{id}/exports` (agent) with body `{format: mbtiles|pmtiles|cog, frameKey | frameKeys[] (≤ 24), bbox | placeId+radiusKm, minZoom, maxZoom, style, tms}`.
  - Caps: ≤ 50,000 tiles per tileset and ≤ 2 GB per file.
  - Response: 202 with `{exportId}`. `GET /v1/exports/{id}` returns status, progress, files with sha256 and expiry. `GET /v1/exports/{id}/files/{name}` downloads.
  - Files live under `${FORESIGNAL_DATA_DIR}/exports/` with 48 h expiry and are included in the raster disk quota.
- **MBTiles:** `org.xerial:sqlite-jdbc` (Apache-2.0), schema 1.3 (`metadata` with attribution, bounds, format, type `overlay`, `json` for the time; `tiles` table), one file per frame.
- **PMTiles v3:**
  - If 111 hasn't added it yet, add an in-house writer in `ai.foresignal.export.pmtiles`: Hilbert tile ids, a clustered layout, a gzip-compressed directory, root and leaf directories, and the JSON metadata. 111 will reuse it.
  - A golden-file test against the spec's header layout.
- **COG:**
  - A minimal in-house classic-TIFF writer: tiled 512×512, deflate, internal overviews (factor 2 until < 512 px), with all IFDs written before tile data (COG layout), ModelPixelScale/ModelTiepoint/GeoKeyDirectory tags for EPSG:4326 or 3857, and a GDAL_NODATA tag.
  - Writes physical values (float32, or the native quantised dtype with scale/offset in `GDAL_METADATA`).
  - When the optional GDAL build-arg exists (054), a test-only validation path runs `gdalinfo` and checks the layout. The unit test verifies IFD ordering by parsing the file.
- Every export embeds attribution and licence (ECMWF "changes made" where relevant).

### 5. Style fragments and TAK

- `GET /v1/products/{id}/style-fragment.json?frameKey&style&tms`, and `GET /v1/products/style-fragment.json?products=a,b&…`.
  - Returns `{version: 8, sources: {...}, layers: [...]}` with concrete content-addressed URLs, `minzoom`/`maxzoom`, bounds and attribution, plus legend links in `metadata`.
  - The layer ids are namespaced `foresignal-{product}`.
  - The frontend guide includes a 30-line merge helper (JS and Dart) that inserts fragments below a named layer of the client's basemap style. The server never fetches a basemap style.
- **ATAK MOBAC:** `GET /v1/products/{id}/mobac.xml?style` (read; meant to be used through a tile-key path).
  - Returns `<customMapSource>` with name, minZoom, maxZoom (native), `tileType` png, `tileUpdate` `None`, and a URL template using `{$z}/{$x}/{$y}` on the `latest` alias with the caller's key prefix.
  - Docs explain ATAK caching and the weekly key rotation (050).

### 6. Place offline pack

- `POST /v1/places/{id}/pack` (agent) with body `{products[] (default: radar, IR, the place's forecast products), frames (≤ 12), radiusKm (default 150), maxZoom (≤ 8)}` → 202 export job.
- The ZIP contains:
  - `manifest.json` (sha256 per file, generated time, frame keys, attribution);
  - the `place-weather` record JSON;
  - the forecast series JSON (`/series`);
  - nearby active alerts as GeoJSON;
  - legends as JSON;
  - a `loop.gif`;
  - a PMTiles file per product (latest frame, z ≤ maxZoom).
- Cap 50 MB (413 `PACK_TOO_LARGE`; suggest a smaller radius). The pack is cached 10 min by its input hash. 111 adds generic vector packs.

### 7. Image links for non-attachment channels

- Setting `notifications.imageLinks` (default **false**) requires `publicBaseUrl` with https (else 400 `IMAGE_LINKS_REQUIRE_HTTPS`).
- When on, the 055 map attachment also gets a URL `{publicBaseUrl}/v1/k/{key}/media/{mediaId}` where `key` is a single-path feed key (035) bound to that media path, read-only, with expiry equal to the media expiry (7 d). Feed keys are created per media item and revoked when the media expires.
- **Channel rendering:**
  - Slack/Mattermost: block `image` with `image_url`;
  - Teams (Workflows Adaptive Card): `Image` element;
  - Google Chat: card `image`;
  - Gotify: markdown `![map](url)` with `client::display` `contentType: text/markdown`;
  - Web Push: `image` (Chromium only);
  - webhook: `attachments[].url`;
  - **Matrix** instead uploads via `/_matrix/media/v3/upload` and sends `m.image`, so no link exposure is needed.
- Docs: what is exposed, rotation, and the "do not enable on a LAN-only instance" note.

### 8. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - every new operation with `x-foresignal-scope`: WMTS, OGC landing/collections/tiles/EDR, data tiles, `.webp`, `tms` param, `tileSize`, `.i16`/tiled `.png` fields, `loop.gif`, exports, `style-fragment`, `mobac.xml`, place pack;
  - media types `application/vnd.sqlite3`, `application/vnd.pmtiles`, `image/tiff; application=geotiff; profile=cloud-optimized`, `image/webp`, `image/gif`, `application/prs.coverage+json`, `application/xml`;
  - problem codes `FORMAT_UNAVAILABLE` (406), `EDR_TOO_LARGE` (413), `PACK_TOO_LARGE` (413), `UNKNOWN_EXPORT` (404), `IMAGE_LINKS_REQUIRE_HTTPS` (400);
  - regenerate Postman with BODIES for `createProductExport` and `createPlacePack`.
- AccessPolicy: all new GETs → READ; exports and pack POSTs → AGENT; `notifications.imageLinks` → ADMIN. Tile and feed keys must permit `/v1/wmts/`, `/v1/ogc/` and `/v1/products/` prefixes when the key's prefixes include them.
- CORS: expose `Link` and `Location` (already) for 301 time redirects.
- Frontend guide: QGIS WMTS, Cesium WMTS with time, OGC API clients, MapLibre `raster-dem` custom data tiles, style-fragment merge, ATAK MOBAC, offline packs.
- `docs/Architecture.md` §27 (raster) appendix: TileMatrixSets and exports.
- `THIRD_PARTY_NOTICES.md`: webp-imageio (and libwebp BSD) and sqlite-jdbc.

## Tests to write (do not run)

- `WorldCrs84QuadTest` (tile ↔ lon/lat, z0 two tiles), `Tile512Test` (native 512 rendering matches four 256 tiles).
- `WebpHealthCheckTest` (disabled path → 406), `DataTileEncoderTest` (round trip within quantisation, no-data alpha 0, nearest only), `I16FieldEncoderTest`, `GifLoopTest` (frame count, delays, palette).
- `WmtsCapabilitiesTest`: XML validity against the bundled WMTS schema subset, Time values, key-prefixed URLs, a concrete Time → 301 to the content-addressed tile.
- `OgcApiTilesIT` (landing, conformance, tileset metadata, row/col order) and `EdrIT` (position → CoverageJSON matches `/sample`; area size cap).
- `MbtilesExportIT`, `PmtilesWriterTest` (golden header and directory), `CogWriterTest` (IFD-before-data layout, GeoKeys, overviews, nodata).
- `StyleFragmentTest` (concrete URLs, namespaced ids), `MobacXmlTest`.
- `PlacePackIT` (manifest hashes, size cap 413, cache by input hash).
- `ImageLinksIT`: off by default; https required; a feed key scoped to one media path; Slack/Teams/Gotify payload shapes; Matrix upload path; key revoked on media expiry.

## Acceptance criteria

- QGIS-style WMTS clients can load a product with Time through a tile-key URL. Concrete times redirect to content-addressed, immutable tiles.
- OGC API Tiles and EDR position/area work for products, and CoverageJSON values match `/v1/sample`.
- MBTiles, PMTiles and COG exports and the place pack complete as async jobs within their caps.
- WebP is health-checked and degrades to 406. Data tiles round-trip values.
- Image links are off by default, require https, and expose exactly one media path per key.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `108: Map Interop And Offline`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/108-CR-Map-Interop-And-Offline.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `108: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
