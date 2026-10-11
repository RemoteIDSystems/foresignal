# What Foresignal is missing: clients, outputs, inputs and roadmap changes

Every candidate was checked against `docs/Roadmap.md` (001–063), Product-Charter, Architecture, Source-Catalog and OpenAPI. Prompt numbers below are the current Roadmap numbers. New prompts get letter suffixes so nothing has to be renumbered. If the untracked `Weather-Imagery-Design.md` renumbering is adopted, map them across.

**Timing matters.** Only 001–003 are built. Prompts 004–028 are "Ready" but not built, so several of the most important fixes are edits to those prompts now, not new work later.

---

> **v1.3 (2026-10-11):** the intelligence gaps (discovery, situations, base rates, rolling hypotheses, forecast accountability, analysis cycles) are designed in [Intelligence Design](Intelligence-Design.md) and built by prompts 116–129.

## 1. Biggest gaps

1. **Offline clients can't keep a correct copy of the data (must).** `/v1/changes` is a plain `seq > after` scan. Retention deletes heads without writing tombstones, records that expire via `validUntil` never produce a change, and record pages carry no snapshot watermark. Flutter mobile and desktop caches will drift without anyone noticing. Fix it in 011, 012 and 016 before they're built: add `asOfSeq`, a per-source purge horizon that returns `410 RESYNC_REQUIRED`, `validUntil` on Change, and purge or expiry tombstones.

2. **Rules can't watch a list of things (must).** Trigger conditions only allow scalar `> >= < <= == !=` and take one source and one kind. "Track these 40 tails across adsb.lol, airplanes.live, OpenSky and my receiver" needs 160 rules today. Add `in / notIn / exists / matches / startsWith` operators, source and kind arrays, CSV-importable watchlists, and a per-kind identity path. A full entity registry can follow later.

3. **History can't be played back (must).** History is raw pages of up to 50k rows, `/versions` takes no parameters, and MVT reads only `record_head` with `valid_until > now()`. Add `asOf` on records and tiles (past tiles cached as immutable), a simplified `/track`, and an MCP `get_state_at`. Without these, goal G3 ("own the history") and the timeline scrubbers in the globe and Flutter clients don't work.

4. **Phones can't get alerts (should, but it blocks the mobile app).** Browser Web Push (027) does not reach a native Flutter app. Three pieces fix this:
   - UnifiedPush works with 027's encryption almost as-is.
   - iOS needs APNs: visible pushes plus a Notification Service Extension, not data-only "tickles", which iOS throttles.
   - Off-LAN HTTPS needs a supported path: a Tailscale Serve profile, trusted-proxy forwarded headers, and a QR/pairing flow so the admin token never touches a phone.

5. **Clients that can't send headers are locked out, and costly endpoints are open (should).** Once `openReads=false`, browser `EventSource`, MapLibre tiles, KML NetworkLinks, calendar apps and feed readers can't authenticate at all. Under `openReads=true`, Ask, semantic search and push-subscribe are open to anyone on the LAN. Fix with:
   - SSE and tile tickets (SSE tickets must survive reconnects; tile tickets are reusable within their TTL)
   - feed keys bound to a path
   - restricted tokens (limited by source, kind, bbox or endpoint)
   - `alwaysAuthenticated` on every endpoint that spends LLM budget

6. **Alerts fire once and never resolve (should).** Alerts only have `acknowledged_at`, and an exit rule is unlinked from the enter alert. Without a lifecycle there's no "resolved", no editing the original message in place, no PagerDuty/Alertmanager/HA state, and no stop condition for escalation. Change the alert model in 014 and 015 now, together with one-tap signed Ack/Snooze/Mute, quiet hours, and storm control (a per-rule `max_alerts_per_hour` plus folding the per-channel backlog).

7. **No named places, and no protection for personal data (should).** Every rule, briefing, Ask scope and client pastes its own polygon. Add a `place` resource (point+radius, polygon, route+buffer) and saved views, plus moving "near me" anchors. Once phones, family members or OwnTracks feed positions in, a per-source `private` flag is needed. It must keep those sources out of `openReads`, tiles, embeddings and LLM egress, and support purge and export per entity.

8. **"Push data to other systems" means notifications only (should).** The record stream is pull-only. A Sink SPI with durable cursors would let existing ecosystems use Foresignal with no new frontend code:
   - MQTT with Home Assistant discovery
   - Cursor-on-Target for ATAK, iTAK and WinTAK
   - readsb `aircraft.json` and SBS-1 for tar1090
   - NMEA AIVDM for OpenCPN and Signal K
   - CelesTrak-compatible `gp.php` for SatDump, Gpredict and Look4Sat

9. **Local radio gear has no general way in (should).** 036 polls `aircraft.json` over HTTP, and nothing else listens on a socket. A TCP/UDP transport with a codec SPI (with LAN-only validation) brings in AIS-catcher, Direwolf KISS, your own Meshtastic and MeshCore nodes, ACARS/VDL2/HFDL, radiosondes and gpsd. It reuses parsers 046 and 048 already need.

