# Elaro — Roadmap

One ordered list of prompts that takes Elaro from an empty repository to a production-ready release.

- **Companion docs:** [Executive Summary](Executive-Summary.md), [Product Charter](Product-Charter.md), [Architecture](Architecture.md), [OpenAPI spec](OpenAPI.json), [Source Catalog](Source-Catalog.md).
- **v1.2 design inputs:**
  - [Core Data Model](Core-Data-Model.md) is authoritative for ingest, history, live state, triggers, SSE, asOf and retention.
  - [Weather & Imagery Design](Weather-Imagery-Design.md) is authoritative for rasters, weather and SatDump.
  - [Gap Analysis](Gap-Analysis.md) covers client, output, input and platform gaps, verified.
  - The ChatGPT, Grok and Gemini architecture reviews are folded into the items below.

## 1. Process

1. Each unit of work is a prompt in `/prompts`, named `NNN-<Prompt-Description>.md`.
2. Claude Code runs them in order, either one at a time or unattended with `prompts/000-Run-Sequence.md`.
3. Each prompt starts by reading the earlier Completion Reports and adapting to what was actually built.
4. Each prompt ends by writing `/completion-reports/NNN-CR-<Prompt-Description>.md` with the implementation commit hash.
5. **Checkpoint prompts** (marked ✅ below) run the test suite built so far and fix failures before the next feature work. Prompt 118 does the final full regression and enforces ≥ 90% coverage.

## 2. Guardrails (in every prompt)

1. **Tests:** feature prompts write unit and integration tests targeting ≥ 90% line coverage but **do not run them**. Tests run only in the checkpoint prompts (012, 022, 041, 057, 097) and the final full run (118).
2. **Finish with Compile → Commit → Push.** Compile with `./gradlew compileJava compileTestJava`, commit, push to `origin main`, and put the commit hash in the Completion Report.
3. **Hibernate only during development.** Schema changes go through Hibernate (`ddl-auto=update`) plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns (partitioned history). **No Flyway until prompt 120.**

## 3. Completion Report template

```markdown
# NNN-CR-<Prompt-Description>

| | |
|---|---|
| Prompt | NNN-<Prompt-Description>.md |
| Status | Complete / Partial / Blocked |
| Implementation commit | <full 40-char hash> |
| Date | YYYY-MM-DD |

## Summary
## Files created / modified
## Deviations from the prompt
## Issues found / open questions
## Tests written (not run) — or, for checkpoints: Test results (pass/fail counts, fixes made)
## Compile result
## Notes for the next prompt
```

**Commit sequence:**
1. Commit the implementation and capture its hash as the *implementation commit*.
2. Write the report.
3. Commit the report separately (`NNN: completion report`).
4. Push both. The final chat message states the implementation commit hash.

## 4. The list

Legend: ✅ checkpoint.

### Foundation and core pipeline

| # | Prompt | Scope | Status |
|---|---|---|---|
| 001 | Project-Scaffold | Build, wrapper, app skeleton, Docker, licence, test base | Done |
| 002 | Persistence-Model | Entities, SchemaBootstrap, settings, durations, DB cleaner | Done |
| 003 | Security-And-Secrets | Bootstrap token, AccessPolicy (fail-closed), cipher, install id, version, ProblemDetail, CORS | Done |
| 004 | Source-Framework | SPI with v1.2 ingest input, descriptor (tier, time base, policies), DERIVED/PUSH modes, `LAN_URL`/`INBOX_PATH`, `InvalidInputException` | Running |
| 005 | Upstream-Client-And-Budgets | Budgeted client, FetchTrace, `getRange`/`download`/`listS3`, mbPerDay, usage ingest counters | Ready |
| 006 | Health-Tracking | Status rules, coverage vs format change, storage, budget outlook, HealthRule SPI | Ready |
| 007 | History-Schema-And-Partitions | Partitioned `record_version` (tier × seq), `record_head`/`record_live`/`fetch_log`/`seq_clock`, PartitionManager, DB flags, binds | Ready |
| 008 | Ingest-Normalizer-And-Policy-Engine | Pure core: normalizer, time contract, hashing, policy engine, PersistGate SPI. Gap descriptor extras: `titleField`, `entityKeys`, `fieldSpecs`, `kindDisplay`, `aggBelowZoom`, `start/endField`, `validFrom`, `private` with precision cap | Ready |
| 009 | Ingest-Writer-And-Change-Bus | Single writer (lock, state, clock guard, fold, batch SQL, touch, purge), ChangeBus and LiveBus, TestRecords | Ready |
| 010 | Collector-Scheduler | Scheduler, FetchExecutor with fetch_log, PolicyAwareRecordSink, fetch-now and probe, streaming supervisor | Ready |
| 011 | USGS-Earthquakes-Adapter | First adapter on v1.2 (event vs observed time, volatile `updated`), Adapter Guide (altitude convention, fieldSpecs contract test) | Ready |
| 012 | ✅ Checkpoint-Core-Pipeline | Run and fix all tests from 001–011 | Ready |

