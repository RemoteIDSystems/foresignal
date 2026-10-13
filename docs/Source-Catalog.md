# Foresignal — Source Catalog

The working inventory of upstreams Foresignal will integrate, merged from God's Eye View (GEV, MIT, so code is **portable**) and ShadowBroker (SB, AGPL, so it is **reference only**). Velocity (VEL) and World Monitor (WM), both AGPL, are reference only. Their findings are merged below.

Reference paths: GEV = `~/Documents/GitLab/gods-eye-view`, SB = `~/Documents/GitLab/Shadowbroker/backend`.

**Defaults** are starting values for the descriptor (`defaultInterval` / `defaultBudget perMinute/perDay`). Each adapter prompt confirms them against the upstream's published policy.

## First source (prompt 011)

| Source id | Kind | Upstream | Protocol | Auth | Default interval · budget | Reference | Notes |
|---|---|---|---|---|---|---|---|
| `usgs-earthquakes` | earthquake | `earthquake.usgs.gov/earthquakes/feed/v1.0/summary/{feed}.geojson`, param `feed` ∈ all/1.0/2.5/4.5/significant × hour/day/week/month (20 feeds, default `all_day`) | REST GeoJSON | none | 60 s · 4/min, 2000/day | GEV `src/layers/earthquakes/records.js` (reference only; nothing ported) | Tier STANDARD, policy ALL (keyframe none), TTL none, ttlAnchor RECEIVED, observedAtMode UPSTREAM (`observedAt` = `updated`; `eventTime` = `time`). significantFields all, volatileFields `updated`: a revision (mag/status/updated) is a SIGNIFICANT version, an `updated`-only change is a touch. Retention: the `event` class default (1825 d, Conventions "Analytic retention"). License US-PD (descriptor `licence`). **Verified 2026-10:** no `ETag`, only `Last-Modified` (generation time) and `cache-control: public, max-age=60`; `If-Modified-Since` returns 304. `metadata.api` 2.7.0; `tz` always null. `depth` (`coordinates[2]`, km) is a payload field, never geometry Z. |

## Core sources (prompts 029–035)

Roadmap prompts: aircraft → 029, `aisstream` → 030, `celestrak-gp`/`launch-library` → 031, `nasa-firms` → 032, `gdelt-events` → 033, `nhc-cyclones`/`gdacs` → 034, `ioda-outages` → 035.

**`nws-alerts` is not in this group any more.** It shipped in prompt 047 (see "Severe weather record sources" below); prompt 067 (Hazard-Adapters) must not re-implement it.

| Source id | Kind | Upstream | Protocol | Auth | Default interval · budget | Reference | Notes |
|---|---|---|---|---|---|---|---|
| `adsblol-mil` | aircraft | `api.adsb.lol/v2/mil` | REST JSON (readsb) | none | 30 s · 4/min, 3000/day | GEV `server/providers/aircraft/adsb-lol.js`, `src/data/adsbLolFallback.js` | Honour `Retry-After` (clamp 5–120 s). ODbL. TTL 5 min. **VEL reports adsb.lol returns HTTP 451 to non-browser User-Agents.** Foresignal never spoofs. If our identifiable UA gets 451 during runtime verification (prompt 062), mark the source failing and pursue adsb.lol's API-key/feeder programme. Do not work around it. |
| `adsblol-region-*` (family) | aircraft | `api.adsb.lol/v2/lat/{lat}/lon/{lon}/dist/{nm}` | REST JSON | none | 60 s · 2/min, 1500/day per region | GEV `opensky.js` fallback; SB regions (6 centres) | Family: one source per region (`na`, `eu`, `asia`, `oceania`, `africa`, `sa`). Bulk endpoint has returned HTTP 451 before, so expect failures and keep OpenSky as an alternative. VEL: the planet-radius form `/v2/point/0/0/20000` returns about 12k aircraft in one call, and `/v2/all-with-pos` is gone (404). Consider one global source instead of six regions (fewer requests), and decide in prompt 029. |
| `opensky-states` | aircraft | `opensky-network.org/api/states/all?extended=1`; OAuth token at `auth.opensky-network.org/.../token` | REST + OAuth2 client_credentials | clientId, clientSecret (optional; anonymous mode is lower-rate) | 300 s · 1/min, 900/day | GEV `server/providers/aircraft/opensky.js` (credit governor) | ~4000 credits/day; `/states/all` costs 4. Honour `X-Rate-Limit-Retry-After-Seconds`. Positional state arrays (idx 0 icao24, 1 callsign, 5 lon, 6 lat, 7 baro alt m, 9 vel m/s, 10 track). Non-commercial terms. VEL (2026): **basic auth is dead since Mar 2026**; the anonymous budget is 400 credits/day per IP; cost is 1 credit for 2×2°, 2 for 10×10°, 4 for the world. Read `x-rate-limit-remaining` and keep a reserve. |
| `aisstream` | vessel | `wss://stream.aisstream.io/v0/stream`; subscribe `{APIKey, BoundingBoxes, FilterMessageTypes}` | WebSocket (streaming) | apiKey | streaming · 6 connects/hour | GEV `server/providers/vessels/ais-live.js`, `ais-store.js`, `src/data/aisStreamAdapter.js`, `aisWatchdog.js` | One connection per key. Reconnect ladder 5 s → 15 s → 60 s → 300 s → 900 s; if the key is rejected, probe hourly. Silence watchdog 120 s. Param `boundingBoxes` (default world), message types PositionReport, StandardClassBPositionReport, ShipStaticData. Merge static data into vessel payload. TTL 30 min. Sentinels: lat 91 / lon 181, SOG 102.3, heading 511. |
| `celestrak-gp` | satellite | `celestrak.org/NORAD/elements/gp.php?GROUP={g}&FORMAT=json` | REST JSON | none | 2 h min · 1/min, 30/day | GEV `server/providers/space/celestrak.js`, `src/data/spaceProviderRequests.js`; SB fair-use notes | **Fair use:** ≤ 1 fetch per group per 2 h, conditional GET, no concurrency, identifiable User-Agent. Param `groups` (default stations, visual, gps-ops, galileo, geo, starlink?). Record = GP element set (no geometry; clients propagate with SGP4). `zeroResultsNormal` on 304. VEL: a repeat pull inside the 2 h publish window returns **HTTP 403 with body "GP data has not updated since your last successful download"**. Treat it as not-modified, not as an error or a backoff. VEL found `FORMAT=json` returning 0 satellites while SB used JSON successfully, so verify the format live and fall back to `FORMAT=tle` (3-line) parsing if needed. |
| `launch-library` | launch | `ll.thespacedevs.com/2.3.0/launches/?net__gte&net__lte&limit=100&mode=detailed` | REST JSON | optional token | 15 min · 1/min, 15/hour equiv (anonymous ≈ 15/h → perDay 300) | GEV `server/providers/space/launch-library.js` | Point geometry from pad. |
| `nasa-firms` | fire | `firms.modaps.eosdis.nasa.gov/api/area/csv/{KEY}/{SOURCE}/world/1` for VIIRS_NOAA20_NRT, VIIRS_NOAA21_NRT, VIIRS_SNPP_NRT, MODIS_NRT | REST CSV | mapKey | 30 min · 4/min, 400/day | GEV `server/providers/firms.js`, `src/data/firmsCsv.js`, `firmsAdapt.js` | Sequential per satellite. Quota 5000 transactions / 10 min / key. External id = satellite+date+time+lat+lon. TTL 24 h. |
| `gdelt-events` | conflict-event | `storage.googleapis.com/data.gdeltproject.org/gdeltv2/lastupdate.txt` → `{ts}.export.CSV.zip` | ZIP of TSV | none | 15 min · 4/min, 400/day | SB `services/geopolitics.py` (reference only) | **Use the `storage.googleapis.com/data.gdeltproject.org/...` URL form**, because the CNAME's certificate doesn't cover the hostname. Fetch only the newest 15-min file each run (track the last file in adapter state). Filter CAMEO root 13–20 by default (param). Columns: [0] GlobalEventID, [1] date, [26] EventCode, [30] Goldstein, [31] NumMentions, [33] NumArticles, [34] AvgTone, [56]/[57] lat/lon, [60] SourceURL. CC BY. TTL 24 h. |
| `nhc-cyclones` | cyclone | `www.nhc.noaa.gov/CurrentStorms.json` (+ NHC MapServer layers for cone/track later) | REST JSON | none | 10 min · 2/min, 300/day | GEV `server/providers/cyclones.js` | Zero results are normal out of season. |
| `gdacs` | disaster | `www.gdacs.org/gdacsapi/api/events/geteventlist/MAP` | REST GeoJSON | none | 15 min · 2/min, 200/day | WM (to confirm in survey) | Multi-hazard (EQ, TC, FL, VO, DR, WF), alert level green/orange/red. |
| `ioda-outages` | outage | `api.ioda.inetintel.cc.gatech.edu/v2/outages/alerts?from&until&limit=500` | REST JSON | none | 5 min · 2/min, 600/day | SB `services/fetchers/infrastructure.py` | Use `bgp` and `ping-slash24` datasources. Region/country entities with no coordinates: geometry from a bundled country-centroid table (no Nominatim calls). |

## God's Eye View ports (prompts 036–040; MIT, portable code)