10. **Every client will rebuild the same discovery and aggregation (should).** Missing pieces:
    - TileJSON, a style and a sprite
    - per-kind field specs with units and display hints
    - a capabilities block on `/v1/system`
    - `/v1/stats` time series and hex grids with a rollup table that outlives retention
    - a small glance document for widgets, watches and e-ink

---

## 2. Client types

| Client | What Foresignal must add | Priority | Effort |
|---|---|---|---|
| Flutter mobile | Native push (UnifiedPush now, APNs/FCM later), pairing, sync protocol, deep links (`foresignal://`), glance endpoint, offline packs | should | M |
| Flutter desktop / tray | Notification inbox with per-device read cursor; presence-aware deferral (desktop active = phone waits, critical always sent) | should | S |
| Local web / PWA | Stream tickets, TileJSON and style, Web Push actions (with fallback where Safari lacks them) | should | S |
| Generated SDKs (Dart, then TS, then Python) | Hand-written SSE resume/`reset`/heartbeat layer; cursor iterators; Dart needs a pre-processed spec because dart-dio handles OpenAPI 3.1 nullable unions badly | should | M |
| Home-screen widgets, Wear OS tiles, Garmin, e-ink (TRMNL BYOS), MagicMirror | `/v1/glance` (~2 KB, ETag, `?format=text`). Wear OS, watchOS and Garmin need native code. TRMNL cloud webhooks are capped at 12/hour | should | M |
| ATAK / iTAK / WinTAK | CoT output (sink + alert-only `tak` channel), neutral/unknown affiliation only; implement CoT ourselves, never copy GPL code | should | M |
| tar1090 / Virtual Radar Server | readsb `aircraft.json` merged by hex (needs 057 fusion); SBS-1 optional; local-only egress default | could | S–M |
| OpenCPN / Signal K | NMEA AIVDM serve sink, sharing 048's codec | could | M |
| SatDump / Gpredict / Look4Sat | CelesTrak-compatible `gp.php`. SatDump actually fetches from orbicentral; unknown `FORMAT` must default to CSV; 031 must also fetch `GROUP=active` | should | S |
| QGIS, GDAL, GeoPandas | OGC API Features Part 1 (+ basic CQL2); QGIS already reads the existing MVT and GeoJSON | could | S–M |
| Google Earth / Cesium | KML NetworkLink + `gx:Track` (needs feed keys); CZML later | could | S / M |
| Home Assistant | Now: cookbook (webhook trigger, `geo_json_events` with a top-level `title`). Then: MQTT discovery channel. HACS integration after v1 (needs a public GitHub mirror) | should / could | S / L |
| Grafana | Pack: Operations dashboard on Prometheus + Infinity over REST; no direct-SQL datasource until a views contract exists | could | S |
| AI agent hosts | Open WebUI and LibreChat over the existing `/mcp` (docs only); OpenAI-compatible `/v1/chat/completions` facade is optional; MCP Apps UI resources optional | could | S–M |
| Chat apps as clients (Telegram, Matrix) | Two-way commands (`/alerts /ack /ask /brief`) via long-poll/`/sync`, allowlisted chat ids mapped to read/agent scope; needs a Charter amendment | should | M |
| CLI | picocli native binary over a generated client; no device grant needed | could | S–M |
| Phone automations (Shortcuts, Tasker) | Docs only. iOS can't trigger an automation from a push; Android can via ntfy intents. Add rule `tags` to push payloads | could | S |
| Embeddable widgets, OBS, Stream Deck/Companion, TV, Android Auto | Separate repos. Server side only needs tickets, TileJSON and narrow tokens. Android Auto has no "alerts" category; skip CarPlay and tvOS | could | M–L |

---

## 3. Outputs and interoperability

