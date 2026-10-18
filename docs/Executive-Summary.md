# Foresignal — Executive Summary

**Foresignal** (foresignal.ai, unrelated to foresignal.com) is an open-source, self-hosted **intelligence broker**. It is a headless service that continuously collects public telemetry from many upstream feeds, normalizes it into one versioned store, alerts on what matters, and re-serves everything through one documented API, an MCP server for AI agents, push notifications and outbound feeds. An optional LLM, either a frontier model or one running locally, turns that data into answers, briefings and context. Foresignal is the data foundation for a family of frontends: a 3D globe, a 2D analyst map, an operator console, mobile apps and agents.

## Current state

**Version 1.0.0 is code-complete, pending owner verification.** All 144 roadmap prompts have been implemented and compile. What is not yet done:

- **Tests.** The test suite was last run, and passed (8,084 tests), at checkpoint 097. Under the owner directive of 2026-10-17, tests for prompts 098–144 were written but not run, and the checkpoints 124, 139 and 142 compiled only. The full suite and the ≥ 90% coverage gate are the owner's first step.
- **Runtime verification.** The procedure is written ([Runtime Verification](Runtime-Verification.md)), but nothing was measured: no soak, drill or end-to-end run has taken place, and the Charter's success criteria are all pending.
- **Schema.** The database schema is now owned by a Flyway baseline, derived by hand from the entities because no database could be started; it must be compared with the pre-cutover schema before release.
- **Release.** The `v1.0.0` tag is created by the owner once the suite passes.

## The problem

