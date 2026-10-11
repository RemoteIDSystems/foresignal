# Foresignal — Product Charter

| | |
|---|---|
| Product | Foresignal — headless intelligence broker |
| Name note | Not affiliated with foresignal.com (an unrelated forex-signals site); the project's domain is foresignal.ai |
| Domain | foresignal.ai |
| Owner | Adam Allard |
| Licence | MIT |
| Repository | gitlab.com/adam.allard/foresignal |
| Status | Charter v1.3 — 2026-10-11 |

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
6. **AI that summarises but never answers for itself.** Where AI exists it summarises the current feed. Risk indices use hand-set weights, correlations are computed and thrown away, briefings have no memory of last week, and forecasts, where they exist at all, have no demonstrated accuracy.

## 4. Goals

| # | Goal | Measure of success |
|---|---|---|
| G1 | Decouple subscription, display and alerting | Disabling display in every client has zero effect on collection; enabling/disabling a source through the API starts/stops fetching within one scheduler tick (≤ 5 s) |
| G2 | Be a good upstream citizen | No source ever exceeds its configured per-minute or per-day request budget; spend is visible per source |
| G3 | Own the history | Every distinct revision of every record is retained per the configured retention; any record's version history is queryable |
| G4 | One API, many clients | A frontend can be built using only the OpenAPI spec; GeoJSON, MVT tiles and filtered SSE are available |
| G5 | Alert on what matters | Threshold and geofence rules fire within one ingest cycle of the triggering record and are delivered via SSE and notification channels |
| G6 | Self-diagnosing adapters | A broken adapter (errors or silent zero results) is visible through the health API without reading logs |
| G7 | Trivial to run | `docker compose up` on Linux or macOS yields a working broker with no file edits beyond optional DB/port settings |
| G8 | Maintainable by an AI agent | Every adapter is self-contained with a recorded fixture and contract test; raw upstream fetches are available as a diagnostic; the adapter doctor explains failures |
| G9 | Reach the operator anywhere | Alerts, briefings and system events reach Slack, Mattermost, Discord, Teams, Google Chat, Telegram, Matrix, ntfy, Gotify, Pushover, email, webhooks and Web Push within 10 s of the alert |
| G10 | Agent-native | Every data query and watch operation is available as an MCP tool, and agents are confined by token scope |
| G11 | Optional intelligence, never required | With an LLM configured, Ask answers with verified citations, briefings are narrative and alerts carry context. Without one, every non-LLM feature works unchanged. Local models are fully supported. |
| G12 | Open to inbound data | Any system can push records into a custom source through an inbound webhook, and those records get the same versioning, alerting and search as polled data |
| G13 | Correlate and remember | Unusual changes, cross-domain co-occurrences and pre-event market or attention moves are detected deterministically with a stated base rate; related evidence is grouped into situations that persist for weeks; "what changed since last week" is answered from stored snapshots |
| G14 | Forecast honestly | Every probability Foresignal produces starts from a stated base rate, is recorded immutably with its knowledge cutoff, resolves automatically from Foresignal's own data where possible, and is scored (Brier, log, CRPS) with calibration shown per forecaster, including the LLM and the operator |

## 5. Non-goals

