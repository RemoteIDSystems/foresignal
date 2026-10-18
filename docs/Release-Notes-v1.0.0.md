# Foresignal 1.0.0 — Release Notes

Release date: 2026-10-18 · Gradle `version = "1.0.0"` · OpenAPI `info.version` `1.0.0`

Foresignal ([foresignal.ai](https://foresignal.ai)) is a self-hosted, headless intelligence broker: it collects public
telemetry from upstream feeds on budgeted schedules, stores it as versioned records in PostgreSQL with PostGIS, and
re-serves it over one documented API (`/v1`), live streams, vector tiles, notifications and an MCP server. It is
unrelated to foresignal.com.

> **Release status: code-complete, not yet verified.** Under the owner directive of 2026-10-17 no test, migration or
> runtime check was executed for this release. The `v1.0.0` tag has **not** been created: it waits for the owner's
> full test run (`./gradlew test jacocoTestReport jacocoTestCoverageVerification`) to pass. See
> [Known limitations](#5-known-limitations).

## 1. Highlights by area

**Collection.** One package per upstream under `ai.foresignal.source.adapters`, polling, streaming (WebSocket, MQTT,
TCP and UDP sockets), push and derived sources behind one SPI. Every request goes through `UpstreamClient` with
per-source budgets, conditional GETs, `Retry-After` handling and an identifiable User-Agent. Reviewed feature and feed
kits (`src/main/resources/kits/`) add ArcGIS, OGC API Features, WFS, RSS, Atom and JSON Feed sources without code.
Health tracking per source, and the adapter doctor (`POST /v1/sources/{sourceId}/doctor`) that diffs a live probe
against the recorded fixture.

**Ingest and storage.** A single writer with ingest policies (`ALL`, `SAMPLED`, `LATEST`), content hashing, a monotonic
change sequence, `record_head` and `record_live` for current state, and `record_version` partitioned by history tier
(STANDARD, TRACK) and by sequence range. Retention by analytic class, history downsampling, and point-in-time reads
(`asOf`).

**Schema ownership (new in this release).** Flyway owns the schema: the baseline
`src/main/resources/db/migration/V1__baseline.sql` creates the PostGIS and pgvector extensions, every table, the
partitioned history parents and every index the old bootstrap code created. Hibernate runs with
`ddl-auto=validate`. `PartitionManager` still creates history leaves at run time. The bootstrap initializers
(`SchemaBootstrap`, `HistorySchemaInitializer`, `SpatialIndexInitializer` and the others) are removed.

**Distribution.** The read API (records, history, GeoJSON), server-sent change and event streams, Mapbox vector tiles
with low-zoom aggregation, raster tiles and styles, OGC API Features, KML, CZML, CAP, STIX, iCalendar and other
standards outputs, outbound sinks, exports and offline packs, and federation between instances.

**Alerting and notification.** A trigger engine on the change stream (thresholds, geofences, windowed and anomaly
rules, watchlists, places), alert lifecycle and feedback, notification channels (webhook, email, Slack, Mattermost,
Teams, Google Chat, Discord, Matrix, Telegram, Zulip, ntfy, Gotify, Pushover, Apprise, Home Assistant, MQTT with Home
Assistant discovery, and Meshtastic and MeshCore radio), Web Push, UnifiedPush and native push, an inbox,
actionable notifications and interactive chat commands.

**Weather, rasters and imagery.** MRMS, GOES ABI, GLM and FDC, GFS, HRRR, NBM, ECMWF, RTMA and more as raster
products; severe-weather records (NWS, SPC, NHC, AWC); derived place, area and storm-cell weather; static maps for
notification attachments; SatDump ground-station integration (the operator builds SatDump separately).

**Intelligence layer.** Entity registry, derived layers, incidents and investigations, pattern of life and trip watch,
situations and storylines, change points, precursor mining, base rates and analogs, a forecast ledger with scoring,
hypotheses and ACH, regional outlooks, impact projection, analysis cycles and assessments, chronicles, reports,
news coverage and media ratings, social sources and narratives. LLM features (Ask, briefings, alert enrichment,
semantic search) work with local Ollama or hosted providers and are optional.

**Access and operations.** Scoped tokens (`open`, `read`, `agent`, `admin`, `peer`), an outbound URL policy against
SSRF, private sources and egress rules, license and provenance terms per source, Prometheus metrics, health probes,
audit events. Client SDKs (Dart, TypeScript, Python, plus EDP provider libraries) in `sdk/`, a command-line client in
`cli/`, an MCP server with OAuth, and plugins.

**Release engineering (new in this release).** `docs/OpenAPI.json` is `1.0.0` and frozen at
`docs/api/v1.0.0/OpenAPI.json`; `./gradlew checkApiCompat` runs `oasdiff` against it. The proposed CI pipeline is in
[CI-Proposal.md](CI-Proposal.md) (not active: no `.gitlab-ci.yml` is added to the repository).

## 2. Supported upstream sources

The ids below are the descriptor ids registered in code (`SourceDescriptor.builder(...)` under
`src/main/java/ai/foresignal`, read for this release). Every source is installed **disabled**; enabling one is the only
thing that makes Foresignal contact its upstream. The running instance's own list, with each source's terms, is
`GET /v1/sources`. [Source-Catalog.md](Source-Catalog.md) holds the per-source details (interval, budget, tier,
policy, license, egress); it also lists upstreams that were surveyed and not implemented. A family (`prefix-*`)
registers one source per reviewed feed or per operator slot.

| Area | Source ids |
|---|---|
| Earthquakes and seismic | `usgs-earthquakes`, `usgs-shakemap`, `emsc-seismic`, `fdsn-*` |
| Aircraft | `adsblol-mil`, `adsblol-area-*`, `adsbfi-mil`, `adsbfi-region-*`, `airplaneslive-mil`, `opensky-states`, `faa-registry`, `derived-aircraft` |
| Aviation safety | `faa-nas-status`, `faa-tfr`, `faa-uasfm`, `faa-uas-*` (feature kit), `awc-metars`, `awc-tafs`, `awc-pireps`, `awc-sigmets`, `awc-gairmets`, `awc-cwas` |
| Vessels and maritime | `aisstream`, `ais-digitraffic`, `ais-kystverket`, `gfw-events`, `nga-msi-warnings`, `imf-portwatch-ports`, `imf-portwatch-chokepoints`, `imf-portwatch-disruptions` (feature kit), `world-port-index` |
| Space | `celestrak-gp`, `celestrak-socrates`, `spacetrack-tip`, `launch-library`, `satellite-passes`, `satnogs-stations`, `sondehub`, `nasa-donki`, `jpl-close-approaches`, `jpl-fireballs`, `spaceports` |
| Space weather | `swpc-alerts`, `swpc-aurora`, `swpc-kp`, `swpc-rtsw`, `swpc-xray` |
| Weather rasters and models | `mrms`, `goes-east-abi`, `goes-west-abi`, `goes-glm`, `goes-fdc`, `nexrad-l3`, `gfs`, `gfs-wave`, `hrrr`, `nbm`, `ecmwf-ifs`, `aifs`, `rtma-ru`, `sst-geopolar` |
| Severe weather and hazards | `nws-alerts`, `nws-gridpoints`, `mrms-probsevere`, `spc-outlooks`, `spc-storm-reports`, `nhc-cyclones`, `nhc-cyclone-cones`, `nhc-wind-probs`, `wpc-heatrisk`, `wpc-ero-d1` … `wpc-ero-d5` (feature kit), `tsunami-warnings` |
| Derived weather | `wx-places`, `wx-areas`, `wx-cells`, `wx-forecast-areas`, `wx-storm-tracks` |
| Fire, smoke and volcanoes | `nasa-firms`, `hms-fire`, `hms-smoke`, `nifc-perimeters`, `inciweb-incidents`, `feature-wfigs-incidents` (feature kit), `cwfis-hotspots`, `cwfis-perimeters`, `cwfis-fwi-stations`, `gvp-volcanoes`, `usgs-hans-volcanoes`, `geonet-volcanoes` |
| Alerts (CAP and civil) | `cap-*`, `meteoalarm-*`, `alerts-in-ua`, `ua-air-raid-siren` |
| Conflict and events | `gdelt-events`, `gdelt-timelines`, `gdelt-articles`, `acled`, `ucdp-ged`, `deepstate-frontline` |
| Humanitarian and disasters | `gdacs`, `reliefweb-disasters`, `copernicus-ems`, `nasa-eonet`, `openfema-declarations`, `unhcr-population`, `who-don`, `travel-advisory-us` |
| Hydrology, coast and environment | `usgs-water`, `nwps-gauges`, `noaa-coops-tides`, `ea-flood-warnings`, `snotel`, `usdm`, `ndbc-buoys`, `openaq`, `airnow-hourly`, `epa-uv`, `epa-radnet`, `safecast-radiation`, `sensor-community`, `cdc-nwss-detections`, `cdc-nwss-wval` |
| Internet and cyber | `ioda-outages`, `cloudflare-radar-outages`, `cloudflare-radar-bgp`, `ris-live`, `ripe-atlas-probes`, `gpsjam-daily`, `status-*` (status pages), `abusech-feodo`, `abusech-urlhaus`, `cisa-kev`, `nvd-cves`, `ct-domain-watch` |
| Energy, markets and series | `eia-930`, `eia-petroleum`, `ercot-public`, `gb-carbon-intensity`, `grid-frequency-gb`, `grid-frequency-fingrid`, `ember-electricity`, `gie-agsi`, `fred-series`, `cftc-cot`, `worldbank-indicators`, `wikimedia-pageviews`, `wiki-edit-surge` |
| Sanctions and filings | `ofac-sdn`, `un-sanctions`, `uk-sanctions-list`, `sec-8k` |
| Transport | `transit-*` and `transit-alerts-*` (GTFS-RT), `gbfs-*`, `amtraker`, `digitraffic-rail`, road events (`open511-drivebc`, `wzdx-*`, `digitraffic-road`, `ndw-situations`, `ndw-srti`) |
| Cameras | `cctv-austin`, `cctv-calgary`, `cctv-caltrans-d*`, `cctv-deldot`, `cctv-digitraffic`, `cctv-drivebc`, `cctv-estonia`, `cctv-nsw`, `cctv-ontario511`, `cctv-tallinn`, `cctv-tfl`, `cctv-vegvesen`, `cctv-warendorf` |
| Radio and mesh | `aprs-is`, `pskreporter-ft8`, `kiwisdr`, `openmhz-systems`, `radio-browser`, `meshtastic-mqtt`, `meshtastic-map`, `meshcore-bridge`, `mesh-reports` |
| Satellite scenes | `stac-sentinel1`, `stac-sentinel2`, `stac-landsat` |
| Citizen science | `inat-observations`, `ebird-notable` |
| Reference datasets | `ourairports`, `wri-power-plants`, `nuclear-plants-wikidata`, `nuclear-reactors-us`, `submarine-cables`, `nps-boundaries`, `osm-infrastructure` |
| News and social | `media-ratings`, `bluesky-jetstream`, `bluesky-search`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram-hashtags`; feed kit bulletins (`feed-cisa-advisories`, `feed-ecdc-threats`, `feed-ncsc-uk`, `feed-nhc-atlantic`, `feed-nhc-eastpacific`, `feed-who-news`) |
| The operator's own receivers (egress `LOCAL_ONLY` by default) | `local-adsb-*`, `station-readsb-*`, `local-sbs-*`, `local-ais-*`, `local-aprs-*`, `local-acars-*`, `local-gpsd-*`, `local-sonde`, `local-remoteid`, `glm-local-*`, `meshtastic-local`, `meshcore-local`, `airq-local-*`, `wxstation-davis-*`, `wxstation-ecowitt-*`, `satdump-*` |
| Derived and intelligence | `derived-*` (AIS gaps, CPA, loitering, rendezvous, GPS jamming, port calls, emergency squawks and others), `pol-*`, `rule-windows`, `foresignal-intel`, `foresignal-analytics`, `foresignal-outlook`, `system-canary` |
| Operator-defined | custom push sources and inbound webhooks, EDP providers, federation peers, operator kit overlays (`op-*`), WebAssembly feed plugins (`wasm-*`) |

Data fetched from an upstream is not covered by Foresignal's license; each provider's terms apply, and sources whose
terms forbid redistribution are served `LOCAL_ONLY`.

## 3. Compose profiles

From `docker-compose.yml`. The broker (`foresignal`) and the database (`db`) bind to `127.0.0.1` by default
(`FORESIGNAL_BIND`, `FORESIGNAL_DB_BIND`).

| Command | Services | Purpose |
|---|---|---|
| `docker compose up -d` | `db`, `foresignal` | The broker and PostgreSQL 17 with PostGIS 3.5 and pgvector (`docker/db`, image `foresignal-db:17-3.5-pgvector`), on `127.0.0.1:8080` and `127.0.0.1:5433` |
| `docker compose up -d db` | `db` | Database only, for running the broker from a working tree (`./gradlew bootRun`) |
| `--profile tailscale` | + `tailscale` (`tailscale/tailscale:v1.102.5`) | HTTPS on the operator's tailnet through Tailscale Serve; needs `TS_AUTHKEY` |
| `--profile local-llm` | + `ollama` (`ollama/ollama:0.40.3`) | A local LLM for Ask, briefings, enrichment and search |
| `--profile web` | + `caddy` (`caddy:2.11.7-alpine`) | HTTPS with HTTP/2 and HTTP/3 and Caddy's local CA, for map and web clients (`FORESIGNAL_HOSTNAME`) |
| `--profile satdump` | + `satdump` (`${SATDUMP_IMAGE:-satdump:local}`) | A SatDump ground station on the host; the operator builds the image from SatDump's own Dockerfile |

The memory profile is chosen with `FORESIGNAL_PROFILE` (`small`, the default, or `large`); the compose file sets the
heap to `-Xmx4g` with G1 and the container limit to `6g` for `small`, and documents `-Xmx8g` with ZGC and `11g` for
`large`.

## 4. Hardware sizing

**No measured figures exist yet.** [Runtime-Verification.md](Runtime-Verification.md) holds the procedure and the
projections; its "Measured" cells are empty because the runtime verification (prompt 143) was not run under the owner
directive of 2026-10-17. The figures below are **projections**, quoted from that document, not measurements:

| Item | Projection |
|---|---|
| Memory profile `small` | `-Xmx4g`, container limit 6 GB (for a 32 GB host) |
| Memory profile `large` | `-Xmx8g`, container limit 11 GB (for a 64 GB host) |
| Idle with 10 sources (NFR-1) | ≤ 512 MB heap |
| Sustained versions (NFR-2) | 20,000/min |
| History growth | 10–15 GB/day |
| WAL | 15–20 GB/day |
| 3-day TRACK window | ≈ 40M rows, ≈ 20 GB |
| Weather products (full set) | 5–8.5 GB/day download, 0.05–0.1 core average decode, 10–12 GB rolling disk |
| Startup to ready (NFR-4) | ≤ 20 s |

Realistic limits for given hardware (sources, dense TRACK feeds, years of retention) and the disk for 1, 3 and 5
years are to be derived from the measured run and written into the Operator Guide.

## 5. Known limitations

- **Trigger resolution under SAMPLED and LATEST.** Trigger edges and samples have *flush resolution* for streams
  (at least 2 s, longer while a batch is in flight) and *poll resolution* for polling sources: a condition that
  starts and ends between two flushes or polls is not seen.
- **`asOf` granularity is one ingest batch.** Point-in-time reads work on the transaction axis (`ingested_at`); a
  valid-time `asOf` is not offered (use `/v1/history`). An `asOf` read requires a `source` or `kind` filter and
  fails with `ASOF_BEYOND_RETENTION` before a source's retention horizon.
- **`asOf` is approximate under SAMPLED and LATEST.** The response carries `approximate: true` when any involved
  source is SAMPLED or LATEST, or the range was downsampled (`PointInTimePlanner`).
- **Skipped verification.** For this release:
  - **No test was run.** The suite (including the new `FlywayBaselineIT`, `FlywaySchemaParityIT`,
    `NoBootstrapDdlTest`, `SourceCatalogParityTest` and `ApiVersionTest`) is written but not executed; the
    ≥ 90 % coverage gate is unverified.
  - **No runtime verification was run** (no start-up, soak, drill or measurement; see section 4).
  - **The Flyway baseline is hand-derived and unverified.** `V1__baseline.sql` was written from the JPA entities
    and the old bootstrap SQL, because no database could be started. It must be compared with a database built by
    the pre-cutover code: run `scripts/db/dump-schema.sh` against both and diff, or run `FlywaySchemaParityIT`.
  - **The `v1.0.0` tag is pending** the owner's test run.
- **Embedding vector slots** (`embedding_a/b`, their HNSW indexes) are not in the baseline: no default dimension is
  configured, so the reindex job creates them at the configured dimension.
- **Readiness does not include catch-up.** Catch-up is its own probe group (`/actuator/health/catchup`); see
  Runtime-Verification section 7 for the open owner decision.

## 6. Upgrade notes

**From a development database built before this release** (Hibernate `ddl-auto=update` plus the bootstrap code):

- Foresignal starts Flyway with `baseline-on-migrate=true` and `baseline-version=1`. A non-empty database without
  `flyway_schema_history` is **adopted at version 1 without running `V1__baseline.sql`**, and a warning is logged.
  Hibernate then validates the existing schema (`ddl-auto=validate`); a table or column the old schema lacks fails
  startup.
- **Check the adopted schema:** run `scripts/db/dump-schema.sh` against the old database and against a fresh
  Flyway-built one, and diff the two.
- **Or start clean:** `docker compose down -v` removes the volumes (**all stored data, including `pgdata` and the
  broker's data volume with `secret.key`**), and the next `docker compose up -d` builds the schema from the baseline.
  Back up first (Operator Guide, "Backups") if anything in them matters.

**External PostgreSQL:** the baseline runs `CREATE EXTENSION IF NOT EXISTS postgis` and
`CREATE EXTENSION IF NOT EXISTS vector`. pgvector is now required at migration time. When Foresignal's database role
is not a superuser, a superuser must create both extensions in the Foresignal database before the first start.

**Configuration:** `spring.jpa.hibernate.ddl-auto` is `validate`; do not override it to `update` (Hibernate would
then diverge from the migrations). Schema changes from now on are Flyway migrations (`V{n}__{description}.sql`,
additive by default; [Adapter-Guide.md](Adapter-Guide.md), "Schema changes (Flyway)").

## 7. Breaking-change policy

**NFR-10: `/v1` is additive-only; breaking changes go to `/v2`.** New operations, optional request fields, response
fields, enum values, error codes and SSE event types may be added to `/v1`; removing or renaming a field, changing a
type, tightening validation or adding a required request field may not. Clients must ignore unknown fields, enum
values and SSE event names. Deprecations carry `deprecated: true` in the spec and the `Deprecation` and `Sunset`
headers, and last at least one minor release.

The gate is `./gradlew checkApiCompat`: `oasdiff breaking docs/api/v1.0.0/OpenAPI.json docs/OpenAPI.json --fail-on
ERR`, using a local `oasdiff` binary, else the Docker image `tufin/oasdiff:v1.33.0`, else skipped with a warning. It is
not wired to `build`; [CI-Proposal.md](CI-Proposal.md) shows it as a CI job.

## 8. Licenses and third-party notices

Foresignal is distributed under the **MIT License** ([LICENSE](../LICENSE), copyright 2026 Adam Allard).

[THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md) records the third-party works incorporated or bundled and the
notices they require: code ported from God's Eye View (MIT) and the PDQ hash from Meta ThreatExchange (BSD), the
libraries a prompt asked to record (for example NetCDF-Java, Orekit, Hipparchus, HiveMQ MQTT Client, jsoup, Apache
PDFBox, Lingua, Nimbus JOSE+JWT, Google Tink and Chicory), bundled data (Natural Earth, GeoNames, Orekit data, map
sprites and color ramps), operator-supplied data (GHSL, OpenStreetMap), and the dependencies of the CLI and SDKs. It
also states the reuse policy: ShadowBroker, Velocity and World Monitor (AGPL-3.0), SatDump and the Meshtastic
firmware and protobufs (GPL-3.0) are reference only, and no code of theirs is in the repository. SatDump is not
distributed with Foresignal.

Data fetched from upstream providers at run time is not covered by Foresignal's license; each provider's terms
continue to apply ([Source-Catalog.md](Source-Catalog.md)).
