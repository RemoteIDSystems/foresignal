# Foresignal — Product Charter

| | |
|---|---|
| Product | Foresignal — headless intelligence broker |
| Name note | Foresignal (foresignal.ai) is unrelated to foresignal.com (a forex-signals site) |
| Domain | foresignal.ai |
| Owner | Adam Allard |
| License | MIT |
| Repository | gitlab.com/adam.allard/foresignal |
| Status | Charter v2.0 — 2026-10-18. Code-complete at v1.0.0 (prompts 001–144); the test suite, the runtime verification and the `v1.0.0` tag are pending the owner (§11, §14). |

This Charter and the [Architecture](Architecture.md) are the authoritative design documents. The design inputs written along the way (Core Data Model, Weather & Imagery Design, Gap Analysis) are archived under [design-history/](design-history/); the amendments they made to this Charter are folded in below.

---

## 1. Purpose

Foresignal is the data foundation for a family of situational-awareness applications. It collects public telemetry (aircraft, vessels, satellites, seismicity, fires, conflict events, weather, outages, infrastructure, cameras, transit, and more), normalizes it into versioned records, evaluates alert rules against the change stream, and serves everything through one stable, documented API.

Foresignal has **no user interface**. Every capability, including all configuration, is reachable through the API and, for agents, the MCP server. Foresignal pushes notifications to the services the operator already uses, and can optionally use an LLM (frontier or local) to answer questions, write briefings and add context to alerts. Frontends are separate repositories.

On top of the collected history, Foresignal runs an **intelligence layer** ([Intelligence Design](Intelligence-Design.md)): it discovers unusual changes and cross-domain patterns, remembers unfolding situations, maintains rolling hypotheses with indicators that watch for themselves, and records probabilistic forecasts in a ledger that scores them, so the operator always knows how often Foresignal was right.

## 2. Vision

> Own your view of the world. Subscribe once, store everything, render anywhere, be told when something matters, and know how often you were right.

## 3. Problem statement