| Output | What Foresignal must add | Priority | Effort |
|---|---|---|---|
| TileJSON 3.0 + tiles index | Extend 013 | should | S |
| MapLibre style + CC0 sprite | Display hints in descriptor; overlay-only, icon/circle layers, no glyph hosting | should | M |
| Aggregated tiles | `agg=grid\|hex` below `aggBelowZoom` (ST_SquareGrid/ST_HexagonGrid in 3857) instead of silently capping at 50k features | should | M |
| GeoJSON `title` | `record_head.title` from `titleField` (title > name > callsign > place); one resolver shared by MCP and Ask | should | S |
| Streaming formats | `format=ndjson\|geojsonseq\|csv`, `fields=`, `precision=`, `simplify=`, gzip incl. geo+json/MVT, ETags | should | S–M |
| Bulk exports | Async GeoParquet 1.1, GeoPackage, FlatGeobuf, GPX with licence sidecar (needs 059); avoid parquet-java's Hadoop baggage | should | M |
| Stats / rollups | `/v1/stats/timeseries\|grid\|top`, `count=true`; rollup driven by a durable cursor, not an in-memory listener | should | M |
| Glance | `/v1/glance`, read scope, no counts exposed under `openReads` unless opted in | should | M |
| MQTT + HA discovery | `mqtt` channel (alerts, health, summaries, LWT); retained per-record state comes later with views. Paho isn't in the catalog yet and is unmaintained, so consider HiveMQ client | should | M |
| CoT/TAK, NMEA AIVDM, SBS, local APRS-IS serve | Sink SPI (SERVE sinks off by default, LAN-bound, no auth) | should | L (split) |
| CelesTrak compat | `/compat/celestrak/gp.php` | should | S |
| Atom / JSON Feed | Alerts and briefing runs; GeoRSS; sanitized markdown-to-HTML | should | S–M |
| iCalendar | Launch NET, TFR, NWS onset/expiry, briefing schedule; UID=source:externalId, SEQUENCE=version | could | M |
| KML/KMZ | NetworkLink, `gx:Track`, capped at 5k features, escaped balloons | could | S–M |
| CAP 1.2 output | `.cap` + Atom index; scope Public + "not an official warning" note (Private requires `<addresses>`); Update+AllClear, not Cancel | could | M |
| OGC API Features | Part 1 + basic CQL2, separate history collection | could | S–M |
| Offline packs | PMTiles v3 (in-house writer), downloaded rather than streamed via `pmtiles://` | could | M |
| Cold archive | Archive-then-delete inside the retention step, hive-partitioned GeoParquet, local first then S3 | should | M |
| Snapshots | `atSeq` (rebuilt from `record_version`), holds exempt from retention, SHA-256 manifest | could | M |
| Event contracts | Add event schemas and the OAS 3.1 `webhooks` object to `OpenAPI.json` now; AsyncAPI only once MQTT/WS exist | could | S |
| Metrics | Data-age gauges in 016; operator-defined projections at `/v1/metrics` (read scope) | could | S |
| CloudEvents envelope | Structured mode on webhooks via a body-template recipe first | could | S |
| Static map snapshots | Basemap-free Java2D or a local PMTiles extract; cloud chat channels opt-in | could | M |
| Share links | Path-bound keys, field masks on by default for personal sources, after 059 | could | M |
| Signed outputs | Instance Ed25519 key + JWKS + detached JWS on exports and briefings; C2PA deferred | could | S |
| Not core (post-v1) | CZML, QuakeML/FDSN, CF-NetCDF DSG, STIX/TAXII, SIEM/ECS (Boot's built-in `logging.structured.format=ecs` covers audit logs now), DCAT, HXL, BagIt, ActivityPub, Matter, Obsidian vault, audio briefings, reports/PDF, GDL 90 | could | — |

---

## 4. Data inputs

### 4a. Local hardware and RF

| Input | Notes | Priority | Effort |
|---|---|---|---|
| Socket transport + codec SPI | TCP client, UDP listener with sender allowlist, LAN validator; SBS-1, readsb JSON, gpsd first. Ports are config, not assumed defaults. Drop Beast | should | L (split) |
| readsb v2 | Keep `mlat`/`tisb`/NACp fields; `stats.json` as receiver health; 978 variant; local-only egress default. Treat MLAT as non-redistributable | should | S |
| AIS-catcher | TCP output first (reuses 048 codec), MQTT/HTTP push preset; station position opt-in | should | S |
| Direwolf APRS over RF | KISS-TCP only, provably receive-only, dedup against `aprs-is`, messages opt-in. Note MeshCore KISS is not AX.25 | should | S |
| Own Meshtastic node | Private-broker JSON route (not nRF52). Keep GPL protobufs out of the MIT tree | should | M |
| MeshCore companion | TCP firmware build (no default port), bridge via `meshcore_py` first; repeater health only from adverts (receive-only) | should | M |
| Mesh field reports | Report grammar over a private PSK, replies via a mesh channel | should | M |
| ACARS / VDL2 / HFDL | Via `acars_router`; metadata-only default, never `openReads` or LLM (legal) | could | M |
| radiosonde_auto_rx | PAYLOAD_SUMMARY over UDP, extends 042 | could | S–M |
| Weather stations (Ecowitt, Davis WLL, WeeWX) | Davis/Ecowitt-local polling; Ecowitt form POST needs a key in the path (cleartext LAN); coarse geometry | could | M |
| LoRaWAN (ChirpStack, TTS) | Two mappings; trackers are personal data | could | S–M |
| Drone Remote ID | DragonSync MQTT; filter out DJI DroneID; strip pilot/home position from public paths | should | S–M |
| GNSS integrity sentinel | gpsd JSON first; UBX via raw passthrough; no CFG writes | could | S–M |
| Local air quality | PurpleAir/airrohr/AirGradient LAN poll; separate indoor kind | could | S |
| Station health (feeders, SatNOGS, UPS) | readsb stats instead of graphs1090 scraping; UPS via NUT push recipe | could | M |
| Rayhunter | GET-only, private source, never fetch QMDL/PCAP; geotag needs a tracker | could | S–M |
| Raspberry Shake, lightning (AS3935/WH57), NOAA SAME, NAVTEX, Inmarsat-C EGC | Mostly push recipes or presets; EGC strictly a SafetyNET allowlist | could | S each |
| Home Assistant state bridge | `subscribe_entities` (non-admin), allowlist enforced in Foresignal, private flag first | could | M |
| Frigate, Kismet, home-network IDS, trunk-recorder | Recipes via 020; high privacy | could | — |

### 4b. Internet feeds

| Input | Notes | Priority | Effort |
|---|---|---|---|
| Tsunami warnings (PAAQ/PHEB + CAP) | Feed holds only the latest message; close on Cancel, not on absence | should | S |
| CAP alert family (national authorities) | Reviewed list seeded from the WMO register; DWD over BBK (BBK is JSON, terms unclear) | should | L |
| SWPC extension + DONKI | Alerts, RTSW downsampled, X-ray, aurora at places; DONKI via `ccmc.gsfc.nasa.gov/DONKI-API` (no key) | should | M |
| SPC Day 1–3 outlooks, WPC ERO | Needs `validFrom` | should | S |
| US hydrology (USGS OGC API, NWPS flood categories, SNOTEL, USDM) | NWPS supplies the flood stages that make gauge triggers meaningful | should | M |
| Road events (Open511, WZDx, DATEX II) | Digitraffic JSON instead of DATEX; NDW v3 | should | M |
| GTFS-RT ServiceAlerts + MobilityData catalogue | Vendored CC0 snapshot, not caller URLs | should | S / M |
| Vulnerabilities (NVD + EPSS) | EPSS stored banded on ingested CVEs only | could | M |
| Government travel advisories | Level parsed from the title; FIPS→ISO crosswalk | could | S |
| BGP (Cloudflare Radar events, RIS Live) | RIS needs peer-visibility tracking | could | M |
| Status pages | Statuspage v2 family + GCP; AWS/Azure via RSS | could | S |
| Smoke (HMS) + Canada fire (CWFIS) + InciWeb | Hotspot layers duplicate FIRMS | could | S–M |
| Volcano levels (HANS, GeoNet), Copernicus EMS, OpenFEMA | No eligibility answers from OpenFEMA data (its terms forbid it) | could | S each |
| ShakeMap/PAGER, EMSC WebSocket + FDSN family, IOC sea level (non-commercial) | — | could | S–M |
| Satellite scenes (STAC) | Footprints and links only; Earth Search default | should | M |
| FAA UAS maps, AWC PIREP/G-AIRMET/METAR, NOTAMs (needs approval) | — | could | S / M |
| Grid ops (EIA-930), carbon intensity (GB), ODIN outages (terms unverified) | — | could | S |
| Health and nature (CDC NWSS, eBird, iNaturalist), Wikipedia edit surge, official social accounts, SOCRATES | — | could | S–M |

### 4c. Generic, no-code and user-generated

| Input | Notes | Priority | Effort |
|---|---|---|---|
| Ingest format SPI | `alertmanager`, `cloudevents`, `cap`, `mapping`; ingest key accepted as Bearer/Basic | should | M |
| RecordMapping + device presets | JSONPath, units, preview, LLM-suggested draft (never auto-applied) | should | M |
| ntfy/Gotify-compatible publish | Every homelab tool (Uptime Kuma, Proxmox via Gotify, Rayhunter) becomes an input | should | S |
| Generic RSS/Atom/GeoRSS/JSON Feed family | Reviewed list; WebSub off by default | should | M |
| OGC API / ArcGIS / WFS kits | Reviewed resource file; refactor 040/050/056 onto it | should | L |
| Personal location (OwnTracks, Traccar/OsmAnd, Overland, GPSLogger) | Per-app auth and reply shapes; requires the private flag | should | M |
| Operator notes, pins, areas | Built-in push source, new `contribute` scope, retention-exempt | should | M |
| Agent findings via MCP | `submit_observation` with verified citations, proposed status, loop guard | should | M |
| Outbound URL policy (SSRF guard) | Prerequisite for any operator-supplied URL | should | M |
| Dataset-by-URL, file upload import | GeoJSON/CSV/KML/GPX first; adds "tombstone absent heads" to the writer | should / could | M |
| Inbox/share target, attachments with EXIF, voice notes, IMAP, inbound SMS, ICS watch areas, browser extension, Ushahidi | After notes, attachments and the URL policy exist | could | — |

---

## 5. Uses and features

| Feature | Notes | Priority | Effort |
|---|---|---|---|
| Watchlists + set operators | Core OSINT workflow | must | M |
| Time travel / tracks | asOf, `/track`, frames | must | M |
| Catch-up digest | `GET /v1/digest?sinceSeq` with read scope; alert window keyed on alert id, not seq | should | S |
| Moving-anchor "near me" geofences | Anchor is an ordinary record; reverse evaluation on anchor change | should | M |
| Correlation incidents | Recipes; `join_reason` per member; same-event dedup | should | L |
| Baselines + anomaly alerts | Count distinct entities; Poisson tail; health-gated; 8–12 week warm-up | should | L |
| Windowed rules (count, absence) | Absence on a scheduler tick and health-gated; cluster identity is hard | should | M |
| Forecast-aware alerts | `validFrom`, `forecastHorizon`, time-driven re-evaluation | should | M |
| Entity registry | Deterministic keys only; FAA registry without owner PII | should | L |
| Satellite passes + pass triggers | Orekit over OMM elements; a SatDump webhook needs a bridge script | should | M |
| Impact and exposure | Infrastructure counts first; population (GHSL→H3) as an optional pack | could | M |
| Case files / alert status | Status enum in 014 now; case files later | could | M |
| Alert-quality feedback and backtests | Feedback column in 014 now; tuning later | could | M |
| Reliability grading (Admiralty) | Source grade in 059; credibility via 057 corroboration (F and 6 mean "unknown") | should | S / M |
| Pattern of life, trip watch, commute corridors | `active_from`/`until` in 014 now; OSRM can't exclude polygons, only Valhalla can | could | L |
| Hobby packs (spotter, ham, maritime, home safety, drone, agriculture) | Packs need merge-import plus placeholders | could | M |
| Kinematic CPA/ETA, exercise mode, terrain LOS, vision triage, translation | Post-v1 | could | — |

---

## 6. Platform for the planned web and Flutter clients

| Capability | Notes | Priority | Effort |
|---|---|---|---|
| Sync protocol (asOfSeq, purge horizon, tombstones) | Into 011/012/016 now | must | S–M |
| Re-filterable multiplexed SSE | `/v1/events/stream` + PATCH filter + `filter-applied {atSeq}`; WebSocket deferred | should | S–M |
| Stream tickets + feed keys | Reconnect-aware SSE tickets; tiles reuse within TTL; URL-only clients use feed keys | should | M |
| Restricted tokens + anonymous restriction profile | One enforcement point covering every read path | should | M |
| Device pairing | Short-lived claim code + QR; device metadata; revoke cascades to push subscriptions; no RFC 8628 | could | S–M |
| Remote access / TLS | Tailscale Serve profile, Tomcat `internal-proxies` (not `framework`), bind 8080 and 5433 to localhost | should | M |
| mDNS | Avahi service file only; no stable install id in TXT | could | S |
| Capabilities | `features`/`limits` on `/v1/system`; oasdiff in CI after v1.0 | should | S |
| Field specs + display hints | Per kind, UCUM units, optional SIDC | should | M |
| Places, saved views, client state store | Read-scope writes for per-device state | should | M |
| Notification inbox | Persist every NotificationMessage, including system events | should | S–M |
| Public-exposure hardening | `alwaysAuthenticated` on cost-bearing endpoints, per-client limits, backoff rather than lockout | should | M |
| Readiness probes | Catch-up as a separate signal; compose healthcheck needs curl in the image | should | S |
| Watchdog heartbeat | Generic dead-man ping URL; off-box monitor | should | S |
| Audit log | Writes and agent tool calls only | could | S |
| Backups + config-as-code | Backup profile from the same DB image; apply-once import with `${env:}` references | should | M |
| Localized notifications | Per-channel timezone, `\|local \|ago` filters; no ICU4J | should | S |
| Gazetteer / region tagging | GeoNames + Natural Earth; tags stored outside the hash | should | M |
| OpenTelemetry | Trace ids in logs now; export off by default | could | S |
| Out-of-tree plugins, federation, edge relay | Post-v1 | could | L |

---

## 7. Notification destinations

| Destination | Notes | Priority | Effort |
|---|---|---|---|
| UnifiedPush | Admin host allowlist (not a global flag), registration verification, Topic header, 4 KB byte cap | should | S |
| APNs / FCM | Visible alerts + NSE; payload contains only id and severity; per-operator keys; relay out of scope | should | M |
| Actionable notifications | Signed single-use POST actions; GET opens a confirm page; ntfy `http`, Telegram callbacks, Web Push actions | should | M |
| Stateful edit-in-place | Discord `?wait=true`+PATCH, Telegram `editMessageText`, Matrix `m.replace`; Slack/Mattermost need bot tokens; others get a follow-up message | should | M |
| Quiet hours, snooze, storm control | Defer via `next_attempt_at`, release as a digest; critical bypass | should | M |
| Deep links | `clientLinks` templates; custom scheme default, verified links optional | should | S |
| Home Assistant native channel | Webhook-trigger mode default; notify mode for iOS critical / Android alarm stream | should | S |
| Mesh radio channels | Meshtastic MQTT JSON downlink (no GPL), MeshCore 133-char cap, never post to public LongFast | should | M |
| Two-way chat | Telegram/Matrix first | should | M |
| Apprise | Partial failures (HTTP 424) not retried | could | S |
| Escalation policies | After the alert lifecycle; Pushover receipt cancel | could | M |
| Watchdog heartbeat | See section 6 | should | S |
| Zulip | Native type with per-rule topics; Rocket.Chat works through the slack type | could | S |
| APRS messages / Winlink | Winlink is already possible via a title template + `plainTextOnly` email | could | S–M |
| DAPNET, JS8Call | JS8 must not transmit unattended without operator confirmation (FCC Part 97) | could | S–M |
| SMS / voice | Self-hosted Android gateway first; ntfy `X-Call` for voice | could | M |
| Alertmanager / PagerDuty | After the alert lifecycle; Alertmanager needs repeated re-posts while firing | could | M |
| Public broadcast (Mastodon, Bluesky) | After 059; dry-run mode, high severity floor | could | M |
| Live Activities, wearable haptics, ambient lights, thermal printer, ActivityPub, Matter | Post-v1, or client-side | could | — |

---

## 8. Proposed roadmap changes

**A. Amend Ready prompts before they are built**

1. **Extend 004** with optional descriptor fields: `titleField`, `entityKeys`, `fieldSpecs` (type, UCUM unit, label), `kindDisplay` (title template, icon, colour, optional SIDC), `aggBelowZoom`, `startField`/`endField`, `private`.
2. **Extend 007** with:
   - a nullable `validFrom` and `record_head.title`
   - an entity-key extraction hook
   - an out-of-order `observedAt` guard: store the late version, don't regress the head, suppress edge triggers
   - a precision cap for private sources, applied before hashing
3. **Extend 009 (Adapter Guide)** with an altitude convention (payload metres plus datum, not geometry Z) and a contract test that flags payload keys not declared in `fieldSpecs`.
4. **Extend 011** with:
   - `asOfSeq` and `/versions` since/until/limit
   - purge horizon + `410 RESYNC_REQUIRED`, and `validUntil` on Change
   - `title` in Features
   - `near`/`radiusKm`, `sort=distance` (its own cursor), and antimeridian splitting
   - `fields`/`precision`/`simplify`
   - `ndjson|geojsonseq|csv`, ETags, and `/v1/kinds/{kind}/schema`
5. **Extend 012** with a swappable filter, multiplexed `/v1/events/stream` + PATCH filter, `reset: resync_required`, and per-token stream limits.
6. **Extend 013** with TileJSON (index + per layer), `agg=grid|hex`, and `title` in MVT.
7. **Extend 014** with:
   - alert lifecycle: state, resolved_at/by, `autoResolve` on inverse edge, tombstone or expiry, `alert.resolved`
   - `acknowledged_by`, a feedback enum and snooze
   - operators `in/notIn/exists/matches/startsWith`, plus source and kind arrays
   - an active window, `active_from`/`until`, and LineString+`bufferMeters` geofences
   - `max_alerts_per_hour`, alert-stream minSeverity/source/kind filters, rule `tags`
   - `forecastHorizon`, and `fromSeq`/`toSeq` backtesting on the rule test
8. **Extend 015** with:
   - `provider_message_ref` + `renderUpdate`
   - `links` and `shortTitle` on NotificationMessage
   - a persisted notification row, including system events
   - per-channel quiet hours + global pause, backlog cap/folding + opt-in grouping
   - Pushover receipt cancel
   - new types `homeassistant`, `apprise`, `zulip`
   - ntfy `call`, email `plainTextOnly`, channel timezone + template filters, a shared Truncator, and an origin marker for loop guards
9. **Extend 016** with:
   - liveness/readiness groups + a catch-up indicator + the app healthcheck in compose
   - data-age and alerts-by-severity gauges
   - purge-horizon recording
   - the watchdog heartbeat and opt-in canary
   - `features`/`limits`/`apiVersion` on SystemInfo
   - optional ECS logging and trace ids in logs
10. **Extend 017** with event-payload schemas and the OAS `webhooks` object, `lintOpenApi` made required, an optional `checkSdkGeneration`, and a written API-evolution convention.
11. **Extend 018 (Operator Guide)** with "Remote access & TLS" (Tailscale/Caddy profiles, localhost binds), an Avahi mDNS file, and backup notes.
12. **Extend 019** with:
    - stream tickets
    - restriction columns on `api_token`
    - device metadata + an optional pairing code
    - `alwaysAuthenticated` on Ask, search, push-subscribe and LLM-backed MCP tools
    - trusted-proxy forwarded headers and a publicBaseUrl https warning
    - `clientLinks`
    - an `audit_event` table + `GET /v1/audit`
    - token revoke cascading to push subscriptions
    - a per-source `private` visibility gate
13. **Extend 020** with the IngestFormat SPI (`/v1/ingest/{id}/{format}`: alertmanager, cloudevents, cap, mapping), ingest key accepted as Bearer/Basic, per-format signature verifiers, and ntfy/Gotify-compatible publish endpoints.
14. **Extend 021** with an optional legacy-SSE flag and `set_alert_status`.
15. **Extend 025** with `GET /v1/digest` (read scope, structured JSON, gap flag) and HTML renditions of runs.
16. **Extend 027** with:
    - the PushTransport SPI shape
    - UnifiedPush: allowlist, send-time IP recheck, registration verification, Topic header, byte cap
    - `ruleId`/`tags`/`shortTitle`/`links`/`lang` in the payload, and a `very-low` urgency downgrade
17. **Extend 028 (cookbooks)** with recipes for:
    - Home Assistant (webhook trigger, `geo_json_events`, REST sensors)
    - Node-RED
    - Vector/Redpanda Connect event-bus bridges over `/v1/changes`
    - Open WebUI/LibreChat over `/mcp`
    - Shortcuts/Tasker
    - a PyTAK→ingest bridge
    - n8n IMAP
    - Grafana via Infinity

**B. New platform prompts between 027 and the adapter block**

18. **New 027a-Native-Mobile-Push (after 027):** APNs (ES256, visible + NSE) and FCM HTTP v1 transports; `/v1/push/devices`; admin provider keys.
19. **New 027b-Actionable-Notifications-And-Inbox (after 027a):** signed action capabilities + confirm-page fallback; action rendering for ntfy, Telegram, Web Push and Matrix; `/v1/notifications` with per-device read cursors and badge; presence deferral; escalation policies.
20. **New 028a-Access-Perimeter (after 028):** restricted-token enforcement point and anonymous profile; feed keys; per-client rate limits + auth-failure backoff; outbound URL policy retrofitted to the LLM `baseUrl` and channel URLs.
21. **New 028b-Places-Views-And-Watchlists:** places, saved views rendered by records/tiles/SSE, watchlists with CSV import + `inWatchlist`, moving-anchor `near` rules, MCP tools.
22. **New 028c-Time-Travel-And-Tracks:** asOf on records and tiles, `/track`, frames, `/timeline`, MCP `get_state_at`/`get_track`.
23. **New 028d-Stats-Rollups-And-Glance:** cursor-driven `activity_rollup`, `/v1/stats/*`, count mode, `/v1/glance`, `/v1/metrics` projections; briefing digest counts move onto it.
24. **New 028e-Private-Sources-And-Personal-Location:** private enforcement on every path; OwnTracks, OsmAnd, Overland and GPSLogger mappers; device-scoped ingest; per-entity export and purge with tombstone fan-out.
25. **New 028f-Gazetteer-And-Region-Tagging:** GeoNames/Natural Earth/US county loader, `{{near.*}}` placeholders, `country`/`admin1` filters, a shared centroid table.
26. **New 028g-Map-Styles-Feeds-And-KML:** styles + sprites, Atom/JSON Feed for alerts and briefings, KML NetworkLink/`gx:Track`.
27. **New 028h-Mapping-Profiles-And-Device-Presets:** RecordMapping, preview, LLM suggest, form/GET ingest, presets (Ecowitt, WU PWS, aiscatcher, ChirpStack/TTS, Alertmanager, airrohr, GMC).
28. **New 028i-MQTT-Channel-And-HA-Discovery:** `mqtt` channel, retained health and summary topics, LWT, HA discovery.
29. **New 028j-Interactive-Chat-Commands (could):** Telegram + Matrix two-way, plus the Charter amendment.

**C. Adapter block**

30. **Extend 031** with `celestrak-socrates`, the CelesTrak-compatible `gp.php` output, the `active` group, and optional `spacetrack-tip`.
31. **Extend 034** with `tsunami-warnings`, a shared CAP 1.2 parser, and NWS onset mapped to `validFrom`.
32. **Extend 036 → 036-Local-Receivers:**
    - socket transport + codec SPI + LAN validator
    - readsb position-source/NACp fields, receiver stats, 978 variant
    - `station-*` health family
    - Davis/Ecowitt local polling
    - local-only egress default
33. **Extend 037/038** with GTFS-RT ServiceAlerts and the MobilityData catalogue resolver.
34. **New 039a-Generic-Feature-And-Feed-Kits (after 039):** ArcGIS FeatureServer, OGC API Features and WFS-JSON families; RSS/Atom/GeoRSS/JSON Feed family with a curated bulletin pack. 040, 050 and 056 are then written on top of it.
35. **New 039b-Road-Events-And-Work-Zones:** Open511, WZDx, DATEX II.
36. **Extend 040** with `inciweb-incidents`, `noaa-hms-smoke`, `cwfis-*`.
37. **Extend 041** with `nvd-cves` (EPSS-enriched) and `ct-domain-watch`.
38. **Extend 042** with the SWPC split (alerts, rtsw, xray, aurora, DONKI), JPL fireballs and close approaches, and `local-sonde`.
39. **Extend 043** with `sensor-community`, `airnow-hourly` and `airq-local`.
40. **New 043a-Hydrology-And-Coast:** usgs-water, nwps-gauges, snotel, usdm, noaa-coops-tides; EA/ECCC optional.
41. **Extend 046** with `local-aprs` (Direwolf KISS) and optional trunk-recorder metadata.
42. **Extend 047 → 047-Mesh-Sources** with `meshtastic-local` and `meshcore-local`.
43. **New 047a-Mesh-Radio-Channels:** Meshtastic/MeshCore outbound with a compact short-text/CBOR profile, duty-cycle limits, report grammar, optional DM ack.
44. **Extend 048** with `local-ais` (AIS-catcher) and the shared AIVDM codec.
45. **New 048a-Local-RF-Decoders:** ACARS/VDL2/HFDL (metadata only), auto_rx, gpsd sentinel, Remote ID (DragonSync), rsudp and SAME presets.
46. **Extend 049** with `faa-uasfm`, `faa-uas-restrictions`, AWC PIREP/G-AIRMET/CWA/METAR-TAF, and optional NMS NOTAMs.
47. **Extend 050** with `imf-portwatch-ports`.
48. **Extend 051 → CAP-Alerts-And-International-Seismic** with the CAP family, `spc-outlooks`, `wpc-ero`, EMSC WebSocket + `fdsn-*`, and `usgs-shakemap`.
49. **Extend 052** with Copernicus EMS, HANS/GeoNet volcano levels, OpenFEMA, travel advisories, and optional HDX HAPI and CDC NWSS.
50. **Extend 054** with Cloudflare BGP events, `status-*`, optional RIS Live, and ODIN (only if its terms clear).
51. **Extend 055** with EIA-930, grid-frequency excursions and GB carbon intensity.
52. **Extend 056** with the World Port Index (buffered) and NPS boundaries.
53. **New 056a-Entity-Registry (after 056):** entity/alias/key tables, `/v1/entities`, `faa-registry` without PII, MCP `get_entity`/`find_entity`.
54. **Extend 057** with aircraft fusion by hex, corroboration/credibility, port calls, kinematic CPA, felt-locally, and acoustic-without-Remote-ID.
55. **New 057a-Windowed-Rules-And-Anomalies (after 057):** count/absence rules, hourly baselines, an `anomaly` rule type.
56. **New 057b-Incidents-Cases-And-Exposure:** correlation recipes, incidents + case files, ExposureService.
57. **Extend 058** with HRRR smoke fields, WPC HeatRisk, EPA UV, `f=png-uv`/`velocity-json` alternates, and a position sample.
58. **Extend 059** with source reliability grades, an instance signing key + JWKS + detached JWS, and sinks listed as an egress path.

**D. After 059**

59. **New 059a-Outbound-Sinks:** Sink SPI with durable cursors; MQTT retained record state; CoT/TAK; readsb `aircraft.json`/SBS; NMEA AIVDM; local APRS-IS serve; CloudEvents option.
60. **New 059b-Exports-Archive-And-Offline-Packs:** async GeoParquet/GeoPackage/FlatGeobuf/GPX; `atSeq` snapshots + holds; cold-archive GeoParquet; PMTiles packs.
61. **New 059c-Standards-And-Header-less-Outputs:** OGC API Features, CAP output + Atom index, iCalendar feeds, share links, basemap-free static map snapshots.
62. **New 060a-MCP-Remote-Authorization (could, before 061):** RFC 9728 + IdP delegation; no built-in authorization server.
63. **Extend 062** with a restore drill, a Tailscale-profile check, a storm scenario, a readiness flip during forced catch-up, and a metric-name drift check.
64. **Extend 063** with Flyway indexes for the new tables and oasdiff CI against the v1.0.0 spec.
65. **Post-v1 backlog (064+):** CLI, HACS integration, embeddable widgets, Live Activities, alert-quality tuning, exercise/replay mode, federation, edge relay, plugins (WASM-first), trip and commute watch, satellite passes, pattern of life, terrain/LOS, vision triage, translation, audio briefings, SIEM/STIX/DCAT/HXL, CZML/QuakeML/NetCDF, BagIt, ActivityPub, Matter, thermal printer.

---

## 9. Considered and rejected

- **Blitzortung lightning:** data is for contributors only. Use GOES GLM, a later L-effort item, instead.
- **Scraping sources:** graphs1090 pages, GVP `reports_weekly.cfm`, utility outage maps, crt.sh polling, Waze/Google traffic, PulsePoint/Broadcastify, and the HTML Federal Register were all rejected (ToS, or scraping).
- **Unofficial or unredistributable data:** DJI DroneID, Inmarsat Aero and private STD-C traffic, POCSAG/FLEX decoding, FlightAware MLAT re-serving, and re-serving OpenSky/ACLED/aisstream data to peers or sinks (licence).
- **Signal via signal-cli:** unofficial client, not an official API.
- **WhatsApp Business, FAA SWIM/SCDS, satellite-messenger channel:** dropped in verification on feasibility or access terms.
- **Opsgenie, Grafana OnCall OSS:** being retired. PagerDuty and incident.io add little for a single operator.
- **CarPlay, tvOS, Android Auto messaging:** no fitting category, or not a supported Flutter target.
- **Sigstore public log:** leaks signer identity. Use instance-key JWS instead.
- **Server-side routing or OSRM hazard avoidance:** OSRM can't exclude polygons, and routing belongs in the client.
- **GraphQL, gRPC, CBOR WebSocket framing, EDXL-SitRep:** low value or no consumers.
- **AsyncAPI now:** OpenAPI 3.1 webhooks and event schemas cover today's needs.
- **pg_featureserv/Martin sidecars, direct-SQL Grafana:** bypass scopes and licence egress.
- **Generic local MQTT subscriber, personal radiation monitors, avalanche forecasts, RBN/WSPR, geo-sleuth agent loop, audience profiles, welfare roll-call, ICS forms, sonification:** dropped in verification as not feasible, not allowed under the policy, or covered by other items.
- **Cut from items that were otherwise kept:** RFC 8628 device grant, server-side SPKI pinning, a global `push.allowPrivateEndpoints` flag, data-only APNs "tickles", Zarr v3 export, and an in-house MCP authorization server.