Every existing open-source "god's-eye" project (ShadowBroker, God's Eye View, Velocity, World Monitor) fuses three concerns into one application:

1. **Subscription:** what data is collected, how often, and at what cost to upstreams.
2. **Display:** what a particular screen shows.
3. **Alerting:** what deserves attention.

Because these are fused, turning a layer off stops collecting it, so you can't watch quietly. Where they use AI, it summarizes the present: risk indices rest on hand-set weights, correlations are computed and discarded, briefings forget last week, and no one measures whether a forecast came true. History lives only as long as a browser tab or a short position cache. Each project re-implements the same 40–100 integrations inside a UI, with little accounting of upstream load. ShadowBroker was formally called out by the Meshtastic project for generating "1–2 orders of magnitude more traffic than typical clients."

## The solution

Foresignal separates the three concerns, and adds an intelligence layer on top:

- **Subscription is server-side** and is the only thing that causes fetching. Each source is enabled, scheduled and **budgeted** through the API, so the broker always knows its spend per upstream and never exceeds it. Every source ships disabled.
- **Display is the client's business.** Frontends use filtered queries, vector and raster tiles, time travel to any past instant, and filtered live streams, and their choices never reach the collector.
- **Alerting is a trigger engine.** It runs over the change stream (thresholds, set and pattern operators, geofences, named places, watchlists, "near me", approach, enter/exit, active windows, and windowed count, absence and anomaly rules) and delivers to Slack, Mattermost, Discord, Teams, Google Chat, Telegram, Matrix, Zulip, ntfy, Gotify, Pushover, email, webhooks, Home Assistant, Apprise, MQTT, Meshtastic and MeshCore mesh radios, and Web Push (browsers, UnifiedPush, APNs and FCM). Alerts can be acknowledged from the notification itself, escalate when nobody answers, and Telegram and Matrix chats can talk back. A subscribed but hidden layer can still announce itself.
- **Weather and imagery, rendered by Foresignal:** radar, satellite and model products (MRMS, GOES, GFS, HRRR, ECMWF) are ingested on budget and rendered into tiles, images, loops and wind fields; storm cells are tracked; weather is sampled at your places and added to alerts; the operator's own SatDump ground station becomes a source. No client request ever causes an upstream fetch, and no third-party basemap is proxied.
- **Intelligence is optional and budgeted:**
  - Ask questions in plain language and get answers with verified citations.
  - Schedule morning briefings, as text or audio.
  - Get LLM context added to alerts.
  - Search everything semantically using pgvector in the same Postgres database.
  - The model can be Anthropic Claude, any OpenAI-compatible API, or a local Ollama model, so data never has to leave the box.
- **Intelligence that keeps score** ([Intelligence Design](Intelligence-Design.md)):
  - Detect unusual changes, cross-domain co-occurrences, and market or attention moves before (or without) an event, each with a stated base rate.
  - Remember unfolding situations for weeks, with "what changed since last week" from stored snapshots.
  - Maintain rolling hypotheses: competing explanations, indicators that watch for themselves, and revisions that explain what moved.
  - Record every forecast in a ledger, resolve it from Foresignal's own data and score it, with calibration for each forecaster, including the LLM and you.
  - Publish daily, weekly and monthly assessments in intelligence-community style (ICD 203).
  - Count evidence by independent origin, know what could not have been seen, and benchmark every detector before trusting it.
- **Derived intelligence from raw tracks:** GPS jamming, AIS gaps, loitering, rendezvous, port calls and closest approaches; a canonical registry of vehicles and objects; correlated intelligence events and investigations; pattern of life and trip watch; an infrastructure dependency graph; backtests and exercise replays.
- **Built for teams and many instances:** named people and groups, sharing under TLP labels, tags, comments, graded evidence, investigations, **chronicles** (saved monitoring threads that keep their own replayable history), reports sent to colleagues here or as signed packages to other Foresignal instances, and instance-to-instance federation.
- **News coverage, not just news:** stories clustered across outlets, with lean, factuality and ownership from licensed rating providers, blindspots and coverage balance used as evidence-quality signals.
- **What people say versus what sensors see:** public social media (Bluesky, Mastodon, Reddit, YouTube, X, Threads and Instagram as the operator chooses, plus any platform an external provider brings) becomes narratives and claims counted by independent origin, compared with physical observations in both directions (corroborated with measured lead time, unobserved despite coverage, events with suspiciously little attention), with image-reuse and coordination checks.
- **Licenses respected end to end:** each source's terms are recorded per record and one egress policy enforces redistribution limits, privacy and TLP on every outbound path; attributions are aggregated per response.
- **Bring any source:** the [External Data Protocol](External-Data-Protocol.md) lets anyone write a data source as a separate project (web maps, scrapers, Telegram channels, prediction markets, niche feeds). Providers describe themselves with a manifest, deliver layers or atomic items, and the operator binds atomic streams to series, evidence, indicators, forecasts or chronicles, without changing Foresignal. Devices can also be onboarded with declarative mappings, and sandboxed WASM plugins can add parsers.
- **Speak the standards other tools speak:** OGC API Features, Tiles and EDR, WMTS, CAP, KML, Atom and JSON Feed, iCalendar, MapLibre styles, CZML, QuakeML, STIX/TAXII; exports to GeoParquet, GeoPackage, FlatGeobuf, GPX, MBTiles and PMTiles; outbound sinks to MQTT, TAK, readsb, NMEA, APRS-IS and CloudEvents.
- **One contract for every frontend** ([Client Contract](Client-Contract.md)): shared object references, a relation graph, analyst validation, and workspaces that keep a globe, an analyst workstation and sensor consoles in sync. Generated Dart, TypeScript and Python SDKs and a command-line client come with it.
- **Agents are first-class:** a built-in MCP server exposes the data and watch management as tools for OpenClaw, Claude and any MCP client, with remote authorization through the operator's own identity provider. Scoped tokens keep agents within limits.
- **Anything can push in:** custom sources accept records through an inbound webhook, from local SDRs, Home Assistant, phones (OwnTracks and others, as private sources), scripts or agents.

All records are **versioned** in PostgreSQL + PostGIS, so every client gets current state, history and playback. **All configuration happens through the API:** sources, credentials, budgets, retention, triggers, channels, tokens, LLM providers and briefings.

## Who it is for

Individuals: OSINT hobbyists, analysts, researchers, makers and developers who want to own their data feed. It runs as an always-on service on a home server, or as something you `docker compose up` when you want it.

## Scope

| In scope | Not in scope |
|---|---|
| Ingest, normalize, version and re-serve public telemetry (105 adapter packages; every source disabled until the operator enables it) | Any UI inside the broker (frontends are separate repositories) |
| Per-source scheduling, budgets, health and an adapter doctor; inbound push sources; private sources | Fringe and specialist sources inside the core (they are external EDP providers) |
| Trigger engine with windowed rules; 19 notification channel types including Web Push, MQTT and LoRa mesh | Multi-user accounts with logins (scoped API tokens and named principals instead) |
| REST, GeoJSON, vector and raster tiles, SSE, MCP, standards outputs, exports and sinks; OpenAPI-first; Postman collection; SDKs and CLI | Horizontal scale-out (single instance by design; instances can federate) |
| Optional LLM: Ask, briefings, enrichment, semantic search, analyst, adapter doctor, translation and speech | Training or fine-tuning neural models |
| Intelligence layer: discovery, situations, base rates, rolling hypotheses, forecast ledger, assessments | Analysis of private individuals; accusations |
| Derived layers, self-rendered radar, satellite and model products, license and provenance tracking | Proxying basemaps or third-party tiles (Foresignal hosts only products derived from data it ingests; GIBS and nowCOAST are catalog entries clients fetch directly) |

## Key decisions

| Decision | Choice |
|---|---|
| Language and framework | Java 21 (virtual threads), Spring Boot 4.1 |
| Store | PostgreSQL 17 + PostGIS 3.5 + pgvector; schema owned by Flyway migrations, validated by Hibernate |
| LLM | Optional. Anthropic via the official Java SDK (default `claude-opus-5-5`), OpenAI-compatible APIs, or local Ollama; budgets on every call |
| Agents | Built-in MCP server (streamable HTTP); scoped tokens (`read` < `agent` < `admin`) |
| Packaging | Single Gradle module; Docker Compose (broker + database, with optional Tailscale, Caddy web, local-LLM and SatDump profiles) |
| API stability | `/v1` additive only; the v1.0.0 spec is frozen and checked with oasdiff |
| License | MIT |
| Reuse | God's Eye View (MIT) code may be ported with notice; ShadowBroker, Velocity and World Monitor (AGPL), SatDump (GPL) and the Meshtastic protobufs are reference only |

## Differentiators

1. **Subscription decoupled from display.** Watch quietly; alert loudly, wherever you are.
2. **Per-source budgets and health.** A good upstream citizen by design, and self-diagnosing when an adapter breaks.
3. **Versioned history you own,** with retention you set and playback at any past instant.
4. **API as the product.** One contract, many frontends, plus MCP for agents and standards outputs for existing tools.
5. **Intelligence without lock-in.** Frontier or local models, with citations checked against real records and model actions gated by scope.
6. **Built for an AI maintainer.** Health API, raw-fetch probes, fixture tests and an LLM adapter doctor.
7. **Analysis that measures itself.** Versioned history, leak-free replays, base rates on day one, rolling hypotheses and a scored forecast ledger. Every comparable open-source tool is AGPL; Foresignal is MIT.

## How it was built

A single ordered list of 144 prompts in the [Roadmap](Roadmap.md), each executed by Claude Code and closed with a completion report:

1. foundation, core pipeline and core API (001–022)
2. tokens, inbound webhooks, MCP, the LLM features, push, places, privacy and the gazetteer (023–041)
3. weather, imagery and SatDump (042–057)
4. interop and about 100 sources (058–097)
5. derived layers, outputs, federation and alert quality (098–115)
6. the intelligence layer, the client contract, collaboration, chronicles, reports, news, social signals and external data providers (116–139)
7. the adapter doctor, SDKs and CLI, the full test run, runtime verification and production hardening (140–144)

See the [Product Charter](Product-Charter.md), [Architecture](Architecture.md), [OpenAPI spec](OpenAPI.json), [Source Catalog](Source-Catalog.md) and [Release Notes](Release-Notes-v1.0.0.md).
