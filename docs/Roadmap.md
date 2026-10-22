# Foresignal — Roadmap

One ordered list of prompts that took Foresignal from an empty repository to a production-ready release. **All 144 prompts are done.** Version 1.0.0 is code-complete; the full test run, the runtime verification and the `v1.0.0` tag are pending the owner (§2).

- **Companion docs:** [Executive Summary](Executive-Summary.md), [Product Charter](Product-Charter.md), [Architecture](Architecture.md), [OpenAPI spec](OpenAPI.json), [Source Catalog](Source-Catalog.md), [Runtime Verification](Runtime-Verification.md), [Release Notes v1.0.0](Release-Notes-v1.0.0.md).
- **Authoritative design:** the [Product Charter](Product-Charter.md) and the [Architecture](Architecture.md) (v2.0), with the [Intelligence Design](Intelligence-Design.md) for the intelligence layer (prompts 116–139), the [Client Contract](Client-Contract.md) for object references, relations, workspaces and deep links (prompt 129, the frontends), and the [Invariants](Invariants.md) index of the invariants every checkpoint verifies.
- **Design history:** the v1.2 design inputs were folded into the Architecture v2.0 on 2026-10-18 and archived under [design-history/](design-history/):
  - [Core Data Model](design-history/Core-Data-Model.md): ingest, history, live state, triggers, SSE, asOf and retention.
  - [Weather & Imagery Design](design-history/Weather-Imagery-Design.md): rasters, weather and SatDump.
  - [Gap Analysis](design-history/Gap-Analysis.md): client, output, input and platform gaps.
  - The ChatGPT, Grok and Gemini architecture reviews are folded into the items below.

## 1. Process

0. **API-first.** `docs/OpenAPI.json` is the complete target API, authored before implementation (each operation tagged with the prompt that implements it and `planned` or `implemented`). Clients are built in parallel against it; prompts implement it and amend it only with recorded deviations (Conventions "API-first"). Since v1.0.0 the spec is frozen at `docs/api/v1.0.0/OpenAPI.json`, and `./gradlew checkApiCompat` (oasdiff) reports breaking changes against it.
1. Each unit of work is a prompt in `/prompts`, named `NNN-<Prompt-Description>.md`.
2. Claude Code runs them in order, either one at a time or unattended with `prompts/000-Run-Sequence.md`.
3. Each prompt starts by reading the earlier Completion Reports and adapting to what was actually built.
4. Each prompt ends by writing `/completion-reports/NNN-CR-<Prompt-Description>.md` with the implementation commit hash.
5. **Checkpoint prompts** (marked ✅ below) were planned to run the test suite built so far and fix failures before the next feature work, with prompt 142 doing the final full regression and enforcing ≥ 90% coverage. See §2 for what actually ran.
6. **Collect early.** After 022 (the core milestone), deploy a collecting instance on the bench server with the event-class sources enabled and the analytic retention defaults (`docs/Conventions.md`), and upgrade it as the build continues. History cannot be backfilled from live feeds, and the intelligence block (116–139) is far more useful on weeks of real history. Reference-history imports (121) supply the long outside view.

## 2. Guardrails (in every prompt)

1. **Tests.** The policy changed during the build:
   - **001–097:** feature prompts wrote unit and integration tests targeting ≥ 90% line coverage but did not run them. The checkpoints ran the whole suite and fixed it until green: 012 (1,577 tests), 022 (2,699), 041 (4,669), 057 (5,682) and 097 (8,084). The sanctioned narrow runs of 007, 008 and 009 each ran only their own listed test classes once, because everything else builds on partitioning, hashing and the writer.
   - **098–123:** tests written and not run, awaiting checkpoint 124.
   - **Owner directive of 2026-10-17:** no tests are run for any prompt, including the checkpoints 124, 139 and 142 and prompts 143–144. Each of them compiles only and records that tests were deferred. The performance smoke, the capacity and soak gates of 143, and the full suite with coverage (`./gradlew test jacocoTestReport jacocoTestCoverageVerification`) are the owner's to run at code-complete.
2. **Finish with Compile → Commit → Push.** Compile with `./gradlew compileJava compileTestJava`, commit, push to `origin main`, and put the commit hash in the Completion Report.
3. **Schema ownership.** Until 143, schema changes went through Hibernate (`ddl-auto=update`) plus the bootstrap SQL that the [Core Data Model](design-history/Core-Data-Model.md) §9 assigned (partitioned history). From 144 the schema is owned by Flyway (`src/main/resources/db/migration/V1__baseline.sql` and later `V{n}__{description}.sql` migrations, additive by default, never edited once applied), and Hibernate only validates (`ddl-auto=validate`).
4. **No CI configuration** is added to the repository; the proposed pipeline is in [CI Proposal](CI-Proposal.md).

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