Open-source situational-awareness projects (ShadowBroker, God's Eye View, Velocity, World Monitor) each bundle collection, display and alerting into one application. The consequences:

1. **Collection is coupled to display.** Hiding a layer stops collecting it; you cannot monitor something without looking at it.
2. **No durable history.** Data lives in the browser or in short position caches; playback and analysis are limited or absent.
3. **Upstream abuse by accident.** Without per-source accounting, projects over-poll; ShadowBroker was called out by Meshtastic for 1–2 orders of magnitude excess traffic.
4. **Duplicated integration effort.** Every project re-implements the same upstreams, inside a UI codebase, and each breaks independently when upstreams change.
5. **One client per backend.** A globe, a phone, a CLI and an agent cannot share one collector.
6. **AI that summarizes but never answers for itself.** Where AI exists it summarizes the current feed. Risk indices use hand-set weights, correlations are computed and thrown away, briefings have no memory of last week, and forecasts, where they exist at all, have no demonstrated accuracy.

## 4. Goals

| # | Goal | Measure of success |
|---|---|---|
| G1 | Decouple subscription, display and alerting | Disabling display in every client has zero effect on collection; enabling/disabling a source through the API starts/stops fetching within one scheduler tick (≤ 5 s) |
| G2 | Be a good upstream citizen | No source ever exceeds its configured per-minute or per-day request budget (or its `mbPerDay` byte budget); spend is visible per source |
| G3 | Own the history | Every distinct revision of every record is retained per the configured retention; any record's version history is queryable |
| G4 | One API, many clients | A frontend can be built using only the OpenAPI spec; GeoJSON, MVT tiles, raster tiles and filtered SSE are available |
| G5 | Alert on what matters | Threshold, geofence and windowed rules fire within one ingest cycle of the triggering record and are delivered via SSE and notification channels |
| G6 | Self-diagnosing adapters | A broken adapter (errors or silent zero results) is visible through the health API without reading logs |
| G7 | Trivial to run | `docker compose up` on Linux or macOS yields a working broker with no file edits beyond optional DB/port settings |
| G8 | Maintainable by an AI agent | Every adapter is self-contained with a recorded fixture and contract test; raw upstream fetches are available as a diagnostic; the adapter doctor explains failures |
| G9 | Reach the operator anywhere | Alerts, briefings and system events reach the configured channels (§8.6: chat apps, push services, email, webhooks, MQTT, Home Assistant, LoRa mesh and Web Push) within 10 s of the alert |
| G10 | Agent-native | Every data query and watch operation is available as an MCP tool, and agents are confined by token scope |
| G11 | Optional intelligence, never required | With an LLM configured, Ask answers with verified citations, briefings are narrative and alerts carry context. Without one, every non-LLM feature works unchanged. Local models are fully supported. |
| G12 | Open to inbound data | Any system can push records into a custom source through an inbound webhook, and those records get the same versioning, alerting and search as polled data |
| G13 | Correlate and remember | Unusual changes, cross-domain co-occurrences and pre-event market or attention moves are detected deterministically with a stated base rate; related evidence is grouped into situations that persist for weeks; "what changed since last week" is answered from stored snapshots |
| G14 | Forecast honestly | Every probability Foresignal produces starts from a stated base rate, is recorded immutably with its knowledge cutoff, resolves automatically from Foresignal's own data where possible, and is scored (Brier, log, CRPS) with calibration shown per forecaster, including the LLM and the operator |

## 5. Non-goals

- A user interface of any kind inside the broker. Frontends (globe, operator console, mobile) are separate repositories.
- Password logins or an identity provider inside the broker. People and agents are **named principals** holding scoped API tokens (`read`, `agent`, `admin`) and pairing codes; groups and per-object sharing exist for work products (130), but Foresignal is not a user-directory product. (Remote MCP clients may present an access token from the operator's own authorization server (141); Foresignal validates it and runs no authorization server itself.)
- Training or fine-tuning neural models, or bundling model weights. Foresignal calls providers the operator configures. (Fitting statistical models to the operator's own data, such as baselines, forecasts, index weights and recalibration, is in scope.)
- Analyzing private individuals, or accusing anyone of wrongdoing. Market findings describe aggregate activity only. The entity registry covers vehicles and objects, never people or organizations (098).
- Sending email or chat messages other than notifications the operator configured, **and replies to commands from chat ids the operator explicitly allowlisted on an interactive channel** (061). The same exception covers replies to senders allowlisted on a `meshtastic` or `meshcore` channel with field reports enabled (084).
- Horizontal scale-out or clustering (single instance by design). Instances may federate with each other (113), but each is a single process.
- Proxying basemaps or third-party tiles. Foresignal hosts products derived from data it ingests; it never proxies basemaps or third-party tiles (GIBS and nowCOAST are external catalog entries clients fetch directly; Esri, Google 3D Tiles and OSM tiles are likewise fetched by clients) (045). Static map images (055: `/v1/maps/static`, `get_map_image`, alert map attachments) are such derived products: drawn from the bundled Natural Earth asset and Foresignal's own frames, with no proxied or fetched public basemap (an operator's own LAN tile server is the only optional exception).
- Fringe and specialist source adapters in the core (web maps, scrapers, browser automation, prediction markets, niche feeds). They are separate projects that speak the [External Data Protocol](External-Data-Protocol.md); Foresignal accepts whatever the operator connects and does not police that choice.
- Real-time sub-second latency guarantees.

## 6. Users and personas

| Persona | Description | Primary needs |
|---|---|---|
| **The Operator** (primary; the owner) | Runs Foresignal on an always-on Linux bench server. Configures sources, budgets, retention and triggers through the API or the future operator console. | Reliability, visibility of spend and health, alerts, zero file editing |
| **The Occasional Runner** | Individual who starts Foresignal with Docker Compose when they want to look at the world, then stops it. | Fast startup, sensible defaults, no mandatory API keys for a useful first run |
| **The Frontend Developer** | Builds a globe, 2D map, phone app, Grafana board or CLI against Foresignal. | Stable, documented, typed API; GeoJSON; tiles; streaming; CORS; SDKs |
| **The Agent** | An MCP-speaking agent (OpenClaw, Claude, n8n) or HTTP client that queries data, manages watches, receives alerts or maintains adapters. | MCP tools, scoped tokens, machine-readable health, raw-fetch diagnostics, predictable schemas |
| **The Integrator** | Connects home automation, local receivers, scripts or chat ops to Foresignal. | Inbound webhooks, device mappings, notification channels, outbound sinks, a Postman collection |
| **The Contributor** | Open-source developer adding or fixing an adapter. | One small class per adapter, fixture-based tests, clear contract |
| **The Analyst** | The operator (or an agent) in analysis mode: follows situations, frames key questions, forecasts, and reads daily, weekly and monthly assessments. | Cited evidence, base rates, competing hypotheses, calibrated probabilities, a track record |

## 7. Core concepts

| Concept | Definition |
|---|---|
| **Source** | One upstream feed integration (e.g. `usgs-earthquakes`). Has an adapter, settings (enabled, interval, params, budget, retention, credentials) and health. |
| **Adapter** | The code that fetches one source and converts the response into normalized records. |
| **Subscription** | A source being *enabled*. The only thing that causes fetching. |
| **Record** | One normalized item (an earthquake, an aircraft, a fire pixel, a sanctions entry). Identified by `(source, externalId)`. |
| **Version** | An immutable revision of a record. A new version is written only when significant content changes, or when the source's ingest policy asks for a keyframe. |
| **History tier** | `STANDARD` (discrete records kept for an operator-chosen duration) or `TRACK` (dense moving objects kept for one rolling window and retained by dropping whole partitions). |
| **Ingest policy** | Per source, `ALL`, `SAMPLED` or `LATEST`, with keyframes: decides which observations become versions and which only update live state (a *touch*). |
| **Kind** | The record's category (e.g. `earthquake`, `aircraft`, `vessel`, `satellite`, `fire`, `conflict-event`, `outage`, `weather-alert`, `camera`). |
| **Change** | The event of a new version being written. Changes are totally ordered by a global sequence number (`seq`). |
| **Budget** | A source's request allowance: max requests per minute and per day, plus a byte allowance (`mbPerDay`) for bulk downloads. |
| **Trigger rule** | A filter (sources/kinds) plus conditions (thresholds, geofences, places, watchlists, approaches) evaluated against changes; or a windowed rule (count, absence, anomaly). |
| **Alert** | A record of a trigger rule firing on a specific record version, with a lifecycle (firing, acknowledged, resolved). |
| **Retention** | How long versions are kept: one window for the TRACK tier, a global default plus per-source and per-kind overrides for the STANDARD tier. |
| **Custom source** | A runtime-defined source that receives records through the inbound webhook instead of being polled. |
| **Private source** | A source invisible on every read and egress path except to admins and holders of a `privateAccess` grant. |
| **License terms** | A source's structured license (commercial use, redistribution `ALLOWED`, `LOCAL_ONLY` or `FORBIDDEN`), resolved per record and enforced on every egress path. |
| **Channel** | A configured notification destination (Slack, ntfy, email, webhook, MQTT, Web Push, …). |
| **Sink** | An outbound feed that streams records to another system (MQTT state, TAK, readsb, NMEA, APRS-IS, CloudEvents) from a durable cursor. |
| **Token scope** | `read` < `agent` < `admin`; determines which API operations and MCP tools a token may use. |
| **Place** | A named point with a radius, polygon or route with a buffer, usable as a filter, a rule geofence or a weather area of interest. |
| **Raster product** | A gridded product (radar, satellite, model) stored as native-grid frames and rendered by Foresignal into tiles, images and loops. |
| **LLM provider** | A configured model endpoint (Anthropic, OpenAI-compatible, Ollama) assigned to the `chat`, `embedding` and/or `tts` role, with its own budget. |
| **Briefing** | A scheduled summary of a scope over a period, rendered as a digest or by the LLM and delivered to channels. |
| **Enrichment** | An LLM-written context summary attached to an alert. |
| **Entity** | A canonical physical object (an aircraft, a vessel) linked to its records through deterministic keys. |
| **Intelligence event** | A correlated occurrence with evidence, confidence, alternatives and the detector version, published as a record. |
| **Finding** | A deterministic discovery (change point, pre-event signal, unexplained move, co-occurrence, active precursor, projected impact, exceedance) with its base rate and caveats. |
| **Situation** | A persistent storyline grouping related intelligence events, findings, alerts and records over days to months, with daily snapshots. |
| **Precursor** | A pattern "X tends to precede Y within T" that survived false-discovery control and validation on later data. |
| **Base rate** | How often an event family occurs per window in a region, with an interval and its sources: the outside view. |
| **Hypothesis set** | A key question with competing hypotheses, priors, indicators, key assumptions and a revision history; reviewed on a cadence. |
| **Indicator** | An observable signal with likelihood ratios across hypotheses; it watches for itself through a trigger rule. |
| **Forecast question / forecast / forecaster** | A resolvable question; an immutable, timestamped probability from a named forecaster (base rate, statistical, precursor, analog, indicator-Bayes, LLM, operator, ensemble). |
| **Ledger** | The store of questions and forecasts, with automatic resolution, scoring and calibration. |
| **Canonical event / evidence group** | A real-world occurrence, linked to every observation of it; observations tracing to one originator form one evidence group, and only evidence groups count as independent. |
| **Observability** | The estimated probability that a source detects a phenomenon in a place at a time; needed before "nothing happened" can be concluded. |
| **Principal / group** | A named person, agent or service that holds tokens; groups collect principals for sharing. |
| **TLP** | Traffic Light Protocol 2.0 label (CLEAR, GREEN, AMBER, AMBER+STRICT, RED) bounding how a work product may be shared. |
| **Chronicle** | A named monitoring thread: a bundle of layers that records its own history while active, independent of global retention, and can be replayed, shared, archived or deleted. |
| **Report / package** | An analyst product with pinned evidence; a signed package carries it to principals on another instance. |
| **Chronicle network** | Relationships between chronicles discovered from keyed fingerprints of their evidence, never their content. |
| **Outlet rating** | Lean, factuality, ownership or state-affiliation of a news outlet from a declared, licensed provider. |
| **Object reference / workspace** | The canonical `type:id` name of any Foresignal object, and a shared investigation focus (selection, time range, viewport) that several clients follow live ([Client Contract](Client-Contract.md)). |
| **Assessment** | The output of a daily, weekly or monthly analysis cycle: key judgments, what changed, hypotheses, indicators, forecasts and speculation, following ICD 203. |

## 8. Functional requirements

Numbers in parentheses are the roadmap prompts that delivered a requirement ([Roadmap](Roadmap.md)).

### 8.1 Source management
- **FR-1** The broker exposes the catalog of all available sources with their descriptor (id, name, kinds, homepage, attribution, license terms, history tier, ingest policy, whether credentials are required, default interval and budget).
- **FR-2** The operator can enable/disable a source, set its polling interval (never below the adapter's minimum), adapter parameters, ingest policy, and budget, via `PUT`.
- **FR-3** The operator can set upstream credentials per source via a write-only endpoint; credentials are never returned by any endpoint.
- **FR-4** Enabling a source starts collection within one scheduler tick; disabling stops it and cancels in-flight scheduling.
- **FR-5** A source requiring credentials cannot be enabled until credentials are set (returns 409 with an explanatory error).
- **FR-6** The operator can trigger a one-off fetch (`POST /v1/sources/{id}/fetch`) that ingests normally, subject to budget.
- **FR-7** The operator can request a diagnostic dry-run (`POST /v1/sources/{id}/probe`) that fetches once, returns the raw upstream response (truncated to a configurable size) plus the parsed records, and stores nothing. Probes consume budget.

### 8.2 Budgets and health
- **FR-8** Every outbound upstream request passes through the source's budget. Requests exceeding budget are refused (not queued) and recorded as `budget_exceeded`. Bulk downloads and byte-range reads are charged against `mbPerDay` before they are sent.
- **FR-9** Per-source usage is recorded per minute: requests, bytes received, errors, budget refusals, and ingest counters (observations, versions persisted, touches, late and clamped records).
- **FR-10** Per-source health reports: status (`healthy`, `degraded`, `failing`, `idle`, `disabled`), last attempt, last success, last data, last error message, consecutive failures, records in last fetch, error rate over the last hour, budget used today / remaining, coverage, and storage (versions per minute, persist ratio, projected bytes).
- **FR-11** A fetch that succeeds but yields zero records when the previous N fetches yielded records marks the source `degraded` (silent-format-change detection), unless the adapter declares zero results normal; a drop in reported coverage (receivers, expected items) is reported as coverage dropped, not as a format change.
- **FR-12** Upstream HTTP 429/503 responses trigger exponential backoff for that source, honoring `Retry-After`.

### 8.3 Ingestion and storage
- **FR-13** Adapters return normalized records: `externalId`, `kind`, `observedAt`, optional `eventTime`, optional geometry (GeoJSON-compatible, WGS84), optional `validFrom` and `validUntil`, a tombstone flag, a JSON payload, optional quality (accuracy, confidence) and up to eight observers. Each source has one fixed time base; a future `observedAt` is clamped to the receive time.
- **FR-14** For each record, the broker computes a static hash over the significant content (kept on the record head) and a full hash over all non-volatile content (kept on the live row). The source's ingest policy (`ALL`, `SAMPLED`, `LATEST`, with keyframes on the time axis and a retention keyframe every window/4) decides whether a changed observation becomes a new version with the next `seq` or only updates live state (a touch). Identical observations write nothing.
- **FR-15** Adapters may declare records removed (e.g. a sanctions entry delisted); the broker writes a tombstone version.
- **FR-16** Records whose `validUntil` has passed are excluded from "current" queries by default (e.g. aircraft not seen for N minutes).
- **FR-17** Retention is configured via `PUT /v1/settings/retention`. TRACK-tier history keeps one window (`trackWindow`, default 3 d) and is retained only by dropping whole partitions; STANDARD-tier history follows the per-source setting, else the adapter's per-kind or analytic-class default, else the global default (7 d); a per-source retention on a TRACK source is refused (`400 RETENTION_TIER_FIXED`). Current state is kept while current; versions follow the window; observed entities are re-keyframed every window/4, so they always keep a recent version (amended by the v1.2 core data model, now [Architecture](Architecture.md)).
- **FR-90** TRACK history older than a raw window is thinned by rewriting whole partitions to a keep set (first, reappearing, significant, tombstone and trigger-edge versions, the newest per entity, every version a head or alert references, and one per entity per interval); point-in-time reads over thinned history are marked `approximate` (044).

### 8.4 Query API
- **FR-18** `GET /v1/records` returns current records filtered by source(s), kind(s), bbox, place, saved view, region, time window, and free-text match on a small set of payload fields, with cursor pagination; output as JSON, GeoJSON, ndjson, GeoJSON sequence or CSV.
- **FR-19** `GET /v1/records/{source}/{externalId}` and `/versions` return a record and its full version history.
- **FR-20** `GET /v1/history` returns all versions within a time range and bbox for playback.
- **FR-21** `GET /v1/changes?after={seq}` returns changes after a cursor; `GET /v1/changes/stream` streams changes (plus live and touch events) over SSE, filterable by source, kind, bbox, place and view, resumable via `Last-Event-ID`, with explicit `resync` and `reset` events when a client falls behind or below the retention floor. `GET /v1/events/stream` multiplexes topics on one connection with a changeable filter.
- **FR-22** `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt` serves Mapbox Vector Tiles of current records, where a layer is a kind or source, with TileJSON and grid or hex aggregation at low zoom.
- **FR-23** The OpenAPI document is served at `/v1/openapi.json`, with interactive docs at `/docs`.
- **FR-91** Any read can be asked "as of" a past instant on the transaction-time axis: records, tiles, per-entity tracks (`/v1/records/{sourceId}/{externalId}/track`), keyframe-plus-delta frames for animation (`/v1/history/frames`) and an event-density timeline (`/v1/timeline`); agents get the same through MCP `get_state_at` and `get_track` (014, 037).
- **FR-92** Aggregates are served without paging raw history: an activity rollup that outlives retention, time series, H3 grids and top-N (`/v1/stats/*`), a count mode, a compact glance (`/v1/glance`) and Prometheus metric projections (038).
- **FR-93** Every record can carry an offline place context (country, first-level region, US county, sea area, nearest town) from bundled GeoNames and Natural Earth data, usable as filters and as `{{near.*}}` message placeholders, without creating versions (040).

### 8.5 Triggers and alerts
- **FR-24** The operator can create, update, enable/disable, snooze, mute and delete trigger rules via the API.
- **FR-25** A rule has: name, enabled, tags, `sources` and `kinds` arrays (the deprecated singular `source`/`kind` fold into them), zero or more conditions on payload paths, an optional geofence (GeoJSON Polygon/MultiPolygon, a route with a buffer, or `near` a place, a point or a moving anchor record) with an optional hysteresis band, a `mode` (`match`: fire on every matching version; `enter`: fire when a record starts satisfying the rule, for example a vessel entering a geofence; `exit`: fire when it stops satisfying it), `reappearAsEnter`, optional `onlyNew` (fire only on the first version of a record), a cooldown per record or per rule (`cooldownScope`), a severity, active windows (`activeFrom`/`activeUntil` and recurring local-time `activeWindow`s in an IANA timezone), a rate cap (`maxAlertsPerHour`, optional auto-disable on storms), an optional forecast horizon, a message template and optional enrichment. Condition operators are `>`, `>=`, `<`, `<=`, `==`, `!=`, `in` and `notIn` (arrays of up to 1,000 values of one type), `exists`, `startsWith`, `matches` (a guarded regular expression), `inWatchlist` (036) and `approach` (114). A rule may instead be windowed (FR-94).
- **FR-26** Rules are evaluated against each committed batch of changes, comparing against persisted state. A match creates an alert containing the rule, record version, a record snapshot and a rendered message.
- **FR-27** Alerts are queryable (`GET /v1/alerts`), streamable (`GET /v1/alerts/stream`, SSE, and Atom/JSON feeds) and have a lifecycle: `firing` → `acknowledged` → `resolved` (terminal). They can be acknowledged, resolved, snoozed and given feedback (`useful`, `noise`, `late`, `false_positive`); they resolve on their own per the rule's `autoResolve` (inverse edge, tombstone, expiry, or not observed for `staleAfter`); exercise alerts are marked as such (115).
- **FR-28** Alerts are delivered to notification channels (§8.6). Each rule has a `severity` (`info`/`warning`/`critical`) that drives notification priority.
- **FR-29** Rules can be tested against existing data without creating alerts (`POST /v1/triggers/{id}/test`) and backtested over retained history.
- **FR-94** Windowed rules of type `count` (N distinct entities in a window), `absence` (nothing matching for a window while the sources are covered) and `anomaly` (activity unusual against a learned, coverage-aware baseline) alert and resolve through the same lifecycle; a coverage drop is never reported as a drop in activity (100).

### 8.6 Notification channels
- **FR-35** The operator can create, test and delete channels of types `webhook`, `slack`, `mattermost`, `discord`, `teams`, `google-chat`, `telegram`, `matrix`, `zulip`, `ntfy`, `gotify`, `pushover`, `email`, `homeassistant`, `apprise`, `mqtt` (060), `meshtastic` and `meshcore` (084), plus the built-in `webpush`. Secret settings are write-only and encrypted. The built-in `webpush` channel also reaches native apps through APNs and FCM, using push providers (the operator's own APNs key and Firebase service account) that the operator configures, tests and deletes; their keys are write-only and encrypted too.
- **FR-36** Channels subscribe to events (`alert`, `briefing`, `system`), optionally filtered by rule and minimum severity, with optional title and body templates, quiet hours, folding, an optional delivery language (translated through the LLM, falling back to the original) and a choice to accept exercise alerts.
- **FR-37** Messages are formatted natively per destination (Block Kit, Adaptive Cards, embeds, HTML, priorities). Generic webhooks are HMAC-signed. Geolocated alerts carry a static map image on channels that accept images and a link elsewhere (055).
- **FR-38** Delivery is durable, with per-destination rate limits, `Retry-After` handling, a retry ladder and a delivery log per channel. Permanent failures raise a system event.
- **FR-95** Telegram and Matrix channels can be made interactive. Allowlisted chats may send commands (`/help`, `/status`, `/alerts`, `/alert`, `/ack`, `/resolve`, `/snooze`, `/feedback`, `/ask`, `/confirm`, `/reject`, `/brief`, `/sources`, `/questions`, `/forecast`), each chat mapped to `read` or `agent` scope (never `admin`). Foresignal replies only to allowlisted chats, uses the Bot API or the Matrix client API (never user sessions), and sends summaries and links rather than raw payloads (061).
- **FR-96** The `mqtt` channel publishes alerts, briefings and system events to the operator's broker, keeps rule states, source health and the glance as retained topics with a Last Will, and announces them to Home Assistant through MQTT discovery (060).
- **FR-97** The `meshtastic` and `meshcore` channels send compact alert lines over the operator's own LoRa mesh (never a public or default channel) under an airtime governor, and accept field reports, acknowledgments and status requests from allowlisted senders (084).

### 8.7 System
- **FR-30** On first start the broker generates a bootstrap admin token, stores its hash, and prints the token once to the log. The operator can rotate it via the API.
- **FR-31** Access is controlled by scoped tokens (§8.8). Data GETs are open by default (`openReads`); writes and configuration reads require the scope listed per operation.
- **FR-32** `GET /v1/system` reports version, uptime, DB status, record counts, storage size (per history tier), rasters, sinks and scheduler state. `GET /actuator/health` (with liveness and readiness groups) is available for container health checks.
- **FR-33** CORS is enabled for all origins on GET endpoints by default; allowed origins are configurable via the API.
- **FR-34** Configuration export/import: `GET /v1/settings/export` returns all settings (excluding credentials) as JSON; `PUT /v1/settings/import` applies them.

### 8.8 Access tokens
- **FR-39** The operator can create named tokens with scope `read`, `agent` or `admin` and an optional expiry, list them (never their values) and revoke them. A non-admin token may carry a read restriction (sources, kinds, bbox or area, endpoints, private exclusion, a request rate, a license policy) that every read path applies; header-less clients get path-bound, restricted, expiring feed keys and tile keys (`/v1/k/{key}/…`) (035, 050).
- **FR-40** `security.openReads` (default true) controls whether data reads need a token; tokenless reads run under the anonymous restriction profile (`security.anonymous`). Every client is rate-limited, repeated failed credentials back off without locking out a valid one, and every operator-supplied outbound URL passes one SSRF policy (035).

### 8.9 Inbound webhooks and custom sources
- **FR-41** The operator can create custom push sources (id, kinds, attribution, license terms, TTL, tile/search/embed fields, inbound rate limit, private flag). Each gets an ingest key shown once and rotatable.
- **FR-42** `POST /v1/ingest/{sourceId}` accepts GeoJSON, Foresignal records and the supported inbound formats (Alertmanager, CloudEvents, CAP, declarative mappings) (≤ 5 MB, ≤ 10,000 records) authenticated by ingest key. Pushed records get full versioning, triggers, search and retention. ntfy- and Gotify-compatible publish endpoints are provided under `/compat`.
- **FR-98** Any JSON, form or query-string device can be onboarded without code through a declarative record mapping with a preview, an LLM-drafted suggestion that is never stored or applied on its own, and bundled device presets for common homelab gear (059).
- **FR-99** Local receivers (readsb, AIS-catcher, weather stations, APRS, mesh nodes, RF decoders) connect over sockets, LAN URLs or inbox directories as ordinary sources, with station health (069, 082–086).

### 8.10 MCP server
- **FR-43** An MCP server at `/mcp` (streamable HTTP) exposes record queries, history and state-at, changes, search, sources and health, alerts and alert status, watches (triggers), places, weather sampling, map images, briefings, the intelligence layer and system status as tools, with scope enforcement and size-bounded outputs. Remote MCP clients may authenticate with an access token from the operator's authorization server per RFC 9728 and RFC 8707; `admin` is never granted that way (025, 141).

### 8.11 LLM providers
- **FR-44** The operator can configure LLM providers of type `anthropic`, `openai-compatible` or `ollama`, assign them to the `chat`, `embedding` and `tts` roles, set per-provider request and token (or character) budgets, test them and list their models.
- **FR-45** Every LLM call is budgeted and metered (`/v1/llm/usage`). Exhausted budgets refuse calls and raise a system event.
- **FR-46** Models never modify configuration. The only model-initiated writes are proposed actions (create or update a watch, create a briefing, acknowledge an alert), which run only on confirmation or when an agent-scope caller allows them.

### 8.12 Semantic search
- **FR-47** Records from sources that declare text fields, plus briefings and enriched alerts, are embedded into pgvector. `GET /v1/search` supports `semantic`, `text` and `hybrid` modes with source, kind, bbox and time filters. Text search works without an LLM.
- **FR-48** Changing the embedding model triggers a zero-downtime reindex into a second index slot. Search status reports coverage and backlog.

### 8.13 Ask
- **FR-49** `POST /v1/ask` (and a streaming variant) answers natural-language questions using Foresignal's data through tools, with citations verified against tool results, threads for follow-ups, and proposed actions. Providers without tool support use retrieval-only mode.

### 8.14 Briefings
- **FR-50** The operator or an agent can schedule briefings (cron and timezone, scope, lookback, digest or LLM mode, instructions, channels), run or preview them on demand, and browse past runs. Briefings can include "Weather at places" (056) and an optional audio rendition with a podcast feed (141).
- **FR-51** Digest mode needs no LLM. LLM mode falls back to digest on failure.

### 8.15 Alert enrichment
- **FR-52** Rules with `enrich=true` get an LLM context summary drawn from nearby records, the record's history and semantic neighbors. Channels can wait up to 60 s to include it. Enrichment never blocks delivery beyond that wait. Every geolocated alert also gets a deterministic weather context, worded as unknown (never clear) when data is stale or missing (056).

### 8.16 Web Push and native push
- **FR-53** Browsers can subscribe for push notifications using Foresignal's VAPID key, with event, rule, severity and tag filters. Native Android and Linux apps subscribe the same way through UnifiedPush, passing Foresignal's VAPID key to their distributor. A subscription receives notifications only after echoing a verification code. Delivery uses RFC 8291 encryption, and expired subscriptions are removed automatically. Native iOS, iPadOS, macOS and Android apps register their APNs or FCM device tokens with event, rule, severity and tag filters and receive visible alerts that carry only ids, the severity and a minimal (or, per device, a short) text; the app fetches the details from Foresignal itself. Dead device tokens are removed automatically, and revoking the registering token removes its devices.

### 8.16a Actionable notifications, inbox and escalation (033)
- **FR-82** The operator can acknowledge, snooze or mute an alert from the notification itself (ntfy and Web Push buttons, Telegram buttons, a Matrix reaction, a Pushover acknowledgment, or a confirm-page link) without any token on the device. Each action is a single-use, expiring capability; opening its link only shows a confirm page, and every execution is audited.
- **FR-83** Every client keeps a notification inbox in sync with per-device read state and badge counts (listing, cursor and sparse reads, a live stream); reading never acknowledges an alert. Phone channels can hold non-critical alerts back while a desktop client is active and drop them if they were read or acknowledged meanwhile; critical alerts always go immediately.
- **FR-84** Escalation policies send an unacknowledged alert to further channels after set delays, with an optional repeat, until it is acknowledged (always) or resolved or snoozed (per policy); muting a rule stops its escalations.

### 8.16b Places, views and watchlists (036)
- **FR-85** The operator or an agent can name places (a point with a radius, a polygon, or a route with a buffer) once and use them everywhere: as `place=` filters on records, history, changes, tiles, streams and search; in rules (`near.placeIds`), views, briefings and Ask scopes; and as the weather area of interest. Places are sensitive: reading them always needs a token, and the LLM, notifications and anonymous readers never see their coordinates. Editing a place moves every rule that uses it without firing edges for records that did not move.
- **FR-86** Saved views give a named filter (sources, kinds, places, an area, conditions, a field projection) that records, map tiles and the SSE stream render identically, and that `view=` applies to the other read paths.
- **FR-87** Watchlists hold up to 100,000 identifiers (ICAO 24-bit addresses, registrations, callsigns, MMSIs, IMO numbers or custom values), imported from CSV or JSON with a dry run and exported as CSV. One `inWatchlist` condition matches the same entities across every source that declares the identity; alerts of such rules are sensitive and hidden from anonymous readers and feed keys.
- **FR-88** "Near me" rules follow a moving anchor record (a phone): they fire when a target comes near the anchor and when the anchor moves near a target, and they pause, with a system notification, while the anchor is stale.

### 8.16c Private sources and personal location (039)
- **FR-89** A source can be private (by its descriptor, a custom source's `private`, or the operator's tightening, which can never be undone for a descriptor-private source). A private source is invisible on every read and egress path (records, history, changes, tiles, SSE, search, MCP, alerts, stats, glance, metrics, notifications, LLM context, exports, sinks, federation, shares) unless the caller is an admin or holds a token, feed key or ticket created with `privateAccess`; anonymous callers never see it and direct reads answer 404. OwnTracks, Traccar Client/OsmAnd, Overland and GPSLogger push natively into a `personal-location` source with device keys that write only their own entity, and any single entity can be exported (GeoJSON, ndjson, GPX) or purged with a tombstone that tells clients and caches to delete it.

### 8.16d Weather, rasters, imagery and maps (042–058, 105–108)
- **FR-55** Radar, satellite and model products (MRMS, GOES, GFS, HRRR, ECMWF IFS and the extended product list) are ingested by enabled, budgeted sources (GRIB2 by byte range, NetCDF and S3 objects), stored as native-grid frames, and rendered by Foresignal itself into WebMercator tiles, single images, animation loops and legends through a products API (`/v1/products`), with a versioned style registry and a bounded render queue. No client request ever causes an upstream fetch; external layers (GIBS, nowCOAST) are catalog entries clients fetch directly (045).
- **FR-100** Images Foresignal derives or receives are kept in a content-addressed media store; every image passes an image guard before any pixel is decoded, and media follow the lifetime of the record that owns them (043).
- **FR-101** Rasters can be sampled at points, along tracks, over areas and as time series (`/v1/sample`), with explicit status so missing or stale data never reads as clear; derived weather sources (`wx-places`, `wx-areas`, `wx-cells`) feed the trigger engine, SSE and MCP (052).
- **FR-102** Wind is delivered as particle fields, barb vector tiles and a speed style; MRMS storm cells are tracked with merges, splits, a nowcast and a lightning-jump flag; places can carry forecast-area thresholds and a model-disagreement flag (051, 105).
- **FR-103** The operator's SatDump ground stations are sources read only through SatDump's files and HTTP API: passes with RF statistics, composites and thumbnails, geostationary frames, georeferenced polar overlays with footprints or estimated swaths, filtered DCS and EMWIN, and remote station uploads (049, 054, 107).
- **FR-104** Map clients get MapLibre overlay styles and sprites generated from kind display hints, Atom and JSON Feed for alerts and briefings, KML with self-refreshing NetworkLinks and tracks, WMTS with time, OGC API Tiles and EDR queries, Terrain-RGB and WebP tiles, and MBTiles, PMTiles and COG exports; every output is overlay-only (058, 108).

### 8.17 Derived layers, products and provenance
- **FR-54** Derived sources compute layers from stored records (GPS jamming cells, AIS gaps, loitering, rendezvous, deception checks, holding patterns, emergency squawks, port calls, closest approaches, corroboration, aircraft fusion and others) and publish them as ordinary records; absence-based layers are coverage-aware and suppressed on degraded input (099).
- **FR-56** Each source has structured license terms (commercial use; redistribution `ALLOWED`, `LOCAL_ONLY` or `FORBIDDEN`; attribution), resolved per record from the fetch that produced it (a DERIVED source takes the most restrictive terms of its inputs). One egress policy applies them, together with privacy and TLP, on every outbound path (API, tiles, SSE, MCP, remote LLMs, notifications, exports, sinks, federation, shares, feeds). Clients can request license-filtered responses (`no-nc`, `no-sa`, `redistributable`, with a per-token floor), and attributions are aggregated per response (`/v1/attributions`). Sources carry reliability grades, records a credibility, and the instance signs manifests, briefings and federation documents with its own Ed25519 key published as JWKS (109).
- **FR-57** The adapter doctor diagnoses a failing source: it probes the upstream, compares the result with the packaged fixture, names what changed deterministically, and with an LLM explains the change and proposes a parser fix that is never applied (140).
- **FR-105** A canonical entity registry links records to one entity per physical object (vehicles and objects, never people) through deterministic keys, with aliases, confidence and validity, and imports the FAA aircraft registry without personal data (098).
- **FR-106** Intelligence events carry evidence, confidence, alternatives and the detector version; deterministic correlation recipes create them, investigations collect them into case files with snapshots and export, and exposure queries estimate who and what is affected (101).
- **FR-107** A scheduled LLM analyst reviews new events and anomalies within a scope, writes validated notes, and proposes watches and intelligence events that run only when allowed (102).
- **FR-108** Rules, correlation recipes and precursor tests ("does X precede Y?") can be backtested over retained history, with caveats, intervals and alert-quality statistics that drive deterministic tuning suggestions (103, 115).
- **FR-109** An infrastructure dependency graph (power, telecom, transport, hospitals) built from reference datasets and OpenStreetMap answers which dependencies a hazard affects (104).
- **FR-110** Pattern of life per tracked entity, trip and commute-corridor watch, and kinematic closest-approach and ETA (`approach` conditions) are computed server-side; routing stays in the client (114).
- **FR-111** Exercise replays run historical data through the trigger engine with every resulting alert and notification marked as an exercise, and channels choose whether to receive them (115).

### 8.17a Outputs, exchange and federation (110–113, 141)
- **FR-112** Outbound sinks stream records from durable cursors to other systems (MQTT retained state, CoT/TAK, readsb `aircraft.json` and SBS, NMEA AIVDM, APRS-IS, CloudEvents), each record passing the egress policy first (110).
- **FR-113** Export jobs write GeoParquet, GeoPackage, FlatGeobuf, GPX and other formats at a point in time (`atSeq`), with holds that keep the needed history, a cold archive with a catalog, and offline packs (111).
- **FR-114** Standards outputs serve existing tools: OGC API – Features (current and history collections), CAP output, iCalendar subscriptions, expiring share links for header-less clients, CZML, FDSN event (QuakeML) and STIX 2.1 / TAXII 2.1 (112, 141).
- **FR-115** Foresignal instances replicate records to each other with provenance, global record references and loop prevention, and send compact intelligence messages for constrained links (113).
- **FR-116** Official clients are generated from the spec (Dart, TypeScript, Python) with a hand-written streaming and workspace layer; a command-line client covers the API; optional WASM parser plugins, approved by hash and sandboxed, extend parsing without changing the core (141).

### 8.18 Intelligence layer (v1.3; [Intelligence Design](Intelligence-Design.md))
- **FR-58** Observations of the same real-world event link to a canonical event and are grouped by originator; only independent evidence groups count as confirmation. A time-versioned observability model gates every absence conclusion, and every analytic output records a replayable manifest (116).
- **FR-59** Market, positioning, attention and structural series (CFTC COT, FRED, Wikipedia pageviews, GDELT timelines, World Bank) are collected as series with release lags and license-aware egress (117).
- **FR-60** Situations group related evidence deterministically, persist with a lifecycle and daily snapshots, and answer "what changed since" without an LLM (118).
- **FR-61** Change points, event studies (including live pre-event signals and unexplained moves) and co-occurrences are detected over registered series with stated base rates and caveats (119).
- **FR-62** Precursor patterns are mined with false-discovery control, validated on later data, kept in a library with a lifecycle, and announced live when their antecedent occurs (120).
- **FR-63** Base rates exist per event family and region from Foresignal's history and imported reference datasets; historical analogs of a situation are found with their outcomes (121).
- **FR-64** A forecast ledger records questions with machine resolvers and immutable forecasts from many forecasters, resolves them automatically, scores them (Brier, log, RPS, CRPS), shows calibration and bias, combines forecasters into a recalibrated ensemble, and evaluates forecasters retrospectively without look-ahead (122).
- **FR-65** A benchmark harness measures false discoveries, recall, delay, duplicate resistance and calibration on planted-truth scenarios and a historical corpus, and gates autonomous features (123).
- **FR-66** Rolling hypothesis sets have priors from base rates, indicators that watch for themselves, deterministic revisions with rationale, an ACH matrix and key assumptions; speculative links carry alternatives and coincidence baselines (125).
- **FR-67** Series forecasts, regional 4-week outlooks and regional indices are published with their measured accuracy; index weights are fitted to outcomes and proposed (126).
- **FR-68** Hazard forecasts are turned into scored, probabilistic impact statements on places and infrastructure (127).
- **FR-69** Daily, weekly and monthly analysis cycles publish assessments that build on the previous one, with optional LLM analyst, red-team, forecaster and proposer roles; an intel board summarizes the layer (128).
- **FR-70** Every object has a canonical reference and envelope; a time-aware relation graph, annotations with human verdicts, synchronized workspaces with deep links, and sensor discovery serve all frontends through one contract (129).
- **FR-71** Principals, groups, access lists and TLP labels let several people share work products on one instance; tags, threaded comments with mentions, attachments, Admiralty-graded evidence items, investigations and saved searches support collaboration (130).
- **FR-72** Chronicles bundle chosen layers into named monitoring threads that record their own history while active, replay at any past moment, and can be proposed by the analysis cycles, shared, archived and deleted (131).
- **FR-73** Reports are drafted from tags, selections, investigations, chronicles or situations, published with pinned evidence and TLP banners, and sent to principals on the same instance or as signed packages to other instances (132).
- **FR-74** News articles (metadata only) are clustered into stories; outlets carry lean, factuality and ownership ratings from licensed providers; coverage balance, blindspots and divergence inform situations, hypotheses and assessments (133).
- **FR-75** Chronicles discover related chronicles (other analysts' and other instances') from privacy-preserving fingerprints, disclosed only as discoverability and TLP allow; shared evidence counts once and circular reporting is detected (134).
- **FR-76** Probabilities are stored as numbers and expressed with ICD 203 terms; likelihood and confidence are separate, and generated text is linted.
- **FR-77** The operator can enable social platform adapters (Bluesky, Mastodon, Reddit, YouTube, X, Threads, Instagram hashtags, and any other platform through EDP providers) with their own credentials and spend caps; tracked topics, places and entities get hourly mention-volume series with coverage-aware baselines and velocity findings; retention and deletion sync are per-platform settings (135).
- **FR-78** Social posts cluster into narratives counted by independent origin, with structured multilingual claims, image fingerprints and reuse findings, cross-platform propagation with earliest observed posts, and coordination indicators (136).
- **FR-79** Social claims are compared with independent physical evidence (corroborated with lead times, unobserved despite adequate coverage, contradicted, insufficient coverage); attention gaps are reported where social coverage is adequate; physical findings cue budgeted social searches; the predictive value of social signals is measured out of sample (137).
- **FR-80** Third parties can add data sources without changing Foresignal through the External Data Protocol: a provider registers a self-describing manifest (by the operator or with a one-time registration token), pushes or serves items (features, events, observations, documents, forecasts, deletes, status), and its layer streams appear as map layers while the operator approves the source (024).
- **FR-81** The operator binds atomic EDP streams to series, canonical-event families, the social and news pipelines, hypothesis evidence and indicators, forecast questions (as scored external forecasters), places, situations and chronicles; provider libraries and a validator make writing providers easy (138, 141).

## 9. Non-functional requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-1 | Footprint | Idle broker with 10 sources enabled runs within 512 MB heap on a modest Linux box. |
| NFR-2 | Throughput | Sustain 40,000 observations/minute, of which up to 20,000 become record versions/minute (dense aircraft feed), on a single node. |
| NFR-3 | Latency | Change visible on the SSE stream ≤ 2 s after commit; alerts ≤ 5 s after the triggering commit. |
| NFR-4 | Startup | Broker ready ≤ 20 s after Postgres is reachable. |
| NFR-5 | Resilience | A failing or hanging adapter never affects other sources (isolated virtual threads, per-request timeouts). |
| NFR-6 | Persistence | Settings, rules, credentials and data survive restarts; scheduler resumes enabled sources on boot. |
| NFR-7 | Security | Admin token hashed at rest; credentials encrypted at rest with a key generated on first start; no secrets in logs. |
| NFR-8 | Testability | ≥ 90% line coverage across unit + integration tests; every adapter has a fixture-based contract test. |
| NFR-9 | Portability | Runs on Linux (amd64/arm64) and macOS via Docker Compose; also runnable as a plain JAR against any PostgreSQL with PostGIS and pgvector. |
| NFR-10 | API stability | `/v1` is additive-only once released; breaking changes go to `/v2`. From v1.0.0 the spec is frozen at `docs/api/v1.0.0/OpenAPI.json` and the Gradle task `checkApiCompat` (oasdiff) reports breaking changes against it. |
| NFR-11 | Observability | Structured JSON logs; Micrometer metrics at `/actuator/prometheus` ([Metrics](Metrics.md)). |
| NFR-12 | Notification latency | Alerts reach channels ≤ 10 s after commit when no enrichment wait is configured. |
| NFR-13 | LLM cost control | No LLM call happens without an explicit trigger (ask, briefing, enrichment, embedding of an opted-in source, analyst run, doctor explanation, translation, speech), and every call is budgeted. |
| NFR-14 | Prompt-injection containment | Record content is treated as untrusted data. Models cannot change configuration, and writes go through proposed actions and scope checks. |
| NFR-15 | Local-first option | Every LLM feature works with a local Ollama model and no external network access. |

None of these has yet been measured on a running instance (§14).

## 10. Source plan

The end-to-end loop was proven first with **USGS Earthquakes** (011). A core set of high-value, mostly keyless sources across distinct kinds followed (062–068):

| Source | Kind | Key? |
|---|---|---|
| USGS Earthquakes (GeoJSON summary feeds) | earthquake | No |
| adsb.lol | aircraft | No |
| OpenSky Network | aircraft | Optional |
| AISStream | vessel | Yes |
| CelesTrak (GP elements) | satellite | No |
| NASA FIRMS | fire | Yes (free MAP_KEY) |
| GDELT (events) | conflict-event | No |
| NWS / NOAA active alerts | weather-alert | No |
| NHC tropical cyclones | cyclone | No |
| IODA internet outages | outage | No |
| GDACS disaster alerts | disaster | No |
| Launch Library 2 | launch | No |

The remaining families were delivered in 069–096 (local receivers, transit, bikeshare, cameras, generic feature and feed kits, road events, fire perimeters, cyber, space weather and ground stations, environment, hydrology, rail, conflict, radio, Meshtastic and MeshCore, keyless AIS, local RF decoders, aviation safety, maritime, CAP and international seismic, disasters and humanitarian, sanctions and filings, internet measurement, energy series, reference datasets, citizen science, satellite scenes), the weather and imagery sources in 042–056 and 105–107, the market and attention series in 117, news in 133, social platforms in 135 and OpenStreetMap infrastructure in 104. The code holds 105 adapter packages under `ai.foresignal.source.adapters`; every source is installed disabled.

Not built, with the reason recorded in the completion reports: prediction markets (evaluated and excluded in 093; they belong to EDP providers), `gfw-sar-detections` and USCG Local Notices to Mariners (088: verification conditions not met), Microsoft Planetary Computer scenes (096: asset links need SAS signing), and the live MBFC ratings client (133: endpoint could not be verified). The [Source Catalog](Source-Catalog.md) lists each source with its prompt, history tier, ingest policy, license and egress, and marks every source that was not implemented.

## 11. Constraints and decisions

- Java 21 LTS, Spring Boot 4.1 (4.1.1), Gradle (Kotlin DSL) with wrapper. Version 1.0.0.
- PostgreSQL 17 + PostGIS 3.5 + pgvector (the compose `db` image). **The schema is owned by Flyway** (144): `src/main/resources/db/migration/V1__baseline.sql` creates the extensions, the partitioned history parents and every table and index; Hibernate only validates (`ddl-auto=validate`). An existing development database is adopted at version 1 without running V1 (`baseline-on-migrate`). Later changes are `V{n}__{description}.sql` migrations, additive by default, one per change, never edited once applied. Dynamic DDL remains only where it is inherent: history and chronicle partition leaves, history downsampling, the embedding reindex slots, reference-data staging tables, and SQLite export files. Until 144 the schema was managed by Hibernate (`ddl-auto=update`) plus bootstrap SQL.
- The V1 baseline was derived by hand from the JPA entities and the former bootstrap SQL, because no database could be started under the owner directive below. It must be compared with a schema built by the pre-cutover code (`scripts/db/dump-schema.sh`; `FlywaySchemaParityIT`, written and not run) before the release is tagged.
- Single Gradle module, packages under `ai.foresignal`; the CLI is a separate Gradle build in `cli/`.
- MIT license. God's Eye View (MIT) code may be ported with its copyright notice retained in `THIRD_PARTY_NOTICES.md`. ShadowBroker, Velocity and World Monitor (AGPL) are reference material only — no code is copied. SatDump (GPL-3.0) is reference only: Foresignal talks to it through files and HTTP and contains none of its code. No Meshtastic code, protobuf class or `.proto` file is in the repository; every Meshtastic path reads JSON.
- Development methodology: numbered prompts executed by Claude Code, each producing a completion report and ending with compile, commit, push.
- **Test policy history.**
  - Prompts 001–097: feature prompts wrote tests without running them; the checkpoints ran the whole suite and fixed it until green (012: 1,577 tests; 022: 2,699; 041: 4,669; 057: 5,682; 097: 8,084, all passing), plus the sanctioned narrow runs of 007–009.
  - Prompts 098–123: tests written and not run, awaiting checkpoint 124.
  - **Owner directive of 2026-10-17:** no tests are run for any prompt, including the checkpoints 124, 139 and 142 and prompts 143–144; every prompt compiles only (`./gradlew compileJava compileTestJava`). Code written in 098–144 has therefore never been executed by a test, and NFR-8 has not been measured since 097. The full suite and the coverage gate (`./gradlew test jacocoTestReport jacocoTestCoverageVerification`) are deferred to the owner at code-complete.
- No CI configuration is committed; the proposed pipeline is in [CI Proposal](CI-Proposal.md) for the owner to adopt.
- LLM integration: Anthropic through the official Java SDK, defaulting to `claude-opus-5-5`; other providers over their HTTP APIs. Foresignal never depends on an LLM to function.

## 12. Assumptions

- The operator's network is trusted for reads; Foresignal is not exposed directly to the internet without a reverse proxy.
- Upstream feeds' terms permit personal, non-commercial use as configured; the operator is responsible for supplying their own keys. Foresignal records each source's terms and enforces their redistribution limits on egress (FR-56), but does not decide what the operator's use permits.
- Docker is available on the target host.

## 13. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Upstream APIs change or vanish | Adapters silently break | Health API with zero-result and coverage detection; fixtures and contract tests; adapter doctor (140); external AI maintenance agent |
| Upstream rate limits / bans | Loss of a source; reputational harm | Mandatory per-source budgets; backoff on 429/503; conservative defaults |
| Storage growth from dense feeds | Disk exhaustion | Retention via API; TRACK window with partition drops; downsampling; per-source overrides; storage per tier in `/v1/system` |
| Token leaked | Config tampering, credential exfiltration | Scoped tokens (agents never hold admin); credentials write-only and encrypted; revocation and rotation; reverse-proxy guidance |
| LLM cost runaway | Unexpected bills | Per-provider request and token budgets; usage API; system notification when a budget is exhausted; local-model option |
| Prompt injection through record content | Model misbehaves or tries to act | Untrusted-data framing; no configuration tools for models; proposed actions plus scope checks; citation verification |
| Data egress to frontier providers | Privacy concern | Operator choice per provider; local Ollama profile; private sources and `LOCAL_ONLY` terms never reach a remote LLM; documented clearly |
| Notification fatigue | Operator ignores alerts | Enter/exit modes, cooldowns, rate caps, severity floors per channel, quiet hours, folding, briefings instead of streams of alerts |
| Hand-derived Flyway baseline differs from the schema the entities expect | Startup validation failure or schema surprises at cut-over | `ddl-auto=validate` fails fast; `scripts/db/dump-schema.sh` and `FlywaySchemaParityIT` compare it with the pre-cutover schema; the owner runs both before tagging |
| Code from 098–144 has never run | Defects found only in operation | The owner runs the full suite and the runtime verification before the `v1.0.0` tag ([Runtime Verification](Runtime-Verification.md)) |
| Scope creep toward a UI | Delays core | UI lives only in separate frontend repositories |
| Spurious correlations from mining many patterns | False confidence | False-discovery control, out-of-sample validation, base rates on every finding, precursor decay |
| Overconfident or biased AI judgments | Misleading assessments | LLM forecasts are scored, capped, recalibrated and monitored for bias; deterministic detectors own the evidence |
| Speculation read as accusation | Reputational and legal harm | No person-level analysis; speculative links carry alternatives and coincidence baselines; fixed wording rules |
| Sensitive work shared too widely | Leak of analysis or sources | TLP on every work product, enforced in `EgressPolicy`; invisible-by-default access lists; audit log |
| Chronicle storage growth | Disk exhaustion | Per-layer capture modes, estimates before activation, size caps with visible fallback to events-only |
| Media ratings licenses or bias in ratings | Terms breach; skewed analysis | License acknowledgment per provider, `LOCAL_ONLY` where needed, provider breakdown and disagreement always shown, coverage treated as a caveat not a verdict |
| Licensed data leaking through outputs | Terms breach | Per-record license terms enforced by one egress policy on every path; `LOCAL_ONLY` for FRED, LSEG-sourced prices, local receivers and MLAT positions; `FORBIDDEN` redistribution for OpenSky, ACLED and AISStream; derived statistics only |

## 14. Success criteria

**Status: not yet measured.** Under the owner directive of 2026-10-17, prompt 143 wrote the verification procedure but nothing was run: the application was not started, no source was enabled, and no criterion below has been checked. The procedure, the measurement tables and one result cell per criterion are in [Runtime Verification](Runtime-Verification.md) §5; every result is pending the owner's run.

1. Foresignal runs on the bench server continuously for 7 days with the core sources enabled, with no source or LLM provider exceeding budget.
2. A trigger rule (e.g. `mag >= 6`, `enter` mode) fires and is delivered by SSE, Slack or Mattermost, ntfy and Web Push, with an LLM context summary.
3. A frontend renders live aircraft and earthquakes using only the OpenAPI spec, GeoJSON/MVT and SSE.
4. All tests pass with ≥ 90% coverage.
5. The health API correctly flags an adapter broken by a simulated upstream format change, and the adapter doctor explains the change.
6. A daily briefing is delivered every morning for 7 days, in both digest and LLM modes, and with a local model.
7. An OpenClaw agent connected over MCP with an `agent` token answers questions from Foresignal's data and creates a watch, and cannot change sources or credentials.
8. A custom source receives records from a script through the inbound webhook, and they trigger alerts.
9. After 90 days of operation, the forecast ledger shows at least 100 resolved questions, and the ensemble's Brier skill against the base-rate forecaster is positive with its interval reported.
10. A weekly assessment is published for 4 consecutive weeks, each explaining what changed against the previous one, with every judgment cited and every probability expressed in ICD 203 terms.