Roadmap prompts: `local-adsb-*` → 036, `transit-*` → 037, `gbfs-*` → 038, `cctv-*` → 039, `nifc-perimeters`/`radio-browser`/`nhc-cyclone-cones` → 040.

| Source id | Kind | Upstream | Protocol | Auth | Reference (GEV) | Notes |
|---|---|---|---|---|---|---|
| `local-adsb-*` (family, operator-defined) | aircraft | dump1090/readsb `aircraft.json` on the operator's LAN | REST JSON | none | `server/providers/local-receivers.js` | **Exception to the static-feed-list rule:** URL is an adapter param restricted to loopback/RFC1918/`.local` hosts. 1–5 s interval. |
| `transit-*` (family) | transit-vehicle | GTFS-RT VehiclePositions: MBTA, CapMetro, Metro Transit, HSL, OVapi, Entur, TransLink SEQ | REST protobuf | none (Entur `ET-Client-Name` header, OVapi needs a User-Agent) | `src/data/transitFeeds.js`, `gtfsRealtime.js`, `src/sources/transitService.js` | Conditional GET. 15 s min interval. TTL 2 min. Needs the `gtfs-realtime-bindings` dependency. |
| `gbfs-*` (family) | bikeshare-station | GBFS `station_information` + `station_status` for allowlisted systems | REST JSON | none | `server/providers/gbfs.js`, `src/data/gbfsSource.js`, `src/layers/bikeshare/registry.js` | 60 s. Reject redirects. |
| `cctv-*` (family) | camera | Catalogues: TfL JamCam, Caltrans d1–d12, Ontario 511, Digitraffic weathercam, DriveBC, NSW, Calgary, Vegvesen, DelDOT, TxDOT, Austin, Tallinn/Warendorf packs | REST JSON/GeoJSON | optional TfL appKey | `server/providers/cctv.js`, `cctv/{sources,constants,catalog,normalize}.js`, `config/cctv_sources.*.json` | Catalogue only (location, direction, image/stream URL); **frames are never stored**, clients fetch them. 15 min interval. Some hosts need specific `Referer`/`Origin` headers. |
| `nifc-perimeters` | fire-perimeter | WFIGS Interagency Perimeters Current (ArcGIS FeatureServer, GeoJSON, offset paging ≤ 5 pages) | REST GeoJSON | none | `server/providers/firePerimeters.js`, `src/layers/perimeters/records.js` | 5 min. Polygon geometry. |
| `radio-browser` | radio-station | `all.api.radio-browser.info/json/servers` → mirror `/json/stations/search?has_geo_info=true` | REST JSON | none | `server/providers/radio/catalog.js`, `src/sources/radioBrowser.js` | 6 h. Mirror discovery and failover. |
| `nhc-cyclone-cones` | cyclone-track | NHC tropical MapServer layers (cone, track, points) | ArcGIS REST | none | `server/providers/cyclones.js` | Extends `nhc-cyclones`. |

**Raster products (prompts 042–052; Weather-Imagery-Design §5).** MRMS, GOES, GFS and HRRR (later ECMWF) are raster data, not records: enabled, budgeted sources store native-grid frames, and Foresignal renders them itself into tiles, images, loops and legends under `/v1/products` (prompt 045). Each complete frame or forecast run is one `raster-frame` record version. GIBS and nowCOAST layers are `kind=external` catalog entries of the DERIVED source `external-catalogue`: listed by `/v1/products?kind=external` with their tile templates, fetched by clients directly, never proxied.

### Raster product sources (prompt 046)

