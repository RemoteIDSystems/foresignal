# Foresignal — Executive Summary

**Foresignal** (foresignal.ai) is an open-source, self-hosted **intelligence broker**. It is a headless service that continuously collects public telemetry from many upstream feeds, normalizes it into one versioned store, alerts on what matters, and re-serves everything through one documented API, an MCP server for AI agents, and push notifications. An optional LLM, either a frontier model or one running locally, turns that data into answers, briefings and context. Foresignal is the data foundation for a family of frontends: a 3D globe, a 2D analyst map, an operator console, mobile apps and agents.

## The problem

Every existing open-source "god's-eye" project (ShadowBroker, God's Eye View, Velocity, World Monitor) fuses three concerns into one application:

1. **Subscription:** what data is collected, how often, and at what cost to upstreams.
2. **Display:** what a particular screen shows.
3. **Alerting:** what deserves attention.

Because these are fused, turning a layer off stops collecting it, so you can't watch quietly. Where they use AI, it summarises the present: risk indices rest on hand-set weights, correlations are computed and discarded, briefings forget last week, and no one measures whether a forecast came true. History lives only as long as a browser tab or a short position cache. Each project re-implements the same 40–100 integrations inside a UI, with little accounting of upstream load. ShadowBroker was formally called out by the Meshtastic project for generating "1–2 orders of magnitude more traffic than typical clients."

## The solution

Foresignal separates the three concerns, and adds an intelligence layer on top:

- **Subscription is server-side** and is the only thing that causes fetching. Each source is enabled, scheduled and **budgeted** through the API, so the broker always knows its spend per upstream and never exceeds it.
- **Display is the client's business.** Frontends use filtered queries, vector tiles and filtered live streams, and their choices never reach the collector.
- **Alerting is a trigger engine.** It runs over the change stream (threshold, geofence, enter/exit) and delivers to Slack, Mattermost, Discord, Teams, Google Chat, Telegram, Matrix, ntfy, Gotify, Pushover, email, webhooks and browser Web Push. A subscribed but hidden layer can still announce itself.
- **Intelligence is optional and budgeted:**
  - Ask questions in plain language and get answers with verified citations.
  - Schedule morning briefings.
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
- **Built for teams and many instances:** named people and groups, sharing under TLP labels, tags, comments, graded evidence, investigations, **chronicles** (saved monitoring threads that keep their own replayable history), and reports sent to colleagues here or as signed packages to other Foresignal instances.
- **News coverage, not just news:** stories clustered across outlets, with lean, factuality and ownership from licensed rating providers, blindspots and coverage balance used as evidence-quality signals.
- **What people say versus what sensors see:** public social media (Bluesky, Mastodon, Reddit, YouTube, X, Threads and Instagram as the operator chooses, plus any platform an external provider brings) becomes narratives and claims counted by independent origin, compared with physical observations in both directions (corroborated with measured lead time, unobserved despite coverage, events with suspiciously little attention), with image-reuse and coordination checks.
- **Bring any source:** the [External Data Protocol](External-Data-Protocol.md) lets anyone write a data source as a separate project (web maps, scrapers, Telegram channels, prediction markets, niche feeds). Providers describe themselves with a manifest, deliver layers or atomic items, and the operator binds atomic streams to series, evidence, indicators, forecasts or chronicles, without changing Foresignal.
- **One contract for every frontend** ([Client Contract](Client-Contract.md)): shared object references, a relation graph, analyst validation, and workspaces that keep a globe, an analyst workstation and sensor consoles in sync.
- **Agents are first-class:** a built-in MCP server exposes the data and watch management as tools for OpenClaw, Claude and any MCP client. Scoped tokens keep agents within limits.
- **Anything can push in:** custom sources accept records through an inbound webhook, from local SDRs, Home Assistant, scripts or agents.

All records are **versioned** in PostgreSQL + PostGIS, so every client gets current state, history and playback. **All configuration happens through the API:** sources, credentials, budgets, retention, triggers, channels, tokens, LLM providers and briefings.

## Who it is for

Individuals: OSINT hobbyists, analysts, researchers, makers and developers who want to own their data feed. It runs as an always-on service on a home server, or as something you `docker compose up` when you want it.

## Scope

| In scope | Not in scope |
|---|---|
| Ingest, normalize, version and re-serve public telemetry (~100 sources) | Any UI inside the broker (frontends are separate repositories) |
| Per-source scheduling, budgets, health; inbound push sources | Fringe and specialist sources inside the core (they are external EDP providers) |
| Trigger engine; 13 notification channel types including Web Push | Multi-user accounts with logins (scoped API tokens instead) |
| REST, GeoJSON, vector tiles, SSE, MCP; OpenAPI-first; Postman collection | Horizontal scale-out (single instance by design) |
| Optional LLM: Ask, briefings, enrichment, semantic search, adapter doctor | Training or fine-tuning neural models |
| Intelligence layer: discovery, situations, base rates, rolling hypotheses, forecast ledger, assessments | Analysis of private individuals; accusations |
| Derived layers, gridded weather products, licence and provenance tracking | Hosting basemaps or imagery (clients fetch tiles directly) |

## Key decisions

| Decision | Choice |
|---|---|
| Language and framework | Java 21 (virtual threads), Spring Boot 4.1 |
| Store | PostgreSQL + PostGIS + pgvector; Hibernate-managed schema in development, Flyway for production |
| LLM | Optional. Anthropic via the official Java SDK (default `claude-opus-5-5`), OpenAI-compatible APIs, or local Ollama; budgets on every call |
| Agents | Built-in MCP server (streamable HTTP); scoped tokens (`read` < `agent` < `admin`) |
| Packaging | Single Gradle module; Docker Compose (broker + database, optional local-LLM profile) |
| Licence | MIT |
| Reuse | God's Eye View (MIT) code may be ported with notice; ShadowBroker, Velocity and World Monitor (AGPL) are reference only |

## Differentiators

1. **Subscription decoupled from display.** Watch quietly; alert loudly, wherever you are.
2. **Per-source budgets and health.** A good upstream citizen by design, and self-diagnosing when an adapter breaks.
3. **Versioned history you own,** with retention you set.
4. **API as the product.** One contract, many frontends, plus MCP for agents.
5. **Intelligence without lock-in.** Frontier or local models, with citations checked against real records and model actions gated by scope.
6. **Built for an AI maintainer.** Health API, raw-fetch probes, fixture tests and an LLM adapter doctor.
7. **Analysis that measures itself.** Years of versioned history, leak-free replays, base rates on day one, rolling hypotheses and a scored forecast ledger. Every comparable open-source tool is AGPL; Foresignal is MIT.

## Plan

A single ordered list of 143 prompts in the [Roadmap](Roadmap.md), in this order:

1. foundation and core API
2. channels, tokens, inbound webhooks, MCP and the LLM features
3. about 100 sources
4. derived layers, gridded products and provenance
5. the intelligence layer: evidence independence and observability, discovery, situations, base rates, the forecast ledger, a benchmark harness, hypotheses, analysis cycles, the client contract, collaboration, chronicles, reports, news coverage, social signals and external data providers
6. the full test run, runtime verification and production hardening

See the [Product Charter](Product-Charter.md), [Architecture](Architecture.md), [OpenAPI spec](OpenAPI.json) and [Source Catalog](Source-Catalog.md).
