# Foresignal — Source Catalog

The working inventory of upstreams Foresignal will integrate, merged from God's Eye View (GEV, MIT, so code is **portable**) and ShadowBroker (SB, AGPL, so it is **reference only**). Velocity (VEL) and World Monitor (WM), both AGPL, are reference only. Their findings are merged below.

Reference paths: GEV = `~/Documents/GitLab/gods-eye-view`, SB = `~/Documents/GitLab/Shadowbroker/backend`.

**Defaults** are starting values for the descriptor (`defaultInterval` / `defaultBudget perMinute/perDay`). Each adapter prompt confirms them against the upstream's published policy.

## First source (prompt 009)

| Source id | Kind | Upstream | Protocol | Auth | Default interval · budget | Reference | Notes |
|---|---|---|---|---|---|---|---|
| `usgs-earthquakes` | earthquake | `earthquake.usgs.gov/earthquakes/feed/v1.0/summary/{feed}.geojson`, param `feed` ∈ all_hour, all_day, 2.5_day, 4.5_week, significant_month… (default `all_day`) | REST GeoJSON | none | 60 s · 4/min, 2000/day | GEV `src/layers/earthquakes/records.js` | Revisions (mag/updated) create new versions. Public domain. |

## Core sources (prompts 029–035)

Roadmap prompts: aircraft → 029, `aisstream` → 030, `celestrak-gp`/`launch-library` → 031, `nasa-firms` → 032, `gdelt-events` → 033, `nws-alerts`/`nhc-cyclones`/`gdacs` → 034, `ioda-outages` → 035.

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
| `nws-alerts` | weather-alert | `api.weather.gov/alerts/active?status=actual` | REST GeoJSON | none (User-Agent required) | 2 min · 2/min, 1440/day | SB `earth_observation.py` | Polygon geometry when present. Tombstone alerts absent from a complete result. Use `expires` as validUntil. |
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

**Gridded products (prompt 058).** GFS/ECMWF wind (GRIB2 byte-range) is raster data, not records. Prompt 058 adds a separate "products" concept: the latest grid per model and cycle, served as binary. nowCOAST imagery stays a client concern.

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
- `meteoalarm`, `spc-storm-reports`, `emsc-seismic` → 051
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
| `awc-sigmets` | aviation-hazard | `aviationweather.gov/api/data/airsigmet?format=geojson` | REST | 5 min. Polygons. METARs are point weather, a later decision. |
| `ndbc-buoys` | buoy-observation | `www.ndbc.noaa.gov/data/latest_obs/latest_obs.txt` | fixed-width text | 10 min. Point stations. |
| `sondehub` | radiosonde | `api.v2.sondehub.org/sondes` | REST JSON | 5 min. Weather-balloon positions. |
| `meteoalarm` | weather-alert | `feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-{country}` | Atom (family by country) | 15 min. EU counterpart to NWS alerts; region polygons need EMMA_ID boundaries (bundle or skip geometry). |
| `spc-storm-reports` | storm-report | `www.spc.noaa.gov/climo/reports/today.csv` | CSV | 15 min. Tornado, hail and wind reports, as points. |
| `ua-air-raid-siren` | air-raid-alert | `siren.pp.ua/api/v3/alerts` | REST JSON | A keyless alternative to `alerts-in-ua`. Verify its terms. |
| `uk-ofsi-sanctions` / `un-sanctions` | sanction | UK OFSI ConList CSV; UN consolidated list XML | CSV / XML | Daily. Pairs with `ofac-sdn`. |
| `gpsjam-daily` | jamming-cell | `gpsjam.org/data/manifest.csv` → `{date}-h3_4.csv` | CSV | Daily. H3 res-4 cells (needs the `h3` Java dependency to turn cells into polygons). Complements Foresignal's derived jamming layer (prompt 057). |

**Excluded from VEL's set:** unofficial web-map endpoints, scraping, and practices that work around blocks.
- FlightRadar24 `feed.js`, ShipXplorer, MyShipTracking, MarineTraffic and VesselFinder web endpoints.
- Every headless-Chromium "sidecar" and the real-Chrome fetch tier.
- The keyless Google Earth and Apple Flyover 3D channels.
- readsb `aircraft.json` mirrors (`globe.theairtraffic.com`, `skylink.hpradar.com`), whose redistribution terms are unclear and which block datacentre IPs.
- Rotating egress proxies and WARP.

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
| `polymarket` / `kalshi` | prediction-market | `gamma-api.polymarket.com`, `api.elections.kalshi.com/trade-api/v2` | none | **Polymarket JA3-blocks server-side clients, so it is excluded.** Kalshi is fine. |

**Design notes worth borrowing (ideas, not code):**
- WM's seed envelope state `OK | OK_ZERO | ERROR` is the same idea as Foresignal's zero-result health rule.
- Its "keep last good data on failure" behaviour is already implicit in Foresignal: records persist until retention removes them.
- Its ACLED-over-GDELT dedup on a 0.1° grid and same-day match is included in derived layers (prompt 057).

**Excluded from WM's set:**
- OREF sirens: needs a residential proxy to evade an Akamai WAF.
- WTO Hormuz tracker: Power BI scraping.
- Yahoo Finance and CNN Fear & Greed: unofficial endpoints and scraping.
- Telegram MTProto: needs a personal user session.
- X API and Windy: commercial keys.
- LiveUAMap.

## Excluded by charter

These are left out because they scrape or use headless browsers, rely on unofficial endpoints, or depend on an upstream's tolerance:

- **LiveUAMap:** Playwright scraping past Cloudflare Turnstile.
- **Telegram OSINT:** HTML scraping of `t.me/s/`.
- **Broadcastify:** HTML scraping.
- **NUFORC:** nonce scraping.
- **yfinance and the unofficial Google Translate endpoint.**
- **Article-title scraping of GDELT URLs.**
- **Windy webcams:** keyed and commercial.
- **Shodan:** keyed, commercial, on-demand recon. It doesn't fit an ingest broker; agents can call it directly.
- **CME Group data:** personal, non-commercial use only; automated access and data mining prohibited and technically enforced. (DataMine is paid.)
- **ICE end-of-day futures data:** paid subscription with redistribution licensing. (ICE's free weekly COT for Brent and gasoil is evaluated in 117.)
- **Prediction-market prices (Polymarket, Kalshi):** terms forbid storing or archiving (093 §8).
- **Metaculus community forecasts:** terms restrict AI/ML and commercial use without written permission; excluded until permission exists.
- **UKMTO / JMIC advisories:** the site blocks automated clients and offers no official feed; NGA MSI (088) and MARAD advisories cover the same ground.

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