Legend: ✅ checkpoint. "Done (report: Partial)" means the prompt was implemented and its Completion Report lists parts that could not be built or verified; "compile-only" means the prompt ran under the owner directive of 2026-10-17 (§2).

### Foundation and core pipeline

| # | Prompt | Scope | Status |
|---|---|---|---|
| 001 | Project-Scaffold | Build, wrapper, app skeleton, Docker, license, test base | Done |
| 002 | Persistence-Model | Entities, SchemaBootstrap, settings, durations, DB cleaner | Done |
| 003 | Security-And-Secrets | Bootstrap token, AccessPolicy (fail-closed), cipher, install id, version, ProblemDetail, CORS | Done |
| 004 | Source-Framework | SPI with v1.2 ingest input, descriptor (tier, time base, policies), DERIVED/PUSH modes, `LAN_URL`/`INBOX_PATH`, `InvalidInputException` | Done |
| 005 | Upstream-Client-And-Budgets | Budgeted client, FetchTrace, `getRange`/`download`/`listS3`, mbPerDay, usage ingest counters | Done |
| 006 | Health-Tracking | Status rules, coverage vs format change, storage, budget outlook, HealthRule SPI | Done |
| 007 | History-Schema-And-Partitions | Partitioned `record_version` (tier × seq), `record_head`/`record_live`/`fetch_log`/`seq_clock`, PartitionManager, DB flags, binds | Done |
| 008 | Ingest-Normalizer-And-Policy-Engine | Pure core: normalizer, time contract, hashing, policy engine, PersistGate SPI. Gap descriptor extras: `titleField`, `entityKeys`, `fieldSpecs`, `kindDisplay`, `aggBelowZoom`, `start/endField`, `validFrom`, `private` with precision cap | Done |
| 009 | Ingest-Writer-And-Change-Bus | Single writer (lock, state, clock guard, fold, batch SQL, touch, purge), ChangeBus and LiveBus, TestRecords | Done |
| 010 | Collector-Scheduler | Scheduler, FetchExecutor with fetch_log, PolicyAwareRecordSink, fetch-now and probe, streaming supervisor | Done |
| 011 | USGS-Earthquakes-Adapter | First adapter on v1.2 (event vs observed time, volatile `updated`), Adapter Guide (altitude convention, fieldSpecs contract test) | Done |
| 012 | ✅ Checkpoint-Core-Pipeline | Run and fix all tests from 001–011 | Done |

### API, alerts and operations

| # | Prompt | Scope | Status |
|---|---|---|---|
| 013 | Sources-API | `/v1/sources/**` with policies, tiers, storage, `/fetches` | Done |
| 014 | Records-Changes-API | Head ⋈ live reads, asOf (transaction time), `asOfSeq`/watermark, purge horizon + `410 RESYNC_REQUIRED`, timeField, near/sort=distance, fields/precision/simplify, ndjson/geojsonseq/csv, ETags, kind schema | Done |
| 015 | SSE-Change-Stream | Change and live lanes, touch, resync, reset floor, multiplexed `/v1/events/stream` + PATCH filter, per-token limits | Done |
| 016 | Vector-Tiles | Live tiles, TileJSON, `agg=grid\|hex`, titles | Done |
| 017 | Trigger-Engine-And-Alerts | Gate-based edges, cooldown on observed_at, alert lifecycle (resolve/snooze/feedback), set operators, source/kind arrays, active windows, route buffers, rate cap, backtest, `cooldownScope`/`near`, AlertBus | Done |
| 018 | Notification-Channels | 15 types incl. Home Assistant, Apprise, Zulip; attachments field; edit-in-place refs; persisted notifications; quiet hours, folding; templates with timezone | Done |
| 019 | Retention-Settings-System | Partition-based retention, kind retention, HeadPurge, consumer clamp, readiness groups, watchdog, capabilities, system history | Done |
| 020 | OpenAPI-Conformance | Served spec, drift test, event schemas + OAS webhooks, required lint, Postman sync | Done |
| 021 | End-To-End-Milestone | E2E incl. touches, asOf and channels; Operator Guide (remote access, mDNS, backups, NTP, storage) | Done |
| 022 | ✅ Checkpoint-Milestone | Run and fix all tests from 001–021 | Done |

### Platform, agents and intelligence services