### API, alerts and operations

| # | Prompt | Scope | Status |
|---|---|---|---|
| 013 | Sources-API | `/v1/sources/**` with policies, tiers, storage, `/fetches` | Ready |
| 014 | Records-Changes-API | Head ⋈ live reads, asOf (transaction time), `asOfSeq`/watermark, purge horizon + `410 RESYNC_REQUIRED`, timeField, near/sort=distance, fields/precision/simplify, ndjson/geojsonseq/csv, ETags, kind schema | Ready |
| 015 | SSE-Change-Stream | Change and live lanes, touch, resync, reset floor, multiplexed `/v1/events/stream` + PATCH filter, per-token limits | Ready |
| 016 | Vector-Tiles | Live tiles, TileJSON, `agg=grid\|hex`, titles | Ready |
| 017 | Trigger-Engine-And-Alerts | Gate-based edges, cooldown on observed_at, alert lifecycle (resolve/snooze/feedback), set operators, source/kind arrays, active windows, route buffers, rate cap, backtest, `cooldownScope`/`near`, AlertBus | Ready |
| 018 | Notification-Channels | 15 types incl. Home Assistant, Apprise, Zulip; attachments field; edit-in-place refs; persisted notifications; quiet hours, folding; templates with timezone | Ready |
| 019 | Retention-Settings-System | Partition-based retention, kind retention, HeadPurge, consumer clamp, readiness groups, watchdog, capabilities, system history | Ready |
| 020 | OpenAPI-Conformance | Served spec, drift test, event schemas + OAS webhooks, required lint, Postman sync | Ready |
| 021 | End-To-End-Milestone | E2E incl. touches, asOf and channels; Operator Guide (remote access, mDNS, backups, NTP, storage) | Ready |
| 022 | ✅ Checkpoint-Milestone | Run and fix all tests from 001–021 | Ready |

### Platform, agents and intelligence services

| # | Prompt | Scope | Status |
|---|---|---|---|
| 023 | Scoped-API-Tokens | Scopes, stream tickets, restricted tokens, device metadata and pairing code, alwaysAuthenticated, trusted proxies, audit log, private gate | Ready |
| 024 | Inbound-Webhooks-And-Custom-Sources | Custom PUSH sources on v1.2, IngestFormat SPI (alertmanager, cloudevents, cap, mapping), ntfy/Gotify-compatible publish | Ready |
| 025 | MCP-Server | Tools with scopes, output shaping, `set_alert_status`, legacy SSE flag | Ready |
| 026 | LLM-Providers | Anthropic SDK, OpenAI-compatible, Ollama; roles, budgets, usage, health | Ready |
| 027 | Embeddings-And-Semantic-Search | pgvector DB image, worker, zero-downtime dual-index reindex, `/v1/search` | Ready |
| 028 | Ask-Elaro | Tool loop, verified citations, threads, proposed actions, streaming | Ready |
| 029 | Scheduled-Briefings | Digest and LLM briefings, `GET /v1/digest`, HTML renditions | Ready |
| 030 | Alert-Enrichment | LLM context on alerts, enrichment wait | Ready |
| 031 | Web-Push | VAPID, RFC 8291, PushTransport SPI, UnifiedPush | Ready |
| 032 | Native-Mobile-Push | APNs (visible + Notification Service Extension) and FCM v1 transports, `/v1/push/devices` | Ready |
| 033 | Actionable-Notifications-And-Inbox | Signed actions, confirm page, `/v1/notifications` with read cursors, presence deferral, escalation | Ready |
| 034 | Agent-Integrations | OpenClaw skill, MCP client configs, cookbooks (Home Assistant, Node-RED, Grafana, Open WebUI, Shortcuts/Tasker, PyTAK, n8n) | Ready |
| 035 | Access-Perimeter | Restricted-token enforcement, anonymous profile, feed keys, per-client limits, outbound URL policy (SSRF) | Ready |
| 036 | Places-Views-And-Watchlists | Places (point/polygon/route), places AOI, saved views, watchlists with CSV import, moving-anchor "near me" | Ready |
| 037 | Time-Travel-And-Tracks | asOf tiles, `/track`, frames, `/timeline`, MCP `get_state_at`/`get_track` | Ready |
| 038 | Stats-Rollups-And-Glance | `activity_rollup`, `/v1/stats/*`, `/v1/glance`, metric projections | Ready |
| 039 | Private-Sources-And-Personal-Location | Private enforcement everywhere; OwnTracks, OsmAnd/Traccar, Overland, GPSLogger; per-entity export and purge | Ready |
| 040 | Gazetteer-And-Region-Tagging | GeoNames/Natural Earth, `{{near.*}}`, country/admin1 filters | Ready |
| 041 | ✅ Checkpoint-Platform | Run and fix all tests from 001–040 | Ready |