- A user interface of any kind inside the broker. Frontends (globe, operator console, mobile) are separate repositories.
- Password logins or an identity provider inside the broker. People and agents are **named principals** holding scoped API tokens (`read`, `agent`, `admin`) and pairing codes; groups and per-object sharing exist for work products (prompt 130), but Foresignal is not a user-directory product.
- Training or fine-tuning neural models, or bundling model weights. Foresignal calls providers the operator configures. (Fitting statistical models to the operator's own data, such as baselines, forecasts, index weights and recalibration, is in scope.)
- Analysing private individuals, or accusing anyone of wrongdoing. Market findings describe aggregate activity only.
- Sending email or chat messages other than notifications the operator configured.
- Horizontal scale-out or clustering (single instance by design).
- Hosting or proxying basemaps and imagery (Esri, GIBS, Google 3D Tiles, OSM tiles are fetched by clients directly).
- Fringe and specialist source adapters in the core (web maps, scrapers, browser automation, prediction markets, niche feeds). They are separate projects that speak the [External Data Protocol](External-Data-Protocol.md); Foresignal accepts whatever the operator connects and does not police that choice.
- Real-time sub-second latency guarantees.

## 6. Users and personas

| Persona | Description | Primary needs |
|---|---|---|
| **The Operator** (primary; the owner) | Runs Foresignal on an always-on Linux bench server. Configures sources, budgets, retention and triggers through the API or the future operator console. | Reliability, visibility of spend and health, alerts, zero file editing |
| **The Occasional Runner** | Individual who starts Foresignal with Docker Compose when they want to look at the world, then stops it. | Fast startup, sensible defaults, no mandatory API keys for a useful first run |
| **The Frontend Developer** | Builds a globe, 2D map, phone app, Grafana board or CLI against Foresignal. | Stable, documented, typed API; GeoJSON; tiles; streaming; CORS |
| **The Agent** | An MCP-speaking agent (OpenClaw, Claude, n8n) or HTTP client that queries data, manages watches, receives alerts or maintains adapters. | MCP tools, scoped tokens, machine-readable health, raw-fetch diagnostics, predictable schemas |
| **The Integrator** | Connects home automation, local receivers, scripts or chat ops to Foresignal. | Inbound webhooks, notification channels, a Postman collection |
| **The Contributor** | Open-source developer adding or fixing an adapter. | One small class per adapter, fixture-based tests, clear contract |
| **The Analyst** | The operator (or an agent) in analysis mode: follows situations, frames key questions, forecasts, and reads daily, weekly and monthly assessments. | Cited evidence, base rates, competing hypotheses, calibrated probabilities, a track record |

## 7. Core concepts

| Concept | Definition |
|---|---|
| **Source** | One upstream feed integration (e.g. `usgs-earthquakes`). Has an adapter, settings (enabled, interval, params, budget, retention, credentials) and health. |
| **Adapter** | The code that fetches one source and converts the response into normalized records. |
| **Subscription** | A source being *enabled*. The only thing that causes fetching. |
| **Record** | One normalized item (an earthquake, an aircraft, a fire pixel, a sanctions entry). Identified by `(source, externalId)`. |
| **Version** | An immutable revision of a record. A new version is written only when content changes. |
| **Kind** | The record's category (e.g. `earthquake`, `aircraft`, `vessel`, `satellite`, `fire`, `conflict-event`, `outage`, `weather-alert`, `camera`). |
| **Change** | The event of a new version being written. Changes are totally ordered by a global sequence number (`seq`). |
| **Budget** | A source's request allowance: max requests per minute and per day. |
| **Trigger rule** | A filter (source/kind) plus conditions (threshold, geofence) evaluated against changes. |
| **Alert** | A record of a trigger rule firing on a specific record version. |
| **Retention** | How long versions are kept; a global default plus per-source overrides. |
| **Custom source** | A runtime-defined source that receives records through the inbound webhook instead of being polled. |
| **Channel** | A configured notification destination (Slack, ntfy, email, webhook, Web Push, …). |
| **Token scope** | `read` < `agent` < `admin`; determines which API operations and MCP tools a token may use. |
| **LLM provider** | A configured model endpoint (Anthropic, OpenAI-compatible, Ollama) assigned to the `chat` and/or `embedding` role, with its own budget. |
| **Briefing** | A scheduled summary of a scope over a period, rendered as a digest or by the LLM and delivered to channels. |
| **Enrichment** | An LLM-written context summary attached to an alert. |
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

### 8.1 Source management
- **FR-1** The broker exposes the catalogue of all available sources with their descriptor (id, name, kinds, homepage, attribution, whether credentials are required, default interval and budget).
- **FR-2** The operator can enable/disable a source, set its polling interval (never below the adapter's minimum), adapter parameters, and budget, via `PUT`.
- **FR-3** The operator can set upstream credentials per source via a write-only endpoint; credentials are never returned by any endpoint.
- **FR-4** Enabling a source starts collection within one scheduler tick; disabling stops it and cancels in-flight scheduling.
- **FR-5** A source requiring credentials cannot be enabled until credentials are set (returns 409 with an explanatory error).
- **FR-6** The operator can trigger a one-off fetch (`POST /v1/sources/{id}/fetch`) that ingests normally, subject to budget.
- **FR-7** The operator can request a diagnostic dry-run (`POST /v1/sources/{id}/probe`) that fetches once, returns the raw upstream response (truncated to a configurable size) plus the parsed records, and stores nothing. Probes consume budget.

### 8.2 Budgets and health
- **FR-8** Every outbound upstream request passes through the source's budget. Requests exceeding budget are refused (not queued) and recorded as `budget_exceeded`.
- **FR-9** Per-source usage is recorded per minute: requests, bytes received, errors, budget refusals.
- **FR-10** Per-source health reports: status (`healthy`, `degraded`, `failing`, `idle`, `disabled`), last attempt, last success, last error message, consecutive failures, records in last fetch, error rate over the last hour, budget used today / remaining.
- **FR-11** A fetch that succeeds but yields zero records when the previous N fetches yielded records marks the source `degraded` (silent-format-change detection), unless the adapter declares zero results normal.
- **FR-12** Upstream HTTP 429/503 responses trigger exponential backoff for that source, honouring `Retry-After`.

### 8.3 Ingestion and storage
- **FR-13** Adapters return normalized records: `externalId`, `kind`, `observedAt`, optional geometry (GeoJSON-compatible, WGS84), optional `validUntil`, and a JSON payload.
- **FR-14** For each record, the broker computes a content hash; if it matches the current version, nothing is written; otherwise a new version with the next `seq` is appended.
- **FR-15** Adapters may declare records removed (e.g. a sanctions entry delisted); the broker writes a tombstone version.
- **FR-16** Records whose `validUntil` has passed are excluded from "current" queries by default (e.g. aircraft not seen for N minutes).
- **FR-17** Retention is configured via `PUT /v1/settings/retention` with a global default and per-source overrides (ISO-8601 durations or shorthand like `7d`); a periodic job deletes versions older than the window. The current version of a record is kept unless it, too, is older than the window.

### 8.4 Query API
- **FR-18** `GET /v1/records` returns current records filtered by source(s), kind(s), bbox, time window, and free-text match on a small set of payload fields, with cursor pagination; output as JSON or GeoJSON.
- **FR-19** `GET /v1/records/{source}/{externalId}` and `/versions` return a record and its full version history.
- **FR-20** `GET /v1/history` returns all versions within a time range and bbox for playback.
- **FR-21** `GET /v1/changes?after={seq}` returns changes after a cursor; `GET /v1/changes/stream` streams changes over SSE, filterable by source, kind and bbox, resumable via `Last-Event-ID`.
- **FR-22** `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt` serves Mapbox Vector Tiles of current records, where a layer is a kind or source.
- **FR-23** The OpenAPI document is served at `/v1/openapi.json`, with interactive docs at `/docs`.

### 8.5 Triggers and alerts
- **FR-24** The operator can create, update, enable/disable and delete trigger rules via the API.
- **FR-25** A rule has: name, enabled, optional source filter, optional kind filter, zero or more threshold conditions (`payload path`, operator `> >= < <= == !=`, value), optional geofence (GeoJSON Polygon/MultiPolygon), a `mode` (`match`: fire on every matching version; `enter`: fire when a record starts satisfying the rule, for example a vessel entering a geofence; `exit`: fire when it stops satisfying it), optional `onlyNew` (fire only on the first version of a record), and a cooldown per record.
- **FR-26** Rules are evaluated against each committed batch of changes. A match creates an alert containing the rule, record version and a rendered message.
- **FR-27** Alerts are queryable (`GET /v1/alerts`), streamable (`GET /v1/alerts/stream`, SSE) and acknowledgeable.
- **FR-28** Alerts are delivered to notification channels (§8.6). Each rule has a `severity` (`info`/`warning`/`critical`) that drives notification priority.
- **FR-29** Rules can be tested against existing data without creating alerts (`POST /v1/triggers/{id}/test`).

### 8.6 Notification channels
- **FR-35** The operator can create, test and delete channels of types `webhook`, `slack`, `mattermost`, `discord`, `teams`, `google-chat`, `telegram`, `matrix`, `ntfy`, `gotify`, `pushover`, `email` and `webpush`. Secret settings are write-only and encrypted.
- **FR-36** Channels subscribe to events (`alert`, `briefing`, `system`), optionally filtered by rule and minimum severity, with optional title and body templates.
- **FR-37** Messages are formatted natively per destination (Block Kit, Adaptive Cards, embeds, HTML, priorities). Generic webhooks are HMAC-signed.
- **FR-38** Delivery is durable, with per-destination rate limits, `Retry-After` handling, a retry ladder and a delivery log per channel. Permanent failures raise a system event.

### 8.7 System
- **FR-30** On first start the broker generates a bootstrap admin token, stores its hash, and prints the token once to the log. The operator can rotate it via the API.
- **FR-31** Access is controlled by scoped tokens (§8.8). Data GETs are open by default (`openReads`); writes and configuration reads require the scope listed per operation.
- **FR-32** `GET /v1/system` reports version, uptime, DB status, record counts, storage size, and scheduler state. `GET /actuator/health` is available for container health checks.
- **FR-33** CORS is enabled for all origins on GET endpoints by default; allowed origins are configurable via the API.
- **FR-34** Configuration export/import: `GET /v1/settings/export` returns all settings (excluding credentials) as JSON; `PUT /v1/settings/import` applies them.

### 8.8 Access tokens
- **FR-39** The operator can create named tokens with scope `read`, `agent` or `admin` and an optional expiry, list them (never their values) and revoke them.
- **FR-40** `security.openReads` (default true) controls whether data reads need a token.

### 8.9 Inbound webhooks and custom sources
- **FR-41** The operator can create custom push sources (id, kinds, attribution, TTL, tile/search/embed fields, inbound rate limit). Each gets an ingest key shown once and rotatable.
- **FR-42** `POST /v1/ingest/{sourceId}` accepts GeoJSON or Foresignal records (≤ 5 MB, ≤ 10,000 records) authenticated by ingest key. Pushed records get full versioning, triggers, search and retention.

### 8.10 MCP server
- **FR-43** An MCP server at `/mcp` (streamable HTTP) exposes record queries, history, changes, search, sources and health, alerts, watches (triggers), briefings and system status as tools, with scope enforcement and size-bounded outputs.

### 8.11 LLM providers
- **FR-44** The operator can configure LLM providers of type `anthropic`, `openai-compatible` or `ollama`, assign them to the `chat` and `embedding` roles, set per-provider request and token budgets, test them and list their models.
- **FR-45** Every LLM call is budgeted and metered (`/v1/llm/usage`). Exhausted budgets refuse calls and raise a system event.
- **FR-46** Models never modify configuration. The only model-initiated writes are proposed actions (create or update a watch, create a briefing, acknowledge an alert), which run only on confirmation or when an agent-scope caller allows them.

### 8.12 Semantic search
- **FR-47** Records from sources that declare text fields, plus briefings and enriched alerts, are embedded into pgvector. `GET /v1/search` supports `semantic`, `text` and `hybrid` modes with source, kind, bbox and time filters. Text search works without an LLM.
- **FR-48** Changing the embedding model triggers a reindex. Search status reports coverage and backlog.

### 8.13 Ask
- **FR-49** `POST /v1/ask` (and a streaming variant) answers natural-language questions using Foresignal's data through tools, with citations verified against tool results, threads for follow-ups, and proposed actions. Providers without tool support use retrieval-only mode.

### 8.14 Briefings
- **FR-50** The operator or an agent can schedule briefings (cron and timezone, scope, lookback, digest or LLM mode, instructions, channels), run or preview them on demand, and browse past runs.
- **FR-51** Digest mode needs no LLM. LLM mode falls back to digest on failure.

### 8.15 Alert enrichment
- **FR-52** Rules with `enrich=true` get an LLM context summary drawn from nearby records, the record's history and semantic neighbours. Channels can wait up to 60 s to include it. Enrichment never blocks delivery beyond that wait.

### 8.16 Web Push
- **FR-53** Browsers can subscribe for push notifications using Foresignal's VAPID key, with event, rule and severity filters. Delivery uses RFC 8291 encryption, and expired subscriptions are removed automatically.

### 8.17 Derived layers, products and provenance
- **FR-54** Derived sources compute layers from stored records (GPS jamming cells, AIS gaps, loitering, rendezvous, deception checks, holding patterns) and publish them as ordinary records.
- **FR-55** Gridded weather products (GFS/ECMWF wind) are fetched by GRIB2 byte range and served through a products API.
- **FR-56** Each record carries licence and provenance metadata. Clients can request licence-filtered responses, and attributions are aggregated per response.
- **FR-57** The adapter doctor uses the LLM to diagnose a failing source: it probes the upstream, compares the result with the fixture, explains what changed and proposes a parser fix.

### 8.18 Intelligence layer (v1.3; [Intelligence Design](Intelligence-Design.md))
- **FR-58** Observations of the same real-world event link to a canonical event and are grouped by originator; only independent evidence groups count as confirmation. A time-versioned observability model gates every absence conclusion, and every analytic output records a replayable manifest (116).
- **FR-59** Market, positioning, attention and structural series (CFTC COT, FRED, Wikipedia pageviews, GDELT timelines, World Bank) are collected as series with release lags and licence-aware egress (117).
- **FR-60** Situations group related evidence deterministically, persist with a lifecycle and daily snapshots, and answer "what changed since" without an LLM (118).
- **FR-61** Change points, event studies (including live pre-event signals and unexplained moves) and co-occurrences are detected over registered series with stated base rates and caveats (119).
- **FR-62** Precursor patterns are mined with false-discovery control, validated on later data, kept in a library with a lifecycle, and announced live when their antecedent occurs (120).
- **FR-63** Base rates exist per event family and region from Foresignal's history and imported reference datasets; historical analogs of a situation are found with their outcomes (121).
- **FR-64** A forecast ledger records questions with machine resolvers and immutable forecasts from many forecasters, resolves them automatically, scores them (Brier, log, RPS, CRPS), shows calibration and bias, combines forecasters into a recalibrated ensemble, and evaluates forecasters retrospectively without look-ahead (122).
- **FR-65** A benchmark harness measures false discoveries, recall, delay, duplicate resistance and calibration on planted-truth scenarios and a historical corpus, and gates autonomous features (123).
- **FR-66** Rolling hypothesis sets have priors from base rates, indicators that watch for themselves, deterministic revisions with rationale, an ACH matrix and key assumptions; speculative links carry alternatives and coincidence baselines (125).
- **FR-67** Series forecasts, regional 4-week outlooks and regional indices are published with their measured accuracy; index weights are fitted to outcomes and proposed (126).
- **FR-68** Hazard forecasts are turned into scored, probabilistic impact statements on places and infrastructure (127).
- **FR-69** Daily, weekly and monthly analysis cycles publish assessments that build on the previous one, with optional LLM analyst, red-team, forecaster and proposer roles; an intel board summarises the layer (128).
- **FR-70** Every object has a canonical reference and envelope; a time-aware relation graph, annotations with human verdicts, synchronised workspaces with deep links, and sensor discovery serve all frontends through one contract (129).
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
| NFR-2 | Throughput | Sustain ingest of 20,000 record versions/minute (dense aircraft feed) on a single node. |
| NFR-3 | Latency | Change visible on the SSE stream ≤ 2 s after commit; alerts ≤ 5 s after the triggering commit. |
| NFR-4 | Startup | Broker ready ≤ 20 s after Postgres is reachable. |
| NFR-5 | Resilience | A failing or hanging adapter never affects other sources (isolated virtual threads, per-request timeouts). |
| NFR-6 | Persistence | Settings, rules, credentials and data survive restarts; scheduler resumes enabled sources on boot. |
| NFR-7 | Security | Admin token hashed at rest; credentials encrypted at rest with a key generated on first start; no secrets in logs. |
| NFR-8 | Testability | ≥ 90% line coverage across unit + integration tests; every adapter has a fixture-based contract test. |
| NFR-9 | Portability | Runs on Linux (amd64/arm64) and macOS via Docker Compose; also runnable as a plain JAR against any Postgres+PostGIS. |
| NFR-10 | API stability | `/v1` is additive-only once released; breaking changes go to `/v2`. |
| NFR-11 | Observability | Structured JSON logs; Micrometer metrics at `/actuator/prometheus`. |
| NFR-12 | Notification latency | Alerts reach channels ≤ 10 s after commit when no enrichment wait is configured. |
| NFR-13 | LLM cost control | No LLM call happens without an explicit trigger (ask, briefing, enrichment, embedding of an opted-in source), and every call is budgeted. |
| NFR-14 | Prompt-injection containment | Record content is treated as untrusted data. Models cannot change configuration, and writes go through proposed actions and scope checks. |
| NFR-15 | Local-first option | Every LLM feature works with a local Ollama model and no external network access. |

## 10. Source plan

The end-to-end loop is proven first with **USGS Earthquakes** (prompt 009). A core set of about ten high-value, mostly keyless sources across distinct kinds follows (prompts 029–035):

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

The remaining sources are ported from God's Eye View (cameras, transit, bikeshare, fire perimeters, radio) and re-implemented from the ShadowBroker, Velocity and World Monitor inventories (cyber, space weather, environment, rail, conflict, radio, Meshtastic, keyless AIS, aviation safety, maritime, disasters, sanctions, internet measurement, energy series, reference datasets). The [Source Catalog](Source-Catalog.md) maps each source to its roadmap prompt (036–056).

## 11. Constraints and decisions

- Java 21 LTS, Spring Boot 4.1, Gradle (Kotlin DSL) with wrapper.
- PostgreSQL 17 + PostGIS 3.5; schema managed by Hibernate (`ddl-auto=update`) during development; Flyway introduced only when preparing for production.
- Single Gradle module, packages under `ai.foresignal`.
- MIT licence. God's Eye View (MIT) code may be ported with its copyright notice retained in `THIRD_PARTY_NOTICES.md`. ShadowBroker, Velocity and World Monitor (AGPL) are reference material only — no code is copied.
- Development methodology: numbered prompts executed by Claude Code, each producing a completion report. Tests are written alongside code but **run only once the project is code complete** (prompt 061). Each prompt ends with compile, commit, push. Flyway arrives in prompt 063.
- LLM integration: Anthropic through the official Java SDK, defaulting to `claude-opus-5-5`; other providers over their HTTP APIs. Foresignal never depends on an LLM to function.

## 12. Assumptions

- The operator's network is trusted for reads; Foresignal is not exposed directly to the internet without a reverse proxy.
- Upstream feeds' terms permit personal, non-commercial use as configured; the operator is responsible for supplying their own keys.
- Docker is available on the target host.

## 13. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Upstream APIs change or vanish | Adapters silently break | Health API with zero-result detection; fixtures and contract tests; adapter doctor (prompt 060); external AI maintenance agent |
| Upstream rate limits / bans | Loss of a source; reputational harm | Mandatory per-source budgets; backoff on 429/503; conservative defaults |
| Storage growth from dense feeds | Disk exhaustion | Retention via API; per-source overrides; storage size in `/v1/system` |
| Token leaked | Config tampering, credential exfiltration | Scoped tokens (agents never hold admin); credentials write-only and encrypted; revocation and rotation; reverse-proxy guidance |
| LLM cost runaway | Unexpected bills | Per-provider request and token budgets; usage API; system notification when a budget is exhausted; local-model option |
| Prompt injection through record content | Model misbehaves or tries to act | Untrusted-data framing; no configuration tools for models; proposed actions plus scope checks; citation verification |
| Data egress to frontier providers | Privacy concern | Operator choice per provider; local Ollama profile; documented clearly |
| Notification fatigue | Operator ignores alerts | Enter/exit modes, cooldowns, severity floors per channel, briefings instead of streams of alerts |
| Hibernate DDL drift before Flyway | Schema surprises at production cut-over | Final roadmap step generates a Flyway baseline from the Hibernate schema |
| Scope creep toward a UI | Delays core | UI lives only in separate frontend repositories |
| Spurious correlations from mining many patterns | False confidence | False-discovery control, out-of-sample validation, base rates on every finding, precursor decay |
| Overconfident or biased AI judgments | Misleading assessments | LLM forecasts are scored, capped, recalibrated and monitored for bias; deterministic detectors own the evidence |
| Speculation read as accusation | Reputational and legal harm | No person-level analysis; speculative links carry alternatives and coincidence baselines; fixed wording rules |
| Sensitive work shared too widely | Leak of analysis or sources | TLP on every work product, enforced in `EgressPolicy`; invisible-by-default access lists; audit log |
| Chronicle storage growth | Disk exhaustion | Per-layer capture modes, estimates before activation, size caps with visible fallback to events-only |
| Media ratings licences or bias in ratings | Terms breach; skewed analysis | Licence acknowledgement per provider, LOCAL_ONLY where needed, provider breakdown and disagreement always shown, coverage treated as a caveat not a verdict |
| Licensed data leaking through outputs | Terms breach | `LOCAL_ONLY` egress for FRED, LSEG-sourced prices and ACLED; derived statistics only |

## 14. Success criteria

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