| # | Prompt | Scope | Status |
|---|---|---|---|
| 023 | Scoped-API-Tokens | Scopes, stream tickets, restricted tokens, device metadata and pairing code, alwaysAuthenticated, trusted proxies, audit log, private gate | Done |
| 024 | Inbound-Webhooks-And-Custom-Sources | Custom PUSH sources on v1.2, IngestFormat SPI (alertmanager, cloudevents, cap, mapping), ntfy/Gotify-compatible publish | Done |
| 025 | MCP-Server | Tools with scopes, output shaping, `set_alert_status`, legacy SSE flag | Done |
| 026 | LLM-Providers | Anthropic SDK, OpenAI-compatible, Ollama; roles, budgets, usage, health | Done |
| 027 | Embeddings-And-Semantic-Search | pgvector DB image, worker, zero-downtime dual-index reindex, `/v1/search` | Done |
| 028 | Ask-Foresignal | Tool loop, verified citations, threads, proposed actions, streaming | Done |
| 029 | Scheduled-Briefings | Digest and LLM briefings, `GET /v1/digest`, HTML renditions | Done |
| 030 | Alert-Enrichment | LLM context on alerts, enrichment wait | Done |
| 031 | Web-Push | VAPID, RFC 8291, PushTransport SPI, UnifiedPush | Done |
| 032 | Native-Mobile-Push | APNs (visible + Notification Service Extension) and FCM v1 transports, `/v1/push/devices` | Done |
| 033 | Actionable-Notifications-And-Inbox | Signed actions, confirm page, `/v1/notifications` with read cursors, presence deferral, escalation | Done |
| 034 | Agent-Integrations | OpenClaw skill, MCP client configs, cookbooks (Home Assistant, Node-RED, Grafana, Open WebUI, Shortcuts/Tasker, PyTAK, n8n) | Done |
| 035 | Access-Perimeter | Restricted-token enforcement, anonymous profile, feed keys, per-client limits, outbound URL policy (SSRF) | Done |
| 036 | Places-Views-And-Watchlists | Places (point/polygon/route), places AOI, saved views, watchlists with CSV import, moving-anchor "near me" | Done |
| 037 | Time-Travel-And-Tracks | asOf tiles, `/track`, frames, `/timeline`, MCP `get_state_at`/`get_track` | Done |
| 038 | Stats-Rollups-And-Glance | `activity_rollup`, `/v1/stats/*`, `/v1/glance`, metric projections | Done |
| 039 | Private-Sources-And-Personal-Location | Private enforcement everywhere; OwnTracks, OsmAnd/Traccar, Overland, GPSLogger; per-entity export and purge | Done |
| 040 | Gazetteer-And-Region-Tagging | GeoNames/Natural Earth, `{{near.*}}`, country/admin1 filters | Done |
| 041 | ✅ Checkpoint-Platform | Run and fix all tests from 001–040 | Done |

### Weather, imagery and SatDump