### Weather, imagery and SatDump

| # | Prompt | Scope | Status |
|---|---|---|---|
| 042 | Raster-Products-Core | ProductSpec registry, frames, `.elg` codec, FrameStore, raster-frame records, `/v1/products` | Ready |
| 043 | Media-Store-And-Image-Guard | Content-addressed media, ImageGuard, thumbnails, `/v1/media` | Ready |
| 044 | History-Downsampling | Track-leaf rewrite-and-swap, asOf widening | Ready |
| 045 | Raster-Tiles-And-Styles | Grid defs, renderer, styles and legends, TileJSON, `/image`, `/loop`, render queue, external catalogue | Ready |
| 046 | Radar-And-Satellite-Rasters | NetCDF-Java, MRMS, GOES-East ABI | Ready |
| 047 | Severe-Weather-Record-Sources | `nws-alerts`, `mrms-probsevere`, `spc-outlooks`, `awc-metars` | Ready |
| 048 | Lightning-And-Fire | GOES GLM, GOES FDC, HMS smoke and fire | Ready |
| 049 | SatDump-Station-Core | Station source (`/api` + inbox), passes, RF statistics, thumbnails, xRIT geostationary frames | Ready |
| 050 | Map-Client-Access | Tile keys, CORS set, Caddy `web` profile, frontend recipes | Ready |
| 051 | Model-Grids-And-Wind | GFS/HRRR (God's Eye View port), wind fields, barbs, timeline | Ready |
| 052 | Sampling-And-Place-Weather | DERIVED runtime, `/sample`, `/series`, `/stats`, wx-places, radar cells, MCP weather tools | Ready |
| 053 | MRMS-Second-Wave-And-Gridpoints | Rotation, QPE, FFG, NWS gridpoints, SPC storm reports | Ready |
| 054 | SatDump-Polar-Overlays | GeoTIFF reader, footprints, Orekit swath, remote ingest | Ready |
| 055 | Static-Maps-And-Notification-Attachments | MapRenderer, `/v1/maps/static`, channel attachments | Ready |
| 056 | Weather-Intelligence | Alert weather context, briefing "weather at places", Ask tools | Ready |
| 057 | ✅ Checkpoint-Weather | Run and fix all tests from 001–056 | Ready |

### Interop and integration

| # | Prompt | Scope | Status |
|---|---|---|---|
| 058 | Map-Styles-Feeds-And-KML | MapLibre styles and sprites, Atom/JSON Feed, KML NetworkLink/`gx:Track` | Ready |
| 059 | Mapping-Profiles-And-Device-Presets | RecordMapping, preview, LLM-suggested drafts, presets (Ecowitt, WU, AIS-catcher, ChirpStack/TTS, airrohr, GMC) | Ready |
| 060 | MQTT-Channel-And-HA-Discovery | `mqtt` channel, retained topics, LWT, Home Assistant discovery | Ready |
| 061 | Interactive-Chat-Commands | Telegram and Matrix two-way commands; Charter amendment | Ready |

### Sources

| # | Prompt | Sources | Status |
|---|---|---|---|
| 062 | Aircraft-Adapters | `adsblol-mil`, adsb.lol global or regional, `opensky-states` | Ready |
| 063 | AISStream-Adapter | `aisstream` | Ready |
| 064 | Space-Adapters | `celestrak-gp` (+`active`, SOCRATES, CelesTrak-compatible `gp.php`, Orekit predicted passes), `launch-library`, optional `spacetrack-tip` | Ready |
| 065 | FIRMS-Adapter | `nasa-firms` | Ready |
| 066 | GDELT-Events-Adapter | `gdelt-events` | Ready |
| 067 | Hazard-Adapters | `nhc-cyclones`, `gdacs`, `tsunami-warnings`, shared CAP 1.2 parser | Ready |
| 068 | IODA-Outages-Adapter | `ioda-outages` | Ready |
| 069 | Local-Receivers | Socket transport + codec SPI, readsb v2 (978, stats), station health, Davis/Ecowitt local | Ready |
| 070 | Transit-GTFS-RT | `transit-*` + ServiceAlerts + MobilityData catalogue | Ready |
| 071 | GBFS-Bikeshare | `gbfs-*` | Ready |
| 072 | CCTV-Catalogs | `cctv-*` | Ready |
| 073 | Generic-Feature-And-Feed-Kits | ArcGIS/OGC API Features/WFS families; RSS/Atom/GeoRSS/JSON Feed family | Ready |
| 074 | Road-Events-And-Work-Zones | Open511, WZDx, DATEX II / Digitraffic | Ready |
| 075 | Fire-Perimeters-Radio-Cyclone-Cones | `nifc-perimeters`, `inciweb-incidents`, `cwfis-*`, `radio-browser`, `nhc-cyclone-cones` | Ready |
| 076 | Cyber-Feeds | `cisa-kev`, `abusech-*`, `nvd-cves` (EPSS), `ct-domain-watch` | Ready |
| 077 | Space-Weather-And-Ground-Stations | SWPC split, DONKI, JPL fireballs and close approaches, SatNOGS, TinyGS, SondeHub, `local-sonde` | Ready |
| 078 | Environment-Sensors | OpenAQ, Safecast, EPA RadNet, NDBC, Sensor.Community, AirNow, local air quality | Ready |
| 079 | Hydrology-And-Coast | USGS water (OGC API), NWPS gauges and flood categories, SNOTEL, USDM, CO-OPS tides | Ready |
| 080 | Rail-And-Supplemental-Aircraft | `amtraker`, `digitraffic-rail`, `airplaneslive-mil`, `adsbfi-region-*` | Ready |
| 081 | Conflict-Sources | `deepstate-frontline`, `alerts-in-ua`, `ua-air-raid-siren`, `acled`, `ucdp-ged` | Ready |
| 082 | Radio-Sources | `pskreporter-ft8`, `kiwisdr`, `openmhz-systems`, `aprs-is`, `local-aprs` (Direwolf KISS) | Ready |
| 083 | Mesh-Sources | `meshtastic-mqtt` (Mesh.md rules), `meshtastic-map`, `meshtastic-local`, `meshcore-local` | Ready |
| 084 | Mesh-Radio-Channels | Meshtastic and MeshCore outbound, compact profile, duty cycle, report grammar | Ready |
| 085 | Keyless-AIS | `ais-kystverket`, `ais-digitraffic`, `local-ais` (AIS-catcher), shared AIVDM codec | Ready |
| 086 | Local-RF-Decoders | ACARS/VDL2/HFDL (metadata only), radiosonde_auto_rx, gpsd sentinel, Remote ID, rsudp, SAME | Ready |
| 087 | Aviation-Safety | `faa-tfr`, `faa-nas-status`, `awc-sigmets`, PIREP/G-AIRMET/CWA, UAS facility maps and restrictions | Ready |
| 088 | Maritime-Intelligence | `nga-msi-warnings`, IMF PortWatch (chokepoints and ports), `gfw-events`, USCG Local Notices to Mariners | Ready |
| 089 | CAP-Alerts-And-International-Seismic | CAP family (WMO register), MeteoAlarm, WPC ERO, EMSC WebSocket + FDSN, USGS ShakeMap | Ready |
| 090 | Disasters-And-Humanitarian | EONET, ReliefWeb, UNHCR, WHO DON, Copernicus EMS, HANS/GeoNet volcano levels, OpenFEMA, travel advisories | Ready |
| 091 | Sanctions-And-Filings | OFAC SDN, UK OFSI, UN, SEC 8-K | Ready |
| 092 | Internet-Measurement | Cloudflare Radar (outages, BGP), RIPE Atlas, gpsjam daily, status pages, optional RIS Live | Ready |
| 093 | Energy-And-Markets-Series | EIA petroleum and EIA-930, ERCOT, GIE AGSI, Ember, grid frequency, GB carbon intensity, Kalshi | Ready |
| 094 | Reference-Datasets | WRI power plants, OurAirports, submarine cables, GVP volcanoes, World Port Index, NPS boundaries, PeeringDB, spaceports, nuclear facilities | Ready |
| 095 | Citizen-Science-And-Signals | iNaturalist, eBird, CDC NWSS, Wikipedia edit surge | Ready |
| 096 | Satellite-Scenes-STAC | Earth Search STAC footprints and links (Sentinel/Landsat) | Ready |
| 097 | ✅ Checkpoint-Sources | Run and fix all tests from 001–096 | Ready |

### Intelligence, outputs and hardening

| # | Prompt | Scope | Status |
|---|---|---|---|
| 098 | Entity-Registry | Canonical entities, aliases, keys, confidence and validity, FAA registry without PII, `/v1/entities`, MCP | Ready |
| 099 | Derived-Layers | GPS jamming, AIS gap, loiter, rendezvous, deception, holding patterns, emergency squawks, chokepoint course changes, military air + naval co-occurrence, aircraft fusion, corroboration, port calls, CPA | Ready |
| 100 | Windowed-Rules-And-Anomalies | Count and absence rules, coverage-aware baselines, `anomaly` rule type | Ready |
| 101 | Incidents-Cases-And-Exposure | `intelligence_event` (evidence, confidence, alternatives, detector version), correlation recipes, case files, exposure | Ready |
| 102 | Autonomous-Analyst | Scheduled LLM analyst over events and anomalies; proposes watches and intelligence events | Ready |
| 103 | Backtesting-And-Hypotheses | Rule and hypothesis backtests over retained history | Ready |
| 104 | Infrastructure-Dependency-Mapping | Facility graph (power, telecom, transport, hospitals) and affected-dependency queries | Ready |
| 105 | Weather-Models-And-Storm-Tracking | ECMWF IFS, forecast areas, TITAN-lite tracking, correlation annotators | Ready |
| 106 | Weather-Products-Extended | Extended product list, HRRR smoke fields, HeatRisk, UV, alternate field formats | Ready |
| 107 | SatDump-Extras | Filtered DCS/EMWIN, SatDump 2.0 hook, local GLM, EMWIN fallback, OMM feed | Ready |
| 108 | Map-Interop-And-Offline | WMTS with Time, OGC API Tiles/EDR, MBTiles/PMTiles/COG export, Terrain-RGB, WebP, style fragments | Ready |
| 109 | Licence-And-Provenance | Per-record licence and egress policy, attribution, reliability grades, instance signing key (JWKS, detached JWS) | Ready |
| 110 | Outbound-Sinks | Sink SPI with cursors; MQTT retained state; CoT/TAK; readsb `aircraft.json`/SBS; NMEA AIVDM; APRS-IS serve; CloudEvents | Ready |
| 111 | Exports-Archive-And-Offline-Packs | GeoParquet/GeoPackage/FlatGeobuf/GPX exports, `atSeq` snapshots and holds, cold archive, PMTiles packs | Ready |
| 112 | Standards-And-Headerless-Outputs | OGC API Features, CAP output, iCalendar, share links | Ready |
| 113 | Federation-And-Edge-Relay | Elaro-to-Elaro replication with provenance and loop prevention; compact intelligence messages for MeshDTN | Ready |
| 114 | Pattern-Of-Life-And-Trip-Watch | Pattern of life, trip and commute corridor watch, kinematic CPA/ETA | Ready |
| 115 | Exercise-Replay-And-Alert-Quality | Exercise/replay mode, alert-quality feedback tuning | Ready |
| 116 | Adapter-Doctor | LLM-assisted diagnosis: probe, compare with fixture, explain, propose parser fix | Ready |
| 117 | Client-SDKs-CLI-And-Extensibility | Dart/TypeScript/Python SDK generation with SSE layer, picocli CLI, MCP remote authorization (RFC 9728), plugin SPI (WASM-first), science and security formats (CZML, QuakeML, STIX), audio briefings and translation | Ready |
| 118 | ✅ Full-Test-Run-And-Fix | Full regression, ≥ 90% coverage gate | Ready |
| 119 | Runtime-Verification | Live soak: storage, WAL, persist ratio, storms, restore drill, Tailscale profile, metrics drift | Ready |
| 120 | Production-Hardening | Docs consolidation into Architecture/Charter, Flyway baseline (tiered DDL, all indexes), `ddl-auto=validate`, oasdiff, release `v1.0.0` | Ready |

**Outside this repository:** client applications built on Elaro have their own repositories and roadmaps:
- the web globe and operator console
- Flutter desktop and mobile apps
- the Home Assistant HACS integration
- embeddable widgets
- iOS Live Activities and wearables

They consume the API, SSE, MCP, push and SDKs above.

## 5. Renumbering map

Some older prompt text, and the design docs, still use earlier numbers. Translate them with these tables.

**Roadmap v1.1 → v1.2:**

| v1.1 | v1.2 | v1.1 | v1.2 |
|---|---|---|---|
| 007 | 008 + 009 (split) | 018 | 021 |
| 008 | 010 | 019 | 023 |
| 009 | 011 | 020 | 024 |
| 010 | 013 | 021 | 025 |
| 011 | 014 | 022 | 026 |
| 012 | 015 | 023 | 027 |
| 013 | 016 | 024 | 028 |
| 014 | 017 | 025 | 029 |
| 015 | 018 | 026 | 030 |
| 016 | 019 | 027 | 031 |
| 017 | 020 | 028 | 034 |

| v1.1 | v1.2 | v1.1 | v1.2 |
|---|---|---|---|
| 029 (aircraft) | 062 | 046 | 082 |
| 030 | 063 | 047 | 083 |
| 031 | 064 | 048 | 085 |
| 032 | 065 | 049 | 087 |
| 033 | 066 | 050 | 088 |
| 034 | 067 | 051 | 089 |
| 035 | 068 | 052 | 090 |
| 036 | 069 | 053 | 091 |
| 037 | 070 | 054 | 092 |
| 038 | 071 | 055 | 093 |
| 039 | 072 | 056 | 094 |
| 040 | 075 | 057 | 099 |
| 041 | 076 | 058 | 042–056 (weather block) |
| 042 | 077 | 059 | 109 |
| 043 | 078 | 060 | 116 |
| 044 | 080 | 061 | 118 |
| 045 | 081 | 062 | 119 |
| | | 063 | 120 |

**Core-Data-Model.md labels:** 006a → 007, 007 → 008, 007b → 009, 030a → 044. Its "063" (Flyway) is now 120.

**Weather-Imagery-Design.md labels** (its §10 list) → v1.2:

| Design | v1.2 | Design | v1.2 |
|---|---|---|---|
| 029 | 042 | 037 | 050 |
| 030 | 043 | 038 | 051 |
| 031 | 045 | 039 | 052 |
| 032 | 046 | 040 | 053 |
| 033 | 036 (Places merged) | 041 | 054 |
| 034 | 047 | 042 | 055 |
| 035 | 048 | 043 | 056 |
| 036 | 049 | 073 | 105 |
| | | 074 | 106 |
| | | 075 | 107 |
| | | 076 | 108 |

Its "080" (runtime verification) is now 119, and its "081" (Flyway) is now 120.

**Gap-Analysis.md labels:**

| Gap label | v1.2 | Gap label | v1.2 |
|---|---|---|---|
| 027a | 032 | 039b | 074 |
| 027b | 033 | 043a | 079 |
| 028a | 035 | 047a | 084 |
| 028b | 036 | 048a | 086 |
| 028c | 037 | 056a | 098 |
| 028d | 038 | 057a | 100 |
| 028e | 039 | 057b | 101 |
| 028f | 040 | 059a | 110 |
| 028g | 058 | 059b | 111 |
| 028h | 059 | 059c | 112 |
| 028i | 060 | 060a | 117 |
| 028j | 061 | | |
| 039a | 073 | | |

Its "extend NNN" items apply to the renumbered prompt: translate NNN with the v1.1 → v1.2 table above.

## 6. API tooling

- `postman/Elaro.postman_collection.json` and `postman/Elaro-Local.postman_environment.json` are generated from `docs/OpenAPI.json` by `scripts/generate-postman.py`. Any prompt that changes the spec regenerates them.
- Prompt 020 adds a contract test that fails when any operation is missing from the collection.