| Source id | Kind | Upstream | Protocol | Auth | Default interval · budget | Notes |
|---|---|---|---|---|---|---|
| `mrms` | raster-frame | `noaa-mrms-pds` (AWS Open Data), prefix `CONUS/{Product}/{YYYYMMDD}/`: `MergedReflectivityQCComposite_00.50` → `mrms-reflectivity` (u8), `PrecipRate_00.00` + `PrecipFlag_00.00` → `mrms-precip-rate` (u16, two bands), `MESH_Max_60min_00.50` → `mrms-mesh-60m` (u8) | S3 ListObjectsV2 (`start-after`) + GET of `.grib2.gz` (GRIB2 template 5.41) | none | 2 min (min 2 min) · 30/min, 12,000/day, **4,000 MB/day**; `maxResponseBytes` 8 MB | Tier STANDARD, policy ALL, TTL none, observedAtMode UPSTREAM (`valid_time` = the key's timestamp). Kinds `[raster-frame]`, `kindRetention{raster-frame: 7d}`, `zeroResultsNormal`. Every frame, no adaptive cadence, one access path (no NCEP `.latest`), at most 15 new frames per product per run, nothing older than 6 h. License US-PD (descriptor `licence`); terms https://registry.opendata.aws/noaa-mrms-pds/. **Verified 2026-10-12** against fixtures: grid 7000 × 3500 at 0.01°, first point 54.995° N, 230.005° E; sentinels reflectivity −999 (no coverage) and −99 (no echo), PrecipRate/PrecipFlag/MESH −3 (no coverage), MESH −1 (no hail). Ladder: every frame for 6 h, then 10-min frames to 48 h. Prompt 053 adds four products to this source (next section); the measured total of all seven at their defaults is about 2.5 GB/day, so `mbPerDay` stays 4,000. |
| `goes-east-abi` | raster-frame | `noaa-goes19`, prefix `ABI-L2-CMIPC/{YYYY}/{DOY}/{HH}/OR_ABI-L2-CMIPC-M`, keys `OR_ABI-L2-CMIPC-M\dC13_G19_s{start}_…` → `goes-east-ir` (u16, scale 0.01 K, offset 150 K) | S3 ListObjectsV2 (`start-after`) + GET of NetCDF4 | none | 5 min · 10/min, 2,000/day, **2,500 MB/day**; `maxResponseBytes` 8 MB | Tier STANDARD, policy ALL, TTL none, observedAtMode UPSTREAM (`valid_time` = `obs_start` = the key's `s` time; `obs_end` from `time_coverage_end`). Variable `CMI` (enhanced; `DQF` > 1 is missing) on the `goes_imager_projection` fixed grid, 2500 × 1500. Scan mode never hard-coded. License US-PD (descriptor `licence`); terms https://registry.opendata.aws/noaa-goes/. **Verified 2026-10-12:** files 3.7–3.9 MB. Ladder: 5-min frames for 24 h, then 30-min to 72 h. |

### Severe weather record sources (prompt 047)

All four are POLLING, tier STANDARD, policy ALL, observedAtMode UPSTREAM, licence US-PD, and tombstone only from a complete, successful response (Adapter Guide §7). Retention: `nws-alert` is the `event` class (1825 d, Conventions "Analytic retention", which overrides the design's 30 d); the other three follow Weather-Imagery-Design §2.4.

| Source id | Kind | Upstream | Protocol | Default interval · budget | Notes |
|---|---|---|---|---|---|
| `nws-alerts` | nws-alert | `https://api.weather.gov/alerts/active?status=actual` (+ `&area=` from param `area`), `Accept: application/geo+json` | REST GeoJSON | 60 s (min 30 s) · 2/min, 1,600/day | TTL none, ttlAnchor RECEIVED; `observedAt` = `sent`, `eventTime` = `onset ?? effective`, `validFrom` = `onset`, `validUntil` = the later of `expires` and `ends`. Params `area` (state/marine codes, empty = national) and `minSeverity`. titleField `headline`; embed `headline, description, instruction`; search `event, headline, areaDesc`; `zeroResultsNormal`. Geometry: polygon, else the `ref_admin2` county union (counties from the SAME codes, simplified to 0.005°, `geometrySource: county-union`), else none. Absent ids (per area) and ids superseded by `Update`/`Cancel` references are tombstoned. Terms https://www.weather.gov/disclaimer. **Verified 2026-10:** the national feed is ≈ 2.6 MB raw / 207 KB gzip when busy (0.94 MB, 217 alerts on 2026-10-13); `If-None-Match` is not honored (a repeat answers 200), so the fetch log's `body_sha256` is the change marker; the API asks for a contact in the User-Agent (`server.contactEmail`). On 2026-10-13, 46 of 217 active alerts had `expires` in the past while `ends` was ahead, and 40 had `onset` after `expires`, hence `validUntil` = max(`expires`, `ends`). |
| `mrms-probsevere` | probsevere-object | `noaa-mrms-pds/ProbSevere/{YYYYMMDD}/MRMS_PROBSEVERE_{YYYYMMDD}_{HHMMSS}.json` | S3 ListObjectsV2 (`start-after`) + GET of the newest file only | 2 min · **4/min, 1,600/day**, 300 MB/day | TTL none; `observedAt` = header `validTime`; `eventTime` null. Kept when the polygon intersects the places-AOI envelope; re-filtered when the AOI changes. titleField `label`; volatile `muCape, mlCape, ebShearKt, srh01km, flashRate`; payload carries `automated: true, notAWarning: true`. Retention 3 d. A list plus a download every 2 min is 1,440 requests a day: the design's 800/day could not cover it. **Verified 2026-10-12/13:** 0.25–0.27 MB per file, 719 files on a full day, every feature property a string. |
| `spc-outlooks` | spc-outlook | `https://www.spc.noaa.gov/products/outlook/day{1,2}otlk_{cat,torn,wind,hail,sigtorn,sigwind,sighail}.lyr.geojson`, `day3otlk_{cat,prob,sigprob}.lyr.geojson` | 17 conditional GETs | 15 min · 20/min, 2,000/day | TTL none; `externalId` = `day{d}:{type}:{LABEL}`; `observedAt` = ISSUE, `eventTime` = VALID, `validUntil` = EXPIRE (`yyyyMMddHHmm` UTC); `dn` is SPC's rank (MDT = 6). titleField `title`. Retention 14 d. Per-file completeness: labels absent from a 200 are tombstoned for that day and type; a 304 changes nothing; a 404 degrades health ("SPC layer {file} missing"). **Verified 2026-10-12:** ETag and Last-Modified on every layer; day 3 has no torn/wind/hail layers (404); an empty layer holds one placeholder feature with an empty GeometryCollection; day-3 `sigprob` still carried a March 2026 polygon, so expired features are skipped. Moved here from prompt 089. |
| `awc-metars` | metar | `https://aviationweather.gov/data/cache/metars.cache.csv.gz` | conditional GET of a gzip CSV | 5 min (min 1 min) · 2/min, 400/day, 100 MB/day | TTL 3 h, ttlAnchor OBSERVED (an old report must not read as current); `externalId` = station id; `observedAt` = `eventTime` = `observation_time`. Params `scope` (`aoi` default, `conus`, `global`) and `stations` (allowlist, overrides scope). titleField `stationId`. Retention 7 d. Nothing is tombstoned. AWC asks for at most 1 request/min per thread and 100/min overall (descriptor description). **Verified 2026-10-13:** ETag, Last-Modified and `max-age=30`; served as `application/octet-stream` without `Content-Encoding` (the adapter inflates it); 5,046 stations, header first (no preamble); a VRB wind appears as `wind_dir_degrees = 0`, so variability is read from the raw report; visibility is `10+` or `6+`. Prompt 087 reuses this source. |

### Lightning and fire sources (prompt 048)

All four are POLLING, tier STANDARD, policy ALL, observedAtMode UPSTREAM, `licence` US-PD, and tombstone only after input that was actually processed: a stalled feed freezes their records and shows the source `degraded` instead of clearing them (Weather-Imagery-Design §7.1, "stale is not clear"). Retention follows Weather-Imagery-Design §2.4 (`lightning-cluster` 3 d; `fire-detection`, `fire-cluster`, `smoke-polygon` 7 d). The Conventions table lists "GLM lightning" and "FIRMS pixels" as `bulk-event` (90 d), but only `event` and `series` windows override a shorter adapter window, so the §2.4 values stand.

| Source id | Kind | Upstream | Protocol | Default interval · budget | Notes |
|---|---|---|---|---|---|
| `goes-glm` | lightning-cluster | `noaa-goes19`, prefix `GLM-L2-LCFA/{YYYY}/{DOY}/{HH}/`, keys `OR_GLM-L2-LCFA_G19_s{start}_e{end}_c{created}.nc` (one 20-s granule each) | S3 ListObjectsV2 (`start-after`) + GET of NetCDF4 | 30 s (min 30 s) · 10/min, 10,000/day, 3,000 MB/day; `maxResponseBytes` 4 MB | **Flashes are not records:** each granule's quality-0 flashes (`flash_lat`, `flash_lon`, `flash_time_offset_of_first_event` + `product_time`, `flash_energy`, `flash_quality_flag == 0`) go to the flash store: the daily archive `rasters/glm/G19/{yyyymmdd}.bin` (16 B per flash, full field of view, 14 d) and the 60-minute in-memory ring of flashes inside the places AOI (CONUS opt-in in the large profile, `foresignal.raster.glm-ring-scope`). Records: `lightning-cluster` (DBSCAN 8 km / 3 flashes / 5 min, identity within 15 km, `lc-{ulid}`, centroid to 0.01°, quantized payload, `automated` and `notAWarning`), TTL none, `titleField` `label`, `zeroResultsNormal`. `observedAt` = newest member flash, `eventTime` = first flash. Tombstoned after 10 minutes of processed data without members; nothing is tombstoned while no file is processed. Every new granule in key order, at most 4 a run, nothing that started more than 60 minutes ago. Health: degraded when the newest processed file is older than 2 minutes. **Budget arithmetic:** 2,880 listings + 4,320 granules = 7,200 requests a day (plus 24 hour-rollover listings) under 10,000; 2 listings + 3 granules = 5 a minute under 10; 4,320 × 0.25–0.43 MB ≈ 1.1–1.9 GB a day under 3,000 MB. **Verified 2026-10-12/13:** 180 granules an hour, 255,210–432,989 bytes on 2026-10-12; an active granule held 450 flashes, 420 with flag 0. Terms https://registry.opendata.aws/noaa-goes/. |
| `goes-fdc` | fire-detection, fire-cluster | `noaa-goes19`, prefix `ABI-L2-FDCC/{YYYY}/{DOY}/{HH}/`, keys `OR_ABI-L2-FDCC-M6_G19_s…_e…_c….nc` (one CONUS scan per 5 min) | S3 ListObjectsV2 (`start-after`) + GET of the newest NetCDF4 file only | 5 min · **4/min, 800/day**, 300 MB/day; `maxResponseBytes` 4 MB | Param `scope` = `aoi` (default, the places-AOI envelope) or `conus`. `fire-detection` per pixel with mask 10–13 or 30–33: `externalId` `g19:{col}:{row}`, Point at the pixel center (`goes_imager_projection` fixed grid), `observedAt` = scan start, `eventTime` = first sighting (adapter state); payload `maskCode`, `confidence`, `temporallyFiltered`, `frpMw` and `areaKm2` (2 significant figures; `Area` is m² upstream), `tempK` (10 K), `satellite`, `method`, `label`. `fire-cluster`: 8-connected components after a 1-pixel dilation, identity by pixel overlap (`fc-{ulid}`), hull buffered 1 km to 0.01°. Both tombstoned after 3 *processed* scans without them; a failed download or unreadable file is not a scan. Health: degraded when the newest processed scan is older than 15 minutes. **Budget arithmetic:** 288 listings + 288 downloads + 24 hour-rollover listings ≈ 600 a day leaves no headroom under the design's 600, hence 800; at most 3 requests a run. 288 × 0.52 MB ≈ 150 MB a day. **Verified 2026-10-12/13:** 515,679–524,009 bytes per file; the 21:56Z scan held 25 detections, two inside the Texas box. |
| `hms-smoke` | smoke-polygon | `https://satepsanone.nesdis.noaa.gov/pub/FIRE/web/HMS/Smoke_Polygons/KML/{YYYY}/{MM}/hms_smoke{YYYYMMDD}.kml` | conditional GET of a daily KML (hardened XML: no DTD, no external entities) | 30 min · 4/min, 200/day; `maxResponseBytes` 16 MB | Today's file (UTC) and, during the first 6 h after midnight, yesterday's; a 404 for today before the first analysis is normal. TTL 24 h, ttlAnchor OBSERVED; `eventTime` = Start, `observedAt` = End; density from the style URL (`#Smoke_Light_style`, `_Medium_`, `_Heavy_`) or ExtendedData; `externalId` = sha256(date\|start\|end\|density\|satellite\|centroid 0.1°)[:16]. Ids absent from a complete file are tombstoned per file day. `titleField` `label`. **Budget arithmetic:** 48 runs × 1–2 files = 48–72 requests a day. **Verified 2026-10-13:** ETag and Last-Modified present; the 2026-10-12 file is 118 KB with 67 placemarks (one `Polygon` each, CDATA description `Start Time: 2026285 1200UTC<br>End Time: …<br>Density: Light<br>Satellite: GOES-WEST`, no ExtendedData); today's file answered 404 at 00:38 UTC. Terms https://www.ospo.noaa.gov/Products/land/hms.html. |
| `hms-fire` | fire-detection | `https://satepsanone.nesdis.noaa.gov/pub/FIRE/web/HMS/Fire_Points/Text/{YYYY}/{MM}/hms_fire{YYYYMMDD}.txt` | conditional GET of a daily CSV | 30 min · 4/min, 200/day; `maxResponseBytes` 16 MB | Same day logic as `hms-smoke`. CSV header `Lon, Lat, YearDay, Time, Satellite, Method, Ecosystem, FRP`, every field padded with spaces; columns by name. `externalId` `hms:{yearDay}:{hhmm}:{lat 4dp}:{lon 4dp}`; `observedAt` = `eventTime` = YearDay + Time (UTC); payload `satellite`, `method`, `ecosystem`, `frpMw` (2 significant figures, left out for −999), `source: "HMS"`. TTL 24 h anchored on `observedAt`; never tombstoned. Shares `fire-detection` with `goes-fdc`, so one "fire near home" rule covers both. **Verified 2026-10-13:** 1.6 MB, 16,777 points on 2026-10-12, 55 with FRP −999. Terms https://www.ospo.noaa.gov/Products/land/hms.html. |

### SatDump ground station (prompt 049)

The operator's own station, LAN only: SatDump is a separate, operator-supplied GPL program, and Foresignal reads only its HTTP `/api` and its output files (Weather-Imagery-Design §4, Architecture §26).

| Source id | Kind | Upstream | Protocol | Default interval · budget | Notes |
|---|---|---|---|---|---|
| `satdump-{station}` (family, one per name in `FORESIGNAL_SATDUMP_STATIONS`) | ground-station, satellite-pass, raster-frame | SatDump legacy CLI web server (`apiUrl`, `LAN_URL`: `/api`, `/polarplot.jpeg`) and SatDump's output directory (`inboxPath`, `INBOX_PATH` under `FORESIGNAL_INBOX_DIR`) | STREAMING: an `/api` loop (optional) and an inbox scan loop | `/api` every 5 s (2–30 s), scan every 30 s (5 s–10 min) · 60/min, 50,000/day, LAN only; each inbox scan is one budget unit, each `/polarplot.jpeg` one request | Tier STANDARD, policy ALL, TTL none, observedAtMode UPSTREAM: `ground-station` records carry the time the `/api` response was read; passes carry SatDump's times (`eventTime` = AOS, `observedAt` = the latest status time); frames carry the xRIT folder time. `kindRetention{ground-station: 30d, satellite-pass: 365d, raster-frame: 7d}`; titleField `title`; `zeroResultsNormal`; descriptor `licence` `operator-owned`. At least one of `apiUrl` and `inboxPath`; `location` is `lat,lon[,altM]` or a place id; `satNames` overrides the name table (`satdump/sat-names.json`, NORAD numbers verified against CelesTrak 2026-10-12). Passes: `{station}:{satKey}:{aosEpochMinute}`, `receiving → recorded → decoded` (or `failed` 20 min after LOS with lock and no products); products join the RF session whose window [first lock − 5 min, LOS + 5 min] holds the `dataset.json` timestamp. xRIT: dynamic products `satdump-{station}-{satKey}-{region}-{channel}`, ladder 72 h, `partial` → complete after 120 s quiet or the next folder. `/fft.jpeg` and `/schedule.jpeg` are never requested. **Verified 2026-10** against the local SatDump checkout (2.0.0-alpha, import of commit `1aedd602`): the `/api` shapes of `legacy live` and `legacy autotrack`, the `object_tracker` keys, `dataset.json`, `product.cbor` and the xRIT folder layout; the `/api` fixtures are hand-written from those keys until a capture from the owner's pinned version replaces them. |

### Forecast model sources (prompt 051)

Both are POLLING product sources: tier STANDARD, policy ALL, TTL none, observedAtMode UPSTREAM (a run's record version is observed at its reference time), kinds `[raster-frame]`, `kindRetention{raster-frame: 7d}`, `zeroResultsNormal`, license US-PD (descriptor `licence`). Every product is `temporal.type=forecast`, stored as i16 on the native grid, ladder "current complete run + previous complete run". Nothing is downloaded whole: each step's `.idx` inventory is read, the wanted messages are matched by **variable and level** (and the run-total time range for accumulations), **never by message number**, and fetched with coalesced byte ranges, each charged to `mbPerDay` in advance from the offsets. Params: `pathTemplate` (tokens `{yyyyMMdd}`, `{HH}`, `{FFF}`/`{FF}`; override it for the GFSv17 layout), `steps` (`N` or `A-B/S` items), `runEvery`, `availabilityLag`. A missing `.idx` 2 h past `availabilityLag` degrades the source; a field missing from an `.idx` degrades it with "possible format change: GFS idx missing GUST:surface" and writes no frame for that product.

| Source id | Kind | Upstream | Protocol | Default interval · budget | Notes |
|---|---|---|---|---|---|
| `gfs` | raster-frame | `noaa-gfs-bdp-pds`, `gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.0p25.f{FFF}` + `.idx` | GET of `.idx`, ranged GETs of GRIB2 messages (template 5.3) | 15 min · 60/min, 3,000/day, **1,000 MB/day**; `maxResponseBytes` 2 MB (`.idx`), ranges capped per read | 4 runs/day (`runEvery` 6h, `availabilityLag` 210m); steps f000–f048 3-hourly, f054–f120 6-hourly (29). Products: `gfs` (`kind=field`, `UGRD`/`VGRD` `10 m above ground`, two bands at 0.01 m/s on `latlon` 1440 × 721, plus God's Eye View fields `rasters/fields/gfs/{run}/f{FFF}.{1p00\|0p25}.f32.gz`; native max zoom 4; required), `gfs-gust` (`GUST:surface`), `gfs-t2m` (`TMP:2 m above ground`, K), `gfs-apcp` (`APCP:surface` run total: `0-{step} hour acc`, or `0-{step/24} day acc` at whole days, the first match when listed twice; no f000). **Verified 2026-10-12** (12z run): the 0.25° file is about 547 MB; the five messages total about 3.5 MB per step (≈ 0.4–0.5 GB/day); `APCP:surface:0-6 hour acc fcst` is listed twice at f006; f024/f048/f072/f096/f120 list `0-1`…`0-5 day acc fcst`; components are earth-relative (flag table 3.3 bit 5 clear). |
| `hrrr` | raster-frame | `noaa-hrrr-bdp-pds`, `hrrr.{yyyyMMdd}/conus/hrrr.t{HH}z.wrfsfcf{FF}.grib2` + `.idx` | GET of `.idx`, ranged GETs of GRIB2 messages (templates 5.3 and 5.0) | 10 min · 60/min, 4,000/day, **2,000 MB/day**; `maxResponseBytes` 2 MB (`.idx`) | Runs every 3 h, every 6 h with `FORESIGNAL_PROFILE=small` (`runEvery`), `availabilityLag` 50m; steps f00–f18. Products on `lcc` 1799 × 1059 (native max zoom 8): `hrrr` (`kind=field`, `UGRD`/`VGRD` `10 m above ground`, rotated from grid-relative to earth-relative; required), `hrrr-refc` (`REFC:entire atmosphere`, dBZ, `nws-reflectivity`), `hrrr-gust` (`GUST:surface`), `hrrr-smoke` (`MASSDEN:8 m above ground`, kg m⁻³ converted to µg m⁻³, `smoke`). **Verified 2026-10-12** (12z run, `wrfsfcf01`): the file is about 153 MB; those four `.idx` strings are present; the wanted messages total about 7.4 MB per step (the 10 m winds use simple packing, 2.4 MB each), so ≈ 0.6 GB/day small, ≈ 1.1 GB/day large; template 3.30 with resolution and component flags `0x08` (grid-relative winds), sphere radius 6,371,229 m, Latin1 = Latin2 = 38.5°, LoV 262.5°; MASSDEN ranged to 9.1 × 10⁻⁸ kg m⁻³ (91 µg m⁻³); f18 of a 12z run was listed about 75 minutes after the cycle. |

### Derived weather sources (prompt 052)

All three are **DERIVED** (`ai.foresignal.derive`): no upstream, no budget use, TTL none (liveness from explicit tombstones and startup reconciliation), tier STANDARD, policy ALL, observedAtMode UPSTREAM, `zeroResultsNormal`, descriptor `licence` "derived (see inputs)", installed disabled. They write a version only when the quantized payload changes and never tombstone because input is stale. Every payload carries `automated: true, notAWarning: true`. Retention follows Weather-Imagery-Design §2.4 (`place-weather` 14 d, `wx-area` 7 d, `radar-cell` 3 d).

| Source id | Kind | Inputs | Trigger | Notes |
|---|---|---|---|---|
| `wx-places` | place-weather | frames of `mrms-reflectivity`, `mrms-mesh-60m`, `mrms-precip-rate`, `mrms-qpe-1h`, `mrms-qpe-24h`, `mrms-ffg-ratio-1h` (053), `hrrr-smoke`, `gfs`, `gfs-gust`, `gfs-t2m`, `gfs-apcp`; the GLM ring; `radar-cell` changes of `wx-cells`; `nws-forecast` changes of `nws-gridpoints` (053); place edits | each frame event and change batch, a 60 s tick | `externalId` `place:{id}`, no geometry (place positions are sensitive), titleField `place`, `significantFields` empty. Sections `radar`, `hail`, `precip` (053 adds `qpe1hMmMax`, `qpe24hMmMax`, `ffgRatio1hPctMax`), `lightning`, `cell`, `smoke`, `forecast.gfs`, `forecast.nws` (053; stale after 6 h), each with `status` and, while not ok, `asOf`; 999 = none within 100 km; verified-clear helpers `radar.verifiedNearest50Km10m`, `lightning.verifiedNearestKm30m`. Distances from the place's representative point over cells pre-indexed within 100 km. Tombstoned when the place is deleted. |
| `wx-areas` | wx-area | the newest MRMS frame of each configured field; the places AOI and place areas | tick, param `interval` 6–10 min (default 8) | `externalId` `{field}:{threshold}:{aoi}`; param `areas` (default `refl:50:aoi`, `mesh:25:aoi`; fields `refl`, `mesh`, `precip`, and from 053 `rotation` (s⁻¹), `qpe1h` (mm), `ffg` (%)). `areaKm2` (2 significant figures, spherical cell areas), `maxValue`, `coveragePct`; geometry = mask polygons snapped to about 1 km, or null. Tombstoned when its place is deleted or it leaves `areas`. |
| `wx-cells` | radar-cell | frames of `mrms-reflectivity` (plus the current `mrms-mesh-60m` frame and the GLM ring for properties); the places AOI | each reflectivity frame no older than 6 min | `externalId` `c-{ulid}`; param `scope` (`aoi`, or `conus` on the large profile). 40 dBZ mask, 3×3 opening, 8-connected union-find, split at 50 dBZ cores, run-rectangle polygons snapped to about 500 m; `maxDbz`, `areaKm2`, `meshMax`, `glmFlashes5m` (bucket), `motion{speedKmh, bearingDeg, method: centroid-v1}`. Cells absent from a processed frame are tombstoned. |

### MRMS second wave, gridpoints and storm reports (prompt 053)

**MRMS second wave** (products of the existing `mrms` source; same S3 `start-after` path, `.grib2.gz` decode and sentinel handling, native max zoom 8, coverage CONUS, warmed like reflectivity at z ≤ 7). Each has a fixed `takeEvery` parameter, never adaptive: the first listed file of each `2 min × takeEvery` bucket of key timestamps is committed and the rest are passed over. Sizes and daily volumes are from the full listing of 2026-10-12; the second wave totals about 0.46 GB/day at the defaults (the design estimated ≈ 0.5 GB; every file of all four would be 1.03 GB), and all seven `mrms` products about 2.5 GB/day.

| Product | Upstream prefix | Upstream cadence · size | `takeEvery` (param, default) · MB/day | Stored | Notes |
|---|---|---|---|---|---|
| `mrms-rotation-60m` | `CONUS/RotationTrack60min_00.50/` | 2 min · 0.26 MB avg | `rotationTakeEvery` 1 · 187 | u16, s⁻¹ = code × 10⁻⁴ − 10⁻⁴, style `rotation@1` | GRIB2 209/3/3 at 500 m, units 0.001 s⁻¹, **0.005° grid (14,000 × 7,000), reduced 2 × 2 by maximum to the shared 0.01° grid**. The MRMS table gives 0 for missing and no coverage alike, so 0 is stored as code 1 (`noEcho`: no rotation detected, or no radar) and only a bitmap-missing sample is no coverage. maxGap 6 min. |
| `mrms-qpe-1h` | `CONUS/RadarOnly_QPE_01H_00.00/` | 2 min · 0.85 MB avg | `qpe1hTakeEvery` 5 · 121 (607 every file) | u16, 0.1 mm, style `qpe@1` | GRIB2 209/6/2 at 0 m; −3 no coverage and −1 missing both stored as 65,535 (no coverage). Cadence 10 min, maxGap 30 min. |
| `mrms-qpe-24h` | `CONUS/MultiSensor_QPE_24H_Pass2_00.00/` | hourly, ≈ 59 min latency · 4.7–4.9 MB on a wet day | `qpe24hTakeEvery` 1 · 130 | u16, 0.1 mm, style `qpe@1` | GRIB2 209/6/41 at 0 m. maxGap **150 min** (the newest file is 59–119 minutes old, so the 90 minutes first proposed would flap hourly). Ladder: hourly frames to 48 h. |
| `mrms-ffg-ratio-1h` | `CONUS/FLASH_QPE_FFG01H_00.00/` | 2 min · 0.14 MB avg | `ffgTakeEvery` 5 · 21 (104 every file) | u16 **percent**, 0.1 %, style `ffg-ratio@1` | GRIB2 209/12/26 at the surface; −999 no coverage. The table says "non-dim" but the file holds whole percents in steps of 5 (p50 5, max 90 on 2026-10-13). Its grid's first longitude is encoded as 230.004996°, so it is a separate `raster_grid` row from the other products. Cadence 10 min, maxGap 30 min. |

**Record sources** (POLLING, tier STANDARD, policy ALL, observedAtMode UPSTREAM, TTL none, ttlAnchor RECEIVED, license US-PD (descriptor `licence`), terms https://www.weather.gov/disclaimer, `zeroResultsNormal`). Retention follows Weather-Imagery-Design §2.4 and the prompt (`storm-report` 30 d, `nws-forecast` 7 d).

| Source id | Kind | Upstream | Protocol | Default interval · budget | Notes |
|---|---|---|---|---|---|
| `nws-gridpoints` | nws-forecast | `https://api.weather.gov/points/{lat},{lon}` (4 decimals) → `forecastGridData` (`/gridpoints/{wfo}/{x},{y}`) per place | REST GeoJSON; conditional GET (`If-Modified-Since`) | 60 s, at most 4 places per run, each place hourly at a fixed minute · 10/min, 3,000/day | `externalId` `place:{placeId}`, no geometry (place positions are sensitive), titleField `place`. Params `allPlaces` (default true; false selects places whose `products` contain `nws`) and `maxPlaces` (default 50, more raise a health warning). `observedAt` = `updateTime`; payload `place, wfo, gridX, gridY, popMax24h, qpf24hMm, tMaxC24h, tMinC24h, gustMaxMs24h, hazards[] (≤ 10, type = phenomenon.significance), forecastUpdated`. The 72-hour hourly series (intervals expanded, accumulations split evenly, wind in m/s) is kept in `forecast_series_cache` (provider `nws`). Resolution cached in adapter state, re-resolved weekly, on a 404 and when the place moves. A 304 keeps the record; a 500 keeps the last forecast, retries in 10 minutes and degrades after 3 in a row; 429/503 honor `Retry-After`. Tombstoned, and its series deleted, when the place is deleted or leaves the selection. User-Agent and contact as for `nws-alerts`. **Verified 2026-10-12:** `Last-Modified` and `Cache-Control: public, max-age≈3600` on `/gridpoints`, no ETag, 304 to `If-Modified-Since`; `/points` is `max-age=86400`; units `degC`, `km_h-1`, `percent`, `mm`. |
| `spc-storm-reports` | storm-report | `https://www.spc.noaa.gov/climo/reports/today.csv` and `yesterday.csv` | conditional GETs of a CSV | 10 min (yesterday hourly) · 2/min, 400/day | `externalId` = first 12 hex of SHA-256 over `type\|convDate\|HHMM\|lat\|lon\|location`; `observedAt` = `eventTime` = report time (HHMM UTC in the 12–12 UTC convective day; rows before 1200 fall on the next date; a file last modified before its day began still holds the previous day). Point geometry; payload `type, magnitude, magnitudeUnits, location, county, state, wfo, comments (≤ 500, untrusted), title`; titleField `title`; embed `comments, location`. Never tombstoned on absence. **Verified 2026-10-13:** ETag, Last-Modified, `max-age=300`; three sections with their own headers (`Time,F_Scale,…`, `Time,Speed,…`, `Time,Size,…`); hail size in hundredths of an inch; **wind speed in mph** (a row of 73 says "measured a gust of 73 mph"), not knots; `UNK` magnitudes omitted. Moved here from prompt 089. |

**GLM, not Blitzortung.** Lightning comes from GOES GLM (US public domain, about 8–10 km footprint over Texas, detection efficiency below 100 %). Blitzortung is skipped: its data is for non-commercial use without redistribution (Weather-Imagery-Design §1.2).

**Corrections from Weather-Imagery-Design §1.4** (verified 2026-10-10; the bucket facts re-checked 2026-10-12):
- NEXRAD Level II is at `unidata-nexrad-level2` (`noaa-nexrad-level2` answers AccessDenied to an anonymous listing, re-checked 2026-10-12); Level III at `unidata-nexrad-level3`.
- GOES-East is GOES-19, bucket `noaa-goes19`.
- NOAA-15 (19 Aug 2025), NOAA-18 (6 Jun 2025) and NOAA-19 (13 Aug 2025) are decommissioned: 137 MHz APT is gone.
- MRMS ProbSevere v3 is at `noaa-mrms-pds/ProbSevere/` (about 0.25–0.27 MB per file); lightning probability at `CONUS/LightningProbabilityNext30minGrid_scale_1/` (both prefixes list objects, 2026-10-12).
- HRRR `wrfsfcf01` is about 142 MB, so HRRR is read by `.idx` byte ranges only.
- GFS: PNS 26-29 proposes GFSv17 in October 2026; PNS 26-30 removes GRIB1, goessimp/special files and the time-averaged CRAIN/CSNOW/CFRZR/CICEP fields. No evidence the 0.5° or 1° files are dropped; Foresignal uses `pgrb2.0p25` with configurable path templates and matches fields by `.idx` variable and level, never by message number.
- ECMWF open data is CC BY 4.0 (the whole real-time catalog since 1 Oct 2025); the open subset is `ifs/0p25` plus `aifs-single` and `aifs-ens`.
- AWC asks for at most 1 request/min per thread and 100/min overall; its cache file updates every minute, so a 5-minute poll complies.

**Not ingested (client concerns):** Google 3D Tiles, Esri, OSM/OpenFreeMap tiles, NASA GIBS, Mapillary tiles, TomTom flow tiles, terrain, geocoding (Photon/Nominatim/Google), routing (OSRM), Open-Meteo point weather, Google News RSS. Frontends call these directly.

## Intelligence and specialist sources (re-implemented; SB is reference only)

Roadmap prompts:
- `cisa-kev`, `abusech-*` → 041
- `noaa-swpc`, `satnogs-stations`, `tinygs` → 042
- `openaq` → 043
- `amtraker`, `digitraffic-rail`, `airplaneslive-mil`, `adsbfi-region-*` → 044
- `deepstate-frontline`, `alerts-in-ua` → 045
- `pskreporter-ft8`, `kiwisdr`, `openmhz-systems`, `aprs-is` → 046
- `meshtastic-*` → 047
- `gfw-events` → 050
- `ripe-atlas-probes` → 054
- `wri-power-plants`, `ourairports`, `gvp-volcanoes` → 056

| Source id | Kind | Upstream | Protocol | Auth | Notes |
|---|---|---|---|---|---|
| `cisa-kev` | vulnerability | `www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json` | REST JSON | none | 6 h. No geometry. |
| `abusech-feodo` | malware-c2 | `feodotracker.abuse.ch/downloads/ipblocklist.json` | REST JSON | none | 15 min. Country centroid only. Flag `geometryApproximate: true` in the payload. |
| `abusech-urlhaus` | malware-url | `urlhaus-api.abuse.ch/v1/urls/recent/limit/100/` | REST JSON | none (an auth key may now be required; verify) | 15 min. |
| `noaa-swpc` | space-weather | `services.swpc.noaa.gov/json/planetary_k_index_1m.json`, `edited_events.json` | REST JSON | none | 5 min. No geometry. |
| `ripe-atlas-probes` | probe-status | `atlas.ripe.net/api/v2/probes/?status=2&page_size=500` | REST JSON | none | 15 min. Point geometry per probe. |
| `gfw-events` | fishing-event | `gateway.api.globalfishingwatch.org/v3/events?datasets[0]=public-global-fishing-events:latest…` | REST JSON | token | 1 h. ~5-day publication lag; query a 10-day window. |
| `openaq` | air-quality | `api.openaq.org/v3/locations?parameter_id=2…` | REST JSON | apiKey (`X-API-Key`) | 30 min. |
| `gvp-volcanoes` | volcano | Smithsonian GVP WFS Holocene volcanoes | WFS GeoJSON | none | Weekly (reference dataset). |
| `deepstate-frontline` | frontline | GitHub mirror `cyterat/deepstate-map-data` raw GeoJSON | REST GeoJSON | none (unauthenticated GitHub) | 30 min. Pin by commit param option. |
| `alerts-in-ua` | air-raid-alert | `api.alerts.in.ua/v1/alerts/active.json` | REST JSON | token | 2 min. Oblast polygons from bundled boundaries. |
| `amtraker` | train | `api.amtraker.com/v3/trains` | REST JSON | none | 60 s. |
| `digitraffic-rail` | train | `rata.digitraffic.fi/api/v1/train-locations/latest` | REST JSON | none | 60 s, gzip. |
| `satnogs-stations` | ground-station | `network.satnogs.org/api/stations/?status=2` | REST JSON | none | 6 h. |
| `tinygs` | satellite-telemetry | `api.tinygs.com/v1/satellitesWorldmap` | REST JSON | none | 30 min. A fragile upstream, so stay gentle. |
| `pskreporter-ft8` | radio-spot | `retrieve.pskreporter.info/query?mode=FT8&flowStartSeconds=-900…` | REST XML | none | 10 min. Use a safe XML parser (no XXE). |
| `kiwisdr` | sdr-receiver | `rx.linkfanel.net/kiwisdr_com.js` | JS-literal parse | none | 24 h. Lenient parser (trailing commas, comments). |
| `openmhz-systems` | scanner | `api.openmhz.com/systems` | REST JSON | none | 1 h. Calls are on demand later. |
| `aprs-is` | aprs-station | `rotate.aprs2.net:14580`, filter `r/{lat}/{lon}/{km}` | raw TCP (streaming) | callsign (+passcode for TX; receive-only uses -1) | **Opt-in, narrow filter mandatory.** Reconnect 30 s → 15 min with jitter. |
| `meshtastic-mqtt` | mesh-node | `mqtt.meshtastic.org:1883`, topic `msh/{root}/#` | MQTT (streaming) | public creds | **Mesh.md rules:** disabled by default, one root topic default, `maxSubscriptions` ≤ 3, 100 msg/min cap, keepalive 120 s, reconnect 15–300 s, identifiable client id `foresignal-<installId>`. |
| `meshtastic-map` | mesh-node | `meshtastic.liamcottle.net/api/v1/nodes` (~37 MB) | REST JSON | none | **Daily at most.** Community hobby service. |
| `wri-power-plants` | power-plant | WRI Global Power Plant Database CSV (GitHub release) | CSV | none | Weekly reference dataset. CC BY 4.0. |
| `ourairports` | airport | `ourairports.com/data/airports.csv` | CSV | none | Weekly reference dataset. |
| `airplaneslive-mil` | aircraft | `api.airplanes.live/v2/mil` | REST JSON | none | 60 s. Serial requests only (it throws 429s). |
| `adsbfi-region-*` | aircraft | `opendata.adsb.fi/api/v3/lat/{lat}/lon/{lon}/dist/{nm}` | REST JSON | none | 120 s. Serial requests only. |

## From the Velocity survey (AGPL, reference only)

Roadmap prompts:
- `sondehub` → 042
- `ndbc-buoys` → 043
- `ua-air-raid-siren` → 045
- `ais-kystverket`, `ais-digitraffic` → 048
- `faa-tfr`, `faa-nas-status`, `awc-sigmets` → 049
- `nga-msi-warnings` → 050
- `meteoalarm`, `emsc-seismic` → 051 (`spc-storm-reports` was built in prompt 053; see "MRMS second wave, gridpoints and storm reports")
- `uk-ofsi-sanctions`, `un-sanctions` → 053
- `gpsjam-daily` → 054

Keyless and official feeds that VEL uses and Foresignal lacks. VEL paths are under `~/Documents/GitLab/velocity/apps/api/app/`.

| Source id | Kind | Upstream | Protocol | Notes |
|---|---|---|---|---|
| `ais-kystverket` | vessel | Norwegian Coastal Administration anonymous NMEA, `153.44.253.27:5631` | raw TCP (streaming) | Keyless, official, Norway coast. Needs an AIVDM decoder: TAG-block strip, multipart reassembly, types 1/2/3/18 for positions and 5/19/24 for static data. Implement it in Java or use a library; verify a Java AIS decoder on Maven Central. Sentinels SOG 102.3 / heading 511. |
| `ais-digitraffic` | vessel | Fintraffic `wss://meri.digitraffic.fi:443/mqtt`, topic `vessels-v2/+/location` (+ metadata REST `/api/ais/v1/vessels`, 12 h) | MQTT over WSS (streaming) | Keyless, official, Finland/Baltic. **Don't use the REST `locations` endpoint**: about 86% of its fixes are months old. If used, drop fixes older than 24 h. |
| `emsc-seismic` | earthquake | `www.seismicportal.eu/fdsnws/event/1/query?format=json` | REST FDSN JSON | 60 s. Complements USGS (strong in Europe/Mediterranean). Cross-source dedup is handled by derived layers (prompt 057). |
| `nga-msi-warnings` | maritime-warning | `msi.nga.mil/api/publications/broadcast-warn?status=active&output=json` | REST JSON | 15 min. Coordinates are free text in DMS, so they need a regex parser; about 4% have none (record without geometry). VEL needed a browser UA and Referer: try our UA first, and **if blocked, mark it failing; don't spoof**. |
| `faa-tfr` | airspace-restriction | `tfr.faa.gov/tfrapi/exportTfrList` + `tfr.faa.gov/download/detail_<id>.xml` | REST JSON + XNOTAM XML | 15 min. Polygon geometry from XNOTAM. Detail ids use underscores. |
| `faa-nas-status` | airport-delay | `nasstatus.faa.gov/api/airport-status-information` | REST JSON | 5 min (also listed by WM). |
| `awc-sigmets` | aviation-hazard | `aviationweather.gov/api/data/airsigmet?format=geojson` | REST | 5 min. Polygons. METARs are `awc-metars` (prompt 047). |
| `ndbc-buoys` | buoy-observation | `www.ndbc.noaa.gov/data/latest_obs/latest_obs.txt` | fixed-width text | 10 min. Point stations. |
| `sondehub` | radiosonde | `api.v2.sondehub.org/sondes` | REST JSON | 5 min. Weather-balloon positions. |
| `meteoalarm` | weather-alert | `feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-{country}` | Atom (family by country) | 15 min. EU counterpart to NWS alerts; region polygons need EMMA_ID boundaries (bundle or skip geometry). |
| `spc-storm-reports` | storm-report | `www.spc.noaa.gov/climo/reports/today.csv` | CSV | Built in prompt 053 (10 min, conditional GET, three-section CSV); see "MRMS second wave, gridpoints and storm reports". |
| `ua-air-raid-siren` | air-raid-alert | `siren.pp.ua/api/v3/alerts` | REST JSON | A keyless alternative to `alerts-in-ua`. Verify its terms. |
| `uk-ofsi-sanctions` / `un-sanctions` | sanction | UK OFSI ConList CSV; UN consolidated list XML | CSV / XML | Daily. Pairs with `ofac-sdn`. |
| `gpsjam-daily` | jamming-cell | `gpsjam.org/data/manifest.csv` → `{date}-h3_4.csv` | CSV | Daily. H3 res-4 cells (needs the `h3` Java dependency to turn cells into polygons). Complements Foresignal's derived jamming layer (prompt 057). |

**Not built into Foresignal from VEL (external provider ideas, EDP):** unofficial web-map endpoints, scraping and browser-based collection.
- FlightRadar24, ShipXplorer, MyShipTracking, MarineTraffic, VesselFinder and readsb `aircraft.json` mirrors: candidate EDP providers (`layer` streams of `feature` items in the built-in `aircraft` and `vessel` kinds, so fusion and rules apply); see "External provider ideas".
- Headless browsers and proxies belong in those provider projects, not in Foresignal.
- The keyless Google Earth and Apple Flyover 3D channels are client basemaps, fetched by clients directly (Foresignal does not host basemaps).

**Design notes from VEL (ideas, not code):**
- Alerts on **enter/exit transitions**, not on every match. Adopted as the trigger rule `mode` (Architecture §8.3).
- When fusing sources, pick the freshest observation by its observation time, not by merge order. Applies to the aircraft-fusion derived layer (prompt 057).
- Publish the age of the data, not of the response. Foresignal's `observedAt` already does this.
- Read credit budgets from response headers (OpenSky `x-rate-limit-remaining`).
- Treat an HTTP 200 with a non-JSON body as throttling. Adapters should throw `UpstreamFormatException` on an unexpected content type.
- Derived-layer recipes for later: `ais_gap` (no fix for 30 min), `loiter` (every fix within 0.5 nm of the centroid over 30 min), `rendezvous` (two vessels within 0.5 nm), deception checks (duplicate MMSI more than 50 km apart within 120 s, vessel speed over 80 kn, aircraft speed over 1400 kn), and the GPS-jamming thresholds (NACp < 8 or NIC < 7; ≥ 30% medium, ≥ 50% high, ≥ 3 aircraft).

## From the World Monitor survey (AGPL, reference only)

Roadmap prompts:
- `safecast-radiation`, `epa-radnet` → 043
- `acled`, `ucdp-ged` → 045
- `imf-portwatch-chokepoints` → 050
- `nasa-eonet`, `reliefweb-disasters`, `unhcr-population`, `who-don` → 052
- `ofac-sdn`, `sec-8k` → 053
- `cloudflare-radar-outages` → 054
- `eia-petroleum`, `gie-agsi`, `ember-electricity`, `kalshi` → 055 (records without geometry)
- `submarine-cables` → 056
- `faa-nas-status` → 049 (also listed under Velocity)

These are structured feeds that rarely appear in OSINT globe projects. Reference files are under `~/Documents/GitLab/worldmonitor/scripts/seed-*.mjs`. `gdacs` (prompt 034) is confirmed at `gdacsapi/api/events/geteventlist/MAP`; WM drops Green alerts, and Foresignal keeps them and lets triggers filter.

| Source id | Kind | Upstream | Auth | Notes |
|---|---|---|---|---|
| `acled` | conflict-event | `acleddata.com/api/acled/read` (OAuth `acleddata.com/oauth/token`) | email+password or token | 15-min rate-limit window; country by UN numeric code. Licence restricts redistribution. |
| `ucdp-ged` | conflict-event | `ucdpapi.pcr.uu.se/api/gedevents/{version}` | access token | 6 h. The annual GED lags about 7 months, so auto-discover the monthly "Candidate" release (`YY.0.M`) and merge it. |
| `nasa-eonet` | natural-event | `eonet.gsfc.nasa.gov/api/v3/events` | none | Multi-hazard and open; complements GDACS. |
| `reliefweb-disasters` | disaster | `api.reliefweb.int/v2/disasters` | appname param | Daily. |
| `unhcr-population` | displacement | `api.unhcr.org/population/v1/population/` | none | Daily, paginated, country-level (centroids). |
| `cloudflare-radar-outages` | outage | `api.cloudflare.com/client/v4/radar/annotations/outages` | API token | 1–3 h. Pairs with IODA. |
| `safecast-radiation` | radiation | `api.safecast.org/measurements.json` | none | 2 h. Point sensors. |
| `epa-radnet` | radiation | `radnet.epa.gov/cdx-radnet-rest/api/rest/csv/{year}/fixed/{state}/{slug}` | none | CSV per station. |
| `imf-portwatch-chokepoints` | chokepoint-transit | ArcGIS `services9.arcgis.com/weJ1QsnbMYJlCHdG/.../Daily_Chokepoints_Data/FeatureServer/0/query` and `portwatch_disruptions_database` | none | 12 h, ArcGIS paging. Maritime chokepoint flows and disruptions. |
| `submarine-cables` | cable | `www.submarinecablemap.com/api/v3/cable/all.json` | none | Weekly reference dataset. **TeleGeography data is CC BY-NC-SA**, so flag it non-commercial in the attribution. |
| `ofac-sdn` | sanction | `sanctionslistservice.ofac.treas.gov/api/PublicationPreview/exports/sdn_advanced.xml` | none | Daily. The official US Treasury source (public domain). Prefer it over the OpenSanctions mirror. Large XML: stream-parse it. No geometry. |
| `sec-8k` | filing | `www.sec.gov/cgi-bin/browse-edgar?action=getcurrent&type=8-K&output=atom` | none (SEC requires a descriptive User-Agent with contact info) | 10 min. Atom. |
| `who-don` | disease-outbreak | `www.who.int/api/emergencies/diseaseoutbreaknews` | none | 6 h. Country-level. |
| `faa-nas-status` | airport-delay | `nasstatus.faa.gov/api/airport-status-information` | none | 5 min. Airport points. |
| `eia-petroleum` / `gie-agsi` / `ember-electricity` | energy | EIA v2 API, `agsi.gie.eu/api`, Ember monthly CSV | keys for EIA and AGSI | Weekly or daily. These are time series with no geometry. Carried as records without geometry, `kind=series-point`, with the series id as `externalId` plus the period (prompt 055). |
| `polymarket` / `kalshi` | prediction-market | `gamma-api.polymarket.com`, `api.elections.kalshi.com/trade-api/v2` | none | External provider ideas (EDP `observation` and `forecast` streams; 138 binds forecasts to ledger questions). |

**Design notes worth borrowing (ideas, not code):**
- WM's seed envelope state `OK | OK_ZERO | ERROR` is the same idea as Foresignal's zero-result health rule.
- Its "keep last good data on failure" behaviour is already implicit in Foresignal: records persist until retention removes them.
- Its ACLED-over-GDELT dedup on a 0.1° grid and same-day match is included in derived layers (prompt 057).

**Not built into Foresignal from WM (external provider ideas, EDP):** OREF sirens, the Hormuz transit dashboard, Yahoo Finance, CNN Fear & Greed, Windy webcams, LiveUAMap and Telegram channels. The X API is a built-in social adapter (135).

## External provider ideas (EDP)

Foresignal ships none of these. Each fits the [External Data Protocol](External-Data-Protocol.md) as a separate provider project (any language, any host); the operator registers the ones they want. Providers that reuse a built-in kind (`aircraft`, `vessel`, `air-raid-alert`, `conflict-event`, `social-post`…) join that kind's layers, rules and analysis automatically.

| Idea | Streams (mode, item type, kind) | Useful binding (138) | Notes for provider authors |
|---|---|---|---|
| FlightRadar24 (official API or web feed) | layer, `feature`, `aircraft` | — (fused with other aircraft) | official API needs a subscription; web feed is unofficial |
| MarineTraffic, VesselFinder, MyShipTracking, ShipXplorer | layer, `feature`, `vessel` | — | official APIs where offered, else web endpoints |
| readsb `aircraft.json` mirrors | layer, `feature`, `aircraft` | — | redistribution terms unclear; mirrors may block datacentre IPs |
| Liveuamap | layer, `event`, `conflict-event` | `canonical-family` (conflict) | official API (key) or the public map (Cloudflare Turnstile) |
| OREF sirens (Israel Home Front Command) | layer, `event`, `air-raid-alert` | `canonical-family` | geo-restricted; usually needs a proxy in Israel |
| UKMTO advisories | layer, `event`, `maritime-incident` | `canonical-family` | site blocks some automated clients |
| Hormuz transit dashboards | atomic, `observation` | `series` | dashboard data queries; fragile |
| NUFORC reports | layer, `event`, `uap-report` | — | scraping |
| Broadcastify feed status and listener counts | atomic, `observation` | `series` (listener spikes are an event signal) | premium API or scraping |
| TinyGS | layer, `feature` / `event` | — | web client endpoints; no published API terms |
| Unofficial camera loaders (TxDOT and other GEV routes), camera frame snapshots | layer, `feature` with `media` | — | no published terms |
| InciWeb extra routes | layer, `event` | `canonical-family` (fire) | undocumented index and pages |
| Telegram channels | atomic, `document` (`social-post`) | `social` | Telegram's terms prohibit scraping since 2026-02-03; MTProto clients (TDLib, Boost licence) use the operator's own account; a `transport.search` endpoint enables cues |
| Facebook pages and groups, TikTok | atomic, `document` (`social-post`) | `social` | no official public-content route |
| Article bodies and bylines, GDELT URL metadata | atomic, `document` (`article`) | `news` | upstream site terms |
| Kalshi, Polymarket | atomic, `observation` (prices) and `forecast` | `series`, `forecast` | Kalshi's terms restrict storing data without consent; see 093 §8 |
| Metaculus community predictions | atomic, `forecast` | `forecast` | terms restrict AI/ML and commercial use without permission |
| VIEWS conflict forecasts | atomic, `forecast` and `observation` | `forecast`, `series` | confirm the data licence |
| CME settlements, ICE end-of-day | atomic, `observation` | `series` | personal, non-commercial use; automated access restricted; ICE needs a subscription |
| Yahoo Finance, CNN Fear & Greed | atomic, `observation` | `series` | unofficial endpoints |
| Google Trends | atomic, `observation` | `series` | official API is a closed alpha; web endpoints are unofficial |
| Electricity Maps | atomic, `observation` | `series` | API key, commercial terms |
| NewsGuard, Ad Fontes ratings | — | operator CSV import into 133 | proprietary ratings; or a provider that emits the CSV |
| Shodan saved queries | atomic, `observation` (counts) and layer `feature` (exposed services) | `series` | API key |
| Windy webcams | layer, `feature` with `media` | — | API key |
| Argo ocean floats (GDAC / ERDDAP) | atomic, `observation` (temperature and salinity profiles by region) | `series` | public data; low volume |
| GCN and IGWN multi-messenger alerts (gravitational waves, neutrinos, GRBs) | layer, `event` | `canonical-family` | public Kafka/JSON notices; rare, high-value events |
| Local RF occupancy and interference (operator's SDRs) | atomic, `observation` per band and site | `series`; declared coverage feeds observability | the operator's own receivers |

## Markets, attention and structural series (prompt 117, v1.3)

All are `series-point` + `series-latest` records (093 model) with `family`, `transform`, `releaseLagDays` and `egress` metadata, analytic class `series` (3650 d).

| Source | Upstream | Auth | Cadence / lag | Licence / egress | Notes |
|---|---|---|---|---|---|
| `cftc-cot` | CFTC Public Reporting (Socrata): legacy `6dca-aqww`, disaggregated `72hh-3qpy`, TFF `gpe5-46if` (and F&O variants) | none (optional app token) | weekly; Tuesday positions released Friday 15:30 ET (lag 3 d) | US government, public domain | Default contracts: WTI, natural gas, RBOB, ULSD, gold, copper, wheat, corn, E-mini S&P, 10-year note, dollar index, euro FX. History backfill from 2010. |
| `fred-series` | FRED API | free API key | daily, lag ~1 d | **`LOCAL_ONLY`**; "not endorsed or certified" notice required; series marked copyrighted (VIXCLS, OVXCLS, DCOILBRENTEU) need explicit opt-in | Defaults: DGS10, DGS2, T10Y2Y, DFF, DTWEXBGS, DCOILWTICO, DHHNGSP |
| `wikimedia-pageviews` | Wikimedia Pageviews REST (per-article daily, top articles) | none; User-Agent with contact | daily, lag 1 d | CC0 | Articles from attention topics; `attention-top` daily top 100 |
| `gdelt-timelines` | GDELT DOC 2.0 API (`TimelineVolRaw`, `TimelineTone`) | none | 30 min; ≤ 1 request / 5 s | open with citation | Volume, share and tone per attention topic |
| `worldbank-indicators` | World Bank Indicators API v2 | none | weekly poll, annual data | mostly CC BY 4.0 (per indicator source) | Structural covariates for regional indices |
| `ice-cot` (optional) | ICE Futures Europe weekly COT (Brent, gasoil) | none | weekly | verify | Implemented only if an official, stable machine-readable file exists (117 decides and records) |

`eia-petroleum` (093) spot prices are sourced from LSEG: those series are `LOCAL_ONLY`.

## Review additions (v1.3)

| Source / layer | Prompt | Notes |
|---|---|---|
| `gfw-sar-detections` | 088 (optional) | Global Fishing Watch SAR vessel detections with AIS-match flag; CC BY-NC 4.0; verify the dataset id at capture |
| `derived-cable-risk` | 099 | anchored or slow vessels near submarine cables outside ports; possible anchor drag |
| `derived-dark-vessel-match` | 099 | AIS gaps matched to unmatched SAR detections (candidates only) |
| `derived-lightning-holdover` | 099 | GLM flashes over dry cells, watched for later fire detections |
| `derived-hf-blackout` | 099 | PSKReporter spot and SNR drops on sunlit paths, with concurrent SWPC X-ray class |

Every catalogued source also has an originator and observability entry in `src/main/resources/intel/originators.json` and `intel/observability.json` (prompt 116).

## News and media ratings (prompt 133, v1.3)

Articles are stored as metadata only (no bodies, no bylines). Ratings are imported from declared providers, never generated.

| Source / provider | Access | Licence / egress | Default | Notes |
|---|---|---|---|---|
| `gdelt-articles` | GDELT DOC 2.0 `ArtList` per attention topic | open with citation | on with topics | shares GDELT host pacing with `gdelt-timelines` (1 request / 5 s) |
| `news-feed-*` | outlet RSS/Atom via the 073 feed kit | per outlet | operator-added | title, link, summary only |
| `wikidata` ratings | Wikidata Query Service | CC0 | on | country, owner, state media, public broadcaster, political alignment (P1387) where present |
| `partisan-web` ratings | The Partisan Web data files | CC BY 4.0 (confirm the data licence) | on once confirmed | map its axes; non left–right axes kept as separate dimensions |
| `allsides` ratings | operator import of published ratings | reported CC BY-NC 4.0 (confirm on allsides.com); `LOCAL_ONLY` unless permission recorded | off | attribution required |
| `mbfc` ratings | Media Bias/Fact Check API with the operator's own paid key | provider terms; `LOCAL_ONLY` | off | |
| `operator` ratings | CSV/JSON import | declared at import | n/a | |

NewsGuard and Ad Fontes Media ratings: through the operator CSV import (or an external provider that produces it); see "External provider ideas".

## Social platforms (prompts 135–137, v1.3)

Foresignal is a single-operator tool: every platform below is an adapter the operator enables with their own credentials and budget, after reading the platform's terms (summarised here; check them yourself, they change). Nothing is hard-blocked. "Collection" says what the data can be used for: **complete** and **paged** paths feed volume baselines; **sample** paths feed claims and leads only.

| Source id | Access | Cost and quota (checked 2026-10-11) | Terms summary | Default | Collection | Deletion sync |
|---|---|---|---|---|---|---|
| `bluesky-jetstream` | Jetstream v2 WebSocket (`jetstream.us-east` / `us-west.bsky.network`), Network Replay with API token | free; replay token from `bsky.network/account` | Bluesky Protocol Services contracts | **on** | complete (posts, reposts) | on (delete events) |
| `bluesky-search` | AppView `app.bsky.feed.searchPosts` | free; app password if required | as above | off | paged | on |
| `mastodon` | per instance: hashtag streams, `/api/v1/trends/tags`, account statuses | free; per-instance limits | each instance's rules | off | complete per instance (streams), daily history (trends) | n/a |
| `reddit` | OAuth API, `/r/{sub}/new`, `/search` | free tier 100 queries/min, non-commercial, manual app approval | Reddit Data API Terms | off | paged | optional |
| `youtube` | Data API v3 | 10,000 units/day; `search.list` 100 units, `videos.list` 1 unit | YouTube API Services Terms | off | sample (search), complete (tracked videos' stats) | n/a |
| `x` | API v2 pay-per-use: `counts/recent`, `search/recent` (optional `counts/all`, `search/all`) | $0.005 per post read; $0.005 per recent counts request; monthly cap `social.x.monthlyCapUsd` (default $25) | X Developer Agreement; remove deleted content within 24 h | off | complete counts; paged search | on |
| `threads` | `keyword_search` with `threads_keyword_search` permission | 2,200 queries per rolling 24 h | Meta Platform Terms | off | paged (`RECENT`) | on |
| `instagram-hashtags` | Graph API `ig_hashtag_search`, `recent_media` (business account, Public Content Access review) | 30 unique hashtags per rolling 7 days | Meta Platform Terms | off | sample | optional |
| `xai-x-search` (cue backend, 137) | xAI API X search tool | ~$5 per 1,000 posts returned plus model tokens (per-result billing since 2026-09-21) | xAI terms | off | sample | n/a |
| EDP providers (024, 138) | any platform as `document` items (`social-post`), bound with target `social` | n/a | provider's and operator's responsibility | n/a | as the manifest declares | provider |

Telegram channels, Facebook pages, TikTok and any other platform: external EDP providers (see "External provider ideas"). Meta Content Library is research-only and TikTok's Research API academic-only, so there is no official route for those.

## Reference-history datasets (prompt 121, v1.3)

Imported as weekly counts per family and region (`reference_history`); never served raw.

| Dataset | Access | Span | Licence / egress |
|---|---|---|---|
| UCDP GED (annual + candidate) | operator download or UCDP API | 1989– | CC BY 4.0 |
| ACLED history | ACLED API with the operator's credentials | by region | ACLED EULA; `LOCAL_ONLY`, never redistributed |
| USGS ComCat | FDSN event service | decades | public domain |
| GDACS archive | GDACS event list API | 2000s– | open, attribution |
| CFTC COT history | via `cftc-cot` backfill | 1986– | public domain |
| IMF PortWatch history | via 088 | 2019– | IMF terms: reuse with attribution |

## Derived layers (prompt 057)

These are computed from stored records rather than fetched. Prompt 057 implements them as "derived sources" that consume the change stream:

- **GPS jamming:** NACp < 8 share per 1° cell, using adsb.lol records.
- **Holding patterns:** detected from aircraft version history.
- **Dark vessels:** AIS gaps.
- **Outage correlation.**