| # | Prompt | Scope | Status |
|---|---|---|---|
| 042 | Raster-Products-Core | ProductSpec registry, frames, `.fsg` codec, FrameStore, raster-frame records, `/v1/products` | Done |
| 043 | Media-Store-And-Image-Guard | Content-addressed media, ImageGuard, thumbnails, `/v1/media` | Done |
| 044 | History-Downsampling | Track-leaf rewrite-and-swap, asOf widening | Done |
| 045 | Raster-Tiles-And-Styles | Grid defs, renderer, styles and legends, TileJSON, `/image`, `/loop`, render queue, external catalog | Done |
| 046 | Radar-And-Satellite-Rasters | NetCDF-Java, MRMS, GOES-East ABI | Done |
| 047 | Severe-Weather-Record-Sources | `nws-alerts`, `mrms-probsevere`, `spc-outlooks`, `awc-metars` | Done |
| 048 | Lightning-And-Fire | GOES GLM, GOES FDC, HMS smoke and fire | Done |
| 049 | SatDump-Station-Core | Station source (`/api` + inbox), passes, RF statistics, thumbnails, xRIT geostationary frames | Done |
| 050 | Map-Client-Access | Tile keys, CORS set, Caddy `web` profile, frontend recipes | Done |
| 051 | Model-Grids-And-Wind | GFS/HRRR (God's Eye View port), wind fields, barbs, timeline | Done |
| 052 | Sampling-And-Place-Weather | DERIVED runtime, `/sample`, `/series`, `/stats`, wx-places, radar cells, MCP weather tools | Done |
| 053 | MRMS-Second-Wave-And-Gridpoints | Rotation, QPE, FFG, NWS gridpoints, SPC storm reports | Done |
| 054 | SatDump-Polar-Overlays | GeoTIFF reader, footprints, Orekit swath, remote ingest | Done |
| 055 | Static-Maps-And-Notification-Attachments | MapRenderer, `/v1/maps/static`, channel attachments | Done |
| 056 | Weather-Intelligence | Alert weather context, briefing "weather at places", Ask tools | Done |
| 057 | ✅ Checkpoint-Weather | Run and fix all tests from 001–056 | Done |

### Interop and integration

| # | Prompt | Scope | Status |
|---|---|---|---|
| 058 | Map-Styles-Feeds-And-KML | MapLibre styles and sprites, Atom/JSON Feed, KML NetworkLink/`gx:Track` | Done |
| 059 | Mapping-Profiles-And-Device-Presets | RecordMapping, preview, LLM-suggested drafts, presets (Ecowitt, WU, AIS-catcher, ChirpStack/TTS, airrohr, GMC) | Done |
| 060 | MQTT-Channel-And-HA-Discovery | `mqtt` channel, retained topics, LWT, Home Assistant discovery | Done |
| 061 | Interactive-Chat-Commands | Telegram and Matrix two-way commands; Charter amendment | Done |

### Sources

| # | Prompt | Sources | Status |
|---|---|---|---|
| 062 | Aircraft-Adapters | `adsblol-mil`, adsb.lol global or regional, `opensky-states` | Done |
| 063 | AISStream-Adapter | `aisstream` | Done |
| 064 | Space-Adapters | `celestrak-gp` (+`active`, SOCRATES, CelesTrak-compatible `gp.php`, Orekit predicted passes), `launch-library`, optional `spacetrack-tip` | Done |
| 065 | FIRMS-Adapter | `nasa-firms` | Done (report: Partial) |
| 066 | GDELT-Events-Adapter | `gdelt-events` | Done |
| 067 | Hazard-Adapters | `nhc-cyclones`, `gdacs`, `tsunami-warnings`, shared CAP 1.2 parser | Done |
| 068 | IODA-Outages-Adapter | `ioda-outages` | Done |
| 069 | Local-Receivers | Socket transport + codec SPI, readsb v2 (978, stats), station health, Davis/Ecowitt local | Done |
| 070 | Transit-GTFS-RT | `transit-*` + ServiceAlerts + MobilityData catalog | Done |
| 071 | GBFS-Bikeshare | `gbfs-*` | Done |
| 072 | CCTV-Catalogs | `cctv-*` | Done |
| 073 | Generic-Feature-And-Feed-Kits | ArcGIS/OGC API Features/WFS families; RSS/Atom/GeoRSS/JSON Feed family | Done |
| 074 | Road-Events-And-Work-Zones | Open511, WZDx, DATEX II / Digitraffic | Done |
| 075 | Fire-Perimeters-Radio-Cyclone-Cones | `nifc-perimeters`, `inciweb-incidents`, `cwfis-*`, `radio-browser`, `nhc-cyclone-cones` | Done |
| 076 | Cyber-Feeds | `cisa-kev`, `abusech-*`, `nvd-cves` (EPSS), `ct-domain-watch` | Done |
| 077 | Space-Weather-And-Ground-Stations | SWPC split, DONKI, JPL fireballs and close approaches, SatNOGS, TinyGS, SondeHub, `local-sonde` | Done |
| 078 | Environment-Sensors | OpenAQ, Safecast, EPA RadNet, NDBC, Sensor.Community, AirNow, local air quality | Done |
| 079 | Hydrology-And-Coast | USGS water (OGC API), NWPS gauges and flood categories, SNOTEL, USDM, CO-OPS tides | Done |
| 080 | Rail-And-Supplemental-Aircraft | `amtraker`, `digitraffic-rail`, `airplaneslive-mil`, `adsbfi-region-*` | Done |
| 081 | Conflict-Sources | `deepstate-frontline`, `alerts-in-ua`, `ua-air-raid-siren`, `acled`, `ucdp-ged` | Done |
| 082 | Radio-Sources | `pskreporter-ft8`, `kiwisdr`, `openmhz-systems`, `aprs-is`, `local-aprs` (Direwolf KISS) | Done |
| 083 | Mesh-Sources | `meshtastic-mqtt` (Mesh.md rules), `meshtastic-map`, `meshtastic-local`, `meshcore-local` | Done |
| 084 | Mesh-Radio-Channels | Meshtastic and MeshCore outbound, compact profile, duty cycle, report grammar | Done |
| 085 | Keyless-AIS | `ais-kystverket`, `ais-digitraffic`, `local-ais` (AIS-catcher), shared AIVDM codec | Done |
| 086 | Local-RF-Decoders | ACARS/VDL2/HFDL (metadata only), radiosonde_auto_rx, gpsd sentinel, Remote ID, rsudp, SAME | Done |
| 087 | Aviation-Safety | `faa-tfr`, `faa-nas-status`, `awc-sigmets`, PIREP/G-AIRMET/CWA, UAS facility maps and restrictions | Done |
| 088 | Maritime-Intelligence | `nga-msi-warnings`, IMF PortWatch (chokepoints and ports), `gfw-events`; USCG Local Notices to Mariners and `gfw-sar-detections` not built | Done |
| 089 | CAP-Alerts-And-International-Seismic | CAP family (WMO register), MeteoAlarm, WPC ERO, EMSC WebSocket + FDSN, USGS ShakeMap | Done |
| 090 | Disasters-And-Humanitarian | EONET, ReliefWeb, UNHCR, WHO DON, Copernicus EMS, HANS/GeoNet volcano levels, OpenFEMA, travel advisories | Done |
| 091 | Sanctions-And-Filings | OFAC SDN, UK OFSI, UN, SEC 8-K | Done |
| 092 | Internet-Measurement | Cloudflare Radar (outages, BGP), RIPE Atlas, gpsjam daily, status pages, optional RIS Live | Done |
| 093 | Energy-And-Markets-Series | EIA petroleum and EIA-930, ERCOT, GIE AGSI, Ember, grid frequency, GB carbon intensity; prediction markets evaluated and excluded | Done |
| 094 | Reference-Datasets | WRI power plants, OurAirports, submarine cables, GVP volcanoes, World Port Index, NPS boundaries, PeeringDB, spaceports, nuclear facilities | Done |
| 095 | Citizen-Science-And-Signals | iNaturalist, eBird, CDC NWSS, Wikipedia edit surge | Done |
| 096 | Satellite-Scenes-STAC | Earth Search STAC footprints and links (Sentinel/Landsat) | Done |
| 097 | ✅ Checkpoint-Sources | Run and fix all tests from 001–096 | Done |

### Intelligence foundations, outputs and integration

| # | Prompt | Scope | Status |
|---|---|---|---|
| 098 | Entity-Registry | Canonical entities, aliases, keys, confidence and validity, FAA registry without PII, `/v1/entities`, MCP | Done |
| 099 | Derived-Layers | GPS jamming, AIS gap, loiter, rendezvous, deception, holding patterns, emergency squawks, chokepoint course changes, military air + naval co-occurrence, aircraft fusion, corroboration, port calls, CPA | Done |
| 100 | Windowed-Rules-And-Anomalies | Count and absence rules, coverage-aware baselines, `anomaly` rule type | Done |
| 101 | Incidents-Investigations-And-Exposure | `intelligence_event` (evidence, confidence, alternatives, detector version), correlation recipes, investigations (case files), exposure | Done |
| 102 | Autonomous-Analyst | Scheduled LLM analyst over events and anomalies; proposes watches and intelligence events | Done |
| 103 | Backtesting-And-Precursor-Tests | Rule, recipe and precursor-test backtests over retained history ("does X precede Y?"); alert quality | Done |
| 104 | Infrastructure-Dependency-Mapping | Facility graph (power, telecom, transport, hospitals) and affected-dependency queries | Done |
| 105 | Weather-Models-And-Storm-Tracking | ECMWF IFS, forecast areas, TITAN-lite tracking, correlation annotators | Done (report: Partial) |
| 106 | Weather-Products-Extended | Extended product list, HRRR smoke fields, HeatRisk, UV, alternate field formats | Done (report: Partial) |
| 107 | SatDump-Extras | Filtered DCS/EMWIN, SatDump 2.0 hook, local GLM, EMWIN fallback, OMM feed | Done |
| 108 | Map-Interop-And-Offline | WMTS with Time, OGC API Tiles/EDR, MBTiles/PMTiles/COG export, Terrain-RGB, WebP, style fragments | Done |
| 109 | Licence-And-Provenance | Per-record license and egress policy, attribution, reliability grades, instance signing key (JWKS, detached JWS) | Done |
| 110 | Outbound-Sinks | Sink SPI with cursors; MQTT retained state; CoT/TAK; readsb `aircraft.json`/SBS; NMEA AIVDM; APRS-IS serve; CloudEvents | Done |
| 111 | Exports-Archive-And-Offline-Packs | GeoParquet/GeoPackage/FlatGeobuf/GPX exports, `atSeq` snapshots and holds, cold archive, PMTiles packs | Done |
| 112 | Standards-And-Headerless-Outputs | OGC API Features, CAP output, iCalendar, share links | Done |
| 113 | Federation-And-Edge-Relay | Foresignal-to-Foresignal replication with provenance and loop prevention; compact intelligence messages for MeshDTN | Done |
| 114 | Pattern-Of-Life-And-Trip-Watch | Pattern of life, trip and commute corridor watch, kinematic CPA/ETA | Done |
| 115 | Exercise-Replay-And-Alert-Quality | Exercise/replay mode, alert-quality feedback tuning | Done |

### Intelligence, collaboration and the client contract (v1.3)

Design: [Intelligence Design](Intelligence-Design.md); client contract: [Client Contract](Client-Contract.md).

| # | Prompt | Scope | Status |
|---|---|---|---|
| 116 | Evidence-Identity-And-Observability | Originators, canonical events and evidence groups (independence, echo flags); time-versioned observability (capture–recapture, presence proxy, receiver range) and coverage tiles; analysis manifests and replay; retrofits 099–103 | Done |
| 117 | Market-And-Attention-Signals | CFTC COT (with history backfill), FRED (local-only), Wikimedia pageviews, GDELT timelines, World Bank indicators; attention topics; series metadata for event studies | Done |
| 118 | Situations-And-Storylines | Persistent storylines: deterministic linker, lifecycle, daily snapshots and "what changed", themes, self-refreshing briefs, merge/split, corroboration from evidence groups | Done |
| 119 | Change-Points-And-Event-Studies | Region rollups, `SeriesReader`, analysis-series registry, findings (`foresignal-analytics`), CUSUM and BOCPD change points, event studies, pre-event signals, unexplained moves, co-occurrence mining with FDR | Done |
| 120 | Precursor-Mining-And-Library | Candidates over canonical events and series, FDR, out-of-sample validation, precursor library and lifecycle, live `precursor-active` | Done |
| 121 | Base-Rates-And-Historical-Analogs | Reference-history imports (UCDP, ACLED, USGS, GDACS), base-rate library with intervals, reporting density, situation signatures and analog search | Done |
| 122 | Forecast-Ledger-And-Scoring | Questions and detectability-aware resolvers, immutable forecasts, baseline forecasters, Brier/log/RPS/CRPS, CORP calibration, bias monitor, ensemble with Platt recalibration, leak-free retrospective evaluation, ICD 203 wording | Done |
| 123 | Analytic-Benchmark-Harness | Planted-truth scenarios and a historical corpus; false discoveries, recall, delay, duplicate resistance, calibration; thresholds; gating of autonomous features | Done |
| 124 | ✅ Checkpoint-Intelligence-Foundations | Run and fix all tests from 001–123; golden statistics, determinism, no look-ahead, duplicate inflation, manifests, ledger integrity, egress, durable cursors, synthetic benchmark baseline, invariants | Done (compile-only; tests not run) |
| 125 | Hypotheses-Indicators-And-ACH | Rolling hypothesis sets: priors from base rates, indicators that watch for themselves, deterministic revisions (evidence groups, detectability-scaled absence), ACH, key assumptions, speculative links, information value and collection gaps, drafting | Done |
| 126 | Series-Forecasts-Regional-Outlooks-And-Indices | Method-selected series forecasts, CAST-style regional 4-week outlooks with published accuracy, decomposed regional indices with weights fitted to outcomes | Done |
| 127 | Impact-Projection | Hazard forecasts × places × infrastructure → probabilistic impact statements, scored in the ledger | Done |
| 128 | Analysis-Cycles-And-Assessments | Daily/weekly/monthly cycles: deterministic packet, LLM analyst, red team, forecaster and proposer roles, ICD 203 assessments that build on the previous one, export bundles, monthly maintenance, intel board with operator vs machine | Done |
| 129 | Object-Graph-Workspaces-And-Annotations | Object references and envelopes, time-aware relation graph and common dependencies, annotations with verdicts and precision metrics, workspaces with live context sync and deep links, sensor discovery, `/v1/contract` | Done |
| 130 | Principals-Groups-Sharing-And-Collaboration | Principals and groups, notification preferences, access lists, TLP 2.0, tags, threaded comments and mentions, attachments and Admiralty-graded evidence items, investigations (owners, members, links), saved searches | Done |
| 131 | Chronicles | Named monitoring threads: layer bundles with capture modes, their own partitioned history, thread, replay, storage estimates and caps, lifecycle, proposals from the analysis cycles, sharing | Done |
| 132 | Reports-And-Intelligence-Exchange | Reports from tags, selections, investigations, chronicles and situations; publish with pinned evidence and TLP banners; send to principals; signed packages between instances; read-only links | Done |
| 133 | News-Coverage-And-Media-Bias | News articles (metadata only), outlet registry with licensed lean, factuality and ownership ratings, story clustering, coverage balance, blindspots and divergence in situations, hypotheses and assessments | Done (report: Partial) |
| 134 | Chronicle-Network-And-Discovery | Related chronicles from privacy-preserving fingerprints (local and federated), discoverability under TLP, opaque links and clusters, access requests, evidence counted once, circular-reporting detection | Done |
| 135 | Social-Sources-And-Volume | Operator-enabled social adapters (Bluesky Jetstream on by default; Bluesky search, Mastodon, Reddit, YouTube, X with spend cap, Threads, Instagram hashtags; other platforms through EDP providers), posts, deletion sync, denominator-scaled volume baselines, velocity findings, social-attention observability | Done (report: Partial) |
| 136 | Narratives-Claims-And-Media | Narratives counted by origin, multilingual claim extraction, claims as graded evidence, PDQ image reuse, cross-platform propagation, coordination indicators | Done (report: Partial) |
| 137 | Social-Physical-Correlation | Claims versus independent physical evidence (corroborated with lead time, unobserved with adequate coverage, contradicted), attention gaps, cue-driven searches, out-of-sample lead–lag and ledger scoring | Done |
| 138 | External-Data-Bindings-And-Provider-Kit | Bindings for atomic EDP streams (series, canonical families, social and news pipelines, evidence, indicators, forecast questions as scored external forecasters, places, situations, chronicles), provenance and declared coverage, provider kit and the provider-ideas catalog | Done |
| 139 | ✅ Checkpoint-Intelligence | Run and fix all tests from 001–138; determinism, no look-ahead, wording, egress, duplicate inflation, manifests, benchmark, collaboration and TLP, chronicle independence, exchange, news independence, chronicle-network disclosure, social independence and absence, external data (pending sources, bindings, independence, external forecasters), invariants | Done (compile-only; tests not run) |

### Maintenance, clients and hardening

| # | Prompt | Scope | Status |
|---|---|---|---|
| 140 | Adapter-Doctor | LLM-assisted diagnosis: probe, compare with fixture, explain, propose parser fix | Done |
| 141 | Client-SDKs-CLI-And-Extensibility | Dart/TypeScript/Python SDK generation with SSE layer and `WorkspaceSync`, picocli CLI, MCP remote authorization (RFC 9728), plugin SPI (WASM-first), science and security formats (CZML, QuakeML, STIX), audio briefings and translation | Done |
| 142 | ✅ Full-Test-Run-And-Fix | Full regression, ≥ 90% coverage gate, invariants registry | Done (compile-only; tests not run) |
| 143 | Runtime-Verification | Live soak on the bench server: storage, WAL, persist ratio, storms, restore drill, disk-full drill, capacity bounds, Tailscale profile, metrics drift; intelligence jobs' runtime and cost | Done (procedure written; nothing measured) |
| 144 | Production-Hardening | Docs consolidation into Architecture/Charter, Flyway baseline (tiered DDL, all indexes), `ddl-auto=validate`, oasdiff, release `v1.0.0` | Done (compile-only; tests and tag pending owner) |

**Outside this repository:** client applications built on Foresignal have their own repositories and roadmaps:
- the web globe and operator console
- Flutter desktop and mobile apps
- the Home Assistant HACS integration
- embeddable widgets
- iOS Live Activities and wearables

They consume the API, SSE, MCP, push and SDKs above.

### Post-release: sources, calibration, vetting and relationship intelligence (API 1.3.0)

Planner additions after v1.2 code-complete. 145–150 are built (compile-only, per the owner's no-test
directive); 151–158 are the backend shore-up that makes the engine **calibrated**, **de-hyped/vetted**,
**timely**, extends it to external forecasting (TimesFM), and opens **people/organization relationship
intelligence** — driven by the 2026-10-22 gap assessment and the ForeSignal Admin screen plan. 151
takes the API to **1.3.0**; 152–158 add under it. External corporate/UBO/PEP/leak **data** sourcing and
fraud recipe sets remain in [Future-Roadmap](Future-Roadmap.md).

| # | Prompt | Scope | Status |
|---|---|---|---|
| 145 | Free-Sources-Enabled-By-Default | Seed free, runnable sources enabled; `enable-free` admin action | Done |
| 146 | Hold-Heavy-Free-Sources-And-Api-1-1-0 | `autoEnable` opt-out (wiki-edit-surge off); API 1.1.0 | Done |
| 147 | Correct-Stale-Source-Descriptions | Fix now-wrong "disabled by default" API descriptions | Done |
| 148 | Sweep-Stale-Default-Comments-And-Tests | Same fix across comments/tests/docs; two spellings | Done |
| 149 | US-English-Guard | `checkUsEnglish` Gradle guard + allowlist + backlog fix | Done |
| 150 | Paced-Ingestion-Governor | Global pull governor + `maxConcurrentPulls` + core default set + `enable-core` | Done |
| 151 | Signal-Calibration | Calibrated intel-event/precursor confidence + confidence-upgrade events; API 1.3.0 | Planned |
| 152 | Quantile-Forecast-Calibration | PIT / quantile-reliability calibration for numeric forecasters | Planned |
| 153 | Dehype-And-Partisan-Vetting | Neutral restatement + framing/partisan score + outlet reputation (incl. lean) into social scoring | Planned |
| 154 | Timely-Corroboration | Auto-corroborate lone claims, modality-aware independence, tunable thresholds | Planned |
| 155 | On-Demand-External-Forecaster | EDP analysis-request round-trip (correlation + callback), external backtest runner, forecaster config, decile mapping (TimesFM) | Planned |
| 156 | Probabilistic-Threshold-Crossing | P(series crosses X by Y) from the predictive quantile distribution | Planned |
| 157 | Authorable-Recipes-Shared-Attribute-Joins | Create/replace-recipe API, shared-attribute join, thresholds → settings | Planned |
| 158 | Relationship-Intelligence-People-Orgs | Person/org entities + corporate keys, ownership edges, ownership-chain/shared-attribute queries (model only; data deferred) | Planned |

## 5. Renumbering map

Some older prompt text, and the design docs, still use earlier numbers. Translate them with these tables.

**Roadmap v1.2 → v1.3** (2026-10-11: intelligence block inserted as 116–139):

| v1.2 | v1.3 | Note |
|---|---|---|
| 001–115 | unchanged | |
| 103 Backtesting-And-Hypotheses | 103 Backtesting-And-Precursor-Tests | same number; its "hypothesis" object is now a **precursor test** |
| — | 116–139 | new intelligence block (evidence, signals, situations, discovery, base rates, ledger, benchmark, hypotheses, outlooks, impact, cycles, client contract, collaboration, chronicles, reports and exchange, news coverage, chronicle network, social signals, external data bindings, checkpoint) |
| 116 Adapter-Doctor | 140 | |
| 117 Client-SDKs-CLI-And-Extensibility | 141 | |
| 118 Full-Test-Run-And-Fix | 142 | |
| 119 Runtime-Verification | 143 | |
| 120 Production-Hardening | 144 | |

Completion Reports 001–005 and the design docs written before 2026-10-11 use v1.2 numbers; where they say 116–120, read 140–144. (A short-lived draft of v1.3 on 2026-10-11 numbered the block 116–127 and the tail 128–132; nothing was built against it.)

Late on 2026-10-11 the foundations checkpoint was inserted as **124**, moving the former 124–133 to 125–134, and the social (135–137) and external-data (138) prompts were added before the intelligence checkpoint (139). All planner documents use these final numbers.

**Product rename (2026-10-11): Elaro → Foresignal.** Completion Reports 001–005 (and any text written before the rename) use the old names. Translate:

| Old | New |
|---|---|
| Elaro / `elaro` / `ELARO` | Foresignal / `foresignal` / `FORESIGNAL` |
| package `ai.elaro` (`src/*/java/ai/elaro`) | `ai.foresignal` (`src/*/java/ai/foresignal`) |
| `ElaroApplication`, `ElaroProperties`, `ElaroVersion`, `ElaroException` | `ForesignalApplication`, `ForesignalProperties`, `ForesignalVersion`, `ForesignalException` |
| env `ELARO_*`, properties `elaro.*` | `FORESIGNAL_*`, `foresignal.*` |
| headers `X-Elaro-*`, `Elaro-*`; OpenAPI `x-elaro-*` | `X-Foresignal-*`, `Foresignal-*`; `x-foresignal-*` |
| token and key prefixes `elr_`, `elf_`, `els_`, `elk_`, `elt_`, `elp_` | `fsr_`, `fsf_`, `fss_`, `fsk_`, `fst_`, `fsp_` |
| DERIVED sources `elaro-intel`, `elaro-analytics`, `elaro-outlook` | `foresignal-intel`, `foresignal-analytics`, `foresignal-outlook` |
| raster frame files `.elg`, magic `ELRG` | `.fsg`, magic `FSRG` |
| domain `elaro.ai`; repo `gitlab.com/adam.allard/elaro`; path `~/Documents/GitLab/elaro` | `foresignal.ai`; `gitlab.com/adam.allard/foresignal`; `~/Documents/GitLab/foresignal` |
| prompt `028-Ask-Elaro` | `028-Ask-Foresignal` |


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
| 043 | 078 | 060 | 116 → v1.3 140 |
| 044 | 080 | 061 | 118 → v1.3 142 |
| 045 | 081 | 062 | 119 → v1.3 143 |
| | | 063 | 120 → v1.3 144 |

**[Core Data Model](design-history/Core-Data-Model.md) labels:** 006a → 007, 007 → 008, 007b → 009, 030a → 044. Its "063" (Flyway) is now 144.

**[Weather & Imagery Design](design-history/Weather-Imagery-Design.md) labels** (its §10 list) → v1.2:

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

Its "080" (runtime verification) is now 143, and its "081" (Flyway) is now 144.

**[Gap Analysis](design-history/Gap-Analysis.md) labels:**

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
| 028i | 060 | 060a | 117 → v1.3 141 |
| 028j | 061 | | |
| 039a | 073 | | |

Its "extend NNN" items apply to the renumbered prompt: translate NNN with the v1.1 → v1.2 table above.

## 6. API tooling

- `postman/Foresignal.postman_collection.json` and `postman/Foresignal-Local.postman_environment.json` are generated from `docs/OpenAPI.json` by `scripts/generate-postman.py`. Any prompt that changes the spec regenerates them.
- Prompt 020 adds a contract test that fails when any operation is missing from the collection, and the route-level test that keeps code and spec in step (`pending-operations.txt` lists the `planned` operations not built yet; it became empty at 141).
- **Parallel client development:** clients generate SDKs from `docs/OpenAPI.json` (for example `openapi-typescript` / `openapi-fetch`, or `dart-dio`) and run a mock server from it (for example `npx @stoplight/prism-cli@5 mock docs/OpenAPI.json`), filtering by `x-foresignal-status` to see what is already live. Prompt 141 published the official SDKs under `sdk/` and the CLI under `cli/`.
- **API compatibility (144):** `docs/api/v1.0.0/OpenAPI.json` is the frozen v1.0.0 spec; `./gradlew checkApiCompat` runs `oasdiff breaking` against it with a local `oasdiff` binary or the Docker image, and skips with a warning when neither is available. It is not wired to `build`.
